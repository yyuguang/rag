# 文档信息

- 文档名称：CURRENT_FOCUS.md
- 当前状态：已完成
- 最近更新阶段：test-executor / code-reviewer
- 最近更新原因：记录 TASK-001 Maven 父子工程骨架落地结果、验证结果和后续设计缺口

# 当前阶段目标

本轮已完成 MVP 模块化单体方向 `03_plan/TODO.md` 的 `TASK-001 初始化 Spring Boot 工程骨架`，但落地范围按用户补充约束收敛为：

- 仓库根目录下创建 `rag-agent/`。
- `rag-agent` 是 Maven 父工程根目录，`packaging=pom`。
- 父 POM 的 `modules` 只包含 `common`、`config`。
- 只创建 `common`、`config` 两个子模块和对应包结构。
- 不创建 `rag-agent-app`、`domain`、`infrastructure`、`services/*` 或任何微服务工程。

# 最近完成事项

1. 按 `workflow-orchestrator` 判定本轮为续接 `enterprise_rag_platform_design` 的工程实现任务，当前只允许执行 TASK-001。
2. 完成 AI Rules Compliance Gate：读取 `AGENTS.md`、`.ai_rules/README.md`、`CODING_STYLE.md`、`PROJECT_STRUCTURE.md`、`SERVICE_STYLE.md`、`COMMENT_STYLE.md`、`LOGGING_STYLE.md`。
3. 读取并续接 `PROJECT_REGISTRY.md`、`STATUS.md`、`CURRENT_FOCUS.md`、`DESIGN_RESEARCH.md`、`DESIGN.md`、`TODO.md`、`TEST_REPORT.md`、`REVIEW.md`。
4. 将用户本轮限制回写到 `03_plan/TODO.md` 的 TASK-001，避免继续沿用旧的 `RagApplication.java` / 可启动 app 范围。
5. 更新 `05_test/TESTPLAN.md`，明确本轮测试只覆盖 Maven 父子工程、模块和包结构。
6. 编写 RED 测试：
   - `rag-agent/common/src/test/java/com/lnzz/rag/common/architecture/CommonModuleStructureTest.java`
   - `rag-agent/config/src/test/java/com/lnzz/rag/config/architecture/ConfigModuleStructureTest.java`
7. 执行 RED：`cd rag-agent && mvn test`，失败原因为 `rag-agent/pom.xml` 缺失。
8. 最小化创建：
   - `rag-agent/pom.xml`
   - `rag-agent/common/pom.xml`
   - `rag-agent/config/pom.xml`
   - `rag-agent/common/src/main/java/com/lnzz/rag/common/package-info.java`
   - `rag-agent/config/src/main/java/com/lnzz/rag/config/package-info.java`
9. 执行 GREEN：`cd rag-agent && mvn test`，Maven reactor 只包含 `rag-agent`、`rag-agent-common`、`rag-agent-config`，5 个测试全部通过。
10. 未实现 TASK-002、数据库、Redis、MQ、MinIO、LLM、RAG 业务功能，未创建微服务模块。

# 当前阻断

- TASK-001 无阻断。
- 后续阻断 / 待确认：`RagApplication.java` 和应用启动模块归属尚未明确。后续如果要进入 Spring Context、Controller、统一响应、异常、traceId 或真实业务功能，必须先补充设计和 TODO 口径，不能直接擅自创建 app 模块。
- 环境说明：系统全局 `JAVA_HOME` 当前配置为 `%JAVA_HOME17%`，Maven 无法直接使用；本轮测试通过临时设置 `JAVA_HOME=E:\Program Files (x86)\engineers\Java\Jdk21` 执行，POM 使用 Maven Compiler `release=17` 保持 Java 17 目标版本。

# 最近 5 个关键设计决策

1. `rag-agent/` 是当前 MVP 模块化单体代码根目录，不与后续 `services/*` 微服务目录混用。
2. TASK-001 父 POM 只声明 `common`、`config` 两个模块，禁止提前加入 app、domain、infrastructure 或微服务模块。
3. 本轮不创建 `RagApplication.java`，因为启动类归属需要后续设计确认。
4. `common` 只预留 `com.lnzz.rag.common` 包结构，不落入统一响应、异常、traceId 等 TASK-002 内容。
5. `config` 只预留 `com.lnzz.rag.config` 包结构，不接入真实 DB、Redis、MQ、MinIO、LLM 或 RAG 配置。

# 最近 5 个真实缺陷或踩坑

1. 原 `TODO.md` 的 TASK-001 写有 `RagApplication.java` 和可启动 Spring Boot 工程，和用户本轮约束冲突；已回写收敛范围。
2. 全局 `JAVA_HOME` 配置为 `%JAVA_HOME17%`，直接执行 `mvn` 会失败；测试报告已记录临时环境变量处理。
3. `rag-agent/` 起初为空目录，RED 首次失败发生在 Maven 项目发现阶段，符合父 POM 缺失的目标行为。
4. 如果在 TASK-001 直接创建 `rag-agent-app` 或 `services/*`，会同时违反用户约束和 MVP 模块化单体边界。
5. 如果把 TASK-002 的统一响应、异常、traceId 提前塞入 `common`，会破坏“一次只执行一个 TASK”的流程约束。

# 当前最重要的下一个任务

不要自动进入下一任务。若用户继续 MVP 实施，下一轮应先确认或补充 `RagApplication.java` / 应用启动模块归属，再按 `TODO.md` 选择单一 TASK。若用户明确要求执行 TASK-002，也必须先进入 `workflow-orchestrator`，重新读取规则和当前任务文档，并先写失败测试。

# 当前需要避免的设计漂移

- 避免把 `rag-agent` 扩成微服务根目录。
- 避免创建 `rag-agent-app`、`domain`、`infrastructure` 或 `services/*`。
- 避免在 TASK-001 后直接实现统一响应、异常、traceId、数据库、Redis、MQ、MinIO、LLM 或 RAG 业务功能。
- 避免在未补充设计前创建 `RagApplication.java`。
- 避免把当前 GREEN 解释为软件可启动或可发布；本轮只证明 Maven 父子工程骨架和架构测试通过。
