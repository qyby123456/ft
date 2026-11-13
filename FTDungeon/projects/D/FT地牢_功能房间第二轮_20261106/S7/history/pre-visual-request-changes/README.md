# S7 蘑菇真菌功能房间第二轮

仅写本组目录；不写 saves/mods。所有通过均为制作方自检，不是独立验收。

|房间|候选SHA256|自检|
|---|---|---|
|S7-11 图书馆/档案室 Library / Archive|3ab7ad3520a34d04e8fcda2155d9d0cdb043a85446cc63ed8af68ea2d213eb26|SELF_CHECK_PASS|
|S7-12 牢房区 Prison Cellblock|c5651a430189f264d790fa0d58accb97bbbc5cbf9425514dfa6469b230320665|SELF_CHECK_PASS|
|S7-13 厨房食堂 Kitchen & Mess|594ad4c90e7a460972dddfac56eb4d2d38823adc2bb9ec34d2124438bbd690a7|SELF_CHECK_PASS|
|S7-14 军械库 Armory|f6429152a5d7461d147e54588fe44d10a57b61377ac216da50560e73d2e76d7a|SELF_CHECK_PASS|
|S7-15 训练场 Training Yard|efd66225af028e44ffd65761350c68f27dd4e5c6bb088f024228d680b73f6086|SELF_CHECK_PASS|
|S7-16 水井蓄水池 Well / Cistern|fbb213bafbf08b132d313bc0885ee7a704c29f8f284e43ba133f3d1d3c9cc2e7|SELF_CHECK_PASS|
|S7-17 工坊 Workshop|ed5e37f6271bb2b223018fa3e716edd03f084838c62a9a619324eabf6a7b6654|SELF_CHECK_PASS|
|S7-18 骨室墓窖 Crypt / Ossuary|3b9c434fd8eb945de2a36e0208ed66255961143b3638e2e84cda66c69926ab3d|SELF_CHECK_PASS|
|S7-19 瞭望台 Watch Balcony|f4fe6de59ca5e774e3b1ccd90053eea3ca3a8d4a0ec86d2caa254dc358611715|SELF_CHECK_PASS|
|S7-20 密道藏宝 Secret Passage & Vault|5120f94e94c75b898773a17c361909a8372e665022e2d4229f92548392d7c197|SELF_CHECK_PASS|

每房间：recipe.mjs 可复现配方；design.json 图像观察、接口、目标、差异；完整 .nbt；full.png / cutaway.png / interior.png 真实 Lodestone 渲染；qa/ 方块与实体清单、1536m Java 原版注册/支撑/人体碰撞路线、当前候选哈希。

复现：node <skill>/scripts/run-build.mjs <room>/recipe.mjs --out-dir <room>；node render.cjs <candidate.nbt>；使用工单Python运行 qa.py <candidate.nbt>。

尚未验证：游戏世界放置、邻居/随机/流体 tick、攀梯物理、战斗与生存通关；待外部独立复核。十图完成前不生成组合。
