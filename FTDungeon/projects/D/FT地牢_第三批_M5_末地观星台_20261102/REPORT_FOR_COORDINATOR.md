# M5 接入交接报告

目标聊天：检查并接入 FT 地牢设计（`01a19758-b0e5-75c2-822a-4860527a1e8b`）。

## 交付结论

M5「末地观星台」独立制作与 QA 完成。未写共享 incoming、FT 核心、已部署 JAR 或玩家存档；未部署。可由接入负责人复核并领取下列四件交付：

`D:\ft工程文件\FT地牢_第三批_M5_末地观星台_20261102\out\M5\`

- `room.nbt`：6181 字节；SHA-256 `37689df85de484a955fe4be126a03f25b4d9f13646f7b7a65707268ab6370717`。
- `room.json`：15×10×15，north，入口脚位 `[7,1,0]`，安全内点 `[7,1,2]`，模板建议 ID `ftdungeonmobs:image25/m5`。
- `preview.png`：实际 NBT、原生方块轮廓与原版纹理软件剖切预览，不是设计图或客户端截图。
- `qa.json`：合并真实静态、原生注册表、Forge 放置与夹具回滚证据。

原图：`D:\ft工程文件\image2.5\dungeon-design-batch3-20261101\out\M5-end-stargazer-terrace-hero.png`。

原图 SHA-256：`dcd0ffbe2727cc55608994a09e224cac2a42d78bd1b60aca13d399d6cf858cae`，已核实与 README/manifest/ROOM_STYLES_BATCH3 一致。未重新付费生图。

## 保留与改编

保留末地石/紫珀拼花、中央望远镜与观星台、四组末地烛、黑曜石水晶柱、紫颂植物和南侧破损装饰门。按当前非花园完整屋顶契约增加玻璃星顶，概念 h8 改为真实 h10，占地仍15×15。全部地板连续，无虚空陷坑、真末地水晶、活动传送门、库存、流体或实体。

## 验收证据

- `qa/static-qa.json`：24/24 PASS；2250 条显式记录、1019 非空气方块、25 状态、128 个平地可达脚位；3×3 北开口、完整地板/屋顶、结构与装饰连到底板、五条干燥往返路线通过。
- `qa/native-registry-qa.json`：Minecraft 1.20.1 原生方块注册表及全部属性核对 PASS，实际 `StructureTemplate` 加载/保存记录数一致。
- `qa/native-world-qa.json`：Forge 47.4.10 真正隔离 GameTest，0°/90°/180°/270° 均放置成功；每次快照核对9000条方块状态，逐非空气块检查 `canSurvive` 与末地烛挂点，按0.6×1.8玩家体积检验干燥通路。80 tick 后重新核对通过，四套夹具回滚为空气并复核 PASS。
- `evidence/forge-gametest-attempt2.log`：1/1 required tests passed、`M5_NATIVE_QA_PASS`、`BUILD SUCCESSFUL`、退出码0。首轮只是夹具相对高度落入地下导致占用保护拒绝，未施工；原失败证据保留，第二轮将夹具固定Y200后通过，NBT未改。
- `evidence/preview-full-envelope.png`：未剖切的完整玻璃屋顶外观；`evidence/plan-dry-routes.png`：首层路线。
- `M5-independent-delivery.zip` 为完整交接包；完整文件及压缩包哈希见 `delivery-manifest.json` 与 `package-receipt.json`。

## 边界与剩余接入工作

已有九间的36份基线交付文件均未改变。基线中的正式0.18.0 JAR在测试期间已不在原路径，只读查看当前为0.19.0；本任务没有向 mods/备份目录写入任何内容，也不恢复、回退或覆盖并行核心负责人的发布。详见 `qa/read-only-boundary-review.json`。

未做 FT 目录注册、固定槽位、实际大厅支路、FT 逐房标记/幂等、整合包客户端或多人游玩测试，不能把本报告算作这些项目通过。请在统一接入时保留既有生成标记和玩家改建，并由核心负责人完成整合 QA 后决定是否部署。

回滚证据只覆盖本工程已确认全空的隔离夹具；正式世界若将来施工，须保存全体积前像，不得把模板中的显式空气当作无条件清空授权。当前生产未改，撤回仅需不领取此M5候选。
