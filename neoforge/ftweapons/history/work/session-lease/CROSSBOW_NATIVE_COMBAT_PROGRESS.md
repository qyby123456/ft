# 原生弩完整使用与战斗入口

唯一 C 工作区，同线程独占写入；未部署，NOT-INSTALLABLE。正式累计候选仍为 actual-current.json。此前 Frost 2e5c6034 的限定通过范围与冻结证据不变。

## 已完成的真实观察

- 30bc9a2e / PID 49892：使用原 Frost 2e5c6034 产品图，真实支付样本 2、原始重载得到非空 lease 与 generation 3。实际 ItemStack.use 返回 CONSUME 并启动使用，但原始 use 的保守 mutation HEAD 撤销 generation。未到上弦阶段。Java 0、观察器 1、原始 stop 与完整世界保存均完成。
- f4f2deae / 7185b0fb / PID 50752：增加仅原版弓、弩、三叉戟的真实 ItemStack.use HEAD 标记，限定实际 Mixin 来源、完整父方法安装证明、线程、帧、玩家、槽位、原始物品身份、规则、不可变全图及同一 lease/generation。没有授予伤害权限。实际 use 后 generation 保持存活；实际 releaseUsing 消耗 1 支箭并装入弹药，但 releaseUsing 的保守 mutation HEAD 撤销 generation，未发射。Java 0、观察器 1、原始 stop 与完整世界保存均完成。图仍为 1381 类 / 716 源码 / 208 守卫，217 项定向 ASM 检查 0 失败。
- 516636b2 为编译失败草案：误写 nativeTaskObjectPolicy 所属类，未运行 Java；修正为 OwnerRepairGenerationAuthority，保留失败日志。
- 8255d03a 编译与 217 项 ASM 检查通过，但静态复核发现实际组件存储回调名为 fullRepairStorageValue，而非 ItemStack 的 fullRepairReturnedMutation；未启动游戏，修正回调选择后再构建。
- b18a804d / f8187e93 / PID 36124：真实 ItemStack.use 与 releaseUsing 上弦后 generation 均存活，普通箭 16→15，实际装入弹药。第二次原始 use 发射造成真实 20→10.08128、磨损 0→1，但 performShooting 清空装填组件的 ItemStack.set 撤销 generation，弹丸缺少签名与付费 carrier，Session 未形成。Java 0、观察器 1、原始 stop 与完整保存完成。保留此失败，不算完整战斗通过。

头部标记单独差异证明 native-ranged-use-head-delta-f4f2deae.json 已冻结：三个行为源码精确逆变换回 Frost 2e5c6034，一个绑定源码仅哈希更新，其他 712 有效源码逐字不变；1381 类、716 源码、208 守卫，3530 个输入哈希。仅证明差异范围，不替代运行通过。

- 428a86f9 / 8e3565ef / PID 62860：真实上弦仍通过；发射清空组件仍失败，实际原始 beginNative.ensureTracked 的语义无改动路径合法产生了输入 successor，原发射计划额外拒绝旧 input.retired。外层日志确认已有 successor。Java 0、观察器 1、原始 stop 与完整保存完成。新增计划改为读取并严格核对实际当前未退休输入的物品、线程、registry、原始输入引用及完整 expected，仍由原始 advance 回调推进，绝不重置 retired。428a86f9 的四个组件行为源码局部逆变换及三个纯哈希绑定差异另以 3532 输入哈希冻结，其他 709 源码不变。
- 69703b57 为仅编译失败：ItemStack 在编译期未实现 Mixin 注入接口，补上既有模式的 Object 转换后构建。无游戏运行。
- 76bc665d / 08e0f45e / PID 9096：上弦及原生发射清空组件已越过原撤销点；首个撤销转移到 AbstractArrow 原始构造器对武器的 copy。原因是 nativeArrowFactory 的独立复制计划仅启用 BOW，没有启用 CROSSBOW。实际伤害 20→8.979199、磨损 0→1，但仍无签名 carrier，不能算通过。Java 0、观察器 1、正常 stop 与完整保存。组件候选差异另已冻结为 native-crossbow-component-delta-76bc665d.json，3532 输入哈希。
- 89f2b629 / 616870ba / PID 8816：真实 gen3 原版弩从 ItemStack.use、releaseUsing 上弦到第二次 ItemStack.use 发射、真实 arrow.tick 命中首次完整通过。伤害 20→8.979199、原始磨损 0→1；弹丸 snapshot 有真实 repair epoch、64 位服务端签名、NativeEntry CROSSBOW 与 paid carrier。唯一 Session 的 traceComplete、coverageInstalled、committed、sealed、bound 为 true，未退休；同一非空 lease/generation 保持存活。原始战后 save 的 gen3 continuation 已认证 COMMITTED。Java 0、观察器 0、正常 stop 与完整保存。仅此代表性普通箭战斗范围通过，不等于完整迁移。
- 9d1607e8 为当前未提升候选：另增加原始提前松弦的严格 no-write 完成分支，要求实际输入身份、完整物品图、物理组件版本、弹药、lease/generation 全部不变，直接关闭观察作用域，不刷新或重建权限。新增夹具先真实使用 1 tick 后提前 release，再重新 use 和完整上弦/发射；正在运行。相对 89f2b629 仅一个语义源码改变，其他 715 源码不变，3533 输入哈希已冻结。1381 类 / 716 源码 / 208 守卫；218 项定向 ASM 检查 0 失败。
- 9d1607e8 / 8d8cfcf0 / PID 61868 已完成：原始 1 tick 提前松弦时，实际输入身份、全物品图、弹药 16 支及同一权限均保持不变；重新原始 use、完整 release 上弦到 15 支箭、再次 use 发射、真实 arrow.tick 命中，伤害 20→8.979199，原始磨损 0→1。唯一完整 live signed Session 的 committed/sealed/bound/coverage/trace 全通过；gen3 原始战后保存已认证提交。Java 0、观察器 0、正常 stop 与完整保存。随后以该战后世界只读复制种子启动全新 JVM fc486b2f / PID 70856，正在检验真实 gen3→4 及已消费存档拒绝。
- fc486b2f / PID 70856 已完成真实全新 JVM 原始加载：同一 epoch 的 generation 3→4，全物品与磨损 1 一致，非空 lease/generation 存活，原始 gen4 保存已提交；已消费旧存档重放被拒绝，未改变首个真实 successor。Java 0、观察器 0、正常 stop 与完整保存。
- 9fa52bf0 / PID 8144 外部入口对照已完成：真实原始提前松弦及上弦先通过；在原始作用域外直接调用三个公开回调，实际 input 身份、物品图及权限不变。随后绕过 ItemStack.use 而外部直调公开 nativeUse，真实原动作只执行一次、生成一支箭且原始磨损 1，generation 按预期退休，弹丸没有有效签名 snapshot 或 paid carrier，也没有完整 Session；全部私有作用域已关闭。Java 0、观察器 0、正常 stop 与完整保存。

## 当前实现范围

为未附魔原版弩、至少两支无额外组件普通箭、未装填状态建立独立上弦预期。仅接受原始 releaseUsing HEAD 和 tryLoadProjectiles 的实际 ItemStack.set / PatchedDataComponentMap.set 路径；原方法仍执行一次。返回时要求恰好一次组件版本变化、其他组件对象身份与图不变、全物品编码等于独立预期、实际弹药恰减一，再通过原始输入 advance 推进并刷新同一非空 lease/generation。发射时另独立预测单支普通箭装填到 EMPTY 的组件变化，在实际既有 ItemStack.set RETURN 回调核对物理版本、对象身份、完整图与参数后推进当前真实输入，继而进入既有箭工厂 copy 与 wear 计划。提前松弦的无写入路径保持实际输入和权限原样。失败保持保守撤销，不重置 retired、不伪造权限。上述限定路径已通过实际运行。

## 冻结验收

native-crossbow-paid-signed-combat-focused-evidence-9d1607e8.json，SHA256 e4951e8dc4144e142e97e225f79f7dec31c8c125d9aa1563ebef6eee8b67e256。四个通过运行（含首次 89f2b629 战斗见证）、四段定向差异证明、8079 个输入哈希；五个较早失败运行明确排除。全部九个本阶段 Java 进程已退出，无权威存储或玩家目录 .tmp/.pending 残留。正式累计候选仍未提升，正式实例未写入。

## 尚未通过

其他弩变体、多弹射、附魔、焰火、完整弹药/中断/生命周期拒绝矩阵、真实认证多人。三叉戟原始使用/投掷/命中/拾取与完整弓使用路径是后续优先项。隔离观察器使用本地 scratch 连接，不代表认证多人通过。全迁移仍未完成，保持 NOT-INSTALLABLE。
