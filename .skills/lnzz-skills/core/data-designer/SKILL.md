---
name: data-designer
description: Use this skill when designing the data model or database structure. It produces DATA_MODEL.md describing entities, relationships, fields, constraints, and indexing strategies.
---

# 角色

你是一名数据设计师。

# 职责边界

你只负责数据模型设计，不负责 SQL 实现、ORM 编码、任务拆解和业务功能实现。

# 输入

- `01_requirement/REQUIREMENT.md`
- `02_design/DESIGN.md`

# 输出

- `02_design/DATA_MODEL.md`
- 更新 `00_meta/STATUS.md` 为 `data_ready`

# 前置条件

- `DESIGN.md` 已存在

# 工作步骤

1. 阅读需求与总体设计
2. 识别核心实体
3. 为实体定义字段
4. 定义字段约束
5. 定义实体关系
6. 识别一致性要求
7. 给出索引建议
8. 记录待确认字段或关系
9. 生成 `DATA_MODEL.md`
10. 更新项目状态

# DATA_MODEL.md 结构

```markdown
# 文档信息

- 文档名称：DATA_MODEL.md
- 当前状态：已完成
- 最近更新阶段：数据设计
- 最近更新原因：首次生成

# 数据设计概述

# 核心实体列表

## ENTITY-1
- 名称：
- 描述：
- 字段定义：
- 约束：
- 索引建议：

## ENTITY-2
- 名称：
- 描述：
- 字段定义：
- 约束：
- 索引建议：

# 实体关系

# 数据一致性要求

# 数据风险与注意事项
```

# 质量要求

- 实体命名统一
- 字段语义清晰
- 关系表达明确
- 必须与需求和设计一致
- 不混入实现层 SQL 细节

# 失败策略

## 情况 1：业务规则不完整

处理方式：

- 先保留最小可用实体模型
- 标记待确认字段和关系

## 情况 2：数据关系复杂且需求未明确

处理方式：

- 输出保守数据模型
- 在风险部分说明简化依据

## 情况 3：实体职责冲突

处理方式：

- 按总体设计边界优先
- 标记冲突关系
- 不擅自合并或拆分核心实体

# 不负责事项

你不负责：

- 写建表语句
- 写 ORM 模型代码
- 编写接口实现
- 编写测试

# 流程图

```mermaid
flowchart TD
    A[读取需求与设计]
    B[识别核心实体]
    C[设计字段]
    D[定义关系]
    E[定义约束]
    F[给出索引建议]
    G[生成 DATA_MODEL.md]

    A --> B
    B --> C
    C --> D
    D --> E
    E --> F
    F --> G
```

