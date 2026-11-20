package com.alextim.phil.agent;

import static org.assertj.core.api.Assertions.assertThat;

import com.alextim.phil.TestPermissionConfig;
import com.alextim.phil.context.ContextManager;
import com.alextim.phil.context.GitStatusProvider;
import com.alextim.phil.context.ProjectMemoryLoader;
import com.alextim.phil.context.SystemPromptBuilder;
import com.alextim.phil.permission.PermissionMode;
import com.alextim.phil.permission.PermissionPrompt;
import com.alextim.phil.permission.PermissionProperties;
import com.alextim.phil.tool.FileStateCache;
import com.alextim.phil.tool.SpringAiToolAdapter;
import com.alextim.phil.tool.Tool;
import com.alextim.phil.tool.ToolContext;
import com.alextim.phil.tool.ToolRegistry;
import com.alextim.phil.tool.ToolResult;
import com.alextim.phil.tool.impl.BashTool;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Живая миссия: настоящий Maven запускается только после явного тестового согласия. */
@Tag("ollama")
@SpringBootTest
@ActiveProfiles("test")
@Import(TestPermissionConfig.class)
class BashMissionOllamaTest {
    @Autowired private ChatModel chatModel;
    @Autowired private List<Tool<?>> tools;

    @Test
    void запускаетБыстрыеТестыПослеПодтверждения() {
        AgentProperties properties = new AgentProperties(6, ".", true);
        RecordingEventPublisher events = new RecordingEventPublisher();
        AtomicReference<ToolResult> executed = new AtomicReference<>();
        AtomicInteger confirmations = new AtomicInteger();
        List<Tool<?>> missionTools =
                new ArrayList<>(
                        tools.stream().filter(tool -> !tool.name().equals("Bash")).toList());
        missionTools.add(
                new BashTool(new com.alextim.phil.tool.WorkspaceGuard(properties)) {
                    @Override
                    public ToolResult call(Input input, ToolContext context) {
                        ToolResult result = super.call(input, context);
                        executed.set(result);
                        return result;
                    }
                });
        // Проверка запуска тестов не должна давать модели возможность менять исходники.
        PermissionProperties permissions =
                com.alextim.phil.TestPermissionConfig.properties(
                        PermissionMode.DEFAULT, List.of(), List.of("Edit", "Write"), List.of());
        PermissionPrompt prompt =
                (name, target, reason) -> {
                    if (name.equals("Bash") && target.strip().equals(".\\mvnw.cmd test")) {
                        confirmations.incrementAndGet();
                        return PermissionPrompt.Answer.ALLOW_ONCE;
                    }
                    return PermissionPrompt.Answer.DENY;
                };
        ToolRegistry registry =
                com.alextim.phil.TestPermissionConfig.registry(
                        missionTools, events, new FileStateCache(), permissions, prompt);
        ContextManager context =
                new ContextManager(
                        new GitStatusProvider(properties),
                        new ProjectMemoryLoader(properties),
                        new SystemPromptBuilder(registry, properties),
                        properties);
        Agent agent =
                new Agent(
                        chatModel,
                        events,
                        registry,
                        new SpringAiToolAdapter(registry),
                        properties,
                        context,
                        new com.alextim.phil.tool.TodoStore(16_384),
                        new com.alextim.phil.context.Compactor(chatModel, events, 16_384));

        String answer = agent.run("Запусти тесты проекта и скажи, всё ли зелено.");

        assertThat(confirmations.get())
                .as("модель должна получить подтверждение команды")
                .isPositive();
        assertThat(executed.get()).as("Bash должен действительно исполнить команду").isNotNull();
        assertThat(executed.get().error()).isFalse();
        assertThat(executed.get().content()).contains("код возврата: 0", "BUILD SUCCESS");
        assertThat(answer).isNotBlank();
        System.out.println("ЖИВАЯ МИССИЯ: " + answer);
    }
}
