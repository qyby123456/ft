# D · 地狱以物易物商店

独立结构候选，**没有部署、没有分配正式槽位、没有实现交易或奖励**。供统一接入线程 `01a19758-b0e5-75c2-822a-4860527a1e8b` 验收后另行接入；本包不修改 FT 核心、其他房间、正式玩家存档或已部署 JAR。

## 四件套

- `out/room.nbt`：gzip、单调色板、Java 1.20.1 / DataVersion 3465，完整显式 `13×9×13` 体积，共 1521 个记录。
- `out/room.json`：暂定 ID `SHOP_D_NETHER_BARTER`，北入口脚部 `(6,1,0)`，安全内点 `(6,1,3)`，完整屋顶，正式槽位 `null`。
- `out/preview.png`：从本包实际 NBT、原版模型/材质渲染的北向剖视，不是概念图或客户端截图。
- `out/qa.json`：静态、原生放置、视觉自检与可重建性汇总，含结构完整 SHA-256。

结构 SHA-256：`d660b18fa96995ece7c51dd1a000b800480802d2fe9e2e2b52d05f2d12791292`。

## 原图与来源核验

- 原图：`D:\ft工程文件\image2.5\dungeon-shop-variants-20261102\out\shop-variant-D-nether-barter.png`。
- 原图 SHA-256：`adc38f2ab9553f7c40975172b105a362ae8b32655d02ecbf73a1aa6e850c8821`，实际可读取 PNG，尺寸 `1536×1024`。
- 原图、原 README、manifest、DISPATCH 和 D 提示词均只读复制到 `reference/`；没有调用图像生成 API。
- 原目录 README 提及 `STYLES.md`，但执行时实际没有该文件。没有伪造或补写源文件；D 风格依据真实 PNG、README、manifest 和 `prompts/D-nether-barter-shop.txt`。
- 接入规范依据只读文件 `D:\ft工程文件\FT地牢_Image25接入_20261101\CONTRACT.md`。本目录是独立新工程；不修改主工程 `docs/**`，docs impact: none。

## 设计落实与安全替换

主体为磨制黑石砖、下界砖、镶金黑石和嵌墙金带角柱。独立长柜台位于 `X=4…8, Z=7`，其四周至少一格双层净空。北侧三宽三高开口连接三宽三高前厅；柜台后可返回货架、侧桶和笼前，不必翻越家具。

后墙双层货架和金边招牌均紧贴实体墙；金块、铁砧式秤台、空酿造台、花盆、绯红菌与骷髅头均有基座。东南铁笼由完整下界砖底座、铁栏杆和完整顶盖组成，顶链一直连接屋顶。没有猪灵、店主、物品框、掉落物、装饰实体或自动战利品。

为满足可施工与安全通行，对概念图作了公开替换：

1. 原约 11×9 轮廓扩大为 11×11 内部，外廓 13×9×13，给柜台循环通路、货架和隔离笼留空间。
2. 四根柱子嵌入四墙角，不使用占中央或贴穿柜台的独立柱。
3. 裸魂火改成两盏安全灵魂灯笼，坐落于墙托；两盏暖色吊灯使用锁链、黑石梁和完整屋顶逐级支撑。
4. 流动熔岩池改为两个六面封闭岩浆块展示箱。岩浆块位于 `(1,1,5)`、`(11,1,5)`，底/背为完整石墙地板，其余完整橙色玻璃包覆；通路没有暴露伤害面。
5. 地面熔岩缝改为完整菌光体地板，不存在流动熔岩、火焰传播或跌落沟槽。
6. 微型金锭、金币、天平和药瓶艺术比例改成原版金块、铁砧、空酿造台与花盆，不伪造道具或交易；红毯改成内嵌红色带釉陶瓦。

三个木桶和一个酿造台均为空；两颗头骨只有合法方块实体数据。装饰金块可在原版被破坏，这里没有新增防破坏规则、支付或奖励逻辑，玩法由后续核心协调。

## 已完成 QA

- `qa/static.json`：18 项静态检查通过，8 个负例正确拒绝（封口、缺屋顶、缺链、破热源罩、额外岩浆、裸魂火、阻挡柜台、战利品表）。
- `qa/native.json`、`qa/forge-gametest.log`：Java 17.0.8、Minecraft 1.20.1、Forge 47.4.10 独立无界面 GameTestServer **实际运行通过**。使用原生注册表解码/属性验证、方块实体加载、模板 load/save 与 `placeInWorld`。
- 四旋转分别放置并触发邻居更新，200 tick 后逐块对比，共 6084 条状态保持；每向 6 个方块实体、94 个可达脚位、0 个额外世界实体。对原生碰撞、存活支撑、柜台环路和热源罩再次检查。
- 原生负例每向验证缺吊链、打开热源罩和堵路，共 12 个探针。四份放置后回采 NBT 保存在 `qa/native-placed-*.nbt`，供独立复核。
- `preview/four-views.png`：北、南、西、东四向均已实际查看；另查看 `plan.png`、完整屋顶 `exterior.png`、回采 `native-world-capture.png` 和交付总览。记录于 `qa/visual-review.json`。
- `qa/reproducibility.json`：重建两次结构指纹一致，并将未旋转原生回采的所有方块状态与交付 NBT 对比。

这些不是完整整合包客户端游玩、实机截图、多人或 FPS 验证。软件预览不模拟原版光照；头骨实体外观与玻璃透明排序近似。剖切仅作用于渲染内存，最终 NBT 屋顶和所有墙面仍完整。正常图中剖开墙/屋顶后露出的墙托或链端，不表示实际无支撑。

## 重建与复测

```powershell
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
& $python tools/build_room.py
& $python tools/qa_static.py
& $python tools/run_native.py
& $python tools/render_room.py
& $python tools/finalize_delivery.py
```

在本目录运行；Python 需要 Pillow。`run_native.py` 采用本机 Java 17 和缓存 Gradle 8.8，默认离线，只启动本工程 `qa/forge-run` 下隔离测试服，并保留原测试日志；不会开启玩家客户端。缺少缓存时需要配置原版 1.20.1/Forge 47.4.10 依赖，不自动改用其他版本。

渲染器只读 `C:\Users\YOUR_USERNAME\.gradle\caches\forge_gradle\minecraft_repo\versions\1.20.1\client.jar`，不在交付包再分发 Minecraft。`nbtio.py`、初始渲染器、原生启动器与空 GameTest 模板来自已存在的独立补给商店工程；仅在本目录复制件上适配，没有修改原工程。可重建脚本默认校验原图绝对路径；迁移机器请显式更新路径并保持同一原图指纹。

`tools/finalize_delivery.py` 汇总当前证据、生成文件清单、压缩 `release/SHOP_D_NETHER_BARTER-standalone-20261102.zip` 并逐文件回读校验；不会把 `.gradle`、`build`、测试世界或运行 JAR 打入交付包。`qa/package-verification.json` 记录 ZIP 实际指纹。

## 统一接入边界

本轮不写共享 `incoming/`，不登记正式 ID/槽位，不编辑 FT 核心、其他房间、正式 JAR 或玩家世界。暂定 ID 不构成已注册或占位。后续由统一接入线程协调唯一 ID、摆放位置、完整体积与缓冲区占用预检、大厅实际连接、逐房持久标记、幂等、防覆盖/实体检查和正式 JAR 回归；本包未声称这些集成行为已通过。

测试源码中的清空隔离夹具仅用于可丢弃 GameTest 世界，**严禁复制成正式施工流程**。当前回滚只是撤回本未部署候选。未来若接入，先停服、备份旧 JAR、配置、世界和 SavedData，再按核心负责人的方案施工/回滚；卸载 JAR 不会自动拆除已生成建筑，禁止盲删生成标记。
