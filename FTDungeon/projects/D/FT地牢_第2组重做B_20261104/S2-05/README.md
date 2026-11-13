# S2-05 砂岩纹带积沙长廊

状态：制作方自检通过，等待独立复核；不是验收或部署许可。只制作本张，第6张未开始，前4张已验收源保持不变。

## 单张构图

本张 recipe.mjs 从空结构独立构建15×12×41的原生Java1.20.1模板，DataVersion3465，多区块。核心为低顶平直通道、六道附墙门肋、三盏中轴吊灯、橙色地纹/墙带、四组贴墙积沙及四个陶罐。无独立柱列、中央台、水池、旗、箱柜、实体或机关。

原图比例按方块尺度估算，符号用錾制红砂岩近似，罐用陶瓦/花盆近似，沙坡用实心砂岩、下半砖及有下托的沙块近似。不是像素一致复刻。顶面Y10有两处真实2×3采光缺口，共12个开口格；其余顶面完整。缺口并非剖切预览伪装，不声明完全防雨封闭。详细取舍见ANALYSIS.md。

## 源与锚点

唯一可供后续集成评审的源为incoming/G2B-05/room.nbt。assembly.json记录包络及局部装配，无世界坐标。北[7,2,0]、南[7,2,40]，各宽5高4，脚部均Y2，与厅面同高。无侧门、无高差。X6..8、Y2..5、Z0..40为逐格核验的全空气直行带；两端完整5×4净空及实心下托均通过静态检查。

## 自检及动态边界

- 原生注册表属性、StructureTemplate回读通过；7380条方块记录含显式空气，14个palette状态，无方块实体、容器或实体。
- 真实原版标签绑定的静态canSurvive共11点：3灯笼、4花盆、4枯灌木。固定亮度15，不代替实际光照。
- 三灯链逐格Y8/9接至Y10实顶，避开采光缺口；六面实块图全连底。12格真实沙均有实心砂岩下托。拓扑连通不是结构力学或重力模拟。
- 原生碰撞路径及0.6×1.8玩家体积半格离线路线覆盖16目标：2端口、14个门肋/灯下/贴墙装饰接近点。不是实际游戏移动或全连续路线证明。
- 无液体或危险块。采光缺口下雨雪、光传播、刷怪、上方地形，以及世界刻、交互、邻居/随机更新、旋转/镜像均未运行，见qa/dynamic-boundaries.json。
- qa/independent-native-frozen.json与qa/independent-routes.json沿用校验器文件名，由制作方只读复用既有工具运行，是自检，不是本张独立验收。
- 材质无关内部占据网格四向筛查覆盖前4张已验收源及其他6组候选，见qa/duplicate-selfcheck.json；只能筛查，不能证明语义唯一。

## 真实预览

incoming/G2B-05/lodestone-exterior.png和lodestone-axial.png均渲染完整源；后者为近端朝远端的内部轴向视角，保留真实屋顶、全部门肋和灯链。preview.png为轴向图原样副本。另有north/south/west/east四个剖切方向，单独衍生NBT只在derivation.json声明范围替换空气，逐坐标核对。四个preview-only衍生结构禁止部署，切顶图的链条截断不代表原结构悬空。

六张均为实际Lodestone WebGL截图，不是AI效果图或Minecraft实机截图。qa/lodestone-screenshots.json关联源、衍生源、相机与图片哈希。registration.json为独立复核入口，FILE_MANIFEST.json为完整文件哈希清单。

## 本机复现

在本目录顺序执行技能scripts/run-build.mjs recipe.mjs --out-dir incoming/G2B-05 --name room --data-version 3465；现有Python运行prepare.py；PowerShell运行validate-native.ps1；Node运行capture.mjs；Python运行finalize.py。最终脚本固定本次源哈希，改几何必须重审并重跑全部证据。

技能目录C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill；Python C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe。可用技能scripts/preview.mjs --nbt incoming/G2B-05/room.nbt --port 0打开完整源预览。

未修改正式存档、模组、配置或部署项；独立验收前不开始S2-06。
