# Multi-stage build: compile with Gradle, run on JRE
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# Pre-copy build scripts and wrapper to leverage layer caching
COPY gradlew gradlew.bat build.gradle settings.gradle /workspace/
COPY gradle /workspace/gradle
COPY src /workspace/src
COPY src/main/proto /workspace/src/main/proto

# Build the application JAR
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the fat JAR from the builder stage
COPY --from=builder /workspace/build/libs/*-SNAPSHOT.jar /app/app.jar

EXPOSE 9090
ENTRYPOINT ["java","-jar","/app/app.jar"]
