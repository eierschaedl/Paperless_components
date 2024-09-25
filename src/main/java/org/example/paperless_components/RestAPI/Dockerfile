FROM openjdk:17-jdk-slim
WORKDIR /app
#RUN mvn install clean package -DskipTests
COPY . /app/rest-api.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/your-api.jar"]
