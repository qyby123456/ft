## Stage35 原版麻雀完整方法与存储适配，2026-11-14

Stage35 源码及限定文字证据已推送 4fc5b33a62fba6c4c0ded07cbc483f96c33b17f1；47 文件经独立远端逐 blob 恢复，4805 文件整树资源排除检查通过。进程/句柄均为零后，仅清理 Git 工作副本 9 份重复 Java/Python（107235 字节），skip-worktree；canonical 冻结输入保留。28 个关闭运行 JAR 保留全部原路径/字节，仅本地只读去重，释放约 150 MiB。源码恢复入口 github-sync/checkpoint-sparrow-original-multiplayer-20261114.json；预期缺失和清理入口 github-sync/checkpoint-sparrow-original-multiplayer-cleanup-20261114.json。模型、贴图、动画、音乐、音频及嵌套/编码资源、JAR、世界一律不上传，远端不提供排除资源恢复。实际多人、standalone 正例和世界重启仍未验收，正式实例未部署。

候选 9d713622cc8c8aa3d2e5b5fa340053a7eb45f8128247346c3152a14c975b886e、NOT-INSTALLABLE，仅替换 SparrowSupport 并补回 SparrowMultiplayerSupport、SparrowAppearanceData/Entry、SparrowMultiplayerRules 及 1.21.1 SavedData 桥。恢复五个完整原方法，11 个玩法方法全部与官方映射后的原版完整图一致。原事件方法只作 NeoForge Pre/ServerTick.Post 明确接口适配，四项反向原图还原和两负控通过。817 组合检查、533 继承成员链接检查；其他所有旧 JAR entry 包括资源字节不变。

真实离线客户端和集成服 18 场景、13010 检查通过，原注册蛋和真实召唤者身份、自然原版支持/分身及公开投降清理不变。24 个实际定义类捕获，16 个助手字节等于候选；342 个实际 Easter 方法图等于前段已证图，原 hurt 和六个分摊方法保持。原保存记录中的实际 fight/stage、players=1、used=1、真实麻雀 UUID 和出现时间在正常存盘后读回，并通过公开原版 load/save 往返及原配额/重复记录/40 tick 冷却等 38 项边界检查。无新世界重启验收，不等于真实多人。

玩家承伤仍用公开 Pre 限为 0.01 的夹具；非完整实战、存活难度或认证多人验收。standalone 攻击者事件的正例仍待原生验证；不伪造 fight/owner、玩家或认证，不调用私有玩法或手动 tick。世界正常保存、退出 0、进程回收、输入哈希不变；28 个关闭依赖 JAR 仅本地按哈希只读去重，路径和资源字节保留。模型、贴图、动画、音乐、音频、JAR、世界、二进制和嵌套/编码资源一律不上传；Git 仅源码与白名单文字证据，不提供排除资源恢复。正式实例及 D 只读、不部署；武器与 Session 暂停、Curios 不自动重试，完整 Boss 目标保持。详见 work/session-boss/SPARROW_ORIGINAL_MULTIPLAYER_20261114.md 与 reports/sparrow-original-multiplayer-closeout-20261114.json。

待办：

- Actual original standalone attacker native Pre hook, participants and repeat appearance behavior
- Authenticated real multiplayer quota, stage plans and persisted world reload; no unattended Curios retry
- Remaining full original Boss normal/impossible battle, phases, attacks, rewards, death and cancellation
- Existing authenticated positive kill/material/reward integration and remaining visuals/dimensions/persistence acceptance
- Final cumulative installable release, formal-write authorization, deployment and rollback; weapons remain paused
