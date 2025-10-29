FROM maven:3.9.11-eclipse-temurin-25 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B 

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine AS runner

# Créer un utilisateur non root et un groupe non root pour exécuter l'application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /app/target/auth-service-*.jar ./app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/heapdump.hprof"

# Donner les permissions nécessaires à l'utilisateur non root
RUN chown -R appuser:appgroup /app

# Passer à l'utilisateur non root
USER appuser

EXPOSE 4000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]