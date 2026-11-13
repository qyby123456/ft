# S5-17 两桶针对性修复

两桶已由STORAGE-FIX-REVIEW.md独立关闭。后续同哈希整体连续运动补证见NATURAL-MOVEMENT.md；本次不再修改两桶及任何结构。

本次仅修复上一轮未关闭的两个高架桶。扩大搜索先于修改：`qa/upper-barrels-halfgrid` 对两个旧目标共14994个半格站位做原生可达检查，筛出16/25个候选；`qa/upper-barrels-native` 对可达候选和桶体每轴9点共729点加中心做实际首命中/4.5格/use检查，旧两桶仍失败，exit1及全部失败记录保留。未删桶、未退役目标，也未把桶改称装饰。

## 最小结构变化

参考原图的工作台墙储物布局，两桶由源17 `[6,8,1]`、`[9,8,1]` 降至 `[6,5,1]`、`[9,5,1]`，各在y4增加一块深色橡木托架；原上方木架、两根立柱、砂轮、工作台、中央模型和外壳不改。旋转后新合并桶位 `[159,11,81]`、`[156,11,81]`。可从普通地面操作，保留原有24箱桶及13拉杆，总37目标。

逐记录比较源17与合并各恰好6格变化：两旧桶变空气、两新位置加桶（含原空Items载荷）、两木托架。其余九源NBT哈希不变，19景深保留。只有第08分片哈希变化，其余九分片不变。`qa/room17-storage-fix/change-and-support-report.json` 给出所有位置、状态、载荷和九源继承记录；原版支撑对新17及合并重新通过。封口、57482格屏障、三格门喉及40格玻璃例外不变。

当前源17 SHA-256：`5696c74344775e0d95401f3f26bea009df37cbfa62c6e716b0f6e0d0aab77844`。

当前合并 SHA-256：`e0c71315c81727e81cd15011eda9a0f5da8624387c59b8b9764c42d8d740397e`。

## 绑定新哈希的证据

- `qa/room17-storage-verified/reach-results.json`：真实隔离Forge1.20.1，全部37项通过（13拉杆拨动复位、24箱桶菜单/use），干燥支持站位、0.6×1.8净空、实际眼点、原生OUTLINE首命中与眼距逐项记录。两桶脚点为 `[156.5,9,78.5]`、`[159.5,9,78.5]`，眼距均2.605336402837406格，小于4.5。
- 同目录 `combined-summary.json`、`independent-routes.json`：从内部入口和终点分别到37站位，74目标全部可达/净空。制作方调用只读原生工具，不冒称审查方复跑或真人行走。
- 同目录 `natural-results.json`、`tests.xml`、`exit.json`：只对新17与合并放置并运行1200自然刻，randomTickSpeed=3，exit0。`full-state-payload-comparison.json` 对placed和after1200完整状态/方块实体载荷逐位置比较，均零漂移；候选到placed的连接属性修正另行完整列出。初轮误以候选状态作放置基线，把原版围栏/铁栏邻接修正判失败，记录保留于 `qa/room17-storage-fix`；修正测试基线后新隔离世界复跑，没有删除差异。
- `post-tick-routes/bidirectional-coverage.json`：新刻后合并双向各886点，仍仅预期两实体门框 `[166,9,21]` / `[166,9,25]` 失败，其余884点通过，保留原三格门喉契约。
- `merged/parts-reconstruction.json`、`source-integrity.json`：10分片591542记录与27方块实体载荷完整重建；源逆旋转一致（原40玻璃例外），屏障57482。
- `qa/reproduction-storage-fix-report.json`：新源17与合并由复制配方在新输出目录生成，两NBT字节一致；九个未变源使用上轮独立输出，不重跑或宣称重跑。
- `S5-17/grindstone-project.png`、`qa/render.json` 与合并新预览绑定新哈希，实际目检中层两桶可见、上木架和中央工作台保留。

## 接入方案继承

接入命令、load/tick tags、密封大厅、入口/终点坐标均未改；沿用已关闭的 `qa/access-native-verified` 实测，不因本次重复世界接入测试，也不把旧实测哈希写成新哈希。`access-package/s5access.zip` 中只替换第08结构和包描述，manifest绑定当前合并；`package-verification.json` 校验命令/标签/大厅与已测资源逐字节相同，其余分片逐字节相同。新第08分片由当前完整反拼校验覆盖。未部署正式实例。

旧结构、预览、清单、接入包备份在 `qa/before-upper-barrel-fix`，旧失败和审查结论不抹除。最新 `ready-for-review.json` / `delivery-manifest.json` 作为本次交付索引；本次是制作方修复/自检完成，仍待独立定向复核，不宣称真人客户端、生存氧气或最终放行。
