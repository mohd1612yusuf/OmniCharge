# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy parent POM
COPY pom.xml .

# Copy ALL module POMs so Maven resolves the multi-module structure
COPY eureka-server/pom.xml eureka-server/
COPY api-gateway/pom.xml api-gateway/
COPY user-service/pom.xml user-service/
COPY operator-service/pom.xml operator-service/
COPY recharge-service/pom.xml recharge-service/
COPY payment-service/pom.xml payment-service/
COPY notification-service/pom.xml notification-service/

# Copy only THIS service's source
COPY eureka-server/src eureka-server/src/

RUN mvn clean package -pl eureka-server -am -DskipTests

# Stage 2: Lightweight runtime image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/eureka-server/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
