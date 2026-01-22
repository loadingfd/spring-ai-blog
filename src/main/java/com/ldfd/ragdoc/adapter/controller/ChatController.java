package com.ldfd.ragdoc.adapter.controller;

import com.ldfd.ragdoc.adapter.common.Result;
import com.ldfd.ragdoc.adapter.controller.dto.MessageDTO;
import com.ldfd.ragdoc.application.ChatService;
import com.ldfd.ragdoc.application.vo.MessageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 多轮对话 - 在指定会话中发送消息
     */
    @PostMapping
    public Result<MessageVo> chat(@Validated @RequestBody MessageDTO message){
        if (message.getSessionId().isBlank()) {
            message.setSessionId(UUID.randomUUID().toString());
        }
        return new Result<>(this.chatService.chat(message.getContent(), message.getSessionId()));
    }




}
