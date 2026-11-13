# 第一批第7组：蘑菇真菌地牢结构模块

本聊天 `01a1a5df-d735-7b43-9d3b-2939bfc6c0ed` 独立制作整组。交付 **9 个实际 vanilla structure NBT**，不是重新生成环境图片。第10张仅作为整体布局参考；第11–20张为后续批次，不混入本批。本线程未调用图片 API，没有图片半写入或待清理文件；没有删除、覆盖任何原图。

## 成品与接口

`incoming/<ID>/` 各含 `room.nbt`、`room.json`、`preview.png`、`qa.json`、`blocks.json`、`views/`。尺寸为 X宽×Y高×Z深。全部模板面向北，脚位 y=1；入口为北侧中央，门洞净宽3、净高3，地板完整。`room.json` 包含每个接口的脚位、外法线、模板外邻接脚位、用途和静态连通结果；不含真实 Jigsaw 方块。

| ID | 名称 | 尺寸 | 出口 |
|---|---|---|---|
| NAT7-01 | 巨菇中枢 | 19×16×19 | 南、东、西 |
| NAT7-02 | 菌光议事室 | 17×10×17 | 南 |
| NAT7-03 | 孢子储藏室 | 11×8×11 | 东 |
| NAT7-04 | 菌柱长厅 | 23×12×17 | 南、东、西 |
| NAT7-05 | 菌脉直廊 | 7×8×25 | 南 |
| NAT7-06 | 菌光折廊 | 17×8×17 | 东，真实L形外壳 |
| NAT7-07 | 菌林门厅 | 19×10×17 | 南 |
| NAT7-08 | 孢光天井 | 23×12×23 | 南、东、西；仅本间中央露天 |
| NAT7-09 | 菌王宝库 | 21×12×19 | 东、西 |

`NAT7-*` 为本组候选标识，不占用旧房间 `S7`，没有分配正式世界槽位。X正向东、Z正向南、Y向上。连接时将接口外邻接脚位与下一模块入口对齐，并核对相反朝向、旋转、整体包围盒、已占用地形与高度。不要把任意两个接口坐标直接视为已经接通的路线。

## 解包证据优先，不以图片替代结构依据

生成第一个 NBT **之前**，使用原参考库的只读解析器逐一重新解析真实解包 NBT，保存 SHA-256、实际尺寸、调色板计数与接口记录到 `qa/source-evidence.json`。`source_evidence.py` 可复跑；每间 `room.json` 均记录主来源路径、SHA-256、原尺寸和改造点。不存在“原尺寸就是图片推测尺寸”的说法。

参考库入口与总结：
- `D:/ft工程文件/结构参考_20261103/README.md`
- `D:/ft工程文件/结构参考_20261103/LEARNING_NOTES.md`
- `D:/ft工程文件/结构参考_20261103/SAMPLE_CATALOG.md`
- `D:/ft工程文件/结构参考_20261103/EVIDENCE.md`

真实结构引用：
- `D:/ft工程文件/结构参考_20261103/unpacked/better_strongholds/data/betterstrongholds/structures/rooms/junction_md.nbt`：19×16×19，提供大型分支枢纽尺度；01保留原尺寸，07压缩门厅高度/深度，08扩大庭院并打开天井。
- `D:/ft工程文件/结构参考_20261103/unpacked/better_strongholds/data/betterstrongholds/structures/rooms/armoury_md.nbt`：13×8×13，02保留中室侧边储藏与中心通行逻辑，扩展到17×10×17给巨菇留空间。
- `D:/ft工程文件/结构参考_20261103/unpacked/better_strongholds/data/betterstrongholds/structures/rooms/storeroom.nbt`：7×8×13，03沿用储藏功能，扩宽4、缩短2，增加东侧连接口。
- `D:/ft工程文件/结构参考_20261103/unpacked/better_strongholds/data/betterstrongholds/structures/rooms/library_md.nbt`：17×8×25，04转置长轴后缩宽2、加高4，侧列功能改造成两排受支撑菌柱，保持中轴通行。
- `D:/ft工程文件/结构参考_20261103/unpacked/stellarity/data/stellarity/structures/stronghold/corridors/straight.nbt`：25×6×7，05旋转为南北向、加高2，保留25格长度、两端相反接口及脚位y=1。
- `D:/ft工程文件/结构参考_20261103/unpacked/stellarity/data/stellarity/structures/stronghold/corridors/turn_1.nbt`：17×6×17，06保持17×17范围、加高2；原偏心南/东接口改为FT北/东中央接口，保留L形而非十字形。
- `D:/ft工程文件/结构参考_20261103/unpacked/better_strongholds/data/betterstrongholds/structures/rooms/prison_lg.nbt`：19×8×19，09沿用分支室外壳尺度、扩宽2、加高4，功能明确改为阶梯宝库，不冒充原监牢复刻。
- `D:/ft工程文件/结构参考_20261103/unpacked/dungeons_arise/data/dungeons_arise/structures/giant_mushroom/red_giant_mushroom_part_2.nbt`：21×38×21，实测红陶瓦2103、白陶瓦433、蘑菇柄304；沿用红白菌盖与浅色柄的材质组合，原创缩小几何，不照搬原模块或原拼装池。

石砖主体取自真实要塞模板；菌丝、灰化土、菌光体和发光地衣为本组原版环境改造。原模组的 Jigsaw、处理器、战利品表、刷怪及函数均不迁入，不声称原池兼容。所有结构由原创程序化配方生成，没有复制或打包原模组NBT字节；原素材授权边界仍按参考库说明处理。

## 第7组独特拓扑

本次差异化修订增加01双层菌柄环廊与两处上行台阶、02强制S型菌壁、05直线坡脊、08低环，以及03双压力板/双音符盒提示组件，均真实写入NBT，不只是换材料。组件触发未做世界实测，战斗节奏仅为空间设计，未刷怪。完整六项设计说明与两回路逻辑图见 `DESIGN_IDENTITY.md`、`DESIGN_IDENTITY.json`；该逻辑图尚未在世界拼装。

只读比对第2/3组当时已落盘18个模板，材料无关、四旋转归一化的实体几何精确重复为0，见 `qa/design-audit.json`；这不等于全组宏观拓扑终审通过。未复用其他组配方或核心路线，最终冻结后的重复度矩阵仍待统一审计。

## 校验结果与边界

- 静态：9/9 PASS；gzip解码、1.20.1 `DataVersion=3465`、完整显式空气体积、唯一坐标、越界、原版材料、所有接口3×3净空/地板/连通、安全点、屋顶、植物和地衣支撑、无实体/危险方块/自动战利品。
- 原生：9/9 PASS；使用真实 Minecraft 1.20.1 注册表校验每个方块属性、容器加载、碰撞形状通行、`StructureTemplate` 加载与保存往返。报告 `qa/native-review.json`，实际编译/运行日志 `qa/native-0.log`、`qa/native-1.log`。
- 预览：每间北/南/西/东内部剖视 + 完整外壳共45张，均从最终NBT渲染；`overview.png` 是9间实际结构总览，不是AI图。剖切只用于显示，最终NBT保留完整外壳。已查看总览及折廊/宝库重点视角；不能把生成全部视图等同于逐图人工终验。
- **未执行 Forge 隔离世界实际放置、四向旋转后更新稳定性、整合包实机游玩或正式接入。** 候选可交接审查，不标成正式可用。没有写玩家存档、mods、正式JAR、核心源码或其他组文件。
- 每间所有位置都有显式空气记录；模板放置有覆盖风险。接入方必须先对整个包围盒做占用拒绝检查。L形的矩形包围盒也含空气，不能当作不受影响区域。
- 本批没有真实水、岩浆、火、刷怪笼或发奖机制。宝箱为空；图中浅水与孢子粒子未伪造为已实现玩法。

## 复跑与 Lodestone

从本目录依次执行 `C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe build_and_check.py`、相同Python的 `design_audit.py`、`package_delivery.py`。构建脚本先读真实解包证据，再逐间调用已安装技能的 `scripts/run-build.mjs`；不修改技能代码。

运行 `Start-LodestonePreview.ps1` 启动本地预览。服务开启后打开 `preview.html` 可选择9间结构，使用真实Lodestone默认材质包和同一个服务。预览只读，不能视为Minecraft实际放置。

完整文件校验见 `SHA256SUMS.json`；统一交接摘要见 `HANDOVER.json`。正式槽位与实际Forge放置复验留给统一接入负责人；本聊天不擅自部署。
