CREATE TABLE IF NOT EXISTS users
(
    id UUID PRIMARY KEY,

    first_name VARCHAR(255) NOT NULL,

    last_name VARCHAR(255) NOT NULL,

    email VARCHAR(255) NOT NULL UNIQUE,

    password VARCHAR(255) NOT NULL,

    status VARCHAR(50) NOT NULL,

    account_non_locked BOOLEAN NOT NULL,

    enabled BOOLEAN NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP
    );