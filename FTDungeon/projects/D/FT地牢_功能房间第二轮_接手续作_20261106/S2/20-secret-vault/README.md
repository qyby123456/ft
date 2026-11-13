# S2-20 接手最终候选

NBT: s2-20-secret-vault.nbt

SHA-256: 9e363db0ad0f5ff6a55de5f891ade2391db1cde55f3d0b4f38d9e0f16200d37f

已看原参考、当前9e363d版本剖切及宝龛焦点近景。宝箱、金块、双柱龛、矮界墙可读；左侧密道高隔墙仍遮挡总览部分焦点，近景补足，非原图完全敞开。拉杆保留近前矮台，真实但未接暗门电路；箱为空，不虚构战利品。当前NBT/实渲/原生报告一致，旧8a4842目检仅作历史保存。五目标包含密道、登阶、箱前、拉杆接近；本房是密道藏宝而非总览。

接口: [{"side": "west", "feet": [0, 2, 9], "width": 3, "clearHeight": 4}, {"side": "east", "feet": [24, 2, 9], "width": 3, "clearHeight": 4}]

与参考差异:
- 伪装墙保留三格净宽常开密道；宝龛前隔墙降为一格矮界墙以展示箱子焦点，拉杆未接线。
- 箱子为空且没有Lock/LootTable，金堆为金方块，不是掉落金币。
- 宝龛使用半格楼梯到一格高平台，蛛网位于高角不堵脚路。
- 这张是独立密道藏宝功能房，不是组总览；补齐完整墙顶。

可复跑: 本组 takeover.py verify 20-secret-vault；run-build.mjs 的配方为本目录 recipe.mjs；本组 render.cjs 接本目录路径。

原生静态、全方块 canSurvive、原目标碰撞路线及220刻隔离ServerLevel已执行；运行时报告 D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S2\runtime-qa\20-secret-vault-runtime.json。动态差异仅楼梯/墙/栅栏连接属性变化，完整详单见 qa/runtime-summary.json；真实玩家游玩、随机刻激活覆盖、所有功能交互及战斗未验证。制作方自检不是独立通过。
