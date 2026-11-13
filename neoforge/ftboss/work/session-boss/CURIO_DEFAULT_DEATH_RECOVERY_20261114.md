# Curios 默认死亡增量验收与恢复

V13 仅验证默认 keepInventory=false 的死亡生命周期：有效原饰品在沉默期间遭受真实致命伤害，原处理器在默认掉落前恢复物品；Curios 生成一份真实 ItemEntity，复活玩家没有复制物品，自然拾取后客户端库存同步，再稳定观察 120 个自然 tick。已有 V6 保留背包死亡、V12 跨维度、四类回退和十八项控制均直接引用，不重跑。

候选 JAR 不变，NOT-INSTALLABLE，未部署。正式实例只读，武器和 Session 暂停。新 JVM 重登仍需用户真实点击实验性页面，目前没有等待窗口。完整状态见 checkpoint-curios-lifecycle-matrix-20261114.json。

本段归档、SHA256、基础包依赖和远端源码恢复证明由 checkpoint-curios-default-death-20261114.json 及对应发布回执记录。增量包必须与基础包 boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip 一起保留；使用 restore_boss_zicai_curio_archive_v2.py <增量包> --base <基础包> 校验，添加 --output <全新目录> 可恢复。所有载荷哈希均校验，未宣称整包物理恢复或大型产物远端恢复。仅清理发布计划中、独立远端核验且无进程/句柄引用的重复源码副本；规范输入、世界、原始证据和失败记录保留。
