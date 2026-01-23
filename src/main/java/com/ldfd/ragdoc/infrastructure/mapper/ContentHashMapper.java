package com.ldfd.ragdoc.infrastructure.mapper;

import com.ldfd.ragdoc.infrastructure.mapper.po.ContentHashPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentHashMapper extends JpaRepository<ContentHashPo, Long> {
    boolean existsByContentHash(String contentHash);
}
