package com.ldfd.ragdoc.domain;

import com.ldfd.ragdoc.exception.BusinessException;
import com.ldfd.ragdoc.infrastructure.mapper.MDocPoMapper;
import com.ldfd.ragdoc.infrastructure.mapper.po.MDocPo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MDocRepository {

    private final MDocPoMapper mDocPoMapper;

    public MDocPo save(MDocPo mDocPo) {
        Assert.notNull(mDocPo, "mDocPo is required");
        return mDocPoMapper.save(mDocPo);
    }

    public MDocPo update(MDocPo mDocPo) {
        Assert.notNull(mDocPo, "mDocPo is required");
        Assert.notNull(mDocPo.getId(), "mDocPo id is required for update");

        if (!mDocPoMapper.existsById(mDocPo.getId())) {
            throw new BusinessException("404", "MDoc not found with id: " + mDocPo.getId());
        }

        return mDocPoMapper.save(mDocPo);
    }

    public MDocPo findById(Long id) {
        Assert.notNull(id, "id is required");

        return mDocPoMapper.findById(id)
                .orElseThrow(() -> new BusinessException("404", "MDoc not found with id: " + id));
    }

    public List<MDocPo> findAll() {
        return mDocPoMapper.findAll();
    }

    public void deleteById(Long id) {
        Assert.notNull(id, "id is required");
        if (!mDocPoMapper.existsById(id)) {
            throw new BusinessException("404", "MDoc not found with id: " + id);
        }
        mDocPoMapper.deleteById(id);
    }
}
