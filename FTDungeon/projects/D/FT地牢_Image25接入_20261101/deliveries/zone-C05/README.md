# zone-C05 水晶角洞

独立拐角房间，Minecraft Java 1.20.1，25×14×25。四件套为 room.nbt、room.json、preview.png、qa.json；另附八张单独视图、两张四联图、平面图、原生世界回采和可复现工程。仅本目录交付，未接入或部署。

北入口 (12,2,0)，内部安全点 (12,2,4)，东出口 (24,2,12)，均为相对模板原点的玩家脚部坐标。北/东端口均5格宽5格高；125格同高主路，至少5格宽、3格净高，两个矩形 XZ=[10..14,0..14] 与 [10..24,10..14] 组成直角路线。底板与625格完整洞顶保留，软件剖切不修改最终NBT。NBT为gzip单调色板、DataVersion3465，共8750条显式记录；放置前必须另行核验整个体积占用，不能据空气记录无条件清场。

保留紫晶群石台、29处有支撑晶芽/晶簇、3组独立落地石柱、4盏有链锚吊灯及2盏箱顶灯、6格废轨、两处33格源水浅池、池沿护栏和实心木箱。池深1格，底部与侧边完整，主路不涉水。无实体、容器库存、刷怪笼、奖励或执行方块。

原图为 D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/corners/C05-corners-crystal-corner-cavern.png。SHA-256为790bb6fe1bbc37401333dd80f98c925abb13f1136459310848daaf7a41053a01，与批次README、docs/manifest.json、docs/verify.json核对；reference/source.json保存两份清单的指纹与C05条目。未重新调用付费生图接口。

概念转译偏差：补齐洞顶和四面围护，新增符合接入契约的北侧中央入口及东出口；晶体用紫水晶块、原版芽簇、方解石和海晶灯转译，外形更方正，无概念图蓝色着色器光晕；矿车实体省略、保留废轨，木箱改为无库存装饰；水池与石柱让开干燥安全通路。外壳为地下埋入式包络，并非地表独立建筑立面。

preview.png选用实际最终NBT的东向剖视；preview/interior-{north,south,west,east}.png与preview/exterior-{north,south,west,east}.png为八张1800×1300单图。仅渲染内存移除近侧墙和洞顶，保留两道横梁及可见柱和灯链。qa/visual-review.json记录逐图审阅。软件采用缓存的1.20.1原版模型与纹理，不等于游戏截图、着色器或整合包FPS证据。

qa/static.json通过结构格式、边界、原图哈希、端口、通路、屋顶、水池和附件检查。qa/native.json为隔离Forge47.4.10 GameTest：实际placeInWorld四向放置，每向8750条，通知邻居并模拟320tick，前后检查35000项旋转状态、原生canSurvive、真实碰撞、灯链/晶芽支撑、125格往返路线、33格池源水、外边界及掉落实体。四份qa/native-placed-*.nbt是稳定后世界回采；qa/capture-comparison.json由独立Python逐坐标检查35000条状态及旋转铁轨，与最终模板一致。最终证据绑定room.json中的完整NBT哈希。

首轮Forge识别出第二个木箱顶被邻接晶芽覆盖、箱顶灯掉落。旧NBT、元数据、静态报告留在qa/rejected-lantern-overlap，失败日志也保留。最终版将第二个木箱/灯移到X=18,Z=22，静态检查加强为要求完整木顶，八视图及全新隔离世界四向320tick全部重做。失败版不得用于接收。

复现：使用 C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe 依次运行tools/build-and-check.py、tools/render-views.py。该Python仅使用Pillow和NBT工具，不调用生图接口。随后在此目录运行 powershell -NoProfile -ExecutionPolicy Bypass -File tools/run-forge.ps1，需要本机缓存Gradle8.8、Java17和ForgeGradle6；脚本创建新的qa/forge-runs时间戳目录，不读取玩家世界。原生测试完成并重新人工审阅八张图后，运行tools/record-visual-review.py及tools/finalize.py封装。

交互预览：在 C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill 目录运行 node scripts/preview.mjs --nbt "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-C05/room.nbt" --port 55405 --no-open。由启动器返回的本机URL可旋转查看完整NBT。

本次仅为制作方独立交付，ready_for_receiver_review=true，ready_for_unified_integration=false。接收方仍需冻结四件套并独立复验，再处理大厅连路、施工占用拒绝、幂等持久化及整合包客户端/多人回归。未修改正式源码/配置、其它ID、mods目录或玩家存档；Forge夹具模组只在本目录运行，不可安装到正式实例。
