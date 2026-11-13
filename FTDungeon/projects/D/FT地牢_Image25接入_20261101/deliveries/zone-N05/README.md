# zone-N05 · 升降枢纽

四件套为本目录的 room.nbt、room.json、preview.png、qa.json。成品尺寸 **29×24×29（X×Y×Z）**，Minecraft Java 1.20.1，DataVersion 3465，gzip 单调色板 NBT，20184 个唯一完整体积记录。Minecraft Builder 配方与可复现工具同目录保留。

原图为 D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/hub/N05-hub-elevator-lift-hub.png，SHA-256 **4838644367efccc2356e4c2aa59b1a0726670e19a75b8dacc07dea99320f6c00**。已交叉核对该批 README、docs/manifest.json、docs/verify.json。参考图为 image2.5 概念图，不是实机截图；本次没有重新生图或付费 API 调用。

## 几何与接口

参考要素落实为石砖墙体、四根落地柱、深色承重梁、顶部铜色绞盘、四角竖链、静态封闭铁笼、井口黑黄警戒带、双层围护、暖色吊灯、侧边木箱造型和控制台。木箱仅使用木方块，无容器、库存或奖励。

三层地板分别为 y=0、6、17；正常脚位分别为 y=1、7、18。顶盖 y=23 完整。主层采用三格宽、三格净高的连续方环与四向出入口，240 个通路脚位均作原生碰撞检查。

| 接口 | 局部脚位 | 安全落点 | 契约 |
| --- | --- | --- | --- |
| H_N 北 | [14,7,0] | [14,7,4] | 3 宽 × 5 高净空，地板 y=6 |
| H_E 东 | [28,7,14] | [24,7,14] | 3 宽 × 5 高净空，地板 y=6 |
| H_S 南 | [14,7,28] | [14,7,24] | 3 宽 × 5 高净空，地板 y=6 |
| H_W 西 | [0,7,14] | [4,7,14] | 3 宽 × 5 高净空，地板 y=6 |
| V_LOWER 下层内部梯道站 | [5,1,9] | [6,1,9] | 实体底板封闭，无外露底部竖井接口 |
| V_UPPER 上层内部梯道站 | [5,18,9] | [6,18,9] | 完整屋顶封闭，无外露顶部竖井接口 |

应急梯位于 x=4,z=9，y=1..18，向东，背靠 x=3 的连续实体墙；主层梯道站为 [5,7,9]。三个站点有侧面围栏及标记地板。下层另有西侧 3 格宽检修门，连通井底安全地面 [14,1,14]。

井口为 x,z=10..18，主层与上层均在 x/z=9、19 设置一格实体矮墙加一格铁栏，总计两格高的连续围护。悬笼 x,z=12..16、底板 y=10、笼顶 y=15，铁栏封闭；它位于护栏内，不能作为通路落点。井底 y=0 为干燥实体地板。

**吊笼、绞盘和操纵台属于静态建筑造型。没有移动平台、电梯时序、传送、插件或命令逻辑；未验证载人升降功能。** 垂直交通使用原版应急梯的几何接口。本次验证了梯道支撑、碰撞与连通，未执行真人攀梯游玩。V_LOWER/V_UPPER 是内部停靠点；未来若要穿透顶底连接其他模块，需另做安全适配与 QA。

## 验证证据

- qa/static.json：完整体积、原图与批次哈希、全原版方块、无越界、实体、方块实体、液体、执行方块或危险方块；六个接口及所有落点连通，主通路、连续梯道、两层护栏、封闭笼、底板与顶盖通过。
- qa/forge-native.json：Forge 47.4.10 隔离 GameTest，实际 StructureTemplate.placeInWorld 放置四旋转，80736 状态逐项核对；强制邻接更新与区块保持加载，240 tick 后零状态漂移、零掉落物。每旋转均验证 18 段梯子、12 盏有支撑灯笼、两层护栏碰撞、240 格主通路与 1835 格全层连通图。
- qa/forge-run.log：最终缓冲区隔离测试，包含 N05_FORGE_PLACEMENT_PASS、All 1 required tests passed、BUILD SUCCESSFUL；qa/native-roundtrip.nbt 为原生读取回写证据。
- preview/four-directions.png、qa/render.json：四向完整外观及四向声明剖视共 8 张；另有四向完整主层、上下层与三个梯道站点共 9 张。17 张独立图均为 1536×1024，来源 NBT 哈希与相机完整记录。剖视仅在渲染器内隐藏屋顶、上层平台/护栏及近侧墙；交付 NBT 不剖切。
- qa/visual-review.json：逐张审阅记录。软件视图不是整合包客户端截图。
- delivery-manifest.json：四件套、源码和证据哈希；qa.json 汇总制作状态，独立接收者 acceptance 仍为 PENDING。

首轮测试在 180 度放置区域外的一格检查边界中检测到天然棕色蘑菇掉落，不能将该轮当作通过。原日志 qa/forge-first-rejected.log 与存档实体解析 qa/rejected-world-items.json 保留。模板调色板没有蘑菇，首轮逐块状态也无漂移；两件蘑菇位于 z≈35.5，模板该侧边界为 z=36。新建世界诊断测试通过后，最终测试进一步在隔离测试区域清出三格缓冲、移除测试区既有实体、关闭环境生物生成与随机刻，再完整复验通过。没有对模板、正式地形或玩家世界作此清理。

## 复现

在本目录执行，使用已安装技能，不修改技能源码：

    node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs ./recipe.mjs --out-dir . --name room --data-version 3465 --json
    node ./tools/inspect.mjs
    powershell -NoProfile -File ./tools/run-forge.ps1
    node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs --nbt ./room.nbt --port 5195 --no-open
    node ./tools/capture.mjs
    node ./tools/finalize.mjs

预览服务运行后，技能默认页面支持鼠标旋转；本目录 preview/viewer.html 可通过服务的 /@fs/ 绝对路径打开，提供检查相机选择器。截图工具默认使用 http://127.0.0.1:5195，可由 N05_PREVIEW_SERVER 指定该次预览实际地址。

## 接入边界

仅制作独立目录；未部署、未修改正式核心、JAR、配置或玩家存档。NBT 显式包含空气，接入者必须检查整个 29×24×29 体积的占用、实体与流体，拒绝覆盖既有空间。放置原点比主层脚位低 7 格，主层地板偏移 6 格；不能直接按底层脚位拼接同层走廊。旋转以 [0,0,0] 为枢轴，四向接口与安全点须一同变换。

隔离服务端 QA 不能替代整合包客户端、真人移动、多人、FPS、外部连接器、占用拒绝、持久幂等或旧世界迁移验收。制作 PASS 不代表已接入或完成独立接收冻结。
