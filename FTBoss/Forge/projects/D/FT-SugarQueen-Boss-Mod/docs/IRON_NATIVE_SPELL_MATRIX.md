# 72 门法术原生表现适配矩阵

由 `check_iron_native_spell_wiring.cjs` 从当前注册表与独占路由生成。12 门 Iron 原生适配（4 模型、8 粒子），23 门扩展主题模型，28 门接入 Iron 原生光线（两组可重叠），13 门本模组独立实体模型；各类按实际接线逐门标注。所有 CHARGE/BEAM 留在现有系统，延迟起爆仅接管 PULSE。

| 法术 | ID | 归属 | 接管阶段 | 方案 |
|---|---|---|---|---|
| 糖霜加冕 | `crystal_coronation` | Iron 原生立体模型 | RELEASE | ICE_CROWN |
| 血茶绽放 | `camellia_blood_bloom` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 陈酿回旋 | `vintage_vortex` | 保留现有主题 | — | — |
| 电容过载 | `capacitor_overload` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 彩秽孢潮 | `prismatic_blight` | Iron 原生粒子 | PULSE | SPORES |
| 金果裁决 | `gilded_verdict` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 潮庭敕令 | `tidal_edict` | 保留现有主题 | — | — |
| 霜晶贯枪 | `frostglass_lance` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 山茶花庭 | `camellia_sanctuary` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 爆桶烈酿 | `cask_rupture` | Iron 原生粒子 | PULSE | CASK_BLAST |
| 斥力放电 | `repulsor_discharge` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 毒性收割 | `virulent_harvest` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 金果圣佑 | `orchard_aegis` | Iron 原生立体模型 | RELEASE | GOLD_SHIELD |
| 深渊涌柱 | `abyssal_geyser` | 保留现有主题 | — | — |
| 偶像连星 | `effigy_volley` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 灵偶守御 | `effigy_ward` | Iron 原生立体模型 | RELEASE | IVORY_SHIELD |
| 暮烛收割 | `candle_reap` | Iron 原生粒子 | RELEASE | BLOOD_REAP |
| 暮烛囚笼 | `candle_prison` | 本模组独立立体模型 | CHARGE / RELEASE / PULSE | SOLID_DEPTH_MESH |
| 秘银齐射 | `arcane_fusillade` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 破阵榴弹 | `breach_shell` | Iron 原生粒子 | PULSE | SHELL_BLAST |
| 黑刃裂甲 | `rending_cleave` | 本模组独立立体模型 | CHARGE / RELEASE / PULSE | SOLID_DEPTH_MESH |
| 赤誓战意 | `crimson_oath` | 本模组独立立体模型 | CHARGE / RELEASE / PULSE | SOLID_DEPTH_MESH |
| 炽锅翻焰 | `searing_pan` | 本模组独立立体模型 | CHARGE / RELEASE / PULSE | SOLID_DEPTH_MESH |
| 炉心暖宴 | `hearth_feast` | Iron 原生粒子 | PULSE | HEARTH |
| 四海奔流 | `four_tides` | 本模组独立立体模型 | CHARGE / RELEASE / PULSE | SOLID_DEPTH_MESH |
| 百药甘霖 | `elixir_rain` | Iron 原生粒子 | PULSE | MEDICINAL_RAIN |
| 机轮卫星 | `orbital_gears` | 本模组独立立体模型 | CHARGE / RELEASE / PULSE | SOLID_DEPTH_MESH |
| 高压贯星 | `pressure_lance` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 影缚镇印 | `shadow_brand` | 本模组独立立体模型 | CHARGE / RELEASE / PULSE | SOLID_DEPTH_MESH |
| 虚空回响 | `void_echo` | 本模组独立立体模型 | CHARGE / RELEASE / PULSE | SOLID_DEPTH_MESH |
| 耀环斥退 | `luminous_rebuke` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 圣环庇所 | `halo_sanctuary` | 本模组独立立体模型 | CHARGE / RELEASE / PULSE | SOLID_DEPTH_MESH |
| 千羽散华 | `feather_fan` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 乘羽轻身 | `wingstep` | Iron 原生立体模型 | RELEASE | WINGS |
| 凝霜飞针 | `frost_needles` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 白夜禁疗 | `whiteout_edict` | Iron 原生粒子 | PULSE | SNOW |
| 糖晶雹雨 | `sugar_hail` | 扩展主题模型 | — | — |
| 霜棱镜甲 | `glacial_prism` | 扩展主题模型 | — | — |
| 王冠碎冰 | `royal_shatter` | 扩展主题模型 | — | — |
| 荆棘巡行 | `briar_procession` | 扩展主题模型 | — | — |
| 绯红花粉 | `scarlet_pollen` | 扩展主题模型 | — | — |
| 山茶再生 | `camellia_rebirth` | 扩展模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 血肉绽放 | `flesh_bloom` | 扩展模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 血线缝合 | `sanguine_suture` | 扩展模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 烈酿吐息 | `cinder_breath` | 扩展主题模型 | — | — |
| 醉勇祝酒 | `intoxicating_toast` | 扩展主题模型 | — | — |
| 陈酿雷区 | `vintage_minefield` | 扩展主题模型 | — | — |
| 离子轨道炮 | `ion_rail` | 扩展模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 静电囚网 | `static_lattice` | 扩展模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 紧急重启 | `emergency_reboot` | 扩展模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 孢囊喷发 | `spore_eruption` | 扩展主题模型 | — | — |
| 腐朽挽歌 | `rotting_requiem` | 扩展主题模型 | — | — |
| 活性腐殖 | `living_mulch` | 扩展主题模型 | — | — |
| 日轮果园 | `solar_orchard` | 扩展主题模型 | — | — |
| 审判种子 | `judgment_seed` | 扩展主题模型 | — | — |
| 金枝收获 | `golden_harvest` | 扩展模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 暗流牵引 | `undertow_snare` | 扩展主题模型 | — | — |
| 深海泡甲 | `abyssal_carapace` | 扩展主题模型 | — | — |
| 裂潮长廊 | `riptide_corridor` | 扩展主题模型 | — | — |
| 星偶巡礼 | `effigy_procession` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 烛月蚀魂 | `candle_eclipse` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 棱晶炮阵 | `prism_battery` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 赤刃裁决 | `blood_verdict` | 本模组独立立体模型 | CHARGE / RELEASE / PULSE | SOLID_DEPTH_MESH |
| 柠日焰轮 | `citrus_nova` | 本模组独立立体模型 | CHARGE / RELEASE / PULSE | SOLID_DEPTH_MESH |
| 碧潮莲华 | `tidal_lotus` | Iron 原生粒子 | RELEASE | HEAL |
| 磁暴天仪 | `magnetic_storm` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 永夜交锋 | `nightfall_blades` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 炽羽天降 | `seraph_descent` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 霜刻时钟 | `winter_clock` | 本模组独立立体模型 | CHARGE / RELEASE / PULSE | SOLID_DEPTH_MESH |
| 归羽结界 | `feather_refuge` | 本模组独立立体模型 | CHARGE / RELEASE / PULSE | SOLID_DEPTH_MESH |
| 星轨十字 | `star_cross` | 既有主题模型 + Iron 原生光线 | BEAM / RELEASE / PULSE 点缀 | — |
| 绯星连奏 | `rose_starfall` | 本模组专属坠星激光模型 | BEAM 三层立体束 + RELEASE / PULSE 点缀 | SOLID_DEPTH_MESH + TRANSLUCENT_BEAM |
