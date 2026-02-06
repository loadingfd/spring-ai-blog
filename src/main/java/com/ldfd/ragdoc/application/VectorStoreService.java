package com.ldfd.ragdoc.application;


import com.ldfd.ragdoc.infrastructure.converter.VectorStoreConverter;
import com.ldfd.ragdoc.application.vo.VectorStoreVo;
import com.ldfd.ragdoc.domain.VectorStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class VectorStoreService {

    private final AuthService authService;
    private final VectorStoreRepository vectorStoreRepository;
    private final VectorStoreConverter vectorStoreConverter;

    public void deleteById(UUID id) {
        vectorStoreRepository.deleteById(id);
    }

    public List<VectorStoreVo> listUserVectors() {
        Long userId = authService.getUserId();
        return vectorStoreConverter.boListToVo(vectorStoreRepository.findByUserId(userId));
    }
}