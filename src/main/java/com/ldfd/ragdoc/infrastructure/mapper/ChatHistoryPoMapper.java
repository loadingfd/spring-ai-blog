package com.ldfd.ragdoc.infrastructure.mapper;

import com.ldfd.ragdoc.infrastructure.mapper.po.ChatHistoryPo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatHistoryPoMapper extends JpaRepository<ChatHistoryPo, Long> {

    /**
     * 查询用户的所有聊天会话，每个会话返回最早的用户消息
     * @param userId 用户ID
     * @return 每个会话最早的用户消息列表
     */
    @Query(value = """
    SELECT * FROM (
        SELECT *,
               ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY id) as rn
        FROM spring_ai_chat_history
        WHERE user_id = :userId AND type = 'USER'
    ) t
    WHERE t.rn = 1
    ORDER BY t.timestamp DESC
    """, nativeQuery = true)
    List<ChatHistoryPo> findByUserId(@Param("userId") Long userId);


    List<ChatHistoryPo> findByUserIdAndSessionIdOrderByTimestamp(Long userId, UUID sessionId, Sort sort);
}
