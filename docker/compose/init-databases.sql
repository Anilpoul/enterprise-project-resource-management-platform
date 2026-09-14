-- ==============================================================================
-- Enterprise Platform Database Initialization Script
-- Automatically executed on first boot by PostgreSQL in docker-compose.
-- ==============================================================================

SELECT 'CREATE DATABASE organization_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'organization_db')\gexec

SELECT 'CREATE DATABASE project_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'project_db')\gexec

SELECT 'CREATE DATABASE task_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'task_db')\gexec

SELECT 'CREATE DATABASE sprint_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sprint_db')\gexec

SELECT 'CREATE DATABASE resource_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'resource_db')\gexec

SELECT 'CREATE DATABASE analytics_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'analytics_db')\gexec

SELECT 'CREATE DATABASE notification_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification_db')\gexec

SELECT 'CREATE DATABASE audit_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'audit_db')\gexec
