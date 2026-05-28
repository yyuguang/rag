# 示例：Prompt 设计任务

## 用户输入

为代码审查模块设计 AI Review 提示词模板，要求输出风险等级、问题位置和修复建议。

---

## workflow-orchestrator 输出示例

### 1. 任务识别结果

- 任务类型：Prompt 设计补充
- 所属模块：代码审查 / AI Review
- 是否跨模块：可能
- 是否涉及 API 变更：可能
- 是否涉及数据模型变更：可能
- 是否涉及 Prompt / AI 推理链路：是
- 风险级别：高
- 复杂度分层：L4

---

### 2. 主流程选择

- 当前入口 Skill：requirement-analyst
- 主流程链路：

requirement-analyst
→ requirement-clarifier
→ design-researcher
→ system-architect
→ prompt-designer
→ api-designer（如 Prompt 输出对外暴露）
→ data-designer（如 Prompt 模板需要持久化）
→ task-planner
→ test-designer

---

### 3. 当前质量门禁

- Design Research Gate：需要现状 Prompt、调用链、方案对比和风险反证。
- Prompt Gate：需要输入契约、输出契约、规则优先级、安全边界、示例集、评估方案和版本治理。

---

### 4. 下一步动作

- 立即执行：读取需求与历史 Prompt 文档，进入 `requirement-analyst`。
- 回退条件：如果缺少业务目标、输出契约或评估标准，回退到 `requirement-clarifier`。
