package com.ldfd.ragdoc.application;

import com.ldfd.ragdoc.adapter.controller.dto.UserDTO;
import com.ldfd.ragdoc.application.vo.ChatHistoryVo;
import com.ldfd.ragdoc.application.vo.UserVo;
import com.ldfd.ragdoc.domain.bo.ChatHistory;
import com.ldfd.ragdoc.infrastructure.converter.ChatHistoryConverter;
import com.ldfd.ragdoc.infrastructure.converter.UserConverter;
import com.ldfd.ragdoc.domain.UserRepository;
import com.ldfd.ragdoc.domain.bo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final PasswordEncoder passwordEncoder;
    private final ChatHistoryConverter chatHistoryConverter;

    @Transactional
    public UserVo create(UserDTO request) {
        User user = userConverter.dtoToBo(request);
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User saved = userRepository.save(user);
        return userConverter.boToVo(saved);
    }

    public UserVo getById(Long id) {
        User user = userRepository.findById(id);
        return userConverter.boToVo(user);
    }

    public List<UserVo> list() {
        List<User> boList = userRepository.findAll();
        return userConverter.boListToVo(boList);
    }

    @Transactional
    public UserVo update(Long id, UserDTO request) {
        User existing = userRepository.findById(id);
        userConverter.updateUserFromDto(request, existing);
        existing.setUpdatedAt(Instant.now());

        User updated = userRepository.update(existing);
        return userConverter.boToVo(updated);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public List<ChatHistoryVo> getUserSessions() {
        Long userId = authService.getUserId();
        List<ChatHistory> sessions = userRepository.getChatHistoriesByUserId(userId);
        return chatHistoryConverter.boListToVo(sessions);
    }

    public List<ChatHistoryVo> getSessionDetail(UUID sessionId) {
        Long userId = authService.getUserId();
        List<ChatHistory> bos = userRepository.getChatHistoryByUserIdAndSessionId(userId, sessionId);
        return chatHistoryConverter.boListToVo((bos));
    }

}
