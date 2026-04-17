package com.alioth.lingtu.rag;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lang
 * @project prodigal-ai-travel
 * @Version: 1.0
 * @description token Transfer
 * @since 2026/4/1
 */
@Component
public class TokenTextSplitter {
    public List<Document> splitDocuments(List<Document> documents) {
        org.springframework.ai.transformer.splitter.TokenTextSplitter splitter = new org.springframework.ai.transformer.splitter.TokenTextSplitter();
        return splitter.apply(documents);
    }

    public List<Document> splitCustomized(List<Document> documents) {
        org.springframework.ai.transformer.splitter.TokenTextSplitter splitter = new org.springframework.ai.transformer.splitter.TokenTextSplitter(1000, 400, 10, 5000, true);
        return splitter.apply(documents);
    }
}
