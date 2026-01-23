package com.ldfd.ragdoc.application;

import com.ldfd.ragdoc.adapter.controller.dto.UserDTO;
import com.ldfd.ragdoc.application.vo.UserVo;
import com.ldfd.ragdoc.application.mapper.UserMapper;
import com.ldfd.ragdoc.domain.UserRepository;
import com.ldfd.ragdoc.domain.bo.User;
import com.ldfd.ragdoc.infrastructure.mapper.po.UserPo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserVo create(UserDTO request) {
        User user = userMapper.dtoToBo(request);
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        UserPo saved = userRepository.save(userMapper.boToPo(user));
        return userMapper.boToVo(userMapper.poToBo(saved));
    }

    public UserVo getById(Long id) {
        UserPo po = userRepository.findById(id);
        return userMapper.boToVo(userMapper.poToBo(po));
    }

    public List<UserVo> list() {
        List<User> boList = userRepository.findAll().stream()
                .map(userMapper::poToBo)
                .toList();
        return userMapper.boListToVo(boList);
    }

    @Transactional
    public UserVo update(Long id, UserDTO request) {
        User existing = userMapper.poToBo(userRepository.findById(id));
        userMapper.updateUserFromDto(request, existing);
        existing.setUpdatedAt(Instant.now());

        UserPo updated = userRepository.update(userMapper.boToPo(existing));
        return userMapper.boToVo(userMapper.poToBo(updated));
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
