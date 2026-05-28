# AGENTS.md

------

## 0. 全局约定

对于任何工程任务：

👉 必须优先使用 **workflow-orchestrator 思维（流程驱动）**
👉 必须遵循 **lnzz-skills/core 主流程 Skill System**
👉 Superpowers 仅作为增强能力，不得替代主流程

```
对于任何工程任务，必须先经过 workflow-orchestrator 进行任务识别、流程路由与能力调度，再进入具体 core skill。
```

------

## 1. 文档目的

本文件定义本仓库中 AI Agent 执行任务时必须遵守的规则、流程和约束。

所有 AI 行为必须遵循以下原则：

- 文档驱动开发
- 单一职责流程
- 可追溯工程流程
- 明确的阶段状态管理

------

## 2. Skill 系统

本仓库采用 **分层 Skill System**：

```text
.skills/
└── lnzz-skills/
    ├── core/
    └── superpowers/
```

------

## 2.1 主流程 Skill（Core）

主流程 Skill 是整个工程执行的唯一主干，不可被替代。

| Skill                 | 职责           |
| --------------------- | -------------- |
| task-bootstrapper     | 初始化任务结构 |
| requirement-analyst   | 需求分析       |
| requirement-clarifier | 需求澄清       |
| design-researcher     | 设计研究门禁   |
| system-architect      | 架构设计       |
| prompt-designer       | Prompt 设计    |
| api-designer          | API 设计       |
| data-designer         | 数据模型设计   |
| task-planner          | 任务拆解       |
| spec-driven-coder     | 代码实现       |
| test-designer         | 测试方案设计   |
| test-writer           | 编写测试代码   |
| test-executor         | 执行测试       |
| code-reviewer         | 代码审查       |
| bug-triager           | 缺陷分析       |
| bug-fixer             | 缺陷修复       |
| release-manager       | 发布检查       |
| knowledge-curator     | 项目沉淀       |

------

## 2.2 Superpowers（辅助能力层）

目录：

```text
lnzz-skills/superpowers/
```

包含能力：

- brainstorming
- dispatching-parallel-agents
- executing-plans
- receiving-code-review
- requesting-code-review
- subagent-driven-development
- systematic-debugging
- test-driven-development
- using-git-worktrees
- using-superpowers
- verification-before-completion
- writing-plans
- writing-skills

------

## 2.3 主次关系（强约束）

必须遵守：

- ✅ core = 唯一流程驱动
- ✅ superpowers = 辅助能力增强
- ❌ 禁止 superpowers 接管流程
- ❌ 禁止跳过 core skill

优先级规则：

```
AGENTS.md
> lnzz-skills/core
> 用户任务
> lnzz-skills/superpowers
```

------

## 2.4 Superpowers 使用规则

### 允许使用场景

- 需求发散 → brainstorming
- 复杂设计研究 / 方案对比 → brainstorming
- 任务拆解增强 → writing-plans
- 执行约束 → executing-plans
- 测试增强 → test-driven-development
- 问题排查 → systematic-debugging
- 交付校验 → verification-before-completion
- 并行执行 → dispatching-parallel-agents
- 子任务拆分 → subagent-driven-development
- Code Review 协作 → requesting / receiving-code-review
- 多分支开发 → using-git-worktrees

------

### 禁止行为

- 用 executing-plans 替代 spec-driven-coder
- 用 writing-plans 替代 task-planner
- 用 debugging 替代 bug-triager
- 用 superpowers 完成完整流程

------

## 2.5 深度设计研究门禁（Design Research Gate）

为避免 AI Agent 只生成格式完整但缺少工程判断的文档，本仓库增加 **design-research gate**。

该门禁由 `design-researcher` core skill 执行，但不得替代 `system-architect`。它是进入 `system-architect` 前的强制质量检查，职责是收集现状证据、比较方案、给出推荐与风险反证。

### 触发场景

满足以下任一条件，必须先完成 design-research gate：

- 涉及架构调整
- 涉及接口协议变更
- 涉及数据模型变更
- 涉及权限、鉴权、网关
- 涉及 Prompt 体系、规则治理、AI 推理链路
- 涉及跨模块协作
- 涉及核心业务逻辑
- 用户明确要求“方案、设计、优化、重构”

### 必须产物

复杂或高风险任务必须生成：

```
02_design/DESIGN_RESEARCH.md
```

内容至少包括：

1. 现状证据
   - 读取了哪些代码、配置、数据库脚本、测试、历史文档
   - 当前真实调用链或数据流是什么
   - 现有模块边界和约束是什么
2. 方案候选
   - 至少 2 个可行方案
   - 每个方案必须说明改动范围、优点、缺点、风险、迁移成本、测试成本
3. 推荐方案
   - 为什么选择该方案
   - 为什么不选择其他方案
   - 关键假设是什么
4. 风险反证
   - 哪些场景可能证明该方案不合适
   - 如何降级、回滚或调整
5. 跨层一致性
   - 架构、API、数据、测试是否互相支撑
   - 是否存在为了实现方便而污染模块边界

### 硬性门槛

- 没有现状代码或文档证据，不允许输出复杂任务最终设计。
- 没有方案对比，不允许进入 `task-planner`。
- 没有说明“不采用方案”的复杂设计，默认视为设计不完整。
- 如果设计研究发现上游需求不清，必须回退到 `requirement-clarifier`。
- design-research gate 可使用 `brainstorming` 增强方案发散，但不得替代 `system-architect`。

------

## 2.6 项目规则强制门禁（AI Rules Compliance Gate）

为避免 `.ai_rules` 只作为“可参考规则”而被忽略，本仓库增加 **AI Rules Compliance Gate**。

该门禁由 `workflow-orchestrator` 在任务识别和流程路由阶段强制触发，并贯穿后续 core skill。它不替代任何 core skill，只负责确认当前工程任务已经读取、引用、执行并验证 `.ai_rules` 中的项目规则。

### 触发场景

所有工程任务都必须触发该门禁，包括但不限于：

- 新功能开发
- Bug 修复
- 重构 / 优化
- 接口、数据库、Service、Controller、Mapper、DTO、配置、脚本或前端页面修改
- `AGENTS.md`、`.ai_rules`、`.skills`、`ai_workspace` 等工程流程或规则文档修改

### 必须读取

进入具体 core skill 前，必须读取：

1. `AGENTS.md`
2. `.ai_rules/README.md`
3. 与任务类型相关的 `.ai_rules/*.md`
4. 当前任务目录中的需求、设计、计划、状态和焦点文档

最低读取规则：

- 任何 Java 代码修改：必须读取 `CODING_STYLE.md`、`PROJECT_STRUCTURE.md`、`SERVICE_STYLE.md`、`COMMENT_STYLE.md`、`LOGGING_STYLE.md`。
- 涉及 API：额外读取 `API_STYLE.md`。
- 涉及数据库、Mapper、Entity 或 SQL：额外读取 `DB_STYLE.md`。
- 涉及规则文件本身：必须读取 `.ai_rules/README.md` 和所有受影响的专项规则文件。

### 阶段绑定

AI Rules Compliance Gate 必须绑定到以下阶段：

1. `task-bootstrapper`：记录本任务适用的 `.ai_rules` 文件清单。
2. `requirement-analyst / requirement-clarifier`：把用户对规则执行、例外和冲突处理的要求写入需求或澄清文档。
3. `design-researcher / system-architect / api-designer / data-designer`：设计文档必须说明 `.ai_rules` 对模块边界、API、数据、日志、注释、分层的约束。
4. `task-planner`：`TODO.md` 的每个 TASK 必须列出适用规则文件、规则检查点和验收方式。
5. `test-designer / test-writer / test-executor`：`TESTPLAN.md` 和 `TEST_REPORT.md` 必须包含 `.ai_rules` 合规验证项，无法自动化时必须记录人工检查方法。
6. `spec-driven-coder`：编码前必须确认当前 TASK 已通过规则门禁；编码后必须说明遵守了哪些规则文件。
7. `code-reviewer`：`REVIEW.md` 必须审查 `.ai_rules` 是否真正执行，而不是只被引用。
8. `release-manager`：发布检查必须包含 `.ai_rules` 门禁结论；未通过时不得建议发布。

### 硬性门槛

- 未读取 `.ai_rules/README.md` 和相关专项规则文件，不允许进入 `task-planner` 或 `spec-driven-coder`。
- `TODO.md` 未列出适用 `.ai_rules`、规则检查点和验收方式，不允许进入 `spec-driven-coder`。
- `TEST_REPORT.md` 未记录 `.ai_rules` 验证结果，不允许进入 `code-reviewer`。
- `REVIEW.md` 未审查 `.ai_rules` 执行情况，不允许进入 `release-manager`。
- 如果 `.ai_rules` 与用户要求、现有设计或代码模式冲突，必须记录冲突、优先级判断和处理结论，不允许静默绕过。

### 必须产物

工程任务中必须在以下文件体现该门禁：

- `01_requirement/REQUIREMENT.md` 或 `01_requirement/CLARIFICATION.md`：记录规则相关需求、例外或冲突。
- `02_design/DESIGN.md` 或相关设计文档：记录规则对设计的约束。
- `03_plan/TODO.md`：记录每个 TASK 适用规则和检查点。
- `05_test/TESTPLAN.md`、`05_test/TEST_REPORT.md`：记录规则验证方式和结果。
- `06_review/REVIEW.md`：记录规则执行审查结论。
- `07_release/RELEASE.md`：记录发布前规则门禁结论。

------

## 3. 默认工作流程（新功能开发）

（在原流程基础上增强）

```
task-bootstrapper
    ↓（可用 subagent-driven-development）
ai-rules-compliance-gate
    ↓（读取 .ai_rules/README.md 和相关专项规则）
requirement-analyst
    ↓（可用 brainstorming）
requirement-clarifier
    ↓
design-researcher
    ↓（复杂/高风险任务强制，可用 brainstorming）
system-architect
    ↓
prompt-designer（涉及 Prompt / AI 推理链路时强制）
    ↓
api-designer
    ↓
data-designer
    ↓
task-planner
    ↓（可用 writing-plans / parallel-agents）
test-designer
    ↓
test-writer
    ↓（TDD：先写失败测试或明确不适用原因）
spec-driven-coder
    ↓（可用 executing-plans）
test-executor
    ↓
code-reviewer
    ↓（request/review）
bug-triager
    ↓（systematic-debugging）
bug-fixer
    ↓
release-manager
    ↓（verification-before-completion）
knowledge-curator
```

------

## 4. Bug 修复流程

```
bug-triager
    ↓（systematic-debugging）
test-writer
    ↓（优先补复现测试）
bug-fixer
    ↓
test-executor
    ↓
code-reviewer
```

如果问题涉及需求、设计或接口问题，必须回退阶段补文档。

如果 Bug 根因涉及架构边界、接口协议、数据模型、权限模型、Prompt 链路或跨模块协作，必须回退到：

```
requirement-clarifier
→ design-researcher
→ system-architect / prompt-designer / api-designer / data-designer
```

缺陷修复默认要求先补复现测试；如果无法自动化复现，必须在 `BUG_REPORT.md` 中说明原因、人工验证路径和残余风险。

------

## 5. 任务文档存储规则

所有 AI 工程任务文档必须放在：

```
ai_workspace/projects/
```

任务目录格式：

```
ai_workspace/projects/{task_name}/
```

`task_name` 必须使用 `snake_case`。

示例：

```
user_login_feature
order_timeout_fix
product_metadata_refactor
```

### 5.1 全局任务注册表

为解决新会话无法获取旧会话上下文而重复创建任务目录的问题，仓库必须维护全局任务注册表：

```
ai_workspace/PROJECT_REGISTRY.md
```

该文件是跨会话任务续接的第一入口，不替代各任务目录内的详细文档。

注册表至少必须记录：

- `task_name`
- 任务中文名
- 任务类型
- 当前状态
- 任务目录
- 相关模块 / 代码目录
- 关键词 / 别名
- 最近更新时间
- 下一步动作

### 5.2 新会话任务续接规则

每次开始工程任务前，必须先执行任务发现，不允许直接新建目录。

任务发现顺序：

1. 读取 `AGENTS.md`。
2. 读取 `ai_workspace/PROJECT_REGISTRY.md`（如果存在）。
3. 扫描 `ai_workspace/projects/*/00_meta/STATUS.md`。
4. 扫描 `ai_workspace/projects/*/00_meta/TASK_CONTEXT.md`。
5. 扫描 `ai_workspace/projects/*/00_meta/CURRENT_FOCUS.md`（如果存在）。
6. 根据用户描述匹配已有 `task_name`、中文名、关键词、相关模块和当前状态。

匹配规则：

- 如果只有一个高置信匹配任务，必须续接该任务目录。
- 如果存在多个候选任务，必须列出候选并向用户确认。
- 如果没有匹配任务，才允许创建新的 `ai_workspace/projects/{task_name}/`。
- 创建新任务目录后，必须同步更新 `ai_workspace/PROJECT_REGISTRY.md`。
- 续接旧任务后，必须更新对应任务的 `CURRENT_FOCUS.md` 和注册表最近更新时间。

禁止行为：

- 未检索注册表和既有任务目录就新建任务。
- 因为新会话缺少上下文而创建重复目录。
- 只依赖用户口头提醒任务归属，不回写到注册表。
- 使用临时目录长期承载正式任务。

------

## 6. 任务目录结构

每个任务必须使用以下结构：

```
ai_workspace/projects/{task_name}/
├── 00_meta/
│   ├── TASK_CONTEXT.md
│   ├── PROJECT_INDEX.md
│   ├── STATUS.md
│   ├── CURRENT_FOCUS.md
│   └── NEXT_PROMPT.md
├── 01_requirement/
│   ├── REQUIREMENT.md
│   └── CLARIFICATION.md
├── 02_design/
│   ├── DESIGN_RESEARCH.md
│   ├── DESIGN.md
│   ├── PROMPT_SPEC.md
│   ├── API_SPEC.md
│   └── DATA_MODEL.md
├── 03_plan/
│   └── TODO.md
├── 04_impl/
│   └── src/
├── 05_test/
│   ├── TESTPLAN.md
│   ├── tests/
│   └── TEST_REPORT.md
├── 06_review/
│   ├── REVIEW.md
│   ├── BUG_REPORT.md
│   └── FIX_LOG.md
├── 07_release/
│   └── RELEASE.md
└── 08_summary/
    └── SUMMARY.md
```

### 6.1 任务检索元数据

每个任务的 `00_meta/TASK_CONTEXT.md` 必须包含跨会话检索元数据：

```markdown
# 任务检索元数据

- task_name：
- 任务中文名：
- 任务类型：
- 相关模块：
- 相关代码目录：
- 关键词：
- 别名：
- 上游任务：
- 下游任务：
```

这些字段用于新会话识别任务归属，并同步到 `ai_workspace/PROJECT_REGISTRY.md`。

------

## 7. 状态机规则

项目必须维护统一状态文件：

```
00_meta/STATUS.md
```

状态列表如下：

```
initialized
requirement_ready
clarification_ready
design_research_ready
design_ready
prompt_ready
api_ready
data_ready
plan_ready
implementation_in_progress
implementation_done
testing_done
review_done
bugfix_in_progress
release_ready
archived
```

每完成一个阶段，必须更新 `STATUS.md`。

如果流程被阻断，必须在 `STATUS.md` 中写明：

- 当前阻断原因
- 阻断阶段
- 建议下一步动作

### 7.1 长期项目焦点文件

长期持续迭代项目必须维护：

```
00_meta/CURRENT_FOCUS.md
```

内容至少包括：

- 当前阶段目标
- 最近完成事项
- 当前阻断
- 最近 5 个关键设计决策
- 最近 5 个真实缺陷或踩坑
- 当前最重要的下一个任务
- 当前需要避免的设计漂移

每次开始长期项目的新任务前，必须优先读取 `CURRENT_FOCUS.md`。如果文件不存在，必须在当前任务中创建或补齐。

------

## 8. 文档语言规则

所有生成文档必须满足以下要求：

- 使用中文
- 使用 Markdown
- 使用结构化标题
- 包含文档信息头
- 明确最近更新阶段和更新原因

文档头模板如下：

```
# 文档信息

- 文档名称：
- 当前状态：
- 最近更新阶段：
- 最近更新原因：
```

------

## 9. 文档驱动规则

在本仓库中，AI Agent 必须遵守文档驱动原则。

### 9.1 新需求

对新需求，必须优先生成：

- `REQUIREMENT.md`
- `CLARIFICATION.md`

### 9.1.1 需求落地要求

需求文档必须承接用户会话中的关键结论，禁止只把需求写成概述。

`REQUIREMENT.md` 至少必须包含：

- 业务目标：为什么要做，不只写“实现什么”。
- 使用场景：谁在什么入口、什么条件下使用。
- 功能规则：明确输入、输出、状态变化、边界条件。
- 非功能要求：性能、安全、权限、可观测性、兼容性、降级要求。
- 验收标准：必须能被测试或人工验收验证。
- 不包含范围：明确本次不做什么。
- 事实 / 假设 / 待确认问题：不得把假设写成已确认事实。

如果用户在会话中补充了关键规则、边界、例外、口径变化，必须在进入下一阶段前回写到：

- `REQUIREMENT.md`
- `CLARIFICATION.md`
- 或 `00_meta/CURRENT_FOCUS.md`

禁止只依赖当前会话上下文保存需求结论。

### 9.2 新设计

对新设计，必须优先生成：

- `DESIGN_RESEARCH.md`（复杂或高风险任务强制）
- `DESIGN.md`
- `PROMPT_SPEC.md`（涉及 Prompt / AI 推理链路时强制）
- `API_SPEC.md`
- `DATA_MODEL.md`

### 9.2.1 设计深度要求

复杂或高风险设计不得只填写模板结构，必须包含以下内容：

- 现状证据：引用已读取的代码、配置、数据库脚本、测试或历史文档。
- 方案对比：至少两个可行方案，并说明改动范围、优缺点、风险和测试成本。
- 不采用方案：明确至少一个被拒绝方案及拒绝原因。
- 推荐方案：说明选择理由、关键假设和适用边界。
- 演进影响：说明对后续扩展、迁移、权限、数据一致性和测试维护的影响。

以下情况视为设计不合格，不允许进入 `task-planner`：

- 只有模块列表，没有设计取舍。
- 只有风险标题，没有触发条件、影响范围和缓解策略。
- 只有“保持兼容”“注意扩展”等空泛表述。
- 没有引用任何现有代码、配置、测试或历史文档证据。
- 没有说明为什么不采用其他方案。

### 9.2.2 设计落地要求

设计文档必须让后续 Agent 不依赖历史会话也能理解方案。

`DESIGN.md` 至少必须明确：

- 变更涉及的模块、包、类、配置、SQL、前端页面或脚本。
- 现有调用链和改造后的调用链。
- 核心数据流、状态流和异常流。
- 权限、鉴权、审计、日志、告警和降级策略。
- 与既有代码模式的对应关系：复用什么、扩展什么、废弃什么。
- 兼容与迁移策略：历史数据、旧配置、旧接口、旧前端入口如何处理。
- 测试映射：每个关键设计点对应哪些测试或验证方式。

如果设计讨论过程中出现以下内容，必须写入 `DESIGN.md` 或 `DESIGN_RESEARCH.md`：

- 方案取舍
- 关键反对意见
- 用户明确拍板的决策
- 被推翻的旧方案
- 影响后续阶段的约束

禁止把设计决策只留在会话上下文里。

### 9.2.3 Prompt 设计要求

对涉及 Prompt 体系、规则治理、AI 推理链路、模型输出契约、可复用提示词模板或产品级 AI 行为的任务，必须优先生成：

- `PROMPT_SPEC.md`

`PROMPT_SPEC.md` 至少必须明确：

- Prompt ID、Prompt 名称、所属能力、调用入口、影响范围和版本。
- 业务目标：Prompt 为什么存在，解决什么业务问题。
- 适用场景与不适用场景。
- 输入契约：变量名、类型、来源、是否必填、默认值、脱敏要求和约束。
- 输出契约：输出格式、字段说明、错误输出和禁止输出内容。
- Prompt 模板：系统角色、任务目标、上下文注入、约束规则、输出格式和失败处理。
- 规则优先级：系统规则、业务规则、用户输入、上下文资料和模型推断之间的优先级。
- 安全与治理：Prompt Injection 防护、敏感信息约束、危险操作约束、幻觉和不确定性处理。
- 示例集：正例、反例、边界例、对抗例。
- 评估方案：Golden Case、回归 Case、评分维度和人工验收路径。
- 版本与兼容策略：版本号、变更原因、兼容范围、回滚方案。

以下情况视为 Prompt 设计不合格，不允许进入 `task-planner`：

- 只有 Prompt 正文，没有输入契约、输出契约和评估方案。
- 没有说明规则优先级，导致用户输入可能覆盖系统规则或业务规则。
- 没有反例、边界例或对抗例。
- 没有说明模型低置信度、无法判断或失败时的输出策略。
- Prompt 正文散落在业务代码中，且没有明确系统内置兜底原因。
- AI 输出可以影响业务决策，但没有说明是否允许推翻规则基线。

轻量一次性对话、临时文案或不进入产品能力的提示词，可不生成完整 `PROMPT_SPEC.md`，但必须说明不适用原因和替代验证方式。

### 9.3 新开发

对开发实施，必须优先生成：

- `TODO.md`

在 `TODO.md` 未完成前，不允许进入大范围编码。

### 9.3.1 TODO 落地要求

`TODO.md` 不是任务标题列表，必须是可执行规格。

每个 TASK 至少必须包含：

- 需求来源：对应的 FR / AC / 澄清项。
- 设计来源：对应的设计章节、方案决策或数据/API 约束。
- 变更位置：预计修改或新增的具体模块、包、文件、类、接口、SQL、页面。
- 前置依赖：依赖哪些 TASK、数据、接口或配置。
- 实现步骤：按可执行顺序拆分，不得只写“实现某功能”。
- 测试策略：先写哪些测试、执行什么命令、期望失败/通过结果。
- 验收标准：完成后如何证明任务结束。
- 风险与回滚：失败时如何降级、回滚或阻断。
- 状态：未开始 / 进行中 / blocked / 已完成。

以下 TODO 视为不合格，不允许进入 `spec-driven-coder`：

- 只有标题，没有文件路径和测试策略。
- 只写“新增接口 / 实现服务 / 联调页面”等泛化动作。
- 没有关联需求编号或设计依据。
- 没有明确验收标准。
- 多个独立目标混在一个 TASK 中。

如果 TODO 过于简洁，必须回退到 `task-planner` 补全，不允许靠会话记忆直接编码。

### 9.4 测试与审查

对测试与审查，必须生成：

- `TESTPLAN.md`
- `TEST_REPORT.md`
- `REVIEW.md`

### 9.5 缺陷处理

对缺陷处理，必须生成：

- `BUG_REPORT.md`
- `FIX_LOG.md`

### 9.6 发布与沉淀

任务结束前必须生成：

- `RELEASE.md`
- `SUMMARY.md`

------

## 10. 代码开发规则

AI 进行代码实现时必须遵守以下规则：

1. 必须基于 `TODO.md`
2. 一次只实现一个 TASK
3. 不允许跳过规划阶段直接大范围编码
4. 不允许在未设计的情况下实现复杂逻辑
5. 修改代码后必须记录变更说明
6. 默认采用 test-driven-development
7. 禁止绕过 TODO.md
8. 复杂任务可使用 executing-plans 强化执行顺序

### 10.0 上下文回写规则

编码前必须确认当前实现所依赖的信息已经落到任务文档中。

如果关键信息只存在于会话上下文中，必须先暂停编码，并回写到对应文档：

- 需求口径 → `REQUIREMENT.md / CLARIFICATION.md`
- 设计决策 → `DESIGN_RESEARCH.md / DESIGN.md`
- Prompt 设计 → `PROMPT_SPEC.md`
- 接口字段 → `API_SPEC.md`
- 数据结构 → `DATA_MODEL.md`
- 执行步骤 → `TODO.md`
- 当前焦点 → `CURRENT_FOCUS.md`

禁止用“刚才会话里已经说过”作为进入编码的依据。

### 10.1 Spec / TDD 绑定规则

进入 `spec-driven-coder` 前，当前 TASK 必须明确：

- 预期行为
- 修改范围
- 需求来源
- 设计来源
- 对应测试文件
- 测试命令
- 成功/失败判定标准

默认执行顺序：

```
1. 先写或补充失败测试
2. 运行测试并确认失败原因符合预期
3. 实现最小代码使测试通过
4. 重新运行测试并记录结果
5. 必要时补充回归测试和架构约束验证
```

允许不先写测试的情况：

- 纯文档任务
- 纯配置说明任务
- 测试成本明显高于变更风险的小改动

但必须在 `TODO.md` 或 `TESTPLAN.md` 中说明原因和替代验证方式。

### 10.2 复杂任务编码前检查

复杂任务编码前必须确认：

- `DESIGN_RESEARCH.md` 已完成，或已说明不适用原因。
- `DESIGN.md / PROMPT_SPEC.md / API_SPEC.md / DATA_MODEL.md` 与 TODO 互相一致。
- 每个 TASK 都有明确测试策略。
- 不存在为了通过测试而破坏模块边界的实现方式。

每次代码改动必须说明：

```
变更模块
变更原因
影响范围
验证方式
风险说明
```



------

## 11. Maven 多模块项目规则

本仓库为 Maven 多模块项目。

AI 修改代码时必须遵守以下规则。

### 11.1 模块隔离

修改前必须先确认变更所属模块，例如：

```
product-auth
product-gateway
product-metadata
oms-product-business
tms-product-business
wms-product-business
```

禁止无理由跨多个模块同时修改。

### 11.2 公共逻辑归属

公共逻辑优先放在合适的公共模块中，例如：

```
otwb-common
```

禁止将公共能力散落到多个业务模块重复实现。

### 11.3 网关模块约束

```
product-gateway
```

该模块职责通常包括：

- 路由转发
- 鉴权接入
- 流量控制
- 网关层编排

禁止在网关模块中实现核心业务逻辑。

### 11.4 认证模块约束

```
product-auth
```

该模块职责通常包括：

- 登录认证
- 注销登录
- Token/Session 处理
- 权限校验相关能力

禁止在认证模块中实现商品业务、订单业务或仓储业务逻辑。

### 11.5 业务模块约束

业务逻辑必须放在对应业务模块中，例如：

```
oms-product-business
tms-product-business
wms-product-business
```

禁止把明确的业务逻辑塞入公共模块或网关模块。

------

## 12. 编译与验证规则

AI 修改代码后必须执行验证。

### 12.1 单模块修改

优先执行对应模块测试：

```
mvn -pl {module} test
```

### 12.2 跨模块修改

如涉及公共模块或多个业务模块，优先执行更完整的验证：

```
mvn clean install
```

### 12.3 验证结果记录

验证后必须记录：

- 执行了什么命令
- 是否通过
- 是否存在失败项
- 失败项是否为环境问题
- 是否需要补充测试

禁止隐藏测试失败。

### 12.4 架构约束验证

对长期项目、复杂任务和高风险任务，测试不仅验证功能行为，还必须验证设计约束。

可采用以下方式：

- 单元测试
- 集成测试
- 静态搜索
- 架构规则测试
- SQL / 配置检查
- 浏览器端到端验收

示例约束：

- Controller 不直接返回 Entity。
- 网关模块不实现核心业务逻辑。
- 认证模块不承载商品、订单、仓储业务逻辑。
- Prompt 正文不得散落在 Java 硬编码中，除非明确属于系统内置兜底。
- SCM 平台 `projectId` 不得与内部 `scmConfigId` 混用。
- AI 分析只能增强诊断原因，不得推翻规则基线风险等级，除非设计明确允许。
- 数据监控默认只读，不提供自动 DDL、自动 DML、kill session 等危险操作。

如果架构约束无法自动化验证，必须在 `TEST_REPORT.md` 中记录人工检查方法和结果。

------

## 13. 代码审查规则

进入 `release-manager` 前，至少必须满足以下条件：

- `TODO.md` 中相关任务已完成或已明确标记状态
- 复杂或高风险任务的 `DESIGN_RESEARCH.md` 已生成或已说明不适用原因
- `TEST_REPORT.md` 已生成
- `REVIEW.md` 已生成
- 所有高优先级 BUG 已关闭，或已明确记录风险接受结论

如果不满足以上条件，默认不进入发布阶段。

允许流程：

```
requesting-code-review
→ receiving-code-review
→ code-reviewer
```

代码审查必须额外检查：

- 实现是否符合 `DESIGN_RESEARCH.md` 中的推荐方案与约束。
- 是否出现只为通过测试而牺牲模块边界的实现。
- 是否存在未被测试覆盖的关键设计假设。
- 是否存在低信息密度文档掩盖的真实风险。

------

## 14. 发布规则

发布前必须执行：

```
verification-before-completion
```

至少必须包含：

- 发布范围
- 已完成项
- 未完成项
- 已知风险
- 发布建议
- 发布后观察建议

如果存在高风险未关闭问题，必须明确写出：

```
不建议发布
```

------

## 15. 知识沉淀规则

任务结束后必须生成：

```
08_summary/SUMMARY.md
```

总结内容至少包括：

- 关键设计决策
- 被拒绝方案及原因
- 关键问题
- 修复经验
- 可复用经验
- 后续优化建议

------

## 16. Agent 行为原则

### 原则 1：优先使用主流程 Skill

优先使用：

```
lnzz-skills/core
```

必要时使用：

```
lnzz-skills/superpowers
```

### 原则 2：优先生成文档

优先生成需求、设计、计划、测试、审查文档，而不是一开始直接写代码。

### 原则 3：发现缺失必须回写

当发现需求、设计、接口、数据或测试信息缺失时，必须回写问题，不能假装信息完整。

### 原则 4：禁止一次性大规模修改

默认采用小步迭代，禁止一次性大规模跨模块修改，除非用户明确要求且已完成必要设计。

### 原则 5：重大变更必须先计划

任何重大改动必须先给出计划和影响分析，再开始编码。

### 原则 6：复杂设计必须先研究

复杂、高风险或长期演进任务，必须先通过 `design-researcher` 完成 design-research gate，再进入架构设计和任务拆分。

### 原则 7：禁止文档形式主义

文档不是阶段通行证。若文档没有证据、取舍、风险和验证策略，即使结构完整，也视为未完成。

------

## 17. Agent 默认行为

```
1. 识别任务类型
2. 读取 PROJECT_REGISTRY.md 并扫描既有任务目录
3. 判断续接已有任务还是创建新任务
4. 执行 AI Rules Compliance Gate，读取 `.ai_rules/README.md` 和相关专项规则
5. 选择 core skill
6. 判断是否需要 superpowers 增强
7. 读取长期项目 CURRENT_FOCUS.md（如适用）
8. 判断是否触发 design-researcher / design-research gate
9. 检查 `.ai_rules` 适用规则是否已写入任务文档和 TODO
10. 检查会话关键结论是否已回写到文档
11. 检查 TODO 是否达到可执行规格
12. 严格按流程推进
13. 写入文档
14. 更新 STATUS.md、CURRENT_FOCUS.md 和 PROJECT_REGISTRY.md
```

------

## 18. 不允许的行为

AI Agent 不允许：

- 跳过需求直接编码
- 在没有 `TODO.md` 的情况下大范围开发
- 修改多个模块但不说明原因
- 隐藏测试失败
- 隐藏缺陷
- 伪造测试结果
- 修改历史文档结论而不留痕迹
- 把假设内容伪装成已确认事实

- 使用 superpowers 替代 core
- 并行执行但未做依赖分析
- 在复杂任务中没有现状证据就直接给最终设计
- 没有方案对比就进入任务拆解
- 用低信息密度文档推进阶段状态
- 为了通过测试而破坏模块边界或职责边界
- 把关键需求、设计、接口、数据或任务细节只保存在会话上下文中
- 使用过于简洁的 TODO 直接进入编码
- 在 TODO 没有关联需求、设计、测试和验收标准时执行实现
- 未读取 `.ai_rules/README.md` 和相关专项规则就进入计划、编码、测试或审查
- 只口头声明遵守 `.ai_rules`，但未在 TODO、TEST_REPORT、REVIEW 或 RELEASE 中记录检查结果
- 未读取 `PROJECT_REGISTRY.md` 和既有任务元信息就新建任务目录
- 为同一长期任务重复创建多个 `task_name` 目录
- 续接任务后不更新 `CURRENT_FOCUS.md` 和 `PROJECT_REGISTRY.md`

------

## 19. Agent 自检清单

在任务完成前，必须自检以下事项：

```
是否生成需求文档
是否生成设计文档
是否生成 Prompt 设计文档（涉及 Prompt / AI 推理链路时）
是否生成任务规划
是否执行测试
是否完成 Spec / TDD 绑定
是否完成复杂任务设计研究
是否记录方案对比和不采用方案
是否读取 PROJECT_REGISTRY.md 并完成任务归属判断
是否读取 `.ai_rules/README.md` 和相关专项规则
是否在 TODO 中记录适用 `.ai_rules`、规则检查点和验收方式
是否将会话关键结论回写到文档
是否确认 TODO 已达到可执行规格
是否验证架构约束
是否在 TEST_REPORT 和 REVIEW 中记录 `.ai_rules` 合规验证结果
是否完成代码审查
是否更新 STATUS.md
是否更新 CURRENT_FOCUS.md（长期项目适用）
是否更新 PROJECT_REGISTRY.md（新建或续接任务适用）
是否更新下一次对话提示词模板（Next Prompt Handoff Gate）
是否确认 NEXT_PROMPT.md、CURRENT_FOCUS.md、STATUS.md 和 PROJECT_REGISTRY.md 进度一致
是否记录风险与验证结果
是否合理使用 superpowers
```

------

## 20. 默认语言

以下内容默认使用中文：

- 文档
- 设计说明
- 分析报告
- 审查结论
- 缺陷报告
- 发布说明
- 总结沉淀

代码、命令、路径、模块名保持原始技术格式。

### 20.1 交互确认前缀

为让用户快速确认当前会话已读取并遵守 `AGENTS.md`，AI Agent 每次与用户交互时，回复开头必须使用固定确认前缀：

```text
好的，Fantasy。
```

要求：

- 该前缀必须出现在每次面向用户的自然语言回复开头。
- 不得写成其他称呼或省略。
- 工具调用本身不需要包含该前缀，但工具调用前后的用户可见说明必须包含。
- 如果因特殊格式输出不适合完整句式，也必须至少保留 `好的，Fantasy`。
- 该前缀只作为规则读取确认，不代表任务已经完成或验证已经通过。

### 20.2 下一次对话提示词模板

为降低新会话、上下文压缩或长期任务切换导致的任务漂移，每次工程任务对话结束前，AI Agent 必须为下一次对话创建或更新提示词模板。

该规则是强制收尾门禁，命名为 **Next Prompt Handoff Gate**。

适用范围：

- 所有工程任务均适用，包括新功能、Bug 修复、重构、测试补充、发布检查、规则文档修改和 skill 修改。
- 长期任务、跨模块任务、Prompt / AI 推理链路任务必须写入任务级和全局模板。
- L1 简单工程任务如确实没有任务目录，必须至少在最终回复中给出轻量续接提示词，并说明未写入文件的原因。

执行时机：

- 每个 TASK 阶段性完成时，必须同步更新任务级模板。
- 每次用户对话即将结束且本轮有工程进展时，必须同步更新任务级模板和全局最近会话模板。
- `release-manager` 和 `knowledge-curator` 收尾前必须检查该门禁；未更新模板不得视为任务完成。

任务级模板路径：

```text
ai_workspace/projects/{task_name}/00_meta/NEXT_PROMPT.md
```

全局最近会话模板路径：

```text
ai_workspace/NEXT_CONVERSATION_PROMPT.md
```

模板至少必须包含：

- 当前任务归属：`task_name`、任务中文名、任务目录。
- 当前阶段：所处 core skill、状态、是否阻断。
- 最近完成事项：仅记录真实完成内容，不得伪造测试或发布结论。
- 下一步动作：下一次对话应进入的 core skill 或质量门禁。
- 必须读取文件：`AGENTS.md`、`PROJECT_REGISTRY.md`、`CURRENT_FOCUS.md`、相关需求 / 设计 / TODO / 测试文档。
- 当前质量门禁：Spec Gate、Design Research Gate、Prompt Gate、Plan Gate、TDD Gate、Review Gate 或 Release Gate。
- 用户可直接发送的提示词模板：用一段可复制文本说明下一次要做什么、从哪里续接、不能跳过什么。

用户可直接发送的提示词必须满足：

- 以当前真实任务进度为准，不得照抄过期 `NEXT_PROMPT.md`。
- 明确“只执行哪个 TASK / 阶段”，并写清不要跳到哪些后续任务。
- 明确必读文件、当前门禁、验证命令或验证方式。
- 明确需要回写哪些任务文档、状态文件和注册表。
- 对存在阻断、未执行测试或环境限制的情况，必须直接写入提示词，不能隐藏。

如果当前对话不属于工程任务，至少应在回复中给出轻量的“下一次对话提示词参考”。如果属于工程任务，必须优先写入上述文件。

禁止行为：

- 只在回复中口头提醒，不写入任务级或全局模板。
- 模板只写“继续上次任务”，没有任务目录、必读文件和下一步动作。
- 模板声明未验证完成项或隐藏阻断风险。
- 任务已经完成、测试已经执行或用户要求收尾时，跳过 Next Prompt Handoff Gate。
- `NEXT_PROMPT.md`、`CURRENT_FOCUS.md`、`STATUS.md` 和 `PROJECT_REGISTRY.md` 互相矛盾且不修正。

------

## 21. 优先级规则

```
AGENTS.md
> lnzz-skills/core
> 用户任务
> lnzz-skills/superpowers
```

------

## 22. 示例

### 示例 1：新功能

用户输入：

```
为 product-auth 模块新增用户注销接口
```

Agent 默认应先走：

```
task-bootstrapper
→ requirement-analyst
→ requirement-clarifier
→ design-researcher
→ system-architect
→ prompt-designer（涉及 Prompt / AI 推理链路时）
→ api-designer
→ data-designer
→ task-planner
```

完成规划后，再进入开发阶段。

### 示例 2：缺陷修复

用户输入：

```
修复 product-gateway 模块鉴权失败问题
```

Agent 默认应先走：

```
bug-triager
→ test-writer（优先补复现测试）
→ bug-fixer
→ test-executor
→ code-reviewer
```

如果修复过程中发现需求或设计问题，再回退到对应阶段补文档。

------

## 23. 项目规则扩展

### 23.1 规则文件清单

AI 在执行任务时，必须同时遵守：

- `.ai_rules/README.md`
- `.ai_rules/CODING_STYLE.md`
- `.ai_rules/DB_STYLE.md`
- `.ai_rules/API_STYLE.md`
- `.ai_rules/PROJECT_STRUCTURE.md`
- `.ai_rules/SERVICE_STYLE.md`
- `.ai_rules/COMMENT_STYLE.md`
- `.ai_rules/LOGGING_STYLE.md`

### 23.2 强制执行方式

本节规则必须通过 `AI Rules Compliance Gate` 强制执行，不能只作为参考材料。

执行要求：

1. 开始工程任务时，必须读取 `.ai_rules/README.md`。
2. 根据任务类型读取相关专项规则文件。
3. 在 `TODO.md` 中写明每个 TASK 适用的 `.ai_rules` 文件和检查点。
4. 在编码后说明已遵守的规则文件清单。
5. 在 `TEST_REPORT.md` 中记录规则验证方式和结果。
6. 在 `REVIEW.md` 中审查规则是否真正执行。
7. 在 `RELEASE.md` 中给出规则门禁结论。

如果缺少以上记录，默认视为 `.ai_rules` 未执行，不得进入下一阶段。

### 23.3 职责分离

规则职责必须保持分离：

- API 相关规则写入 `API_STYLE.md`
- 数据库相关规则写入 `DB_STYLE.md`
- 通用编码规则写入 `CODING_STYLE.md`
- 包结构和分层规则写入 `PROJECT_STRUCTURE.md`
- Service 专项规则写入 `SERVICE_STYLE.md`
- 注释专项规则写入 `COMMENT_STYLE.md`
- 日志专项规则写入 `LOGGING_STYLE.md`

禁止将所有新增规则堆叠到既有三个 rules 文件中。

## 24. 最终目标

确保 AI 在本仓库中具备以下能力：

- 可控的软件工程流程
- 完整的文档追溯能力
- 有证据、有取舍的深度设计能力
- 可重复执行的开发过程
- 低风险代码修改机制
- 明确的验证、审查和发布闭环

- 可扩展的 AI 能力体系（Superpowers）
