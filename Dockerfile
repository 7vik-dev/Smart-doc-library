# Stage 1: Build React Frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend-react
COPY frontend-react/package*.json ./
RUN npm install
COPY frontend-react/ ./
RUN npm run build

# Stage 2: Build Spring Boot Backend
FROM maven:3.8.5-openjdk-17 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Copy React build to Spring Boot static resources
COPY --from=frontend-build /app/frontend-react/dist ./src/main/resources/static
RUN mvn clean package -DskipTests

# Stage 3: Run
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=backend-build /app/target/smart-doc-library-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
