package com.ldfd.ragdoc.application.vo;

import lombok.Data;

import java.time.Instant;

@Data
public class MDocVo {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Boolean processed;
    private Instant createdAt;
    private Instant updatedAt;
}
