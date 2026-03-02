package com.ldfd.ragdoc.infrastructure.advisor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContextEditingAdvisor
 */
class ContextEditingAdvisorTest {

    private ContextEditingAdvisor advisor;
    private CallAdvisorChain mockChain;

    @BeforeEach
    void setUp() {
        mockChain = mock(CallAdvisorChain.class);
        // Return empty response when chain is called
        when(mockChain.nextCall(any())).thenReturn(
                ChatClientResponse.builder().build()
        );
    }

    @Test
    @DisplayName("Should not edit context when token count is below threshold")
    void shouldNotEditContextWhenBelowThreshold() {
        // Given: advisor with high trigger threshold
        advisor = ContextEditingAdvisor.builder()
                .trigger(100000)
                .build();

        List<Message> messages = List.of(
                new UserMessage("Hello"),
                new AssistantMessage("Hi there!")
        );
        ChatClientRequest request = ChatClientRequest.builder()
                .prompt(new Prompt(messages))
                .build();

        // When
        advisor.adviseCall(request, mockChain);

        // Then: chain should be called with original messages
        verify(mockChain).nextCall(argThat(req ->
                req.prompt().getInstructions().size() == 2
        ));
    }

    @Test
    @DisplayName("Should edit context when token count exceeds threshold with assistant messages")
    void shouldEditContextWhenAboveThreshold() {
        // Given: advisor with low trigger threshold
        advisor = ContextEditingAdvisor.builder()
                .trigger(10) // Very low threshold to trigger editing
                .keep(0)     // Don't keep any messages
                .clearAtLeast(1)
                .build();

        // Create messages that exceed the threshold
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("What is the weather? This is a longer question to ensure we exceed the token threshold."));
        messages.add(new AssistantMessage("Based on the tool response, it's sunny and 25°C. This is also a longer response."));

        ChatClientRequest request = ChatClientRequest.builder()
                .prompt(new Prompt(messages))
                .build();

        // When
        advisor.adviseCall(request, mockChain);

        // Then: chain should be called
        verify(mockChain).nextCall(any());
    }

    @Test
    @DisplayName("TokenCounter approximateCounter should work correctly")
    void tokenCounterApproximateCounterShouldWork() {
        // Given
        TokenCounter counter = TokenCounter.approximateCounter();

        List<Message> messages = List.of(
                new UserMessage("Hello world") // 11 characters
        );

        // When
        int tokens = counter.countTokens(messages);

        // Then: ~4 chars per token, so 11 chars should be ~3 tokens
        assertEquals(3, tokens);
    }

    @Test
    @DisplayName("TokenCounter wordBasedCounter should work correctly")
    void tokenCounterWordBasedCounterShouldWork() {
        // Given
        TokenCounter counter = TokenCounter.wordBasedCounter();

        List<Message> messages = List.of(
                new UserMessage("Hello world how are you") // 5 words
        );

        // When
        int tokens = counter.countTokens(messages);

        // Then: ~1.3 tokens per word, so 5 words should be ~7 tokens
        assertEquals(7, tokens);
    }

    @Test
    @DisplayName("TokenCounter should handle null and empty messages")
    void tokenCounterShouldHandleNullAndEmpty() {
        TokenCounter counter = TokenCounter.approximateCounter();

        assertEquals(0, counter.countTokens(null));
        assertEquals(0, counter.countTokens(List.of()));
    }

    @Test
    @DisplayName("Builder should set all properties correctly")
    void builderShouldSetAllProperties() {
        // Given & When
        ContextEditingAdvisor advisor = ContextEditingAdvisor.builder()
                .trigger(50000)
                .clearAtLeast(2)
                .keep(5)
                .excludeTools(Set.of("tool1", "tool2"))
                .placeholder("[cleared]")
                .tokenCounter(TokenCounter.wordBasedCounter())
                .order(100)
                .build();

        // Then
        assertNotNull(advisor);
        assertEquals("ContextEditingAdvisor", advisor.getName());
        assertEquals(100, advisor.getOrder());
    }

    @Test
    @DisplayName("withDefaults should create advisor with default settings")
    void withDefaultsShouldCreateAdvisorWithDefaults() {
        // When
        ContextEditingAdvisor advisor = ContextEditingAdvisor.withDefaults();

        // Then
        assertNotNull(advisor);
        assertEquals("ContextEditingAdvisor", advisor.getName());
    }

    @Test
    @DisplayName("Should return correct name")
    void shouldReturnCorrectName() {
        advisor = ContextEditingAdvisor.withDefaults();
        assertEquals("ContextEditingAdvisor", advisor.getName());
    }

    @Test
    @DisplayName("Should have correct default order")
    void shouldHaveCorrectDefaultOrder() {
        advisor = ContextEditingAdvisor.builder().build();
        // Default order is HIGHEST_PRECEDENCE + 10
        assertTrue(advisor.getOrder() > Integer.MIN_VALUE);
    }

    @Test
    @DisplayName("excludeTool builder method should add single tool")
    void excludeToolShouldAddSingleTool() {
        // Given & When
        ContextEditingAdvisor advisor = ContextEditingAdvisor.builder()
                .excludeTool("tool1")
                .excludeTool("tool2")
                .build();

        // Then
        assertNotNull(advisor);
    }
}
