# Online Notebook - Main Application

A Spring Boot web application for managing student grades, assignments, and attendance. This is the main monolith application that communicates with the attendance microservice.

## 🚀 Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17** or higher
- **Maven 3.6+**
- **MySQL 8.0+** (or compatible database)
- **Git**

## 📋 Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/tgadzhalov/notebook.git
cd notebook/OnlineNotebook
```

### 2. Database Configuration

1. **Install and start MySQL** on your machine

2. **Create the database** (or it will be created automatically):
   ```sql
   CREATE DATABASE IF NOT EXISTS notebook;
   ```

3. **Configure database credentials** in `src/main/resources/application.properties`:
   
   **Option 1: Set environment variables (recommended)**
   ```bash
   export DB_USERNAME=your_username
   export DB_PASSWORD=your_password
   ```
   
   **Option 2: Update application.properties directly**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/notebook?createDatabaseIfNotExist=true
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```
   
   **Note**: The application uses environment variables by default. If not set, it will use `root` for username and empty password. Make sure to configure these before running.

### 3. JWT Configuration

**IMPORTANT**: The JWT secret must match between the main application and the attendance microservice.

Update `src/main/resources/application.properties`:
```properties
jwt.secret=your-shared-secret-key-between-monolith-and-microservice-must-be-at-least-256-bits-long-for-HS256-algorithm
jwt.expiration-hours=1
```

**⚠️ Security Note**: For production, use a strong, randomly generated secret key and store it securely (environment variables, secrets manager, etc.).

### 4. Attendance Microservice Configuration

Ensure the microservice URL is correct in `src/main/resources/application.properties`:
```properties
attendance.microservice.base-url=http://localhost:8081
attendance.microservice.endpoint=/api/v1/attendance
```

### 5. Build the Project

```bash
mvn clean install
```

Or using the Maven wrapper:
```bash
./mvnw clean install
```

On Windows:
```bash
mvnw.cmd clean install
```

### 6. Run the Application

```bash
mvn spring-boot:run
```

Or using the Maven wrapper:
```bash
./mvnw spring-boot:run
```

On Windows:
```bash
mvnw.cmd spring-boot:run
```

The application will start on **http://localhost:8080**

## 🔧 Configuration

### Application Properties

Key configuration in `src/main/resources/application.properties`:

- **Database**: MySQL connection settings
- **JWT**: Secret key and expiration time
- **Microservice**: Attendance service URL and endpoint
- **JPA**: Hibernate settings (DDL auto-update enabled)

### Port

Default port: **8080**

To change the port, add to `application.properties`:
```properties
server.port=8080
```

## 🏃 Running with the Microservice

**Important**: The attendance microservice must be running before starting this application.

1. **Start the attendance microservice** first (see [attendence-microservice README](../attendence-microservice/README.md))
2. **Then start this application**

Both services must use the **same JWT secret** for authentication to work.

## 🛠️ Technology Stack

- **Spring Boot 3.4.0**
- **Spring Data JPA**
- **Spring Security**
- **Thymeleaf** (templating engine)
- **MySQL** (database)
- **Maven** (build tool)
- **Lombok** (reducing boilerplate)
- **Spring Cloud OpenFeign** (microservice communication)
- **JWT** (authentication)

## 📁 Project Structure

```
OnlineNotebook/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/OnlineNotebook/
│   │   │       ├── client/          # Feign client for microservice
│   │   │       ├── configurations/ # Spring configurations
│   │   │       ├── controller/     # REST/Web controllers
│   │   │       ├── exceptions/     # Exception handlers
│   │   │       ├── models/         # Entities, DTOs, Enums
│   │   │       ├── repositories/   # JPA repositories
│   │   │       ├── security/       # Security configuration
│   │   │       └── services/      # Business logic
│   │   └── resources/
│   │       ├── static/            # CSS, JS files
│   │       └── templates/        # Thymeleaf templates
│   └── test/                      # Test files
└── pom.xml
```

## 🔐 Default Users

After first run, you may need to create users through the registration endpoint or admin panel.

## 🐛 Troubleshooting

### Database Connection Issues
- Verify MySQL is running: `mysql -u root -p`
- Check database credentials in `application.properties`
- Ensure the database exists or `createDatabaseIfNotExist=true` is set

### Microservice Connection Issues
- Ensure the attendance microservice is running on port 8081
- Verify the JWT secret matches in both applications
- Check the microservice URL in `application.properties`

### Port Already in Use
- Change the port in `application.properties`: `server.port=8082`
- Or stop the process using port 8080

## 📝 Notes

- The application uses `spring.jpa.hibernate.ddl-auto=update`, which automatically creates/updates database schema
- For production, consider using `validate` or `none` and manage schema migrations properly
- Database credentials are currently in `application.properties` - use environment variables or a secrets manager for production

## 📄 License

This project is part of a learning exercise.

## 👤 Author

tgadzhalov

