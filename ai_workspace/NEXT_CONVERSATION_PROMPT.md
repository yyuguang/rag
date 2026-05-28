# 文档信息

- 文档名称：NEXT_CONVERSATION_PROMPT.md
- 当前状态：已完成
- 最近更新阶段：code-reviewer
- 最近更新原因：同步最近活跃任务的微服务 API、数据模型和任务拆分结果

# 当前任务归属

- task_name：enterprise_rag_platform_design
- 任务中文名：企业级 RAG 知识库问答平台系统设计
- 任务目录：`ai_workspace/projects/enterprise_rag_platform_design`

# 当前阶段

- 所处 core skill：code-reviewer / Next Prompt Handoff Gate
- 状态：review_done
- 是否阻断：否

# 最近完成事项

已按 C 方案“绞杀者式渐进拆分”完成微服务方向三份新增文档：

- `02_design/MICROSERVICE_API_SPEC.md`
- `02_design/MICROSERVICE_DATA_MODEL.md`
- `03_plan/MICROSERVICE_TODO.md`

本轮明确调整：第一阶段不单独拆 `tenant-service`，也不单独拆 `iam-service`；租户、用户、部门、角色、权限、权限快照、租户配置、租户配额和服务间鉴权先合并进 `auth-service`。后续当 IAM / tenant 能力复杂化、需要独立团队维护或被多个系统复用时，再从 `auth-service` 中拆出独立服务。

本轮未编写业务代码，未创建 Java 服务工程，未执行 Maven 测试。验证方式为文档静态检查和人工架构约束审查，结果已写入 `05_test/TEST_REPORT.md` 和 `06_review/REVIEW.md`。

# 下一步动作

若继续微服务实施，只执行 `03_plan/MICROSERVICE_TODO.md` 的 `MS-TASK-001`：冻结微服务边界与契约目录。下一轮必须先进入 `test-designer / test-writer`，编写失败测试；不要直接编码，不要创建全量 Java 服务工程，不要跳到后续服务拆分。

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
- `ai_workspace/projects/enterprise_rag_platform_design/00_meta/NEXT_PROMPT.md`
- `ai_workspace/projects/enterprise_rag_platform_design/00_meta/CURRENT_FOCUS.md`
- `ai_workspace/projects/enterprise_rag_platform_design/00_meta/STATUS.md`
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

Design Research Gate、Design Gate、Prompt Gate、API Gate、Data Gate、Plan Gate、Review Gate 和 Next Prompt Handoff Gate 已通过。任何代码实现前必须重新进入 TDD Gate。

# 用户可直接发送的提示词模板

```text
继续 enterprise_rag_platform_design 任务，只执行微服务方向的 MS-TASK-001 前置测试设计与失败测试。请严格遵守 AGENTS.md，先经过 workflow-orchestrator 识别任务类型。本轮仍不允许直接编码业务服务，不允许创建全量 Java 服务工程，不允许跳到后续服务拆分。

必须先读取 AGENTS.md、.ai_rules/README.md、.ai_rules/API_STYLE.md、.ai_rules/DB_STYLE.md、.ai_rules/CODING_STYLE.md、.ai_rules/PROJECT_STRUCTURE.md、.ai_rules/SERVICE_STYLE.md、.ai_rules/COMMENT_STYLE.md、.ai_rules/LOGGING_STYLE.md、ai_workspace/PROJECT_REGISTRY.md，以及 enterprise_rag_platform_design 任务目录下 NEXT_PROMPT.md、CURRENT_FOCUS.md、STATUS.md、DESIGN_RESEARCH.md、DESIGN.md、MICROSERVICE_DESIGN.md、MICROSERVICE_API_SPEC.md、MICROSERVICE_DATA_MODEL.md、PROMPT_SPEC.md、API_SPEC.md、DATA_MODEL.md、TODO.md、MICROSERVICE_TODO.md、TEST_REPORT.md、REVIEW.md。

本轮只围绕 03_plan/MICROSERVICE_TODO.md 的 MS-TASK-001：冻结微服务边界与契约目录。请进入 test-designer / test-writer，先设计并编写 MicroserviceContractStructureTest 的失败测试，验证 14 个服务契约目录、服务边界登记表、错误码登记表和事件命名规范缺失时应 RED。完成后同步更新 STATUS.md、CURRENT_FOCUS.md、TEST_REPORT.md、REVIEW.md、NEXT_PROMPT.md、ai_workspace/NEXT_CONVERSATION_PROMPT.md 和 PROJECT_REGISTRY.md。
```
