# 原版卷轴与单一满阶法术

2026-09-18，按用户最新要求，全部70个新增铁魔法法术统一调整：非庄园37个、庄园33个（包含新追加的11个第三法术）。

## 当前规则

- 物品仍为 `irons_spellbooks:scroll`，直接使用铁魔法原版卷轴模型与学派贴图。BossScrollModel 已移除；旧专属卷轴模型/PNG 不参与资源打包。独立 HUD 法术图标、世界法术模型与光效保留。
- 每门法术只有一个满阶形态。原生 API 的最小/最大等级均为1，固定传奇品质；这个唯一等级拥有完整的原 V 级效果，不是降回原 I 级。创造栏每门法术一张，不再出现五档版本。
- Boss 合法击杀奖励其全部已启用法术，每种一张满阶卷轴。保持原有归属、分身、复活取消、掉落规则和防重复保护。
- 原生卷轴锻造台使用纸、传奇墨水和学派媒介直接制作满阶卷轴；抄写台、法术书和卷轴直接施放继续有效；奥术铁砧不再产生升级结果。
- 既有卷轴及法术书保留原 ID/NBT，不改玩家存档；即使旧 NBT 记为1至5级，也统一执行满阶数值。原生界面可能保留旧物品的历史等级文字，但不再产生数值或升级差异。
- 原生法术启用、冷却、制作许可、法力/法强倍率和装备学派加成继续有效。等级上限与品质由新增法术固定，旧配置不能重新打开等级成长；不改铁魔法自带法术。

## 数值约定

`FullPowerBossSpell` 将原第5级的基础法力与法强固定到唯一等级，并把逐级增长值设为0。非庄园基础魔力为规则表 mana+40；攻击威力为 damage+16，治疗/护盾威力为 damage+4。庄园基础魔力为 mana+32；攻击威力为 damage+8，辅助法术保留其独立满阶计算。

BossSpellRules 与 ManorSpellRules 的效果等级固定为5，因此连锁目标数、治疗、护盾、状态强度和持续时间同样保持满阶，不只是修改显示伤害。冷却、咏唱、命中形状和安全判定不因本次调整而改动。

## 验证

- `node tools/verification/check_boss_iron_spells.cjs`：37门非庄园注册、素材、接线与规则断言。
- `node tools/verification/check_manor_iron_spells.cjs`：33门庄园 ID、图标、语言、掉落与施法保护。
- `gradle clean build --offline`：干净构建、规则/模型/原生光效检查及发布 jar 一致性验证。
- `gradle -I tools/verification/manor_integration_gametest.gradle runGameTestServer --offline`：原生 Forge/铁魔法工作台、满阶掉落、拒绝升级、伤害/治疗与安全测试；FullPowerSpellTests 专门遍历70门法术，验证任意旧等级输入的威力/魔力一致及旧卷轴保存往返兼容。

测试只运行在隔离工程中，不向玩家存档写入任何测试实体或道具。服务端验证不代替整合包客户端画面验收。

## 本次交付

2026-09-18：干净构建成功；97项真实 Forge GameTest 全部通过（包含70门法术数值与旧卷轴兼容、满阶掉落、原生锻造/抄录和拒绝升级）。非庄园规则62921项、资源接线828项断言通过，庄园资源/语言检查通过；构建自带的威胁表、庄园规则、70种法术几何和原生光效检查均通过。

发布 jar 为42,151,390字节，SHA256：`FC42E0B15C09A7099400687F50C89523620449CEAF563F5FF3693D5106405DEE`。包内不含 BossScrollModel 或 boss_scrolls 专属美术，70张 HUD 法术图标完整保留。1251个源码、资源及构建输入哈希与共享工程一致，保留其他线程最新修改。

通过 `tools/deploy_jar.ps1` 同步至桌面 `qys-bosses.jar`、桌面 `FT经济-波动mod成品/qys-bosses.jar` 和 `FarmingTales_ForgeⅡ v1.5.2_beta3/mods/qys-bosses.jar`，三处哈希一致。旧 `FarmingTales Forge/mods/qys-bosses.jar` 未覆盖。

回退备份与完整日志位于旧实例工作目录 `_work/native-full-power-spells-20260918/`：`backup/`、`build-final.log`、`gametest-final.log` 与 `verified-source-manifest.json`。未运行整合包客户端视觉验收。
