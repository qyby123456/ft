# S7 蘑菇真菌 11—20 接手续作交付候选

状态：制作方检查与合并完成，等待当前最终哈希的独立复核；group_accepted=false。旧根只读，不写正式saves/mods/config/KubeJS。

|房间|最终候选 SHA-256|全非空气支撑格|原指定目标|
|---|---|---:|---:|
|S7-11|0f22ddfdbd072596384bc8576ced98b706240c6f8a9c8b1583d070394fc72b89|10111|7|
|S7-12|4e61eb1af2eb6134e6745ff8469b3de9008b6a9a4919a2a7229a06de50a6a4d0|10547|9|
|S7-13|275433233782748c5898e34e7c11131df97b29694e087372ba794811cc38e4b9|10845|7|
|S7-14|0ef237823e51c9c7ba626ec4edf68d5d3dad66df9db68814f3757dcc28698ffa|8921|7|
|S7-15|83a73f077132ec235ef5d3e842a582028f6a086b88c697cff7d54a937c1dd9dd|11247|6|
|S7-16|78c1d968be212b027619939badcf1dc248414a694a90c85c5d7f2839bed27518|10897|6|
|S7-17|bf7a18c21933c01e0834e088860c22aa4070e5b2425e7683e54e72baee5ccd91|8070|6|
|S7-18|fe1bb4b101b0d14781a59138631b03fe4da1324d2ca63c0fd51f93bfa8d50948|10641|7|
|S7-19|7740bc68e9ecc5c9d8e2f98cef577d962eb8b6f9c4ea477a0a355169d30f51bf|17768|6|
|S7-20|6a7f9c3cb249522fb940e73f870bb7702b9ea5a6f5b8bdf23fb4a4826fe7669e|11110|7|

合并：`assembly/s7-fungal-functions-connected.nbt`，SHA-256 `0d1d19064a170e607c47c3278200eaf7a937fadbc65b73ee2e06ea20524829d5`，尺寸 [326, 40, 151]，15 片，每轴≤48。反拼 185825 条方块记录、46 方块实体、3 具装备实体及坐标/完整载荷逐项相同。

合并原95目标及所有连接步点的三格宽截面，共 1184 目标均通过0.6×1.8原生碰撞检查；176370格全非空气支撑无失败。外侧屏障 58243 格位于房间和走廊联合包络外，不进入室内；两端外出口有屏障封口，此为离线候选，不是世界部署入口。

隔离原生1.20.1行为测试：Forge 47.4.10仅作GameTest载体，十房各1200刻，randomTickSpeed=3、doFireTick=true，显式邻居/流体调度；无支撑失败、掉落物、火和新流体格。六牢门API往返开关，14号铁/金/铁三具装备实体及铁剑实际保留。所有状态变化及逐点前后值保留在worldqa/world-report.json；蘑菇贴面、铁栏/石墙连接发生原生属性变化，方块ID未损失。此为制作方测试，不是独立验收。

复现：node <skill>/scripts/run-build.mjs <room>/recipe.mjs --out-dir <room> --json，然后运行工单Python -X utf8 revise.py <编号>，最后render.cjs及qa.py。reproduce.py将十房生成到reproduction，八房文件哈希完全相同，11/12字节哈希不同但逐格状态、尺寸、DataVersion、方块实体和实体载荷完全相同；两个哈希均保留，不冒称字节一致。assemble.py从最终十房重建合并；routecoverage.py覆盖走廊每个步点和三格宽截面。

真实Lodestone图：每房full/cutaway/interior与feature-detail；17另有project-detail，20另有vault-detail。assembly含完整、剖切、室内和plan-cutaway。剖切仅显示副本，源NBT保留壳体。

逐房复核回应见review-response.json，17/18/19/20新候选的作者目检不等于独立视觉通过。旧候选和失败证据位置见takeover-source-manifest.json及旧根history。

未验证项：
- Current final hashes require fresh independent static/visual/merge review
- Ladder climb and upper archive access using real player inputs
- Full player interactions, chest GUI and furnace operation
- Merged assembly placement and neighbor/random/fluid tick simulation
- Combat, survival completion and production-world deployment
- Long-duration and multiple-rotation updates beyond single-orientation 1200-tick individual test

## 补充证据版本

最新清单：D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S7\submissions\evidence-supplement-20261107-035739\ready-for-review.json

本次不改变十房NBT、合并NBT或15个分片；合并SHA-256仍为 0d1d19064a170e607c47c3278200eaf7a937fadbc65b73ee2e06ea20524829d5。

上一清单声明的渲染脚本哈希与补近景后脚本不同，旧清单原字节保存在prior-ready-for-review.json。changes_from_prior_manifest逐项记录旧声明与当前捕获哈希，不将其伪称一致。独立验收方已经冻结的03:46证据不修改。

新增12 guard-detail、13 prep-detail、15 dummy-detail、16 channel-detail，均为原候选的真实Lodestone渲染，已实际目检；无新NBT候选。该版本目录中的证据为本次捕获副本，后续整改必须另建版本目录。

20号拉杆旧缺陷已由独立复核关闭；14号三实体和装备证据保留。独立报告原路径与哈希见independent-evidence-update.json。

范围纠正：totalIssues不统计state_changes，火/流体只遍历源显式记录；独立前后NBT检查补足的是结构内部方块类型/遗漏格，没有外部传播证明。setOpen是API往返，非玩家输入/拉杆操作；世界测试未被父线程独立重跑。

仍待：独立最终视觉与合并验收、11上层玩家攀梯、16侧阶/小菌群整改、真实玩家开箱与完整往返、合并结构动态更新。group_accepted=false。
