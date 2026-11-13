# S2-11 制作方交付

参考实际观察：双侧石拱、两座阅读桌、L形高书架、靠架梯子、后方柜体、陶罐、橙色地纹与采光。

NBT：s2-11-library.nbt；尺寸 [25, 13, 23]；Java 1.20.1 / DataVersion 3465。
SHA-256：`72efa523731bd9f2af6a1d3665cd8996ecabe91ce2e7c1bbb781557ad3e9f37a`

## 实际接口（局部整数脚点；人体中心 x/z +0.5）
- west：[0, 2, 9]，净宽 3 / 净高 4
- east：[24, 2, 18]，净宽 3 / 净高 4

## 与图差异
- 剖切墙顶恢复完整砂岩墙顶，黄色玻璃天窗保留采光意向。
- 书页用原版讲台替代，不伪造有书状态；无散落卷轴自定义模型。
- 已补原版橙色菱形纹旗帜；书梯竖直贴架，独立梯子和旗帜近景证明；无斜梯与尘埃光束。
- 柜体仅为空桶，不虚构锁定交互。

## 可复跑
- node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\11-library\recipe.mjs" --out-dir "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\11-library" --json
- C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\check.py" 11-library
- node "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\render.cjs" "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\11-library"

真实 Lodestone 完整 / 剖切 / 内景预览见 PNG；剖切只在显示副本执行，NBT完整。
qa/inventory.json 包含方块与实体载荷。qa/commands.json 记录原生调用；每次 Java -Xmx1536M，在当前 qa 工作目录运行，未运行共享脚本。
制作方自检：原版注册属性、模板读取、所选脆弱装饰支撑、0.6×1.8 离线碰撞路线、空库存、静态风险。
未执行真实世界放置、邻居/随机/液体 tick、动态照明、战斗或生存通关；不等于独立验收。

## 视觉检查
R2实际查看梯子、旗帜和室内：x22 z11梯子清楚贴书架，背墙橙旗确实可见；Lodestone未显示Patterns菱形图层，NBT保留原版旗帜图层待游戏复核。新增柱头檐口、地纹和门柱拱券；原五处坐标标签未改。旧候选与QA在archive-preliminary-v1。
