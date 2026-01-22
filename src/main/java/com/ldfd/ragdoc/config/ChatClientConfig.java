package com.ldfd.ragdoc.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatMemory jdbcChatMemory, OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(jdbcChatMemory).build())
                .build();
    }
}
