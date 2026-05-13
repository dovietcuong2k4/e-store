ALTER TABLE review_ai_summaries
    ADD COLUMN external_summary TEXT,
    ADD COLUMN external_sources TEXT,
    ADD COLUMN external_generated_at DATETIME;

