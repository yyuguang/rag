# 文档信息

- 文档名称：API_SPEC.md
- 当前状态：已完成
- 最近更新阶段：api-designer
- 最近更新原因：定义企业级 RAG 平台主要 REST API

# 接口概述

所有接口遵循 RESTful 风格，统一前缀 `/api/v1/`。Controller 只负责参数校验、鉴权上下文读取和调用 Service，不直接访问 Mapper 或返回 Entity。

# 鉴权说明

- 用户接口使用 Bearer Token。
- 管理接口要求租户管理员、平台管理员或知识库管理员角色。
- 所有接口从登录态解析 `tenantId`、`userId`、`roleIds`、`deptId`。
- 严禁客户端自行传入租户后覆盖服务端上下文；如需要查询指定租户，仅平台管理员可使用。

# 通用约定

## 统一返回

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "traceId": "9f8c..."
}
```

## 分页参数

```json
{
  "pageNo": 1,
  "pageSize": 20
}
```

# 错误码规范

| code | 含义 |
| --- | --- |
| 0 | 成功 |
| 400001 | 参数错误 |
| 401001 | 未登录或 Token 无效 |
| 403001 | 无权限 |
| 404001 | 资源不存在 |
| 409001 | 幂等冲突或状态冲突 |
| 429001 | 请求过于频繁 |
| 500001 | 系统异常 |
| 502001 | 模型供应商调用失败 |
| 503001 | 服务降级或暂不可用 |

# 接口列表

## API-1 用户认证接口

- URL：`POST /api/v1/auth/login`
- Method：POST
- 请求参数：

```json
{
  "username": "zhangsan",
  "password": "******",
  "tenantCode": "demo"
}
```

- 返回字段：`accessToken`、`refreshToken`、`expiresIn`、`userId`、`tenantId`、`roles`。
- 关键校验逻辑：用户名密码必填；租户必须启用；密码错误次数限流；返回日志不得打印密码。

## API-2 知识库列表接口

- URL：`GET /api/v1/knowledge-bases`
- Method：GET
- 请求参数：`pageNo`、`pageSize`、`keyword`、`categoryId`、`status`。
- 返回字段：`id`、`name`、`description`、`categoryId`、`visibilityType`、`documentCount`、`chunkCount`、`status`。
- 关键校验逻辑：普通用户只返回有 READ 权限的知识库；管理员返回本租户知识库。

## API-3 创建知识库接口

- URL：`POST /api/v1/knowledge-bases`
- Method：POST
- 请求参数：

```json
{
  "name": "售后知识库",
  "description": "售后政策和故障处理",
  "categoryId": 10,
  "visibilityType": "PRIVATE",
  "embeddingModelCode": "text-embedding",
  "retrievalConfig": {
    "topK": 8,
    "scoreThreshold": 0.65,
    "hybridEnabled": true
  }
}
```

- 返回字段：`knowledgeBaseId`、`status`。
- 关键校验逻辑：名称租户内唯一；创建人拥有知识库管理权限；检索配置范围合法。

## API-4 修改知识库接口

- URL：`PUT /api/v1/knowledge-bases/{knowledgeBaseId}`
- Method：PUT
- 请求参数：名称、描述、分类、可见范围、检索配置、状态。
- 返回字段：`success`。
- 关键校验逻辑：仅知识库管理员或租户管理员可修改；已存在索引时修改 Embedding 模型需提示重建索引。

## API-5 文档上传接口

- URL：`POST /api/v1/documents/upload`
- Method：POST
- 请求参数：`knowledgeBaseId`、`file`、`permissionMode`、`tags`、`versionRemark`。
- 返回字段：`documentId`、`fileName`、`status=UPLOADED`、`taskId`。
- 关键校验逻辑：文件类型白名单；大小限制；知识库 WRITE 权限；保存 MinIO 后写 DB，再投递 MQ。

## API-6 文档状态查询接口

- URL：`GET /api/v1/documents/{documentId}/status`
- Method：GET
- 请求参数：路径参数 `documentId`。
- 返回字段：`documentId`、`status`、`progress`、`chunkCount`、`errorCode`、`errorMessage`、`updatedTime`。
- 关键校验逻辑：用户必须对文档所属知识库有 READ 或 ADMIN 权限。

## API-7 文档删除接口

- URL：`DELETE /api/v1/documents/{documentId}`
- Method：DELETE
- 请求参数：路径参数 `documentId`。
- 返回字段：`success`、`deletedChunkCount`。
- 关键校验逻辑：WRITE / ADMIN 权限；软删除文档；异步删除向量和搜索索引；操作写审计日志。

## API-8 文档重新索引接口

- URL：`POST /api/v1/documents/{documentId}/reindex`
- Method：POST
- 请求参数：

```json
{
  "forceParse": false,
  "reason": "调整切片策略后重建"
}
```

- 返回字段：`taskId`、`status`。
- 关键校验逻辑：仅管理员可操作；文档不能处于 `PARSING` 或 `EMBEDDING`；生成幂等任务号。

## API-9 非流式问答接口

- URL：`POST /api/v1/chat/completions`
- Method：POST
- 请求参数：

```json
{
  "sessionId": 10001,
  "knowledgeBaseIds": [1, 2],
  "question": "售后换货时效是多少？",
  "retrievalOptions": {
    "topK": 8,
    "hybridEnabled": true,
    "rerankEnabled": true
  },
  "modelCode": "default-chat"
}
```

- 返回字段：`messageId`、`answer`、`citations`、`confidence`、`tokenUsage`、`costAmount`、`traceId`。
- 关键校验逻辑：问题非空；知识库必须有 READ 权限；租户预算未超限；低置信拒答。

## API-10 流式问答接口

- URL：`POST /api/v1/chat/completions/stream`
- Method：POST
- 请求参数：同非流式问答。
- 返回字段：SSE 事件 `message_start`、`answer_delta`、`citation`、`usage`、`message_end`、`error`。
- 关键校验逻辑：鉴权和权限同非流式；客户端断开时停止模型流；结束帧保存完整回答。

## API-11 会话列表接口

- URL：`GET /api/v1/chat/sessions`
- Method：GET
- 请求参数：`pageNo`、`pageSize`、`keyword`、`knowledgeBaseId`、`startTime`、`endTime`。
- 返回字段：`sessionId`、`title`、`lastMessage`、`messageCount`、`updatedTime`。
- 关键校验逻辑：普通用户只看自己的会话；管理员可按权限查看审计范围内会话。

## API-12 会话详情接口

- URL：`GET /api/v1/chat/sessions/{sessionId}`
- Method：GET
- 请求参数：路径参数 `sessionId`。
- 返回字段：`session`、`messages`、`citations`。
- 关键校验逻辑：会话归属用户或管理员权限；引用来源需再次过滤权限。

## API-13 反馈接口

- URL：`POST /api/v1/feedback`
- Method：POST
- 请求参数：

```json
{
  "messageId": 20001,
  "feedbackType": "DISLIKE",
  "score": 2,
  "reasonTags": ["ANSWER_INACCURATE", "MISSING_CITATION"],
  "comment": "没有引用制度原文"
}
```

- 返回字段：`feedbackId`、`success`。
- 关键校验逻辑：只能反馈自己可见的消息；评分范围合法；敏感评论脱敏。

## API-14 模型配置接口

- URL：`POST /api/v1/models`
- Method：POST
- 请求参数：`provider`、`modelCode`、`modelType`、`endpoint`、`apiKeyCipher`、`timeoutMs`、`maxTokens`、`priceConfig`、`enabled`。
- 返回字段：`modelConfigId`、`status`。
- 关键校验逻辑：仅平台管理员；API Key 加密存储；模型编码唯一。

## API-15 Prompt 模板接口

- URL：`POST /api/v1/prompt-templates`
- Method：POST
- 请求参数：`promptCode`、`promptName`、`version`、`templateContent`、`variables`、`scenario`、`enabled`。
- 返回字段：`promptTemplateId`、`version`。
- 关键校验逻辑：Prompt 变量必须与模板一致；版本不可覆盖历史；变更写审计。

## API-16 Token 统计接口

- URL：`GET /api/v1/statistics/token-usage`
- Method：GET
- 请求参数：`tenantId`、`userId`、`modelCode`、`startDate`、`endDate`、`groupBy`。
- 返回字段：`promptTokens`、`completionTokens`、`totalTokens`、`costAmount`、`requestCount`。
- 关键校验逻辑：普通用户只能查自己；租户管理员查本租户；平台管理员可跨租户。

# 幂等与追踪

- 上传、重建索引、创建知识库等写接口支持 `Idempotency-Key`。
- 所有接口返回 `traceId`，日志中同值贯穿 DB、MQ、模型和向量库调用。

