FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .
COPY gradle/ gradle/

COPY src/ src/

RUN ./gradlew build


FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENV TAMTAM_BOTAPI_ENDPOINT "https://botapi.max.ru"

EXPOSE 80

ENTRYPOINT ["java", "-jar", "app.jar"]