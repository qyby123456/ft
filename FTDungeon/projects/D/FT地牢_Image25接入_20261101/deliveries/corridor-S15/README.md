# S15 陈旧监牢短廊 · 独立交付

四件套：`room.nbt`、`room.json`、`preview.png`、`qa.json`。使用 Minecraft Builder / Lodestone 生成与预览，Minecraft Java 1.20.1，DataVersion 3465，15×10×16，Z 轴16格短直廊。最终哈希见 `room.json`、`qa.json`、`handoff.json` 与 `SHA256SUMS.txt`。

## 入口与通路

- 北入口 `(7,1,0)`，南出口 `(7,1,15)`；两端常开三宽三高，主路 `X=6..8,Y=1..3,Z=0..15`，地面 `Y=0`。
- 安全点 `(7,1,2)`、`(7,1,13)`，均有三宽三深三高净空与完整平地。
- 不转弯、不分岔、不设楼梯；主路没有蛛网、流体、碎石或低垂碰撞。统一接入时延伸两端三格宽的外接地板，不可堵南口。

## 设计对应与适配

参考 `D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S15-prison-cells-aged.png`，SHA-256 `f0c77a9be5e1d6db19c5874c431de002f2f17a21232f83a879bd774b2c6be09b`；批次 `README.md`、`SPEC.md`、`manifest.json` 与 `out/corridor_index.json` 均在参考目录。

保留陈旧裂石/苔石、两侧各三间牢房、深木横梁、四盏链悬魂灯、两只四链吊笼及封闭牢内上角蛛网。原版铁栏是灰铁，未伪称能原样呈现概念图锈色。概念图尽头封门改为常开南出口。牢房为封闭装饰空间，不计作走道分支；铁门无机关、箱子或囚犯。床铺为落地木台与毯，不使用FT维度有爆炸风险的床。吊笼底板从 `Y=4` 起，顶盖经四链各自连到完整顶层屋顶，三高主路不受压缩。

## 验证与证据

- `qa/static.json`：2400格完整显式NBT记录、尺寸/边界/调色板、参考哈希、144格主路净空、48格贯通地面、240格完整顶层及底板、两处安全点、梁柱链笼连接、零容器/库存/战利品/实体/液体、NBT回读与固体连接图。
- `qa/native.json`、`qa/forge-gametest.log`：隔离 Forge 47.4.10 GameTest；四旋转原生 `StructureTemplate.placeInWorld`，注册表与全部状态、`canSurvive`、邻面派生形状、真实碰撞和坚实地板检查；强制区块持续加载并稳定200tick后比对9600格，四向世界回采NBT再加载均匹配；断灯链/笼链/屋顶锚点及堵路共16项负例通过。
- `preview/north.png`、`south.png`、`west.png`、`east.png`、`four-views.png` 为最终NBT四向内部剖视；`exterior.png`、`exterior-south.png` 为完整外壳；`plan.png` 为通路平面；`native-world-capture.png` 来自Forge稳定后回采NBT。
- `preview/lodestone-interior.png` 与根目录 `preview.png` 来自完整最终NBT的Lodestone内部一点透视；`qa/lodestone.json` 记录模型、相机与浏览器验证，`qa/four-view-review.json` 记录对图复核。

所有预览均为实际NBT的软件渲染，不是AI概念图，也不是Minecraft客户端截图。剖视只改渲染内存，不改交付屋顶；支持锚点保留用于核查。Lodestone软件灯光不等于游戏内准确光照，四视图纹理渲染也不模拟整合包着色器。隔离Forge测试不代表完整整合包客户端、多人生存或FPS验收。

## 复现

在 `C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill` 运行：

```powershell
node scripts/run-build.mjs "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S15/recipe.mjs" --out-dir "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S15" --name room --json
node "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S15/tools/prepare.mjs"
```

随后在本交付目录，使用已配置的Python（需Pillow）依次运行 `tools/static_qa.py`、`tools/run_native.py`、`tools/render_room.py`。原生测试使用已有Java 17与Gradle 8.8，仅写本目录 `qa/forge-run`；严禁把测试模组部署到正式实例。

在技能目录执行 `node scripts/preview.mjs --nbt "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S15/room.nbt" --port 53315 --no-open --lighting night`；然后运行本目录 `node tools/capture_lodestone.cjs http://127.0.0.1:53315` 和 `tools/finalize_delivery.py`。截图脚本使用本机Edge无头模式与已配置Playwright；会核对服务端NBT字节一致。

## 并行与接入边界

本任务仅写 `deliveries/corridor-S15`。没有部署、分配正式槽位、修改共享登记、正式源码/JAR/配置/资源包或打开玩家存档。`CORRIDOR_S15` 是交付命名，不覆盖历史房间 `S1/S5/S15`；最终唯一ID与槽位交统一接入线程确认。`handoff.json` 为机器可读交接摘要，独立隔离测试世界不是玩家世界。
