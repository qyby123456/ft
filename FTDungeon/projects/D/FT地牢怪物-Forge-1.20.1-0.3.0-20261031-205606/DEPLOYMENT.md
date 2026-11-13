# 本机部署记录 · FT地牢怪物 0.3.0

## 当前版本

- 部署时间：2026-10-31 20:53:40，Asia/Shanghai。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.3.0.jar`。
- 构建来源：`D:\ft工程文件\FT-Dungeon-Mobs-Forge-1.20.1\build\libs\ft-dungeon-mobs-forge-1.20.1-0.3.0.jar`，187220 字节。
- SHA-256：`2fa073396d43b4660b39346df435306a0ee30bb26cbacedca854882a474c21fb`，构建与部署一致。
- 上一版 JAR 及原配置备份目录：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\ft-mod-backups\ftdungeonmobs\20261031-205338`。
- 保留本模组现有难度配置的所有原字段与数值，仅补充 `[arenaTorch] radius = 32`。没有覆盖为配置示例。
- 活动 `mods/` 中本模组仅一份；其余 360 个模组 JAR 的文件名、大小与修改时间保持不变。未更改其他配置、前置或玩家存档。
- 机器可读记录：`build/reports/deployment-0.3.0.json`。

启动或重启该 Forge 整合包，在“FT地牢怪物”或“功能方块”创造栏获取“竞技场长矛火炬”，也可执行 `/give @s ftdungeonmobs:arena_torch`。留出两格高度放置，右键上下任一半部即可按 1→2→3→4→5→1 调级，火焰依次为蓝、绿、金、紫、红。默认控制半径32格内自动分级的 FT 怪物；完整规则、配方与接口见 `ARENA_TORCH.md`。

已完成 Forge 构建、实际资源解析、两组独立 Forge GameTest 以及 Blockbench 结构、动画帧和 Display 核对。未启动完整整合包客户端，不将上述检查描述为整合包内战斗与多人联机均已实测。

若要回滚，先退出游戏并完整备份存档，再将当前 0.3.0 移出 `mods/`，从上述备份目录恢复 0.2.0 和原配置，只保留一版。本次新增了注册方块与物品，旧版无法识别已放置或持有的火炬，不建议在放置后直接降级读取唯一存档。

## 0.2.0 历史部署记录

以下路径与状态仅记录上一次部署，不代表当前活动版本。

- 部署时间：2026-10-31 19:43:37，Asia/Shanghai。
- 目标版本：Minecraft 1.20.1，Forge 47.4.10；不是 NeoForge 整合包。
- 已安装 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.2.0.jar`。
- 构建来源：`D:\ft工程文件\FT-Dungeon-Mobs-Forge-1.20.1\build\libs\ft-dungeon-mobs-forge-1.20.1-0.2.0.jar`。
- 文件大小：160119 字节。
- SHA-256：`8f8a152e974c1fc2ef4e6a8703d88a4f8ff16542e94e53f2fe6c9c1f72b96c9e`，构建文件与部署文件一致。
- 新增的配置入口：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\config\ftdungeonmobs-common.toml`。
- 旧版备份：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\ft-mod-backups\ftdungeonmobs\20261031-194333\FT地牢怪物-Forge-1.20.1-0.1.1.jar`。

## 游戏中使用

启动或重启该 Forge 整合包，进入创造模式，翻页找到“FT地牢怪物”分类。分类图标为金色副色的骷髅王刷怪蛋；四种蛋顺序为骷髅战士、骷髅弓手、骷髅法师、骷髅王。也可从原版“刷怪蛋”分类或搜索“骷髅”找到。

右键地面生成对应怪物，右键原版刷怪笼设置其实体类型。命令取得方式及伤害接入说明见 `README.md`。没有添加自然生成规则。

属性编辑与地牢接口见 `DIFFICULTY.md`。默认简单 / 普通 / 困难对应一 / 二 / 三档，四、五档可用 `/ftdungeon difficulty set <targets> <tier>` 指定。修改上述 TOML 后执行 `/ftdungeon difficulty reload`，立即重新读取并应用配置。

## 部署范围与验证

- 只替换本模组旧 JAR，并新增本模组自己的初始配置；没有删除、替换或重命名其他模组，没有改动其他配置、玩家存档或四份原始 bbmodel。
- 目标整合包已存在 GeckoLib 4.8.4、Iron's Spells 'n Spellbooks 3.16.3、Curios、Player Animator 和 Iron's Lib 依赖，本次没有重复安装前置。
- 已完成资源核对、GeckoLib 实际模型/动画解析、Forge 编译与重混淆、独立 `runData` 依赖加载、真实 Forge GameTest 服务端难度系统验证及部署后 SHA-256 核对。
- 未启动完整整合包客户端，不将以上验证等同于游戏内刷怪、战斗、多人联机和其他模组交互均已实测。
- 机器可读记录：`build/reports/deployment-0.2.0.json`。

模型提供者仅标注为“一颗大豆，b站uid355533780”，没有登记为模组代码作者。

需要回滚时，先退出游戏并备份存档，再移出当前 0.2.0 JAR，将上述独立备份目录中的 0.1.1 JAR 放回 `mods/`，不要同时放两版，也不要移除其他模组共用的前置。回滚后旧版不会消费难度配置和新接口；已经保存的增强实体不保证回退后的数值与新版相同。
