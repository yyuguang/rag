# 文档信息

- 文档名称：CURRENT_FOCUS.md
- 当前状态：已完成
- 最近更新阶段：code-reviewer
- 最近更新原因：记录本轮微服务 API、数据模型和任务拆分完成后的当前焦点、关键决策、风险和下一步动作

# 当前阶段目标

本轮目标已完成：在既有模块化单体设计和 `02_design/MICROSERVICE_DESIGN.md` 基础上，按用户明确拍板的 C 方案“绞杀者式渐进拆分”继续完成微服务方向的文档设计和任务拆分。

本轮核心调整是：第一阶段不单独拆 `tenant-service`，也不单独拆 `iam-service`；租户、用户、部门、角色、权限、权限快照、租户配置、租户配额和服务间鉴权先合并进 `auth-service`。后续当 IAM / tenant 能力复杂化、需要独立团队维护或被多个系统复用时，再从 `auth-service` 中拆出独立 `iam-service` 或 `tenant-service`。

# 最近完成事项

1. 按 `workflow-orchestrator` 判定本轮为 L4 高风险架构设计 / 微服务演进规划任务。
2. 完成 AI Rules Compliance Gate：读取 `AGENTS.md`、`.ai_rules/README.md`、API、DB、编码、分层、Service、注释、日志规则。
3. 续接已有 `enterprise_rag_platform_design` 任务目录，未新建重复任务。
4. 读取既有 `STATUS.md`、`CURRENT_FOCUS.md`、`DESIGN_RESEARCH.md`、`DESIGN.md`、`MICROSERVICE_DESIGN.md`、`PROMPT_SPEC.md`、`API_SPEC.md`、`DATA_MODEL.md`、`TODO.md`、`TEST_REPORT.md`、`REVIEW.md`。
5. 在 `02_design/MICROSERVICE_DESIGN.md` 顶部补充本轮修订覆盖说明，明确新文档优先级和 auth-service 合并 tenant / IAM 的新口径。
6. 新增 `02_design/MICROSERVICE_API_SPEC.md`，定义 14 个服务的外部 `/api/v1/` 和内部 `/internal/v1/` API，包含鉴权、DTO、错误码、幂等、超时、重试、熔断和审计要求。
7. 新增 `02_design/MICROSERVICE_DATA_MODEL.md`，定义独立 schema、表归属、Outbox、Inbox / consume log、缓存 key、索引归属和跨库禁止约束。
8. 新增 `03_plan/MICROSERVICE_TODO.md`，按 7 个阶段拆分 23 个可执行微服务 TASK。
9. 完成静态验证：3 份新增文档存在；API 文档包含 14 个服务；TODO 包含 7 个阶段和必填 TASK 字段；数据模型包含 auth 合并后的核心表、Outbox/consume log、禁止跨库和主技术栈约束。
10. 更新 `STATUS.md`、`TEST_REPORT.md`、`REVIEW.md`、`NEXT_PROMPT.md`、全局 `NEXT_CONVERSATION_PROMPT.md` 和 `PROJECT_REGISTRY.md`。

# 当前阻断

无文档流程阻断。当前仍未实现业务代码，未创建 Java 服务工程，未执行 Maven 单元测试，不能声明软件可运行、性能达标或可发布。

# 最近 5 个关键设计决策

1. 微服务演进采用 C 方案：绞杀者式渐进拆分，而不是一次性全量微服务化。
2. 第一阶段只拆 `auth-service` 和 `audit-service`；`auth-service` 合并认证、租户、用户、部门、角色、权限、权限快照、租户配置、租户配额和服务间鉴权。
3. 14 个服务作为当前微服务设计清单：`auth-service`、`kb-service`、`document-service`、`document-worker`、`embedding-service`、`retrieval-service`、`rag-chat-service`、`llm-gateway-service`、`prompt-service`、`feedback-service`、`evaluation-service`、`audit-service`、`statistics-service`、`admin-config-service`。
4. RAG 问答链路固定为 `rag-chat-service -> auth-service(permission snapshot / readable scope) -> retrieval-service -> vector/search -> rerank -> prompt-service -> llm-gateway-service`。
5. 文档上传链路固定为 `document-service -> auth-service(check KB/document permission) -> MQ -> document-worker -> parser -> chunk -> embedding -> vector/search`；管理后台链路固定为 `admin-config-service -> auth-service(api permission / data scope) -> target service`。

# 最近 5 个真实缺陷或踩坑

1. 旧版 `MICROSERVICE_DESIGN.md` 曾把 `tenant-service`、`iam-service` 作为第一批独立服务；本轮已明确该口径被新文档覆盖，避免第一阶段拆分过细。
2. 如果 `auth-service` 合并过多能力但内部包边界不清，后续拆出 IAM / tenant 会很痛，因此 `MICROSERVICE_TODO.md` 把契约、上下文和 Outbox 标准放在第一阶段之前。
3. 如果只写服务清单而没有 API、数据、事件和测试策略，微服务拆分会变成空泛列表；本轮已补 API、数据模型和 23 个 TASK。
4. 如果 `rag-chat-service` 绕过 `auth-service` 权限快照，会导致越权 Chunk 进入 Prompt；本轮在 API 和 TODO 中设置了架构约束测试。
5. 如果业务服务直接调用模型供应商 API，Token 成本、API Key 保护、熔断和审计都会返工；本轮继续把模型调用唯一出口固定为 `llm-gateway-service`。

# 当前最重要的下一个任务

若继续微服务实施，下一步只执行 `03_plan/MICROSERVICE_TODO.md` 的 `MS-TASK-001`：冻结微服务边界与契约目录。必须先进入 `test-designer / test-writer`，编写 `MicroserviceContractStructureTest` 的失败测试，再最小化创建契约目录和服务边界登记表。

# 当前需要避免的设计漂移

- 避免重新把 `tenant-service`、`iam-service` 放回第一阶段独立拆分。
- 避免让 `rag-chat-service` 绕过 `auth-service` 权限快照 / readable scope。
- 避免让 `document-service` 绕过 `auth-service` 的 KB / document permission check。
- 避免让 `admin-config-service` 直接写目标服务数据库。
- 避免让业务服务直接调用模型供应商 API。
- 避免把 Prompt 硬编码在 `rag-chat-service`。
- 避免服务之间跨库直接查询或跨 schema join。
- 避免把 PostgreSQL + pgvector、Sa-Token、LangChain4j 改成主方案。
- 避免在未实现和未压测前把 P95、首 Token、成本或可用性指标说成真实结果。
