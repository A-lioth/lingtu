# 灵途（lingtu）

灵途是一套面向中国境内旅游场景的 AI 对话助手：用户在聊天中即可获得景点讲解、行程规划、天气与出行建议、配图推荐，以及邮件 / PDF 输出等能力。仓库由「Spring Boot 后端主服务 + Umi/React 前端 + 独立 MCP 工具服务」三个可独立构建的子工程组成。

## 技术栈

以下信息来自 `pom.xml`、`lingtu-frontend/package.json`、`lingtu-mcp-server/pom.xml` 与 `src/main/resources/application*.yml`：

| 层次 | 技术 / 版本 | 来源 |
| --- | --- | --- |
| 语言 | Java 21（后端主服务与 MCP Server 均为 `java.version=21`） | `pom.xml` |
| 后端框架 | Spring Boot 3.5.13（parent） | `pom.xml` |
| AI 框架 | Spring AI 1.0.0（BOM）、Spring AI Alibaba 1.0.0.2（BOM） | `pom.xml` |
| 大模型 | DashScope（`spring-ai-alibaba-starter-dashscope`，配置模型 `qwen3-max-2026-01-23`）、Ollama（配置 `qwen3.5:4b`，嵌入模型 `nomic-embed-text`） | `application-local.yml` |
| 向量检索 | Spring AI PgVector Store（PostgreSQL + pgvector，HNSW / 1536 维 / COSINE_DISTANCE）、`spring-ai-rag` | `pom.xml`、`PgVectorStoreConfig.java` |
| 业务数据库 | MySQL（MyBatis-Plus 3.5.9 + `mysql-connector-j`），逻辑删除字段 `deleted` | `pom.xml`、`application-local.yml` |
| 缓存 | Redis（`spring-boot-starter-data-redis`，用于 JWT 白名单、邮箱验证码）；`caffeine` 已在 `pom.xml` 声明，但当前源码中未见直接调用 | `pom.xml` |
| 其他后端组件 | Hutool 5.8.37、Lombok、Apache POI 5.3.0、iText 9.1.0（PDF）、spring-boot-starter-mail、jsonschema-generator 4.38.0、Knife4j 4.4.0（OpenAPI 3） | `pom.xml` |
| 构建工具 | Maven（`spring-boot-maven-plugin`） | `pom.xml` |
| 前端 | Umi（`@umijs/max` 4.5.x）+ React 18.3 + Ant Design 5 + TypeScript 5.6，ReactMarkdown / remark-gfm 渲染 Markdown，axios 请求，npm 作为包管理器（`npmClient: npm`） | `package.json`、`config/config.ts` |
| MCP 服务 | Spring AI MCP Server（`spring-ai-starter-mcp-server-webmvc` 1.1.2），支持 SSE / stdio | `lingtu-mcp-server/pom.xml` |
| 外部 API | 高德天气（WeatherTool）、Pexels（图片搜索）、SMTP 邮件（QQ 邮箱示例） | `tools/`、`application-local.yml` |

## 目录结构

```
lingtu/
├─ src/                        # 后端主服务（Spring Boot）
│  ├─ main/java/com/alioth/lingtu/   # controller / service / rag / tools / security / config 等
│  ├─ main/resources/          # application.yml、application-local.yml、rag 知识库、mapper XML
│  └─ test/java/               # 少量单元测试
├─ lingtu-frontend/            # 前端工程（Umi + React）
│  ├─ config/                  # umi 配置（含 /api 代理到 8088）
│  ├─ src/                     # pages / components / services / contexts / layouts / utils
│  ├─ Dockerfile、nginx.conf   # 前端容器化构建与 Nginx 配置
│  └─ package.json
├─ lingtu-mcp-server/          # MCP 工具微服务（Spring AI MCP Server）
│  └─ src/main/                # ImageSearchTool、application-sse.yml / application-stdio.yml
├─ sql/                        # mysql.sql（建库建表）、postgresql.sql（pgvector 扩展与向量表）
├─ docs/                       # INTRODUCTION.md（项目介绍文档）
├─ temp/                       # 工具运行期产生的文件 / PDF 目录（FILE_SAVE_PATH）
├─ .kilocode/                  # 编辑器插件配置（mcp.json）
├─ pom.xml
└─ .gitignore
```

## 主要功能 / 模块说明

### 后端主服务（`src/main/java/com/alioth/lingtu`）

| 模块 | 关键类 | 说明 |
| --- | --- | --- |
| 启动入口 | `LingtuApplication` | `@SpringBootApplication`，排除 JDBC ChatMemory 自动配置，`@MapperScan` 扫描 mapper，启动后打印 ASCII 横幅 |
| 对话接口 | `controller/LingtuAssistantController` | `POST /travel/chat/sse` 以 `Flux<String>` 返回 SSE 流式回答；`GET/DELETE /travel/conversations` 查询与删除历史会话；`/travel/chat`、`/travel/chat/sse_emitter`、`/travel/manus/chat` 目前为注释状态 |
| 认证接口 | `controller/AuthController` | `/auth/send-code`、`/auth/register`、`/auth/login`、`/auth/login/by-code`、`/auth/logout`、`/auth/deregister` |
| 健康检查 | `controller/HealthController` | `POST /health/check` 返回 `OK` |
| 对话客户端 | `client/LingtuChatClient` | 分别构建 Ollama ChatClient（内存会话记忆）与 DashScope ChatClient（MySQL 会话记忆）；提供 `chatWithOllama`、`chatWithDashscopeBySse`、`chatWithRag`、`chatWithTools` 四种调用方式 |
| 会话记忆 | `chatmemory/MySQLChatMemory` | 实现 Spring AI `ChatMemory`，把消息写入 `chat_conversation` / `chat_message`；会话 id 格式为 `userId:chatId` |
| RAG 链路 | `rag/DocumentLoader`、`TokenTextSplitter`、`KeywordEnricher`、`QueryRewriter`、`RagCustomAdvisorFactory` | 加载 `classpath:/rag/*.md` 并按语义切分；查询重写；`MultiQueryExpander`（3 个变体）+ `VectorStoreDocumentRetriever`（阈值 0.7、Top-K 3）+ `ContextualQueryAugmenter` 组成检索增强顾问 |
| 向量库初始化 | `config/PgVectorStoreConfig`、`config/PgVectorDataSourceConfig` | 独立 PostgreSQL 数据源；建表 `vector_store1`；启动时将知识库切分、加关键词、按内容去重后批量写入（每批 25 条） |
| 工具注册 | `tools/ToolRegisterConfig` | 通过 `ToolCallbacks.from(...)` 注册 File、WebSearch、Email、Weather、DateTime、IP、ImageSearch、PDF、Terminate 共 9 个工具 |
| 具体工具 | `tools/FileOperationTool`、`WebSearchTool`、`EmailTool`、`WeatherTool`、`DateTimeTool`、`IPTool`、`ImageSearchTool`、`PDFGenerationTool`、`TerminateTool`、`ClientIpResolver` | 文件读写、联网搜索、邮件发送（纯文本 / HTML / 附件 / 图片 URL 附件，最多 5 张图）、天气查询、时间、IP 归属地、配图检索、PDF 生成、任务终止 |
| 安全 | `security/JwtAuthenticationFilter`、`JwtTokenHelper`、`LoginTokenCache`、`config/AuthWebFilterConfig` | 仅拦截 `/travel/*`；校验 `Authorization: Bearer` 头 + Hutool JWT 验签 + Redis 白名单，通过后写入 `REQUEST_ATTR_USER_ID`；未通过返回 401 JSON |
| 业务服务 | `service/auth/UserAuthService`、`UserService`、`service/chat/ChatConversationService`、`ChatMessageService`、`service/MailService` | 注册 / 登录 / 验证码 / 退出 / 注销；会话与消息持久化；邮件发送 |
| 通用设施 | `common/BaseResult`、`ResultUtils`、`exception/GlobalExceptionHandler`、`BusinessException`、`ThrowUtils`、`ResponseStatus` | 统一响应结构与全局异常处理 |
| 配置 | `config/CorsConfig`、`MailConfig`、`MybatisPlusConfig`、`OpenApiSecurityConfig`、`properties/JwtProperties`、`AuthRegisterProperties` | 跨域、邮件、MyBatis-Plus 分页、Knife4j 鉴权展示、JWT 与注册验证码参数 |

### RAG 知识库（`src/main/resources/rag/`）

共 8 篇 Markdown，聚焦贵州与「三山五岳」主题：

- `01-贵州旅游景点概览.md`、`02-贵州景点详解与开放信息.md`、`03-贵州气候季节与出行建议.md`、`04-贵州行程规划-按出发地智能调整.md`
- `05-三山五岳旅游总览与目的地画像.md`、`06-三山五岳逐山详解与游玩强度.md`、`07-三山五岳路线规划模板-按天数与出发区域.md`、`08-三山五岳季节天气与人群出行建议.md`

系统提示词 `constant/LingtuConstant.SYSTEM_PROMPT` 将助手限定为「灵途」旅游垂直助手，并约定工具调用规则（时间、天气、PDF、邮件、配图等）。

### 前端（`lingtu-frontend/src`）

| 模块 | 位置 | 说明 |
| --- | --- | --- |
| 入口页 | `pages/entry/index.tsx` | 选择「AI 旅游助手（travel）」或「超级智能体（manus）」模式后跳转 |
| 对话页 | `pages/chat/travel/index.tsx`、`pages/chat/manus/index.tsx`、`pages/chat/index.tsx` | `/chat` 重定向到 `/entry`；travel / manus 复用 `ChatWorkspace` |
| 对话工作台 | `components/chat/ChatWorkspace.tsx`、`MessageList.tsx`、`MessageInput.tsx`、`Sidebar.tsx`、`ToolPanel.tsx`、`UserSessionBlock.tsx` | 消息流渲染、输入、历史会话侧栏、工具示例面板 |
| 认证 UI | `components/auth/AuthScreen.tsx`、`AuthCard.tsx`、`contexts/AuthContext.tsx` | 登录注册界面与登录态上下文 |
| 服务层 | `services/travelChat.ts`、`services/api.ts` | SSE 流解析（`streamTravelAnswer` / `streamManusAnswer`，路径 `/api/travel/chat/sse`、`/api/travel/manus/chat`），兼容 CRLF 与 `[DONE]` |
| 接口定义 | `src/api/`、`src/generated/`、`types/` | 由 OpenAPI（`openapi2ts`）生成的接口与类型 |
| 工具函数 | `utils/authStorage.ts`、`chatMode.ts`、`constants.ts`、`imageUrl.ts`、`manusChatStorage.ts`、`manusStepSummary.ts`、`manusStreamRouting.ts`、`apiResult.ts` | token 存储、模式持久化、Manus 步骤摘要等 |

### MCP 工具服务（`lingtu-mcp-server`）

- `LingtuMcpServerApplication`（包名实际为 `com.prodigal.mcp`）注册 `ToolCallbackProvider`，挂载 `ImageSearchTool`。
- `ImageSearchTool` 调用 Pexels `https://api.pexels.com/v1/search`，按关键词返回逗号分隔的图片 URL。
- `application-sse.yml`：`spring.ai.mcp.server` 开启 Web（SSE），`base-url: /api/v1`；`application-stdio.yml`：`stdio: true` 且 `web-application-type: none`。
- 主服务若注入 `ToolCallbackProvider`，可将 MCP 工具与本地工具一起挂载到 ChatClient（`LingtuAssistantController` 已保留相关依赖与注释代码）。

## 运行 / 构建方式

### 前置环境

- JDK 21、Maven
- Node.js ≥ 18（`package.json` 的 `engines.node`）、npm
- MySQL 8（业务库 `lingtu`）、PostgreSQL（需启用 `vector` 扩展）、Redis
- 可选：本地 Ollama（`http://localhost:11434`）及 `qwen3.5:4b`、`nomic-embed-text` 模型

### 1. 数据库初始化

```bash
# MySQL：建库、建表（user / chat_conversation / chat_message / spring_ai_chat_memory）并插入 admin 账号
mysql -u root -p < sql/mysql.sql

# PostgreSQL：启用 uuid-ossp 与 vector 扩展，创建 public.vector_store1
psql -U <user> -d postgres -f sql/postgresql.sql
```

### 2. 后端主服务

```bash
# 在仓库根目录
mvn spring-boot:run
# 或先打包
mvn clean package
java -jar target/lingtu-0.0.1-SNAPSHOT.jar
```

- 默认端口 `8088`，`server.servlet.context-path=/api`，因此接口实际路径形如 `http://localhost:8088/api/travel/chat/sse`。
- 默认激活 `local` profile（`application.yml` 中 `spring.profiles.active: local`），配置读取 `src/main/resources/application-local.yml`。
- 该文件中的数据库账号、DashScope API Key、邮箱密码等为示例/明文值，运行前需替换为自己的配置。
- 接口文档：Knife4j / Swagger UI 位于 `/api/swagger-ui.html`，OpenAPI JSON 位于 `/api/v3/api-docs`。

### 3. 前端

```bash
cd lingtu-frontend
npm install        # 会自动执行 postinstall: umi setup
npm run dev        # umi dev（config 中未指定端口，使用 Umi 默认 8000）
# 生产构建
npm run build      # 产物输出到 dist/
```

- `config/config.ts` 中把 `/api` 代理到 `http://localhost:8088`，开发时无需额外配置跨域。
- 也可用 `Dockerfile` 构建镜像（Node 20 构建 + Nginx 1.27 托管，`nginx.conf` 将 `/api/` 反代到 `prodigal-api:8088`）。

### 4. MCP Server（可选）

```bash
cd lingtu-mcp-server
mvn spring-boot:run        # 默认端口 8090，profile 为 local,sse
```

- SSE 模式端点前缀 `/api/v1`；如需 stdio 模式，将激活 profile 换成 `stdio`（见 `application-stdio.yml`）。
- 需在 `application.yml` 中填写 `pexels.api-key`（当前为占位值 `XXX`）。

### 测试

```bash
mvn test                                  # 根目录后端测试（LingtuApplicationTests 等）
cd lingtu-mcp-server && mvn test          # MCP Server 测试
```

## 备注

- 本仓库是学习 / 练习性质的个人项目：包名与注释中残留 `prodigal-travel`、`com.prodigal` 等旧命名（如 `LingtuChatClient` 的注释、`lingtu-mcp-server` 的 `LingtuMcpServerApplication` 实际位于 `com.prodigal.mcp` 包），`docs/INTRODUCTION.md` 也提到部分能力「规划中」。
- 部分代码处于注释/未启用状态：`service/TravelAssistantService.java` 整个文件被注释；`LingtuAssistantController` 中的 `/travel/chat`、`/travel/chat/sse_emitter`、`/travel/manus/chat` 被注释，前端 Manus 模式对应的后端链路尚未接通。
- `docs/INTRODUCTION.md` 已对项目做过一次梳理，本 README 以其为参考并结合实际源码/配置核对；两者若有出入（例如 Manus 后端状态），以代码为准。
- `application-local.yml` 当前未纳入版本控制（`git status` 显示为未跟踪），但其中含真实形态的 API Key、数据库口令与邮箱授权码，提交前务必脱敏或改用环境变量。
- `temp/` 目录用于 `PDFGenerationTool`、`FileOperationTool` 的输出（`LingtuConstant.FILE_SAVE_PATH`），运行时自动产生文件。
- 仓库根目录原有 `docs/`、`.kilocode/`、`temp/` 等目录，`target/` 与前端 `node_modules/` 属于构建/依赖产物，不在本文档的目录树中展开。
