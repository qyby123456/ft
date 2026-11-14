# 炫彩答辩超人：Boss 本体重制 V5

2026-09-16。只改 `ftboss:compost_boss` 的几何、双阶段皮肤、发光蒙版与动作，不改 Java 战斗逻辑、掉落、血量、伤害、石像或其他 Boss。

## 当前外观

- 五层堆肥卷尖头、黑褐眼罩、琥珀瞳与歪笑；不再使用过高的九层塔状头部。
- 宽肩麻布胸甲、深色皮护腕、金属靴、稻草手脚、荧绿堆肥胸章。
- 四节酒红披风，带折痕和破损下摆；只放在背后，不加容易读成额外手臂的侧片。
- 常态褐色，二阶段六色头部与加强的局部发光蒙版。两套 geo 完全一致，运行时仍沿用 `CompostGeoModel` 的只换贴图机制。
- 135 个 cube、32 根骨骼、逐面 UV。512×512 图集保持约 1.82 px/模型单位的主体中位密度；尺寸大的图集不等于高密度纹理。
- 静态含装饰包围盒约 2.01×3.47×1.15 格，原判定 1.6×3.4 格不变；手、肩和苍蝇不额外扩大伤害判定。

## 动作与接口

手臂为上臂→前臂→手掌，腿为大腿→小腿→脚，躯干为髋→腰→胸；披风四节带相位延迟。保留 `hand_r` 土豆加农炮渲染挂点与旧骨骼兼容节点。

十一段动画名称全部沿用现有控制器：idle、walk、float_move、turn_l、turn_r、cannon_fire、gas_charge、phase_shift、transform、hurt、death。

举炮 1.6 秒、蓄气 2.6 秒、切阶段 2.2 秒、变身 2.8 秒、受击 0.45 秒，与 `CompostBossEntity` 的 tick 常量校验一致。死亡 2 秒并保持末帧。行走根据两脚的世界坐标补偿高度；61 点抽样最大脚底误差约 0.006 模型单位。

关键帧统一使用四位小数时间键，避免 JS 整数键重排。离线渲染复用 `cube_space.cjs`；根据本地 GeckoLib 4.8.4 字节码，在原模型坐标空间将旋转转换成 `(-X,+Y,-Z)` 后求变换。

## 可重复生成

在工程目录下执行，输出到独立候选目录，不会自动覆盖正式资源：

```text
node tools/model3d/gen_compost_boss_v5.cjs <输出目录>
node tools/model3d/preview_compost_v5.cjs <输出目录>
node tools/model3d/validate_compost_v5.cjs <输出目录>
node tools/model3d/validate.cjs <输出目录>/compost_superman.geo.json
node tools/model3d/validate_anim.cjs <输出目录>/compost_superman.geo.json <输出目录>/compost_superman.animation.json src/main/java
node tools/model3d/check_pixel_style.cjs <输出目录>/compost_superman.geo.json <输出目录>/compost_superman.png --out <输出目录>/pixel_style.json
```

唯一造型源为 `tools/model3d/gen_compost_boss_v5.cjs`，动作源为 `tools/model3d/compost_v5_anims.cjs`。V4 生成器保留作历史版本，不要拿它覆盖本次资源。生成器同时输出两份内嵌贴图及动画的 `.bbmodel`。

## 本次验证及交付

- 结构、父级顺序、动画引用/循环/时间键顺序/时长、810 个面的 UV 全覆盖、双阶段一致性、蒙版、Blockbench 层级及内嵌贴图校验通过。
- 离线查看了双阶段正面/背面/三分之四角度以及举炮、行走、飞行、变身、倒地姿势。
- 骨骼 AABB 报告中的关节嵌入属于连接设计；它不等价于精确的全姿势碰撞证明。
- 像素密度工具对苍蝇翅膀、薄边等小面提示复查，超过 3 px/单位的面积约 0.1%；主体密度在目标范围内。
- 未进入 Minecraft 或 Blockbench GUI 验收；外部手持土豆炮的实际贴合与游戏发光层需要实测。渲染图不包含这两层，不能当作游戏截图。
- 当前实例 `resourcepacks/FTBoss-Compost-Superman-v5` 提供七个资源的独立覆盖，默认未启用，不改 `options.txt`。
- 桌面导出目录：`C:/Users/YOUR_USERNAME/OneDrive/桌面/FT-Boss-大便超人重制_2026-09-16`。
- 本次检测到游戏运行，未覆盖实例 JAR。另一个会话也在改源码，因此本次使用实例 `_work/compost-superman-v5/build_snapshot` 的独立源码快照构建，不触碰共享的构建目录。
- 独立 Gradle 构建成功，构建中仅有既有 API 弃用告警；JAR 全条目 CRC 与七个本次资源的字节一致性检查通过。桌面导出的 `qys-bosses.jar` 是该快照，不代表之后其他会话提交的更新。
- 正式部署应等游戏关闭，回主工程运行 `tools/deploy_jar.ps1` **重新构建并部署**，不要用 `-NoBuild` 拿到另一个会话的旧成品。若只想改变本 Boss 的外观，使用资源包即可。
- 修改前的七个原资源备份在实例 `_work/compost-superman-v5/baseline`。
- 交付复核（23:43）：客户端已退出，实例中的 JAR 经其他并行构建同步后已包含本次七个新版资源，逐字节比对通过。本任务只读验证，没有再次覆盖该 JAR。下次启动可直接加载新版 Boss；资源包作为独立备份保留，非必需。
