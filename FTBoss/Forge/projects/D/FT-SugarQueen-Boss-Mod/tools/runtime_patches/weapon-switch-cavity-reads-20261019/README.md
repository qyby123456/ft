# 武器切换：消除单次剑体构筑的重复读取

本补丁只修改生产包中的 SwordCavityBuild.class，不用主工程整包覆盖新武器、UI 或原生耐久守卫闭包。

## 变更边界

- effectiveCells 在一次调用中读取一次适用性、功能规则和进化等级，而不是对 27 个格子逐格重读；不跨调用保存武器状态。
- legacyBuild 从已经解析的有效格子生成三部件材料列表，避免 legacyMaterials 再次读取同一武器。
- 新增 private static legacyMaterialValues 纯函数；其余 54 个原方法、字段、所有其他 JAR 条目保持原样。
- 适用性为 false 时保留逐格遮蔽的语义；空/短列表的异常边界不引入特殊放行。
- 不更改权限、修理事务、NBT 键/格式、伤害/损耗/羁绊计算、渲染或武器模型。
- 主源码的 legacyBuild 已以内联方式复用有效格子，本工具仅以等价私有助手实现最小字节码补丁。

## 文件及验证

- template 是只用于移植两个纯读取方法的最小编译输入，不是可直接打包的完整运行类。
- PatchCavityReads 要求原有效格子实现恰有一处 cellUnlocked 调用、legacyBuild 恰有一处 legacyMaterials 调用，否则拒绝修改。
- VerifyCavityReads 检查条目逐字节一致性、仅两个旧方法改变、仅新增一个私有助手、字段一致。
- CavityReadParity 在隔离类加载器中执行实际生产方法与实际纯规则；只有 NBT/扩展入口换为固定输入。覆盖 756 组格子/规则/等级/适用性组合，及 3024 组含链接奖励的构筑比较，另检查输入不变与调用次数。
- 同一回归也对完整主源码单文件编译结果执行，避免最小模板与主源码行为分离。
- 必须使用 Java 17 和 ASM 9.8。编译模板时以官方映射 Forge 1.20.1 和当次生产包为 classpath；编译工具需 asm、asm-tree、asm-util。
- 用 JDK jar.exe 更新 candidate 的唯一类，再运行逐条目验证、ZipStreamCheck、tools/verification/check_jar_zip_rules.cjs。
- 正式部署前还需当前候选包在独立 Forge 客户端/测试世界完成实际耐久回归。切勿把隔离纯方法回归称作完整游戏验收。
- 本轮候选已经完成 34 项独立 Forge 耐久烟雾回归，正式实例安装后的哈希、唯一条目增量和 ZIP/CRC 回读也已通过；尚未取得玩家切换时的帧时间 A/B。

## 现场证据与限制

本次任务工作区：D:/ft工程文件/FT-SugarQueen-Boss-Mod/_work/weapon-switch-live-profile-20261019。
现场 JFR 证实之前的 int[] alias 补丁仍已生效，但生产 effectiveCells 仍逐格重建规则。
两段现场采样含石像界面、背包/JEI 与自定义武器渲染，不能作为纯切换动作的帧率 A/B 验收。
其中石像界面还有独立的作者规则快照/规范化热点；本补丁不删除完整性检查，也不缓存其授权结果。
原始 JFR 可能包含 JVM 环境和启动参数，不应公开或提交版本库。

部署必须先退出所有 Minecraft 客户端，通过工程锁、实例锁、安装锁和 ExpectedTargetHash 防止并发回退；始终走 tools/deploy_jar.ps1，只写用户指定的 Forge v1.5.2 实例。

Docs impact: none；只减少单次读取内重复计算，不修改对外契约，未写 docs/**。
