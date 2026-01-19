# User Management System - Project 06: E2E Testing


**Total Tests: 63** 


```
10 E2E Tests        (Full stack with real HTTP)
    ^
39 Integration Tests (All layers with embedded DB)
    ^
14 Unit Tests        (Isolated components with mocks)

```

## Testing Strategy

### 1. **Unit Tests (14 tests)** 
Tests individual components in isolation using mocks.

**Location:** `src/test/java/.../controller/` and `.../service/`

- **UserControllerTest** (5 tests)
  - Tests REST controller endpoints with mocked service layer
  - Uses `@WebMvcTest` for focused controller testing
  - Verifies HTTP responses, status codes, and JSON serialization

- **UserServiceTest** (8 tests)
  - Tests business logic with mocked repository
  - Validates service methods behavior
  - Tests error scenarios and edge cases

- **Application Context Test** (1 test)
  - Verifies Spring application context loads successfully

**Key Features:**
- Fast execution (isolated components)
- Focused on single responsibility
- Uses Mockito for mocking dependencies

### 2. **Integration Tests (39 tests)**
Tests multiple layers working together with a real embedded database.

**Location:** `src/test/java/.../integration/`

- **UserControllerIntegrationTest** (10 tests)
  - Full HTTP request/response cycle testing
  - Uses MockMvc for simulated HTTP requests
  - Validates REST API with real service and repository layers

- **UserServiceIntegrationTest** (13 tests)
  - Tests service layer with real database operations
  - Verifies transaction handling and data consistency
  - Tests concurrent operations and business logic flows

- **UserRepositoryIntegrationTest** (16 tests)
  - Tests MongoDB CRUD operations directly
  - Validates query methods and database interactions
  - Tests batch operations and data persistence

**Key Features:**
- Tests component interactions
- Uses embedded MongoDB (Flapdoodle)
- No Docker required for testing
- Validates data persistence

### 3. **End-to-End (E2E) Tests (10 tests)**
Simulates real user scenarios with actual HTTP requests to a running application.

**Location:** `src/test/java/.../e2e/`

**Test Scenarios:**

1. **Complete User Lifecycle**
   - Create → Read → Update → Delete flow
   - Verifies full CRUD workflow end-to-end

2. **Multiple Users Management**
   - Creates multiple users simultaneously
   - Retrieves and validates all users
   - Tests list operations

3. **User Workflow with Multiple Updates**
   - Sequential update operations
   - Validates state changes persist correctly
   - Tests data consistency across updates

4. **Bulk Operations**
   - Creates 5 users
   - Selectively deletes specific users
   - Verifies remaining users are correct

5. **Selective Retrieval**
   - Creates multiple users
   - Retrieves specific user by ID
   - Validates correct user is returned

6. **Deletion Scenarios**
   - Tests deleting non-existent users
   - Verifies proper error handling

7. **Data Integrity Verification**
   - Creates user via REST API
   - Directly queries database to verify
   - Ensures API and database are in sync

8. **Concurrent Operations**
   - Rapid succession of create/update/retrieve
   - Tests system consistency under load
   - Validates no race conditions

9. **Error Handling**
   - Tests retrieving non-existent users
   - Verifies proper error responses
   - Tests edge cases

10. **Empty Database Scenario**
    - Tests behavior with no data
    - Verifies empty list response

**Key Features:**
- Real HTTP requests using `TestRestTemplate`
- Full application starts on random port
- Tests entire stack: API → Service → Repository → Database
- Validates real-world user scenarios
- Database persistence verification

## Running Tests

### Run All Tests (63 tests)
```bash
./gradlew test
```

### Run Specific Test Suites

**Unit Tests Only:**
```bash
./gradlew test --tests "*Test"
```

**Integration Tests Only:**
```bash
./gradlew test --tests "*.integration.*"
```

**E2E Tests Only:**
```bash
./gradlew test --tests "*.e2e.*"
```

### View Test Report
After running tests, open the HTML report:
```bash
open build/reports/tests/test/index.html
```

## Setup & Run Application

### Prerequisites
- Java 21+
- Docker Desktop (for production deployment)
- Gradle 9.2.1 (included via wrapper)

### Running with Docker
```bash
docker-compose up --build
```
Application starts on `http://localhost:8080`

### Running Locally
```bash
# Start MongoDB in Docker
docker-compose up -d mongo-db-container

# Run application
./gradlew bootRun
```

## API Documentation

### Base URL
```
http://localhost:8080
```

### Endpoints

#### 1. Create User
**POST** `/users`

**Request Body:**
```json
{
    "firstName": "Mukesh",
    "lastName": "Ch",
    "companyName": "EA",
    "experienceMonths": 24,
    "mobileNumber": "9876543210"
}
```

**Response:** `200 OK`
```json
{
    "userId": "651a... (auto-generated)",
    "firstName": "Mukesh",
    "lastName": "Ch",
    "companyName": "EA",
    "experienceMonths": 24,
    "mobileNumber": "9876543210"
}
```

#### 2. Get All Users
**GET** `/users`

**Response:** `200 OK`
```json
[
    {
   "userId": "651a...",
   "firstName": "Mukesh",
   "lastName": "Ch",
   "companyName": "EA",
   "experienceMonths": 24,
   "mobileNumber": "9876543210"
    }
]
```

#### 3. Get User by ID
**GET** `/users/{userId}`

**Response:** `200 OK`
```json
{
    "userId": "651a...",
    "firstName": "Mukesh",
    "lastName": "Ch",
    "companyName": "EA",
    "experienceMonths": 24,
    "mobileNumber": "9876543210"
}
```

#### 4. Update User
**PUT** `/users/{userId}`

**Request Body:**
```json
{
    "firstName": "Mukesh",
    "lastName": "Chevula",
    "companyName": "Electronic Arts",
    "experienceMonths": 30,
    "mobileNumber": "9876543210"
}
```

**Response:** `200 OK` (Updated user object)

#### 5. Delete User
**DELETE** `/users/{userId}`

**Response:** `200 OK`
```json
true
```

## Test Documentation

### Unit Test Examples

**UserControllerTest.java**
```java
@Test
void createUser_ShouldReturnCreatedUser() throws Exception {
    when(service.save(any(User.class))).thenReturn(user1);
    
    mockMvc.perform(post("/users")
       .contentType(MediaType.APPLICATION_JSON)
       .content(objectMapper.writeValueAsString(user1)))
   .andExpect(status().isOk())
   .andExpect(jsonPath("$.firstName").value("Mukesh"));
}
```

### Integration Test Examples

**UserRepositoryIntegrationTest.java**
```java
@Test
void save_ShouldPersistUserToDatabase() {
    // Uses testUser1 from @BeforeEach setUp()
    // testUser1 = new User(null, "Mukesh", "Ch", "EA", 24, "1234567890");
    
    User savedUser = userRepository.save(testUser1);
    
    assertThat(savedUser.getUserId()).isNotNull();
    assertThat(userRepository.findById(savedUser.getUserId())).isPresent();
}
```

### E2E Test Examples

**UserE2ETest.java**
```java
@Test
void completeUserLifecycle_CreateReadUpdateDelete_ShouldWorkEndToEnd() {
    // Create user via REST API
    ResponseEntity<User> createResponse = restTemplate.postForEntity(
   baseUrl, newUser, User.class
    );
    String userId = createResponse.getBody().getUserId();
    
    // Read, Update, Delete operations...
    // Verifies complete workflow end-to-end
}
```
