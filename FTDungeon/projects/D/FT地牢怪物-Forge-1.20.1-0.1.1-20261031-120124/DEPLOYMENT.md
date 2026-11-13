# 本机部署记录 · FT地牢怪物 0.1.1

- 部署时间：2026-10-31 12:00:43，Asia/Shanghai。
- 目标版本：Minecraft 1.20.1，Forge 47.4.10；不是 NeoForge 整合包。
- 已安装 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.1.1.jar`。
- 构建来源：`D:\ft工程文件\FT-Dungeon-Mobs-Forge-1.20.1\build\libs\ft-dungeon-mobs-forge-1.20.1-0.1.1.jar`。
- 文件大小：134428 字节。
- SHA-256：`a5e824a230bb08f8d6c49f6b93f03d346951572892b0a70552fc9a1718f4dc6c`，构建文件与部署文件一致。

## 游戏中使用

启动或重启该 Forge 整合包，进入创造模式，翻页找到“FT地牢怪物”分类。分类图标为金色副色的骷髅王刷怪蛋；四种蛋顺序为骷髅战士、骷髅弓手、骷髅法师、骷髅王。也可从原版“刷怪蛋”分类或搜索“骷髅”找到。

右键地面生成对应怪物，右键原版刷怪笼设置其实体类型。命令取得方式及伤害接入说明见 `README.md`。没有添加自然生成规则。

## 部署范围与验证

- 仅新增该 JAR；没有删除、替换或重命名其他已有模组，没有改动配置、玩家存档或四份原始 bbmodel。
- 目标整合包已存在 GeckoLib 4.8.4、Iron's Spells 'n Spellbooks 3.16.3、Curios、Player Animator 和 Iron's Lib 依赖，本次没有重复安装前置。
- 已完成资源核对、GeckoLib 实际模型/动画解析、Forge 编译与重混淆、独立开发环境的 `runData` 依赖加载及部署后 SHA-256 核对。
- 未启动完整整合包客户端，不将以上验证等同于游戏内刷怪、战斗、多人联机和其他模组交互均已实测。
- 机器可读记录：`build/reports/deployment.json`。

模型提供者仅标注为“一颗大豆，b站uid355533780”，没有登记为模组代码作者。

需要撤销部署时，先退出游戏，再移出上述 `FT地牢怪物-Forge-1.20.1-0.1.1.jar`；不要移除其他模组共用的前置。存在本模组实体或物品的存档应先备份，避免在缺少该模组时直接继续游玩。
