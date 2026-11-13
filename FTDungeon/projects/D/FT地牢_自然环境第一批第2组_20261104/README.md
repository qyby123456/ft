# 第一批第2组：砂岩荒漠实际结构模块

状态：9 个原创改造模块已生成，静态 QA 与 Minecraft 1.20.1 原生加载/碰撞/往返序列化 9/9 PASS；尚未执行 Forge 世界四向放置、区块稳定性与实机游玩，不可标记最终接入通过。

本线程生成图片接口调用 0 次；没有生图半写入，无需清理。PNG 仅为最终 NBT 软件渲染，不是 AI 图或游戏截图。

## 权威依据

- `D:\ft工程文件\结构参考_20261103\LEARNING_NOTES.md`：交通网络、门洞、功能房、终止件分层；Jigsaw 与 YUNG 扩展运行时不混用。
- `D:\ft工程文件\结构参考_20261103\SAMPLE_CATALOG.md`：模板尺寸和资源证据索引。
- `D:\ft工程文件\FT地牢_自然环境第一批第2组_20261104\authority-evidence.json`：逐模块原始 NBT、SHA-256、原始尺寸/接口/调色板、沿用和改造点。构建前读取原始 NBT 并与目录尺寸和方块数核对。

原版石砖系对应砂岩、雕纹砂岩和切制砂岩；删除占位玻璃、刷怪与战利品运行层，不把处理器占位材质当成外观。原件仅只读研究，没有复制/再分发源 NBT、改源 JAR 或运行模组函数。

## 模块清单

尺寸为 X×Y×Z；北为 -Z，东为 +X；连接坐标为局部玩家脚部位置。北入口始终三宽三高、有实心地板；出口和法线见各自 room.json。

| 模块 | 名称 | 尺寸 | 接口方向 | Lodestone |
| --- | --- | --- | --- | --- |
| NAT1-G2-01 | 荒漠日轮祭坛 | 19×16×19 | north/south/east | [预览](http://127.0.0.1:5282/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=NAT1-G2-01) |
| NAT1-G2-02 | 砂岩蓄水间 | 13×8×13 | north/south/east | [预览](http://127.0.0.1:5284/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=NAT1-G2-02) |
| NAT1-G2-03 | 沙埋藏宝室 | 7×8×7 | north | [预览](http://127.0.0.1:5285/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=NAT1-G2-03) |
| NAT1-G2-04 | 双列柱大厅 | 19×8×13 | north/south/east | [预览](http://127.0.0.1:5287/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=NAT1-G2-04) |
| NAT1-G2-05 | 砂岩直廊 | 5×8×17 | north/south | [预览](http://127.0.0.1:5286/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=NAT1-G2-05) |
| NAT1-G2-06 | 砂岩直角廊 | 13×8×13 | north/east | [预览](http://127.0.0.1:5289/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=NAT1-G2-06) |
| NAT1-G2-07 | 荒漠守卫前厅 | 13×10×13 | north/south | [预览](http://127.0.0.1:5290/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=NAT1-G2-07) |
| NAT1-G2-08 | 绿洲天井庭院 | 19×16×19 | north/south/east/west | [预览](http://127.0.0.1:5291/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=NAT1-G2-08) |
| NAT1-G2-09 | 日轮宝库祭坛 | 19×16×19 | north | [预览](http://127.0.0.1:5292/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=NAT1-G2-09) |

## 差异化修订 R2

实际修改01/02/04/07/09的NBT：错位屏墙前厅、绕金顶日轮塔的双路、可登高西侧测日廊、02/04真实三向接口、侧路拉杆测水灯及屏墙后的宝库。详见DISTINCT_DESIGN.md六维说明、distinct-route-plan.json双环端口图和duplicate-audit-input.json跨组材质无关矩阵输入。

已对第3/7组当时可用的18个实际模板作只读快照比较；不是全组独特性认证。双环为端口兼容规划，尚未布置世界路线；拉杆电路静态存在，但刻更新未实测。

## 交付与边界

每模块目录包含 room.nbt、room.json、preview.png、materials.json、qa.json、内部与完整外壳四向图、构建日志。recipe.mjs 是 minecraft-builder-skill 的可复现程序化配方；build_and_review.py 负责来源核对、构建与渲染，design_audit.py输出差异化与跨组筛查证据，finalize.py 汇总原生结果和哈希。

第10张整体概念图仅记录于 layout-reference.json，不伪装成可放置的第10个房间。世界绝对坐标、正式槽位、连接路线、部署和实机验收归统一接入线程；本目录未写正式存档、mods、配置或其他线程房间。

NBT 显式记录空气，包括 L 形包围盒内的空隙；接入前必须对完整包围体做占用/实体/液体/世界边界检查，不能无条件清空现有世界。各接口重定位并非复制源 Jigsaw 偏移，不声称可直接接入 YUNG 生成器。

当前 Lodestone 预览是本地临时服务；重启命令：

```powershell
& node "C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs" --nbt "D:\ft工程文件\FT地牢_自然环境第一批第2组_20261104\incoming\NAT1-G2-01\room.nbt" --port 5282 --no-open
```
