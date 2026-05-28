# 文档信息

- 文档名称：CLARIFICATION.md
- 当前状态：已完成
- 最近更新阶段：requirement-clarifier
- 最近更新原因：根据用户确认更新技术栈口径

# 已确认内容

1. 设计对象是企业级 RAG 知识库问答平台，不是简单 Demo。
2. 目标读者是 5 年经验 Java 后端开发工程师。
3. 技术栈已确认：Java 17+、Spring Boot 3.x、Spring Security、Spring AI、MyBatis-Plus、MySQL 8.0、Redis、OpenSearch / Elasticsearch、Milvus / Qdrant、MinIO、RabbitMQ / Kafka、Docker、Prometheus、Grafana。
4. 文档解析链路引入 Spring AI，使用 Spring AI ETL / DocumentReader / DocumentTransformer 作为文档进入 RAG 的统一抽象；复杂格式仍可由 Tika、PDFBox、POI、EasyExcel 等底层解析器补充。
5. 大模型接入必须统一封装，支持 OpenAI、Azure OpenAI、DeepSeek、通义千问、智谱、本地大模型等多个供应商。
6. 设计必须覆盖业务、架构、数据、API、Prompt、安全、性能、可观测、部署、代码结构、关键类、MVP、演进、简历和面试。

# 保守设计假设

| 编号 | 假设 | 影响范围 | 后续确认方式 |
| --- | --- | --- | --- |
| A-1 | MVP 先做模块化单体，生产再拆服务 | 架构、部署、代码结构 | 根据团队规模和 SLA 决定是否提前拆服务 |
| A-2 | 主数据库固定为 MySQL 8.0，向量检索独立使用 Milvus / Qdrant | 数据库、向量检索 | 根据文档量、向量规模和召回延迟验证 |
| A-3 | 企业文档默认有租户、知识库、文档三级权限 | 权限、检索、数据模型 | 对接实际组织架构和 ACL 模型 |
| A-4 | RAG 答案必须给引用，不能无依据编造 | Prompt、问答流程、评估 | 用 Golden Case 和人工验收确认 |
| A-5 | 模型供应商需可插拔 | LLM Gateway、配置、成本 | 根据企业采购模型和内网环境确认 |

# 待确认问题

## CQ-1 文档规模

- 问题：预计文档数量、总容量、最大单文档大小、日增量是多少？
- 影响：向量库选择、索引分片、异步队列、文件存储和批处理策略。
- 当前处理：MySQL 8.0 只承载业务关系数据、权限、审计和元数据；向量数据由 Milvus / Qdrant 承载，避免在 MySQL 中硬做向量检索。

## CQ-2 权限来源

- 问题：企业是否已有统一身份系统、部门树、岗位、数据权限或文档 ACL？
- 影响：租户模型、权限表、检索过滤和审计。
- 当前处理：设计为内置 RBAC + 知识库 ACL + 文档 ACL，可对接 SSO。

## CQ-3 模型部署方式

- 问题：使用 OpenAI / Azure OpenAI / DeepSeek / 通义千问 / 智谱 / 本地模型中的哪些？
- 影响：网关适配、API Key、成本、SLA 和私有化部署。
- 当前处理：统一 LLM Gateway，基于 Spring AI 封装 Chat、Embedding、Rerank 调用入口，并通过供应商适配器支持多个大模型。

## CQ-4 合规要求

- 问题：是否涉及金融、医疗、政企、涉密或国产化要求？
- 影响：日志脱敏、模型调用边界、数据出境、审计留存。
- 当前处理：默认敏感数据脱敏，支持本地模型和私有化部署。

# 规则执行要求

本任务需遵守：

- API：使用 `/api/v1/`，RESTful，统一返回结构和错误码。
- 数据库：表和字段使用 snake_case，业务表包含 `tenant_id`、软删除、审计字段、状态和必要索引。
- 分层：未来代码按 controller、service、dao、dto 分层，Controller 不直接返回 Entity。
- Service：interface + impl，写操作事务边界在 Service impl。
- 日志：记录 traceId、tenantId、userId、bizId、operation、costMs，敏感信息脱敏。
- 注释：public 类和方法、DTO 字段、复杂业务规则需要中文注释或 API 注解。
