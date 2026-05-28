# 文档信息

- 文档名称：TODO.md
- 当前状态：已完成
- 最近更新阶段：task-planner
- 最近更新原因：重新拆分为权限基础优先、业务能力后置的原子化企业级实施计划

# 总体说明

本 TODO 面向后续代码实现阶段。本轮仅更新设计和计划文档，不进入 `spec-driven-coder`。后续开发必须一次只实现一个 TASK，且在进入实现前按 TDD Gate 先补失败测试或在测试文档中记录明确豁免原因。

执行总原则：

- 先完成工程基础、统一响应、异常、日志、traceId、MySQL、Redis、MinIO、MQ、OpenSearch、Milvus / Qdrant 配置。
- 再完成租户、用户、部门、角色、权限、数据隔离、知识库权限、文档权限、检索权限过滤和审计基础。
- 权限与多租户未完成前，不进入知识库、文档、RAG 问答、模型网关等上层能力。
- MySQL 8.0 只承载关系数据和元数据；向量检索由 Milvus / Qdrant 承担；关键词检索由 OpenSearch / Elasticsearch 承担。
- Prompt 不硬编码在业务代码中；模型供应商 API 不由业务 Service 直接调用，统一走 LLM Gateway。

# 规则简称

为避免重复长文本，以下 TASK 中的规则文件使用简称：

- `README`：`.ai_rules/README.md`
- `API`：`.ai_rules/API_STYLE.md`
- `DB`：`.ai_rules/DB_STYLE.md`
- `CODING`：`.ai_rules/CODING_STYLE.md`
- `STRUCTURE`：`.ai_rules/PROJECT_STRUCTURE.md`
- `SERVICE`：`.ai_rules/SERVICE_STYLE.md`
- `COMMENT`：`.ai_rules/COMMENT_STYLE.md`
- `LOGGING`：`.ai_rules/LOGGING_STYLE.md`

# 阶段 0：工程基础

## TASK-001 初始化 Spring Boot 工程骨架

- 所属模块：`common`、`config`
- 需求来源：AC-2、CLARIFICATION A-1
- 设计来源：`DESIGN.md` 第 2、13 节
- 变更位置：`pom.xml`、`src/main/java/com/lnzz/rag/RagApplication.java`、`com.lnzz.rag.*`
- 前置依赖：无
- 输入：Java 17+、Spring Boot 3.x、项目包结构规范
- 输出：可启动的空 Spring Boot 工程和标准包结构
- 实现步骤：创建工程；引入 Web、Validation、Security、MyBatis-Plus、Redis、Spring AI 基础依赖；按 `com.lnzz.rag` 创建模块包；配置 profiles。
- 测试策略：先写启动上下文测试和包结构扫描测试。
- 测试文件：`src/test/java/com/lnzz/rag/RagApplicationTests.java`、`ArchitecturePackageTest.java`
- 测试命令：`mvn test -Dtest=RagApplicationTests,ArchitecturePackageTest`
- 预期 RED：应用入口、依赖或包结构不存在导致测试失败。
- 预期 GREEN：Spring Context 启动成功，核心包存在。
- 验收标准：本地可执行 `mvn test` 并加载基础上下文。
- 风险与回滚：依赖冲突时回退到最小 Web + Test 依赖，再逐步引入。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：根包为 `com.lnzz.rag`；业务包按 controller/service/dao/dto 分层预留；无业务逻辑散落在入口类。
- 状态：未开始

## TASK-002 引入统一响应、异常、日志、traceId

- 所属模块：`common`、`observability`
- 需求来源：AC-2、AC-5、非功能需求可观测性
- 设计来源：`DESIGN.md` 第 0、11、13 节
- 变更位置：`common/response`、`common/exception`、`common/context`、`config/observability`
- 前置依赖：TASK-001
- 输入：API 统一返回规范、日志规范、错误码规范
- 输出：`ApiResult`、`BizException`、`GlobalExceptionHandler`、`TraceIdFilter`
- 实现步骤：定义统一响应；定义业务错误码；实现全局异常处理；实现 traceId 过滤器和 MDC 清理；配置 JSON 日志字段。
- 测试策略：Controller Mock 测试成功响应、业务异常、参数异常和 traceId 返回。
- 测试文件：`GlobalExceptionHandlerTest.java`、`TraceIdFilterTest.java`
- 测试命令：`mvn test -Dtest=GlobalExceptionHandlerTest,TraceIdFilterTest`
- 预期 RED：异常处理器和 traceId 缺失。
- 预期 GREEN：响应包含 `code/message/data/traceId`，异常信息清晰。
- 验收标准：所有 HTTP 响应结构统一，日志 MDC 包含 traceId。
- 风险与回滚：异常封装影响调试时保留 dev profile 详细错误，prod 脱敏。
- 适用 .ai_rules 文件：README、API、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：禁止返回“系统错误”；禁止日志打印敏感信息；Controller 不处理业务异常细节。
- 状态：未开始

## TASK-003 初始化 MySQL、MyBatis-Plus、Flyway / SQL 管理

- 所属模块：`common`、`config`
- 需求来源：AC-2、AC-3、CLARIFICATION A-2
- 设计来源：`DESIGN.md` 第 3、5、13 节；`DATA_MODEL.md`
- 变更位置：`config/mybatis`、`src/main/resources/db/migration`、`common/dao`
- 前置依赖：TASK-002
- 输入：MySQL 8.0、MyBatis-Plus、通用字段规范
- 输出：数据源配置、MyBatis-Plus 配置、SQL 迁移目录、通用 Entity 基类
- 实现步骤：配置 datasource；引入 Flyway 或 SQL 管理；定义 `BaseEntity`；配置逻辑删除、乐观锁、分页；预留租户拦截器。
- 测试策略：使用 Testcontainers 或 H2 兼容模式验证迁移和通用字段。
- 测试文件：`DatabaseMigrationTest.java`、`MybatisPlusConfigTest.java`
- 测试命令：`mvn test -Dtest=DatabaseMigrationTest,MybatisPlusConfigTest`
- 预期 RED：无数据源配置或迁移脚本导致测试失败。
- 预期 GREEN：迁移执行成功，BaseEntity 字段完整。
- 验收标准：数据库初始化可重复执行，通用字段符合 DB 规则。
- 风险与回滚：Flyway 引入复杂时可先使用 SQL 脚本目录并记录执行顺序。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：表字段 snake_case；必须有审计字段、软删除、version；禁止 `select *`。
- 状态：未开始

## TASK-004 初始化 Redis、MinIO、RabbitMQ、OpenSearch、Milvus / Qdrant 配置

- 所属模块：`config`、`common`
- 需求来源：FR-3、FR-4、非功能需求高可用和性能
- 设计来源：`DESIGN.md` 第 2、6、10、12 节
- 变更位置：`config/redis`、`config/minio`、`config/mq`、`config/opensearch`、`config/vector`
- 前置依赖：TASK-003
- 输入：基础设施连接参数
- 输出：各基础设施 Client Bean、健康检查、配置属性类
- 实现步骤：定义配置属性；创建 Redis、MinIO、RabbitMQ、OpenSearch、VectorStore client；添加连接健康检查；为测试提供 mock profile。
- 测试策略：配置属性绑定测试和 profile 条件装配测试。
- 测试文件：`InfrastructureConfigTest.java`
- 测试命令：`mvn test -Dtest=InfrastructureConfigTest`
- 预期 RED：配置 Bean 不存在。
- 预期 GREEN：mock profile 下基础设施 Bean 可加载。
- 验收标准：应用能在无真实中间件的测试环境启动，生产配置可外置。
- 风险与回滚：外部依赖不可用时使用 mock/stub，避免阻塞基础开发。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：配置不包含真实密钥；日志不打印连接密码；基础设施封装不进入 Controller。
- 状态：未开始

# 阶段 1：权限与多租户优先

## TASK-010 租户表与 TenantContext

- 所属模块：`tenant`、`common`
- 需求来源：FR-1、AC-3、CLARIFICATION A-3
- 设计来源：`DESIGN.md` 第 3.1、3.15、3.16、3.17 节；`DATA_MODEL.md` ENTITY-1
- 变更位置：`tenant/dao.entity/SysTenant.java`、`tenant/service/TenantService.java`、`common/context/TenantContextHolder.java`、`db/migration/V010__tenant.sql`
- 前置依赖：TASK-003
- 输入：租户编码、租户状态、请求上下文
- 输出：`sys_tenant` 表、TenantContext、租户可用性校验
- 实现步骤：创建租户表；实现 TenantContextHolder；实现租户查询和可用性校验；为后续拦截器提供 tenantId。
- 测试策略：租户创建、禁用租户、Context 清理测试。
- 测试文件：`TenantServiceTest.java`、`TenantContextHolderTest.java`
- 测试命令：`mvn test -Dtest=TenantServiceTest,TenantContextHolderTest`
- 预期 RED：租户表和 TenantContext 不存在。
- 预期 GREEN：租户可按编码查询，禁用租户被拒绝，ThreadLocal 被清理。
- 验收标准：所有后续业务可从上下文获取 `tenantId`。
- 风险与回滚：上下文泄漏会跨请求污染，Filter finally 必须清理。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：租户表字段和索引完整；Service interface + impl；日志带 tenantId。
- 状态：未开始

## TASK-011 用户、角色、部门基础模型

- 所属模块：`user`、`role`
- 需求来源：FR-1、AC-3
- 设计来源：`DESIGN.md` 第 3.2、3.3、3.4、4.2 user/role 节
- 变更位置：`user/*`、`role/*`、`db/migration/V011__user_role_dept.sql`
- 前置依赖：TASK-010
- 输入：用户、部门、角色基础数据
- 输出：`sys_user`、`sys_dept`、`sys_role`、`sys_user_role` 表和基础 Service
- 实现步骤：创建 DDL；实现用户、部门、角色 Entity/Mapper；实现基础查询和状态校验；定义角色类型枚举。
- 测试策略：租户内用户名唯一、部门树、用户角色绑定。
- 测试文件：`UserServiceTest.java`、`DeptServiceTest.java`、`RoleServiceTest.java`
- 测试命令：`mvn test -Dtest=UserServiceTest,DeptServiceTest,RoleServiceTest`
- 预期 RED：用户角色部门模型不存在。
- 预期 GREEN：用户、部门、角色可按租户隔离查询。
- 验收标准：认证和权限模块可复用用户、部门、角色数据。
- 风险与回滚：角色模型过早复杂化时保留最小角色类型，后续扩展权限点。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：用户名租户内唯一；Controller 不返回 Entity；DTO 字段有中文注释。
- 状态：未开始

## TASK-012 Spring Security 登录认证

- 所属模块：`auth`
- 需求来源：FR-1、API_SPEC API-1
- 设计来源：`DESIGN.md` 第 3.13、4.2 auth 节
- 变更位置：`auth/controller/AuthController.java`、`auth/service/AuthService.java`、`config/security/SecurityConfig.java`
- 前置依赖：TASK-011
- 输入：用户名、密码、租户编码
- 输出：登录认证能力和 `LoginUserPrincipal`
- 实现步骤：配置 Spring Security；实现密码校验；加载用户角色和租户状态；构建 Authentication；记录登录日志。
- 测试策略：成功登录、密码错误、禁用用户、禁用租户。
- 测试文件：`AuthControllerTest.java`、`AuthServiceTest.java`
- 测试命令：`mvn test -Dtest=AuthControllerTest,AuthServiceTest`
- 预期 RED：登录接口 404 或认证失败。
- 预期 GREEN：合法用户登录成功，异常场景返回明确错误码。
- 验收标准：登录接口遵循 `/api/v1/auth/login` 和统一返回结构。
- 风险与回滚：认证链影响所有接口，先保持除登录外默认鉴权，测试 profile 可放行健康检查。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：密码不入日志；Controller 只调用 Service；错误信息清晰。
- 状态：未开始

## TASK-013 JWT / Token 生成与校验

- 所属模块：`auth`、`common`
- 需求来源：FR-1、AC-3
- 设计来源：`DESIGN.md` 第 3.14 节
- 变更位置：`auth/service/TokenService.java`、`auth/dao.entity/SysUserToken.java`、`config/security/JwtAuthenticationFilter.java`
- 前置依赖：TASK-012
- 输入：登录成功用户、Refresh Token
- 输出：Access Token、Refresh Token、Token 校验过滤器、撤销能力
- 实现步骤：定义 Token claims；生成 access/refresh token；实现校验过滤器；实现刷新和退出；接入 Redis 黑名单或 tokenVersion。
- 测试策略：过期、刷新、退出、黑名单、tokenVersion 失效。
- 测试文件：`TokenServiceTest.java`、`JwtAuthenticationFilterTest.java`
- 测试命令：`mvn test -Dtest=TokenServiceTest,JwtAuthenticationFilterTest`
- 预期 RED：受保护接口无法识别 Token。
- 预期 GREEN：Bearer Token 可构建 SecurityContext，退出后失效。
- 验收标准：后续 API 能从 SecurityContext 获取用户和租户。
- 风险与回滚：密钥轮换复杂时 MVP 先单密钥，设计保留 kid。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：Token 不明文入库；日志不打印 Token；Refresh Token 可撤销。
- 状态：未开始

## TASK-014 API 权限拦截

- 所属模块：`permission`、`auth`
- 需求来源：FR-1、AC-3
- 设计来源：`DESIGN.md` 第 3.5、3.7、3.13 节
- 变更位置：`permission/*`、`config/security/ApiPermissionFilter.java`、`db/migration/V014__api_permission.sql`
- 前置依赖：TASK-013
- 输入：URI、HTTP Method、用户权限码
- 输出：API 权限点、角色权限关系、接口拦截器
- 实现步骤：创建权限表和角色权限表；定义权限码；实现 API 权限匹配；接入 Filter 和 `@PreAuthorize`。
- 测试策略：无权限访问管理接口返回 403，有权限放行。
- 测试文件：`ApiPermissionFilterTest.java`、`PermissionServiceTest.java`
- 测试命令：`mvn test -Dtest=ApiPermissionFilterTest,PermissionServiceTest`
- 预期 RED：权限拦截不存在，普通用户可访问管理接口。
- 预期 GREEN：无权限请求被拒绝并写审计。
- 验收标准：所有非公开 API 必须经过权限拦截。
- 风险与回滚：权限配置错误会误拦截，可保留平台管理员兜底权限。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：API URL RESTful；权限拒绝日志不泄露敏感参数。
- 状态：未开始

## TASK-015 数据权限拦截

- 所属模块：`permission`、`tenant`、`config`
- 需求来源：FR-1、AC-3
- 设计来源：`DESIGN.md` 第 3.8、3.17 节
- 变更位置：`config/mybatis/TenantLineConfig.java`、`permission/service/DataPermissionService.java`、`db/migration/V015__data_scope.sql`
- 前置依赖：TASK-014
- 输入：TenantContext、DataScope、Mapper 查询
- 输出：MySQL 查询强制 `tenant_id` 和数据范围过滤
- 实现步骤：启用 MyBatis-Plus 租户拦截器；定义白名单表；实现数据权限注入；添加危险 SQL 检查测试。
- 测试策略：跨租户数据不可查，白名单表可被平台管理员访问。
- 测试文件：`TenantLineInterceptorTest.java`、`DataPermissionServiceTest.java`
- 测试命令：`mvn test -Dtest=TenantLineInterceptorTest,DataPermissionServiceTest`
- 预期 RED：查询未带 tenant_id，可查到其他租户数据。
- 预期 GREEN：普通查询自动按租户隔离，越权详情被拒绝。
- 验收标准：业务表查询默认带租户条件。
- 风险与回滚：复杂 SQL 解析失败时必须 Service 层显式校验。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：所有业务表有 tenant_id；白名单表必须审查；禁止全表更新。
- 状态：未开始

## TASK-016 知识库权限模型

- 所属模块：`permission`、`knowledgebase`
- 需求来源：FR-1、FR-2、AC-3
- 设计来源：`DESIGN.md` 第 3.9、4.2 permission/knowledgebase 节；`DATA_MODEL.md` ENTITY-6
- 变更位置：`knowledgebase/dao.entity/KbPermission.java`、`permission/service/KnowledgeBasePermissionService.java`
- 前置依赖：TASK-015
- 输入：知识库 ID、授权主体、READ/WRITE/ADMIN
- 输出：知识库 ACL 表和权限校验 Service
- 实现步骤：创建 `kb_permission`；实现授权主体 USER/ROLE/DEPT；实现 READ/WRITE/ADMIN 校验；接入权限缓存。
- 测试策略：角色、部门、用户授权和权限撤销。
- 测试文件：`KnowledgeBasePermissionServiceTest.java`
- 测试命令：`mvn test -Dtest=KnowledgeBasePermissionServiceTest`
- 预期 RED：无法判断用户对知识库权限。
- 预期 GREEN：权限主体命中后可返回正确权限，撤销后立即失效。
- 验收标准：后续知识库列表、文档上传、RAG 问答都能复用 KB 权限。
- 风险与回滚：缓存不一致时以 DB 为准，并提供缓存刷新。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：权限唯一约束；权限变更写审计；默认拒绝未知权限。
- 状态：未开始

## TASK-017 文档权限模型

- 所属模块：`permission`、`document`
- 需求来源：FR-1、FR-3、AC-3
- 设计来源：`DESIGN.md` 第 3.10、3.12、4.2 document 节
- 变更位置：`document/dao.entity/DocPermission.java`、`permission/service/DocumentPermissionService.java`、`db/migration/V017__doc_permission.sql`
- 前置依赖：TASK-016
- 输入：文档 ID、权限模式、授权主体
- 输出：文档 ACL、继承知识库权限和文档级收紧能力
- 实现步骤：创建 `doc_permission`；实现 `INHERIT_KB/CUSTOM/PRIVATE`；实现文档 READ/WRITE 校验；定义权限版本号。
- 测试策略：继承、覆盖、私有文档、权限收紧后引用不可见。
- 测试文件：`DocumentPermissionServiceTest.java`
- 测试命令：`mvn test -Dtest=DocumentPermissionServiceTest`
- 预期 RED：所有 KB 可读用户都能看到私有文档。
- 预期 GREEN：文档级 ACL 可收紧访问范围。
- 验收标准：文档下载、删除、引用展示都可调用文档权限校验。
- 风险与回滚：ACL 过大时先 DB 二次校验，向量 payload 只保留权限版本。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：文档权限不能无审计扩大；接口不返回无权限文档字段。
- 状态：未开始

## TASK-018 检索权限过滤器

- 所属模块：`permission`、`retrieval`
- 需求来源：FR-1、FR-4、AC-3
- 设计来源：`DESIGN.md` 第 3.18、3.19、7 节
- 变更位置：`retrieval/service/RetrievalPermissionFilter.java`、`permission/service/PermissionService.java`
- 前置依赖：TASK-017
- 输入：候选 Chunk、用户上下文、可见知识库列表
- 输出：检索前范围、metadata filter、检索后二次校验结果
- 实现步骤：计算可见 KB 交集；构造向量和搜索 metadata filter；批量校验候选 `chunk_id`；默认拒绝异常。
- 测试策略：跨租户、跨角色、私有文档、权限服务异常。
- 测试文件：`RetrievalPermissionFilterTest.java`
- 测试命令：`mvn test -Dtest=RetrievalPermissionFilterTest`
- 预期 RED：检索候选包含无权限 Chunk。
- 预期 GREEN：无权限候选被过滤且不进入 Prompt。
- 验收标准：RAG 问答无法看到越权 Chunk 和引用。
- 风险与回滚：过滤过严导致召回为空时拒答，不降级为越权返回。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：权限异常默认拒绝；日志记录 filteredCount；不输出完整 ACL 给模型。
- 状态：未开始

## TASK-019 审计日志基础

- 所属模块：`audit`
- 需求来源：FR-1、FR-6、AC-5
- 设计来源：`DESIGN.md` 第 4.2 audit、11 节；`DATA_MODEL.md` ENTITY-16
- 变更位置：`audit/*`、`db/migration/V019__audit_log.sql`
- 前置依赖：TASK-018
- 输入：登录、权限拒绝、配置变更、文档操作事件
- 输出：`audit_log` 表、审计 Service、审计事件发布能力
- 实现步骤：创建审计表；实现审计记录 Service；封装审计事件 DTO；接入登录和权限拒绝场景。
- 测试策略：越权、登录失败、配置变更审计字段完整。
- 测试文件：`AuditLogServiceTest.java`
- 测试命令：`mvn test -Dtest=AuditLogServiceTest`
- 预期 RED：关键安全操作无审计记录。
- 预期 GREEN：审计日志包含 tenantId、userId、operation、resource、traceId。
- 验收标准：权限基础阶段形成可追溯安全闭环。
- 风险与回滚：审计异步失败不能影响主流程，但需补偿和告警。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：敏感信息脱敏；审计表索引完整；异常日志带 traceId。
- 状态：未开始

# 阶段 2：知识库和文档基础

## TASK-020 知识库 CRUD

- 所属模块：`knowledgebase`
- 需求来源：FR-2、API_SPEC API-2/3/4
- 设计来源：`DESIGN.md` 第 4.2 knowledgebase 节；`DATA_MODEL.md` ENTITY-5
- 变更位置：`knowledgebase/controller`、`knowledgebase/service`、`knowledgebase/dao`、`db/migration/V020__knowledge_base.sql`
- 前置依赖：TASK-019
- 输入：知识库名称、描述、分类、检索配置
- 输出：知识库创建、查询、更新、启停能力
- 实现步骤：创建知识库表；实现 CRUD Service；实现 REST API；接入租户和权限校验。
- 测试策略：名称唯一、分页查询、禁用状态、无权限访问。
- 测试文件：`KnowledgeBaseControllerTest.java`、`KnowledgeBaseServiceTest.java`
- 测试命令：`mvn test -Dtest=KnowledgeBaseControllerTest,KnowledgeBaseServiceTest`
- 预期 RED：知识库 API 不存在。
- 预期 GREEN：管理员可 CRUD，普通用户只看到授权知识库。
- 验收标准：知识库作为文档和检索配置聚合根可用。
- 风险与回滚：检索配置错误时保留默认配置并拒绝非法值。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：RESTful `/api/v1/knowledge-bases`；DTO 不返回 Entity；写操作事务。
- 状态：未开始

## TASK-021 知识库权限配置

- 所属模块：`knowledgebase`、`permission`
- 需求来源：FR-1、FR-2、AC-3
- 设计来源：`DESIGN.md` 第 3.9、4.2 knowledgebase 节
- 变更位置：`knowledgebase/controller/KnowledgeBasePermissionController.java`、`permission/service/KnowledgeBasePermissionService.java`
- 前置依赖：TASK-020
- 输入：知识库 ID、授权主体、权限类型
- 输出：知识库授权 API 和权限变更审计
- 实现步骤：实现授权接口；校验管理员权限；写入 `kb_permission`；刷新权限缓存和 permission revision。
- 测试策略：授权、撤销、越权授权、缓存刷新。
- 测试文件：`KnowledgeBasePermissionControllerTest.java`
- 测试命令：`mvn test -Dtest=KnowledgeBasePermissionControllerTest`
- 预期 RED：无法配置知识库 ACL。
- 预期 GREEN：授权后用户可见，撤销后不可见。
- 验收标准：知识库权限可配置且审计完整。
- 风险与回滚：误授权时支持禁用授权记录并刷新缓存。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：权限变更必须审计；普通用户不可授权；默认拒绝。
- 状态：未开始

## TASK-022 文档上传到 MinIO

- 所属模块：`document`
- 需求来源：FR-3、API_SPEC API-5
- 设计来源：`DESIGN.md` 第 6.2、6.3 节
- 变更位置：`document/controller/DocumentUploadController.java`、`document/service/DocumentStorageService.java`
- 前置依赖：TASK-021
- 输入：文件、knowledgeBaseId、permissionMode、tags
- 输出：MinIO 原文件、objectKey、上传审计
- 实现步骤：实现 multipart 接口；校验 KB WRITE；校验文件白名单和大小；生成 objectKey；上传 MinIO。
- 测试策略：合法文件、非法类型、超大文件、无权限上传。
- 测试文件：`DocumentUploadControllerTest.java`、`DocumentStorageServiceTest.java`
- 测试命令：`mvn test -Dtest=DocumentUploadControllerTest,DocumentStorageServiceTest`
- 预期 RED：上传接口不存在或不校验权限。
- 预期 GREEN：合法文件写入 MinIO，非法和越权被拒绝。
- 验收标准：原始文件安全存储，路径带租户和知识库。
- 风险与回滚：MinIO 成功后后续失败需清理对象或补偿。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：文件名日志脱敏；禁止真实密钥；上传接口鉴权。
- 状态：未开始

## TASK-023 文档元数据入 MySQL

- 所属模块：`document`
- 需求来源：FR-3、AC-2
- 设计来源：`DESIGN.md` 第 6.4 节；`DATA_MODEL.md` ENTITY-7
- 变更位置：`document/dao.entity/DocDocument.java`、`document/dao.mapper/DocDocumentMapper.java`、`db/migration/V023__doc_document.sql`
- 前置依赖：TASK-022
- 输入：上传文件信息、MinIO objectKey、checksum
- 输出：`doc_document` 记录，状态 `UPLOADED`
- 实现步骤：创建文档表；实现元数据保存；写入 checksum、版本、状态；处理 MinIO 成功 DB 失败补偿。
- 测试策略：元数据字段完整、checksum 唯一、事务回滚。
- 测试文件：`DocumentMetadataServiceTest.java`
- 测试命令：`mvn test -Dtest=DocumentMetadataServiceTest`
- 预期 RED：上传后无 DB 元数据。
- 预期 GREEN：上传后生成文档 ID，状态为 UPLOADED。
- 验收标准：文档可通过 ID 查询状态。
- 风险与回滚：DB 失败时删除 MinIO 对象，避免孤儿文件。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：字段 snake_case；状态枚举；写操作事务。
- 状态：未开始

## TASK-024 文档状态机

- 所属模块：`document`
- 需求来源：FR-3、AC-2
- 设计来源：`DESIGN.md` 第 6.12 节
- 变更位置：`document/service/DocumentStatusService.java`、`document/domain/DocumentStatusMachine.java`
- 前置依赖：TASK-023
- 输入：当前状态、目标状态、任务结果
- 输出：合法状态流转和失败原因记录
- 实现步骤：定义状态枚举；实现状态机校验；状态更新使用乐观锁；记录错误码和错误信息。
- 测试策略：合法流转、非法回退、并发更新、失败重试。
- 测试文件：`DocumentStatusMachineTest.java`
- 测试命令：`mvn test -Dtest=DocumentStatusMachineTest`
- 预期 RED：非法状态可任意更新。
- 预期 GREEN：状态只能按设计流转，失败可重试。
- 验收标准：文档生命周期可追踪、可恢复。
- 风险与回滚：状态写错会阻塞任务，需提供管理员重试和修正入口。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：状态必须枚举；状态变更日志记录 old/new；乐观锁。
- 状态：未开始

## TASK-025 文档解析任务投递 MQ

- 所属模块：`document`
- 需求来源：FR-3、非功能高可用
- 设计来源：`DESIGN.md` 第 6.5、6.12 节
- 变更位置：`document/service/DocumentTaskService.java`、`document/mq/DocumentTaskProducer.java`、`db/migration/V025__doc_process_task.sql`
- 前置依赖：TASK-024
- 输入：documentId、taskType、tenantId、traceId
- 输出：任务表记录和 RabbitMQ 消息
- 实现步骤：创建任务表；实现幂等任务号；上传后写任务并投递 MQ；投递失败可补偿扫描。
- 测试策略：重复投递幂等、MQ 失败补偿、消息 Header 完整。
- 测试文件：`DocumentTaskProducerTest.java`
- 测试命令：`mvn test -Dtest=DocumentTaskProducerTest`
- 预期 RED：上传后无解析任务。
- 预期 GREEN：任务被投递且带 tenantId/traceId。
- 验收标准：文档处理从同步上传解耦为异步任务。
- 风险与回滚：MQ 不可用时任务保留 PENDING，定时补偿。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：幂等键唯一；MQ 日志不打印原文；状态流转审计。
- 状态：未开始

## TASK-026 文档解析任务消费

- 所属模块：`parser`、`document`
- 需求来源：FR-3
- 设计来源：`DESIGN.md` 第 6.5、6.12 节
- 变更位置：`parser/mq/DocumentParseConsumer.java`、`parser/service/DocumentParseService.java`
- 前置依赖：TASK-025
- 输入：解析任务消息
- 输出：状态 `PARSING`、解析结果或失败记录
- 实现步骤：实现 MQ Consumer；恢复 TenantContext 和 TraceContext；加任务幂等锁；更新状态；调用解析 Service。
- 测试策略：重复消费、失败重试、上下文恢复、死信。
- 测试文件：`DocumentParseConsumerTest.java`
- 测试命令：`mvn test -Dtest=DocumentParseConsumerTest`
- 预期 RED：任务无人消费或上下文缺失。
- 预期 GREEN：任务消费后状态推进，失败记录明确。
- 验收标准：异步 Worker 可稳定消费解析任务。
- 风险与回滚：消费异常需 nack/retry，避免消息丢失。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：消费端清理 ThreadLocal；异常日志带任务 ID；不吞异常。
- 状态：未开始

## TASK-027 Spring AI 文档 ETL 接入

- 所属模块：`parser`
- 需求来源：FR-3、CLARIFICATION 第 4 条
- 设计来源：`DESIGN.md` 第 6.6 节
- 变更位置：`parser/service/SpringAiDocumentIngestionService.java`、`parser/service/impl`
- 前置依赖：TASK-026
- 输入：MinIO 文件流、文件类型、文档元数据
- 输出：Spring AI `Document` 列表和标准 metadata
- 实现步骤：封装 DocumentReader；接入 DocumentTransformer；标准化 metadata；输出统一解析 DTO。
- 测试策略：TXT/MD/PDF 示例解析、metadata 保留、空文档处理。
- 测试文件：`SpringAiDocumentIngestionServiceTest.java`
- 测试命令：`mvn test -Dtest=SpringAiDocumentIngestionServiceTest`
- 预期 RED：无法把文件转为 Spring AI Document。
- 预期 GREEN：解析输出包含正文、页码、标题和来源 metadata。
- 验收标准：文档解析链路以 Spring AI ETL 为统一入口。
- 风险与回滚：Spring AI 不支持格式时转入底层解析器适配。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：解析 Service interface + impl；日志记录耗时和字符数，不打印全文。
- 状态：未开始

## TASK-028 底层解析器适配

- 所属模块：`parser`
- 需求来源：FR-3
- 设计来源：`DESIGN.md` 第 6.6 节
- 变更位置：`parser/adapter/PdfDocumentReaderAdapter.java`、`OfficeDocumentReaderAdapter.java`、`ExcelDocumentReaderAdapter.java`
- 前置依赖：TASK-027
- 输入：PDF、DOCX、XLSX、HTML 等文件
- 输出：标准 ParsedDocumentDTO
- 实现步骤：定义 `DocumentParser` 接口；实现多格式适配器；按 contentType 选择解析器；保留页码、sheet、标题。
- 测试策略：不同格式样本、加密/损坏文件、表格元数据。
- 测试文件：`DocumentParserAdapterTest.java`
- 测试命令：`mvn test -Dtest=DocumentParserAdapterTest`
- 预期 RED：复杂格式无法解析。
- 预期 GREEN：支持格式输出标准 DTO，不支持格式给出明确错误。
- 验收标准：常见企业文档格式可进入统一 ETL。
- 风险与回滚：解析器异常隔离，不影响任务进程稳定。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：复杂逻辑有注释；异常不吞；错误码清晰。
- 状态：未开始

## TASK-029 文档清洗

- 所属模块：`parser`
- 需求来源：FR-3、非功能安全
- 设计来源：`DESIGN.md` 第 6.7 节
- 变更位置：`parser/service/DocumentCleanerService.java`、`parser/rule/*`
- 前置依赖：TASK-028
- 输入：解析文本和 metadata
- 输出：清洗后的 Document、敏感命中记录、清洗统计
- 实现步骤：实现空白和乱码清理；去页眉页脚；保留标题路径；敏感信息脱敏；记录清洗前后长度。
- 测试策略：页眉页脚、表格、敏感信息、乱码样本。
- 测试文件：`DocumentCleanerServiceTest.java`
- 测试命令：`mvn test -Dtest=DocumentCleanerServiceTest`
- 预期 RED：原始噪声直接进入 Chunk。
- 预期 GREEN：清洗结果保留语义且敏感字段被脱敏。
- 验收标准：进入 Chunk 的文本质量可控。
- 风险与回滚：清洗过度会丢失信息，规则需可配置和可关闭。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：敏感信息脱敏；规则命中日志不包含原文全量。
- 状态：未开始

# 阶段 3：Chunk、Embedding、索引

## TASK-030 Chunk 策略

- 所属模块：`chunk`
- 需求来源：FR-3、FR-4
- 设计来源：`DESIGN.md` 第 6.8、15.4 节
- 变更位置：`chunk/service/ChunkingStrategy.java`、`chunk/service/impl/TitleAwareChunkingStrategy.java`
- 前置依赖：TASK-029
- 输入：清洗后的文档、切片配置
- 输出：Chunk 列表、标题路径、页码、tokenCount
- 实现步骤：定义策略接口；实现标题感知切片；实现 token 长度和 overlap；支持 Parent-Child Chunk。
- 测试策略：长文档、短文档、标题层级、Overlap、表格文本。
- 测试文件：`ChunkingStrategyTest.java`
- 测试命令：`mvn test -Dtest=ChunkingStrategyTest`
- 预期 RED：无法生成满足长度和元数据要求的 Chunk。
- 预期 GREEN：Chunk 长度、Overlap、metadata 符合配置。
- 验收标准：切片结果可用于 Embedding 和引用。
- 风险与回滚：策略效果差时支持配置切换到固定长度策略。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：策略类单一职责；复杂边界注释；无魔法值。
- 状态：未开始

## TASK-031 Chunk 元数据入库

- 所属模块：`chunk`、`document`
- 需求来源：FR-3、AC-2
- 设计来源：`DESIGN.md` 第 5.4、6.8 节；`DATA_MODEL.md` ENTITY-8
- 变更位置：`chunk/dao.entity/DocChunk.java`、`chunk/service/ChunkService.java`、`db/migration/V031__doc_chunk.sql`
- 前置依赖：TASK-030
- 输入：Chunk 列表
- 输出：`doc_chunk` 记录和 chunk_id
- 实现步骤：创建表；批量保存 Chunk；写入 contentHash、pageNo、titlePath、permission metadata；保证重复处理幂等。
- 测试策略：批量保存、重复处理、版本隔离、软删除旧版本。
- 测试文件：`ChunkServiceTest.java`
- 测试命令：`mvn test -Dtest=ChunkServiceTest`
- 预期 RED：Chunk 只在内存中存在，无法追溯。
- 预期 GREEN：Chunk 持久化并可按文档版本查询。
- 验收标准：每个引用都能映射回 MySQL Chunk。
- 风险与回滚：Chunk 内容过大时后续冷热分离。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：联合唯一索引；分页排序；禁止 select *。
- 状态：未开始

## TASK-032 Spring AI Embedding 接入

- 所属模块：`embedding`、`llm`
- 需求来源：FR-3、FR-4
- 设计来源：`DESIGN.md` 第 6.9、8.3 节
- 变更位置：`embedding/service/EmbeddingService.java`、`llm/service/LlmGateway.java`
- 前置依赖：TASK-031
- 输入：文本列表、Embedding 模型配置
- 输出：Embedding 向量和维度信息
- 实现步骤：封装 Spring AI EmbeddingModel；支持通过 LLM Gateway 路由模型；校验向量维度；记录用量。
- 测试策略：Mock Embedding 模型、空文本、维度不匹配。
- 测试文件：`EmbeddingServiceTest.java`
- 测试命令：`mvn test -Dtest=EmbeddingServiceTest`
- 预期 RED：无法生成向量。
- 预期 GREEN：文本批次返回指定维度向量。
- 验收标准：Chunk 可进入向量化阶段。
- 风险与回滚：外部模型不可用时使用本地/mock 模型完成测试。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：模型调用不在业务代码散落；日志记录耗时和模型编码。
- 状态：未开始

## TASK-033 Embedding 批处理

- 所属模块：`embedding`
- 需求来源：FR-3、性能要求
- 设计来源：`DESIGN.md` 第 6.9、10.1 节
- 变更位置：`embedding/service/EmbeddingTaskService.java`、`embedding/service/EmbeddingRequestBatcher.java`
- 前置依赖：TASK-032
- 输入：待向量化 Chunk
- 输出：批量 Embedding 任务结果和 Token 用量
- 实现步骤：按 batchSize 分批；处理限流和重试；记录 token/cost；失败 Chunk 可重试。
- 测试策略：批量切分、部分失败、限流重试、空批次。
- 测试文件：`EmbeddingBatchServiceTest.java`
- 测试命令：`mvn test -Dtest=EmbeddingBatchServiceTest`
- 预期 RED：每个 Chunk 单独调用模型，无法限流重试。
- 预期 GREEN：批处理可控，失败可恢复。
- 验收标准：Embedding 不阻塞上传请求，成本可统计。
- 风险与回滚：批次过大导致供应商拒绝时自动降低 batchSize。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：外部调用有超时、日志、异常；敏感文本不完整打印。
- 状态：未开始

## TASK-034 Milvus / Qdrant VectorStore 接入

- 所属模块：`vector`
- 需求来源：FR-4、CLARIFICATION A-2
- 设计来源：`DESIGN.md` 第 5.2、6.10、10.5 节
- 变更位置：`vector/service/VectorStoreService.java`、`vector/adapter/MilvusVectorStoreAdapter.java`、`QdrantVectorStoreAdapter.java`
- 前置依赖：TASK-033
- 输入：Embedding 向量、Chunk metadata
- 输出：向量库 upsert/search/delete 能力
- 实现步骤：定义 VectorStoreService；实现 Milvus 或 Qdrant adapter；payload 写入权限元数据；保存 vectorId 回 MySQL。
- 测试策略：upsert、search、delete、metadata filter、维度错误。
- 测试文件：`VectorStoreServiceTest.java`
- 测试命令：`mvn test -Dtest=VectorStoreServiceTest`
- 预期 RED：无法写入或检索向量。
- 预期 GREEN：同租户授权 metadata 可检索，跨租户不可见。
- 验收标准：向量库成为语义检索主路径。
- 风险与回滚：Milvus/Qdrant 不可用时检索降级关键词搜索。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：向量 metadata 必须含 tenant_id；MySQL 不做向量检索。
- 状态：未开始

## TASK-035 OpenSearch 关键词索引

- 所属模块：`retrieval`、`chunk`
- 需求来源：FR-4
- 设计来源：`DESIGN.md` 第 5.3、6.11、10.4 节
- 变更位置：`retrieval/service/SearchIndexService.java`、`config/opensearch`
- 前置依赖：TASK-031
- 输入：Chunk 内容和 metadata
- 输出：OpenSearch 文档索引、关键词检索能力
- 实现步骤：定义 index mapping；批量写入 Chunk；实现关键词搜索；支持按文档删除索引。
- 测试策略：关键词命中、tenant filter、删除索引、高亮字段。
- 测试文件：`SearchIndexServiceTest.java`
- 测试命令：`mvn test -Dtest=SearchIndexServiceTest`
- 预期 RED：错误码、标题等精确查询无法召回。
- 预期 GREEN：BM25 可按权限 metadata 过滤召回。
- 验收标准：关键词检索可参与混合召回。
- 风险与回滚：OpenSearch 不可用时只走向量检索并记录降级。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：索引字段包含 tenant_id/status；日志不打印全文。
- 状态：未开始

## TASK-036 索引一致性和重建索引

- 所属模块：`document`、`chunk`、`vector`、`retrieval`
- 需求来源：FR-3、FR-4
- 设计来源：`DESIGN.md` 第 5.5、6.12、15.7 节
- 变更位置：`document/service/DocumentReindexService.java`、`vector/service/VectorIndexAdminService.java`、`retrieval/service/SearchIndexService.java`
- 前置依赖：TASK-034、TASK-035
- 输入：documentId、forceParse、reason
- 输出：重建索引任务、索引一致性检查报告
- 实现步骤：实现重建 API；删除旧索引或标记旧版本；重新 Embedding 和写索引；实现一致性巡检。
- 测试策略：重建成功、重建中状态冲突、向量成功搜索失败补偿。
- 测试文件：`DocumentReindexServiceTest.java`、`IndexConsistencyCheckTest.java`
- 测试命令：`mvn test -Dtest=DocumentReindexServiceTest,IndexConsistencyCheckTest`
- 预期 RED：模型或切片变更后无法重建索引。
- 预期 GREEN：重建后新版本生效，旧版本不可检索。
- 验收标准：文档更新、删除、重建具备一致性闭环。
- 风险与回滚：重建失败保留旧索引可继续服务，避免知识库不可用。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：重建幂等；状态机合法；操作审计。
- 状态：未开始

# 阶段 4：LLM Gateway

## TASK-040 模型配置表

- 所属模块：`llm`、`adminconfig`
- 需求来源：FR-6、API_SPEC API-14
- 设计来源：`DESIGN.md` 第 8、4.2 llm gateway 节；`DATA_MODEL.md` ENTITY-12
- 变更位置：`llm/dao.entity/LlmModelConfig.java`、`adminconfig/controller/ModelConfigController.java`、`db/migration/V040__llm_model_config.sql`
- 前置依赖：TASK-019
- 输入：provider、modelCode、modelType、endpoint、timeout、priceConfig
- 输出：模型配置 CRUD 和状态管理
- 实现步骤：创建模型配置表；实现 ModelConfigService；校验 modelCode 唯一；支持 CHAT/EMBEDDING/RERANK。
- 测试策略：模型编码唯一、状态启停、租户覆盖全局默认。
- 测试文件：`ModelConfigServiceTest.java`
- 测试命令：`mvn test -Dtest=ModelConfigServiceTest`
- 预期 RED：模型配置无法持久化。
- 预期 GREEN：可按租户和模型类型解析有效配置。
- 验收标准：Gateway 可从配置表获取模型路由基础数据。
- 风险与回滚：错误配置可禁用并回退默认模型。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：价格字段 decimal/json；API 仅管理员；配置变更审计。
- 状态：未开始

## TASK-041 API Key 加密

- 所属模块：`llm`、`common`
- 需求来源：FR-6、安全要求
- 设计来源：`DESIGN.md` 第 8.9、9 节
- 变更位置：`common/crypto`、`llm/service/ApiKeyCryptoService.java`
- 前置依赖：TASK-040
- 输入：API Key 明文、主密钥配置
- 输出：API Key 密文、指纹、解密能力
- 实现步骤：实现加密/解密 Service；保存密文和 key 指纹；日志脱敏；支持密钥轮换预留。
- 测试策略：加解密、错误密钥、日志脱敏、密文不可等于明文。
- 测试文件：`ApiKeyCryptoServiceTest.java`
- 测试命令：`mvn test -Dtest=ApiKeyCryptoServiceTest`
- 预期 RED：API Key 明文入库或日志可见。
- 预期 GREEN：只保存密文，日志只显示指纹。
- 验收标准：模型供应商密钥安全托管。
- 风险与回滚：主密钥丢失无法解密，需运维备份和轮换方案。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：禁止打印 API Key；密钥字段不进入响应 DTO。
- 状态：未开始

## TASK-042 Spring AI ChatClient 封装

- 所属模块：`llm`
- 需求来源：FR-4、FR-6
- 设计来源：`DESIGN.md` 第 8.3、8.8、14.4 节
- 变更位置：`llm/service/LlmGateway.java`、`llm/service/impl/SpringAiLlmGatewayImpl.java`
- 前置依赖：TASK-041
- 输入：ChatCompletionReqDTO、模型配置
- 输出：统一 Chat 和 StreamChat 结果
- 实现步骤：封装 Spring AI ChatClient；统一消息结构；适配流式回调；标准化错误和 usage。
- 测试策略：Mock ChatClient 成功、失败、流式 delta、usage 返回。
- 测试文件：`SpringAiLlmGatewayTest.java`
- 测试命令：`mvn test -Dtest=SpringAiLlmGatewayTest`
- 预期 RED：业务无法通过统一 Gateway 调 Chat。
- 预期 GREEN：Chat 和 streamChat 输出统一 DTO。
- 验收标准：业务模块不直接依赖供应商 SDK。
- 风险与回滚：Spring AI 不支持的供应商由自定义 Adapter 扩展。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：模型调用集中在 llm；外部调用日志记录耗时和异常。
- 状态：未开始

## TASK-043 多模型供应商适配

- 所属模块：`llm`
- 需求来源：FR-6、CLARIFICATION 第 5 条
- 设计来源：`DESIGN.md` 第 8.2 节
- 变更位置：`llm/adapter/OpenAiAdapter.java`、`AzureOpenAiAdapter.java`、`DeepSeekAdapter.java`、`QwenAdapter.java`、`ZhipuAdapter.java`、`LocalModelAdapter.java`
- 前置依赖：TASK-042
- 输入：provider、modelType、请求 DTO
- 输出：统一供应商适配层
- 实现步骤：定义 `ModelProviderAdapter`；实现 OpenAI-compatible 适配；扩展 Azure、DeepSeek、通义、智谱、本地模型；统一异常。
- 测试策略：supports 匹配、请求转换、错误转换、mock 响应。
- 测试文件：`ModelProviderAdapterTest.java`
- 测试命令：`mvn test -Dtest=ModelProviderAdapterTest`
- 预期 RED：只能调用单一模型。
- 预期 GREEN：不同 provider 返回统一结果。
- 验收标准：模型供应商可插拔。
- 风险与回滚：先实现 OpenAI-compatible，其他供应商逐步接入。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：供应商差异不污染业务 Service；异常清晰。
- 状态：未开始

## TASK-044 模型路由

- 所属模块：`llm`
- 需求来源：FR-6、性能和成本要求
- 设计来源：`DESIGN.md` 第 8.5 节
- 变更位置：`llm/service/ModelRouter.java`、`llm/domain/ModelRoutePolicy.java`
- 前置依赖：TASK-043
- 输入：tenantId、operationType、modelCode、路由策略
- 输出：目标模型配置和备用模型
- 实现步骤：实现租户默认模型；支持质量/成本/延迟策略；支持主备模型；记录路由原因。
- 测试策略：租户覆盖全局、模型禁用、主备切换。
- 测试文件：`ModelRouterTest.java`
- 测试命令：`mvn test -Dtest=ModelRouterTest`
- 预期 RED：所有调用固定一个模型。
- 预期 GREEN：按租户和操作类型选择模型。
- 验收标准：Chat、Embedding、Rerank 可独立路由。
- 风险与回滚：路由错误时可禁用租户配置回退全局默认。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：路由日志不含密钥；状态字段枚举。
- 状态：未开始

## TASK-045 超时、重试、熔断、降级

- 所属模块：`llm`
- 需求来源：非功能高可用、安全成本要求
- 设计来源：`DESIGN.md` 第 8.6 节
- 变更位置：`llm/resilience`、`llm/service/impl/LlmGatewayResilienceDecorator.java`
- 前置依赖：TASK-044
- 输入：模型调用请求、模型配置中的 timeout/retry/fallback
- 输出：可控的超时、重试、熔断和降级行为
- 实现步骤：配置超时；对可重试错误重试；熔断失败模型；降级到备用模型或关闭 Rerank。
- 测试策略：超时、429、5xx、熔断打开、备用模型成功。
- 测试文件：`LlmGatewayResilienceTest.java`
- 测试命令：`mvn test -Dtest=LlmGatewayResilienceTest`
- 预期 RED：供应商超时拖垮请求。
- 预期 GREEN：超时受控，降级路径明确。
- 验收标准：模型故障不导致系统级雪崩。
- 风险与回滚：重试可能增加成本，按错误类型限制。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：外部调用异常不吞；日志有 provider/model/costMs。
- 状态：未开始

## TASK-046 Token 和成本统计

- 所属模块：`llm`、`statistics`
- 需求来源：FR-6、AC-5
- 设计来源：`DESIGN.md` 第 8.7、11 节；`DATA_MODEL.md` ENTITY-14
- 变更位置：`statistics/service/TokenUsageService.java`、`llm/dao.entity/LlmTokenUsage.java`
- 前置依赖：TASK-045
- 输入：模型 usage、价格配置、上下文 ID
- 输出：Token 用量和成本记录
- 实现步骤：创建用量表；计算成本；记录 chat/embedding/rerank 用量；支持估算标记。
- 测试策略：成本 decimal 计算、缺失 usage 估算、按租户统计。
- 测试文件：`TokenUsageServiceTest.java`
- 测试命令：`mvn test -Dtest=TokenUsageServiceTest`
- 预期 RED：模型调用后无用量记录。
- 预期 GREEN：用量可按租户、用户、模型统计。
- 验收标准：Token 和成本治理可用。
- 风险与回滚：供应商计量差异时保留 raw usage 和估算标记。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：金额使用 decimal；统计接口分页排序；敏感上下文不入库。
- 状态：未开始

## TASK-047 流式输出适配

- 所属模块：`llm`、`chat`
- 需求来源：FR-4、API_SPEC API-10
- 设计来源：`DESIGN.md` 第 8.8、7.2 节
- 变更位置：`llm/stream`、`chat/controller/ChatStreamController.java`
- 前置依赖：TASK-046
- 输入：streamChat 请求、供应商 delta
- 输出：统一 SSE 事件
- 实现步骤：定义 StreamCallback；供应商 delta 转内部事件；处理客户端断开；结束帧输出 citation/usage。
- 测试策略：正常流、异常流、客户端断开、结束帧完整。
- 测试文件：`StreamChatAdapterTest.java`
- 测试命令：`mvn test -Dtest=StreamChatAdapterTest`
- 预期 RED：流式协议不统一或无法落库 usage。
- 预期 GREEN：SSE 输出 `message_start/answer_delta/usage/message_end`。
- 验收标准：前端可稳定消费流式回答。
- 风险与回滚：流式异常时降级为非流式或返回错误帧。
- 适用 .ai_rules 文件：README、API、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：SSE 错误清晰；断开后释放资源；不输出隐藏策略。
- 状态：未开始

# 阶段 5：RAG 问答

## TASK-050 会话管理

- 所属模块：`chat`
- 需求来源：FR-4、API_SPEC API-11/12
- 设计来源：`DESIGN.md` 第 4.2 chat、7 节；`DATA_MODEL.md` ENTITY-9
- 变更位置：`chat/dao.entity/ChatSession.java`、`chat/service/ChatSessionService.java`、`db/migration/V050__chat_session.sql`
- 前置依赖：TASK-019
- 输入：用户、知识库范围、会话标题
- 输出：会话创建、列表、详情、归档
- 实现步骤：创建会话表；实现会话 Service；校验会话归属；支持会话摘要字段。
- 测试策略：用户只能看自己会话，管理员按权限查看。
- 测试文件：`ChatSessionServiceTest.java`
- 测试命令：`mvn test -Dtest=ChatSessionServiceTest`
- 预期 RED：会话无法保存或越权查看。
- 预期 GREEN：会话按租户和用户隔离。
- 验收标准：问答有稳定会话上下文。
- 风险与回滚：会话摘要错误时不影响单轮问答。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：会话查询带 tenant_id/user_id；接口返回 DTO。
- 状态：未开始

## TASK-051 用户消息保存

- 所属模块：`chat`
- 需求来源：FR-4
- 设计来源：`DESIGN.md` 第 7.2 节；`DATA_MODEL.md` ENTITY-10
- 变更位置：`chat/dao.entity/ChatMessage.java`、`chat/service/ChatMessageService.java`、`db/migration/V051__chat_message.sql`
- 前置依赖：TASK-050
- 输入：用户问题、改写问题、回答、状态
- 输出：消息记录和状态流转
- 实现步骤：创建消息表；实现保存用户消息；实现助手消息状态 `PROCESSING/SUCCESS/FAILED/REFUSED`；关联 traceId。
- 测试策略：消息保存、状态更新、失败记录、分页查询。
- 测试文件：`ChatMessageServiceTest.java`
- 测试命令：`mvn test -Dtest=ChatMessageServiceTest`
- 预期 RED：问答过程不可追溯。
- 预期 GREEN：每次提问和回答都有 messageId 和状态。
- 验收标准：后续引用、反馈、Token 统计可关联消息。
- 风险与回滚：流式中断时消息标记 FAILED/CANCELED。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：问题日志 hash 化或脱敏；状态变更记录 old/new。
- 状态：未开始

## TASK-052 Query Rewrite

- 所属模块：`chat`、`prompt`、`llm`
- 需求来源：FR-4、PROMPT_SPEC
- 设计来源：`DESIGN.md` 第 7.2 节；`PROMPT_SPEC.md`
- 变更位置：`chat/service/QueryRewriteService.java`、`prompt/template/query_rewrite`
- 前置依赖：TASK-047、TASK-051
- 输入：当前问题、会话摘要、最近消息
- 输出：独立问题和意图标签
- 实现步骤：定义 Query Rewrite Prompt；通过 LLM Gateway 调用；保存 rewrittenQuestion；失败时回退原问题。
- 测试策略：多轮追问、单轮问题、Prompt Injection 输入、模型失败。
- 测试文件：`QueryRewriteServiceTest.java`
- 测试命令：`mvn test -Dtest=QueryRewriteServiceTest`
- 预期 RED：追问无法改写为独立问题。
- 预期 GREEN：改写结果不覆盖安全规则，失败可回退。
- 验收标准：多轮问答检索上下文更准确。
- 风险与回滚：Rewrite 增加成本，可按租户配置关闭。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：Prompt 版本化；用户输入不能覆盖系统规则；Token 记录。
- 状态：未开始

## TASK-053 向量检索

- 所属模块：`retrieval`、`vector`
- 需求来源：FR-4
- 设计来源：`DESIGN.md` 第 7.2、10.5 节
- 变更位置：`retrieval/service/VectorRetriever.java`
- 前置依赖：TASK-034、TASK-052
- 输入：query vector、metadata filter、topN
- 输出：向量候选 Chunk
- 实现步骤：调用 VectorStoreService search；传入 tenant/KB/status/ACL filter；转换结果 DTO。
- 测试策略：正常召回、跨租户过滤、向量库异常。
- 测试文件：`VectorRetrieverTest.java`
- 测试命令：`mvn test -Dtest=VectorRetrieverTest`
- 预期 RED：语义相似问题无法召回。
- 预期 GREEN：返回同租户授权候选。
- 验收标准：语义召回可用于混合检索。
- 风险与回滚：向量库失败时降级关键词检索。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：filter 必带 tenant_id；日志记录 topN/costMs。
- 状态：未开始

## TASK-054 关键词检索

- 所属模块：`retrieval`
- 需求来源：FR-4
- 设计来源：`DESIGN.md` 第 5.3、7.2 节
- 变更位置：`retrieval/service/KeywordRetriever.java`
- 前置依赖：TASK-035、TASK-052
- 输入：rewrittenQuestion、metadata filter、topN
- 输出：BM25 候选 Chunk
- 实现步骤：构造 OpenSearch 查询；加入 tenant/KB/status/ACL filter；返回高亮和 score。
- 测试策略：错误码、接口名、标题关键词、权限过滤。
- 测试文件：`KeywordRetrieverTest.java`
- 测试命令：`mvn test -Dtest=KeywordRetrieverTest`
- 预期 RED：精确关键词无法召回。
- 预期 GREEN：关键词召回可按权限过滤。
- 验收标准：关键词结果进入混合召回。
- 风险与回滚：搜索引擎失败时只走向量召回。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：查询日志不打印全文；filter 必带 tenant_id。
- 状态：未开始

## TASK-055 混合检索

- 所属模块：`retrieval`
- 需求来源：FR-4
- 设计来源：`DESIGN.md` 第 7.2、15.1 节
- 变更位置：`retrieval/service/HybridRetriever.java`、`retrieval/service/RetrievalService.java`
- 前置依赖：TASK-053、TASK-054
- 输入：向量候选、关键词候选、合并策略
- 输出：去重后的候选 TopN
- 实现步骤：实现 RRF/加权合并；按 chunkId 去重；保留来源分数；记录召回统计。
- 测试策略：重复 Chunk、单路失败、RRF 排序、TopN 截断。
- 测试文件：`HybridRetrieverTest.java`
- 测试命令：`mvn test -Dtest=HybridRetrieverTest`
- 预期 RED：向量和关键词结果无法合并。
- 预期 GREEN：混合结果稳定排序并去重。
- 验收标准：RetrievalService 输出授权候选 TopN。
- 风险与回滚：合并策略效果差时配置权重或关闭一路召回。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：策略参数配置化；无魔法值；日志有 candidateCount。
- 状态：未开始

## TASK-056 权限过滤二次校验

- 所属模块：`retrieval`、`permission`
- 需求来源：FR-1、FR-4、AC-3
- 设计来源：`DESIGN.md` 第 3.19、7.2 节
- 变更位置：`retrieval/service/RetrievalPermissionFilter.java`
- 前置依赖：TASK-055
- 输入：混合候选 Chunk ID
- 输出：二次校验后的授权候选
- 实现步骤：批量查询 Chunk 和文档权限；剔除无权限、已删除、版本失效候选；记录 filteredCount。
- 测试策略：候选中混入跨租户、私有文档、已删除文档。
- 测试文件：`RetrievalPermissionFilterIntegrationTest.java`
- 测试命令：`mvn test -Dtest=RetrievalPermissionFilterIntegrationTest`
- 预期 RED：无权限候选进入 Prompt。
- 预期 GREEN：Prompt 上下文只包含授权 Chunk。
- 验收标准：越权检索风险闭环。
- 风险与回滚：权限异常时返回空候选并拒答。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：默认拒绝；权限过滤日志脱敏；不得暴露 ACL 细节给用户。
- 状态：未开始

## TASK-057 Rerank

- 所属模块：`rerank`
- 需求来源：FR-4
- 设计来源：`DESIGN.md` 第 4.2 rerank、7.2、8.3 节
- 变更位置：`rerank/service/RerankService.java`
- 前置依赖：TASK-056、TASK-045
- 输入：query、授权候选 Chunk
- 输出：重排后的 TopK
- 实现步骤：调用 LLM Gateway Rerank；归一化分数；设置阈值；失败降级原排序。
- 测试策略：排序变化、超时降级、候选为空、分数阈值。
- 测试文件：`RerankServiceTest.java`
- 测试命令：`mvn test -Dtest=RerankServiceTest`
- 预期 RED：候选噪声无法重排。
- 预期 GREEN：Rerank 后相关片段靠前，失败可降级。
- 验收标准：RAG Prompt 使用高质量 TopK。
- 风险与回滚：Rerank 成本高，可按知识库配置关闭。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：只对授权候选重排；Token 统计；超时处理。
- 状态：未开始

## TASK-058 Prompt 模板渲染

- 所属模块：`prompt`
- 需求来源：FR-4、PROMPT_SPEC
- 设计来源：`DESIGN.md` 第 4.2 prompt、8.4 节；`PROMPT_SPEC.md`
- 变更位置：`prompt/service/PromptTemplateService.java`、`prompt/service/PromptRenderService.java`
- 前置依赖：TASK-057
- 输入：Prompt code、变量、授权上下文
- 输出：渲染后的 Prompt messages
- 实现步骤：保存 RAG Prompt 模板；校验变量；渲染引用上下文；注入安全规则和输出格式。
- 测试策略：变量缺失、模板版本、Prompt Injection 上下文、拒答模板。
- 测试文件：`PromptRenderServiceTest.java`
- 测试命令：`mvn test -Dtest=PromptRenderServiceTest`
- 预期 RED：Prompt 硬编码在 ChatService 或变量缺失。
- 预期 GREEN：模板配置化、版本化、变量校验通过。
- 验收标准：Chat 模块不拼接大段 Prompt。
- 风险与回滚：模板错误时回滚到上一 ACTIVE 版本。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：Prompt 不散落业务代码；版本不可覆盖；审计变更。
- 状态：未开始

## TASK-059 非流式问答

- 所属模块：`chat`
- 需求来源：FR-4、API_SPEC API-9
- 设计来源：`DESIGN.md` 第 7.2、14.3 节
- 变更位置：`chat/controller/RagChatController.java`、`chat/service/RagChatService.java`
- 前置依赖：TASK-058、TASK-047
- 输入：ChatAskReqDTO
- 输出：答案、引用、置信度、Token、成本、traceId
- 实现步骤：编排会话、Rewrite、Retrieval、Rerank、Prompt、Gateway；保存回答和引用；返回统一 DTO。
- 测试策略：有答案、无答案、无权限、模型失败。
- 测试文件：`RagChatServiceTest.java`、`RagChatControllerTest.java`
- 测试命令：`mvn test -Dtest=RagChatServiceTest,RagChatControllerTest`
- 预期 RED：问答接口无法返回引用答案。
- 预期 GREEN：基于授权 Chunk 回答并保存消息。
- 验收标准：非流式 RAG 问答闭环。
- 风险与回滚：模型失败返回降级错误，不伪造答案。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：Controller 不直接编排复杂流程；引用必须权限校验。
- 状态：未开始

## TASK-060 流式问答

- 所属模块：`chat`
- 需求来源：FR-4、API_SPEC API-10
- 设计来源：`DESIGN.md` 第 7.2、8.8、15.9 节
- 变更位置：`chat/controller/RagChatStreamController.java`、`chat/service/RagChatService.java`
- 前置依赖：TASK-059
- 输入：ChatStreamReqDTO
- 输出：SSE 分片、结束帧、引用、usage
- 实现步骤：复用 RAG 编排；调用 streamChat；缓存 delta；结束后保存完整答案和引用；处理断开。
- 测试策略：正常流、模型异常、客户端断开、结束帧包含 usage。
- 测试文件：`RagChatStreamControllerTest.java`
- 测试命令：`mvn test -Dtest=RagChatStreamControllerTest`
- 预期 RED：流式响应缺失或中断后状态错误。
- 预期 GREEN：SSE 协议稳定，消息最终状态正确。
- 验收标准：用户可获得低体感延迟回答。
- 风险与回滚：流式不可用时保留非流式接口。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：断开释放资源；异常返回 error 事件；日志脱敏。
- 状态：未开始

## TASK-061 引用来源保存

- 所属模块：`chat`
- 需求来源：FR-4、PROMPT_SPEC 输出契约
- 设计来源：`DESIGN.md` 第 3.12、7.2 节；`DATA_MODEL.md` ENTITY-11
- 变更位置：`chat/dao.entity/ChatCitation.java`、`chat/service/ChatCitationService.java`、`db/migration/V061__chat_citation.sql`
- 前置依赖：TASK-059
- 输入：TopK Chunk、答案引用编号、messageId
- 输出：引用来源快照和展示 DTO
- 实现步骤：创建引用表；保存 documentId/chunkId/pageNo/quote/score；展示前二次权限校验。
- 测试策略：引用保存、文档权限收紧后不可见、删除文档后展示策略。
- 测试文件：`ChatCitationServiceTest.java`
- 测试命令：`mvn test -Dtest=ChatCitationServiceTest`
- 预期 RED：答案没有可追溯引用。
- 预期 GREEN：引用能追溯到授权文档和 Chunk。
- 验收标准：回答关键结论可追溯。
- 风险与回滚：权限收紧后只展示不可访问提示和脱敏快照。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：引用返回前校验权限；quote 脱敏；索引完整。
- 状态：未开始

## TASK-062 无答案拒答

- 所属模块：`chat`、`prompt`
- 需求来源：FR-4、PROMPT_SPEC
- 设计来源：`DESIGN.md` 第 7.3、9 节；`PROMPT_SPEC.md`
- 变更位置：`chat/service/NoAnswerPolicy.java`、`prompt/templates/rag_qa_answer_v1`
- 前置依赖：TASK-061
- 输入：检索结果、Rerank 分数、权限过滤结果、敏感意图
- 输出：拒答答案和拒答原因
- 实现步骤：定义拒答原因枚举；实现阈值判断；Prompt 失败处理；保存 REFUSED 消息。
- 测试策略：无召回、低分、越权、Prompt Injection、敏感问题。
- 测试文件：`NoAnswerPolicyTest.java`
- 测试命令：`mvn test -Dtest=NoAnswerPolicyTest`
- 预期 RED：无上下文时模型编造答案。
- 预期 GREEN：触发条件下返回标准拒答并记录原因。
- 验收标准：模型不能在无授权上下文时回答。
- 风险与回滚：拒答过严时调整阈值，不降低权限规则。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：拒答原因可审计；不得泄露权限细节；Prompt Gate 通过。
- 状态：未开始

# 阶段 6：反馈、评估、可观测

## TASK-070 用户反馈

- 所属模块：`feedback`
- 需求来源：FR-5、API_SPEC API-13
- 设计来源：`DESIGN.md` 第 4.2 feedback 节；`DATA_MODEL.md` ENTITY-15
- 变更位置：`feedback/controller/FeedbackController.java`、`feedback/service/FeedbackService.java`、`db/migration/V070__qa_feedback.sql`
- 前置依赖：TASK-061
- 输入：messageId、feedbackType、score、reasonTags、comment
- 输出：反馈记录
- 实现步骤：创建反馈表；实现反馈接口；校验消息可见性；评论脱敏；支持重复反馈策略。
- 测试策略：自己消息、他人消息、评分范围、敏感评论。
- 测试文件：`FeedbackServiceTest.java`、`FeedbackControllerTest.java`
- 测试命令：`mvn test -Dtest=FeedbackServiceTest,FeedbackControllerTest`
- 预期 RED：用户无法反馈答案质量。
- 预期 GREEN：授权用户可反馈，越权反馈被拒绝。
- 验收标准：反馈进入质量闭环。
- 风险与回滚：恶意刷反馈时按用户限流。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：评论脱敏；接口鉴权；分页排序。
- 状态：未开始

## TASK-071 评测集

- 所属模块：`evaluation`
- 需求来源：FR-5、PROMPT_SPEC 评估方案
- 设计来源：`DESIGN.md` 第 4.2 evaluation 节；`PROMPT_SPEC.md`
- 变更位置：`evaluation/*`、`db/migration/V071__evaluation_dataset.sql`
- 前置依赖：TASK-070
- 输入：Golden Case、问题、期望答案、期望引用
- 输出：评测集、评测用例 CRUD
- 实现步骤：创建评测集表；实现用例导入和查询；支持租户隔离和脱敏字段。
- 测试策略：导入、重复用例、跨租户隔离、脱敏。
- 测试文件：`EvaluationDatasetServiceTest.java`
- 测试命令：`mvn test -Dtest=EvaluationDatasetServiceTest`
- 预期 RED：Prompt 和检索效果无法回归。
- 预期 GREEN：评测用例可维护并按租户隔离。
- 验收标准：质量评估有稳定样本来源。
- 风险与回滚：评测集包含敏感数据时必须脱敏或限制访问。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：评测数据 tenant_id；敏感内容脱敏；管理员权限。
- 状态：未开始

## TASK-072 答案质量评分

- 所属模块：`evaluation`
- 需求来源：FR-5
- 设计来源：`DESIGN.md` 第 4.2 evaluation、11 节
- 变更位置：`evaluation/service/AnswerScoringService.java`、`evaluation/service/EvaluationRunner.java`
- 前置依赖：TASK-071、TASK-059
- 输入：评测用例、RAG 输出、引用
- 输出：正确性、引用准确性、拒答准确性评分
- 实现步骤：实现评测任务；调用 RAG 非流式问答；比对期望引用；生成评分结果。
- 测试策略：正确答案、错误引用、无答案拒答、Prompt Injection。
- 测试文件：`AnswerScoringServiceTest.java`
- 测试命令：`mvn test -Dtest=AnswerScoringServiceTest`
- 预期 RED：无法量化答案质量。
- 预期 GREEN：评测运行产出评分和失败原因。
- 验收标准：Prompt 和检索调整可回归验证。
- 风险与回滚：模型评分不稳定时以规则评分和人工复核为准。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：评测任务状态机；成本记录；不泄露敏感样本。
- 状态：未开始

## TASK-073 Token 统计报表

- 所属模块：`statistics`
- 需求来源：FR-6、API_SPEC API-16
- 设计来源：`DESIGN.md` 第 4.2 statistics、11 节
- 变更位置：`statistics/controller/TokenUsageController.java`、`statistics/service/TokenUsageReportService.java`
- 前置依赖：TASK-046
- 输入：时间范围、tenantId、userId、modelCode、groupBy
- 输出：Token 统计分页/汇总报表
- 实现步骤：实现查询 DTO；按权限限制范围；聚合 prompt/completion/total tokens；返回分页和汇总。
- 测试策略：普通用户、租户管理员、平台管理员统计范围。
- 测试文件：`TokenUsageReportServiceTest.java`
- 测试命令：`mvn test -Dtest=TokenUsageReportServiceTest`
- 预期 RED：Token 用量无法查询。
- 预期 GREEN：按租户、用户、模型统计准确。
- 验收标准：租户能监控 Token 消耗。
- 风险与回滚：大表查询慢时引入日聚合表。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：分页排序；金额/Token 统计权限；禁止跨租户。
- 状态：未开始

## TASK-074 成本统计报表

- 所属模块：`statistics`
- 需求来源：FR-6、非功能成本控制
- 设计来源：`DESIGN.md` 第 8.7、10、11 节
- 变更位置：`statistics/controller/CostReportController.java`、`statistics/service/CostStatisticsService.java`
- 前置依赖：TASK-073
- 输入：Token usage、priceConfig、时间范围
- 输出：成本报表、预算使用率
- 实现步骤：按模型价格计算成本；按租户/用户/模型聚合；输出预算告警字段。
- 测试策略：decimal 精度、缺失价格、预算阈值、权限范围。
- 测试文件：`CostStatisticsServiceTest.java`
- 测试命令：`mvn test -Dtest=CostStatisticsServiceTest`
- 预期 RED：无法统计成本。
- 预期 GREEN：成本按模型和租户准确聚合。
- 验收标准：管理员可看成本和预算风险。
- 风险与回滚：供应商价格变更需版本化价格配置。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：成本使用 decimal；查询权限；错误信息清晰。
- 状态：未开始

## TASK-075 结构化日志

- 所属模块：`observability`
- 需求来源：AC-5、LOGGING_STYLE
- 设计来源：`DESIGN.md` 第 11.1 节
- 变更位置：`config/observability/LoggingConfig.java`、`common/context`
- 前置依赖：TASK-002
- 输入：MDC 上下文、业务日志
- 输出：结构化 JSON 日志和字段规范
- 实现步骤：配置日志 encoder；统一 MDC 字段；封装日志工具或切面；敏感字段脱敏。
- 测试策略：日志字段存在、敏感值脱敏、ThreadLocal 清理。
- 测试文件：`StructuredLoggingTest.java`
- 测试命令：`mvn test -Dtest=StructuredLoggingTest`
- 预期 RED：日志无 traceId/tenantId 或泄露敏感字段。
- 预期 GREEN：关键日志字段完整且脱敏。
- 验收标准：排障和审计有统一日志基础。
- 风险与回滚：日志过量时调整级别和采样。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：禁止 System.out；禁止打印密码/Token/API Key；异常日志带对象。
- 状态：未开始

## TASK-076 Prometheus 指标

- 所属模块：`observability`
- 需求来源：可观测性要求
- 设计来源：`DESIGN.md` 第 11.2 节
- 变更位置：`observability/metrics`、`config/observability/MetricsConfig.java`
- 前置依赖：TASK-075
- 输入：API、模型、检索、文档处理事件
- 输出：Prometheus 指标
- 实现步骤：接入 Micrometer；定义文档、检索、模型、反馈指标；配置 Actuator 暴露。
- 测试策略：指标注册、标签基数、关键指标递增。
- 测试文件：`MetricsRecorderTest.java`
- 测试命令：`mvn test -Dtest=MetricsRecorderTest`
- 预期 RED：无业务指标。
- 预期 GREEN：关键操作产生指标。
- 验收标准：Grafana 可基于指标构建看板。
- 风险与回滚：标签过细导致高基数，限制 question/content 等标签。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：指标标签不含敏感原文；命名清晰。
- 状态：未开始

## TASK-077 OpenTelemetry Trace

- 所属模块：`observability`
- 需求来源：可观测性要求
- 设计来源：`DESIGN.md` 第 11.3 节
- 变更位置：`config/observability/OpenTelemetryConfig.java`、`common/context/MdcTaskDecorator.java`
- 前置依赖：TASK-076
- 输入：HTTP 请求、MQ 消息、模型调用
- 输出：跨 API、DB、MQ、模型的 trace
- 实现步骤：接入 OTel；HTTP 自动埋点；MQ 传递 trace context；外部调用创建 span。
- 测试策略：traceId 贯穿 API 到 MQ，异步线程 MDC 不丢失。
- 测试文件：`OpenTelemetryTraceTest.java`
- 测试命令：`mvn test -Dtest=OpenTelemetryTraceTest`
- 预期 RED：异步任务和模型调用 trace 断链。
- 预期 GREEN：一个 traceId 串联完整链路。
- 验收标准：问题可从用户请求追到模型和索引调用。
- 风险与回滚：采样率过高增加开销，生产按比例采样。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：Trace 不带敏感正文；上下文 finally 清理。
- 状态：未开始

## TASK-078 告警策略

- 所属模块：`observability`、`adminconfig`
- 需求来源：非功能可观测性和高可用
- 设计来源：`DESIGN.md` 第 11.4 节
- 变更位置：`observability/alert`、`deploy/prometheus/alert-rules.yml`
- 前置依赖：TASK-076、TASK-077
- 输入：指标阈值、租户预算、错误率
- 输出：Prometheus 告警规则和告警配置
- 实现步骤：定义模型失败率、任务积压、成本预算、越权异常、P95 延迟告警；编写规则文件；记录处理建议。
- 测试策略：规则语法校验、模拟指标触发。
- 测试文件：`AlertRuleConfigTest.java`
- 测试命令：`mvn test -Dtest=AlertRuleConfigTest`
- 预期 RED：异常无告警规则。
- 预期 GREEN：关键风险能触发告警。
- 验收标准：运维可及时发现模型、索引、成本和安全异常。
- 风险与回滚：告警过多导致疲劳，阈值分级和静默窗口配置。
- 适用 .ai_rules 文件：README、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：告警说明清晰；不包含敏感信息；配置可回滚。
- 状态：未开始

# 阶段 7：管理后台与部署

## TASK-080 模型配置管理

- 所属模块：`adminconfig`、`llm`
- 需求来源：FR-6、API_SPEC API-14
- 设计来源：`DESIGN.md` 第 4.2 admin config、8 节
- 变更位置：`adminconfig/controller/ModelConfigController.java`、`llm/service/ModelConfigService.java`
- 前置依赖：TASK-047
- 输入：模型配置 DTO、API Key、价格配置
- 输出：模型配置管理 API
- 实现步骤：实现新增、修改、禁用、测试连接；API Key 加密；配置变更审计。
- 测试策略：管理员权限、API Key 脱敏、模型测试失败。
- 测试文件：`ModelConfigControllerTest.java`
- 测试命令：`mvn test -Dtest=ModelConfigControllerTest`
- 预期 RED：模型无法后台配置。
- 预期 GREEN：管理员可配置模型，普通用户 403。
- 验收标准：模型供应商可通过后台管理。
- 风险与回滚：错误配置禁用并回退默认模型。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：API Key 不返回；管理操作审计；RESTful。
- 状态：未开始

## TASK-081 Prompt 模板管理

- 所属模块：`adminconfig`、`prompt`
- 需求来源：FR-6、PROMPT_SPEC
- 设计来源：`DESIGN.md` 第 4.2 prompt/admin config 节
- 变更位置：`adminconfig/controller/PromptTemplateController.java`、`prompt/service/PromptTemplateService.java`
- 前置依赖：TASK-058
- 输入：Prompt 模板、变量契约、版本、场景
- 输出：Prompt 模板 CRUD、发布、回滚 API
- 实现步骤：实现模板新增、草稿、激活、禁用；校验变量；保存历史；写审计。
- 测试策略：版本不可覆盖、变量缺失、回滚、普通用户拒绝。
- 测试文件：`PromptTemplateControllerTest.java`
- 测试命令：`mvn test -Dtest=PromptTemplateControllerTest`
- 预期 RED：Prompt 只能代码修改。
- 预期 GREEN：Prompt 可配置化、版本化和回滚。
- 验收标准：Prompt 不硬编码散落。
- 风险与回滚：错误 Prompt 可一键回滚上一 ACTIVE。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：Prompt Gate；版本审计；模板不返回敏感策略给普通用户。
- 状态：未开始

## TASK-082 知识库管理后台 API

- 所属模块：`adminconfig`、`knowledgebase`
- 需求来源：FR-2
- 设计来源：`DESIGN.md` 第 4.2 knowledgebase/admin config 节
- 变更位置：`knowledgebase/controller/admin/AdminKnowledgeBaseController.java`
- 前置依赖：TASK-021
- 输入：知识库查询、配置、启停、统计请求
- 输出：后台知识库管理 API
- 实现步骤：实现管理列表；详情；启停；检索配置修改；统计字段查询；审计。
- 测试策略：租户管理员、KB 管理员、普通用户权限差异。
- 测试文件：`AdminKnowledgeBaseControllerTest.java`
- 测试命令：`mvn test -Dtest=AdminKnowledgeBaseControllerTest`
- 预期 RED：管理员无法集中管理知识库。
- 预期 GREEN：按角色返回不同管理能力。
- 验收标准：知识库后台 API 支撑前端管理台。
- 风险与回滚：错误配置导致检索异常时可回滚默认检索配置。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：管理 API 鉴权；DTO 不返回 Entity；配置变更审计。
- 状态：未开始

## TASK-083 文档管理后台 API

- 所属模块：`adminconfig`、`document`
- 需求来源：FR-3
- 设计来源：`DESIGN.md` 第 4.2 document、6 节
- 变更位置：`document/controller/admin/AdminDocumentController.java`
- 前置依赖：TASK-036
- 输入：文档查询、删除、重试、重建索引请求
- 输出：文档管理后台 API
- 实现步骤：实现文档列表；状态查询；失败原因查看；重试解析；重建索引；删除索引。
- 测试策略：权限、状态冲突、重试幂等、审计。
- 测试文件：`AdminDocumentControllerTest.java`
- 测试命令：`mvn test -Dtest=AdminDocumentControllerTest`
- 预期 RED：失败文档无法运维处理。
- 预期 GREEN：管理员可按权限处理文档任务。
- 验收标准：文档处理具备运营维护入口。
- 风险与回滚：误删文档先软删除，索引异步清理可恢复。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：删除/重建需 WRITE/ADMIN；状态机合法；审计记录。
- 状态：未开始

## TASK-084 Docker Compose

- 所属模块：`deploy`
- 需求来源：部署要求、AC-2
- 设计来源：`DESIGN.md` 第 12.1 节
- 变更位置：`docker-compose.yml`、`deploy/env/.env.example`
- 前置依赖：TASK-004
- 输入：应用镜像和基础设施镜像
- 输出：本地 MVP 依赖编排文件
- 实现步骤：编写 MySQL、Redis、MinIO、RabbitMQ、OpenSearch、Milvus/Qdrant、Prometheus、Grafana、应用服务配置；提供 `.env.example`。
- 测试策略：`docker compose config` 语法校验；最小依赖启动检查。
- 测试文件：`deploy/tests/docker-compose-validation.md`
- 测试命令：`docker compose config`
- 预期 RED：缺少 compose 文件或配置非法。
- 预期 GREEN：配置可解析，服务名和网络完整。
- 验收标准：本地可按文档启动基础设施。
- 风险与回滚：机器资源不足时提供 minimal profile。
- 适用 .ai_rules 文件：README、CODING、LOGGING
- 规则检查点：不提交真实密钥；配置说明清晰；日志路径可控。
- 状态：未开始

## TASK-085 Nginx

- 所属模块：`deploy`
- 需求来源：部署要求、安全要求
- 设计来源：`DESIGN.md` 第 12 节
- 变更位置：`deploy/nginx/nginx.conf`
- 前置依赖：TASK-084
- 输入：前端静态资源路径、后端 API 地址
- 输出：Nginx 反向代理和基础安全配置
- 实现步骤：配置 `/api/` 代理；配置 SSE 支持；限制上传大小；配置 gzip、超时和安全 header。
- 测试策略：配置语法、SSE 不被缓冲、上传大小限制。
- 测试文件：`deploy/tests/nginx-validation.md`
- 测试命令：`nginx -t -c deploy/nginx/nginx.conf`
- 预期 RED：Nginx 配置缺失或 SSE 不可用。
- 预期 GREEN：配置语法通过并支持 API 代理。
- 验收标准：本地和生产入口有统一代理配置。
- 风险与回滚：上传大小过小会阻断文档上传，参数可配置。
- 适用 .ai_rules 文件：README、API、LOGGING
- 规则检查点：不暴露内部服务；上传限制；超时明确。
- 状态：未开始

## TASK-086 初始化演示数据

- 所属模块：`deploy`、`tenant`、`knowledgebase`、`document`
- 需求来源：AC-1、AC-2
- 设计来源：`DESIGN.md` 第 12、15 节
- 变更位置：`db/seed`、`deploy/demo`
- 前置依赖：TASK-083
- 输入：演示租户、用户、角色、知识库、样本文档
- 输出：可演示的数据集和导入脚本
- 实现步骤：准备 demo 租户；创建管理员和普通用户；导入知识库和文档；配置模型和 Prompt。
- 测试策略：导入脚本可重复执行，演示用户权限隔离。
- 测试文件：`DemoDataSeedTest.java`
- 测试命令：`mvn test -Dtest=DemoDataSeedTest`
- 预期 RED：无演示数据无法完整展示链路。
- 预期 GREEN：演示账号可分别看到不同知识库。
- 验收标准：上传、索引、问答、引用、反馈可演示。
- 风险与回滚：演示数据不得包含真实敏感信息，可一键清理。
- 适用 .ai_rules 文件：README、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：seed 脚本幂等；无真实密钥；租户隔离。
- 状态：未开始

## TASK-087 README 和演示脚本

- 所属模块：`docs`、`deploy`
- 需求来源：AC-1、AC-5
- 设计来源：`DESIGN.md` 第 12、15 节
- 变更位置：`README.md`、`docs/demo.md`、`scripts/demo`
- 前置依赖：TASK-086
- 输入：部署步骤、API 示例、演示数据
- 输出：项目说明、启动步骤、演示脚本、已知风险
- 实现步骤：编写 README；说明技术栈、架构、启动、测试、演示路径；提供 API 调用脚本；记录未完成项和风险。
- 测试策略：按 README 从空环境执行一次最小启动和 API 验证。
- 测试文件：`docs/README_VERIFICATION.md`
- 测试命令：`mvn test && docker compose config`
- 预期 RED：文档缺失导致新开发者无法启动。
- 预期 GREEN：按文档可完成本地演示准备。
- 验收标准：项目可被复现、演示和继续开发。
- 风险与回滚：文档滞后必须在每次发布前更新。
- 适用 .ai_rules 文件：README、API、DB、CODING、STRUCTURE、SERVICE、COMMENT、LOGGING
- 规则检查点：不夸大未验证性能；不暴露密钥；验证命令真实。
- 状态：未开始

# 执行顺序建议

严格按阶段执行：

1. TASK-001 到 TASK-004：工程基础。
2. TASK-010 到 TASK-019：权限、多租户、数据隔离、审计基础。
3. TASK-020 到 TASK-029：知识库和文档基础。
4. TASK-030 到 TASK-036：Chunk、Embedding、向量和关键词索引。
5. TASK-040 到 TASK-047：LLM Gateway。
6. TASK-050 到 TASK-062：RAG 问答。
7. TASK-070 到 TASK-078：反馈、评估、可观测。
8. TASK-080 到 TASK-087：后台管理、部署和演示。

安全门禁：

- TASK-018 未完成前，不允许实现真实 RAG 问答。
- TASK-041 未完成前，不允许接入真实模型 API Key。
- TASK-058 未完成前，不允许把 Prompt 硬编码进入 Chat Service。
- TASK-061 未完成前，不允许对外展示无引用答案。
- TASK-062 未完成前，不允许把无上下文问题交给模型自由回答。

# Plan Gate 自检

- 每个 TASK 均包含编号、名称、所属模块、需求来源、设计来源、变更位置、前置依赖、输入、输出、实现步骤、测试策略、测试文件、测试命令、预期 RED、预期 GREEN、验收标准、风险与回滚、适用 `.ai_rules` 文件、规则检查点和状态。
- TASK 顺序已调整为权限基础优先，RAG 问答后置。
- 每个 TASK 只包含一个主要目标，避免多个独立目标混入一个任务。
- 本 TODO 仍属于计划文档，未执行代码实现；进入实现前必须针对单个 TASK 执行 TDD Gate。
