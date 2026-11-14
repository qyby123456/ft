# 糖霜女王模型重做

## 外观与范围

糖霜女王现在使用奶油白卷发、莓果色三层裱花礼裙、浅青糖晶冠、金边双尾披风与红白糖杖。保留 Minecraft 方块造型与低密度像素纹理，不使用平面图片冒充模型。

- 33 个骨骼、209 个立方体，256×256 RGBA 图集，逐面 UV。
- 主体贴图目标密度 1.5 px/模型单位；亚像素尺寸小装饰向上取整到至少 1 px。
- 主体动作：待机、行进、抬手接持续施法、杖击、受击、倒下；保留原有铁魔法兼容动画名。
- 手臂拆分肩、前臂、手掌；腿拆分大腿、小腿、鞋；披风与裙摆分节。
- 不改变 Boss ID、碰撞箱、血量、伤害、召唤、掉落、音乐、交易和图腾机制；不替换旧石像。
- 青色糖晶是普通实体贴图，不声明额外发光、粒子或法术效果。

## 接线

`client/model/GingerbreadKingModel.java` 保留类名，只将三条资源定位换成：

```
geo/sugar_queen_v3.geo.json
textures/entity/sugar_queen_v3.png
animations/sugar_queen_v3.animation.json
```

动画 ID 保留 `animation.king.*`，无需变更实体的动画控制器。原版动画中的 `blank` 是刻意保留的空动画，结构检查会提示 warning。石像仍引用旧的独立方块资源，未因本次更换发生改变。

## 重生成与检查

在源码工程根目录运行：

```powershell
node tools/model3d/gen_sugar_queen.cjs art/sugar-queen-v3
node tools/model3d/validate.cjs art/sugar-queen-v3/sugar_queen_v3.geo.json root,body,head,arm_l,arm_r,scepter,cape,leg_l,leg_r
node tools/model3d/validate_anim.cjs art/sugar-queen-v3/sugar_queen_v3.geo.json art/sugar-queen-v3/sugar_queen_v3.animation.json
node tools/model3d/queen_pose_export.cjs art/sugar-queen-v3
python tools/model3d/render_sugar_queen.py art/sugar-queen-v3
```

渲染需要 Pillow 和 NumPy。生成器只写给定输出目录；检查后才将三个运行时资源复制入 `src/main/resources/assets/ftboss` 对应目录。不要运行旧的全量模型生成器覆盖新资源。

`sugar_queen_v3.bbmodel` 内嵌图集及完整骨骼动画，可用安装 GeckoLib 模型插件的 Blockbench 打开。几何和动画 JSON 也可单独导入。

预览采用 `queen_gecko_pose.cjs` 将 GeckoLib 4.8.4 的旋转约定转换回模型坐标，再交给 `cube_space.cjs` 进行立方体/骨骼变换，以逐像素深度缓冲和真实 UV 渲染。它是离线验收图，不是游戏截图；游戏中的光照、动画切换、铁魔法叠加与碰撞仍需实机复核。

## 施法抬手方向

2026-09-17 根据游戏内反馈，修正起手与持续施法的双侧上臂、前臂 X 旋转方向；兼容施法动画从同一组姿态生成。动作时长、Y/Z 旋转、模型、贴图和战斗逻辑不变。

根因是旧预览直接把 JSON 角度当作模型空间右手旋转。已用本机 GeckoLib 4.8.4 的 `BakedAnimationsAdapter.buildKeyframeStack` 与 `BakedModelFactory$Builtin` 字节码核对：旋转 X/Y 取负，几何 X 也镜像；转换回原始模型坐标后，应使用 `[-rotationX, rotationY, -rotationZ]`。旧预览未做转换，因此画面上的“向前”在游戏内反而向后。

`verify_queen_cast_direction.cjs <geo> <animation>` 在引擎约定下逐帧计算双手相对肩膀的向前距离，覆盖起手、持续施法及兼容施法轨道。旧版断言失败，新版应全部通过；`cast-side.png` 用于侧视复核。不要为了让旧预览看起来正确再次将 X 角度改回正数。

## 施法手臂避让（2026-09-17）

后续实机反馈表明，仅修正 X 旋转不能解决穿模：旧肩部 Z 旋转将双臂向内收，前臂和长权杖会扫过胸口、头部与裙摆。

- 肩部改为向外展开、向前抬起，前臂与手腕保持较自然的弯曲；随抬手幅度增加少量肩部外移和前移，避开胸饰、卷发及披风。
- 权杖增加独立补偿旋转，使杖头保持在身侧前方，不再随手臂横扫头部，杖尾也不插入礼裙。
- `animation.king.scepter_raise` 改为一次性起手：准备 → 外展 → 前抬 → 保持，结束姿态与 `long_cast` 首帧一致；持续施法首尾一致，不反复重播抬手。
- 同步调整待机、行进、受击及杖击的手臂基准，避免中断施法或收手时经过旧的内收姿态。保留所有动画名、时长、几何、贴图、死亡动画及 Java 战斗逻辑。
- 验证器在 GeckoLib 模型空间以 60 FPS 采样，并检查六种控制器动作之间的混合过渡，共 4,438 个样本。通过旋转包围盒分离轴检测检查手臂、手掌、袖子和权杖与胸部、头发、裙摆及披风的相交，同时检查肩部连接未断开。只豁免泡泡袖与躯干/同侧肩领的设计连接，不豁免胸饰或头发；死亡动画不属于此项施法检查。

```powershell
node tools/model3d/gen_sugar_queen.cjs art/sugar-queen-arm-clearance/after
node tools/model3d/verify_sugar_queen.cjs art/sugar-queen-arm-clearance/after
node tools/model3d/verify_queen_arm_clearance.cjs art/sugar-queen-arm-clearance/after
node tools/model3d/verify_queen_cast_direction.cjs art/sugar-queen-arm-clearance/after/sugar_queen_v3.geo.json art/sugar-queen-arm-clearance/after/sugar_queen_v3.animation.json
node tools/model3d/queen_pose_export.cjs art/sugar-queen-arm-clearance/after
python tools/model3d/render_sugar_queen.py art/sugar-queen-arm-clearance/after
```

`arm-clearance.json` 保存检查结果；`arm-motion-review.png` 展示起手、施法和收手的斜前/侧面视图。此次仅同步源码中的动画资源，未覆盖游戏 JAR；离线抽样不代替实机验收。

## 安全部署

制作开始时检测到 Minecraft 正在运行，因此没有覆盖运行中的 JAR。23:35 构建前复查确认客户端已退出，随后通过 `tools/deploy_jar.ps1` 完成构建、四处部署和 Lightspeed 缓存清理。

关闭客户端后，从源码工程根目录运行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/deploy_jar.ps1
```

这是重新构建当前工程后再部署，不使用一份可能落后于其他会话改动的旧 JAR。游戏运行时脚本会拒绝部署。

正式 JAR 直接使用新的 v3 路径，不依赖额外资源包，没有改动 `options.txt` 的资源包启用列表。启动客户端后即可验收。

游戏内重点观察：静止、移动、长施法、近战挥杖、受击和死亡。本次方向修正不修改实体控制器，保留工程当前的施法和动作优先级。
