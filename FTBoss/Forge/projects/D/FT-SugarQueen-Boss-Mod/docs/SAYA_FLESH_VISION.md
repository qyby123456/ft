# 沙耶荆棘王冠：血肉视界

沙耶的掉落装备 `ftboss:thorn_crown` 现在使用专属血肉遮挡，替换原先单张红褐色渐变滤镜。不增加新物品，不改王冠掉落、护甲、耐久、反伤或修复配方。

## 视觉与触发

- 两张独立的 1024×576 RGBA 纹理：肉膜层包含酒红色褶皱、粉红色边缘和五条筋膜触须；血管层提供分叉毛细血管的轻微搏动。
- 肉膜主要占据画面四周。中央 50% 宽 × 44% 高的区域在两张纹理中完全透明；中央仅覆盖约 4.7% 不透明度的淡血色。
- 每 36 tick（1.8 秒）一次轻微双拍搏动。肉膜整体不透明度仅在 0.93–0.98 间变化，不闪屏、不缩放、不摇晃摄像机。
- 保留原有条件：本地存活、非旁观玩家戴在头部装备槽，且摄像机属于本人时显示。背包内、手持、别人佩戴均不触发；摘下、死亡、旁观或切换到其他实体的摄像机立即停止绘制，无残留状态。
- 保留原效果的第一/第三人称行为，不新加视角限制。画在原版暗角之后、准星和快捷栏之前，不与失明/黑暗共用纹理或状态，也不加载全屏后处理着色器。
- 每帧仅一个淡色矩形和两个纹理四边形；纹理只在资源加载时读取，无逐帧生成、上传和联网数据包。

## 资源与实现

- 肉膜：`assets/ftboss/textures/gui/overlays/saya_flesh_frame.png`
- 血管：`assets/ftboss/textures/gui/overlays/saya_flesh_veins.png`
- 渲染：`src/main/java/net/farmingtales/ftboss/client/ThornCrownOverlay.java`
- 搏动规则：`src/main/java/net/farmingtales/ftboss/util/SayaFleshVisionRules.java`
- 生成器：`node tools/model3d/gen_saya_flesh_vision.cjs <assets/ftboss 输出目录>`，只输出上述两张纹理，固定种子可复现。
- 原王冠生成器也已接入新生成器。旧 `textures/misc/thorn_crown_vision.png` 不再生成、引用或打包，构建必须使用 `clean build` 清理旧输出。
- 中英文提示更新为血肉视界，其他装备效果和提示不变。

## 验证与预览

`node tools/verification/check_saya_flesh_vision.cjs <临时输出目录>` 检查生成一致性、中央透明区域、边缘覆盖、客户端注册、佩戴条件、资源引用、语言提示以及 GUI 刷新后的混合状态。

编译并运行 `tools/verification/SayaFleshVisionCheck.java` 与规则类，检查周期性、平滑度、透明度范围和异常时钟输入。

`node tools/model3d/preview_saya_flesh_vision.cjs <游戏截图.png> <输出目录>` 生成 16:9、4:3、21:9 和浅色背景合成预览。预览是纹理叠加在旧截图上的离线合成，不是新版实机截图；旧截图里的 HUD 也属于背景像素，预览会盖到它们，不代表实际游戏中 HUD 的绘制层级。

更新模组后完全重启 beta3 实例；旧王冠无需重新获取。实机应检查佩戴/摘下、死亡/旁观切换、正常 HUD 显示、光影开启和资源重载。构建及离线检查不能代替这些实机验收。
