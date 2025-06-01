# Этап сборки: используем Maven с Java 23 для сборки проекта
FROM maven:3.9-eclipse-temurin-23-alpine AS builder

# Установка рабочей директории
WORKDIR /app

# Копируем только файлы, необходимые для загрузки зависимостей
COPY pom.xml .
COPY src ./src

# Скачиваем зависимости и собираем приложение
RUN mvn clean package -DskipTests

# Финальный этап: создаем минимальный образ для запуска
FROM eclipse-temurin:23-jre-alpine

# Установка рабочей директории
WORKDIR /app

# Копируем собранный JAR из предыдущего этапа
COPY --from=builder /app/target/*.jar app.jar

# Открываем порт, который использует приложение
EXPOSE 8080

# Команда запуска приложения
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
