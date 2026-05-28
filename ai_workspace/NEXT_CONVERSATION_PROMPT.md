# 文档信息

- 文档名称：NEXT_CONVERSATION_PROMPT.md
- 当前状态：已完成
- 最近更新阶段：code-reviewer / Next Prompt Handoff Gate
- 最近更新原因：同步最近活跃任务 TASK-001 Maven 父子工程骨架完成结果

# 当前任务归属

- task_name：enterprise_rag_platform_design
- 任务中文名：企业级 RAG 知识库问答平台系统设计
- 任务目录：`ai_workspace/projects/enterprise_rag_platform_design`
- 当前代码目录：`rag-agent/`

# 当前阶段

- 所处 core skill：code-reviewer / Next Prompt Handoff Gate
- 状态：review_done
- 是否阻断：TASK-001 无阻断；后续 app 启动模块归属待确认

# 最近完成事项

已完成 MVP 模块化单体方向 `TASK-001 初始化 Spring Boot 工程骨架`：

- 创建 `rag-agent/pom.xml`，作为 Maven 父工程，`packaging=pom`。
- 父 POM 的 `modules` 只包含 `common`、`config`。
- 创建 `rag-agent/common/pom.xml` 和 `rag-agent/config/pom.xml`。
- 预留 `com.lnzz.rag.common` 和 `com.lnzz.rag.config` 包结构。
- 编写并执行架构测试：
  - `CommonModuleStructureTest`
  - `ConfigModuleStructureTest`
- RED：父 POM 缺失导致 `mvn test` 失败。
- GREEN：`cd rag-agent && mvn test` 通过，5 个测试全部通过。

本轮未创建 `rag-agent-app`、`domain`、`infrastructure`、`services/*` 或任何微服务模块，未实现 TASK-002 或任何业务功能。

# 下一步动作

若继续 MVP 实施，下一轮不要直接越界创建 app 或微服务模块。应先确认或补充 `RagApplication.java` / app 模块归属，再按 `TODO.md` 选择一个单一 TASK，并先写失败测试。

# 必须读取文件

- `AGENTS.md`
- `.ai_rules/README.md`
- `.ai_rules/CODING_STYLE.md`
- `.ai_rules/PROJECT_STRUCTURE.md`
- `.ai_rules/SERVICE_STYLE.md`
- `.ai_rules/COMMENT_STYLE.md`
- `.ai_rules/LOGGING_STYLE.md`
- `ai_workspace/PROJECT_REGISTRY.md`
- `ai_workspace/projects/enterprise_rag_platform_design/00_meta/STATUS.md`
- `ai_workspace/projects/enterprise_rag_platform_design/00_meta/CURRENT_FOCUS.md`
- `ai_workspace/projects/enterprise_rag_platform_design/00_meta/NEXT_PROMPT.md`
- `ai_workspace/projects/enterprise_rag_platform_design/02_design/DESIGN_RESEARCH.md`
- `ai_workspace/projects/enterprise_rag_platform_design/02_design/DESIGN.md`
- `ai_workspace/projects/enterprise_rag_platform_design/03_plan/TODO.md`
- `ai_workspace/projects/enterprise_rag_platform_design/05_test/TESTPLAN.md`
- `ai_workspace/projects/enterprise_rag_platform_design/05_test/TEST_REPORT.md`
- `ai_workspace/projects/enterprise_rag_platform_design/06_review/REVIEW.md`

# 当前质量门禁

AI Rules Compliance Gate、Plan Gate、TDD Gate、Implementation Gate、Test Execution Gate、Review Gate、Next Prompt Handoff Gate 已完成。Release Gate 未进入，软件整体不可发布。

# 用户可直接发送的提示词模板

```text
继续 enterprise_rag_platform_design 任务，仍走 MVP 模块化单体方向。请严格遵守 AGENTS.md，先经过 workflow-orchestrator 识别任务类型，并先读取 AGENTS.md、.ai_rules/README.md、.ai_rules/CODING_STYLE.md、.ai_rules/PROJECT_STRUCTURE.md、.ai_rules/SERVICE_STYLE.md、.ai_rules/COMMENT_STYLE.md、.ai_rules/LOGGING_STYLE.md、ai_workspace/PROJECT_REGISTRY.md，以及 enterprise_rag_platform_design 任务目录下 STATUS.md、CURRENT_FOCUS.md、NEXT_PROMPT.md、DESIGN_RESEARCH.md、DESIGN.md、TODO.md、TESTPLAN.md、TEST_REPORT.md、REVIEW.md。

当前 TASK-001 已完成：rag-agent Maven 父工程和 common/config 子模块已创建，mvn test 已通过。下一轮不要创建微服务工程，不要跳过 TDD Gate。如果要继续 TASK-002，必须先确认或补充 RagApplication.java / app 模块归属，再进入 test-designer / test-writer 编写失败测试；不允许直接实现统一响应、异常、traceId、数据库、Redis、MQ、MinIO、LLM 或 RAG 业务功能。
```
