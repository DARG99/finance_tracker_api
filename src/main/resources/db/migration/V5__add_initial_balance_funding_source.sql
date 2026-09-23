ALTER TABLE funding_sources
    ADD COLUMN initial_balance NUMERIC(14,2) NOT NULL DEFAULT 0.00;