package com.ldfd.ragdoc.adapter.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class MessageDTO {

    String sessionId;

    @NotBlank
    String content;
}
