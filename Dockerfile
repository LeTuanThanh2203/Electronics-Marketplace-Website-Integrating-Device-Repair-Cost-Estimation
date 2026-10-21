# =========================================================================
# Multi-stage Dockerfile for Electronics Marketplace (Jakarta EE on Tomcat)
# Optimized for Render.com Web Service
# =========================================================================

# Stage 1: Build the WAR artifact
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build WAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime Environment with Apache Tomcat 11 (Jakarta EE 11 / Servlet 6.1)
FROM tomcat:11.0-jdk17-temurin
WORKDIR /usr/local/tomcat

# Clean default Tomcat web applications
RUN rm -rf webapps/*

# Deploy the application as ROOT.war so it serves directly at /
COPY --from=builder /app/target/*.war webapps/ROOT.war

# Entrypoint script: dynamically configure port according to Render's $PORT env
RUN printf '#!/bin/sh\n\
PORT="${PORT:-8080}"\n\
sed -i "s/port=\\\"8080\\\"/port=\\\"$PORT\\\"/g" conf/server.xml\n\
echo "Starting Tomcat on port $PORT..."\n\
exec catalina.sh run\n' > /entrypoint.sh && chmod +x /entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
