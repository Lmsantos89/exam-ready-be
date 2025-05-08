# Exam Ready

Exam Ready is a Learning Management System (LMS) application built with Spring Boot.

## Project Structure

The project follows a standard Spring Boot application structure:

```
.
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── lms
│   │   │           └── examready
│   │   │               ├── controller
│   │   │               ├── dto
│   │   │               ├── exception
│   │   │               ├── model
│   │   │               ├── repository
│   │   │               ├── security
│   │   │               └── service
│   │   └── resources
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       ├── application-staging.properties
│   │       ├── application-test.properties
│   │       └── db
│   │           └── migration
│   └── test
│       └── java
│           └── com
│               └── lms
│                   └── examready
├── build.gradle
├── docker-compose.yml
├── gradlew
└── gradlew.bat
```

## Prerequisites

- Java 21
- Docker (for running PostgreSQL)
- Gradle (Wrapper is included in the project)

## Setup

1. Clone the repository:
   ```
   git clone https://github.com/Lmsantos89/exam-ready.git
   cd exam-ready
   ```

2. Start the PostgreSQL database using Docker:
   ```
   docker-compose up -d
   ```

3. Run the application:
   ```
   ./gradlew bootRun
   ```

## Building

To build the project, run:

```
./gradlew build
```

## Testing

To run the tests, execute:

```
./gradlew test
```

## Configuration

The application uses different property files for various environments:

- `application-dev.properties`: Development environment
- `application-test.properties`: Test environment
- `application-staging.properties`: Staging environment
- `application-prod.properties`: Production environment


## Database Migrations

This project uses Flyway for database migrations. Migration scripts are located in `src/main/resources/db/migration`.

## API Documentation

The main API endpoints are:

- Authentication:
    - POST `/api/auth/signup`: User registration
```
POST /api/auth/sign-up HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
    "username": "user",
    "password": "testpassword",
    "email": "test@email.com"
}
```

  - POST `/api/auth/signin`: User login

```
POST /api/auth/sign-up HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
    "username": "user",
    "password": "testpassword",
    "email": "test@email.com"
}
```

For detailed API documentation, please refer to the controller classes in the `com.lms.examready.controller` package.
