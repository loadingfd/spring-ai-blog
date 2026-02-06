package com.ldfd.ragdoc.domain;

import com.ldfd.ragdoc.domain.bo.VectorStore;
import com.ldfd.ragdoc.infrastructure.mapper.VectorStorePoMapper;
import com.ldfd.ragdoc.infrastructure.mapper.po.VectorStorePo;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VectorStoreRepository 集成测试
 * 需要运行真实的数据库进行测试
 * 如果没有测试数据库环境，请跳过此测试
 */
@SpringBootTest
class VectorStoreRepositoryIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreRepositoryIntegrationTest.class);
    @Autowired
    private VectorStoreRepository vectorStoreRepository;

    @Autowired
    private VectorStorePoMapper vectorStorePoMapper;

    @Test
    void testFindByUserId_Integration() {
        // Given
        // Long userId = 2L;

        // When
        // List<VectorStorePo> pos = vectorStorePoMapper.findByUserId(userId);

        // Then
        // assertNotNull(pos);
        // 根据实际数据库数据情况验证
        // log.info("pos={}", pos);
    }
}
