# Boss 咏唱时长 +1/3（2026-09-20 v177）

用户原话：「boss施法时间变长1/3倍」。

## 口径

- 只改**咏唱（施法）时长**：冷却、施法间隔、伤害、蓝耗、命中判定一律不动；
- 旧值：Boss 实际咏唱 = 法术原始时长 × 1/2（旧 `halfCast` = `(t + 1) / 2`，向上取整）；
- 新值：Boss 实际咏唱 = 法术原始时长 × 2/3（`BossSpellTuning.bossCast` = `max(1, ceil(t × BOSS_CAST_MULT))`，`BOSS_CAST_MULT` = 2.0 / 3.0）—— 正好是旧值的 4/3，即「比原来长 1/3」；
- 判定口径是「施法者是不是 Boss」：玩家用同一门卷轴 / 法术书施法仍按法术自身时长，不受影响。

## 覆盖的三条路径

| 路径 | 换算前 | 换算后 | 出处 |
|---|---|---|---|
| 常规 Boss 招牌法术（37 门） | 32 tick（`CAST_TICKS`）；`FROST_LANCE` / `DEFECT_PULSE` 为 20 | 22 tick；那两个特例 14 tick | `integ/spells/BossSignatureSpell.java:49-52`、`util/BossSpellRules.java:9,138` |
| 庄园 Boss 招牌法术（33 门） | 20 / 24 / 28 / 32 / 36 tick | 14 / 16 / 19 / 22 / 24 tick | `integ/spells/ManorSignatureSpell.java:45-48`、`util/ManorSpellRules.java:14-47` |
| 常规 Boss 的原生铁魔法 `CastType.LONG` 法术 | 原时长 | 原时长 × 2/3 | `integ/spells/BossSpellTuning.java:63-70`（`fasterLegacyCast`） |

- 换算结果仍 clamp 到 1 ~ 200 tick（`integ/spells/ManorSpellCombat.java:79`）；
- 常规招牌法术的取消闸门是 60 tick（`integ/spells/BossSpellCombat.java:57`），最长换算值 24 tick，窗口充裕；
- 瞬发（`CastType.INSTANT`）法术本来就瞬发，不受影响。

## 未覆盖

- 庄园五技能、朵拉狙击等**技能前摇**走各自的 `HiddenBossCastRules.quicker` / 技能时长，不是铁魔法咏唱，本次未动；
- 柠檬猫献祭齐射（`LemonSpellBarrage`）里每个法术的时长自动跟随同一换算，因为它读的是 `spell.getEffectiveCastTime`，无需单独改。

## 回归

- `tools/verification/check_boss_cast_time.cjs`（`node tools/verify_project.cjs boss-cast-time`）：常量与公式、三条路径、旧 `halfCast` 无残留、逐 tick 数值、60 / 200 tick 窗口；
- `tools/verification/manor_integration_gametest/BossCombatBalanceTests.java` 的 `regularCastSpeedAndIndependentDamage` 已同步改用 `bossCast`。
