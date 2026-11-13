# H02 废墟宴会厅

独立交付，尚未部署或接入正式地牢。制作与验收只写入本目录；未写入正式模组、玩家存档或其他房间。

## 四件套

- [room.nbt](room.nbt)：Minecraft Java 1.20.1 gzip 单调色板模板，29 × 16 × 33，DataVersion 3465。
- [room.json](room.json)：房间 ID、参考图、入口、安全点、尺寸与最终指纹。
- [preview.png](preview.png)：从最终 NBT 渲染的北向内部剖视。
- [qa.json](qa.json)：交付检查、真实结果、证据索引与限制。

最终 NBT SHA-256：`bb3d0dc820024d33c57c56c9e8fdad19ea10939fa291aef6621456fa99d91138`。

参考图：`D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/halls/H02-halls-banquet-hall-ruined.png`。

参考图 SHA-256：`d11b8f4c4fd01d0058b779811853271a011e6da302ed27e5ca049cb162f60896`。已核对批次 README、manifest 和 verify，未重新生图。

## 坐标与建筑

模板原点相对 XYZ；入口 `[14, 1, 0]`、安全点 `[14, 1, 5]` 为玩家脚部坐标。北门宽三格、高四格，门下为完整方块地板。入口与环形主路宽三格、高三格，216 个主路脚位可双向通行。环路沿宴桌东西两侧及南侧绕行，家具和残骸位于主路外。

中央宴桌有六条完整落地桌腿、十把高背椅、白色餐盘、红色桌旗及烛台。六根带独立柱基的石柱承接木梁和完整双层顶盖。中央环形吊灯有四条屋顶吊链；边侧吊灯同样接入屋顶。墙边保留红金壁挂、书架、柜台、蓝色窗和落地石木残片。

模板含 15,312 条显式记录，6,392 个非空气方块，40 种方块状态。没有实体、容器、流体、刷怪笼、命令方块或其他危险方块。真实屋顶完整，剖切仅用于生成预览。

## 验收证据

- [静态检查](qa/static.json)：入口、地板、顶盖、主路、家具支撑、六根柱和全部非空气构件的落地连通性通过。
- [内部四视图](qa/four-interior-views.png)、[外部四视图](qa/four-exterior-views.png)、[布局图](qa/floor-plan.png)、[桌椅支撑细节](qa/furniture-supported.png) 与 [视图清单](qa/render-manifest.json)。
- [视觉复核](qa/visual-review.json)：记录逐项复核与初版椅背半格间隙的修正。旧版本与旧报告仅保留于 `qa/previous-chair-back-gap/`，不作为最终结果。
- [Forge 原生检查](qa/native.json)：Minecraft 1.20.1 / Forge 47.4.10 隔离 GameTest，四个旋转各放置 15,312 条记录；立即及 320 tick 后逐条校验，方块变化为零、掉落实体为零。主路、完整地板与顶盖、canSurvive、椅背 AABB 接触面、链条锚点与外围 4,218 格边界均通过。
- [回读对比](qa/capture-comparison.json)：独立 Python 解码 Forge 原生往返及四个稳定后世界快照；逐条比较全部空气和实方块坐标、朝向与轴向，共 61,248 条，差异为零。
- 最终原生日志：`qa/forge-20261103-142151-546.log`，含 `H02_NATIVE_PASS` 及 `BUILD SUCCESSFUL`。
- [SHA256SUMS.txt](SHA256SUMS.txt) 与 [原图核验](qa/reference-verification.json) 提供交付指纹证据。

软件视图使用 Java 1.20.1 实际模型和纹理，不模拟动态光照，不是客户端游戏截图。Forge 验证使用一次性隔离测试世界，不等同于整合包客户端、多人性能或正式地牢连接路线验收。

## 重现

使用已安装的 Minecraft Builder：

```powershell
node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs ./tools/recipe.mjs --out-dir . --name room --data-version 3465 --json
C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe ./tools/static-qa.py
./tools/run-forge.ps1
C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe ./tools/compare-captures.py
C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe ./tools/render-views.py
C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe ./tools/render-details.py
C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe ./tools/finalize.py
```

在本房间目录执行。生成器由 Minecraft Builder/Lodestone 输出 NBT；随附 NBT 读取器与渲染器用于独立 QA。修改布局后需要重新视觉复核并更新 `qa/visual-review.json` 的源指纹；封装器拒绝混用旧报告。

本次已启动 [Lodestone 实际 NBT 预览](http://127.0.0.1:5176/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=room.nbt)，该本机地址只在预览进程存活时可用。也可使用技能的 `scripts/preview.mjs --nbt ./room.nbt` 重开。

后续正式接入须先验证完整体积占用，再放置模板；显式空气记录不能当作对玩家区域的清空授权。本次不执行部署或世界施工。
