# gRPC User Management System - Client Streaming


## Features

- **Create User**: Add new users to the system (client streaming)
- **Update User**: Modify existing user information (client streaming)
- **Delete User**: Remove users from the system (client streaming)
- **Get User by ID**: Retrieve a specific user (unary)
- **Get All Users**: List all users in the system (unary)

## User Schema

```java
User {
    String id;           
    String firstname;
    String lastname;
    String email;
    String phone;
}
```


## Configuration

The application is configured in `application.properties` and `application.yml`:

```properties
spring.application.name=client-streaming
spring.cassandra.local-datacenter=datacenter1
spring.cassandra.contact-points=localhost
spring.cassandra.port=9042
spring.cassandra.keyspace-name=userkeyspace
spring.cassandra.schema-action=create_if_not_exists
grpc.server.port=9090
```

## Build

```bash
./gradlew clean build
```

## Running the Application

### Option 1: Docker Compose (Recommended)

Start Cassandra and the app together:

```bash
cd /Users/mchevula/IdeaProjects/EARampUp/Client-Streaming
docker compose up --build
```

To stop:
```bash
docker compose down
```

### Option 2: Local Setup

#### 1. Start Cassandra

```bash
# Via Docker
docker run -d -p 9042:9042 --name cassandra cassandra:5.0

# Wait for startup (~30s)
```

#### 2. Initialize Cassandra Schema

```bash
docker exec cassandra cqlsh -e "
CREATE KEYSPACE IF NOT EXISTS userkeyspace 
WITH replication = {'class':'SimpleStrategy','replication_factor':1};
CREATE TABLE IF NOT EXISTS userkeyspace.users (
  id text PRIMARY KEY,
  firstname text,
  lastname text,
  email text,
  phone text
);"
```

#### 3. Run the Spring Boot Application

```bash
./gradlew bootRun
```

The application will start with:
- **gRPC Server**: port 9090
- **Cassandra**: localhost:9042

## Testing

### Run Unit Tests

```bash
./gradlew test
```

### Manual

Use the provided test script:

```bash
chmod +x grpc-test.sh
./grpc-test.sh
```

Or manually test each operation:

#### Create User (Client Streaming)
```bash
cat > /tmp/create.json <<'EOF'
{"field":"firstname","value":"Mukesh"}
{"field":"lastname","value":"Ch"}
{"field":"email","value":"mukesh.ch@ea.com"}
{"field":"phone","value":"1234567890"}
EOF

grpcurl -plaintext -d @ localhost:9090 UserService/CreateUser < /tmp/create.json
```

#### Get All Users (Unary)
```bash
grpcurl -plaintext -d '{}' localhost:9090 UserService/GetAllUsers
```

#### Get User by ID (Unary)
```bash
grpcurl -plaintext -d '{"id":"<user-id>"}' localhost:9090 UserService/GetUser
```

#### Update User (Client Streaming)
```bash
cat > /tmp/update.json <<'EOF'
{"field":"id","value":"<user-id>"}
{"field":"firstname","value":"Mookesh"}
{"field":"email","value":"mookesh.ch@ea.com"}
EOF

grpcurl -plaintext -d @ localhost:9090 UserService/UpdateUser < /tmp/update.json
```

#### Delete User (Client Streaming)
```bash
cat > /tmp/delete.json <<'EOF'
{"field":"id","value":"<user-id>"}
EOF

grpcurl -plaintext -d @ localhost:9090 UserService/DeleteUser < /tmp/delete.json
```

## gRPC Service Definition

The proto file defines the following RPC methods:

```protobuf
service UserService {
  rpc CreateUser(stream UserField) returns (UserResponse);
  rpc UpdateUser(stream UserField) returns (UserResponse);
  rpc DeleteUser(stream UserField) returns (UserResponse);
  rpc GetUser(UserRequest) returns (User);
  rpc GetAllUsers(Empty) returns (Users);
}

message UserField {
  string field = 1;   // firstname, lastname, email, phone, id
  string value = 2;
}

message UserResponse {
  string status = 1;
  string message = 2;
}

message User {
  string id = 1;
  string firstname = 2;
  string lastname = 3;
  string email = 4;
  string phone = 5;
}

message Users {
  repeated User users = 1;
}
```
