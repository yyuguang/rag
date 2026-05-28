# 示例：多模块任务

## 用户输入

优化商品查询性能，涉及 product-gateway、product-metadata、oms-product-business

---

## workflow-orchestrator 输出示例

### 1. 任务识别结果

- 任务类型：复杂优化任务
- 所属模块：多个
- 是否跨模块：是
- 是否涉及数据模型变更：可能
- 风险级别：高
- 复杂度分层：L4

---

### 2. 主流程选择

- 当前入口 Skill：task-bootstrapper
- 主流程链路（简化）：

task-bootstrapper
→ requirement-analyst
→ requirement-clarifier
→ design-researcher
→ system-architect
→ task-planner
→ test-designer
→ test-writer
→ spec-driven-coder

---

### 3. Superpowers 判断

- 是否需要：是
- 使用 Skill：
  - subagent-driven-development
  - dispatching-parallel-agents
- 使用原因：
  - 多模块需要拆分子任务
  - 可并行执行优化

---

### 4. 当前阶段目标

- 当前阶段：task-bootstrapper
- 目标：
  拆分子任务结构
- 产物：
  - TASK_CONTEXT.md
  - 子任务列表

---

### 5. 下一步动作

- 立即执行：
  → task-bootstrapper（结合 subagent-driven-development）
- 后续阶段：
  → requirement-analyst（逐模块分析）

---

## ⚠️ 特别约束

- 必须先做影响分析
- 必须先通过 design-researcher
- 禁止直接并行编码
- 必须拆分依赖关系
- 必须维护 PROJECT_REGISTRY 和 CURRENT_FOCUS
