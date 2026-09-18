-- Flyway migration: create funding_sources table

CREATE TABLE IF NOT EXISTS funding_sources (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_funding_sources_user_id ON funding_sources(user_id);

