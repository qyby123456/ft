# 原生组件空写修复与真实战斗缺口

同一唯一写入者继续；正式实例与 D 历史目录只读，未部署，NOT-INSTALLABLE。

## 已定位并修复的根因

原始 `Player.attack` 首先进入 `FoodWeaponBondHits.attack`。`FoodWeaponLifecycle.ensureTracked` 原来即使生命周期数据完全相同仍执行原生组件提交，触发 `ItemStack.set` 的真实修改撤销边界。fd5476e2 只消除该处空写；运行 56048ccb 已证明连续两次 ensureTracked 保持同一组件实例、完整物品图像和真实非空付费 lease/generation。

后续真实攻击又进入旧 `FoodWeaponEvolution.weaponHit`，其 `saveData` 即使进化数据无变化仍执行 `CustomData.update`，再次撤销 generation。e770d538 在该原始写回入口增加结构相等检查，真实变化仍执行原写回，不删除任何撤销守卫或签名授权条件。

静态差异 `reports/native-semantic-noop-delta-e770d538.json` 按包名加文件名逐一比较全部 716 有效源码：仅上述两个空写行为变化，以及一份安装证明的类哈希绑定刷新。1381 类、716 源码、208 守卫不变；200 项定向 ASM 校验零失败。此静态结论不是完整运行等价证明。

## 六类真实付费恢复后的空写与 ABA 回归

运行 9a01ec4f 从冻结 63062257 世界副本经实际 PlayerDataStorage.load 恢复六类 generation 3；不伪造付款、lease 或签名。六件代表均验证连续两次原始 ensureTracked 和连续两次原始 saveData 保持完全相同的组件实例、完整图像和活 generation。

剑/弩/三叉戟经原始进化写回真正改变 LastSocketProc；弓/斧/霜后法杖经原始生命周期提交真正改变 WearStep。随后走原始提交恢复完整图像，已撤销 generation 均不复活。这里是普通数据修改的负向控制，不冒充受授权的实际游戏磨损流程。saveData 的反射调用只调用原始私有数据入口，不反射写任何 authority。

报告 `reports/native-repair-runtime-semantic-noop-regression.json` 六项零失败，Java 正常退出并完成世界保存。该测试不含攻击，不表示任何家族完整 signed Session 已通过。

## 未通过项必须保留

- 3c0f3945：7751b0a9 真实 Player.attack 因 ensureTracked 空写撤销，签名伤害链未通过。
- 56048ccb：fd5476e2 的 ensureTracked 空写回归通过，但旧进化写回再次撤销；虽有 MELEE 快照，真实伤害阶段 resolveReadOnly 为空，publication/Session 为空。Java 正常退出不是战斗验收通过。
- 4240cbb1：7751b0a9 霜后法杖真实 ItemStack.use 返回 FAIL，目标生命不变，generation 撤销，无伤害事件或完整 Session。不是 Iron's 实际施法测试。

普通近战当前在 AttackEntityEvent 建立快照，但 resolveReadOnly 对玩家直接伤害只接受原始受约束 primary/generic 或 actual-hurt scope；现有 NativeHurtScope/root-site 覆盖载体、meat、Iron's 等入口，不能把旧 MELEE map 的存在当成正式来源授权。接下来必须从实际 Player.attack 调用/目标/原始伤害来源/真实磨损建立不可伪造的原生入口，再贯通现有 owner read-cut、预留、Session 与终态，不可简单放宽 resolveReadOnly 或删除守卫。

霜后原始 standalone ray/nova 不等于 Iron's CastingItem。当前 FourWeaponUseGuard 和原始 FoodWeaponUseBridge 尚未把它的实际 use/indirectMagic/磨损接入完整来源链。不能为了绿色测试允许所有 staff 或给八件原始零耐久法杖随意添加耐久。

六类付费修复/重启证据仍冻结在 7751b0a9，stage5 保存/恢复证据仍冻结在 2ca083af；不得扩大成 e770d538 全生命周期结论。认证多人、真实客户端、其余迁移终验尚未完成。

## 弓磨损回归暴露的兼容性修正

e770d538 的实际付费弓运行 f30ba60e 未通过：省略生命周期空写后，原始 `nativeLifecycleReturned` 仍硬编码按一次 CUSTOM_DATA 写入验证物理组件变更版本，导致 successor input 被撤销；继而真实 hurtAndBreak 无活权限。这不是应该绕过的守卫失败，不能复用旧弓通过结果冒充本候选通过。

524a9d86 保留两个空写修复，仅在原始初始化返回处增加独立零写入判定：无待生成 identity，原始 physical observation 仍精确 matches，独立计划 expected 与原始 input.expected 完全相等，才使用零写入、非 custom-data 变化的原验证路径。仍须通过原始 nativeTransitionReturned 的线程、实际字段、版本、before/after、lease/generation 全部检查；其余真实变化保持一次写入路径。

`reports/native-semantic-noop-delta-524a9d86.json` 精确还原比较三个方法改动及唯一哈希绑定刷新，其他有效源码不变。e770d538 的六类空写通过仍保留，f30ba60e 失败也保留。524a9d86 的定向六类回归和真实付费弓 Session 结果以各自实际运行报告和最终冻结聚合为准；没有报告通过前不得宣布通过。

## 524a9d86 本轮实际结果

- a2ddc532：原始付费弓加载 generation 0→1，原始已消费存档重放拒绝且保留首个 successor，原始 releaseUsing 磨损后新 input 未撤销，实际箭矢伤害完成唯一 publication，其 Session.traceComplete=true、prepared_retired=false，随后调用原始战斗后保存。Java/观察器均退出 0，完成正常世界保存。本项未新增战斗后再次跨进程加载验证，也不把普通本地 scratch connection 冒充认证多人。
- 16dc161d：新候选六类 generation 3 真实加载后的精确组件/图像/权限空写回归及真实修改再恢复图像 ABA 拒绝全部通过。Java/观察器均退出 0，完成正常世界保存。
- 本轮完整聚合 `reports/native-semantic-noop-focused-evidence.json` 验证候选、有效源码差异、实际运行输入和正常退出证据；全部旧失败仍保留并明确排除。普通近战和法杖完整 Session 仍未通过，未提升 adopted 候选，未部署。
