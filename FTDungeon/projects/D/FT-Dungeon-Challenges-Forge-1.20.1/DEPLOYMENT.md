# FT地牢挑战 1.0.0 部署

已新增到当前 FarmingTales 实例：

```text
C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢挑战-Forge-1.20.1-1.0.0.jar
```

- SHA256：`14f9bd510b8b0b550ebef442540874db0c5568552953222d2cab67f8ad4d6eae`。
- **只新增挑战模块**，保留已安装 FT地牢怪物0.5.0，未替换、回滚或重制它的模型。
- 部署前按真实 `mods.toml` 模组ID确认主模块唯一、挑战模块未重复；检查主模块与隔离验证所用JAR哈希一致。
- 部署后核对其他362个JAR文件名/大小/修改时间、主模块SHA256、原难度配置SHA256均不变。
- 原难度配置SHA256：`46da1c106667fa356ecfb3b4e05eae69653d77302299a0003622e60a80a82515`。
- 未启动正式游戏、未写玩家存档。新配置尚未生成，第一次加载本模块时由Forge生成 `config/ftdungeonmobs-challenges.toml`，不会覆盖原难度配置。
- 备份与清单目录：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\ft-mod-backups\ftdungeonchallenges\20261101-010015`。

## 生效与游玩

重新启动游戏/服务器加载新模块。多人服务器需要给客户端安装同版模块，以显示新增语言与提示。

普通右键火炬选档 → 双手空手潜行右键开战/加入 → 首波开始后不再招募。发起者用 `/ftchallenge stop` 取消；管理员用 `/ftchallenge cancel <x> <y> <z>`。

请先在测试竞技场实际游玩检查场地与数值，尤其是安全刷怪地面、骷髅王净高、领地交互权限、怪物接近路径和奖励经济。

## 撤除

卸载前取消活动挑战并正常关服。只移走 `FT地牢挑战-Forge-1.20.1-1.0.0.jar`，不要移除原FT地牢怪物JAR、删除原难度配置或批量清理存档。已发出的正常通关奖励不会自动追回。冷却SavedData可保留；未加载的崩溃残留敌人需要本模块加载才能识别清理。

## 部署工具

`tools/Deploy-Challenges.ps1` 默认仅演练，加 `-Deploy` 才落盘；可用 `-Instance` 显式选择实例。它拒绝覆盖已有挑战模块、拒绝与未验证的主模块混用、拒绝游戏客户端运行时部署；采用临时文件校验后重命名，异常时仅撤回自己刚增加且哈希吻合的JAR，不回滚其他模组的独立更新。
