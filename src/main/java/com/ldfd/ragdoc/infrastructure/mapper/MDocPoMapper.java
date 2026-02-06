package com.ldfd.ragdoc.infrastructure.mapper;

import com.ldfd.ragdoc.infrastructure.mapper.po.MDocPo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MDocPoMapper extends JpaRepository<MDocPo, Long> {

    List<MDocPo> findByUserId(Long userId);

}
