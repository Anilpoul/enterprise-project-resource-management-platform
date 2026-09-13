CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(100),
    action VARCHAR(50) NOT NULL,
    performed_by VARCHAR(100),
    ip_address VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    details TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_org_timestamp ON audit_logs(organization_id, timestamp DESC);
CREATE INDEX idx_audit_logs_org_entity ON audit_logs(organization_id, entity_type, entity_id);
CREATE INDEX idx_audit_logs_org_user ON audit_logs(organization_id, performed_by);
CREATE INDEX idx_audit_logs_org_action ON audit_logs(organization_id, action);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp DESC);
