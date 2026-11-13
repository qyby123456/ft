# S05 黑石积水吊笼短廊

独立结构已制作并验收：11×10×16，轴向正好 16 格（1 区块），北南一字贯通。采用 Minecraft Builder 配方与 Lodestone 生成 gzip 单调色板原版 Minecraft Java 1.20.1 NBT（DataVersion 3465）。本目录交付供统一接入审查的结构与证据，没有部署、写入玩家存档或修改正式 JAR。

四件套：room.nbt、room.json、preview.png、qa.json。

结构 SHA-256：c31084a7ac041e079595c0549205ae72a903efb86d5e2eb469c798e1ab3a0076。

![实际 NBT 一点透视预览](preview/lodestone-interior.png)

## 设计与接入坐标

- 原图：D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S05-blackstone-deepslate-flooded.png。
- 原图 SHA-256：07435538db7e9d2361a768877170d8b9e622b7e49109fca0ec677e35ecf95c1a。
- 批次说明：同批次 README.md、SPEC.md、manifest.json 和 prompts/S05-blackstone-deepslate-flooded.txt。
- 北入口脚位 (5,2,0)；内部安全脚位 (5,2,3)；南出口脚位 (5,2,15)。两端开口三格宽、四格净高，主路至少三格净高。
- 主路 X=4…6、Y=2、Z=0…15，脚下完整实心地板 Y=1。全程同高，无台阶、转弯或分支，48 个干燥脚位全部互通。
- 两侧浅槽在 Y=1，共 52 格真实水源，水深一格；完整底板、侧墙和首尾端坎封水。8 根落地柱衔接 4 道横梁，两只方块装饰空吊笼以 4 根双节链锚固于完整屋顶，6 支壁火把均有墙支撑。
- 为安全直通，将参考图满地浅水改为两侧浅槽与中央三格干路，将尽头关闭铁门改为可通行拱口。藻痕用少量低位苔石砖表现。铁件用原版铁栏杆、铁活板门与锁链，无定制贴图或实体。

接入须以整个 11×10×16 体积做占用、实体、液体与世界边界检查，再连接两端同高道路。NBT 含显式空气不表示授权清空世界。暂未分配 FT 核心唯一槽位，也未改共享目录中其它 ID。

## 验收证据

- qa/static.json：13 项静态检查通过，包括参考指纹、单调色板、完整体积、通路、水封、柱梁、吊笼链锚与火把。
- qa/native.json、qa/forge-gametest.log：Forge 47.4.10 / Minecraft 1.20.1 / Java 17.0.8 隔离 GameTest 通过。0°、90°、180°、270° 四向实际 StructureTemplate.placeInWorld，合计 7040 条记录。注册表、状态属性、模板往返、碰撞通路、支撑、无容器/库存/实体、水槽边界和外侧一圈无水均通过。
- 水更新启用，tick 200 与 240 回采结构完全稳定；每向 52 水源、48 干燥脚位、6 壁火把。阻路、挖穿水槽底板、移除吊笼顶锚各 4 次负向探针均被正确拒绝。
- preview/four-views.png 为北南西东内部剖视；preview/exterior.png、preview/exterior-south.png 为完整外壳；preview/native-four-views.png 为四向原生回采对照；preview/plan.png 为通路与水封平面。
- qa/visual-review.json 记录对原图、最终结构、四视图、完整外壳、原生回采和通路平面的目视核验。剖视仅隐藏顶盖及近侧双层外墙，柱梁、铁笼、灯和支撑链不移位。部分贴墙火把的支撑墙会被剖开，其最终 NBT 支撑已由 Forge 核验。
- preview/lodestone-interior.png 是最终完整 NBT 的一点透视眼平视角；preview/lodestone-exterior.png 是完整外壳；qa/lodestone.json 记录成功加载 1760 个方块、无浏览器页面错误及画面指纹。
- 首次 QA 因遗漏测试包 pack.mcmeta 找不到空白模板而失败，保留 qa/forge-20261103-101323.log。补齐元数据后在新隔离世界重跑通过，不把首次失败计为成功。

预览为原版模型与纹理的软件或 Lodestone 渲染，并非整合包游戏截图。没有客户端游玩、FPS、多人、FT 核心连接、正式世界占用/幂等或部署验证。

## 复现

1. 在 C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill 执行：node scripts/run-build.mjs <本目录>/tools/corridor-s05.mjs --out-dir <本目录> --name room --data-version 3465 --json。
2. 用 C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe 依次执行 tools/qa_static.py、tools/run_forge.py、tools/render_s05.py。Forge 脚本在本目录 forge/isolated-run-时间戳/ 新建测试世界，不打开玩家存档。
3. 技能目录执行：node scripts/preview.mjs --nbt <本目录>/room.nbt --port 3435 --no-open；再执行本目录 node tools/capture_lodestone.mjs 保存交互预览证据。随后重跑 tools/render_s05.py，把 Lodestone 图加入渲染清单。
4. 核对目视审查仍适用相同最终 NBT 后，执行 tools/finalize.py 更新 qa.json 与 SHA256SUMS.txt。结构变更后须重做静态、Forge、渲染与目视审查，不可沿用旧指纹。

配方、NBT 读写器、静态 QA、渲染器和 Forge 项目源码均在本目录内；依赖已安装 Minecraft Builder、缓存的 Gradle 8.8 / Forge / Java 17、原版 1.20.1 客户端资产，以及本机 Python/Pillow 和 Playwright/Edge。隔离测试世界及构建输出仅作复验现场，不安装到正式实例。
