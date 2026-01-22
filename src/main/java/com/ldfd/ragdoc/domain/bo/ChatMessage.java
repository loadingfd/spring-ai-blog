package com.ldfd.ragdoc.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long messageId;
    private UUID sessionId;
    private String sender;
    private String content;
    private LocalDateTime createdAt;

    public static final String SENDER_USER = "user";
    public static final String SENDER_ASSISTANT = "assistant";
}
