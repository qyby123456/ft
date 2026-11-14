# 苹果爵士 · 蜜脆庄园

2026-09-16，本体模型与双阶段贴图重做。

## 外观与兼容
- 收底、分层果肩的红苹果外壳；保留礼帽、墨镜、包装金带与苹果徽章。
- 枝根四足、肘膝关节、双叶刃、三节叶脉披风；三颗环绕苹果沿用原骨骼。
- 腐果二阶段使用独立配色，虫子继续由现有阶段控制器探出，蜡盖沿用缩放隐藏。
- 141 个立体部件、37 根骨骼、846 个独立 UV 面，两个 256×256 图集；主体密度约 1–2 px/模型单位。
- 37 根骨骼的名称、父级和枢轴与原版逐项一致，16 条动画文件原样保留。不修改 Java、战斗机制、数值、碰撞箱、掉落武器或石像。
- 苹果分身原本就共用本体几何与一阶段贴图，因此也会换成这个外观。

## 产物
- `apple_boss.bbmodel`：Blockbench Bedrock 工程，内嵌两张贴图与 16 条动画；默认使用一阶段贴图。
- `apple_boss.geo.json`：GeckoLib 模型，替换 `assets/ftboss/geo/apple_boss.geo.json`。
- `apple_boss.png`、`apple_boss_p2.png`：替换 `assets/ftboss/textures/entity/` 下同名文件。
- `apple_boss_preview_labeled.png`：正面、立体、背面、二阶段离线预览，不是游戏截图。
- `preview_*.png`：待机、行走、攻击、翻滚、变身、死亡与二阶段姿势抽查；取景自动居中，不代表实际地面高度判定。
- `validation_report.json`：UV 不重叠、两阶段采样无透明漏面、骨骼不变、工程动画引用与轴对齐静态共面检查。

## 验收范围
- 已运行几何与动画结构校验，并抽查真实 UV 采样的姿势预览。
- 旧动画的 idle 有 10 条末关键帧早于动画结束的警告；这是原文件已有内容，本次未改动画，以免带入无关修改。
- 旋转件共面与完整运动穿模不能靠静态校验完全排除；最终仍需重启游戏后检查阶段切换、挥击、翻滚与死亡。
- 运行中的客户端不会自动使用新资源。禁止覆盖正在运行的 `mods/qys-bosses.jar`。
- 源工程完成构建后，使用源码目录的 `tools/deploy_jar.ps1 -NoBuild` 部署；它会检测游戏进程、拒绝热覆盖并在安全部署后清理 Lightspeed 缓存。

## 重新生成
在源码工程根目录运行：

```powershell
node tools/model3d/gen_apple_boss.cjs art/apple_boss_remodel
node tools/model3d/preview_apple_boss.cjs art/apple_boss_remodel src/main/resources/assets/ftboss/animations/apple_boss.animation.json
node tools/model3d/validate_apple_boss.cjs art/apple_boss_remodel art/apple_boss_remodel/baseline
node tools/model3d/validate.cjs art/apple_boss_remodel/apple_boss.geo.json
node tools/model3d/validate_anim.cjs art/apple_boss_remodel/apple_boss.geo.json src/main/resources/assets/ftboss/animations/apple_boss.animation.json src/main/java
```

生成器先输出到独立目录，校验后仅安装上述三个运行时资源文件；不要批量覆盖其他 Boss 资源。旧版几何、贴图、动画与生成器保存在本次 baseline 及源码 `_backup` 目录。

## 本次交付核验
- Gradle `build` 成功；项目没有 Java 测试源，不能把 `test NO-SOURCE` 当成实机验收。
- 生成器重复生成的几何和两张贴图，SHA256 均与交付文件一致。
- 已逐个读取构建 jar 和运行实例 jar，确认新版几何、两张贴图与原动画的字节均与源码一致。
- 2026-09-16 23:44 构建产物与四个部署位置哈希一致：`B47B666DA82563A3FAD9B8B1D63A7971BCCE8C61028D66F28001F8E042E88E23`。共享工程可能在后续构建改变整包哈希，以资源字节核验为准。
- 全量 `check_assets.cjs` 对既有 `textures/entity/easter/xueli.png` 报自写 PNG 解码器错误。该文件与改动前已安装 jar 字节相同，Windows 解码器可正常读出 64×64；本次未修改这个无关资源，不能声称全量检查零告警。
- 最终游戏效果仍需启动客户端确认；本次图片全部为离线模型渲染。
