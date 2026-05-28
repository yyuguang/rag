# 注释规范

# 文档信息

- 文档名称：注释规范
- 当前状态：生效中
- 最近更新阶段：规则体系补强
- 最近更新原因：补强接口、Service、Mapper、DTO 和复杂业务逻辑注释要求

---

## 1. 总体原则

注释必须解释业务意图、边界条件和设计原因，不写无意义注释。

必须注释：

- public 类。
- public 方法。
- Controller 接口方法。
- Service interface 方法。
- Service impl public 方法。
- Mapper 方法。
- DTO 字段。
- 复杂业务规则。
- 状态流转。
- 边界处理。
- 异常分支。

禁止注释：

- 机械复述代码。
- 与代码不一致的过期说明。
- 无业务含义的模板化废话。

---

## 2. 类注释

所有业务类必须包含类注释。

模板：

```java
/**
 * @classname: XxxServiceImpl
 * @author: Fantasy
 * @date: 2026/05/17 15:23
 * @description: 用户注销业务实现
 */
```

要求：

- `@description` 必须写清楚业务职责。
- 类注释不得只写“服务实现类”“控制器”等空泛描述。

---

## 3. 方法注释

所有 public 方法必须包含 JavaDoc。

模板：

```java
/**
 * 创建订单并初始化订单状态。
 *
 * @param reqDTO 创建订单请求参数
 * @return 订单创建结果
 * @author Fantasy
 * @date 2026/05/17 15:23
 */
OrderCreateResDTO createOrder(OrderCreateReqDTO reqDTO);
```

要求：

- 第一行必须说明业务动作。
- `@param` 必须说明业务含义，不只重复参数名。
- `@return` 必须说明返回业务含义。
- 可能抛出业务异常时，应补充异常说明。

---

## 4. Controller 注释

Controller 接口方法必须说明：

- 接口用途。
- 请求参数含义。
- 响应结果含义。
- 关键业务限制。

如果使用 Swagger / OpenAPI 注解，注解内容必须与 JavaDoc 一致。

---

## 5. Service 注释

Service interface 方法注释必须说明业务能力和边界。

Service impl public 方法注释必须说明：

- 业务流程概览。
- 关键校验。
- 关键状态变更。
- 事务边界。

复杂 private 方法建议添加简短注释，说明拆分原因和业务规则。

---

## 6. Mapper 注释

Mapper 方法必须说明：

- 查询或写入目的。
- 关键查询条件。
- 返回数据含义。

涉及自定义 SQL 时，必须说明：

- SQL 的业务目的。
- 重要 where 条件。
- 排序或分页原因。

---

## 7. DTO 字段注释

DTO 字段必须有中文注释或 Swagger 注解。

示例：

```java
/**
 * 用户ID
 */
private Long userId;

/**
 * 注销原因编码
 */
private String logoutReasonCode;
```

禁止：

- 字段无注释。
- 注释只写“字段1”“数据”“信息”。
- 请求 DTO 和响应 DTO 字段含义混用。

---

## 8. 行内注释

必须添加行内注释的场景：

- 多条件业务判断。
- 状态机流转。
- 幂等处理。
- 分布式锁或乐观锁。
- 外部系统兼容逻辑。
- 历史数据兼容逻辑。
- 非直观算法或转换。

禁止：

```java
// 设置用户名
user.setName(name);
```

推荐：

```java
// 已取消订单允许重复取消，保证客户端重试幂等
if (OrderStatus.CANCELED.equals(order.getStatus())) {
    return buildCanceledResult(order);
}
```

---

## 9. AI 生成代码检查清单

生成或修改代码后必须检查：

- public 类是否有类注释。
- public 方法是否有 JavaDoc。
- Controller 方法是否有接口说明。
- Service interface 和 impl 是否都有必要注释。
- Mapper 方法是否说明查询或写入目的。
- DTO 字段是否有中文注释。
- 复杂业务逻辑是否有解释原因的注释。
- 是否存在机械复述代码的无效注释。
