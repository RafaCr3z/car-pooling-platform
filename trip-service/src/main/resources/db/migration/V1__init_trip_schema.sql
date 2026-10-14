-- ============================================================================
-- V1__init_trip_schema.sql - Initial Schema for Trip & Reservation Management Service
-- ============================================================================

CREATE TABLE IF NOT EXISTS trips (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    driver_name VARCHAR(255),
    vehicle_id BIGINT,
    vehicle_info VARCHAR(255),
    departure_location VARCHAR(255) NOT NULL,
    destination_location VARCHAR(255) NOT NULL,
    origin_latitude DOUBLE PRECISION,
    origin_longitude DOUBLE PRECISION,
    destination_latitude DOUBLE PRECISION,
    destination_longitude DOUBLE PRECISION,
    departure_date_time TIMESTAMP NOT NULL,
    available_seats INTEGER NOT NULL,
    occupied_seats INTEGER NOT NULL DEFAULT 0,
    total_cost NUMERIC(38,2) NOT NULL,
    cost_per_passenger NUMERIC(38,2) NOT NULL,
    estimated_fuel_cost NUMERIC(38,2) DEFAULT 0.00,
    estimated_tolls NUMERIC(38,2) DEFAULT 0.00,
    distance DOUBLE PRECISION NOT NULL,
    co2saved_kg DOUBLE PRECISION DEFAULT 0.0,
    description VARCHAR(1000),
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reservations (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    passenger_id BIGINT NOT NULL,
    passenger_name VARCHAR(255),
    passenger_email VARCHAR(255),
    passenger_phone VARCHAR(255),
    number_of_seats INTEGER NOT NULL DEFAULT 1,
    total_price NUMERIC(38,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_trips_driver ON trips(driver_id);
CREATE INDEX IF NOT EXISTS idx_trips_status ON trips(status);
CREATE INDEX IF NOT EXISTS idx_trips_route ON trips(departure_location, destination_location);
CREATE INDEX IF NOT EXISTS idx_reservations_trip ON reservations(trip_id);
CREATE INDEX IF NOT EXISTS idx_reservations_passenger ON reservations(passenger_id);
