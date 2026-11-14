# 普通 Boss 原生入场与限定新 JVM 重载，2026-11-14

本段只接受无真实玩家在场时的普通实体入场，以及九类非克隆 Boss 的原生存档重载。Stage28 候选保持 c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e，产品代码没有变化，仍为 NOT-INSTALLABLE；本段不部署，两个正式实例只读，武器和 Session 继续暂停。

## 本段通过的范围

普通入场 V2 为十类注册 Boss 各两种属性场景，共 20 个实体、2189 项检查。调用公开 EntityType.create 与 ServerLevel.addFreshEntity，观察实际原生入场事件及自然实体 tick；屠夫使用原有公开克隆初始化，仅用于该入场夹具，不代表真实玩家召唤。该段及其 V1 失败已独立归档。

新 JVM V3 从已经通过且正常保存退出的普通入场 V2 原始世界逐文件校验后复制。完整 28 模组独立复制，保留全部 20 个原 UUID，实际观察到 20 次 loadedFromDisk=true 的原生入场。十八个非克隆实体组成独立持久化验收范围：sugar_queen、wine_barrel、rat_boss、defect_core、compost_boss、apple_boss、tea_tree、tide_fish_boss、benger，各包含无测试修饰符和永久 ADD_VALUE +5 攻击修饰符两种情况。

V3 共通过 2030 项检查；全部实体载入后观察 40 个自然服务端 tick，停止时每个接受实体至少有 37 次真实 EntityTickEvent.Post。原 UUID、注册类型、入场血量和最大血量、最大生命基础值、攻击基础值、持久化 base_hp 标签及永久 +5 修饰符保持正确；普通难度未获终结标记、没有终结翻倍修饰符。浮点字段按原生 float32 位比较，double 字段按 double 位比较，没有用较宽容差掩盖差异。临时装备或强化修饰符的完整即时总值只记录观察，不冒充所有瞬态修饰符的持久化证明。

为保持与入场夹具相同的自然模拟条件，V3 在隔离世界通过公开 ServerLevel.setChunkForced 为十八个主体建立原生区块票据，并记录原状态及返回值；没有直接调用实体 tick。票据只作用于新副本，未修改原始种子、难度、解锁、胜利、玩家身份或世界版本/警告标志。退出码为 0，完整存档，进程已回收，冻结输入及原始种子哈希均未变化。

## 失败与排除项

重载 V1 严格失败，共 586 项检查。第一失败是把公开屠夫克隆字段错误地预设为持久化字段；另两条断言受 JSON 十进制与 float32 展开误差影响。原版和当前 MeatMountainEntity 都继承 Vindicator，都只有普通私有 cloneButcher 字段，没有声明 NBT 保存/读取覆写；构造及公开 setter 写字段，getter 和原生 tick 读取。独立可重复 javap 审查绑定两份 JAR/类 SHA256。此为声明契约及当前原生观测，不声称运行过原版的新 JVM，也不把它定性为已证明的产品缺陷。

V2 改用精确原生数值位并明确分出十八个非克隆实体，十八份入场持久化字段均通过；但整个用例仍严格失败，共 1683 项检查。四个边缘区块主体在 40 个服务端 tick 后没有实体 tick 回调；服务端 tick 数不能替代实体 tick 证明。V3 仅补齐公开原生模拟票据，保留真实实体回调最低 20 次与 40 个服务端 tick 的要求。两次失败均正常停止、完整保留，未用于任何后续种子。

V3 仍加载并记录两只屠夫克隆：克隆标记为 false，随后由原生逻辑移除。它们不计入十八个持久化通过项，原先二十实体整体持久化仍明确未通过，真实屠夫召唤/战斗生命周期仍待验收。没有在重载时重设克隆字段或调用私有玩法方法。

Curios 新 JVM 真实玩家登录仍未接受，本次未重启实验性确认客户端。真实玩家合法召唤、偏好/挑战解锁、终结难度、人数变化缩放、战斗奖励、多人与模型资源完整性等仍按总需求继续验收。本段不能替代这些门槛，也不表示完整 Boss 移植完成。

## 归档与源码恢复

最终限定证据包为 work/session-boss/archives/boss-nonmanor-ordinary-native-join-and-scoped-reload-v1-20261114.zip，依赖原有 base 包 boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip，base SHA256 为 aca9e96def7887e0cf52637152df8922174326d5970376918d42b08bee864929。校验命令：

```powershell
python -B work/session-boss/tools/restore_boss_zicai_curio_archive_v2.py work/session-boss/archives/boss-nonmanor-ordinary-native-join-and-scoped-reload-v1-20261114.zip --base work/session-boss/archives/boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip
```

校验覆盖包内所有引用载荷的 SHA256，不声称执行过完整物理还原；如需物理还原，可为上述工具指定全新空目录的 --output。原始世界、失败世界、日志、JAR 和所有来源文件均保留。仅对已关闭、完整本地归档并校验的相同只读 JAR 做硬链接去重，所有原路径和哈希保持，新运行实例仍独立复制全部 JAR。

源码同步保持 codex/neoforge-source-20261113 和 neoforge/ftboss 子目录，不推送大体积世界/JAR/ZIP。新增源码先扫描、推送，再从独立远端仓库逐路径/大小/SHA256/commit 核验；仅在之后按新清单清理 sidecar 重复源码缓存。C 原件、历史失败、大体积工件和正式实例均不在清理范围。最终提交、树及远端核验以新的 ordinary-native-source 检查点和本地 publication-closeout.json 为准。
