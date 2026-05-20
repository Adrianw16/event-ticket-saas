# -- Stage 1: Build JAR with Maven
# -- Uses maven:3.9-eclipse-temurin-21 to compile and package
# -- Thus stage takes more time but only used during build
FROM maven:3.9-eclipse-temurin-21 AS builder

# -- Set working directory inside container
WORKDIR /app

# -- Copy pom.xml and download all Maven dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:resolve

# -- Copy source code and compile package into JAR (Skips tests, they were run in the CI)
COPY src ./src
RUN mvn clean package -DskipTests


# -- Stage 2: Runtime with minimal image
# -- Uses eclipse-temurin:21-jre-alpine (much smaller, only JRE not SDK)
# -- This is the final image deployed to prod
FROM eclipse-temurin:21-jre-alpine

# -- Set working directory
WORKDIR /app

# -- Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# -- Health check: curl Spring Boot actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider httpl://localhost:8080/health || exit 1

# -- Expose port 8080
EXPOSE 8080

# -- Run springboot JAR
ENTRYPOINT ["java", "-jar", "app.jar"]