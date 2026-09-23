ALTER TABLE users
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

ALTER TABLE users
    ADD CONSTRAINT users_role_valid
        CHECK (role IN ('USER', 'ADMIN'));