package com.ldfd.ragdoc.adapter.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ChatHistoryDTO {

    @NotNull
    private Long userId;

    @NotNull
    private UUID sessionId;

    @NotBlank
    private String content;

    @NotBlank
    private String type;

    private Instant timestamp;
}
