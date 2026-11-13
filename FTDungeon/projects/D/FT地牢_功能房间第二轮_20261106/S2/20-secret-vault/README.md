# S2-20 制作方交付

参考实际观察：左侧伪装墙窄道、右侧双柱宝龛、台阶上的箱子和金堆、前方低台拉杆、上角蛛网、两端拱门。

NBT：s2-20-secret-vault.nbt；尺寸 [25, 12, 21]；Java 1.20.1 / DataVersion 3465。
SHA-256：`8a484209deef1a2ce371cc50223b3e623f8d3937aa25069f675738b93a4879ce`

## 实际接口（局部整数脚点；人体中心 x/z +0.5）
- west：[0, 2, 9]，净宽 3 / 净高 4
- east：[24, 2, 9]，净宽 3 / 净高 4

## 与图差异
- 伪装墙保留3格净宽常开口，不虚构活塞暗门逻辑；拉杆为真实但未接线的装饰控制。
- 箱子为空且没有Lock/LootTable，金堆为金方块，不是掉落金币。
- 宝龛使用半格楼梯到一格高平台，蛛网位于高角不堵脚路。
- 这张是独立密道藏宝功能房，不是组总览；补齐完整墙顶。

## 可复跑
- node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\20-secret-vault\recipe.mjs" --out-dir "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\20-secret-vault" --json
- C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\check.py" 20-secret-vault
- node "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\render.cjs" "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\20-secret-vault"

真实 Lodestone 完整 / 剖切 / 内景预览见 PNG；剖切只在显示副本执行，NBT完整。
qa/inventory.json 包含方块与实体载荷。qa/commands.json 记录原生调用；每次 Java -Xmx1536M，在当前 qa 工作目录运行，未运行共享脚本。
制作方自检：原版注册属性、模板读取、所选脆弱装饰支撑、0.6×1.8 离线碰撞路线、空库存、静态风险。
未执行真实世界放置、邻居/随机/液体 tick、动态照明、战斗或生存通关；不等于独立验收。

## 视觉检查
真实剖切及宝龛内景已查看，隐道墙、双柱宝龛、金块空箱与高角蛛网可读。原生五处通路包含窄道和登台箱前通过；拉杆不接线，未宣称暗门机关或战利品玩法。
