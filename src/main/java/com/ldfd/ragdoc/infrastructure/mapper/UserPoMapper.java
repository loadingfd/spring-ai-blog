package com.ldfd.ragdoc.infrastructure.mapper;

import com.ldfd.ragdoc.infrastructure.mapper.po.UserPo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPoMapper extends JpaRepository<UserPo, Long> {

    Optional<UserPo> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
