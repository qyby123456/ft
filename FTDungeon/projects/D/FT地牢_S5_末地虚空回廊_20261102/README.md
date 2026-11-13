# S5 末地虚空回廊 · 独立制作

本目录只制作 S5，不部署、不写玩家存档、不改地牢核心或其它房间。以同批 README、manifest、ROOM_STYLES_BATCH3.md 和 S5 prompt 为设计依据；原始 PNG 为七次 image2.5 失败后的本地 THREE 示意渲染，绝非 AI 成功图或游戏截图。

## 使用与交接

- `out/S5/`：交付的 `room.nbt`、`room.json`、`preview.png`、`qa.json`。
- `HANDOFF.md`：接入坐标、最终哈希、证据路径、限制及协调事项。
- `delivery-manifest.json`：最终四文件及证据的 SHA-256。
- 结构尺寸 XYZ 为 **7×8×13**。北入口脚部 **[3,3,0]**、安全内点脚部 **[3,3,10]**；floor Y=2，完整 roof Y=7。北接口3格宽3格高，连接道路需适配脚部 +3。
- 结构内部保留5×11、净高4的设计内腔。主路三格宽；断裂转译为安全玻璃观察槽，不让玩家跳跃过虚空。黑曜石底座、紫珀承梁/横肋与封护玻璃保证结构连通。
- 六根末地烛有整块紫珀支撑；两朵 age=5 紫颂花落在末地石上；两块无眼末地门框置于黑曜石上，不能组成传送门。无实体、方块实体、库存、奖励、流体、火或爆炸水晶。

## 重建和验证

1. 使用带 Pillow 的 Python 运行 `tools/build_s5.py`，生成实际结构、元数据、从最终 NBT 渲染的1536×1024预览和静态 QA。重建会将 native 状态恢复为 pending，避免旧原生证据套用于新结构。
2. JDK17 + Gradle8.8：`gradle runGameTestServer --offline --console=plain -Dnet.minecraftforge.gradle.check.certs=false`。只加载独立验证 Mod，使用本目录 `qa/forge-run/` 内的临时 GameTest 世界。完整控制台输出须保存为 `qa/forge-native-console.log`。
3. 运行 `tools/finalize_s5.py`。它比对当前NBT、元数据、原生输出、控制台通过标记和来源指纹，再执行7个安全负例，更新最终 QA/manifest/HANDOFF。

现有 Python：`C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe`。现有 JDK17：`C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot`。原版模型/纹理只读来自本机 Gradle 缓存 `client.jar`。

原生测试实际调用 `ServerLevel` 和 `StructureTemplate.placeInWorld`，测试四向各两次放置，邻居通知和100tick稳定性、全部728状态、合法属性、支撑、玩家碰撞体、完整干燥路线和跌落护边；不是仅NBT解码。它仍不代表完整整合包客户端游玩、多人大交通道、奖励/战斗、FPS、正式核心占用拒绝或持久标记幂等测试。

## 工具来源

`tools/vendor/nbtio.py` 是既有 `output/dungeon-expansion-20261101/tools/nbtio.py` 的本地副本。`tools/vendor/asset_faces.py` 取自既有 `墓狱档案地牢_20261101/tools/render_archive.py` 的原版模型/纹理面渲染通用部分，移除了其它房间生成/顶视/排版逻辑与房间依赖。没有修改上述源工具，也不依赖其它制作线程房间生成脚本。此为软件预览，不重新付费生图。

最终接入需唯一负责人选择slot/origin、连接大厅道路、验收占用拒绝与持久标记，再协调部署。本目录不生成生产MOD发行包，开发验证Mod禁止部署。
