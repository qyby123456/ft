# 河豚棒 — 紫潮·豚刺

`ftboss:puffer_bat` 的默认外观为已确认的“紫潮·豚刺”立体模型，无需启用额外资源包。
名称仍是“河豚棒”，原物品注册、数值、附魔、掉落及紫菜 Boss 机制均保持不变。

第一人称正面朝向与左右手镜像见 `WEAPON_FACING.md`；第三人称握点校准保持不变。

## 资源与维护

- 83 个方块、8 个编辑分组、128×128 像素贴图。
- 金黄河豚、奶白鼓腹、青碧鱼鳍、骨白毒绿刺尖、紫菜缠柄。
- 原版静态 `elements`，无生成式父链；显式配置 8 个显示槽位。
- 模型：`src/main/resources/assets/ftboss/models/item/puffer_bat.json`。
- 贴图：`src/main/resources/assets/ftboss/textures/item/zicai_puffer_bat_v1.png`。
- 唯一模型/UV/贴图来源：`art/puffer_bat/Zicai_Puffer_Bat.bbmodel`，贴图已嵌入。
- 实际模型的离线预览和四视图在 `art/puffer_bat/`，不是游戏截图。

生成器直接导出已确认的 Blockbench 源文件，不重画贴图，也不重新布置几何。
`gen_weapons3d.cjs puffer_bat` 委托同一个生成器，避免批量维护时覆盖为旧外观。
脚本使用工程已有工具库，不依赖外部 Node 包。

```text
node tools/model3d/gen_puffer_bat.cjs
node tools/model3d/check_puffer_bat.cjs
node tools/model3d/lint_java_model.cjs src/main/resources/assets/ftboss/models/item/puffer_bat.json
node tools/model3d/check_uv_coverage.cjs src/main/resources/assets/ftboss/models/item/puffer_bat.json src/main/resources/assets/ftboss/textures/item/zicai_puffer_bat_v1.png
```

`gen_puffer_bat.cjs` 与 `check_puffer_bat.cjs` 的可选第一个参数是暂存 `assets/ftboss` 目录。
维护流程：在 Blockbench 编辑源文件 → 导出生成物 → 校验 → 构建 → 通过 `tools/deploy_jar.ps1` 安全部署。
生成前保留备份；不要运行旧的全武器脚本来更新单个物品。

## 游戏验证

### 手持定位

第三人称左右手以“深紫握柄”的几何中心 `[8, 2.55, 8]` 为握点，
`translation` 为 `[0, 1.0358, -0.1257]`；旋转和缩放保持原样。
GUI、掉落、展示框和头部槽位均未调整，几何、UV、贴图及技能不变。
第一人称使用 `WEAPON_FACING.md` 中的正面展示参数，不沿用早期的侧背面角度。

`tools/model3d/puffer_bat_hand.cjs` 复现 `ItemInHandLayer` 的
`Rx(-90) · Ry(180) · T(±1, 2, -10)`，随后按 `ItemTransform.apply` 的左手镜像及
`Quaternionf.rotationXYZ` 顺序应用物品变换，再减去 Java 模型中心 `[8, 8, 8]`。
空间旋转复用 `cube_space.cjs`；不沿用缺少手部矩阵的旧握点估算。
Steve 的掌心取手臂局部 `[-1, 8, 0]` / `[1, 8, 0]`，Alex 按原版半单位补偿。
已用 Minecraft 使用的 JOML 1.10.5 矩阵独立核验，四种左右手/手臂宽度误差均小于 0.001 模型单位。
检查器同时保留原偏移的回归用例；这不替代游戏内对附加姿势、光影和其他模组的验收。

完整退出游戏后部署，再重新启动客户端；已有河豚棒和紫菜 Boss 手持物均使用内置模型。
若启用了旧的 `FT-Zicai-Puffer-Bat-v1` 外观包，可将其停用，以免以后覆盖内置资源。
测试命令：`/give @s ftboss:puffer_bat`。
检查背包、第一/第三人称左右手、展示框、掉落和紫菜 Boss 持握；离线校验不能代替游戏内验收。
绿色刺尖和眼高光不自发光，鱼鳍无动画；此模型不赋予玩家毒伤或 Boss 的毒雾技能。

## 构建与部署状态

2026-09-16：Gradle 离线构建成功；498 个面通过 UV 覆盖检查。
首次构建与当时实例 JAR 比较，仅河豚棒模型变化及新增河豚棒贴图，其余条目字节保持一致。
`build/libs/qys-bosses.jar` 内两份资源与源码逐字节一致。
客户端退出后，已通过 `tools/deploy_jar.ps1 -NoBuild` 完成主实例和三处桌面成品同步，四处 SHA256 一致。
部署使用最新构建，保留了工程中已有的果梗模型/贴图更新；河豚棒资源与确认稿一致。
Lightspeed 资源缓存已清理；额外的 ModernFix 缓存删除未执行，保留已有结构缓存。
首次外观版本部署 SHA256：`B703DF6179370DB00672FCA34B236BF40FC3E37DF3D01786807437B99921AEB3`。

手持修正版已通过构建，JAR 中河豚棒模型与源码一致；`grip-audit.json` 与 `grip-comparison.png`
保存在 `art/puffer_bat/`。原版手部矩阵计算的握点误差由约 3.0295 降至 0.000034 模型单位。
2026-09-16 23:46 核验：最新统一构建已由并行部署同步至主实例与三处桌面成品。
四处 JAR 均含左右手平移 `[0, 1.0358, -0.1257]`，河豚棒模型及贴图与当前源码逐字节一致。
未用此前旧构建覆盖最新产物；四处 SHA256 为 `B47B666DA82563A3FAD9B8B1D63A7971BCCE8C61028D66F28001F8E042E88E23`。
Lightspeed 缓存已清，游戏内手持效果尚未复验。
