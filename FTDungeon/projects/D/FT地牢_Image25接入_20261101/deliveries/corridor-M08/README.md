# M08 · 焚烬熔炉直廊（中型独立交付）

## 建筑

- 模板 `room.nbt`，Minecraft Java 1.20.1 / DataVersion 3465，gzip、单一调色板。
- 尺寸 **17 × 13 × 41**（XYZ），轴线沿 Z；长度 41 格，约 2.56 区块。
- 北端脚位 `[8,3,0]`，内部安全脚位 `[8,3,3]`，南端脚位 `[8,3,40]`；两端接口宽 5 格、净高至少 5 格。
- 中央 `x=6..10, y=3, z=0..40` 为 5 格宽平直净路，净高至少 3 格，地板标高始终 y=2；无分岔、转弯、可行走台阶或关闭门。
- 黑石 / 开裂黑石砖与黑色混凝土表现焚毁烟熏；8 处玻璃密封熔炉窗、8 座铁砧、8 座空吊铁笼、两条玻璃盖板熔岩细沟及氧化铜横梁。煤炭块在原生验证中判为可燃，已替换而不放宽检查。
- 完整双层屋顶与基础；只用 1.20.1 原版方块，无实体、刷怪笼、战利品、命令方块、可燃结构或开放火焰。

## 原图追溯

参考图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M08-forge-lava-burnt.png`。

SHA-256：`ea87fc89e452e478a91bca5ea505acea8b97d2da8bd468a07108db61f7e46050`。

保留原图的黑石锻造主题、焚毁状态、橙色熔岩辉光、铁砧、成对吊笼和远端拱门。原图近远尺度不是游戏方块测量值，本交付按批次中型 32–48 格要求落地；概念图的台阶感门槛改为同高双向接口，地面熔岩裂缝和炉口全部加玻璃隔离。

本任务不重新生图，不调用付费 API，不修改连接配置。

## 预览与验证

- `preview.png`：从完整最终 NBT 渲染的北端轴线视角。
- `preview/index.html`：两向轴线、北东南西内部剖切、北东南西完整外壳；图片来自实际 NBT，不使用概念图代替成品。
- `preview/four-view-interior.png` / `preview/four-view-exterior.png`：四向拼图。
- `qa/static.json`：完整体积、静态直路、屋顶、基础和无侧分岔检查。
- `qa/render.json`：源 NBT 指纹、相机、图片指纹；渲染为 Lodestone 0.6.0 / ThreeStructureRenderer / Edge headless SwiftShader。
- `qa/native.json`：独立 Forge 47.4.10 / Minecraft 1.20.1 GameTest，真实 `StructureTemplate.placeInWorld` 的四旋转加载、方块状态、支撑、熔岩封闭、边界无泄漏、双向通路和 600 tick 稳定性。
- `qa.json`：最终状态、证据文件与验证限制；`checksums.json` 固定主要交付物指纹。

软件预览与隔离 Forge 原生放置，不等于完整整合包客户端 / 多人游玩验收。是否通过以证据文件中的真实结果为准。

## 安全接入约束

**未部署、未修改地牢核心、未打开或写入任何玩家存档。** 生成器与验证器仅写本 `corridor-M08` 子目录；`tools/verification-resources` 只用于测试，不能打入正式模组。

模板含整个体积的显式空气。后续接入前必须对**完整 17×13×41 体积**执行占用、液体、实体和边界检查；不得把显式空气作为无条件清空命令。已生成的房间必须有独立持久化标记，重复入场不覆盖玩家修改。

## 重现

使用已安装的 `C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill` 及原有依赖：

```powershell
node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs ./tools/build.mjs --out-dir . --name room --data-version 3465 --json
node ./tools/finalize.mjs
./tools/run_forge.ps1
node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs --nbt ./room.nbt --port 5188 --no-open
```

预览服务运行后，在另一终端本目录执行 `node ./tools/capture.mjs`，最后 `node ./tools/package.mjs` 重新汇总。预览端口变动可用进程环境变量 `M08_PREVIEW_URL` 指向对应本机 URL。Forge 每次创建独立虚空平坦 QA 世界，测试在高空 y=300 放置四份模板，不连接玩家世界。
