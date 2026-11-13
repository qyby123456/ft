# zone-M05 水晶矿窟

独立分区房间；不属于 M05 或 corridor-M05。仅交付，不部署。

四件套为 `room.nbt`、`room.json`、`preview.png`、`qa.json`。结构为 Java 1.20.1 DataVersion 3465、gzip、单一调色板，尺寸 **27×15×27**，10935 个显式坐标，含空气；它是房间模板，不是对占用世界执行清空的授权。

北入口脚位 `[13,2,0]`，内部安全脚位 `[13,2,5]`，面向 north。北口五格宽、五格高；主路 `x=11..15,z=0..24,y=2`，四格净空。底座 y=0、地板 y=1，完整屋顶 y=14。四根 2×2 木支架连续落地接顶，四盏灯链固定于顶梁，两盏矿灯置于木箱上。自然岩壁、蓝紫晶簇、矿轨、固定方块破损矿车、工作台和一格深封闭浅水池构成矿窟主题。

参考图为 `D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/mines/M05-mines-crystal-mine-grotto.png`。SHA-256：`288230505f527d38f4045a44a1d57707de14d7b380bd939fdb3d935087413bb6`，已与该批 README、manifest、verify 记录核对并解码；证据在 `qa/reference.json`。未重新生图。

最终 NBT SHA-256：`d796d3e87cba8abdc650bc111f9287f71fa20b0f779839ca3337fcb7ebec9239`。全部交付文件指纹见 `SHA256SUMS.txt`、`delivery-manifest.json`。

验收证据：

- `qa/static.json`：全体积、材料、支撑、屋顶、入口、通路、封闭水池、无实体/物品/禁用方块检查通过；保守仅空气通行扫描为 349 个干燥脚位。
- `views/`：实际 NBT 的四向完整外观、四向内存剖切、四向室内平视，共 12 图和三张合成图。剖切不改 NBT；`qa/views.json` 记录原始截图哈希，`qa/visual-review.json` 记录人工视图复核。
- `qa/native.json`、`qa/forge-gametest.log`：隔离 Forge 1.20.1-47.4.10 实际放置 NONE/CLOCKWISE_90/CLOCKWISE_180/COUNTERCLOCKWISE_90，强制区块 tick、邻居更新和水 tick，200 tick 后 43740 条状态匹配。每个旋转 358 个真实碰撞判定干燥可达脚位、41 格水源、15 处附着紫晶、6 盏支撑完整灯、4 根落地柱和 729 格屋顶。轨道可步行导致真实脚位比保守空气扫描多 9 格。
- 四种旋转均执行占用空气格、移除灯链、阻断通路、移除水池底的负向探针；四份 `qa/native-placed-*.nbt` 均经过原生保存、重新解码和逐格比较，无实体掉落、状态漂移或模板外液体。

蓝晶采用原版淡蓝/青色玻璃、方解石与海晶灯表达；破损矿车采用固定方块道具，模板不存储矿车实体。没有容器、自动奖励、刷怪或命令逻辑。

复现：从已安装 `C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill` 使用 `scripts/run-build.mjs` 执行 `source/recipe.mjs`，输出到本目录、命名 room；运行 `node source/qa-static.mjs`。隔离原生验证运行 `source/run-native.ps1`；它只使用本目录 `native-qa/run-isolated`，不要把 QA 模组部署到玩家实例。预览使用 skill 的 `scripts/preview.mjs --nbt <本目录/room.nbt> --port 5365 --no-open`，再运行 `node source/render-views.mjs`、`node source/make-sheets.mjs`；复核全部视图后运行 `node source/finalize.mjs` 汇总指纹。

本次证据为真实结构的软件渲染及隔离 Forge 原生 GameTest；未进行完整整合包客户端/多人游玩。未部署、未打开玩家存档、未修改核心源码/JAR/既有 M05/corridor-M05 或其它并行房间；接线、首次占用拒绝、持久幂等与正式部署由集成方后续处理。
