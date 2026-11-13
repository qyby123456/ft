# S2-16 制作方交付

参考实际观察：中央矩形井、双木柱横梁链桶、右侧低水渠与桥、后墙阶梯、左右石拱与水罐。

NBT：s2-16-well-cistern.nbt；尺寸 [25, 13, 25]；Java 1.20.1 / DataVersion 3465。
SHA-256：`e77ebc2cdc35d93fa292eba9bc07a54b810100231962d5a1e89ec97811598106`

## 实际接口（局部整数脚点；人体中心 x/z +0.5）
- west：[0, 2, 7]，净宽 3 / 净高 4
- east：[24, 2, 6]，净宽 3 / 净高 4

## 与图差异
- 井水深一格，完整井壁与封底；取水桶为装饰空桶，无升降机械。
- 水渠独立封闭，石半砖桥跨越；干路测试不要求涉水。
- 后阶梯通向实际维护平台；新增平台路线目标，原五处目标不删改。
- 完整墙顶补齐，未执行液体tick，流动稳定性仍需真实世界复核。

## 可复跑
- node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\16-well-cistern\recipe.mjs" --out-dir "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\16-well-cistern" --json
- C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\check.py" 16-well-cistern
- node "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\render.cjs" "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\16-well-cistern"

真实 Lodestone 完整 / 剖切 / 内景预览见 PNG；剖切只在显示副本执行，NBT完整。
qa/inventory.json 包含方块与实体载荷。qa/commands.json 记录原生调用；每次 Java -Xmx1536M，在当前 qa 工作目录运行，未运行共享脚本。
制作方自检：原版注册属性、模板读取、所选脆弱装饰支撑、0.6×1.8 离线碰撞路线、空库存、静态风险。
未执行真实世界放置、邻居/随机/液体 tick、动态照明、战斗或生存通关；不等于独立验收。

## 视觉检查
R2剖切已查看，新增维护平台真实接台阶；原五目标全部保留，追加平台第六目标原生路线通过。水井与桥渠保留。未执行流体tick与跌水救援测试，作为待独立复核限制。
