FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/Paperless_components-0.0.1-SNAPSHOT.jar /app/paperless.jar
ENTRYPOINT ["java", "-jar", "/app/paperless.jar"]
