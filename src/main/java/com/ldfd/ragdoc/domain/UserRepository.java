package com.ldfd.ragdoc.domain;

import com.ldfd.ragdoc.domain.bo.ChatHistory;
import com.ldfd.ragdoc.domain.bo.User;
import com.ldfd.ragdoc.exception.BusinessException;
import com.ldfd.ragdoc.infrastructure.converter.UserConverter;
import com.ldfd.ragdoc.infrastructure.mapper.UserPoMapper;
import com.ldfd.ragdoc.infrastructure.mapper.po.UserPo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final ChatHistoryRepository chatHistoryRepository;
    private final UserPoMapper userPoMapper;
    private final UserConverter userConverter;

    public User save(User user) {
        Assert.notNull(user, "user is required");
        UserPo po = userConverter.boToPo(user);
        UserPo saved = userPoMapper.save(po);
        return userConverter.poToBo(saved);
    }

    public User update(User user) {
        Assert.notNull(user, "user is required");
        Assert.notNull(user.getId(), "user id is required for update");

        if (!userPoMapper.existsById(user.getId())) {
            throw new BusinessException("404", "User not found with id: " + user.getId());
        }

        UserPo po = userConverter.boToPo(user);
        UserPo updated = userPoMapper.save(po);
        return userConverter.poToBo(updated);
    }

    public User findById(Long id) {
        Assert.notNull(id, "id is required");

        UserPo po = userPoMapper.findById(id)
                .orElseThrow(() -> new BusinessException("404", "User not found with id: " + id));
        return userConverter.poToBo(po);
    }

    public List<User> findAll() {
        return userPoMapper.findAll().stream()
                .map(userConverter::poToBo)
                .toList();
    }

    public void deleteById(Long id) {
        Assert.notNull(id, "id is required");
        if (!userPoMapper.existsById(id)) {
            throw new BusinessException("404", "User not found with id: " + id);
        }
        userPoMapper.deleteById(id);
    }

    public Optional<User> findByUsername(String username) {
        Assert.notNull(username, "username is required");
        return userPoMapper.findByUsername(username)
                .map(userConverter::poToBo);
    }

    public boolean existsByUsername(String username) {
        Assert.notNull(username, "username is required");
        return userPoMapper.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        Assert.notNull(email, "email is required");
        return userPoMapper.existsByEmail(email);
    }

    public List<ChatHistory> getChatHistoriesByUserId(Long userId) {
        Assert.notNull(userId, "userId is required");
        return chatHistoryRepository.findByUserId(userId);
    }

    public List<ChatHistory> getChatHistoryByUserIdAndSessionId(Long userId, UUID sessionId) {
        Assert.notNull(userId, "userId is required");
        Assert.notNull(sessionId, "sessionId is required");
        return chatHistoryRepository.findByUserIdAndSessionId(userId, sessionId);
    }
}
