INSERT INTO permissions
(
    id,
    name,
    description,
    created_at
)
VALUES
    (
        gen_random_uuid(),
        'USER_CREATE',
        'Create users',
        NOW()
    ),
    (
        gen_random_uuid(),
        'USER_UPDATE',
        'Update users',
        NOW()
    ),
    (
        gen_random_uuid(),
        'USER_DELETE',
        'Delete users',
        NOW()
    ),
    (
        gen_random_uuid(),
        'PROJECT_CREATE',
        'Create projects',
        NOW()
    ),
    (
        gen_random_uuid(),
        'PROJECT_UPDATE',
        'Update projects',
        NOW()
    ),
    (
        gen_random_uuid(),
        'PROJECT_DELETE',
        'Delete projects',
        NOW()
    ),
    (
        gen_random_uuid(),
        'TASK_CREATE',
        'Create tasks',
        NOW()
    ),
    (
        gen_random_uuid(),
        'TASK_ASSIGN',
        'Assign tasks',
        NOW()
    ),
    (
        gen_random_uuid(),
        'TASK_UPDATE',
        'Update tasks',
        NOW()
    ),
    (
        gen_random_uuid(),
        'KPI_VIEW',
        'View KPI data',
        NOW()
    ),
    (
        gen_random_uuid(),
        'REPORT_VIEW',
        'View reports',
        NOW()
    )
    ON CONFLICT (name) DO NOTHING;