# OrderManagementSystemBackEnd

# Quick Setup for `application-dev.properties` (Spring Boot)

Create `application-dev.properties` in `src/main/resources`:

```properties

spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DB:order_management_system}?serverTimezone=UTC
spring.datasource.username=${MYSQL_USER:orderuser}
spring.datasource.password=${MYSQL_PASSWORD:1234}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.open-in-view=false
```

Defaults will be used if environment variables are missing. Make sure MySQL is running and the database/user exist:

```sql
CREATE DATABASE order_management_system;
CREATE USER 'orderuser'@'localhost' IDENTIFIED BY '1996';
GRANT ALL PRIVILEGES ON order_management_system.* TO 'orderuser'@'localhost';
FLUSH PRIVILEGES;
```

Restart the app; `dev` profile should load correctly. Disable SQL init scripts if not used:

```properties
spring.sql.init.mode=never
```
