# FT 地牢 · A 破损无人商店

**独立候选制作已完成，静态与 Forge 原生四向放置通过；未接入核心、未部署、未打开或修改玩家存档。**

只负责 A，不覆盖其他商店变体、原补给商店、其他房间或共享 incoming。未调用付费生图接口，未改图像配置或密钥，未替换任何已部署 JAR。须由统一接入线程 `01a19758-b0e5-75c2-822a-4860527a1e8b` 确认 ID、槽位及接收时机。

## 接收文件

| 文件 | 用途 |
| --- | --- |
| `out/room.nbt` | gzip / 单调色板 / Java 1.20.1 / DataVersion 3465，完整 15×9×15 结构 |
| `out/room.json` | 暂定 ID、入口、内点、图像指纹、结构哈希与未部署声明 |
| `out/preview.png` | 真实最终 NBT 的北侧材质剖视，保留梁与支撑灯组 |
| `out/qa.json` | 静态、原生、视觉和可重建总报告 |
| `out/blueprint.json` | 柜台、通路、碎块、蛛网与支撑精确坐标 |
| `out/materials.json` | 非空气材料数量 |
| `preview/four-views.png` | 保留柱梁的四方向剖视 |
| `preview/furniture-detail.png` | 额外隐藏高梁、吊灯整组及柱帽的家具细节剖视 |
| `preview/plan.png` | 三格主路、柜台一格环路和落地废墟俯视图 |
| `preview/exterior.png` | 完整封顶、北侧三格接口 |
| `preview/native-world-capture.png` | Forge 放置 100 tick 后回采 NBT 的软件预览 |
| `qa/native-placed-*.nbt` | 四向原生世界回采结构 |
| `release/SHOP_A_RUINED-20261102.zip` | 除缓存、测试世界和二进制 Mod 外的交付包 |

## 指纹与来源

- 原图：`D:/ft工程文件/image2.5/dungeon-shop-variants-20261102/out/shop-variant-A-broken-unmanned.png`，1536×1024。
- 图片 SHA-256：`a5ec41abe1f7a491b3742e7221ea0c86530d681f0a6b83689c0c45c2ded7fe30`。
- 结构 SHA-256：`2f6f6d9dafa9c4ee6d21f901c44c79350e20d5d959bb5a1a4c613cd781d565dd`。
- 原图、来源 README/manifest/DISPATCH 的只读副本在 `reference/`；源 README 提及的 `STYLES.md` 实际不存在，未伪造补齐。实际采用核验图、README 布局和 manifest 风格。
- FT 契约：`D:/ft工程文件/FT地牢_Image25接入_20261101/CONTRACT.md`，只读采用。
- `nbtio.py`、`run_native.py` 和原生测试基础框架、材质渲染器来自既有独立补给商店工程的副本；只在本目录适配 A，来源不改。没有复用它的成品 NBT。

## 布局与落地取舍

- 暂定 `SHOP_A_RUINED`，尺寸 **15×9×15**；北入口玩家脚位 **(7,1,0)**，安全内点 **(7,1,3)**；`assigned_slot=null`，不是已注册正式房间。
- 主路 **X=6…8、Y=1…3、Z=0…13** 全部空气，三格宽且三格高，贯穿断柜台直到后墙。
- 两截柜台为 X=3…5 / 9…11、Z=9，断口用原版楼梯塑形；两侧每截都有一格净空环路。
- **13 块**塌梁、碎石、倒架全部 Y=1 放在完整地板上；无浮空碎片。梁两端保留屋顶连接，最终屋顶完整。四根柱均在角墙线上，不穿家具。
- **10 处**蛛网贴墙或接货架，避开主路及柜台环路；两盏吊灯有完整链条和高梁，另有一盏落地灯。
- 地面破洞用开裂石砖、苔石与安山岩材质转译，不挖坑。斜木牌用合法墙牌，药瓶金币转译为空花盆、未点蜡烛及空酿造台。
- 无角色、实体、刷怪、战利品表或库存；2 箱、3 桶、1 酿造台均空。这里只做无人商店建筑，不实现交易或奖励。

## 验收证据

1. `qa/static.json`：**31 项 PASS**。单调色板、2025 条完整且唯一记录、原版方块、无库存、完整地板/屋顶、北接口、126 格主路净空、48 格柜台环路、柱家具分离、废墟贴地与蛛网贴边等。
2. `qa/native.json` 与 `qa/forge-gametest.log`：**Java 17.0.8 / Minecraft 1.20.1 / Forge 47.4.10** 的隔离无界面 GameTestServer，真实 `StructureTemplate.load/placeInWorld/fillFromWorld`，非仅解码或 DataGen。
3. **0°/90°/180°/270°** 四向实际放置、邻居更新，等待 **100 tick** 后逐格核验 **8100 条**状态；每向 2025 条记录、7 个合法空方块实体、139 个可达脚位、13 处贴地废墟、10 处贴边蛛网，额外世界实体为 0；原生 `canSurvive`、碰撞形状及空库存均通过。
4. 每向分别设置断链、石块堵路、蛛网堵路和废墟地板缺失负例，合计 **16 个探针**均识别并恢复。断链探针用 `UPDATE_KNOWN_SHAPE` 暂停自动拆除后检查原生存活条件；蛛网即使碰撞形状为空也不能被判作安全路。
5. 四向回采模板原生重载通过；`qa/reproducibility.json` 另检查 `NONE` 回采与源模板全部方块状态完全相同，并复建 NBT/元数据/布局得到相同指纹。
6. 最终日志包含 `SHOP_A_NATIVE_GAME_TEST_PASS: rotations=4/4, records=8100, ticks=100, stock=0`、`All 1 required tests passed` 及 `BUILD SUCCESSFUL`。`qa/visual-review.json` 记录实际 PNG 视觉复核。

**证据边界**：图片为 NBT 软件材质渲染，不是 Minecraft 实机截图。隔离 Forge 通过不等于完整整合包客户端、多人、FPS、FT 大厅连路、核心幂等、防覆盖、返程或交易验收。上层梁可能遮挡北侧部分柜面，另交付明确标注的家具细节剖视，不在最终 NBT 删梁。

## 重建与复测

在本工程目录执行（本机既有 Python/Pillow、Java 17、Gradle 8.8 和 Forge 缓存）：

```powershell
$python = 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe'
& $python tools/build_room.py
& $python tools/qa_static.py
& $python tools/run_native.py
& $python tools/render_delivery.py
& $python tools/finalize_delivery.py
& $python tools/package_delivery.py
```

`run_native.py` 默认离线，只启动本目录 `qa/forge-run` 内的隔离 GameTestServer，不启动客户端、不访问玩家存档；复测会归档上一份报告和日志。异机可传 `--java-home` / `--gradle`，须准备相应版本依赖。构建脚本核对本地图源和交付副本指纹；移机需保留或显式适配其来源路径。渲染只读本机 vanilla `client.jar` 模型与材质，不把客户端文件打包分发。

`qa/visual-review.json` 是本次人工式视觉复核记录；修改几何/渲染后应重新看图并更新记录，不应把旧记录当作未来自动视觉验收。打包脚本按白名单收集源码和证据，不收 `build/`、`.gradle/` 或 `qa/forge-run/`。

## 接入边界

本目录不写共享 incoming、核心目录、其他房间或正式实例 mods/saves。接收方必须先确认唯一正式 ID 和槽位，再做完整体积/实体/液体占用预检、大厅实际可走道路、逐房持久生成、幂等、防覆盖、原门返程与难度回归。隔离测试夹具清空单元只用于测试，严禁搬到正式施工代码。

当前只需撤回本独立候选即可回滚，没有正式世界变更。未来若部署，应先由协调人停服、备份并制定回滚；不能盲删地牢 SavedData 或生成标记，卸载模板不自动拆除已建建筑。
