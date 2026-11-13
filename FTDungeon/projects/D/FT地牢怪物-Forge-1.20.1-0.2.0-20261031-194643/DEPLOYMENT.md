# 本机部署记录 · FT地牢怪物 0.2.0

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
