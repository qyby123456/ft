# 独立 Event · Forge 完整源码与离线封存成品

状态：`SEALED_OFFLINE_NOT_DEPLOYED`。本目录不表示 Event 已获启用或正式发布批准。

## 内容与边界

- `project/src/main/java`：完整规范工程 Java，合入天气预报机四向放置与许愿井核心旋转/镜像补丁；保留金币锅奖励代码。
- `project/src/main/resources`：从冻结的活动包提取完整资源，包含减面后的预言机/天气机模型、图集、贴图、四向 blockstates、语言与数据。
- `project/src/test/java`：事件/天气/金币锅规则验证，增加核心四向旋转、镜像与作用域验证。
- `integration/kubejs`：许愿井核心注册、模型/贴图、升级商店发货脚本与提示的只读快照。它们不属于 Event JAR，不能省略或冒称已经打包进 Mod。
- `deliverables`：由本目录完整源码构建的独立 Event JAR，待单独审查与启用授权。
- `reference`：历史包与部署回执，仅用于来源核验，不能作为自动恢复/启用脚本输入。
- `audit`：实际活动包反编译阅读参考、来源调查、JAR 条目差异、源码对应关系、验证与状态回执。

没有部署脚本，没有修改或合并 Boss，没有访问或编辑存档，没有访问 NF 实例。
实例中观察到活动 Event 存在，只是事实记录，不作为本批正确启用状态的批准。
当前授权边界下不删除活动包、不恢复 `.disabled`、不复制任何文件进入 `mods` 或 `kubejs`。

## 构建

目标 Minecraft 1.20.1 / Forge 47.4.10 / Java 17 / Gradle 8.8，固定 ForgeGradle 6.0.54；modId `qys_event`，内部版本保留 `1.0.6`。
使用 `project/build.ps1 -JavaHome <Java17JDK目录> -GradleHome <Gradle8.8目录> -Clean`。
脚本把 Gradle 缓存、临时文件与 Java 用户目录放到本目录 `_work`；默认离线。
在全新环境先安装 Java 17、Gradle 8.8，再用 `-Online` 下载 ForgeGradle 依赖。
构建执行 `clean build reobfJar verifyQysEvent`。成品位于 `project/build/libs`。
需要本批构建环境与结果时查阅 `audit/build-environment.json` 和构建日志。
`_work` 与生成的 `project/build`、`project/.gradle` 不进入源码 ZIP。

## 精确对应关系

`reference/observed-active-262B3284.jar` 是 10/12 金币锅增量包的冻结副本，不是本次完整源码构建产物。
它等于独立分离版 `9FC66CD0...` 加五个金币锅类；原 112 个 ZIP 条目字节完全不变。
旧规范工程直接产物 `83E6305F...` 不含完整四向/模型修复，不能覆盖活动包。
本次封存的源码与 `deliverables` 新构建包精确对应，完整 SHA256、条目与来源映射见 `audit/source-jar-map.json`。
`audit/observed-runtime-decompiled` 是旧活动包的 SRG 反编译阅读参考，不是官方命名构建源码，不保证旧包字节级重建。

## 审查与待办

离线构建/自动化验证通过不等于游戏内验收，也不等于批准启用。
未来部署需先取得用户明确“启用 Event”的授权，重新核验活动/禁用文件与最新 SHA256，处理重复包，再单独部署独立 Mod。
授权确认前只保留本目录封存产物，不回滚、禁用或覆盖实例中的任何内容。
若停用 Event 或回滚为无金币锅类旧版，第 101 个金币锅起的奖池不能工作；现有脚本在奖池不可用时拒绝消费金币锅。
