# 折耳根：专属黑暗滤镜与铁魔法／近战并行

2026-09-19，Forge 1.20.1 / Iron's Spells 3.16.3。

## 黑暗滤镜

- 触发条件是玩家身上的 `ftboss:shadow_blind`（暗影笼罩），沿用折耳根真实近战、法术命中及交战维持逻辑，不是靠检测附近名字或距离临时猜测。
- 两张独立1024×576透明贴图：`textures/gui/overlays/zheergen_dark_frame.png` 是暗紫蚀边与幽影薄雾，`zheergen_dark_sigils.png` 是渐细裂纹、侧翼裂瞳和破碎符文。
- 中央约48%宽×44%高没有装饰遮挡，全屏只加约25%的暗紫底色，不把准星和目标直接涂黑。外围渐暗，纹理和符文以5秒周期轻微呼吸，不闪屏、不强制旋转镜头。
- 切入／退出各8tick（0.4秒），强度在客户端tick推进，与帧率无关；重复补挂效果不会重启动画。
- 使用Forge原生 `RegisterGuiOverlaysEvent` 注册在VIGNETTE之后、HUD之前，准星、快捷栏、生命值后画。滤镜独立于原版雾效，光影接管雾效不会绕过这两张GUI贴图。
- `BlindnessOverlay` 遇到暗影笼罩或本滤镜淡出时跳过旧的纯黑矩形遮罩，避免两套遮罩叠黑。普通来源的失明／黑暗仍保留原逻辑。
- 玩家死亡、重生换实体、退出／切换世界和旁观状态清理强度；暂停时不推进动画，远程观察其他实体时不画玩家滤镜。没有增加网络包，沿用原生状态效果同步。
- 可见之戒清除原版失明／黑暗后，暗影笼罩仍触发专属贴图，保留既有规则；不调整其持续时间、侵蚀伤害或其他Boss效果。

## 铁魔法与近战

当前主工程在本次开始前已具备 `ManorSpellCombat` 并行施法通道。本次复用并专门回归折耳根，不另加第二套AI或重复施法伤害：

- 影缚镇印、虚空回响、永夜交锋均使用现有Iron咏唱队列，遵守现有双法术组合、等级、冷却与取消条件。
- 开始法术使用 `playParallelSpell`，不写入阻断近战的 `castTicks`，不锁寻路；折耳根保持近战型的追击和攻击AI。
- 正在挥剑时可以起法术，吟唱期间仍可挥剑命中；近战动画覆盖展示姿势，但不清除Iron队列。
- 法术完成或失去目标取消时只清理法术自身状态，不清除正在播放的挥剑动作。死亡、目标丢失、距离／视线变化等原有安全检查仍保留。
- 普通武器技能自身的蓄力／承诺动作仍可限制近战，不把所有动作锁一概删除，也不修改其他Boss战斗参数。

## 复现

```text
node tools/model3d/gen_zheergen_darkness.cjs <staging-assets-directory>
node tools/verification/check_zheergen_darkness.cjs <regenerated-assets-directory>
node tools/model3d/preview_zheergen_darkness.cjs <existing-screenshot.png> <preview-directory>
gradle build runGameTestServer -I tools/verification/zheergen_gametest.gradle --offline
```

`ShadowDarknessCheck.java` 可用JDK17与 `ShadowDarknessRules.java` 独立编译执行，检查淡入淡出、周期、边界、NaN与亮度限制。

专属GameTest检查三种法术吟唱中近战命中、真实近战AI的启动／继续、挥剑中起法术、法术完成不清挥剑、目标丢失取消、独立冷却，以及真实Boss近战／Iron命中施加专属滤镜效果。测试代码不进入发布jar。测试夹具保留Boss原生目标AI，单独准备原版20tick目标检测间隔和测试玩家出生保护，不修改生产战斗规则。

2026-09-19验证：7项Forge服务端GameTest全部通过，淡入淡出规则8034项断言通过，确定性贴图再生成／透明区域校验通过，工程14组检查通过；正式jar包含客户端注册类与两张贴图，不包含测试类和测试场景。

预览目录含16:9、4:3、21:9、灰底与半强度的离线合成图；这是将实际贴图合成到旧截图上，不是本轮游戏实拍，因此截图原有HUD也被预览程序压暗，游戏内HUD绘制层在滤镜之后。完整整合包客户端光影兼容和动态观感仍需游戏内验收。
