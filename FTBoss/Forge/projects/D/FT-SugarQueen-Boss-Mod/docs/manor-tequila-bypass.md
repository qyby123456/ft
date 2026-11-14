# 庄园 Boss 与龙舌兰限伤

- 庄园 Boss 本体、分身，以及伤害来源能追溯到它们的弹体、法术和召唤物，无视 `kaleidoscope_world_liquor:tequila` 的单次伤害上限。
- 无实体来源的庄园技能通过现有 `ManorMercy.withOwner` 伤害上下文识别；不会把战场内所有环境伤害都视为 Boss 攻击。
- 龙舌兰 Buff 不会被移除、重加或缩短。普通生物、玩家、非庄园 Boss 和环境伤害仍执行原有限伤。
- 仅跳过世界名酒 `DamageEvents.onLivingHurt` 中的龙舌兰限伤，不恢复或覆盖其他监听器的伤害结果；护甲、抗性提升、吸收生命和伤害取消照常生效。
- 世界名酒是可选兼容目标；未安装时 `@Pseudo` Mixin 不应用，不引入运行时硬依赖。

专项 GameTest：`ManorTequilaGameTests`。安装和未安装世界名酒两种环境都需要验证；有模组时通过其实际事件监听器和真实伤害流程验证，而不是模拟限伤公式代替集成测试。
