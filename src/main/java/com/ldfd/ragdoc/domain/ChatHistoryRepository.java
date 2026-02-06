package com.ldfd.ragdoc.domain;

import com.ldfd.ragdoc.domain.bo.ChatHistory;
import com.ldfd.ragdoc.exception.BusinessException;
import com.ldfd.ragdoc.infrastructure.converter.ChatHistoryConverter;
import com.ldfd.ragdoc.infrastructure.mapper.ChatHistoryPoMapper;
import com.ldfd.ragdoc.infrastructure.mapper.po.ChatHistoryPo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ChatHistoryRepository {

    private final ChatHistoryPoMapper chatHistoryPoMapper;
    private final ChatHistoryConverter chatHistoryConverter;

    public ChatHistory save(ChatHistory chatHistory) {
        Assert.notNull(chatHistory, "chatHistory is required");
        ChatHistoryPo po = chatHistoryConverter.boToPo(chatHistory);
        ChatHistoryPo saved = chatHistoryPoMapper.save(po);
        return chatHistoryConverter.poToBo(saved);
    }

    public ChatHistory update(ChatHistory chatHistory) {
        Assert.notNull(chatHistory, "chatHistory is required");
        Assert.notNull(chatHistory.getId(), "chatHistory id is required for update");

        if (!chatHistoryPoMapper.existsById(chatHistory.getId())) {
            throw new BusinessException("404", "ChatHistory not found with id: " + chatHistory.getId());
        }

        ChatHistoryPo po = chatHistoryConverter.boToPo(chatHistory);
        ChatHistoryPo updated = chatHistoryPoMapper.save(po);
        return chatHistoryConverter.poToBo(updated);
    }

    public ChatHistory findById(Long id) {
        Assert.notNull(id, "id is required");

        ChatHistoryPo po = chatHistoryPoMapper.findById(id)
                .orElseThrow(() -> new BusinessException("404", "ChatHistory not found with id: " + id));
        return chatHistoryConverter.poToBo(po);
    }

    public List<ChatHistory> findAll() {
        return chatHistoryPoMapper.findAll().stream()
                .map(chatHistoryConverter::poToBo)
                .toList();
    }

    public void deleteById(Long id) {
        Assert.notNull(id, "id is required");
        if (!chatHistoryPoMapper.existsById(id)) {
            throw new BusinessException("404", "ChatHistory not found with id: " + id);
        }
        chatHistoryPoMapper.deleteById(id);
    }

    // 获取非重复的聊天列表，每个会话返回最早的用户消息，content截取前10个字符
    // 只包含id, content, timestamp 字段
    public List<ChatHistory> findByUserId(Long userId) {
        Assert.notNull(userId, "userId is required");
        return chatHistoryPoMapper.findByUserId(userId).stream()
                .map(po -> {
                    ChatHistory chatHistory = chatHistoryConverter.poToBo(po);
                    // 截取 content 的前10个字符
                    if (chatHistory.getContent() != null && chatHistory.getContent().length() > 10) {
                        chatHistory.setContent(chatHistory.getContent().substring(0, 10));
                    }
                    return chatHistory;
                })
                .toList();
    }

    public List<ChatHistory> findByUserIdAndSessionId(Long userId, UUID sessionId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        return chatHistoryPoMapper.findByUserIdAndSessionIdOrderByTimestamp(userId, sessionId, sort)
                .stream()
                .map(chatHistoryConverter::poToBo)
                .toList();
    }
}
