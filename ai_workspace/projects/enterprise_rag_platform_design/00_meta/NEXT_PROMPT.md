# 文档信息

- 文档名称：NEXT_PROMPT.md
- 当前状态：已完成
- 最近更新阶段：code-reviewer / Next Prompt Handoff Gate
- 最近更新原因：完成 TASK-001 Maven 父子工程骨架后更新下一次对话交接

# 当前任务归属

- task_name：enterprise_rag_platform_design
- 任务中文名：企业级 RAG 知识库问答平台系统设计
- 任务目录：`ai_workspace/projects/enterprise_rag_platform_design`
- 当前代码目录：`rag-agent/`

# 当前阶段

- 所处 core skill：code-reviewer / Next Prompt Handoff Gate
- 状态：review_done
- 是否阻断：TASK-001 无阻断；后续 app 启动模块归属待确认
- 当前质量门禁：TDD Gate、Implementation Gate、Test Execution Gate、Review Gate、Next Prompt Handoff Gate 已完成；Release Gate 未进入。

# 最近完成事项

- 续接 `enterprise_rag_platform_design`，未创建重复任务目录。
- 回写 TASK-001 本轮范围：仅创建 `rag-agent` Maven 父工程和 `common`、`config` 子模块。
- 编写 RED 架构测试：
  - `rag-agent/common/src/test/java/com/lnzz/rag/common/architecture/CommonModuleStructureTest.java`
  - `rag-agent/config/src/test/java/com/lnzz/rag/config/architecture/ConfigModuleStructureTest.java`
- RED：`cd rag-agent && mvn test` 失败，原因是父 POM 缺失。
- GREEN：创建父 POM、两个子 POM 和包结构后，`cd rag-agent && mvn test` 通过；5 个测试全部通过。
- 已更新 `STATUS.md`、`CURRENT_FOCUS.md`、`TODO.md`、`TESTPLAN.md`、`TEST_REPORT.md`、`REVIEW.md`、`PROJECT_REGISTRY.md` 和全局 `NEXT_CONVERSATION_PROMPT.md`。
- 未创建 `rag-agent-app`、微服务工程或 TASK-002 内容。

# 下一步动作

不要自动进入下一任务。若继续 MVP 模块化单体方向，下一轮必须先确认或补充 `RagApplication.java` / app 模块归属，再选择单一 TASK 执行。若用户明确要求 TASK-002，仍需先经过 `workflow-orchestrator`，读取规则和当前文档，先进入 `test-designer / test-writer` 编写失败测试。

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

- AI Rules Compliance Gate：通过。
- Plan Gate：TASK-001 已回写本轮范围并完成。
- TDD Gate：通过，RED / GREEN 已记录。
- Implementation Gate：通过，未越界实现。
- Review Gate：通过。
- Next Prompt Handoff Gate：已更新任务级和全局提示词。
- Release Gate：未进入，软件整体不可发布。

# 用户可直接发送的提示词模板

```text
继续 enterprise_rag_platform_design 任务，仍走 MVP 模块化单体方向。请严格遵守 AGENTS.md，先经过 workflow-orchestrator 识别任务类型，并先读取 AGENTS.md、.ai_rules/README.md、.ai_rules/CODING_STYLE.md、.ai_rules/PROJECT_STRUCTURE.md、.ai_rules/SERVICE_STYLE.md、.ai_rules/COMMENT_STYLE.md、.ai_rules/LOGGING_STYLE.md、ai_workspace/PROJECT_REGISTRY.md，以及 enterprise_rag_platform_design 任务目录下 STATUS.md、CURRENT_FOCUS.md、NEXT_PROMPT.md、DESIGN_RESEARCH.md、DESIGN.md、TODO.md、TESTPLAN.md、TEST_REPORT.md、REVIEW.md。

当前 TASK-001 已完成：rag-agent Maven 父工程和 common/config 子模块已创建，mvn test 已通过。下一轮不要创建微服务工程，不要跳过 TDD Gate。如果要继续 TASK-002，必须先确认或补充 RagApplication.java / app 模块归属，再进入 test-designer / test-writer 编写失败测试；不允许直接实现统一响应、异常、traceId、数据库、Redis、MQ、MinIO、LLM 或 RAG 业务功能。
```
