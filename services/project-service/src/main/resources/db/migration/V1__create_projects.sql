CREATE TABLE projects
(
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    project_key VARCHAR(10) NOT NULL,
    description VARCHAR(2000),
    project_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    visibility VARCHAR(50) NOT NULL,
    lead_user_id UUID NOT NULL,
    start_date DATE,
    target_end_date DATE,
    actual_end_date DATE,
    budget NUMERIC(15, 2),
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,

    CONSTRAINT uq_org_project_key UNIQUE (organization_id, project_key),
    CONSTRAINT uq_org_project_name UNIQUE (organization_id, name)
);

CREATE INDEX idx_projects_org_id ON projects(organization_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_lead ON projects(lead_user_id);
CREATE INDEX idx_projects_key ON projects(project_key);
