package com.ldfd.ragdoc.infrastructure.mapper.po;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "mdoc")
public class MDocPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String title;
    private String content;
    private Boolean processed;
    private Instant createdAt;
    private Instant updatedAt;
}
