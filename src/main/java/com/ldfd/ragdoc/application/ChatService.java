package com.ldfd.ragdoc.application;

import com.ldfd.ragdoc.application.vo.MessageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;

    /**
     * 多轮对话 - 在指定会话中发送消息
     */
    public MessageVo chat(String message, String conversationId) {
        String ret = chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(CONVERSATION_ID, conversationId))
                .call()
                .content();

        return MessageVo.builder()
                .sessionId(conversationId)
                .content(ret)
                .build();
    }

    /**
     * 流式对话 - 在指定会话中发送消息并流式返回
     */
    public Flux<MessageVo> chatStream(String message, String conversationId) {
        // 先发送包含 sessionId 的消息
        Flux<MessageVo> sessionIdFlux = Flux.just(
                MessageVo.builder()
                        .sessionId(conversationId)
                        .content("")
                        .build()
        );

        // 然后发送流式内容
        Flux<MessageVo> contentFlux = chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .map(chunk -> MessageVo.builder()
                        .sessionId(conversationId)
                        .content(chunk)
                        .build());

        return sessionIdFlux.concatWith(contentFlux);
    }

}
