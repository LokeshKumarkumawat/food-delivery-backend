# syntax=docker/dockerfile:1.7
# Multi-stage build. Three reasons it looks like this:
#   1. Dependencies resolve in their own layer, so a code change does not
#      re-download Maven Central.
#   2. The jar is exploded into Spring Boot layers, so a code change rebuilds
#      ~2 MB instead of ~60 MB.
#   3. The runtime image has a JRE and no build tools, which removes most of
#      the CVE surface and about 400 MB.

# ---------- stage 1: dependency cache ----------
FROM maven:3.9-eclipse-temurin-21 AS deps
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B

# ---------- stage 2: build ----------
FROM deps AS build
COPY src ./src
RUN mvn clean package -DskipTests -B
RUN mv target/*.jar target/application.jar \
 && java -Djarmode=tools -jar target/application.jar extract --layers --destination extracted

# ---------- stage 3: runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN apk add --no-cache curl tzdata \
 && addgroup -S app \
 && adduser -S -G app -h /app app

ENV TZ=Asia/Kolkata
WORKDIR /app

# Ordered least- to most-frequently changed, so Docker reuses the top layers.
COPY --from=build --chown=app:app /build/extracted/dependencies/ ./
COPY --from=build --chown=app:app /build/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /build/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /build/extracted/application/ ./

USER app
EXPOSE 8090

# MaxRAMPercentage makes the JVM read the container limit instead of the host's
# memory. Without it a JVM in a 512 Mi pod sizes its heap against the node and
# gets OOMKilled.
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "application.jar"]
