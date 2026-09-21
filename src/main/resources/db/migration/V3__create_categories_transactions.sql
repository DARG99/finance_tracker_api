CREATE TYPE transaction_type AS ENUM (
    'INCOME',
    'EXPENSE',
    'TRANSFER'
);

CREATE TABLE categories (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            name VARCHAR(100) NOT NULL,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                            CONSTRAINT categories_name_per_user_unique
                                UNIQUE (user_id, name)
);

CREATE TABLE transactions (
                              id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

                              type transaction_type NOT NULL,
                              amount NUMERIC(14,2) NOT NULL CHECK (amount > 0),

                              source_funding_source_id BIGINT
                                  REFERENCES funding_sources(id),

                              destination_funding_source_id BIGINT
                                  REFERENCES funding_sources(id),

                              category_id BIGINT
                                  REFERENCES categories(id),

                              description TEXT,
                              transaction_date DATE NOT NULL DEFAULT CURRENT_DATE,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                              CONSTRAINT transaction_movement_is_valid CHECK (
                                  -- Expense: account → outside; must be categorised
                                  (
                                      type = 'EXPENSE'
                                          AND source_funding_source_id IS NOT NULL
                                          AND destination_funding_source_id IS NULL
                                          AND category_id IS NOT NULL
                                      )

                                      OR

                                      -- Income: outside → account; no category
                                  (
                                      type = 'INCOME'
                                          AND source_funding_source_id IS NULL
                                          AND destination_funding_source_id IS NOT NULL
                                          AND category_id IS NULL
                                      )

                                      OR

                                      -- Transfer: one of your accounts → another; no category
                                  (
                                      type = 'TRANSFER'
                                          AND source_funding_source_id IS NOT NULL
                                          AND destination_funding_source_id IS NOT NULL
                                          AND source_funding_source_id <> destination_funding_source_id
                                          AND category_id IS NULL
                                      )
                                  )
);