package com.ldfd.ragdoc.adapter.controller;

import com.ldfd.ragdoc.adapter.controller.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatContoller {

    private final OllamaChatModel ollamaChatModel;

    @PostMapping
    public String chat(@RequestBody MessageDTO message){
        String msg = message.getMessage();
        Prompt prompt = new Prompt(new UserMessage(msg));
        ChatResponse response = ollamaChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}
