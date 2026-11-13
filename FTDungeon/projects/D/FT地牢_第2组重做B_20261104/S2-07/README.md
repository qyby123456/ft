# S2-07 砂岩双扇门入口庭院

状态：制作方自检完成，等待独立复核。未验收、未部署，不开始08，前6张已验收源保持不变。

## 原图与真实结构

recipe.mjs 从空结构独立建立27×17×29原生Java1.20.1模板，DataVersion3465。前方关闭双扇木门、外台阶、开放庭院、高拱门廊、两侧封背储物灯龛、空桶木箱、木框告示牌、石台火盆、橙色地纹和绿藤按本图分别安排。无前图水池、祭坛或转角复用。

尺寸估算非逐像素复刻。原版门2格高，原图高门板比例简化；六个空桶近似木箱；陶瓦/花盆近似陶罐；告示牌为深木框/浅木心，无地图或文字；点燃营火近似火盆，保留真实危险。见ANALYSIS.md。

## 门、台阶、锚点

唯一供后续集成评审的闭门源：incoming/G2B-07/room.nbt。北外平台脚部[12,1,0]，四格宽实际X11..14。两级原生楼梯分别Z1/Y1和Z2/Y2，升到庭院脚部Y3。双门X12/13,Z4，门下半Y3、上半Y4，左右铰链、未供能、原状关闭。双门宽2，不能把外平台宽4误报为门宽。

后门连接[13,3,28]，五宽六高，厅面/门廊脚部Y3；侧灯龛封背不是出口。assembly.json记录局部装配，无世界坐标。模板包含显式空气，未来须审查整个27×17×29包络清地形影响。

## 关门阻挡与开门分析

闭门源身体路线如实记录：4个外部目标可达，12个内部目标受门阻挡，不报闭门可通。闭门原版加载检查只核外侧站位，不宣称穿门寻路通过。

analysis-only/incoming/G2B-07/room.nbt仅将4个门半块的open从false改true；其余方块、容器、实体及尺寸完全相同。gate-state-derivation.json逐格记录差异。开门副本的原生网格及0.6×1.8半格身体路线16目标均通过，覆盖台阶、门内、庭院、后廊、两龛与火盆外围。网格检查对开门形状使用中心探针，身体检查另外使用实际原版碰撞盒。不是实际点击、双扇同步或世界刻测试，副本不可部署。

qa/gate-route-summary.json明确绑定两个哈希与各自结论，不能拿开门路线冒充闭门源路线。目标按连接口2个加内部/台阶14个计数；独立复核可另将interior重复计入，口径须说明。

## 原版与支撑自检

- 注册表/属性、StructureTemplate往返及7个方块实体加载通过：6空桶、1营火，无战利品与生物实体。13311条方块记录含显式空气，24个palette状态。
- 真实原版标签与静态canSurvive共23点通过：4门半块、8藤、3灯笼、4花盆、4枯灌木。使用固定亮度15，不等于实机光传播。
- qa/IndependentNativeAudit.java是既有复核校验器的本地副本，只额外选择门半块做存活检查，未修改复核目录原文件。所有带independent文件名的本目录报告均为制作方自检，不是本张独立验收。
- 三吊灯逐链连实顶/灯龛帽，藤每格背靠石柱，花盆有下托、灌木在支持沙上、营火在石台上，六面实块图全连底，无分离灯座。拓扑检查不是力学或物理认证。
- 实心支持和几何净空按北外平台X11..14、后口X11..15逐格验证；实际双门为条件通路。6桶全部空，顶部桶有下桶支持；未验证玩家开桶交互。

## 真实预览与动态边界

lodestone-exterior.png和lodestone-entrance.png均从闭门完整源渲染，保留门、台阶、门廊顶；preview.png为entrance原样副本。另有4个方向剖切，衍生NBT仅在derivation.json声明的屋顶/近墙范围替换空气。北剖切会删除门，不用于证明门可通行；完整源图和门状态分析单独绑定，四向衍生结构均不可部署。

六图为真实Lodestone WebGL截图，不是AI效果图或Minecraft实机截图。qa/lodestone-screenshots.json绑定源/衍生源与截图哈希。registration.json为复核入口，FILE_MANIFEST.json覆盖全包。

庭院露天，仅后门廊有顶。点燃营火位于[19,4,15]，在石台上，主路线避开，但依然存在伤害/烟气；不宣称消防或玩法安全。世界刻、门交互/更新/同步、营火伤害与熄灭、实际光照/天气/刷怪/上方地形、旋转/镜像均NOT_RUN，见qa/dynamic-boundaries.json。

## 本机复现

顺序：技能scripts/run-build.mjs recipe.mjs --out-dir incoming/G2B-07 --name room --data-version 3465；现有Python运行prepare.py；PowerShell运行validate-native.ps1；Node运行capture.mjs；Python运行finalize.py。最终脚本固定源与开门副本哈希，改几何后必须重审并重跑全证据。

技能C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill；Python C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe。可用技能scripts/preview.mjs --nbt incoming/G2B-07/room.nbt --port 0打开闭门完整源。

未修改正式存档、模组、配置或部署；完成停候独立复核。
