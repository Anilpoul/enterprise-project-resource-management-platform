-- ============================================================
-- V10: Add User Management Permissions
-- ============================================================

-- Update the existing CHECK constraint so that the database
-- accepts both existing and newly introduced permissions.

ALTER TABLE permissions
DROP CONSTRAINT IF EXISTS permissions_name_check;

ALTER TABLE permissions
    ADD CONSTRAINT permissions_name_check
        CHECK (
            name IN (
                -- Existing permissions
                     'USER_CREATE',
                     'USER_UPDATE',
                     'USER_DELETE',

                     'PROJECT_CREATE',
                     'PROJECT_UPDATE',
                     'PROJECT_DELETE',

                     'TASK_CREATE',
                     'TASK_ASSIGN',
                     'TASK_UPDATE',

                     'KPI_VIEW',
                     'REPORT_VIEW',

                -- User/Profile management
                     'USER_VIEW',
                     'PROFILE_VIEW',
                     'PROFILE_UPDATE',
                     'USER_ASSIGN_MANAGER',

                -- Department management
                     'DEPARTMENT_CREATE',
                     'DEPARTMENT_UPDATE',
                     'DEPARTMENT_DELETE',

                -- Designation management
                     'DESIGNATION_CREATE',
                     'DESIGNATION_UPDATE',
                     'DESIGNATION_DELETE'
                )
            );

-- Insert newly introduced permissions.
-- ON CONFLICT prevents duplicate inserts if a permission
-- already exists.

INSERT INTO permissions (
    id,
    name,
    created_at,
    updated_at
)
VALUES
    (gen_random_uuid(), 'USER_VIEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'PROFILE_VIEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'PROFILE_UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'DEPARTMENT_CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'DEPARTMENT_UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'DEPARTMENT_DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'DESIGNATION_CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'DESIGNATION_UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'DESIGNATION_DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'USER_ASSIGN_MANAGER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT DO NOTHING;