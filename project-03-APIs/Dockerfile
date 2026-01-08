FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

RUN mkdir -p /app/data

VOLUME ["/app/data"]

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

USER mchevula