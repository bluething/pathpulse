# Architecture Decision Records (ADRs)

## ADR-1: Utilization of WebSockets for Real-Time Ingest
- **Status**: Accepted
- **Context**: Clients submit location updates extremely frequently (potentially every few seconds). Utilizing standard REST APIs would force clients to establish new TCP connections and TLS handshakes, severely eating up server resources via heavy protocol overheads.
- **Decision**: Implement persistent bidirectional connections using **WebSockets**.
- **Consequences**:
    - *Positive*: Extremely low overhead per message. Enables server to push configurations or commands down to the device in the future.
    - *Negative*: Load balancers require connection tracking and sticky session management, making rolling deployments slightly trickier.

## ADR-2: Inserting Kafka Between Ingestion and DB
- **Status**: Accepted
- **Context**: 10,000 concurrent clients could generate upwards of 5,000 to 10,000 database inserts a second. A DB might experience lock contention, IO bottlenecking, or slowdowns during garbage collection/indexing. If the DB halts, ingestion requests fail, dropping user data.
- **Decision**: Introduce **Apache Kafka** as an intermediary message queue.
- **Consequences**:
    - *Positive*: Provides extreme durability. Isolates the ingestion layer from the database layer, allowing the system to absorb traffic bursts and defer writes.
    - *Negative*: Adds operational complexity. Requires ZooKeeper/KRaft cluster and disk management.

## ADR-3: Utilizing TimescaleDB over Document NoSQL (e.g., MongoDB)
- **Status**: Accepted
- **Context**: The system must handle an extreme write throughput of entirely append-only, chronologically ordered location data. While document NoSQL databases like MongoDB provide vertical ingest scale and schema flexibility, they are fundamentally designed for discrete JSON document retrieval. Storing continuous location metrics as individual BSON documents leads to inefficient storage overhead, eventual B-Tree index bloat, and forces the use of complex, non-standard aggregation pipelines to perform fundamental geospatial or time-window analytics.
- **Decision**: Opt for **TimescaleDB**, a time-series optimized extension for PostgreSQL relying on heavily partitioned hypertables.
- **Consequences**:
  - *Positive*:
    - **Zero Index Bloat**: Automatically chunks table data by time/space boundaries, keeping the active write indexes significantly smaller and completely resident in RAM.
    - **Geospatial & Analytic Superiority**: Native integration with PostGIS for powerful location-based queries (e.g., finding overlapping paths, point-in-polygon) and native Timescale window functions (like `time_bucket`), which are vastly simpler to execute than MongoDB aggregation pipelines.
    - **Columnar Compression**: Older time chunks are automatically converted to compressed columnar storage behind the scenes, profoundly reducing the disk footprint compared to storing millions of uncompressed MongoDB documents.
    - **Mature Drivers**: Allows the backend to utilize rock-solid, synchronous C++ Postgres drivers (`libpqxx`).
  - *Negative*:
    - Inherently lacks the "schemaless" flexibility of MongoDB (however, a location payload of lat/lng/timestamp is rigidly structured regardless).
    - Requires active management of PostgreSQL extensions.

## ADR-4: Database Batch Writes
- **Status**: Accepted
- **Context**: Calling standard SQL `INSERT INTO...` queries row-by-row on strings dequeued from Kafka will waste significant time on network round-trip delays and transaction wrapping.
- **Decision**: The backend C++ Consumer microservice will pull from Kafka in memory buffers and write in batches (e.g. 1000 records at a time) to TimescaleDB.
- **Consequences**:
    - *Positive*: Can increase insert throughput by over 100x compared to standard iteration logic.
    - *Negative*: A slight propagation delay (1-2 seconds) is introduced before the DB and CLI can read the latest client location.
