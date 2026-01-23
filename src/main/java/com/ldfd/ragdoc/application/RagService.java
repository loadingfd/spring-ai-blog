package com.ldfd.ragdoc.application;

import com.ldfd.ragdoc.application.vo.MDocVo;
import com.ldfd.ragdoc.infrastructure.mapper.ContentHashMapper;
import com.ldfd.ragdoc.infrastructure.mapper.po.ContentHashPo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class RagService {

    private final ContentHashMapper contentHashMapper;
    private final MarkdownDocumentReaderConfig mdReaderConfig;
    private final MDocService mDocService;
    private final VectorStore vectorStore;

    public void preProcessMarkdown(Long docId) {
        MDocVo mDoc = mDocService.getById(docId);
        String content = mDoc.getContent();
        if (content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content is empty");
        }
        if (isContentHashExists(content)) {
            return;
        }
        Resource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
        MarkdownDocumentReader mdReader = new MarkdownDocumentReader(resource, mdReaderConfig);
        TokenTextSplitter splitter = new TokenTextSplitter();
        vectorStore.add(splitter.split(mdReader.get()));
    }

    boolean isContentHashExists(String contentHash) {
        if (contentHashMapper.existsByContentHash(contentHash)) {
            return true;
        }
        ContentHashPo po = new ContentHashPo();
        po.setContentHash(contentHash);
        contentHashMapper.save(po);
        return false;
    }

}
