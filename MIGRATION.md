# Migrating WooCom from Firestore to Supabase

This is the working document for the backend swap. It is written the way an engineer
would write it for a team: what mapped to what, what changed in the client, and what the
security posture was before and after.

- **Before:** Firebase Auth + Cloud Firestore, 15 files calling `Firestore.collection()` directly.
- **After:** Supabase (Postgres + Auth + RLS + Edge Functions), 2 repository implementations behind
  the same interfaces the UI already depends on.

---

## 1. Collection → table mapping

| Firestore | Supabase | Notes |
| --- | --- | --- |
| `data/stock/products` | `public.products` | `id` is a text business key in both. `actualPrice` → `actual_price`, `otherDetails` → `other_details` (jsonb). The collection's ad-hoc category *field* becomes an indexed column. |
| `data/stock/categoties` | `public.categories` | The misspelling (`categoties`) is fixed in SQL; the app never sees either name. |
| `data/banner` (`urls` array) | `public.banners.urls` (jsonb) | One row per banner row. |
| `user/{uid}` document | `public.profiles` | `id` now references `auth.users`, so there is exactly one row per account by construction — the Firestore code needed `document(uid)` vs `whereEqualTo("userId", uid)` disambiguation. |
| `user/{uid}.cartItems` map | `public.cart_items` | `Map<productId, qty>` → `(user_id, product_id, quantity)` rows with a `quantity > 0` check. |
| `user/{uid}.favorites` map | `public.favorites` | `Map<productId, true>` → existence of a row. |
| `orders` collection | `public.orders` | `items` stays jsonb (`{productId: qty}`) for a 1:1 mapping with `OrderModel`; per-line pricing would justify a separate `order_items` table later. |
| *(none)* | `public.profiles` trigger | `on_auth_user_created` creates the profile row — replaces the client write at sign-up. |

## 2. What the security model changes

### Before (Firestore, as deployed)

```text
client  ──write──►  user/{uid}.cartItems     (app must remember to scope queries by uid)
client  ──write──►  orders                   (client sets amount, status, paymentId)
client  ──write──►  data/stock/products      (guarded only by console-side rules)
```

The amount was computed on the device and the payment status was written by the device.
Rules could be tightened, but the *client still chose the numbers*.

### After (Postgres RLS)

| Table | Who can read | Who can write |
| --- | --- | --- |
| `products`, `categories`, `banners` | anyone | nobody (no write policies exist) |
| `profiles` | own row (`auth.uid() = id`) | own row, `update` only — `insert` happens in the trigger |
| `cart_items`, `favorites` | own rows | own rows |
| `orders` | own rows | `insert` **only when `status = 'pending'`**; no `update` policy at all |

The `orders` table is the important one. Clients have no UPDATE policy, so the two status
transitions go through functions instead:

- `paid` — only the `verify-payment` Edge Function, using the service-role key, after it
  recomputes the Razorpay HMAC from the key secret (`mark_order_paid` is granted to
  `service_role` alone).
- `failed` — only the `mark_order_failed` RPC, which is `security definer`, re-checks
  `auth.uid()` and refuses to touch any order that is not still `pending`.

A modified APK can therefore neither name its own price nor promote its own order.

## 3. Payment path

```text
before                                          after
----                                            ----
CheckoutPage                                    paymentGateway.beginCheckout()
  ├─ writes orders(status='pending') ──┐         └─ create-razorpay-order (Edge Function)
  └─ totalAmount computed on device    │            ├─ reads cart_items, prices from products
Razorpay Checkout                      │            ├─ POST razorpay /v1/orders (amount in paise)
MainActivity.onPaymentSuccess          │            └─ inserts orders(status='pending')
  ├─ markOrderPaid(orderId)            │
  └─ clears cart                       │         Razorpay Checkout
                                       │            └─ returns payment_id + order_id + signature
                                       ▼
                                     verify-payment (Edge Function)
                                       ├─ HMAC-SHA256(order|payment, KEY_SECRET) compare
                                       ├─ rpc mark_order_paid(...)  -- status='paid', cart cleared
                                       └─ returns ok
```

The client never sends an amount, and it cannot mark an order paid. `MainActivity`
implements Razorpay's `PaymentResultWithDataListener` (not `PaymentResultListener`) —
the SDK checks the plain listener first and short-circuits to it, which would drop the
`PaymentData` carrying `razorpay_signature` that `verify-payment` needs.

## 4. What changed in the Android client

Almost nothing in `screens/`, `components/` or the list/search/detail pages — that was the
point of introducing the repository interfaces first. Only the two entry points that were
still backend-aware moved:

| File | Change |
| --- | --- |
| `data/PaymentGateway.kt` | new — `FirestorePaymentGateway` (writes its own pending document) and `SupabasePaymentGateway` (invokes `create-razorpay-order` / `verify-payment`, then the `mark_order_failed` RPC) |
| `data/ServiceLocator.kt` | picks all four dependencies from `BuildConfig.SUPABASE_URL` (falls back to Firestore when unset) |
| `data/ProductRepository.kt` | + `SupabaseProductRepository` — search is now a server-side `ILIKE`, batched ids are one `in (...)` |
| `data/UserRepository.kt` | + `SupabaseUserRepository` (cart/favourites as row upserts, `add_to_cart` RPC for atomic increments); order *writes* moved out to `PaymentGateway` |
| `data/AuthRepository.kt` | new — email/password sign-in, sign-up, session, sign-out for both backends |
| `model/*` | annotated `@Serializable` with `@SerialName` snake_case mappings; `IsoDateTimeAsEpochMillis` and `AnyMapAsJsonObject` bridge the two shapes |
| `viewmodel/AuthViewModel.kt` | talks to `AuthRepository` instead of `FirebaseAuth` |
| `pages/CheckoutPage.kt` | hands the cart to `PaymentGateway.beginCheckout()` and passes a server `order_id` to Razorpay when there is one |
| `pages/ProfilePage.kt`, `screens/SplashScreen.kt` | sign-out / session check go through `AuthRepository` |
| `MainActivity.kt` | settles or fails the pending session through `PaymentGateway` from the Razorpay callback |

## 5. Latency / cost

Not measurable until the project has real traffic; the honest statement today is
structural: the home screen went from 7 reads per refresh to 3, cart rows are batched
into one `whereIn`, and per-row N+1 product lookups are gone. Numbers belong here once a
Supabase project is running the app.

## 6. Running it locally

```bash
supabase start                 # Docker; applies supabase/migrations/*.sql then seed.sql
supabase functions serve       # loads .env with RAZORPAY_KEY_ID / RAZORPAY_KEY_SECRET
```

Then supply the project URL and anon key to Gradle (they are *public* identifiers — the
secret is the RLS, not the URL):

```properties
# gradle.properties (local only, or CI secret)
SUPABASE_URL=http://127.0.0.1:54321
SUPABASE_ANON_KEY=eyJ...
```

Leave them blank to keep running against Firestore — the two backends coexist behind the
same interfaces precisely so the switch is reversible.

## 7. Open follow-ups

- Export the live Firestore catalogue into `seed.sql` (the current seed is a stand-in).
- Move `products.price` from display strings to `numeric` once the UI formats from paise.
- `order_items` table if line-level pricing/discounts appear.
- Storage bucket for product images (currently remote URLs).
- Measured before/after latency for section 5.
