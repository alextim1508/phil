package com.alextim.phil.agent;

import com.alextim.phil.tool.SpringAiToolAdapter;
import com.alextim.phil.tool.Tool;
import com.alextim.phil.tool.ToolRegistry;
import java.util.List;
import org.springframework.ai.chat.model.ChatModel;

/** Сборка агента для тестов: без Spring-контекста и без сети. */
final class TestAgents {

    private TestAgents() {}

    static Agent agent(ChatModel model, RecordingEventPublisher events, Tool<?>... tools) {
        return agent(model, events, new AgentProperties(20, "."), tools);
    }

    static Agent agent(
            ChatModel model, RecordingEventPublisher events, AgentProperties properties, Tool<?>... tools) {
        ToolRegistry registry = new ToolRegistry(List.of(tools), events);
        return new Agent(model, events, registry, new SpringAiToolAdapter(registry), properties);
    }
}
