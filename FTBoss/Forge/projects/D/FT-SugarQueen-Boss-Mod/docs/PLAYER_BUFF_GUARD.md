# 全局玩家增益拦截（已取消）

2026-09-29 起，按用户要求取消 FTBoss 对玩家增益的全局拦截。

- PlayerBuffGuard 仅作为兼容类保留，ENABLED = false，不再自动订阅 Forge 事件。
- 玩家获得的食物附魔、药水效果和其它正面效果不再被这套全局规则拦截或定期清除。
- 庄园终结难度的专属禁术/食物规则仍由 ManorImpossibleRules、ManorSpellBan 独立处理，未随本次全局开关改变。
- 回归：tools/verification/check_player_buff_guard.cjs。
