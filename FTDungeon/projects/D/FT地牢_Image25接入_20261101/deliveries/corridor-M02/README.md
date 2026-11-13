# M02 · 浸水档案长廊（独立候选，不部署）

## 成品规格

- Minecraft Java 1.20.1 / DataVersion 3465 / gzip NBT / 单一调色板；Minecraft Builder skill（Lodestone 0.6.0）生成。
- 外框 XYZ：17 × 13 × 41，长度 2.5625 区块；全部 9,061 个坐标显式记录，包含空气。NBT 不是清空或施工指令。
- 北入口脚位 `[8,2,0]`，内部安全点 `[8,2,3]`，南出口脚位 `[8,2,40]`；模板朝向 north。接入时两端走道脚位必须对齐到原点 Y+2。
- 主路 X=7..9，Z=0..40，地板 Y=1，三格净空 Y=2..4；无转弯、无分岔、无楼梯，双端开放。
- 暗橡木书架、羊皮卷形去皮桦木、10 处蜡烛龛、5 组铁笼与梁下链条、苔石灰砖墙和完整双层石屋顶。
- 222 格一层深静水，仅位于两侧封闭水槽，不穿主路。将概念图的全面积水适配为同一水平的三格宽干燥中轴，防止主路浸水。
- 用隐藏菌光体与橙玻璃复现熔岩暖光，未放真熔岩或火，避免书库木结构失火。无实体、容器库存、刷怪笼、命令或结构方块。

## 原图与指纹

设计图：`D:\ft工程文件\image2.5\dungeon-corridors-20261103\out\M02-archive-library-flooded.png`。

原图 SHA-256：`96e45b7d9176fdc57b70e99af2dfde21638295e60f263d90b3cdb9099c8d6ccc`。

NBT SHA-256 及完整成品/证据指纹见 `room.json`、`qa.json`、`delivery-manifest.json`。遵循该图同目录 README、manifest、SPEC 与 corridor_index 中 M02 的 archive-library / flooded / lava-glow / hanging-cages 组合；不重新生图。

## QA 与证据边界

- `qa/static.json`：结构体积、调色板、坐标、双端安全、完整屋顶、平直三格主路、流体底/侧封闭、蜡烛与吊笼支撑、主题材料等 14 项静态校验。
- `qa/forge-native.json` 与 `qa/forge-m02-final-02.log`：独立 Forge 47.4.10 GameTestServer 中 0/90/180/270° 四次原生放置；每方向即时和 200 tick 后对比全部 9,061 方块，水源各 222、漏水 0、方块漂移 0、掉落实体 0。
- 随机刻速度 3、火焰刻保持 true；测试北口阻塞、拆除水槽边界、移除吊笼链条、挖开屋顶的负对照，各四次成功识别。
- 四向真实世界回捕 NBT：`qa/native-placed-*.nbt`；`qa/native-roundtrip.nbt` 为原生加载/保存回环证据。
- `preview.png` 与 `views/` 从实际 NBT 用 Lodestone 软件渲染；四向外观保留屋顶，两张侧向内部剖切只影响渲染副本，不修改成品。
- 这些图不是完整整合包实机截图。未做客户端实玩、多人、帧率或长期压力测试，也未验证核心接入、连接路线、占用拒绝和持久化幂等。
- 所有 Forge 测试写入本目录 `qa/forge-runs/`；从未部署模组、修改 FT 核心或正式存档。

## 复现

在本目录运行（不会打开玩家世界）：

```powershell
node C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs .\tools\build.mjs --out-dir . --name room --data-version 3465
node .\tools\static-qa.mjs
.\tools\run-native.ps1 -Run unique-isolated-run-name
```

预览先启动 Builder 自带服务：

```powershell
node C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\preview.mjs --nbt .\room.nbt --port 5312 --no-open
node .\tools\render-views.mjs
```

重跑后使用对应最新日志，不要把旧指纹的证据混入新模板。`tools/finalize.py` 使用本机随 Codex 捆绑的 Pillow 生成总览与证据清单。
