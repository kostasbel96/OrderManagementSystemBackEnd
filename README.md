# Order Management System

A robust Spring Boot application designed to manage orders, products, and customers efficiently. This system provides a RESTful API for performing CRUD operations and includes advanced features like pagination, sorting, and specification-based searching. It also incorporates Spring Security with JWT for authentication and authorization, including an admin user.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Database Setup](#database-setup)
  - [Configuration](#configuration)
  - [Running the Application](#running-the-application)
- [Security](#security)
  - [Admin User](#admin-user)
  - [JWT Configuration](#jwt-configuration)
- [API Endpoints](#api-endpoints)
  - [Authentication](#authentication)
  - [Products](#products)
  - [Orders](#orders)
  - [Customers](#customers)
- [Usage Examples (Postman)](#usage-examples-postman)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Product Management**: Add, view, update, delete (soft delete) products.
- **Order Management**: Create orders with multiple items, view, and manage orders.
- **Customer Management**: Manage customer details.
- **RESTful API**: Expose all functionalities via a well-structured REST API.
- **Pagination & Sorting**: Efficiently retrieve large datasets with pagination and custom sorting.
- **Search & Filtering**: Advanced search capabilities using Spring Data JPA Specifications (e.g., search products by name, orders by customer name).
- **Data Validation**: Input validation for DTOs.
- **Error Handling**: Centralized exception handling.
- **Auditing**: Automatic tracking of creation and update timestamps for entities.
- **Database Integration**: PostgreSQL database integration.
- **Security**: JWT-based authentication and authorization with an admin user.

## Technologies Used

- **Spring Boot**: Framework for building the application.
- **Spring Data JPA**: For database interaction and repository abstraction.
- **Hibernate**: JPA implementation.
- **PostgreSQL**: Relational database.
- **Lombok**: To reduce boilerplate code (getters, setters, constructors).
- **Maven**: Build automation tool.
- **Jakarta Persistence API (JPA)**: For object-relational mapping.
- **Spring Security**: For authentication and authorization (JWT).
- **SLF4J/Logback**: For logging.

## Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK)**: Version 17 or higher.
- **Maven**: Version 3.6.3 or higher.
- **Git**: For cloning the repository.
- **PostgreSQL Server**: Version 10 or higher.
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code (with Java extensions).

### Installation

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/kostasbel96/OrderManagementSystemBackEnd.git
    cd project-spring
    ```

2.  **Build the project**:
    ```bash
    mvn clean install
    ```

### Database Setup

1.  **Create a PostgreSQL database**:
    ```sql
    CREATE DATABASE order_management_db;
    ```

2.  **Update `application.properties`**: Ensure your `src/main/resources/application.properties` file has the correct database connection details.

### Configuration

Open `src/main/resources/application.properties` and configure the following:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/order_management_db
spring.datasource.username=your_postgres_username
spring.datasource.password=your_postgres_password
spring.datasource.driverClassName=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update # Use 'update' for development, 'none' for production
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.globally_quoted_identifiers=true

# Admin User Credentials (set via environment variables)
application.admin.username=${USERNAME}
application.admin.password=${USER_PASSWORD}
# Spring Data JPA
spring.data.jpa.repositories.enabled=true
spring.jpa.open-in-view=false

# Active Profile
spring.profiles.active=dev

# JWT Configuration (Example - set via environment variables in production)
# application.security.jwt.secret-key=YourSuperSecretKeyThatIsAtLeast256BitsLongAndShouldBeStoredSecurely
# jwt.expiration=86400000 # 24 hours in milliseconds
```
*(Replace `your_postgres_username` and `your_postgres_password` with your actual PostgreSQL credentials)*

### Running the Application

You can run the application using Maven:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## Security

This application uses JWT (JSON Web Tokens) for authentication and authorization.

### Admin User

The application is configured with an initial admin user. You might need to check the `application.properties` or a dedicated security configuration file (e.g., `SecurityConfig.java`) for the default admin username and password, or how to create one.

### JWT Configuration

The JWT secret key is crucial for signing and verifying tokens. It should be a strong, randomly generated string and **NEVER hardcoded in production**.

It's recommended to set the `jwt.secret` property via environment variables (e.g., `JWT_SECRET`) when deploying the application.

## API Endpoints

Here's a brief overview of the main API endpoints. For detailed request/response bodies, refer to the DTOs in `src/main/java/com/project/ordermanagementsystem/dto`.

**Note**: Most endpoints require authentication. You will need to obtain a JWT token by authenticating first and then include it in the `Authorization` header as a Bearer token (`Authorization: Bearer <your_jwt_token>`).

### Authentication

-   `POST /api/auth/login`: Authenticate a user and receive a JWT token.

### Products

-   `POST /api/products/save`: Create a new product. (Requires authentication)
-   `GET /api/products`: Get all products with pagination and default sorting. (Might be public or require authentication depending on configuration)
-   `GET /api/products/search?name={name}&page={page}&pageSize={pageSize}&sortBy={sortBy}&sortDirection={sortDirection}`: Search products by name with pagination and sorting. (Might be public or require authentication)
-   `GET /api/products/{name}`: Get a product by its exact name. (Might be public or require authentication)
-   `PUT /api/products/update`: Update an existing product. (Requires authentication)
-   `DELETE /api/products/delete`: Soft delete a product (sets `active` to false). (Requires authentication)

### Orders

-   `POST /api/orders/save`: Create a new order. (Requires authentication)
-   `GET /api/orders`: Get all orders with pagination and default sorting. (Requires authentication)
-   `GET /api/orders/search?customerName={name}&customerLastName={lastName}&orderId={id}&page={page}&pageSize={pageSize}&sortBy={sortBy}&sortDirection={sortDirection}`: Search orders by customer name, last name, or order ID. (Requires authentication)

### Customers

-   `POST /api/customers/save`: Create a new customer. (Requires authentication)
-   `GET /api/customers`: Get all customers with pagination and default sorting. (Requires authentication)
-   `GET /api/customers/{id}`: Get a customer by ID. (Requires authentication)
-   `PUT /api/customers/update`: Update an existing customer. (Requires authentication)
-   `DELETE /api/customers/delete`: Delete a customer. (Requires authentication)

## Usage Examples (Postman)

### Authenticate (Login)

-   **URL**: `POST http://localhost:8080/api/auth/login`
-   **Headers**: `Content-Type: application/json`
-   **Body (JSON)**:
    ```json
    {
        "username": "admin",
        "password": "admin_password"
    }
    ```
    *(Replace `admin` and `admin_password` with your actual admin credentials)*
    **Response**: You will receive a JWT token in the response. Copy this token for subsequent authenticated requests.

### Create a Product (Authenticated)

-   **URL**: `POST http://localhost:8080/api/products/save`
-   **Headers**:
    *   `Content-Type: application/json`
    *   `Authorization: Bearer <your_jwt_token>` *(Replace `<your_jwt_token>` with the token obtained from login)*
-   **Body (JSON)**:
    ```json
    {
        "name": "Laptop Pro",
        "description": "High-performance laptop",
        "quantity": 10,
        "price": "1200.00"
    }
    ```

### Search Products (Authenticated)

-   **URL**: `GET http://localhost:8080/api/products/search?name=laptop&page=0&pageSize=5&sortBy=name&sortDirection=ASC`
-   **Headers**:
    *   `Authorization: Bearer <your_jwt_token>`

### Create an Order (Authenticated)

-   **URL**: `POST http://localhost:8080/api/orders/save`
-   **Headers**:
    *   `Content-Type: application/json`
    *   `Authorization: Bearer <your_jwt_token>`
-   **Body (JSON)**:
    ```json
    {
        "customerId": 1,
        "address": "123 Main St, City",
        "items": [
            {
                "productId": 1,
                "quantity": 1
            },
            {
                "productId": 2,
                "quantity": 2
            }
        ]
    }
    ```

## Contributing

Contributions are welcome! If you have suggestions for improvements or find any bugs, please open an issue or submit a pull request.

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.
