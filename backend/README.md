# Backend Application (Spring Boot)

Spring Boot 3 REST API backend service configured for Java 21 and Maven.

## Package Architecture (`src/main/java/com/analyzer/`)

- **`config/`**: Security (Spring Security/JWT), CORS, database, and Spring bean configurations.
- **`controller/`**: REST API endpoints and controllers for handling incoming HTTP requests.
- **`service/`**: Business logic, AI integration services (e.g. OpenAI/Gemini/Ollama parsers), and application workflows.
- **`repository/`**: Spring Data JPA repositories interfacing with the persistent storage.
- **`model/`**: JPA domain entities representing application data structures.
- **`dto/`**: Data Transfer Objects (Request/Response payload objects).
- **`exception/`**: Global exception handlers (`@ControllerAdvice`) and custom application exceptions.
- **`util/`**: Shared helper utility functions (file parsing, text extraction, resume format converters).
- **`src/main/resources/`**: Application configuration properties (`application.yml`), database connection settings, and static templates.
