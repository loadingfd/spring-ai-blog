package com.ldfd.ragdoc.domain;

import com.ldfd.ragdoc.domain.bo.VectorStore;
import com.ldfd.ragdoc.infrastructure.converter.VectorStoreConverter;
import com.ldfd.ragdoc.infrastructure.mapper.VectorStorePoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class VectorStoreRepository {

    private final VectorStorePoMapper vectorStorePoMapper;
    private final VectorStoreConverter vectorStoreConverter;

    public List<VectorStore> findByUserId(Long userId) {
        List<VectorStore> debug = vectorStorePoMapper.findByUserId(userId).stream()
                .map(vectorStoreConverter::poToBo)
                .toList();
        return  debug;
    }

    public void deleteById(UUID id) {
        vectorStorePoMapper.deleteById(id);
    }

    public void deleteByDocId(Long docId) {
        vectorStorePoMapper.deleteByDocId(docId);
    }
}
