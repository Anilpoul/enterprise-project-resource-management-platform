CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id UUID PRIMARY KEY,

    token VARCHAR(1000) NOT NULL UNIQUE,

    user_id UUID NOT NULL,

    expiry_date TIMESTAMP NOT NULL,

    revoked BOOLEAN NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    CONSTRAINT fk_refresh_token_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    );