This repository contains my solution for a system design interview exercise. The original problem can be found in the [backend-interview](https://github.com/bitwyre/backend-interview) epository. The implementation is written in Java.

# The Problem
Design and implement two micro-services in C++, one that uses WebSockets to get location data from multiple clients, labels it and sends it to a Kafka broker and another micro-service (Iterates!) consumes said data and inserts it into a SQL or NoSQL DB of your choice and has a CLI interface to interact with the DB in order to retrieve the data via a simple interface.  
Note that in a real life scenario, the location data tends to be a lot when you have about 10,000 users as you might be able to guess, so making this kind of a program scaleable from the start is crucial.  
The Kafka technique may not be the best but is mandatory to test your knowledge of the system.

# Requirement Understanding and Constraints
- Core Operations: Clients send high-frequency location data.  
- Scale: Target is 10,000+ concurrent users, which translates to massive, consistent inbound messaging traffic (e.g., 10k messages every few seconds).  
- Mandatory Technologies:  
  - Java and Spring Boot for Microservices.  
  - WebSockets for ingestion.  
  - Kafka as the message broker.  
  - CLI to retrieve the data.
- Persistence: SQL or NoSQL DB of choice.

# Approach
My approach centers around building an asynchronous, non-blocking event pipeline that gracefully decouples high-speed data ingestion from slower data persistence.  
1. Ingestion Layer (WebSocket Service):  
   - Needs to securely accept connections and immediately ingest data without blocking.  
   - A single modern Java asynchronous worker can handle >100,000 concurrent connections.  
   - The service will parse the incoming payload, tag/label it (e.g., attaching the ingestion server's receive timestamp), and fire it off to Kafka.
2. Message Broker (Apache Kafka):  
   - Kafka acts as a "shock absorber". It prevents database backpressure from slowing down or crashing the WebSocket server.  
   - It also naturally provides ordering and log persistence, allowing parallel consumer scaling.
3. Persistence Layer (Consumer Service & Database):  
   - The consumer microservice reads from Kafka and persists records to a Database.  
   - Single-row inserts are notoriously slow in DBs. Thus, this service must utilize batch inserts.  
   - Choice of Database: Because location data is purely time-series (append-only, time-based querying), an optimized DB is necessary. TimescaleDB (built on PostgreSQL) provides familiar SQL while automatically partitioning data by time via hypertables, maximizing write throughput. Alternatively, purely distributed columnar NoSQL like ScyllaDB/Cassandra is equally suitable. Let's design around TimescaleDB.
4. Data Retrieval (CLI):  
   - A lightweight CLI written in Java that takes inputs (like user ID) and fires simple SELECT queries to TimescaleDB.

# System Design Concepts Applied  
- Decoupling: The producer (WebSocket) knows nothing about the consumer (DB Writer). By placing Kafka in between, the two services can scale independently.  
- Event-Driven Architecture: The entire pipeline operates based on streams of continuous events (user locations) flowing from clients all the way to disk blocks.  
- Batching: To improve I/O efficiency, the consumer microservice groups hundreds of messages from Kafka into a single atomic write transaction before sending to the database.  
- Horizontal Scaling & Statelessness: The C++ WebSocket service is entirely stateless. We can just add load balancers and scale from 1 node to N nodes effortlessly.  
- Partitioning: Kafka topics can be partitioned by a key (e.g., user_id) assuring that events from the same user are processed in strict chronological order by the consumer.