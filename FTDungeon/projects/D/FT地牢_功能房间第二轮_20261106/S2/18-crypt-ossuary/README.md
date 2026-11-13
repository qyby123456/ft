# S2-18 制作方交付

参考实际观察：后墙左右三层骨龛、中央橙色纪念壁、双吊灯、中间祭台、前景左右石棺与蜡烛。

NBT：s2-18-crypt-ossuary.nbt；尺寸 [27, 13, 27]；Java 1.20.1 / DataVersion 3465。
SHA-256：`5e6c9532b1e2f3e1004b45581b7807a37912b8355535c5476da90d561d2900a3`

## 实际接口（局部整数脚点；人体中心 x/z +0.5）
- west：[0, 2, 9]，净宽 3 / 净高 4
- east：[26, 2, 9]，净宽 3 / 净高 4

## 与图差异
- 颅骨和骨块使用原版方块；不生成生物或掉落骨头。
- 石棺为固定石构而非可开容器，中央祭台没有召唤或战斗功能。
- 已补原版橙色菱形纹纪念旗，颅骨与骨块为实际原版方块。
- 完整墙顶恢复，通路绕开台基与棺体，无需攀跳。

## 可复跑
- node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\18-crypt-ossuary\recipe.mjs" --out-dir "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\18-crypt-ossuary" --json
- C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\check.py" 18-crypt-ossuary
- node "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\render.cjs" "D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S2\18-crypt-ossuary"

真实 Lodestone 完整 / 剖切 / 内景预览见 PNG；剖切只在显示副本执行，NBT完整。
qa/inventory.json 包含方块与实体载荷。qa/commands.json 记录原生调用；每次 Java -Xmx1536M，在当前 qa 工作目录运行，未运行共享脚本。
制作方自检：原版注册属性、模板读取、所选脆弱装饰支撑、0.6×1.8 离线碰撞路线、空库存、静态风险。
未执行真实世界放置、邻居/随机/液体 tick、动态照明、战斗或生存通关；不等于独立验收。

## 视觉检查
R2内景已查看：新增檐口门框、地纹与橙旗，骨龛及双棺祭台保留。头颅块及skull载荷清单已原生读取，Lodestone远景模型简化，未冒称实体。原五目标通过。
