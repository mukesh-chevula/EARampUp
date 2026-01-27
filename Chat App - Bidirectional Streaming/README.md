# gRPC + Spring Boot + Cassandra Chat Application

A complete, production-ready bidirectional streaming chat application featuring real-time messaging with persistent storage.


- Bidirectional gRPC Streaming - Real-time message exchange between clients and server
- Cassandra Database - Persistent message storage and history
- Spring Boot Server - Scalable, managed application framework
- Java Swing GUI - Clean, intuitive client interface
- Broadcast Messaging - Server can send messages to all connected clients
- Docker Support - Easy Cassandra setup with Docker Compose
- Gradle Build - Dependency management and automated code generation

## Start


### Setup Steps
```bash
# Start Cassandra
docker-compose up -d cassandra

# Build project
./gradlew clean build

# Terminal 1: Start server
./gradlew bootRun

# Terminal 2: Start client
./gradlew runClient
```


Message flow:
1. Client sends message via gRPC stream to server
2. Server saves to Cassandra and broadcasts to all clients
3. All connected clients receive the message
4. Server terminal can also broadcast messages

