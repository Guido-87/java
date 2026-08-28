# Build
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY . .
RUN java -version
RUN mvn -version
RUN mvn -B -DskipTests package

# Runtime
FROM gcr.io/distroless/java25-debian13
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]