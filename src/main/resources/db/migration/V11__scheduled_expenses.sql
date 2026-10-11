ALTER TABLE users ADD COLUMN timezone VARCHAR(100) NOT NULL DEFAULT 'Europe/Lisbon';
ALTER TABLE transactions ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'POSTED'
    CHECK (status IN ('SCHEDULED', 'POSTED'));
ALTER TABLE transactions ADD COLUMN subscription_id BIGINT REFERENCES subscriptions(id);
ALTER TABLE transactions ADD COLUMN occurrence_date DATE;
ALTER TABLE transactions ADD CONSTRAINT transaction_occurrence_unique UNIQUE (subscription_id, occurrence_date);
ALTER TABLE transactions ADD CONSTRAINT transaction_occurrence_pair CHECK (
    (subscription_id IS NULL) = (occurrence_date IS NULL));
ALTER TABLE transactions ADD CONSTRAINT scheduled_expense_only CHECK (status <> 'SCHEDULED' OR type = 'EXPENSE');
UPDATE transactions t SET subscription_id = p.subscription_id, occurrence_date = p.scheduled_for
FROM subscription_payments p WHERE p.transaction_id = t.id;
CREATE INDEX idx_transactions_scheduled ON transactions(user_id, transaction_date) WHERE status = 'SCHEDULED';
-- Retain the occurrence receipt after deleting a posted transaction, preventing replay.
ALTER TABLE subscription_payments ALTER COLUMN transaction_id DROP NOT NULL;
