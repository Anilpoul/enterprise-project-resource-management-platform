CREATE TABLE IF NOT EXISTS resource_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    job_title VARCHAR(150) NOT NULL,
    weekly_capacity_hours NUMERIC(5, 2) NOT NULL DEFAULT 40.00,
    skills TEXT,
    hourly_rate NUMERIC(10, 2),
    currency VARCHAR(10) DEFAULT 'USD',
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_resource_profiles_org_user UNIQUE (organization_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_resource_profiles_org_id ON resource_profiles(organization_id);
CREATE INDEX IF NOT EXISTS idx_resource_profiles_user_id ON resource_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_resource_profiles_status ON resource_profiles(status);
