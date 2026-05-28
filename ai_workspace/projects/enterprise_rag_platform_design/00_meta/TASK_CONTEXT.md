# 文档信息

- 文档名称：TASK_CONTEXT.md
- 当前状态：已完成
- 最近更新阶段：requirement-clarifier
- 最近更新原因：根据用户确认更新技术栈设计

# 任务检索元数据

- task_name：enterprise_rag_platform_design
- 任务中文名：企业级 RAG 知识库问答平台系统设计
- 任务类型：L4 长期 / 跨模块 / AI 架构设计 / Prompt 设计 / API 设计 / 数据设计
- 相关模块：auth、user、tenant、knowledgebase、document、parser、embedding、vector、retrieval、rerank、chat、llm、prompt、feedback、audit、common、config
- 相关代码目录：当前任务为设计文档任务，尚未创建业务代码；建议未来使用 `com.lnzz.rag.*`
- 关键词：RAG、企业知识库、Spring Security、MySQL 8.0、Spring AI、LLM Gateway、多租户、权限隔离、文档解析、向量化、混合检索、Rerank、Prompt Injection、Token 成本、可观测性、简历项目、面试
- 别名：企业级知识库问答平台、RAG 平台、AI 知识库助手、企业 AI 助手
- 上游任务：无
- 下游任务：MVP 实现、数据库 DDL 细化、Spring Boot 工程初始化、RAG 评测集建设

# 原始任务描述

用户要求设计一个接近真实企业可落地的“企业级 RAG 知识库问答平台”，覆盖项目定位、业务需求、非功能需求、整体架构、技术选型、RAG 流程、文档处理流程、数据库设计、接口设计、Prompt、多租户权限、检索策略、模型网关、安全、性能、可观测性、部署、Java 代码结构、关键类、核心难点、8 周 MVP、生产演进、简历包装和面试高频问题。

# 初始理解

本任务不是简单 Demo 说明，而是面向 5 年 Java 后端工程师的系统化学习、开发、简历和面试材料。技术栈已由用户确认：权限认证使用 Spring Security，主数据库使用 MySQL 8.0，AI 编排使用 Spring AI，文档解析链路引入 Spring AI，模型接入通过统一封装支持多个大模型供应商。设计还需结合 Redis、OpenSearch / Elasticsearch、Milvus / Qdrant、MinIO、RabbitMQ / Kafka、Docker、Prometheus、Grafana 等企业技术栈，并体现多租户、安全、权限过滤、Token 成本、可观测和可演进架构。

# 任务名称建议

采用 `enterprise_rag_platform_design`，符合 snake_case 规则。

# 目录初始化说明

已创建标准任务目录：

```text
ai_workspace/projects/enterprise_rag_platform_design/
```

# AI Rules Compliance Gate

本任务适用并已读取：

- `AGENTS.md`
- `.ai_rules/README.md`
- `.ai_rules/API_STYLE.md`
- `.ai_rules/DB_STYLE.md`
- `.ai_rules/CODING_STYLE.md`
- `.ai_rules/PROJECT_STRUCTURE.md`
- `.ai_rules/SERVICE_STYLE.md`
- `.ai_rules/COMMENT_STYLE.md`
- `.ai_rules/LOGGING_STYLE.md`
- `.skills/lnzz-skills/core/workflow-orchestrator/QUALITY_GATES.md`

# 备注

当前仓库尚无业务源码和数据库脚本，本次设计研究的“现状证据”来自用户需求、仓库流程文档、`.ai_rules`、core skill 和空仓库结构。该事实已写入 `DESIGN_RESEARCH.md`，不得伪装为已有实现。
