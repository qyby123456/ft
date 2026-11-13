# S5 海底珊瑚结构模块 · 首批

本聊天一个组，九个独立 vanilla structure NBT。整体图片只作布局参考，不伪装为第十个可放置房间。不调用生图 API，无图片半写入，无源图修改。

## 权威来源与改造

优先读取 `D:/ft工程文件/结构参考_20261103/LEARNING_NOTES.md`、`SAMPLE_CATALOG.md`，实际解码五份 YUNG 海底神殿 NBT。`source-evidence.json` 保存绝对路径、SHA-256、真实尺寸、原始调色板、接口原始数据，以及每个新模块对应的源模块和尺寸缩减因子。源文件不复制、不修改、不重打包。

- lower_middle_chamber_1：35×23×25；沿用海晶主调和中央房间功能；新大厅缩减为 19×11×19。
- side_lower_chamber_dripstone_left_0：35×19×30；沿用附属房/支线功能；改为中房与祭坛支线，不复制滴水石机关。
- maze_chamber_battle_room_0：11×13×16；沿用迷宫模块尺度级别；转换为小房、直廊、拐弯，不插入战斗或刷怪。
- parthenon_0：50×24×25；沿用轴线、双列立柱与大厅/前厅功能；缩减为 FT 模块尺寸。
- top_dome_0：23×11×23；沿用穹顶采光空间尺寸；改为同外包尺寸、密封玻璃天井。
- YUNG `yung_jigsaw`、池权重、kelp 插槽和处理器不与 FT 原版模板兼容，未移植。蓝玻璃支腿占位不照搬。水平接口改成外置 JSON 中的 FT 北向中央门及相反朝向出口；无 jigsaw 方块。
- 海底概念图仅作青蓝/珊瑚风格参考。实际主路干燥；活珊瑚封装于有底、有壁、有盖玻璃水景。无实体、战利品、刷怪笼或游戏命令。

## 模块与连接

尺寸单位为方块，X×Y×Z；全部北向，坐标为本地玩家脚位，地板 y=0，接口净空 3×3。`room.json` 包含入口、安全点、出口和所有连接点。

| ID | 名称 | 尺寸 | 连接点 |
| --- | --- | --- | --- |
| NAT-S5-01 | 珊瑚环礁大厅 | 19×11×19 | north:9,1,0; south:9,1,18; east:18,1,9 |
| NAT-S5-02 | 海晶休憩室 | 15×9×15 | north:7,1,0; south:7,1,14 |
| NAT-S5-03 | 珊瑚守望龛 | 11×7×17 | north:5,1,0; east:10,1,8 |
| NAT-S5-04 | 潮汐柱廊 | 25×12×15 | north:12,1,0; south:12,1,14 |
| NAT-S5-05 | 海光直廊 | 11×8×17 | north:5,1,0; south:5,1,16 |
| NAT-S5-06 | 珊瑚转角廊 | 15×8×15 | north:7,1,0; east:14,1,7 |
| NAT-S5-07 | 海底遗迹前厅 | 17×10×15 | north:8,1,0; south:8,1,14 |
| NAT-S5-08 | 水镜天井庭院 | 23×11×23 | north:11,1,0; south:11,1,22; west:0,1,11 |
| NAT-S5-09 | 潮心祭坛宝库 | 17×10×17 | north:8,1,0; east:16,1,8 |

所有声明端口均已通过北入口到端口的干燥双格身高路径验证。旋转接入应同步变换 JSON 端口和朝向；本包不包含统一世界槽位或连接大厅施工。

## 交付及校验

每房：`room.nbt`、`room.json`、`materials.json`、`qa.json`、`native-qa.json`、`preview.png`、东南西北内部剖视、`exterior.png`。

- 静态检查 9/9：DataVersion 3465、坐标/尺寸、完整显式体积、地板/屋顶、所有门口 3×3 净空、干路连通、水体密封、珊瑚邻水、无实体与禁止方块。
- 原版实际加载 9/9：Minecraft 1.20.1 registry、方块属性、真实碰撞形状路径、StructureTemplate 读写回环。日志位于 `qa/native-0.log`、`qa/native-1.log`。
- 四向 PNG 从最终 NBT 生成；完整外壳不会被剖视修改。纹理软件渲染不等于 Forge 实机截图。Lodestone 交互链接从相同最终 NBT 读取，已校验九个端点的 SHA-256。
- **未完成：Forge 世界放置、旋转放置、液体/珊瑚 tick 稳定性和实机游玩。不得宣称已通过这些项目。**
- **未部署**：没有改 mods、配置、正式 JAR、世界存档、接收槽位或其它组。显式空气会清空放置体积，接入端必须先检查完整体积占用并拒绝冲突；禁止盲目覆盖。

## 复现与预览

1. 用已配置 Python 执行 `evidence.py`，再运行 `build.ps1`（调用已安装 minecraft-builder-skill 的 run-build.mjs）。
2. 执行 `verify.py` 生成静态、原版和纹理预览证据。
3. `node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs --nbt <本目录>/rooms/NAT-S5-01/room.nbt --host 127.0.0.1 --port 5195 --no-open`。
4. 其它模块使用相同命令但替换 NBT，端口按模块顺序为 5196～5203；执行 `package.py` 校验九个交互端点，生成 `manifest.json` 与 `lodestone-index.html`。端口若被占用需使用实际分配端口调整链接。

## 差异化修订

见 `DESIGN-DISTINCTIVENESS.md` 与 `distinctiveness-audit.json`。大房间改为错位双礁，中房改为 S 形隔墙，庭院新增可步行观景夹层；原版真实碰撞验证夹层安全点 [3,4,10] 可达。新版哈希和预览已更新。五个其它组的平面遮罩筛查存在两项需语义复核，不等于全局去重通过；G1/G3 与所有组终态仍须最终矩阵审计。无实际机关和刷怪战斗系统。
