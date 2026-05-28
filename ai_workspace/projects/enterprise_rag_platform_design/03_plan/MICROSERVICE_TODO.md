# 文档信息

- 文档名称：MICROSERVICE_TODO.md
- 当前状态：已合并 / 已废弃为主入口
- 最近更新阶段：task-planner 合并计划入口
- 最近更新原因：微服务演进 TASK 已合并进入 `03_plan/TODO.md`，本文仅保留历史追溯，不再作为主计划入口

# 已合并 / 废弃说明

`MICROSERVICE_TODO.md` 已完成迁移，不再作为后续任务计划主入口。后续任务计划只读取：

- `03_plan/TODO.md`

本文正文保留上一轮微服务任务拆分的历史材料。当前有效微服务演进 TASK 已合并到 `TODO.md` 的“阶段 8：微服务演进 TASK”，并继续保留 7 个阶段：

1. 冻结模块边界和契约。
2. 先拆 `auth-service` 和 `audit-service`，其中 `auth-service` 合并 tenant / IAM 能力。
3. 再拆 `kb-service`、`document-service`。
4. 再拆 `document-worker`、`embedding-service`。
5. 再拆 `llm-gateway-service`、`prompt-service`。
6. 再拆 `retrieval-service`、`rag-chat-service`。
7. 最后拆 `feedback-service`、`evaluation-service`、`statistics-service`、`admin-config-service`。

后续实施不得只读取本文，也不得跳过 `TODO.md`、TDD Gate 或用户本轮“禁止直接编码、禁止创建 Java 服务工程”的限制。

# 0. Workflow Orchestrator 任务识别

- 任务归属：`enterprise_rag_platform_design`
- 任务类型：L4 高风险架构设计 / 微服务演进规划 / 任务拆解
- 当前主流程：`workflow-orchestrator -> design-researcher -> system-architect -> api-designer -> data-designer -> task-planner`
- 本轮限制：不写业务代码，不创建 Java 服务工程，不进入 `spec-driven-coder`
- 推荐方案：C 方案，绞杀者式渐进拆分
- 本轮关键调整：第一阶段不单独拆 `tenant-service`，也不单独拆 `iam-service`；租户、用户、部门、角色、权限、权限快照、租户配置、租户配额、服务间鉴权先合并进 `auth-service`

# 1. 总体执行原则

- 微服务实施必须从模块化单体边界冻结开始，不允许直接创建 14 个服务工程。
- 一次只执行一个 TASK。
- 每个 TASK 进入实现前必须先进入 `test-designer / test-writer`，补契约测试、架构约束测试或明确测试豁免。
- 所有跨服务 API 必须先有 OpenAPI / 契约测试，再有实现。
- 所有服务必须独立 schema，第一阶段可共用同一个 MySQL 8.0 实例，但必须独立账号。
- 禁止跨服务直接查库，禁止跨 schema join。
- `rag-chat-service` 不得绕过 `auth-service` 权限链路。
- 业务服务不得直接调用模型供应商 API，必须统一通过 `llm-gateway-service`。
- Prompt 不得硬编码在 `rag-chat-service`，必须通过 `prompt-service`。

# 2. 阶段顺序

本轮微服务 C 方案必须按以下顺序推进：

1. 冻结模块边界和契约。
2. 先拆 `auth-service` 和 `audit-service`，其中 `auth-service` 合并 tenant / IAM 能力。
3. 再拆 `kb-service`、`document-service`。
4. 再拆 `document-worker`、`embedding-service`。
5. 再拆 `llm-gateway-service`、`prompt-service`。
6. 再拆 `retrieval-service`、`rag-chat-service`。
7. 最后拆 `feedback-service`、`evaluation-service`、`statistics-service`、`admin-config-service`。

# 3. 规则简称

- `README`：`.ai_rules/README.md`
- `API`：`.ai_rules/API_STYLE.md`
- `DB`：`.ai_rules/DB_STYLE.md`
- `CODING`：`.ai_rules/CODING_STYLE.md`
- `STRUCTURE`：`.ai_rules/PROJECT_STRUCTURE.md`
- `SERVICE`：`.ai_rules/SERVICE_STYLE.md`
- `COMMENT`：`.ai_rules/COMMENT_STYLE.md`
- `LOGGING`：`.ai_rules/LOGGING_STYLE.md`

# 4. 任务列表

## 阶段 1：冻结模块边界和契约

### MS-TASK-001 冻结微服务边界与契约目录

- 需求来源：本轮用户要求；`CURRENT_FOCUS.md` “继续微服务方向”；`REVIEW.md` RV-MS-1
- 设计来源：`MICROSERVICE_DESIGN.md` 第 1、2、3、14 节；`MICROSERVICE_API_SPEC.md` 第 2、3 节
- 变更位置：`contracts/openapi/`、`contracts/events/`、`contracts/errors/`、`docs/microservice-boundaries.md`
- 前置依赖：无
- 输入：14 个服务清单、外部 `/api/v1/` 和内部 `/internal/v1/` 规范、通用错误码
- 输出：契约目录、服务边界登记表、错误码登记表、事件命名规范
- 实现步骤：创建契约目录结构；为每个服务建立 OpenAPI 占位文件；建立事件 schema 命名规则；建立服务边界登记表；标记 `tenant-service` / `iam-service` 第一阶段不独立拆分
- 测试策略：先写契约目录结构测试和服务清单一致性测试
- 测试文件：`src/test/java/com/lnzz/rag/architecture/MicroserviceContractStructureTest.java`
- 测试命令：`mvn test -Dtest=MicroserviceContractStructureTest`
- 预期 RED：契约目录或 14 个服务 OpenAPI 占位文件不存在，测试失败
- 预期 GREEN：14 个服务契约文件存在，且不包含第一阶段独立 `tenant-service` / `iam-service`
- 验收标准：后续所有服务实现都能以契约目录为唯一入口，不依赖会话记忆
- 风险与回滚：若服务清单变化，先更新契约登记表，不直接改实现
- 适用 `.ai_rules` 文件：README、API、CODING、STRUCTURE、COMMENT
- 规则检查点：API 版本前缀明确；DTO 不暴露 Entity；文档中文说明完整
- 状态：未开始

### MS-TASK-002 冻结跨服务上下文与鉴权头

- 需求来源：本轮用户要求的权限链路；AGENTS 高风险权限门禁
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 3、4、5.2 节；`MICROSERVICE_DATA_MODEL.md` 第 6 节
- 变更位置：`contracts/headers/service-context.yaml`、`common-security-contract/`、`docs/service-auth.md`
- 前置依赖：MS-TASK-001
- 输入：`traceId`、`tenantId`、`userId`、`permissionRevision`、服务身份、用户委托身份
- 输出：统一请求头、内部 JWT claims、服务签名字段、上下文传递规范
- 实现步骤：定义 `X-Trace-Id`、`X-Tenant-Id`、`X-User-Id`、`X-Permission-Revision`、`X-Service-Code`、`X-Service-Signature`；定义服务间鉴权失败错误码；定义日志字段白名单
- 测试策略：先写 header schema 校验测试和敏感头日志禁止测试
- 测试文件：`ServiceContextHeaderContractTest.java`、`SensitiveHeaderLoggingRuleTest.java`
- 测试命令：`mvn test -Dtest=ServiceContextHeaderContractTest,SensitiveHeaderLoggingRuleTest`
- 预期 RED：内部调用上下文缺字段或日志允许打印 Authorization
- 预期 GREEN：上下文字段完整，敏感头不允许进入日志
- 验收标准：所有内部 API 具备服务身份和用户委托身份的契约基础
- 风险与回滚：若后续接入 mTLS，保留内部 JWT 兼容期，不破坏现有 header
- 适用 `.ai_rules` 文件：README、API、CODING、STRUCTURE、SERVICE、LOGGING
- 规则检查点：traceId 必传；Token/API Key 不入日志；服务间鉴权失败默认拒绝
- 状态：未开始

### MS-TASK-003 冻结 Outbox / Inbox / Consume Log 标准

- 需求来源：本轮要求明确事件、任务和测试策略；微服务禁止分布式事务隐式假设
- 设计来源：`MICROSERVICE_DATA_MODEL.md` 第 5、8 节；`MICROSERVICE_DESIGN.md` 数据一致性章节
- 变更位置：`contracts/events/outbox-standard.md`、`contracts/events/common-event-envelope.yaml`、`common-event-contract/`
- 前置依赖：MS-TASK-001
- 输入：事件信封字段、Outbox 通用字段、consume log 通用字段
- 输出：事件信封契约、Outbox 表模板、consume log 表模板、幂等消费规则
- 实现步骤：定义 `eventId`、`eventType`、`tenantId`、`aggregateId`、`traceId`、`payload`；定义重试和死信字段；定义消费者幂等键；定义事件版本兼容策略
- 测试策略：先写事件 schema 校验测试和重复消费幂等测试
- 测试文件：`EventEnvelopeSchemaTest.java`、`ConsumeLogIdempotencyContractTest.java`
- 测试命令：`mvn test -Dtest=EventEnvelopeSchemaTest,ConsumeLogIdempotencyContractTest`
- 预期 RED：事件缺少 `eventId` 或 consume log 唯一约束未定义
- 预期 GREEN：事件契约和幂等消费规则通过校验
- 验收标准：后续所有服务事件发布和消费均复用同一标准
- 风险与回滚：事件字段扩展必须只新增可选字段，避免破坏消费者
- 适用 `.ai_rules` 文件：README、DB、CODING、STRUCTURE、SERVICE、LOGGING
- 规则检查点：表字段 snake_case；事件含 traceId；失败重试可追踪
- 状态：未开始

## 阶段 2：先拆 auth-service 和 audit-service

### MS-TASK-004 设计并实现 auth-service 基础 schema 与认证 API

- 需求来源：本轮要求 auth-service 合并认证、租户、用户、部门、角色、权限、权限快照、租户配置、租户配额、服务间鉴权
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 5 节；`MICROSERVICE_DATA_MODEL.md` 第 6 节
- 变更位置：`services/auth-service/`、`contracts/openapi/auth-service.yaml`、`db/rag_auth/`
- 前置依赖：MS-TASK-001、MS-TASK-002、MS-TASK-003
- 输入：`auth_tenant`、`auth_user`、`auth_user_credential`、`auth_refresh_token` 等表设计；登录/刷新/退出 API 契约
- 输出：auth-service 工程骨架、认证 API、`rag_auth` schema 初版
- 实现步骤：创建 auth-service 工程；创建 `rag_auth` DDL；实现登录、刷新、退出、当前用户信息接口；接入 Spring Security；写登录审计事件 Outbox
- 测试策略：先写登录契约测试、禁用租户/用户测试、Token 不入日志测试
- 测试文件：`AuthLoginContractTest.java`、`AuthTenantUserStatusTest.java`、`AuthSensitiveLoggingTest.java`
- 测试命令：`mvn -pl services/auth-service test -Dtest=AuthLoginContractTest,AuthTenantUserStatusTest,AuthSensitiveLoggingTest`
- 预期 RED：auth-service 不存在或登录 API 404；禁用租户仍可登录
- 预期 GREEN：登录/刷新/退出符合契约，禁用租户和用户被拒绝，密码和 Token 不入日志
- 验收标准：用户认证能力可作为所有外部 API 的安全入口
- 风险与回滚：认证链异常会阻断所有业务；保留健康检查白名单和回滚镜像
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：Controller 只调用 Service；密码不明文；业务表含审计字段；错误信息清晰
- 状态：未开始

### MS-TASK-005 实现 auth-service 权限快照、可读范围和配额内部 API

- 需求来源：RAG 问答权限链路、文档上传权限链路、管理后台权限链路
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 4、5.2 节；`MICROSERVICE_DATA_MODEL.md` 第 6.2、6.3 节
- 变更位置：`services/auth-service/src/main/java/com/lnzz/rag/auth/permission`、`contracts/openapi/auth-service.yaml`
- 前置依赖：MS-TASK-004
- 输入：用户、部门、角色、权限、数据范围、权限快照、租户配置、租户配额
- 输出：`/internal/v1/auth/permission-snapshots`、`readable-scopes`、`permissions/*:check`、`tenant-quotas/reservations` 等内部 API
- 实现步骤：实现权限快照生成；实现 API 权限、数据范围、KB 权限、文档权限检查接口；实现租户配置读取；实现配额预占和确认；发布 `PermissionSnapshotChanged`
- 测试策略：先写 RAG readable scope 契约测试、权限服务不可用默认拒绝测试、配额超限测试
- 测试文件：`PermissionSnapshotContractTest.java`、`ReadableScopeContractTest.java`、`TenantQuotaReservationTest.java`
- 测试命令：`mvn -pl services/auth-service test -Dtest=PermissionSnapshotContractTest,ReadableScopeContractTest,TenantQuotaReservationTest`
- 预期 RED：rag-chat 无法获取权限快照；权限服务异常时返回允许
- 预期 GREEN：权限快照包含 revision；无权限默认拒绝；配额超限返回 `429001`
- 验收标准：RAG、文档、后台均只能通过 auth-service 做权限判断
- 风险与回滚：权限缓存不一致时以 DB 和最新 revision 为准，缓存可一键失效
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：权限异常默认拒绝；日志记录 filteredCount 但不暴露 ACL 明细；缓存 key 含 permissionRevision
- 状态：未开始

### MS-TASK-006 拆出 audit-service

- 需求来源：AGENTS 发布前必须审计；本轮要求先拆 auth-service 和 audit-service
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 16 节；`MICROSERVICE_DATA_MODEL.md` 第 7.11 节
- 变更位置：`services/audit-service/`、`contracts/openapi/audit-service.yaml`、`db/rag_audit/`
- 前置依赖：MS-TASK-003、MS-TASK-004
- 输入：审计事件信封、`audit_event`、`audit_risk_event`、审计归档策略
- 输出：audit-service 工程、审计事件写入 API、审计查询 API、消费幂等记录
- 实现步骤：创建 `rag_audit` schema；实现 `/internal/v1/audit/events`；实现审计分页；消费 auth-service 审计事件；支持归档批次
- 测试策略：先写审计事件幂等测试、无权限审计查询测试、敏感字段脱敏测试
- 测试文件：`AuditEventIdempotencyTest.java`、`AuditQueryPermissionTest.java`、`AuditSensitivePayloadTest.java`
- 测试命令：`mvn -pl services/audit-service test -Dtest=AuditEventIdempotencyTest,AuditQueryPermissionTest,AuditSensitivePayloadTest`
- 预期 RED：重复 eventId 产生重复审计；普通用户可查询全租户审计
- 预期 GREEN：审计事件幂等，查询受数据范围约束，敏感字段脱敏
- 验收标准：auth-service 的登录、权限拒绝、授权变更都能进入 audit-service
- 风险与回滚：audit-service 异常时低风险业务走 Outbox 补偿，高风险配置变更可阻断
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：审计表索引按 tenant/time/resource；查询分页排序；日志不打印敏感 payload
- 状态：未开始

### MS-TASK-007 建立 auth-service 与 audit-service 的契约和回归门禁

- 需求来源：第一批服务拆分完成标准；AI Rules Compliance Gate 要求 TEST_REPORT / REVIEW 记录规则验证
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 5、16、19 节；`MICROSERVICE_DATA_MODEL.md` 第 12 节
- 变更位置：`contracts/tests/auth-audit/`、`services/auth-service/src/test/`、`services/audit-service/src/test/`
- 前置依赖：MS-TASK-005、MS-TASK-006
- 输入：auth-service 事件、audit-service 写入 API、错误码和 traceId
- 输出：auth-audit 契约测试、事件消费回归测试、规则合规检查
- 实现步骤：编写契约测试；验证登录失败事件进入审计；验证权限拒绝事件进入审计；验证 traceId 贯穿；输出测试报告模板
- 测试策略：契约测试 + 事件消费集成测试
- 测试文件：`AuthAuditContractTest.java`、`AuthAuditEventFlowTest.java`
- 测试命令：`mvn test -Dtest=AuthAuditContractTest,AuthAuditEventFlowTest`
- 预期 RED：auth 事件无法被 audit 幂等消费或 traceId 丢失
- 预期 GREEN：认证、权限拒绝、授权变更审计链路可回归
- 验收标准：第一批服务拆分具备可验证的安全和审计闭环
- 风险与回滚：若事件链路不稳定，auth-service 保留本地 Outbox 待补偿，不直接丢事件
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、LOGGING
- 规则检查点：所有高风险操作有审计；错误不隐藏；测试报告记录真实结果
- 状态：未开始

## 阶段 3：再拆 kb-service、document-service

### MS-TASK-008 拆出 kb-service

- 需求来源：知识库管理和 KB ACL 独立；RAG 可读范围依赖 KB 边界
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 6 节；`MICROSERVICE_DATA_MODEL.md` 第 7.1 节
- 变更位置：`services/kb-service/`、`contracts/openapi/kb-service.yaml`、`db/rag_kb/`
- 前置依赖：MS-TASK-005、MS-TASK-007
- 输入：知识库元数据、分类、KB ACL、检索配置
- 输出：kb-service 工程、知识库 CRUD、KB 权限更新、内部可读 KB 交集 API
- 实现步骤：创建 `rag_kb` schema；实现知识库分页、创建、更新、授权；调用 auth-service 做 API 权限和数据范围校验；发布 KB 事件
- 测试策略：先写 KB CRUD 契约测试、无权限授权测试、KB ACL 变更事件测试
- 测试文件：`KnowledgeBaseContractTest.java`、`KbPermissionContractTest.java`、`KbOutboxEventTest.java`
- 测试命令：`mvn -pl services/kb-service test -Dtest=KnowledgeBaseContractTest,KbPermissionContractTest,KbOutboxEventTest`
- 预期 RED：普通用户可创建或授权 KB；KB ACL 变更不发布事件
- 预期 GREEN：KB API 鉴权通过 auth-service，ACL 变更触发权限缓存失效事件
- 验收标准：KB 权限和配置作为 document / retrieval / rag-chat 的稳定依赖
- 风险与回滚：KB ACL 与 auth 权限快照不一致时，以 auth 最新 revision 和 KB DB 为准重算
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：KB 表唯一约束；接口 RESTful；权限变更审计；DTO 不返回 Entity
- 状态：未开始

### MS-TASK-009 拆出 document-service 上传、状态机和文档 ACL

- 需求来源：文档上传权限链路；文档处理必须异步化
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 7 节；`MICROSERVICE_DATA_MODEL.md` 第 7.2 节
- 变更位置：`services/document-service/`、`contracts/openapi/document-service.yaml`、`db/rag_document/`
- 前置依赖：MS-TASK-005、MS-TASK-008
- 输入：文档上传、MinIO 对象 key、文档状态机、文档 ACL、Outbox
- 输出：document-service 工程、上传/状态/删除/重建索引 API、`DocumentParseRequested` 事件
- 实现步骤：创建 `rag_document` schema；实现上传接口；上传前调用 auth-service 校验 KB/document 权限；写 MinIO、DB、Outbox；实现状态查询、删除、重建索引
- 测试策略：先写上传权限测试、状态机非法流转测试、MinIO 成功 DB 失败补偿测试
- 测试文件：`DocumentUploadPermissionTest.java`、`DocumentStateMachineTest.java`、`DocumentUploadCompensationTest.java`
- 测试命令：`mvn -pl services/document-service test -Dtest=DocumentUploadPermissionTest,DocumentStateMachineTest,DocumentUploadCompensationTest`
- 预期 RED：无 KB WRITE 权限仍可上传；状态可乱序推进
- 预期 GREEN：上传先校验权限，状态机按设计推进，失败可补偿
- 验收标准：上传请求只完成文件存储、元数据和任务投递，不等待解析/Embedding/索引
- 风险与回滚：上传链路失败时按对象 key 清理 MinIO 或由补偿任务补齐 Outbox
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：文件名和对象 key 不泄露敏感信息；写操作幂等；状态变更审计
- 状态：未开始

### MS-TASK-010 建立 kb/document 与 auth 权限链路契约

- 需求来源：文档上传链路必须 `document-service -> auth-service(check KB/document permission) -> MQ`
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 4.2、5.2、6、7 节
- 变更位置：`contracts/tests/auth-kb-document/`、`services/kb-service/src/test/`、`services/document-service/src/test/`
- 前置依赖：MS-TASK-008、MS-TASK-009
- 输入：auth-service KB/document 权限检查 API、kb-service ACL、document-service ACL
- 输出：权限链路契约测试和回归测试
- 实现步骤：编写 KB READ/WRITE/ADMIN 权限契约；编写文档继承/私有/CUSTOM 权限契约；验证权限变更后缓存失效；验证文档上传前置权限校验
- 测试策略：契约测试 + 集成测试
- 测试文件：`AuthKbDocumentPermissionFlowTest.java`、`DocumentUploadAuthContractTest.java`
- 测试命令：`mvn test -Dtest=AuthKbDocumentPermissionFlowTest,DocumentUploadAuthContractTest`
- 预期 RED：document-service 绕过 auth-service 或权限撤销后仍可上传
- 预期 GREEN：所有文档写操作先通过 auth-service 权限判断，撤销后立即拒绝或缓存过期拒绝
- 验收标准：文档入口不再自建权限 SQL，不跨库查询 auth schema
- 风险与回滚：权限接口延迟过高时只允许使用新鲜权限快照，不允许用过期快照扩大权限
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、LOGGING
- 规则检查点：服务之间只走 API；权限异常默认拒绝；拒绝写审计
- 状态：未开始

## 阶段 4：再拆 document-worker、embedding-service

### MS-TASK-011 拆出 document-worker

- 需求来源：文档处理异步链路；解析、清洗、切片和索引构建独立扩容
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 8 节；`MICROSERVICE_DATA_MODEL.md` 第 7.3、8.1 节
- 变更位置：`services/document-worker/`、`contracts/events/document-events.yaml`、`db/rag_document_worker/`
- 前置依赖：MS-TASK-009、MS-TASK-010
- 输入：`DocumentParseRequested` 事件、MinIO object key、文档元数据、Worker 任务表
- 输出：document-worker 工程、MQ 消费、解析任务状态、失败重试和死信策略
- 实现步骤：创建 Worker 工程；消费 `DocumentParseRequested`；通过 document-service 读取元数据；读取 MinIO；执行解析、清洗、切片；写任务尝试记录；失败进入重试或 DLQ
- 测试策略：先写重复事件幂等测试、解析失败重试测试、Worker 不接收用户 HTTP 上传测试
- 测试文件：`DocumentWorkerConsumeIdempotencyTest.java`、`DocumentWorkerRetryTest.java`、`DocumentWorkerBoundaryTest.java`
- 测试命令：`mvn -pl services/document-worker test -Dtest=DocumentWorkerConsumeIdempotencyTest,DocumentWorkerRetryTest,DocumentWorkerBoundaryTest`
- 预期 RED：重复事件重复解析；Worker 暴露用户上传接口
- 预期 GREEN：eventId 幂等消费，失败可重试，Worker 只处理内部任务
- 验收标准：文档上传与重处理能力解耦，Worker 可独立扩容
- 风险与回滚：解析器异常导致队列堆积时可暂停消费并转入 DLQ
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：消费记录唯一；任务日志带 traceId/documentId；不打印文档敏感正文
- 状态：未开始

### MS-TASK-012 拆出 embedding-service

- 需求来源：Embedding 受模型限流和批处理资源影响，需要独立服务
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 9 节；`MICROSERVICE_DATA_MODEL.md` 第 7.4 节
- 变更位置：`services/embedding-service/`、`contracts/openapi/embedding-service.yaml`、`db/rag_embedding/`
- 前置依赖：MS-TASK-011
- 输入：文本批次、模型编码、租户配额、LLM Gateway Embedding provider API
- 输出：批量 Embedding API、Query Embedding API、用量事件
- 实现步骤：创建 embedding-service 工程；实现批量 Embedding 和 Query Embedding；调用 llm-gateway-service 的 Embedding 接口；写批次和用量摘要；支持文本 hash 去重
- 测试策略：先写批量请求幂等测试、429 重试测试、配额超限测试
- 测试文件：`EmbeddingBatchContractTest.java`、`EmbeddingRetryPolicyTest.java`、`EmbeddingQuotaTest.java`
- 测试命令：`mvn -pl services/embedding-service test -Dtest=EmbeddingBatchContractTest,EmbeddingRetryPolicyTest,EmbeddingQuotaTest`
- 预期 RED：同一 requestId 重复生成；429 不重试或无限重试
- 预期 GREEN：Embedding 幂等、有限重试、配额超限拒绝并记录事件
- 验收标准：Worker 和 Retrieval 均通过 embedding-service 获取向量
- 风险与回滚：llm-gateway 未完成前可使用 Stub Provider，但必须在测试报告中标明
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：模型调用不在业务服务散落；Token 用量不使用 float/double；日志不打印全文
- 状态：未开始

### MS-TASK-013 建立文档处理到索引的一致性巡检

- 需求来源：文档处理链路 `document-worker -> parser -> chunk -> embedding -> vector/search`
- 设计来源：`MICROSERVICE_DATA_MODEL.md` 第 8.1、9 节；`MICROSERVICE_API_SPEC.md` 第 10 节
- 变更位置：`services/document-worker/consistency`、`services/retrieval-service/index-consistency`、`contracts/events/index-events.yaml`
- 前置依赖：MS-TASK-011、MS-TASK-012
- 输入：`doc_chunk`、Vector ID、Search Doc ID、索引投影
- 输出：索引写入任务、一致性巡检、补偿重建事件
- 实现步骤：定义一致性主键；实现索引写入请求契约；比对 DB Chunk 数、Vector 数、Search 索引数；输出补偿任务；记录巡检报告
- 测试策略：先写缺向量、缺搜索索引、重复 chunk 的巡检测试
- 测试文件：`DocumentIndexConsistencyTest.java`、`IndexRepairEventTest.java`
- 测试命令：`mvn test -Dtest=DocumentIndexConsistencyTest,IndexRepairEventTest`
- 预期 RED：索引缺失时文档仍进入 INDEXED
- 预期 GREEN：缺失能被巡检发现，文档状态和补偿任务可追踪
- 验收标准：Chunk、向量库、OpenSearch/Elasticsearch 具备最终一致性保障
- 风险与回滚：补偿任务误判时支持按 documentVersion 回滚重建
- 适用 `.ai_rules` 文件：README、DB、CODING、STRUCTURE、SERVICE、LOGGING
- 规则检查点：索引 ID 可反查 Chunk；巡检日志不打印完整正文；失败原因清晰
- 状态：未开始

## 阶段 5：再拆 llm-gateway-service、prompt-service

### MS-TASK-014 拆出 llm-gateway-service

- 需求来源：不允许业务服务直接调用模型供应商 API
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 12 节；`MICROSERVICE_DATA_MODEL.md` 第 7.7 节；`PROMPT_SPEC.md`
- 变更位置：`services/llm-gateway-service/`、`contracts/openapi/llm-gateway-service.yaml`、`db/rag_llm_gateway/`
- 前置依赖：MS-TASK-005、MS-TASK-012
- 输入：模型配置、供应商配置、API Key 密文、路由策略、租户配额
- 输出：Chat、Stream Chat、Embedding Provider、Rerank Provider 内部 API，Token 和成本原始记录
- 实现步骤：创建 schema 和模型配置表；实现供应商适配接口；实现 API Key 解密和脱敏；实现路由、限流、熔断、降级；发布 `LlmUsageRecorded`
- 测试策略：先写业务服务禁止供应商 SDK 依赖的架构测试、API Key 脱敏测试、熔断降级测试
- 测试文件：`NoProviderSdkOutsideGatewayTest.java`、`LlmApiKeyMaskingTest.java`、`LlmCircuitBreakerTest.java`
- 测试命令：`mvn test -Dtest=NoProviderSdkOutsideGatewayTest,LlmApiKeyMaskingTest,LlmCircuitBreakerTest`
- 预期 RED：rag-chat 或 embedding 直接依赖供应商 SDK；API Key 出现在日志
- 预期 GREEN：模型调用唯一出口为 llm-gateway-service，usage 和成本被记录
- 验收标准：Chat、Embedding、Rerank 调用统一经 LLM Gateway
- 风险与回滚：供应商不可用时按模型路由降级，必要时返回检索摘要和拒答
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：API Key 加密；成本 decimal；外部调用记录 costMs；敏感日志脱敏
- 状态：未开始

### MS-TASK-015 拆出 prompt-service

- 需求来源：不允许 Prompt 硬编码在 `rag-chat-service`
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 13 节；`MICROSERVICE_DATA_MODEL.md` 第 7.8 节；`PROMPT_SPEC.md`
- 变更位置：`services/prompt-service/`、`contracts/openapi/prompt-service.yaml`、`db/rag_prompt/`
- 前置依赖：MS-TASK-005、MS-TASK-014
- 输入：Prompt 模板、变量契约、版本、灰度、回滚策略
- 输出：Prompt 渲染 API、激活模板 API、发布和回滚 API
- 实现步骤：创建 prompt schema；实现模板版本不可覆盖；实现变量契约校验；实现 RAG Prompt 渲染；实现发布、灰度和回滚；发布 Prompt 变更事件
- 测试策略：先写 Prompt 变量契约测试、版本不可覆盖测试、Chat 服务禁止硬编码 Prompt 测试
- 测试文件：`PromptRenderContractTest.java`、`PromptVersioningTest.java`、`NoHardcodedPromptInChatTest.java`
- 测试命令：`mvn test -Dtest=PromptRenderContractTest,PromptVersioningTest,NoHardcodedPromptInChatTest`
- 预期 RED：Prompt 模板只能从代码读取或变量缺失仍渲染成功
- 预期 GREEN：Prompt 可配置化、版本化、变量校验失败返回清晰错误
- 验收标准：RAG Prompt 由 prompt-service 管理，支持版本追踪和回滚
- 风险与回滚：错误 Prompt 激活后可回滚上一 ACTIVE 版本
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：Prompt Gate 完整；模板变更审计；渲染日志不打印完整授权上下文
- 状态：未开始

### MS-TASK-016 建立 LLM Gateway 与 Prompt 的安全治理回归

- 需求来源：Prompt Injection、防敏感信息泄露、模型调用治理
- 设计来源：`PROMPT_SPEC.md` 安全与治理；`MICROSERVICE_API_SPEC.md` 第 12、13 节
- 变更位置：`contracts/tests/llm-prompt-security/`、`services/llm-gateway-service/src/test/`、`services/prompt-service/src/test/`
- 前置依赖：MS-TASK-014、MS-TASK-015
- 输入：Prompt 安全规则、模型调用日志、Token 用量、拒答契约
- 输出：Prompt Injection 回归测试、敏感信息输出测试、模型调用治理测试
- 实现步骤：编写对抗用例；验证系统规则优先级；验证 Prompt 渲染不泄露隐藏策略；验证 LLM Gateway 不记录完整 API Key 和敏感上下文
- 测试策略：安全回归测试 + 架构约束测试
- 测试文件：`PromptInjectionRegressionTest.java`、`LlmSensitiveDataGovernanceTest.java`
- 测试命令：`mvn test -Dtest=PromptInjectionRegressionTest,LlmSensitiveDataGovernanceTest`
- 预期 RED：用户输入可覆盖系统规则或日志泄露 API Key
- 预期 GREEN：Prompt Injection 被拒答，API Key 和敏感上下文脱敏
- 验收标准：模型治理边界可测试，不依赖人工口头检查
- 风险与回滚：模型输出不稳定时以规则校验和人工复核为准
- 适用 `.ai_rules` 文件：README、API、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：用户输入不能覆盖系统规则；危险操作拒答；日志脱敏
- 状态：未开始

## 阶段 6：再拆 retrieval-service、rag-chat-service

### MS-TASK-017 拆出 retrieval-service

- 需求来源：RAG 问答权限链路必须 `auth-service -> retrieval-service -> vector/search -> rerank`
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 10 节；`MICROSERVICE_DATA_MODEL.md` 第 7.5、9 节
- 变更位置：`services/retrieval-service/`、`contracts/openapi/retrieval-service.yaml`、`db/rag_retrieval/`
- 前置依赖：MS-TASK-010、MS-TASK-013、MS-TASK-014
- 输入：权限快照、可读范围、query embedding、OpenSearch、Milvus/Qdrant、document chunk 权限校验
- 输出：授权混合检索 API、索引写入/删除 API、Rerank 集成
- 实现步骤：实现授权检索接口；调用 auth-service 获取或校验可读范围；并行访问向量库和搜索索引；调用 document-service 做候选二次权限校验；调用 llm-gateway 做 Rerank 或降级排序
- 测试策略：先写越权 Chunk 过滤测试、向量库不可用降级测试、Rerank 失败降级测试
- 测试文件：`RetrievalPermissionFilterTest.java`、`RetrievalFallbackTest.java`、`RerankDegradationTest.java`
- 测试命令：`mvn -pl services/retrieval-service test -Dtest=RetrievalPermissionFilterTest,RetrievalFallbackTest,RerankDegradationTest`
- 预期 RED：无权限 Chunk 进入结果；Rerank 失败导致整个问答崩溃
- 预期 GREEN：候选二次权限校验通过，Rerank 失败可降级原排序
- 验收标准：retrieval-service 成为检索和索引归属服务，Chat 不直接访问 vector/search
- 风险与回滚：检索延迟过高时使用缓存和 TopK 限制，必要时回退关键词检索
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：权限异常默认拒绝；不跨库查 auth/document；检索日志不打印完整正文
- 状态：未开始

### MS-TASK-018 拆出 rag-chat-service

- 需求来源：RAG 问答服务只负责编排、会话、SSE 和引用，不得绕过权限和模型网关
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 4.1、11 节；`MICROSERVICE_DATA_MODEL.md` 第 7.6 节；`PROMPT_SPEC.md`
- 变更位置：`services/rag-chat-service/`、`contracts/openapi/rag-chat-service.yaml`、`db/rag_chat/`
- 前置依赖：MS-TASK-005、MS-TASK-015、MS-TASK-017
- 输入：用户问题、会话、知识库 ID、权限快照、检索结果、Prompt 渲染结果、LLM Gateway 输出
- 输出：非流式问答、流式问答、会话、消息、引用快照
- 实现步骤：创建 chat schema；实现会话和消息状态；问答前调用 auth-service 获取权限快照和可读范围；调用 retrieval-service；调用 prompt-service 渲染；调用 llm-gateway；保存 answer 和 citation；发布 chat 事件
- 测试策略：先写链路顺序架构测试、Chat 不直连模型供应商测试、Prompt 不硬编码测试、SSE 中断状态测试
- 测试文件：`RagChatPipelineOrderTest.java`、`RagChatNoProviderSdkTest.java`、`RagChatNoHardcodedPromptTest.java`、`ChatStreamStateTest.java`
- 测试命令：`mvn -pl services/rag-chat-service test -Dtest=RagChatPipelineOrderTest,RagChatNoProviderSdkTest,RagChatNoHardcodedPromptTest,ChatStreamStateTest`
- 预期 RED：Chat 直接调用 LLM SDK、绕过 auth-service 或直接拼 Prompt
- 预期 GREEN：链路固定为 `rag-chat -> auth -> retrieval -> prompt -> llm-gateway`
- 验收标准：RAG 问答权限、Prompt、模型调用边界均可验证
- 风险与回滚：LLM 不可用时返回检索摘要或拒答；流式失败时消息状态标记 FAILED/CANCELED
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：答案必须带引用；无上下文拒答；引用展示前再校验权限
- 状态：未开始

### MS-TASK-019 建立 RAG 问答端到端契约测试

- 需求来源：关键链路口径 1：`rag-chat-service -> auth-service -> retrieval-service -> vector/search -> rerank -> prompt-service -> llm-gateway-service`
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 4.1；`PROMPT_SPEC.md`；`MICROSERVICE_DATA_MODEL.md` 第 8、9 节
- 变更位置：`contracts/tests/rag-chat-e2e/`、`integration-tests/rag-chat-flow/`
- 前置依赖：MS-TASK-017、MS-TASK-018
- 输入：演示租户、用户、KB、文档、Chunk、Prompt、模型 Stub
- 输出：RAG 问答链路契约测试、越权拒答回归、无上下文拒答回归
- 实现步骤：准备最小测试数据；使用 Stub LLM Gateway；执行非流式和流式问答；验证权限快照调用、检索授权、Prompt 版本、引用权限、Token 事件
- 测试策略：端到端契约测试 + 安全回归测试
- 测试文件：`RagChatE2EContractTest.java`、`RagChatAuthorizationRegressionTest.java`
- 测试命令：`mvn test -Dtest=RagChatE2EContractTest,RagChatAuthorizationRegressionTest`
- 预期 RED：问答绕过权限或引用无权限文档
- 预期 GREEN：授权用户得到带引用答案，无权限用户拒答或过滤引用
- 验收标准：RAG 核心链路可被自动化证明，不靠人工观察
- 风险与回滚：端到端环境不稳定时保留契约测试和 Stub 回归，不伪造真实模型结果
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：所有响应带 traceId；引用可追溯；权限异常默认拒绝
- 状态：未开始

## 阶段 7：最后拆 feedback-service、evaluation-service、statistics-service、admin-config-service

### MS-TASK-020 拆出 feedback-service

- 需求来源：答案反馈闭环和质量运营
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 14 节；`MICROSERVICE_DATA_MODEL.md` 第 7.9 节
- 变更位置：`services/feedback-service/`、`contracts/openapi/feedback-service.yaml`、`db/rag_feedback/`
- 前置依赖：MS-TASK-018、MS-TASK-019
- 输入：messageId、feedbackType、score、reasonTags、comment
- 输出：反馈创建、反馈分页、反馈处理、反馈事件
- 实现步骤：创建 feedback schema；创建反馈接口；调用 rag-chat-service 校验消息可见性；评论脱敏；发布反馈事件
- 测试策略：先写他人消息不可反馈测试、评论脱敏测试、重复反馈幂等测试
- 测试文件：`FeedbackVisibilityTest.java`、`FeedbackDesensitizationTest.java`、`FeedbackIdempotencyTest.java`
- 测试命令：`mvn -pl services/feedback-service test -Dtest=FeedbackVisibilityTest,FeedbackDesensitizationTest,FeedbackIdempotencyTest`
- 预期 RED：用户可反馈他人消息或敏感评论原样入库
- 预期 GREEN：只可反馈可见消息，敏感信息脱敏，重复反馈按策略处理
- 验收标准：反馈可进入评测和统计闭环
- 风险与回滚：恶意刷反馈时按用户限流，处理状态可回滚为 OPEN
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：接口鉴权；分页排序；评论脱敏；处理动作审计
- 状态：未开始

### MS-TASK-021 拆出 evaluation-service

- 需求来源：Prompt_SPEC 评估方案、Golden Case 和回归 Case
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 15 节；`MICROSERVICE_DATA_MODEL.md` 第 7.10 节；`PROMPT_SPEC.md` 评估方案
- 变更位置：`services/evaluation-service/`、`contracts/openapi/evaluation-service.yaml`、`db/rag_evaluation/`
- 前置依赖：MS-TASK-018、MS-TASK-020
- 输入：评测集、评测用例、Prompt 版本、检索配置、模型 Stub 或真实模型配置
- 输出：评测集管理、评测任务、评分结果、评测事件
- 实现步骤：创建 evaluation schema；实现评测集和用例导入；实现评测运行任务；调用 rag-chat-service 非流式问答；记录正确性、引用准确性、拒答准确性
- 测试策略：先写无答案拒答、Prompt Injection、错误引用评分测试
- 测试文件：`EvaluationDatasetContractTest.java`、`RagEvaluationRunTest.java`、`PromptInjectionEvaluationTest.java`
- 测试命令：`mvn -pl services/evaluation-service test -Dtest=EvaluationDatasetContractTest,RagEvaluationRunTest,PromptInjectionEvaluationTest`
- 预期 RED：Prompt 和检索效果无法回归，越权样本被当作正确回答
- 预期 GREEN：评测任务产出评分，越权和注入样本被识别
- 验收标准：RAG 质量变化可通过评测集回归
- 风险与回滚：模型评分不稳定时以规则评分和人工复核为准
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：评测样本租户隔离；敏感样本脱敏；任务状态可追踪
- 状态：未开始

### MS-TASK-022 拆出 statistics-service

- 需求来源：Token、成本、质量和文档处理统计独立聚合，不影响在线问答
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 17 节；`MICROSERVICE_DATA_MODEL.md` 第 7.12 节
- 变更位置：`services/statistics-service/`、`contracts/openapi/statistics-service.yaml`、`db/rag_statistics/`
- 前置依赖：MS-TASK-014、MS-TASK-020、MS-TASK-021
- 输入：`LlmUsageRecorded`、`FeedbackCreated`、`EvaluationRunCompleted`、文档处理事件
- 输出：Token 统计、成本统计、反馈统计、评测统计、聚合任务
- 实现步骤：创建 statistics schema；实现统计事件摄取；实现日聚合任务；实现 Token 和成本报表；返回 `statTime`
- 测试策略：先写事件幂等摄取测试、decimal 成本精度测试、跨租户报表拒绝测试
- 测试文件：`StatisticsEventIngestionTest.java`、`CostDecimalPrecisionTest.java`、`StatisticsDataScopeTest.java`
- 测试命令：`mvn -pl services/statistics-service test -Dtest=StatisticsEventIngestionTest,CostDecimalPrecisionTest,StatisticsDataScopeTest`
- 预期 RED：重复事件重复计数；成本使用 double 导致精度误差；普通用户跨租户查询
- 预期 GREEN：事件幂等、成本 decimal 精确、报表遵守数据范围
- 验收标准：统计聚合最终一致且不影响在线问答
- 风险与回滚：聚合失败时可按窗口重跑，报表标明统计时间
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：金额 decimal；分页排序；数据范围校验；统计延迟透明
- 状态：未开始

### MS-TASK-023 拆出 admin-config-service

- 需求来源：管理后台权限链路：`admin-config-service -> auth-service(api permission / data scope) -> target service`
- 设计来源：`MICROSERVICE_API_SPEC.md` 第 4.3、18 节；`MICROSERVICE_DATA_MODEL.md` 第 7.13 节
- 变更位置：`services/admin-config-service/`、`contracts/openapi/admin-config-service.yaml`、`db/rag_admin_config/`
- 前置依赖：MS-TASK-014、MS-TASK-015、MS-TASK-022
- 输入：管理员请求、API 权限、数据范围、模型配置、Prompt 配置、统计概览
- 输出：管理后台聚合 API、配置草稿、发布任务、目标服务配置转发
- 实现步骤：创建 admin schema；实现 overview 聚合；所有管理操作先调用 auth-service 检查 API 权限和数据范围；配置发布调用目标服务 API；写审计和编排记录
- 测试策略：先写管理权限链路测试、禁止跨库写目标服务 DB 的架构测试、部分下游失败降级测试
- 测试文件：`AdminConfigPermissionChainTest.java`、`AdminConfigNoCrossDbWriteTest.java`、`AdminOverviewDegradeTest.java`
- 测试命令：`mvn -pl services/admin-config-service test -Dtest=AdminConfigPermissionChainTest,AdminConfigNoCrossDbWriteTest,AdminOverviewDegradeTest`
- 预期 RED：admin-config 直接写 llm/prompt/kb DB 或绕过 auth-service 权限
- 预期 GREEN：管理操作先鉴权，再调用目标服务 API；下游部分失败返回 degraded
- 验收标准：后台配置聚合不污染领域服务数据边界
- 风险与回滚：发布失败时回滚草稿状态，目标服务已成功的操作按补偿记录处理
- 适用 `.ai_rules` 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：不跨库；不返回敏感 API Key；管理操作审计；错误信息清晰
- 状态：未开始

# 5. 微服务演进安全门禁

- MS-TASK-005 未完成前，不允许 `rag-chat-service`、`retrieval-service` 或 `document-service` 实现真实权限链路。
- MS-TASK-010 未完成前，不允许文档上传进入真实 Worker 队列。
- MS-TASK-014 未完成前，不允许任何业务服务接入真实模型供应商 API。
- MS-TASK-015 未完成前，不允许实现生产级 RAG Chat Prompt 渲染。
- MS-TASK-017 未完成前，不允许 `rag-chat-service` 直接访问向量库或 OpenSearch。
- MS-TASK-018 未完成前，不允许对外开放 `/api/v1/chat/completions`。
- MS-TASK-023 未完成前，不允许后台配置服务跨库修改任何目标服务数据。

# 6. Plan Gate 自检

| 检查项 | 结论 |
| --- | --- |
| 阶段顺序符合用户要求 | 通过 |
| `auth-service` 合并 tenant / IAM 能力 | 通过 |
| 每个 TASK 包含需求来源、设计来源、变更位置、前置依赖、输入、输出、实现步骤 | 通过 |
| 每个 TASK 包含测试策略、测试文件、测试命令、预期 RED、预期 GREEN | 通过 |
| 每个 TASK 包含验收标准、风险与回滚、适用 `.ai_rules`、规则检查点、状态 | 通过 |
| 未进入业务代码实现 | 通过 |
| 未创建 Java 服务工程 | 通过 |
| 后续实现前要求 TDD Gate | 通过 |

# 7. 下一步建议

若用户继续微服务实施，不要直接编码全量服务。下一次只选择 `MS-TASK-001`，先进入 `test-designer / test-writer`，补 `MicroserviceContractStructureTest` 的失败测试，再最小化创建契约目录和服务边界登记表。
