# 非庄园终结难度：映射纠正与原生属性算术证据

本检查点推进 Boss 移植前的有效基线工作，未修改产品，未证明真实 Boss 入场后的运行时缺陷，不构成完整 Boss、挑战解锁、战斗或部署验收。两个正式实例只读；武器与 Session 暂停。

## 已排除的误判

此前初步审查中的“原版遍历所有属性、当前只遍历可同步属性”疑点已排除。MCP 1.20.1 joined.tsrg 的 bhd.b()Ljava/util/Collection; 对应 m_22170_；Mojang 原始映射将同一类/方法解析为 AttributeMap.getSyncableAttributes()。原版与当前实现都遍历可同步属性集合。旧审查原件保留，新检查点明确纠正，不能据此扩张到非同步属性或修改产品。证明及完整输入哈希位于 build/boss-nonmanor-finale-effective-v1-20261114。

## 已完成的限定验证

扫描原始 Forge JAR 与当前 Stage28 JAR 的直接调用及 invokedynamic 引用，共 126 处相关调用。扫描此前真实运行所用 28 个 mod 存档中的 470 条声明式 Mixin 目标，未解析条目为零，没有发现直接以 NonManorFinale、TeaTreeBossEntity、AttributeMap 或 AttributeInstance 为目标的声明。这不覆盖动态插件、条件配置、mod-folder 资源或完整 live transformation。

当前有效静态入口是 BossHandler.onPotionForThreat -> NonManorFinale.onJoin，其事件优先级为 LOWEST；原版 NonManorFinaleDifficulty.onJoin 为 HIGHEST。当前 BossHandler 的服务器 tick 也调用 NonManorFinale。事件顺序差异本身不证明运行时缺陷。

通过真正 NeoForge 21.1.233 专服的 ServerStartedEvent 运行属性算术探针：六种场景共 24 项检查通过，进程 exit0、原生完整保存、所有冻结输入哈希不变，关键服务器配置读回一致。该隔离实例只加载 Minecraft、NeoForge 和算术观察器，没有加载 Boss 或武器 mod，没有玩家登录，没有创建挑战解锁或胜利数据。

使用真实 AttributeInstance、AttributeModifier、RangedAttribute 重放从冻结字节码提取的两种操作：原始基础值×2、当前 ADD_MULTIPLIED_TOTAL。无既有修饰符时都是 20；既有 ADD_VALUE=5 时分别为 25 与 30；再有 ADD_MULTIPLIED_BASE=0.25 时分别为 31.25 与 37.5。已有乘算修饰符的对照与重复调用幂等性也已检查。这证明底层算术条件差异，不能替代真实 Boss 调用链证据。

## 严格失败与恢复边界

V1 未引导原生注册表、V2 尚无 FML LoadingModList、V3 观察器默认 package 导致加载失败，原件和日志均保留。V4 的 24 项算术观察全部通过，但把会由服务器补全的 server.properties 错列为冻结输入，整轮严格失败且未被改为通过。V5 保持同一 V4 Java 算术观察器及全部断言，使用独立不可变配置模板，并逐项读回实际服务器配置；使用全新世界，不重用失败世界。

## 下一项实际证据

需要在真实、合资格的注册 Boss 上建立普通/终结入场对照；既有加值修饰符与无修饰符对照；原始与当前事件优先级、缩放处理的实际顺序；最大生命基础值、受伤比例、正常保存与重载；茶树原始终结状态的有效上下文。必须保留原有解锁/胜利/身份规则，不手造数据，不通过调用私有方法或伪装登录填充验收。证明这些条件前不改产品。

Curios 新 JVM 重登仍需用户准备就绪后在真实确认页操作；当前没有等待窗口，不自动重试。原来已通过的 Curios 和 Boss 控制没有重跑。

## 归档和源码恢复

本段新证据使用内容寻址增量 ZIP，并引用固定基础归档 boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip（SHA256 aca9e96def7887e0cf52637152df8922174326d5970376918d42b08bee864929）。使用 tools/restore_boss_zicai_curio_archive_v2.py <增量ZIP> --base <基础ZIP> 校验或恢复至新目录。所有原始源码、失败世界和日志保留。源码发布仅在 codex/neoforge-source-20261113 的 neoforge/ftboss 下，逐文件路径、大小和 SHA256 的独立远端核验后，只清理计划内重复源码缓存。源码恢复不能证明本地 JAR、SDK、世界和 ZIP 已获得远端大文件恢复保障。
