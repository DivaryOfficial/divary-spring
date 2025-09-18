# --- 1단계: 빌드 스테이지 ---
FROM openjdk:21-jdk-slim as builder
WORKDIR /app
COPY build.gradle settings.gradle .
COPY gradle ./gradle
RUN ./gradlew dependencies --build-cache || return 0
COPY src ./src
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

# --- 2단계: 실행 스테이지 ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
COPY --from=builder --chown=appuser:appgroup /app/build/libs/*.jar app.jar
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]