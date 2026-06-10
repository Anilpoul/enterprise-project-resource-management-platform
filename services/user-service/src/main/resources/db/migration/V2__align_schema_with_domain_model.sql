ALTER TABLE departments
    ADD COLUMN status VARCHAR(50);

UPDATE departments
SET status = 'ACTIVE';

ALTER TABLE departments
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE designations
    ADD COLUMN level INTEGER;

UPDATE designations
SET level = 1;

ALTER TABLE designations
    ALTER COLUMN level SET NOT NULL;

ALTER TABLE designations
    ADD COLUMN status VARCHAR(50);

UPDATE designations
SET status = 'ACTIVE';

ALTER TABLE designations
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE user_profiles
    ADD COLUMN status VARCHAR(50);

UPDATE user_profiles
SET status = 'ACTIVE';

ALTER TABLE user_profiles
    ALTER COLUMN status SET NOT NULL;