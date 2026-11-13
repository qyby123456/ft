# S2-16 接手最终候选

NBT: s2-16-well-cistern.nbt

SHA-256: e77ebc2cdc35d93fa292eba9bc07a54b810100231962d5a1e89ec97811598106

已看原参考及实际剖切、内景。井池、横梁、链吊空桶、后侧登台楼梯及右侧浅水槽/过桥保持。静态六目标路线包含阶梯平台通过，不能替代实际掉水后逃生和流体刻。水槽为受限一格深源水，真实邻居更新和水源保持待隔离世界测试。

接口: [{"side": "west", "feet": [0, 2, 7], "width": 3, "clearHeight": 4}, {"side": "east", "feet": [24, 2, 6], "width": 3, "clearHeight": 4}]

与参考差异:
- 井水深一格，完整井壁与封底；取水桶为装饰空桶，无升降机械。
- 水渠独立封闭，石半砖桥跨越；干路测试不要求涉水。
- 后阶梯通向实际维护平台；新增平台路线目标，原五处目标不删改。
- 完整墙顶补齐，未执行液体tick，流动稳定性仍需真实世界复核。

可复跑: 本组 takeover.py verify 16-well-cistern；run-build.mjs 的配方为本目录 recipe.mjs；本组 render.cjs 接本目录路径。

原生静态、全方块 canSurvive、原目标碰撞路线及220刻隔离ServerLevel已执行；运行时报告 D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S2\runtime-qa\16-well-cistern-runtime.json。动态差异仅楼梯/墙/栅栏连接属性变化，完整详单见 qa/runtime-summary.json；真实玩家游玩、随机刻激活覆盖、所有功能交互及战斗未验证。制作方自检不是独立通过。
