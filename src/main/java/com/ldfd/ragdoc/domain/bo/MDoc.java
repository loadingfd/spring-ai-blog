package com.ldfd.ragdoc.domain.bo;

import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Data
@ToString(doNotUseGetters = true)
public class MDoc implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    Long id;
    Long userId;
    String title;
    String content;
    Boolean processed;
    Instant createdAt;
    Instant updatedAt;
}
