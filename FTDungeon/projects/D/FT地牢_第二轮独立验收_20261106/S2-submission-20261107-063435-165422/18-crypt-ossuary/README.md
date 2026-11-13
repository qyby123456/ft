# S2-18 接手最终候选

NBT: s2-18-crypt-ossuary.nbt

SHA-256: 5e6c9532b1e2f3e1004b45581b7807a37912b8355535c5476da90d561d2900a3

已看原参考、剖切与内景。双棺、阶坛、两边分层骨龛及中央旗帜可辨；骨龛浅色方块不据实渲自动认作骷髅模型，原生注册表确认实际skeleton_skull和bone_block。Lodestone未呈现完整颅骨贴图与旗纹，需保留限制。橙色地纹局部被大台基覆盖，与参考密纹不同。五目标通路和全非空气支撑通过。

接口: [{"side": "west", "feet": [0, 2, 9], "width": 3, "clearHeight": 4}, {"side": "east", "feet": [26, 2, 9], "width": 3, "clearHeight": 4}]

与参考差异:
- 颅骨和骨块使用原版方块；不生成生物或掉落骨头。
- 石棺为固定石构而非可开容器，中央祭台没有召唤或战斗功能。
- 已补原版橙色菱形纹纪念旗，颅骨与骨块为实际原版方块。
- 完整墙顶恢复，通路绕开台基与棺体，无需攀跳。

可复跑: 本组 takeover.py verify 18-crypt-ossuary；run-build.mjs 的配方为本目录 recipe.mjs；本组 render.cjs 接本目录路径。

原生静态、全方块 canSurvive、原目标碰撞路线及220刻隔离ServerLevel已执行；运行时报告 D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S2\runtime-qa\18-crypt-ossuary-runtime.json。动态差异仅楼梯/墙/栅栏连接属性变化，完整详单见 qa/runtime-summary.json；真实玩家游玩、随机刻激活覆盖、所有功能交互及战斗未验证。制作方自检不是独立通过。
