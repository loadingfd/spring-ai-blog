package com.ldfd.ragdoc.adapter.controller;

import com.ldfd.ragdoc.adapter.common.Result;
import com.ldfd.ragdoc.adapter.controller.dto.MessageDTO;
import com.ldfd.ragdoc.application.ChatService;
import com.ldfd.ragdoc.application.vo.MessageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Objects;
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
        if (Objects.isNull(message.getSessionId()) || message.getSessionId().isBlank()) {
            message.setSessionId(UUID.randomUUID().toString());
        }
        return new Result<>(this.chatService.chat(message.getContent(), message.getSessionId()));
    }

    /**
     * 流式对话 - 在指定会话中发送消息并流式返回
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MessageVo> chatStream(@Validated @RequestBody MessageDTO message){
        if (Objects.isNull(message.getSessionId()) || message.getSessionId().isBlank()) {
            message.setSessionId(UUID.randomUUID().toString());
        }
        return this.chatService.chatStream(message.getContent(), message.getSessionId());
    }

}
