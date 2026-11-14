# Curios 有人值守的新 JVM 重登工具与 V18 诊断

当前状态：V18 未通过。新 JVM 公开打开已接受的 V6 正常保存世界的全新副本，实际进入实验性 BackupConfirmScreen。启动前及页面出现时均明确通知用户立即操作，页面等待 180 秒后仍未收到确认。探针严格失败 ActualUserExperimentalConfirmationNotReceivedWithin180Seconds，客户端正常退出、进程已回收，未进入集成服务端或玩家登录，因此没有完整世界保存或持久化验收声明。所有冻结输入哈希保持不变。失败世界仅作证据，不得作为后续种子。

## 下次操作

先由用户在本聊天确认已经在电脑前、准备立即操作，然后启动以下命令；该命令每次创建新的副本，原始 V6 世界不会被改动。看到隔离 Minecraft 窗口的 Worlds using Experimental Settings are not supported 页面后，用户需在 180 秒内点击 **Create Backup and Load**。之后不要关闭窗口，等待探针验证真实登录、原饰品数量/组件、无残留沉默记录、120 个自然 tick 和客户端同步，再正常退出。当前没有等待窗口，工具不会自行重试。

`python -B work/session-boss/tools/run_boss_zicai_curio_client_v18.py work/session-boss/build/boss-zicai-curio-silence-v1-20261113/candidate-build-receipt.json candidate work/session-boss/reports/zicai-curio-client-v6-candidate-ae98fdc5.json`

此命令需要既有本地 SDK、Java21、依赖、候选 JAR、种子及冻结输入；普通 Git 源码恢复不等于这些大产物均可远端恢复。前台状态见新 attempt/game/attended-reload-status.json，记录实际页面、按钮、截止时刻，不注入点击。只读观察到实际页面离开后还必须经过真实原生登录才能通过；不宣称额外监控了人机输入来源。

## 证据与恢复

源码检查点为 github-sync/checkpoint-curios-attended-reload-v18-20261114.json；其中保留严格失败、归档哈希和未通过状态。增量归档仍依赖基础包 boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip，SHA256 aca9e96def7887e0cf52637152df8922174326d5970376918d42b08bee864929。使用 restore_boss_zicai_curio_archive_v2.py <增量包> --base <基础包> 校验，添加 --output <新目录> 可恢复。全部载荷哈希校验，不宣称整包物理恢复或大型产物的远端恢复。源码发布后独立从远端拉回，核对每个路径、大小、SHA256、提交和祖先关系；只清理已核验且无进程或句柄引用的重复源码缓存，规范输入、失败世界、原始归档和 JAR 均保留。

已通过的四类回退、18 项归属、跨维度、默认死亡、真实容器、多饰品和停止状态直接引用，没有重跑。另已记录非庄园终结难度的原/当前属性缩放静态差异及真实基线需求；未将类名、标签或反编译差异冒充运行时缺陷，也未绕过挑战解锁门槛。完整 Boss 目标仍未完成。

NOT-INSTALLABLE、不部署、不覆盖正式实例；候选 JAR 保持不变，武器与 Session 暂停，同一 C 工作区唯一写入者。
