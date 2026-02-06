package com.ldfd.ragdoc.infrastructure.mapper;

import com.ldfd.ragdoc.infrastructure.mapper.po.VectorStorePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface VectorStorePoMapper extends JpaRepository<VectorStorePo, UUID> {

    @Query(
            value = "SELECT id, content, metadata FROM vector_store WHERE (metadata->>'userId')::bigint = :userId",
            nativeQuery = true
    )
    List<VectorStorePo> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(
        value = "DELETE FROM vector_store WHERE (metadata->>'docId')::bigint = :docId",
        nativeQuery = true
    )
    void deleteByDocId(@Param("docId") Long docId);
}
