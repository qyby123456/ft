# S2-13 制作方交付

参考实际观察：后墙巨型炉灶和锅、右侧分层器皿架、灶前案台、前景两张长餐桌与长凳、两侧拱门。

NBT：s2-13-kitchen-mess.nbt；尺寸 [29, 13, 25]；Java 1.20.1 / DataVersion 3465。
SHA-256：`ce414788bea096d1f3213e084410b17d81df7b6515f224b9f18fc2adeae57366`

## 实际接口（局部整数脚点；人体中心 x/z +0.5）
- west：[0, 2, 8]，净宽 3 / 净高 4
- east：[28, 2, 8]，净宽 3 / 净高 4

## 与图差异
- 独立3×3黑石大锅包围原版半满水炼药锅，暖光菌光体代替火与熔岩；宽壁炉罩与烟囱完整。
- 原版切石机作为切配案工具意象，无自定义肉食或刀具。
- 器皿由空花盆与陶罐表达，餐桌没有可拾取餐食。
- 恢复完整墙顶，保留剖切展示副本。

## 可复跑
- node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\13-kitchen-mess\recipe.mjs" --out-dir "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\13-kitchen-mess" --json
- C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\check.py" 13-kitchen-mess
- node "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\render.cjs" "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\13-kitchen-mess"

真实 Lodestone 完整 / 剖切 / 内景预览见 PNG；剖切只在显示副本执行，NBT完整。
qa/inventory.json 包含方块与实体载荷。qa/commands.json 记录原生调用；每次 Java -Xmx1536M，在当前 qa 工作目录运行，未运行共享脚本。
制作方自检：原版注册属性、模板读取、所选脆弱装饰支撑、0.6×1.8 离线碰撞路线、空库存、静态风险。
未执行真实世界放置、邻居/随机/液体 tick、动态照明、战斗或生存通关；不等于独立验收。

## 视觉检查
R2大锅近景已查看：3x3黑石锅沿包围实际水炼药锅，暖光基座、完整壁炉罩与烟囱存在。是方块化大锅替代，不宣称圆锅或可烹饪交互；双餐桌和原五目标保留通过。
