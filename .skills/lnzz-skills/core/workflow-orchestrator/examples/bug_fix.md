# 示例：Bug 修复

## 用户输入

修复 product-gateway 模块鉴权失败问题

---

## workflow-orchestrator 输出示例

### 1. 任务识别结果

- 任务类型：Bug 修复
- 所属模块：product-gateway
- 是否跨模块：可能
- 是否涉及接口变更：未知
- 风险级别：高
- 复杂度分层：L3

---

### 2. 主流程选择

- 当前入口 Skill：bug-triager
- 主流程链路：

bug-triager
→ test-writer
→ bug-fixer
→ test-executor
→ code-reviewer

---

### 3. Superpowers 判断

- 是否需要：是
- 使用 Skill：
  - systematic-debugging
- 使用原因：
  - 鉴权问题通常涉及多层调用链
  - 需要系统化排查

---

### 4. 当前阶段目标

- 当前阶段：bug-triager
- 目标：
  定位问题根因
- 产物：
  - BUG_REPORT.md
- 完成标准：
  - 明确问题发生位置
  - 明确触发条件
  - 给出最小复现路径或不可复现原因
- 当前质量门禁：
  - TDD Gate：bug-fixer 前必须有复现测试、RED 记录或测试豁免说明

---

### 5. 下一步动作

- 立即执行：
  → bug-triager（结合 systematic-debugging）
- 后续阶段：
  → test-writer（补复现测试）
