# S7 蘑菇真菌功能房间第二轮

仅写本组目录；不写 saves/mods。所有通过均为制作方自检，不是独立验收。

|房间|候选SHA256|自检|
|---|---|---|
|S7-11 图书馆/档案室 Library / Archive|0f22ddfdbd072596384bc8576ced98b706240c6f8a9c8b1583d070394fc72b89|SELF_CHECK_PASS|
|S7-12 牢房区 Prison Cellblock|4e61eb1af2eb6134e6745ff8469b3de9008b6a9a4919a2a7229a06de50a6a4d0|SELF_CHECK_PASS|
|S7-13 厨房食堂 Kitchen & Mess|275433233782748c5898e34e7c11131df97b29694e087372ba794811cc38e4b9|SELF_CHECK_PASS|
|S7-14 军械库 Armory|0ef237823e51c9c7ba626ec4edf68d5d3dad66df9db68814f3757dcc28698ffa|SELF_CHECK_PASS|
|S7-15 训练场 Training Yard|83a73f077132ec235ef5d3e842a582028f6a086b88c697cff7d54a937c1dd9dd|SELF_CHECK_PASS|
|S7-16 水井蓄水池 Well / Cistern|78c1d968be212b027619939badcf1dc248414a694a90c85c5d7f2839bed27518|SELF_CHECK_PASS|
|S7-17 工坊 Workshop|ed5e37f6271bb2b223018fa3e716edd03f084838c62a9a619324eabf6a7b6654|SELF_CHECK_PASS|
|S7-18 骨室墓窖 Crypt / Ossuary|3b9c434fd8eb945de2a36e0208ed66255961143b3638e2e84cda66c69926ab3d|SELF_CHECK_PASS|
|S7-19 瞭望台 Watch Balcony|f4fe6de59ca5e774e3b1ccd90053eea3ca3a8d4a0ec86d2caa254dc358611715|SELF_CHECK_PASS|
|S7-20 密道藏宝 Secret Passage & Vault|5120f94e94c75b898773a17c361909a8372e665022e2d4229f92548392d7c197|SELF_CHECK_PASS|

每房间：recipe.mjs 可复现配方；design.json 图像观察、接口、目标、差异；完整 .nbt；full.png / cutaway.png / interior.png 真实 Lodestone 渲染；qa/ 方块与实体清单、1536m Java 原版注册/支撑/人体碰撞路线、当前候选哈希。

复现：node <skill>/scripts/run-build.mjs <room>/recipe.mjs --out-dir <room>；node render.cjs <candidate.nbt>；使用工单Python运行 qa.py <candidate.nbt>。

尚未验证：游戏世界放置、邻居/随机/流体 tick、攀梯物理、战斗与生存通关；待外部独立复核。十图完成前不生成组合。
