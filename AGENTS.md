# Repository Guidelines

## Project Structure & Module Organization

This is a Java 25 terminal coding-agent project built with Spring Boot and Spring AI. Production code lives under `src/main/java/com/alextim/phil/`: `agent` manages conversations and agent events, `tool` implements tool execution and workspace access, `hud` renders terminal output, and `context` contains token estimation. Configuration is in `src/main/resources/application.yaml`. Tests mirror the package structure under `src/test/java`. The Gradle wrapper and `build.gradle` define the build; `docker-compose.yml` provides local service configuration.

## Build, Test, and Development Commands

Run the Gradle wrapper from the repository root (use `./gradlew` on Unix-like systems):

- `.\gradlew.bat build` — compile, run the default tests, and build the application.
- `.\gradlew.bat test` — run the default test suite without external-service tests.
- `.\gradlew.bat test -PincludeTags=ollama` — run tests tagged `ollama` against a running Ollama instance.
- `.\gradlew.bat bootRun` — start the terminal application; configure the Ollama endpoint and model in `application.yaml`.

## Coding Style & Naming Conventions

Follow the existing Java style: four-space indentation, `PascalCase` for types, `camelCase` for methods and fields, and lowercase package names. Keep classes in their appropriate feature package and preserve UTF-8 encoding. Gradle enables deprecation warnings; no formatter or linter is configured. Prefer small, focused changes consistent with nearby code.

## Testing Guidelines

Tests use JUnit 5, Spring Boot's test support, Mockito, and AssertJ. Name test classes `*Test` or `*Tests` and place them alongside the corresponding package hierarchy. Add focused tests for changed behavior; use existing scripted models and event recorders when testing agent loops. The default Gradle test task excludes `ollama` and `mcp` tags; opt in with `-PincludeTags=ollama` or another tag when the required service is available. No coverage threshold is configured.

## Commit & Pull Request Guidelines

Recent commits use short, imperative English subjects, such as `Add agent tool calling`; follow that pattern. Pull requests should explain the change and its motivation, link relevant issues when available, and report the tests run. Include terminal output or screenshots when a visible HUD change needs illustration.

## Configuration & Security

Ollama defaults to `localhost:11434` and model `qwen3.5:4b`; adjust settings for your environment. Keep credentials and machine-specific secrets out of version control. Respect `harness.agent.workspace-root` when implementing file-access tools.
