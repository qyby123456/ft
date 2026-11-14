# 糖果魔像·石像 贴图完善 & 注册导入检查报告

工程来源:zip《制作一个和糖类食物有关的boss模型 (1).zip》→ Fabric 1.21.1 模组 `mod_fe92d830`(糖果魔像 Boss + 1×1×2 石像方块)

## 一、注册/导入链路检查(全部通过 ✅)

| 环节 | 文件 | 状态 |
|---|---|---|
| 方块注册 | `ModBlocks.java` → `candy_golem_statue`(TallPlantBlock 上下半格联动) | ✅ |
| 方块物品 | 同名 `BlockItem` 注册,进「建筑方块」创造页 | ✅ |
| 初始化触发 | `GeneratedMod.onInitialize()` 主动触碰 `ModBlocks` | ✅ |
| 方块状态 | `blockstates/candy_golem_statue.json` half=lower/upper | ✅ |
| 上下半模型 | `models/block/candy_golem_statue_lower/upper.json`(16 盒,UV≤16 对应 128px 纹理) | ✅ |
| 物品模型 | `models/item/candy_golem_statue.json` | ✅ |
| 贴图 | `textures/block/candy_golem_statue.png`(128×128,与 models/ 源文件一致) | ✅ |
| 语言 | zh_cn:糖果魔像石像 / en_us 已含 | ✅ |
| 掉落/配方 | loot_table + recipe(3 石+1 糖) | ✅ |
| 成品 jar | `build/libs/mod_fe92d830-1.0.0-r3.jar` 内含全部资源(fabric.mod.json、blockstate、双半模型、贴图) | ✅ |
| UV 覆盖 | 逐一核验 16 个盒子全部引用面,无不透明破洞 | ✅ |

**导入方式**:把 `mod_fe92d830-1.0.0-r3.jar` 放进 **Minecraft 1.21.1 + Fabric Loader ≥0.19.3 + Fabric API** 的 `mods/` 文件夹。
⚠️ 注意:该工程是 **Fabric/1.21.1**;不能直接放进你们 Forge 1.20.1 的 FarmingTales 整合包(需另行移植或用 1.21.1 Fabric 实例)。

## 二、贴图"完善"处理

现状:原版贴图仅 4126px 着色(49 色),整体灰暗——平均亮度 0.51、平均饱和度 0.13(做旧过度、细节难辨)。本工作目录给出两个完善版(128×128,格式一致,可直接替换):

| 文件 | 说明 | 亮度/饱和度 |
|---|---|---|
| `candy_golem_statue_variantA_weathered.png` | **风化完善**(推荐默认):保留做旧质感,对比度+8%、饱和度×1.75、轻微提亮+暖化,裂纹与做旧细节更清晰 | 0.53 / 0.26 |
| `candy_golem_statue_variantB_fresh.png` | **鲜亮糖果**:饱和度×3 提鲜(像刚出厂的新雕像),适合想要糖果色更跳脱 | 0.53 / 0.44 |

**切换方法(二选一)**:
1. 直接替换:把选中的 png 另存为 `src/main/resources/assets/mod_fe92d830/textures/block/candy_golem_statue.png`(或解包后替换 jar 内同名文件);
2. 不覆盖原文件,把模型 JSON 里 `"0": "mod_fe92d830:block/candy_golem_statue"` 改成新文件名引用即可。

预览图 `preview_statue_3variants.png` 竖向排列:原版 / A / B(2× 放大,棋盘格底)。

## 三、后续可选

- 喜欢哪个方向后,我可以继续做:**逐面手绘精修**(脸谱漩涡、焦糖淋痕、糖豆纽扣、裂缝/青苔分布)——需要你把游戏内或 Blockbench 截图发我对照;
- 或把该石像方块**移植为 Forge 1.20.1**(适配你们 ftboss 糖霜女皇工程)。
