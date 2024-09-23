FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

# Step 2: Kopiere deine Projektdateien
COPY . .

# Step 3: Baue das Projekt
RUN mvn install clean package -DskipTests

# Step 4: Verwende ein leichtgewichtiges OpenJDK Runtime Image
FROM openjdk:17-jdk-alpine
WORKDIR /app

# Step 5: Kopiere das fertige JAR-File von der Build-Stage
COPY --from=build /app/target/paperless.jar /app/paperless.jar

# Step 6: Starte die Anwendung
ENTRYPOINT ["java", "-jar", "/app/deine-app.jar"]
