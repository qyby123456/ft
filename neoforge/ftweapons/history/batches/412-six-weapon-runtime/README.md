# 412：六类保留与非六类旧数据拒绝实测

使用411累计1376类，在真实NeoForge 21.1.233 / MC 1.21.1启动，运行目录focused-storage308-bd6d01fb，PID62968已退出。exitCode=0、probe failures=0、stdin stop=true、completeSave=true。1376运行类逐字节哈希核验通过；原生8/9/60、74项覆盖、43个完整父证明克隆反例和4个反射伪造反例保持通过。

六类正例为铁剑、弓、弩、铁斧、三叉戟、Iron's Spellbooks九元法杖，均被统一类型范围保留。这仅证明这些代表物品，不能宣称所有第三方武器均已通过。

12个反例为六种原版锄头、铁镐、铁铲、剪刀、盾牌、钻石胸甲和木棍。每件均注入旧butcher成长Profile与钻石合金材料数据，真实检查统一范围拒绝、附件策略排除、当前/legacyOrdinary进化候选拒绝、耐久合金计数为零且材料回退ORIGINAL。没有清空或改写玩家正式物品旧数据。

非空blood_feast仍触发80tick、生命10变11、计时器80L；选中1条指令但done=false、traceRevoked=true，首拒绝仍为FoundationAuthorRuleSnapshots.fingerprint第60行临时NBT写入。正向signed Session未完成。

本批仅封存六类准入及旧数据拒绝，不宣称所有独立UI/网络/资源路径已经删除。FTBoss附魔系统尚未删除。状态NOT-INSTALLABLE，不部署。后续继续按用户收缩后的范围处理，不恢复锄头等旧武器适配。
