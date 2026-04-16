-- pg vector 向量数据库，暂时不启用
SELECT * FROM pg_extension WHERE extname = 'vector';

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE IF NOT EXISTS vector_store (
                                                   id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
                                                   content text,
                                                   metadata json,
                                                   embedding vector(1536)
);

-- 确保 uuid-ossp 扩展已启用
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS vector;

-- 1. 删除旧表
DROP TABLE IF EXISTS vector_store;

-- 2. 创建新表（关键：id 为 TEXT，embedding 维度与你模型一致）
-- 假设你使用 OpenAI（1536维）：
CREATE TABLE vector_store (
                              id TEXT PRIMARY KEY,          -- 必须是 TEXT！
                              content TEXT,
                              metadata JSONB,
                              embedding VECTOR(1536)        -- 必须与你的 embedding model 维度一致
);

-- 3. 创建索引
CREATE INDEX vector_store_embedding_idx ON vector_store USING hnsw (embedding vector_cosine_ops);

-- 1. 删除旧表（确保干净）
DROP TABLE IF EXISTS vector_store;

-- 2. 确保 pgvector 扩展已安装（必须！）
CREATE EXTENSION IF NOT EXISTS vector;

-- 3. 让 Spring AI 自动建表（推荐）
--   → 不要手动建！直接重启应用
