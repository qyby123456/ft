# S2-04 砂岩旗帜列柱大殿

状态：制作方自检通过，等待独立复核；不是验收或部署许可。只制作本张，第 5 张未开始，前 3 张已验收源保持不变。

## 单张构图

本张 recipe.mjs 从空结构独立构建 29×21×39 的原生 Java 1.20.1 模板，DataVersion 3465，多区块体量。中心是长轴线的四段菱形橙色地纹，无台、池或箱；两排六根柱子分隔中央通道与侧廊，配九面原生橙色墙旗、八盏长链吊灯、升高后门、左右拱门、陶罐和残砖。不是把前图中央台换材质。

原图比例为方块尺度估算，不逐像素一致。墙旗采用原版黄色边框与菱纹，幅长与细节简化；陶罐为陶瓦/花盆，残砖为贴地半砖。外部沙丘与仙人掌不在模块内。屋顶为后段 Z8..38 完整阶梯拱壳，前段 Z4..7 保留露天低墙入口，不声称全封闭。与原图关系及取舍见 ANALYSIS.md。

## 源与接口

唯一可供后续集成评审的源为 incoming/G2B-04/room.nbt。assembly.json 记录局部包络及装配，无真实世界坐标。北入口 [14,1,0]，宽7高4；南出口 [14,3,38]，宽5高4；西/东出口 [0,2,19]、[28,2,19]，各宽5高4。厅面脚部 Y2，北外平台低一格，南出口高一格，均有原生楼梯。不要抹去接口高差。

## 自检证据

- 原生注册表属性、9 个旗帜方块实体加载与 StructureTemplate 回读通过；23751 条方块记录含显式空气、17 个 palette 状态，无容器、战利品或实体。
- 真实原版标签绑定的静态 canSurvive 20 点通过：8 灯笼、6 花盆、6 枯灌木。固定亮度15，不等于真实光传播。
- 九面墙旗独立核对实心背板与 Patterns NBT，并原生加载；不虚计入上述20点 canSurvive 检查。世界中的邻居更新未执行。
- 八条灯链逐格直连相应屋顶，六面固体图全连底，无分离组件。梁、柱帽、拱顶有六面路径；这是静态拓扑，不是结构力学或重力认证。
- 四口边界完整声明宽高净空及下托逐格检查。原生碰撞路线及0.6×1.8玩家体积半格离线路线覆盖18目标：4口及14个入口台阶、长轴、前后左右侧廊和后门台阶目标。不是实际玩家移动或所有连续轨迹证明。
- 无液体或危险块；6格沙有实心下托。前段敞口的天气、积雪、光照、刷怪、上方地形，以及世界刻、交互、邻居/随机更新、旋转/镜像，均未运行。见 qa/dynamic-boundaries.json。
- qa/independent-native-frozen.json 与 qa/independent-routes.json 沿用校验器文件名，但由制作方只读复用既有工具运行，是自检而非本张独立验收。
- 材质无关内部占据网格与前三张已验收源及其他6组候选四向旋转筛查，见 qa/duplicate-selfcheck.json。指标不证明语义唯一。

## 真实预览

incoming/G2B-04/lodestone-exterior.png 为完整源实际 Lodestone WebGL 外观。north/south/west/east 为四个独立衍生 NBT 的实际内景截图；preview.png 原样复制 north。剖切只在 derivation.json 声明的屋顶/梁或近墙范围替换空气，逐坐标与源比较，不能据剖切图判定原灯链悬空。preview-only 下全部衍生结构不可部署。

截图不是 AI 效果图，也不是 Minecraft 实机截图；不能代替世界刻验证。qa/lodestone-screenshots.json 绑定完整源、衍生源、相机与截图哈希。registration.json 为独立复核入口，FILE_MANIFEST.json 为完整文件哈希清单。

## 本机复现

在本目录按顺序执行：技能 scripts/run-build.mjs recipe.mjs --out-dir incoming/G2B-04 --name room --data-version 3465；现有 Python 运行 prepare.py；PowerShell 运行 validate-native.ps1；Node 运行 capture.mjs；Python 运行 finalize.py。最终脚本固定本次源哈希，任何几何修改后必须重审并重跑全部证据，不可沿用旧报告。

技能目录 C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill；Python C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe。可用技能 scripts/preview.mjs --nbt incoming/G2B-04/room.nbt --port 0 打开完整源预览。

未修改正式存档、模组、配置或部署项；独立验收前不开始 S2-05。
