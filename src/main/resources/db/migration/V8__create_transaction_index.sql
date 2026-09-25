CREATE INDEX idx_transactions_user_date_created
    ON transactions(user_id, transaction_date DESC, created_at DESC);

ALTER TABLE subscription_payments
    DROP CONSTRAINT subscription_payments_transaction_id_fkey;

ALTER TABLE subscription_payments
    ADD CONSTRAINT subscription_payments_transaction_id_fkey
        FOREIGN KEY (transaction_id)
            REFERENCES transactions(id)
            ON DELETE CASCADE;