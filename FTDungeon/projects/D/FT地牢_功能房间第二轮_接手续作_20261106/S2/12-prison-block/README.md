# S2-12 接手最终候选

NBT: s2-12-prison-block.nbt

SHA-256: d019a35d85378e50deb3f2d188149a6d235ab1f63f78448e606deafb1f07201b

参考及修正后实际预览已查看。运行时发现原拉杆挂铁门上，使用掉落；已将八个拉杆移动一格至新增雕纹砂岩实心支座，八门Lever.use实测由关闭到打开再关闭且无掉落。保留双排牢房、看守桌、所有原路线目标。原生五目标与全方块支撑通过；并未把外走廊可达等同于真实玩家牢内穿行。

接口: [{"side": "south", "feet": [13, 2, 34], "width": 3, "clearHeight": 4}, {"side": "north", "feet": [13, 2, 0], "width": 3, "clearHeight": 4}]

与参考差异:
- 补齐完整墙顶，剖切仅预览。
- 铁门默认关闭，走廊侧拉杆改为靠门雕纹砂岩实心支座；隔离ServerLevel中八门均通过Lever.use开关复位。未宣称关闭铁门可穿行或真实玩家牢内路线已验证。
- 干草铺位代替定制床垫；无钥匙实体或虚构监禁玩法。
- 看守桌略偏西保留右侧三格以上绕行路线。

可复跑: 本组 takeover.py verify 12-prison-block；run-build.mjs 的配方为本目录 recipe.mjs；本组 render.cjs 接本目录路径。

原生静态、全方块 canSurvive、原目标碰撞路线及220刻隔离ServerLevel已执行；运行时报告 D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S2\runtime-qa-r2\12-prison-block-runtime.json。动态差异仅楼梯/墙/栅栏连接属性变化，完整详单见 qa/runtime-summary.json；真实玩家游玩、随机刻激活覆盖、所有功能交互及战斗未验证。制作方自检不是独立通过。
