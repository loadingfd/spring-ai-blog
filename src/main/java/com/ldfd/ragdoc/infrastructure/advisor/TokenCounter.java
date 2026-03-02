package com.ldfd.ragdoc.infrastructure.advisor;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * Functional interface for counting tokens in messages.
 * Provides a default approximate implementation based on character estimation.
 */
@FunctionalInterface
public interface TokenCounter {

    /**
     * Count the number of tokens in the given messages.
     *
     * @param messages the messages to count tokens for
     * @return the estimated token count
     */
    int countTokens(List<Message> messages);

    /**
     * Returns an approximate token counter that estimates tokens based on character count.
     * Uses a simple heuristic of ~4 characters per token (common for English text).
     *
     * @return an approximate TokenCounter implementation
     */
    static TokenCounter approximateCounter() {
        return messages -> {
            if (messages == null || messages.isEmpty()) {
                return 0;
            }
            int totalChars = 0;
            for (Message message : messages) {
                String content = message.getText();
                if (content != null) {
                    totalChars += content.length();
                }
            }
            // Approximate: ~4 characters per token (common heuristic)
            return (totalChars + 3) / 4;
        };
    }

    /**
     * Returns a token counter that uses word count as estimation.
     * Estimates ~1.3 tokens per word (accounts for subword tokenization).
     *
     * @return a word-based TokenCounter implementation
     */
    static TokenCounter wordBasedCounter() {
        return messages -> {
            if (messages == null || messages.isEmpty()) {
                return 0;
            }
            int totalWords = 0;
            for (Message message : messages) {
                String content = message.getText();
                if (content != null && !content.isBlank()) {
                    // Split on whitespace and count
                    totalWords += content.split("\\s+").length;
                }
            }
            // Approximate: ~1.3 tokens per word
            return (int) Math.ceil(totalWords * 1.3);
        };
    }
}

