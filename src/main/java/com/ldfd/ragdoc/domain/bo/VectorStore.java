package com.ldfd.ragdoc.domain.bo;

import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@ToString(doNotUseGetters = true)
public class VectorStore implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String content;
    private Map<String, Object> metadata;
    private float[] embedding;
}
