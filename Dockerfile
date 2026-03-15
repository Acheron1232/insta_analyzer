# syntax=docker/dockerfile:1.7

FROM node:22-alpine AS frontend-builder
WORKDIR /frontend

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend ./
RUN npm run build

FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace

COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle
COPY gradle gradle
COPY src src
COPY --from=frontend-builder /frontend/dist src/main/resources/static

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jdk
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

RUN mkdir -p /app/data && chown -R app:app /app

ENV SPRING_DATASOURCE_URL=jdbc:sqlite:/app/data/inst_bot.db

EXPOSE 8080
USER app

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
