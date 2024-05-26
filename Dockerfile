FROM openjdk:21-jdk-slim as builder

WORKDIR /app

COPY . .

RUN ./mvnw dependency:go-offline
RUN ./mvnw clean install -DskipTests


FROM openjdk:21-jdk-slim

WORKDIR /app
EXPOSE 9000

COPY --from=builder /app/target/*.jar /app/*.jar

ENTRYPOINT ["java", "-jar", "/app/*.jar"]
