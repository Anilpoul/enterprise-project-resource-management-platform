CREATE TABLE IF NOT EXISTS project_task_sequences (
    project_id UUID PRIMARY KEY,
    project_key VARCHAR(20) NOT NULL,
    current_sequence BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
