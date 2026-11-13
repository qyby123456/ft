# S2-15 制作方交付

参考实际观察：后墙三个靶、中央三具稻草木人、前景两道矮栏、右侧吊挂障碍框、前墙观众长凳、双侧拱门。

NBT：s2-15-training-yard.nbt；尺寸 [31, 13, 27]；Java 1.20.1 / DataVersion 3465。
SHA-256：`e4b2aa3e4b08e56b40cc6683d300542073835df91ae80e030b8620e4146a5d91`

## 实际接口（局部整数脚点；人体中心 x/z +0.5）
- west：[0, 2, 8]，净宽 3 / 净高 4
- east：[30, 2, 8]，净宽 3 / 净高 4

## 与图差异
- 完整围护顶使用大面积淡黄玻璃还原露天采光，不漏顶；庭院开放感以玻璃天井表达。
- 三具稻草木人是固定方块，不是战斗实体。
- 障碍有独立绕行道；不声明无需跳跃能越过障碍本体。
- 靶采用原版target，未安装计分电路；无武器库存或训练战斗逻辑。
- R2补三面橙色菱形原版旗帜；靶为原版六面靶纹，无单独朝向属性。

## 可复跑
- node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\15-training-yard\recipe.mjs" --out-dir "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\15-training-yard" --json
- C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\check.py" 15-training-yard
- node "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\render.cjs" "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\15-training-yard"

真实 Lodestone 完整 / 剖切 / 内景预览见 PNG；剖切只在显示副本执行，NBT完整。
qa/inventory.json 包含方块与实体载荷。qa/commands.json 记录原生调用；每次 Java -Xmx1536M，在当前 qa 工作目录运行，未运行共享脚本。
制作方自检：原版注册属性、模板读取、所选脆弱装饰支撑、0.6×1.8 离线碰撞路线、空库存、静态风险。
未执行真实世界放置、邻居/随机/液体 tick、动态照明、战斗或生存通关；不等于独立验收。

## 视觉检查
R2内景已查看：三面橙旗、原三木人三靶双障碍与门柱檐口存在。靶原版六面皆有纹无朝向属性；未删除障碍或原五路线目标，全部通过。旗图层Lodestone不显示待游戏复核。
