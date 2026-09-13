CREATE TABLE IF NOT EXISTS notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    in_app_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    task_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    sprint_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    resource_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_notification_pref_org_user UNIQUE (organization_id, user_id)
);

CREATE INDEX idx_notification_pref_org_user ON notification_preferences(organization_id, user_id);
