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

## ADR-3: Utilizing TimescaleDB over Standard Postgres/NoSQL
- **Status**: Accepted
- **Context**: We need to perform large amounts of writes on mostly append-only time-ordered data. Traditional SQL databases experience index bloat and B-Tree structure decay when tables reach billions of rows, slowing down inserts significantly.
- **Decision**: Opt for **TimescaleDB** (PostgreSQL extension) providing time-based chunking.
- **Consequences**:
    - *Positive*: Avoids index bloat. Allows us to leverage existing mature C++ driver ecosystems (`libpqxx`) and SQL knowledge instead of learning proprietary NoSQL structures.
    - *Negative*: Single-node configuration has an eventual limit compared to natively distributed systems like Cassandra, though this limit is very high and sufficient for 10k users.

## ADR-4: Database Batch Writes
- **Status**: Accepted
- **Context**: Calling standard SQL `INSERT INTO...` queries row-by-row on strings dequeued from Kafka will waste significant time on network round-trip delays and transaction wrapping.
- **Decision**: The backend C++ Consumer microservice will pull from Kafka in memory buffers and write in batches (e.g. 1000 records at a time) to TimescaleDB.
- **Consequences**:
    - *Positive*: Can increase insert throughput by over 100x compared to standard iteration logic.
    - *Negative*: A slight propagation delay (1-2 seconds) is introduced before the DB and CLI can read the latest client location.
