package com.ldfd.ragdoc.adapter.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MDocDTO {

    @NotNull
    private Long userId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
