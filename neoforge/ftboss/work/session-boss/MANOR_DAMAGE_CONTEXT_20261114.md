# Manor 原生伤害上下文适配，2026-11-14

当前 Stage29 是限定范围通过的 Boss 候选，NOT-INSTALLABLE，未部署。两个正式实例仍只读；武器与 Session 开发、验证继续暂停。模型、贴图、动画、音乐及带这些资源的 JAR/ZIP 不得上传，以下归档只保存源码、文本证据与哈希。

真实离线集成客户端中，生存玩家对正常存活的麻雀 Boss 使用原版空手攻击，Stage28 在 ManorFinalDamage.settle 调用生成的 ftboss$reduceManorDamage 时触发 EmptyStackException。NeoForge 21.1.233 的 LivingEntity.getDamageAfterMagicAbsorb 对抗性/附魔减伤使用 damageContainers.peek；原版 1.20.1 的独立庄园结算没有这一平台上下文。初始失败、完整保存日志与回执收尾失败均保留，失败世界没有作为后续种子。

Stage29 只修改 NativeManorFinalDamageInstaller 一个类：为生成的减伤访问器建立本次调用自己的 DamageContainer，在原有护甲→魔法减伤两次调用之后正常退出或原异常退出时清理本次上下文。原结算数值、分摊、吸收、冷却、门槛及剩余方法保持原序。72 项生成字节码生命周期检查覆盖普通、外层上下文、嵌套调用、两处异常身份与四个破坏控制；这部分是合成验证，不能代替游戏证据。

全新游戏世界中的真实 ServerPlayer.attack 已通过 701 项限定探针检查，Boss 生命从 1024 降至 1023.72，一次原有 BeforeHealth 事件，120 次自然实体 Post 与 220 次自然玩家 Post；游戏正常完整保存、退出码 0，所有冻结输入不变。该运行明确是离线 fixture，没有认证、完整战斗或新 JVM 旧存档持久化验收声明。

采集 Agent 因 Windows 原生选项字符串编码将输出写到中文路径的乱码/截断目录。只迁回本次两个运行的 11 个文件，共 660530 字节，逐文件迁移前后 SHA256 相同；未移动历史 Session 子树，未删除其文件。V1 原运行回执的采集门槛失败保持原样，本收口补充绑定找回的原始定义字节。对真实 VM 定义的 EasterBossEntity、LivingEntity 及失败基线对照，22 项检查确认失败版没有上下文、成功版有压栈及两条清理路径，hurt、manorHurt、hitFeedback、sparrowTick 指令不变。Agent 始终返回 null，没有重定义或重转换。V3 后续 Agent 改为 Base64URL 编码 UTF-8 路径，中文目录往返合成采集验证通过；没有为修正路径重跑游戏。

本次探针确认原有自定义 BeforeHealth 事件与真实生命变化；NeoForge LivingDamage.Post 回调仍为 0，完整原生伤害事件兼容契约未验收。麻雀原版闪避尚未移植；实际有效受击基线已就绪，观测窗口没有导航路径，观察后的公开路径规划可达。下一段先完成原版闪避等价移植及正反向自然场景，再补齐其他 Boss/资源表现、认证多人战斗、奖励和生命周期要求。

构建与来源：work/session-boss/build/boss-manor-damage-context-v1-20261114/candidate-build-receipt.json。
收口：work/session-boss/reports/manor-damage-context-v1-closeout-20261114.json。
原失败：work/session-boss/reports/sparrow-evasion-native-baseline-v2-39125bde.json。
原成功行为/初始采集门槛失败回执：work/session-boss/reports/manor-damage-context-native-v1-820f458d.json。

候选和运行原始资源仅留必要本地缓存；源码 Git 归档不构成这些本地资源的远端恢复证明，不能据此删除它们的唯一副本。
