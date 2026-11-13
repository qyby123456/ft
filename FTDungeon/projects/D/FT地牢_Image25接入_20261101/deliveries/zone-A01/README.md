# A01 · 废弃书房

独立交付编号 **zone-A01**，使用 Minecraft Builder Skill / Lodestone 0.6.0 制作。仅写入本交付目录；尚未部署或接入正式实例。四件套为 `room.nbt`、`room.json`、`preview.png`、`qa.json`，完整指纹见 `delivery-manifest.json`。

## 规格与接口

- Minecraft Java 1.20.1 / DataVersion 3465，gzip 原版单调色板结构。尺寸 XYZ **17×11×17**，共 3,179 条全体积唯一坐标记录，含 1,596 个非空气方块。
- 北入口脚部 **[8,2,0]**，3 格宽、4 格高；内侧安全脚部 **[8,2,4]**。原路返回，同一入口；没有额外出口或岔口。
- 主通路 x=7..9、z=0..14、脚部 y=2，3 宽×4 高净空；安全区域 x=7..9、z=3..5。地板 y=0/1 双层完整，屋顶 y=10 共289格完整实木。
- 书架落地并贴西/南墙；西侧完整书桌用倒置楼梯与上半砖形成承重端和桌面。地面椅子/碎板位于侧区，桌面纸张与盆栽有实物支撑。东侧冷壁炉贴墙，黑石炉灰没有火源或可燃燃料。
- 6 根木柱落在石座上，嵌顶横梁支撑2组链条吊灯；另有桌面与木桶顶灯。5处蛛网位于侧墙高处，主通路不经过蛛网。
- 3个木桶分别位于 [2,2,2]、[13,2,14]、[14,2,3]，显式 `Items=[]`，没有 LootTable、战利品、书本或物品实体。可从入口走到书桌、书架、壁炉和各木桶附近。

NBT SHA-256：`d4bd1ffca427cb8d5693d32204fb1dd5957993339507edd8e2b96718affa28c9`

## 来源与偏差

参考图：`D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/abandoned/A01-abandoned-ruined-study.png`

参考图 SHA-256：`e28fe9c5d774a0f141b616ebeb683f09bc6a8c0bc90fe74e39d88b4f1f2d9a0d`

已经核对本地图、README、manifest/verify记录、文件字节数及完整PNG解码，1536×1024 RGB。保留苔石墙、深色木梁、旧书架、纸张书桌、破椅、冷壁炉、墙角蛛网、木桶与磨损红毯的主题关系。

- 概念图为无顶剖视；NBT补齐封闭实木屋顶与墙体，预览只在渲染副本中剖切。
- 倾斜书柜与翻倒椅子用原版轴向书架、楼梯和半砖表达，没有斜放自定义模型或悬浮书架。
- 散落书本与纸张用棕色/白色地毯表达，没有可拾取物品或可读书库存。
- 中央磨损红毯使用与地板齐平的红/棕羊毛，留出平坦主路；壁炉冷置，没有火焰、熔岩或隐蔽伤害。
- 原版材质与Lodestone照明比概念图更明亮；预览是实际NBT的软件渲染，不冒充实机截图或自定义光影。

## QA证据

- `qa/static.json`：15项静态检查通过，覆盖来源指纹、版本、原版方块、边界、完整记录、屋顶/双层地板、单入口、主路净空、空容器、书架支撑和吊灯锚点。
- `qa/native.json`：真实 Forge 47.4.10 / Minecraft 1.20.1 / Java17.0.8，在本目录的独立 GameTestServer/ServerLevel 调用 `StructureTemplate.placeInWorld`，不是只解码或模拟。
- 0°、90°、180°、270°各放一次。立即与400tick后分别逐格核对12,716条记录，每方向1,596个非空气状态通过原生 `canSurvive`，181个干燥脚位连通，3个空木桶库存保持0。
- 实际执行12,716次邻居通知与76,296次方向形状检查；火tick开启，`randomTickSpeed=3`。选定方块没有随机tick状态，强制随机tick次数为0，不宣称额外生长压力测试。
- 每方向外围1,514格空气保持不变，无流体、掉落物或实体；完整屋顶及吊灯锚点在四向均稳定。
- 成功日志 `evidence/forge-gametest-a01-attempt1-20261103.log` 含 `A01_NATIVE_GAME_TEST_PASS`、`All 1 required tests passed`、`BUILD SUCCESSFUL`，退出码0。静态首轮仅为校验器读取 verify.json 字段名错误，修正后通过，原报告保留于 `evidence/static-attempt1.json`。
- `views/interior-*.png` 与 `views/exterior-*.png` 为四向内部/完整外壳8张图，`views/interior-four.png`、`views/exterior-four.png` 为四宫格。`qa/render.json`和`qa/visual-review.json`绑定同一最终NBT指纹。

本次没有完整整合包客户端/多人游玩或FPS验收；安全点表示几何和干燥碰撞空间，隔离夹具关闭自然刷怪。未修改生产JAR、正式实例/存档、共用账本或其它并行房间。

## 复现

1. 在已安装的Minecraft Builder技能目录运行 `node scripts/run-build.mjs "本目录/recipe.mjs" --out-dir "本目录" --name room --data-version 3465 --json`。
2. 运行 `node tools/static-qa.mjs`，然后 `./Run-NativeQA.ps1` 生成独立测试世界和日志；不要部署测试夹具。
3. 运行 `node tools/render-lodestone.cjs` 重新渲染8图，目视复核后更新视觉报告。
4. 使用带Pillow的Python运行 `tools/finalize.py`，门禁通过后生成四件套QA及完整哈希清单。
