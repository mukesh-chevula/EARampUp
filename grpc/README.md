# gRPC User Management System

- **Create User**: Add new users to the system
- **Update User**: Modify existing user information
- **Delete User**: Remove users from the system
- **Get User by ID**: Retrieve a specific user
- **Get All Users**: List all users in the system


## User Schema

```java
User {
    String userId;
    String firstName;
    String lastName;
    String company;
    int age;
    String phoneNumber;
}
```

## Configuration

The application is configured in `application.properties`:

```properties
spring.application.name=grpc
spring.grpc.server.port=9090
spring.cassandra.contact-points=localhost:9042
spring.cassandra.keyspace-name=grpc_keyspace
spring.cassandra.username=cassandra
spring.cassandra.password=cassandra
spring.cassandra.ssl=false
```

## Build


```bash
./gradlew clean build
```


## Running the Application

### 1. Start Cassandra

```bash
#Docker
docker run -d -p 9042:9042 --name cassandra cassandra:latest

#Local
cassandra
```

Wait for Cassandra to start (check logs with `docker logs cassandra`).

### 2. Initialize Cassandra Schema

Using cqlsh (Cassandra Query Language Shell):

```bash
# If using Docker
docker exec -it cassandra cqlsh

# Or if installed locally
cqlsh localhost 9042
```

Then run the commands from `src/main/resources/cassandra-init.cql`:

```cql
CREATE KEYSPACE IF NOT EXISTS grpc_keyspace
WITH replication = {
    'class': 'SimpleStrategy',
    'replication_factor': 1
};

CREATE TABLE IF NOT EXISTS grpc_keyspace.users (
    user_id text PRIMARY KEY,
    first_name text,
    last_name text,
    company text,
    age int,
    phone_number text
);
```

### 3. Run the Spring Boot Application

```bash
./gradlew bootRun
```

The application will start with:
- gRPC server on port **9090**

## Testing

Run the test suite:

```bash
./gradlew test
```

The tests cover:
- Creating users
- Updating users
- Deleting users
- Getting user by ID
- Getting all users

## gRPC Service 

The proto file defines the following RPC methods:

```protobuf
service UserService {
  rpc CreateUser(CreateUserRequest) returns (UserResponse);
  rpc UpdateUser(UpdateUserRequest) returns (UserResponse);
  rpc DeleteUser(DeleteUserRequest) returns (DeleteUserResponse);
  rpc GetUserById(GetUserByIdRequest) returns (UserResponse);
  rpc GetAllUsers(GetAllUsersRequest) returns (GetAllUsersResponse);
}
```

