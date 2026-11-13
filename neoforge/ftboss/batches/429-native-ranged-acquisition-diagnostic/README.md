# 429 原生远程计时器获取身份诊断

累计产品仍为427，无产品源码修改。真实NeoForge 21.1.233 / Minecraft 1.21.1测试专服运行于本批次隔离目录，进程19724正常退出、保存完成；运行工具的终端退出码和探针退出码均为0。

两个自然箭矢命中样本在命中前都没有FTBossRangedProcTimers复合键。只读反射诊断显示预规划Book.rangedRoot与真实玩家persistent data对象相同，而Book.rangedBuffer为null；原始saveHitTimers随后确实写入新的计时器复合对象。没有通过反射写入权限状态，没有预先给玩家安装计时器根，没有强制调用命中回调。

tools/seal_identity429.py逐项断言上述事实，输出reports/actual-ranged-identity429-sealed.json。它引用原始运行输出、覆盖报告和完整封存报告，并保存文件哈希。完整Session仍traceRevoked=true、非空计时器组done=false，首个失败仍是原始getter身份与独立规划不一致。

1381类一致性、74项覆盖、原生8/9/60、43项父方法克隆拒绝、四项调用者冒充拒绝、六类武器范围及176次旧附魔拒绝保持通过。6104条配方、160项插槽指纹、五项基础规则指纹和192项组件物品编码对照保持通过。这是测试玩家，不代表真人认证多人验收。NOT-INSTALLABLE，不部署。
