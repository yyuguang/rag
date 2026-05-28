# AI 规则索引

# 文档信息

- 文档名称：AI 规则索引
- 当前状态：生效中
- 最近更新阶段：规则体系补强
- 最近更新原因：按职责拆分 `.ai_rules` 规则文件，避免规则堆叠

---

## 1. 规则读取顺序

AI 执行工程任务时，必须按以下顺序读取规则：

1. `AGENTS.md`
2. `.ai_rules/README.md`
3. 与任务相关的 `.ai_rules/*.md`
4. 当前任务目录下的需求、设计、计划和状态文档

当规则存在冲突时，优先级遵循：

```text
AGENTS.md
> 用户当前任务中的明确补充要求
> .ai_rules/README.md
> .ai_rules 专项规则
> 当前任务文档
```

用户在当前任务中的明确要求可用于补充或收紧规则，但不得违反 `AGENTS.md`。

---

## 2. 规则文件职责

| 文件 | 职责 |
| --- | --- |
| `API_STYLE.md` | API URL、请求、响应、状态码、错误、分页、安全、追踪规范 |
| `DB_STYLE.md` | 数据库表、字段、索引、SQL、并发、时间规范 |
| `CODING_STYLE.md` | Java 通用编码原则、类方法设计、命名、异常、空值处理等基础规范 |
| `PROJECT_STRUCTURE.md` | 项目包结构、代码分层、跨层调用边界 |
| `SERVICE_STYLE.md` | Service interface、impl、事务、业务编排、参数返回规范 |
| `COMMENT_STYLE.md` | JavaDoc、接口注释、业务逻辑注释、DTO 字段注释规范 |
| `LOGGING_STYLE.md` | Service 日志、异常日志、状态变更日志、敏感信息约束 |

---

## 3. 职责分离原则

- 新增 API 规则写入 `API_STYLE.md`。
- 新增数据库规则写入 `DB_STYLE.md`。
- 新增通用编码原则写入 `CODING_STYLE.md`。
- 新增包结构和分层规则写入 `PROJECT_STRUCTURE.md`。
- 新增 Service 专项规则写入 `SERVICE_STYLE.md`。
- 新增注释规则写入 `COMMENT_STYLE.md`。
- 新增日志规则写入 `LOGGING_STYLE.md`。

禁止把所有规则集中写入某一个文件。若新增规则无法归入现有文件，应新增职责清晰的规则文件，并同步更新本索引。

---

## 4. 代码生成最低读取清单

涉及 Java 代码生成或修改时，AI 至少必须读取：

- `CODING_STYLE.md`
- `PROJECT_STRUCTURE.md`
- `SERVICE_STYLE.md`
- `COMMENT_STYLE.md`
- `LOGGING_STYLE.md`

涉及接口时，额外读取：

- `API_STYLE.md`

涉及数据库、Mapper、Entity 或 SQL 时，额外读取：

- `DB_STYLE.md`

---

## 5. AI 输出要求

涉及代码生成或修改时，输出必须包含：

- 变更模块
- 变更原因
- 影响范围
- 验证方式
- 风险说明
- 已遵守的规则文件清单

---

## 6. 强制门禁要求

`.ai_rules` 不是建议项，必须通过 `AGENTS.md` 中的 `AI Rules Compliance Gate` 强制执行。

### 6.1 任务开始

AI 开始任何工程任务时必须：

1. 先读取 `AGENTS.md`。
2. 再读取本文件。
3. 根据任务类型读取相关专项规则。
4. 在任务文档中记录本次适用规则文件清单。

### 6.2 计划阶段

`TODO.md` 的每个 TASK 必须包含：

- 适用 `.ai_rules` 文件。
- 规则检查点。
- 验收方式。
- 若某类规则不适用，必须说明原因。

缺少以上内容时，不允许进入编码。

### 6.3 编码阶段

编码前必须确认当前 TASK 已通过规则门禁。

编码后必须在变更说明中写明：

- 已读取的规则文件。
- 已落实的关键规则。
- 未适用规则及原因。
- 与现有代码冲突时的处理结论。

### 6.4 测试与审查阶段

`TESTPLAN.md`、`TEST_REPORT.md` 和 `REVIEW.md` 必须包含 `.ai_rules` 合规验证。

可采用：

- 静态搜索。
- 文件路径和包结构检查。
- 单元测试或集成测试。
- 人工审查记录。

无法自动化验证时，必须写明人工检查路径和残余风险。

### 6.5 发布阶段

`RELEASE.md` 必须给出 `.ai_rules` 门禁结论。

如果存在未关闭的高风险规则违反项，必须明确写出：

```text
不建议发布
```
