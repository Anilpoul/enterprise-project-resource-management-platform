
-- ROLE_TEAM_MEMBER

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p
              ON p.name IN (
                            'PROFILE_VIEW',
                            'PROFILE_UPDATE'
                  )
WHERE r.name = 'ROLE_TEAM_MEMBER'
    ON CONFLICT DO NOTHING;

-- ROLE_TEAM_LEAD

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p
              ON p.name IN (
                            'PROFILE_VIEW',
                            'PROFILE_UPDATE',
                            'TEAM_VIEW',
                            'TEAM_ASSIGN',
                            'TASK_CREATE',
                            'TASK_UPDATE',
                            'TASK_VIEW'
                  )
WHERE r.name = 'ROLE_TEAM_LEAD'
    ON CONFLICT DO NOTHING;


-- ROLE_MANAGER

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p
              ON p.name IN (
                            'PROFILE_VIEW',
                            'PROFILE_UPDATE',
                            'TEAM_VIEW',
                            'TEAM_ASSIGN',
                            'TASK_CREATE',
                            'TASK_UPDATE',
                            'TASK_VIEW',
                            'PROJECT_VIEW',
                            'PROJECT_ASSIGN',
                            'USER_VIEW',
                            'USER_UPDATE'
                  )
WHERE r.name = 'ROLE_MANAGER'
    ON CONFLICT DO NOTHING;