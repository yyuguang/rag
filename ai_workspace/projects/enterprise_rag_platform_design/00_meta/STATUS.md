# 文档信息

- 文档名称：STATUS.md
- 当前状态：已完成
- 最近更新阶段：code-reviewer
- 最近更新原因：基于 C 方案和 auth-service 合并 tenant / IAM 能力的新口径，新增微服务 API、数据模型和任务拆分文档，并完成静态验证与审查

# 项目状态

- 项目名称：企业级 RAG 知识库问答平台系统设计
- 当前状态：review_done
- 最近更新时间：2026-05-28
- 当前阻断：无文档流程阻断；当前仍为微服务方向设计与计划文档任务，业务代码尚未实现，未创建 Java 服务工程，未执行 Maven 单元测试，不能声明软件可运行或可发布。
- 下一阶段：若继续微服务实施，只能从 `03_plan/MICROSERVICE_TODO.md` 的 `MS-TASK-001` 开始，先进入 `test-designer / test-writer` 编写失败测试；不要直接进入 `spec-driven-coder`。

## 阶段记录

### initialized

- 状态：完成
- 说明：已创建任务目录和元信息文件。

### requirement_ready

- 状态：完成
- 说明：已生成 `01_requirement/REQUIREMENT.md`。

### clarification_ready

- 状态：完成
- 说明：已生成 `01_requirement/CLARIFICATION.md`，并记录技术栈口径：Spring Security、MySQL 8.0、Spring AI、统一 LLM Gateway。

### design_research_ready

- 状态：完成
- 说明：已生成 `02_design/DESIGN_RESEARCH.md`；本轮继续采用 C 方案“绞杀者式渐进拆分”，并把第一阶段独立 `tenant-service` / `iam-service` 调整为合并进 `auth-service`。

### design_ready

- 状态：完成
- 说明：既有 `02_design/DESIGN.md` 和 `02_design/MICROSERVICE_DESIGN.md` 可作为基础设计；本轮在 `MICROSERVICE_DESIGN.md` 顶部补充新口径覆盖说明。

### prompt_ready

- 状态：完成
- 说明：已生成 `02_design/PROMPT_SPEC.md`；本轮继续要求 Prompt 由 `prompt-service` 配置化、版本化、审计化，不得硬编码散落在 `rag-chat-service`。

### api_ready

- 状态：完成
- 说明：本轮新增 `02_design/MICROSERVICE_API_SPEC.md`，定义 14 个服务的外部 `/api/v1/` 和内部 `/internal/v1/` API；`auth-service` 合并 tenant / IAM 能力。

### data_ready

- 状态：完成
- 说明：本轮新增 `02_design/MICROSERVICE_DATA_MODEL.md`，定义独立 schema、表归属、Outbox、Inbox / consume log、缓存 key、索引归属和禁止跨库查询约束。

### plan_ready

- 状态：完成
- 说明：本轮新增 `03_plan/MICROSERVICE_TODO.md`，按 7 个阶段拆分 C 方案，共 23 个微服务原子 TASK，每个 TASK 均包含需求来源、设计来源、变更位置、测试策略、RED/GREEN、验收、风险回滚和 `.ai_rules` 检查点。

### testing_done

- 状态：完成
- 说明：本轮未编写业务代码，未执行 Maven 单元测试；已更新 `05_test/TEST_REPORT.md`，采用文档静态检查和人工架构约束验证。

### review_done

- 状态：完成
- 说明：已更新 `06_review/REVIEW.md`，结论为本轮微服务 API / 数据 / 任务拆分文档可交付；软件仍不可发布。

### release_ready

- 状态：未进入软件发布
- 说明：本轮不进入发布阶段；当前仅设计与计划文档可交付，软件不可发布。

### archived

- 状态：未归档
- 说明：本轮是续接任务后的新阶段，不再沿用上一轮 `archived` 作为当前状态。
