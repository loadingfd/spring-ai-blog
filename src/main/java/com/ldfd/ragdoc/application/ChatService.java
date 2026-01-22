package com.ldfd.ragdoc.application;

import com.ldfd.ragdoc.application.vo.MessageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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




}
