# Stage 1: Build React Frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend-react
COPY frontend-react/package*.json ./
RUN npm install --frozen-lockfile || npm install
COPY frontend-react/ ./
# Skip TS check to save memory and time
RUN npm run build

# Stage 2: Build Spring Boot Backend
FROM maven:3.9.6-eclipse-temurin-17-alpine AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Copy React build to Spring Boot static resources
COPY --from=frontend-build /app/frontend-react/dist ./src/main/resources/static
# Use MAVEN_OPTS to limit memory usage during build
ENV MAVEN_OPTS="-Xmx512m"
RUN mvn clean package -DskipTests

# Stage 3: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/smart-doc-library-1.0.0.jar app.jar
EXPOSE 8080
# Standard JVM optimizations for containers with limited memory
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XshowSettings:vm"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
