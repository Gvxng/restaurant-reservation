# ============================================
# MULTI-STAGE BUILD
# ============================================
# This Dockerfile uses a multi-stage build approach to:
# 1. Build the application in one stage
# 2. Run the application in a smaller runtime image
# This reduces the final image size significantly

# ============================================
# STAGE 1: BUILD STAGE
# ============================================
# Use Gradle with Java 17 as the base image for building
FROM gradle:8.5-jdk17-alpine AS build

# Set the working directory inside the container
# All subsequent commands will be executed from this directory
WORKDIR /app

# Copy the Gradle wrapper and build files first
# Copying these separately allows Docker to cache dependencies
# Dependencies won't be re-downloaded unless these files change
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Make gradlew executable (in case permissions are lost)
RUN chmod +x gradlew

# Download all dependencies defined in build.gradle
# This layer will be cached if build.gradle hasn't changed
RUN ./gradlew dependencies --no-daemon

# Copy the entire source code into the container
# This includes src/, resources/, etc.
COPY src ./src

# Build the application
# - clean: removes previous build artifacts
# - bootJar: creates an executable JAR file (Spring Boot)
# - --no-daemon: prevents Gradle daemon from running (better for containers)
# - -x test: skips running tests during build (optional, remove to run tests)
RUN ./gradlew clean bootJar --no-daemon -x test

# ============================================
# STAGE 2: RUNTIME STAGE
# ============================================
# Use a smaller JRE-only image for running the application
# This significantly reduces the final image size (no Gradle, no build tools)
FROM eclipse-temurin:17-jre-alpine

# Set the working directory for the runtime container
WORKDIR /app

# Copy the built JAR file from the build stage
# The JAR file is located in /app/build/libs/ from the build stage
# Rename it to app.jar for simplicity
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port 8080
# This is the default port Spring Boot runs on
# This doesn't actually publish the port, it's documentation
# Use -p flag when running docker run to publish the port
EXPOSE 8080

# Set environment variables
# These can be overridden when running the container
ENV SPRING_PROFILES_ACTIVE=default
ENV JAVA_OPTS=""

# Health check to verify the application is running
# Docker will periodically check if the app is healthy
# Adjust the URL if your app has a different health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Define the command to run the application
# ENTRYPOINT defines the base command that always runs
# CMD provides default arguments that can be overridden
# The sh -c allows for environment variable expansion
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ============================================
# OPTIONAL: Add metadata labels
# ============================================
LABEL maintainer="hhichri@champlaincollege.qc.ca"
LABEL version="1.0"
LABEL description="HandymanApp - Service Request Management System"