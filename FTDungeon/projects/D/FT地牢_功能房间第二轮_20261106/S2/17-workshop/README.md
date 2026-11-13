# S2-17 制作方交付

参考实际观察：左后烟囱炉、后墙工具案台、右后材料架与砂轮、中央有未完成框架的工台、铁砧、双侧出口。

NBT：s2-17-workshop.nbt；尺寸 [25, 13, 25]；Java 1.20.1 / DataVersion 3465。
SHA-256：`7c2d9685f9af00cbe940eec346edb0e93344f6c38567255ed013a9a5179595af`

## 实际接口（局部整数脚点；人体中心 x/z +0.5）
- west：[0, 2, 7]，净宽 3 / 净高 4
- east：[24, 2, 7]，净宽 3 / 净高 4

## 与图差异
- 熔炉为空且熄灭，邻近菌光体只表达暖光，无火焰或熔岩。
- 砂轮用原版grindstone，中央框架是静态木构，不虚构加工过程。
- 工具材料使用原版工作方块和矿料方块，无掉落工具实体。
- 恢复完整墙顶，烟囱止于顶，不形成未封闭穿洞。
- R2中央桌改四根木腿，移除实心侧台结构；木框通透，增加切石机与花盆工具意象近景。

## 可复跑
- node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\17-workshop\recipe.mjs" --out-dir "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\17-workshop" --json
- C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\check.py" 17-workshop
- node "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\render.cjs" "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\17-workshop"

真实 Lodestone 完整 / 剖切 / 内景预览见 PNG；剖切只在显示副本执行，NBT完整。
qa/inventory.json 包含方块与实体载荷。qa/commands.json 记录原生调用；每次 Java -Xmx1536M，在当前 qa 工作目录运行，未运行共享脚本。
制作方自检：原版注册属性、模板读取、所选脆弱装饰支撑、0.6×1.8 离线碰撞路线、空库存、静态风险。
未执行真实世界放置、邻居/随机/液体 tick、动态照明、战斗或生存通关；不等于独立验收。

## 视觉检查
R2木工桌定向近景已查看：四腿桌下镂空，通透木框、切石机、按钮花盆工具意象明确；炉与铁砧、后案料架保留。原五目标和全格支撑通过。
