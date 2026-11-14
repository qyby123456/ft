# 文玄蓝技能半径与超能光束蓄力（2026-09-19）

本次仅调整文玄蓝 Boss 的技能范围及超能光束前摇，不改玩家掉落武器、玩家卷轴、其他 Boss、伤害、冷却、飞轮数量或追踪距离。

| 技能 | 调整前 | 调整后 |
|---|---|---|
| 超能光束 | 通用前摇缩放后的 6 tick；半径 3 格 | 固定 20 tick（1 秒），不再被通用加速缩短；半径 4 格 |
| 飞轮绞杀 | 半径 5.25 格 | 半径 6.25 格 |
| 蒸汽泄压旧路径 | 锥形径向距离 9.75 格 | 10.75 格，锥角不变 |
| 光束打击连招 | 半径 1.6 格 | 半径 2.6 格，原触发距离与光束长度不变 |
| 磁轨连射 | 半宽 1.2 格 | 半宽 2.2 格，长度仍为 18 格 |
| 超压震环 | 三次环半径 3 / 6 / 9 格 | 4 / 7 / 10 格，环厚不变 |
| 机轮卫星 | Boss 半径 6 格 | 7 格 |
| 高压贯星（含蒸汽泄压的铁魔法替换路径） | Boss 射线判定半径 1.2 格 | 2.2 格，射线长度不变 |
| 磁暴天仪 | Boss 半径 8 格 | 9 格 |

专属铁魔法以当前 Boss 倍率后的半径再加 1，避免误变成加 2 格；玩家施放这些卷轴时仍使用原始半径。飞轮投射物本身的尺寸及普通攻击距离不变。

超能光束的引导仍为 40 tick，脉冲间隔仍为 10 tick，长度仍为 40 格。预警、动作与状态机共用固定 20 tick。磁轨双轨预警、震环落点、铁魔法粒子与模型缩放同步到新范围。

验证入口：`node tools/verification/check_hidden_boss_cast.cjs`、`tools/manor-five/ManorFiveCombatRulesTest.java`、`tools/verification/manor_integration_gametest/BossCombatBalanceTests.java`。

## 本轮验证与交付状态

- 独立工程 `xuanlan-skill-radii-20260919-0836/project` 的标准 `build` 通过；11 项 `BossCombatBalanceTests` 全部通过，包含本次新增的 20 tick 状态机与范围/玩家隔离实测。
- `ManorRangeOpeningCheck` 通过 5095 项断言；前摇接线脚本通过；成品字节码确认超能光束 20 tick、半径 4，旧技能范围常量均正确。
- 较早的 151 项全量游戏测试有 9 项失败；其中本轮新增的射线测试已修正目标尺寸与血量基准并单独复验通过。其余 8 项涉及其他技能/并行功能，未在本任务内修复，也未宣称全量回归通过。
- 旧 `ManorFiveCombatRulesTest` 的 `minimum dodge window` 断言在未修改基线同样失败；该旧阈值不作为本次玄蓝验证依据。独立范围检查入口为 `ManorRangeOpeningCheck`。
- 本轮未部署：结束时仍存在游戏/测试进程，且其他线程又更新了 `XueliMaids.java`、`HiddenBossChallenge.java`。不要直接覆盖为此快照；等进程退出后重新汇总主工程构建，并使用 `tools/deploy_jar.ps1` 同步四个目标。
