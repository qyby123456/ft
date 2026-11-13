# S2-12 制作方交付

参考实际观察：纵向中央走廊，两侧四列分隔牢房、铁栏铁门、成对吊灯、前方看守桌、两端出口。

NBT：s2-12-prison-block.nbt；尺寸 [27, 12, 35]；Java 1.20.1 / DataVersion 3465。
SHA-256：`8c80694a5865485f6fb2c6ac430a722f2617bb77bb82db13ddf89185ab3c3b7e`

## 实际接口（局部整数脚点；人体中心 x/z +0.5）
- south：[13, 2, 34]，净宽 3 / 净高 4
- north：[13, 2, 0]，净宽 3 / 净高 4

## 与图差异
- 补齐完整墙顶，剖切仅预览。
- 铁门默认关闭且有走廊侧拉杆；未声明离线路线可穿关闭铁门，牢房内部交互需游戏复核。
- 干草铺位代替定制床垫；无钥匙实体或虚构监禁玩法。
- 看守桌略偏西保留右侧三格以上绕行路线。

## 可复跑
- node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\12-prison-block\recipe.mjs" --out-dir "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\12-prison-block" --json
- C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\check.py" 12-prison-block
- node "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\render.cjs" "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\12-prison-block"

真实 Lodestone 完整 / 剖切 / 内景预览见 PNG；剖切只在显示副本执行，NBT完整。
qa/inventory.json 包含方块与实体载荷。qa/commands.json 记录原生调用；每次 Java -Xmx1536M，在当前 qa 工作目录运行，未运行共享脚本。
制作方自检：原版注册属性、模板读取、所选脆弱装饰支撑、0.6×1.8 离线碰撞路线、空库存、静态风险。
未执行真实世界放置、邻居/随机/液体 tick、动态照明、战斗或生存通关；不等于独立验收。

## 视觉检查
R2剖切已查看：端部门框凸柱柱头、分层檐口与边框地纹已补；所有原路线目标保留通过。铁门逐格canSurvive通过，拉杆为真实支撑，但未模拟电路更新与全牢房开门操作。
