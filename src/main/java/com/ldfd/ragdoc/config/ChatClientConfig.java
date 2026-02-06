package com.ldfd.ragdoc.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatJdbcClient(@Qualifier("jdbcChatMemory") ChatMemory jdbcChatMemory, ZhiPuAiChatModel openAiChatModel, Advisor chatHistoryAdvisor) {
//        var options = OpenAiChatOptions.builder()
//                .maxTokens(131072)
//                .extraBody(Map.of("transforms", List.of("middle-out")))
//                .build();
        return ChatClient.builder(openAiChatModel)
//                .defaultOptions(options)
                .defaultAdvisors(chatHistoryAdvisor, MessageChatMemoryAdvisor.builder(jdbcChatMemory).build())
                .defaultSystem("你是一个有帮助的AI助手。必须用中文回复所有问题。")
                .build();
    }

    @Bean
    public ChatClient chatInMemoryClient(@Qualifier("inMemoryChatMemory") ChatMemory inMemoryChatMemory, ZhiPuAiChatModel openAiChatModel, Advisor chatHistoryAdvisor) {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(chatHistoryAdvisor, MessageChatMemoryAdvisor.builder(inMemoryChatMemory).build())
                .defaultSystem("你是一个有帮助的AI助手。必须用中文回复所有问题。")
                .build();
    }
}
