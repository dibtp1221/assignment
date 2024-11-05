FROM gradle:8.10-jdk17 AS build
WORKDIR /app

# ARG로 전달된 파일 디렉터리 설정
ARG FILE_DIRECTORY

# 파일 복사
COPY $FILE_DIRECTORY /app

# auth 모듈만 빌드
RUN ./gradlew :service:clean :service:build -x test --no-daemon

FROM openjdk:17.0.1

COPY --from=build /app/service/build/libs/*SNAPSHOT.jar /app.jar

EXPOSE 8080

# JAR 파일 실행
CMD ["java", "-jar", "/app.jar"]