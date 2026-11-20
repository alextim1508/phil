package com.alextim.phil.agent;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.metadata.Usage;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConversationTest {

    @Test
    void countsNewMessagesOnTopOfModelUsage() {
        Conversation conversation = new Conversation("SYSTEM");
        conversation.addUserMessage("question");
        conversation.addAssistantMessage(new AssistantMessage("answer"));
        Usage usage = mock(Usage.class);
        when(usage.getTotalTokens()).thenReturn(100);
        conversation.countedByModel(usage);

        conversation.addUserMessage("12345678");

        assertThat(conversation.tokens()).isEqualTo(102);
    }

    @Test
    void clearingCountedToolResultsUpdatesModelUsage() {
        Conversation conversation = new Conversation("SYSTEM");
        conversation.addToolResponses(ToolResponseMessage.builder()
                .responses(List.of(new ToolResponseMessage.ToolResponse("1", "Read", "x".repeat(400))))
                .build());
        Usage usage = mock(Usage.class);
        when(usage.getTotalTokens()).thenReturn(200);
        conversation.countedByModel(usage);

        int freed = conversation.clearToolResults(Set.of("Read"), 0, "gone");

        assertThat(freed).isEqualTo(99);
        assertThat(conversation.tokens()).isEqualTo(101);
        assertThat(conversation.clearToolResults(Set.of("Read"), 0, "gone")).isZero();
        assertThat(conversation.tokens()).isEqualTo(101);
    }

    @Test
    void системныйПромптПопадаетВПромпт() {
        Conversation conversation = new Conversation("SYSTEM");
        conversation.addUserMessage("привет");

        assertThat(conversation.toPromptMessages()).hasSize(2);
    }

    @Test
    void собираетПромптВПравильномПорядке() {
        Conversation conversation = new Conversation("SYSTEM");
        conversation.addUserMessage("вопрос");
        conversation.addAssistantMessage(new AssistantMessage("ответ"));

        assertThat(conversation.toPromptMessages())
                .extracting(Message::getMessageType)
                .containsExactly(MessageType.SYSTEM, MessageType.USER, MessageType.ASSISTANT);
    }


}
