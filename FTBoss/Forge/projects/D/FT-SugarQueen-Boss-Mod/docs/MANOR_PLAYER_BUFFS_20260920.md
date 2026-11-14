# 庄园玩家增益不再受战场限制

2026-09-20 按用户要求取消庄园 Boss 战对玩家 buff 的统一限制。

后续新增的「不可能」难度单独恢复旧食物增益清除和止心术/橡肤禁令；普通难度仍按本文件放行。详见 `MANOR_IMPOSSIBLE_DIFFICULTY.md`。

- 移除战场每 10 tick 的食物增益清除入口及其禁用名单/清除实现。饱腹代偿（`kaleidoscope_cookery:satiated_shield`）、滋养（`farmersdelight:nourishment`）、饱腹（`farm_and_charm:satiation`）不再因身处庄园而被清除，也不会触发禁食训话或铁砧音效。
- 玩家获得效果的等级、持续时间、续期及无限时长均交给原效果来源处理，不新增庄园上限。
- 同步解除两个增益法术「止心术」和「橡肤」的庄园施法禁令；黑洞不是玩家 buff，本次保留其原有庄园禁令。
- 不改变蛋蛋、紫菜等 Boss 技能自身的抽取/驱散，不改变 Boss 复制增益、战场祝福或动态伤害机制。
- 柠檬猫自身防止无法击杀的增益过滤继续保留，与玩家限制彻底解耦。
- 已被旧版本移除的效果无法凭空恢复，需要按原途径重新获得。

## 验证

`ManorPlayerBuffGameTests` 通过 Forge 实体、效果注册表、实际战场循环和施法事件总线验证：

- 连续跨过四次旧清除周期，三种食物增益仍保留等级和时长；再次施加更高等级、无限时长仍保留。
- 止心术、橡肤在战场内可正常通过施法事件并保留效果；黑洞在场内仍受限，场外不受限。
- 柠檬猫仍清除自己的危险增益，不影响玩家同名效果或普通 Boss 增益。

食物模组未加载的精简测试环境使用测试专用的同注册名有益效果占位，验证的是庄园不再清除这些 ID，不替代完整食物模组的功能验收。测试类及占位注册不打入正式 jar。

专项运行入口：`tools/verification/manor_player_buff_gametest.gradle`；既有柠檬猫回归：`node tools/verification/check_lemon_immune_buff.cjs`。

旧版 `docs/boss_reference/`、`docs/all_boss_reference/` 和审计记录中的禁食/增益禁术描述是历史快照，以本文件和当前源码为准。

## 交付记录

- 2026-09-20：干净构建成功；合入并行线程最新源码后再次构建及运行上述 3 项 Forge GameTest，全部通过。柠檬猫既有源码回归通过。
- 成品反汇编确认不再包含食物禁用名单和清除实现，战场循环无清除调用；增益法术放行，黑洞及柠檬猫保护保留，测试占位效果未打入成品。
- 发布前核对 1381 个源码、资源及构建输入哈希与共享工程一致，经 `deploy_jar.ps1` 同步桌面、桌面成品、OneDrive 成品和 152beta3，四处 SHA256 一致；旧实例 `FarmingTales Forge/mods` 未改动。
- 成品 SHA256：`4112A0A85E7CA349E88D1815C67D2CBBFDC0414258A2445FA4F3243C4FAE8E26`。
- 日志保存在 `FarmingTales Forge/_work/manor-player-buffs-20260920/`：`build-tests.log`、`build-final.log`、`verified-artifact.json`、`verified-source-manifest.json`、`deployment.log`。
- 未启动完整客户端或宣称全部战斗回归通过；本轮测试集中于玩家增益放行及保留规则。

## 2026-09-24 部分回滚：止心术与饱腹代偿重新禁用

用户要求「禁用部分铁魔法」并点名**止心术**、**饱腹代偿**。本文记录的 2026-09-20「普通难度放行」
被**部分**收回，只收回这两样：

| 项目 | 2026-09-20 起（本次之前） | 2026-09-24 起 |
|---|---|---|
| 止心术 `irons_spellbooks:heartstop` | 普通难度放行，仅终结难度拦 | **两档难度都拦**（进 `ManorBan.BANNED_SPELLS`，与黑洞同级） |
| 橡肤 `irons_spellbooks:oakskin` | 普通难度放行，仅终结难度拦 | **不变**（用户没点名，仍只在终结难度拦） |
| 黑洞 `black_hole` | 两档难度都拦 | 不变 |
| 饱腹代偿 `kaleidoscope_cookery:satiated_shield` | 普通难度不清，仅终结难度清 | **两档难度都清**（进新增的 `ManorImpossibleRules.SHARED_BANNED_FOOD_BUFFS`） |
| 滋养 `farmersdelight:nourishment` | 普通难度不清 | **不变**（仍只在终结难度清） |
| 饱腹 `farm_and_charm:satiation` | 普通难度不清 | **不变**（仍只在终结难度清） |

细节：

- `purgeFoodBuffs` 的判据从「只有终结难度」放宽成「终结难度 **或** 正在庄园里打」
  （`if (!impossible && !ManorFight.isPlayerFighting(player)) return 0;`），场外玩家不受影响。
- 训话文案按难度取 label：普通难度是「普通难度禁用的食物增益（N 项）」。
- 拦截机制没变：仍是取消 Iron's Spells 的 `SpellPreCastEvent` + 训话；
  食物增益仍是每 10 tick 清一次。
- 回归：`tools/verification/check_manor_spell_bans.cjs`；
  GameTest 旧断言同步更新（见 `ManorImpossibleGameTests` 与 `ManorPlayerBuffGameTests`）。
