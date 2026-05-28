# 示例：新功能开发

## 用户输入

为 product-auth 模块新增用户注销接口

---

## workflow-orchestrator 输出示例

### 1. 任务识别结果

- 任务类型：新功能开发
- 所属模块：product-auth
- 是否跨模块：否
- 是否涉及接口变更：是
- 是否涉及数据模型变更：否
- 风险级别：中
- 复杂度分层：L2（接口变更可提升到 L3，需视鉴权/兼容性影响判断）

---

### 2. 主流程选择

- 当前入口 Skill：task-bootstrapper
- 主流程链路：

task-bootstrapper
→ requirement-analyst
→ requirement-clarifier
→ design-researcher（如判定为 L3 或高风险）
→ system-architect
→ api-designer
→ data-designer
→ task-planner
→ test-designer
→ test-writer
→ spec-driven-coder
→ test-executor
→ code-reviewer
→ bug-triager
→ bug-fixer
→ release-manager
→ knowledge-curator

---

### 3. Superpowers 判断

- 是否需要：是
- 使用 Skill：
  - brainstorming（需求补充）
  - writing-plans（任务拆解）
- 使用原因：
  - 用户需求较简略，需要补充业务细节
  - 需要生成结构化 TODO 任务

---

### 4. 当前阶段目标

- 当前阶段：task-bootstrapper
- 目标：
  初始化任务目录与基础上下文
- 产物：
  - TASK_CONTEXT.md
  - STATUS.md（initialized）
- 完成标准：
  - 任务目录结构完整
  - 基础信息可追溯
- 当前质量门禁：
  - Spec Gate

---

### 5. 下一步动作

- 立即执行：
  → task-bootstrapper
- 后续阶段：
  → requirement-analyst（结合 brainstorming）
