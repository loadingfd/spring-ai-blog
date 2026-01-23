package com.ldfd.ragdoc.infrastructure.mapper;

import com.ldfd.ragdoc.infrastructure.mapper.po.UserPo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPoMapper extends JpaRepository<UserPo, Long> {

}
