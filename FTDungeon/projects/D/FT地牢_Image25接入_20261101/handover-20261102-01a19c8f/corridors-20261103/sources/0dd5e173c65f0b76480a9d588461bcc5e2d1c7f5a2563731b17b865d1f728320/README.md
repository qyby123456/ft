# 地牢走道 · 40 张（一字贯通 · 中 / 短两档）

- **需求**：「由你发布任务和子线程绘制地牢走道图片（一字贯通）分为中，短两种（2~3区块，1区块）总计40个图片左右，交给image生成，注意风格不要太重复」。
- **交付**：**40 张**（中 M01–M20 = 2–3 区块长；短 S01–S20 = 1 区块长），统一 **一点透视、一字贯通**（镜头居中直视前方、无转弯无分岔）；1536×1024 · `gpt-image-2.5-flare` · high · n=1（AI 概念图，非实机）。
- **时间**：2026-11-03 09:22–09:38（Asia/Shanghai）。

![地牢走道 · 中（M01–M20）](out/corridors-medium-sheet.png)

![地牢走道 · 短（S01–S20）](out/corridors-short-sheet.png)

## 一、风格矩阵（避免重复）

每张由「**主题 × 状态 × 光源 × 特征**」组合（见 `out/corridor_index.json`，中/短各 20 张组合不重复）：
- **主题 20 种**：石砖 / 黑石深板岩 / 冰霜 / 地狱 / 末地 / 灾变暗铁 / 废弃矿道 / 蔓生废墟 / 被淹水廊 / 蛛网 / 档案书库 / 牢房 / 熔岩锻造 / 骨墓 / 真菌 / 水晶 / 铜机械 / 血腥祭坛 / 冰封牢狱 / 黑曜试炼
- **状态 6 类**：完好 / 轻损 / 蔓生 / 积水 / 焚毁 / 陈旧
- **光源 6 类**：壁火把 / 吊灯 / 灵魂灯 / 熔岩辉光 / 末地烛 / 蜡烛
- **特征 10 类**：柱列 / 侧龛 / 破拱 / 铜管铁链 / 地面排水沟 / 铁门 / 雕像龛 / 吊笼 / 破旗 / 裂壁画

## 二、生成与执行（如实）

- **派发**：原计划 3 线程分开绘制（线程A 13 / 线程B 13 / 线程C 14）；线程B 会话已归档无法投递，其 13 张由 Lead 兜底。
- **API 故障与绕法**：当日 imagegen CLI 返回 `b64_json=None`（服务端只返 URL）→ `Invoke-Image.ps1` 全部报 `b64decode NoneType`；改用 `node gen.mjs --url` 生成、按目标名重命名成功；已通知线程 A/C 同样绕法。
- **实际落盘**：40/40 均在 `out/`（Lead 兜底批量为主，`gen.mjs --url` 6 并发；线程A 报告补落 M05 及其余存在性跳过；线程C 参与部分）。生成前存在性检查，无覆盖冲突/无重复文件；线程A 有 2 次撞车请求被丢弃（未重复扣费）。
- 线程A 回执 13/13 在盘、失败 0；Lead 最终核验 40 张全部 1536×1024 RGB PNG 可解码。

## 三、文件

- 两张总览：`out/corridors-medium-sheet.png`（2160×1526）、`out/corridors-short-sheet.png`（2160×1526）
- 40 张原图：`out/M01-*.png` … `out/S20-*.png`（40 个）
- 提示词 40 条：`prompts/`；风格/构图规格：`SPEC.md`；派发清单：`DISPATCH.md`
- 机器可读：`out/corridor_index.json`（每张主题/状态/光源/特征）、`out/corridors_meta.json`（路径/尺寸/字节/sha256）、`manifest.json`

## 四、清单（40 张）

| id | 文件名 | 体积 | sha256 |
| --- | --- | --- | --- |
| M01 | M01-nether-intact | 1.75 MB | `625157979074e1ce…` |
| M02 | M02-archive-library-flooded | 1.84 MB | `96e45b7d9176fdc5…` |
| M03 | M03-blood-ritual-intact | 1.61 MB | `2bd12059b40ca01c…` |
| M04 | M04-end-flooded | 2.05 MB | `537f369f2598d7c3…` |
| M05 | M05-prison-cells-intact | 1.90 MB | `40b8d7e4dd5dffbf…` |
| M06 | M06-frozen-prison-burnt | 2.00 MB | `3a7bc9bffe8a727c…` |
| M07 | M07-cataclysm-dark-iron-damaged | 2.00 MB | `7aa60a98ff9c54db…` |
| M08 | M08-forge-lava-burnt | 1.83 MB | `ea87fc89e452e478…` |
| M09 | M09-obsidian-trial-damaged | 1.89 MB | `485e717ae64f6bc8…` |
| M10 | M10-abandoned-mine-burnt | 1.97 MB | `f0bfc91e258dab25…` |
| M11 | M11-bone-crypt-overgrown | 1.98 MB | `78c7104c420851a6…` |
| M12 | M12-stone-brick-aged | 1.71 MB | `d525bbd2f9b7059a…` |
| M13 | M13-overgrown-ruins-overgrown | 1.91 MB | `1fdf0434a35e4261…` |
| M14 | M14-fungal-cave-aged | 1.82 MB | `aeea1468c5e12912…` |
| M15 | M15-blackstone-deepslate-overgrown | 1.74 MB | `501484bf3ef3bc46…` |
| M16 | M16-flooded-intact | 1.89 MB | `3dd8be10bea89290…` |
| M17 | M17-crystal-cave-flooded | 2.21 MB | `29a6f9575aae21c6…` |
| M18 | M18-ice-frost-intact | 1.85 MB | `8d2f06845f6e1310…` |
| M19 | M19-webbed-spider-flooded | 1.96 MB | `8f3c423cf62ca1d8…` |
| M20 | M20-copper-machinery-intact | 2.13 MB | `ba38b1a755aed266…` |
| S01 | S01-bone-crypt-overgrown | 1.96 MB | `31b3e9c04d8f1e6c…` |
| S02 | S02-stone-brick-aged | 1.66 MB | `e068ca984a82719d…` |
| S03 | S03-overgrown-ruins-overgrown | 1.88 MB | `f33bb90a2418cf5d…` |
| S04 | S04-fungal-cave-aged | 1.92 MB | `6160f7b8403ec6ee…` |
| S05 | S05-blackstone-deepslate-flooded | 1.57 MB | `07435538db7e9d23…` |
| S06 | S06-flooded-intact | 1.62 MB | `72daa317fc6e42fb…` |
| S07 | S07-crystal-cave-flooded | 1.91 MB | `7775b4e93df8aca4…` |
| S08 | S08-ice-frost-intact | 1.65 MB | `5d95e20ec07b1659…` |
| S09 | S09-webbed-spider-burnt | 1.98 MB | `eeb80b6a2cf8e64e…` |
| S10 | S10-copper-machinery-damaged | 1.95 MB | `c6a7c3600395f94a…` |
| S11 | S11-nether-burnt | 1.73 MB | `2bdda4f75bf87f19…` |
| S12 | S12-archive-library-damaged | 1.84 MB | `0aaf6c72391d8b31…` |
| S13 | S13-blood-ritual-aged | 1.66 MB | `91a5ac1e968b27ea…` |
| S14 | S14-end-overgrown | 1.98 MB | `55d41c576a5d4061…` |
| S15 | S15-prison-cells-aged | 1.92 MB | `f0c77a9be5e1d6db…` |
| S16 | S16-frozen-prison-overgrown | 1.78 MB | `253f2d0d39dd3a64…` |
| S17 | S17-cataclysm-dark-iron-intact | 1.95 MB | `18a1c608777d9f99…` |
| S18 | S18-forge-lava-flooded | 1.61 MB | `90f93164051207db…` |
| S19 | S19-obsidian-trial-intact | 1.82 MB | `420458b3957e67b0…` |
| S20 | S20-abandoned-mine-flooded | 1.73 MB | `f29b7371cb7f6000…` |

## 五、限制
1. AI 概念图，非实机截图；方块纹理/比例可能与游戏内不同，建造以 `SPEC.md`/提示词描述为准。
2. 服务端返回格式变化（无 b64）导致 CLI 失败，已改 `--url` 链路；所有失败请求无图、无半成品。
3. 线程B 归档未能参与，本批实际以 Lead 兜底为主。
