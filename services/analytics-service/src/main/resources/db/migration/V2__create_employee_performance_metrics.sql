CREATE TABLE IF NOT EXISTS employee_performance_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    tasks_assigned INT NOT NULL DEFAULT 0,
    tasks_completed INT NOT NULL DEFAULT 0,
    tasks_overdue INT NOT NULL DEFAULT 0,
    story_points_delivered INT NOT NULL DEFAULT 0,
    on_time_completion_rate NUMERIC(5, 2) NOT NULL DEFAULT 100.00,
    performance_score NUMERIC(5, 2) NOT NULL DEFAULT 100.00,
    performance_rating VARCHAR(30) NOT NULL DEFAULT 'EXCELLENT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_emp_perf_org_user UNIQUE (organization_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_emp_perf_org_id ON employee_performance_metrics(organization_id);
CREATE INDEX IF NOT EXISTS idx_emp_perf_user_id ON employee_performance_metrics(user_id);
