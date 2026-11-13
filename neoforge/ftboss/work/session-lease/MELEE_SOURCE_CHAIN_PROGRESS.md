# 实际近战来源链：部分贯通，未验收

唯一 C 工作区、同一唯一写入者继续。正式实例和 D 历史只读，未部署，NOT-INSTALLABLE。前一阶段 524a9d86 的六类空写/ABA 与付费弓 Session 证据保持原范围，不扩大为近战通过。

## 新实现

候选 8e76bf0d 在已有 NativeReadCutPlayerMixin 中包装实际 Player.attack 的唯一 Entity.hurt 调用，保留原始参数及 Operation。新原始边界检查核验实际 Player 类、attack 方法描述符、合并后处理器描述符和 MixinMerged 来源，且要求真实服务端线程、主手与选中槽的同一单件物品、同一玩家直接 PLAYER_ATTACK 伤害来源、世界中同一目标对象、有限正原始伤害。

当前精确支持原版 SwordItem、AxeItem 的该入口；没有放开任意子类、所有武器或法杖。沿用既有 NativeIngressToken、NativeHurtScope、独立 WeaponExpectation、真正 generation/lease、服务器 epoch/签名及原始 interaction read-cut。新 MELEE 枚举值在其他旧发行/载体入口明确拒绝，不能借用旧分支取得权限。

新快照仅在实际伤害动态 scope 内有效；伤害返回后关闭 interaction 与 expectation，不保留可重放的 actual-hurt 签名权限。BondHits 对这一已验证的实际 MELEE scope 使用同一签名快照，其他旧来源行为不改。

1381 类、716 有效源码、208 守卫不变，无新增产品类。207 项定向 ASM 零失败。`reports/native-melee-ingress-delta-8e76bf0d.json` 按包名和文件名比较全部有效源码，精确逆转源码修改确认仅上述三个行为源码及必要哈希绑定刷新；不是完整近战运行验收。

## 实际运行与保留失败

- e3de73a5 / b0e917b5：第一原型在对既有签名快照重算 factors 时触发原有“Factors must precede HMAC”拒绝。真实攻击未完成，此失败保留，不能算通过。8e76bf0d 在重新冻结 factors 前移除旧 HMAC 并重新签名；来源捕获失败时仅拒绝附加权限，原始伤害回调仍原样执行，不吞掉其异常。
- 8e76bf0d / 59518864：从真实已付费世界经原始 load 恢复非空 generation 3。外国直接调用不能制造 scope，原始回调恰好执行一次且原异常对象身份不变。实际 Player.attack 返回；BeforeMitigation 与 BeforeHealth 都解析出非空、有效、带 repair epoch 和 64 字符服务器签名的 `NATIVE_MELEE_ACTUAL_HURT`。同一原伤害来源与签名快照在返回后均拒绝。真实目标掉血，原始磨损 0→1。
- 该运行确实产生唯一原始 publication，preparedCommitted=true、originalSequencePresent=true、preparation bound/sealed 且未 retired；但 Session.traceComplete=false，generation 最终 retired=true。观察器失败 1，未达到验收，也未进入斧样本。不能将“有签名快照/掉血/有 publication”冒充完整 Session。
- 918ed33e 的同候选只读诊断捕获 firstObservationFailure：NativeTimerTransitionPlan.nativeAssignment 拒绝，因为 TIMER 的原始 ItemStack.set 赋值入口证据未成立。Session.coverageInstalled=false、traceRevoked=true。诊断随后读取不存在的 Step.kind 字段另有观察器错误，保留并排除其总体通过结论；此前已经记录的首个真实失败堆栈可用于定位。修正诊断仅读取真实存在的 phase/closed/failedPhase。
- 临时 09fa7e2b / 75200621 只增加 assignmentEntering 拒绝日志，无规则放宽；实际同样失败且日志零次，表明该分支未被到达。临时日志已从可变 proposal 中精确移除，冻结候选和运行仍保留，不采用为功能候选。

## 下一实际断点

`plannedNativeTimerMutation` 在进入 assignmentEntering 前还要求有效 Session 覆盖证明及原始调用链；现正以 8e76bf0d 原冻结产品做只读覆盖目录/已安装目标/late-stage 状态诊断，不伪造注册或 coverage 标志，不删除计时器和撤销检查。需要依据实际缺失状态修正原始注册/加载/绑定流程，而不是猜测为“守卫太严格”。

即便 TIMER Session 后续修复，仍须覆盖原始 hurtEnemy/postHurtEnemy 的真实磨损连续性、斧样本、后续保存及拒绝输入。霜后法杖来源链、其余六类变体、认证多人、真实客户端及全部迁移终验仍未完成。

## 覆盖目录实测定位

3299d8c3 的修正只读诊断正常完成且保持真实失败：registryInvalid=false，四个 late-stage 都为 2，prepared sealed/bound 且未 unsupported/retired，但 coverage 与 catalogueGeneration 都为空。未按旧普通安装模式填充的 14 行中，其他行属于已有 NF 原生替代认证；第 58 行 NativeExpectedCaptureBoundaryMixin → FoodWeaponUseBridge 未加载/未观察，无替代认证分支。普通近战此前不经过该原始使用适配器，全球覆盖目录因此不能签发 generation。

下一候选在真实原始入口通过 FoodWeaponUseBridge.kind 解析武器类型，触发此原始类的正常加载、Mixin 与完整安装证明，不写 registry/coverage 标志、不改目录要求、不添加备用签名。该静态 kind 方法只委托 NativeWeaponTypes.kind，不执行物品 use，也不额外磨损。结果必须以新候选的实际攻击运行验证，不能把定位本身当作 Session 通过。

## 2026-11-11 原进程收尾与战后连续性

25cdcaa5 / 0bd91833 的实际剑攻击已完成真实非空 generation 3 的完整 signed Session：traceComplete=true，原始 publication 已提交且 preparation sealed/bound、未 retired。随后原始 ItemStack.copy 的守卫入口撤销 generation，故整体近战验收仍失败。

原 PID 51268 的 focused observer deadline 发生于跨夜后，启动器结束并不等于 Java 退出。按用户要求继续检查同一进程、完整观察文件、日志与权威存储，不自动重启。确认其观察器已经结束后，单独的测试辅助 agent 经公开 MinecraftServer.execute 调度原始 halt(false)，正常保存并退出；未强杀、未变更权威字段、未重定义类。原父进程已丢失 Java 退出码，因此只声称有完整正常保存/关闭日志和 PID 已不存在。冻结收据 `reports/native-melee-observer-deadline-0bd91833.json` 保留辅助工具及输入哈希；此后附加 agent 的环境不用于 JVM 定义工厂无 agent 证明。

新启动器的观察超时仅记录 deadline，保持同一 Java 进程的 stdin 与观察循环，不再自行退出并遗留进程；原冻结启动器未覆盖。

80e93548 / 25262771 在原始 Player.attack 的 copy、hurtEnemy、postHurtEnemy 三个精确调用点加入私有动态操作与独立终态核验。原始复制仍不给复制品 lease/generation；仅精确原版剑/斧、真实玩家选中主手、已修复活跃权威可进入。其他旧权限检查不改。静态差异收据 `reports/native-melee-wear-delta-80e93548.json` 比较全部 716 有效源码，5 个行为源码、3 个纯哈希绑定源码，1381 类和 208 守卫不变，757 个输入哈希，定向 ASM 零失败。

该运行完整 Session 仍通过，真实掉血与磨损 0→1，但首次权威撤销已移动到原始 postHurtEnemy 的 void 入口；整体观察器仍失败 1，尚未进入斧或战后保存样本。Java 退出 0、原始 stop 和所有维度保存完成。当前继续核查独立磨损计划在该入口为何未成立，不放开普通复制、任意回调或未知写入。

73f30794 / 3751ec76 的临时诊断精确定位独立磨损计划拒绝于 foundation-view：实际剑无法通过旧 FourWeaponFoundationAdapter.read(detached).valid()。临时日志已从可变提案移除，原失败保留。新近战计划仅以当前私有 meleeSite=3 操作、原主手/线程、同一已签发输入及活跃 lease/generation、独立解码后完整图像、原始 nativeTaskObjectPolicy、同一物品/数量/耐久和物理存储身份共同准入；非该私有操作的旧 Foundation 分支逐字不变。

ba5914db / 3830a323 已通过 postHurtEnemy 入口，首次撤销移动到其原始 setDamageValue。dbbe3e59 / 0031f034 增加原始 spec 9 入口后，首次撤销又移动到 IItemExtension.setDamage → ItemStack.set 的原有 OwnerStatueWearWrites.rawDamage/write/lambda 包装链。两次均真实 Session 完整、观察器失败 1、Java 退出 0且正常保存；不能算完整磨损通过。eef72352 精确加入该原有包装器的 rawDamage/write 及其编译器 lambda，仍要求实际 nativeAppliedDamage 动态边界、同一原件、最多一次原始损耗写入、独立前后物理图与预期图像；当前运行验证中。

## eef72352 剑、斧实战与战后跨进程恢复通过

58a3db1d 实际原始 Player.attack 的剑、斧样本均通过，Java/观察器为 0，原始 stop 与所有维度保存完成。两件均来自真实付款、CLAIM 与原始存档加载取得的非空 generation 3；BeforeMitigation/BeforeHealth 签名有效且带 repair epoch，原始唯一 publication 已提交，prepared sealed/bound、未 retired，Session.traceComplete=true。真实目标生命由 20 降至 19.807775；原始剑磨损 0→1、斧磨损 0→2，实际 lease/generation 仍为同一活对象。原始复制不复制权威，普通外部调用不产生 scope，同一实际来源/签名快照退出 actual-hurt 后不能复用，外部原始回调异常身份保留。两件战后原始保存的认证 continuation 均为 COMMITTED_ORIGINAL_SAVE。

2a29819c 是复制该已正常保存世界后启动的实际新 Java 进程，不修改源世界。经原始 PlayerDataStorage.load 验证完整物品与磨损状态一致，同一 lease epoch 从 generation 3 前进到 4；同一已消费旧存档再次原始 load 不能发行权威，也不改变首个活 successor、认证文档和 lineage 字节。随后原始 successor 保存提交 generation 4。该运行 Java/观察器亦为 0，完成正常 stop 和全部维度保存。

冻结聚合 `reports/native-melee-paid-signed-combat-focused-evidence-eef72352.json` 核验 900 项输入/输出哈希，包含有效源码差异、候选、两个实际运行、原始世界与认证文件、观察器和启动器；不表示完整六类战斗、任意磨损阈值/变体、真实客户端或认证多人已通过。候选仍未提升，NOT-INSTALLABLE，未部署。当前继续法杖原始实际 use/ray/nova 来源链。

b7ae38a9 在未改变 eef72352 产品候选的实际新进程中，原始加载两件战后 generation 4 存档取得真正活 generation 5，分别验证外部 public melee copy 和 post 包装入口。外部原始回调抛出的同一 RuntimeException 保留身份且只执行一次、物品与活权威不变；随后真正外部 copy/post 原始方法同样各执行一次，复制品没有 lease/generation，原件 genuine generation 必须 retired。外部斧 post 原始磨损仍实际增加 2，不借用合法原始 Player.attack 的权限。两条路径的 NATIVE_REPAIR_OPERATION/NATIVE_WEAR/PRIVATE_NATIVE_HURT/PRIVATE_INGRESS 均无遗留，未重发权威。Java/观察器 0，正常 stop 与所有维度保存完成。独立控制聚合 `reports/native-melee-paid-signed-combat-controls-focused-evidence-eef72352.json` 复核此前 900 项证据并累计 948 项哈希，原冻结报告未覆盖。

终态只读进程审计确认本 C 工作区没有遗留 Java/Python/javac 进程；可变 semantic 提案与 eef72352 冻结源码逐字节一致。实际已采用候选 actual-current.json 仍为 1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243。唯一写入者保持当前线程，不撤销、不换线。法杖原始反汇编、实际 gate 缺口和后续精确私有 scope 方案已记录于 FROST_QUEEN_STAFF_INGRESS_DESIGN.md，尚未实现或声称法杖通过。
