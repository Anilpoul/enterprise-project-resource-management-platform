CREATE TABLE organization_members
(
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,

    CONSTRAINT fk_org_member_org
        FOREIGN KEY (organization_id)
            REFERENCES organizations(id)
            ON DELETE CASCADE,

    CONSTRAINT uq_org_member
        UNIQUE (organization_id, user_id)
);

CREATE INDEX idx_org_member_org_id ON organization_members(organization_id);
CREATE INDEX idx_org_member_user_id ON organization_members(user_id);
CREATE INDEX idx_org_member_role ON organization_members(role);
