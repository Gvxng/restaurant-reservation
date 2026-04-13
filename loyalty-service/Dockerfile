# syntax=docker/dockerfile:1.7
FROM gradle:9.3.1-jdk17-alpine AS build

WORKDIR /app

COPY settings.gradle ./
COPY build.gradle ./
COPY src ./src

RUN --mount=type=cache,id=gradle-loyalty,target=/home/gradle/.gradle,sharing=locked \
    sh -c 'for i in 1 2 3; do gradle bootJar --no-daemon -x test && exit 0; echo "Gradle build failed, retrying in 10s..."; sleep 10; done; exit 1'

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
