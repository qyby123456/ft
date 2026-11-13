# S2-06 砂岩灯龛折角廊

状态：制作方自检通过，等待独立复核，不是验收或部署许可。仅制作本张，第7张未开始；前5张已验收源保持不变。

## 真实转角与单张构图

recipe.mjs 从空结构独立构建25×13×25原生Java1.20.1模板，DataVersion3465。实际实块包络为L形，北臂X7轴线向南到[7,2,17]，再沿Z17向东接到[24,2,17]。不是直廊旋转，也不是交叉路口；南方直行被外墙封闭，东北缺角没有地板。

内拐角主体转折、正面吊灯龛、两臂各两道门肋、橙色折线地纹与墙带、贴墙沙堆/陶罐/枯灌木按图独立安排。内灯[11,6,14]由链Y7/8接入Y9附墙顶帽，底台Y2..4留出灯龛，背靠主体；两臂灯分别[7,8,5]、[20,8,17]，均链连Y11屋顶。无台、箱、水、旗或独立柱列。

原图比例是方块估计，非逐像素一致；陶罐为陶瓦/花盆，沙坡为实心砂岩、受托沙和低半砖，符号以錾制红砂岩近似，枯垂叶细节省略。顶面有一个真实2×3采光缺口，其余L形顶面完整，不能声称全封闭。见ANALYSIS.md。

## 源文件与接口

唯一可供集成评审的源是incoming/G2B-06/room.nbt；assembly.json记录局部包络，无世界坐标。北[7,2,0]、东[24,2,17]，相互垂直，均5宽4高，厅面脚部Y2无高差。两端声明截面全部净空/实心下托逐格检查。

连续三格宽、四格高的L通路：X6..8/Y2..5/Z0..18，与X6..24/Y2..5/Z16..18的并集，全部为空气且有下托；16个路线目标覆盖两个接口、两臂、转弯与装饰接近点。

完整矩形模板显式写入空气：东北缺角及边缘空气也属于25×13×25放置包络，实际放置可能清除对应原地形。未来必须审查整个包络，不能仅检查L形固体，也不能把缺角随意作为出口。本次无部署。

## 自检与动态边界

- 原生注册表属性及StructureTemplate回读通过；8125条记录含显式空气，14个palette状态，无方块实体、容器、战利品或实体。
- 真实原版标签绑定后的静态canSurvive共11点通过：3灯笼、4花盆、4枯灌木。亮度固定15，不代表实际光传播。
- 三盏灯逐格核对链条至实顶/附墙帽；所有实块六面连底，无分离灯座。8格真实沙均有实心下托，沙上枯灌木和陶瓦上花盆分别检查。图连通是静态拓扑，不是物理/重力认证。
- 原生碰撞及0.6×1.8玩家体积半格离线路线16目标通过，双口支撑净空与三宽连续拐弯带另逐格验证。不是实机移动、所有连续轨迹或旋转认证。
- 无液体、火或仙人掌等危险块；采光缺口天气、积雪、实际光照/刷怪/上方地形，世界刻、交互、邻居/随机更新与旋转/镜像均NOT_RUN，见qa/dynamic-boundaries.json。
- qa/independent-native-frozen.json与qa/independent-routes.json沿用校验器文件名，但为制作方只读复用工具运行的自检，不是本张独立验收。
- 材质无关内部占据网格四向筛查覆盖前5张已验收源与其他6组候选，见qa/duplicate-selfcheck.json；指标不能证明语义唯一。

## 真实Lodestone预览

incoming/G2B-06/lodestone-exterior.png及lodestone-corner.png均为完整源真实WebGL截图，后者从转角外侧内部看内拐角与两臂，保留真实屋顶/链/灯龛。preview.png是corner的原样副本。四向north/south/west/east剖切另存preview-only衍生NBT，只按derivation.json声明范围替换空气，并逐坐标核对。切顶会截断链的上部，不代表原结构悬空；衍生结构禁止部署。

六张均为实际Lodestone截图，不是AI效果图或Minecraft实机截图。qa/lodestone-screenshots.json绑定源/衍生文件、相机与图片哈希。registration.json为独立复核入口，FILE_MANIFEST.json为全部交付文件哈希清单。

## 本机复现顺序

在本目录执行技能scripts/run-build.mjs recipe.mjs --out-dir incoming/G2B-06 --name room --data-version 3465；现有Python运行prepare.py；PowerShell运行validate-native.ps1；Node运行capture.mjs；Python运行finalize.py。最后脚本固定本次源哈希，几何修改需重新审核并重跑全证据。

技能目录C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill；Python C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe。可用技能scripts/preview.mjs --nbt incoming/G2B-06/room.nbt --port 0打开完整源预览。

未修改正式存档、模组、配置或部署；本张独立验收前不开始S2-07。
