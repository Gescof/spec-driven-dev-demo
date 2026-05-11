FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S appuser && adduser -S appuser -G appuser
WORKDIR /app
COPY --from=builder /build/target/*.jar /app/app.jar
RUN chown appuser:appuser /app/app.jar
USER appuser
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
