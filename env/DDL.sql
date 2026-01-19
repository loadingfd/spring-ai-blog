-- SQL (用于在 psql 客户端执行)
-- 1) 创建数据库（只需运行一次）
CREATE DATABASE ragdb;

-- 2) 连接到刚刚创建的数据库（psql 元命令）
\c ragdb

-- 3) 在 ragdb 中创建扩展、表和索引
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE document_chunk (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    embedding VECTOR(1536) NOT NULL
);

-- CREATE INDEX idx_chunk_embedding
-- ON document_chunk USING ivfflat (embedding vector_l2_ops)
-- WITH (lists = 100);

CREATE INDEX hnsw_idx_chunking_embedding ON document_chunk USING hnsw (embedding vector_l2_ops)
    WITH (m = 16, ef_construction = 64);

CREATE TABLE ragdoc (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

alter table ragdoc add column updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
