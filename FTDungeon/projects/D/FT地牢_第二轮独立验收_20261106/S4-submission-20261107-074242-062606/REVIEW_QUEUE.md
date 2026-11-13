# S4 R2 待独立复核

先读 ready-for-review.json；accepted=false，独立决定仍待更新。旧 R1 REQUEST_CHANGES 有效，新作者 PASS 不替代独立验收。

1. 19 号仅局部整改，核其新 SHA、完整/局部/玩家观察图，以及随包栏外水域、根系、残墙和木桩。另九房无需重造，按冻结哈希保留已查视觉结论。
2. 核 DELIVERY-SHA256.json 的所有当前文件、参考图/提示词及工具外部依赖；排除项为明确缓存、历史或生成世界。用 assembly/reproduce.ps1 -OutputDirectory <独立输出> 重建合并与 18 片，比对 NBT 压缩字节及完整状态/BE/实体。作者自己的独立输出复现见 qa/assembly-reproducibility-r2.json。
3. 核 merged-interaction.json 中全部接口 AABB：模块全宽全高及连接廊逐列，不能以有限功能目标代替。两端外侧屏障是封闭测试候选的终端，不是已接通外部世界的入口；终端记录不隐瞒封口。
4. 核 qa/merged-world-r2 的当前 exit/tests/world-report/控制台、精确候选哈希、三阶段 NBT。失败日志留在 history-*，基线地形污染的旧失败不得替代最终结果。最终测试放在实测地形最高点上方，放置前清基线、放置后无清理。
5. 核 56 个原目标实际碰撞往返、6 次拉杆真实玩家 use 开关门、32 个容器真实打开菜单、眼距/视线/返回路线，明确 FakePlayer 脚本运动和网络客户端实玩区别。
6. 按 [-2,0,-2] 捕获偏移核 classified-differences.json，逐项看有限藤蔓扩展、红树叶 distance 默认更新、门半部及铁栏属性、原版补出的默认 BE 和装备架字段。ArmorStand 的 Mob 专用 PersistenceRequired 不会保存；不把原始载荷宣称完全一致。qa/r2-all 对三份刻后快照重新验证全部 56 个目标及支撑。

最终合并 415×24×67，18 片反拼 667320 格；源模块 123439 格、32 个源 BE、2 具实体保持。旧 S4 577 文件只读哈希复核见 qa/source-readonly-check.json。

未认证：正式世界外部接入、客户端按键、生存/战斗/刷怪、旋转镜像和长期植物生长。以实际 harness_rules 为准：最终 doMobSpawning=true、doWeatherCycle=true，测试未改规则；不能凭开关开启就宣称玩法认证。
