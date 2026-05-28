# 项目分层规范

# 文档信息

- 文档名称：项目分层规范
- 当前状态：生效中
- 最近更新阶段：规则体系补强
- 最近更新原因：明确 controller、dao、dto、service 的标准职责边界

---

## 1. 标准包结构

Java 业务模块必须优先使用以下结构：

```text
com.lnzz.{module}
├── controller
├── dao
│   ├── entity
│   └── mapper
├── dto
│   ├── req
│   ├── res
│   └── common
└── service
    ├── XxxService.java
    └── impl
        └── XxxServiceImpl.java
```

说明：

- `controller`：只存放 Controller。
- `dao`：只存放数据访问相关代码。
- `dao.entity`：存放数据库实体。
- `dao.mapper`：存放 Mapper 接口。
- `dto`：存放接口入参、出参和跨层传输对象。
- `dto.req`：存放请求 DTO。
- `dto.res`：存放响应 DTO。
- `service`：存放业务接口。
- `service.impl`：存放业务实现类。

---

## 2. Controller 层职责

Controller 只负责：

- 接收 HTTP 请求。
- 参数基本校验。
- 调用 Service。
- 返回统一响应结构。
- 补充接口级注释和 API 注解。

Controller 禁止：

- 编写核心业务逻辑。
- 直接访问 Mapper、Entity 或数据库。
- 拼装复杂业务流程。
- 直接处理事务。
- 返回数据库 Entity。

---

## 3. DAO 层职责

DAO 层只负责数据访问。

Mapper 只负责：

- 定义数据库操作方法。
- 承载 MyBatis / MyBatis-Plus 数据访问能力。
- 配合 XML 或注解 SQL 完成持久化。

Entity 只负责：

- 映射数据库表结构。
- 保存数据库字段。
- 表达持久化状态。

DAO 层禁止：

- 编写业务逻辑。
- 调用 Service。
- 返回面向接口的响应 DTO。
- 在 Entity 中写接口展示逻辑。

---

## 4. DTO 层职责

DTO 只负责数据传输。

推荐命名：

- 请求对象：`XxxReqDTO`
- 响应对象：`XxxResDTO`
- 通用传输对象：`XxxDTO`
- 批量请求对象：`XxxBatchReqDTO`
- 查询请求对象：`XxxQueryReqDTO`

DTO 必须：

- 字段命名表达业务含义。
- 字段具备中文注释或 Swagger 注解。
- 按请求、响应、通用对象分包管理。

DTO 禁止：

- 编写业务逻辑。
- 直接依赖 Mapper。
- 混入数据库持久化注解，除非该对象同时是明确设计的持久化对象。
- 用 Entity 替代接口响应 DTO。

---

## 5. Service 层职责

Service 层负责业务能力抽象与业务实现。

必须遵守：

- interface 放在 `service` 包。
- 实现类放在 `service.impl` 包。
- 实现类命名为 `XxxServiceImpl`。
- Controller 只能依赖 Service interface。
- Service 可以依赖 Mapper，不允许 Controller 越过 Service 依赖 Mapper。

详细规则见：

- `.ai_rules/SERVICE_STYLE.md`
- `.ai_rules/LOGGING_STYLE.md`

---

## 6. 跨层调用规则

标准调用方向：

```text
controller -> service -> dao.mapper -> dao.entity
```

允许：

- `controller` 调用 `service`。
- `service.impl` 调用 `dao.mapper`。
- `service.impl` 使用 `dto` 和 `dao.entity` 做转换。
- `mapper` 操作 `entity`。

禁止：

- `controller -> mapper`
- `controller -> entity` 作为接口返回
- `mapper -> service`
- `entity -> service`
- `dto -> mapper`
- `dto -> service`

---

## 7. AI 生成代码检查清单

生成或修改代码前必须检查：

- 是否放入正确包路径。
- Controller 是否只调用 Service。
- Service interface 是否在 `service` 包。
- Service 实现类是否在 `service.impl` 包。
- Mapper 与 Entity 是否位于 `dao` 下。
- DTO 是否位于 `dto` 下并按 req、res 分类。
- 是否存在 Entity 直接作为接口响应。
- 是否存在跨层反向依赖。
