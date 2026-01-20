package com.ldfd.ragdoc.application;

import com.ldfd.ragdoc.domain.MDocRepository;
import com.ldfd.ragdoc.infrastructure.mapper.MDocPoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final OllamaChatModel ollamaChatModel;
    private final MDocRepository mDocRepository;
    private final MDocPoMapper mDocPoMapper;

    /**
     * 单轮对话（兼容原有接口）
     */
    public String chat(String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        ChatResponse response = ollamaChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

}
