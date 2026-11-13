# 最终普通安装 JAR 的独立故障测试

执行身份：core-final-runtime-faults-not-runtime-maker，独立于 runtime 制作方。精确 JAR：`bab12080dae0e8fb2290f73b6988f40af2094d932a3303cdabc12aaac250263f`。服务器均为普通 Forge 1.20.1-47.4.10 + 此唯一候选 mod，无 GameTest 测试 mod。所有启动、写入、复制世界、终止动作仅在本目录；最多同时运行一个本执行者拥有的 server。

input-lock.json 对最终 runtime 78 文件封存、只读借用脚本、Forge 库、源世界与复制世界逐文件 SHA256。输入世界来自制作方的已停止牺牲世界，仅作为真实原生状态基线；本执行者独立启动新 JVM、注入故障、发实际命令、强杀与离线读回，不继承制作方的故障 PASS。原 source world 与 runtime 封存会在结束时再次校验。

测试语义沿用只读 integration/v5-diagnostics/faults.py，并适配最终 FTD5；最终 FTD5 读回参考脚本亦锁定。每个进程记录 pid、parentPid、Java 命令、cwd 和日志哈希。真实强杀前通过 CIM 核对 PID、父 PID、Java 路径、唯一 world 参数、libraries 参数，再检查 creation ticks 和命令行不变，仅终止匹配的自有 Java PID，不终止进程树或其他 live Java。

六个故障用例与实际结果见 result.json，未完成或失败不得按 PASS 交付：

1. 截断 journal：FTD5 截为 32 字节，实际 build 命令拒绝 Truncated journal，原模板方块/BE 逐格比较不变。
2. 校验和损坏：翻转 journal 最后一位，实际 build 拒绝 checksum mismatch，原模板方块/BE 逐格比较不变。
3. Foreign solid：新世界实际 plan 后，在包络内放 diamond_block，再 build。要求 cursor/owned=0、FAILED、active=false、tickets=0；整个包络逐格读回只允许该 foreign block 与原空气，无生成器写入。
4. Unknown inventory/BE：真实原件 chest 原生插入 7 emerald，save-all flush 后核验自有 PID 强杀。新 JVM 显式 build 必须隔离；原生已保存 chest 的未知 Items 必须原样保留。此合成未知库存是负向冲突夹具，不宣称来源原 NBT 自带丰富库存。
5. 部分构建强杀：新世界实际规划，BUILDING 且 durable checkpoint 后强杀。FTD5 cursor 必须等于 completedThrough；新 JVM 初始 idle/tickets=0，显式 build 后完成自然 tick 观察与 native 读回，最后 pending、active=false、tickets=0。离线检查原生 BE、流体/火逸出及围护壳；原始状态差异全部保存，不暗中忽略。不是掉电测试。
6. 保护事件：副本的房间以下包络保护空气内由 operator 命令放置 piston/redstone，触发真实 Forge piston 事件。要求取消伸展、隔离、撤销 receipt、改变 challenge、后续 build 拒绝，原模板块/BE 不变。该用例不声称玩家真实网络放置事件已测。

所有合法 COMMITTED 操作均未执行；最终 receipt 齐备后的合法提交由 integration 完成。本证据不宣称整套路线、玩家 UI、生产晋升或掉电持久性验收。

最终观察：六项均通过。本执行者所有 JVM 已退出。恢复用例保存的 BUILDING cursor/completedThrough 均为 28672，ownedThrough=29696；重启初始 active=false/tickets=0，显式恢复到 879648 操作全部完成、自然观察 2400 ticks、pending 且 active=false/tickets=0。离线 BE 字段、逸出流体/火、围护壳差异均为零。原状态差异 76 处完整保留：68 处 deepslate_brick_wall 仅 up=true→false；8 处 s8_08 原 air 变 vine；均落在原运行时允许的墙形状/原生藤蔓演化语义，实际 native semantic readback 及自然观察已通过。不宣称原状态逐位完全一致。

真实活塞事件结果为 ROLLBACK_REQUIRED、active=false/tickets=0、blocked piston；活塞未伸展，保护目标空气未变，challenge 变化且 receipt 清空，127239 个模板源位置的 state/BE 无变化。它验证人为 operator fixture 触发的实际 Forge 活塞事件及隔离，不替代玩家网络交互测试。
