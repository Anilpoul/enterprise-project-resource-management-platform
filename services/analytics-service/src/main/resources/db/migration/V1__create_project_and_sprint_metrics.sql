CREATE TABLE IF NOT EXISTS project_metric_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    project_id UUID NOT NULL,
    total_tasks INT NOT NULL DEFAULT 0,
    completed_tasks INT NOT NULL DEFAULT 0,
    in_progress_tasks INT NOT NULL DEFAULT 0,
    blocked_tasks INT NOT NULL DEFAULT 0,
    overdue_tasks INT NOT NULL DEFAULT 0,
    total_story_points INT NOT NULL DEFAULT 0,
    completed_story_points INT NOT NULL DEFAULT 0,
    completion_rate NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    health_status VARCHAR(30) NOT NULL DEFAULT 'ON_TRACK',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_proj_metrics_org_proj UNIQUE (organization_id, project_id)
);

CREATE INDEX IF NOT EXISTS idx_proj_metrics_org_id ON project_metric_snapshots(organization_id);
CREATE INDEX IF NOT EXISTS idx_proj_metrics_project_id ON project_metric_snapshots(project_id);

CREATE TABLE IF NOT EXISTS sprint_metric_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    project_id UUID NOT NULL,
    sprint_id UUID NOT NULL,
    sprint_name VARCHAR(150),
    committed_story_points INT NOT NULL DEFAULT 0,
    completed_story_points INT NOT NULL DEFAULT 0,
    velocity NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    completion_rate NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    spillover_tasks_count INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_sprint_metrics_org_sprint UNIQUE (organization_id, sprint_id)
);

CREATE INDEX IF NOT EXISTS idx_sprint_metrics_org_id ON sprint_metric_snapshots(organization_id);
CREATE INDEX IF NOT EXISTS idx_sprint_metrics_project_id ON sprint_metric_snapshots(project_id);
CREATE INDEX IF NOT EXISTS idx_sprint_metrics_sprint_id ON sprint_metric_snapshots(sprint_id);
