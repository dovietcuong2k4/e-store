-- Order status migration to new workflow
-- CONFIRMED -> PROCESSING
-- PREPARING -> PROCESSING
-- PENDING -> CREATED (legacy pre-processing state)

UPDATE orders
SET status = 'PROCESSING'
WHERE status IN ('CONFIRMED', 'PREPARING');

UPDATE orders
SET status = 'CREATED'
WHERE status = 'PENDING';
