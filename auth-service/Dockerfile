FROM gradle:8.14.2-jdk21 AS build

WORKDIR /home/gradle/project

COPY build.gradle .
COPY settings.gradle .
COPY src ./src

RUN gradle --no-daemon clean build -x test

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8082

CMD ["java", "-jar", "app.jar"]