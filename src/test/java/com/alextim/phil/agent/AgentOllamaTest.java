package com.alextim.phil.agent;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тест на живой модели. Исключён из обычного {@code gradlew.bat test} (см. настройки test в build.gradle),
 * потому что требует поднятой Ollama и работает секундами, а не миллисекундами.
 *
 * <p>Прогнать только его: {@code gradlew.bat test -PincludeTags=ollama}
 */
@Tag("ollama")
@SpringBootTest
@ActiveProfiles("test")
class AgentOllamaTest {

    @Autowired
    private Agent agent;

    @Test
    void модельОтвечаетИНеДуматьВслух() {
        String answer = agent.run("Reply with exactly one short sentence about yourself.");

        assertThat(answer).isNotBlank();
        // Если think-option: false не подхватился, Ollama вернёт рассуждения модели
        // в отдельном поле, а некоторые сборки — прямо в тексте тегами <think>.
        assertThat(answer).doesNotContain("<think>");
    }
}
