# 雪黎 · 夜宴双器（模组内置版）

2026-09-17 更新：已新增独立可掉落玩家武器及技能、发光层，详见 `docs/XUELI_PLAYER_WEAPONS.md`。
下文保留 2026-09-16 的 Boss 外观接入说明；其中“不新增物品”只描述当时的外观版本。

2026-09-16：将已确认的平底锅与擀面杖模型内置到 `qys-bosses.jar`。

## 本次构建与部署

专属模型校验、独立 Forge 编译与 `reobfJar` 均通过。正式部署采用增量合并：只向最新已部署 JAR
加入两份客户端 class、两份模型 JSON 与四张 PNG，其余 717 个归档条目逐字节保持不变。
已通过安全部署脚本同步至实例与桌面四处，部署时 SHA-256 一致，旧雪黎资源包已停用，其他资源包不动。

整工程快照因其他会话尚在修改 ManorIntro / BlackCleaver 而出现无关编译错误；本次未修改这些代码，
也未把只含雪黎选择器的中间 JAR 当完整模组发布。主工程已保存完整源码接入，后续全量构建自然包含本功能。
实例工作目录 `_work/xueli-weapons-v1/builtin-deployment.json` 记录完整成品、备份、哈希和部署位置。
仍未进入游戏实测。

## 接入方式

- 雪黎仍装备 `farmersdelight:skillet` 与 `bakery:rolling_pin`，不新增物品，不修改技能、伤害、耐久或掉落。
- `XueliWeaponModel` 在客户端注册两份附加模型，并包装这两个物品的模型选择器。
- 渲染持有者为 `EasterBossEntity` 且同步变体为 `XUELI_VARIANT` 时，选择雪黎模型；既有存档与雪黎分身同样适用，不需要修改 NBT 或重新召唤。
- 其他持有者与普通物品完整委托原模型的 overrides，保留普通厨具的烹饪、柠檬猫专属 CMD 等行为。
- 两个模型包装器可以串联，不覆盖 `LemonCatWeaponModel`，不更改厨具模组资源或加载顺序。
- 原有独立资源包是全局替换版。安装本内置版后必须停用 `FT-Xueli-Kitchen-Arsenal-v1`，否则普通厨具仍会被旧包覆盖。
- 所有正式资源位于 `assets/ftboss`；没有额外运行时、OptiFine、光影或厨具模组编译依赖。

## 美术文件与维护

`art/xueli_weapons/` 包含内嵌贴图的两个 Blockbench Java 源文件、粒子贴图、预览、四视图及几何校验报告。
平底锅 86 个元素、5 个编辑组；擀面杖 46 个元素、6 个编辑组，各使用 128×128 图集。

```powershell
node tools/model3d/import_xueli_weapons.cjs
node tools/model3d/verify_xueli_weapons.cjs
node tools/model3d/lint_java_model.cjs src/main/resources/assets/ftboss/models/item/xueli/skillet.json src/main/resources/assets/ftboss/models/item/xueli/rolling_pin.json
```

导入器从 `.bbmodel` 还原 Java 模型和贴图，只输出这六个正式资源，支持传入独立输出目录。
内置模型不再保留独立资源包中平底锅的 `cooking` 覆盖：普通锅仍由原模型处理，Boss 专属外观为静态模型。

## 实机验收

重启后检查雪黎及分身的双手、挥击与附魔光效，并对照普通厨具、柠檬猫与原版烹饪状态。
仅为方便预览，也支持精确 `CustomModelData:160417` 标记；不会自动给任何装备写入此标签。

```mcfunction
/give @s farmersdelight:skillet{CustomModelData:160417}
/give @s bakery:rolling_pin{CustomModelData:160417}
```

标记物品使用静态外观，不显示原烹饪模型。普通未标记物品完全不受影响。
预览图来自实际几何与 UV 的离线渲染，不是游戏截图；实际持握、Boss 姿态和烹饪渲染仍需游戏验收。
