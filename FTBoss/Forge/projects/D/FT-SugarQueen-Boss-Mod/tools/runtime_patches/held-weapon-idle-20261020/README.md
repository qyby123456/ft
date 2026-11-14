# 持续手持武器的空路径热修

本补丁针对玩家实际手持场景的 JFR 热点，不是继续调整剑体格子循环。生产基线为 EAD051992BF236B3F3F161F31D454AD6DC91D2C644B5A5B551BC0990937B0994。

## 行为边界

- OwnerRepairContinuation 增加包内 static noBinding，仅在原有 OwnerFullRepairGuard 锁内查询同一个 BINDINGS 弱引用表是否没有记录；无授权、句柄或状态写入。
- OwnerStatueWearAdmission.entered 在“没有 continuation binding 且原生 wearOrigin 为空”时直接返回 null。任一上下文存在，仍执行原方法的完整 eligibility、调用者、原生语义和修理事务校验。
- NativeFoodWeaponWear.settleDamage 在原 server() 返回 null 时提前返回。原 holder() 在同样情况下也只能返回 null；服务器线程上的真实损耗结算保持原样。
- FoodWeaponLifecycle.beforeDamageChange 在保留 INTERNAL_WRITE 前置检查后，只对非负、与当前耐久相同、且原 statueRepairWrite 返回 false 的写入直接返回原数值。实际修理上下文与真实耐久改变仍走原方法；这不是跳过减损/修理校验。
- FoodWeaponEvolution.socketTypeCount 保留 foundation read、有效/持久/等级检查；先按原定义计算付费、解锁的 FOOD 条目计数，计数为零时不再重复执行额外 ready/use/broken/fingerprint 检查。任何非零结果仍必须经过全部原检查。
- FoodSocketPowers.flight 先计鱼类；少于两份鱼直接 false，不再读取不可能激活飞行的酒类。满足鱼类数量仍执行原飞行组合纯规则。
- 不缓存权限、NBT、来源、武器状态或读数；不移除规范化、防伪、所有权或实际修理/损耗验证；不更改伤害、材料、数量、注册、模型、食物组合配方。

## 补丁与源码

主工程运行源码尚不包含生产的完整原生守卫闭包，也没有生产 foundation 分支，不能用整包重编译覆盖生产包。主源码仅同步可直接对应的 flight 短路；其余四旧方法和一个 helper 以本工具精确移植。template 只供提取 noBinding 方法，绝不能把模板类整体装进运行包。PatchHeldIdle 钉住全部六个原始类 payload SHA-256，任一类改变必须先审查、重新回归，不允许静默覆盖并行修改。

PowerShell 7 示例：从本目录执行 ./build-candidate.ps1 -BaselineJar <当次生产包> -OutputDirectory <工程/_work/新的隔离目录>。不运行主工程全量构建。

VerifyHeldIdle 要求只有六个类、五个旧方法改变，新增一个包内 static helper，404 个其他方法、字段和其余 4101 个条目原样保留。SocketCountParity 执行 18000 组实际生产字节码差分，覆盖有效/无效 profile、付费/解锁、非零结果完整校验、旧制分支和空输入。DamageNoopParity 覆盖 4488 组实际耐久、同值写入、修理钳位、内部写入、负值/极值和空输入。HeldIdleParity 覆盖 81 个飞行组合、8 个无上下文/有绑定/有原生来源的拒绝分支，以及客户端无服务器/服务端 eligibility 和绑定缺失查询。以上都不能替代真实 Forge 回归或玩家 FPS 验收。

最终候选还必须通过 -Xverify:all、独立 Forge 私有测试世界全部 34 项原生耐久烟雾回归、ZIP/CRC/descriptor 校验。使用不同哈希的候选要重新做游戏回归。独立性能探针的原方法镜像仅用于对照，不得打入交付 JAR。

## 部署与证据

现场证据位于 D:/ft工程文件/FT-SugarQueen-Boss-Mod/_work/held-weapon-live-profile-20261020。原始 JFR 含潜在 JVM 环境信息，不能提交或公开。只发布脱敏的方法统计及回归结果。

最终 b8570113f405f7853f02e4a65c8385519c1786694e6c8973cfa9dbfc109e960c 包已于 2026-10-20 10:41 在当前实例部署并通过安装回读。最终 34 项真实 Forge 回归见 runtime-native34-final，四路径方法对照见 runtime-heldbow-final；release/DEPLOYMENT.json 与 FINAL_VALIDATION.json 保存精确范围。玩家实际 FPS 尚未验收，方法耗时对照不是 FPS 提升声明。

部署需要退出游戏，取得工程、实例、mods 安装三把锁，用当次完整基线 SHA 阻止并发回退，调用 tools/deploy_jar.ps1 的 NoBuild、ArtifactPath、TargetPath、ExpectedTargetHash 参数。只写 FarmingTales_ForgeⅡ v1.5.2；不写 fix2、桌面、NeoForge 或玩家存档。部署后必须回读实际安装包的 hash、类/方法 delta 和 ZIP。

Docs impact：只增加不授予能力的负向内部查询，不改变已有对外契约；边界说明提案在 _work/docs-proposals/held-weapon-idle-negative-probes-20261020.md。没有写入 docs/**。
