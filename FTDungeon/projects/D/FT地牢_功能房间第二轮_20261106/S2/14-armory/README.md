# S2-14 制作方交付

参考实际观察：后方长兵器架与箭架、银金两具铠甲陈列、右侧高柜、中央验装桌、右前铁砧、双侧拱门。

NBT：s2-14-armory.nbt；尺寸 [27, 12, 25]；Java 1.20.1 / DataVersion 3465。
SHA-256：`eae663b4bec551379ffe0ebf8ebf5ad390bc6b9b5c61948fab80120930e606bb`

## 实际接口（局部整数脚点；人体中心 x/z +0.5）
- west：[0, 2, 7]，净宽 3 / 净高 4
- east：[26, 2, 7]，净宽 3 / 净高 4

## 与图差异
- 银金铠甲用稳定方块陈列替代盔甲架实体，不声明能穿戴或取用。
- 兵器架用铁栏与链表达轮廓，不虚构可取武器、箭矢与钥匙。
- 高柜为空桶，没有锁定交互或战利品。
- 完整顶墙、支撑铁砧台，中央桌可四周绕行。

## 可复跑
- node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\14-armory\recipe.mjs" --out-dir "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\14-armory" --json
- C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\check.py" 14-armory
- node "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\render.cjs" "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\14-armory"

真实 Lodestone 完整 / 剖切 / 内景预览见 PNG；剖切只在显示副本执行，NBT完整。
qa/inventory.json 包含方块与实体载荷。qa/commands.json 记录原生调用；每次 Java -Xmx1536M，在当前 qa 工作目录运行，未运行共享脚本。
制作方自检：原版注册属性、模板读取、所选脆弱装饰支撑、0.6×1.8 离线碰撞路线、空库存、静态风险。
未执行真实世界放置、邻居/随机/液体 tick、动态照明、战斗或生存通关；不等于独立验收。

## 视觉检查
R2内景已查看：新立体门框、檐口、柱头与中央框纹存在，展示架与双人形保留；陈列为方块不是可穿戴实体，原五目标全部通过。
