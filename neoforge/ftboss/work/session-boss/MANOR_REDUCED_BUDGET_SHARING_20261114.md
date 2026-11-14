## Stage34 原版合法战斗分摊矩阵，2026-11-14

麻雀后续完整玩法审查：11 个原方法经官方映射及明确 Holder API 适配后 6 个相同，tick、hasVisited(LivingEntity)、noteFightMember、beginStage、spawnSparrow 五个仍有真实玩法差异；缺少 SparrowMultiplayerSupport、SparrowAppearanceData、其 Entry、SparrowMultiplayerRules 四类。两个原生生命周期事件入口单独保留，不把跨版本事件签名误判为玩法缺失。严格原图对照按预期失败并保留全部差异和两个负控；审查不是多人功能已恢复的证明。证据 reports/sparrow-original-support-full-surface-audit-v2-20261114/receipt.json。

本段产品 JAR 未变，仍为 Stage33 d691bdb1a2b5f540d9bcd8149e6d04baa5c2e1df1a6c01578b4b32b75cfdb69c、NOT-INSTALLABLE。真实原注册 Youzi 蛋建立原版 fight/owner 并拒绝重复；自然支持缺席时由实际召唤者公开投降、40 自然 tick 清理后重新合法召唤，最多八场，同一新世界、不伪造身份/随机计划。原三次命中与 40 tick 冷却自然生成分身。18 场景通过：三次正例、Incoming 取消/减半、原生 Pre 置零、BeforeHealth 取消/减半、麻雀严格距离 48 排除及 47.5 内部、分身 64 范围外/保护排除、麻雀保护排除、真实攻击准备低血、麻雀/分身分摊致死、麻雀死后继续，实际召唤者原版公开投降及 40 自然 tick 清理。正例均为真实生存空手满蓄力 ServerPlayer.attack，保留修饰，仅暂时校准公开 base；无私有玩法、手动 tick、FakePlayer、fight/owner 写入、直接实体生成或 Boss/助手血量写入。

观察器两处预期错误纠正：原预算为 BeforeMitigation 最终值减原生护甲/魔法 reduction，即原 settle local 4，不等于 Incoming；原 absorbed-zero 仍可分摊，Pre 置零亦不能等同助手不扣血，与正数被最终事件置零的 guard 分开。8228 项纯原规则及完整图、两负控通过。实际 342 方法与组合图相同，18 类捕获、10 助手字节等于候选；原 hurt/六个完整分摊方法通过。实际类完整帧解析，模拟旧 StackMap 仅作图快照。测试玩家仍生存/非无敌，公开 native Pre 将其承伤限到 0.01，Boss AI 不停；这是分摊夹具，不是完整实战、生存难度或真实多人验收。助手低血由真实攻击加公开事件分次最多 250 伤害准备，遵守原 final 300 guard。

预算、吸收零值、真实玩家死亡、普通分身准备路径的严格运行失败保留，正常保存/退出 0/回收/输入不变；两次编译期失败未启动游戏或建世界；V7 保留六 tick 自然回血导致末次分身未死的严格失败，实际分摊扣血仍逐值吻合。最终致死夹具只临时把其他既存分身移出 64 范围后恢复，原新增分身仍参与；对主目标公开 Pre 设 250 上界以保留原 final-300 门槛，预算与原回血不改。关闭/未启动副本只在本地按哈希只读去重，路径及资源字节保留。模型/贴图/动画/音乐/音频、JAR 和世界不上传。正式实例哈希不变、D/正式实例只读、不部署；武器与 Session 暂停、Curios 不自动重试、唯一写入者及完整 Boss 目标保持。下一段恢复 SparrowMultiplayerSupport / SparrowAppearanceData 等完整原版助手、SparrowSupport 玩法及原生事件/存储适配。详见 work/session-boss/MANOR_REDUCED_BUDGET_SHARING_20261114.md 与 reports/manor-reduced-budget-sharing-closeout-20261114.json。


待办：

- Restore original Sparrow multiplayer/appearance-data helper closure and full gameplay methods with exact native event and SavedData adapters
- Real multiplayer participants/quota/appearance persistence and original action/visual fidelity
- Authenticated complete normal/impossible Boss fights, positive material/reward kill integration, death/cancel/reward
- Dimensions, persistence, restart and attended Curios new-JVM gate; no automatic V18 retry
- Final cumulative installable artifact and authorized formal deployment/rollback/archive; weapon and Session remain paused
