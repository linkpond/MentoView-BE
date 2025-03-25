# FROM openjdk:17
# ARG JAR_FILE=build/libs/*.jar
# COPY ${JAR_FILE} app.jar
# ENTRYPOINT ["java", "-jar", "/app.jar"]

### Tesseract와 Leptonica가 설치된 베이스 이미지 사용
FROM joe1534/mentoview-tesseract:v3

### 라이브러리 경로 설정 (Tesseract와 Leptonica)
ENV TESSDATA_PREFIX=/usr/local/share
ENV LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && \
    apt-get install -y tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    dpkg-reconfigure --frontend noninteractive tzdata

# 타임존을 Asia/Seoul로 설정
#ENV TZ=Asia/Seoul

### 워킹 디렉토리 설정
WORKDIR /app

### jar 파일 app.jar로 복사
COPY build/libs/*.jar app.jar

### app.jar 실행
#CMD ["java", "-jar", "app.jar"]
ENTRYPOINT ["java", "-jar", "app.jar"]

# 타임존을 환경 변수로 설정하여 JVM 옵션으로 전달합니다.
#ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Duser.timezone=${TZ}", "-jar", "app.jar"]
