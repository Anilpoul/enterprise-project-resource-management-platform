ALTER TABLE departments
    ADD COLUMN created_by VARCHAR(255);

ALTER TABLE departments
    ADD COLUMN updated_by VARCHAR(255);

UPDATE departments
SET
    created_by='SYSTEM',
    updated_by='SYSTEM';

ALTER TABLE departments
    ALTER COLUMN created_by SET NOT NULL;

ALTER TABLE departments
    ALTER COLUMN updated_by SET NOT NULL;

ALTER TABLE designations
    ADD COLUMN created_by VARCHAR(255);

ALTER TABLE designations
    ADD COLUMN updated_by VARCHAR(255);

UPDATE designations
SET
    created_by='SYSTEM',
    updated_by='SYSTEM';

ALTER TABLE designations
    ALTER COLUMN created_by SET NOT NULL;

ALTER TABLE designations
    ALTER COLUMN updated_by SET NOT NULL;

ALTER TABLE user_profiles
    ADD COLUMN created_by VARCHAR(255);

ALTER TABLE user_profiles
    ADD COLUMN updated_by VARCHAR(255);

UPDATE user_profiles
SET
    created_by='SYSTEM',
    updated_by='SYSTEM';

ALTER TABLE user_profiles
    ALTER COLUMN created_by SET NOT NULL;

ALTER TABLE user_profiles
    ALTER COLUMN updated_by SET NOT NULL;