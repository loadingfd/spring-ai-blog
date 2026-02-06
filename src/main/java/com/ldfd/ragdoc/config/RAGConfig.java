package com.ldfd.ragdoc.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RAGConfig {

    @Bean
    MarkdownDocumentReaderConfig markdownDocumentReaderConfig() {
        return MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .build();
    }

    @Bean
    public Advisor retrievalTransAdvisor(VectorStore vectorStore, ZhiPuAiChatModel ollamaChatModel) {

        // 温度设置为 0，确保生成结果更稳定
//        var options = OpenAiChatOptions.builder()
//                .temperature(0.0)
//                .build();
        var options = ZhiPuAiChatOptions.builder()
                .temperature(0.0)
                .build();

        var chatClientBuilder = ChatClient.builder(ollamaChatModel)
                .defaultSystem("你是一个有帮助的AI助手。请使用中文处理所有请求。");

        return RetrievalAugmentationAdvisor.builder()
                // 1. 设置查询转换器（比如把用户乱七八糟的问题重写一遍）
                // 注意：这里用 .mutate() 克隆一个 builder，防止污染全局配置
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(chatClientBuilder.defaultOptions(options))
                        .build())
                // 2. 设置检索器（去向量库捞数据）
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50) // 相似度阈值
                        .vectorStore(vectorStore)
                        .build())
                // 3. 设置查询增强器（把捞到的数据拼回到用户问题里）
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();
    }
}


