# S8 水晶紫晶：功能房间第二轮 11—20

本组独占目录，所有旧组及存档只读。逐张看原图、建模、真实 Lodestone 渲染及静态原版 QA。自检不是独立验收；不部署、不新增付费图片、不虚构战斗功能。

当前：11—20十间及本轮新合并候选已完成，等待独立复核；不是独立验收通过。准确路径与SHA256见 `ready-for-review.json`。各房间目录含独立NBT、recipe.mjs、inventory.json、SELF-QA.json、完整/剖切/室内真实预览及原版静态报告。20号为密道藏宝室，已制作且参与合并。

复现：技能目录运行 `node scripts/run-build.mjs <本组/S8-11/recipe.mjs> --out-dir <本组/S8-11>`；本组运行 `node qa-room.mjs S8-11`、`powershell -File run-native.ps1 S8-11`、`node render.cjs S8-11`。DataVersion3465。

真实 WebGL 渲染保存 complete/cutaway/interior.png。剖切只切显示副本。原版检查复用只读检查类，单进程1536MB堆；方法限制见各报告。未执行游戏放置、流体/随机tick、玩家实走或独立验收。

合并结构为240×29×125，使用全新双排十房与中央紫晶玻璃长廊布局。房间整体平移、不旋转，50,204个原房记录状态及方块实体载荷完全保留。原脚位Y2变为合并Y4，瞭望台后门Y6变为Y8，半格适配廊接回主通道。38个原指定目标在合并中全部可达；外包络屏障79,602格逐格检查，不截断内部连接。

复现合并：本组执行 `node assembly/build.mjs` → `node qa-room.mjs assembly` → `node assembly/verify.mjs` → `powershell -File run-native.ps1 assembly` → `node render.cjs assembly` → `node finalize.mjs`。verify同时生成15个各轴不超过48格的分片，并将158,946格方块状态及方块实体载荷反拼逐格对照。源NBT及parts均为DataVersion3465、gzip Java原版结构格式。assembly/recipe.mjs是现有合并NBT的技能导入适配器；完整可复现生成源是assembly/build.mjs与十间源配方。

首轮视觉初审REQUEST_CHANGES已按反馈整改：去掉连续白腰线与规则棋盘、加局部矿脉与厚门拱檐口；15训练地改灰紫斑块、18增大中央晶簇、19降低透明围护、20加入凹入式紫晶壁龛。逐房原版本在qa/visual-r0；20首次登台失败的NBT与原报告在qa/history-r0。所有原路线目标和家具保留，不以移动目标代替修复。整改后重新检查和渲染，最终视觉决定仍归独立复核方。

限制：没有真实世界自然刻、流体刻、门/箱交互、照明传播、生存/战斗或玩家实走证据。军械库盔甲为方块雕塑；工具、钥匙、望远镜、吊桶为装饰替代物，空容器不含虚构战利品。蓄水池与水道静态封闭。合并为外围屏障封闭的复核产物，尚无对外部署入口，不写任何saves/mods。水井维护台与图书馆梯子的攀爬物理另需世界复核。
