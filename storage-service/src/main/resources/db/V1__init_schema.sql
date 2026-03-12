CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE location_events (
    id VARCHAR(255) PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    accuracy DOUBLE PRECISION NOT NULL,
    client_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    server_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    label VARCHAR(255)
);

-- Convert to TimescaleDB hypertable
-- We must make the partitioning column (server_timestamp) part of the primary key or just don't use unique keys that don't include it.
-- Since TimescaleDB hypertables require the time partitioning column to be part of any UNIQUE/PRIMARY KEY constraint if one exists,
-- we must drop the primary key constraint on `id` and recreate it as a composite key (id, server_timestamp) if we want to enforce uniqueness,
-- OR we can just use a standard table that has no PRIMARY KEY but has an index on id, OR we partition it.
-- For location data firehose, it's typical to NOT have a unique constraint on ID if we partition by time, but given JPA requires an @Id,
-- we can keep the primary key locally on `id` if we don't use it for uniqueness checking across chunks, or we just drop the PK constraint on DB level.
-- Actually, the simpler way for TimescaleDB is to make the primary key composite:
ALTER TABLE location_events DROP CONSTRAINT location_events_pkey;
ALTER TABLE location_events ADD PRIMARY KEY (id, server_timestamp);

SELECT create_hypertable('location_events', 'server_timestamp');

-- Create composite index for our most common query pattern (findByUserIdOrderByServerTimestampDesc)
CREATE INDEX idx_user_time ON location_events (user_id, server_timestamp DESC);
