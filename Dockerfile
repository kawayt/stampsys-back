# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Gradle Wrapperと設定ファイルをコピー
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle

# Windows環境で作成されたgradlewの改行コード対策
RUN sed -i 's/\r$//' gradlew
RUN chmod +x gradlew

# ソースコードをコピー
COPY src ./src

# テストをスキップしてビルド (Renderでのビルド時間短縮のため)
RUN ./gradlew bootJar -x test

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# ビルドステージで生成されたJARファイルをコピー
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
