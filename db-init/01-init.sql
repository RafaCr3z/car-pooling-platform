-- Create databases
CREATE DATABASE carpooling_auth;
CREATE DATABASE carpooling_trips;
CREATE DATABASE carpooling_vehicles;
CREATE DATABASE carpooling_payments;
CREATE DATABASE carpooling_admin;
CREATE DATABASE carpooling_notifications;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE carpooling_auth TO carpooling;
GRANT ALL PRIVILEGES ON DATABASE carpooling_trips TO carpooling;
GRANT ALL PRIVILEGES ON DATABASE carpooling_vehicles TO carpooling;
GRANT ALL PRIVILEGES ON DATABASE carpooling_payments TO carpooling;
GRANT ALL PRIVILEGES ON DATABASE carpooling_admin TO carpooling;
GRANT ALL PRIVILEGES ON DATABASE carpooling_notifications TO carpooling;
