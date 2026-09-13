CREATE TABLE IF NOT EXISTS resource_allocations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    project_id UUID NOT NULL,
    allocation_percentage NUMERIC(5, 2) NOT NULL,
    allocated_hours_per_week NUMERIC(5, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_allocations_resource FOREIGN KEY (resource_id) REFERENCES resource_profiles(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_allocations_resource_id ON resource_allocations(resource_id);
CREATE INDEX IF NOT EXISTS idx_allocations_org_id ON resource_allocations(organization_id);
CREATE INDEX IF NOT EXISTS idx_allocations_project_id ON resource_allocations(project_id);
CREATE INDEX IF NOT EXISTS idx_allocations_status ON resource_allocations(status);
CREATE INDEX IF NOT EXISTS idx_allocations_dates ON resource_allocations(start_date, end_date);
