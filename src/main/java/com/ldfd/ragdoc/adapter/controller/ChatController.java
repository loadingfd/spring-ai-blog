package com.ldfd.ragdoc.adapter.controller;

import com.ldfd.ragdoc.adapter.common.Result;
import com.ldfd.ragdoc.adapter.controller.dto.MessageDTO;
import com.ldfd.ragdoc.application.ChatService;
import com.ldfd.ragdoc.application.UserService;
import com.ldfd.ragdoc.application.vo.ChatHistoryVo;
import com.ldfd.ragdoc.application.vo.MessageVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

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
        return this.chatService.chatStream(message);
    }

    /**
     * 获取用户的所有会话列表
     * 只包含 id content截取 timestamp 简要信息
     */
    @GetMapping("/sessions")
    public Result<List<ChatHistoryVo>> getUserSessions() {
        return new Result<>(this.userService.getUserSessions());
    }

    /**
     * 获取指定会话的完整聊天记录
     */
    @GetMapping("/sessions/{sessionId}")
    public Result<List<ChatHistoryVo>> getSessionDetail(@PathVariable UUID sessionId) {
        return new Result<>(this.userService.getSessionDetail(sessionId));
    }

    /**
     * 文档总结 - 并行流式返回计划和Mermaid思维导图
     * Agent 1 和 Agent 2 并行执行，结果流式返回
     *
     * @param docId 文档ID
     * @param message 包含用户消息和会话ID的消息对象
     */
    @PostMapping(value = "/summary/{docId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MessageVo> summaryDocuments(@PathVariable Long docId, @RequestBody MessageDTO message) {
        // 如果没有提供会话ID，生成新的
        if (Objects.isNull(message.getSessionId()) || message.getSessionId().isBlank()) {
            message.setSessionId(UUID.randomUUID().toString());
        }

        // 如果用户没有提供消息内容，使用默认消息
        String userMessage = message.getContent();
        if (userMessage == null || userMessage.isBlank()) {
            userMessage = "请总结这个文档的内容";
        }

        return this.chatService.summaryDocuments(docId, message.getSessionId(), userMessage);
    }

    /**
     * 主题讲解 - 根据用户输入的主题，通过plan拆解和搜索tool调用，获取相关知识讲解
     * 使用 Spring AI 工具集成方式，AI 会自动调用搜索工具获取相关信息
     *
     * @param message 包含主题内容和会话ID的消息对象
     */
    @PostMapping(value = "/explain", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MessageVo> explainTopic(@Validated @RequestBody MessageDTO message) {
        // 如果没有提供会话ID，生成新的
        if (Objects.isNull(message.getSessionId()) || message.getSessionId().isBlank()) {
            message.setSessionId(UUID.randomUUID().toString());
        }

        // 获取主题内容
        String topic = message.getContent();
        if (topic == null || topic.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "主题内容不能为空");
        }

        return this.chatService.explainTopic(topic, message.getSessionId());
    }


}
