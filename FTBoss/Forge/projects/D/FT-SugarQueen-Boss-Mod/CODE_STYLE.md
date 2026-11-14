# 代码规范(FTBoss)

> 2026-09-12 统一, 目的是**方便管理**与减少低级错误。

## 文件与编码
- 文本文件一律 **UTF-8(无 BOM)**; 换行 **LF**; 文件末尾**恰一个换行**。唯一例外是需由 Windows PowerShell 5.1 直接解析且含中文诊断的 `tools/merge_jar_extras.ps1`，使用 UTF-8 BOM。
- 行尾不留空白; 连续空行最多 2 行(即 3 个 \n)。
- 缩进 **4 空格**(Java) / **2 空格**(JSON、.cjs 工具脚本); 不用 Tab。

## Java
- 缩进 4 空格; 行宽建议 ≤ 160。
- 每个类文件顶部保留**中文说明注释**: 用途 / 关键参数 / 修改历史(日期 + 原因)。
- 常量优先: 数值与时长写进 `private static final` 并注明单位(tick 或秒)。
- 客户端类统一放 `client/`(model / render 分目录); 工具类放 `util` 或 `integ`。
- 事件处理集中: 全局 tick 在 `BossHandler`, 死亡结算在 `BossMercy`, 属性强化在 `BossEmpower`。

## 包与职责边界
- `entity/` 只保留实体状态、同步数据、存档与生命周期；可复用战斗判定放 `boss/ai/`。
- `boss/` 负责战斗编排，`boss/ai/` 负责服务端战术组件；客户端渲染不得放入这两处。
- `item/` 存放物品状态与事件，具体武器放 `item/weapons/`；网络包只放 `network/`。
- `client/model/`、`client/render/` 分开；通用端代码不得静态引用客户端类。
- `util/` 只接受无注册副作用、可跨功能复用的纯规则/数学类；新功能不得继续把事件监听器或实体状态机堆入 `util/`。
- 每个 Java 文件只放一个 public 顶层类型，package 必须与目录一致；注册 ID、NBT 键和资源路径不能为了“整理”而改名。
- 单类超过约 1200 行时，新需求优先抽取同领域组件，不继续扩大神类；已有大类按可回归的功能边界逐步拆分。

## 构建校验代码
- `gradle/verification.gradle` 只做入口；具体任务按领域放在 `gradle/verification/*.gradle`。
- 可执行回归放 `tools/verification/`，临时修补脚本不得混入该目录。
- `checkProjectLayout` 会拒绝 package/路径不一致、运行源码中的备份文件、非法大小写资源路径与根目录构建日志。

## 资源
- 资源路径全小写 + 下划线; 贴图随模型同目录层级(`textures/entity`、`textures/block`、`textures/mob_effect`)。
- 石像类方块一律 `statue_<boss>` 命名; 每座必须有: 模型 4 件套 + blockstate + 战利品表 + 中英 lang + 创造栏 + 掉落。

## 工具脚本(`tools/**/*.cjs`)
- 一律**独立脚本文件**, 不用内联 `node -e`(会被 shell 引号/编码破坏)。
- 改动源码前先备份; 构建失败要能整文件还原。
- 生成器输出目录 `out_*` 属于临时产物, 清理前确认无需保留。
