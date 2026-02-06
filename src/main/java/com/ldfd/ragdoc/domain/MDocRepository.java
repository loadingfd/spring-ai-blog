package com.ldfd.ragdoc.domain;

import com.ldfd.ragdoc.domain.bo.MDoc;
import com.ldfd.ragdoc.exception.BusinessException;
import com.ldfd.ragdoc.infrastructure.converter.MDocConverter;
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
    private final MDocConverter mDocConverter;

    public MDoc save(MDoc mDoc) {
        Assert.notNull(mDoc, "mDoc is required");
        MDocPo po = mDocConverter.boToPo(mDoc);
        MDocPo saved = mDocPoMapper.save(po);
        return mDocConverter.poToBo(saved);
    }

    public MDoc update(MDoc mDoc) {
        Assert.notNull(mDoc, "mDoc is required");
        Assert.notNull(mDoc.getId(), "mDoc id is required for update");

        if (!mDocPoMapper.existsById(mDoc.getId())) {
            throw new BusinessException("404", "MDoc not found with id: " + mDoc.getId());
        }

        MDocPo po = mDocConverter.boToPo(mDoc);
        MDocPo updated = mDocPoMapper.save(po);
        return mDocConverter.poToBo(updated);
    }

    public MDoc findById(Long id) {
        Assert.notNull(id, "id is required");

        MDocPo po = mDocPoMapper.findById(id)
                .orElseThrow(() -> new BusinessException("404", "MDoc not found with id: " + id));
        return mDocConverter.poToBo(po);
    }

    public List<MDoc> findAll() {
        return mDocPoMapper.findAll().stream()
                .map(mDocConverter::poToBo)
                .toList();
    }

    public List<MDoc> findByUserId(Long userId) {
        Assert.notNull(userId, "userId is required");
        return mDocPoMapper.findByUserId(userId).stream()
                .map(mDocConverter::poToBo)
                .toList();
    }

    public void deleteById(Long id) {
        Assert.notNull(id, "id is required");
        if (!mDocPoMapper.existsById(id)) {
            throw new BusinessException("404", "MDoc not found with id: " + id);
        }
        mDocPoMapper.deleteById(id);
    }
}
