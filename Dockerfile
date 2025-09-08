# 1. 베이스 이미지 설정 (OpenJDK 21 사용)
FROM eclipse-temurin:21-jre-jammy

# 2. 빌드된 JAR 파일명을 인자로 받기
ARG JAR_FILE=build/libs/*.jar

# 3. JAR 파일을 컨테이너 내부로 복사
COPY ${JAR_FILE} app.jar

# 4. 컨테이너가 외부에 노출할 포트 설정
EXPOSE 8080

# 5. 컨테이너가 시작될 때 실행할 명령어
ENTRYPOINT ["exec", "java", "-jar", "/app.jar"]