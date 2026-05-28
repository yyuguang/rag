# TRIGGERS.md

## 目的

定义 workflow-orchestrator 的自动触发规则。

用于：

- 自动识别任务类型
- 自动选择主流程
- 自动决定是否使用 superpowers

------

## 1. 触发优先级

当匹配多个规则时，优先级如下：

```
Bug修复
> 发布检查
> 新功能开发
> 设计补充
> Prompt设计
> 测试补充
> 设计研究
> 默认（新功能）
```

------

## 2. Bug 修复触发规则

### 关键词

```
修复
bug
报错
异常
失败
问题
不生效
不工作
error
fix
```

### 示例

- 修复登录失败问题
- 接口报错 500
- 网关鉴权异常

### 行为

- 任务类型：Bug 修复
- 入口 Skill：bug-triager
- 必须在 bug-fixer 前进入 test-writer，优先补复现测试或失败测试
- 强制使用：
  - systematic-debugging（推荐）

------

## 3. 发布检查触发规则

### 关键词

```
可以上线吗
能发布吗
是否可以发布
发布检查
上线风险
ready to release
```

### 行为

- 任务类型：发布检查
- 入口 Skill：release-manager
- 强制使用：
  - verification-before-completion

------

## 4. 新功能开发触发规则

### 关键词

```
新增
添加
实现
开发
支持
增加接口
新增接口
实现功能
build
implement
```

### 示例

- 新增用户注销接口
- 实现订单超时取消
- 添加权限校验

### 行为

- 任务类型：新功能开发
- 入口 Skill：task-bootstrapper
- 默认测试先行：task-planner 之后必须进入 test-designer 和 test-writer，再进入 spec-driven-coder
- 推荐使用：
  - brainstorming（需求不清晰）
  - writing-plans（复杂任务）

------

## 5. 设计补充触发规则

### 关键词

```
设计
方案
架构
如何实现
技术选型
设计一个
设计方案
architecture
design
```

### 行为

- 任务类型：设计补充
- 入口 Skill：requirement-analyst
- 若涉及方案、优化、重构、架构、接口、数据、权限或核心业务，必须在 system-architect 前进入 design-researcher
- 推荐使用：
  - brainstorming

------

## 5.1 设计研究触发规则

### 关键词

```
方案对比
设计研究
深度设计
优化
重构
取舍
权衡
trade-off
design research
```

### 行为

- 任务类型：设计研究
- 入口 Skill：design-researcher
- 推荐使用：
  - brainstorming

------

## 5.2 Prompt 设计触发规则

### 关键词

```
Prompt
提示词
提示词模板
模型输出
输出契约
AI 推理链路
规则治理
prompt template
prompt engineering
prompt injection
Golden Case
```

### 行为

- 任务类型：Prompt 设计补充
- 入口 Skill：requirement-analyst；如果需求和架构已明确，可进入 prompt-designer
- 若 Prompt 影响产品行为、AI 推理链路、规则治理、接口输出或业务判断，必须先进入 design-researcher，再进入 system-architect 和 prompt-designer
- 必须检查：
  - Prompt Gate
  - 输入契约
  - 输出契约
  - 安全边界
  - 示例集
  - 评估方案
  - 版本治理
- 推荐使用：
  - brainstorming

------

## 6. 测试补充触发规则

### 关键词

```
测试
测试用例
覆盖率
单测
测试代码
test
unit test
```

### 行为

- 任务类型：测试补充
- 入口 Skill：test-designer
- 推荐使用：
  - test-driven-development

------

## 7. 并行任务触发规则

### 条件

满足以下任意：

- 出现 “多个模块”
- 出现 “同时优化”
- 出现 “批量处理”
- 出现 “并行”
- 明确多个独立子任务

### 行为

- 允许使用：
  - subagent-driven-development
  - dispatching-parallel-agents

⚠️ 前提：

- 必须先完成依赖分析
- 禁止直接并行编码

------

## 8. 高风险任务识别规则

### 关键词

```
网关
鉴权
认证
公共模块
数据模型
数据库
接口变更
协议变更
Prompt
提示词
AI 推理链路
规则治理
```

### 行为

- 风险级别：高
- 强制要求：
  - 进入 design-researcher
  - 完整设计流程
  - 不允许跳过 system-architect
  - 发布前必须 verification-before-completion

------

## 8.1 复杂度分层触发规则

workflow-orchestrator 必须按 `QUALITY_GATES.md` 判定复杂度：

- L1：简单低风险单点任务。
- L2：标准单模块任务。
- L3：复杂或高风险任务。
- L4：长期、跨模块、平台级、规则治理或 AI 推理链路任务。

行为：

- L1 可轻量执行，但必须说明验证方式。
- L2 必须有 Spec、TODO、Test、Review。
- L3 必须有 design-researcher。
- L4 必须有 design-researcher、PROJECT_REGISTRY 和 CURRENT_FOCUS。

------

## 9. 默认规则

当无法明确分类时：

- 任务类型：新功能开发
- 入口 Skill：task-bootstrapper

------

## 10. 冲突处理规则

当一个输入同时匹配多个类型：

### 示例

```
修复登录接口并新增日志
```

处理方式：

1. 拆分为多个子任务：
   - Bug 修复
   - 新功能开发
2. 分别走流程：

- Bug → bug-triager
- Feature → task-bootstrapper

------

## 11. 输出要求

触发后，必须输出：

- 任务类型
- 匹配规则
- 选择理由
- 入口 skill
- 是否使用 superpowers
- 复杂度分层
- 质量门禁
- 下一步动作

------

## 一句话总结

Trigger Rules =
输入识别器 + 流程自动选择器 + 风险感知器
