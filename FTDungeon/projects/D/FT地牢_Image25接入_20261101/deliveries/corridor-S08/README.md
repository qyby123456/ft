# corridor-S08 · 冰霜石拱短直廊

根据 `D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S08-ice-frost-intact.png` 制作；原图 SHA-256 为 `5d95e20ec07b165939bf147ee8368de8fe12ec1d66b0ace897f3d904dde06758`。

## 规格与边界

- Minecraft Java 1.20.1，DataVersion 3465；11×10×16，一区块长，Z 轴直行，无支路、转弯或楼梯。
- 北入口脚位 `[5,2,0]`，内部脚位 `[5,2,7]`，南出口脚位 `[5,2,15]`；地板 Y=1，两端保留三宽四高开口，主路全长至少三宽三高。
- 蓝冰与浮冰墙地、四组完整灰石拱、八盏暖色壁火把、白色积雪和挂接冰棱。图中的尽端关闭门改为开放拱口，保证“一字贯通”。
- 火把邻近位置不使用可融化的普通冰或雪层；雪块与石英台沿表现白雪，浅蓝玻璃片表现有顶支撑冰棱。冰面仍保留原版滑动特性。
- 最终 NBT 保留完整屋顶与外壳；剖切仅用于预览，不改写结构。模板明确记录全部空气格，导入前必须检查整个体积，不能无条件覆盖已有建筑。
- 无实体、方块实体、容器库存、战利品、刷怪笼、命令方块或逻辑。只在本独立交付目录写文件，不部署、不修改正式存档或其它走道。

## 重建

从已安装 Minecraft Builder 技能目录运行：

```powershell
node scripts/run-build.mjs 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S08/recipe.mjs' --out-dir 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S08' --name room --data-version 3465
```

随后运行 `tools/qa_static.py`、`tools/render.mjs`、`tools/run_forge.ps1`；使用 `tools/finalize.py --boards-only` 输出四向总览，完成图片复核并更新 `qa/visual.json` 后运行 `tools/finalize.py` 汇总。所有 QA 结论必须对应最终 `room.nbt` 的完整 SHA-256，详见 `qa.json`；运行中的报告不代表通过。

## 验收证据

- `preview.png`：最终 NBT 的 Lodestone 正中透视软件渲染，不是概念图或游戏截图。
- `views/four-interior.png`、`views/four-exterior.png`：北、南、西、东内部剖视及不剖切完整外壳。侧向剖视保留立柱及火把支撑，避免移除护墙后造成虚假的悬空视觉；不改写原 NBT。
- `qa/static.json`：NBT 数据版本、单调色板、完整体积、地基/屋顶、连续三宽净空、入口/内部点/出口、火把与冰棱支撑、零实体/库存/危险方块。
- `qa/forge.json`、`qa/forge-run.log`：真实隔离 Forge 47.4.10 GameTest，0°/90°/180°/270°四向放置，120 tick 后逐条核对 7040 条记录，四份世界结构回读一致；每向均包含拆支撑和堵路的反向探针。
- `qa/captures/*.nbt`：四种旋转的真实世界结构回读，不是以源文件复制冒充放置证据。
- `qa/forge-run-*.log` 保留首次测试夹具资源元数据缺失导致的失败；补齐独立 QA 资源包元数据后重跑通过，未改动成品结构。

独立制作自验通过后仍需统一交接线程分配槽位并复验。这里不宣称已接入 FT 目录、整合包客户端/多人游玩或 FPS 已验收。
