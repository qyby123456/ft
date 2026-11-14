# 武器第一人称正面朝向

打狗棒、河豚棒、污秽虹冠、充能机械爪的第一人称左右手展示正面五官、脸纹或能量面板。
只修改这四件模型的 `firstperson_righthand` / `firstperson_lefthand` 显示槽。
几何、UV、贴图、缩放、第三人称、GUI、掉落、展示框及战斗代码保持不变。
柚子小像使用已有的 155° 朝向，检查后无需修改；枪械、刀刃及无单面五官的模型不做统一翻转。

## 变换约定

- 原版相机看向 −Z。五官位于模型 −Z 面，直接沿用 −70° / −90° 的普通工具姿势会隐藏正面。
- 四件武器使用 145° 偏航，保留斜向持握；旋转时以原右手握柄中心为锚补偿平移。
- `ItemTransform.apply` 已对左手的 Y/Z 旋转与 X 平移取反，左右 JSON 使用同一参数，不再重复反号。
- 静止第一人称的 `applyItemArmAttackTransform` 中 +45° / −45° 互相抵消；基础平移为 `[±0.56, -0.52, -0.72]` 格。
- 第三人称另有手臂矩阵，不能跟着第一人称一起翻转。河豚棒原有第三人称握点校准保留。

## 维护与验证

源头分别为 `benger_weapon_spec.json`、`art/puffer_bat/Zicai_Puffer_Bat.bbmodel`、
`compost_crown/build_model.py`、`defect_spire/weapon.cjs`，均已同步。
实例中狗棒、河豚棒及污秽虹冠的可选外观包同步第一人称参数，避免覆盖内置修正。

```text
node tools/model3d/check_weapon_facing.cjs <修改前四件JSON的目录> <预览输出目录>
node tools/model3d/puffer_bat_hand.cjs
```

检查器复用 `cube_space.cjs` / `puffer_bat_hand.cjs` 的变换，按透视深度与纹理透明度计算可见像素。
覆盖左右手及 60° / 70° / 90° FOV，逐项检查双眼、鼻嘴、脸纹与能量板仍可见，
并断言除两个第一人称槽外所有模型数据不变、右手握柄中心不漂移。
离线预览不含玩家手臂、光影、视检/挥动动画或其他模组的额外变换，不能替代游戏内验收。
完整退出并重新启动客户端后，用已有武器验证；无需更换物品或重建存档。
