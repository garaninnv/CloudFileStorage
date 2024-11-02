FROM openjdk:17-jdk-slim

WORKDIR /app

# Копируем файл jar приложения в контейнер
COPY target/CloudFileStorage-3.2.3.jar app.jar

# Указываем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
