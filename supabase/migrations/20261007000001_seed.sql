-- Demo catalogue — the single source of seed data for local and hosted databases.
--
-- It lives in a migration rather than `seed.sql` on purpose: `supabase start`,
-- `db reset` and the hosted project all go through `supabase/migrations`, so the one
-- file below is what every environment runs. Every insert is idempotent because
-- migrations can be replayed against an existing database.

insert into public.categories (id, name, image_url) values
    ('phones', 'Phones', 'https://placehold.co/96x96/141414/A5E800?text=ph'),
    ('laptops', 'Laptops', 'https://placehold.co/96x96/141414/A5E800?text=lp'),
    ('audio', 'Audio', 'https://placehold.co/96x96/141414/A5E800?text=au'),
    ('wearables', 'Wearables', 'https://placehold.co/96x96/141414/A5E800?text=wr')
on conflict (id) do nothing;

insert into public.products (id, title, description, category, price, actual_price, images) values
    ('p-aurora-x', 'Aurora X 5G', '6.7" AMOLED, 108MP camera, 5000mAh battery.',
     'phones', '34999', '39999',
     '["https://placehold.co/600x600/141414/A5E800?text=Aurora+X"]'),
    ('p-aurora-lite', 'Aurora Lite', '6.4" LCD, 50MP camera, all-day battery.',
     'phones', '16999', '19999',
     '["https://placehold.co/600x600/141414/A5E800?text=Aurora+Lite"]'),
    ('p-nimbus-14', 'Nimbus 14 Ultrabook', '14" 2.8K OLED, 16GB RAM, 1TB SSD.',
     'laptops', '78990', '89990',
     '["https://placehold.co/600x600/141414/A5E800?text=Nimbus+14"]'),
    ('p-echo-buds', 'Echo Buds Pro', 'Active noise cancellation, 30h total playback.',
     'audio', '4999', '7999',
     '["https://placehold.co/600x600/141414/A5E800?text=Echo+Buds"]'),
    ('p-pulse-watch', 'Pulse Watch 2', 'AMOLED always-on display, 14-day battery.',
     'wearables', '8999', '12999',
     '["https://placehold.co/600x600/141414/A5E800?text=Pulse+Watch"]')
on conflict (id) do nothing;

insert into public.banners (urls)
select '["https://placehold.co/800x300/141414/A5E800?text=New+arrivals"]'::jsonb
where not exists (select 1 from public.banners);
