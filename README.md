# Product Service

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

This service handles:
- Products
- Bundles
- Allergens
- Reservations

This service handles the relationship between products, bundles, allergens and reservations, allowing users and vendors to access the endpoints. 
This service pushes messages to RabbitMQ when a vendor verifies a claim code, so the streak on the User Service can be updated.
The Image below highlights where in the architecture this service operates.

<p>
  <img src="Architecture_ProductService.png" alt="Project Logo" height="500px">
</p>

## Documentation

[![Swagger Docs](https://img.shields.io/badge/Swagger-OpenAPI%20Docs-85EA2D?style=for-the-badge&logo=openapi-initiative&logoColor=black)](https://thelastfork.shop/api/productservice/docs)

## Tech Stack

### Core & Build

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Maven](https://img.shields.io/badge/Apache%20Maven-%23C71A36.svg?style=for-the-badge&logo=Apache%20Maven&logoColor=white)

### Database
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)

### Deployment
![Kubernetes](https://img.shields.io/badge/kubernetes-%23326ce5.svg?style=for-the-badge&logo=kubernetes&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

### Messaging
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-%23FF6600.svg?style=for-the-badge&logo=rabbitmq&logoColor=white)

### Testing
![JUnit 5](https://img.shields.io/badge/Junit5-%2325A162.svg?style=for-the-badge&logo=junit5&logoColor=white)


## How to Run Tests
> Intructions for **all microservices** can be found on the [**LocalDeployment**](https://github.com/Team-Tiger1/LocalDeployment) repo but the below instructions are for running just the Product Service tests
### Requirements for running tests
- **Git**
- **Java JDK 17+**

## Run Tests

### Run Tests (Windows)

1. Open Terminal, Clone and open this repository
```Bash
  git clone https://github.com/Team-Tiger1/ProductService

  cd ProductService
```

2. run the following command to run tests
```Bash
    ./mvnw.cmd test
```
3. If successful you should see somthing similar to
```Bash
[INFO] Results:
[INFO]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  8.860 s
[INFO] Finished at: 2026-02-11T22:21:36Z
[INFO] ------------------------------------------------------------------------
```

###  Run Tests (Linux/MacOs)
1. Clone this repository
``` Bash
    git clone https://github.com/Team-Tiger1/ProductService

  cd ProductService
```

2. run the following commands to give access for maven to be executable and to run tests
```Bash
    chmod +x mvnw
    ./mvnw test
```

3. If successful you should see somthing similar to
```Bash
[INFO] Results:
[INFO]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  8.860 s
[INFO] Finished at: 2026-02-11T22:21:36Z
[INFO] ------------------------------------------------------------------------
```



## Contribution

**Author: Robert Rainer**

<br>

**Author: Daniel Jackson**
- Led code structure discussions with Robert Rainer about using the MVC model
- Setup Bundle Components:
    - Created Bundle Endpoints and specified the data required in the request
    - Created endpoints that support pagination for SQL queries
    - Validated incoming requests data at the DTO layer
    - Wrote SQL Queries that joined multiple tables
    - Defined Bundle and BundleProducts (joining table) database tables using Spring Boot JPA
    - Wrote business logic in Bundle Service Layer that accesses the database
    - Defined Custom Exceptions to improve visibility in the logs
- Setup Reservation Components:
    - Created Reservation Endpoints and specified the data required in the request
    - Validated incoming request data at the DTO layer
    - Defined Reservation and Claim code database tables and linked them
    - Defined RabbitMQ configuration and published messages to the queue
    - Defined Custom Exceptions to improve visibility in the logs
    - Wrote business logic in the Reservation Service Layer that accesses the database
- Added OpenAPI documentation to improve visibility of the backend for the front-end developers
- Enforced Controller-Service-Repository model to improve consistency across services for developers
- Used Maven Licensing Plugin to check permissions of dependency licenses (Software Inventory)
- Created README file to show details about the repository

<br>

**Author: Jed Leas**


- Setting up all CI/CD workflows to handle
  1. Automatic testing on push of main branch on the Product Service repo
  2. Automatic Deployment onto k3s with zero downtime on compleation of automatic testing so broken code won't make it to deployment
- Set up the connection to the Postgres database, and RabbitMq

<br>

**Author: Ivy Figari**