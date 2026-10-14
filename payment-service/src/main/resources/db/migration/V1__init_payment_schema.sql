-- ============================================================================
-- V1__init_payment_schema.sql - Initial Schema for Payment Processing Service
-- ============================================================================

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    reservation_id BIGINT,
    passenger_id BIGINT NOT NULL,
    driver_id BIGINT,
    amount NUMERIC(38,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    method VARCHAR(50) NOT NULL DEFAULT 'MBWAY',
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payments_trip ON payments(trip_id);
CREATE INDEX IF NOT EXISTS idx_payments_passenger ON payments(passenger_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_tx ON payments(transaction_id);
