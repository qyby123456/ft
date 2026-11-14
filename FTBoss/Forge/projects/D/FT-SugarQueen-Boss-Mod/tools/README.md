# 工具入口

所有命令从工程根目录运行。针对旧版本行号或文本的一次性 `fix_*` / `recover_*` / `patch_*` 脚本已列入归档白名单，只保留作历史参考，不应重新执行。当前入口如下。

| 任务 | 命令 / 位置 |
|---|---|
| 七位 Boss 37法术规则、图标、卷轴接线 | `node tools/verification/check_boss_iron_spells.cjs` |
| 铁魔法独立测试服（含抄录/升级/扩展效果） | 隔离工程准备 `smoke-libs` 后，`gradle -I tools/verification/boss_spell_gametest.gradle runGameTestServer`；见 `docs/BOSS_EXPANSION.md` |
| 资源、模型、动画、已移除物品、模型缓存回归 | `node tools/verify_project.cjs` |
| Java 包路径、资源命名与源码树卫生 | `node tools/verification/check_project_layout.cjs` |
| 食武插槽/附魔单次快照与套装等级等价性 | `node tools/verification/check_food_weapon_summaries.cjs` |
| 查看引用与疑似死代码报告 | `node tools/model3d/audit.cjs .` |
| 退役巨人代码、旧卷轴资源与兼容标记回归 | `node tools/verification/check_manor_no_giant.cjs` |
| 检查运行时 JSON / 纹理解码 | `node tools/model3d/check_assets.cjs` |
| 模型结构校验 | `node tools/model3d/validate.cjs --all` |
| 动画结构与时间轴校验 | `node tools/model3d/validate_anim.cjs --all` |
| 模型缓存单项回归 | `node tools/verification/check_model_resources.cjs` |
| 性能与召唤物生命周期回归 | `node tools/verification/check_performance.cjs` |
| 玄蓝骨骼/拖影规则与资源分件 | `node tools/verification/check_xuanlan_motion.cjs` |
| 玄蓝主副手、状态同步、渲染路由回归 | `node tools/verification/check_xuanlan_client.cjs` |
| 玩家双飞轮容量、真实飞行与回收清理 | `node tools/verification/check_xuanlan_blades.cjs` |
| 庄园开场演出回归 | `powershell -NoProfile -ExecutionPolicy Bypass -File tools/verification/check_manor_intro.ps1` |
| 构建并安全部署 | `powershell -NoProfile -ExecutionPolicy Bypass -File tools/deploy_jar.ps1` |
| 独立构建产物部署 | 上一条追加 `-NoBuild -ArtifactPath "完整jar路径"` |
| 合并仅存在于稳定包的兼容项 | `tools/merge_jar_extras.ps1 -Target <新jar> -Source <当前稳定jar>`；随后必须再走安全部署 |
| 手动等待退出再部署 | `tools/wait_deploy.ps1`，复用同一安全部署入口，不检查“文件可写就算退出” |
| 整理预览（只读） | `powershell -NoProfile -ExecutionPolicy Bypass -File tools/organize_files.ps1` |
| 执行归档（不删除） | 上一条追加 `-Apply` |
| 模型 / 美术工具 | `tools/model3d/README.md` |
| 音乐工具 | `tools/music/README.md` |
| 文档生成器 | `tools/docgen/` |

## 校验结果

`verify_project.cjs` 的各项日志写入 `build/reports/verification/`。任何子检查失败，入口返回非零退出码；疑似未引用资源只在审计中提示，不自动删除。

纹理校验通过 JDK ImageIO 解码，不再假定 `.png` 后缀一定是 8 位非隔行 RGBA PNG。`easter/xueli.png` 原素材是 JPEG，现已保持 RGB 像素不变转为 RGBA PNG；柚子、朵拉、雪黎仅在玩家皮肤外层 UV 恢复黑色底板的透明度。复现工具为 `repair_easter_skin_alpha.py <原图目录> <输出目录>`，不重画素材、不修改基础层或模型。

模型缓存回归使用真实模型源码、真实 `OrbType` 与最小 Minecraft / GeckoLib API 替身，检查路径、对象复用和阶段/球种切换。它不是完整客户端渲染测试，Forge 类型兼容性由完整构建验证。

## 归档与恢复

`organize_files.ps1` 只处理白名单中的历史日志、已移除物品的旧贴图、已停用的独立卷轴素材、无引用的 `IronCast` / `SphereMesh` 和已退役的一次性源码修补脚本（包括按旧行号删代码的 `cleanup_dead_code.cjs`）。两个旧类若再次出现运行时引用会拒绝归档。执行前输出清单；执行时验证路径不越出工程、不经过重解析点、不覆盖已有归档，并校验 SHA-256。

归档目录：`_dev_backup/organization-时间戳/`；`manifest.json` 记录源路径、归档路径、大小和哈希。恢复时先核对源位置没有新版本，再按清单逐文件复制回原位置。原来的 `_backup/`、模型输出、参考素材和游戏存档不在清理范围。

未接入声音事件的四个 `apple_boss_*` 旧音效也在归档白名单中；若运行时代码或 JSON 再次引用这些名字，工具会拒绝归档。当前战斗音乐不在此名单中。
