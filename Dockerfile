FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/caro-backend.jar /app/caro-backend.jar

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "/app/caro-backend.jar"]