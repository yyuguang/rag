# 示例：发布检查

## 用户输入

这个需求可以上线了吗？

---

## workflow-orchestrator 输出示例

### 1. 任务识别结果

- 任务类型：发布检查
- 风险级别：取决于任务

---

### 2. 主流程选择

- 当前入口 Skill：release-manager
- 主流程链路：

release-manager
→ knowledge-curator

---

### 3. Superpowers 判断

- 是否需要：是
- 使用 Skill：
  - verification-before-completion
- 使用原因：
  - 需要完整校验交付质量

---

### 4. 当前阶段目标

- 当前阶段：release-manager
- 目标：
  判断是否具备发布条件
- 产物：
  - RELEASE.md
- 完成标准：
  - 测试完成
  - Review 完成
  - 风险已评估

---

### 5. 下一步动作

- 立即执行：
  → release-manager（结合 verification-before-completion）
- 后续阶段：
  → knowledge-curator