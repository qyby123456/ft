# Benger 模型重制交付

2026-09-16：根据用户的黑白边牧头像重做 Boss 本体。已完成源码接入、离线预览、模型与 UV 专项检查、动画兼容检查、Gradle 离线构建及安全部署。

## 文件

- `Benger_Reference_Rework.bbmodel`：可编辑的 Blockbench 工程，包含贴图与 9 段动画。
- `benger.geo.json`、`benger.png`、`benger.animation.json`：正式游戏资源。
- `benger-preview.png`：全身、面部、正侧视图。
- `benger-poses.png`：九种动画姿势的离线采样。
- `validation.json`、`uv-layout.json`：校验结果与逐面 UV 清单。
- 桌面交付目录中的 `qys-bosses.jar`：与游戏实例部署版本一致。

## 检查结果

- 27 骨骼，122 方块，732 个独立面画区；画区及一像素外扩全部不透明、无重叠。
- 原有 9 段动画的名称、长度、循环标记和关键帧数值保持一致。177 条轨道的序列化时间键均已升序。
- 211 个动画采样点数值有限，四肢与尾链的相邻骨骼 AABB 无超过 0.5 模型单位的间隙。
- 生成器重复生成的几何、贴图、动画与 Blockbench 工程四项哈希一致。
- Gradle `build --offline` 成功；JAR 内三个 Benger 资源逐字节匹配源码。
- `tools/deploy_jar.ps1 -NoBuild` 在确认 Minecraft 已退出后完成部署，四处成品哈希一致，Lightspeed 缓存已清理。
- 共享工程随后有其他构建更新整体 JAR；交付前再次核验游戏实例与桌面交付 JAR，三个 Benger 资源仍与本次源码逐字节一致。整包哈希会随其他内容更新，以 `validation.json` 中的 Benger 资源指纹为准。

## 已知边界

- 通用动画检查器仍提示原有 idle 的四条小腿轨道只有一个恒定关键帧；未改变其数值与动作。
- 全资源扫描器对原有 `textures/entity/easter/xueli.png` 报解码告警。已确认它在旧、新 JAR 中字节完全相同，`@napi-rs/canvas` 可以正常解码为 64×64；本次未修改该资源，也不据此声称它已经修复。
- 图片是离线模型预览，不是游戏截图；尚未完成进入世界后的实际战斗验收。下次启动客户端加载新外观。

旧版本保留于源码工程 `_backup/benger_reference_20260916/`。
