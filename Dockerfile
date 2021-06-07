FROM openjdk:11-jdk-slim as build

WORKDIR /app

COPY . .

RUN ./gradlew build --no-daemon

FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/application.jar

ENTRYPOINT ["java", "-jar", "application.jar"]
