## Rabobank Assignment for Authorizations Area

### This project provides RESTFull APIs for access Power Of Attorney and Account information

Power of Attorney API:
 - Granting Read/Write access for Payment and Savings accounts
 - Granted Accounts
 - List of Power Of Attorneys

Account API Endpoints:
- Creating Payment or Savings Account
- Account information
- Accounts

### API documentation
- [Swagger UI](http://localhost:8080/swagger-ui/index.html) http://localhost:8080/swagger-ui/index.html

### Local Environment preparation

Install next soft
 - Java 11
 - Maven
 - Intellij Idea

### How to Build project
- Setup Java
   - Project Settings -> Project -> Setup Project SDK to Java 11, Project language level also to 11
- Maven build:
```bash
mvn clean install
```

### How to Run Application
start `nl.rabobank.RaboAssignmentApplication` from IDE
Or run from `api` folder
```bash
mvn spring-boot:run
```
or
```bash
java -jar ./api/target/rabobank-assignment-api-0.0.1-SNAPSHOT.jar
```
### Application run on
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### How to Run Tests
```bash
mvn test
```

### Original Rabobank Assignment task
[Rabobank Assignment](./TASK.md)