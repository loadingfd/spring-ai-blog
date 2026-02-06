package com.ldfd.ragdoc.application;

import com.ldfd.ragdoc.application.vo.MessageVo;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.ldfd.ragdoc.annotation.WithMockUserId;
import org.testng.Assert;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

import static org.testng.AssertJUnit.assertNotNull;

/*
 * ChatService 集成测试 - 真实调用
 *
 * 注意：需要以下服务正常运行：
 * - Spring AI / Ollama 服务
 * - 数据库
 * - 搜索工具（SearXNG）
 */
@ActiveProfiles("test")
@SpringBootTest
class ChatServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceTest.class);

    @Autowired
    private ChatService chatService;

    @Test
    @DisplayName("explainTopic - 复杂主题真实调用")
    @WithMockUserId(username = "testuser")
    void testExplainTopic_ComplexTopic_RealCall() {
        String topic = "分布式系统设计模式和最佳实践";
        String sessionId = UUID.randomUUID().toString();

        Flux<MessageVo> result = chatService.explainTopic(topic, sessionId);

        result.doOnNext(message -> log.info("Received message: {}", message.getContent()))
                .doOnError(error -> log.error("Error occurred: ", error))
                .doOnComplete(() -> log.info("Stream completed"))
                .blockLast(Duration.ofSeconds(120));
    }


    @DisplayName("explainTopic - 特殊字符主题真实调用")
    @WithMockUserId(username = "testuser")
    void testExplainTopic_WithSpecialCharacters_RealCall() {
        String topic = "C++ vs Python vs Go 语言对比";
        String sessionId = UUID.randomUUID().toString();

//        Flux<MessageVo> result = chatService.explainTopic(topic, sessionId);
        MessageVo result = chatService.explainTopicSync(topic, sessionId);
        log.info("Starting test for topic with special characters: {}", topic);
        log.info("Result: {}", result);

//        result.doOnNext(message -> log.info("Received message: {}", message.getContent()))
//                .doOnError(error -> log.error("Error occurred: ", error))
//                .doOnComplete(() -> log.info("Stream completed"))
//                .blockLast(Duration.ofSeconds(30));
    }


    @DisplayName("最低可用性")
    @WithMockUserId(username = "testuser")
    void testChat() {
        String sessionId = UUID.randomUUID().toString();
        String prompt = "你是谁";

        var res = this.chatService.chat(prompt, sessionId);
        log.info("Chat response: {}", res);
        if (Strings.isBlank(res.getContent())) {
            Assert.fail("Response is blank");
        } else {
            assertNotNull(res);
        }
    }




}
