# QYS Bosses / FTBoss

Minecraft Forge **1.20.1 / 47.4.10** Boss 模组，modId 为 `ftboss`，产物固定为 `qys-bosses.jar`。

## 快速开始

- 使用 JDK 17、Gradle 8.8；本地编译依赖在 `libs/`，运行时依赖由整合包提供。
- 构建：`build.bat`；交付前完整重建：`build.bat clean`。
- 全量静态资源与模型缓存检查：`node tools/verify_project.cjs`，需要 Node.js 和 JDK 17。
- 安全部署：`powershell -NoProfile -ExecutionPolicy Bypass -File tools/deploy_jar.ps1`。
- 已构建产物部署：同一命令追加 `-NoBuild`；必须先确认是本次完整构建的产物。
- 独立快照部署：追加 `-NoBuild -ArtifactPath "完整jar路径"`，不会改写其他会话的共享构建目录。
- 检测到 Minecraft 运行时，部署退出码为 **2**，不会覆盖实例 jar。禁止绕过检查。
- 部署目标（2026-09-19 修正）：`Desktop\qys-bosses.jar`、`Desktop\FT经济-波动mod成品`、
  `OneDrive\桌面\FT经济-波动mod成品`、实例 `versions\FarmingTales_ForgeⅡ v1.5.2_beta3\mods` 四处。
  `Desktop` 与 `OneDrive\桌面` 是独立目录，必须分别同步；不再写旧实例 `FarmingTales Forge\mods`。
- 全量服务端回归：在独立工程准备 `smoke-libs/` 运行依赖后，运行
  `gradle -I tools/verification/manor_integration_gametest.gradle clean build runGameTestServer --rerun-tasks`。
  此入口包含庄园、非庄园、柠檬继承、柚子分身、折耳根和铁魔法原生粒子专项；使用全新测试世界，避免旧实体干扰。

## 目录导航

| 目录 | 用途 |
|---|---|
| `src/main/java/net/farmingtales/ftboss/` | 游戏逻辑、实体、客户端表现与注册入口 |
| `src/main/resources/` | 运行时模型、动画、贴图、音频、语言、配方与掉落 |
| `gradle/verification.gradle`、`gradle/verification/` | 按领域拆分的源码、资源、模型与最终 jar 回归检查 |
| `libs/` | 本地编译期 API 依赖，不能当作杂物删除 |
| `tools/` | 构建部署、校验及素材工具，入口见 `tools/README.md` |
| `docs/` | 文档库：入口与目录导航 `docs/README.md`，写作规则 `docs/WRITING_RULES.md`；代理级约束 `.agents/DOC_CONSTRAINTS.md`（实现代理不得直接改 `docs/**`） |
| `art/`、`ref/`、`_ref/` | 美术源文件与参考素材，不参与打包 |
| `_backup/`、`_dev_backup/` | 回滚备份与带清单的归档，不参与打包 |
| `build/`、`.gradle/` | 可再生成的构建输出与缓存 |
| `_work/` | 每项任务独占的构建快照、验证报告与回滚副本 |

## 维护边界

- 主源码工程在桌面的 `FT-SugarQueen-Boss-Mod`；游戏实例 `_work/` 下的工程是快照，不要混当主源码。
- 不通过“全文搜不到”就删除资源：模型动态拼接、发光层和资源包约定都可能隐式引用。
- 保留注册 ID、NBT 键、网络协议和资源路径；整理不应改变伤害、掉落、交易或阶段规则。
- 新代码先按职责落到 `boss/ai`、`item/weapons`、`client/model`、`client/render` 等领域目录；不要继续扩大通用 `util`。
- 根目录不保存构建历史日志；通过 `tools/organize_files.ps1 -Apply` 带哈希归档到 `_dev_backup/`。
- 模型生成器只能向临时目录输出，逐文件比对后再替换目标；不要全量覆盖已手工精修的素材。
- 并行会话修改期间不要共享 `build/`；复制 `src/`、`libs/`、`gradle/` 及三个 Gradle 配置文件到独立目录再构建。
- 校验通过不代表实机已验收；游戏内的动画、发光、掉落和多人同步仍需重启后验证。
- 文档库（`docs/**`）由专属 docs-writer 代理按文档库写作规则（`docs/WRITING_RULES.md`、`docs/AGENTS.md`）与代理级约束（`.agents/DOC_CONSTRAINTS.md`）维护：实现代理不得直接编辑，只能把文档影响写成 `_work/docs-proposals/` 下的提案交由主代理裁决。

结构说明见 `docs/ARCHITECTURE.md`；文档库导航与维护规则见 `docs/README.md`；最新性能优化与文件整理见 `docs/PERFORMANCE_20260918.md`，上一轮整理见 `docs/CODE_CLEANUP_20260917.md`。
