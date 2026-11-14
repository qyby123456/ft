# 庄园铁魔法：13 门独立立体模型

2026-09-21，Forge 1.20.1 / Iron's Spells 'n Spellbooks 3.16.3。

## 范围

本轮只处理庄园 Boss 法术中**既没有接入 Iron 原生模型，也没有接入 Iron 原生粒子/光线**的法术。范围由代码自动反推：遍历 `ManorSpellRules.Kind`，排除 `IronNativeSpellRules` 和 `IronSpellLightRules` 已接管的法术，结果严格为 13 门：

- 暮烛囚笼 `candle_prison`
- 黑刃裂甲 `rending_cleave`
- 赤誓战意 `crimson_oath`
- 炽锅翻焰 `searing_pan`
- 四海奔流 `four_tides`
- 机轮卫星 `orbital_gears`
- 影缚镇印 `shadow_brand`
- 虚空回响 `void_echo`
- 圣环庇所 `halo_sanctuary`
- 赤刃裁决 `blood_verdict`
- 柠日焰轮 `citrus_nova`
- 霜刻时钟 `winter_clock`
- 归羽结界 `feather_refuge`

`candle_eclipse` 等法术虽然没有复用 Iron 实体模型，但已经使用原生 Siphon/Trace/Zap 等粒子，因此不在本批重复叠加主体模型。

## 模型设计

13 门均为独立程序化实体网格，不是同一个轮廓只换颜色：

- 囚笼：八支立体蜡烛、锁芯、链杆和紫晶火焰。
- 裂甲：三把带刃口、握柄和血槽的重型裂甲刀。
- 赤誓：三块誓约盾牌、双轴誓环和中央血晶。
- 翻焰：完整锅体、锅沿、把手、炉火与翻焰弧。
- 四海：四把带侧鳍、三叉尖和水晶枪头的潮汐戟。
- 机轮：三组十齿卫星齿轮、轴心、黄铜箍和底部主轮。
- 影缚：六根影钉、拘束链、中央镇印棱柱和地面齿印。
- 回响：双侧虚空钟、三轴回声环和暗晶共鸣核。
- 庇所：八柱穹顶、圣坛、悬浮圣晶和顶部金白环冠。
- 裁决：实体裁决巨剑、横护、双秤盘、锁链和地面放射刃。
- 焰轮：分瓣柑橘球、经纬镶线、双叶和十道日芒。
- 时钟：实体钟框、内齿轮、十二刻晶、双指针和六枚霜晶。
- 结界：十片带羽轴/羽枝的实体巨羽、收束金线和顶部冠环。

模型使用独立 `POSITION_COLOR` 深度材质批次，开启透明混合、深度测试并继续写入深度；模型主体保持设计尺寸，最后最多 3 tick 才以透明度平滑消失。原有法阵、危险范围、Iron 粒子与命中提示继续作为第二层效果，不改服务端判定。

## 接线与性能

- `ManorStandaloneSpellModels`：13 门模型真源、动画与材质配色。
- `ManorSpellModelRenderType`：双面、写深度且支持收招透明消散的模型批次。
- `SpellVisualRenderer`：每帧只取一次模型缓冲，先画模型再画原有透明法阵；沿用距离、视锥、实体 UUID、死亡/换维度清理。
- 全部/减少/最少粒子设置分别最多绘制 12/8/4 个独立模型，按距离优先；不会生成新实体或网络包。
- CHARGE / RELEASE / PULSE 都有模型，BEAM 仍由已有真实射线端点与 Iron 原生光线处理，避免模型误导命中路径。

## 验证

`checkManorStandaloneSpellModels` 自动确认：

1. 13 门集合恰好等于“庄园法术 - Iron 原生模型/粒子/光线”；新增法术漏建模会直接失败。
2. 三阶段、多帧网格有限、非退化、有界、材质不少于四种，13 个轮廓签名互不相同。
3. 模型缓冲每帧只获取/结束一次，材质支持透明消失并继续写深度，且受画质预算限制。
4. 输出 `build/verification/manor-standalone-spell-models/preview.png`；该图由游戏使用的同一份网格离线光栅化，不是游戏截图。

仅修改客户端表现；伤害、治疗、护盾、法力、冷却、AI、卷轴及网络协议均未改变。最终仍需在 Minecraft 客户端检查光影包下的深度与亮度。