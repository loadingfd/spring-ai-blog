package com.ldfd.ragdoc.application.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatHistoryVo {
    private Long id;
    private Long userId;
    private UUID sessionId;
    private String content;
    private String type;
    private Instant timestamp;
}
