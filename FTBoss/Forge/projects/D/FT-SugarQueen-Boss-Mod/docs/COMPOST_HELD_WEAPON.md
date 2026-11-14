# 答辩超人手持武器对齐（2026-09-19）

问题在实体的握持变换，而不是玩家物品模型：`BlockAndItemGeoLayer` 已把原点移到 `hand_r` 的骨骼枢轴，但旧渲染器直接使用玩家的 `THIRD_PERSON_RIGHT_HAND` 显示变换。大号手掌与玩家手臂坐标不同，握柄偏在手腕外，锤身还竖向伸入前臂。

- 仅 Boss 持有 `ftboss:compost_crown` 时使用专用 `CompostWeaponPose`；仍读取实际主手装备，不凭空生成武器。
- 使用 `ItemDisplayContext.NONE`，以真实 `grip_core` 中心 `[8,-0.5,8]` 为握点，补偿物品渲染器的 `[8,8,8]` 居中平移。
- 相对 `hand_r` 的握点偏移为 `[0,-1.8,-0.5]` 模型单位（16 单位 = 1 格），缩放保留 0.63；柄轴沿手掌前方，不再沿前臂竖起。
- 保持手掌→前臂→手臂的原骨骼链，现有待机、行走、抬手、浮空、变身动画照常带动武器；两个阶段的手骨骼一致。
- 不修改玩家第一/第三人称、背包、掉落物的模型显示，不修改武器伤害、掉落或 Boss 机制。

## 验证

- `checkCompostWeaponPose` 已接入 Gradle `check`：调用正式 Java 握持方法和 `PoseStack`，核对真实模型柄心、原版物品居中补偿、朝向、两阶段手掌范围、73 组父级旋转与非均匀缩放、矩阵恢复；旧玩家变换作为偏移反例。
- `node tools/verification/check_compost_weapon.cjs`：装备过滤、手骨架、专用握姿接线、模型与原掉落来源。
- `node tools/model3d/preview_compost_grip.cjs build/verification/compost-weapon/pose.json <输出目录>`：直接使用 Java 测试导出的真实变换矩阵，生成待机、行走、抬手、二阶段浮空、变身前后对照图；左红为旧版，右绿为修正版。
- 预览使用现有离线 GeckoLib 坐标约定和贴图栅格化，不包含游戏内光照/发光层，不替代客户端实测。
