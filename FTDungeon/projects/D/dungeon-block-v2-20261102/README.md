# dungeon_block v2 · 修正后四视图导出

- 日期：2026-11-02 16:24（Asia/Shanghai）
- 输入（全部原样保存为数据文件，渲染器直接读取）：
  - `inputs/dungeon_block.json` —— Java Block 模型（单 cube `[0,0,0]`→`[16,16,16]`，含 **cullface**）
  - `inputs/dungeon_block_textures.json` —— 7 色 palette + `side/top/bottom` 三层 ASCII 16×16
  - `inputs/dungeon_block_four_views.json` —— 四视图画布/网格/六视角/字幕/修正清单
- 渲染器：`qa/render_v2.py`（纯 Pillow，数据驱动；Blockbench MCP 仍不可用，故离线按 JSON 渲染）

## 修正项验收（按你的三条诊断逐条核对）

| 修正 | 验收结果 |
|---|---|
| 灰浆提亮到 `#33333A`、**严格 1px**、无相邻深色加粗 | ✅ palette M 已用 `#33333A`；逐层扫描 **2×2 灰浆块 = 0**（side/top/bottom 全部 0），灰浆在每层只占单行/单列 |
| **真错缝砌法**（奇数带竖缝 0/8，偶数带 4/12，砖长 7px+1px 缝） | ✅ 由提供的 ASCII 直接复现：side 层竖缝列在 `{0,8}`（行1–3、9–11）与 `{4,12}`（行5–7、13–15）交替；横缝在行 0/4/8/12 各 1 行 |
| 苔藓成簇（≥2×2），无孤立单点；明度差 ≤6 级 | ✅ moss 像素：side 10 / top 53 / bottom 0，**孤立苔藓像素 = 0**（4 邻域检查）；palette 明度 51/78/92/103，共 4 级（≤6） |

> 说明：`g`（#4E6B39）在给定三层 ASCII 中未被使用（已定义保留）；三层实际使用 `M,a,b,c,G,H`。

## 输出

### 1) 适配 1536×1024 的四视图总表（规格画布）

![四视图 1536x1024](out/dungeon-block-fourview-1536x1024.png)

`out/dungeon-block-fourview-1536x1024.png`｜1536×1024｜sha256 `b4be9f2c49ef68b227054b69621d163e60d4e00a255219db0435fff221304743`

- 3 列 × 2 行：FRONT / RIGHT SIDE / BACK / TOP / DOWN / 3-4 ISO VIEW，标签与字幕按你的 four_views JSON
- 背景 `#F2F2F2`、caption 两行（模型说明 + 7 色 palette 图例）

### 2) 规格字面版（512px 单元 / pixel_scale 32）

![四视图 512 单元](out/dungeon-block-fourview-512cells.png)

`out/dungeon-block-fourview-512cells.png`｜1600×1176｜sha256 `73b41a7358f406d250d6344aea01da0adf8e4e0c014693c6c7fb7fd6f171a8a6`

> **画布数学说明（如实）**：你给的是 `1536×1024 + 3×2 格 512px + gap 8`，但 `3×512+2×8 = 1552 > 1536`、`2×512+gap+标签+字幕 > 1024`，两者无法同时满足；因此导出两版：**① 适配版严格 1536×1024**（单元 440、scale 27.5、标签/字幕齐全）；**② 字面版**保留 512px 单元/32 倍像素（画布相应放宽到 1600×1176）。

### 3) 3/4 等距单图

![iso](out/dungeon-block-iso.png)

`out/dungeon-block-iso.png`｜768×768｜sha256 `796051d470e4ec9e47362c030d4c6b4dae564657b2dffccab9da58742adf1a6`
- 正交投影 yaw 45° / pitch 30°，可见面 = **north + east + up**；明暗 0.82 / 0.66 / 1.0；黑色细描边

### 4) 贴图（由你的 palette+layers 1:1 还原）

| 文件 | 内容 | sha256 |
|---|---|---|
| `out/textures/dungeon_side.png` | 1px 灰浆、错缝砖、簇状苔藓 | `e6633e8f…` |
| `out/textures/dungeon_top.png` | 苔石顶面（簇状苔藓） | `a2b8c2fb…` |
| `out/textures/dungeon_bottom.png` | 深色石底 | `8f9582f2…` |

## 模型 JSON 核对

- 单 cube `[0,0,0]`→`[16,16,16]`，`texture_size [16,16]`
- 贴图引用 `#side/#top/#bottom`（对应 side/top/bottom PNG），六面 UV `[0,0,16,16]`
- 六面 **cullface** 均已设置（down/up/north/south/west/east）
- 未修改几何结构，仅按你给的 JSON 保存并用于渲染

## 文件

- 输入：`inputs/*.json`
- 渲染：`out/dungeon-block-fourview-1536x1024.png`、`out/dungeon-block-fourview-512cells.png`、`out/dungeon-block-iso.png`、`out/textures/*.png`
- 脚本：[qa/render_v2.py](qa/render_v2.py)（重跑即可复现）
- 清单：[out/manifest.json](out/manifest.json)（含 QA 结果与画布说明）

## 限制

- 仍为离线 Pillow 渲染（Blockbench 未连接），等距光照为简化双面明暗，非 Blockbench/游戏内光照
- 平视图按你的 `flip: none` 输出（FRONT/RIGHT/BACK 三面同 side 贴图的原始方向，未镜像）
