# 文档信息

- 文档名称：SUMMARY.md
- 当前状态：已完成
- 最近更新阶段：knowledge-curator
- 最近更新原因：沉淀技术栈更新后的企业级 RAG 平台系统设计关键决策和后续建议

# 项目概述

本轮完成企业级 RAG 知识库问答平台系统设计，面向 Java 后端工程师学习、开发、简历和面试使用。设计覆盖文档处理、RAG 问答、多租户权限、模型网关、安全、性能、可观测、部署、代码结构、关键类、MVP 计划、生产演进、简历包装和面试问题。

# 关键决策回顾

1. MVP 采用模块化单体，生产根据压力拆分微服务。
2. 模型调用统一收口到 LLM Gateway，业务代码不直接调用供应商 API。
3. 检索采用向量 + 关键词 + Rerank 的混合策略。
4. 权限过滤贯穿检索前、检索中、检索后和引用返回前。
5. Prompt 配置化、版本化，并纳入安全和评估治理。
6. 数据模型统一带租户、审计、软删除、状态和索引。

# 被拒绝方案及原因

- 拒绝“第一阶段直接微服务化”：对个人 8 周 MVP 复杂度过高，容易拖慢核心 RAG 链路闭环。
- 拒绝“业务代码直接调用模型 API”：无法支撑供应商切换、成本统计、限流熔断、审计和安全治理。

# 关键问题回顾

- 当前仓库无业务代码，不能伪造现有调用链、测试结果或上线能力。
- RAG 的难点不只是模型调用，更是文档质量、检索质量、权限安全、成本控制和评估闭环。
- 真实企业场景下，权限模型和合规要求可能显著影响架构。

# 修复经验总结

本轮无业务代码缺陷修复。文档层面已补齐注册表、任务目录、需求、设计、计划、测试、审查、发布和总结闭环。

# 设计与实现偏差总结

当前只有设计，没有实现。后续进入开发时，必须按 `TODO.md` 从 TASK-001 开始，不得直接跳到 RAG 问答或前端演示。

# 可复用经验

- RAG 平台设计应先划清“文档处理链路”和“在线问答链路”。
- 企业级 RAG 必须把权限、审计、Token 和成本作为一等能力。
- Prompt 不是一段文本，而是输入契约、输出契约、安全边界、评估集和版本治理的组合。
- 简历项目要避免“调模型 Demo”表达，应强调工程闭环和可观测治理。

# 后续优化建议

1. 补充真实 DDL 脚本和 Flyway / Liquibase 迁移。
2. 搭建 Spring Boot 3.x + Spring Security + MySQL 8.0 + Spring AI MVP 工程。
3. 增加 RAG Golden Case 评测集。
4. 针对 Milvus、Qdrant 做小规模基准测试，并验证 MySQL 8.0 元数据查询与向量检索结果合并的延迟。
5. 增加前端管理台和问答界面原型。
6. 补充本地模型私有化部署方案。

# 下一次对话提示词

下一次如果继续实现，请发送：

```text
继续 enterprise_rag_platform_design 任务，只执行 TASK-001：初始化 Spring Boot 3.x + Spring Security + MySQL 8.0 + Spring AI 模块化单体工程骨架与模型网关基础。请先读取 AGENTS.md、ai_workspace/PROJECT_REGISTRY.md、ai_workspace/projects/enterprise_rag_platform_design/00_meta/CURRENT_FOCUS.md、00_meta/STATUS.md、03_plan/TODO.md、02_design/DESIGN.md、02_design/PROMPT_SPEC.md、02_design/API_SPEC.md 和 05_test/TESTPLAN.md。不要跳到文档上传、向量化或 RAG 问答实现；先补 LlmGatewayTest、ChatControllerTest 和 Spring Security 鉴权相关失败测试或记录测试豁免，再进入 spec-driven-coder。
```
