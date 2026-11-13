# S19 黑曜试炼短廊 · 金纹悬笼

独立结构交付，**未部署、未分配世界槽位、未修改玩家存档或正式模组**。设计编号 S19，避免与既有房间重名的建议接入 ID 为 `CORRIDOR_S19`。

## 交付与接口

- 根目录四件套：`room.nbt`、`room.json`、`preview.png`、`qa.json`。
- Java 1.20.1，DataVersion 3465，gzip 单调色板，15×11×16；长度 16 格，区块边界对齐时水平占地 1×1 区块。
- 北入口脚位 `[7,2,0]`、内部安全点 `[7,2,3]`，朝北；南出口 `[7,2,15]`、南内安全点 `[7,2,12]`。
- 主通路 `x=6..8, y=2..4, z=0..15` 全空气；脚下完整实心地板，3 宽×3 高连续贯通，无转弯、支路或通行楼梯。
- 4 组有梁锚定的悬笼、32 格铁栏杆、4 格铁链、20 格末地烛；黑曜石/哭泣黑曜石、黑石梁网、金纹墙板与两组地面符纹。
- 零流体、零容器、零方块实体、零模板实体、零战利品与命令/结构/拼图方块。

## 参考与转译

参考：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S19-obsidian-trial-intact.png`。

参考 SHA-256：`420458b3957e67b0f5c6b995fdf154363c12656f9027084270574e4c7b556c95`。

规格、索引与提示词来自同批 `SPEC.md`、`out/corridor_index.json`、`prompts/S19-obsidian-trial-intact.txt`。只落实既有参考，没有调用付费生图。

概念图末端闭门转译为相同金纹开放拱门，以满足一字贯通；熔岩裂缝改为菌光体背衬和橙色染色玻璃，无真实岩浆。金纹按整数体素重构，不含自定义纹理。末地烛保留原版白光，不宣称青色彩光。原版哭泣黑曜石自带的客户端粒子未在软件预览复现。

## 验收证据

- `qa/static.json`：2640 个显式唯一体积记录、原图和 NBT 指纹、240 格完整屋顶/基底、连续 3×3 通路、安全点可达、无危险库存或流体，PASS。
- `qa/native.json` 与 `qa/forge-run.log`：Java 17 / Forge 47.4.10 隔离 GameTestServer 真实 `StructureTemplate.placeInWorld`，四向 10560 记录逐格比较、注册表和属性、碰撞通路、吊件支撑、边界和实体检查，PASS。
- 启用火焰 tick、随机 tick=3 和显式邻居更新，**400 tick 后四向再次逐格一致**；每向 152 个可达脚位、20 根有锚点末地烛、4 组有锚点悬笼、240 格完整屋顶、1338 格外部空气边界，无掉落或实体。
- `qa/native-placed-{none,clockwise_90,clockwise_180,counterclockwise_90}.nbt`：稳定性复验后的原生世界快照；`qa/native-roundtrip.nbt`：原生模板往返。
- `preview.png` 为最终 NBT 的室内一点透视；`qa/{north,south,west,east}.png` 与 `qa/four-views.png` 为四向剖视；`qa/exterior.png` 完整外壳、`qa/floorplan.png` 地板图，均为 Lodestone 软件渲染而非 AI 图或游戏截图。
- 剖视仅隐藏朝向墙、顶面和部分顶板以暴露梁锚点，**未裁切最终 room.nbt**；`qa/visual-review.json` 记录人工图像核对。
- 首次测试在 Y=100 的空域预检检测到占用并拒绝施工，日志保留于 `qa/forge-attempt1-occupied-refused.log`。复测选用 Y=240 的全空隔离区域，未清空首次占用位置。

## 重建与复验

使用安装好的 Minecraft Builder skill；依赖已在该技能本地安装，无全局安装或修改技能代码。

```powershell
node 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs' './tools/recipe.mjs' --out-dir . --name room --data-version 3465 --json
node './tools/static-qa.mjs'
& './tools/run-forge.ps1'
node 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs' --nbt './room.nbt' --no-open
node './tools/capture.mjs' 'http://127.0.0.1:实际端口'
node './tools/finalize.mjs'
```

Forge 夹具仅可在隔离 GameTestServer 使用，位于本交付 `forge/` 和 `qa/forge-run/`；不要将其打入正式模组或打开玩家存档。重复运行原生测试会因旧夹具占用拒绝放置，需另选空白隔离测试目录，不得对玩家存档执行清空。

## 接入边界

由唯一统一接入负责人分配唯一槽位和连接路径。本交付不写 shared incoming、登记表、核心源码、JAR、NeoForge、KubeJS、资源包、配置或玩家存档。接入前须验证**完整体积及边界、液体和实体**，占用拒绝，首次持久标记，既有建筑不重建、不覆盖玩家修改；显式 air 不授权清空世界。

本次 PASS 仅覆盖独立结构静态/软件四视图/隔离原生 Forge QA，**不是完整整合包客户端、性能、多人或实际大厅连接验收**。
