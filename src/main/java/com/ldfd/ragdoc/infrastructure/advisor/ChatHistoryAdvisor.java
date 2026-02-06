package com.ldfd.ragdoc.infrastructure.advisor;

import com.ldfd.ragdoc.infrastructure.mapper.ChatHistoryPoMapper;
import com.ldfd.ragdoc.infrastructure.mapper.po.ChatHistoryPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHistoryAdvisor implements CallAdvisor, StreamAdvisor, Ordered {

    private final ChatHistoryPoMapper chatHistoryPoMapper;

    public static final String USER_ID = "USER_ID";
    public static final String ORIGINAL_USER_MESSAGE = "ORIGINAL_USER_MESSAGE";

    /**
     * 非流式调用
     */
    @Override
    @NonNull
    public ChatClientResponse adviseCall(@NonNull ChatClientRequest chatClientRequest, @NonNull CallAdvisorChain callAdvisorChain) {
        // 提取原始的用户请求文本
        String originalUserText = extractUserMsg(chatClientRequest);

        // 放行请求，继续执行后续的 Advisor 链和 AI 调用
        ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);

        // AI 响应回来后，记录日志（包括用户问题和 AI 回答）
        saveHistory(chatClientRequest, response, originalUserText);

        return response;
    }

    /**
     * 流式调用
     */
    @Override
    @NonNull
    public Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest chatClientRequest, @NonNull StreamAdvisorChain streamAdvisorChain) {
        // 先拿到“本次请求”的用户原始问题（用于后续入库）
        String userMsg = extractUserMsg(chatClientRequest);

        // 继续执行后续 Advisor/模型调用，得到“流式”的响应 Flux（会一边生成一边返回）
        Flux<ChatClientResponse> responseFlux = streamAdvisorChain.nextStream(chatClientRequest);

        // 由于流式响应是分片返回的，单个分片不一定包含完整回答；
        // 这里用聚合器在“后台”把分片拼成一条完整的最终响应，然后统一写入聊天记录。
        // 注意：对调用方仍然是流式返回，不会等待聚合完成才返回（不阻塞）。
        return new ChatClientMessageAggregator()
                .aggregateChatClientResponse(responseFlux,
                        aggregatedResponse ->
                                saveHistory(chatClientRequest, aggregatedResponse, userMsg));
    }

    /**
     * 从请求中提取最后一条用户消息的内容
     * 优先使用context中的ORIGINAL_USER_MESSAGE，如果不存在则提取最后一条USER消息
     */
   private String extractUserMsg(ChatClientRequest request) {
        try {
            // 优先使用context中设置的原始用户消息
            var context = request.context();
            String originalMessage = (String) context.get(ORIGINAL_USER_MESSAGE);
            if (StringUtils.hasText(originalMessage)) {
                return originalMessage;
            }

            // 否则从prompt中提取最后一条USER消息
            return request.prompt()
                    .getInstructions()
                    .stream()
                    .filter(m -> m.getMessageType() == MessageType.USER)
                    // 获取最后一条，通常是当前用户的提问
                    .reduce((first, second) -> second)
                    .map(Message::getText)
                    .orElse("");
        } catch (Exception e) {
            log.warn("Failed to extract user text", e);
            return "";
        }
    }


    /**
     * 保存聊天历史日志
     *
     * @param request  原始请求
     * @param response AI 响应
     * @param userMsg  用户消息文本
     */
    private void saveHistory(ChatClientRequest request, ChatClientResponse response, String userMsg) {
        try {
            var context = request.context();
            // 获取会话 ID
            String sessionId = (String) context.get(ChatMemory.CONVERSATION_ID);
            if (Objects.isNull(sessionId) || sessionId.isEmpty()) {
                log.warn("Session ID is null, skipping chat history log");
                return;
            }
            // 获取用户 ID
            Long userId = (Long) context.get(USER_ID);
            if (Objects.isNull(userId)) {
                log.warn("User ID is null, skipping chat history log");
                return;
            }
            // 保存用户提问日志
            if (StringUtils.hasText(userMsg)) {
                ChatHistoryPo po = ChatHistoryPo.builder()
                        .userId(userId)
                        .sessionId(UUID.fromString(sessionId))
                        .content(userMsg)
                        .type(MessageType.USER.name())
                        .timestamp(Instant.now())
                        .build();
                chatHistoryPoMapper.save(po);
            }
            // 保存 AI 回答日志
            Optional.ofNullable(response.chatResponse())
                    .map(ChatResponse::getResult)
                    .map(Generation::getOutput)
                    .map(AbstractMessage::getText)
                    .filter(StringUtils::hasText)
                    .ifPresent(outputText -> {
                        ChatHistoryPo po = ChatHistoryPo.builder()
                                .userId(userId)
                                .sessionId(UUID.fromString(sessionId))
                                .content(outputText)
                                .type(MessageType.ASSISTANT.name())
                                .timestamp(Instant.now())
                                .build();
                        chatHistoryPoMapper.save(po);
                    });

        } catch (Exception e) {
            log.error("Failed to save chat history log", e);
        }
    }

    @Override
    @NonNull
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 设置最高优先级 (HIGHEST_PRECEDENCE)
     * 确保该 Advisor 最早拦截请求，以便获取未被修改的用户输入
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
