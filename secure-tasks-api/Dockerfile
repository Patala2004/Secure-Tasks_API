# Dockerfile
FROM eclipse-temurin:21-jre-alpine@sha256:704db3c40204a44f471191446ddd9cda5d60dab40f0e15c6507b815ed897238b
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY target/secure-tasks-api-0.0.1-SNAPSHOT.jar app.jar
RUN chown appuser:appgroup /app/app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]