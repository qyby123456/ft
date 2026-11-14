# 污秽虹冠：正式武器接入

2026-09-16，采用用户确认的第一版重锤外观。

## 物品

| 项目 | 首版设定 |
| --- | --- |
| 注册 ID | `ftboss:compost_crown` |
| 中文 / 英文 | 污秽虹冠 / Compost Crown |
| 基础攻击伤害 | 14 点，含玩家基础 1 + 武器参数 10 + FTWeaponTier 材质 3 |
| 基础攻击速度 | 0.8 次/秒，玩家基础 4 加修正 -3.2 |
| 耐久 / 修复 | 1500，铁锭修复，沿用 FTWeaponTier |
| 稀有度 | EPIC |
| 命中效果 | 中毒 I，80 tick = 4 秒，仅服务端施加 |
| 获取 | 创造栏武器区、指令、击败炫彩答辩超人 |

使用现有重锤的 `SwordItem` 路径，正常消耗耐久，支持原版剑类附魔和攻击冷却。没有额外右键技能、范围毒云或自发光渲染器。只对主命中目标附加毒效，不对扫击的旁观目标额外施毒，不伤害持有者。刷新中毒遵循原版效果合并，不增加等级，仍尊重亡灵和 Boss 等目标的中毒免疫。

## 掉落

新增 `data/ftboss/loot_tables/entities/compost_boss.json`，一次正常死亡掉落一把；不依赖抢夺等级，不重复添加到中央武器掉落钩子。原版生物掉落规则关闭时不会掉落本武器。怜悯离场采用 `discard()`，不走死亡战利品流程，因此不额外奖励武器。

不恢复此前取消的炫彩肥料掉落，不修改石像、唱片、Boss 本体、生命阶段及其他武器。

## 模型与源文件

- 正式模型：`src/main/resources/assets/ftboss/models/item/compost_crown.json`。
- 正式贴图：`src/main/resources/assets/ftboss/textures/item/compost_crown.png`。
- 可编辑工程：`art/compost_crown/compost_crown.bbmodel`，已内嵌 PNG。
- 设计图：`art/compost_crown/compost_crown_design.png`。
- 生成器：`tools/model3d/compost_crown/build_model.py` 与 `render_preview.py`。

保留已确认的 187 部件 / 512² 图集 / 八槽显示变换；主体约 3 px/模型单位，较工程一般 1～2 的目标略密，本次不擅自重绘已确认方案。正式静态模型没有 `item/generated`、`item/handheld` 或 `builtin/entity` 父链，不会把展开图集当成平面图标。

这次是沿用确认稿，不运行会重出全部武器的 `gen_weapons3d.cjs`。没有把木斧 CustomModelData 映射装入正式模组。

```powershell
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
& $python -B tools/model3d/compost_crown/build_model.py
& $python -B tools/model3d/compost_crown/render_preview.py
```

默认只生成到 `art/compost_crown/`。检查结果后，需要覆盖正式模型及贴图时再给 `build_model.py` 加 `--install`。运行前应另存任何手工修改的模型；生成器不会回读手改的 Blockbench 工程。

## 验收与部署

```powershell
node tools/model3d/lint_java_model.cjs src/main/resources/assets/ftboss/models/item/compost_crown.json
node tools/model3d/check_uv_coverage.cjs src/main/resources/assets/ftboss/models/item/compost_crown.json src/main/resources/assets/ftboss/textures/item/compost_crown.png
```

构建后用 `tools/deploy_jar.ps1 -NoBuild` 部署；该脚本检测游戏进程，游戏运行时拒绝覆盖。不得使用 `-Force` 绕过。不能把 Gradle Daemon 当作正在运行的 Minecraft。

装入新版模组后直接领取：

```mcfunction
/give @p ftboss:compost_crown
```

不需要启用旧木斧预览资源包。实机验收包括：创造栏名称、左右手第一/第三人称、GUI/展示框/掉落物、普通目标中毒、亡灵免疫、耐久消耗及修复、Boss 正常死亡只掉一把、关闭 doMobLoot 不掉武器、怜悯离场不掉武器。离线预览和构建成功不等同于已经通过实机握持或战斗验收。
