OrderManagementSystemBackEnd
Quick Setup for application-dev.properties (Spring Boot)

Create application-dev.properties in src/main/resources:

spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:order_management_system}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:1234}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.open-in-view=false

Defaults will be used if environment variables are missing. Make sure PostgreSQL is running and the database/user exist:

CREATE DATABASE order_management_system;

CREATE USER orderuser WITH PASSWORD '1996';

GRANT ALL PRIVILEGES ON DATABASE order_management_system TO orderuser;

Restart the app; dev profile should load correctly. Disable SQL init scripts if not used:

spring.sql.init.mode=never