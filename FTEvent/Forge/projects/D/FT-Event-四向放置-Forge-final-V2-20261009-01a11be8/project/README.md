# Event 四向放置 · Forge-only 最终 V2

所属聊天：`01a11be8-fba2-77a3-a117-5471717a4c17`，仅负责预言机、许愿井核心、天气预报机的水平四向放置与结构旋转/镜像。

## 当前实现与优化

- 预言机、天气预报机保留当前北/东/南/西 `facing` 与对应 0/90/180/270 度模型状态，放置时正面朝向玩家。
- 核心继续使用现有 KubeJS `cardinal` 注册和原两段碰撞箱，不覆盖脚本或资源；本次 qys-event 自有 Mixin 补齐其默认方块行为回退中的结构旋转与镜像。
- Mixin 精确定位 Forge 1.20.1 `BlockBehaviour` 的旋转/镜像方法；仅当注册 ID 为 `farmingtales:wishing_well_core` 且含水平 facing 属性时返回新状态。其他方块、预言机、天气机及独立整井不受影响；没有 tick 或渲染回调。
- 保留模型线程已部署的最终减面模型：天气机 288 面、预言机 510 面、核心 282 面，总计 1080 面。仅复验，不重复生成模型、覆盖资源或恢复旧几何。
- 原 48 个 qys-event 类、事件规则、交易/许愿/合成、客户端天气 Mixin、纹理、模型和存档 ID 不变。新增仅 1 份自有 Mixin 配置和 2 个类。
- 庄园放大/加速与 Boss 技能效果审计不适用于本线程；范围证据见 `verification.json`，不修改共享 `qys-bosses.jar`。

## 构建、校验与安全部署

`release.py prepare` 从当前 Forge JAR/相关脚本资源取得新基线，同时只读比对协调线程记载的 NeoForge 回滚 SHA256。
`build.ps1` 编译精确增量，在当前 JAR 上构建候选包并校验原事件、JVM/ASM、Mixin 选择器、核心旋转镜像与范围 guard；不使用早期整包安装。
重新运行资源/减面回归、核心四向碰撞边界、三模型 21 视角、12 项朝向与缩放/旋转/时间轴/移动端交互预览，统一入口挂载为 `/event_four_way/viewer.html`。
`audit.py` 汇总检查并锁定测试 payload SHA256。`release.py deploy` 仅写当前 Forge `mods/qys-event-forge-1.20.1-1.0.6.jar`，共享锁与占用预检后，先备份和记录 SHA256，再保留当刻 JAR 的非目标条目原子合并。
在独立 D 盘工程中复验时，先设置 `FT_EVENT_FORGE_WORKSPACE` 为明确的 Forge 1.5.2 实例目录；不允许把审计候选整包当安装源。
任何基线变化或占用即停止，不重试、不停止占用进程、不写 NeoForge、不覆盖 KubeJS 或 qys-bosses。
运行时增量 ZIP 只含三个 JAR 条目，不能直接丢入 mods；候选整包与基线副本仅供审计，不是允许覆盖未来实例的安装源。

## 实机待验

离线 Java、Mixin 选择器与网页预览校验不是真实 Minecraft Mixin 启动验证。需要完全退出并重启 Forge，再验四向摆放、结构旋转镜像、旧存档、核心碰撞/结构识别、绿宝石许愿/预言/预报、联机与光照。
当前 NeoForge JAR、核心脚本和状态 JSON 均只读复核协调线程回滚值；不在该实例保留本轮新增修改。

最终独立 D 盘归档包含 editable project、entry-only runtime、latest preview、validation、handoff 与逐文件 SHA256；结果见 `deployment.json`、`archive-sha256.json` 与 `handoff.json`。
早期 `_work/event-four-way-20261008`、`_work/event-four-way-final-20261008` 和 D 盘 V1 均仅标记 superseded 并保留，待协调线程集中审计清理；预览比较用的早期模型只作为测试夹具，不被运行时或预览渲染引用。

## 最终状态：安全部署停止，V2 增量未安装

本次唯一部署尝试在共享锁内的 JAR 占用预检失败，立即停止，没有重试、没有创建部署备份，也没有写入 Forge/NeoForge 实例文件。
已有三方块四向放置仍保留；本次补齐核心结构旋转/镜像的三个条目仅封存为已验证候选，不宣称其已在游戏内生效。`tracking.json` 保持本线程未 ready，记录独立归档与停止原因。
部署前后 Forge qys-event JAR SHA256 均为 `36b8165e3ece55506abd4ea325608732fec20e26cc17af9e1f11ca4056afade8`；脚本、状态、模型和纹理哈希均未变，NeoForge 三项 SHA256 均与协调线程回滚记录一致。
待用户解决占用并重新明确授权后，必须从新的当刻基线重新准备/验证/备份和原子合并，不能安装本归档中的旧整包；本工程禁止自动重试。
