INSERT INTO roles
(
    id,
    name,
    description,
    created_at
)
VALUES
    (
        gen_random_uuid(),
        'ROLE_ADMIN',
        'System Administrator',
        NOW()
    ),
    (
        gen_random_uuid(),
        'ROLE_MANAGER',
        'Project Manager',
        NOW()
    ),
    (
        gen_random_uuid(),
        'ROLE_TEAM_LEAD',
        'Team Lead',
        NOW()
    ),
    (
        gen_random_uuid(),
        'ROLE_TEAM_MEMBER',
        'Team Member',
        NOW()
    )
    ON CONFLICT (name) DO NOTHING;