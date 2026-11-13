# M06 焚痕冰封牢狱直廊 · 独立交付

2026-11-03，Asia/Shanghai。仅制作本目录，不部署、不修改玩家存档、不改 FT 核心或其他房间；未调用收费生图接口。

## 结构与接口

- 四件套：`room.nbt`、`room.json`、`preview.png`、`qa.json`；实际结果和完整指纹以 JSON 为准。
- Minecraft Java 1.20.1 / DataVersion 3465，gzip 单调色板，15×13×40（XYZ），40 格长即 2.5 区块，中型一字直廊；无转弯、岔路或楼梯主路。
- 模板 ID：`corridor-M06`，来源图 ID：`M06`；不要与旧批次房间 `M6` 混用。
- 北入口脚位 `[7,2,0]`，内部安全脚位 `[7,2,4]`，南出口脚位 `[7,2,39]`；`facing=north`。全长中线三格宽、三格高净空，脚下完整且非冰地板；端口实际门洞更宽，不含关闭门。
- 7,800 格显式记录（包含空气）；完整 600 格基础与 600 格屋顶。预览剖切不改变 NBT。显式空气不代表授权清空已有建筑。

## 参考图落地

只读参考：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M06-frozen-prison-burnt.png`。
SHA-256：`3a7bc9bffe8a727ca0b05ca07b492fe1623ac2c0e82c26ada0867b144d19eadf`，已对照该批 README、manifest、SPEC、索引与提示词。

保留冷灰石砖柱梁、双侧冰封牢栏、六座链悬空铁笼、十组暖色蜡烛台和煤黑焦痕。铁笼全部由原版方块组成，不含生物或实体。浮冰与蓝冰代替会融化的普通冰；四处凹入炉光用菌光体表达余烬，不使用真火、熔岩或伤害地板。主路全长连续且不滑；牢栏后冻结区不构成侧支路。

## 验证证据

- `qa/static.json`：实际重读 NBT，参考指纹、gzip、尺寸、坐标唯一/界内、原版材料、无实体/方块实体/活液体/危险块、基础/屋顶、双端口及全长三格净空、蜡烛支撑和六笼锚点。
- `qa/render.json`、`preview/`：Minecraft Builder / Lodestone 0.6.0 从实际 `room.nbt` 渲染北、南、西、东四向完整外观与剖切内部（8 张），另有正反向眼平直廊（2 张）。`preview.png` 为北端眼平图。它们是结构软件渲染，不是游戏截图或 AI 概念图。
- `qa/native.json`、`qa/forge-gametest.log`：隔离 Forge 47.4.10 / Java17 的真实 GameTestServer、ServerLevel、StructureTemplate.placeInWorld；同一原件四旋转各 7,800 记录，共 31,200。逐格检查状态、原生 canSurvive、完整顶/底、支撑、主路往返、外围留白、无液体/方块实体/掉落实体，并执行原生邻居形状更新和 400 tick 稳定性复查。
- `qa/native-roundtrip.nbt`、`qa/native-placed-*.nbt` 保存原生模板往返与四向实际放置后的导出；其来源和指纹绑定在最终 `qa.json`。
- `qa/native-snapshots.json`：独立用 Lodestone 重读原生往返与 400 tick 后四向世界导出，按实际旋转坐标和朝向逐格对照最终原件，共匹配 39,000 条记录。
- 首次测试夹具缺少 `pack.mcmeta`，在加载测试结构之前失败；已修正夹具并保留 `qa/forge-gametest-attempt1-missing-pack-metadata.log`。是否通过以最终 GameTest 日志与 `qa/native.json` 为准，不把失败尝试冒充成功。
- 第二次夹具在 y=100 的第四方向遇到自然地形，占用预检安全拒绝；保留 `qa/forge-gametest-attempt2-terrain-occupied.log`。最终独立夹具改在 y=240，仍执行全部体积占用拒绝，不清除地形。
- 第三次四向初始放置及通路通过，邻居更新后的稳定性检查检出通用 Lodestone 自动铁栏连接把蜡烛误判为完整支撑面。已在配方中显式按本结构材料设置连接，生成时禁用通用状态推导；保留 `qa/forge-gametest-attempt3-iron-bar-shape-drift.log` 与修正前 NBT/指纹，不放宽最终逐格一致性要求。
- 四向审查图使用较亮的检查光，正反向主视图使用冷色氛围光；软件渲染不实现蜡烛火焰粒子或完整客户端光照，不能据预览判断客户端粒子、着色器或 FPS。

## 复现

`./Run-QA.ps1 -Rebuild` 在本目录建立新的隔离测试世界，不打开正式 `saves`，不会部署测试模组。

技能默认预览：在已安装技能目录运行 `node scripts/preview.mjs --nbt "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-M06/room.nbt" --port 5306 --no-open`。固定四向渲染：在本目录运行 `node tools/preview-server.mjs 5308`，然后在另一个终端运行 `node tools/capture-preview.mjs http://127.0.0.1:5308/`。服务器只绑定本机回环地址，无外部资源依赖。

目视核对八向审查图与正反向主视图后，运行 `node tools/finalize.mjs --visual-reviewed` 生成绑定所有证据指纹的 `qa.json`。运行依赖本机已安装 Minecraft Builder、Edge、bundled Playwright/Sharp、Gradle8.8/Forge缓存和 Java17，不修改图像 API 或系统默认环境。

## 接入边界

本交付不是完整整合包客户端/多人游玩验收，不宣称 FPS、着色器表现或现有地牢路网已完成。接入负责人仍需确认唯一 ID、两端连接、占用/实体/液体/边界拒绝、持久化与幂等、个人返程与五档难度兼容，完成生产打包、备份和部署。禁止将 `native-test` 测试模组或隔离世界放入正式实例。
