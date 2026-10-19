# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build skip tests for faster deployment, remove -DskipTests if you want to run tests during build
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache tzdata
ENV TZ=America/Sao_Paulo
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY public.pub .
COPY private.pem .

# Create upload directory
RUN mkdir -p uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
