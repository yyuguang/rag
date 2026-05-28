# 日志规范

# 文档信息

- 文档名称：日志规范
- 当前状态：生效中
- 最近更新阶段：规则体系补强
- 最近更新原因：补强 Service 层日志、异常日志和状态变更日志要求

---

## 1. 总体原则

日志必须服务于问题排查、业务追踪和风险审计。

Service 层必须记录关键日志，尤其是：

- 写操作。
- 状态变更。
- 外部系统调用。
- 异常分支。
- 幂等命中。
- 关键业务拒绝。

禁止：

- 使用 `System.out.println`。
- 打印无意义日志。
- 打印密码、Token、身份证号、手机号完整值等敏感信息。
- 在循环中打印大量无控制日志。
- 捕获异常后只打印日志但不处理、不抛出。

---

## 2. Service 日志基础要求

每个 Service impl 必须使用日志：

```java
@Slf4j
@Service
public class XxxServiceImpl implements XxxService {
}
```

Service 写操作至少包含：

- 开始日志。
- 成功日志。
- 异常日志。

复杂查询至少包含：

- 查询条件摘要。
- 查询结果数量或关键结果。

---

## 3. 日志字段要求

日志优先包含以下字段：

- `traceId`：链路追踪 ID，如项目已有上下文工具则必须使用。
- `tenantId`：租户 ID，如涉及多租户。
- `userId`：用户 ID。
- `bizId`：业务主键，如 orderId、productId、taskId。
- `operation`：业务动作。
- `status`：状态变更前后值。
- `costMs`：外部调用或复杂操作耗时。

当字段不存在时，不得伪造。

---

## 4. 日志级别

### info

用于记录正常关键业务流程：

- 写操作开始和成功。
- 状态变更成功。
- 外部调用开始和成功。
- 重要异步任务开始和结束。

### warn

用于记录可预期但需要关注的业务分支：

- 幂等命中。
- 参数业务校验不通过。
- 数据不存在但不属于系统异常。
- 外部系统返回业务失败。

### error

用于记录异常：

- 数据库异常。
- 外部系统调用异常。
- 非预期运行时异常。
- 业务流程无法继续的严重错误。

---

## 5. 推荐日志示例

写操作开始：

```java
log.info("订单创建开始, userId={}, requestNo={}", reqDTO.getUserId(), reqDTO.getRequestNo());
```

状态变更成功：

```java
log.info("订单状态更新成功, orderId={}, oldStatus={}, newStatus={}", orderId, oldStatus, newStatus);
```

幂等命中：

```java
log.warn("订单创建幂等命中, requestNo={}, existingOrderId={}", reqDTO.getRequestNo(), orderId);
```

异常日志：

```java
log.error("订单创建失败, userId={}, requestNo={}", reqDTO.getUserId(), reqDTO.getRequestNo(), ex);
```

---

## 6. 敏感信息规则

禁止完整打印：

- 密码。
- Token。
- 身份证号。
- 手机号。
- 银行卡号。
- 详细地址。
- 私钥、密钥、签名原文。

如确需排查，应脱敏：

```java
log.info("用户登录失败, userId={}, mobile={}", userId, maskMobile(mobile));
```

---

## 7. 异常处理日志

异常日志必须包含：

- 业务动作。
- 关键业务 ID。
- 关键入参摘要。
- 异常对象。

禁止：

```java
log.error("系统错误");
```

推荐：

```java
log.error("用户注销失败, userId={}, logoutReasonCode={}", userId, reasonCode, ex);
```

---

## 8. AI 生成代码检查清单

生成或修改 Service 代码后必须检查：

- Service impl 是否有 `@Slf4j`。
- 写操作是否有开始、成功、异常日志。
- 状态变更是否记录前后状态。
- 外部调用是否记录请求摘要、结果摘要和耗时。
- warn 日志是否覆盖业务拒绝或幂等命中。
- error 日志是否带异常对象。
- 是否避免敏感信息明文输出。
- 是否避免无意义日志。
