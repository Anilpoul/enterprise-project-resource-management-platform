CREATE TABLE IF NOT EXISTS permissions
(
    id UUID PRIMARY KEY,

    name VARCHAR(255) NOT NULL UNIQUE,

    description VARCHAR(500),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP
    );