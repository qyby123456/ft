# 未重做旧物品清理

2026-09-17，按用户点名的旧物品清单移除以下五件物品，不是仅从 JEI 隐藏。

| 名称 | 原注册 ID |
|---|---|
| 茶刃 | `ftboss:tea_leaf_blade` |
| 糖霜权杖 | `ftboss:sugar_scepter` |
| 屠夫剁骨刀 | `ftboss:butcher_cleaver` |
| 炫彩肥料 | `ftboss:rainbow_fertilizer` |
| 故障核心碎片 | `ftboss:defect_fragment` |

- 删除物品注册、实现、创造栏条目、语言条目、物品模型及剁骨刀的两份刀具标签。
- 故障机器人不再生成旧碎片；死亡爆炸、清场、音乐释放和石像掉落不变。
- 旧武器生成器不再生成前三件；构建排除五件旧物品的模型与贴图，保留历史贴图源文件用于回退。
- 清理当前实例 JEI 收藏及查找历史中的旧碎片条目。
- 已重做的武器、Boss 召唤物、石像、唱片及整合包正常农业肥料不变。未明确列入废弃清单的其他物品不推测删除。
- 旧存档中的这五类物品无法继续保留。载入前请备份存档；Forge 可能提示缺失注册项。

验证：`node tools/verification/check_removed_items.cjs`；隔离构建后检查 jar 中注册字节码、死亡路径以及旧模型、贴图和类文件均无残留。

回退备份位于实例 `_work/unfinished-item-removal-20260917/backup/`，包含原源码、资源、JEI 文件和部署前的 jar。

## 验收与部署

- 隔离构建 `BUILD SUCCESSFUL`；专项校验通过，解析 190 份 JSON，确认 12 件保留物品仍有注册与模型。
- 核验 255 个 class 和 385 份打包资源；五件旧物品的类、注册字符串、模型、贴图及数据引用不在成品中。
- 构建前后 593 份源码与资源一致，纳入源码中已有的玄蓝月武更新，没有用旧构建覆盖其他模型。
- 已通过 `deploy_jar.ps1 -NoBuild` 同步游戏实例、普通桌面 jar 及两处桌面成品目录；四处 SHA256 一致：`9AE79B85A4C3291F31BE114A9D8AA5EE491BA676EBEF4140AF9301C37BA6ACCD`。
- 没有启动游戏做实机验证。全量资源检查器对原有 `textures/entity/easter/xueli.png` 报 `unexpected end of file`；该文件与部署前 jar 字节完全一致，本次没有修改，不计为本次清理的通过项。
