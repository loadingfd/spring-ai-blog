package com.ldfd.ragdoc.application;

import com.ldfd.ragdoc.adapter.controller.dto.MDocDTO;
import com.ldfd.ragdoc.application.vo.MDocVo;
import com.ldfd.ragdoc.exception.BusinessException;
import com.ldfd.ragdoc.infrastructure.converter.MDocConverter;
import com.ldfd.ragdoc.domain.MDocRepository;
import com.ldfd.ragdoc.domain.bo.MDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MDocService {

    private final AuthService authService;
    private final MDocRepository mDocRepository;
    private final MDocConverter mDocConverter;

    @Transactional
    public MDocVo create(MDocDTO request) {
        MDoc mDoc = mDocConverter.dtoToBo(request);
        Instant now = Instant.now();
        mDoc.setCreatedAt(now);
        mDoc.setUpdatedAt(now);

        MDoc saved = mDocRepository.save(mDoc);
        return mDocConverter.boToVo(saved);
    }

    public MDocVo getById(Long id) {
        Long userId = authService.getUserId();
        MDoc mDoc = mDocRepository.findById(id);
        if (!Objects.equals(userId, mDoc.getUserId())) {
            throw new BusinessException("Unauthorized access to document");
        }
        return mDocConverter.boToVo(mDoc);
    }

    public List<MDocVo> listByUserId(Long userId) {
        List<MDoc> boList = mDocRepository.findByUserId(userId);
        return mDocConverter.boListToVo(boList);
    }

    public List<MDocVo> list() {
        List<MDoc> boList = mDocRepository.findAll();
        return mDocConverter.boListToVo(boList);
    }

    @Transactional
    public MDocVo update(Long id, MDocDTO request) {
        MDoc existing = mDocRepository.findById(id);
        existing.setUserId(request.getUserId());
        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        existing.setProcessed(false);
        existing.setUpdatedAt(Instant.now());

        MDoc updated = mDocRepository.update(existing);
        return mDocConverter.boToVo(updated);
    }

    @Transactional
    public void delete(Long id) {
        mDocRepository.deleteById(id);
    }
}
