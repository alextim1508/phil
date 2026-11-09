# phil

Учебный проект для прохождения курса [harness-course](https://github.com/jeka1978/harness-course).

Проект использует Java 25, Gradle, Spring Boot и Spring AI.

```powershell
.\gradlew.bat test
.\gradlew.bat bootRun
```

Обычный запуск тестов исключает тесты с метками `ollama` и `mcp`, которым нужны внешние сервисы. Для запуска тестов с живой Ollama:

```powershell
.\gradlew.bat test -PincludeTags=ollama
```

Для работы приложения нужна Ollama с настроенной моделью; параметры подключения указаны в `application.yaml`.
