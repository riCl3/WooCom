-- WooCom: initial Postgres schema + Row Level Security.
--
-- Replaces the Firestore collections documented in README.md. Design rules applied here:
--
--   1. The catalogue (products/categories/banners) is world-readable and write-protected
--      by the absence of INSERT/UPDATE/DELETE policies — the app has no admin surface.
--   2. Everything user-owned is keyed by auth.uid(), so a leaked anon key still cannot
--      read another user's cart, favourites or orders.
--   3. Orders are insertable but NOT updatable by clients: `pending` is written by the
--      app, `paid`/`failed` can only be written by the `verify-payment` Edge Function
--      running with the service-role key. This is the server-side money path.

-- ---------------------------------------------------------------------------
-- Catalogue
-- ---------------------------------------------------------------------------

create table public.products (
    id           text primary key,
    title        text not null,
    description  text not null default '',
    category     text not null default '',
    -- Prices are kept as text to match the existing backend's display strings; the
    -- app parses them in AppUtil.parsePrice. Casting to numeric is a migration TODO.
    price        text not null default '0',
    actual_price text not null default '0',
    images       jsonb not null default '[]'::jsonb,
    other_details jsonb not null default '{}'::jsonb
);

create index products_category_idx on public.products (category);

create table public.categories (
    id        text primary key,
    name      text not null,
    image_url text not null default ''
);

create table public.banners (
    id   bigint generated always as identity primary key,
    urls jsonb not null default '[]'::jsonb
);

-- ---------------------------------------------------------------------------
-- User-owned data
-- ---------------------------------------------------------------------------

create table public.profiles (
    id         uuid primary key references auth.users (id) on delete cascade,
    name       text not null default '',
    email      text not null default '',
    created_at timestamptz not null default now()
);

create table public.cart_items (
    user_id    uuid not null references public.profiles (id) on delete cascade,
    product_id text not null references public.products (id) on delete cascade,
    quantity   bigint not null check (quantity > 0),
    primary key (user_id, product_id)
);

create table public.favorites (
    user_id    uuid not null references public.profiles (id) on delete cascade,
    product_id text not null references public.products (id) on delete cascade,
    primary key (user_id, product_id)
);

create table public.orders (
    id            uuid primary key default gen_random_uuid(),
    user_id       uuid not null references public.profiles (id) on delete cascade,
    amount        double precision not null check (amount >= 0),
    item_count    integer not null default 0,
    status        text not null default 'pending'
                  check (status in ('pending', 'paid', 'failed')),
    payment_id    text not null default '',
    -- Razorpay's own order id (rzp_order_…), distinct from our uuid.
    razorpay_order_id text not null default '',
    failure_reason text not null default '',
    items         jsonb not null default '{}'::jsonb,
    created_at    timestamptz not null default now()
);

create index orders_user_idx on public.orders (user_id, created_at desc);

-- ---------------------------------------------------------------------------
-- Row Level Security
-- ---------------------------------------------------------------------------

alter table public.products enable row level security;
alter table public.categories enable row level security;
alter table public.banners enable row level security;
alter table public.profiles enable row level security;
alter table public.cart_items enable row level security;
alter table public.favorites enable row level security;
alter table public.orders enable row level security;

-- Catalogue: readable by everyone, writable by nobody (policies below are read-only).
create policy "catalogue is public" on public.products
    for select using (true);
create policy "categories are public" on public.categories
    for select using (true);
create policy "banners are public" on public.banners
    for select using (true);

-- Profiles: a user sees and edits only their own row. The row is created by the
-- handle_new_user trigger below, so clients only need select/update.
create policy "own profile is readable" on public.profiles
    for select using (auth.uid() = id);
create policy "own profile is editable" on public.profiles
    for update using (auth.uid() = id);

-- Cart and favourites: fully scoped to the signed-in user.
create policy "own cart is readable" on public.cart_items
    for select using (auth.uid() = user_id);
create policy "own cart is insertable" on public.cart_items
    for insert with check (auth.uid() = user_id);
create policy "own cart is updatable" on public.cart_items
    for update using (auth.uid() = user_id);
create policy "own cart is deletable" on public.cart_items
    for delete using (auth.uid() = user_id);

create policy "own favorites are readable" on public.favorites
    for select using (auth.uid() = user_id);
create policy "own favorites are insertable" on public.favorites
    for insert with check (auth.uid() = user_id);
create policy "own favorites are deletable" on public.favorites
    for delete using (auth.uid() = user_id);

-- Orders: the client may place and read its own orders, but there is deliberately no
-- UPDATE policy — status transitions belong to the service role in verify-payment.
create policy "own orders are readable" on public.orders
    for select using (auth.uid() = user_id);
create policy "own orders are insertable" on public.orders
    for insert with check (auth.uid() = user_id and status = 'pending');

-- ---------------------------------------------------------------------------
-- Profile auto-provisioning (matches the Firestore behaviour of writing a user doc
-- at sign-up, but without a client round-trip).
-- ---------------------------------------------------------------------------

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
    insert into public.profiles (id, email, name)
    values (
        new.id,
        new.email,
        coalesce(new.raw_user_meta_data ->> 'name', '')
    )
    on conflict (id) do nothing;
    return new;
end;
$$;

create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

create or replace function public.add_to_cart(p_product_id text)
returns void
language sql
security invoker
set search_path = public
as $$
    insert into public.cart_items as ci (user_id, product_id, quantity)
    values (auth.uid(), p_product_id, 1)
    on conflict (user_id, product_id)
    do update set quantity = ci.quantity + 1;
$$;

-- RLS runs as the caller (security invoker), so a signed-in user can only ever touch
-- their own row; anon gets nothing because auth.uid() is null and the insert check fails.
revoke execute on function public.add_to_cart(text) from anon;
grant execute on function public.add_to_cart(text) to authenticated;

-- ---------------------------------------------------------------------------
-- RPC used by the checkout Edge Function: atomically flips an order to paid and
-- empties the buyer's cart in a single transaction.
--
-- Takes the user id explicitly because the function runs as the service role
-- (security definer), where auth.uid() is null. It is granted to service_role only.
-- ---------------------------------------------------------------------------

create or replace function public.mark_order_paid(
    p_order_id uuid,
    p_payment_id text,
    p_user_id uuid
)
returns void
language plpgsql
security definer set search_path = public
as $$
begin
    update public.orders
       set status = 'paid',
           payment_id = p_payment_id,
           failure_reason = ''
     where id = p_order_id
       and user_id = p_user_id
       and status = 'pending';

    if not found then
        raise exception 'order % is not a pending order of %', p_order_id, p_user_id;
    end if;

    delete from public.cart_items where user_id = p_user_id;
end;
$$;

revoke execute on function public.mark_order_paid(uuid, text, uuid) from public, anon, authenticated;
grant execute on function public.mark_order_paid(uuid, text, uuid) to service_role;

-- ---------------------------------------------------------------------------
-- Failed checkout bookkeeping.
--
-- `orders` deliberately has no client UPDATE policy, so this security-definer function
-- is the only way an app can move an order to `failed`. It re-checks ownership with
-- auth.uid() and refuses to touch anything that is not still pending — the paid/failed
-- flip for a successful payment stays exclusive to verify-payment.
-- ---------------------------------------------------------------------------

create or replace function public.mark_order_failed(p_order_id uuid, p_reason text)
returns void
language plpgsql
security definer set search_path = public
as $$
begin
    if auth.uid() is null then
        raise exception 'not authenticated';
    end if;

    update public.orders
       set status = 'failed',
           failure_reason = left(p_reason, 200)
     where id = p_order_id
       and user_id = auth.uid()
       and status = 'pending';

    if not found then
        raise exception 'order % is not a pending order of %', p_order_id, auth.uid();
    end if;
end;
$$;

revoke execute on function public.mark_order_failed(uuid, text) from public, anon;
grant execute on function public.mark_order_failed(uuid, text) to authenticated;
