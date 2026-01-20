package com.ldfd.ragdoc.domain;

import com.ldfd.ragdoc.infrastructure.mapper.MDocPoMapper;
import com.ldfd.ragdoc.infrastructure.mapper.po.MDocPo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
@RequiredArgsConstructor
public class MDocRepository {

    private final MDocPoMapper MDocPoMapper;

    public MDocPo save(MDocPo mDocPo) {
        Assert.notNull(mDocPo, "mDocPo is required");
        return MDocPoMapper.save(mDocPo);
    }

    public MDocPo update(MDocPo mDocPo) {
        Assert.notNull(mDocPo, "mDocPo is required");
        Assert.notNull(mDocPo.getId(), "mDocPo id is required for update");

        if (!MDocPoMapper.existsById(mDocPo.getId())) {
            throw new IllegalArgumentException("MDoc not found with id: " + mDocPo.getId());
        }

        return MDocPoMapper.save(mDocPo);
    }

    public MDocPo findById(Long id) {
        Assert.notNull(id, "id is required");

        return MDocPoMapper.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MDoc not found with id: " + id));
    }
}
