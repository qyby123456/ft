# S8-07 v5 派生入口与完整九房隔离实验包

**侧双门不能封闭中央主通道。** 原相邻双门完整移为左侧可交互门道，中央入口改为三宽三高拱洞。此交付是**八个原字节房间加一个派生建筑候选**，不能称九原房字节未变或原建筑原样通过。当前所有合并包模板 accepted=false；协调线程已许可此设计继续作隔离实验候选，并未提升独立验收资格。

## 可直接核验的原件与派生件

原源路径：`D:\ft工程文件\FT地牢_Image25接入_20261101\deliveries\S8-rebuild-single-20261104-07\incoming\S8R_IMG07\room.nbt`

- 原SHA256：ad71c099fe5f87345f34d659655075a4ca0025952119b96247f88ff527a76bd7。
- 实际派生SHA256：bd7d0661b818164a3617629096580c646d17d95832269e6aca989933dc815ae3。
- 新模板ID：s8_07_widearch_side_doublegate_v5，resourceId=biomedungeon:s8_07_widearch_side_doublegate_v5。
- 单房候选：derived-candidate/data/biomedungeon/structures/s8_07_widearch_side_doublegate_v5.nbt。
- 九房完整包：nine-room-package-v5/data/biomedungeon/manifest.json 与同级structures；根candidate-manifest.json为相同审阅副本。所有9份模板实际存在，各自SHA见source-map.json。

## 精确20格改动与保留

derived-candidate/change-ledger.json及九房包同名账本列出每格before/after，绑定原SHA和实际派生SHA。

1. 中央X11—13、Y4—6、Z4—5改为三宽三高空气拱洞。原本已是空气的格不算改动，实际改10格（含移走的原双门4半部）。
2. 原双门四半部从X11/12平移到X8/9，Z4、Y4/5保持，north朝向、left/right铰链、open=true与powered=false全部保留；实际改4格。
3. 侧门后X8/9、Z5、Y4—6清出回接通路；实际改6格。

全部12格原楼梯、所有Y0—3格、原高差、6份BE及完整原家具/载荷保持；双门数量和相邻配对保留，位置发生改变。门闭合时应阻断侧路，不能阻断中央拱洞。闭门/重开/返程仍需原生测试，离线断言不冒称交互已通过。

## 失败复核

原北边界width3真实存在，但内部原Z4两格双门的打开门叶仍有碰撞，实际净宽1.625；右侧X13实墙还缩窄原整带。0.6身体中心只能落于约X[11.4875,12.5125]，当前要求为X[11.3,13.7]。独立区间计算逐个复现原四朝向各28条、合计112条失败横坐标。新catalog没有运行Minecraft；原生AABB来自integration冻结证据，并与原源状态核对。

边界width不天然等于任意内部横断面的同宽，但当前已经明确采用三宽内向直行/返程和三高验收，本交付保留这些要求。外部绕路不能改变固定原局部采样点的碰撞，不能以删目标或改窄扫描来通过。

## 全量目标及原生测试消费

nine-room-package-v5/all-original-target-mapping.json提供36案例、每旋转148个原目标（四转592项）的完整原局部到候选局部再到世界坐标映射。原目标、重复标签、顺序和分数坐标全部保留；07仍有每转16原目标。rotated-route-inputs.json按原点旋转再平移，不暗移脚点或按AABB最小值正规化。九房包path-check-matrix.json完整保留2200原检查，增48项侧门往返；每转另有4个sidegate目标，共16个新增目标，不替换原目标。

native-test-contract.json明确中央原整宽/三高扫描不变、侧门双半部开合/回接目标，以及新ID消费要求。原01楼梯、08熔岩/藤蔓仍需在新精确产物中原生复核。不能把新07资源写回旧s8_07 ID/hash，也不能只修改manifest的accepted来绕过正式加载器。协调线程可令runtime/integration在授权的隔离测试入口消费accepted=false候选，重新绑定core/runtime/JAR/manifest哈希；本目录没有向其他线程发消息或写入其文件。

完整包角色：派生07为entrance，原04为mainHall的normal角色（source-map保留真实源ID），原09为terminal，其他normal。九房包需真实规划器确保真实角色与全图回入口路径，不拿两廊道代替。

## 状态与证据

geometry-review.json、target-audit.json记录原112失败和36/2200原目标完整性。verification.json记录实际派生NBT回读/20格差异/12楼梯/6BE/Y0—3全保持。tests.log与nine-package-tests.log分别记录离线审查与完整九房/目标映射测试；并不构成新世界路线、开门、视觉或建筑独立验收。ready.json与SHA256SUMS.json封存实际资源、账本、全量目标、manifest及消费约定。

早期包含未生成/空派生哈希描述的提案阶段说明完整归档在history/README-before-seal.md，其旧ready和清单也归档保留；它们是历史记录，非现行状态。现行派生SHA与实际文件一致，accepted继续false。旧catalog基线、v2/v3/v4、原房源和正式实例未修改。

复现顺序：既有Python解释器以-B运行build_candidate.py、test_review.py、package_nine.py、test_nine.py，最后finalize.py。review.py只用于重新分析原阻断，会重建早期提案状态，若重跑必须随后build_candidate.py恢复实际派生状态并重新封存。没有调用生图API。
