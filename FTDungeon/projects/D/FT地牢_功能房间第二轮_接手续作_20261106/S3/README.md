# S3 冰原霜穴 11—20：独立复核后整改候选

2026-11-07。当前新版本为制作方检查完成、等待独立复核。上版独立结论REQUEST_CHANGES accepted=false，只适用于上版冻结哈希；不继承为当前通过。

08:06运行时补验见 `RUNTIME-REVIEW.md` 和 `runtime-ready-for-review.json`，有效世界证据在 `worldqa-r3`，梯子逐刻物理证据在 `ladderqa-r2`。原候选哈希及原静态/视觉证据均未变：十房和合并真实放置、邻居更新、1200服务器刻、完整刻后NBT回读、81目标碰撞往返、6处锁的18次实际use、六门开态稳定及临时拉杆、原梯上下转换、管理员进出命令已补测通过。以下原“未验”描述为本次补验前记录，以新报告的准确覆盖范围为准；其他工作方块、客户端照明、生存与战斗仍未覆盖。`deployment` 是未安装的完整清空/分片放置及管理员主持接入包，整套install函数尚未整体执行。

有效目录：D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S3。旧S3只读。revisions/before-independent-changes保留整改前配方、NBT、预览和QA，旧ready清单位于revisions/previous-ready-for-review.json。

## 新改内容

- 19：35×23×61；原高台、桌、围护、六格高差与楼梯整体南移30格，前方扩展30格完整封闭冰洞，开厚石观景口，洞壁/顶/冻石层次形成纵深。原四目标同量平移，未删除。watch-detail/cavern-depth为实际完整NBT内景。
- 20：保留曲折密道，降低局部直隔墙，增加厚石凹室入口；vault-entry证明从凹室入口站位可直视空锁箱与金堆。没有虚构红石隐门。
- 15：加入可识别原木横杆、A架支脚和半格渐升斜坡，保留草人、靶与观众席。
- 全房：入口三格纵深厚石框、分层石檐与错落冰雪体块。12另增牢区石梁；13炉口石框；14武器架背衬；16井沿冻石角与分层冰壁；17炉位石壁龛；18骨龛边柱与出檐。11增加梯子站位近景。

## 交付及验证

rooms/s3-XX：完整Java1.20.1/DataVersion3465 NBT、单房设计与清单、真实Lodestone完整/剖切/近景、原生QA。roomXX.mjs与common.mjs为可重现配方。全部10房源已变化，全部重新构建、重验、重渲染并目检，旧预览结论没有继承。

rooms/s3-merged/s3-merged.nbt：207×35×217；新走廊避开源体积。parts/：25个各轴≤48分片；逐记录及BE载荷反拼与10源记录保持比对。完整哈希、记录数、边界检查数在ready-for-review.json与合并qa内。

十房40目标、合并41目标静态0.6×1.8人体路线全部保留并通过。原生注册/属性、模板读写、全方块canSurvive、声明接口、空容器无LootTable均重新检查。Java单进程、堆1536MB。风险清单为risk-audit.json。十房与合并配方重建字节比对证据为takeover-provenance.json和merged-reproduction.json。

## 复现和待审

运行reproduce.ps1重建并检查（仅当前目录），新源仍须重新render.cjs并由人实际看图后登记review.py。合并必须带--no-block-state-updates保留源属性。finalize.py校验当前哈希并生成ready-for-review.json。

TAKEOVER-REVIEW.md说明逐房修改和未验证项。12真实门控/邻居更新、所有容器/工作方块交互、世界放置、随机/流体tick、照明、生存与战斗未验；静态技术通过不是整组视觉或运行时通过。井水冻结、装备/望远镜为雕塑的差异保留。未写saves/mods/config/KubeJS、未付费生图。
