ALTER TABLE transactions
    ADD COLUMN transaction_nature VARCHAR(20) NOT NULL DEFAULT 'NORMAL'
        CHECK (transaction_nature IN ('NORMAL', 'REIMBURSEMENT'));

ALTER TABLE transactions
    ADD COLUMN reimbursement_for_transaction_id BIGINT
        REFERENCES transactions(id) ON DELETE RESTRICT;

ALTER TABLE transactions
    ADD CONSTRAINT reimbursement_must_be_income
        CHECK (
            transaction_nature = 'NORMAL'
                OR type = 'INCOME'
            );