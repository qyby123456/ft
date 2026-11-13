# DSH 地牢交接续接账本

负责人聊天：`01a19c8f-6747-7572-a77a-6e90084bbd34`。旧三聊天仅作历史证据，禁止发消息或等待恢复。

本目录只进行候选收件、槽位预留、四视图、独立复验和接入协调。**不是部署目录，也不授予覆盖正式 JAR、源码或玩家存档的权限。**

## 新增走道批次（2026-11-03）

`corridors-20261103/latest.json` 指向 40 条新走道的独立盘点，约束见该目录 `CONTRACT.md`，线程映射见 `thread-map.json`。使用 `tools/intake_corridors.py` 重扫已有交付，不重复派发、不自动部署。

走道使用 `corridor-M01`–`corridor-M20`、`corridor-S01`–`corridor-S20`，仅在接收账本预留 41–80；旧房间 0–40 及旧 `latest.json` 不变。`corridor-M07` 与 `M07`/`M7` 不同。两张设计总览不占槽位。

新批的 `received` 只统计完整四件套已冻结的实际成品，不把 40 张设计图当作 40 个 NBT。制作线程的 PASS 不能自动代替接收者四视图审阅和隔离原生复验；全部通过后才可标记可交核心接入。

## 入口与证据

- `latest.json` 指向最近一次完整盘点的 `runs/时间戳/REPORT.md`、`registry.json` 和五类数量。
- `runs/时间戳/queue.json` 是待验收清单。历史 `queue/batch3.json` 原样保留，其未派发状态已经滞后；六间制作交付都已找到，禁止重复派发。
- `incoming/ID/完整NBT-SHA/` 是不可覆盖的冻结副本；全部哈希、原图指纹、源文件和 QA 类型保存在每次收件记录中。
- 最新美化九间实际没有房间内 `qa.json`，收件明确携带原样 aggregate QA，不伪造制作线程的单房 QA；相应生成组、主汇总原生报告和变更均有哈希来源。
- `reviews/ID/完整SHA/four-views/` 存最终 NBT 的北南西东内部剖视及完整外壳，`manifest.json` 绑定图片哈希和输入。
- `reviews/ID/完整SHA/native/时间戳/` 存复制后的房间专属静态/Forge 夹具、全新临时世界、命令、结果和日志；不复用制作线程世界、不覆盖制作报告。
- `reviews/ID/完整SHA/acceptance.json` 才是本接收者视觉与复验的结论。现已完成 M07、R4；两间保持原 NBT SHA，均重新四向放置并观察 240 tick。

## ID 与槽位

旧九间维持已装目录槽位 0–8，最新美化仅是这些 ID 的替换候选，不另建 ID，也不能强刷旧世界。

新 32 间在本账本预留 9–40：第三批 9–14、补给店 15、M07–M12 16–21、R1–R6 22–27、楼梯 A–D 28–31、商店 A–I 32–40。**预留不是运行时接入，不产生施工和持久标记。** M7 与 M07 字符串、哈希、槽位始终不同。楼梯 A/B 两个同哈希交付路径只收一次。

正式已装为 0.20.0，SHA-256 `9bee4bb411ef404ef34163375115e5b5f5dc21512019e9e398d76c0c5914920f`，目录仍是原九间。历史 0.17.0 收据不可解释为新房已部署。

## 当前门禁

41 间已登记；39 待接收者四视图，39 待接收者复验；2 间可进入统一接入阶段（M07、R4）；14 间明确有接入阻断。这些轴可重叠，不是互斥阶段。

10 间商店尚需共享核心禁刷策略接收与回归；补给店还依赖 FT 交易桌 1.2.0，当前原版方块限定不接受该方块。4 间楼梯需上/下层接口连接和路径回归。所有候选仍要在接入阶段验证实际大厅路线、整卷占用拒绝、独立持久指纹与旧世界不覆盖。

原实例 `output/dungeon-interiors-20261101-v2` 是另一套参考地图：12 个命名审查模板、16 房暂存地图，另存 `isolated-v2.json`；不与 image25 ID/槽位混用。历史 40 tick 结果不冒充本轮复验；最终世界交付验收未闭环。

## 可复现命令

Python：`C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe`。

在 `tools/` 执行 `python -B -X utf8 intake.py` 会新建时间戳盘点，拒绝改写变化后的同哈希副本和有歧义的多个活跃交付。验证既有清单：`python -B -X utf8 intake.py --verify 绝对registry.json路径`。

`recheck_priority.py native M07`、`recheck_priority.py native R4` 复制只读源夹具到本目录运行，不改原工程。`render` 子命令只生成副本图，不改 NBT；同一渲染目录存在时拒绝覆盖。`record_priority_acceptance.py` 中的视觉结论只对应本次实际打开检查的两间，**不是新候选或其他房间的自动视觉验收器**。

软件预览不是实机截图；无界面 Forge 验证不是整合包客户端、多人或 FPS 证据。共享核心写入或部署前，必须先报告候选 JAR 哈希、范围、依赖和接收条件，另行确认。
