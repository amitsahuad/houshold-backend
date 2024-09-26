FROM maven:3.9.6-eclipse-temurin-22-jammy AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:22-jdk-slim
COPY --from=build /target/household-0.0.1-SNAPSHOT.jar household.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","household.jar"]