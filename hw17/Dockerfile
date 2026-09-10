FROM node:22.21.1-alpine AS frontend
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-17 AS deps
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline

FROM deps AS dev
WORKDIR /app
COPY . .
CMD ["mvn", "-B", "spring-boot:run"]

FROM deps AS build
WORKDIR /app
COPY . .
COPY --from=frontend /frontend/dist ./src/main/resources/static
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jre-jammy AS runtime
RUN groupadd -r app && useradd -r -g app app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
