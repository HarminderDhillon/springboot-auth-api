# Spring Boot Auth API

A RESTful authentication API built with Spring Boot 3.x, Java 21, MongoDB, JWT, and email verification.

## Features
- User registration
- Email verification
- JWT-based login and authentication
- MongoDB integration

## Requirements
- Java 21+
- MongoDB instance

## Getting Started

1. Clone this repository or copy the project directory.
2. Configure MongoDB and email settings in `src/main/resources/application.properties`.
3. Build and run:
   ```sh
   ./gradlew bootRun
   ```

## Endpoints
- `/api/auth/register` – Register user
- `/api/auth/verify` – Verify email
- `/api/auth/login` – Login and receive JWT

## License
MIT
