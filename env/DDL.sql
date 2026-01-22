-- psql -U cbksb -d ragdb

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
    embedding VECTOR(1024) NOT NULL
);


CREATE INDEX hnsw_idx_chunking_embedding ON document_chunk USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

CREATE TABLE mdoc (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE chat_session (
  session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id BIGINT NOT NULL,
  title TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_session_user_id ON chat_session(user_id);

CREATE TABLE chat_message (
  message_id BIGSERIAL PRIMARY KEY, -- 消息唯一ID
  session_id UUID NOT NULL,          -- 所属会话ID
  sender TEXT NOT NULL,              -- 发送者（用户或系统）
  content TEXT NOT NULL,             -- 消息内容
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_message_session_id ON chat_message(session_id);

-- ALTER TABLE chat_message
--     ALTER COLUMN session_id TYPE UUID USING session_id::uuid;
