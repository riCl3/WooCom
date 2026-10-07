-- Local development seed: a small catalogue so `supabase start` + the app gives a
-- populated home screen without needing to import the Firestore export first.
--
--   supabase db reset   # applies migrations, then this file

insert into public.categories (id, name, image_url) values
    ('phones', 'Phones', 'https://placehold.co/96x96/141414/A5E800?text=ph'),
    ('laptops', 'Laptops', 'https://placehold.co/96x96/141414/A5E800?text=lp'),
    ('audio', 'Audio', 'https://placehold.co/96x96/141414/A5E800?text=au'),
    ('wearables', 'Wearables', 'https://placehold.co/96x96/141414/A5E800?text=wr');

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
     '["https://placehold.co/600x600/141414/A5E800?text=Pulse+Watch"]');

insert into public.banners (urls) values
    ('["https://placehold.co/800x300/141414/A5E800?text=New+arrivals"]'
     ::jsonb);
