# 415：附魔退休与六类范围真实回归

414累计候选1376类在真实NeoForge 21.1.233 / MC 1.21.1运行。目录focused-storage308-411315df，PID28912已正常退出；exitCode=0、probe failures=0、stdin stop=true、completeSave=true。1376个运行类逐字节哈希核验通过。原生8/9/60、74项覆盖、43个完整父证明克隆拒绝及4个反射伪造拒绝保持通过。

六类代表正例和12种带旧成长/合金数据的非保留物品反例保持通过。附魔退休使用真实ServerPlayer对象与铁剑、64块生牛肉、20级经验；武器带butcher成长与blood_feast、cleaver_might、butcher_hide、vintage_crit旧食物附魔数据。11种tint和8个槽索引、当前及legacyOrdinary接口共176次请求全部拒绝，候选判断拒绝，旧展示为空，护甲请求亦拒绝。前后ItemStack.matches、食物数量、武器数据、经验等级与总经验相同。该玩家为隔离测试对象，不是真人或经过认证的联网玩家；没有测试真实客户端发包。

移除回调的无副作用证据亦见413的23项字节码断言，包含防御、击杀、磨石及护甲事件。旧请求ABI只作为拒绝壳保留；包处理常量、旧目录/定义、翻译等协议/资源残留仍需最终裁剪，客户端真实GL尚未验证，不宣称零残留或完整附魔删除验收。

非空成长技能blood_feast仍在80tick使生命10变11、写入LastSkillProc.blood_feast=80L，与已移除的同名食物附魔枚举不是同一功能。选中1条指令但done=false、traceRevoked=true，首个拒绝仍为FoundationAuthorRuleSnapshots.fingerprint第60行Profile临时CompoundTag写入。完整signed Session、lease/repair/payment及认证多人等验收未完成，NOT-INSTALLABLE，不部署。

运行中仅用jcmd Thread.print记录一次只读线程栈，确认正在进行FoundationApprovedRules配方指纹计算；未设置断点、未用JDWP、未修改运行对象、未因观察耗时重启。线程观察文件保留于运行目录。
