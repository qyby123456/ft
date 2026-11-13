# D / integration：只读快照整合与隔离验证

本目录独占写入；core、runtime、catalog 与正式实例全部只读。工具可消费不代表生成器通过验收。

最新结果见 `INTEGRATION_REPORT.md` 与 `final-evidence.json`，其中保留首轮独立两项失败及后续修正的精确日志，不以生产者测试通过抵消独立失败。`test-matrix.json` 区分 PASS / PARTIAL / NOT_RUN；生成器完整发布验收仍未通过。

## 入口

Python 使用本机已验证的 `C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`，不依赖第三方包。

在本目录运行：

```powershell
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$java = "$env:APPDATA\.minecraft\runtime\java-runtime-gamma-snapshot"
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
& $python test_integration.py
& $python integrate.py probe
& $python integrate.py snapshot
& $python integrate.py build --gradle $gradle --java-home $java --offline
& $python integrate.py gametest --gradle $gradle --java-home $java --timeout 900 --offline
& $python run_smoke.py --gradle $gradle --java-home $java --timeout 420
& $python publish_tooling.py
```

probe/snapshot/build 返回码 2 表示 WAITING/REJECTED/构建失败，不是成功。只存在源码、JAR 或 progress.json 不能解锁依赖。不会自动把完整测试矩阵提升为 PASS。独立 world-overlay.gradle 注册真实 GameTest 运行配置，不修改 runtime 所有者文件；若接口变化导致编译失败，保留失败而不修其他组件。

## 冻结协议与整合

- 每个 ready.json 必须提供 `component`、`apiVersion:1`，若有 `status` 必须明确 READY/COMPLETE/COMPLETED；`ready:true` 加 `READY_...` 范围状态也接受，显式 `ready:false` 永不解锁。
- 支持 `files:{"相对路径":"SHA256"}` 或嵌套 `{path/file,sha256}` 描述，及已核对 `inventorySha256` 的 `inventory` 外置清单。拒绝无逐文件哈希清单、绝对路径、越界、符号链接/目录联接、哈希错误。
- 所有会被构建消费的源码、Gradle文件、资源和验收证据必须出现在哈希清单；未列出的文件不会复制。ready 文件自身另行计算和锁定哈希。
- 所有依赖 READY 后才新建 `staging/时间戳/`；复制前核对标记，复制后核对源/目标哈希，生成 snapshot-lock.json。失败快照保留，未通过门禁不会构建。
- `assembled/` 从冻结 runtime 生成；`-PcoreSource` 指向同一快照中的真实 core。若 runtime 内含 core stub，则拒绝整合，不修改所有者源码。
- catalog 相同字节的多份 manifest 允许存在；冲突则拒绝。只打包白名单 NBT，拒绝未验收模板、空池、非法坐标/接口枚举和哈希错误。独立验收的实质依然以 catalog 所冻结证据为依据，工具字段检查不是新的独立验收。
- 构建后检查候选 JAR 中真实 core API、重复ZIP条目、manifest 原始字节、NBT 白名单及原始字节哈希；不会部署到正式 mods。
- Gradle项目在 staging 内，JavaExec 和 Forge run 工作目录重定向到 integration/run。允许复用本机 Gradle 用户缓存，不复制缓存。子进程命令、cwd、退出码、超时、日志均记录。超时终止该进程树，不删除世界证据。

## 隔离实测边界

`run_smoke.py` 使用 `smoke-src` 创建独立 Forge 1.20.1 / 47.4.10 项目，运行 GameTest 写入并回读一个金块。此项目**没有加载地牢组件或真实房间**，仅验证环境可启动，绝不计为生成器现场通过。默认 offline；仅 offline 时关闭 ForgeGradle 的联网证书预检。`--online` 不关闭证书检查。

烟雾项目模板为明确标注的人工空结构，不属于正式房间池。其生成、编译、世界和日志全部位于 integration/run。ForgeGameTest 进程退出码为零仍需检查测试计数与报告，不能推断任何地牢测试通过。

## 测试矩阵与故障注入

`test_integration.py` 为工具单测：无ready、存在文件但未ready、显式WAITING、API不符、缺少哈希清单、源文件篡改、越界、快照后源变化、未冻结文件不复制；manifest 空池、错误NBT哈希、越界资源ID、未验收、缺证据、错误命名空间、布尔尺寸、非法干湿/朝向/脚点/门宽。所有fixture只在 test-work 下生成并清理。

`test-matrix.json` 的十一项为**世界验收**，初始全部 NOT_RUN：房间级随机、四旋转、玩家覆盖拒绝、空池/坏哈希零写入、预检零写入、中断重启与幂等、方块/实体载荷回读、边界/票据释放、返回路线、门缝屏障。离线fixture测试通过不能替代这些断言。

`world-tests` 提供三项真实断言：非法锚点拒绝及哨兵保留；真实模板操作集末尾注入金块，逐格比较完整操作集，确认预检零写入；真实单房间分刻放置、暂停并重新加载磁盘journal后续建、全操作回读。第三项是同一JVM中的持久日志重新加载，**不是进程重启**。测试源码/overlay 在启动前复制到 run 并生成 verification-lock.json；它们不打包进候选生产JAR。日志中的 `INTEGRATION_ASSERT` 由断言后产生，另存 world-observations.json，仍不自动宣称完整矩阵通过。

后续独立断言新增：68,868 个真实模板方块/四旋转半开盒补偿检查；32 种子、反序模板池稳定性、两模板抽样；真实混合三房间放置后的完整操作回读、所有已用门脚及走廊折点的实体碰撞/承重面 BFS、外围屏障回读；四个旋转分别在真实世界放置两种源模板，按独立整数公式逐格对比原始内部载荷。最终是否实际通过以报告的测试数和精确日志为准。未用门面会安全封口，原始载荷四旋转回读排除这些应被改写的边界面。

`replay_restart.py --snapshot <staging快照> --gradle <路径> --java-home <路径>` 独立复跑冻结生产者的 prepare/resume 测试，两个 JVM、同一隔离世界；不等同强杀/断电。

`installed_server.py --snapshot <staging快照> --java-home <路径> --installer <本地安装器> --official-sha1 <官方实时摘要>` 在新时间戳目录安装普通 Forge，加载精确重混淆候选 JAR 并执行真实命令。支持受验证的本地安装器；复制后校验官方 SHA1、ZIP CRC、install_profile.json。官方摘要通过 PowerShell Invoke-WebRequest 从同一官方 URL 的 `.sha1` 获取，记录在 `logs/official-installer-sha1.json`。最早 urllib HTTP403 失败保留在旧 run/result.json，不再当作当前阻塞。

`verify_saved_world.py --snapshot <快照> --world <普通服务端/world> --output <integration内JSON>` 使用冻结且校验哈希的 catalog NBT 基础解码器，独立实现 Anvil region/chunk section 长整型位打包读取；直接比较保存到磁盘的原始模板内部方块（独立旋转坐标与状态属性）、所有外围 barrier 和保守的两格全空气/整块地板通路。未用门封口会改变边界，原始载荷对比明确排除这些面。此程序不是 runtime 自己的 operations 回读，不依赖 ForgeGradle 测试类。

`installed_recovery.py --snapshot <快照> --run <已安装隔离目录> --java-home <路径>` 运行两个普通独立服务端 JVM，分刻中途显式暂停/干净关闭、确认磁盘cursor，第二进程确认 active=false/tickets=0/原cursor 后显式续建，COMMITTED 后再次 build 验证幂等，最后独立回读保存区块。不是强杀/断电。

`installed_conflict.py --snapshot <快照> --run <已安装隔离目录> --java-home <路径>` 在新高空区域计划后用原版 setblock 放置金块，build 必须 FAILED/cursor=attempted=0/tickets=0，干净保存后逐格确认完整包围区只有该金块，其余空气。所有命令和进程PID日志保留。三脚本运行同一世界时必须串行，不能在服务端仍运行时读离线区块。

最初 JAR 试跑结果、独立初次读盘、安装环境恢复、安装环境冲突分别记录于 `installed-server-result.json`、`installed-readback-initial.json`、`installed-recovery-result.json`、`installed-conflict-result.json`。实际是否 PASS 以最终报告为准，保留阶段性限制；不得把早期缺少独立回读的命令提交结果单独宣称完整安全。

测试结束后运行 `finalize_evidence.py` 汇总历史失败/最新成功、精确快照与JAR哈希和局限，再运行 `publish_tooling.py` 重新测试工具并发布交付哈希。不要在收集证据后再运行 probe 覆盖当前阶段。

实测时每项必须记录精确 snapshot-lock/JAR 哈希、命令、世界目录、测试输入与种子、前后区域快照、独立断言结果和日志；重启项必须保留两个服务端进程及持久日志证据。其余故障注入仍未实现/执行，矩阵不是已执行声明。世界随机刻和流体传播未验证，不修改全局世界规则掩盖问题。

## 证据文件

progress.json / dependency-status.json：当前依赖与阶段；ISSUES.json：阻塞原因；ready.json：工具交付范围及哈希；logs：实际工具测试；environment-smoke.json：环境试运行结果。没有精确哈希整合构建及真实地牢现场证据前，不称生成器MVP。

smoke-src 的 ForgeGradle版本、Forge依赖和映射只读参考 `D:\ft工程文件\FT-Dungeon-Mobs-Forge-1.20.1\build.gradle`；未复制正式JAR、mods、saves、配置或缓存。
