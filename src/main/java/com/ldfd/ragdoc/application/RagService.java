package com.ldfd.ragdoc.application;

import com.ldfd.ragdoc.application.vo.MDocVo;
import com.ldfd.ragdoc.domain.MDocRepository;
import com.ldfd.ragdoc.domain.VectorStoreRepository;
import com.ldfd.ragdoc.domain.bo.MDoc;
import com.ldfd.ragdoc.exception.BusinessException;
import com.ldfd.ragdoc.infrastructure.mapper.ContentHashMapper;
import com.ldfd.ragdoc.infrastructure.mapper.po.ContentHashPo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RagService {

    private final ContentHashMapper contentHashMapper;
    private final MarkdownDocumentReaderConfig mdReaderConfig;
    private final VectorStore vectorStore;
    private final AuthService authService;
    private final VectorStoreRepository vectorStoreRepository;
    private final MDocRepository mDocRepository;

    /**
     * 预处理Markdown文档：分割、向量化、存储
     * @param docId 文档ID
     * @throws BusinessException 如果内容为空
     */
    public void preProcessMarkdown(Long docId) {
        MDoc mDoc = mDocRepository.findById(docId);
        Long userId = authService.getUserId();
        if (userId == null || !userId.equals(mDoc.getUserId())) {
            throw new BusinessException("403", "You do not have permission to access this document");
        }
        String content = mDoc.getContent();

        if (content == null || content.isBlank()) {
            throw new BusinessException("400", "Document content cannot be empty");
        }

        // 删除旧的向量化结果
        vectorStoreRepository.deleteByDocId(docId);

        // 计算内容哈希值以检测重复
        String contentHash = calculateSHA256(content);

        // 如果内容已处理过，则跳过以避免重复向量化
        if (isContentHashExists(contentHash)) {
            log.info("Content already processed, skipping vector embedding for docId: {}", docId);
            return;
        }

        try {
            // 读取并处理Markdown文档
            Resource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
            MarkdownDocumentReader mdReader = new MarkdownDocumentReader(resource, mdReaderConfig);
            List<Document> document = mdReader.read();
            document.forEach(doc -> doc.getMetadata()
                    .putAll(Map.of("userId", mDoc.getUserId()
                            ,"docId", docId)));
            // 分割文本为向量化处理的单元
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> splitDocs = splitter.split(document);
            // 添加分割后的文档到向量库
            vectorStore.add(splitDocs);
            mDoc.setProcessed(true);
            mDocRepository.update(mDoc);
            log.info("Successfully processed and embedded markdown for docId: {}", docId);
        } catch (Exception e) {
            log.error("Failed to preprocess markdown for docId: {}", docId, e);
            throw new BusinessException("500", "Failed to process document: " + e.getMessage());
        }
    }

    /**
     * 检查内容哈希是否已存在
     * @param contentHash SHA256哈希值
     * @return 如果哈希值已存在返回true，否则保存并返回false
     */
    private boolean isContentHashExists(String contentHash) {
        if (contentHashMapper.existsByContentHash(contentHash)) {
            return true;
        }

        // 保存新的内容哈希记录
        ContentHashPo po = new ContentHashPo();
        po.setContentHash(contentHash);
        contentHashMapper.save(po);
        return false;
    }

    /**
     * 计算文本的SHA256哈希值
     * @param content 文本内容
     * @return SHA256哈希值（16进制字符串）
     */
    private String calculateSHA256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));

            // 转换为16进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new BusinessException("500", "Failed to calculate content hash");
        }
    }

}
