-- ============================================================================
-- V1__init_auth_schema.sql - Initial Schema for Auth & User Management Service
-- ============================================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    bio VARCHAR(500),
    rating DOUBLE PRECISION DEFAULT 5.0,
    rating_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    is_blocked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE IF NOT EXISTS user_ratings (
    id BIGSERIAL PRIMARY KEY,
    rater_id BIGINT NOT NULL,
    rater_name VARCHAR(255) NOT NULL,
    rated_user_id BIGINT NOT NULL,
    trip_id BIGINT,
    score INTEGER NOT NULL,
    comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_ratings_rated_user ON user_ratings(rated_user_id);
