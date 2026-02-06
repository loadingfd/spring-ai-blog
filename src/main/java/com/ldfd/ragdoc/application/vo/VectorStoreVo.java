package com.ldfd.ragdoc.application.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class VectorStoreVo {

    private UUID id;
    private String content;
}
