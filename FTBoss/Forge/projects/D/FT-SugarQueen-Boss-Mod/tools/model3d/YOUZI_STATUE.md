# 柚子石像模型

`youzi_statue_spec.cjs` 是石像的唯一规格。两个历史生成入口均调用它，避免重新生成时恢复旧的平面物品父链。

- 保留柚子白发、粉白服饰与 Alex 三像素手臂，身体六部件加六个皮肤外层。
- 手臂左右对称外展 22.5°，双腿留细缝；身体、衣服和头发使用不同厚度，避免接缝共面。
- 实体皮肤不修改。它的外层透明区被保存为 RGB 0–12 的不透明黑底；仅在石像副本的外层 UV 区域，将 RGB 最大值不超过 16 的像素还原为透明。基础皮肤像素保持完全一致。更换皮肤时应复核这项针对当前皮肤的处理。
- 采用 `minecraft:block/block` 父链和显式八槽 display，不使用 generated/handheld 父链。
- 第三人称左右手缩放 0.7，绕 X 旋转 90°，以腰部 `[8, 6.84375, 8]` 对齐物品局部原点。第一人称缩放 0.8、正面略侧向镜头；GUI、展示框和地面独立配置。
- 不修改伤害、技能、注册信息或 Boss 手臂动画。

从工程根目录运行：

```powershell
node tools/model3d/gen_statue_youzi.cjs
node tools/model3d/validate_youzi_statue.cjs
node tools/model3d/lint_java_model.cjs src/main/resources/assets/ftboss/models/item/statue_youzi.json
node tools/model3d/preview_youzi_statue.cjs src/main/resources/assets/ftboss/models/item/statue_youzi.json src/main/resources/assets/ftboss/textures/block/statue_youzi.png preview.png
```

生成器接受 `[assetsRoot] [outputRoot]`，可先生成到临时目录检查，仅产生本石像的三个资源。预览追加 `display` 时依次显示 GUI、第一人称、第三人称右手和左手的离线方向参考；省略玩家手臂、相机与手部固定平移，不能当成游戏内握持精度证明。实机需检查玩家左右手、柚子 Boss 静止与攻击姿态。

本次只改模型和贴图，资源打包从部署时最新 jar 合并这三个资源，并逐条核对其他条目不变；通过隔离目录中的 `tools/deploy_jar.ps1 -NoBuild` 部署，不覆盖其他会话的源码构建目录。所有原件和部署前 jar 保存在实例 `_work/youzi-statue-v2/backup/`。
