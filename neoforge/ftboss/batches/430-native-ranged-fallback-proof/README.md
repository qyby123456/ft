# 430 原生缺失与错误类型回退复现

不修改累计候选。tools/check430.py校验427输入，再用已固定的真实MC依赖和完整产品类路径编译、以-Xverify:all执行独立JVM复现。这不是带Mixin的游戏运行；真实游戏分支证据见429。

十二项回退覆盖缺失键及byte、short、int、long、float、double、byte array、string、list、int array、long array旧键。每项都证明：原生getCompound返回新建空对象，两次调用返回不同对象；获取及对新缓冲区写入不会改变玩家原键，直到原始put才完成替换；当前独立规划器rangedAcquire对这些合法原生分支全部拒绝。已存CompoundTag对照保留原对象身份，规划和实际Math.max语义保留较大95L，不将其改成80L。

reports/actual-native-fallback430-sealed.json封存十二项复现、已附着对照、来源与输出类哈希。早期失败尝试保留：首次脚本读取不存在outputHashes字段，第二次测试误用Operation.value，第三次缺少报告目录；最终通过尝试未略过断言。

根因同时涉及规划模型与对象分配身份，不是NBT内容不同。后续修复必须独立预规划缺失/错误类型时的空缓冲区与延后附着，证明缓冲区来自本次真实原生getter分配，再绑定对象身份。不能用equals替代身份、接受任意空CompoundTag、把观测后的计时器值当预期、预先写玩家根或全局放宽原始Map读取。已有复合键分支、异常路径和未知别名修改撤销仍须保留。该修复尚未实现，NOT-INSTALLABLE。
