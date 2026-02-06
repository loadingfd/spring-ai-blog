package com.ldfd.ragdoc.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory {
    private Long id;
    private Long userId;
    private UUID sessionId;
    private String content;
    private String type;
    private Instant timestamp;

    // Constants for message type
    public static final String TYPE_USER = "USER";
    public static final String TYPE_ASSISTANT = "ASSISTANT";
    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_TOOL = "TOOL";
}
