package com.ldfd.ragdoc.infrastructure.mapper.po;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "spring_ai_chat_history")
public class ChatHistoryPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private UUID sessionId;
    private String content;
    // USER, ASSISTANT, SYSTEM, TOOL
    private String type;
    private Instant timestamp;
}
