package com.ldfd.ragdoc.application;

import com.ldfd.ragdoc.adapter.controller.dto.MDocDTO;
import com.ldfd.ragdoc.application.vo.MDocVo;
import com.ldfd.ragdoc.application.mapper.MDocMapper;
import com.ldfd.ragdoc.domain.MDocRepository;
import com.ldfd.ragdoc.domain.bo.MDoc;
import com.ldfd.ragdoc.infrastructure.mapper.po.MDocPo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MDocService {

    private final MDocRepository mDocRepository;
    private final MDocMapper mDocMapper;

    @Transactional
    public MDocVo create(MDocDTO request) {
        MDoc mDoc = mDocMapper.dtoToBo(request);
        Instant now = Instant.now();
        mDoc.setCreatedAt(now);
        mDoc.setUpdatedAt(now);

        MDocPo saved = mDocRepository.save(mDocMapper.boToPo(mDoc));
        return mDocMapper.boToVo(mDocMapper.poToBo(saved));
    }

    public MDocVo getById(Long id) {
        MDocPo po = mDocRepository.findById(id);
        return mDocMapper.boToVo(mDocMapper.poToBo(po));
    }

    public List<MDocVo> list() {
        List<MDoc> boList = mDocRepository.findAll().stream()
                .map(mDocMapper::poToBo)
                .toList();
        return mDocMapper.boListToVo(boList);
    }

    @Transactional
    public MDocVo update(Long id, MDocDTO request) {
        MDoc existing = mDocMapper.poToBo(mDocRepository.findById(id));
        existing.setUserId(request.getUserId());
        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        existing.setUpdatedAt(Instant.now());

        MDocPo updated = mDocRepository.update(mDocMapper.boToPo(existing));
        return mDocMapper.boToVo(mDocMapper.poToBo(updated));
    }

    @Transactional
    public void delete(Long id) {
        mDocRepository.deleteById(id);
    }
}
