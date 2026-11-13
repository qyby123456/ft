# S7 visual-depth-v3 新版本整改候选

状态：15/19/20优先项已制作与自检，剩余逐房视觉与交互继续开放；不是整组通过。旧0d1d1906合并与其正式清单、冻结证据不修改。

|房间|本版修改|候选SHA-256|
|---|---|---|
|S7-11|原哈希继承|0f22ddfdbd072596384bc8576ced98b706240c6f8a9c8b1583d070394fc72b89|
|S7-12|原哈希继承|4e61eb1af2eb6134e6745ff8469b3de9008b6a9a4919a2a7229a06de50a6a4d0|
|S7-13|原哈希继承|275433233782748c5898e34e7c11131df97b29694e087372ba794811cc38e4b9|
|S7-14|原哈希继承|0ef237823e51c9c7ba626ec4edf68d5d3dad66df9db68814f3757dcc28698ffa|
|S7-15|新候选|2fbd5e920170f35d6c72e62377df938650bc69337dad37c8da834c1aa1b2dca7|
|S7-16|原哈希继承|78c1d968be212b027619939badcf1dc248414a694a90c85c5d7f2839bed27518|
|S7-17|原哈希继承|bf7a18c21933c01e0834e088860c22aa4070e5b2425e7683e54e72baee5ccd91|
|S7-18|原哈希继承|fe1bb4b101b0d14781a59138631b03fe4da1324d2ca63c0fd51f93bfa8d50948|
|S7-19|新候选|4082e63fcde97a432e70786b2fb32a9460e8da7f24edfb865be777affb1d4505|
|S7-20|新候选|00fa50ef3af01ffd9e0c870bcd1538f38219dd1ca740d2eebf7a04753b165408|

新合并SHA-256：df8c0e02dcd2b362a2c059c39764f43f11c1b3a2bf633b905646a40a47df16d5；尺寸[326, 40, 151]；15个分片。方块、BE与3实体完整装备/位置反拼见assembly.json。原目标保留，19坐标随功能区域+18 X平移；新增高台视点和三器械接近点。初始目标99点，全宽采样1134点均通过；全非空气185113格零失败/不支持。这些是制作方检查，不是独立放行。

19真实高台视点脚点[28.5,9,18.5]、眼位[28.5,10.62,18.5]，作为明确目标通过原生静态碰撞；站位图使用完整NBT，未剖去玻璃/栏杆。原功能台、楼梯及上下6格高差保留，仅扩充窗外受围护的观景洞体。

20保留当前宝箱入口视线、密道、金堆与原拉杆支撑，增加顶部/左右菌岩体块。15增加明确器械但为可选侧线，未以删目标/家具换通行。

清场方案：assembly/clearance/clear-entire-bounds.mcfunction，110条fill覆盖1969040格完整外包，含分片省略的空分区；每条≤32768且不重叠。只生成，不执行；执行锚点需是未来批准的整结构最小角，零旋转，必须先全清场再放任意分片。它只处理方块，不删除世界实体。

复现：node <skill>/scripts/run-build.mjs S7-XX/recipe.mjs --out-dir S7-XX --json；工单Python -X utf8 revise.py XX；node render.cjs <NBT>；Python -X utf8 qa.py <NBT>。assemble.py重建合并与分片，routecoverage.py验证走廊采样，clearance.py重建清场方案。reproduce.py十房均逐格/载荷一致，8房字节哈希一致，11/12记录顺序导致字节哈希差异不冒称一致。

依赖：本目录common/qa/revise/render/assemble，技能安装目录的run-build/preview和固定Lodestone依赖，原生审核类及映射1.20.1 jar，nbtio.py。外部路径与文件哈希见external-dependencies.json。旧7房原生报告保留旧路径作为原哈希证据；15/19/20及新合并均新跑。原世界1200刻测试不覆盖这三个新哈希，不能继承为新版世界通过。

仍未闭合：
- Independent review of new 15/19/20 hashes and new merge/parts
- 11: thick entry, upper ladder real-player access
- 12: thick cell frame and real door control interaction
- 13: embedded layered stove and entry structure
- 14: weapon rack detail and entry frame
- 16: layered stone-soil base, smaller fungi, side access
- 17: thick entry and shelf cave relief
- 18: thicker niche frames and coffin side relief
- New source world updates for 15/19/20 and merged structure
- Player chest/lever/menu interaction, full traversal and multiple orientations
- Independent coverage proof for continuous width at every turn; sampled native checks alone do not prove continuous global clearance
- Clearance plan independently inspect and authorize before any deployment; not executed
