# AGENTS.md — FT-SugarQueen-Boss-Mod（糖霜女皇 Boss mod, Forge 1.20.1）

## Documentation ownership (hard rule — 2026-10-18)

`docs/**` is the documentation library. It is owned by the dedicated docs-writer agent.
Agent-level rules, roles and the proposal channel:

- profiles: `.agents/docs-writer.md`, `.agents/implementation-agent.md`
- agent constraints (English): `.agents/DOC_CONSTRAINTS.md`
- repository-facing documentation policy (maintained with the docs): `docs/README.md`
  (navigation), `docs/AGENTS.md` (directory write constraints), `docs/WRITING_RULES.md`
  (writing rules)

Hard rules:

- **Implementation agents/subagents MUST NOT create, edit, move, rename, or delete anything
  under `docs/`.** No exceptions for "small syncs". Only the dedicated docs-writer
  subagent, dispatched by the Lead after approval, writes `docs/**`.
- If a code change affects documented architecture, boundaries, contracts, or stable
  reference data, write a proposal using `_work/docs-proposals/_TEMPLATE.md` into
  `_work/docs-proposals/<topic>.md`, or report `docs impact: none` in the task report.
  The Lead decides what enters the docs library. Implementation agents read
  `_work/docs-proposals/README.md`.
- One writer at a time on `docs/`. If an agent detects docs edits it did not make, it stops
  and reports instead of overwriting; last-writer-wins is a defect.
- Documentation describes the current architecture and contracts, not line-by-line
  implementation (readers follow symbol/package anchors into the codebase); no change-log,
  dated, `CHANGE-*`, handover or session files in the docs library (history belongs to
  version control); keep `docs/README.md` navigation current.
- Harness/plugin configuration should enforce the write scopes with hard permissions. A
  docs edit made by any non-docs-writer agent is a process violation even if the content
  looks correct. Repository-side enforcement artifacts: `.agents/agents.json` (machine
  readable write scopes), `.githooks/pre-commit` + `tools/check_docs_policy.cjs` (commit-time
  gate; docs-writer commits export `FTBOSS_DOCS_WRITER=1`), enabled with
  `git config core.hooksPath .githooks`.

## 最新部署范围（2026-10-12 用户指定）
- 部署不需要同步桌面或 `FarmingTales_ForgeⅡ v1.5.2fix2`：默认不写 Desktop、OneDrive 桌面及其成品目录，也不写 fix2 或清理 fix2 缓存，除非用户另行明确要求。
- 本节覆盖下文历史记录中要求桌面多处同步及 fix2 同步的旧口径。已经同步的副本保持不动，不自动删除或回滚。
- 本次 benger 任务的部署脚本仅写当前 `FarmingTales_ForgeⅡ v1.5.2` 实例。

## 统一工程目录（2026-09-29 用户指定）
- 主工程唯一位置：`D:\ft工程文件\FT-SugarQueen-Boss-Mod`。以后源码、`_work` 隔离构建树、备份和工程中间产物统一放在 `D:\ft工程文件` 下，不得重新在桌面或 C 盘创建工程副本。
- `.minecraft\versions` 与成品 JAR 部署目录仍保持原位；它们是运行实例/交付物，不属于源码工程迁移范围。

## 项目本质
Minecraft Forge 1.20.1-47.4.10 mod：糖霜女皇 Boss + 糖果魔像模型 + 石像交易方块。
modId=`ftboss`，jar=`qys-bosses.jar`（用户 2026-09-10 起固定产物名，不再带版本号）。与价格 mod（`FT-Econ-Core-Mod`，ftecon）完全独立，互不依赖。

## 模型四视图验收铁律（2026-10-02 用户指定）
- 任何模型的新建、修改、导入、移植或同步，在模型专项测试通过后、构建/部署前，必须再用**最终运行时使用的 geo/模型与贴图**渲染一次四视图：**正面、背面、左侧、右侧**。可以合成一张四宫格，也可以输出四张图，但四个方向必须齐全。
- 必须实际查看四视图，自检：缺块/缺面、悬空或断层、错误遮挡、正反面贴图或 UV 异常、侧面厚度、左右不对称、枢轴/层级错位、越界裁切、透明与剔面问题；不能只凭脚本 PASS、单角度预览或游戏截图判定模型完成。
- 有阶段、部件开关或形态变化的模型，至少对基础状态及本次改动涉及的每个关键状态各渲染一套四视图；有骨骼动画的模型还要选本次改动涉及的关键帧复查。
- 四视图预览必须保存在本次任务的 art/、build/verification/ 或任务记录目录，并在交付时报告预览位置与自检结论。若现有预览器不能输出四视图，应先补齐/修改预览器，禁止静默跳过。

## 环境/命令
- JDK17: `C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot`
- Gradle 8.8（cache 于 `%USERPROFILE%\.gradle\wrapper\dists\gradle-8.8-bin\*`）
- 构建（PowerShell）：
  `$env:JAVA_HOME=...; Set-Location -LiteralPath 'D:\ft工程文件\FT-SugarQueen-Boss-Mod'; <gradle.bat> build --console=plain --no-daemon`
- ★★ **删掉资源文件后必须 `gradle clean build`，`build --rerun-tasks` 不够**（2026-09-18 实测）：
  `build/resources/main/` 里会**残留**上一次构建拷进去、这次已从 `src` 删掉的文件，
  而 `processResources` **不清**输出目录 → 打出来的 jar 里仍然带着那些孤儿资源。
  本轮删 `apple_core.json/.png` 时中招：第一次 `--rerun-tasks` 重建后 jar 里**依旧有这两条**，
  只有 `clean build` 才真正消失（资源数 576 → 574、jar 条目 993 → 991）。
  **判别口诀**：删了资源却看到"条目数该降没降"，先怀疑 `build/resources` 残留，别怀疑构建脚本。
  同理适用于重命名资源（会新旧两份都在 jar 里）。
- ★ **同一工程并发 `gradle` 会互相踩缓存**：本轮为了不碰主工程的 `build/`，
  把 `src`+`libs`+`gradle` 复制到 `_work/<task>/build-project` 里构建。
  ★ 同步源码到这种隔离工程时**要用 `robocopy /MIR` 而不是 `/E`** ——
  `/E` 只复制不删除，你删掉的源文件会在隔离工程里阴魂不散（正是上面那个坑的放大器）。
- ★★ **在 `_work/<task>` 隔离工程里改完必须并回主工程**（2026-09-28 实际踩中，代价是两天实机版缺功能）：
  09-24 晚上那批改动（折耳根紫色残影/庄园场地区块常加载/Boss 水中移速/血腥契约只对敌对生物吸血/
  创造栏三叉戟与长矛）当时**只落在隔离工程** `_work/boss-tweaks-20260924/build-project`，没并回主工程；
  而 09-25~09-27 主线继续往前跑（万味胚刃/弓、武器进化、荆棘护甲…），于是从主线构建并部署的 jar 里
  那 5 个类（`ManorArenaChunks`/`ShadowPhantomEntity`/`ShadowPhantomModel`/`ShadowPhantomRenderer`/
  `SanguineLifestealGuard`）**一个都没有** —— 5 个部署点同哈希，看着"部署成功了"，其实悄悄回退了。
  **并回的做法**（见 `docs/CHANGELOG.md` 的「把 09-24 那一批改动并回主线」一节）：
  ① 用 SHA256 + mtime 逐文件比对两棵树，分成"只在我这棵树""两边内容不同""只在主线"三类
  （脚本 `_work/buff-guard-20260928/compare_trees.ps1`）；
  ② 对"内容不同"的每一份，先用**本批特征串**（如 `phantomEcho`、`MINION_CAP`、`WATER_SLOW_DOWN`）
  grep 两边，确认主线版本就是分叉基点（主线没有我的特征串、我有主线的特征串）→ 才允许整体覆盖；
  ③ 主线 mtime 晚于分叉点的文件**一律保留主线版本**，只把我的改动手工并进去（本轮是
  `check_manor_teleport.cjs` 的反向断言与 `verify_project.cjs` 的 8 条注册）；
  ④ lang/CHANGELOG 这类两边都在长的文件**按行/按键合并**（CHANGELOG 用 H2 标题做分界，
  脚本 `_work/buff-guard-20260928/merge_changelog.cjs`），绝不整份覆盖。
- ★★ **改 `FTBossMod.java` 不会进部署版 jar**（2026-09-28 查清，别在这上面白花时间）：
  `tools/merge_jar_extras.ps1` 的保留条目里有 `net/farmingtales/ftboss/FTBossMod.class`，
  合并时**用旧 jar 的那份字节码整份覆盖**新构建的（目的是保住血腥契约
  `integ/tridents/SanguineBondHooks` 的注册 —— 那一行只在旧 class 里，磁盘上没有对应 .java）。
  实测：线上 jar 的 `FTBossMod.class` 里仍有 `SanguineBondHooks`，而源码里早就没有它了。
  **所以新加的事件监听器一律在类上写 `@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)`**
  （Forge 扫 jar 完成订阅，不经过 `FTBossMod.class`；`logs/debug.log` 里有 94 条
  `Auto-subscribing … <ftboss 类> to FORGE` 佐证这条路是通的），不要指望在 `FTBossMod` 构造函数里
  加一行 `EVENT_BUS.register(...)`。要恢复"显式注册也生效"，得先把 `SanguineBondHooks` 那行补回源码、
  再把 `FTBossMod.class` 从保留条目里去掉，并连着验证血腥契约整套功能。
- ★ **部署目标（2026-09-19 修正后口径，共 4 处）**：
  ① `Desktop\qys-bosses.jar`；② `Desktop\FT经济-波动mod成品\qys-bosses.jar`；
  ③ `OneDrive\桌面\FT经济-波动mod成品\qys-bosses.jar`；
  ④ 实例 `versions\FarmingTales_ForgeⅡ v1.5.2_beta3\mods\qys-bosses.jar`（用户测试用这个实例）。
  - ⚠ **`OneDrive\桌面` 不是 `Desktop` 的 junction**（2026-09-19 实测：在
    `桌面\FT经济-波动mod成品\` 放探针文件，`OneDrive\桌面\FT经济-波动mod成品\` 看不到）。
    两者是独立目录，所以 ③ 必须单独写 —— 早期文档里"只写桌面两处就覆盖了"的说法是错的。
  - **不再写主实例 `versions\FarmingTales Forge\mods`**（旧口径的第 ① 处）。
  - 实例名里的 `Ⅱ` 是 **U+2161（罗马数字二）**，既不是全角字母 I 也不是 `II`；
    `deploy_jar.ps1` 里按码点拼出这个字，避免脚本编码/复制过程把它弄坏。
  - `deploy_jar.ps1` 会清 **152beta3** 与主实例两个 `lightspeed-cache`（主实例那个照清不误，
    它 mods 里还留着上一版 jar，免得哪天回去测试吃到旧资源索引）。
  - 旧名 jar（`qys-sugar-queen-boss-*.jar`、`qys-boss-*.jar`）一律删除，避免同名 mod 重复加载
  - ★ **2026-09-24 追加第 ⑤ 处：用户实际在玩的 `versions\FarmingTales_ForgeⅡ v1.5.2\mods\qys-bosses.jar`**
    （`deploy_jar.ps1` 的列表里**没有**它 —— 2026-09-23 21:52 那版 jar 就落在那里被实机测试，
    所以从那天起它必须一起更新；本轮是 deploy 脚本跑完后单独 `Copy-Item` 的，缓存也补清了）。
  - ★ **2026-09-24 起，从主工程重建 jar 之后必须再跑 `tools/merge_jar_extras.ps1`**：
    部署版里有 6 个「血腥契约 SanguineBond」条目（外加 `FTBossMod.class`、`SpellVisualRules.class`、
    两个 lang 的补键）**全盘找不到 .java**，用户口径是保留 —— 不合并就会把那些功能从新 jar 里丢掉。
    配套的 `tools/merge_lang_keys.cjs` 负责"只补键、不动已有值"（整份覆盖会把本轮改过的头衔风味文本冲掉）。
  - ★★ **写这个 jar 绝对不要用 .NET 的 `ZipArchive`（尤其 Update 模式）**（2026-09-24 实机崩溃）：
    它会把 jar 里 95 个**目录条目**重写成 **STORED + data descriptor**，Forge 的 `securejarhandler`
    只允许 **DEFLATED** 条目带这个标记 → ModLauncher **在 Forge 日志系统起来之前**就退出：
    `java.util.zip.ZipException: only DEFLATED entries can have EXT descriptor`。
    症状是 `latest.log` 只有 ~880 字节、**没有 crash-report / hs_err**，而 `jar tf`、7-zip、
    `java.util.zip.ZipFile` 都能正常读那个坏 jar —— 只能靠启动器「崩溃分析」里的 stdout 定位。
    现在 `merge_jar_extras.ps1` 走的是 **JDK 的 `jar --create --manifest`**，并在打包前后各跑一次
    `tools/check_jar_zip_rules.cjs` + `tools/verification/ZipStreamCheck.java`（后者与 Forge 同源）。
    自己另写脚本改 jar 时也照这个来：**打包只用 JDK 工具，改完必须过那两道闸**。
- ★ **部署前必须检查游戏是否在跑**（用户 2026-09-13 实测崩溃）：`java.exe/javaw.exe` 命令行里带 `forgeclient`/`--gameDir` 就是游戏。**就地覆盖运行中的 mods jar 会让 JVM 懒加载 class 失败**（`NoClassDefFoundError`），崩溃点还是"之后第一次用到那个类"的时刻，可能隔好几分钟，极难联想。已封装 `tools/deploy_jar.ps1`：**检测到游戏就只构建、拒绝拷贝**；一律走它，别手写 `Copy-Item -Force`。
- ★ **音频一律导出到桌面**（用户 2026-09-13 定规：**"以后音乐都导出一份"**；2026-09-14 追加：**"以后导出音频，现在导出一份"**，并明确目的是**"做成可以直接听的版本，允许我在游戏外测试音频"**）：每次新增/改动 `.ogg` 音乐后，除了入库构建，必须同时导出到
  `C:\Users\YOUR_USERNAME\OneDrive\桌面\FT-Boss-音频_<日期>\`，按下面**四个部分**导，并在同级写/更新 `说明.txt`：
  0. `00_直接试听_mp3\` —— ★**必带**：`.ogg` 是游戏格式但 **Windows 默认播不了**，`.wav` 太大，所以每首另转一份 **320 kbps MP3（双击即听）**。
     本机现成 ffmpeg（带 libmp3lame）：`C:\Users\YOUR_USERNAME\AppData\Local\oopz\ffmpeg.exe`
     ```
     & $ff -hide_banner -y -i <in> -c:a libmp3lame -b:a 320k -map_metadata -1 -af "volume=-0.6dB" <out.mp3>
     ```
     ⚠️ `volume=-0.6dB` 是为**电平对齐**：`audioToWav` 会把 wav 归一化到满刻度（比 ogg 高 0.58 dB），
     转新曲要回退掉，MP3 才等于"游戏里实际听到的"响度；转 jar 里的 ogg 时用 `0dB`。
     转完用 `-af volumedetect` 核 max_volume 别超 0 dB。
  1. `01_新增_xxx\` —— 本次新增/改动的曲子（`.ogg` + `.wav` + 渲染器源码，两版就都放）
  2. `02_模组内全部音频\` —— **每次都带一份全量**，从当次部署的 jar 里原样导出（保留 `music/`、`effects/` 结构）：
     `tar -xf <jar> -C <tmp> assets/ftboss/sounds` 再拷贝；导出后用 `tools\audio_tools\ogg_dur.cjs <ogg...>` 核时长、
     `tools\music\verify-ogg.cjs`（在 `tools\music` 下跑）核解码无误，把清单+时长写进 `说明.txt`
  3. `99_对比_xxx\` —— 需要 A/B 的旧版本（如被否的习作）放这里，并写清它差在哪
  ⚠️ **桌面真身是 `C:\Users\YOUR_USERNAME\OneDrive\桌面`**（注册表确认），不是 `C:\Users\YOUR_USERNAME\Desktop` —— 写错地方用户根本看不到（2026-09-13 已踩过一次）。
  ⚠️ 全量导出是**当次 jar 的现状**：还没接线的曲子（只在渲染目录里）要放进 `01`，并在 `说明.txt` 里注明"尚未进 jar"。
- ★ **导出后必须量"解码真峰"，>1.0 就是会削**（2026-09-14 实测）：这条链子（软削波 + 高频提升的亮母带）编成 Ogg Vorbis 会**必然过冲 ~2.5 dB** ——
  `manor_guignol` 母带浮点峰值 0.935，解码出来 **1.2501**。而参考曲（商业母带，做过真峰限制）解码峰值正好 0.00 dBFS。
  **换编码质量完全没用**（实测 q0.6/0.85/1.0 → 1.2646 / 1.2659 / 1.2879，`tools\ogg_overshoot_test.cjs`）；
  **编码前做低通也没用，反而更糟**（16k/15k/14k ×1~×2 低通 → 1.2848~1.3074，`ogg_overshoot_test2.cjs`）——
  过冲来自削波的时间域边沿，不是近奈奎斯特振铃。**唯一解法是整体降电平**：
  对成品 gain×0.804(−1.9 dB) → 真峰 1.2646→0.9912；重渲时改用 targetRms 0.234(−1.9 dB RMS) 只到 **1.0723**，
  真要 ≤1.00 得降到 0.218 左右(−2.5 dB RMS)。所以"响"与"不削"在土法链里不可兼得，要响就得认。
  量法：`cd tools\music; node verify-ogg.cjs <ogg>`（`peak` 字段就是游戏解码后的真峰；16bit 转换会硬削超过部分）。
  已知 jar 里就有的过冲: `manor_epic` 1.115 / `theatre_curtain` 1.117 / `defect_robot` 1.076 / `manor_battle` 1.043。
  ⚠️ 另: **`audioToWav`（spessasynth_core）会把 wav 归一化到满刻度**，所以 wav 的峰值/响度比同一份 ogg 高约 0.58 dB ——
  `wav_scan.cjs` 量 wav 得到的数字不能直接当成游戏里听到的电平，量 ogg 才准。
- ★ **写 MIDI 的 CC/自动化不能落在 0 号轨道**（2026-09-14 踩）：format 1 的 conductor track 只收速度/拍号，
  写 `cc(T.cond, ...)` 直接抛 `Can't add voice messages to the conductor track (0)`。CC 要按通道归到它自己的轨道上
  （弦乐→T.vln/T.low、合唱→T.org、铜管→T.brass、吉他→T.gtr…）。CC 本身是**通道级**的，放哪条轨道只影响事件顺序。
- ★ **"掉坑"（突然安静的高潮前留白）光靠少写音挖不出来**（2026-09-14 实测）：前段**混响尾巴**会把坑的前 ~3 秒糊住
  （坑内第 1 秒只有 −11 dB，第 3.8 秒才到 −22.8，坑短了就完全听不出）。做法：
  ① 进坑前 **2 拍收掉混响送** `cc(trk,ch,t0-beat(2),91,0)`；② 进坑前 **0.1 拍把长音通道表情归零** `cc(trk,ch,t0-beat(0.1),11,0)`；
  ③ 坑拉到 **≥3 小节**（第 1 小节给尾巴衰减、第 2 小节才是真坑、第 3 小节出来）；④ 出坑恢复 CC11=127 与各自 init 的 CC91。
  这样才拿到 −27 dB（比驱动段低 18 dB）。混响送/表情的"提前收干"本身也是很好的"抽真空"效果。
- **部署规则（用户 2026-09-08 确认）：每次代码更新构建出 jar 后，必须立即把 jar 同步到上述三处**（系统桌面=OneDrive\桌面）；大版本交付后再镜像源码工程到 `OneDrive\桌面`
- ★ **`deploy_jar.ps1` 必须用 `powershell.exe -ExecutionPolicy Bypass` 调用**（2026-09-18 踩）：
  本机执行策略禁脚本，直接 `& tools\deploy_jar.ps1` 会 `UnauthorizedAccess`
  （"running scripts is disabled on this system"）。可用的写法：
  `powershell.exe -NoProfile -ExecutionPolicy Bypass -File tools\deploy_jar.ps1 -NoBuild -ArtifactPath <jar>`

## 蛋蛋「末影封锁」为什么曾经形同虚设（2026-09-18 查证）
- 用户问「检查庄园 boss 的末影珍珠使用，看看是不是没触发过珍珠使用」。**查日志的办法**：
  把 `versions\*\logs\` 全部 487 个文件（含 `.gz`）逐行读一遍，按日志前缀归属来源 ——
  同一句话可能被两个类打印，只看文字会误判（本例 `末影珍珠落地` 在
  `EasterPearlEntity` 与 `BlackCleaverAndShadow` 里各有一份）。
- 结论：`DandanPearlBan` **注册是好的**（`FTBossMod` 第 55 行显式注册，不是靠注解），
  但它的拦截消息 `[FTBoss][Easter] 蛋蛋末影封锁拦截 …` 在全部日志里**一次都没出现**；
  同时蛋蛋自己的珍珠砸了 1095 次、玩家的裂瞳投珠放了 358 次。即：**封锁从未生效过**，
  因为玩家从没在蛋蛋 48 格内扔过**普通末影珍珠**，而模组自己的珍珠位移一条都不走那两条钩子：
  - 暗影剑右键：`new ThrownEnderpearl(...)`，手里拿的是**剑** → `is(Items.ENDER_PEARL)` 不成立；
  - 裂瞳投珠：自研 `ShadowPearlEntity extends ThrowableItemProjectile`，
    落地走 `ShadowSwordItem.teleportStrike` 里的 `ServerPlayer.teleportTo` → 连
    `EntityTeleportEvent.EnderPearl` 都不触发。
- **已修**：`DandanPearlBan` 新增 `blockThrow` / `blockLanding` 两个公开入口，
  三处显式调用（暗影剑起手/落点 AOE、裂瞳投珠起手/落地），
  回归脚本 `tools/verification/check_dandan_pearl_ban.cjs`。
- ★ 这类"钩子只认原版物品/原版实体"的漏法**通用**：凡是自研武器复刻了某个原版行为，
  都要回头问一句"这个行为原来是怎么被拦下来的，我的复刻还经过那条路吗"。
- ★ 查"某机制是否触发过"时，**别只搜中文提示语**：先确认那句话在源码里是不是
  唯一的、由谁打印的（`grep` 到两个类就说明不能靠文字归属）。
- ★★ **"某行代码在" ≠ "这个功能会生效"**（2026-09-20 v191 实测）：`check_qizai_sky_nova.cjs` 断言了
  "`setInvulnerable(true)` 存在"、"`setTarget(null)` 存在"，全部 PASS —— 而这两行**正是**让第八招
  「天陨星裁」整场放不出来的原因：那一招要的"无法选中"被 `QizaiMagic` 的通用看门狗当成"被冻住/失控"，
  起手后**下一 tick 就自我 `cancel()`**。实机判据是"日志里只有起手、没有落地结算"
  （22:53:56 / 22:55:36 两次起手 → 0 次「落地结算」）。凡是**技能 / 法术 / 演出**类功能，回归里至少要有一条问：
  **「它靠什么跑完整场？中途有没有哪个通用看门狗会把它掐掉？」**
- ★★ 同理，**只断言"枚举里有这门法术"不够，要断言"这门法术有承接的实现路径"**：同样是 v191，
  借来的辅助法术「灶火盛宴 / 光环圣所」在枚举里、也会被选出来、日志也报"放出去了"，
  但它们是**光环型**（`Field` 每 20 tick 结算），而 `Field` 的存活判据要求"施法者是庄园施法者 + 变体匹配"——
  祁崽(variant 11) 两条都不满足，光环生成后下一 tick 就被丢掉，只剩起手特效。
  样板见 `tools/verification/check_qizai_spell_liveness.cjs`：**从枚举反推分类**（直发 / 光环 / 第三批），
  再逐条查承接 —— 以后往枚举里加一门而忘了接路径，它会红。
- ★ 排查"某个法术没效果"时，**先看日志能不能区分"放出去了"和"生效了"**：v191 顺手把「四门齐发」
  的日志从"几门"改成**列出真实法术 id**（`star_cross(主) + rose_starfall + halo_sanctuary + tidal_lotus`）。
  日志只报数量的地方，静默失效永远查不出来。
- ⚠ **并发会话期间不要共用隔离构建树**（2026-09-19 实测）：两个会话各自 `gradle`
  会互踩缓存，而且 `robocopy /MIR` 会把对方**改到一半的源码**快照走 ——
  本轮就快照到 `ManorVictory.java` 里一个 `ItemEntity.setOwner(UUID)`（1.20.1 没这方法）
  导致编译失败。做法：每次构建**自己新开一个** `_work/<task>-<date>/build-project`，
  并把**快照时间点**打出来，好在出错时区分"是谁的锅"。
- ⚠ `gradle build` 会跑 `gradle/verification.gradle` 里挂到 `check` 上的校验任务
  （如 `checkManorVictoryGeometry`）。**别人功能没做完时，它会让整个 build 失败**，
  即使你改的文件全都编译通过 —— 这时别用 `-x <task>` 绕过去出包，
  那个守卫正是在拦"半成品进 jar"。

## 顶点缓冲铁律（2026-09-18，从两次实机崩溃里总结）- ★★ **每种 `RenderType` 每帧只能 `getBuffer()` 一次**，取到的 `VertexConsumer` 要**传进**
  绘制方法，**绝不能在循环（多个目标 × 多个片）里反复取**。
  `MultiBufferSource.BufferSource` 对同一 RenderType 返回的是**同一个正在构建中**的 builder，
  反复取用会让顶点元素计数错位 → `IllegalStateException: Not filled all elements of the vertex`
  （栈顶是 `BufferBuilder.endVertex`）。2026-09-16 22:20 的崩报就是这么来的
  （`ShadowSigilVisual`：X 个阵 × 4 个柱片的循环里取 buffer）。
  正确样板：`LemonCatSkillVisual` / `DandanScytheVisual` / `XuanlanWeaponVisual` —— "取一次 → 写完 → `endBatch` 一次"。
- 允许的例外只有一种：循环**每一轮都配一次 `endBatch`**（如 `YouziStatueVisual` 每个 aura 一取一冲），
  因为它每轮都会清空 builder。**判别口诀**：`endBatch` 在循环**外**、`getBuffer` 在循环**内** → 就是 bug。
- ⚠ 排查同类崩溃时**别只看本模组的类**：本次第二个崩溃
  （`Description: Rendering Particle`，2026-09-18 22:59）栈顶是
  `io.redspace.ironsspellbooks.particle.SoulfireRayParticle.drawQuad:111` —— **铁魔法自己的粒子有 bug**，
  但**生成它的是我们**（`IronSpellLightRules` 把 `PRESSURE_LANCE`/`ION_RAIL` 映射到 `Beam.SOUL_RAY`）。
  结论：**引用了别人的粒子/渲染类时，崩报里没有自己的帧也可能是自己触发的**，要顺着"我们有没有 new 它"查回去。
  `Beam.SOUL_RAY` 已退役，`tools/verification/IronSpellLightCheck.java` 有源码级守卫防止它复活。

## 核心目录
- `block/` 石像（statue，双格高 DoublePlant 方块，CandyStatueBlock）
- `boss/SugarQueenBoss.java` 女王战斗静态大脑（本体用铁魔法施法 + 波次召唤 + 图腾终局 + 血条）
- `boss/BossHandler.java` 事件（死亡/图腾、右键召唤、石像交易入口）
- `boss/ai/` 各 Boss 的战术组件（`TeaTactics` 沙耶召唤/汲取、`SayaBuds` 茶芽庇佑、`BossDamageGate`、`BossRage`、`MinionBlessing`）
- `entity/` SugarQueenEntity(继承铁魔法 AbstractSpellCastingMob) / MeatMountainEntity / TeaTreeBossEntity(沙耶) / 酒桶王 / 鼠群
- `sound/BossMusic.java` BGM 全局调度器（按开战 Boss 决定曲目组；三幕剧按血量推进）
- `client/BossMusicClient.java` 战斗期间压掉原版背景音乐 + 无 Boss 时兜底停乐
- `integ/WizardSummon.java` Iron's Spells 桥接（法术池 spellPool()）
- `trade/TradeHandler.java` 石像交易（config/ftboss_trades.json）
- 依赖（仅编译期）：`libs/irons_spellbooks*.jar`、`libs/geckolib*.jar`；运行期整合包提供

## BGM 结构（2026-09-10 起：按 Boss 分曲）
每场战斗只播一首，由**第一个开战的 Boss** 决定曲目组，中途加入的 Boss 不换曲（避免叠放）：
- **沙耶**(`tea_tree`) → `BOSS_MUSIC_SAYA` → `music/saya.ogg` = 自写《茶の唄》(`render_saya.cjs`)，整场一首不切
- **糖霜女皇**(`sugar_queen`) → `BOSS_MUSIC_GINGERBREAD` → `music/gingerbread.ogg` = 自写《木偶剧场·加洛普》(`render_puppet.cjs`)
- **其余 Boss** → 三幕剧 overture/masquerade/finale，按全场最低血量比例推进（只前进不回退，每幕最短 10 秒）；真死时奏 curtain

改曲目映射改 `BossMusic.groupOf()`；`FTSounds.BOSS_MUSIC_GINGERBREAD` 早先指向 `music/saya` 且**从未被播放**（女王走的是 `BossMusic.request`），2026-09-10 已拆成独立文件并真正接线。作曲工具与调参表见 `tools/music/README.md`。

- **跨死亡存数据不能用玩家 persistentData**: 玩家死亡重生时服务端会**新建 ServerPlayer 实例**, ForgeData 随之清空
  (实测: "连死 5 次"计数永远停在 1) —— 要跨死亡/重进/重启保留, 用 **`SavedData`**(`level.getServer().overworld().getDataStorage().computeIfAbsent(...)`)。
- ★★ **绝对不要用 PowerShell 往返改写 UTF-8 文本(尤其含中文的 JSON)**: `Get-Content -Raw` 按系统代码页(GBK)解码 →
  `Set-Content -Encoding UTF8` 写回, 会把中文变乱码并**吞掉引号**, JSON 直接损坏(2026-09-11 实测写坏 `lang/zh_cn.json`)。
  改文本一律用 fs 工具(edit/write)。**写坏后的恢复办法**: 从上一版已构建的 jar 里提取同名资源
  (`System.IO.Compression.ZipFile` 读 `assets/...`) 后 `WriteAllBytes` 覆盖, 并注意别留 BOM。
  ⚠️ **2026-09-14 又犯一次**(写坏 `_work/11boss-music/音频产出与接线.md`, 只为一个字符串替换): 教训是
  **连"只替换一个词"也别用 PowerShell**; 没有 jar 可提取的文本(md/笔记)只能靠
  `_work/11boss-music/tools/fix_double_encode.cjs` 反解(GBK 编回字节→按 UTF-8 走一遍, 落单字节标 □),
  但**约 2.6% 的字节是真丢了**, 得靠上下文重写 —— 所以最好的恢复是"别写坏"。
- ★ **javac 中文报错在控制台是乱码**(`����: [re\nmoval]`), 容易被误判成 deprecation 噪音;
  给 gradle 加 `"-Dorg.gradle.jvmargs=-Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8"` 就能拿到英文报错, 直接 `-match 'error:'`。
- ★ **原版没有 `ParticleTypes.GLASS_BREAK`**(那只是音效 `SoundEvents.GLASS_BREAK`): 玻璃碎裂用 `ParticleTypes.CRIT` + `ITEM_SNOWBALL`,
  或 `new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GLASS.defaultBlockState())`。
- ★★ **想要"圆球"就不能用 GeckoLib/geo**: Bedrock geo 只有长方体(`GeoCube`/`GeoQuad`)。三条可行路线:
  **A 代码 tessellate 经纬球**(本项目已实现 `client/render/SphereMesh.java`: 顶点+法线+等距柱状 UV, 配 `RenderType.entityCutoutNoCull` 画实体球、
  `RenderType.eyes` 画自发光内球; 也可作为 GeckoLib `GeoRenderer#renderRecursively` 的钩子挂在骨骼上 —— 那个钩子的 PoseStack 已在骨骼变换里, 半径用**格**;
  **⚠ 当前未启用**: 用户最终选了方块方案, 该文件留作备用);
  **B Forge OBJ**(`net.minecraftforge.client.model.obj.ObjLoader`, Blender 导真网格, 但 OBJ 无骨骼动画);
  **C 圆片 billboard**(带 alpha 的圆形贴图, 最省事但没体积感)。
  方块方案下要"尽量圆"的做法: **3×3×3 排布去掉 8 个角(19 cube)** 再按需叠加细节。
- ★★ **`defineSynchedData()` 的 super 规则(2026-09-11 实测崩服)**: 自定义实体重写它时——
  **`LivingEntity`/`Mob` 系子类必须调 `super.defineSynchedData()`**, 否则原版同步数据(生命值/旗帜/名条/姿态…)**从未注册**,
  一旦有人读 `getHealth()` 就 `NullPointerException: ... "dataitem" is null` → **服务器直接崩**
  (实测: `ChargedOrbEntity` 漏了 super, 生成充能球时崩在 `DefectOrbs.summonOrb`)。
  但 **直接继承 `Projectile` 的类不能调 super** —— `Projectile` 没实现该方法, 父级是 `Entity` 的抽象方法, 写了会编译报
  "abstract method defineSynchedData() in Entity cannot be accessed directly"(弹体没有额外字段, 留空即可)。
- ★★ **客户端要用的自定义实体状态必须走 `SynchedEntityData`**: 只存字段 + NBT 的话, **客户端实体是凭生成包新建的, 拿不到值**,
  渲染时永远用默认值(实测: 充能球的 `orbType` 没同步 → 五种球全渲染成默认的青蓝, 看起来"没有颜色区分")。
  做法: `SynchedEntityData.defineId(类.class, EntityDataSerializers.STRING)` + `defineSynchedData()` 里 define + getter/setter 读写 `entityData`。
- **javac 的 instanceof 模式坑**: `x instanceof T t` 在 **x 的静态类型本身就是 T** 时报错
  ("模式类型 T 是表达式类型 T 的子类型"), 例如 `LivingDeathEvent.getEntity()` 已返回 `LivingEntity`,
  `Projectile.getOwner()` 已返回 `Entity` —— 这时要直接赋值, 不要写 instanceof 模式。
- **单例 Boss 大脑的"多实例"做法**: 本模组女王/屠夫的剧情逻辑放在 static 字段里(单例),
  想让玩家能召多只时, **第一只保留静态大脑, 额外的做成"复制体"**: 实体上打一个 `cloneXxx` 标记,
  由实体自身的简化 AI(悬停/近战/随机施法)驱动, 并自带血条; 同时**所有"清理残留实体"的逻辑都要跳过复制体**
  (否则一召唤就被当幽灵删掉)。见 `SugarQueenEntity.tickCloneBrain` / `MeatMountainEntity.initCloneButcher` / `BossMercy`。
- **想让实体"消失但不给掉落/经验"**: 用 `discard()`, **不要**用 `kill()`/`hurt(Float.MAX)` ——
  后者会走 `LivingDeathEvent` → 跑战利品表、掉经验、触发掉落逻辑。

## 铁魔法集成要点（多次试错结论）

- **法术稀有度**: `io.redspace.ironsspellbooks.api.spells.SpellRarity` = COMMON/UNCOMMON/RARE/EPIC/LEGENDARY,
  `AbstractSpell.getRarity(int castLevel)` **随等级变化**(高等级才升到 EPIC/LEGENDARY), `getMinRarity()/getMaxRarity()` 可用。
  "狂暴时更倾向史诗/传说" 的实现: `WizardSummon.spellPoolRare(castLevel, minOrdinal)`(ordinal 3=EPIC 4=LEGENDARY),
  取不到自动回退整池; 建议用**概率**(本模组 70%)而不是全换, 免得 Boss 只会两三招。
- **火球投射物**(不发散方块伤害): `io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball(Level, LivingEntity owner)`,
  配 `setDamage(float)` / `setExplosionRadius(float)` / `shoot(Vec3 dir)`(父类 `AbstractMagicProjectile`); 不要用原版 `LargeFireball`(会炸地形)。
- **投射物 + GeckoLib 渲染**: `GeoEntityRenderer<T>` 的泛型上界是 **`Entity & GeoAnimatable`** ——
  自定义投射物想用 GeckoLib 渲染就必须 `implements GeoAnimatable`(instance cache + 空的 controllers),
  否则编译期报"类型参数不在类型变量 T 的范围内"。**投射物直接复用某个 Boss 的 geo/贴图**(缩放 + 渲染器里翻滚)是省美术的好办法,
  见 `BarrelCannonballEntity` / `BarrelCannonballRenderer`(就是缩小 0.18 的巨桶)。
- **爆炸威力**: 原版苦力怕 3.0 / **带电(闪电)苦力怕 6.0**; `level.explode(src, x,y,z, 6f, Level.ExplosionInteraction.NONE)`
  就是"闪电苦力怕伤害但不毁方块"(要拆地形用 `BLOCK`)。
- 本模组用的 debuff/效果 id 大多是 **armamentarium** 的: `blood_loss`(流血, 伤害类型是 `armamentarium:hemorrhage`)、
  `pure_venom`(纯净毒液)、`clipped`(断筋)。Boss 侧统一由 `BossDamageGate`(伤害) + `MobEffectEvent.Applicable`(效果) 拦。

- `MagicData.initiateCast`/`WizardAttackGoal` 对非铁魔法原生实体不可靠；
- 可行方案：实体继承 `AbstractSpellCastingMob`，由我们每 tick `setTarget(玩家)` + 手动 `initiateCastSpell`（施法前尝试反射绑定 `TargetEntityCastData`），铁魔法自动完成咏唱结算；
- `integ.WizardSummon.spellPool()` = SpellRegistry 反射法术池。

## 已知工程性坑（必须遵守）
- **像素质子缓冲写入前必须夹到 0..255**（2026-09-10 实测）：`Uint8Array` 会**静默取模**（`256→0`、`257→1`）。
  当明暗公式让 `base*k` 超过 255、而 r/g/b 在不同亮度上越界时，就会写出**五颜六色的杂点**
  （实测把亮白写成青色/蓝色斑点，石像图标每张 15~35 个）。已在 `png.cjs` 的 `Canvas.set` 里统一 clamp。
- **重新生成"成品已被手改过"的模型之前，必须先与仓库逐文件比对**（2026-09-10 又踩一次）：
  石像 `statue_rat_swarm_upper` 在仓库里只有 1 个 cube（早先"上半格越界/空"的占位修复），
  而 `gen_statues.cjs` 重出一遍会变成 13 个 cube —— 几何被悄悄改回去。
  **做法**：生成到临时目录 → 与 `src/main/resources/...` 逐文件 diff → 只覆盖本次真正要改的文件类型；
  改坏了就从**上一版构建出的 jar** 里取回旧文件（jar 是最可靠的历史副本）。
- **铁魔法(3.16.3)的物品 API 三个坑**（2026-09-10 实测）：
  1. `StaffItem` 的 `(Properties,double,double,Map)` 重载**编译期不可用**（标为待删除，javac 直接报"找不到符号"）。
     要做法杖就继承 `io.redspace.ironsspellbooks.item.CastingItem` —— 右键施法行为就在它里面 —— 属性自己在
     `getDefaultAttributeModifiers` 里给。
  2. **`getDefaultAttributeModifiers(slot)` 返回的多半是 Forge 的不可变 multimap，`base.put(...)` 会在运行期抛异常**。
     永远 `HashMultimap.create()` → `putAll(super…)` → 再 put 自己的，并缓存。
  3. 铁魔法的"**法术槽**"是**物品自带的 `ISpellContainer`**（法术书/卷轴/法杖共用）：
     `ISpellContainer.create(槽数, 是否需装备, 是否接法术轮盘)` + `set/getOrCreate/isSpellContainer`。
     `MagicData` 里**没有**槽位 API（别去翻）；升级法术书槽位的是 `SpellSlotUpgradeItem` + `ArcaneAnvilMenu`。
     另外属性一律**懒加载**（`AttributeRegistry` 在构造期不保证就绪）。
- **跨 mod 兼容优先用"名字反查 + 数据包标签"，别加编译期依赖**：农夫乐事不在 `libs/` 里，
  所以"兼容菜刀"是 `ToolAction.get("knife_dig"/"knife_harvest")` + `data/forge/tags/items/tools/knives.json`
  与 `data/farmersdelight/tags/items/tools/knives.json`（`replace:false` 合并），不 import FD 的类。
- **armamentarium 效果 id 速查**：`blood_loss`=流血 · `vulnerable`=易伤 · `clipped`=剪断/禁飞 ·
  `sluggish`/`lethargy`/`fatigue`/`fragile`/`restrained`… 其语言文件**只有 en_us**（整合包汉化包不含它），
  所以游戏里显示英文名 —— 用户说的中文效果名可能是转述，实现前先在 jar 的 `en_us.json` 里核对。
- **生成物与生成器必须同步，否则"重新生成"会悄悄回退已修好的 bug**（2026-09-10 实测）：
  屠夫 `ButcherBossModel.java` 被手改修过穿模，而 `tools/model3d/gen_butcher.cjs` 停留在旧版 ——
  它 emit 的 `setupAnim` 里还写着 `this.body.y = 10.0F`（正是当初"躯干下沉 7 单位插进腿里"的那行），
  SPEC 里也缺"肩宽 23→20 / 腰带 20→19 / 手臂枢轴 11→12 / 大腿 9→8"这四处修复。
  **规矩**：改生成物就当场回写生成器（或只改生成器再生成）；开工前先跑一次生成器与仓库成品 `Compare-Object`，
  差异必须只有你本次想改的东西。屠夫的规格真源已抽到 `tools/model3d/butcher_spec.cjs`。
  ★ **2026-09-15 又踩一次（这次是图标生成器）**：只想改黑切的平面图标 → 跑 `gen_weapons3d.cjs`
  （它一次重出**全部**武器，没有按 id 过滤的参数）→ 它顺手把 `models/item/cheese_wheel_mace.json`
  从 13608 B 写回 7259 B（= `ref/cheese_wheel_mace_before_bluecheese.json`，**蓝纹奶酪那次手改被冲掉**）。
  **规矩**：没有单件过滤的生成器，一律"**先全量备份 → 跑 → 逐文件 diff → 把不属于本次目标的文件回退**"。
  本次做法可直接抄：备份 `models/item` + `textures/item` → 跑 → SHA256 逐文件比对 → 只保留 black_cleaver*。
- **穿模要按"动画姿势"查，静态查不出来**：`tools/model3d/fit_check.cjs` 复刻 `setupAnim` 的旋转/位移写进矩阵链、
  逐 cube 求交（比 `check_clip.cjs` 的逐骨骼 AABB 精确），并可导出各姿势 geo 用 `render_tex.cjs` 渲染验证。
  本次据此查出并修掉：走路时大腿前摆穿过围裙/挂钩（穿透 0.26–0.42 格）、挥砍时右前臂穿过大腿、静止时手掌压进靴子。
- **Bedrock 的骨骼 `pivot` 是绝对模型坐标，Java 的 `PartPose.offset` 是相对父骨骼的**：两者互转要逐级累加父偏移，
  否则嵌套骨骼（头/手臂）一旦带旋转就会绕错点。
- **Boss 专属 BGM 的接线三处**（新增一首曲子时别漏）：`sound/FTSounds`（注册 SoundEvent）、
  `assets/ftboss/sounds.json`（指向 ogg + 字幕键）、`sound/BossMusic`（`Group` / `groupOf` / `trackEvent` / `allTrackEvents` / `describe`）。
  **还有第四处、也是最容易漏的：该 Boss 的「实体」必须每 tick 调用 `BossMusic.request(level, this)` 并在死亡/移除时 `release`**
  —— 2026-09-10 三首新曲"像是没导入"，真因就是屠夫/酒桶王/鼠群三种实体从来没有请求过音乐（调度器收不到登记），
  ogg 文件与 sounds.json 全都没问题。多实体 Boss（鼠群）只让**首领**请求，否则同类型的小怪会各自请求造成叠放。
  五王各有专属单曲后，`Group.THEATRE` 已退为"未登记 Boss"的兜底，别删那几首三幕剧。
- **方块加"运行时着色"**（石像上色）的做法：方块状态加 `IntegerProperty tint` + 模型每个面加 `"tintindex": 0`
  + 客户端 `RegisterColorHandlersEvent.Block` 按状态返回颜色 —— **不需要为每种颜色各出一张贴图**。
  `blockstates/*.json` 里只写了 `half=lower`/`half=upper` 时，未列出的属性视为通配，**不必为新增属性改 blockstate**。
- **模型/动画改完必须跑校验器**（`tools/model3d/README.md` 有全表）：
  `validate.cjs` 查结构、`validate_anim.cjs` 查动画**并抓"定义了但代码从没调用"的死动画**（茶芽小妖的 attack/pollen_burst/death 就这么白放了很久）、
  `check_clip.cjs` 查骨骼穿模、`inspect.cjs` 查模型与碰撞箱比值、`audit.cjs` 做全工程体检（无引用资源/语言键/data 悬空/无用 import/死成员）。
  **不要在没跑预览的情况下声称模型没问题**：`preview.cjs` 出线框、`render_tex.cjs` 出带贴图四视图（box-UV 与逐面 UV 都支持）。
- **GeckoLib 动画的 `position` 单位是 1/16 格（模型单位）**：`[0,2,0]` 只有 2 像素，别当成 2 格；1 格要写 16。
  另：`loop: "hold_on_last_frame"` **不是**循环；校验循环闭合时别把它算进去。
- **同一实体类型里"大小两种角色"要用 `getDimensions(Pose)` 分档**：鼠群的大老鼠 0.6x0.975、小老鼠 0.4x0.6；
  之前共用 0.6x0.975 导致小鼠碰撞箱比模型大 2.4 倍（能打到空气）。
- **Boss 清场只能用 `discard()`，不能用 `kill()`**（2026-09-10 定位）：
  `LivingEntity.kill()` 走的是 `hurt(genericKill, MAX)`，会经过实体自己的 `hurt()` 覆写（限伤 `BossDamageGate`）、无敌帧、`isDeadOrDying()` 早退、以及 `LivingDeathEvent` 钩子，**任一环节拦下就清不掉**。
  后果实证：`MeatBoss.cleanup()` 用 `kill()` 后，退出游戏时屠夫没被移除 → 存进存档 → 下次进世界加载出一只"残留屠夫"（日志里表现为入世界时就出现一条 `首次渲染 血量=0.0/…`，而真正的 `屠夫 召唤` 在几秒之后）。
  现在：`MeatBoss.cleanup()` 用 `discard()`，另有 `purgeStrays()`（召唤前 + 每 200t 兜底）与 `EntityJoinLevelEvent` 里的 `isBoss()` 判定三道清理。
  **注意登记顺序**：`tryStart` 必须在 `addFreshEntity` **之前**写好 `active/bossUUID`，否则入场事件里的"无主屠夫清理"会把刚召唤的本体一起删掉。
- **召唤失败提示必须按真实原因分别给**：`MeatHeartItem` 曾把"任何失败"统一显示成"屠夫已在战斗中，或此处无法召唤"，而 `MeatBoss.tryStart` 里另有一条"糖霜女皇在场就拒绝召唤屠夫"的互斥门槛；两条都是 actionbar，后者覆盖前者 → 玩家只看到"已在战斗中"，误判成"有个看不见的屠夫"。
  **该互斥门槛已于 2026-09-10 按用户确认取消**（五个 Boss 允许同时在场）。新增 Boss 时不要偷偷再加跨 Boss 门槛。
- **Boss 必须有顶部血条**（`ServerBossEvent` + `BossBars.track`）：屠夫此前只有 actionbar 血量，导致"实体没同步/模型没渲染"时玩家无法判断实体在不在场。有血条后，"有没有血条"即等价于"实体在不在场"。
- **EMF 与屠夫：先前"屠夫消失 = EMF 的 allowedCEM"的结论已被反编译推翻，不要再沿这条线排查。**
  证据（EMF `entity_model_features-3.2.4-1.20.1-forge` 的 `EMFManager.injectIntoModelRootGetter` 字节码）：
  第一道判定是 `if (!"minecraft".equals(layerLocation.getNamespace())) { if (!allowedCEM.allowsUnknowns()) return; }`。
  屠夫的模型层是 `ftboss:butcher#main`，命名空间=`ftboss` → 判为 unknown → `KNOWN_ONLY` 下**直接 return，根本不接管**。
  而 `isModelDisabled(modelsNamesDisabled)` 这个检查在其**之后**才执行，所以：
  1) `modelsNamesDisabled` 对屠夫是**空操作**（EMF 每次启动会回写该配置并清掉未知实体条目，属正常，不必再补）；
  2) 只要现场没有资源包提供 CEM 模型（当前 `options.txt` 的 `resourcePacks:[]`，唯一带 cem 的 mod 只改了鱼），EMF 就不可能替换屠夫模型。
  `allowedCEM` 保持 `KNOWN_ONLY` 仍有价值（防止以后装 Fresh Animations 类资源包时被接管），但它**不是**"屠夫消失"的原因。
  屠夫诊断已埋点：渲染器会各打一次 `[FTBoss][屠夫诊断] 模型层 … 部件数=N` 与 `… 首次渲染: 隐身=…`，按这两行定位。
- **实体血量不要只改 `createAttributes()`**：`MeatBoss.MAX_HP` / `SugarQueenBoss.MAX_HP` / `TeaTreeBossEntity.MAX_HP` 这类常量会被 `setHealth(MAX_HP)` 覆盖生成血量、也是 HUD 与二命复活血量基准，改血量必须同步改这些常量。
- **Lightspeed 的资源索引会让"新加的资源文件"加载不到**（本项目反复踩）：
  症状是日志里 `[Lightspeed-Reload-N/WARN] [SoundManager/]: File ftboss:sounds/... does not exist` 或贴图紫黑。
  它把 per-pack 索引持久化在实例目录 `lightspeed-cache/`（含 `namespaces/*.ser`、`resourceLists`）。
  **必做**：加/换资源文件后，先关游戏，再删 `lightspeed-cache/`（或跑实例根目录的 `FTBoss-清理资源缓存.bat`），并保持
  `config/lightspeed-common.toml` 的 `cacheResourceExistence = false`；若仍失败，把 `parallelResourceLookup` 也置 false。
  **客户端进程启动后 EMF/资源索引都不会再重读，任何资源/配置类改动都必须重启客户端才算数。**
- **该文件批量/大段 edit_file 后偶发"尾部内容重复"**：每次编辑 SugarQueenBoss.java 后必须检查文件尾是否在最后一个 `}` 后有垃圾，若垃圾需按 ownerIdOf 标记截断；编译报错先查尾。
- 不要整篇"凭记忆重写"未被当前工作区证实的旧内容——先读文件。
- 石像资源路径族（blockstates/statue.json、models/block/statue_a|b.json、textures/block/candy_statue.png）改动需保持引用一致。
- 改名部署时注意 ModernFix(LightSpeed) 动态资源缓存：同名同路径资源改内容可能继续读到旧索引，必要时换新文件名或清实例下 `modernfix` 缓存目录。
- **并发写同一工程会互相踩**：另一个会话/代理同时改这个工程时，双方都会各自 build+部署同一个 `qys-bosses.jar`，谁后写谁覆盖，且四处部署点会不一致。开工前先确认没有别的会话在改（看源码 mtime 是否还在跳）。

## 三维模型工具链（tools/model3d，2026-09-10 起）
- 位置：把生成器与校验脚本放在 `tools/model3d/`（当前实现基于 Node + 自写的 PNG 编解码，**无外部依赖**）。
- **原则：模型不要手写数字对齐**。一份规格(JS)同时产出成品与预览数据，改参数即重出，避免"几何与预览走样"。
- `preview.cjs <geo> <out.png> [箱宽] [箱高]`：出四视图(正面/左侧/俯视/等轴) + 16 单位网格 + **碰撞箱线框**，并打印包围盒与"模型/碰撞箱"比值 —— **做模型前必看这个比值**，酒桶王就是因此查出模型只有箱子一半大。
- `validate.cjs <geo> [必需骨骼名]`：骨骼名完整性、父子引用、尺寸合法性、UV 整数与越界。**改完模型必须跑**。
- 贴图生成的坑(都踩过)：像素缓冲下标必须是整数，浮点 UV 打包坐标会让 `Uint8Array` 静默丢弃写入 → 整片材质空白；box-UV 布局里每面在 `(u+d, v+d)` 起的区域，画五官要按这个算，且**不要多个不同尺寸的 cube 共用一个材质**（会串脸）。
- 大模型注意：碰撞箱由 `FTEntities.register(id, factory, 宽, 高)` 决定，模型尺寸应与之匹配；**改模型尺寸时同步核对这个比值**。
- **小模型取景**：`render_tex.cjs` 的面板/缩放可用环境变量放大，小模型不放大根本看不清五官：
  `FT_PANEL`(面板边长, 默认 250) / `FT_SCALE`(每单位像素, 默认 4.2) / `FT_OY`(脚底在面板内的 y, 默认 210)。
  例(小姜饼人)：`$env:FT_PANEL=620; $env:FT_SCALE=26; $env:FT_OY=520`。
  另有 `zoom_tex.cjs <in.png> <out.png> [倍数]` 把**贴图**最近邻放大，用来核对"五官到底画在 box-UV 的哪一面"。
- **两个 cube 的面共面会闪烁**（小姜饼人实测）：头骨正面与领口正面都在 z=-3 → 交界处一条闪烁带。
  修法=让其中一个内缩 0.5。袖子/手臂这种关节位则用"埋进躯干"或"留 0.2 缝"，**不要正好贴住**。
- ★★ **"装饰件贴在主体上"必须**每个轴**都错开 ≥0.2 单位**（2026-09-14 benger 三处连爆，用户报"贴图叠加"）：
  共面闪烁最容易踩的**不是**并排的方块，而是**故意嵌在一起**的装饰件 —— 实测三处：
  ① 金牌正面 `z=-19` 与白喉正面 `z=-19` 完全重合 → 金块和白毛互相闪（修：金牌挪到 `z=-20.6`，凸到项圈**前面** 0.8）；
  ② 木棍金箍与棍身**上下前后四个面**全重合（只有左右差 0.2）→ 修：金箍四周各放大 0.2；
  ③ 小腿与大腿**同宽同 x**（侧面重合）、爪与小腿同理 → 修：小腿内缩 0.2、爪子外扩 0.3，交错开。
  **口诀**：任何两块共边界的方块，**每个轴**都要有 0.2 ~ 1 单位的差；只在一个轴上错开是不够的。
  （实体按固定顺序绘制所以"看着没事"，但同平面的两个面必然 z-fighting；`validate.cjs` 查不出这个，只能靠这条规矩。）
- ★★ **cube 的 `rotation` 是绕"立方体自身中心"转的 → 用它拼接的链条一定会裂**（2026-09-14 benger 尾巴"断开"）：
  我给尾巴 7 段每段加递增 `rotation` 想做出曲线，结果段与段之间被转开了（用户报"游戏内尾巴模型断开了"）。
  **正解**：几何体**不要旋转**，用"每段埋进上一段 2~3 单位"的**重叠串联**；曲线交给**骨头**——
  把每节骨的 `pivot` 放在**两段的交界处**，动画里逐节旋转就等于绕关节转，天然连成一体（尾巴/脖子/披风都照这个做）。
  `rotation` 只用于**孤立的小件**（单块斜贴毛片），且必须保证它仍与母体重叠 ≥2 单位。
- ★★ **肢体必须做成"关节骨链"，一根骨头一条腿会"移动很奇怪"**（2026-09-14 用户对 benger 的反馈）：
  正解是 `leg(大腿, 枢轴在髋/肩) → shin(小腿, 枢轴在膝) → paw(爪, 枢轴在踝)` 三段，
  几何也按这三段切（相邻段重叠 1~1.5 单位，关节处不断开），并给小腿**自动派生"与大腿反相、半幅"的膝盖键**
  （benger 的 `gen_benger.cjs` 里那段 `SHIN_OF` 循环就是模板）。膝盖一有键，走/跑/扑就是"折叠-伸展"，
  否则整条腿硬摆，玩家一眼就看出怪。躯干同理：**胸 → 腰 → 臀**至少三节，配相位延迟的摆动键才有"扭腰"感。
- **`check_clip.cjs` 报的骨骼重叠不全是 bug**：手臂/腿的顶端按要求"埋进躯干 0.5~1 单位"以避免共面闪烁时，
  它一定会在骨骼 AABB 层面报重叠（关节处），这类是**有意为之**；真正要看的是一次性动画摆动时会不会甩出体外。
- 动画：一次性动画的 `animation_length` 必须与 Java 里 `T_ATTACK/T_HURT` 的卡帧 tick 对上
  （0.5s↔10t / 0.4s↔8t），否则要么被腰斩要么卡住不动；死亡动画用 `"loop":"hold_on_last_frame"` 保持倒地姿势。
- **"倒地"动画必须补偿高度**：绕 x 轴转 90° 后，模型的 y' 范围变成原来的 **z 范围**，
  小姜饼人 z 是 ±4 单位，所以末尾要把 `root` 的 position.y 抬 **+4**，否则半截陷进地里。
  写完用 `bake_pose.cjs <geo> <out.geo> rotX=90 posY=4` 烘焙成静态 geo 再 `render_tex.cjs` 出图核对（它会直接报"陷进地面 x 单位"）。
- Blockbench MCP：插件(`blockbench-mcp-plugin` 1.6.1)会在 Blockbench 内起 MCP-over-HTTP 服务(`http://localhost:3000/bb-mcp`)，可直接用 HTTP JSON-RPC 驱动，无需 MCP 客户端。但 Blockbench **不扫描 plugins 目录**，须在 GUI 里 `File > Plugins > Load Plugin from File` 加载，且它会请求网络权限。

### 纹理像素密度标准（2026-09-16 定，工具 `check_pixel_style.cjs`）
**"贴图太大"不是看 PNG 尺寸，是看同一个面铺了多少纹理像素。** 同样长 16 模型单位的刃：

| 对应纹理长度 | 像素密度 | 视觉倾向 |
|---|---:|---|
| 16 px | **1 px/模型单位** | 接近常见原版纹理尺度 |
| 32 px | 2 px/模型单位 | 适度精细 |
| 64 px | 4 px/模型单位 | 容易出现细碎纹理 |
| 128 px | 8 px/模型单位 | 与原版环境的尺度差异明显 |

- **项目标准**：主体目标密度 **1~2 px/模型单位**；刃口/魔眼允许局部精细，**超过 3 列为复查项**；
  同一面横纵密度差 **>1.5 倍**提示纹理拉伸；单纹素小于一个屏幕像素提示易闪烁。
- 工具**只提示，不自动缩图、不改 UV**：`node tools/model3d/check_pixel_style.cjs <geo> <贴图>`。
- ⚠ **整模汇总值会被"光效种子块"带偏**：`blade_glow`/`eye_glow` 那种 `size=0.05` 的定位小块，
  随便给个 24×24 的 UV 就是 **480 px/单位**，会把 p90 拉成荒谬值。判断看报告里的
  `body_excluding_glow` 与 `by_bone` 两段，别看总分。
- 实测（2026-09-16）：暗影剑**主体中位 12.54 px/单位**（超目标 6 倍），最密的可见件
  `eye_mount 70.47`（占 58 单位面积）、`crystal_link 66`、`pupil 37`、`crystal 21.7`；
  刃身/握柄/爪尖在 10.5~19.9。
- **怎么改**（用户定的方向，**不要直接把整张图从 256 缩到 64**）：
  ① 先降**主体大面的有效细节密度** —— 黑铁保留成块明暗、裂纹减少分叉、刃口用连续亮边、魔眼保留少量高光；
  ② 要真正把 px/单位 降下来就得**缩 UV 岛并重新打包图集**，改完必须逐面复核贴图对齐；
  ③ 已经手绘精修的版本**保留、逐面调**，别让"重新生成"覆盖手绘内容。
- 配套工具：`shadow_sword_tex_fix.cjs`（明度，治"一团黑"）、
  `shadow_sword_de_densify.cjs`（颜色台阶量化 + 去噪点，治"碎杂"；**不会改变 px/单位**，那是 UV 决定的）。
- ⚠ 重跑 `regen_shadow_sword.cjs` 会覆盖上面两项手改（已在脚本写盘处加了提示）。

### 手持位置怎么定（2026-09-15 定下的流程）
物品 `display` 的旋转/平移作用在**手部局部空间**，那个空间的基（轴指向、Y 朝上还是朝下）由渲染管线决定。
**离线工具里没有它，所以"离线算出来的握持高度"一定和游戏里差一个常数** —— 别拿离线数字直接改 `display`。
- 几何变换的唯一真源是 `cube_space.cjs`（`cubeCorners` / `getBoneChain` / 子→父骨骼链 / 隐藏骨骼 / `display`），
  `hand_check.cjs` 与 `check_shadow_struct.cjs` 共用它，**不要再抄第三份变换函数**。
- `hand_check.cjs`：第三人称比例参考图（玩家参照体 + 剑 + 手心红点），把偏差**打印成数字**；
  图上会把剑显式平移到手心并注明"红点必然重合"，所以图不能当"握持正确"的证据。
- `shadow_sword_pos_audit.cjs`：位置专项审计（原点是否在握点 / 动画是否碰握持链 /
  与旧静态版同一物理点对照 / 实机标定步骤）。
- **实机标定层**：`client/render/SwordAxisDebugLayer`，加 JVM 参数 `-Dftboss.debugSwordAxes=true` 开启，
  在游戏里画出 display 原点（白十字）、红+X/绿+Y/蓝+Z 三根一格长的轴、沿 +Y 每 8 单位的刻度。
  手持截图即可读出"手心在原点上方/下方几格"，据此调 `translation.y`；绿轴指向剑尖还是柄尾
  顺便确定 Y 的朝向 —— 这是闭环这一环的唯一办法。
- ★ 两版 `display` **不能互抄**：暗影剑从静态 v18（原点=柄尾）换成 GeckoLib 骨骼版（原点=握柄中部，高 2.67 单位）时，
  两版 `trans.y` 只差 0.5，但原点约定差了 0.167 格。要比就比**同一个物理点**落在哪。
- ★★ **Boss/怪物手里的物品，姿态由"实体模型的手臂骨"决定，不是物品 display**（2026-09-15 实机定位）：
  折耳根（`EasterBossRenderer` = `MobRenderer` + 自定义 `EasterBossModel extends PlayerModel` + 原版
  `ItemInHandLayer`）持暗影剑时看着"剑往外飘 + 角度不对"，我一开始去调 `item/shadow_sword.json` 的
  display —— **方向错了**。真凶是 `EasterBossModel.applyCastPose` 里那段专属姿态：
  `rightArm.xRot=-0.785f`（前上 45°）+ 向外 0.30，叠加剑自身 `rot.z=28°` → 剑被推成"横着往外戳"。
  **举剑时前臂本来就该基本下垂**，剑的立/斜交给 display；手臂再前抬 45° 只会把剑推平。
  调参项已提成 `SWORD_POSE_X_ROT / Y_ROT / Z_ROT` 三个常量，改完 `gradle build` 即可。
  区分办法：**看日志谁在拿** —— `[FTBoss][Easter] 折耳根 装备武器: 目标=ftboss:shadow_sword`
  说明是 Boss 实体；玩家手持则看 `display`。两条路径完全不同。
  另：`PlayerModel` 右臂符号约定（见 `arm()` 助手的 `-sign*inward` / `sign*inward`）——
  **右臂 `yRot` 取正 = 往体内收，取负 = 往外张**；`zRot` 取负 = 往外。

- **item 模型与方块模型的 textures 写法不一样**（2026-09-10 实测踩坑）：方块模型的面可以写
  `{"texture":"...","tintindex":0}`，但 **item 模型（`parent: item/generated`）的 `textures.layer0` 只能是字符串**；
  写成对象会让客户端在 `JsonElement.getAsString` 抛 `UnsupportedOperationException(JsonObject)`，
  整个模型变 missing（日志：`Couldn't load model ... substituting missing`）。物品染色**不需要**写 tintindex
  —— 原版给 generated 生成的 quad 自带 tintindex 0，`ItemColors` 直接作用（皮革甲/刷怪蛋同理）。
  `audit.cjs` 的 **[8]** 就是查这个。
- **`DoublePlantBlock` 系列（石像）改状态时不要把 `HALF` 写丢**：`StateDefinition.any()` 是"**每个属性取第一个可能值**"，
  而 `DoubleBlockHalf` 枚举顺序是 `UPPER, LOWER` → `any()` 给的是 **UPPER**。父类构造里显式写了 `LOWER`，
  子类若再 `registerDefaultState(any().setValue(其他属性...))` 就会把 HALF **覆盖回 UPPER**，
  后果是 `BlockItem` 拿着上半格去放、`setPlacedBy` 又叠一个上半格 → **方块怎么点都放不下来**。
  子类必须写 `.setValue(HALF, DoubleBlockHalf.LOWER)`。
- **"整幅贴图 × 单色乘色"等于纯色**（石像 v1 的教训）；**手绘花纹贴图 ≠ Boss 本人**（v2 的教训）：
  要让雕像"就是那个 Boss"，正解是**复用 Boss 自己的 geo + 贴图**：把 Bedrock 的 **box-UV 换算成 Java 的逐面 UV**
  （`gen_statue_real.cjs`）。要点：
  ① Java 的 uv 是 **0..16 单位**（16 = 整张贴图），贴图像素要乘 `16/贴图尺寸`；
  ② box-UV 行1 = `up[u+d,v]`、`down[u+d+w,v]`，行2 = `east[u,v+d]`/`north[u+d,v+d]`/`west[u+d+w,v+d]`/`south[u+d+w+d,v+d]`；
  ③ geo 里 **box-UV 数组** 与 **逐面 UV 对象**(`uv`+`uv_size`)两种写法都要兼容；
  ④ 按 y=16 切半格时，4 个竖直面的贴图 **v 范围要按比例切**；
  ⑤ 四足/长条模型(大鼠)按 1 格缩放会变成细线 → 该砍的部件(尾巴)就砍掉，或把水平上限放宽到 ~1.25 格。
- **预览器的正面视图"屏幕右"是 -x**（不是 +x）：Java `BlockFaceUV.getU/getV` 的语义是"贴图 u1 列落在该面的屏幕左侧"
  （从外面看这个面时贴图不镜像）。用 +x 投影会让预览左右翻转，白白怀疑自己的转换出错。
- **密集模型画图标时，描边必须紧接着自己的填充**：把所有面填完再统一描边，会让重叠的边线**互相叠加**，
  91 个 cube 的酒桶王图标中间会糊成黑块。正确顺序 = 逐个面「填充 → 高光 → 描边」（画家算法）。
- **有 blockstate 但没有 `loot_tables/blocks/<id>.json` 的方块，挖掉什么都不掉**（石像漏了很久）。
  新增方块时记得配战利品表；`audit.cjs` 的 **[9]** 会查。
- ★ **改"默认外观"时别复用旧状态值**（石像"新外观没生效"的真因）：方块状态**存在存档里**，换贴图/换模型不会改它。
  早期版本用 `tint=0` 表示白膜，于是所有**已放置**的石像永远是白膜，玩家看到的就是"改动没生效"。
  正确做法是**新增一个属性**（如 `white`，默认值 = 想显示的新外观）—— MC 的 `BlockStateParser` 对缺失属性取默认值，
  旧存档的方块会自动落到新默认值上，不用让玩家拆了重放。选模型只认新属性，旧属性留作通配兼容。
- ★ **实体模型允许"部件互穿"，烘成方块模型/小雕像时要单独修**（2026-09-10 实测：屠夫雕像"头/手插进身体"）。
  MC 模型里把头压进躯干、手臂嵌进身体是常态（屠夫头嵌进躯干 4~6.5 单位），实体渲染按固定顺序画看不出来，
  但小尺寸方块雕像上非常明显。做法：生成器里按**骨骼名**做局部微调（如 `head dy=+5`、`arm_l dx=+1.2`），
  改的只是石像、不动 Boss 本体；量重叠用 `tools/model3d/overlap_measure.cjs`（带骨骼名，输出重叠量）。
  另外注意：preview geo 的 cube 原点是**绝对坐标**，不要再累加骨骼枢轴（那是 Java 骨内局部的约定）。
- ★ **把实体模型烘成方块模型时，务必给每个 cube 加微量膨胀 (0.02 单位) 并关掉 AO**（2026-09-10 实测："头/手贴图和身体小叠加"）。
  实体模型里相邻部件经常**面贴面**(实测屠夫 31 个 cube 有 51 对面重叠量 = 0.000)：实体按固定顺序绘制所以看不出，
  方块渲染(Embeddium/Sodium)下就是 **z-fighting**。做法：每个 element 的 from 减 0.02 / to 加 0.02
  (1/800 格, 肉眼不可见) → 共面变轻微互穿, 内部面被挡住; 再给模型加 `"ambientocclusion": false`,
  让它按"实体"的平光渲染(也避免互穿处的方块 AO 脏阴影)。检查脚本思路见 `tools/model3d/butcher_diag.cjs`。
- ★★ **Bedrock 骨骼展开：子骨骼要继承"完整矩阵"，不是 `translate(pivot)`**（2026-09-10 实测：石像"错位/穿模"的真因）。
  Bedrock 里 cube 的 origin 是**绝对模型坐标**，骨骼变换只是"绕枢轴旋转" = `translate(piv)·rot·translate(-piv)`；
  子级必须继承这个**完整**矩阵（静止姿势下就等于父矩阵）。只传 `translate(piv)` 那一步的话，
  **每深一层就多吃一次祖先枢轴的平移** → 越深的部件偏得越远（权杖飘到头旁边、披风/手臂错位），包围盒还会被撑大 1.5~3 倍。
- ★ **Bedrock 的 box-UV 矩形不能原样抄进 Java 方块模型**（2026-09-10 实测：石像贴图"进去了但整个错乱"）。
  两边"贴图角 ↔ 立方体角"的对应不同：Java 的顶点 0..3 依次拿 **TL, BL, BR, TR**(见 `FaceInfo` + `BlockFaceUV`),
  Bedrock(GeckoLib `VertexSet.quadX`)的 4 个顶点依次拿 **TL, TR, BR, BL**。对齐后规则极简：
  **4 个侧面发 `[u2,v1,u1,v2]`(uv 都交换), 顶/底面发 `[u1,v2,u2,v1]`(只换 v)** —— Java 允许 u1>u2 的镜像写法。
  详细两张顶点表见 `PROJECT_STATUS.md` 对应条目与 `gen_statue_real.cjs` 的 `orientRect`。
  **方法论**: 跨引擎搬 UV/顶点这类东西, 不要"看着公式一样就照抄", 从字节码里把两边的顶点表挖出来逐面对齐最稳
  (本机就有 `forge-..._mapped_official_1.20.1.jar` 与 GeckoLib jar 可 javap)。
- ★ **方块模型绝不能引用 `textures/entity/**`**（2026-09-10 实测：五座石像全变紫黑，**日志里一句报错都没有**）。
  本整合包装了 **ETF/EMF**（entity_texture_features / entity_model_features）+ ModernFix 动态资源 + Lightspeed，
  它们接管实体贴图 → 方块模型走 `entity/` 路径会拿到 missing sprite 且**不写日志**。
  规矩：方块要用 Boss/生物贴图时，**把图复制一份到 `textures/block/`** 再引用（实体侧继续用 entity 那份）。
  `audit.cjs` 的 **[10]** 专查这个。诊断口诀：白膜正常 + 上色紫黑 = 贴图路径问题；两者都紫黑 = 模型问题。
- **改同名资源后要清两个缓存**：`versions/<实例>/lightspeed-cache` **和** `modernfix`（同名同路径资源改内容
  可能继续读到旧索引）。改文件名是更稳的规避手段。
- PowerShell 小坑：**变量名大小写不敏感**（`$A` 与 `$a` 是同一个变量，会互相覆盖）；
  `[System.IO.File]::ReadAllText("相对路径")` **不认 PowerShell 的 `cd`**（.NET 用进程工作目录）→ 一律用绝对路径。

- ★ **一个 0 字节的 `.animation.json` 能让游戏"静默卡死在启动界面"**（2026-09-11 实测，玩家报"启动卡死"）：
  `assets/ftboss/animations/ion_orb.animation.json` 被某次中断的生成/复制写成了 **0 字节**，而日志里**没有一行 ftboss 报错**，
  真错被 Lightspeed 吞掉了，只剩一行极易淹没的：
  `[Lightspeed-.../ERROR] [ResourceReloadFailureGuard]: Lightspeed isolated resource reload failure in GeckoLibCache$$Lambda`
  `software.bernie.geckolib.GeckoLibException: ftboss:animations/ion_orb.animation.json: Error loading animation file`
  `Caused by: com.google.gson.JsonParseException: java.io.EOFException: End of input at line 1 column 1 path $`
  链子：空文件 → GeckoLib `GeckoLibCache.loadAnimations` 抛异常 → **Lightspeed `ResourceReloadFailureGuard` 把它"隔离"掉**
  （不崩溃、不弹错误屏、不中断启动）→ 资源重载 future 永不完成 → 卡在加载中（`Reloading ResourceManager` 之后
  **永远等不到 `Created: ...-atlas`**，最后是玩家手动退出打出 `Stopping!`）。
  **排查口诀**：遇到"启动卡死/加载不动" ①先 `node tools/model3d/check_assets.cjs <assets/ftboss>`（已内置 0 字节检查）；
  ②日志 grep `ResourceReloadFailureGuard` 与 `EOFException`（这才是被隔离的真错，grep `ftboss` 是搜不到的）。
  ③症状类"卡死"永远先怀疑**资源文件**（编译器管不到），别一头扎进 Java 逻辑。
- 生成器脚本写出的文件**必须复查字节数**：`writeFileSync` 那一轮若被中断/被外部占用，会留下 0 字节残file，
  而后续 `Copy-Item` 会把这个"空"忠实传播到 src 和 jar（本次三处都是 0 字节）。

- ★ **新增 Boss 时必须自己把"全家桶" tick 钩子调全**: `BossDamageGate.tick` / `TrapBreaker.tick` / **`BossRage.tickBoss`**。
  2026-09-11 实测：`DefectCoreEntity` 漏了 `BossRage.tickBoss(this)`（其余 5 个 Boss 都有），
  后果是 `BossRage.tierOf()` 恒为 0 → **"狂暴后进入回想形态"这个已经写好的分支永远走不到**（日志里一句报错都没有，只是永远不发生）。
  排查口诀：**"某机制不生效"先确认它的 tick 入口被调了**，再怀疑逻辑。
- **只让玩家能拆召唤物**（悬浮/传送型实体专用）：`if (!(source.getEntity() instanceof ServerPlayer)) return false;`
  在 `hurt()` 里做来源白名单。原因：这类实体靠 `teleportTo` 移动（本模组的球还会高速撞击），
  很容易瞬间处于方块内 → **窒息/岩浆/摔落**等环境伤害会把它们"白掉"（玩家视角=球莫名其妙少一颗）。
  玩家本体、弓箭、铁魔法都以玩家为 `source.getEntity()`，白名单不会挡住正当输出。
- **`LivingEntity.knockback(strength, x, z)` 的 (x,z) 是"推的方向"**（`Vec3(x,0,z).normalize().scale(strength)`），
  不是"推离的点"。冲撞类技能沿冲撞方向击飞：`target.knockback(1.5, this.dashX, this.dashZ)`。
- **高速冲刺要自己查墙**：`teleportTo` 不看碰撞，直接穿墙。用
  `level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))`
  逐 tick 检查下一格，命中就中止/弹回。

- ★ **动画时长必须与 Java 端常量 1:1 对齐**(1 秒 = 20tick): `DefectCoreEntity` 里 `T_CAST/T_HURT/T_PHASE/T_SUMMON/T_ECHO`
  与 `defect_core_anims.cjs` 的 `animation_length` 是**两份独立的数字**, 改了一边忘了另一边 → 动画被中途切回 idle(看起来"动作一顿")。
  多球种共用"施法"时尤其注意: 玻璃 1.15s / 黑球 1.3s / 其余 1.0s, 所以 `playCastAnim()` 里有 per-type 的时长分支。
- **动画改完要"出图"而不是只看校验**: `validate_anim.cjs` 只查结构(骨骼是否存在/关键帧是否合法), **看不出姿势好不好看**。
  姿势自查流程(2026-09-11 新增):
  `node pose_bake.cjs <geo> <anim.json> <动画名> <秒> <out.geo.json>` → `FT_VIEWS=front,right,back,iso node render_tex.cjs <out.geo.json> <tex> <png> 2 3.2`。
  (`render_tex.cjs` 为此补上了**骨骼 position** 支持, 顺序 = 父级 · T(position) · T(pivot) · R · T(-pivot)。)
  ⚠ `pose_bake.cjs` 的关键帧查表**必须按数值排序**, 不能按字符串匹配 —— 手写键有 `"0.6"`, 自动生成的可能是 `"0.60"`。
- **模型的两套下半身用"同名骨骼"承载**(`leg_l/leg_r`): 腿 / 悬浮反重力鳍 共用同一批骨骼名, 动画就完全通用,
  换造型只需要 `FT_VARIANT=legs|float` 重跑生成器 —— 以后想给 Boss 换轮廓时优先考虑这个套路, 别另起骨骼名。

- ★ **受击动画会"吃掉"施法动画**(2026-09-11 实测"施法看不出动作"的真因): `AnimationState` 的 predicate 里
  只要把 `hurtTime > 0 → 播 HURT` 放在**最前面**, 玩家连续输出时 `hurtTime` 长期 > 0, 施法/召唤/阶段动画几乎每帧被挤掉。
  规矩: **动作优先, 受击让位** —— 施法/阶段/召唤/回想一旦开始就播完(用"动画剩余 tick"兜底), 受击只在没有动作在播时插空。
- **技能生效要卡在动画的"推出/爆发"关键帧上**: `playCastAnim()` 只是播动作, 真正的伤害/法术要**排队延迟**到那一帧
  (`DefectOrbs.releaseDelay(type)` 与 JSON 的关键帧时间一一对应)。立刻结算是"手没抬起来伤害就打完了"的脱节感来源。
- **手写稀疏关键帧 + 自动平滑重采样**, 别手动一帧一帧写: `defect_core_anims.cjs` 里的 `smoothAll()` 会在导出前把每段按
  smoothstep 插成中间帧(一次性 3 帧/段, 循环 2 帧/段) —— GeckoLib 只做线性插值, 关键帧密一点曲线就"缓入缓出"了。
  代价是动画 JSON 会胀 2 倍(71KB → 150KB), 可接受。
- **模型要"做大"时改渲染缩放, 不要改几何坐标**: 方块模型 box-UV 是按整数尺寸算的, 坐标乘 1.2 会产生小数尺寸 → 贴图边缘渗色。
  正解是覆写 GeckoLib 的 `GeoRenderer#scaleModelForRender(widthScale*K, heightScale*K, ...)`(发光层同样生效),
  再把实体碰撞箱(`FTEntities` 的 `register(..., w, h)`)同步放大, 保证视觉与判定一致。

- ★ **"精细"靠转角度和外壳层, 不是堆方块**（2026-09-11 量化桌面 YSM 模型后学到的）:
  专业 Bedrock 模型(`OneDrive\桌面\yes_steve_model\`)的特征 —— `wine_fox`: 188 骨骼 / 412 cube, **75% 的 cube 带 `rotation`**,
  **139 个 cube 用 `inflate`** 做外壳层, 层级深 15; `default`: 65 骨骼 / 172 cube / 84 旋转。
  做法: ①给碎块/护甲/冠片加 `rotation` 做斜角与倒角(方壳四角加 45° 斜轨就是"削角"); ②`inflate` 让一层壳盖在另一层之上;
  ③层级要深(肩→臂→肘环→手→掌背→爪→指节), 动一下带一串。本工程 v8 就是照这三条改的(30→35 骨骼, 79→106 cube, 0→46 旋转 cube)。
- ★ **"流畅"靠密集烘焙关键帧**: YSM 的动画就是**每 1/24 秒一个关键帧**(8350 / 5118 键)。
  本工程的套路: **手写稀疏"姿势"关键帧 + 导出前自动按 fps 重采样**(`defect_core_anims.cjs` 的 `smoothAll`,
  一次性 24fps / 循环 12fps, 值走 smoothstep)。GeckoLib 只做线性插值, 但键足够密就等价于曲线。
  代价: 动画 JSON 会胀到几百 KB(593KB / 9750 键, 与 YSM 同量级) —— 可以接受, 资源重载只解析一次。
  注意 GeckoLib **也支持 `"lerp_mode": "catmullrom"`**(jar 里有 `EasingType$CatmullRomEasing` + `BakedAnimationsAdapter` 会读),
  想省体积时可以"稀疏键 + catmullrom"代替密集烘焙。
- ★ **披风/尾巴这类布料必须分段**: 一整块板只会"拍", 分段(背 3 段 + 侧 2 段)后给每段**相位延迟 + 放大摆幅**才有波浪。
  本工程用 `expandCape()` 自动展开: 手写仍写整块, 循环动画按**环绕相位**重采样(不破坏首尾同值), 一次性动画用**时间延迟**做滞后。
- **参考素材位置**: `C:\Users\YOUR_USERNAME\OneDrive\桌面\yes_steve_model\` —— `.ysm` 是 **YSGP 明文包**(header `YSGP`),
  且**已有解包目录**(`default`/`wine_fox`/`alex`/`steve`/`宇航员酒狐`: `models/main.json` + `animations/*.animation.json` + `textures/`),
  格式与 GeckoLib 完全一致 → 需要高质量参考或直接移植素材时先来这里看。量化工具: `node tools/model3d/ysm_probe.cjs <model> [anim]`。

- ★ **"侧挂装饰片"会被读成第二条手臂**(2026-09-11 用户报"第三只手"): 披风的左右侧片与手臂**同高、同宽、同样垂着**时,
  玩家看到的就是"第三条手臂"。规矩: 装饰件要么收进背后(只留背披风/裙摆), 要么明显做小做薄, 别与肢体在同一个高度带里并列。
- ★ **手掌朝向: Bedrock 正面 = -z**。掌心/发光垫放 **-z**, 掌背护板放 **+z** —— 反了就是"手掌前后反了"(本工程踩过)。
- ★ **贴图要"逐部件绘制", 不要"按材质刷噪点"**(2026-09-11 用户要求): 同一材质的所有 cube 长得一模一样 = 廉价感。
  正解是**几何判定画图形**: 用 (x,y) 与矩形尺寸 `fw/fh` 判断"包边/四角包角/圆外压暗/同心环/高光弧/折痕带/下摆缝线/格栅叶片"。
  本工程把它拆成独立模块 `tools/model3d/defect_core_paint.cjs`(导出 `makePaint({M, cv, nz, getCore})`),
  按球种变色的部分靠 `getCore()` 闭包读当前配色 —— 生成器里用 `patch_paint.cjs` 按标记替换成 `require`(和动画模块同一套路)。
  画图小抄: `onFrame(x,y,fw,fh,t)` 包边 · `inCorner(x,y,fw,fh,len)` 包角 · `ndist(x,y,fw,fh)` 中心距离(做球感/径向渐变)。

- ★ **"侧边悬浮的零件"必须给个连接交代**(2026-09-11 用户报"侧边悬浮模型要不就删掉要不就做好一点"):
  45° 长条护肩、悬空的关节球、腰侧小件这类**独自悬在身侧**的装饰件 = 飘着的碎块。
  正解: 加**插座/支架/能量链接**把它接到主体上(本工程: 躯干侧插座 → 关节球 → 护肩三层 → 肩臂之间一根发光 CRACK 链接)。
  要"悬浮断开"的机械感可以保留, 但**断开处必须有交代**(发光链接/插槽), 否则读起来就是穿模或飘件。
- ★ **左右对称件用镜像助手写, 别手抄两遍**: `cubePair(kind, boneL, boneR, o, s, mat, rot, inflate)`,
  右件坐标 = `[-(x+w), y, z]`, 旋转 = `[rx, -ry, -rz]` —— 手写镜像常见零点几的偏差, 小尺寸模型上肉眼可见。
- ★ **左右对称的动画也只写一边**: `mirrorAnim(anim)`(骨骼名 `_l`↔`_r` 互换, 旋转 y/z 取反, 位移 x 取反)自动生成另一半
  (本工程 turn_r 由 turn_l 镜像而来)。**动作幅度用"后处理放大表"**(`amplifyCasts` 的 CAST_AMP 按骨骼乘系数),
  不要为了"动作大一点"去重写所有关键帧 —— 表里改一个数就能整体调。

- ★ **悬浮/传送型 Boss 不要写步行动画**: 本工程的 Boss 大多是 `setNoGravity` + `teleportTo` 移动(没有寻路、没有 walk 控制器),
  给它们写迈腿动画 = "踩着空气走路"。正解是**飞行姿态 + 推进特效**: 双腿向后收起、身体前倾、脚下持续喷流
  (本工程 `DefectCoreEntity.tickThrusterFx()`: 一双喷口粒子 + 每 10t 一圈 CLOUD 下洗气流)。
- ★ **召唤物配方有固定范式, 新 Boss 照抄**: `data/ftboss/recipes/<item>.json` = `minecraft:crafting_shaped`,
  3×3 图案 `ABC/D#E/FGH`, **八个角放八个不同的高级物品**(按 Boss 主题选: 食物/奶酪/茶/酒/机械动力组件),
  **中心统一 `farmingtales:gold_bucket`(金桶)**。价值别跨档(第 6 个 Boss 用 Create 精密构件/机械臂/烈焰人燃烧室 + 矿石块)。
- **跨 mod 物品 ID 不要凭记忆写**: 从对应 mod 的 jar 里列 `assets/<ns>/models/item/*.json`(每个物品一个模型文件)来核对,
  再用 `audit.cjs` 的 **[5] data 引用检查**兜底(引用到不存在的物品会在这里报出来)。

- ★ **"施法动作"要手臂主导, 别让躯干顶胸口**(2026-09-11 用户报"老是挺着核心"): 放大施法幅度时**只放大手臂/手/指**,
  躯干与头要**反向压住**(本工程 `CAST_AMP` 手臂 ×1.45 / `CAST_DAMP` 躯干 ×0.45)。躯干幅度一大, 视觉焦点就跑到胸口核心上,
  玩家读到的就是"挺着核心往前顶"而不是"在施法"。
- **直线型技能(光束)的通用做法**: ①开始时**锁定方向**(方向定死, 之后不追踪 → 玩家能靠侧移躲); ②每 tick 用**粒子沿射线打点**画线
  (step 1 格 + `END_ROD` 芯 + `ELECTRIC_SPARK` 外层); ③伤害**按脉冲结算**(每 0.5s 一次, 结算前 `invulnerableTime = 0`),
  这样"每秒 N 点"才吃得住原版无敌帧; ④命中判定用"沿射线投影 ∈ [0, 长度] 且垂距 ≤ 半径 + 目标半宽", 比 AABB 精确。
- **长引导技能要抢动画优先级**: 本工程 `beam` 放在 predicate 的**最前面**(仅次于死亡), 且引导期间 `DefectOrbs.tick` 直接 return,
  否则球技能/受击会把 5 秒的引导动画切碎。

- ★★ **绝不能在游戏运行期间覆盖 jar**(2026-09-11 已踩两次, 表现为 `NoClassDefFoundError` 崩溃):
  症状: `java.lang.NoClassDefFoundError: net/farmingtales/ftboss/boss/SugarQueenBoss` +
  `Caused by: ClassNotFoundException`, 堆栈落在 `BossHandler.onServerTick` 这类**运行中才惰性加载**的代码上,
  而 jar 里**明明有这个 class**(CRC 全量校验 251 条目全可读、magic=cafebabe、build 与实例哈希一致)。
  真因: JVM/Forge 把 jar 的 zip 索引缓存住了; 运行中被 `Copy-Item` 覆盖后, 后加载的类读不到 → 报"找不到类"。
  **规矩: 部署前必须先查 `Get-Process java,javaw`; 有进程就停手, 等玩家退出再部署。**
  (Windows 允许覆盖被 Java 打开的文件, 所以不会报"文件占用", 只会安静地崩在几秒之后 —— 更难查。)
  排查口诀: 见到"类找不到但 jar 里有" → **先看时间线**(日志首行时间 vs jar 修改时间), 别去翻代码。

- ★★ **`BUILD SUCCESSFUL` 不等于"jar 里是你刚改的字节码"**（2026-09-15 实测踩到）：
  改了 Java → `gradle build` 成功 → 部署 → 游戏里**行为还是旧的，而且没有任何报错**。
  实测证据：`build/classes/.../BloodGlowLayer.class` 里是 `ldc 15728880`（新），
  **同一时刻的 jar 里**却是 `iload 8`（旧）—— `compileJava` 确实重编了，
  但 `reobfJar`/`jar` 那一步的**增量判定把它当成没变**，沿用了上一次 reobf 的旧 class。
  **最毒的是"静默"**：编译日志干净、五处部署哈希一致、游戏不报错，只有行为不对。
  **规矩**：
  ① 改完 Java 要验收时，**验 jar 里的字节码**（`javap -c -p` 把 class 从 jar 里提出来看），
     或拿一条**新加的字符串/常量**当指纹查一遍 ——
     ⚠ 注意 `static final int` 会被 javac **内联成字面量**，查 `LightTexture` 这种类名是查不到的，
     要查**数值本身**（如 `15728880` / `0x00F000F0`）；
  ② 一旦怀疑陈旧就 `Remove-Item build\reobfJar, build\libs -Recurse -Force` 再 build
     （9 秒，比重查一小时便宜得多）；
  ③ 诱因之一：**两个会话同时在这个工程上 build** 会互相踩
     （`.gradle` 的 executionHistory / buildOutputCleanup 锁是共享的）——
     并发构建之后尤其要按 ① 验一遍。

- ★★ **`ExplosionInteraction.NONE` 只挡"方块", 实体伤害照算**（2026-09-14 v106 用户报"爵士的爆炸会炸毁凋落物"）：
  `ItemEntity.hurt()` 是存在的 → 地上没捡的战利品被爆炸扫过**直接消失**。我们一堆 Boss 的爆炸
  （烂苹果堆自爆 / 酒桶炮 / 故障机器人超载 / 肥料王…）常年在吃掉落物。
  正解：监听 `ExplosionEvent.Detonate`，把 `ItemEntity` 从 `getAffectedEntities()` 里摘掉（伤害结算**之前**），
  见 `boss/ai/SafeExplosion`。范围要收窄（只在本模组实体是爆炸来源时动手），别顺手改掉原版 TNT/苦力怕的规则。
- ★★ **往实体身上挂"扫注册表收来的模组效果"必须"先摘旧实例再加 + try/catch 拉黑"**（2026-09-14 v106 紫菜崩服）：
  减益池是 `ForgeRegistries.MOB_EFFECTS` 全扫来的（实测 62 种），里面只要有一个效果的
  `addAttributeModifiers` 用**固定 UUID**、又不处理"修饰符已经挂上"，第二次施加就抛
  `IllegalArgumentException: Modifier is already applied on this attribute!` —— **服务端 tick 直接崩**。
  范式（见 `EasterBossEntity.safeAddDebuff`）：① `if (target.hasEffect(eff)) target.removeEffect(eff);`
  把旧实例连同属性修饰符摘干净；② `addEffect` 包 `try/catch(Throwable)`，出事就记一条**带注册 id** 的日志
  并把该效果**拉黑**。最坏只少一种效果，不会崩。
- ★ **"某技能/机制一直没触发"先查"入口和顺序"，别先怀疑逻辑**（2026-09-14 v106 用户报"雪黎的禁疗未生效"）：
  `xueliNext` 从 **0** 起轮，而庄园里的雪黎两三个技能就被打死 → 排在后面的"禁疗飞刀"**一次都没轮到**
  （日志里汤锅/擀面杖/平底锅都有、飞刀一行都没有）。同理"机制不生效"要顺次确认
  ① tick 入口被调了吗 ② 事件监听真的注册了吗（本项目 `@Mod.EventBusSubscriber` 自动注册 与
  `MinecraftForge.EVENT_BUS.register(...)` 显式注册**混用**，排查时分不清；统一用显式注册，
  并在关键分支补一条"生效了"的日志，下次能直接从日志分辨"没拦住"还是"根本没触发"）。
- **`pwsh` 不一定在 PATH**：本项目宿主里只有 Windows PowerShell 5.1，脚本要用
  `powershell -NoProfile -ExecutionPolicy Bypass -File ...`（默认执行策略禁止跑 .ps1）。
  另外 PS 5.1 在 `$ErrorActionPreference='Stop'` 下会把**原生命令写到 stderr 的告警**（gradle 的
  `[removal]` 警告就是）当成终止性错误 `NativeCommandError` 打断脚本 —— 调用外部命令的那一步要临时把
  ErrorActionPreference 调回 `Continue` 并只看 `BUILD SUCCESSFUL` 字样（`tools/deploy_jar.ps1` 已这么改）。

- ★ **导入用户提供的 Blockbench 物品模型**（2026-09-14 v108 导入"酒桶弩"时定的流程，照着做就不会出紫黑/错位）：
  ① 先看文件顶层键判断格式 —— **`elements` + `display` + `texture_size` = Java Block/Item 模型**
  （直接放进 `models/item/`，**UV 不用换算**：Java 物品模型的 UV 本来就是 0..16 空间）；
  **`minecraft:geometry` + `bones` = Bedrock 实体 geo**（那才需要 box-UV→逐面 UV 那一套换算，见下面石像条目）。
  ② 贴图放 `textures/item/<名字>.png`，尺寸要和模型里的 `texture_size` 一致（不一致不会错位，但会掉画质/糊）。
  ③ 模型 JSON 里把 `textures` 的引用改成 **`ftboss:item/<名字>`**（`"0"` 和 `particle` 都要）。
  不写命名空间会被解析成 `minecraft:barrel_crossbow` → 贴图变紫黑；`groups`/`gui_light`/`format_version`
  这些 Blockbench 字段留着无害（`candy_cane.json` 就是带 `groups` 的）。
  ④ 注册物品（`FTBossItems`）+ 进创造页（`FTCreativeTabs`）+ 两条语言（`zh_cn`/`en_us`）。
  ⑤ **要给 GeckoLib 实体"拿在手里"时，原版 `ItemInHandLayer` 不管用**（GeckoLib 的模型不实现 `ArmedModel`，
  加了也不显示）→ 必须用 `software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer`
  （现成范例：`CompostRenderer` 的土豆加农炮、`WineBarrelGeoRenderer` 的酒桶弩），
  并在 `addRenderLayer` 里按**骨骼名**筛 `getMainHandItem()`。
  ⑥ 挂点不好判断时，把 **骨骼名 / 偏移 / 缩放 / 朝向** 提成 `public static final` 常量写在渲染器顶部
  （单位写清楚：16 = 1 格），这样用户看一眼游戏就能点名改哪个数。
  ⑦ **模型导入/校验已脚本化**（2026-09-15 导入 3D「大肉腿」时补）：
  `node tools/model3d/import_item_model.cjs "<Blockbench导出的.json>" <名字> [--png <贴图.png>]`
  自动完成③：删掉 Java 不认识的 Bedrock 专用键（`format_version`/`credit`/`texture_size`/`display.on_shelf`
  —— 按①留着确实无害，删掉更干净、日志不再刷 unknown key）、把 `textures` 全部命名空间化成
  `ftboss:item/<名字>`（面里的 `#键名` 引用不动）、把旧模型备份到 `ref/<名字>_prev.json.bak`、
  打印包围盒/UV/display 自检。**`parent` 默认不加**（Blockbench 的 Java 物品模型自带 display，不需要父模型；
  要挂就显式 `--parent <id>`）。
  ★★ **千万别给带 `elements` 的 Java 物品模型挂 `minecraft:item/handheld` / `item/generated`**：
  那条父链的根是 `builtin/generated`，MC 会走"读 `#layer0` 生成平面物品"那条烘焙路径；
  Blockbench 导出的模型贴图键通常是 `"0"`/`"2"` 这种、**没有 `layer0`**，于是**整个模型烘不出任何面** ——
  游戏里表现为**物品完全看不见（第一/第三人称、物品栏全空），而日志零报错**，极难查
  （2026-09-15「大肉腿」踩过一次；隔壁会话那两份 `parent: item/handheld` 的 3D 备份最后也是因此被弃用改回平面的）。
  对照：工程里 `black_cleaver`/`shadow_sword`/`puffer_bat`/`wenxuanlan_flywheel` 也挂了 `item/generated|handheld`，
  ~~但它们**同时定义了 `layer0`**，所以照样出模型 —— 说明致命点就是"挂了生成式父链却没有 `layer0`"。~~
  ★★ **上面这句是错的，2026-09-15 实测推翻（用户报"几乎全部武器贴图被展开"）**：
  **有 `layer0` 并不会救回来** —— 只要挂了生成式父链，MC 就走 `builtin/generated` 那条路，
  把 `layer0` 当**一张平面图标**烘出来，模型自带的 `elements` 根本不参与。
  症状因此**不是"看不见"而是"看得见但贴图是摊开的"**：`gen_weapons3d.cjs` 出的是 **256² 逐面 UV 图集**
  （各部件在画布上分开摆），当平面图标画出来就是"零件散开/被展开"。
  用户给的判定证据：**同一个生成器、同一张图集、只是没挂父链的 `cheese_wheel_mace` 是正常的**；
  `shadow_sword` 正常是因为它是 GeoItem（GeckoLib 自己画，绕开了原版物品模型那条路）。
  **正确写法**：带 `elements` 的物品模型**不挂任何生成式父链**，并且把父链原本提供的 display 槽
  **显式补进模型**（`ground` / `head` / `fixed` + `gui_light: "front"`；`gui` 在原版里本来就没有条目 = 单位矩阵）。
  数值别背，去本机 `client-extra.jar` 的 `assets/minecraft/models/item/generated.json` 读。
  2026-09-15 已用 `tools/model3d/fix_handheld_parent.cjs` 把 10 个模型（apple_stem / black_cleaver /
  blooming_thorns / butcher_cleaver / dog_beating_stick / puffer_bat / sugar_scepter / tea_leaf_blade /
  wenxuanlan_flywheel / wine_bottle_mace）改成"无父链 + 7 槽"，原件备份在 `_backup/handheld_parent_fix_20260915/`。
  **以后 `gen_weapons3d.cjs` 产出的模型必须走这条写法**（生成器里就别写 parent）。

- ★★★ **GeckoLib 物品（BEWLR）必须给物品模型写 `"parent": "builtin/entity"`，否则渲染器永远不会被调用**
  （2026-09-16 定位，黑切"贴图错乱 + 无法展开"的真凶）。
  证据链：Forge 改过的 `ItemRenderer.render` 里，**先**调 `BakedModel.isCustomRenderer()`，
  为真才跳到 `IClientItemExtensions.getCustomRenderer()` 并 `renderByItem(...)`；
  而 `isCustomRenderer()` 只有在物品模型声明 `builtin/entity` 时才为真。
  所以**只实现 `IClientItemExtensions` + `initializeClient` 是不够的** ——
  运行时日志表现为 `initializeClient` 被调用（调用链①），但 `getCustomRenderer()` **从来没被调用**（②不打），
  于是 vanilla 老老实实去画那个平面模型 → 玩家看到"贴图是图标摊开的、而且任何渲染层/展开都不动"，**且零报错**。
  **正确写法（照抄原版 `assets/minecraft/models/item/shield.json`）**：
  ```json
  { "parent": "builtin/entity", "gui_light": "front", "textures": { "particle": "..." }, "display": { ... } }
  ```
  （`builtin/entity` 会忽略 `elements`，几何交给 BEWLR 画。）
  工程里走 BEWLR 的有两件：`black_cleaver`（2026-09-16 已修）与 **`shadow_sword`（同样缺这一行，
  所以它一直在画平面图标 —— 只是它的图标看着像把剑，所以没人发现）**。
  能正常显示的 3D 物品模型长这样：`barrel_crossbow`/`candy_cane`/`blooming_thorns` = **无 parent**，
  或者 `statue_*` = `parent: minecraft:block/block`。
  **用户只发 json 没发 png = 贴图沿用现有的，别乱覆盖。**
  用户发 `.bbmodel`（Blockbench 工程）时：它是 `meta.model_format = "java_block"`、`resolution` 64x64、
  贴图**内嵌**（base64）—— 可以拿内嵌贴图直接和工程里的 png 比 sha256（2026-09-15 比过，完全一致），
  `ambientocclusion` / `front_gui_light` 就是该写进模型的 `ambientocclusion` / `gui_light: "front"`。
  ★ **UV 左右方向（2026-09-15 查证后定）**：Java 每面 `uv:[u1,v1,u2,v2]` 里的 `[u1,v1]` = **"从外面看该面时的左上角"**；
  面内 u 轴方向是 north→**−x** / south→+x / east→−z / west→+z（v 一律贴图上方→面的上方）。
  于是对 north 面，**画区左边缘(u1) 落在模型 x 大的那一侧**；`south` 必须写成 `[u2,v1,u1,v2]`（镜像），
  否则同一个方块正反两面会左右错乱。做刀/剑这种单刃造型时，**"亮刃口画在画区哪半边"和"几何斜切切哪一侧"
  必须落在同一物理侧**（2026-09-15 暗影刀踩过）。
  ★ **贴图画布必须钳位**：自己写 `mix()`/`put()` 时，插值系数要 clamp 到 t∈[0,1]、写入通道要 clamp 到 0..255。
  画区宽度常是小数（如 7.7px），循环里 `x/(w-1)` 会算出 >1 的比例 → 外插把通道顶到 >255 →
  写进 `Uint8Array` 会**回绕**成小值，表现为贴图上凭空出现金色/杂色点（v3 真踩到：尖端一条金线，
  `node tools/model3d/check_uv_coverage.cjs` 之外可以再扫一遍"暖色像素数应为 0"）。
  ★ `preview_spin.cjs` 是**每面平均色**渲染：只能看轮廓/比例/大色块，**看不出贴图细节、半透明、UV 左右方向**。
  要判断这些必须看放大后的贴图本身（带棋盘格的那种放大图）或者直接进游戏。
  ★★ **宽面 UV 必须正反互为镜像**（2026-09-15 暗影刀 v9 踩到）：把"整件侧视图"按模型坐标对齐来画时，
  很容易顺手给 `north` 和 `south` 同一个矩形 —— 但 MC 里 **north 的面内 u 轴指向 −x（u1 对应 x 大侧）、
  south 指向 +x（u1 对应 x 小侧）**，同一个矩形给两面 = 一个正一个反。正确写法（画区 u 随模型 x 增大时）：
  `north: [u(x1)+m, v(y1)-m, u(x0)-m, v(y0)+m]`、`south: [u(x0)-m, v(y1)-m, u(x1)+m, v(y0)+m]`（m = 外扩量）；
  自检：每个部件都应有 `north.u1 > south.u1 && north.u2 < south.u2`。
  ★ **相邻方块互相重叠 0.05，不要严丝合缝**：两块贴在一起的方块共面会 z-fighting 闪烁
  （YSM 专业模型大量用 `inflate` 也是这个目的）。Java 元素本身也支持 `"inflate": <float>`，做"叠一层细节"时可用。
  ★ **参考专业模型**：`C:\Users\YOUR_USERNAME\OneDrive\桌面\yes_steve_model`（YSM）里 `default/` `alex/` `steve/` `wine_fox/` `custom/*`
  有**明文 Bedrock geo + png**（`.ysm` 是加密包，别硬啃）；量化工具是 `tools/_archive/model3d/ysm_probe.cjs`
  （统计贴图尺寸/骨骼数/cube 密度/rotation/inflate/逐面 UV/层级深度，也可统计动画关键帧密度）。
  2026-09-15 实测口径：`alex/main.json` 64×64 贴图 / 61 骨骼 / 47 cube / **逐面 UV 100%**；
  `default_controllers/main.json` 256×256 / 133 骨骼 / **241 cube** / 62% 带 rotation。
  可借鉴的三条：**逐面 UV**、**用很多小 cube + 旋转堆细节**、**贴图 256×256 级别**。
  ★★ **逐面 UV 的前提是"每个面都有自己比例正确的矩形"**：别给一批部件共用一条侧面条 —— 比例一差就拉伸
  （2026-09-15 暗影刀 v9 真踩：`pommel` 侧面比例 0.76 取了 0.20 的条，纵向拉长 3~4 倍；宝石侧面甚至取到了柄的缠绳纹）。
  正确做法：按 `(厚度 × 高度)` 给**每个部件**自动装箱分配矩形，并加一条"面比例 == 块比例"的自检。
  ★ **贴图观感的两条专业习惯**（参考 YSM `宇航员酒狐/textures/skin.png`）：① **暗部不要写纯黑**，往蓝紫偏一点
  （hue shift），高光往光源色偏；② **面板之间用 1px 深缝切开、缝上缘再压 1px 亮线** —— 比一整块平涂立体得多；
  整体是"低饱和主体 + 极少量高饱和点缀"。
  ★ **物品模型贴图的公共库**：`tools/model3d/lib_itemtex.cjs`（2026-09-15 从暗影刀 v3~v11 合并）
  = `makeCanvas`（put/mix/rect，通道与插值系数全 clamp）+ `makePacker`（侧/盖面按自身比例自动装箱）
  + `outline`（1px 外描边）+ `checks`（四项自检：暖色杂点/正反镜像/浮点噪声/面比例）。
  做新物品模型直接 require 它，别每次重写画布与自检；范例见 `tools/model3d/regen_shadow_sword.cjs`（v11）。
  · 圆柱类部件用**四段色阶**（高光→亮→中→暗→反光边）比两段平涂立体；
  · 想让部件"浮"出来可用 Java 元素自带的 **`inflate`**（YSM 用了 13 处），不必手算厚度差；
  · 非轴向造型用 **±22.5°/±45° 旋转块**（YSM 62% 的 cube 带 rotation；本工程 `candy_cane` 13/13、
    `blooming_thorns` 39/39 也都是这么做的）。注意旋转块的四角会外凸，做"斜切/尖"不如阶梯块干净，
    它适合做**杆/绳/钩**这类本身就是斜的构件。
  ★★ **两边的调研结论都整理在 `docs/MODEL_REFERENCE_NOTES.md`**（YSM 九个模型的量化表、灾变贴图口径、
  已借用到暗影刀上的清单）—— 要参考"专业模型怎么做的"先看那份，别重新拆包。
  其中三条最常用：**细分靠多而小**（YSM 一支箭 27 cube / 25 带旋转）、**旋转块做斜构件**（旗舰 75% 带旋转）、
  **`inflate` 做分层**（旗舰 139 处）。
  ★ **发光层**：灾变给每把武器配 `_layer` 发光贴图；本工程用 OptiFine/Iris 的 **`_e` 约定**
  （`textures/item/<名字>_e.png`，只保留要高亮的像素，其余透明）—— 装支持 emissive 的 shaderpack 才看得见，
  没装也无害。生成方式见 `regen_shadow_sword.cjs`（按"紫罗兰高亮"筛选像素）。
  ★★ **批量做武器用 `tools/model3d/gen_weapons3d.cjs`**：每个武器只写"部件表 + 配色表"（`SPECS`），
  其余（逐面 UV、侧盖面按比例装箱、重叠防闪烁、四段色阶、细噪点、1px 描边、发光层、四项自检）都由
  `lib_itemtex.cjs` + 角色画法 `PAINT` 负责；加新武器就是往 `SPECS` 里加一条。
  三个本轮踩到的坑：
  ① **装箱尺寸要乘 k**：侧/盖面矩形的尺寸是 UV 单位，而部件尺寸是模型单位，忘了乘 k 会让大部件（比如 7.2 格的河豚球）
     溢出 0..16（`lint` 会报"uv 超出 0..16"）；
  ② **"暖色像素自检"只当参考**：木质/铜钉/河豚这类武器本来就暖色，真正的溢出特征是**饱和金色**
     （蓝通道回绕成很小值，如 `[222,192,31]`）→ 用 `checks.goldArtifacts()` 判错，`checks.warmPixels()` 只报信息；
  ③ **资源文件可能带只读属性**（老 `gen_weapons.cjs` 留下的），`writeFileSync`/`copyFileSync` 会 EPERM/EACCES
     → 一律用库里的 `writeSafe()` / `safeCopy()`（会先清只读）。
  ★ **参考"灾变"(L_Enders Cataclysm)贴图**：它的实体模型是**编译进 class 的** —— jar 里 0 个 `models/entity/*.json`、
  0 个 geo，185 个渲染器 class，别在那儿找模型；能看的是贴图 `assets/cataclysm/textures/entity/**`
  （256 张，旗舰 `netherite_monstrosity.png` 是 **512×512**、其余 256×256，分辨率按模型在屏上的大小定档）。
  可借鉴四条：① 大面积**撒细噪点**防死平；② **高饱和"热芯"发光裂纹**（亮芯 + 暗晕）；③ **每块面板 1px 描边**；
  ④ **沿板边排铆钉**。
  ★ 读别人的 mod jar 用 .NET 而不是 tar：`[System.IO.Compression.ZipFile]::OpenRead($jar)` 枚举条目
  （`Length` 排序）+ `[System.IO.Compression.ZipFileExtensions]::ExtractToFile($e,$out,$true)` 解单个文件 ——
  `tar -tvf` 输出的列宽对齐会变，用正则抠大小容易抠错列（2026-09-15 就是抠错成日期才换的 .NET）。
  校验两条：`node tools/model3d/lint_java_model.cjs <模型...>`（顶层键、display 槽位、元素旋转角只能
  ±45/±22.5、UV 必须 0..16、每面贴图引用要能解析 —— 全按 Java 版硬规则，
  Blockbench 的 `on_shelf` 这类 Bedrock 槽位会被点名）+ `node tools/model3d/check_uv_coverage.cjs
  <模型> <贴图>`（**判断"这张贴图到底配不配这个模型"**：采样每个面的 UV 区域，落在一半以上透明像素的
  面会列出来 —— 拿平面图标贴图配 3D 展开模型就是这种症状；展开图通常是 40% 左右不透明、1~2KB）。
- **PS 5.1 的 `ConvertTo-Json` 默认 `-Depth 2` 会把模型 `elements` 截断**：要动 Blockbench 模型 JSON 时，
  优先用**字符串替换**（`[System.IO.File]::ReadAllText/WriteAllText` + `String.Replace`）只改 `textures` 那几行，
  整份重新序列化很容易毁掉模型。

- ★ **用户要"全 Boss 数值/机制文档"时的流水线（2026-09-14 建）**：
  ★★ **2026-09-20 用户指示：「模组部分，还有 word 文档只在我要你写的时候才写」** ——
  也就是说这条流水线**只在用户明确要求生成/更新 Word 时才跑**：
  平时改代码**不要**自动执行 `make_boss_doc.cjs`，**不要**自动往桌面 / `OneDrive\桌面` 复制 .docx，
  也不要在收尾汇报里反复提议"要不要顺便更新 Word"（用户要的时候会说）。
  仓库内的 markdown（`docs/all_boss_reference`、`docs/CHANGELOG.md`）仍随改动更新 ——
  它们是 Word 的**源**，保持最新才能在用户开口时一次生成对。
  源 = `docs/boss_reference/*.md`（**按文件名排序拼成文档**，所以用 `00_ / 10_ / 20_ …` 前缀控顺序），
  生成器 = `tools/docgen/make_boss_doc.cjs`，命令：
★ 写入规则（用户 2026-09-14 定）：**只写"当前生效的数值/机制"，不要写历史修正**。
  即：不要出现"v118 改成 12（原 6×4/3=8）""原来 4.5，现在 8.0"这类对照 ——
  数值一律以**代码里当前的值**为准（改完代码重跑 check_numbers.cjs，它会把不一致点出来）。
  版本沿革只记在 docs/CHANGELOG.md，那份才是变更历史。
  `node tools/docgen/make_boss_doc.cjs "C:\Users\YOUR_USERNAME\OneDrive\桌面\FT-Boss-数值与机制速查_v108.docx"`。
  ① .docx 就是个 zip，本机**没有 pandoc / python-docx 且不能联网装依赖**，所以 OOXML 与 ZIP 都是手写的
  （`make_boss_doc.cjs` 里自带 store 方式的 ZIP 打包器 —— **别用 PowerShell 的
  `ZipFile::CreateFromDirectory`，.NET Framework 写出来的条目名是反斜杠，Word 打不开**）。
  ② 那个脚本只吃一个**很小的 markdown 子集**：`#/##/###` 标题、`- ` 列表（只一层）、
  `| a | b |` 表格（表头 + `|---|` 分隔行）、普通段落、行内 `` `代码` `` 与 `**粗体**`；
  代码块 / HTML / 嵌套列表 / 单元格内换行都会原样变成丑段落。
  ③ 内容可以并行抓：给每个子代理划一批 Boss + 明确输出文件路径 + **上面那个格式清单** +
  "数字必须能从代码追溯、查不到写 ?、不许猜"，一次 5 个子代理跑完 8 位主 Boss + 隐藏 Boss 十人 + 全局规则。
  ④ 生成后自检：`ZipFile::OpenRead` 能列出 6 个部件 + `XmlDocument.LoadXml(word/document.xml)` 不报错。
  ⑤ 数值会随版本变 —— **用户要文档时**再重跑一次生成器（见上面的 2026-09-20 指示：平时不要自动生成），
  并在文档副标题里写清构建戳。

## 特效链路：世界贴花必须自己发包（2026-09-16 定，`ftboss` 的第一个网络通道）
- ★★ **教训**：用户报「暗影剑的传送法阵未生效」——
  **不是参数错、不是逻辑没接线，是根本没实现**。`textures/fx/shadow_sigil.png`（256×256）
  与 `textures/fx/shadow_pillar.png`（32×128）从 2026-09-15 就由
  `tools/model3d/gen_sigil_fx.cjs` 生成好了，但**全工程没有任何 Java 引用它**
  （`docs/model-assets.md` 里那句"无 Java 引用，保留待确认"就是这个缺口，当时没人跟）。
  ⇒ 排查"某特效没生效"的**第一步永远是先证明它存在**：
  `grep -r "shadow_sigil\|shadow_pillar" src/main/java` → 零命中就是"没做"，别再调参数。
- ★ **贴图在 jar 里 ≠ 游戏里会出现**。资源只在**被某个 Java 路径引用**时才进渲染。
  "模型/贴图工具的产物清单"和"实际生效清单"是两件事，交接时别混。
- ★ **粒子与音效服务端会自动同步（`ServerLevel#sendParticles` / `playSound`），
  "世界贴花"绝对不会** —— 魔法阵/冲积环/刻痕这类铺在地面上的图，
  既不是实体也不是粒子（粒子是点精灵，画不出阵列贴图），**必须自己发 S2C 包**。
  本工程 2026-09-16 才有第一个通道，就为此事：
  - `skill/FTNetwork.java` —— `SimpleChannel`，通道名 `ftboss:main`，协议版本 `"1"`（改包结构必须一起递增）
  - `skill/ShadowSigilPacket.java` —— 只有 3 个 double + 1 个 boolean（最小可验证）
  - `client/render/ShadowSigilVisual.java` —— 客户端活动表 + `ClientTickEvent` 推进寿命 +
    `RenderLevelStageEvent.Stage.AFTER_PARTICLES` 出画（**别在渲染回调里推进进度**，
    否则速度随帧率变，同 `CleaverBloodVisualState` 的规矩）
  - 服务端入口：`ShadowSwordItem.teleportStrike` 里 `FTNetwork.sendSigilNear(...)`
- ★ **写这条链路时踩掉的两个 1.20.1 API 坑（都已用编译报错钉死，别再试）**：
  1. `PoseStack` **没有** `rotate(float,float,float,float)`，**也没有** `rotate(Quaternionf)` ——
     只有 `translate(double,double,double)` / `mulPose(Quaternionf)` / `pushPose()` / `popPose()` / `last()`。
     正确写法是工程里已有的那套：`pose.mulPose(Axis.YP.rotationDegrees(deg))`
     （见 `BarrelCannonballRenderer` / `FlywheelRenderer` / `WineBarrelGeoRenderer`）。
  2. `NetworkRegistry` 的方法名是 **`acceptMissingOr(String)`**，
     **不是**高版本才改名的 `acceptVanillaOrMissing`。
  3. ★★ `PacketDistributor.TargetPoint` 的**第 1 个参数是 `excluded`**（要排除的玩家），
     不是"发起者" —— 原版 `PlayerList.broadcast` 字节码第一条就是
     `if (player == excluded) continue;`。我第一次写成 `new TargetPoint(source, ...)`，
     结果**把施法者本人排除掉了**：单人/局域网里视野内只有他一个 → 一个包都发不出去，
     症状是"服务端日志正常、客户端毫无反应"，很容易误判成渲染问题。**要发给本人就传 `null`**。
  4. `PacketDistributor.NEAR` 的第 4 个参数是**半径的平方**（字段名就叫 `r2`；已用 `javap -c`
     反汇编原版 `PlayerList.broadcast` 核对字节码：算的是 `dx*dx+dy*dy+dz*dz` 再与该参数比较）。
- 渲染类型仍用 `RenderType.eyes(tex)`（血怒发光层同款）：ADDITIVE_TRANSPARENCY 下
  **只有亮的会发光、暗的几乎看不见**，正好适合"别做成发亮的饼"的阵列贴图；
  顶点 lightmap 必须显式给 `LightTexture.FULL_BRIGHT`，否则进洞法阵跟着变暗。
  **几何面片用 −90°（不是 +90°）放平**：绕 X 轴 +90° 会把贴图的 v 正方向翻到世界 −Z，
  圆环看不出来但四个尖角/8 个符文会南北镜像。
- 验收入口：`/ftshadow sigil | flat | here <x y z> | strike`（调试指令，需 OP）；
  客户端日志 `-Dftboss.debugSigil=true` 打「登记 / 过期移除」。

## 禁止/注意
- 不修改 ftecon 价格 mod 的任何逻辑（仅共享打包/部署习惯）。
- 不在未经用户确认时改动石像交易语义、女王掉落、图腾二阶段设定。
