# 文档信息

- 文档名称：NEXT_PROMPT.md
- 当前状态：已完成
- 最近更新阶段：code-reviewer
- 最近更新原因：新增微服务 API、数据模型和任务拆分文档后更新下一次对话交接

# 当前任务归属

- task_name：enterprise_rag_platform_design
- 任务中文名：企业级 RAG 知识库问答平台系统设计
- 任务目录：`ai_workspace/projects/enterprise_rag_platform_design`

# 当前阶段

- 所处 core skill：code-reviewer / Next Prompt Handoff Gate
- 状态：review_done
- 是否阻断：否
- 当前质量门禁：Design Research Gate、Design Gate、Prompt Gate、API Gate、Data Gate、Plan Gate、Review Gate、Next Prompt Handoff Gate 已完成；若进入代码实现，必须重新通过 TDD Gate。

# 最近完成事项

- 在 `02_design/MICROSERVICE_DESIGN.md` 顶部补充本轮修订覆盖说明，明确新口径：第一阶段不单独拆 `tenant-service` / `iam-service`，二者先合并进 `auth-service`。
- 新增 `02_design/MICROSERVICE_API_SPEC.md`，定义 14 个服务的外部 `/api/v1/` 和内部 `/internal/v1/` API。
- 新增 `02_design/MICROSERVICE_DATA_MODEL.md`，定义独立 schema、`rag_auth` 合并表归属、Outbox、Inbox / consume log、缓存 key、索引归属和禁止跨库查询。
- 新增 `03_plan/MICROSERVICE_TODO.md`，按 7 个阶段拆分 23 个微服务 TASK。
- 更新 `00_meta/STATUS.md`、`00_meta/CURRENT_FOCUS.md`、`05_test/TEST_REPORT.md`、`06_review/REVIEW.md`、`ai_workspace/PROJECT_REGISTRY.md` 和全局 `NEXT_CONVERSATION_PROMPT.md`。
- 本轮未编写业务代码，未创建 Java 服务工程，未执行 Maven 测试；验证方式为文档静态检查和人工架构约束审查。

# 下一步动作

若用户继续微服务实施，只执行一个阶段：进入 `test-designer / test-writer`，围绕 `03_plan/MICROSERVICE_TODO.md` 的 `MS-TASK-001` 编写失败测试 `MicroserviceContractStructureTest`。不要直接编码，不要创建全量 Java 服务工程，不要跳到后续服务拆分。

若用户只是继续完善文档，可优先清理 `02_design/MICROSERVICE_DESIGN.md` 正文中上一轮独立 `tenant-service` / `iam-service` 的历史口径，但仍必须以本轮新增三份文档为准。

# 必须读取文件

- `AGENTS.md`
- `.ai_rules/README.md`
- `.ai_rules/API_STYLE.md`
- `.ai_rules/DB_STYLE.md`
- `.ai_rules/CODING_STYLE.md`
- `.ai_rules/PROJECT_STRUCTURE.md`
- `.ai_rules/SERVICE_STYLE.md`
- `.ai_rules/COMMENT_STYLE.md`
- `.ai_rules/LOGGING_STYLE.md`
- `ai_workspace/PROJECT_REGISTRY.md`
- `ai_workspace/projects/enterprise_rag_platform_design/00_meta/STATUS.md`
- `ai_workspace/projects/enterprise_rag_platform_design/00_meta/CURRENT_FOCUS.md`
- `ai_workspace/projects/enterprise_rag_platform_design/01_requirement/REQUIREMENT.md`
- `ai_workspace/projects/enterprise_rag_platform_design/01_requirement/CLARIFICATION.md`
- `ai_workspace/projects/enterprise_rag_platform_design/02_design/DESIGN_RESEARCH.md`
- `ai_workspace/projects/enterprise_rag_platform_design/02_design/DESIGN.md`
- `ai_workspace/projects/enterprise_rag_platform_design/02_design/MICROSERVICE_DESIGN.md`
- `ai_workspace/projects/enterprise_rag_platform_design/02_design/MICROSERVICE_API_SPEC.md`
- `ai_workspace/projects/enterprise_rag_platform_design/02_design/MICROSERVICE_DATA_MODEL.md`
- `ai_workspace/projects/enterprise_rag_platform_design/02_design/PROMPT_SPEC.md`
- `ai_workspace/projects/enterprise_rag_platform_design/02_design/API_SPEC.md`
- `ai_workspace/projects/enterprise_rag_platform_design/02_design/DATA_MODEL.md`
- `ai_workspace/projects/enterprise_rag_platform_design/03_plan/TODO.md`
- `ai_workspace/projects/enterprise_rag_platform_design/03_plan/MICROSERVICE_TODO.md`
- `ai_workspace/projects/enterprise_rag_platform_design/05_test/TEST_REPORT.md`
- `ai_workspace/projects/enterprise_rag_platform_design/06_review/REVIEW.md`

# 当前质量门禁

- Design Research Gate：通过。
- Design Gate：通过。
- Prompt Gate：通过，继续要求 Prompt 服务化、版本化、不可硬编码。
- API Gate：通过，`MICROSERVICE_API_SPEC.md` 已生成。
- Data Gate：通过，`MICROSERVICE_DATA_MODEL.md` 已生成。
- Plan Gate：通过，`MICROSERVICE_TODO.md` 已生成并通过静态检查。
- TDD Gate：未进入；下一次如果开发任何代码，必须先写失败测试。
- Review Gate：本轮文档审查通过，软件实现未审查。

# 用户可直接发送的提示词模板

```text
继续 enterprise_rag_platform_design 任务，只执行微服务方向的 MS-TASK-001 前置测试设计与失败测试。请严格遵守 AGENTS.md，先经过 workflow-orchestrator 识别任务类型。本轮仍不允许直接编码业务服务，不允许创建全量 Java 服务工程，不允许跳到后续服务拆分。

必须先读取 AGENTS.md、.ai_rules/README.md、.ai_rules/API_STYLE.md、.ai_rules/DB_STYLE.md、.ai_rules/CODING_STYLE.md、.ai_rules/PROJECT_STRUCTURE.md、.ai_rules/SERVICE_STYLE.md、.ai_rules/COMMENT_STYLE.md、.ai_rules/LOGGING_STYLE.md、ai_workspace/PROJECT_REGISTRY.md，以及 enterprise_rag_platform_design 任务目录下 STATUS.md、CURRENT_FOCUS.md、DESIGN_RESEARCH.md、DESIGN.md、MICROSERVICE_DESIGN.md、MICROSERVICE_API_SPEC.md、MICROSERVICE_DATA_MODEL.md、PROMPT_SPEC.md、API_SPEC.md、DATA_MODEL.md、TODO.md、MICROSERVICE_TODO.md、TEST_REPORT.md、REVIEW.md。

本轮只围绕 03_plan/MICROSERVICE_TODO.md 的 MS-TASK-001：冻结微服务边界与契约目录。请进入 test-designer / test-writer，先设计并编写 MicroserviceContractStructureTest 的失败测试，验证 14 个服务契约目录、服务边界登记表、错误码登记表和事件命名规范缺失时应 RED。完成后同步更新 STATUS.md、CURRENT_FOCUS.md、TEST_REPORT.md、REVIEW.md、NEXT_PROMPT.md、ai_workspace/NEXT_CONVERSATION_PROMPT.md 和 PROJECT_REGISTRY.md。
```
