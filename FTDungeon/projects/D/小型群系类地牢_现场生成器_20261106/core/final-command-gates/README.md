# 最终安装工件的命令门禁与四朝向保存重启

此目录为独立执行者 core-final-command-gates-not-runtime-maker 的普通 Forge 实测。唯一候选 JAR SHA256 为 bab12080dae0e8fb2290f73b6988f40af2094d932a3303cdabc12aaac250263f；运行 VERSION 未改。没有构建或修改 runtime，也没有在 integration 原世界启动 JVM。所有执行世界均为本目录下的停服副本，最多一个本执行者的 server。

输入：只读复制 integration/v6-final 的最终 snapshot、四朝向停服安装世界（worldSeed=267611119455246637，anchor=1000,200,1000，ordinal=0..3）、Forge 库和所需脚本。input-lock.json 记录原路径、SHA256 与副本初始 SHA256。references/ 保留原夹具；运行脚本只修改本目录的导入、输入/输出路径和验证断言。结束时对所有原输入逐文件核哈希；旧 final-runtime-faults 的 1115 文件封存亦逐项复核。

## 夹具修正及过程记录

faults.Server 的导入会把 v4-unified 放到模块搜索路径前端，导致普通 import readback 实际误载 FTD4 读回。此处通过 spec_from_file_location 显式装载本目录复制的 v6 FTD5 readback.py；installed.context 同样明确装载本目录副本。FTD5 reader 使用 integration 原安装脚本引用的锁定 NBT reader。初次夹具错误发生在 baseline 读回且尚未启动 JVM；原始错误 runner log 和遗留副本保留，不计作运行时缺陷或 PASS。

首轮运行期间收到增加具体拒绝原因与 context 核验的要求，故保留 initial-before-refined-assertions/ 首轮记录，并在首轮全部 JVM 退出后串行补跑 receipt 与四朝向，最终结果只使用新增断言的最后一轮。无重叠启动。

## 实测边界

- pause：在复制世界的真实 journal.nbt.pending 路径建立目录，引发 FileChannel.open 的实际 IOException。实际 build/pause/status/list 同批控制台命令触发已安装 UnifiedEngine.pause；验证 DISABLED、active=false/tickets=0、后续 build 拒绝、延时不自启动、正式 journal 哈希不变。停服后 127239 源格与 24 个原生 BE 读回，原状态差异集合与基线相同，BE/逸出流体火/围护壳无差异。
- receipt：六项分别为 missing receipt、错误命令 hash、错误 worldIdentity binding、错误 challenge、missing gate、未知顶层字段。每项明确断言其专属拒绝原因，特别 missing gate 必须到达 Every mandatory gate must be independently decided；每次仍 idle/tickets=0 且非 COMMITTED。始终保留 NEGATIVE_RECEIPT_TEST_NEVER_APPROVAL blocker 和空 gates/reports，未伪造合法 PASS 证据。
- 四朝向：仅在 installed/q0..q3 副本的同一个 canonical 新路径做 pre-readback → 独立 JVM启动 → status/context/evidence/save-all flush → stop → post-readback。要求加载 pending、active=false/tickets=0；实际 context JAR/instance/新 canonical worldIdentity 正确，且区别于 integration 原路径 identity。pre/post 原始状态差异集合相同，原生 BE、逸出流体/火、围护壳均无异常。保存 FTD5 中的真实 request 再核 anchor=1000,200,1000、原 seed、ordinal 0..3、challenge 与 context 一致。

result.json 与 ready.json 只在所有新增断言及输入哈希检查通过后产生。PID、parentPID、cwd、完整 Java 启动命令、exitCode 与日志 SHA256 随结果封存。停止后才封存，没有复用旧 core 规划自测作为世界通过证据。

实际合法 COMMITTED 未执行，生产晋升=false，掉电未覆盖。普通 JVM 保存重启不能表述为掉电保证；所有 negative receipt 均为明确无效测试。

封存复核发现 integration 所有者在复制后更新了 pause_io.py 与 restart_saved.py；本执行者没有修改原件。本次使用的 references/ 原文 SHA256 与初次锁定一致，实际执行本目录修正副本。result.json 记录原始复制版本及当前所有者版本的两个哈希。其余原世界、最终 snapshot、运行依赖及旧1115证据均无变化；没有声称活动夹具始终未变。
