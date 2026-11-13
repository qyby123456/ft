# S2-19 制作方交付

参考实际观察：有柱支撑的高台、前侧护栏、桌面望远镜意象、后门与兵器架、右侧长下行楼梯。

NBT：s2-19-watch-balcony.nbt；尺寸 [29, 20, 29]；Java 1.20.1 / DataVersion 3465。
SHA-256：`92d7d1b056c24ae87ce4d91f56fc0b3a3225ccb73aa0e4e5a03e5f917b77f957`

## 实际接口（局部整数脚点；人体中心 x/z +0.5）
- north：[14, 8, 0]，净宽 3 / 净高 4
- east：[28, 2, 23]，净宽 3 / 净高 4

## 与图差异
- 高台保持6格高差，六级原版楼梯实际连接上下；不以传送替代。
- 四周完整外壳和深灰玻璃观景窗代替无边界深渊，避免跌落外部。
- 桌面避雷针仅表达镜筒轮廓，不声明可用望远镜；无武器库存。
- 上层护栏与下层支柱保留，完整顶墙在交付NBT中。
- R2侧阶梯护墙降为随台阶递减护栏，柱间增加倒置楼梯拱形支撑；新增底层支撑近景。

## 可复跑
- node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\19-watch-balcony\recipe.mjs" --out-dir "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\19-watch-balcony" --json
- C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\check.py" 19-watch-balcony
- node "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\render.cjs" "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\19-watch-balcony"

真实 Lodestone 完整 / 剖切 / 内景预览见 PNG；剖切只在显示副本执行，NBT完整。
qa/inventory.json 包含方块与实体载荷。qa/commands.json 记录原生调用；每次 Java -Xmx1536M，在当前 qa 工作目录运行，未运行共享脚本。
制作方自检：原版注册属性、模板读取、所选脆弱装饰支撑、0.6×1.8 离线碰撞路线、空库存、静态风险。
未执行真实世界放置、邻居/随机/液体 tick、动态照明、战斗或生存通关；不等于独立验收。

## 视觉检查
R2底层支撑定向近景已查看，柱间倒置楼梯形成阶梯拱托；台阶侧墙降为随级护栏。上门到下门原五目标不变且通过，六格高差保留。避雷针镜筒仅陈设无望远镜交互。
