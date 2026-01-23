package com.ldfd.ragdoc.infrastructure.mapper.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Content Hash 实体类
 * 用于存储内容的哈希值，避免重复处理
 */
@Entity
@Table(name = "content_hash",
       indexes = @Index(name = "idx_content_hash", columnList = "contentHash", unique = true))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentHashPo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 内容哈希值（唯一）
     * 使用固定长度的哈希算法 SHA-256(64)
     */
    @Column(name = "content_hash", nullable = false, unique = true, length = 64)
    private String contentHash;

}
