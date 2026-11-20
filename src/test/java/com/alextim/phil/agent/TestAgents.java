package com.alextim.phil.agent;

import com.alextim.phil.TestPermissionConfig;
import com.alextim.phil.context.Compactor;
import com.alextim.phil.context.ContextManager;
import com.alextim.phil.context.GitStatusProvider;
import com.alextim.phil.context.ProjectMemoryLoader;
import com.alextim.phil.context.SystemPromptBuilder;
import com.alextim.phil.tool.FileStateCache;
import com.alextim.phil.tool.SpringAiToolAdapter;
import com.alextim.phil.tool.TodoStore;
import com.alextim.phil.tool.Tool;
import com.alextim.phil.tool.ToolRegistry;

import org.springframework.ai.chat.model.ChatModel;

import java.util.List;

/** Сборка агента для тестов: без Spring-контекста и без сети. */
final class TestAgents {

    private TestAgents() {}

    static Agent agent(ChatModel model, RecordingEventPublisher events, Tool<?>... tools) {
        return agent(model, events, new AgentProperties(20, ".", true), tools);
    }

    static Agent agent(
            ChatModel model,
            RecordingEventPublisher events,
            AgentProperties properties,
            Tool<?>... tools) {
        TodoStore todos = new TodoStore(16_384);
        ToolRegistry registry =
                com.alextim.phil.TestPermissionConfig.registry(
                        List.of(tools),
                        events,
                        new FileStateCache(),
                        todos,
                        TestPermissionConfig.permissive(),
                        TestPermissionConfig.allowOnce());
        return new Agent(
                model,
                events,
                registry,
                new SpringAiToolAdapter(registry),
                properties,
                context(registry, properties),
                todos,
                new Compactor(model, events, 16_384));
    }

    /**
     * Настоящий {@link ContextManager}, а не заглушка: весь контекст — обычные объекты с
     * конструкторами, и собрать их руками дешевле, чем объяснять подмену.
     */
    private static ContextManager context(ToolRegistry registry, AgentProperties properties) {
        return new ContextManager(
                new GitStatusProvider(properties),
                new ProjectMemoryLoader(properties),
                new SystemPromptBuilder(registry, properties),
                properties);
    }
}
