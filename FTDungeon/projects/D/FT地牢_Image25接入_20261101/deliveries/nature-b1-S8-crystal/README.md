# 第8组：水晶紫晶环境 · 首批结构交付

本线程独立制作九个 Minecraft Java 1.20.1 vanilla structure 模块。使用 minecraft-builder-skill 的 Lodestone Structure API 和 run-build.mjs 导出，gzip、DataVersion 3465、单调色板。没有调用图片生成接口，没有新图片半写入，也没有部署、修改 JAR 或玩家世界。

## 权威输入及改造

已实际解码并核对原始 NBT 尺寸，不仅阅读概念图片。可复查的完整路径、原件 SHA256、原调色板、原 Jigsaw 方块记录、尺寸与逐房改造见 `source/metadata/authority.json` 和各 `room.json.authority`。

- 解包总结：`D:/ft工程文件/结构参考_20261103/LEARNING_NOTES.md`，采用“交通拓扑、房间、细节分层；NBT 与运行时逻辑分离”原则。
- 代表目录：`D:/ft工程文件/结构参考_20261103/SAMPLE_CATALOG.md`。
- 原始模板根：`D:/ft工程文件/结构参考_20261103/unpacked/better_strongholds/data/betterstrongholds/structures/`。
- 可读详情根：`D:/ft工程文件/结构参考_20261103/nbt-details/better_strongholds/betterstrongholds/`。
- 池：`D:/ft工程文件/结构参考_20261103/unpacked/better_strongholds/data/betterstrongholds/worldgen/template_pool/hallway.json`，原例走廊池与 terminators fallback 的语义区分；本交付不复制池或宣称接入 YUNG 生成器。
- 接口例：`D:/ft工程文件/结构参考_20261103/catalog/sample-connections.json`；读取方向和匹配规则，但不将静态匹配冒充世界放置成功。

原模板主要石砖/錾制石砖/石砖楼梯，分别原创替换为深板岩砖/錾制深板岩/磨制深板岩楼梯；方解石、紫水晶、晶簇和海晶灯为主题改造。没有原样再分发 Mod 建筑几何，没有复制刷怪笼、战利品、Jigsaw 或处理器占位材质。整体图片仅作美术方向，不作为精确尺寸与连接点证据。

## 模块与尺寸

尺寸顺序均为 X×Y×Z，坐标为玩家脚部位置。+X 东，+Z 南。每个北入口位于 `(floor(X/2),1,0)`，净空3×3、有完整干燥地板。完整连接点见每个 room.json 的 connections，朝向是朝外法向。

| 暂定ID | 功能 | 尺寸 | 解包依据 | 出口 |
|---|---|---|---|---|
| N8_01 | 晶核大殿 | 19×16×19 | rooms/junction_md，保留尺度与交汇语义 | 南(9,1,18)、东(18,1,9) |
| N8_02 | 紫晶研究室 | 13×8×13 | rooms/armoury_md，保留尺度，改书架与晶体 | 南(6,1,12) |
| N8_03 | 晶簇密室 | 9×8×9 | rooms/crypt_sm，7×8×7扩宽2格以适配门洞 | 东(8,1,4) |
| N8_04 | 方解石柱廊大厅 | 19×8×19 | rooms/prison_lg，保留尺度，分格改双列柱 | 南(9,1,18) |
| N8_05 | 紫晶直廊 | 9×8×21 | hallways/hallway_2，5×8×5扩宽及四段延长 | 南(4,1,20) |
| N8_06 | 晶脉直角回廊 | 15×8×15 | hallways/hallway_turn，平面3倍、保持8高 | 东(14,1,11) |
| N8_07 | 晶洞入口前厅 | 13×8×13 | rooms/armoury_md，保留尺度，改休息凳与守卫龛 | 南(6,1,12) |
| N8_08 | 露天天晶庭院 | 19×16×19 | rooms/junction_md，保留外包、中心开9×9天井 | 南(9,1,18) |
| N8_09 | 晶核宝库祭坛 | 19×16×19 | rooms/treasure_room_lg，保留尺度与终点语义 | 无，终点房 |

## 文件与证据等级

每间在 `incoming/N8_XX/` 下包含 room.nbt、room.json、materials.json、preview.png、qa.json 和 Lodestone summary.json。

- `qa/static-review.json`：边界、唯一坐标、尺寸、空箱、无实体/危险流体/禁用方块、晶簇支撑、完整屋顶或庭院开口、所有接口三格净空及可达性。
- `qa/native-review.json`：实际 Minecraft 1.20.1 原版 NBT 解码、注册表/属性、方块实体加载、原生碰撞入口通路和 StructureTemplate 往返；不是世界放置。
- `four-view-review/r1/N8_XX/`：由最终 NBT 生成北南西东内部剖视与完整外壳图，含源 NBT 哈希快照；`preview-contact.png` 是九间联系表。静态图不是 Lodestone 截屏，也不是实机截图。
- `manifest.json`：图片指纹、模块清单及边界。正式 ID 和世界槽位由统一接入方分配，不占用现有 S8 房间编号。
- `qa/packaging.json`：交付文件可读、哈希一致和逐房材料清单核对结果。

实机/Forge 世界四向放置、邻居更新稳定性、整合包游玩均未运行，不标记为通过。当前为独立结构候选，不宣称已统一接入。

## Lodestone 交互预览与复跑

当前主殿预览：<http://127.0.0.1:5288/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=room.nbt>。这是本地临时服务，关闭服务后失效。

预览任何模块：进入 `C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill`，执行 `node scripts/preview.mjs --nbt "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/nature-b1-S8-crystal/incoming/N8_01/room.nbt" --port 5288 --no-open`，按需替换 N8_01 和端口。

使用配置的 Python 依次执行 source/evidence.py、source/build.py、source/finish.py native、source/finish.py render、source/finish.py manifest、source/package.py。原始 Mod 证据只读；脚本仅写本交付目录。复跑覆盖本组候选，不应在它已被协调方接收后擅自重生成。

## 接入限制

NBT 明确记录空气；L弯外包盒空白区域也在模板体积内。接入前必须检测完整外包体积的占用、世界边界、实体和液体，绝不对现有房间无条件清空。旋转后须同步旋转所有接口与安全点，不能只改朝向字符串。房间拼接仍需协调方检查包围盒碰撞、相邻门洞、支撑及路径。除庭院外完整屋顶保留在最终 NBT 中，剖视仅影响预览。
