CREATE TABLE organization_settings
(
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL UNIQUE,
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    date_format VARCHAR(50) NOT NULL DEFAULT 'YYYY-MM-DD',
    allow_external_sharing BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_required BOOLEAN NOT NULL DEFAULT FALSE,
    max_projects INTEGER NOT NULL DEFAULT 100,
    max_users INTEGER NOT NULL DEFAULT 500,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,

    CONSTRAINT fk_org_settings_org
        FOREIGN KEY (organization_id)
            REFERENCES organizations(id)
            ON DELETE CASCADE
);
