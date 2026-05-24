FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY pom.xml .
COPY checkstyle checkstyle
COPY src src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV JAVA_OPTS="-Xms256m -Xmx256m -Xmn64m -XX:MaxTenuringThreshold=1"

COPY --from=builder /workspace/target/ops-fault-lab-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
