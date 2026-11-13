# Curios 跨维度生命周期验收与恢复（2026-11-14）

当前仍为 NOT-INSTALLABLE，未部署。Stage28 候选 JAR 未改动，两个正式实例保持只读；同一写入者继续开发，武器与 Session 暂停。

## 本段接受范围

V12 新建原生隔离世界通过 998 项检查：一次真实 Boss 正伤害后，玩家携带有效沉默记录切换至下界；记录原到期时间保持不变，在下界按服务端主世界时钟自然到期；原物品数量及组件保持正确。客户端实际观察到下界中的隐藏与恢复状态，返回主世界后再稳定观察 40 个自然 tick。记录到两次真实维度切换事件。沉默开始于 tick129，原 Until229，去程 tick149，恢复同步及返回快照 tick231。

此前四类背包回退与十八项归属控制直接引用，没有重跑。本段没有调用私有玩法方法、构造假玩家或事件、手动 tick、修改时钟。自然传送门碰撞、完整交互界面、认证多人和完整 Boss 验收不在本段接受范围。

## 新 JVM 重登需要的真实操作

V7/V8 失败证据保留：公开世界打开流程停在 `BackupConfirmScreen`，标题为 `Worlds using Experimental Settings are not supported`，尚未进入集成服务端或玩家登录。当前 computer-use 技能要求 `node_repl`，本会话没有该入口，因此没有使用替代自动化、猜测输入、私有回调或世界标志修改。

目前没有遗留的等待窗口。下一次需先准备允许真实交互的重登观察器，在新建的 V6 已接受存档副本中启动验收，然后由用户实际点击 **Create Backup and Load**。点击后还需等待原生登录、物品恢复与客户端同步观察完成，才可接受该项。旧 V8 是有界诊断，不能直接当作无人值守恢复指令；不得使用失败世界或正式实例世界。逐项状态见 `github-sync/checkpoint-curios-lifecycle-matrix-20261114.json`。

## 本地证据恢复

保留增量包 `work/session-boss/archives/boss-core-stage28-curio-dimension-increment-v1-20261114.zip`，SHA256 为 `465ab4fc033d72734b22a0c9a20b158268caf5ae7b5ce4fb6f0d147d9177b1d0`。它依赖基础包 `boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip`，SHA256 为 `aca9e96def7887e0cf52637152df8922174326d5970376918d42b08bee864929`。

本增量选择的 1,945 个文件路径均已完整校验路径、大小和载荷哈希，新增 49 个唯一载荷。它不是独立 SDK 或运行环境安装包。运行 `python -B work/session-boss/tools/restore_boss_zicai_curio_archive_v2.py <增量包> --base <基础包>` 可重新校验；添加 `--output <全新空目录>` 可重建所选路径。未进行整包物理恢复，所有原件保留。压缩包、JAR 和世界均尚无独立远端恢复证明。

## 源码与重复缓存

从 `https://github.com/qyby123456/ft.git` 的 `codex/neoforge-source-20261113` 分支恢复 `neoforge/ftboss`。发布后应独立拉取并核对每个文件的路径、大小、SHA256、提交及祖先关系，同时保留原仓库 README 和历史导入清单的原始绑定。

`github-sync/checkpoint-curios-dimension-cache-cleanup-plan-20261114.json` 只列出两份本段新增的重复测试源码缓存；之前缺失的 1,916 个路径由前两份清理计划合并确认。V3 清理工具支持累积计划，保留 `.git` 及最低恢复文件。仅在计划与说明已发布且独立恢复核验后，运行 `python -B work/session-boss/tools/prune_github_verified_source_cache_v3.py <计划> <新远端证明> --apply --output <新的 work/session-boss/reports 子目录>`。记录路径哈希、提交、释放字节，并要求引用进程和文件句柄均为零。规范源码、冻结输入、原始证据、失败记录、正式回执、D 盘历史及两个正式实例均排除在清理范围外。
