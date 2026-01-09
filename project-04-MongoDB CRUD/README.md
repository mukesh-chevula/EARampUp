# User Management System (MongoDB)
This System allows to Create, Read, Update & Delete (CRUD) Operation over User Data using MongoDB as the database.

## Tech Stack 
* Spring Boot
* Spring Data MongoDB
* Docker & Docker Compose
* Gradle
* HTTP

## Setup & Run

### Prerequisites
* Java 21+
* Docker Desktop installed and running

### Running with Docker
The easiest way to run the application and the MongoDB database is using Docker Compose.

1. Open a terminal in the project root directory.
2. Build and start the containers:
   ```bash
   docker-compose up --build
   ```
3. The application will start on port `8080`.

To stop the application:
```bash
docker-compose down
```

### Running Locally (without Docker for App)
If you want to run the application itself locally but use MongoDB in Docker:
1. Start only the database:
   ```bash
   docker-compose up -d mongo-db-container
   ```
2. Run the Spring Boot application using Gradle:
   ```bash
   ./gradlew bootRun
   ```

## Testing

### Running Tests Locally
The project includes unit tests (for Service layer) and integration tests (for Controller layer). To run them:

```bash
./gradlew test
```

### Tests in Docker Build
The `Dockerfile` is configured to run tests automatically during the build process. If any test fails, the container build will fail, preventing deployment of broken code.

To verify this, simply run:
```bash
docker-compose build
```

## Methods

### Create a User 

`POST /users`

#### BODY:

```json
{
    "firstName": "Mukesh",
    "lastName": "Ch",
    "companyName": "EA",
    "experienceMonths": 12,
    "mobileNumber": "9876543210"
}
```

#### Response:

```json
{
    "userId": "651a... (auto-generated)",
    "firstName": "Mukesh",
    "lastName": "Ch",
    "companyName": "EA",
    "experienceMonths": 12,
    "mobileNumber": "9876543210"
}
```

### Get User by userId

`GET /users/{id}`

Example: `GET /users/651a...`

#### Response:

```json
{
    "userId": "651a...",
    "firstName": "Mukesh",
    "lastName": "Ch",
    "companyName": "EA",
    "experienceMonths": 12,
    "mobileNumber": "9876543210"
}
```

### Get All Users

`GET /users`

#### Response:

```json
[
    {
        "userId": "651b...",
        "firstName": "Daivik",
        "lastName": null,
        "companyName": "Qualcomm",
        "experienceMonths": 12,
        "mobileNumber": "8185929110"
    },
    {
        "userId": "651a...",
        "firstName": "Mukesh",
        "lastName": "Ch",
        "companyName": "EA",
        "experienceMonths": 12,
        "mobileNumber": "9876543210"
    }
]
```

### Update a user 

`PUT /users/{id}`

Example: `PUT /users/651a...`

#### BODY:

```json
{
    "firstName": "Mukesh",
    "lastName": "Ch",
    "companyName": "Electronic Arts",
    "experienceMonths": 18,
    "mobileNumber": "9876543210"
}
```

#### Response:

```json
{
    "userId": "651a...",
    "firstName": "Mukesh",
    "lastName": "Ch",
    "companyName": "Electronic Arts",
    "experienceMonths": 18,
    "mobileNumber": "9876543210"
}
```

### Delete a user

`DELETE /users/{id}`

Example: `DELETE /users/651a...`

#### Response 

```json
true
```
