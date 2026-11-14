# 铁魔法原生表现适配：线程分工

本线程只负责铁魔法 3.16.3 原生模型与粒子的适配，不重做线条、光束、法阵、程序化轮廓，不改伤害/施法/卷轴逻辑。

## 文件边界

- 新实现使用 `IronNativeSpell*` 独立文件和 `client/compat/irons/` 子目录。
- 不修改 `SpellVisualGeometry.java`、`BossExpandedSpellVisuals.java`、图集生成器或法术规则注册表。
- 共用渲染器只增加一个接收路由入口；原生适配接管指定阶段时清理旧效果，不同时播放两套主体。
- 不新增实体、不新增网络包、不复制 Iron 的贴图；Iron 未安装时走原有表现。

## 70 门法术的分工

### 本线程接管的 12 门

| ID | 原生资源方案 | 接管阶段 |
|---|---|---|
| crystal_coronation | Iron IceSpike 模型组成冰晶冠，分段生长 | RELEASE |
| candle_reap | Iron 血滴粒子，沿前方镰斩弧向内回收 | RELEASE |
| prismatic_blight | Iron 酸液/酸泡，低空孢子簇 | PULSE |
| tidal_lotus | Iron 净化粒子与暖色治愈浮点 | RELEASE |
| cask_rupture | Iron 爆炸帧动画、余烬与烟，仅实际爆炸 | PULSE |
| orchard_aegis | Iron Shield 模型，六片金色绕身护盾 | RELEASE |
| effigy_ward | Iron Shield 模型，四片象牙色守御盾 | RELEASE |
| breach_shell | Iron 爆炸帧动画与破片火星，仅实际起爆 | PULSE |
| hearth_feast | Iron 净化/余烬，四次实际治疗脉冲 | PULSE |
| elixir_rain | Iron 酸泡表现药露，自上而下散落 | PULSE |
| wingstep | Iron AngelWings 模型，背部羽翼展开/振翅 | RELEASE |
| whiteout_edict | Iron 雪花/雪尘，真实禁疗脉冲驱动 | PULSE |

吟唱 CHARGE、弹道 BEAM 仍由原有系统负责；延迟技能的 RELEASE 警戒造型保留，不提前播放爆炸。

### 另一线条/扩展模型线程拥有的 23 门（禁止接管）

`sugar_hail`, `glacial_prism`, `royal_shatter`, `briar_procession`, `scarlet_pollen`,
`camellia_rebirth`, `flesh_bloom`, `sanguine_suture`, `cinder_breath`, `intoxicating_toast`,
`vintage_minefield`, `ion_rail`, `static_lattice`, `emergency_reboot`, `spore_eruption`,
`rotting_requiem`, `living_mulch`, `solar_orchard`, `judgment_seed`, `golden_harvest`,
`undertow_snare`, `abyssal_carapace`, `riptide_corridor`。

其余 35 门保留现有独立主题造型，尤其光束、镰斩、齿轮、酒桶、灵偶以及庄园第三法术；不为了复用而强行套入不相符的原生实体。

## 已完成与验证

- `IronNativeSpellRules` 只选 12 门：4 门原生立体模型（冰刺晶冠、金色护盾、象牙护盾、羽翼）和 8 门原生粒子（血滴、酸液、治疗、爆炸、余烬、药露、雪花）。
- 所有 `CHARGE` / `BEAM` 仍由既有特效和另一线程的原生光线适配处理；本线程不接管其主体。爆桶与破阵榴弹只在真实 `PULSE` 爆发时接管，`RELEASE` 不提前播放爆炸。
- 与 `NATIVE_SPELL_LIGHTS.md` 的原生光线适配及另一线程的 23 门扩展模型集合没有同法术重叠；客户端入口按阶段独占，避免两套效果叠加。
- 模型复用 Iron 3.16.3 的 `IceSpikeModel`、`ShieldModel`、`AngelWingsModel` 和原版材质，护盾/羽翼运行时生成中性色材质并由法术颜色乘色，不复制 Iron 贴图到本模组。
- 适配仍无实体、无新网络包、无伤害逻辑；Iron 未加载时由 `ModList` 门控，保留原有自定义表现。
- 验证结果：70 门审计、12 门独占路由、23 门保留给另一线程、35 门维持现有主题；原生模型 `186732` 项断言、分工状态 `2833` 项断言、91 项 Forge GameTest 全部通过。
- 完整矩阵见 `docs/IRON_NATIVE_SPELL_MATRIX.md`；实际模型预览为 `build/verification/iron-native/preview`，这是离线模型光栅化，不冒充游戏截图。
