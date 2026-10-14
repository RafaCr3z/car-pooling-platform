-- ============================================================================
-- V1__init_vehicle_schema.sql - Initial Schema for Vehicle Management Service
-- ============================================================================

CREATE TABLE IF NOT EXISTS vehicles (
    id BIGSERIAL PRIMARY KEY,
    license_plate VARCHAR(50) NOT NULL UNIQUE,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    color VARCHAR(50),
    seat_capacity INTEGER NOT NULL DEFAULT 4,
    fuel_type VARCHAR(50),
    fuel_consumption_lper100km DOUBLE PRECISION DEFAULT 5.5,
    co2_emissions_gper_km DOUBLE PRECISION DEFAULT 115.0,
    type VARCHAR(50) NOT NULL DEFAULT 'CAR',
    owner_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    price_per_hour NUMERIC(38,2) NOT NULL DEFAULT 0.00,
    price_per_km NUMERIC(38,2) NOT NULL DEFAULT 0.00,
    location VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_vehicles_owner ON vehicles(owner_id);
CREATE INDEX IF NOT EXISTS idx_vehicles_status ON vehicles(status);
CREATE INDEX IF NOT EXISTS idx_vehicles_plate ON vehicles(license_plate);
