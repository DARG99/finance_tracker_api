CREATE TABLE subscriptions (
                               id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                               user_id BIGINT NOT NULL
                                   REFERENCES users(id) ON DELETE CASCADE,

                               funding_source_id BIGINT NOT NULL
                                   REFERENCES funding_sources(id),

                               category_id BIGINT NOT NULL
                                   REFERENCES categories(id),

                               name VARCHAR(100) NOT NULL,
                               amount NUMERIC(14,2) NOT NULL CHECK (amount > 0),

                               frequency VARCHAR(20) NOT NULL
                                   CHECK (frequency IN ('MONTHLY', 'YEARLY')),

                               billing_day SMALLINT NOT NULL
                                   CHECK (billing_day BETWEEN 1 AND 31),

                               next_payment_date DATE NOT NULL,
                               active BOOLEAN NOT NULL DEFAULT TRUE,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE subscription_payments (
                                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                       subscription_id BIGINT NOT NULL
                                           REFERENCES subscriptions(id),

                                       transaction_id BIGINT NOT NULL
                                           REFERENCES transactions(id),

                                       scheduled_for DATE NOT NULL,
                                       amount NUMERIC(14,2) NOT NULL CHECK (amount > 0),

                                       created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                       CONSTRAINT subscription_payment_once_per_due_date
                                           UNIQUE (subscription_id, scheduled_for),

                                       CONSTRAINT subscription_payment_one_transaction
                                           UNIQUE (transaction_id)
);

CREATE INDEX idx_subscriptions_due
    ON subscriptions (active, next_payment_date);