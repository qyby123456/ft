# S3-06 灯角冰桥回廊 — 第6张候选待独立验收

唯一参考：`D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-06-corner.png`。前置放行为复核目录`S3-05-independent/independent-details.json`，状态ACCEPTED_FOR_SEQUENTIAL_PRODUCTION，绑定05的实际哈希。当前只制作06；未读/制作07，不修改此前验收目录或正式存档、mods、config、统一incoming。recipe独立新写，只复用原生验收及viewer截图工具。

## 原图事实及转译

原图1536×1024，观察点在冰路转弯外侧；两条近乎垂直的廊臂包住厚石墙内角。转角石面有木挑梁和暖灯，两侧灰石/深木、青藤、积雪、上挂冰与墙根冰笋，左廊地上有小骨骸；前景冰桥旁可见暗沟。图没有完整出口/罗盘，无法由透视证明精确长度、深渊深度或下游房间。采用37×19×35包围盒，L占地而非满矩形房间，廊内宽13格、两条约30格廊臂；近木撑1格宽、柱基3格左右，转角局部3格厚石角。这些是比例离散化，非声称像素精确还原。

|要素|实际NBT|
|---|---|
|真正L形|北口[8,5,0]朝南进入，到[8,5,26]右转向东，东口[36,5,26]；不设南口/西口。实体内角挡住两端直线，不能直穿|
|墙体与顶|占地x≤18或z≥16；东北未占象限全部显式空气。内廊为x2..14/z2..32与x2..34/z20..32的并集，上部Y15..18封顶，1007个占地柱有屋顶|
|转角地标|灰石内角x14..16/z17..19、两侧木竖撑，主暖灯[13,11,20]悬于木挑梁，顶上雪块。它附着于分隔两腿的墙，非独立中央祭台|
|廊臂装饰|8组贴墙小木撑，另有转角木撑；9盏暖吊灯/9链，46格附墙地衣、侧冰笋与顶冰挂、9骨块形成小骨骸|
|冰桥和浅沟|主地板Y4，脚Y5。桥芯x6..12/z27..30为blue_ice，下面Y1..3为空气，底Y0有实体；雪缘在x5/13，桥面连接前后地面。周边浅沟有可见开口，不是无底深渊或活水|

关键近似：原图细尖冰晶采用方块冰挂/冰笋，青藤采用附墙glow_lichen，薄雪采用snow_block或齐平地板雪斑；不能冒称保留原图透明晶体、藤纹或薄雪模型。浅沟只离散化前景高差，未凭图推断巨大深渊。双端口作为连接点是建筑转译，不代表图中明确可见完整边界。没有传送、箱子、刷怪、战利品或脚本机关。

## 路线和接入

原点[0,0,0]，+X东/+Z南，人工指定北入东出；入口脚位[8,5,0]、安全点[8,5,4]，出口[36,5,26]。两口保守5宽4高，有完整Y4地板。route-targets.json保留10个门脚位和12个目标：双口、安全点、北腿3点、转角、冰桥、转角灯前、东腿3点。主路径固定脚Y5，冰桥可走但转向东口可在桥北侧完成，不宣称桥是必经机关。桥侧有浅沟，未假装全室无跌落可能。

NativeQA.java实际使用原版1.20.1碰撞体，0.6×1.8玩家身体、0.5格网格、每边0.25格中点检查、完整脚下方块、固定脚Y5。路径逐步记录于native-qa.json；没有跳跃、下沟再返回、冰面滑行、连续扫掠或战斗仿真。静态格网可达与真实玩家运动并不等价。

rotated-connectors.json提供非负包围盒四向连接，90/270尺寸35×19×37；引擎须补偿pivot平移。门面放在另一门的外侧相邻一格、朝向相反，不得共面覆盖。入口脚位是Y5，不能沿用前张Y2。整个37×19×35显式空气体积会覆盖现有方块，未来接入须先检验完整体积的占用/实体/世界边界/玩家建筑，冲突就拒绝，不能先清空。未实机旋转拼装。

## 实际检查和证据

inspect.mjs从真实最终NBT读回：DataVersion3465、24605完整唯一坐标、原版ID、无实体/方块实体/容器/活流体/命令、双垂直门、L形净路、直线被实体内角阻挡、桥下空气、1007占地柱顶盖、未用象限纯空气、非空气面连地基、剖面严格只改为空气。逐列屋顶、附着、坐标、路线输入均独立JSON。

原生Java实际读取mapped jar，绑定170个原版block tags，验证注册表和每个显式属性、StructureTemplate.load/save，并对全部24605记录canSurvive；12个身体路线目标有逐步证据。执行在新Temp隔离目录，NBT复制后测试，仅日志/结果回本目录。依赖哈希与05独立放行记录在validation-provenance.json。

暖灯亮度15周边只用不融化的packed_ice/blue_ice、snow_block，无普通ice、snow雪层或活水。世界放置、邻接更新、光照传播、随机刻、重载、Forge游玩仍NOT_RUN；静态支撑不替代世界力学。

八张内外视图：interior-01..04是同源只删剖面四个对角方位，exterior-01..04是完整NBT四个对角方位。eight-view-sheet.png仅为拼版。nave-full-nbt.png沿用工具文件名，实际为本张转角内景，相机[7.5,10.5,29.5]朝[16,10,18]；north-leg-full-nbt.png和east-leg-full-nbt.png分别检查两腿的完整屋顶和内角阻挡。三张内部相机仅在Playwright响应内覆盖viewer相机，不删几何、不修改安装脚本。view-map.json绑定所有实际PNG与NBT哈希。

本张为两口垂直相接、厚内角阻断视线、外弯带浅沟冰桥的转角；05为轴向双口长直廊，04为六口高殿，01～03的中央物/低池/壁龛均未复制。灰石、冰木和暖灯是参考组主题，共通材质不等于拓扑相同；独立视觉还原/重复度验收仍PENDING。本地PASS不等于独立ACCEPTED，不并做07，不部署。

另附bridge-full-nbt.png：完整NBT相机[10.5,12,32]朝[6,4,27.5]，检查雪缘、蓝冰桥与侧沟高差；同样不改几何。当前共8张标准内外视图+4张完整NBT局部内景，额外相机统一记录于view-map.json。

## 复现

在skill根目录`C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill`执行：

```powershell
$room = 'D:/ft工程文件/FT地牢_第3组重做B_20261104/S3-06'
node scripts/run-build.mjs "$room/recipe.mjs" --out-dir $room --name room --data-version 3465
node scripts/run-build.mjs "$room/cutaway.mjs" --out-dir "$room/preview-only" --data-version 3465
node "$room/inspect.mjs"
& "$room/Check-Native.ps1"
```

两个viewer各占一个独立终端，再在第三终端截图：

```powershell
node scripts/preview.mjs --nbt "$room/room.nbt" --port 5327 --no-open
node scripts/preview.mjs --nbt "$room/preview-only/preview-only-cutaway.nbt" --port 5328 --no-open
node "$room/capture.cjs"
node "$room/finalize.mjs"
```

拼版按内景上排/外景下排各4张缩至360×275；finalize校验05放行和前五张完整清单，生成当前SHA256.json并逐文件读回。
