# OmniCharge ⚡

OmniCharge is a robust, scalable backend platform for mobile recharges and utility payments built using a **Java Spring Boot Microservices architecture**. It features secure JWT authentication, API Gateway routing, dynamic service discovery, and an asynchronous event-driven payment processing engine utilizing RabbitMQ.

## 🏗️ Architecture Overview

The system is composed of several decoupled and specialized microservices communicating both synchronously (via OpenFeign) and asynchronously (via RabbitMQ).

![Microservices Architecture](https://miro.medium.com/max/1400/1*yX_9q9vD69JkX5w1M8g2wA.png) <!-- Replace with your actual architecture diagram if you have one -->

### Core Microservices

- **Service Registry (`eureka-server` | Port: 8761):** Provides dynamic network service discovery for all microservices. Employs Spring Cloud Netflix Eureka.
- **API Gateway (`api-gateway` | Port: 8080):** Centralized entry point. Handles requests routing, centralized JWT authentication checks, and CORS.
- **User Service (`user-service` | Port: 8081):** Manages user registration, profiles, and highly secure JWT token generation via Spring Security.
- **Operator Service (`operator-service` | Port: 8082):** Manages telecommunication operators and their specific mobile recharge plans.
- **Recharge Service (`recharge-service` | Port: 8083):** Handles incoming recharge requests. Operates closely with the Operator Service to validate plans, saves 'Pending' recharges, and pushes payment events to the RabbitMQ exchange.
- **Payment Service (`payment-service` | Port: 8084):** Listens for events on RabbitMQ, simulates payment gateway processing, updates local transaction states, and pushes final status events back to RabbitMQ.
- **Notification Service (`notification-service` | Port: 8085):** Non-blocking service that listens to the `notification_queue` on RabbitMQ to send out success SMS/Emails to customers once their recharge is completed.

## 💻 Tech Stack
* **Java 21**
* **Spring Boot (v3.2.4)**
* **Spring Cloud** (Netflix Eureka, Spring Cloud Gateway)
* **PostgreSQL** (Relational Database)
* **RabbitMQ** (Message Broker & AMQP)
* **Docker & Docker Compose** (Containerization & Infrastructure)
* **Spring Security & JWT** (Stateless authentication & Authorization)
* **OpenAPI/Swagger** (API Documentation)
* **Maven** (Dependency Management)

## 🚀 Getting Started

### Prerequisites
* Java 21 JDK installed
* Maven installed
* Docker Desktop installed and running

### 1. Start Infrastructure via Docker
The application relies on PostgreSQL and RabbitMQ. These are defined in the `docker-compose.yml` file located at the project root for rapid spin-up.

```bash
docker-compose up -d
```
*This starts a PostgreSQL database on `localhost:5432` and a RabbitMQ instance (with management UI) on `localhost:5672` (and 15672).*

### 2. Build the Microservices
Build the entire parent project from the root directory to download dependencies and compile the `.ear`/`.jar` files.

```bash
mvn clean install
```

### 3. Run the Microservices
Start the microservices independently using your IDE (IntelliJ IDEA / Eclipse) or using the Maven wrapper. 

**Important Startup Order:**
1. Start `eureka-server` (wait until it's fully up).
2. Start `api-gateway`.
3. Start the remaining business services (order does not matter): `user-service`, `operator-service`, `recharge-service`, `payment-service`, `notification-service`.

## 📡 API Endpoints & Usage

Once started, **all requests should flow through the API Gateway (Port 8080).**

### 1. User Authentication
* **Register:** `POST http://localhost:8080/api/auth/signup`
* **Login:** `POST http://localhost:8080/api/auth/signin` (Returns your `Bearer Token`)

*All subsequent requests below require an `Authorization` Header containing `Bearer <your_token>`.*

### 2. Operator Management
* **Create Operator:** `POST http://localhost:8080/api/operators`
* **Add Plan:** `POST http://localhost:8080/api/operators/{operatorId}/plans`

### 3. Recharge Flow (The Asynchronous Magic)
* **Initiate Recharge:** `POST http://localhost:8080/api/recharges/initiate`
  * *Request Body Example:*
    ```json
    {
      "userId": 1,
      "planId": 1,
      "mobileNumber": "1234567890",
      "amount": 299.00
    }
    ```
  * *How it works:* The Recharge Service immediately saves this as `PENDING` and tells RabbitMQ it needs processing. The Payment Service processes it silently in the background, updates the database, and alerts the Notification Service to simulate an SMS trigger!

## 📖 API Documentation (Swagger)

Interactive Swagger UI documentation is available for all business services natively without needing Postman. Access them natively on their standard local ports:

- **User Service:** http://localhost:8081/swagger-ui/index.html
- **Operator Service:** http://localhost:8082/swagger-ui/index.html
- **Recharge Service:** http://localhost:8083/swagger-ui/index.html
- **Payment Service:** http://localhost:8084/swagger-ui/index.html

## 🤝 Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
