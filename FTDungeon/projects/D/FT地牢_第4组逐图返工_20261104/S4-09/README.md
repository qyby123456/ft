# S4-09 双卫湿地藏宝高台

状态：SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE。仅本张候选，待独立复核；不启动S4-10。用户已确认S4-08独立验收通过，前张NBT SHA256为e0d77c77057d8587096cd1e36b3ccff7a5b8cb01e33eef977ba24a928b116223；其independent-details.json中的机器检查状态为PASS，不将该字段冒写为验收状态。

## 原图与交付

- 原图：D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-09-treasure.png。
- 完整候选：incoming/S4-09/room.nbt；Java1.20.1，DataVersion3465，31×22×31，8263条显式方块记录（包括236个空气记录），30个方块状态，7个方块实体，无普通实体。
- 身份/朝向/路线/偏差：incoming/S4-09/room.json。最终SHA见acceptance.json及SHA256SUMS.txt。
- 本张recipe.mjs从原图新建，不用旧结构作几何输入。复用的是NBT解析、原版注册表/碰撞QA、浏览器截图等验证脚手架。

## 图像特征与实现

| 原图要素 | 本张实现与坐标 | 有意偏差 |
| --- | --- | --- |
| 后部中央宝箱高台 | 单箱(15,9,7)，下平台x11..19/z4..12，上平台x12..18/z5..9，三排5宽北向台阶z12/11/10、y6/7/8 | 尺寸按画面比例估计，不是测绘；箱内未配置奖励 |
| 高台左右石守卫 | 中轴x9/21、z7，双层底座、细墙体躯干、侧臂、y12錾制头、朝南按钮脸 | 原版方块雕塑近似，不放生物、盔甲架或刷怪笼 |
| 两侧拱口 | 西(0,6,12)、东(30,6,12)，拱内3×4净空与3格向内深度 | 不假设图外已有相连房间 |
| 前池、木栈桥、上厅台阶 | 池x3..27/z18..27，156水格；木桥x13..17/z17..30；两排北向台阶z18/y4、z17/y5 | 不是整屋多数淹水；无08庭院树岛、二层廊或瀑布 |
| 左罐架、右桌与桶 | 左侧3层木架6花盆，右桌2花盆；4桶均空 | 花盆代替大型陶罐，木桌未加原图布纹，无自定义材质 |
| 后墙双旗、暖灯、苔藤 | 两面棕底黄边黄菱形旗实体；9蛙明灯；84贴墙藤、8洞穴藤、8常驻叶、14睡莲 | Lodestone默认包可能不显示旗纹；常驻叶不执行自然消失，非树冠 |
| 阶梯残墙、敞顶剪影 | 后墙及两侧阶梯封边完整保留，前池250列在y10..21无实心屋盖 | 原图开顶遗迹，无凭空补全封闭顶；省略外部沼泽森林、水雾、光影和水反射 |

## 容器与可达性

宝箱是minecraft:chest，facing=south、type=single、waterlogged=false。Items=[]且无LootTable；4桶同样Items=[]且无LootTable。**藏宝室是场景类型，不表示已装有宝藏；本交付没有战利品生成配置。**

箱前站位(15,9,8)，箱盖保守净空x14..16/y10..11/z6..8共18格全空气。原生方块实体加载成功，但真实世界右键开箱、箱盖动画和奖励生成均NOT_RUN。

南入口(15,4,30)，西/东接口足部y6；全部接口检查3宽×4高×3向内、脚下支撑。原版0.6×1.8玩家AABB、半格网格、最大半格步进、无跳跃/游泳检查23处目标，包括木桥、两段上厅阶梯、三段高台阶梯、箱前与箱两侧、左右拱口及陈设前方。qa/body-routes.json给出1904个可达网格节点；这不是实际玩家移动测试。

## 原生与静态证据

- review.py → geometry-check.json/materials.json：单箱状态、净空、守卫、25台阶、8花盆、常驻叶、池边/池底封闭、建筑实心部分六邻接单组件。
- native.py → qa/native-review.json：原版1.20.1 NBT解析、注册表状态与7方块实体、StructureTemplate往返、接口净空。原Verifier通用整格路线字段为0且未执行；有效路线证据是独立BodyRoutes，不把0冒充路线通过。
- qa/VineStability.java：绑定170项原版标签，对144处选定脆弱/邻接状态进行canSurvive/六向updateShape，一轮固定点、状态变化0、失败0；14睡莲和8盆有效。120次流体刻请求被拦截计数，**没有实际执行流体刻**。
- compare.py/repetition-check.json：本地可获得自然组结构精确占位重复筛查；粗采样旋转相似度只作线索，不代表视觉原创性验收。
- QA用映射jar仅复制去签名到本张qa，本源不改，来源hash见qa/fixture-provenance.json。

## 原NBT截图与裁切追溯

capture.cjs通过Edge WebGL/Lodestone渲染。逐次校验HTTP提供的NBT字节与磁盘文件一致；仅注入相机引用，不替换材质、几何或图片。preview-check.json记录每张相机与源NBT SHA。

- preview-exterior.png、preview-reverse.png：完整原NBT正面/反面。
- preview-eye.png、preview-chest.png：完整原NBT厅内/箱前净空近景。
- preview-wardens.png、preview-pottery.png：完整原NBT双守卫/左陶罐架细节。
- preview-night.png：完整原NBT夜间灯光预览，不代表实机光照效果。
- preview-cutaway.png：仅展示辅助裁切，不是交付替代版本。

cutaway.py从候选原坐标读取，仅把y>=14的945个非空气方块替换为空气，不改包围盒、不添方块、不改剩余材质/朝向。同步删除被裁掉的2面旗的方块实体，剩余5个容器实体负载逐一保持原值；精确坐标与删除负载见preview-only/removal-trace.json。保留箱、守卫头、全部台阶及y13木架；y14最上层花盆随规则被裁掉。裁切包围盒31×22×31。cutaway-check.json记录原/派生hash和逐块断言，派生NBT禁止部署。

## 复现与边界

在minecraft-builder-skill目录运行：node scripts/run-build.mjs 本张/recipe.mjs --out-dir 本张/nbt --data-version 3465 --json。随后用已配置Python依次执行review.py、cutaway.py、compare.py。为完整/裁切NBT启动Lodestone预览5494/5495端口，再node capture.cjs，最后python finalize.py。脚本保留明确本机QA依赖绝对路径，并非可移植发行工具。

SHA256SUMS.txt含本张全部交付/复现文件，排除自身和Python缓存。finalize.py同时逐条验证S4-01至08已验收清单，保持这些目录不变。不修改正式saves/mods/config或部署目录。

实际世界放置、随机刻/水传播/植物生长、交互玩法、跨模块整合和部署均NOT_RUN。独立图像及结构验收PENDING；S4-10未开始、不得预生成。不得把本张自检或本说明当成独立验收或全组完成。
