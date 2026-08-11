FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /workspace/target/remindercat-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
