# S4-05 垂藤积水木框长廊

本张已制作、自检通过，等待独立验收。仅05获授权；06及以后未开始。实际世界放置、世界刻、玩家玩法、运行时拼接、整合与部署均NOT_RUN。未修改正式存档、mods、config。前四张交付由摘要回读核对，未改写。

## 参考与建筑

唯一参考为 `D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-05-corridor.png`。先核图后全新编写本张几何：有顶积水走廊、五组重复木框、两侧共十盏木托暖灯、苔石砖墙顶、墙藤与三格长垂藤、根脚、睡莲、两端高拱。未套用04开顶大厅或其他旧候选的结构；仅复用离线QA逻辑。

尺寸15×14×39，由画面比例估算，并非图片提供的精确尺寸；Java1.20.1，DataVersion3465，共5,628条显式方块记录、21个状态，无方块实体/实体。屋顶在全部15×39=585个列位均保留Y11..13三层实体，Y10有重复木横梁。建筑实体均六面连到基础，排除水、睡莲与藤蔓植物后只有一个建筑分量；十盏壁灯由上木托和木柱连接，不依赖孤立灯台。

301格裸水面占407格内底平面约74%；中央含水上半石砖台阶组成两格宽、每六格轻微错位的连续石径，顶面与水面齐平。它将参考中水下散石近似为无需游泳的路线，显式偏差已记录，未建满宽干地。垂藤用无发光浆果的洞穴藤蔓近似，固定末端age25不等于禁止世界随机生长。原版暖色蛙明灯加木托近似图中框灯，没有复刻光影水反射、雾、生物群系水色或外部接续场景。

04前置独立验收真值为 `D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-04-R1-independent/independent-details.json`，状态ACCEPTED_FOR_SEQUENTIAL_PRODUCTION，SHA a809502d03ce697502679a9134bb5920aa18bf6826c9b35926968da20cc871cc。根目录旧04-REVIEW.json仍记录R0缺陷，应以该R1独立目录为准。

## 候选与连接

- 唯一完整候选：`incoming/S4-05/room.nbt`，与`nbt/s4-05-flooded-timber-corridor.nbt`字节一致；元数据`incoming/S4-05/room.json`。
- 原点为NBT最小角；X东、Y上、Z南。北口脚底(7,4,0)，南口(7,4,38)，两口3宽×4高×3向内深支持净空，端部平台5格宽。无侧口，不挪用04四口要求。
- 中央石径顶面脚底Y4；元数据列出14个路线目标。北口对齐世界脚底A时，未旋转模板原点=A−(7,4,0)。尺寸均在默认48保存上限内，但本次不导入世界；未来运行时仍须全体积15×14×39占用拒绝、边界接口与旋转验证，不可仅测入口或非空气块。
- `acceptance.json`是制作方自检登记，独立验收PENDING；`SHA256SUMS.txt`是文件回读摘要。

## 实际NBT视图与裁切规则

- `preview-exterior.png`：完整候选外景，可见完整石质屋顶。
- `preview-interior-south.png`、`preview-interior-north.png`：相机分别置于完整候选两端内部；完整屋顶及墙体均保留，不是裁切图。
- `preview-interior-night.png`：完整候选的Lodestone夜光预设，不能当作世界光照/刷怪验证。
- `preview-cutaway.png`：明确标记PREVIEW ONLY的裁切视图，来自`preview-only/roof-west-cutaway.nbt`，不能部署。
- 唯一裁切公式：原坐标处，当 **Y≥11 或（X≤1 且Y≥4）**，把原非空气方块替换为空气；其余方块原样保留。保留15×14×39包围盒、水/基础层、木柱和Y10横梁。`cutaway-check.json`逐格断言只减为空气，无新增、换材或挪位。被裁掉支撑后的局部植物不代表完整候选中悬空，裁切体不参与生存验收。
- `capture.cjs`从本地预览HTTP取得NBT并与候选/衍生文件逐字节和SHA核对；仅注入相机控制句柄，无纹理、场景或结构替换。`preview-check.json`保存五张图各自来源摘要和相机参数。

## 检查与复现

`review.py`检查尺寸、屋顶、边界水体无直接底/侧空气边、睡莲水面支撑、实体禁用项和全建筑分量连接；原版注册表/结构NBT解析与StructureTemplate往返见`qa/native-review.json`。离线映射fixture只是剥离失效签名元数据的本地副本，类字节不变、源jar未改，见`qa/fixture-provenance.json`。

`qa/BodyRoutes.java`用原版碰撞盒检查0.6×1.8身体和0.5格步进，水平分段扫掠，避免身体进入水中，不跳跃/游泳；14目标及两端接口回归通过。`qa/VineStability.java`绑定170个原版标签，包含墙藤、洞穴垂藤、睡莲、含水石条/根和围栏的canSurvive与六向updateShape固定点检查；调度刻仅计数后抑制，实际流体/随机刻、完整传播调度与玩家玩法未运行。

`compare.py`仅做可得本地NBT样本去重与粗网格旋转筛查；结果见`repetition-check.json`。封闭小房间和封闭长廊的归一化实心占比可产生较高相似度，不能据此声明视觉原创已验收，也不自动判为重复；完整几何无完全重复，仍待独立对图。

复现顺序：技能目录运行`node scripts/run-build.mjs <本目录>/recipe.mjs --out-dir <本目录>/nbt --data-version 3465 --json`；本目录用已配置Python依次运行`review.py`、`cutaway.py`。以技能`preview.mjs`分别用5419端口服务完整候选、5420端口服务裁切体，再运行`node capture.cjs`、`python compare.py`、`python finalize.py`。自检通过不表示世界或部署验收。
