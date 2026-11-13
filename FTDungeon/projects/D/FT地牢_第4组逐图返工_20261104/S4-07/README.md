# S4-07 沼泽双门接引前庭

本张制作完成、自检通过，停候独立复核。仅07获授权；08及以后未开始。实际世界刻、玩家开门交互、火盆伤害/火行为、世界物理、整合与部署均NOT_RUN。正式存档、mods与config未改动；前六张文件清单由finalize.py逐项回读保持一致。

前置独立真值：`D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-06-independent/independent-details.json`，状态ACCEPTED_FOR_SEQUENTIAL_PRODUCTION，SHA0c5f783f22bd6d7f4cf7fcf20a0ea43191839b13ece344108727f167d35cacc0。本张acceptance.json仅为制作方自检，不能代替独立验收。

## 单图构成与近似

唯一参考 `D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-07-entrance.png`，先核图后独立编写几何。入口构图由南侧双扇木门、外台阶与门柱暖灯、露天积水前庭、两侧木架桶堆、左后木框告示板、离中轴火盆、后方高拱和阶梯状残墙组成。未复用06转角或05长廊几何，QA与截图逻辑复用。

尺寸29×20×29根据画面比例估算，非测绘尺寸。Java1.20.1，DataVersion3465；6,750条显式方块记录、28种状态；9个方块实体为8个空桶和1个点燃营火，无游离实体。桶堆分两侧台座，有一层叠桶；告示板用深色木框、桦木板和雕纹砂岩形成浅色图纹，明确是地图/公告画面的方块近似，不是依赖世界map数据的filled_map。

火盆是石座上的原版lit campfire，位置(9,7,17)，与路径目标X至少相隔3格；未把它替换成无火装饰。伤害、烟、随机刻和实际玩家靠近体验未运行。框灯采用暖色蛙明灯和木托，外部沼泽树、水域、雾和光影反射未生成。268格裸水面占23×19=437格庭内底平面约61%；中央含水上半砖近似原图浸水石道，保持身体路线所需的连续承重，不是世界水物理认证。

全部建筑方块（排除水与植物）六面连接到基础，包括双门柱、桶台、告示板、侧灯横托、火盆和前灯托座；不把睡莲合法分离判为悬空建筑。后方高拱保持通透，前庭无完整顶盖，残墙阶梯收低到前沿是设计。

## 候选、门和台阶路线

- 唯一完整候选为`incoming/S4-07/room.nbt`，与`nbt/s4-07-swamp-gate-courtyard.nbt`逐字节相同；元数据为`incoming/S4-07/room.json`。
- 原点为模板最小角，X东、Y上、Z南。南侧入口脚底(13,3,28)，北拱脚底(14,5,0)。南侧台阶Z28/Y2、Z27/Y3、Z26/Y4，均朝北的bottom石砖楼梯，逐半格上升到门槛脚底Y5；六格宽梯面收束到两格宽门口。
- 双扇门在X13/14、Z25、Y5/6，facing=south，相反铰链、上下半齐全，交付状态均CLOSED且powered=false；Y7..8有实体门楣。门口最小净高2格、宽2格，不能宣称3×4×3开放接口。元数据南口depth3表示外梯进深，沿程脚底高度变化，不是同高矩形净空。
- 北拱是3宽×4高×3进深支持净空。15个目标覆盖外梯、两扇门各自位置、庭内石道和北拱；不把闭门候选标记成可直接穿门。
- `qa/body-routes.json`分两种内存场景：原样CLOSED时从外梯不能走到庭内；只将4个既有门半的OPEN属性改为true后，0.6×1.8身体按0.5格步进与水平5点扫掠，15个目标全可达。反向从北拱经开门下梯回南入口也通过。没有给门挖空气洞，没有修改交付NBT；开门、关门、红石与同步更新并非实际世界玩家交互认证。
- 通用VerifyDeliveries假设入口朝北、全格承重且开放，不适用本张闭门南入口/楼梯。因此其路径段明确禁用，`native_collision_path_reachable_positions=0`不是通过路径或路径失败计数；该工具只用于原版NBT、注册表、方块实体和StructureTemplate往返。门/台阶/接口由独立BodyRoutes原生碰撞场景检查，不能混淆两份证据。
- 未旋转时，若南入口脚底定位到世界A，则模板原点=A−(13,3,28)。将来导入须全29×20×29体积占用拒绝、适配梯前高程和北口连接，不能只验入口或非空气方块。本次未导入、未验证旋转拼接与世界边界。

## 实际Lodestone视图与裁切规则

- `preview-exterior.png`完整候选全景；`preview-gate-stairs.png`完整闭门和外梯近景。
- `preview-interior.png`庭内相机看后方高拱、板、桶与火盆；`preview-board-barrels.png`完整候选的细节视角。原始墙、门、石道均保留，未裁切。`preview-interior-night.png`为预览夜光预设，不能用于世界刷怪亮度认证。
- `preview-cutaway.png`使用`preview-only/upper-cutaway.nbt`，明确PREVIEW ONLY。唯一规则为原坐标 **Y≥12的非空气方块置为空气**，保持29×20×29包围盒，保留低处双门、板、桶、火盆、庭地、台阶和水。`cutaway-check.json`逐格断言只减为空气，无新增、换材或移动；裁切体绝不部署。
- `capture.cjs`调用技能Lodestone ThreeStructureRenderer与Edge WebGL，仅注入相机控制句柄，未替换材质/结构/场景；HTTP实际NBT与候选/裁切文件字节及SHA一致。六图相机和来源摘要见`preview-check.json`。外部环境与地图细节的缺失已在元数据说明。

## 检查与复现

`review.py`检查尺寸、禁用项、8个空桶与营火方块实体、成对门状态、睡莲支撑、流体侧底无直接空气边、全建筑连地和火盆离路线；`geometry-check.json`与`materials.json`保存结果。`qa/vine-stability.json`绑定170个原版标签，包含门/营火、墙藤与洞穴藤、睡莲及含水石道的canSurvive和六向updateShape固定点检查。状态变化0，调度刻仅计数抑制，没有执行世界流体/随机刻和完整传播调度。

`native.py`对实际候选执行原版解码、注册表、9个方块实体加载和结构往返；本地fixture仅剥离源映射jar失效签名元数据，类字节不改、源文件不动，见`qa/fixture-provenance.json`。`compare.py`对可得本地NBT做完全占位几何去重及粗网格旋转筛查；仅表明样本范围无完全几何重复，不是独立视觉原创认证。

`acceptance.json`记录SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE与世界/部署NOT_RUN；`SHA256SUMS.txt`覆盖本张文件并回读验证。复现顺序：技能目录执行`node scripts/run-build.mjs <本目录>/recipe.mjs --out-dir <本目录>/nbt --data-version 3465 --json`；本目录执行`python review.py`、`python cutaway.py`。技能preview.mjs分别以5444服务完整候选、5445服务裁切体，再运行`node capture.cjs`、`python compare.py`、`python finalize.py`。端口以实际服务为准，HTTP字节一致检查不能跳过。

完工停候独立复核，不推进08。
