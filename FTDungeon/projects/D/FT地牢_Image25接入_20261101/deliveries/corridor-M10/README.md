# corridor-M10 · 焚毁废弃矿道

独立中型直廊已用 Minecraft Builder / Lodestone 制作。模板 15 × 11 × 40，长 2.5 区块，Java 1.20.1 / DataVersion 3465，gzip 单调色板。仅在本交付目录内建造、渲染和原生验证，未部署，未使用或修改玩家存档。

- 北入口脚位 `[7,2,0]`，内部安全点 `[7,2,3]`，南出口 `[7,2,39]`。朝北，沿 Z 轴直通。
- 中央安全通路为 X=6–8、Y=2–4、Z=0–39，三格宽三格高，实心平地，无转弯、分岔或通行台阶。两端实际拱洞宽五格；3×3 接口居中对齐。
- 7 组矿井承重框、3 个吊笼、36 段铁轨、12 盏灯笼、矿脉、焦痕及四处装饰余烬。余烬由菌光体、橙色玻璃和铁盖表现，无真火、熔岩或流体；无实体、容器、库存或战利品。

参考图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M10-abandoned-mine-burnt.png`。

参考 SHA-256：`f0bfc91e258dab253c9d0272f516d54821e82b60357cbc6dcdb1510a722f3ab0`。

最终 NBT SHA-256：`921370b026e16e939b1c48f4aa85aef29e888e98b71691d8a5b48cfd0ae957b5`。

## 证据

- `room.nbt` / `room.json`：模板与完整接口元数据；`preview.png`：从最终 NBT 生成的北入口步行视角。
- `views/interior-{north,south,west,east}.png`：四向内部剖视；`views/exterior-{north,south,west,east}.png`：完整外壳。剖切仅影响渲染，不修改交付模板。
- `views/interior-four-views.png` / `views/exterior-four-views.png`：四视图总览。`views/interior-south-eye.png` 为反向步行视角。
- `qa/static.json`：108 项静态检查通过；6600 个唯一且完整的显式坐标，39 个方块状态，4049 个非空气方块。
- `qa/native.json` / `qa/forge.log`：Minecraft 1.20.1 / Forge 47.4.10 隔离 GameTestServer 实际四向放置；邻块与形状更新后运行 600 tick，前后共 52800 条方块状态比较完全一致，四向通路、支撑、屋顶、边界及实体检查全部通过。每方向 396 个平地可达脚位、4049 个非空气方块支撑检查、2682 个边界空气检查。
- `qa/visual.json` / `views/manifest.json`：参考图对照、最终 NBT 视图指纹和视觉检查结论；`qa.json` 汇总全部验收层级。
- `corridor-M10-handoff.zip` / `delivery-manifest.json` / `package-receipt.json`：只含交付件、证据和复现源码，不含隔离测试世界、Gradle 缓存或构建 JAR。

预览为实际 NBT 的 Lodestone 软件渲染，未冒充游戏客户端截图。原生测试运行隔离服务器，尚未做完整整合包客户端或多人游玩验证。

## 复现

在 `C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill` 运行：

```powershell
node scripts/run-build.mjs 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-M10/tools/recipe.mjs' --out-dir 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-M10' --name room --data-version 3465 --json
node 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-M10/tools/static-qa.mjs'
& 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-M10/tools/run-forge.ps1'
node scripts/preview.mjs --nbt 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-M10/room.nbt' --host 127.0.0.1 --port 41770 --no-open
```

预览服务运行后，另一个终端执行 `tools/render.mjs`；该脚本使用已安装的 Edge 无头渲染与工作区 Playwright。图片经视觉复核后执行 `tools/finalize.py` 重建证据清单和交付包。

## 接入边界

独立 ID 固定为 `corridor-M10`，避免与旧房间 M10 混淆。唯一槽位由统一接入负责人分配。正式接入前必须检查完整 15 × 11 × 40 体积（含空气记录）、占用、世界边界、流体与实体；占用应拒绝。不得直接使用含显式空气的结构覆盖已有建筑。北、南接口均需连接到原有路线，独立交付通过不等于已接入或已部署。
