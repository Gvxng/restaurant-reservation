FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle ./
COPY reservation-service reservation-service
COPY menu-service menu-service
COPY loyalty-service loyalty-service
COPY api-gateway-service api-gateway-service

RUN chmod +x gradlew && ./gradlew :reservation-service:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /workspace/reservation-service/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
