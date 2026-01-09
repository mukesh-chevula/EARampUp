FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

# Force layer rebuild by adding a build argument
ARG CACHEBUST=1
COPY --from=build /app/build/libs/*.jar app.jar

RUN mkdir -p /app/data

VOLUME ["/app/data"]

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

USER root