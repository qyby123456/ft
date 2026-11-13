# 用户确认的介绍与提示

用户于 2026-09-17 亲自修改了模组介绍和提示，并要求后续记住并保留。以下以当前源码和用户截图为准。

- 模组名称：`食刻生动`（用户于 2026-09-18 确认改名，名称不附加英文。）
- 模组介绍：`为食物增加使用和检视动作`
- 作者：`紫菜`；许可证：`ARR`
- 图标：`logo.png`，`logoBlur=false`，沿用用户当前图标。
- 中文物品提示：`[当前食用键]食用  [当前检视键]检视`。按键文字由实际绑定动态获取；括号深灰、按键灰色、动作文字金色。保持一行，两项间两个空格。
- 英文提示文字：`Use` / `Inspect`。
- 中文按键名称：`食用动画` / `检视手中酒水`；类别：`农场物语：饮用与检视`。
- 英文按键名称：`Eating animation` / `Inspect held bottle`；类别：`FarmingTales: Drinking & Inspection`。

维护位置：`src/main/resources/META-INF/mods.toml`、`src/main/resources/assets/farmingtales_drink_anim/lang/zh_cn.json`、`en_us.json`，以及 `src/main/java/com/farmingtales/drinkanim/client/ClientEvents.java` 的 `tooltip` 方法。

后续制作、构建、升级或恢复历史资源时，保留这些用户文案、图标和提示样式。版本号可以按发布流程更新；不要因旧 JAR、备份、发布校验快照或生成脚本与当前文案不一致而将其回退。用户的新修改优先于此记录。截图中的 1.8.59 是当时版本，不是固定版本要求。
