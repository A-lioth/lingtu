-- 确保 uuid-ossp 扩展已启用
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS vector;

-- 1. 开启 pgvector 扩展 (如果尚未开启)
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. 创建存储向量的表
CREATE TABLE IF NOT EXISTS public.vector_store1
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content   TEXT,
    metadata  JSONB,
    embedding VECTOR(1536)
);

DROP TABLE IF EXISTS public.vector_store;
