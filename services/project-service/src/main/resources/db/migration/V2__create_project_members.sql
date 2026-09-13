CREATE TABLE project_members
(
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,

    CONSTRAINT uq_project_member UNIQUE (project_id, user_id)
);

CREATE INDEX idx_proj_member_proj_id ON project_members(project_id);
CREATE INDEX idx_proj_member_org_id ON project_members(organization_id);
CREATE INDEX idx_proj_member_user_id ON project_members(user_id);
CREATE INDEX idx_proj_member_role ON project_members(role);
