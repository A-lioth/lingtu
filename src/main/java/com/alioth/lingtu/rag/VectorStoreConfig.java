package com.alioth.lingtu.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

/**
 * 初始化向量数据库并保存文档-基于内存
 * 注意：此类已废弃，请使用 PgVectorStoreConfig
 *
 * @author alioth
 */
// @Configuration  // 已禁用，使用 PgVectorStoreConfig 代替
@Configuration
public class VectorStoreConfig {

    @Resource
    private KnowledgeLoader knowledgeLoader;

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate pgJdbcTemplate, EmbeddingModel ollamaEmbeddingModel) {
        VectorStore vectorStore = PgVectorStore.builder(pgJdbcTemplate, ollamaEmbeddingModel)
                .dimensions(1536)                    // Optional: defaults to model dimensions or 1536
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // 自动初始化 schema
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                .build();
        // 加载文档
        List<Document> documents = knowledgeLoader.loadMarkdown();
        vectorStore.add(documents);
        return vectorStore;
    }
}

