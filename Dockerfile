FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q -DskipTests package && mv target/*.jar app.jar

FROM eclipse-temurin:25-jre
# The JRE image ships without curl (or wget) — needed for the HEALTHCHECK.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/app.jar app.jar

EXPOSE 5000
CMD ["java", "-jar", "app.jar"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -fsS --max-time 4 http://127.0.0.1:5000/actuator/health/liveness || exit 1