FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

RUN mvn clean test package -DskipTests=false

FROM eclipse-temurin:21-jre

WORKDIR /app

# Force layer rebuild by adding a build argument
ARG CACHEBUST=1
COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/data

VOLUME ["/app/data"]

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

USER root