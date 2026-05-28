# Service 设计规范

# 文档信息

- 文档名称：Service 设计规范
- 当前状态：生效中
- 最近更新阶段：规则体系补强
- 最近更新原因：明确 Service interface、impl、事务、业务编排和日志接入规则

---

## 1. 基础结构

Service 必须采用 interface + impl 结构：

```text
service
├── XxxService.java
└── impl
    └── XxxServiceImpl.java
```

要求：

- `XxxService` 定义业务能力。
- `XxxServiceImpl` 实现业务流程。
- `XxxServiceImpl` 必须使用 `@Service`。
- 存在写操作或多数据操作时，必须明确事务边界。
- Service 日志必须遵守 `.ai_rules/LOGGING_STYLE.md`。
- Service 注释必须遵守 `.ai_rules/COMMENT_STYLE.md`。

---

## 2. Interface 职责

Service interface 只负责定义业务能力，不写实现细节。

必须包含：

- 清晰的方法名。
- 完整 JavaDoc。
- 明确参数对象。
- 明确返回对象。
- 必要的业务异常说明。

禁止：

- 在 interface 中写默认业务实现。
- 使用含糊方法名，如 `handle`、`process`、`doSomething`。
- 暴露数据库 Entity 作为接口层返回值，除非已有设计明确允许。

---

## 3. Impl 职责

Service impl 负责业务流程编排。

应包含：

- 参数业务校验。
- 核心业务规则。
- 数据查询和持久化调用。
- DTO 与 Entity 转换。
- 事务控制。
- 关键业务日志。
- 异常处理和业务异常抛出。

禁止：

- 把 HTTP 请求对象、`HttpServletRequest`、`HttpServletResponse` 作为常规业务参数向下传递。
- 在 Service 中拼接 Controller 返回包装结构。
- 直接吞异常。
- 写无边界的大方法。
- 跳过 Mapper 直接手写不受管理的数据库连接。

---

## 4. 方法设计

Service 方法必须满足：

- 一个 public 方法对应一个明确业务用例。
- 参数超过 3 个时必须封装为请求对象。
- 写操作方法必须表达动作，如 `createOrder`、`cancelOrder`、`updateUserStatus`。
- 查询方法必须表达查询意图，如 `queryOrderPage`、`getUserDetail`。
- 复杂流程必须拆分 private 方法。
- private 方法命名必须表达业务语义。

---

## 5. 事务规则

必须添加事务的场景：

- 新增、修改、删除数据。
- 一个业务流程涉及多个写操作。
- 同时更新主表和明细表。
- 状态流转和库存、额度、余额等关键数据变更。

事务要求：

- 事务边界优先放在 Service impl 的 public 方法。
- 只读查询不添加写事务。
- 不在 Controller 中控制事务。
- 不在 Mapper 中控制业务事务。

---

## 6. 参数和返回值

Controller 调用 Service 时：

- 入参优先使用 `dto.req` 下的请求 DTO。
- 出参优先使用 `dto.res` 下的响应 DTO。
- Service 内部可使用 BO 或内部 DTO，但必须职责清晰。

禁止：

- Controller 直接传入或返回 Entity。
- Service 直接返回未过滤敏感字段的对象。
- 使用 `Map<String, Object>` 替代明确 DTO，除非设计文档明确说明。

---

## 7. 日志要求

每个 Service impl 必须具备日志能力：

```java
@Slf4j
@Service
public class XxxServiceImpl implements XxxService {
}
```

Service 方法必须在关键节点记录日志：

- 写操作开始。
- 写操作成功。
- 状态变更。
- 外部系统调用开始和结束。
- 业务拒绝或重要分支。
- 异常捕获和重新抛出前。

详细规则见 `.ai_rules/LOGGING_STYLE.md`。

---

## 8. AI 生成代码检查清单

生成 Service 代码前必须检查：

- 是否创建 interface 和 impl。
- impl 是否位于 `service.impl`。
- Controller 是否依赖 interface。
- 写操作是否有事务。
- public 方法是否有 JavaDoc。
- 复杂逻辑是否拆分 private 方法。
- 是否有关键业务日志。
- 是否避免返回 Entity 和敏感字段。
