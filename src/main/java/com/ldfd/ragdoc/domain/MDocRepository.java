package com.ldfd.ragdoc.domain;

import com.ldfd.ragdoc.domain.bo.MDoc;
import com.ldfd.ragdoc.application.mapper.MDocMapper;
import com.ldfd.ragdoc.infrastructure.mapper.MDocPoMapper;
import com.ldfd.ragdoc.infrastructure.mapper.po.MDocPo;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;

@RequiredArgsConstructor
public class MDocRepository {

    private final MDocPoMapper MDocPoMapper;
    private final MDocMapper MDocMapper;

    public MDocPo findById(Long id) {
        Assert.notNull(id, "id is required");

        return MDocPoMapper.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MDoc not found with id: " + id));
    }
}
