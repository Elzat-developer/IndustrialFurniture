# Этап 1: Сборка JAR
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -x test

# Этап 2: Запуск
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Создаем папку под файлы
RUN mkdir -p /app/uploads

# Ограничиваем оперативный объем JVM, чтобы 4GB RAM хватало с запасом
ENTRYPOINT ["java", "-Xms256m", "-Xmx768m", "-jar", "app.jar"]