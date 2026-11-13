# S2-11 接手最终候选

NBT: s2-11-library.nbt

SHA-256: b13b636feb8bcfe128792810131249fea66b656f5b7579fca5c11a199dc8bcf8

新b13b636候选保留梯旗、门拱、两桌与L形书架。原生Player.travel实测旧梯能爬但顶檐阻挡落脚，故加西侧三格半砖窄平台及两端贴架托臂；七段梯不删不改。已看新梯近景，深木平台在顶端可见。runtime-qa-r4真实ServerLevel与生存FakePlayer仅水平前进travel爬升，顶端转西侧移站稳到相对[21.317,9,11.5]，noCollision与onGround通过；未注入上升速度或起点后传送。下降、人类输入、GUI及完整游玩未测。旧72efa独立证据只保留历史，新哈希待独立复核。

接口: [{"side": "west", "feet": [0, 2, 9], "width": 3, "clearHeight": 4}, {"side": "east", "feet": [24, 2, 18], "width": 3, "clearHeight": 4}]

与参考差异:
- 剖切墙顶恢复完整砂岩墙顶，黄色玻璃天窗保留采光意向。
- 书页用原版讲台替代，不伪造有书状态；无散落卷轴自定义模型。
- 原版橙旗和贴架直梯保留；梯顶向西加三格窄木落脚板及两端连架托臂，避开檐口，不改变书架主体占地。原概念斜梯改直梯，无尘埃光束。
- 柜体仅为空桶，不虚构锁定交互。

可复跑: 本组 takeover.py verify 11-library；run-build.mjs 的配方为本目录 recipe.mjs；本组 render.cjs 接本目录路径。

原生静态、全方块 canSurvive、原目标碰撞路线及220刻隔离ServerLevel已执行；运行时报告 D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S2\runtime-qa-r4\11-library-runtime.json。动态差异仅楼梯/墙/栅栏连接属性变化，完整详单见 qa/runtime-summary.json；真实玩家游玩、随机刻激活覆盖、所有功能交互及战斗未验证。制作方自检不是独立通过。
