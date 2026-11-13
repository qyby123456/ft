# S2-02 砂岩藏书蓄水室

状态：制作方自检完成，等待独立复核；不是验收通过或部署许可。S2-03 未开始。

## 参照与结构

本张单独编写 recipe.mjs，未复用第 1 张祭坛厅几何。参考为 S2-02-medium-room.png，尺寸是依据画面估算的方块尺度，并非逐像素复制。结构为 23×14×25，跨区块；原生 Java 1.20.1、DataVersion 3465。

中心是长方形浅水池：底台 9×7，池沿 7×5，水面 5×3，共 15 个源水方块。保留环路、后方主门、右侧侧门、书架箱柜、左后储物壁龛、三个挂灯、枯灌木和橙色墙带。左后壁龛有封闭背墙，不是出口。陶罐用陶瓦与花盆近似；未声明风化、雕饰密度或光照像素一致。

坐标均为局部 XYZ 脚部位置。北入口 [11,1,0]，大厅脚部 Y2，南出口 [11,3,24]，东出口 [22,3,15]。北入口宽 7，南/东宽 3，接口高 3。连接方位与高差必须保留，不能平移成同一标高。完整放置源只有 incoming/G2B-02/room.nbt；assembly.json 没有真实世界坐标或部署操作。

## 自检证据

- 原生注册表、属性、StructureTemplate 往返与 6 个空容器加载通过，无战利品表或预填物品。
- 实际 vanilla 标签绑定后的静态 canSurvive 检查 9 点通过；亮度固定为 15，不代表真实光传播。
- 原生碰撞路径及 0.6×1.8 玩家体积半格采样路线覆盖 17 个目标，含三接口、入口小台阶、水池四周与储物点。离线采样不等于真实游戏物理。
- 15 个源水的底部和四邻围挡已逐点检查，完整屋顶保持。没有执行邻居更新、水流 tick、随机 tick 或世界运行。
- 材质无关的内部占据网格与第 1 张及其余六组现有候选做四向旋转筛查，见 qa/duplicate-selfcheck.json。仅为筛查，不证明语义唯一。
- qa/native-review.json 为本次制作方运行；qa/independent-native-frozen.json 和 qa/independent-routes.json 虽沿用校验器文件名，仍是制作方自检，不是独立复核结论。校验器只读复用独立复核目录的类和标签，未修改复核方文件。

## 真实预览与边界

incoming/G2B-02/lodestone-exterior.png 是完整源结构的真实 Lodestone WebGL 截图。其余 north/south/west/east 为单独 preview-only 衍生 NBT 的真实截图，仅按 derivation.json 切除屋顶及对应近侧遮挡墙；逐坐标核对为指定范围内替换空气，不改变源结构。preview.png 是 north 截图的原样副本。所有截图均不是 Minecraft 实机截图，不是 AI 效果图。衍生结构不得用于部署。

最终源与全部截图、衍生结构的哈希在 registration.json、derivation.json、qa/lodestone-screenshots.json 和 FILE_MANIFEST.json 关联。第 1 张已验收源哈希保持不变，本次未修改其结构或证据。尚无本张的独立验收；复核通过前不得开始第 3 张。

## 复现

使用已安装 minecraft-builder-skill 的 scripts/run-build.mjs 对 recipe.mjs 构建，参数为 --out-dir incoming/G2B-02 --name room --data-version 3465。随后用既有 imagegen Python 环境运行 prepare.py，执行 qa/VerifyDeliveries.java 及原生静态存活/体积路径校验，再运行 node capture.mjs 和 Python finalize.py。finalize.py 会强制核对本次固定源哈希；修改结构后必须重新审查、更新固定哈希并重跑全部证据，而不是沿用旧报告。

可用技能 scripts/preview.mjs --nbt incoming/G2B-02/room.nbt --port 0 打开完整结构预览。没有修改正式存档、mods、config 或部署配置。
