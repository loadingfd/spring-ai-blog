package com.ldfd.ragdoc.application.vo;

import lombok.Data;

import java.time.Instant;

@Data
public class UserVo {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Instant createdAt;
    private Instant updatedAt;
}
