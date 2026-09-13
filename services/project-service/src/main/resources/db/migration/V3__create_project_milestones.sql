CREATE TABLE project_milestones
(
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    due_date DATE,
    status VARCHAR(50) NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

CREATE INDEX idx_milestone_proj_id ON project_milestones(project_id);
CREATE INDEX idx_milestone_org_id ON project_milestones(organization_id);
CREATE INDEX idx_milestone_status ON project_milestones(status);
