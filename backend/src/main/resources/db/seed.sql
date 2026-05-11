-- Seed data for Pet Shop — run after backend starts (ddl-auto=create-drop recreates schema on startup)
-- Usage: psql -U petshop_user -d petshop -f backend/src/main/resources/db/seed.sql

INSERT INTO category (name, description) VALUES
    ('Dogs',  'Everything your canine companion needs'),
    ('Cats',  'Products for your feline friends'),
    ('Birds', 'Supplies for pet birds of all kinds'),
    ('Fish',  'Aquarium essentials and aquatic life');

INSERT INTO product (name, description, price, category_id, image_url, available, created_at) VALUES
    ('Premium Dog Food 5kg',     'High-protein dry kibble with real chicken, balanced for adult dogs.',          29.99,
     (SELECT id FROM category WHERE name = 'Dogs'),
     'https://images.unsplash.com/photo-1568640347023-a616a30bc3bd?w=400', true, NOW()),

    ('Dog Chew Toy Set',         'Durable rubber chew toys in three sizes — great for dental health.',           12.49,
     (SELECT id FROM category WHERE name = 'Dogs'),
     'https://images.unsplash.com/photo-1601758125946-6ec2ef64daf8?w=400', true, NOW()),

    ('Adjustable Dog Harness',   'No-pull harness with reflective stitching, fits dogs 10–35 kg.',              24.99,
     (SELECT id FROM category WHERE name = 'Dogs'),
     'https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400', true, NOW()),

    ('Premium Cat Food 3kg',     'Grain-free wet food formula with real tuna, ideal for indoor cats.',          19.99,
     (SELECT id FROM category WHERE name = 'Cats'),
     'https://images.unsplash.com/photo-1589883661923-6476cb0ae9f2?w=400', true, NOW()),

    ('Interactive Cat Wand',     'Feather and bell wand toy that keeps cats mentally stimulated.',               8.99,
     (SELECT id FROM category WHERE name = 'Cats'),
     'https://images.unsplash.com/photo-1545249390-6bdfa286032f?w=400', true, NOW()),

    ('Self-Cleaning Litter Box', 'Automatic sifting mechanism, odor-seal lid, fits standard-size litters.',    59.99,
     (SELECT id FROM category WHERE name = 'Cats'),
     'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400', true, NOW()),

    ('Parrot Cage Deluxe',       'Powder-coated steel cage with perches, feeder cups, and play top.',          149.99,
     (SELECT id FROM category WHERE name = 'Birds'),
     'https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=400', true, NOW()),

    ('Aquarium Starter Kit 40L', 'Complete kit: tank, filter, heater, LED light, and water conditioner.',       89.99,
     (SELECT id FROM category WHERE name = 'Fish'),
     'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400', true, NOW());
