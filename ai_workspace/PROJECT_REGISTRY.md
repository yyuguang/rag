# 文档信息

- 文档名称：PROJECT_REGISTRY.md
- 当前状态：生效中
- 最近更新阶段：test-executor / code-reviewer / Next Prompt Handoff Gate
- 最近更新原因：完成 MVP 模块化单体方向 TASK-001：初始化 `rag-agent` Maven 父工程与 `common`、`config` 子模块

# 全局任务注册表

| task_name | 任务中文名 | 任务类型 | 当前状态 | 任务目录 | 相关模块 / 代码目录 | 关键词 / 别名 | 最近更新时间 | 下一步动作 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| enterprise_rag_platform_design | 企业级 RAG 知识库问答平台系统设计 | L4 长期 / 跨模块 / AI 架构设计 / MVP 模块化单体实施 / 微服务演进设计 | review_done | `ai_workspace/projects/enterprise_rag_platform_design` | `rag-agent,rag-agent/common,rag-agent/config,common,config,auth-service,kb-service,document-service,document-worker,embedding-service,retrieval-service,rag-chat-service,llm-gateway-service,prompt-service,feedback-service,evaluation-service,audit-service,statistics-service,admin-config-service` | RAG, 知识库问答, Spring Security, MySQL 8.0, Spring AI, LLM Gateway, 多租户, IAM, MVP 模块化单体, rag-agent, Maven 父子工程, common, config, DESIGN.md 主入口, TODO.md 主入口, auth-service 合并 tenant/IAM, 微服务拆分, 绞杀者式渐进拆分 | 2026-05-28 | TASK-001 已完成。若继续 MVP 实施，先确认或补充 `RagApplication.java` / app 模块归属，再按 `TODO.md` 选择单一 TASK 并先写失败测试；不要直接创建微服务工程或跳过 TDD Gate |
