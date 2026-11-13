# S2-14 接手最终候选

NBT: s2-14-armory.nbt

SHA-256: eae663b4bec551379ffe0ebf8ebf5ad390bc6b9b5c61948fab80120930e606bb

已看原参考、剖切与室内实渲。双武器架、银金方块甲、中央桌、右侧高柜与铁砧可读，原尺寸布局保留。陈列为方块雕像而非盔甲架或可取装备，武器为铁栏/链形态，不能声称装备实体交互。橙色中心边框及门柱、柱头已存在。原生五目标及全非空气支撑通过。

接口: [{"side": "west", "feet": [0, 2, 7], "width": 3, "clearHeight": 4}, {"side": "east", "feet": [26, 2, 7], "width": 3, "clearHeight": 4}]

与参考差异:
- 银金铠甲用稳定方块陈列替代盔甲架实体，不声明能穿戴或取用。
- 兵器架用铁栏与链表达轮廓，不虚构可取武器、箭矢与钥匙。
- 高柜为空桶，没有锁定交互或战利品。
- 完整顶墙、支撑铁砧台，中央桌可四周绕行。

可复跑: 本组 takeover.py verify 14-armory；run-build.mjs 的配方为本目录 recipe.mjs；本组 render.cjs 接本目录路径。

原生静态、全方块 canSurvive、原目标碰撞路线及220刻隔离ServerLevel已执行；运行时报告 D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S2\runtime-qa\14-armory-runtime.json。动态差异仅楼梯/墙/栅栏连接属性变化，完整详单见 qa/runtime-summary.json；真实玩家游玩、随机刻激活覆盖、所有功能交互及战斗未验证。制作方自检不是独立通过。
