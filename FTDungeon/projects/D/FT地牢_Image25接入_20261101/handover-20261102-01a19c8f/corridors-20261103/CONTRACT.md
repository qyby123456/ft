# 2026-11-03 走道独立收件与复验约束

## ID、槽位和边界

- 本批唯一标识为 `corridor-M01` 至 `corridor-M20`、`corridor-S01` 至 `corridor-S20`，一条走道对应 `thread-map.json` 中一个专属线程。
- 沿用上一轮 41 条本地记录的槽位 0–40，新批只在本接收账本预留 41–80；中型 41–60，短型 61–80。
- `corridor-M07` 不等于旧房间 `M07` 或 `M7`，模板候选使用独立路径 `ftdungeonmobs:image25/corridor_m07`，不得去前缀或去前导零合并。
- 这是本地接入排队号，不代表核心已支持 81 间、已导入走道、已打包或已施工；不改旧槽位、旧接收账本和制作线程交付。
- 不生图、不重复分派、不部署、不改正式源码、Mod JAR、游戏配置或玩家存档，不向其他聊天发送消息。

## 输入与收件

原图批次：`D:/ft工程文件/image2.5/dungeon-corridors-20261103`。必须交叉核对 README、SPEC、manifest、index，40 张原图逐张完整解码、尺寸 1536×1024、字节数和完整 SHA-256 一致。两张总览单独记录，不能增加 ID 或槽位。

成品目录固定为 `D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-<ID>`，最低四件套为 `room.nbt`、`room.json`、`preview.png`、`qa.json`。不替制作线程创建空交付，不把原图当成品或把未完成文件标为验收通过。

走道登记 ID 永远带前缀。制作 metadata 的局部短 ID 仅在专属目录、唯一原图及哈希同时匹配时记录为 `producer_metadata_id`，不能成为全局别名。

稳定读取后冻结到 `incoming/<canonical-ID>/<NBT-SHA>/<bundle-SHA>/`；后续同一 NBT 的 JSON/QA/预览更新会产生新的 bundle 指纹，不能继承旧验收。只收件不覆盖原件。

## 验收分层

1. 接收静态筛查：gzip 单调色板 1.20.1 NBT、范围、重复坐标、metadata 哈希与参考图、原版方块、无实体/刷怪笼/执行方块/容器奖励。M 长轴为 32–48，S 为 16；北南入口和三格宽三格高的直通主路同高、有安全地板和顶盖。静态筛查不能证明全部方块状态、支撑和液体稳定性。
2. 制作线程的 QA、四向图、原生报告只作为来源证据，不自动升级为本接收者验收。
3. 接收者四视图必须来自冻结的最终 NBT，北南西东各一张内部剖视和一张完整外壳；逐图审阅，不能用设计图、总览或只看 preview 代替。
4. 接收者原生复验必须在新建隔离目录/临时世界复制夹具后实际运行：四个旋转放置，至少 240 tick，验证两端可通行、完整通路、液体封闭、支撑/悬挂/光源/方块更新及结构稳定，无正式存档访问。缺少夹具或报告则保持待验，不伪造 PASS。
5. 最终接收结论固定在 `reviews/<ID>/<NBT-SHA>/<bundle-SHA>/acceptance.json`，绑定 ID、NBT/参考图/bundle 哈希、审阅者、八张图及原生报告/日志/测试结果文件哈希。只有全部通过且没有阻塞，才标记 `ready_for_unified_integration=true`。

`ready_for_unified_integration` 仅表示可交核心负责人做连接接入；大厅路线、两端连接、整卷占用拒绝、幂等/持久指纹、旧世界不覆盖及相关依赖仍需集成回归。本批没有部署授权。

## 入口

- `../tools/intake_corridors.py`：重扫已存在交付并创建不可覆盖的带时间戳快照，不监听、不自行发消息。
- `slots.json`：冻结的旧账本指纹与 40 个唯一槽位；`latest.json`：本批最近盘点。
- `runs/<时间戳>/REPORT.md`：实际数量及逐项状态；`registry.json`：接收证据；`combined-reservations.json`：旧 41 + 新 40 的槽位去重结果。
- `--verify <registry.json>` 校验账本和冻结证据；`--self-test <registry.json>` 用内存负例验证 ID、槽位、线程、哈希及越级接入防线。

本目录的软件 NBT 视图不是游戏截图；Forge 无界面复验也不是整合包客户端或多人实机验收。
