# v5 模板与运行时 VERSION 的离线 core 补证

本目录仅新增测试与证据。复用已封存的原始 core JAR 和 envelope planner v3 JAR，不修改或重新构建任何生产规划器源码。此前 envelope-usability-review 的 397 个封存文件及 ready.json 在准备和结束时逐项校验。

输入为 catalog/s8-07-doorway-review-v5/nine-room-package-v5/candidate-manifest.json 的真实九个 ID、NBT SHA256、尺寸与端口。入口确实使用 s8_07_widearch_side_doublegate_v5（SHA256 bd7d0661b818164a3617629096580c646d17d95832269e6aca989933dc815ae3）。这是八个原件加一个衍生件；所有 accepted 字段均为 false。

精确 VERSION：`runtime-v5-explicit-commit1-envelope-v3-derived07`。worldSeed 0–31 各跑 ordinal 0–3，另跑原失败 worldSeed 267611119455246637、ordinal 3，共 129 个请求。每个成功请求进行同输入重放；另验证原失败 seed 在新 v5 上下文中的模板及端口顺序独立性。此用例保留原 seed/ordinal，但新 ID 与 VERSION 改变了随机上下文，不宣称复刻旧版 instanceId。

结果：129/129；Java 17.0.8 共 29,672 断言，Python 独立几何复核 29,310 断言。验证九房八连接、实际旋转边界、Y 对齐、端口宽高介质、完整走廊壳碰撞、树连通、原样 runtime 操作包络与预算。矩阵最大操作体积 1,198,400。原失败 seed 新上下文的内包络 197×25×142、操作体积 931,364。

测试请求 anchor=(1000,200,1000)、overworld、s8、9 房、maxAttempts=50000，空 occupied boxes；限制 minY=164、maxYExclusive=316、maxGraphHeight=64（假定 runtime maxBuildHeight=320），rootQuarterTurns=ordinal%4。未扩大 256×96×256 / 1,500,000 预算。

runtime/next-commit 仍为活动目录；results/runtime-binding.json 记录开始和结束的 VERSION 与源码 SHA256。VERSION、manifest 或任何 NBT 资源变化都必须重新 prepare.py、编译及完整跑矩阵，之后再验证和封存。build-and-test.ps1 在当前输入快照上重跑；prepare.py 会显式更新快照。

未启动世界，未验证实际门洞全宽可通行，未批准模板、未修改正式 mods/saves，未晋升生产。实际门洞与世界回滚测试仍由集成验收承担。ready.json 仅封存本目录离线证据。
