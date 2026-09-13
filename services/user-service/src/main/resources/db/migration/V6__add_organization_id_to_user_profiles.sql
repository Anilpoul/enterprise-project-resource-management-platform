ALTER TABLE user_profiles
    ADD COLUMN organization_id UUID;

CREATE INDEX idx_user_profile_org_id ON user_profiles(organization_id);
