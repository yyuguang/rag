# 文档信息

- 文档名称：MICROSERVICE_API_SPEC.md
- 当前状态：已完成
- 最近更新阶段：api-designer
- 最近更新原因：基于 C 方案“绞杀者式渐进拆分”和本轮服务合并调整，补充微服务外部 API 与内部 API 契约

# 0. Workflow Orchestrator 任务识别

- 任务归属：`enterprise_rag_platform_design`
- 任务类型：L4 高风险架构设计 / 微服务演进规划 / API 契约设计
- 当前主流程：`workflow-orchestrator -> design-researcher -> system-architect -> api-designer`
- 本轮限制：不写业务代码，不创建 Java 服务工程，不进入 `spec-driven-coder`
- 方案结论：采用 C 方案，绞杀者式渐进拆分
- 关键调整：第一阶段不单独拆 `tenant-service`，也不单独拆 `iam-service`；租户、用户、部门、角色、权限、权限快照、租户配置、租户配额和服务间鉴权均合并进 `auth-service`

# 1. API 设计依据与规则门禁

## 1.1 已读取与适用规则

- `AGENTS.md`
- `.ai_rules/README.md`
- `.ai_rules/API_STYLE.md`
- `.ai_rules/DB_STYLE.md`
- `.ai_rules/CODING_STYLE.md`
- `.ai_rules/PROJECT_STRUCTURE.md`
- `.ai_rules/SERVICE_STYLE.md`
- `.ai_rules/COMMENT_STYLE.md`
- `.ai_rules/LOGGING_STYLE.md`
- `02_design/DESIGN_RESEARCH.md`
- `02_design/DESIGN.md`
- `02_design/MICROSERVICE_DESIGN.md`
- `02_design/PROMPT_SPEC.md`
- `02_design/API_SPEC.md`
- `02_design/DATA_MODEL.md`

## 1.2 `.ai_rules` 对本 API 文档的约束

- 外部 API 统一使用 `/api/v1/`。
- 内部 API 统一使用 `/internal/v1/`，只允许内网、mTLS 或内部 JWT 访问。
- API URL 使用名词，避免动词式路径。
- 所有请求和响应使用 DTO，禁止跨服务暴露 Entity。
- 所有响应携带 `traceId`。
- 创建、上传、重建索引、评测任务、配置发布等写接口必须支持幂等。
- 所有接口必须明确鉴权方式、错误码、超时、重试、熔断和审计要求。
- 错误信息必须清晰，禁止只返回“系统错误”。

# 2. 服务清单与职责调整

本轮微服务清单调整为 14 个服务：

| 服务 | 职责边界 |
| --- | --- |
| `auth-service` | 认证、租户、用户、部门、角色、权限、权限快照、租户配置、租户配额、服务间鉴权 |
| `kb-service` | 知识库元数据、分类、KB ACL、检索配置 |
| `document-service` | 文档上传、元数据、版本、状态机、文档 ACL、MinIO 对象归属 |
| `document-worker` | 文档解析、清洗、切片、索引任务编排 |
| `embedding-service` | Query / Chunk Embedding 批处理和模型调用编排 |
| `retrieval-service` | 授权混合检索、向量检索、关键词检索、Rerank、候选权限二次校验 |
| `rag-chat-service` | 会话、问答编排、SSE、引用落库、回答状态管理 |
| `llm-gateway-service` | 模型供应商唯一出口、模型路由、API Key 解密、限流、熔断、Token 和成本原始记录 |
| `prompt-service` | Prompt 模板、版本、变量契约、渲染、灰度、回滚 |
| `feedback-service` | 用户反馈、问题标注、反馈处理流转 |
| `evaluation-service` | 评测集、评测任务、RAG 回归、质量评分 |
| `audit-service` | 审计事件采集、查询、归档、风险审计 |
| `statistics-service` | Token、成本、文档、问答、反馈、评测统计聚合 |
| `admin-config-service` | 管理后台聚合入口，负责配置编排，不拥有其他服务数据真相 |

后续演进说明：

- 当 IAM 能力需要独立团队维护、被多个系统复用、接入 SSO / LDAP / OAuth2 或权限模型复杂化时，可从 `auth-service` 中拆出独立 `iam-service`。
- 当租户治理、套餐计费、跨系统租户管理、租户运营复杂化时，可从 `auth-service` 中拆出独立 `tenant-service`。
- 拆出前，所有业务服务统一调用 `auth-service` 的内部权限、租户和服务间鉴权接口。

# 3. 通用 API 契约

## 3.1 统一响应

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "traceId": "trace-20260528-0001"
}
```

## 3.2 通用错误码

| code | 含义 | 处理要求 |
| --- | --- | --- |
| `0` | 成功 | 正常返回 |
| `400001` | 参数错误 | 返回具体字段和原因 |
| `401001` | 用户 Token 无效 | 用户重新登录 |
| `401101` | 服务间凭证无效 | 拒绝内部调用并审计 |
| `403001` | API / 数据 / KB / 文档无权限 | 默认拒绝 |
| `404001` | 资源不存在 | 不暴露无权限资源细节 |
| `409001` | 幂等冲突或状态冲突 | 返回已存在资源或当前状态 |
| `429001` | 用户、租户、模型或服务限流 | 可重试但需退避 |
| `500001` | 服务内部异常 | 记录 traceId 和异常摘要 |
| `502001` | 模型供应商或外部系统失败 | 触发重试或降级 |
| `503001` | 下游服务不可用或熔断 | 返回降级或拒答 |
| `503101` | 权限服务不可用且无可用快照 | 默认拒绝 |
| `503201` | 检索服务不可用 | 问答拒答或降级为无上下文 |

## 3.3 鉴权方式

| 鉴权方式 | 适用场景 | 说明 |
| --- | --- | --- |
| `PUBLIC_RATE_LIMITED` | 登录、健康检查 | 无用户 Token，但必须限流和审计异常 |
| `USER_BEARER` | 外部用户 API | Bearer Token，服务端解析 `tenantId/userId/permissionRevision` |
| `ADMIN_BEARER` | 管理后台 API | Bearer Token + API 权限 + 数据范围 |
| `SERVICE_JWT` | 内部服务间调用 | 内部 JWT 或签名头，证明调用方服务身份 |
| `SERVICE_JWT_DELEGATED_USER` | 带用户上下文的服务间调用 | 服务身份 + 用户委托上下文 |
| `MQ_EVENT` | 事件消费 | 消息签名 / broker ACL + `eventId` 幂等 |

## 3.4 超时、重试、熔断默认值

| 接口类型 | 默认超时 | 重试 | 熔断 |
| --- | --- | --- | --- |
| 权限快照 / 权限检查 | 300ms-800ms | 只对网络超时重试 1 次 | 按 `auth-service` 和接口维度熔断，失败默认拒绝 |
| 普通查询 | 800ms-1500ms | 不默认重试 | 错误率和 P95 超阈值熔断 |
| 写操作 | 1500ms-3000ms | 仅幂等接口可重试 | 状态冲突不重试 |
| 文件上传入口 | 10s-30s | 客户端使用 `Idempotency-Key` 重试 | MinIO / DB 异常分开降级 |
| 文档 Worker 内部任务 | 30s-300s | 指数退避 + 死信 | 按任务类型熔断 |
| Embedding / Rerank | 5s-30s | 429 / 网络超时重试 1-2 次 | 按 provider + model 熔断 |
| Chat 非流式 | 30s-60s | 默认不重试生成，避免重复副作用 | 按 provider + model 熔断 |
| Chat 流式 | 首包 10s，总时长 120s | 建连失败可重试，已出 token 不重试 | 按 provider + model 熔断 |

## 3.5 幂等约定

- 外部写接口使用请求头 `Idempotency-Key`。
- 内部事件使用 `eventId`，消费者写 `consume_log`。
- 文档任务使用 `taskId + documentVersion + operationType`。
- 索引写入使用 `tenantId + documentId + documentVersion + chunkNo`。
- 模型调用默认不重试已开始生成的请求；Embedding 批处理可以按 chunk 幂等重试。

# 4. 关键链路口径

## 4.1 RAG 问答权限链路

```text
rag-chat-service
-> auth-service(permission snapshot / readable scope)
-> retrieval-service
-> vector/search
-> rerank
-> prompt-service
-> llm-gateway-service
```

约束：

- `rag-chat-service` 不得绕过 `auth-service` 的权限快照和可读范围接口。
- `retrieval-service` 不能只信任 Chat 传入的 KB ID，必须结合权限快照、metadata filter 和候选二次权限校验。
- Prompt 只来自 `prompt-service`，不得硬编码在 `rag-chat-service`。
- 模型调用只通过 `llm-gateway-service`。

## 4.2 文档上传权限链路

```text
document-service
-> auth-service(check KB/document permission)
-> MQ
-> document-worker
-> parser
-> chunk
-> embedding
-> vector/search
```

约束：

- `document-service` 上传、删除、重建索引前必须调用 `auth-service` 校验 KB / Document 权限。
- `document-worker` 不使用用户 Access Token，使用服务身份和任务上下文。
- Worker 只能通过内部 API 回写状态，不能直接修改 `document-service` schema。

## 4.3 管理后台权限链路

```text
admin-config-service
-> auth-service(api permission / data scope)
-> target service
```

约束：

- `admin-config-service` 只是管理后台聚合入口，不跨库查询或修改目标服务数据库。
- 配置变更必须调用目标服务 API，并写 `audit-service`。

# 5. `auth-service` API

## 5.1 外部 `/api/v1/` API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 登录 | `/api/v1/auth/login` | POST | Web / Admin | `PUBLIC_RATE_LIMITED` | `LoginReqDTO(tenantCode, username, password, captchaToken)` | `LoginResDTO(accessToken, refreshToken, expiresIn, userProfile, permissionRevision)` | `400001,401001,429001` | 不幂等；密码错误计数幂等更新 | 800ms | 不重试 | 按租户和用户名限流熔断 | 成功、失败、锁定均审计；密码不入日志 |
| 刷新 Token | `/api/v1/auth/token/refresh` | POST | Web / Admin | `USER_BEARER` 或 Refresh Token | `TokenRefreshReqDTO(refreshToken)` | `TokenRefreshResDTO(accessToken, expiresIn, permissionRevision)` | `401001,409001,429001` | `refreshToken + jti` 防重复刷新 | 800ms | 网络失败可重试 1 次 | 按用户熔断 | 记录刷新、撤销命中 |
| 退出登录 | `/api/v1/auth/logout` | POST | Web / Admin | `USER_BEARER` | `LogoutReqDTO(jti, deviceId)` | `BooleanResDTO(success)` | `401001,409001` | 幂等，重复退出返回成功 | 800ms | 可重试 | 按用户熔断 | 记录退出和 Token 撤销 |
| 当前用户信息 | `/api/v1/auth/me` | GET | Web / Admin | `USER_BEARER` | `CurrentUserQueryDTO()` | `CurrentUserResDTO(userId, tenantId, deptId, roles, permissions, quotaSummary)` | `401001,403001` | 只读 | 800ms | 可重试 1 次 | 按 auth-service 熔断 | 仅异常审计 |
| 用户分页 | `/api/v1/auth/users` | GET | Admin | `ADMIN_BEARER` | `UserPageReqDTO(pageNo, pageSize, keyword, deptId, status)` | `PageResDTO<UserResDTO>` | `401001,403001,400001` | 只读 | 1200ms | 可重试 1 次 | 按接口熔断 | 管理查询审计摘要 |
| 创建用户 | `/api/v1/auth/users` | POST | Admin | `ADMIN_BEARER` | `UserCreateReqDTO(username, displayName, deptId, roleIds, status)` | `UserCreateResDTO(userId)` | `400001,403001,409001` | `Idempotency-Key` + `tenantId + username` | 1500ms | 幂等键可重试 | 按接口熔断 | 记录创建人、用户、角色摘要 |
| 更新用户状态 | `/api/v1/auth/users/{userId}/status` | PUT | Admin | `ADMIN_BEARER` | `UserStatusUpdateReqDTO(status, reason)` | `BooleanResDTO(success)` | `403001,404001,409001` | 状态相同返回成功 | 1500ms | 可重试 | 按接口熔断 | 必须审计 |
| 部门树 | `/api/v1/auth/depts/tree` | GET | Admin | `ADMIN_BEARER` | `DeptTreeReqDTO(status)` | `DeptTreeResDTO(nodes)` | `401001,403001` | 只读 | 1200ms | 可重试 | 按接口熔断 | 异常审计 |
| 角色授权 | `/api/v1/auth/roles/{roleId}/permissions` | PUT | Admin | `ADMIN_BEARER` | `RolePermissionUpdateReqDTO(permissionCodes, dataScope, reason)` | `PermissionRevisionResDTO(permissionRevision)` | `403001,404001,409001` | `Idempotency-Key` + role version | 2000ms | 幂等键可重试 | 按授权接口熔断 | 必须审计，发布权限变更事件 |
| 租户配置更新 | `/api/v1/auth/tenant-config` | PUT | Tenant Admin / Platform Admin | `ADMIN_BEARER` | `TenantConfigUpdateReqDTO(defaultModel, retrievalPolicy, securityPolicy)` | `TenantConfigResDTO(configVersion)` | `403001,409001` | `Idempotency-Key` + config version | 2000ms | 幂等键可重试 | 按租户熔断 | 必须审计，发布配置变更事件 |
| 租户配额更新 | `/api/v1/auth/tenant-quotas` | PUT | Platform Admin | `ADMIN_BEARER` | `TenantQuotaUpdateReqDTO(tenantId, tokenMonthly, storageBytes, documentLimit, concurrentLimit)` | `TenantQuotaResDTO(quotaVersion)` | `403001,404001,409001` | `Idempotency-Key` + quota version | 2000ms | 幂等键可重试 | 按平台管理接口熔断 | 必须审计 |

## 5.2 内部 `/internal/v1/` API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Token introspect | `/internal/v1/auth/tokens/introspect` | POST | API Gateway / services | `SERVICE_JWT` | `TokenIntrospectReqDTO(token, requiredScopes)` | `TokenIntrospectResDTO(active, tenantId, userId, permissionRevision, jti)` | `401101,401001` | 只读 | 300ms | 可重试 1 次 | 失败率熔断；失败默认拒绝 | 记录无效 Token 摘要，不记录原文 |
| 服务间鉴权 | `/internal/v1/auth/service-credentials/verify` | POST | services | `SERVICE_JWT` | `ServiceCredentialVerifyReqDTO(callerService, signature, timestamp)` | `ServiceCredentialVerifyResDTO(allowed, scopes)` | `401101,403001` | 只读 | 300ms | 可重试 1 次 | 按调用方熔断 | 拒绝必须审计 |
| 权限快照 | `/internal/v1/auth/permission-snapshots` | POST | rag-chat / retrieval / kb / document / admin-config | `SERVICE_JWT_DELEGATED_USER` | `PermissionSnapshotReqDTO(tenantId, userId, permissionRevision)` | `PermissionSnapshotResDTO(apiCodes, dataScopes, readableKbIds, permissionRevision, expiresAt)` | `403001,503101` | 只读，可缓存 | 500ms | 网络错误重试 1 次 | 不可用默认拒绝 | 记录拒绝和超时 |
| API 权限检查 | `/internal/v1/auth/permissions/api:check` | POST | admin-config / gateway / target services | `SERVICE_JWT_DELEGATED_USER` | `ApiPermissionCheckReqDTO(method, path, permissionCode, resourceId)` | `PermissionCheckResDTO(allowed, reason, permissionRevision)` | `403001,503101` | 只读 | 500ms | 可重试 1 次 | 不可用默认拒绝 | 拒绝必须审计 |
| 数据范围检查 | `/internal/v1/auth/permissions/data-scope:check` | POST | admin-config / statistics / audit | `SERVICE_JWT_DELEGATED_USER` | `DataScopeCheckReqDTO(resourceType, resourceTenantId, deptId, ownerUserId)` | `PermissionCheckResDTO(allowed, dataScope, reason)` | `403001,503101` | 只读 | 500ms | 可重试 1 次 | 不可用默认拒绝 | 拒绝必须审计 |
| 可读范围 | `/internal/v1/auth/readable-scopes` | POST | rag-chat / retrieval | `SERVICE_JWT_DELEGATED_USER` | `ReadableScopeReqDTO(tenantId, userId, requestedKbIds, permissionRevision)` | `ReadableScopeResDTO(readableKbIds, readableDocPolicy, permissionRevision)` | `403001,503101` | 只读，可缓存 | 800ms | 可重试 1 次 | 不可用默认拒绝 | 记录 filtered count |
| KB 权限检查 | `/internal/v1/auth/permissions/kb:check` | POST | kb / document / rag-chat / retrieval | `SERVICE_JWT_DELEGATED_USER` | `KbPermissionCheckReqDTO(knowledgeBaseId, operation)` | `PermissionCheckResDTO(allowed, reason, permissionRevision)` | `403001,404001,503101` | 只读 | 500ms | 可重试 1 次 | 不可用默认拒绝 | 拒绝必须审计 |
| 文档权限检查 | `/internal/v1/auth/permissions/documents:check` | POST | document / retrieval / rag-chat | `SERVICE_JWT_DELEGATED_USER` | `DocumentPermissionCheckReqDTO(documentIds, chunkIds, operation)` | `DocumentPermissionCheckResDTO(allowedIds, deniedIds, reason)` | `403001,404001,503101` | 只读 | 800ms | 可重试 1 次 | 不可用默认拒绝 | 记录 denied count |
| 租户可用性 | `/internal/v1/auth/tenants/{tenantId}/availability` | GET | all services | `SERVICE_JWT` | `TenantAvailabilityReqDTO(operationType)` | `TenantAvailabilityResDTO(active, quotaStatus, configVersion)` | `403001,429001,503001` | 只读，可缓存 | 300ms | 可重试 1 次 | 按租户熔断 | 超限和禁用审计 |
| 租户配置 | `/internal/v1/auth/tenants/{tenantId}/config` | GET | llm-gateway / retrieval / prompt / embedding | `SERVICE_JWT` | `TenantConfigQueryReqDTO(configKeys)` | `TenantConfigResDTO(defaultModel, retrievalPolicy, securityPolicy, version)` | `404001,503001` | 只读，可缓存 | 500ms | 可重试 1 次 | 按租户熔断 | 配置缺失告警 |
| 租户配额预占 | `/internal/v1/auth/tenant-quotas/reservations` | POST | document / llm-gateway / embedding | `SERVICE_JWT_DELEGATED_USER` | `QuotaReserveReqDTO(resourceType, amount, requestId)` | `QuotaReserveResDTO(reservationId, allowed, reason)` | `429001,409001,503001` | `requestId` 幂等 | 800ms | 幂等键可重试 | 按租户熔断 | 超限必须审计 |
| 租户配额确认 | `/internal/v1/auth/tenant-quotas/reservations/{reservationId}` | PUT | document / llm-gateway / embedding | `SERVICE_JWT` | `QuotaCommitReqDTO(actualAmount, status)` | `BooleanResDTO(success)` | `404001,409001` | 幂等确认 | 800ms | 可重试 | 按租户熔断 | 记录用量确认 |

# 6. `kb-service` API

## 6.1 外部 API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 知识库分页 | `/api/v1/knowledge-bases` | GET | Web / Admin | `USER_BEARER` | `KnowledgeBasePageReqDTO(pageNo,pageSize,keyword,status)` | `PageResDTO<KnowledgeBaseResDTO>` | `401001,403001` | 只读 | 1200ms | 可重试 | 按接口熔断 | 异常审计 |
| 创建知识库 | `/api/v1/knowledge-bases` | POST | Admin | `ADMIN_BEARER` | `KnowledgeBaseCreateReqDTO(name,categoryId,visibility,retrievalConfig)` | `KnowledgeBaseCreateResDTO(knowledgeBaseId)` | `400001,403001,409001` | `Idempotency-Key` + name 唯一 | 2000ms | 幂等键可重试 | 按接口熔断 | 必须审计 |
| 更新知识库 | `/api/v1/knowledge-bases/{knowledgeBaseId}` | PUT | Admin | `ADMIN_BEARER` | `KnowledgeBaseUpdateReqDTO(name,status,retrievalConfig,version)` | `BooleanResDTO(success)` | `403001,404001,409001` | version 乐观锁 | 2000ms | 状态冲突不重试 | 按接口熔断 | 必须审计 |
| 知识库授权 | `/api/v1/knowledge-bases/{knowledgeBaseId}/permissions` | PUT | Admin | `ADMIN_BEARER` | `KbPermissionUpdateReqDTO(subjects,permissionType,reason)` | `PermissionRevisionResDTO(permissionRevision)` | `403001,404001,409001` | `Idempotency-Key` + ACL version | 2000ms | 幂等键可重试 | 按授权接口熔断 | 必须审计并发事件 |

## 6.2 内部 API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| KB 详情 | `/internal/v1/knowledge-bases/{knowledgeBaseId}` | GET | document / retrieval / rag-chat | `SERVICE_JWT_DELEGATED_USER` | `KbDetailReqDTO(includeConfig)` | `KbDetailResDTO(id,status,retrievalConfig,permissionVersion)` | `403001,404001` | 只读 | 500ms | 可重试 | 按 kb-service 熔断 | 异常审计 |
| 可读 KB 交集 | `/internal/v1/knowledge-bases/readable:intersect` | POST | rag-chat / retrieval | `SERVICE_JWT_DELEGATED_USER` | `KbReadableIntersectReqDTO(requestedKbIds, permissionSnapshot)` | `KbReadableIntersectResDTO(readableKbIds, filteredKbIds)` | `403001,503101` | 只读，可缓存 | 800ms | 可重试 1 次 | 不可用默认拒绝 | 记录 filtered count |
| KB 配置快照 | `/internal/v1/knowledge-bases/config-snapshots` | POST | retrieval / document-worker | `SERVICE_JWT` | `KbConfigSnapshotReqDTO(knowledgeBaseIds)` | `KbConfigSnapshotResDTO(configs)` | `404001,503001` | 只读 | 800ms | 可重试 | 按接口熔断 | 异常审计 |

# 7. `document-service` API

## 7.1 外部 API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 上传文档 | `/api/v1/documents/upload` | POST | Web / Admin | `USER_BEARER` | `DocumentUploadReqDTO(knowledgeBaseId,file,permissionMode,tags,checksum)` | `DocumentUploadResDTO(documentId,taskId,status)` | `400001,403001,409001,429001` | `Idempotency-Key` + checksum | 30s | 客户端可重试 | MinIO/DB 熔断分开 | 必须审计 |
| 文档状态 | `/api/v1/documents/{documentId}/status` | GET | Web / Admin | `USER_BEARER` | `DocumentStatusReqDTO(documentId)` | `DocumentStatusResDTO(status,progress,errorCode,updatedTime)` | `403001,404001` | 只读 | 1000ms | 可重试 | 按接口熔断 | 异常审计 |
| 删除文档 | `/api/v1/documents/{documentId}` | DELETE | Admin / KB Admin | `ADMIN_BEARER` | `DocumentDeleteReqDTO(reason,version)` | `BooleanResDTO(success)` | `403001,404001,409001` | 重复删除返回成功 | 2000ms | 可重试 | 按接口熔断 | 必须审计 |
| 重建索引 | `/api/v1/documents/{documentId}/reindex` | POST | Admin / KB Admin | `ADMIN_BEARER` | `DocumentReindexReqDTO(forceParse,reason)` | `DocumentReindexResDTO(taskId,status)` | `403001,404001,409001` | `Idempotency-Key` + operation | 2000ms | 幂等键可重试 | 按接口熔断 | 必须审计 |

## 7.2 内部 API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 文档元数据 | `/internal/v1/documents/{documentId}` | GET | document-worker / retrieval / rag-chat | `SERVICE_JWT` | `DocumentMetadataReqDTO(includeObjectKey,includeAcl)` | `DocumentMetadataResDTO(documentId,status,objectKey,version,aclSummary)` | `404001,403001` | 只读 | 800ms | 可重试 | 按 document 熔断 | 访问对象 key 需审计 |
| 批量 Chunk 权限 | `/internal/v1/documents/chunks/permissions:check` | POST | retrieval / rag-chat | `SERVICE_JWT_DELEGATED_USER` | `ChunkPermissionCheckReqDTO(chunkIds,operation,permissionSnapshot)` | `ChunkPermissionCheckResDTO(allowedChunkIds,deniedChunkIds)` | `403001,503101` | 只读 | 1000ms | 可重试 1 次 | 不可用默认拒绝 | 记录 denied count |
| 回写状态 | `/internal/v1/documents/{documentId}/status` | PUT | document-worker | `SERVICE_JWT` | `DocumentStatusUpdateReqDTO(expectedStatus,newStatus,taskId,errorCode,version)` | `DocumentStatusUpdateResDTO(updated,currentStatus,version)` | `409001,404001` | `taskId + newStatus` 幂等 | 1200ms | 幂等可重试 | 按状态接口熔断 | 状态变更审计 |
| 写入 Chunk 元数据 | `/internal/v1/documents/{documentId}/chunks` | POST | document-worker | `SERVICE_JWT` | `DocumentChunkBatchCreateReqDTO(documentVersion,chunks)` | `DocumentChunkBatchCreateResDTO(chunkIds)` | `409001,400001` | `documentId + version + chunkNo` | 3000ms | 幂等可重试 | 按写入接口熔断 | 记录 chunk 数 |

# 8. `document-worker` API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 查询任务 | `/internal/v1/document-worker/tasks/{taskId}` | GET | admin-config / document-service | `SERVICE_JWT` | `WorkerTaskQueryReqDTO(taskId)` | `WorkerTaskResDTO(status,attempt,errorCode,progress)` | `404001` | 只读 | 800ms | 可重试 | 按 worker 熔断 | 异常审计 |
| 重试任务 | `/internal/v1/document-worker/tasks/{taskId}/retry` | POST | document-service / admin-config | `SERVICE_JWT` | `WorkerTaskRetryReqDTO(reason,requestedBy)` | `WorkerTaskRetryResDTO(newAttemptId,status)` | `403001,404001,409001` | `Idempotency-Key` + task version | 1500ms | 幂等可重试 | 按重试接口熔断 | 必须审计 |
| 消费文档任务 | `MQ: DocumentParseRequested` | EVENT | document-service | `MQ_EVENT` | `DocumentParseRequestedEvent(eventId,tenantId,documentId,documentVersion,objectKey,traceId)` | `DocumentIndexSucceeded/FailedEvent` | `409001,503001` | `eventId` + consume log | 单阶段 300s | 指数退避 + DLQ | 按任务类型熔断 | 每次失败和成功审计摘要 |

# 9. `embedding-service` API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 批量 Embedding | `/internal/v1/embeddings/batch` | POST | document-worker / retrieval | `SERVICE_JWT` | `EmbeddingBatchReqDTO(texts,modelCode,requestId,metadata)` | `EmbeddingBatchResDTO(vectors,dimension,usage)` | `400001,429001,502001,503001` | `requestId + textHash` | 30s | 429/超时重试 2 次 | 按 provider + model 熔断 | 记录 token 和 cost 事件 |
| Query Embedding | `/internal/v1/embeddings/query` | POST | retrieval-service | `SERVICE_JWT_DELEGATED_USER` | `QueryEmbeddingReqDTO(query,modelCode,requestId)` | `QueryEmbeddingResDTO(vector,usage)` | `400001,429001,502001` | `requestId + queryHash` | 10s | 429/超时重试 1 次 | 按 provider + model 熔断 | 记录用量摘要 |

# 10. `retrieval-service` API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 授权检索 | `/internal/v1/retrieval/query` | POST | rag-chat-service | `SERVICE_JWT_DELEGATED_USER` | `RetrievalQueryReqDTO(question,readableScope,topK,hybridEnabled,permissionRevision)` | `RetrievalQueryResDTO(chunks,citations,filterSummary)` | `403001,503101,503201` | queryHash 可缓存，只读 | 3000ms | 检索超时不重试生成链路 | 按 vector/search/rerank 熔断 | 记录候选数、过滤数、耗时 |
| 写入索引 | `/internal/v1/retrieval/indexes/chunks:upsert` | POST | document-worker | `SERVICE_JWT` | `ChunkIndexUpsertReqDTO(documentId,version,chunks,vectors)` | `ChunkIndexUpsertResDTO(indexedCount,failedCount)` | `400001,409001,503001` | `tenantId + documentId + version + chunkNo` | 30s | 幂等可重试 | 按 vector/search 写入熔断 | 记录写入数量和失败原因 |
| 删除索引 | `/internal/v1/retrieval/indexes/documents/{documentId}` | DELETE | document-service / document-worker | `SERVICE_JWT` | `DocumentIndexDeleteReqDTO(documentVersion,reason)` | `BooleanResDTO(success)` | `404001,409001,503001` | 重复删除成功 | 10s | 可重试 | 按索引删除熔断 | 必须审计 |
| 检索配置校验 | `/internal/v1/retrieval/configs:validate` | POST | kb-service / admin-config | `SERVICE_JWT` | `RetrievalConfigValidateReqDTO(config)` | `RetrievalConfigValidateResDTO(valid,warnings)` | `400001` | 只读 | 800ms | 可重试 | 按接口熔断 | 异常审计 |

# 11. `rag-chat-service` API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 非流式问答 | `/api/v1/chat/completions` | POST | Web | `USER_BEARER` | `ChatCompletionReqDTO(sessionId,knowledgeBaseIds,question,retrievalOptions,modelCode)` | `ChatCompletionResDTO(messageId,answer,citations,confidence,usage)` | `400001,403001,429001,503101,503201,502001` | `Idempotency-Key` 可防重复提问 | 60s | 已生成不重试 | 按 auth/retrieval/prompt/llm 熔断 | 必须审计问答摘要和引用 |
| 流式问答 | `/api/v1/chat/completions/stream` | POST | Web | `USER_BEARER` | `ChatStreamReqDTO(sessionId,knowledgeBaseIds,question,retrievalOptions,modelCode)` | `SseEventDTO(message_start,answer_delta,citation,usage,message_end,error)` | `400001,403001,429001,503101,503201,502001` | `Idempotency-Key` 建议使用 | 首包 10s，总 120s | 建连失败可重试，已出 token 不重试 | 按下游熔断 | 必须审计开始、结束、失败 |
| 会话分页 | `/api/v1/chat/sessions` | GET | Web | `USER_BEARER` | `ChatSessionPageReqDTO(pageNo,pageSize,keyword,startTime,endTime)` | `PageResDTO<ChatSessionResDTO>` | `401001,403001` | 只读 | 1200ms | 可重试 | 按接口熔断 | 异常审计 |
| 会话详情 | `/api/v1/chat/sessions/{sessionId}` | GET | Web | `USER_BEARER` | `ChatSessionDetailReqDTO(sessionId)` | `ChatSessionDetailResDTO(session,messages,citations)` | `403001,404001` | 只读 | 1200ms | 可重试 | 按接口熔断 | 引用展示前审计拒绝 |
| 消息可见性 | `/internal/v1/chat/messages/{messageId}/visibility:check` | POST | feedback / evaluation / audit | `SERVICE_JWT_DELEGATED_USER` | `MessageVisibilityCheckReqDTO(messageId,operation)` | `PermissionCheckResDTO(allowed,reason)` | `403001,404001` | 只读 | 500ms | 可重试 | 按接口熔断 | 拒绝审计 |

# 12. `llm-gateway-service` API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Chat 调用 | `/internal/v1/llm/chat` | POST | rag-chat / evaluation | `SERVICE_JWT_DELEGATED_USER` | `LlmChatReqDTO(messages,modelCode,temperature,maxTokens,requestId)` | `LlmChatResDTO(answer,finishReason,usage,cost)` | `400001,429001,502001,503001` | `requestId` 记录但默认不重放答案 | 60s | 仅供应商未开始生成时重试 | 按 provider + model 熔断 | 记录原始 usage 和成本 |
| 流式 Chat | `/internal/v1/llm/chat/stream` | POST | rag-chat | `SERVICE_JWT_DELEGATED_USER` | `LlmStreamReqDTO(messages,modelCode,streamOptions,requestId)` | `StreamChunkDTO(delta,usage,finishReason,error)` | `429001,502001,503001` | `requestId` 防重复建连 | 首包 10s，总 120s | 出 token 后不重试 | 按 provider + model 熔断 | 记录开始、结束、失败和 usage |
| Embedding Provider | `/internal/v1/llm/embeddings` | POST | embedding-service | `SERVICE_JWT` | `LlmEmbeddingReqDTO(texts,modelCode,requestId)` | `LlmEmbeddingResDTO(vectors,usage,cost)` | `429001,502001,503001` | `requestId + textHash` | 30s | 429/超时重试 2 次 | 按 provider + model 熔断 | 记录 usage |
| Rerank Provider | `/internal/v1/llm/rerank` | POST | retrieval-service | `SERVICE_JWT` | `LlmRerankReqDTO(query,documents,topN,modelCode,requestId)` | `LlmRerankResDTO(results,usage,cost)` | `429001,502001,503001` | `requestId + queryHash` | 10s | 429/超时重试 1 次 | 按 provider + model 熔断；失败可降级原排序 | 记录 usage |

# 13. `prompt-service` API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 渲染 Prompt | `/internal/v1/prompts/render` | POST | rag-chat / evaluation | `SERVICE_JWT_DELEGATED_USER` | `PromptRenderReqDTO(promptCode,scenario,variables,contextRefs)` | `PromptRenderResDTO(renderedMessages,promptVersion,policySummary)` | `400001,403001,404001,409001` | 只读，可按变量 hash 缓存 | 1000ms | 可重试 | 按 prompt-service 熔断 | 记录 promptCode/version，不记录完整上下文 |
| 获取激活模板 | `/internal/v1/prompts/{promptCode}/active-template` | GET | rag-chat / evaluation / admin-config | `SERVICE_JWT` | `PromptActiveTemplateReqDTO(scenario,tenantId)` | `PromptTemplateResDTO(version,variables,status)` | `404001,503001` | 只读 | 800ms | 可重试 | 按接口熔断 | 异常审计 |
| 发布模板 | `/api/v1/prompt-templates/{promptCode}/versions` | POST | Admin | `ADMIN_BEARER` | `PromptTemplatePublishReqDTO(version,templateContent,variables,reason)` | `PromptTemplateResDTO(promptId,version,status)` | `400001,403001,409001` | `Idempotency-Key` + version 唯一 | 2000ms | 幂等键可重试 | 按发布接口熔断 | 必须审计 |
| 回滚模板 | `/api/v1/prompt-templates/{promptCode}/rollback` | POST | Admin | `ADMIN_BEARER` | `PromptRollbackReqDTO(targetVersion,reason)` | `PromptTemplateResDTO(activeVersion,status)` | `403001,404001,409001` | `Idempotency-Key` + targetVersion | 2000ms | 幂等键可重试 | 按发布接口熔断 | 必须审计 |

# 14. `feedback-service` API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 创建反馈 | `/api/v1/feedback` | POST | Web | `USER_BEARER` | `FeedbackCreateReqDTO(messageId,feedbackType,score,reasonTags,comment)` | `FeedbackCreateResDTO(feedbackId)` | `400001,403001,404001,409001` | `Idempotency-Key` + messageId + userId | 1500ms | 幂等键可重试 | 按接口熔断 | 必须审计，评论脱敏 |
| 反馈分页 | `/api/v1/feedback` | GET | Admin / Ops | `ADMIN_BEARER` | `FeedbackPageReqDTO(pageNo,pageSize,status,score,startTime,endTime)` | `PageResDTO<FeedbackResDTO>` | `403001` | 只读 | 1200ms | 可重试 | 按接口熔断 | 查询审计摘要 |
| 处理反馈 | `/api/v1/feedback/{feedbackId}/status` | PUT | Admin / Ops | `ADMIN_BEARER` | `FeedbackStatusUpdateReqDTO(status,handlerComment)` | `BooleanResDTO(success)` | `403001,404001,409001` | version 乐观锁 | 1500ms | 状态冲突不重试 | 按接口熔断 | 必须审计 |

# 15. `evaluation-service` API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 创建评测集 | `/api/v1/evaluations/datasets` | POST | Admin / Ops | `ADMIN_BEARER` | `EvaluationDatasetCreateReqDTO(name,description,visibility)` | `EvaluationDatasetResDTO(datasetId)` | `403001,409001` | `Idempotency-Key` + name 唯一 | 1500ms | 幂等可重试 | 按接口熔断 | 必须审计 |
| 导入用例 | `/api/v1/evaluations/datasets/{datasetId}/cases:import` | POST | Admin / Ops | `ADMIN_BEARER` | `EvaluationCaseImportReqDTO(cases,importMode)` | `EvaluationCaseImportResDTO(imported,skipped)` | `400001,403001,409001` | `Idempotency-Key` + caseHash | 5000ms | 幂等可重试 | 按导入接口熔断 | 必须审计，样本脱敏 |
| 启动评测 | `/api/v1/evaluations/runs` | POST | Admin / Ops | `ADMIN_BEARER` | `EvaluationRunCreateReqDTO(datasetId,promptVersion,retrievalConfig,modelCode)` | `EvaluationRunResDTO(runId,status)` | `403001,404001,409001` | `Idempotency-Key` | 2000ms | 幂等可重试 | 按任务接口熔断 | 必须审计 |
| 评测内部执行 | `/internal/v1/evaluations/runs/{runId}/execute` | POST | scheduler / admin-config | `SERVICE_JWT` | `EvaluationRunExecuteReqDTO(runId,shardNo)` | `EvaluationRunExecuteResDTO(status,processedCount)` | `409001,503001` | `runId + shardNo` | 300s | 指数退避 | 按任务类型熔断 | 记录执行摘要 |

# 16. `audit-service` API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 写审计事件 | `/internal/v1/audit/events` | POST | all services | `SERVICE_JWT` | `AuditEventCreateReqDTO(eventId,tenantId,userId,operation,resource,result,traceId)` | `AuditEventCreateResDTO(auditId,accepted)` | `400001,409001,503001` | `eventId` 幂等 | 800ms | 幂等可重试 | 按 audit-service 熔断；低风险可异步补偿 | 审计服务自身记录接收日志 |
| 审计分页 | `/api/v1/audit/events` | GET | Admin / Auditor | `ADMIN_BEARER` | `AuditEventPageReqDTO(pageNo,pageSize,operation,resourceType,startTime,endTime)` | `PageResDTO<AuditEventResDTO>` | `403001,400001` | 只读 | 1500ms | 可重试 | 按查询接口熔断 | 审计查询也要审计 |
| 审计归档 | `/internal/v1/audit/events:archive` | POST | scheduler | `SERVICE_JWT` | `AuditArchiveReqDTO(beforeTime,batchSize)` | `AuditArchiveResDTO(archivedCount)` | `409001,503001` | `archiveWindow` 幂等 | 300s | 可重试 | 按归档任务熔断 | 记录归档批次 |

# 17. `statistics-service` API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Token 统计 | `/api/v1/statistics/token-usage` | GET | Admin / User | `USER_BEARER` | `TokenUsageQueryReqDTO(startTime,endTime,groupBy,userId,modelCode)` | `TokenUsageReportResDTO(items,total)` | `403001,400001` | 只读 | 2000ms | 可重试 | 按查询接口熔断 | 管理查询审计 |
| 成本统计 | `/api/v1/statistics/costs` | GET | Admin | `ADMIN_BEARER` | `CostQueryReqDTO(startTime,endTime,groupBy,tenantId,modelCode)` | `CostReportResDTO(items,totalCost,budgetRate)` | `403001,400001` | 只读 | 2000ms | 可重试 | 按查询接口熔断 | 管理查询审计 |
| 事件摄取 | `/internal/v1/statistics/events` | POST | llm-gateway / feedback / evaluation / document-worker | `SERVICE_JWT` | `StatisticsEventCreateReqDTO(eventId,eventType,payload,traceId)` | `StatisticsEventCreateResDTO(accepted)` | `400001,409001,503001` | `eventId` 幂等 | 800ms | 幂等可重试 | 按摄取接口熔断 | 接收异常告警 |
| 聚合任务 | `/internal/v1/statistics/aggregations:run` | POST | scheduler | `SERVICE_JWT` | `StatisticsAggregationRunReqDTO(metricType,windowStart,windowEnd)` | `StatisticsAggregationRunResDTO(status,aggregatedCount)` | `409001,503001` | metric + window 幂等 | 300s | 可重试 | 按任务类型熔断 | 记录任务摘要 |

# 18. `admin-config-service` API

| 接口 | 路径 | 方法 | 调用方 | 鉴权方式 | 请求 DTO | 响应 DTO | 错误码 | 幂等要求 | 超时 | 重试 | 熔断 | 审计要求 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 管理首页配置 | `/api/v1/admin/config/overview` | GET | Admin | `ADMIN_BEARER` | `AdminOverviewReqDTO()` | `AdminOverviewResDTO(tenantConfig,modelSummary,kbSummary,quotaSummary)` | `403001,503001` | 只读 | 2000ms | 可重试 | 按目标服务分别熔断，部分失败返回 degraded | 查询审计摘要 |
| 模型配置发布 | `/api/v1/admin/config/models` | POST | Platform / Tenant Admin | `ADMIN_BEARER` | `AdminModelConfigPublishReqDTO(provider,modelCode,apiKey,priceConfig,scope)` | `AdminModelConfigResDTO(configId,status)` | `400001,403001,409001` | `Idempotency-Key` | 3000ms | 幂等可重试 | 调用 llm-gateway 失败回滚 | 必须审计，API Key 脱敏 |
| Prompt 配置发布 | `/api/v1/admin/config/prompts` | POST | Admin | `ADMIN_BEARER` | `AdminPromptPublishReqDTO(promptCode,version,templateContent,variables,reason)` | `AdminPromptPublishResDTO(promptId,activeVersion)` | `400001,403001,409001` | `Idempotency-Key` | 3000ms | 幂等可重试 | 调用 prompt-service 失败回滚 | 必须审计 |
| 目标服务配置转发 | `/internal/v1/admin/config/proxy` | POST | internal admin jobs | `SERVICE_JWT` | `AdminConfigProxyReqDTO(targetService,operation,payload,requestId)` | `AdminConfigProxyResDTO(targetStatus,result)` | `403001,409001,503001` | `requestId` 幂等 | 3000ms | 幂等可重试 | 按 targetService 熔断 | 必须记录目标服务和操作 |

# 19. API 设计一致性检查

| 检查项 | 结论 |
| --- | --- |
| 外部接口统一 `/api/v1/` | 通过 |
| 内部接口统一 `/internal/v1/` | 通过 |
| `auth-service` 合并 tenant / IAM 能力 | 通过 |
| RAG 问答权限链路不绕过 `auth-service` | 通过 |
| 文档上传权限链路先校验权限再投递 MQ | 通过 |
| 管理后台通过 `auth-service` 校验 API 权限和数据范围 | 通过 |
| 业务服务不直接调用模型供应商 API | 通过，统一走 `llm-gateway-service` |
| Prompt 不硬编码在 `rag-chat-service` | 通过，统一走 `prompt-service` |
| 服务间禁止跨库直接查询 | 通过，接口契约只暴露 DTO |
| 每个接口包含路径、方法、调用方、鉴权、DTO、错误码、幂等、超时、重试、熔断、审计 | 通过 |

# 20. 待后续细化事项

- 进入代码实现前，需要为每个 API 生成 OpenAPI / Swagger 契约和契约测试。
- DTO 字段需要在具体服务设计或实现 TASK 中继续展开，并补中文字段注释。
- 后续若从 `auth-service` 拆出 `iam-service` 或 `tenant-service`，本文件中的 `/internal/v1/auth/permissions/*` 和 `/internal/v1/auth/tenants/*` 需要迁移为新服务 API，并保留兼容代理期。
