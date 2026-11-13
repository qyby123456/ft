# S2-03 砂岩单箱遗迹庭院

状态：制作方自检通过，等待独立复核，未验收、未部署。只制作本张，第 4 张未开始。已验收的 S2-01 与 S2-02 源保持不变。

## 单张构图与差异

独立空白 recipe.mjs 构建 19×11×21 的原生 Java 1.20.1 结构（DataVersion 3465），没有导入前两张几何。主地标是一个空箱和 3×3 极低半砖环座，中心仅 1×1 实心托座；周围橙色十字地纹、左侧陶罐壁龛、右侧小龛、后方与右侧拱门、前方低墙台阶。没有水池、高祭坛、书架或挂灯。

原图为露天遗迹，因此完整源主动保留开放天空，而非截图切顶。尺寸比例为估算，不承诺像素一致。原图陶罐用陶瓦与花盆近似，挂枯植物用附墙橙色陶瓦短条近似，外部沙丘和仙人掌不纳入模板。详细取舍见 ANALYSIS.md。

## 源文件与接口

唯一可供后续集成评审的源是 incoming/G2B-03/room.nbt，不表示已获部署许可。完整包络、局部原点、装配次序见 assembly.json；没有世界坐标。

北入口 [9,1,0]，宽 5、高 3；南出口 [9,2,20] 与东出口 [18,2,13]，各宽 3、高 4。大厅脚部 Y2，北外平台 Y1，通过原生楼梯过渡。左壁龛不是出口。三口完整边界截面的地面与净空已静态逐格检查；后续旋转/镜像没有验证。

## 自检与动态边界

- 原生注册表属性、方块实体加载和 StructureTemplate 回读通过；4389 条方块记录包含显式空气。15 种 palette 状态，1 个空箱，无战利品表或实体。
- 真实原版标签绑定后的静态 canSurvive 共 10 点（6 花盆、4 枯灌木）通过；使用固定亮度 15，不代替实际光照。
- 原生碰撞路线及 0.6×1.8 玩家体积半格采样的 17 个目标通过，含 3 个连接口、入口台阶、箱子四周和壁龛接近点。半格路线为离线形状采样，不是实际游戏移动测试。
- 六面实块图全连底，无悬空组件；沙块有实心下托，箱底实心、箱顶留空。拓扑连通不是物理附着模拟。
- 没有液体、火或仙人掌等危险块。开放顶部的天气、积雪、光传播、刷怪、上方地形与邻居/随机 tick 未运行，必须留待集成检查，见 qa/dynamic-boundaries.json。
- qa/independent-native-frozen.json 与 qa/independent-routes.json 是制作方只读复用既有校验器后生成的自检证据，文件名不代表独立验收。
- 材质无关的内部占据网格四向旋转筛查覆盖两张已验收源及其他 6 组候选，见 qa/duplicate-selfcheck.json；不能据此证明语义唯一。

## 真实预览

incoming/G2B-03/lodestone-exterior.png 是完整源实际 Lodestone WebGL 截图，preview.png 为它的原样副本。另有 north/south/west/east 四向近墙剖切截图，衍生 NBT 单独存于 preview-only；仅在 derivation.json 声明范围内替换空气，不改原源、台阶或箱子。原源无屋顶，无切顶伪装。四份衍生结构均禁止部署。

截图不是 AI 效果图、不是软件替代渲染，也不是 Minecraft 实机截图。qa/lodestone-screenshots.json 关联源、衍生文件、相机参数和截图哈希。registration.json 是复核入口；FILE_MANIFEST.json 为全部交付文件的哈希清单。

## 本机复现顺序

在本目录运行技能 scripts/run-build.mjs recipe.mjs --out-dir incoming/G2B-03 --name room --data-version 3465，再用现有 Python 环境运行 prepare.py、PowerShell 运行 validate-native.ps1、Node 运行 capture.mjs，最后 Python 运行 finalize.py。后者固定本次源哈希；改结构必须重跑全部证据并重新审核，不能沿用旧报告。

技能目录 C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill；Python C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe。可用技能 scripts/preview.mjs --nbt incoming/G2B-03/room.nbt --port 0 打开完整结构。

未修改正式存档、模组、配置或部署项；本张独立验收前，不开始 S2-04。
