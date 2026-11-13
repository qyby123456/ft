# S7 v7 自动钩子补证（原v6安装包不变）

只补自动load/tick调度缺口，不修改v6冻结828文件、建筑、交互和自然运动课程。制作方补证通过，整组accepted=false，等待独立复核。

原v6 ready SHA256：c33269f92a4b4b697ec94aa4e34cbe62db77ae25771fa53f683d0c9e3272fdc4
原安装ZIP SHA256：426e58f55609494e6288ab8df1d7f8d103933d5cb4a605bc5145df1bc4acc168
合并NBT SHA256：906e63eea067ab01c4f0df2619102dd17a446b7c6858c732f4f03a6f3582de22

原ZIP已含data/minecraft/tags/functions/load.json与tick.json；上轮worldqa-proof测试模组没有安装这些标签，而是人工调用函数。因此此前证据仅支持函数机理和安装，本轮修复证据，不修改安装包版本。旧新ZIP差异为空，15个zip模板的压缩NBT哈希逐一等于冻结合并分片。

本轮直接将原ZIP按原字节放入全新世界world/datapacks，再启动Forge；测试模组资源只含空模板、mods.toml和pack.mcmeta，不包含fts7函数或minecraft函数标签。运行时minecraft:load与minecraft:tick标签实际解析为fts7:load与fts7:tick，且测试进入前两个trigger目标已自动创建。

测试仅管理员显式执行function fts7:install。随后在不同真实world tick等待entry自然解锁、用权限0 trigger提交，下一自然tick进入；等待return自然解锁，权限0 trigger提交，下一自然tick返回；在平台自然取得的旧entry许可带到远处提交，自动tick拒绝传送并清零锁定；远处再试未启用trigger也拒绝。全过程没有显式load/tick调用、直接函数管理器execute或人工scoreboard enable。源码、命令账本和逐刻轨迹保留。

有效运行：C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603\s7-maker-hooks-isolated\automatic-hooks-20261107-v7-run2
实际世界：C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603\s7-maker-hooks-isolated\automatic-hooks-20261107-v7-run2\server\world
有效原始报告：C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603\s7-maker-hooks-isolated\automatic-hooks-20261107-v7-run2\automatic-hooks-report.json

首轮失败保留：C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603\s7-maker-hooks-isolated\automatic-hooks-20261107-v7。该轮漏带测试模组pack.mcmeta，服务器在GameTest空模板查找阶段失败，无自动钩子通过结论。第二轮在全新目录补齐测试模组元数据后运行，原安装ZIP未改。

范围：服务器GameTest作者模拟。使用FakePlayer注册到服务器玩家集合，并为命令传送使用原版ServerGamePacketListenerImpl和Netty EmbeddedChannel；权限0是实际CommandSourceStack权限0与原生trigger命令派发。未声称真实TCP客户端输入、收包或确认；但load/tick函数完全由服务器自然调度，不由测试手工调用。只限既有零旋转安装方案。

D盘仅保存小型报告、索引与哈希清单；世界、构建、日志和原包都在指定C盘独占子目录。run.ps1遇已初始化世界拒绝覆盖。复跑应复制最小测试源码、mod元数据、empty模板、构建设置、server.properties和原ZIP到新的独占目录后运行。

本轮没有关闭独立验收项，也未部署正式实例。
