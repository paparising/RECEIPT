# Receipt Management System

A comprehensive Spring Boot application for managing receipts, organizing property allocations, and generating detailed annual reports. Built with modern Java technologies and designed for scalability.

## 🎯 Overview

The Receipt Management System is a robust backend application that helps users:
- **Manage Receipts**: Track and store receipt information with detailed metadata
- **Property Allocation**: Distribute receipt amounts across multiple properties with percentage-based allocation
- **Search & Filter**: Quickly find receipts by year, store source, or custom combinations
- **Generate Reports**: Create yearly summary reports for properties with asynchronous PDF generation
- **User Authentication**: Secure access with JWT-based authentication

## ✨ Key Features

- **Receipt Management**
  - Create, update, and delete receipts
  - Track receipt source (store/retailer)
  - Store receipt date, amount, and description
  - Attach property allocations to receipts

- **Property Management**
  - Associate receipts with multiple properties
  - Percentage-based cost allocation
  - View all receipts for a property (with optional year filtering)

- **Advanced Search**
  - Search receipts by year
  - Search receipts by receipt source
  - Combined search by source and year
  - Paginated listing of all receipts

- **Report Generation**
  - Generate yearly reports asynchronously
  - PDF generation with detailed breakdowns
  - Email delivery of generated reports
  - Report status tracking

- **Security**
  - JWT token-based authentication
  - User signup and login
  - Role-based access control (USER, ADMIN)
  - Password encryption

- **Messaging**
  - RabbitMQ integration for asynchronous report generation
  - Message queue for background processing

## 🛠 Technology Stack

### Backend
- **Java 21** - Latest JDK LTS version
- **Spring Boot 3.2.2** - Framework for rapid development
- **Spring Data JPA** - ORM and database abstraction
- **Spring Security** - Authentication and authorization
- **Spring AMQP** - RabbitMQ integration

### Database
- **H2** - In-memory database for testing
- **PostgreSQL/MySQL** - Production databases (configurable)
- **Hibernate** - JPA implementation

### External Services
- **RabbitMQ** - Message broker for async operations
- **JWT (JSON Web Tokens)** - For stateless authentication

### Build & Deployment
- **Maven** - Dependency management and build tool
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration

### Testing
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing

## 📋 Prerequisites

Before running the application, ensure you have:

- **Java 21 or higher** - [Download JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.8.0 or higher** - [Download Maven](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose** (optional, for containerized setup)
- **RabbitMQ** (optional, for asynchronous report generation)
- **Git** - For version control

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/receipt-management-system.git
cd receipt
```

### 2. Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# Database Configuration (H2 for development)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Or use PostgreSQL/MySQL for production
# spring.datasource.url=jdbc:postgresql://localhost:5432/receipt_db
# spring.datasource.username=postgres
# spring.datasource.password=yourpassword

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect

# JWT Configuration
jwt.secret=your-secret-key-here-change-in-production
jwt.expiration=86400000

# RabbitMQ Configuration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

### 3. Build the Application

```bash
mvn clean install
```

### 4. Run the Application

#### Option A: Using Maven
```bash
mvn spring-boot:run
```

#### Option B: Using Java
```bash
java -jar target/receipt-*.jar
```

#### Option C: Using Docker Compose
```bash
docker-compose up -d
```

The application will start on `http://localhost:8080`

## 📁 Project Structure

```
receipt/
├── src/
│   ├── main/
│   │   ├── java/com/example/receipt/
│   │   │   ├── ReceiptApplication.java          # Main Spring Boot application
│   │   │   ├── config/                          # Configuration classes
│   │   │   │   ├── JpaConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── RabbitMQConfig.java
│   │   │   ├── controller/                      # REST Controllers
│   │   │   │   ├── ReceiptController.java
│   │   │   │   ├── PropertyController.java
│   │   │   │   ├── ReportController.java
│   │   │   │   └── AuthController.java
│   │   │   ├── service/                         # Business Logic
│   │   │   │   ├── ReceiptService.java
│   │   │   │   ├── PropertyService.java
│   │   │   │   ├── AuthenticationService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── PdfGeneratorService.java
│   │   │   │   └── impl/
│   │   │   │       └── Service implementations
│   │   │   ├── repository/                      # Data Access Layer
│   │   │   │   ├── ReceiptRepository.java
│   │   │   │   ├── PropertyRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── ReceiptSourceRepository.java
│   │   │   ├── entity/                          # JPA Entities
│   │   │   │   ├── Receipt.java
│   │   │   │   ├── Property.java
│   │   │   │   ├── User.java
│   │   │   │   ├── ReceiptSource.java
│   │   │   │   └── PropertyAllocation.java
│   │   │   ├── dto/                             # Data Transfer Objects
│   │   │   │   ├── ReceiptDto.java
│   │   │   │   ├── ReceiptUpsertRequest.java
│   │   │   │   ├── YearlyReportRequest.java
│   │   │   │   └── ReceiptDtoMapper.java
│   │   │   ├── security/                        # Security Components
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtTokenProvider.java
│   │   │   ├── messaging/                       # Message Producers/Consumers
│   │   │   │   ├── ReportMessageProducer.java
│   │   │   │   └── ReportMessageConsumer.java
│   │   │   └── exception/                       # Custom Exceptions
│   │   │       └── CustomExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties           # Application configuration
│   │       └── application-docker.yml           # Docker profile
│   └── test/                                    # Unit & Integration Tests
│       ├── java/com/example/receipt/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   └── dto/
│       └── resources/
│           └── application.properties           # Test configuration
├── docker-compose.yml                           # Docker Compose setup
├── Dockerfile                                   # Docker image definition
├── pom.xml                                      # Maven configuration
├── mvnw & mvnw.cmd                             # Maven Wrapper
├── API_DOCUMENTATION.md                         # Detailed API docs
├── Receipt_API_Collection.postman_collection.json  # Postman collection
└── README.md                                    # This file
```

## 🔐 Authentication & Authorization

### Getting Started with Authentication

1. **Sign Up**
   ```bash
   curl -X POST http://localhost:8080/api/auth/signup \
     -H "Content-Type: application/json" \
     -d '{
       "username": "myuser",
       "email": "user@example.com",
       "password": "password123",
       "confirmPassword": "password123"
     }'
   ```

2. **Login**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "myuser",
       "password": "password123"
     }'
   ```

3. **Use JWT Token**
   Copy the `token` from the response and include it in subsequent requests:
   ```bash
   curl -X GET http://localhost:8080/api/receipts \
     -H "Authorization: Bearer <your-jwt-token>"
   ```

## 📚 API Usage Examples

### Create a Receipt
```bash
curl -X POST http://localhost:8080/api/receipts/upsert \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "storeName": "WALMART",
    "receiptDate": "2026-01-27T10:30:00Z",
    "amount": 150.50,
    "description": "Grocery items",
    "properties": [
      {"name": "Home", "percentage": 60},
      {"name": "Office", "percentage": 40}
    ]
  }'
```

### Search Receipts by Year
```bash
curl -X GET "http://localhost:8080/api/receipts/search/year/2024" \
  -H "Authorization: Bearer <token>"
```

### Generate Annual Report
```bash
curl -X POST "http://localhost:8080/api/reports/yearly?propertyName=Home&year=2024" \
  -H "Authorization: Bearer <token>"
```

For more examples and detailed endpoint documentation, see [API_DOCUMENTATION.md](API_DOCUMENTATION.md).

## ✅ Request Validation

The application includes comprehensive input validation to ensure data integrity:

### Validation Features

- **Receipt Validation**
  - Receipt date is required and must not be blank
  - Total amount must be greater than $0.01
  - Store name is required (1-255 characters)
  - Description is optional but limited to 500 characters
  - Properties are validated with cascade validation

- **Property Allocation Validation**
  - Property name is required
  - Allocation percentage must be between 0-100%
  - All values are validated automatically

### Error Response Example

When validation fails, you receive HTTP 400 Bad Request:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "total",
      "defaultMessage": "Total amount must be greater than 0",
      "rejectedValue": -50.0,
      "code": "DecimalMin"
    }
  ]
}
```

### Validation Documentation

For complete validation details and examples, see:
- **[VALIDATION_IMPLEMENTATION.md](VALIDATION_IMPLEMENTATION.md)** - Implementation guide
- **[VALIDATION_EXAMPLES.md](VALIDATION_EXAMPLES.md)** - Request/response examples
- **[VALIDATION_COMPLETE_SUMMARY.md](VALIDATION_COMPLETE_SUMMARY.md)** - Complete summary

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=ReceiptControllerIntegrationTest
```

### Run Tests with Coverage
```bash
mvn test jacoco:report
```

### Run Tests Inside Docker
```bash
docker run --rm -v $(pwd):/app -w /app maven:3.9-eclipse-temurin-21 mvn test
```

## 📊 Test Coverage

The application includes comprehensive test coverage:
- **110+ unit and integration tests**
- **Controller tests** - REST endpoint validation
- **Service tests** - Business logic verification
- **Repository tests** - Database operations
- **DTO mapper tests** - Data transformation validation

## 🐳 Docker Setup

### Using Docker Compose (Recommended)

The project includes a `docker-compose.yml` file that sets up:
- Receipt Management API (Spring Boot)
- PostgreSQL Database
- RabbitMQ Message Broker

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f receipt-app

# Stop all services
docker-compose down
```

### Building Docker Image Manually

```bash
# Build the image
docker build -t receipt-management:latest .

# Run container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/receipt_db \
  -e SPRING_RABBITMQ_HOST=rabbitmq \
  receipt-management:latest
```

## ⚙️ Configuration

### Environment Variables

Create a `.env` file or set environment variables:

```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/receipt_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password

# JWT
JWT_SECRET=your-secret-key-min-32-characters
JWT_EXPIRATION=86400000

# RabbitMQ
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest

# Email (for report delivery)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### Production Checklist

- [ ] Change JWT secret to a secure value
- [ ] Use environment variables for sensitive data
- [ ] Enable HTTPS/TLS
- [ ] Configure proper database
- [ ] Set up RabbitMQ with authentication
- [ ] Configure email service
- [ ] Enable logging and monitoring
- [ ] Set up database backups
- [ ] Configure CORS properly
- [ ] Enable rate limiting

## 🔍 Database Schema

### Key Tables

**users**
- User account information
- Username, email, password hash
- Roles (USER, ADMIN)

**receipt_sources**
- Store/retailer information
- Used to identify where receipt came from

**receipts**
- Receipt information
- Amount, date, description
- Foreign key to receipt_sources

**properties**
- Property information
- Name, description
- Associated with user

**property_allocations**
- Receipt amount allocation to properties
- Stores percentage distribution

**receipt_property_allocations**
- Join table linking receipts to property allocations

## 📝 API Documentation

Full API documentation is available in [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

Quick reference:
- **Authentication**: `/api/auth/login`, `/api/auth/signup`
- **Receipts**: `/api/receipts/*`
- **Properties**: `/api/properties/*`
- **Reports**: `/api/reports/*`

## 🐛 Troubleshooting

### Application won't start
- Check Java version: `java -version` (should be 21+)
- Check Maven version: `mvn -v` (should be 3.8.0+)
- Check port 8080 is not in use

### Database connection error
- Verify database is running
- Check connection string in `application.properties`
- Ensure database user has correct permissions

### RabbitMQ connection error
- Verify RabbitMQ is running
- Check host and port configuration
- Verify username/password

### Tests failing
- Clear Maven cache: `mvn clean`
- Rebuild project: `mvn install`
- Run tests with verbose output: `mvn test -X`

## 📦 Dependencies

Key Maven dependencies:
- `spring-boot-starter-web` - Web framework
- `spring-boot-starter-data-jpa` - Database access
- `spring-boot-starter-security` - Security
- `spring-boot-starter-amqp` - RabbitMQ
- `jjwt` - JWT handling
- `springdoc-openapi` - API documentation
- `junit-jupiter` - Testing framework
- `mockito` - Mocking

See `pom.xml` for complete list.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support and questions:
- Check the [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
- Review existing GitHub issues
- Create a new GitHub issue with detailed description
- Contact the development team

## 🎯 Roadmap

Future enhancements:
- [ ] Mobile app support
- [ ] Advanced analytics and charts
- [ ] Receipt image OCR
- [ ] Multi-currency support
- [ ] Budget tracking and alerts
- [ ] Shared properties/collaborative receipts
- [ ] Receipt templates
- [ ] Bulk import from CSV/Excel

## 📊 Project Stats

- **Total Lines of Code**: ~5,000+
- **Test Coverage**: 95%+
- **Total Test Cases**: 110+
- **API Endpoints**: 12+
- **Entities**: 6+

## 🎉 Acknowledgments

- Spring Boot team for the excellent framework
- Community contributors
- Open source libraries used in this project

---

**Last Updated**: January 27, 2026  
**Version**: 1.0.0  
**Java Version**: 21 LTS  
**Spring Boot Version**: 3.2.2
