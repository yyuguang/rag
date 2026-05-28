# 文档信息

- 文档名称：PROJECT_REGISTRY.md
- 当前状态：生效中
- 最近更新阶段：code-reviewer
- 最近更新原因：新增微服务 API、数据模型和任务拆分文档，并将第一阶段 tenant / IAM 能力合并进 auth-service 的新口径同步到任务注册表

# 全局任务注册表

| task_name | 任务中文名 | 任务类型 | 当前状态 | 任务目录 | 相关模块 / 代码目录 | 关键词 / 别名 | 最近更新时间 | 下一步动作 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| enterprise_rag_platform_design | 企业级 RAG 知识库问答平台系统设计 | L4 长期 / 跨模块 / AI 架构设计 / 微服务拆分设计 / API 与数据模型设计 | review_done | `ai_workspace/projects/enterprise_rag_platform_design` | `auth-service,kb-service,document-service,document-worker,embedding-service,retrieval-service,rag-chat-service,llm-gateway-service,prompt-service,feedback-service,evaluation-service,audit-service,statistics-service,admin-config-service,common,config` | RAG, 知识库问答, Spring Security, MySQL 8.0, Spring AI, LLM Gateway, 多租户, IAM, auth-service 合并 tenant/IAM, 微服务拆分, 绞杀者式渐进拆分, 权限链路, 文档处理, 向量检索, 混合检索, Prompt Injection, Outbox, 服务间鉴权 | 2026-05-28 | 若继续微服务实施，只执行 `03_plan/MICROSERVICE_TODO.md` 的 `MS-TASK-001`，先进入 `test-designer / test-writer` 编写失败测试；不要直接编码，不要创建全量 Java 服务工程 |
