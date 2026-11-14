# 青衣三叉戟视检与朵拉掉落枪朝向（2026-09-20）

- `ftboss:qingyi_confluence_trident`（四象归一戟 · 青衣三叉戟）新增独立 `qingyi_confluence` 视检动作，沿用默认 N 键、左右手镜像和战斗即时打断，不需要物品 NBT 标记。动作共 4.2 秒，拉远观察两侧后回到原握姿。
- 朵拉的实际掉落物 `ftboss:dora_nova_launcher` 使用 `dora_smg_v1`。新冲锋枪枪口沿模型 +X，而旧地脉脉冲枪模型沿 -X；直接复用旧枪的 display 旋转会使新枪指向玩家背后。本次只纠正新冲锋枪四个手持视角的 yaw，生成器与运行资源同步修改。
- `ftboss:dora_pulse_gun` 和旧枪外观模型不改。新旧枪一起进入朝向回归，避免把朝向正确的旧枪反转。
- 不改两把武器的技能、伤害、射速、掉落、贴图、几何、地面或背包展示；不改三叉戟战斗动画。

## 检查

- `node tools/verification/check_weapon_inspect.cjs`：实际 Java 动作读取、插值、镜像、播放和中断，34 个物品、19 套动作。
- `node tools/verification/check_dora_weapon_models.cjs`：掉落/脉冲枪父模型和生成器逐字节往返。
- `gradle checkDoraWeaponPose`：使用 Minecraft `ItemTransform` 和 `PoseStack`，检查新旧枪第一/第三人称、左右手、抬头低头；反转 yaw 的旧错误姿态必须失败。已接入 `build` 的 `check`。
- `node tools/model3d/preview_weapon_inspect.cjs <motion-samples.json> <output> --qingyi-dora`：新增青衣与两把朵拉枪的离线取景检查，支持青衣的 box-UV。预览不包含玩家手臂、实时 GeckoLib 动画或整合包着色器，不能当作实机截图。

## 游戏内验收

完整退出客户端后用 `tools/deploy_jar.ps1` 部署，再重启 152beta3。手持青衣三叉戟按 N，检查双面展示与收回，攻击、施法、换槽应立即中止；将主手设为左手再测一次。持朵拉掉落冲锋枪，第一人称与 F5 抬枪时枪口应朝前；旧地脉脉冲枪保持原方向。
