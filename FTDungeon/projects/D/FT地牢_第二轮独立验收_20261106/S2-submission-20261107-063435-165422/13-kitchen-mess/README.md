# S2-13 接手最终候选

NBT: s2-13-kitchen-mess.nbt

SHA-256: de9a5b1f3bc12791bb23d4b2777bae9b5ba8c98ce9a35fc36f43932ece77b28e

已看原参考、两次修正后Lodestone锅近景。原先3×3全高黑石遮锅，现外圈半砖、锅心独立抬高，可读原版锅体及锅口；烟囱壁炉罩、双长桌、储物架未删。锅与概念图比例差异仍明显，仅为原版单格锅；不宣称真实生火或食物烹饪。五处原生碰撞目标和逐非空气支撑通过，菌光体替代火。

接口: [{"side": "west", "feet": [0, 2, 8], "width": 3, "clearHeight": 4}, {"side": "east", "feet": [28, 2, 8], "width": 3, "clearHeight": 4}]

与参考差异:
- 原版二级水炼药锅独立抬高于黑石炉台，前后黑石外圈降半砖露出锅体；菌光体替代火与熔岩，壁炉罩和烟囱完整。
- 原版切石机作为切配案工具意象，无自定义肉食或刀具。
- 器皿由空花盆与陶罐表达，餐桌没有可拾取餐食。
- 恢复完整墙顶，保留剖切展示副本。

可复跑: 本组 takeover.py verify 13-kitchen-mess；run-build.mjs 的配方为本目录 recipe.mjs；本组 render.cjs 接本目录路径。

原生静态、全方块 canSurvive、原目标碰撞路线及220刻隔离ServerLevel已执行；运行时报告 D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S2\runtime-qa\13-kitchen-mess-runtime.json。动态差异仅楼梯/墙/栅栏连接属性变化，完整详单见 qa/runtime-summary.json；真实玩家游玩、随机刻激活覆盖、所有功能交互及战斗未验证。制作方自检不是独立通过。
