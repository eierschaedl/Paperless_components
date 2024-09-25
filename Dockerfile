FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

# Kopiere deine Projektdateien ins /app-Verzeichnis
COPY . .

# Baue das Projekt und erzeuge die JAR-Datei
RUN mvn clean package -DskipTests

# Verwende ein leichtgewichtiges OpenJDK Runtime Image
FROM openjdk:17-jdk-alpine
WORKDIR /app

# Kopiere das fertige JAR-File von der Build-Stage
COPY --from=build /app/target/Paperless_components-0.0.1-SNAPSHOT.jar /app/paperless.jar

# Starte die Anwendung
ENTRYPOINT ["java", "-jar", "/app/paperless.jar"]
