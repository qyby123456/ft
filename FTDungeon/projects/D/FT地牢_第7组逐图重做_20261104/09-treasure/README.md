# S7R2-09 菌窟阶台秘藏

状态：AWAITING_INDEPENDENT_REGISTRATION_ACCEPTANCE。本次仅09，交付后停候复核，不开始10，不部署。

正式候选：incoming/S7R2-09/room.nbt，Java 1.20.1、DataVersion 3465、gzip，31×19×29，17081条唯一坐标。SHA256：d90d7a20d9d061cdd5575bacaf1c488cb0235639773ca45f144f1c4b124410b7。配方recipe.mjs依据正确S7-09-treasure.png独立新建，不复用前房几何。材料计数见incoming/S7R2-09/blocks.json。

主体为中央阶台单宝箱、左右高菌土巨菇、雕饰石柱与红黄旗、左储物桶和右盆栽陈设、前场双浅池。原图金色箱由原版木箱替代。正式源y18的899格顶完整封闭；开放切面仅预览派生。低前场脚高2、后场3、宝箱操作台5；半砖级差连续2→2.5→3及3→3.5→4→4.5→5，无跳跃路线声明。

三个3×4接口：南[15,2,28]、西[0,3,12]、东[30,3,12]。左右两口对应提示词two sides，南口对应图中前景阶口。本地方位为工程选择；外部连接、世界坐标未知。connections.json与rotated-connectors.json给出高程、法线、开口空气和底部坐标；31×29矩形每次四分之一转互换宽深，运行时世界旋转放置NOT_RUN。

原版注册/状态、模板读写通过；90处静态存活检查通过（官方标签绑定，亮度15假设）；24个目标通过0.6×1.8身体、半格网格、最大0.5级差的升高扫掠与反向扫掠，2357个可达节点。路线覆盖三接口、中轴各半格台阶、宝箱前及木桶前。高菇台只作结构支撑，不声称地面可达。

真实方块实体11个：1空单箱、6空木桶、4面图案旗；无自动LootTable。原生BlockEntity.loadStatic及saveWithFullMetadata核对ID、空物品、旗Patterns和箱CustomName。宝箱[15,5,5]向南，操作点[15,5,6]；上方完整一格空间无碰撞、空气且非红石导体，覆盖保守箱盖净空。7容器操作点均可达，眼点至朝南操作面距离小于4.5格，101点采样射线无阻挡。上述是离线静态几何与数据检查；真实开箱/木桶界面操作NOT_RUN，外部猫或实体UNKNOWN。

6张实际Lodestone截图已逐张查看：exterior、reverse-exterior、treasure-eye、reverse-eye、chest-detail直接展示正式源；interior仅展示PREVIEW_ONLY_cutaway.nbt。三内部相机均按脚高+1.62设置，并有原生身体可站立且可达记录。浏览器响应仅调整相机，未改技能文件或NBT。派生仅裁y18顶与x0西/z28南墙y≥2，逐坐标记录air改动和哈希，正式源不变。裁墙后西侧藤蔓可能悬空，正式源有壁支撑且canSurvive通过。旗图案在原生数据中完整，当前Lodestone只显示简化布面。软件投影图另有标签，不冒充Lodestone或游戏实测。

18源水为两个封边3×3浅池；原版静态水、方形轮廓、规律阶台和实心菌土台简化了概念图有机地形；没有青色发光菌、紫色动态光、孢子雾或shader光束。水流/邻居/随机刻、繁殖蔓延、骨粉、支撑破坏、世界光传播、真实交互、玩法、注册与部署全部NOT_RUN。

必须预留完整31×19×29包围盒，包括显式air，拒绝冲突后才可放置。永久边界：S7-06的L外接方盒含L外显式air，原版放置会清相应地形；需完整包围盒预留，或另行实现并验证跳过空象限的放置器。保留前08高沿无地面楼梯、上层地面可达未被认证的边界；不扩大前图接受范围。

交接入口：REGISTRATION_PENDING.json、manifest.json、qa/latest-native-run.json、原生run内container-access.json和human-eye-poses.json、qa/block-entities.json、qa/reference-binding.json、qa/visual-review.json。正确原图、提示词和08接受快照随包提供。SHA256SUMS.json覆盖全交接文件；ZIP做CRC及逐文件SHA256回读。旧隔离运行只作同源历史证据，不注册测试副本或预览派生。

执行Preview.ps1打开完整源预览服务，默认http://127.0.0.1:5385。生产方自检通过不等于独立接受。
