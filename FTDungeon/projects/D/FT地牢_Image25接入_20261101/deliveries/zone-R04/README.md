# zone-R04 · 焦毁营房

独立地牢分区房间，Minecraft Builder / Lodestone 制作，Java 1.20.1 / DataVersion 3465，尺寸 **25 × 13 × 25**。仅写本交付目录；未部署、未改正式模组或资源，未打开或修改玩家存档。

- 四件套：room.nbt、room.json、preview.png、qa.json。
- 北入口脚位 [12,2,0]；内部安全点 [12,2,5]，中央备用安全点 [12,2,12]；南出口 [12,2,24]；朝北。南北接口宽 3 格、高 4 格。
- 中央 X=11–13、Y=2–4、Z=0–24 为连续三格宽、三格高平地通路；床侧走道与两个安全点均可步行到达。
- 四组 2×2 落地石柱贯通至梁和屋面。两组双层床架共四层床板，由四角连续木柱承托，梯子有完整背板，破布铺在顶半砖上。
- 两条阶梯式斜落木梁逐段以黑石、深板岩残骸垫实至地面；六盏灯中四盏由梁上的链条吊挂、两盏落在木台上。
- 焦墙、灰堆、破布、焦红旗、骨块和铜盔状楼梯表现废墟。余烬采用菌光体、橙玻璃与铁盖；无真火、熔岩、流体、伤害方块、实体、方块实体、容器、库存或战利品。
- 屋顶 Y=11 连续封闭，保留焦木承重梁与外围残垛；两处冷色玻璃天窗表现破损采光。概念图的开放缺口改为玻璃封口，以适配独立地牢室内边界。

参考图 SHA-256：e04fa19324a4906d5110666f5b274868dd296e3f14f4d4657e2811de960b506a。

最终 NBT SHA-256：da8e9f21e39d9bd24991261eff4f04d56dde825a545b8c7e857459c3f9ef185f。

reference/ 保存 R04 概念图、原提示词和原批次 README、manifest、verify 的只读副本；源图、批次 manifest 和 verify 三处指纹一致。概念图不是游戏截图。

## 验收证据

- qa/static.json：94 项检查通过，8125 个完整显式坐标、3366 个非空气方块、37 个方块状态、440 个平地可达脚位。检查入口、安全点、通路、四柱、床架/梯子/破布、断梁、灯链支撑、封顶与禁用方块。
- qa/native.json 与 qa/forge.log：Minecraft 1.20.1 / Forge 47.4.10 隔离 GameTestServer 实际四向放置，触发邻块和形状更新，开启火焰刻和正常随机刻，加载夹具区块后运行 600 tick。四向前后共 65000 次方块状态比较完全一致；每方向检查 3366 个非空气方块生存条件、2810 个外边界空气位置、440 个可达脚位，实体为 0。
- views/interior-{north,south,west,east}.png 与 views/exterior-{north,south,west,east}.png：最终 NBT 的四向内部和四向完整外壳。内视仅在渲染时移除顶盖和临近墙，保留承重梁，不改交付 NBT。
- views/interior-four-views.png、views/exterior-four-views.png：八向总览；views/entrance-eye.png、views/safe-point-eye.png：完整 NBT 的入口与安全点步行视角；qa/visual.json 记录目视核对。
- delivery-manifest.json 保存交付文件完整 SHA-256；zone-R04-handoff.zip 包含四件套、参考、QA、视图与复现源码，不含隔离测试世界、Gradle 缓存或构建 JAR；package-receipt.json 保存 ZIP 指纹。

预览为实际 NBT 的 Lodestone 软件渲染。原生验收使用独立服务器，未做完整整合包客户端、多人或正式地牢接入测试。

## 复现

使用已安装 Minecraft Builder，node 运行技能 scripts/run-build.mjs，输入 tools/recipe.mjs，输出本目录，参数 --name room --data-version 3465 --json。随后运行 node tools/static-qa.mjs、PowerShell tools/run-forge.ps1。

使用技能 scripts/preview.mjs --nbt 本目录/room.nbt --host 127.0.0.1 --port 41904 --no-open 启动本地预览，再运行 node tools/render.mjs。目视核对最终视图后运行 node tools/finalize.mjs 更新汇总与交付包。源码中的本机运行时路径仅供此工作站复现。

## 接入边界

唯一 ID 固定 zone-R04。槽位尚未分配，接入负责人应完整检查 25×13×25 体积（包括显式空气）、世界边界、占用、流体与实体，确认南北接口均有安全接路后再接入。存在占用必须拒绝，不可用含显式空气的模板覆盖既有建筑。独立验收通过不表示已部署。
