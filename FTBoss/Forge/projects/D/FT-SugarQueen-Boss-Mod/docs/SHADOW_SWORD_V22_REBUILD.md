# 暗影剑 V22：按原设计重做模型

日期：2026-09-17。当前模型版本 **V22**；不是旧历史脚本 `patch_shadow_v22.cjs` 所指的静态贴图迭代。

> **最新部署状态（2026-09-17）**：用户要求“部署入模组”后，已完成 Gradle `build`（475 份资源、802 个 jar 条目校验通过），通过 `tools/deploy_jar.ps1 -NoBuild` 部署至游戏实例及三处桌面成品目录。jar 已内置 V22 模型与下述第三人称刃口修正，不再需要启用验收资源包。本轮最初部署的 SHA-256 为 `84e2307447707ac903737c23c676a65dc1ac99faca3a15d908ac7234ff4de167`；其他会话随后再次构建并部署，因此没有用旧产物覆盖回去，而是重新核验四处最新 jar：六份暗影剑资源均与当前源码一致。最终观察到的哈希及检查时间见实例 `_work/shadow-sword-v22-deploy/deployment.json`，同目录保存备份与构建日志。部署前确认游戏退出、保存旧 jar，部署后已清 lightspeed 缓存。下文“未部署 jar / 需要启用资源包”仅描述此前阶段，不再代表当前状态；游戏内持握仍需实测。

## 范围与设计

本轮依据已记录的七项原设计要求重建几何与图集，不是继续微调旧方块。本机未找到原设计图片，因此以原生成器及 CHANGELOG 中的设计文字为依据。

- 单刃轮廓：八段收尖刀身、两处刀背倒钩；不做对称十字长剑。
- 厚度分层：薄刃、冷灰紫斜刃面、暗色主面、凹槽、凸起刀脊；避免整条白色灯带。
- 魔力裂纹：按刀身坐标绘制分叉路径，少量紫色亮点，不加满面噪点。
- 护手：内收暗影双爪围住三层魔眼，正反两面均有独立黑色竖瞳。
- 握柄：深色柄芯、两条斜缠带、上下金属箍。
- 柄首：阶梯黑色底座包裹紫晶，保留原晶体动画接口。

## 兼容性与数据

| 项目 | V21 基线 | V22 |
| --- | --- | --- |
| 骨骼 | 22 | 22，名称、父级、pivot、rotation 不变 |
| cube | 65 | 72 |
| 动画 | 9 | 9，文件逐字节不变 |
| 图集 | 256×256 | 128×64，153 个带外扩像素边的 UV 岛 |
| 主体中位像素密度 | 12.54 px/模型单位 | 2.33 px/模型单位 |

物品 ID、技能、冷却、Java 渲染代码均未修改。沿用 V21 第三人称 translation `[0,-0.73,0.6]` 和 scale `[0.85,0.85,0.85]`，第一人称及其他 display 不变；第三人称 rotation 已按下述反馈修正。握点仍需实机确认，离线预览不能证明已贴合玩家或 YSM 的手。

### 第三人称刃口方向修正（2026-09-17）

用户反馈刀刃朝向玩家自身。V22 重建时原样沿用了 V21 的第三人称旋转，本次仅将物品绕自身柄轴翻转 180°，不改几何、贴图、骨骼、动画、握点平移或比例：

- 右手 rotation：`[0,-90,-20]` → `[0,90,20]`。
- 左手 rotation：`[0,90,20]` → `[0,-90,-20]`。
- Minecraft 1.20.1 `ItemTransform.apply` 使用 JOML `Quaternionf.rotationXYZ`，左手还会将 Y/Z 两角取反。两个槽位均同时修正 Y/Z，等价于在原旋转右侧乘局部 Y 轴半转；只改 Y 会连带改变刀尖倾斜。
- 验证使用 Minecraft 同版本 JOML，核对左右手柄轴不变、刃口横轴反向；此数学检查不替代 YSM/玩家模型下的实机验收。
- 专用生成器与现有 `FTBoss-ShadowSword-V22` 资源包同步，重生成不会恢复旧朝向。过程记录在游戏实例 `_work/shadow-sword-v22-facing/`；原 V22 报告里的 `displayUnchanged` 是重建当时的历史结果，不适用于此次第三人称修正。
- 检查时正在运行的 jar 内仍为修正前朝向，`options.txt` 中未启用资源包。本次不覆盖运行中的 jar、不改 options；在游戏启用 V22 包后才能加载修正，只按 F3+T 不会自动启用它。

三张贴图一起重绘：普通、ready、`_e` 蒙版。ready 只提亮魔力像素，不改变黑铁和握柄；当前 Java 渲染器没有接入 `_e`，不能将这个文件称作已生效的自发光层。

## 本轮检查

- `validate.cjs`：0 error / 0 warning / 0 review。
- `check_sword_compat.cjs`：全部骨骼接口保留；9 段动画未增删、未修改。
- `check_shadow_struct.cjs`：9 段动画、111 个采样姿态，稳定性检查通过、疑似脱离 0。
- 自检：432 个面均在图集内且覆盖不透明像素；ready 没有通道变暗；生成器重复运行的五份资源 SHA-256 一致。
- 共面自检：同一骨骼的未旋转方块，同向面面积重叠 0；不等同于所有动画姿态的完整三角面碰撞检测。
- 密度报告仍有 156 个面提示复查：高密度面积占主体 16.7%，主要是魔眼、晶体、法球以及窄边的像素取整；并未声称全模都在 1–2 px/单位内。
- AABB 重叠和 UV 复用报告仍存在：魔眼嵌入眼框、爪与主体连接及同材质复用是有意设计；它们不等于已排除所有实际穿模。
- 已检查离线待机、cast@0.349s、release@0.1s 四视图。预览使用共享 `cube_space.cjs`，只作数值关键帧线性采样，不包含实际游戏控制器混合、光照和持握管线。
- `validate.cjs` 修复镜像 UV 边界误判，并以正/负 U/V 的五个回归样例验证。
- `export_bbmodel.cjs` 修复逐帧时间写法、恒定向量通道、动画名称和骨骼 UUID 关联；离线导出 `.bbmodel`，未在 Blockbench GUI 中验收。

动画 SHA-256：`de191d57947cb9f23ba741777687659efec40a717cfdb03213be3142a4b9e062`。

## 文件与复现

正式资源位于 `src/main/resources/assets/ftboss/` 下：

- `geo/shadow_sword.geo.json`
- `models/item/shadow_sword.json`
- `textures/item/shadow_sword.png`
- `textures/item/shadow_sword_ready.png`
- `textures/item/shadow_sword_e.png`

只用本轮专用生成器，不运行旧静态生成器或历史 patch 脚本：

```powershell
node tools/model3d/rebuild_shadow_sword_v22.cjs ref/shadow_sword_v22_base _work/shadow-sword-v22-generated
node tools/model3d/validate.cjs _work/shadow-sword-v22-generated/geo/shadow_sword.geo.json
node tools/model3d/check_sword_compat.cjs ref/shadow_sword_v22_base/shadow_sword.geo.json _work/shadow-sword-v22-generated/geo/shadow_sword.geo.json ref/shadow_sword_v22_base/shadow_sword.animation.json src/main/resources/assets/ftboss/animations/shadow_sword.animation.json
node tools/model3d/export_bbmodel.cjs art/shadow_sword_v22.bbmodel
```

生成器向输出目录写上述五份资源与一份清单。验收后按相对路径拷贝五份资源到正式目录，清单不必进入 assets。`ref/shadow_sword_v22_base/` 保存六份 V21 原始资源，可复现或回退；不要将成品覆盖基线。旧 `regen_shadow_sword.cjs` 现在会拒绝覆盖 `builtin/entity` 骨骼模型。

Blockbench 文件：`art/shadow_sword_v22.bbmodel`，内嵌普通贴图与九段动画。它是便于编辑/检查的导出件，生成器与正式资源才是本轮可复现真源。

## 安装与实机验收

本轮没有构建、覆盖或部署 `qys-bosses.jar`。游戏运行期间不得替换它。

已安装独立包：

`C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales Forge/resourcepacks/FTBoss-ShadowSword-V22`

在游戏“选项 → 资源包”启用 V22，置于其他暗影剑覆盖包上方，停用 V21 等旧版暗影剑包。包中包含匹配的新 geo、模型 JSON 和三张贴图，不能只取几何搭配旧贴图。确认应用后需要时按 F3+T 重载。

实机仍需看第一/第三人称、左右手、背包图标、掉落物及蓄力/释放；尤其核对三张截图中的离手偏移是否消失。若 GeckoLib 缓存未刷新，可退出客户端再启动，不应通过覆盖运行中的 jar 解决。

停用 V22 资源包即可回到 jar 的版本。源码若需回退，应将 `ref/shadow_sword_v22_base/` 对应资源恢复；不会自动回退其他会话的 Java 或资源修改。

完整过程报告与预览在游戏实例 `_work/shadow-sword-v22/`：`quality-check.json`、`structure.json`、`pixels.json`、`delivery-check.json`、`idle.png`、`cast.png`、`release.png`。
