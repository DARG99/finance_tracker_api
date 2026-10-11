-- Existing users must choose their payday; do not guess it from transaction descriptions.
ALTER TABLE users ADD COLUMN payday_day INTEGER
    CHECK (payday_day BETWEEN 1 AND 31);
