# 文档信息

- 文档名称：TESTPLAN.md
- 当前状态：已完成
- 最近更新阶段：test-designer
- 最近更新原因：为 TASK-001 初始化 `rag-agent` Maven 父子工程骨架补充 TDD 测试方案

# 测试目标

验证本轮 TASK-001 只创建 `rag-agent` Maven 父工程和 `common`、`config` 两个子模块，且不越界创建 app 模块、微服务模块、数据库 / Redis / MQ / MinIO / LLM / RAG 业务功能。

# 测试范围

- `rag-agent/pom.xml` 必须存在，且 `packaging=pom`。
- 父 POM 的 `modules` 只能包含 `common`、`config`。
- 父 POM 统一管理 Java 17、Spring Boot 3.x、依赖版本和 Maven 插件版本。
- `rag-agent/common/pom.xml` 和 `rag-agent/config/pom.xml` 必须能被 Maven reactor 识别。
- `common` 仅预留 `com.lnzz.rag.common` 包结构。
- `config` 仅预留 `com.lnzz.rag.config` 包结构。
- 不创建 `rag-agent-app`、`domain`、`infrastructure`、`services` 或任何 `*-service` 微服务目录。

# 不测试范围

- 不启动 Spring Context，因为本轮不创建 `RagApplication.java` 或 app 模块。
- 不测试统一响应、异常、traceId、数据库、Redis、MQ、MinIO、LLM、RAG 业务功能。
- 不测试 Controller、Service、Mapper、DTO 分层实现，因为本轮只做父子工程骨架。
- 不验证真实外部中间件连接。

# 测试策略

采用 TDD：

1. 先在 `common` 和 `config` 子模块下编写架构测试。
2. 在生产骨架创建前执行 `cd rag-agent && mvn test`，记录 RED。
3. 最小化创建父 POM、子 POM 和包结构。
4. 再执行 `cd rag-agent && mvn test`，记录 GREEN。

# TDD 计划

## RED 测试

- `CommonModuleStructureTest`：校验父 POM packaging、modules、Java / Spring Boot 版本管理、`common` 包结构和禁止模块。
- `ConfigModuleStructureTest`：校验 `config` 子模块 POM、`config` 包结构，以及未创建真实基础设施配置。

## GREEN 判定

- `mvn test` 在 `rag-agent` 父工程下成功执行。
- Maven reactor 只构建 `rag-agent`、`common`、`config`。
- 两个架构测试全部通过。

## 回归测试

后续 TASK-002 或 TASK-004 如果要新增 `common` 或 `config` 具体能力，必须先更新对应 TASK 的测试方案，不能直接沿用本轮“只预留包结构”的验收口径。

## 测试豁免与替代验证

本轮不做 Spring Context 测试，原因是 `RagApplication.java` 启动类归属尚未在设计中明确，且用户明确禁止擅自创建 app 模块。

# 用例分类

## 功能测试

- TC-TASK001-001：父 POM 存在且 packaging 为 `pom`。
- TC-TASK001-002：父 POM modules 只包含 `common`、`config`。
- TC-TASK001-003：`common`、`config` 子模块 POM 继承 `rag-agent` 父工程。
- TC-TASK001-004：`com.lnzz.rag.common` 和 `com.lnzz.rag.config` 包路径存在。

## 接口测试

不适用。本轮不创建 API。

## 异常测试

- 父 POM 缺失时 RED。
- 子模块 POM 缺失时 RED。
- 创建禁用模块目录时 RED。

## 边界测试

- 父 POM 出现 `rag-agent-app`、`domain`、`infrastructure`、`services` 或任意微服务模块时失败。
- `config` 中出现真实 DB / Redis / MQ / MinIO / LLM 配置目录或配置文件时失败。

## 性能测试

不适用。本轮不涉及运行时性能。

# 覆盖要求

- 覆盖 Maven 父子工程结构。
- 覆盖 `common`、`config` 包结构。
- 覆盖用户本轮禁止事项。
- 覆盖 `.ai_rules` 中包名、模块职责、日志敏感信息和注释边界的适用检查项。

# 架构约束验证

- `common` 只能作为公共基础模块骨架，不能落入 RAG 业务逻辑。
- `config` 只能作为配置模块骨架，不能接入真实基础设施。
- `rag-agent` 本轮不是微服务根目录，不能创建 `services/*`。
- 启动类归属未明确前，不创建 `RagApplication.java`。

# Prompt 评估

不适用。本轮不涉及 Prompt 模板、AI 推理链路或模型输出契约。

# 风险项

- 本机 `JAVA_HOME` 当前配置为 `%JAVA_HOME17%`，需要在测试命令中临时指定可用 JDK 路径；测试报告必须如实记录。
- 原 TODO 曾要求创建启动类，本轮由用户明确收紧范围；该设计缺口必须写入 `CURRENT_FOCUS.md` 或后续 TASK 待确认项。
