-- ============================================================================
-- V1__init_admin_schema.sql - Initial Schema for Admin & Moderation Service
-- ============================================================================

CREATE TABLE IF NOT EXISTS admin_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_username VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    target_entity VARCHAR(255),
    target_id BIGINT,
    details VARCHAR(1000),
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_logs_timestamp ON admin_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_admin_logs_admin ON admin_logs(admin_username);
