# Spring AI Blog — RAG 智能问答系统

基于 **Spring AI** 构建的检索增强生成（RAG）文档问答平台，支持多模型接入、流式对话、向量检索与聊天历史持久化。

## ✨ 功能特性

| 模块 | 说明 |
|------|------|
| 用户认证 | JWT 登录 / 注册，Spring Security 鉴权 |
| 文档管理 | 上传、存储、查询 Markdown 文档（MDoc） |
| RAG 流水线 | 文档分块 → 向量化 → pgvector 存储 → 语义检索 |
| 智能对话 | 普通对话 & SSE 流式对话，支持多会话隔离 |
| 聊天记忆 | 基于 JDBC 的聊天历史持久化，跨会话可查 |
| 文档总结 | 对上传文档自动生成摘要 |
| 联网搜索 | 集成 SearXNG，实现实时网页检索增强 |
| 多模型支持 | Ollama（本地）/ ZhipuAI / OpenAI 可灵活切换 |

## 🏗️ 技术栈

- **Java 21** · **Spring Boot 3.5** · **Spring AI 1.1**
- **PostgreSQL + pgvector**（向量存储，HNSW 索引）
- **Ollama**（本地推理，开发环境默认 `qwen3:8b` 聊天 + `bge-m3` 向量化）
- **MapStruct** · **Lombok** · **JWT（jjwt 0.12）**

## 🚀 快速部署

### 前置条件

| 依赖 | 版本要求 |
|------|----------|
| JDK | 21+ |
| PostgreSQL + pgvector | pg17 / pg18（推荐使用官方镜像） |
| Ollama | 最新版，需提前拉取模型 |
| SearXNG（可选） | 任意版本，用于联网搜索 |

---

### 方式一：本地开发启动

#### 1. 启动 PostgreSQL（含 pgvector）

```bash
docker run -d \
  --name rag_postgres \
  -e POSTGRES_USER=cbksb \
  -e POSTGRES_PASSWORD=123456 \
  -e POSTGRES_DB=ragdb \
  -p 5432:5432 \
  pgvector/pgvector:pg18-trixie
```

或使用项目提供的 Compose 文件：

```bash
docker compose -f env/postgres.yaml up -d
```

#### 2. 初始化数据库表结构

```bash
psql -h localhost -U cbksb -d ragdb -f env/DDL.sql
```

#### 3. 启动 Ollama 并拉取模型

```bash
# 安装 Ollama 后执行：
ollama pull qwen3:8b      # 对话模型
ollama pull bge-m3        # 向量化模型
```

#### 4. 配置环境变量

```bash
export ZHIPUAI_API_KEY=<your-zhipuai-api-key>
export JWT_SECRET=<256-bit-random-string>
```

#### 5. 编译并运行

```bash
./gradlew bootRun
```

服务默认监听 **`http://localhost:8080`**。

---

### 方式二：Docker Compose 生产部署

> 项目根目录的 `Dockerfile` 已内置多阶段构建。

#### 1. 设置必要的环境变量

```bash
export DB_USERNAME=<db-user>
export DB_PASSWORD=<db-password>
export JWT_SECRET=<256-bit-random-string>
export ZHIPUAI_API_KEY=<your-zhipuai-api-key>   # 若使用 ZhipuAI
export OPENAI_API_KEY=<your-openai-api-key>       # 若使用 OpenAI
export OLLAMA_BASE_URL=http://ollama:11434         # 若 Ollama 以容器方式运行
```

#### 2. 启动所有服务

```bash
docker compose up -d
```

#### 3. 初始化数据库（首次部署）

```bash
docker exec -i rag_postgres psql -U $DB_USERNAME -d $DB_NAME < env/DDL.sql
```

---

### 生产环境关键配置（`application-prod.yaml`）

激活 prod profile：

```bash
export SPRING_PROFILES_ACTIVE=prod
```

| 环境变量 | 说明 | 是否必填 |
|----------|------|----------|
| `DB_USERNAME` | 数据库用户名 | ✅ |
| `DB_PASSWORD` | 数据库密码 | ✅ |
| `JWT_SECRET` | JWT 签名密钥（≥256 bit） | ✅ |
| `DB_HOST` | 数据库主机（默认 `localhost`） | 可选 |
| `DB_PORT` | 数据库端口（默认 `5432`） | 可选 |
| `DB_NAME` | 数据库名（默认 `ragdb`） | 可选 |
| `OLLAMA_BASE_URL` | Ollama 地址（默认 `http://localhost:11434`） | 可选 |
| `OLLAMA_CHAT_MODEL` | 对话模型（默认 `qwen2:8b`） | 可选 |
| `OLLAMA_EMBEDDING_MODEL` | 向量模型（默认 `bge-m3`） | 可选 |
| `ZHIPUAI_API_KEY` | 智谱 AI 密钥 | 可选 |
| `OPENAI_API_KEY` | OpenAI 密钥 | 可选 |
| `JWT_EXPIRATION` | Token 过期时间 ms（默认 `86400000`） | 可选 |

---

## 📡 主要 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/register` | 注册 |
| POST | `/auth/login` | 登录，返回 JWT |
| POST | `/chat` | 普通对话 |
| GET  | `/chat/stream` | SSE 流式对话 |
| POST | `/mdoc` | 上传文档 |
| POST | `/rag/process` | 文档向量化处理 |
| GET  | `/health` | 健康检查 |

> 除 `/auth/**` 和 `/health` 外，所有接口需在请求头中携带 `Authorization: Bearer <token>`。

## 📝 License

本项目仅供学习与参考，请遵守所使用 AI 服务的相关使用条款。
