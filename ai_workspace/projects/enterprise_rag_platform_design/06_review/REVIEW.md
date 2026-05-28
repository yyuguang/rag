# 文档信息

- 文档名称：REVIEW.md
- 当前状态：已完成
- 最近更新阶段：code-reviewer
- 最近更新原因：审查本轮新增微服务 API、数据模型、任务拆分文档与 C 方案和 auth-service 合并 tenant / IAM 口径的一致性

# 审查结论

通过。本轮新增的 `MICROSERVICE_API_SPEC.md`、`MICROSERVICE_DATA_MODEL.md` 和 `MICROSERVICE_TODO.md` 能在既有模块化单体设计和 `MICROSERVICE_DESIGN.md` 基础上，落实用户明确拍板的 C 方案：绞杀者式渐进拆分。

关键口径已修正：第一阶段不单独拆 `tenant-service`，也不单独拆 `iam-service`；租户、用户、部门、角色、权限、权限快照、租户配置、租户配额和服务间鉴权先合并进 `auth-service`。

当前交付结论仅适用于设计和计划文档，不代表软件实现已完成或可发布。

# 优点

- 服务清单已调整为 14 个服务，避免第一阶段拆分过细。
- `auth-service` 职责被明确扩大，覆盖认证、租户、IAM、权限快照、租户配置、租户配额和服务间鉴权。
- API 文档不仅列服务，还落到每个接口的路径、方法、调用方、鉴权、DTO、错误码、幂等、超时、重试、熔断和审计要求。
- 数据模型文档明确独立 schema、表归属、Outbox、Inbox / consume log、缓存 key 和索引归属。
- 微服务 TODO 按用户要求的 7 个阶段拆分为 23 个原子 TASK，且每个 TASK 包含 RED/GREEN、测试文件、测试命令和 `.ai_rules` 检查点。
- 关键链路口径清晰：RAG 问答、文档上传、管理后台都必须先经过 `auth-service` 权限链路。
- 保留后续演进说明：IAM / tenant 复杂化、独立团队维护或多系统复用后，可再从 `auth-service` 拆出独立服务。

# 问题列表

无阻断问题。

## RV-MS-3

- 严重级别：低
- 位置：`02_design/MICROSERVICE_DESIGN.md`
- 问题描述：该历史基础文档正文中仍保留上一轮 16 服务拆分的详细内容，包含独立 `tenant-service` 和 `iam-service` 章节。
- 风险说明：如果后续 Agent 只读旧正文、不读本轮新增文档，可能误以为第一阶段仍要拆独立 tenant / IAM 服务。
- 修改建议：本轮已在 `MICROSERVICE_DESIGN.md` 顶部新增“本轮修订覆盖说明”，并明确后续以 `MICROSERVICE_API_SPEC.md`、`MICROSERVICE_DATA_MODEL.md`、`MICROSERVICE_TODO.md` 为准。下一轮若继续做架构清理，可进一步重写旧文档正文，消除历史口径。

## RV-MS-4

- 严重级别：低
- 位置：`03_plan/MICROSERVICE_TODO.md`
- 问题描述：微服务任务已经拆到 23 个 TASK，但仍未进入测试设计和测试代码阶段。
- 风险说明：如果下一轮直接创建服务工程，会违反 TDD Gate。
- 修改建议：下一轮只选择 `MS-TASK-001`，先进入 `test-designer / test-writer`，编写失败测试后再进入实现。

# 与需求一致性检查

通过。用户要求的核心内容均已覆盖：

- 采用 C 方案：绞杀者式渐进拆分。
- 第一阶段不单独拆 `tenant-service` / `iam-service`。
- `auth-service` 合并认证、租户、用户、部门、角色、权限、权限快照、租户配置、租户配额、服务间鉴权。
- 生成 `MICROSERVICE_API_SPEC.md`。
- 生成 `MICROSERVICE_DATA_MODEL.md`。
- 生成 `MICROSERVICE_TODO.md`。
- RAG 问答权限链路改为 `rag-chat-service -> auth-service(permission snapshot / readable scope) -> retrieval-service -> vector/search -> rerank -> prompt-service -> llm-gateway-service`。
- 文档上传权限链路改为 `document-service -> auth-service(check KB/document permission) -> MQ -> document-worker -> parser -> chunk -> embedding -> vector/search`。
- 管理后台权限链路改为 `admin-config-service -> auth-service(api permission / data scope) -> target service`。
- 保留后续从 `auth-service` 拆出 `iam-service` 或 `tenant-service` 的演进条件。

# 与设计研究一致性检查

通过。本轮没有推翻既有 `DESIGN_RESEARCH.md` 的“模块化单体优先、生产按能力拆分”结论，而是在微服务方向进一步选择 C 方案作为演进路径。

# 与 Prompt 设计一致性检查

通过。新 API、数据模型和 TODO 均要求 Prompt 由 `prompt-service` 管理，`rag-chat-service` 不得硬编码 Prompt，不得允许用户输入覆盖系统规则和业务规则。

# API 与数据一致性检查

通过。

- API 文档中 `auth-service` 内部 API 提供权限快照、可读范围、API 权限、数据范围、KB 权限、文档权限、租户配置和租户配额能力。
- 数据模型文档中 `rag_auth` schema 覆盖 auth、tenant、user、dept、role、permission、data_scope、permission_snapshot、tenant_config、tenant_quota。
- API 和数据模型均明确禁止服务间跨库直接查询和跨 schema join。
- API 与数据模型均保持 MySQL 8.0、Redis、MinIO、RabbitMQ/Kafka、OpenSearch/Elasticsearch、Milvus/Qdrant 主方案。

# TDD 与测试覆盖检查

通过但有范围限制。本轮为纯文档任务，`TEST_REPORT.md` 已记录测试豁免原因和静态验证结果。后续进入微服务实施时，必须先针对 `MS-TASK-001` 写失败测试，不允许跳过 TDD Gate。

# 架构边界检查

通过。

- `auth-service` 是第一阶段权限、租户和 IAM 的统一服务。
- `rag-chat-service` 只负责编排、会话、SSE 和引用落库。
- `retrieval-service` 负责授权检索、向量/关键词召回、Rerank 和候选权限二次校验。
- `llm-gateway-service` 是唯一模型供应商调用出口。
- `document-service` 与 `document-worker` 职责分离。
- `admin-config-service` 只做管理聚合和配置编排，不直接改目标服务数据库。
- 服务间禁止跨库直接查询。

# `.ai_rules` 执行审查

通过。规则不是仅被引用，而是落到了以下位置：

- `MICROSERVICE_API_SPEC.md` 记录 `/api/v1/`、`/internal/v1/`、DTO、错误码、鉴权、幂等、超时、重试、熔断、审计要求。
- `MICROSERVICE_DATA_MODEL.md` 记录 MySQL 8.0、独立 schema、表归属、Outbox、consume log、缓存 key、索引归属和禁止跨库查询。
- `MICROSERVICE_TODO.md` 的每个 TASK 记录适用 `.ai_rules` 文件、规则检查点和验收方式。
- `TEST_REPORT.md` 记录 `.ai_rules` 合规验证结果。
- `REVIEW.md` 本文件记录规则执行审查结论。

# 发布建议

微服务方向的 API、数据模型和任务拆分文档可以交付。软件不可发布，因为业务代码、服务工程、数据库脚本、服务契约测试、集成测试、部署和性能验证尚未实现。
