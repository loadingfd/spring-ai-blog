-- psql -U cbksb -d ragdb

-- SQL (用于在 psql 客户端执行)
-- 1) 创建数据库（只需运行一次）
CREATE DATABASE ragdb;

-- 2) 连接到刚刚创建的数据库（psql 元命令）
\c ragdb

-- 3) 在 ragdb 中创建扩展、表和索引
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE mdoc (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE content_hash (
    id BIGSERIAL PRIMARY KEY,
    content_hash TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 采用jsonb 替代官方的json， 可以建立索引
create table vector_store
(
    id uuid default uuid_generate_v4() not null
        primary key,
    content   text,
    metadata  jsonb,
    embedding vector(1024)
);

CREATE INDEX CONCURRENTLY idx_vector_store_userid_int ON vector_store (((metadata->>'userId')::bigint));
CREATE INDEX CONCURRENTLY idx_vector_store_docid_int ON vector_store (((metadata->>'docId')::bigint));
-- ddl/create_spring_ai_chat_history.sql
-- 持久化聊天记录
CREATE TABLE spring_ai_chat_history (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL,
    session_id uuid,
    content text,
    type varchar(16) NOT NULL CHECK (type IN ('USER','ASSISTANT','SYSTEM','TOOL')),
    timestamp timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_chat_history_user_id ON spring_ai_chat_history(user_id);
CREATE INDEX idx_chat_history_session_id ON spring_ai_chat_history(session_id);
