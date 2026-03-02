package com.ldfd.ragdoc.infrastructure.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Context Editing Advisor that manages conversation token count by clearing older tool results.
 * <p>
 * This advisor mirrors Anthropic's context editing capability by intercepting requests,
 * estimating token usage, and replacing older tool-related messages with placeholder text
 * to stay within configured limits.
 * <p>
 * The advisor identifies clearable messages (ToolResponseMessage and AssistantMessage with tool calls)
 * and replaces their content with a configurable placeholder while respecting the `keep` setting
 * to preserve recent tool messages.
 *
 * @author ragdoc
 */
@Slf4j
public class ContextEditingAdvisor implements CallAdvisor, StreamAdvisor, Ordered {

    /**
     * Default placeholder text for cleared tool responses
     */
    public static final String DEFAULT_PLACEHOLDER = "[Earlier tool response cleared to save context space]";

    /**
     * Default token threshold to trigger context editing
     */
    public static final int DEFAULT_TRIGGER = 100000;

    /**
     * Default number of recent tool messages to keep
     */
    public static final int DEFAULT_KEEP = 3;

    /**
     * Default minimum number of messages to clear
     */
    public static final int DEFAULT_CLEAR_AT_LEAST = 1;

    private final int trigger;
    private final int clearAtLeast;
    private final int keep;
    private final Set<String> excludeTools;
    private final String placeholder;
    private final TokenCounter tokenCounter;
    private final int order;

    private ContextEditingAdvisor(Builder builder) {
        this.trigger = builder.trigger;
        this.clearAtLeast = builder.clearAtLeast;
        this.keep = builder.keep;
        this.excludeTools = builder.excludeTools;
        this.placeholder = builder.placeholder;
        this.tokenCounter = builder.tokenCounter;
        this.order = builder.order;
    }

    /**
     * Creates a new builder for ContextEditingAdvisor.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a ContextEditingAdvisor with default settings.
     *
     * @return a new ContextEditingAdvisor with default configuration
     */
    public static ContextEditingAdvisor withDefaults() {
        return builder().build();
    }

    @Override
    @NonNull
    public ChatClientResponse adviseCall(@NonNull ChatClientRequest chatClientRequest,
                                          @NonNull CallAdvisorChain callAdvisorChain) {
        // Edit context before passing to the chain
        ChatClientRequest editedRequest = editContext(chatClientRequest);
        return callAdvisorChain.nextCall(editedRequest);
    }

    @Override
    @NonNull
    public Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest chatClientRequest,
                                                  @NonNull StreamAdvisorChain streamAdvisorChain) {
        // Apply same context editing logic for streaming
        ChatClientRequest editedRequest = editContext(chatClientRequest);
        return streamAdvisorChain.nextStream(editedRequest);
    }

    /**
     * Edits the context by clearing older tool responses when token count exceeds threshold.
     *
     * @param request the original chat client request
     * @return the edited request with potentially cleared tool responses
     */
    private ChatClientRequest editContext(ChatClientRequest request) {
        List<Message> messages = request.prompt().getInstructions();

        // Count current tokens
        int currentTokens = tokenCounter.countTokens(messages);

        if (currentTokens < trigger) {
            log.debug("Token count {} is below trigger {}, no context editing needed", currentTokens, trigger);
            return request;
        }

        log.info("Token count {} exceeds trigger {}, initiating context editing", currentTokens, trigger);

        // Find clearable messages (tool responses and assistant messages with tool calls)
        List<ClearableMessage> clearableMessages = findClearableMessages(messages);

        if (clearableMessages.isEmpty()) {
            log.debug("No clearable messages found");
            return request;
        }

        // Calculate how many messages we can clear (respecting 'keep' setting)
        int maxClearable = Math.max(0, clearableMessages.size() - keep);
        // Clear at least 'clearAtLeast' messages, but not more than maxClearable
        int toClear = Math.max(clearAtLeast, 0);
        if (toClear > maxClearable) {
            toClear = maxClearable;
        }

        if (toClear == 0) {
            log.debug("No messages to clear (keep={}, clearable={})", keep, clearableMessages.size());
            return request;
        }

        log.info("Clearing {} of {} clearable messages (keeping {} recent)", toClear, clearableMessages.size(), keep);

        // Create new message list with cleared content
        List<Message> editedMessages = clearOlderToolResponses(messages, clearableMessages, toClear);

        // Build new request with edited messages
        return ChatClientRequest.builder()
                .prompt(new Prompt(editedMessages, request.prompt().getOptions()))
                .context(request.context())
                .build();
    }

    /**
     * Finds all messages that can be cleared (tool responses and assistant messages with tool calls).
     *
     * @param messages the list of messages to search
     * @return list of clearable messages with their indices
     */
    private List<ClearableMessage> findClearableMessages(List<Message> messages) {
        List<ClearableMessage> clearable = new ArrayList<>();

        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);

            if (message instanceof ToolResponseMessage toolResponse) {
                // Check if this tool should be excluded
                String toolName = extractToolName(toolResponse);
                if (toolName != null && excludeTools.contains(toolName)) {
                    continue;
                }
                clearable.add(new ClearableMessage(i, message, ClearableType.TOOL_RESPONSE));
            } else if (message instanceof AssistantMessage assistantMessage) {
                // Check if this assistant message has tool calls
                if (hasToolCalls(assistantMessage)) {
                    clearable.add(new ClearableMessage(i, message, ClearableType.ASSISTANT_WITH_TOOL_CALLS));
                }
            }
        }

        return clearable;
    }

    /**
     * Clears the older tool responses in the message list.
     *
     * @param originalMessages the original messages
     * @param clearableMessages the messages that can be cleared
     * @param toClear number of messages to clear
     * @return new list with cleared messages
     */
    private List<Message> clearOlderToolResponses(List<Message> originalMessages,
                                                   List<ClearableMessage> clearableMessages,
                                                   int toClear) {
        // Create a set of indices to clear (the oldest ones)
        Set<Integer> indicesToClear = new HashSet<>();
        for (int i = 0; i < toClear && i < clearableMessages.size(); i++) {
            indicesToClear.add(clearableMessages.get(i).index);
        }

        // Build new message list
        List<Message> editedMessages = new ArrayList<>();
        for (int i = 0; i < originalMessages.size(); i++) {
            Message original = originalMessages.get(i);

            if (indicesToClear.contains(i)) {
                // Replace with placeholder message
                Message replacement = createPlaceholderMessage(original);
                editedMessages.add(replacement);
                log.debug("Cleared message at index {}: {} -> placeholder", i, original.getMessageType());
            } else {
                editedMessages.add(original);
            }
        }

        return editedMessages;
    }

    /**
     * Creates a placeholder message to replace the original.
     *
     * @param original the original message to replace
     * @return a placeholder message of the same type
     */
    private Message createPlaceholderMessage(Message original) {
        if (original instanceof ToolResponseMessage) {
            // For tool responses, create a user message with placeholder
            // (ToolResponseMessage requires tool call id, so we use UserMessage as placeholder)
            return new UserMessage(placeholder);
        } else if (original instanceof AssistantMessage) {
            // For assistant messages, create an assistant message with placeholder
            return new AssistantMessage(placeholder);
        }
        // Fallback: return as UserMessage
        return new UserMessage(placeholder);
    }

    /**
     * Extracts the tool name from a ToolResponseMessage.
     *
     * @param toolResponse the tool response message
     * @return the tool name, or null if not found
     */
    private String extractToolName(ToolResponseMessage toolResponse) {
        // Try to extract tool name from the response
        if (!toolResponse.getResponses().isEmpty()) {
            return toolResponse.getResponses().getFirst().name();
        }
        return null;
    }

    /**
     * Checks if an AssistantMessage contains tool calls.
     *
     * @param assistantMessage the assistant message to check
     * @return true if the message has tool calls
     */
    private boolean hasToolCalls(AssistantMessage assistantMessage) {
        return assistantMessage.hasToolCalls();
    }

    @Override
    @NonNull
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Internal class to track clearable messages with their indices.
     */
    private record ClearableMessage(int index, Message message, ClearableType type) {}

    private enum ClearableType {
        TOOL_RESPONSE,
        ASSISTANT_WITH_TOOL_CALLS
    }

    /**
     * Builder for ContextEditingAdvisor.
     */
    public static class Builder {
        private int trigger = DEFAULT_TRIGGER;
        private int clearAtLeast = DEFAULT_CLEAR_AT_LEAST;
        private int keep = DEFAULT_KEEP;
        private Set<String> excludeTools = new HashSet<>();
        private String placeholder = DEFAULT_PLACEHOLDER;
        private TokenCounter tokenCounter = TokenCounter.approximateCounter();
        private int order = Ordered.HIGHEST_PRECEDENCE + 10; // After ChatHistoryAdvisor

        public Builder() {}

        /**
         * Sets the token threshold that triggers context editing.
         *
         * @param trigger the token count threshold
         * @return this builder
         */
        public Builder trigger(int trigger) {
            Assert.isTrue(trigger > 0, "trigger must be positive");
            this.trigger = trigger;
            return this;
        }

        /**
         * Sets the minimum number of tool messages to clear when triggered.
         *
         * @param clearAtLeast minimum messages to clear
         * @return this builder
         */
        public Builder clearAtLeast(int clearAtLeast) {
            Assert.isTrue(clearAtLeast >= 0, "clearAtLeast must be non-negative");
            this.clearAtLeast = clearAtLeast;
            return this;
        }

        /**
         * Sets the number of recent tool messages to preserve (never clear).
         *
         * @param keep number of recent messages to keep
         * @return this builder
         */
        public Builder keep(int keep) {
            Assert.isTrue(keep >= 0, "keep must be non-negative");
            this.keep = keep;
            return this;
        }

        /**
         * Sets tool names to exclude from clearing.
         *
         * @param toolNames set of tool names to exclude
         * @return this builder
         */
        public Builder excludeTools(Set<String> toolNames) {
            this.excludeTools = toolNames != null ? new HashSet<>(toolNames) : new HashSet<>();
            return this;
        }

        /**
         * Adds a tool name to exclude from clearing.
         *
         * @param toolName the tool name to exclude
         * @return this builder
         */
        public Builder excludeTool(String toolName) {
            if (toolName != null) {
                this.excludeTools.add(toolName);
            }
            return this;
        }

        /**
         * Sets the placeholder text for cleared messages.
         *
         * @param placeholder the placeholder text
         * @return this builder
         */
        public Builder placeholder(String placeholder) {
            Assert.hasText(placeholder, "placeholder must not be empty");
            this.placeholder = placeholder;
            return this;
        }

        /**
         * Sets the token counter implementation.
         *
         * @param tokenCounter the token counter
         * @return this builder
         */
        public Builder tokenCounter(TokenCounter tokenCounter) {
            Assert.notNull(tokenCounter, "tokenCounter must not be null");
            this.tokenCounter = tokenCounter;
            return this;
        }

        /**
         * Sets the advisor order (priority).
         *
         * @param order the order value
         * @return this builder
         */
        public Builder order(int order) {
            this.order = order;
            return this;
        }

        /**
         * Builds the ContextEditingAdvisor.
         *
         * @return a new ContextEditingAdvisor instance
         */
        public ContextEditingAdvisor build() {
            return new ContextEditingAdvisor(this);
        }
    }
}




