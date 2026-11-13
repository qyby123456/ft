# dungeon_block.bbmodel · 四视图渲染

- 日期：2026-11-02 16:14–16:18（Asia/Shanghai）
- 输入：你给的 `dungeon_block.bbmodel`（Java Block 格式，单个 cube `[0,0,0]`→`[16,16,16]`；north/east/south/west→texture 0，up→texture 1，down→texture 2），已原样保存为本目录 `dungeon_block.bbmodel`
- **说明：Blockbench MCP 连接失败（fetch failed），未能在 Blockbench 内导入，改用本地 Pillow 渲染器按 bbmodel 的几何与贴图槽映射离线渲染**
- **贴图：你的 bbmodel 里 `textures: []` 没有带 PNG**，所以我按文件名画了 3 张 16×16 占位贴图（`textures/dungeon_side.png` / `dungeon_top.png` / `dungeon_bottom.png`，地牢石砖/苔石顶/深色底）用于四视图可视化；换成你的正式贴图后重跑 `qa/render.py` 即可（脚本直接读这三张文件）

## 交付

### 四视图总表（含 DOWN 与 3/4 预览）

![dungeon_block 四视图](out/dungeon-block-fourview-sheet.png)

`out/dungeon-block-fourview-sheet.png`｜1680×1254｜sha256 `5823b7ef22aee225ab5742770be6284d7a328448af5d609ae9cd601741c874b8`

- **FRONT / RIGHT SIDE / BACK**：你模型四面都用 side 贴图（slot 0），所以三视一致——这是模型本身设定，非渲染错误
- **TOP**：up 贴图（slot 1，苔石）
- **DOWN**：down 贴图（slot 2，深色石底）
- **3/4 ISO VIEW**：真正按 cube 几何+三张贴图做的等距渲染（顶+左右两面，硬边像素无插值）

### 3/4 等距渲染单图

![3/4 ISO](out/dungeon-block-iso.png)

`out/dungeon-block-iso.png`｜716×819｜sha256 `0dd4c5ab5ca78e8c5aee3259bde5544ba58f914adbac4d2b1674fbc5896c0043`

### 贴图清单

![贴图](out/dungeon-block-textures.png)

| 文件 | 用途 | 尺寸 | sha256 |
|---|---|---|---|
| `textures/dungeon_side.png` | north/east/south/west（slot 0） | 16×16 | `cc94738b…` |
| `textures/dungeon_top.png` | up（slot 1） | 16×16 | `c9b8e0af…` |
| `textures/dungeon_bottom.png` | down（slot 2） | 16×16 | `2387b1f7…` |

## 模型核对

- 几何 ✅ 单 cube 0–16 立方体、origin(8,8,8)、无旋转——四视图与 3/4 渲染均一致
- 贴图槽 ✅ 四面=side、顶=top、底=bottom，与 bbmodel 的 faces 映射完全一致
- UV ✅ 六面均 `[0,0,16,16]` 满贴图
- 备注：`outliner` 只有一个组 `dungeon_block` 包住 `dungeon_body`，无多余元素

## 在 Blockbench 里使用（按你原来的步骤）

1. 打开 Blockbench（Java Block 格式），导入 `dungeon_block.bbmodel`
2. 左侧「纹理」面板拖入 `textures/dungeon_side.png`、`dungeon_top.png`、`dungeon_bottom.png`
3. 选中立方体，把六面纹理按 north/east/south/west→side、up→top、down→bottom 指过去
4. 就得到本页四视图同样的效果（Blockbench 内即可旋转/正交查看）

## 文件

- 模型：[dungeon_block.bbmodel](dungeon_block.bbmodel)
- 渲染：`out/dungeon-block-fourview-sheet.png`、`out/dungeon-block-iso.png`、`out/dungeon-block-textures.png`
- 贴图：`textures/*.png`（占位，可替换）
- 渲染脚本：[qa/render.py](qa/render.py)（替换贴图后直接重跑）
- [out/manifest.json](out/manifest.json)

## 限制

- 未能在 Blockbench 内渲染（MCP fetch failed）；本渲染为离线复刻，光照是简化双面明暗（非 Blockbench 光照）
- 贴图为本地占位；换成正式贴图后可重跑得到准确四视图
- 若需要严格"前/右/后/顶"正交相机视图（只看到单个面），可以直接从 `dungeon_side/top/bottom` 取 16×16 放大——本表已按此方式给出
