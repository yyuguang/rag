# 文档信息

- 文档名称：MICROSERVICE_DATA_MODEL.md
- 当前状态：已完成
- 最近更新阶段：data-designer
- 最近更新原因：基于 C 方案“绞杀者式渐进拆分”和本轮服务合并调整，补充微服务独立 schema、表归属、事件表、缓存 key 和索引归属

# 0. Workflow Orchestrator 任务识别

- 任务归属：`enterprise_rag_platform_design`
- 任务类型：L4 高风险架构设计 / 微服务演进规划 / 数据模型设计
- 当前主流程：`workflow-orchestrator -> design-researcher -> system-architect -> data-designer`
- 本轮限制：不写业务代码，不创建 Java 服务工程，不输出 DDL 实现脚本
- 方案结论：采用 C 方案，绞杀者式渐进拆分
- 关键调整：第一阶段不单独拆 `tenant-service` 和 `iam-service`；认证、租户、用户、部门、角色、权限、数据范围、权限快照、租户配置、租户配额和服务间鉴权均归属 `auth-service`

# 1. 数据设计依据与规则门禁

## 1.1 已读取与适用规则

- `AGENTS.md`
- `.ai_rules/README.md`
- `.ai_rules/API_STYLE.md`
- `.ai_rules/DB_STYLE.md`
- `.ai_rules/CODING_STYLE.md`
- `.ai_rules/PROJECT_STRUCTURE.md`
- `.ai_rules/SERVICE_STYLE.md`
- `.ai_rules/COMMENT_STYLE.md`
- `.ai_rules/LOGGING_STYLE.md`
- `02_design/DESIGN_RESEARCH.md`
- `02_design/DESIGN.md`
- `02_design/MICROSERVICE_DESIGN.md`
- `02_design/API_SPEC.md`
- `02_design/DATA_MODEL.md`
- `02_design/MICROSERVICE_API_SPEC.md`

## 1.2 `.ai_rules` 对本数据文档的约束

- 表名和字段名使用 snake_case。
- 业务表必须包含 `id`、`create_by`、`create_time`、`update_by`、`update_time`。
- 推荐包含 `tenant_id`、`is_deleted`、`version`、`status`。
- 唯一约束必须落库。
- 查询条件字段需要索引，联合索引顺序必须服务真实查询。
- 状态字段使用枚举语义。
- 金额和成本使用 decimal，禁止 float / double。
- 禁止跨服务直接查库，禁止跨 schema join。
- 服务间只通过 API、事件或只读投影同步数据。

# 2. 数据基础设施主方案

| 类型 | 主方案 | 说明 |
| --- | --- | --- |
| 关系数据库 | MySQL 8.0 | 每个服务独立 schema；第一阶段可共用同一 MySQL 实例，但必须独立账号和权限 |
| 缓存 | Redis | 权限快照、租户配置、限流计数、检索缓存、任务锁、热点统计 |
| 对象存储 | MinIO | 原始文档、解析中间文件、导出文件 |
| 消息队列 | RabbitMQ / Kafka | 第一阶段 RabbitMQ，吞吐或事件回放要求增强后演进 Kafka |
| 关键词索引 | OpenSearch / Elasticsearch | BM25、过滤、高亮、索引投影 |
| 向量库 | Milvus / Qdrant | Chunk 向量、ANN 检索、metadata filter |

明确不作为主方案：

- 不使用 PostgreSQL + pgvector 作为主向量检索方案。
- 不使用 Sa-Token 作为主认证方案，认证仍基于 Spring Security。
- 不使用 LangChain4j 作为主 AI 编排方案，主方案为 Spring AI + 自研 LLM Gateway。

# 3. 独立 schema 总览

| 服务 | schema | 数据真相 | 允许外部访问方式 |
| --- | --- | --- | --- |
| `auth-service` | `rag_auth` | 认证、租户、用户、部门、角色、权限、数据范围、权限快照、租户配置、租户配额、服务间凭证 | `/internal/v1/auth/*` API 和事件 |
| `kb-service` | `rag_kb` | 知识库、分类、KB ACL、检索配置 | `/internal/v1/knowledge-bases/*` API 和事件 |
| `document-service` | `rag_document` | 文档元数据、文档版本、文档 ACL、Chunk 元数据真相、处理任务入口 | `/internal/v1/documents/*` API 和事件 |
| `document-worker` | `rag_document_worker` | Worker 任务、尝试记录、消费记录、处理过程快照 | MQ 事件和内部 API |
| `embedding-service` | `rag_embedding` | Embedding 请求批次、模型调用摘要、向量生成记录 | `/internal/v1/embeddings/*` API 和事件 |
| `retrieval-service` | `rag_retrieval` | 检索索引投影、检索缓存元数据、重排记录、索引写入任务 | `/internal/v1/retrieval/*` API 和事件 |
| `rag-chat-service` | `rag_chat` | 会话、消息、引用快照、问答请求状态 | `/api/v1/chat/*` 和内部可见性 API |
| `llm-gateway-service` | `rag_llm_gateway` | 模型配置、供应商配置、API Key 密文、原始 Token 用量、成本记录、限流状态 | `/internal/v1/llm/*` API 和事件 |
| `prompt-service` | `rag_prompt` | Prompt 模板、版本、变量契约、发布记录、灰度策略 | `/internal/v1/prompts/*` API 和事件 |
| `feedback-service` | `rag_feedback` | 用户反馈、反馈处理、反馈标签 | `/api/v1/feedback/*` API 和事件 |
| `evaluation-service` | `rag_evaluation` | 评测集、评测用例、评测运行、评分结果 | `/api/v1/evaluations/*` API 和事件 |
| `audit-service` | `rag_audit` | 审计事件、归档批次、风险审计记录 | `/internal/v1/audit/*` API 和事件 |
| `statistics-service` | `rag_statistics` | 统计事件、日/月聚合、报表投影 | `/api/v1/statistics/*` API 和事件 |
| `admin-config-service` | `rag_admin_config` | 管理后台编排记录、操作草稿、审批流和配置发布任务 | 通过目标服务 API 编排 |

# 4. 强制数据边界

## 4.1 禁止行为

- 禁止 `rag-chat-service` 直接查询 `rag_auth`、`rag_document` 或 `rag_prompt` schema。
- 禁止 `retrieval-service` 直接查询 `rag_auth` 的用户、角色、权限表。
- 禁止 `admin-config-service` 直接修改 `rag_llm_gateway` 或 `rag_prompt` 表。
- 禁止任何服务跨 schema join。
- 禁止多个服务共享同一张业务真相表。
- 禁止把数据库 Entity 作为服务间 DTO 暴露。

## 4.2 允许的数据协作方式

- 同步 API：用于权限快照、可读范围、文档权限、Prompt 渲染、模型调用等强一致或近实时链路。
- 异步事件：用于审计、统计、文档处理、权限变更、索引构建、评测结果。
- 只读投影：用于统计、检索索引、后台聚合，但投影不是业务真相。
- Outbox / Inbox：每个写服务使用本地 Outbox 发表事件，消费者使用 Inbox / consume log 幂等。

# 5. 通用表规范

## 5.1 通用字段

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | bigint | 是 | 主键 |
| `tenant_id` | bigint | 业务表必填 | 租户 ID；平台级全局配置可使用 `0` |
| `status` | varchar(32) | 推荐 | 业务状态枚举 |
| `create_by` | varchar(64) | 是 | 创建人 |
| `create_time` | datetime | 是 | 创建时间，统一时区 |
| `update_by` | varchar(64) | 是 | 更新人 |
| `update_time` | datetime | 是 | 更新时间，统一时区 |
| `is_deleted` | tinyint(1) | 推荐 | 软删除，0 未删除，1 已删除 |
| `version` | int | 推荐 | 乐观锁版本 |

## 5.2 每个服务必须具备的事件表

| 表 | 归属 | 说明 | 核心索引 |
| --- | --- | --- | --- |
| `{service}_outbox_event` | 每个会产生领域事件的服务 | 本地事务内写入，异步发布到 MQ / Kafka | `uk_event_id(event_id)`、`idx_status_time(status, create_time)` |
| `{service}_inbox_event` 或 `{service}_consume_log` | 每个消费者服务 | 记录已消费事件，保证至少一次投递下的幂等 | `uk_event_consumer(event_id, consumer_group)`、`idx_status_time(status, consume_time)` |

Outbox 通用字段：

- `event_id`
- `event_type`
- `aggregate_type`
- `aggregate_id`
- `tenant_id`
- `payload_json`
- `trace_id`
- `status`
- `retry_count`
- `next_retry_time`
- `published_time`

Inbox / consume log 通用字段：

- `event_id`
- `consumer_group`
- `event_type`
- `tenant_id`
- `status`
- `consume_time`
- `error_message`
- `retry_count`

# 6. `auth-service` 数据模型

## 6.1 schema 职责

`rag_auth` 是第一阶段权限与租户治理的统一数据真相，负责：

- auth：登录认证、密码凭证、Refresh Token、登录日志。
- tenant：租户基本信息、状态、租户配置、租户配额。
- user：用户资料、用户状态、用户扩展属性。
- dept：部门树、部门路径。
- role：角色、用户角色关系。
- permission：API 权限、菜单权限、按钮权限、资源权限。
- data_scope：租户、部门、本人、指定范围数据权限。
- permission_snapshot：权限快照和权限版本。
- tenant_config：租户级模型、检索、安全、限流策略。
- tenant_quota：租户 Token、存储、文档数、并发配额与用量。
- service credential：服务间鉴权凭证、调用范围和轮换记录。

## 6.2 核心表

| 表 | 说明 | 关键字段 | 主要索引 |
| --- | --- | --- | --- |
| `auth_tenant` | 租户基本信息 | `tenant_code`、`tenant_name`、`status`、`expire_time` | `uk_tenant_code(tenant_code)`、`idx_tenant_status(status)` |
| `auth_tenant_config` | 租户配置 | `tenant_id`、`config_key`、`config_value_json`、`config_version` | `uk_tenant_config(tenant_id, config_key)` |
| `auth_tenant_quota` | 租户配额 | `tenant_id`、`resource_type`、`quota_limit`、`period_type`、`status` | `uk_quota_resource(tenant_id, resource_type, period_type)` |
| `auth_tenant_quota_usage` | 租户配额用量 | `tenant_id`、`resource_type`、`period_key`、`used_amount`、`reserved_amount` | `uk_quota_usage(tenant_id, resource_type, period_key)` |
| `auth_quota_reservation` | 配额预占 | `tenant_id`、`resource_type`、`request_id`、`reserved_amount`、`status` | `uk_quota_request(tenant_id, request_id)` |
| `auth_user` | 用户资料 | `tenant_id`、`username`、`display_name`、`dept_id`、`status` | `uk_user_tenant_username(tenant_id, username)`、`idx_user_dept(tenant_id, dept_id)` |
| `auth_user_credential` | 用户凭证 | `tenant_id`、`user_id`、`password_hash`、`password_version`、`status` | `uk_credential_user(tenant_id, user_id)` |
| `auth_refresh_token` | Refresh Token | `tenant_id`、`user_id`、`jti`、`device_id`、`expire_time`、`revoked` | `uk_refresh_jti(jti)`、`idx_refresh_user(tenant_id, user_id)` |
| `auth_login_log` | 登录日志 | `tenant_id`、`user_id`、`username`、`result_status`、`client_ip`、`trace_id` | `idx_login_user_time(tenant_id, user_id, create_time)`、`idx_login_trace(trace_id)` |
| `auth_dept` | 部门树 | `tenant_id`、`parent_id`、`ancestors`、`dept_path`、`status` | `idx_dept_parent(tenant_id, parent_id)`、`idx_dept_path(tenant_id, dept_path)` |
| `auth_role` | 角色 | `tenant_id`、`role_code`、`role_type`、`status` | `uk_role_code(tenant_id, role_code)` |
| `auth_user_role` | 用户角色关系 | `tenant_id`、`user_id`、`role_id` | `uk_user_role(tenant_id, user_id, role_id)`、`idx_role_user(tenant_id, role_id)` |
| `auth_permission` | 权限点 | `permission_code`、`permission_type`、`resource_pattern`、`status` | `uk_permission_code(permission_code)`、`idx_permission_type(permission_type)` |
| `auth_role_permission` | 角色权限 | `tenant_id`、`role_id`、`permission_id` | `uk_role_permission(tenant_id, role_id, permission_id)` |
| `auth_data_scope` | 数据范围 | `tenant_id`、`subject_type`、`subject_id`、`scope_type`、`scope_value_json` | `idx_data_scope_subject(tenant_id, subject_type, subject_id)` |
| `auth_permission_snapshot` | 权限快照 | `tenant_id`、`user_id`、`permission_revision`、`snapshot_json`、`expire_time` | `uk_snapshot_revision(tenant_id, user_id, permission_revision)` |
| `auth_service_credential` | 服务间凭证 | `service_code`、`credential_key_id`、`secret_cipher`、`allowed_scopes`、`status` | `uk_service_key(service_code, credential_key_id)` |
| `auth_service_call_log` | 服务间鉴权日志 | `caller_service`、`target_service`、`result_status`、`trace_id` | `idx_service_call_time(caller_service, create_time)`、`idx_service_trace(trace_id)` |
| `auth_outbox_event` | auth 事件 | 见通用 Outbox | `uk_event_id(event_id)` |
| `auth_consume_log` | auth 消费记录 | 见通用 consume log | `uk_event_consumer(event_id, consumer_group)` |

## 6.3 auth-service 缓存 key

| key | 用途 | TTL / 失效 |
| --- | --- | --- |
| `auth:token:blacklist:{jti}` | 已撤销 Token | 到 Token 过期 |
| `auth:login:fail:{tenantId}:{username}` | 登录失败计数 | 15 分钟 |
| `auth:principal:{tenantId}:{userId}` | 用户主体摘要 | 5-15 分钟；用户状态或角色变更失效 |
| `auth:permission_snapshot:{tenantId}:{userId}:{revision}` | 权限快照 | 5 分钟；权限变更事件失效 |
| `auth:readable_scope:{tenantId}:{userId}:{revision}:{scopeHash}` | 可读范围 | 1-5 分钟；权限或 KB ACL 变更失效 |
| `auth:tenant:config:{tenantId}:{version}` | 租户配置 | 10 分钟；配置变更事件失效 |
| `auth:tenant:availability:{tenantId}` | 租户可用性 | 1 分钟；租户状态变更失效 |
| `auth:quota:usage:{tenantId}:{resourceType}:{periodKey}` | 配额用量 | 周期内有效；用量确认刷新 |
| `auth:service_credential:{serviceCode}:{keyId}` | 服务凭证摘要 | 10 分钟；轮换失效 |

## 6.4 auth-service 事件

| 事件 | 触发 | 消费方 |
| --- | --- | --- |
| `TenantCreated` | 创建租户 | audit、statistics、admin-config |
| `TenantDisabled` | 禁用租户 | kb、document、rag-chat、retrieval、llm-gateway、statistics |
| `TenantConfigChanged` | 租户配置变更 | retrieval、prompt、llm-gateway、embedding、admin-config |
| `TenantQuotaChanged` | 租户配额变更 | document、llm-gateway、statistics |
| `UserDisabled` | 禁用用户 | rag-chat、audit |
| `RolePermissionChanged` | 角色授权变化 | kb、document、retrieval、rag-chat、admin-config |
| `DataScopeChanged` | 数据范围变化 | statistics、audit、admin-config |
| `PermissionSnapshotChanged` | 权限快照版本变化 | rag-chat、retrieval、kb、document |
| `TokenRevoked` | Token 撤销 | gateway、rag-chat |

# 7. 业务服务数据归属

## 7.1 `kb-service` - `rag_kb`

| 表 | 说明 | 主要索引 |
| --- | --- | --- |
| `kb_knowledge_base` | 知识库元数据 | `uk_kb_tenant_name(tenant_id, name)`、`idx_kb_status(tenant_id, status)` |
| `kb_category` | 知识库分类 | `idx_category_parent(tenant_id, parent_id)` |
| `kb_permission` | KB ACL 真相 | `uk_kb_permission(tenant_id, knowledge_base_id, subject_type, subject_id, permission_type)` |
| `kb_retrieval_config` | KB 检索配置 | `uk_kb_retrieval_config(tenant_id, knowledge_base_id, config_version)` |
| `kb_outbox_event` | KB 领域事件 | `uk_event_id(event_id)` |
| `kb_consume_log` | KB 消费记录 | `uk_event_consumer(event_id, consumer_group)` |

缓存 key：

- `kb:detail:{tenantId}:{knowledgeBaseId}`
- `kb:config:{tenantId}:{knowledgeBaseId}:{configVersion}`
- `kb:permission_version:{tenantId}:{knowledgeBaseId}`

事件：

- `KnowledgeBaseCreated`
- `KnowledgeBaseUpdated`
- `KnowledgeBasePermissionChanged`
- `KnowledgeBaseDisabled`

## 7.2 `document-service` - `rag_document`

| 表 | 说明 | 主要索引 |
| --- | --- | --- |
| `doc_document` | 文档元数据和状态真相 | `idx_doc_kb_status(tenant_id, knowledge_base_id, status)`、`uk_doc_checksum_version(tenant_id, checksum, document_version)` |
| `doc_document_version` | 文档版本历史 | `uk_doc_version(tenant_id, document_id, document_version)` |
| `doc_permission` | 文档 ACL | `uk_doc_permission(tenant_id, document_id, subject_type, subject_id, permission_type)` |
| `doc_processing_task` | 文档处理任务入口 | `uk_task_request(tenant_id, request_id)`、`idx_task_status(status, create_time)` |
| `doc_chunk` | Chunk 元数据真相 | `uk_chunk_doc_no(tenant_id, document_id, document_version, chunk_no)`、`idx_chunk_doc(tenant_id, document_id, document_version)` |
| `doc_outbox_event` | 文档领域事件 | `uk_event_id(event_id)` |
| `doc_consume_log` | 文档消费记录 | `uk_event_consumer(event_id, consumer_group)` |

对象存储归属：

- Bucket：`rag-documents`
- Object key：`tenant/{tenantId}/kb/{knowledgeBaseId}/document/{documentId}/v{documentVersion}/{checksum}/{fileName}`
- 原始文件真相归 `document-service`，Worker 只能通过内部 API 和对象 key 读取。

缓存 key：

- `doc:status:{tenantId}:{documentId}`
- `doc:metadata:{tenantId}:{documentId}:{documentVersion}`
- `doc:permission:{tenantId}:{documentId}:{permissionRevision}`

事件：

- `DocumentUploaded`
- `DocumentParseRequested`
- `DocumentDeleted`
- `DocumentReindexRequested`
- `DocumentPermissionChanged`
- `DocumentStatusChanged`

## 7.3 `document-worker` - `rag_document_worker`

| 表 | 说明 | 主要索引 |
| --- | --- | --- |
| `worker_task` | Worker 本地任务视图 | `uk_worker_task(tenant_id, task_id)`、`idx_worker_status(status, next_retry_time)` |
| `worker_task_attempt` | 任务尝试记录 | `idx_attempt_task(tenant_id, task_id, attempt_no)` |
| `worker_parse_snapshot` | 解析过程快照，可选 | `idx_parse_doc(tenant_id, document_id, document_version)` |
| `worker_consume_log` | MQ 消费幂等 | `uk_event_consumer(event_id, consumer_group)` |
| `worker_outbox_event` | Worker 结果事件 | `uk_event_id(event_id)` |

缓存 key：

- `worker:task:lock:{taskId}`
- `worker:progress:{tenantId}:{taskId}`

事件：

- 消费 `DocumentParseRequested`
- 发布 `DocumentParsed`
- 发布 `ChunksReady`
- 发布 `DocumentIndexSucceeded`
- 发布 `DocumentIndexFailed`

## 7.4 `embedding-service` - `rag_embedding`

| 表 | 说明 | 主要索引 |
| --- | --- | --- |
| `embedding_request_batch` | Embedding 批次 | `uk_embedding_request(request_id)`、`idx_embedding_status(status, create_time)` |
| `embedding_request_item` | 批次明细 | `idx_embedding_batch(batch_id)`、`uk_embedding_text_hash(batch_id, text_hash)` |
| `embedding_model_usage` | Embedding 调用摘要 | `idx_embedding_usage(tenant_id, model_code, create_time)` |
| `embedding_outbox_event` | Embedding 事件 | `uk_event_id(event_id)` |
| `embedding_consume_log` | 消费记录 | `uk_event_consumer(event_id, consumer_group)` |

缓存 key：

- `embedding:dedupe:{modelCode}:{textHash}`
- `embedding:rate_limit:{tenantId}:{modelCode}`

说明：

- 向量真相不保存在 MySQL；MySQL 只保存请求、用量和幂等摘要。
- 实际向量写入 Milvus / Qdrant 由 `retrieval-service` 统一持有索引归属。

## 7.5 `retrieval-service` - `rag_retrieval`

| 表 | 说明 | 主要索引 |
| --- | --- | --- |
| `retrieval_index_projection` | Chunk 索引投影 | `uk_chunk_projection(tenant_id, chunk_id, document_version)`、`idx_projection_doc(tenant_id, document_id, document_version)` |
| `retrieval_index_task` | 索引写入 / 删除任务 | `uk_index_task(request_id)`、`idx_index_task_status(status, create_time)` |
| `retrieval_query_log` | 检索日志摘要 | `idx_query_trace(trace_id)`、`idx_query_user_time(tenant_id, user_id, create_time)` |
| `retrieval_rerank_log` | Rerank 摘要 | `idx_rerank_trace(trace_id)` |
| `retrieval_outbox_event` | 检索事件 | `uk_event_id(event_id)` |
| `retrieval_consume_log` | 消费记录 | `uk_event_consumer(event_id, consumer_group)` |

索引归属：

- OpenSearch / Elasticsearch index：`rag_chunk_{tenantShard}` 或按大租户独立 index。
- Milvus collection / Qdrant collection：`rag_chunk_vector_{tenantShard}` 或按大租户独立 collection。
- Vector ID / Search Doc ID：`tenantId_documentId_documentVersion_chunkNo`。

缓存 key：

- `retrieval:query:{tenantId}:{userId}:{permissionRevision}:{queryHash}:{configHash}`
- `retrieval:index_status:{tenantId}:{documentId}:{documentVersion}`
- `retrieval:rerank:{tenantId}:{queryHash}:{candidateHash}`

事件：

- `ChunkIndexUpserted`
- `ChunkIndexDeleted`
- `RetrievalFailed`
- `RetrievalCompleted`

## 7.6 `rag-chat-service` - `rag_chat`

| 表 | 说明 | 主要索引 |
| --- | --- | --- |
| `chat_session` | 会话 | `idx_session_user_time(tenant_id, user_id, last_message_time)` |
| `chat_message` | 问答消息 | `idx_message_session(tenant_id, session_id, create_time)`、`idx_message_trace(trace_id)` |
| `chat_citation` | 引用快照 | `idx_citation_message(tenant_id, message_id)`、`idx_citation_doc(tenant_id, document_id)` |
| `chat_request_state` | 问答请求状态和幂等 | `uk_chat_request(tenant_id, request_id)` |
| `chat_outbox_event` | Chat 事件 | `uk_event_id(event_id)` |
| `chat_consume_log` | 消费记录 | `uk_event_consumer(event_id, consumer_group)` |

缓存 key：

- `chat:session_summary:{tenantId}:{sessionId}`
- `chat:stream_state:{tenantId}:{messageId}`
- `chat:hot_answer:{tenantId}:{permissionRevision}:{questionHash}:{kbHash}`

事件：

- `ChatStarted`
- `ChatCompleted`
- `ChatFailed`
- `ChatCitationCreated`

## 7.7 `llm-gateway-service` - `rag_llm_gateway`

| 表 | 说明 | 主要索引 |
| --- | --- | --- |
| `llm_provider_config` | 供应商配置 | `uk_provider_code(provider_code)` |
| `llm_model_config` | 模型配置 | `uk_model_tenant_code(tenant_id, model_code)`、`idx_model_type_status(tenant_id, model_type, status)` |
| `llm_api_key` | API Key 密文 | `uk_api_key_scope(tenant_id, provider_code, key_name)` |
| `llm_route_policy` | 模型路由策略 | `idx_route_policy(tenant_id, operation_type, status)` |
| `llm_call_log` | 模型调用日志摘要 | `idx_llm_trace(trace_id)`、`idx_llm_model_time(tenant_id, model_code, create_time)` |
| `llm_token_usage` | 原始 Token 用量和成本真相 | `idx_usage_tenant_time(tenant_id, create_time)`、`idx_usage_message(tenant_id, message_id)` |
| `llm_rate_limit_state` | 限流状态，可选持久化 | `idx_rate_limit(tenant_id, model_code, period_key)` |
| `llm_outbox_event` | LLM 事件 | `uk_event_id(event_id)` |
| `llm_consume_log` | 消费记录 | `uk_event_consumer(event_id, consumer_group)` |

缓存 key：

- `llm:model_config:{tenantId}:{modelCode}:{version}`
- `llm:route_policy:{tenantId}:{operationType}`
- `llm:rate_limit:{tenantId}:{modelCode}:{periodKey}`
- `llm:circuit:{provider}:{modelCode}`

事件：

- `LlmUsageRecorded`
- `LlmCallFailed`
- `ModelConfigChanged`
- `ApiKeyRotated`

## 7.8 `prompt-service` - `rag_prompt`

| 表 | 说明 | 主要索引 |
| --- | --- | --- |
| `prompt_template` | Prompt 模板 | `uk_prompt_version(tenant_id, prompt_code, version_no)` |
| `prompt_template_variable` | 变量契约 | `idx_prompt_variable(prompt_template_id)` |
| `prompt_release_record` | 发布记录 | `idx_prompt_release(tenant_id, prompt_code, create_time)` |
| `prompt_gray_policy` | 灰度策略 | `idx_prompt_gray(tenant_id, prompt_code, status)` |
| `prompt_render_log` | 渲染摘要 | `idx_prompt_render_trace(trace_id)` |
| `prompt_outbox_event` | Prompt 事件 | `uk_event_id(event_id)` |
| `prompt_consume_log` | 消费记录 | `uk_event_consumer(event_id, consumer_group)` |

缓存 key：

- `prompt:active:{tenantId}:{promptCode}:{scenario}`
- `prompt:template:{tenantId}:{promptCode}:{version}`
- `prompt:render:{tenantId}:{promptCode}:{version}:{variablesHash}`

事件：

- `PromptTemplatePublished`
- `PromptTemplateRolledBack`
- `PromptGrayPolicyChanged`

## 7.9 `feedback-service` - `rag_feedback`

| 表 | 说明 | 主要索引 |
| --- | --- | --- |
| `feedback_record` | 用户反馈 | `idx_feedback_message(tenant_id, message_id)`、`idx_feedback_status_time(tenant_id, status, create_time)` |
| `feedback_tag` | 反馈标签 | `uk_feedback_tag(tenant_id, tag_code)` |
| `feedback_handle_record` | 处理记录 | `idx_feedback_handle(feedback_id, create_time)` |
| `feedback_outbox_event` | 反馈事件 | `uk_event_id(event_id)` |
| `feedback_consume_log` | 消费记录 | `uk_event_consumer(event_id, consumer_group)` |

缓存 key：

- `feedback:rate_limit:{tenantId}:{userId}`
- `feedback:summary:{tenantId}:{messageId}`

事件：

- `FeedbackCreated`
- `FeedbackResolved`

## 7.10 `evaluation-service` - `rag_evaluation`

| 表 | 说明 | 主要索引 |
| --- | --- | --- |
| `eval_dataset` | 评测集 | `uk_eval_dataset(tenant_id, name)` |
| `eval_case` | 评测用例 | `uk_eval_case_hash(tenant_id, dataset_id, case_hash)` |
| `eval_run` | 评测运行 | `idx_eval_run_status(tenant_id, status, create_time)` |
| `eval_run_case_result` | 单用例结果 | `idx_eval_case_result(run_id, case_id)` |
| `eval_score_summary` | 评分汇总 | `idx_eval_summary(tenant_id, run_id)` |
| `eval_outbox_event` | 评测事件 | `uk_event_id(event_id)` |
| `eval_consume_log` | 消费记录 | `uk_event_consumer(event_id, consumer_group)` |

缓存 key：

- `evaluation:run:lock:{runId}`
- `evaluation:dataset:{tenantId}:{datasetId}`

事件：

- `EvaluationRunStarted`
- `EvaluationRunCompleted`
- `EvaluationCaseFailed`

## 7.11 `audit-service` - `rag_audit`

| 表 | 说明 | 主要索引 |
| --- | --- | --- |
| `audit_event` | 审计事件真相 | `idx_audit_tenant_time(tenant_id, create_time)`、`idx_audit_trace(trace_id)`、`idx_audit_resource(tenant_id, resource_type, resource_id)` |
| `audit_risk_event` | 风险审计事件 | `idx_risk_status(tenant_id, risk_level, status, create_time)` |
| `audit_archive_batch` | 审计归档批次 | `uk_archive_window(archive_type, window_start, window_end)` |
| `audit_consume_log` | 审计消费记录 | `uk_event_consumer(event_id, consumer_group)` |

缓存 key：

- `audit:query:rate_limit:{tenantId}:{userId}`

说明：

- 审计服务通常只消费事件，不要求其他服务同步等待成功；高风险配置变更可要求同步写入或确认事件接收。

## 7.12 `statistics-service` - `rag_statistics`

| 表 | 说明 | 主要索引 |
| --- | --- | --- |
| `stat_event` | 统计原始事件投影 | `uk_stat_event(event_id)`、`idx_stat_event_type_time(event_type, create_time)` |
| `stat_token_daily` | Token 日聚合 | `uk_token_daily(tenant_id, stat_date, model_code, user_id)` |
| `stat_cost_daily` | 成本日聚合 | `uk_cost_daily(tenant_id, stat_date, model_code)` |
| `stat_document_daily` | 文档日聚合 | `uk_doc_daily(tenant_id, stat_date, knowledge_base_id)` |
| `stat_chat_daily` | 问答日聚合 | `uk_chat_daily(tenant_id, stat_date, user_id)` |
| `stat_feedback_daily` | 反馈日聚合 | `uk_feedback_daily(tenant_id, stat_date)` |
| `stat_aggregation_task` | 聚合任务 | `uk_aggregation_window(metric_type, window_start, window_end)` |
| `stat_consume_log` | 消费记录 | `uk_event_consumer(event_id, consumer_group)` |

缓存 key：

- `statistics:report:{tenantId}:{reportType}:{queryHash}`
- `statistics:aggregation:lock:{metricType}:{window}`

说明：

- `statistics-service` 不直接计算供应商原始 Token；原始用量以 `llm-gateway-service.llm_token_usage` 和 `LlmUsageRecorded` 事件为准。
- 报表存在最终一致性延迟，必须暴露 `statTime`。

## 7.13 `admin-config-service` - `rag_admin_config`

| 表 | 说明 | 主要索引 |
| --- | --- | --- |
| `admin_operation` | 后台操作编排记录 | `idx_admin_operation(tenant_id, operator_user_id, create_time)` |
| `admin_config_draft` | 配置草稿 | `uk_config_draft(tenant_id, config_type, draft_key)` |
| `admin_publish_task` | 配置发布任务 | `uk_publish_request(request_id)`、`idx_publish_status(status, create_time)` |
| `admin_approval_record` | 审批记录，可选 | `idx_approval_task(publish_task_id, create_time)` |
| `admin_outbox_event` | 管理事件 | `uk_event_id(event_id)` |
| `admin_consume_log` | 消费记录 | `uk_event_consumer(event_id, consumer_group)` |

缓存 key：

- `admin:overview:{tenantId}:{userId}:{permissionRevision}`
- `admin:publish:lock:{requestId}`

说明：

- `admin-config-service` 不拥有模型配置、Prompt 模板、知识库、租户配额等业务真相。
- 发布配置必须调用目标服务 API，失败时记录编排状态并回滚草稿。

# 8. 跨服务一致性模型

## 8.1 文档处理一致性

状态真相：

- 文档状态真相在 `document-service.doc_document`。
- Worker 处理状态真相在 `document-worker.worker_task`。
- Chunk 元数据真相在 `document-service.doc_chunk`。
- 向量和关键词索引归 `retrieval-service` 管理。

一致性主键：

```text
tenantId + documentId + documentVersion + chunkNo
```

要求：

- 上传先写 MinIO 和 `doc_document`，再写 `doc_outbox_event`。
- `DocumentParseRequested` 至少一次投递，`worker_consume_log` 去重。
- Worker 只能通过 `document-service` 内部 API 回写状态。
- 索引写入由 `retrieval-service` 负责，写入成功后才允许文档进入 `INDEXED`。
- 删除文档先将 `doc_document.status = DELETED` 或不可见，再异步删除向量和搜索索引。

## 8.2 权限缓存一致性

- `auth-service` 生成 `permission_revision`。
- 权限、角色、部门、数据范围、KB ACL、文档 ACL 变化必须触发权限版本或资源权限版本变化。
- RAG Chat、Retrieval、KB、Document 的缓存 key 必须包含 `permissionRevision` 或资源 ACL version。
- 权限服务不可用且无新鲜快照时，默认拒绝，不使用过期权限扩大访问。

## 8.3 Token 和成本一致性

- 原始 Token 和成本真相在 `llm-gateway-service.llm_token_usage`。
- `statistics-service` 通过 `LlmUsageRecorded` 事件做聚合。
- 成本字段使用 `decimal(18,6)`。
- 供应商未返回 Token 时可以估算，但必须记录 `estimated = true`。

## 8.4 审计一致性

- 高风险业务操作的 Outbox 事件必须与业务表同事务写入。
- `audit-service` 按 `eventId` 幂等消费。
- 审计写失败不能隐藏；低风险操作可异步补偿，高风险配置变更可要求确认审计接收后返回成功。

# 9. 索引归属与检索数据模型

## 9.1 OpenSearch / Elasticsearch

归属：`retrieval-service`

推荐索引：

| 索引 | 说明 | 核心字段 |
| --- | --- | --- |
| `rag_chunk_{tenantShard}` | Chunk 关键词检索索引 | `tenant_id`、`knowledge_base_id`、`document_id`、`document_version`、`chunk_id`、`status`、`permission_revision`、`title_path`、`content`、`page_no` |
| `rag_chunk_large_tenant_{tenantId}` | 大租户独立索引，可选 | 同上 |

要求：

- filter 字段必须使用 keyword / numeric 类型。
- 禁止索引完整敏感 ACL 明细过大；生产可使用 `permission_revision` + DB 二次校验。
- Search Doc ID 与 Vector ID 统一可反查 Chunk。

## 9.2 Milvus / Qdrant

归属：`retrieval-service`

推荐 collection：

| collection | 说明 | 核心 payload |
| --- | --- | --- |
| `rag_chunk_vector_{tenantShard}` | 通用向量 collection | `tenant_id`、`knowledge_base_id`、`document_id`、`document_version`、`chunk_id`、`status`、`permission_revision`、`page_no`、`title_path` |
| `rag_chunk_vector_tenant_{tenantId}` | 大租户独立 collection，可选 | 同上 |

要求：

- Vector ID：`tenantId_documentId_documentVersion_chunkNo`。
- payload filter 至少包含 `tenant_id`、`knowledge_base_id`、`status`。
- 权限复杂时不把超大 `allow_user_ids` 写入 payload，改用粗过滤 + 候选二次校验。

# 10. 缓存归属总览

| 服务 | 关键缓存 | 失效来源 |
| --- | --- | --- |
| `auth-service` | 权限快照、租户配置、租户配额、服务凭证 | 权限变更、租户变更、凭证轮换 |
| `kb-service` | KB 详情、KB 检索配置、KB 权限版本 | KB 更新、ACL 变更 |
| `document-service` | 文档状态、文档元数据、文档权限 | 状态变更、ACL 变更、删除 |
| `document-worker` | 任务锁、进度 | 任务完成、失败、超时 |
| `embedding-service` | 文本 hash 去重、模型限流计数 | TTL、模型配置变更 |
| `retrieval-service` | 检索结果、索引状态、Rerank 结果 | 权限版本、索引更新、配置变更 |
| `rag-chat-service` | 会话摘要、流式状态、热点问答 | 会话更新、权限版本、Prompt/检索配置变化 |
| `llm-gateway-service` | 模型配置、路由策略、限流、熔断状态 | 模型配置变更、熔断恢复 |
| `prompt-service` | 激活模板、渲染结果 | Prompt 发布、回滚、灰度变更 |
| `statistics-service` | 报表查询结果、聚合锁 | 聚合完成、查询 TTL |
| `admin-config-service` | 管理首页聚合视图、发布锁 | 目标服务事件、发布完成 |

# 11. 数据风险与回滚

| 风险 | 影响 | 缓解与回滚 |
| --- | --- | --- |
| auth-service 第一阶段职责过重 | 认证、租户、IAM 都在同一服务，复杂度上升 | 通过包边界和 schema 内逻辑分区保持可拆性；后续按接口拆 `iam-service` / `tenant-service` |
| 权限快照过期 | 可能越权或误拒绝 | 过期无法刷新时默认拒绝；缓存 TTL 兜底 |
| 跨服务事件丢失 | 审计、统计、索引不一致 | Outbox + consume log + 定时补偿 |
| Chunk、向量、搜索索引不一致 | 检索错误或引用错误 | 一致性巡检；按 documentVersion 重建索引 |
| 统计延迟 | 报表非实时 | 报表返回 `statTime`，原始用量可从 llm-gateway 查询 |
| 服务拆分后跨服务延迟增加 | 问答 P95 升高 | 权限快照缓存、并行检索、超时预算和熔断降级 |
| 后续拆出 tenant / IAM | API 和数据迁移成本 | 先通过 `auth-service` 内部 API 稳定契约，拆出时保留兼容代理层 |

# 12. 数据设计一致性检查

| 检查项 | 结论 |
| --- | --- |
| `auth-service` schema 负责 auth、tenant、user、dept、role、permission、data_scope、permission_snapshot、tenant_config、tenant_quota | 通过 |
| 每个服务拥有独立 schema | 通过 |
| 每个服务明确 Outbox 与 Inbox / consume log | 通过 |
| 每个服务明确缓存 key | 通过 |
| OpenSearch / Elasticsearch 与 Milvus / Qdrant 索引归 `retrieval-service` 管理 | 通过 |
| 禁止跨服务直接查库和跨 schema join | 通过 |
| 保持 MySQL 8.0、Redis、MinIO、RabbitMQ/Kafka、OpenSearch/Elasticsearch、Milvus/Qdrant 主方案 | 通过 |
| 不采用 PostgreSQL + pgvector、Sa-Token、LangChain4j 主方案 | 通过 |

# 13. 待后续细化事项

- 进入实现前，需要按 `MICROSERVICE_TODO.md` 为每个服务拆分 DDL 文件、Entity、Mapper、DTO 和契约测试。
- `auth-service` 后续若拆出 `tenant-service` 或 `iam-service`，需要设计在线迁移、双写或事件回放策略。
- 大租户是否独立 OpenSearch index / Milvus collection 需要根据真实数据量和查询延迟压测决定。
