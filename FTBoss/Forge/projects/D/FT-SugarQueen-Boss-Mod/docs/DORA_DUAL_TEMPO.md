# 朵拉：枪矛双持与致命节奏

本实现只调整朵拉（变体 2），保留机枪「地脉新星」外观和现有长枪物品模型。
完整数值见 `boss_reference/30_manor_shared.md` 的朵拉段落。

## 验证

- `node tools/easter_combat/verify_dora.cjs`：双持、伤害成功判定、同步姿势、调度、语言键与图标接线。
- 用 JDK 17 编译 `src/main/java/net/farmingtales/ftboss/util/DoraCombatTempo.java` 与 `tools/easter_combat/DoraTempoProbe.java`，运行 `DoraTempoProbe`：连续命中、射偏、目标切换、过期、叠层上限、各层射速与装弹冷却。
- `node tools/easter_combat/verify.cjs`：现有彩蛋 Boss 战斗回归检查。
- `gradle build --offline --no-daemon --console=plain`：编译、资源与重混淆 JAR 完整性检查。
- 图标可由 `node tools/easter_combat/gen_dora_icon.cjs` 重新生成。

## 游戏内验收

1. 本次成品已安全部署；启动客户端即可验收。需要重新安装时使用下面记录的快照部署脚本，运行中严禁替换模组 JAR。
2. 召唤朵拉，确认机枪与长枪始终分别在两手，装弹时也不消失或换手。
3. 在生存模式观察：近身副手突刺、远程枪击均推开玩家，墙后、创造与旁观玩家不用于叠层。
4. 连续有效命中三次后，Boss 血条出现「致命节奏 1/6」；继续命中到 6/6，射速、下一轮发数提高，冷却缩短。
5. 断开命中六秒后强化消失；同一子弹的物理和魔法两段不能重复叠层。
6. 检查射击与装弹姿态、突刺方向与客户端位移。离线检查不代替实机观感验收。

## 本次构建与部署记录

- 部署时间：2026-09-17 10:03，已确认无 Minecraft 客户端进程，通过现有安全部署脚本完成实例与三份桌面成品同步，清理 Lightspeed 缓存。
- 完整源码构建受既有 `ButcherBossModel.java` 调用 `MeatMountainEntity.getMeleeAge(float)` 缺失接口阻塞。本次未修改该模块，采用定向编译、Forge 重混淆和白名单合包，保留已安装屠夫版本。
- 共享实体同时包含功底的并行修改，因此定向编译包含其现有依赖、渲染与伤害类型资源，不丢失相关接线。未改动这些依赖源码。
- `DoraTempoProbe` 53 项检查通过；朵拉接线、既有彩蛋战斗、功底接线及文档数值核验均通过。
- 增量构建成功，校验 24 个增量 JAR 条目；合包包含 814 个条目，应用 23 个白名单条目，其余条目内容逐字节保留并核验。
- 成品 SHA-256：`1DBF54BFC21B6BD7B2A7BA1B4ABF442E723F2F1364D25985BB310D586D48981C`。
- 成品：`C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales Forge/_work/dora-dual-tempo-20260917/project/build/libs/qys-bosses.jar`。
- 回装命令：`& "C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales Forge/_work/dora-dual-tempo-20260917/project/tools/deploy_jar.ps1" -NoBuild`。主工程构建目录另有并行产物，未覆盖，回装时勿误用。
- 增量入口：`tools/easter_combat/dora_overlay.gradle`；白名单合包：`tools/easter_combat/package_dora.ps1`。基础 JAR 留存在快照同级的 `baseline-for-deploy.jar`。
