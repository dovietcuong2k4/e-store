ALTER TABLE products
    ADD COLUMN product_embedding LONGTEXT NULL;

-- Backfill existing rows with embeddings through a one-off application job after deployment.