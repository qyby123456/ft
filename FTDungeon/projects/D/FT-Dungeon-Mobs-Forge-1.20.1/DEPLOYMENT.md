# 本机部署记录 · FT地牢怪物

## 当前版本：0.20.0（房间分级与冒险保护，2026-11-02）

- 11:55:56 安装到 `C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.20.0.jar`，同模组 ID 活动包唯一；未启动客户端、未打开玩家世界。
- 新包 654463 字节，SHA-256 `9bee4bb411ef404ef34163375115e5b5f5dc21512019e9e398d76c0c5914920f`；安装包、构建包与固定交付副本一致。
- 0.19.0 与原有 FT 配置已备份到实例内 `ft-mod-backups/ftdungeonmobs/room-rules-20261102T035554Z/`；旧包 SHA-256 `679cae94edb0ac63d5e572a2bfcdc44d56f165b1603410705ad93f0acd79db95`。原配置未覆盖，其余 385 个模组文件元数据未变，挑战扩展仍禁用，未动 NeoForge 或远端服务器。
- 固定交付副本 `output/room-tiers-adventure-20261102/release/ft-dungeon-mobs-forge-1.20.1-0.20.0.jar`；发布/部署审核分别为同级上层目录的 `release-verification.json`、`deployment.json`。源码修改前备份在 `source-before/`。
- 两端完整重启后生效。首次启动自动生成/补齐 `config/ftdungeonmobs-spawns.toml` 的普通/精英分池选项，不需要重建房间；旧轮仅迁移未生成且不合等级的剩余名额，不重发已消耗怪物。
- 玩家无论通过入口或其他方式进入 FT 地牢均转冒险，交互/攻击怪物保留，离开恢复原模式。完整名单、配置、安全边界见 `ROOM_RULES.md`；GameTest 与资源回归见 `VERIFICATION.md`，完整整合包真人验收尚未进行。

## 历史版本：0.19.0（房间靠近刷怪点，2026-11-02）

- 已安装到当前 `FarmingTales_ForgeⅡ v1.5.2/mods/FT地牢怪物-Forge-1.20.1-0.19.0.jar`，同模组 ID 活动包唯一；原 0.18.0 已移至实例内 `ft-mod-backups/ftdungeonmobs/spawn-points-20261102T021507Z/`，原有 FT 配置一并备份。
- 新包 644057 字节，SHA-256 `679cae94edb0ac63d5e572a2bfcdc44d56f165b1603410705ad93f0acd79db95`；备份旧包 SHA-256 `89c40826b66ca76bc08e5254e334e57e60053fcf1b6d25a7d44fcb91fe8a5758`。安装包和固定交付副本哈希一致，旧包备份哈希一致，pending/部署锁已清理。
- 固定交付副本：`output/spawn-points-20261102/release/ft-dungeon-mobs-forge-1.20.1-0.19.0.jar`；部署与资源对比报告：同目录上级的 `deployment.json`、`release-verification.json`。
- 已有 FT 配置哈希不变，其余 385 个模组文件名称、长度和修改时间不变；禁用的挑战扩展保持禁用。未启动当前整合包、未打开玩家存档、未修改远端服务器或 NeoForge 实例。
- 下次完整启动会自动生成 `config/ftdungeonmobs-spawns.toml`。从入口正常开始一轮，再以生存/冒险模式接近房间；创造/旁观不会触发。管理员可用 `/ftdungeon spawns status` 查看；单人联机和服务器双方都需更新。
- 原生 Forge GameTest 4/4、真实房间覆盖 11/11 和完整 `build` 通过；尚未在完整整合包客户端中进行人工游玩与美术观感验收，详见 `VERIFICATION.md`、`SPAWN_POINTS.md`。

## 历史版本：0.18.0（24 款地牢建筑方块）

- 部署完成：2026-11-01 23:44，Asia/Shanghai；23:44:52 独立复核 `POST_DEPLOYMENT_PASS`。检查目标无 Minecraft 运行后部署，没有停止进程、启动客户端/服务器或打开玩家世界；其它服务器实例和远端服未动。两端完整重启且同版后使用。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.18.0.jar`，615710 字节；构建来源 `build/libs/ft-dungeon-mobs-forge-1.20.1-0.18.0.jar`。
- 构建/安装 SHA-256：`89c40826b66ca76bc08e5254e334e57e60053fcf1b6d25a7d44fcb91fe8a5758`。
- 旧 0.17.0 备份：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\ft-mod-backups\ftdungeonmobs\20261101-234427-dungeon-architecture-0.18.0\FT地牢怪物-Forge-1.20.1-0.17.0.jar`；SHA-256 `a806046dadc63f2b264313a94b490deb0f01fec5126c4a58e17decae04d4b009`。同目录有旧配置、385 个其它模组哈希与部署收据。
- 新增 24 款可合成建筑件与固定光源，创造栏“FT地牢怪物”和原版“建筑方块”均可获取。95 个自定义体块/500 面，零新增方块实体/ticker；三路并行制作记录、24 份 bbmodel、模型总览与四向预览见 `output/dungeon-architecture-20261101-231800/`，完整 ID/配方/拼装见 `DUNGEON_ARCHITECTURE.md`。
- 活动同 ID 包恰好一份；385 个其它模组名称/大小/哈希及配置保持不变，FTBoss 未改，FT 地牢挑战仍禁用。配置 SHA-256 仍为 `46da1c106667fa356ecfb3b4e05eae69653d77302299a0003622e60a80a82515`；无 pending/锁残留。
- 原 11 份房间与原有骷髅、门、火盆、血池美术和规则不变；不自动替换世界内方块。沿用既有返程门占用 P1，不称为本次已修复；未做玩家世界实机、多人或 FPS 验收。

### 回退 0.18.0

先关闭游戏/服务器并备份完整世界，只保留一份同 ID 包。0.17.0 不识别新增 24 种方块和物品；若世界、容器或背包已使用新套件，仅换旧 JAR 会缺失注册并可能丢失内容，应同时恢复升级前世界备份。本次没有替用户清空方块、容器或回退世界。

## 历史：0.17.0（十一间地牢建筑与安全通路）

- 部署完成：2026-11-01 22:48:02 +08；22:49:16 独立复核 `POST_DEPLOYMENT_PASS`。只安装到下述 FarmingTales 实例，确认该目标没有运行；另一个名为「服务器」的实例与远端服未修改，没有强制停止任何进程。
- 当前 JAR：`C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/FT地牢怪物-Forge-1.20.1-0.17.0.jar`，550520 字节；构建包 `D:/ft工程文件/FT-Dungeon-Mobs-Forge-1.20.1/build/libs/ft-dungeon-mobs-forge-1.20.1-0.17.0.jar`。
- 构建、安装与共享发布 SHA-256：`a806046dadc63f2b264313a94b490deb0f01fec5126c4a58e17decae04d4b009`。
- 原 0.16.0 备份：`C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/ft-mod-backups/ftdungeonmobs/20261101-224747-dungeon-rooms-0.17.0/FT地牢怪物-Forge-1.20.1-0.16.0.jar`；SHA-256 `a871fc6fe5caa011b3914836b1f404aad17f10b82b36b8f8bb70a3076563b43a`。同目录有原配置、其余模组完整哈希清单和部署收据。
- 同 ID 活动包恰好一份；无 `.pending` 或部署锁残留；其余 385 个模组文件名、大小与 SHA 均未变，配置 SHA 保持 `46da1c106667fa356ecfb3b4e05eae69653d77302299a0003622e60a80a82515`，FT 地牢挑战仍禁用。
- 原 31 / 30_1 原样打包，另有 L1/L2/M1/M2/S1/S2/L3/L4/S3 九间固定槽位主题房。完整重启后，从原入口成功入场才执行安全施工；大厅向南到 31，向东到 30_1，沿东支路继续到 X=80 的发光主干进入三行主题房。OP2 `/ftdungeon rooms status` 查看逐房状态；`/ftdungeon rooms build` 安全重试。占用就拒绝，不清空，不覆盖玩家改造。
- 未打开或改写玩家存档；只启动工程隔离 GameTest 服务端。完整构建 69 秒、Forge 数据验证 70 秒、真实 FT 维度 GameTest 104 秒通过；后者核对十一间 74,910 条记录、17 个 Lootr BE、九条步行连接、安全拒绝、独立持久化、幂等与 40 tick 无水外溢。不冒充完整整合包多人/FPS 实测。
- 门、返程、6000 tick 入场、五档难度保持；不添加怪物波次或奖励。既有合法返程门占用误报 P1 未修复，不能把建筑验收当作多人入场问题已解决。
- 核心证据：`D:/ft工程文件/FT-Dungeon-Mobs-Forge-1.20.1/output/room-import-20261101-211745/`。共享交付、坐标、原图/模板/预览指纹、发布包与收据：`D:/ft工程文件/FT地牢_Image25接入_20261101/integration/`。玩法边界和安全说明见 `ROOM_IMPORT.md`。

### 停服同步与回退 0.17.0

服务器和客户端须完整关闭后再同步同一份 0.17.0，只保留一份 `ftdungeonmobs` 包；31 模板要求兼容 Lootr。此次未给「服务器」实例或远端自动安装，不在运行中热替换。先备份目标世界、维度与配置，再复制已核对哈希的发行 JAR，完整重启后由管理员查看状态和服务端诊断。无需删除、重建地牢维度或玩家返回记录。

回退时先完整备份世界，停服移走 0.17.0 后恢复备份的 0.16.0 JAR，不要同时保留两版。此轮没有新增方块注册；已建原版/Lootr 房间仍在世界里，0.16.0 不再提供扩建入口与逐房目录。移除 Lootr 是另一项有缺失注册风险的操作，本轮不执行。不要删 `ftdungeonmobs_rooms.dat` 强迫覆盖；崩溃残留应从备份核对恢复。

## 历史：0.16.0（血祭血池建筑、液体与触发）

- 部署时间：2026-11-01 20:19，Asia/Shanghai；20:19:28 独立复核 `POST_DEPLOYMENT_PASS`。检查 Minecraft 未运行后再部署，没有停止进程、启动游戏/服务器或修改世界。完整重启生效，客户端与服务器均须同版。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.16.0.jar`，401104 字节；来源 `build/libs/ft-dungeon-mobs-forge-1.20.1-0.16.0.jar`。
- 构建/安装 SHA-256：`a871fc6fe5caa011b3914836b1f404aad17f10b82b36b8f8bb70a3076563b43a`。
- 旧包备份：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\ft-mod-backups\ftdungeonmobs\20261101-201903-sacrifice-blood-pool-0.16.0\FT地牢怪物-Forge-1.20.1-0.15.0.jar`；SHA-256 `08dd26642400f78edf7b76a5384e3106d24ee1f95908ec6abc9b91700b9bba18`。同目录有原配置、其它模组哈希和安装收据。
- 新增九种血池建筑、可装桶流动血液和一个祭台实体，详见 `BLOOD_POOL.md`。OP2 站在平整地面执行 `/ftbloodpool build ~ ~1 ~` 可安全预检后生成示例；未在玩家世界代执行。手持/掉落供品累计 16 点后空手确认，15 级输出 200 tick，冷却 600 tick；不自动启用波次扩展或修改原门入场规则。示例柱顶复用真实火盆，避免搭在已有门六格控制区内。
- 活动同 ID 包恰好一份，无 `.pending` 或锁残留；其余 385 个模组名称/大小/哈希均未变，FTBoss 未改、FT 地牢挑战继续禁用。配置 SHA-256 仍为 `46da1c106667fa356ecfb3b4e05eae69653d77302299a0003622e60a80a82515`。
- 完整构建 44 秒、Forge 验证 48 秒、旧资源保持性、原生编辑器与独立安装校验通过；没有实机液体/供品/红石/多人或 FPS 测试。已知 0.15.0 合法返程门占用误报 P1 此轮未修改，不能视为多人入场已修复。
- 证据：`output/sacrifice-blood-pool-20261101-192317/`；带停机、同 ID、配置/基线/候选哈希、互斥锁与失败回退检查的部署脚本及安装后独立核验脚本一并保存。

### 回退 0.16.0

关闭游戏/服务器并备份完整世界后，只保留一份模组再更换 JAR。**0.15.0 没有这次新增的方块、液体、物品及祭台注册**：只换回旧 JAR 会产生缺失注册并可能丢失血池内容。若世界已使用新内容，应同时恢复升级前世界备份；不承诺移走可见方块就足以清理所有实体、背包和区块数据。本次没有自动删块、删维度或回退世界。

## 历史：0.15.0（火盆联动、门色与五分钟入场）

- 部署时间：2026-11-01 17:52:51，Asia/Shanghai；17:54:12 独立复核 `POST_DEPLOYMENT_PASS`。未启动 Minecraft 客户端、服务器世界、修改世界文件或强制停止进程；完整重启后生效，多人两端须使用同版。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.15.0.jar`，322800 字节；构建来源 `build/libs/ft-dungeon-mobs-forge-1.20.1-0.15.0.jar`。
- 构建与安装 SHA-256：`08dd26642400f78edf7b76a5384e3106d24ee1f95908ec6abc9b91700b9bba18`。
- 旧 0.14.0 完整备份：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\ft-mod-backups\ftdungeonmobs\20261101-175241-dungeon-gate-rounds-0.15.0\FT地牢怪物-Forge-1.20.1-0.14.0.jar`；SHA-256：`0a893f7cfd5daa2739598a54164bf18ce035267eb60238c49d767aacf1222d05`。同目录保存原配置、其余模组哈希清单与部署收据。
- 最近火盆含熄灭态持续控制入口：熄灭关门，点燃常开，已点燃档位切换不关门；传送门表面按蓝/绿/金/紫/红同步，黑铁门框不染色。普通和潜行主手交互均调档，门框火炬委托火盆交互，不再单独切换门状态。
- 首位玩家成功进入后冻结本轮难度、锁定入口附近 6 格火盆，并开始 6000 服务端游戏 tick 的入场窗口（20 TPS 下五分钟）。后来进入不延时，到期拒绝进入并安全关门，返程始终可用；窗口结束且本轮成员全部离开后才解锁。离线成员、截止时间与档位持久保存；停服/暂停不消耗游戏 tick，低 TPS 会使实际时长增长。
- 活动 `ftdungeonmobs` JAR 恰好一份，无 `.pending` 或部署锁残留；其余 385 个模组文件名称、大小、哈希不变，FTBoss 未改，FT 地牢挑战仍禁用。原配置 SHA-256 保持 `46da1c106667fa356ecfb3b4e05eae69653d77302299a0003622e60a80a82515`。
- 完整构建 42 秒、Forge 数据验证 33 秒、最终 JAR 资源与几何保持性核验通过。未进行真实世界多人测试或 FPS 测试；软件五色示意见 `output/dungeon-gate-rounds-20261101-172859/preview/dungeon-gate-tier-colors.png`。规则与验收见 `DUNGEON_GATES.md`，验证证据与安装收据均在本轮输出目录。

### 回退 0.15.0

先在 0.15.0 下让全部本轮成员离开地牢、等待窗口结束并保存；关闭游戏/服务器且备份完整世界后，移出活动 0.15.0 JAR，恢复上述 0.14.0 备份，只保留一份同 ID 包。0.14.0 不识别新增门色属性和入场轮次逻辑，不能保证活动轮次直接降级安全，不要通过降级绕过离线成员锁定；最稳妥是恢复升级前世界备份。未自动删除维度、清理成员或修改离线玩家位置。

## 历史：0.14.0（火炬交互开关门动画）

- 部署时间：2026-11-01 17:15:04，Asia/Shanghai；17:17:51 独立复核 `POST_DEPLOYMENT_PASS`，无 `.pending` 或部署锁残留。没有启动 Minecraft 客户端、服务器世界、修改世界文件或强制停止任何进程；下次完整重启游戏后生效，联机两端使用同版。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.14.0.jar`；构建来源 `build/libs/ft-dungeon-mobs-forge-1.20.1-0.14.0.jar`，313313 字节。
- 构建/安装 SHA-256：`0a893f7cfd5daa2739598a54164bf18ce035267eb60238c49d767aacf1222d05`。
- 旧 0.13.1 完整备份：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\ft-mod-backups\ftdungeonmobs\20261101-171452-dungeon-gate-animation-0.14.0\FT地牢怪物-Forge-1.20.1-0.13.1.jar`；SHA-256 `40aea6b139a516deb02cd45b8eb4e627d269a811ff76148b3f1a59493f5cdfa8`。备份目录内另有原配置、其它模组哈希清单和部署收据。
- 门旁普通右键血棘祭炉，或点击两门自带壁灯，切换铁闸下沉/升起；每个完整行程 40 tick（2 秒），不需要压力板。全开后走入或右键门框传送；关门防夹、中途反向、NBT/网络状态同步已接入，实际游戏效果仍待验收。保持 3×4、原 ID、个人返回点和入口难度逻辑，不挖井或覆盖建筑。
- 同时整合独立完成的火盆熄灭态；默认未开启，第一次通过火盆开门时点燃至 1 档，已有档位不改。空手潜行右键只调档，不开门；附近无门时保留普通右键调档。五档点燃美术与所有骷髅美术保持不变，旧 DSH 火炬资源继续退役。
- 活动本模组只一份，385 个其它模组文件名称、大小、哈希不变；配置 SHA-256 仍为 `46da1c106667fa356ecfb3b4e05eae69653d77302299a0003622e60a80a82515`；FTBoss 未改，FT 地牢挑战仍禁用。部署工具有停机检查、基线/候选/配置哈希校验、同 ID 扫描、互斥锁、待安装副本及失败回退；独立安装后复核另存收据。
- 完整构建、Forge 数据回归、编辑器关/开姿态和最终 JAR 校验通过；没有客户端实机或 FPS 测试。操作见 `DUNGEON_GATES.md`，证据在 `output/dungeon-gate-animation-20261101-163724/`。

### 回退 0.14.0

先完整关闭游戏/服务器并备份世界，移出活动 0.14.0 包，恢复上述 0.13.1 备份，只保留一个同 ID 包。0.13.1 没有新增 open/moving 状态、门动画 BE 和火盆 tier=0，因此退回后门将不再具有动画，熄灭态无法由旧版表示，可能被默认化；不要把旧版当作无损状态回退。不得直接降至没有地牢维度的 0.12.0；先让所有玩家离开新增维度，历史降级风险见下文。

## 历史：0.13.1（参考图石砖骷髅铁闸）

- 部署时间：2026-11-01 16:08:41，Asia/Shanghai；独立复核 `POST_DEPLOYMENT_PASS`。未启动 Minecraft 客户端、服务器世界或修改世界文件。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.13.1.jar`，285569 字节；构建来源 `build/libs/ft-dungeon-mobs-forge-1.20.1-0.13.1.jar`。
- 构建与安装 SHA-256：`40aea6b139a516deb02cd45b8eb4e627d269a811ff76148b3f1a59493f5cdfa8`。
- 旧 0.13.0 完整备份：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\ft-mod-backups\ftdungeonmobs\20261101-160819-dungeon-gate-reference-0.13.1\FT地牢怪物-Forge-1.20.1-0.13.0.jar`；SHA-256 `0817a621281e3b3f81250c2784d444f3fd5629817c06fd2daf1a35a914511fef`。同目录保留原配置与其它模组哈希清单。
- 两扇门按用户五张概念图改为石砖、骷髅、阶梯绞盘、矩形链环、尖齿铁闸、暖焰和冷雾，入口附血痕、返程附冷蓝路符。保持原 3×4 占位、两种 ID 与 96 个朝向/部件状态。此轮只改模型与纹理，不加入下沉动画或压力板控制；门槛符牌不是开关。
- 全部 28 个既有运行 class 与 0.13.0 逐字节一致；传送、个人返回点、火盆难度、地牢维度、配方与掉落均未改变。原配置 SHA-256 仍为 `46da1c106667fa356ecfb3b4e05eae69653d77302299a0003622e60a80a82515`。
- 活动本模组仅一份，其余 385 个模组文件名称、大小与哈希不变；FTBoss 未改，FT 地牢挑战仍禁用，五张参考图片原文件未改，无 `.pending` 或部署锁残留。
- Blockbench 已重新打开两个可编辑源，分别 182 / 181 个整数长方体、8 组、3 张内嵌首帧纹理，并保存实际编辑器截图；原先未保存的火盆工程完整编译快照比对无变化且已恢复选中。没有修改交易桌或其它已有工程。
- 完整构建及 Forge 数据回归通过；实际整合包内画面、交互和联机仍待验收，重启游戏后查看。来源、成本与使用见 `DUNGEON_GATES.md`；证据目录 `output/dungeon-gate-reference-20261101-152422/`。

### 回退 0.13.1

关闭游戏/服务器后移出活动 0.13.1 包，恢复上述 0.13.0 备份，只保留一个同 ID 包。该回退保留原维度、门 ID 和存档格式，只恢复前版外观；仍建议先备份世界。不按此方法直接回退到没有新维度的 0.12.0，早期降级风险见历史章节。

## 历史：0.13.0（地牢往返大门与入口火盆控制）

- 部署时间：2026-11-01 15:11:49，Asia/Shanghai；独立安装后复核于 15:12:34 通过。未启动 Minecraft 客户端、服务器世界或修改玩家世界。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.13.0.jar`，260860 字节；构建来源 `build/libs/ft-dungeon-mobs-forge-1.20.1-0.13.0.jar`。
- 构建、安装 SHA-256：`0817a621281e3b3f81250c2784d444f3fd5629817c06fd2daf1a35a914511fef`。
- 旧 0.12.0 完整备份：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\ft-mod-backups\ftdungeonmobs\20261101-151133-dungeon-gates-0.13.0\FT地牢怪物-Forge-1.20.1-0.12.0.jar`；SHA-256 `fac3e79adc1cefbfecc77b5ca54fbfacc6c326fa65cc1a79e0d43a6ff88568a2`。同目录保存原配置与其它模组哈希清单。
- 活动 `ftdungeonmobs` 包只有一份；其余 385 个模组文件名称、大小、哈希不变，FTBoss 未改，`FT地牢挑战-Forge-1.20.1-1.0.0.jar.disabled` 仍禁用。原配置哈希仍为 `46da1c106667fa356ecfb3b4e05eae69653d77302299a0003622e60a80a82515`；无 `.pending` 或部署锁残留。
- 新增入口门 `ftdungeonmobs:dungeon_entry_gate`、返程门 `ftdungeonmobs:dungeon_return_gate` 与维度 `ftdungeonmobs:dungeon`。完整重启后使用；多人服务器两端须装同版。门为 3×4，多方块点击中下格摆放，主手右键传送；首次到达准备起始大厅及返程门，不是完整随机地牢或波次系统。
- 入口 6 格内最近火盆控制地牢维度的自动分级 FT 怪物；玩家分别记录返回位置，返程不依赖原火盆仍存在。规则与验收见 `DUNGEON_GATES.md`。
- 构建、原生模型/GeckoLib 回归、Forge `runData` 注册与维度 Codec、SavedData/非法返回 NBT 检查、最终 JAR CRC/源码资源/旧美术保持性检查均通过；实际客户端放置、联机与传送仍待实机验收。
- 证据：`output/dungeon-gates-20261101-144504/`，含完整源码改前副本、哈希基线、构建及 Forge 数据校验日志、模型示意、JAR 核验、部署与独立复核收据；`build/reports/dungeon-gates-runtime.txt` 保存无世界启动的运行时校验结果。

### 回退 0.13.0

先让所有玩家在 0.13.0 下离开地牢并保存，关闭游戏/服务器并备份完整世界，然后移出活动 0.13.0 JAR、恢复上述 0.12.0 备份；不要让两个同 ID 包同时处于 `mods`。0.12.0 没有新维度和新门注册，不能保证已使用新内容的世界直接降级安全；最稳妥是恢复升级前世界备份。未替用户自动删除维度文件或修改离线玩家位置。

## 历史：0.12.0（五档变色；删除旧 DSH 火炬模型）

- 部署时间：2026-11-01 12:08:29，Asia/Shanghai；未启动 Minecraft 客户端。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.12.0.jar`，207785 字节；构建来源 `build/libs/ft-dungeon-mobs-forge-1.20.1-0.12.0.jar`。
- 构建与安装 SHA-256 一致：`fac3e79adc1cefbfecc77b5ca54fbfacc6c326fa65cc1a79e0d43a6ff88568a2`。
- 旧 0.11.0 备份：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\ft-mod-backups\ftdungeonmobs\20261101-120820-bloodthorn-tier-colors-0.12.0\FT地牢怪物-Forge-1.20.1-0.11.0.jar`；SHA-256 `556c304bb9d3ec891582db1d320c3febaa4f9034d096027e32fe419dcb76f7d6`。
- 已确认活动同 ID JAR 仅一份；其余 385 个模组文件的名称、大小与 SHA-256 不变，FTBoss 和禁用的 FT 地牢挑战均未改动。
- 配置未变，SHA-256 `46da1c106667fa356ecfb3b4e05eae69653d77302299a0003622e60a80a82515`；不修改世界或其它实例。
- 血棘祭炉按难度呈苍蓝、翠绿、鎏金、紫焰、猩红；共享几何与本体，所有档位保留 16 帧/32 tick 循环。旧 DSH 的 geo、骨骼动画、六张贴图及三份客户端渲染类均不在安装包中。
- 旧 `arena_torch` ID 只作兼容：旧下半部显示新模型，上半部无模型/碰撞；旧库存物品直接放置新祭炉，旧摆放挖下掉落新祭炉，重放后为单格。保留惰性 BE 类型以兼容旧存档注册，不再运行旧动画或渲染器。
- 源码和退役模型的恢复副本仅位于 `output/bloodthorn-tier-colors-20261101-120156/source-before/` 与 `retired-assets/`，不会被打入 JAR；证据同目录 `clean.log`、`build.log`、`jar-verification.json`、`source-preservation.json`、`deployment-receipt.json`、`post-deployment-verification.json`。
- 回退时先退出游戏并备份世界，将 0.12.0 移出 `mods` 后恢复上述 0.11.0 备份，只保留本模组一份。0.11.0 会恢复旧火炬资源，且祭炉不再随难度变色。独立服务器需要人工同步升级，不能推定本次已替它更新。

## 历史版本：0.11.0（第二种火盆：血棘祭炉）

- 部署时间：2026-11-01 11:21:12，Asia/Shanghai；未启动 Minecraft 客户端。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.11.0.jar`，219353 字节。
- 构建来源：`build/libs/ft-dungeon-mobs-forge-1.20.1-0.11.0.jar`；构建产物与安装文件 SHA-256：`556c304bb9d3ec891582db1d320c3febaa4f9034d096027e32fe419dcb76f7d6`。
- 旧 0.10.0 已移出 `mods`，备份至 `C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\ft-mod-backups\ftdungeonmobs\20261101-112100-bloodthorn-0.11.0\FT地牢怪物-Forge-1.20.1-0.10.0.jar`；旧 SHA-256 仍为 `42c688bc0a4a3a7b195a0bbe48b099e0716ecc0060469d8cac193315e1aa5628`。
- 通过 `mods.toml` 扫描确认仅一个启用的 `ftdungeonmobs`；385 个其他 `mods` 文件（含禁用文件）名称、大小、SHA-256 全部不变。FTBoss 未改动，`FT地牢挑战-Forge-1.20.1-1.0.0.jar.disabled` 保持禁用。
- `config/ftdungeonmobs-common.toml` 保留原值，SHA-256：`46da1c106667fa356ecfb3b4e05eae69653d77302299a0003622e60a80a82515`；未改写世界存档、其它实例或桌面/OneDrive。
- 操作采用独占部署锁、候选文件校验、旧包备份、重命名安装和失败回滚；安装后再次独立核对安装/备份/配置哈希，确认无旧活动包、残留 pending 或锁文件。
- 证据目录：`output/bloodthorn-integration-20261101-111539/`，含 `build.log`、`source-preservation.json`、`jar-verification.json`、`deployment-receipt.json`、`post-deployment-verification.json`；改前源码副本见同目录 `source-before/`。

### 0.11.0 使用与回退

- 获取新火盆：`/give @s ftdungeonmobs:bloodthorn_brazier`；旧火炬仍是 `ftdungeonmobs:arena_torch`。新火盆为一格原版方块模型，旧火炬仍为两格 GeckoLib 骨骼动画模型。
- 客户端与服务端均须更新到同一版本；本次只部署到上面明确列出的本机实例，不推定其它服务器已同步。
- 如需回退，先关闭游戏并备份世界，再将 0.11.0 JAR 移出 `mods`、将上述 0.10.0 备份恢复到 `mods`，保持本模组仅一份。**0.10.0 不认识新方块 ID，已经摆放血棘祭炉的世界直接回退可能丢失该方块。**

## 历史版本：0.10.0（火炬 v8 · GeckoLib 4.8.4 骨骼动画版）

- 部署时间：2026-11-01 09:32:03，Asia/Shanghai。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.10.0.jar`。
- 构建来源：`D:\ft工程文件\FT-Dungeon-Mobs-Forge-1.20.1\build\libs\ft-dungeon-mobs-forge-1.20.1-0.10.0.jar`，**205283 字节**。
- SHA-256：`42c688bc0a4a3a7b195a0bbe48b099e0716ecc0060469d8cac193315e1aa5628`，构建与部署一致（逐字节核对通过）。
- 本版为 **v8（GeckoLib 4.8.4 骨骼动画版）**：2 格 BaseEntityBlock + BE（lower）+ GeckoLib 渲染；blockstates 保留 HALF+tier 10 variants（→空占位模型）；geo **16 bones/59 cubes/354 faces**（整数网格 0 违规）+ `animation.arena_brazier.idle`（loop，2.0s）；body 图集火焰 tile 5 档换色（`arena_torch_body.png` + `_t1..t5`，64×64）；item 静态模型（59 元素/display 7/ymax 32/无 texture_size）；**旧 v7 静态 17 项已清理**（7 模型 + 5 火焰 + 5 mcmeta）。
  - **Java 7 文件**（geckolib 实现）：改 `ArenaTorchBlock.java`（→BaseEntityBlock + HALF/TIER + ENTITYBLOCK_ANIMATED + lower-only BE + 粒子高度 +20..31px）、`FTDungeonMobs.java`（BLOCK_ENTITY_TYPES/ARENA_TORCH_ENTITY）、`client/DungeonClient.java`（+registerBlockEntityRenderer + cutout）；新 `ArenaTorchBlockEntity.java`、`client/ArenaTorchModel.java`、`client/ArenaTorchRenderer.java`、`client/ArenaTorchEmissiveLayer.java`；其余 12 Java 未动。
  - 方块/物品 ID、HALF+tier blockstate、右键 1→2→3→4→5→1、难度接口、配方/loot/语言键不变；旧存档 blockstate 兼容。
- 构建校验：Gradle `build` 通过（离线 Gradle 8.8 + JDK17，`BUILD SUCCESSFUL in 21s`）：
  - `verifyArenaAssets`（v8 GeckoLib 验证器）：`ARENA_ASSETS_V8_VERIFIED: GeckoLib 4.8.4 parsed/baked geo (bones=16, cubes=59, faces=354, flameBones=2, integerViolations=0); animation=animation.arena_brazier.idle (loop=true, length=2.0, bones=10); textures body 64x64 + tierBodies t1..t5; blockstates=10 variants (placeholder elements=0); item 59 elements with display; legacy v7 static resources cleaned; java=17 files, block/be/renderer/gecko/registered=true`；
  - `verifyAssets`（骷髅线回归）：4 模型 / 218 骨骼 / 17 动画通过。
- JAR 内容核对：arena 资源 **11 项**（geo/anim/blockstates/占位/item/6 body 纹理）、**无 v7 旧静态 17 项残留**、无 v2/第三方素材，`Implementation-Version: 0.10.0`。
- 活动 `mods/` 中本模组仅一份；mods 可见文件数 373 前后不变（全部条目 386，含外部工作线独立模组，非本模组），其他模组抽样 SHA 未变，配置/存档未改。
- 机器可读记录：`build/reports/deployment-0.10.0.json`；集成/构建/部署证据：`D:\ft工程文件\竞技场长矛火炬模型_20261031\work\v8\integration\INTEGRATION_REPORT.md`。

启动或重启该 Forge 整合包，在“FT地牢怪物”或“功能方块”创造栏获取“竞技场长矛火炬”，也可执行 `/give @s ftdungeonmobs:arena_torch`。留出两格高度放置，右键上下任一半部即可按 1→2→3→4→5→1 调级（火焰颜色随档位变化，GeckoLib idle 动画）。

**回滚（七级）**：
1. **0.10.0 → 0.9.0（v7）**：退出游戏 → 备份存档 → 删除 `mods/FT地牢怪物-Forge-1.20.1-0.10.0.jar` → 从 `...\ft-mod-backups\ftdungeonmobs\20261101013203\` 拷回 0.9.0 jar（`4bfb43d0…`，204787B）+ 配置。
2. **0.9.0 → 0.8.0（v6）**：从 `...\20261101010040\`（`1e33e8a2…`，201959B）。
3. **0.8.0 → 0.7.0（v5）**：从 `...\20261101003038\`（`4b4fdbd3…`，201317B）。
4. **0.7.0 → 0.6.0（v4）**：从 `...\20261101000936\`（`9e9b01ed…`，203393B）。
5. **0.6.0 → 0.5.0（v3）**：从 `...\20261031234809\`（`877d795f…`，198646B）。
6. **0.5.0 → 0.4.0（v2）**：从 `...\20261031162146\`（`f8f5c52a…`，240858B）。
配置无需改动（`46da1c10…` 部署前后一致）。确认 mods 只留一版、可见文件数 373。源码回滚：v8 前 → `work/v8/integration/backup-20261101-093126/project/`（0.9.0 源 + geckolib Java）。

**已知 P2（待实机验收）**：GeckoLib idle 动画/渲染观感、火焰 tile 5 档换色、EmissiveLayer 发光层、BE 同步/性能、item 静态模型 GUI/手持差异、旧存档首次加载兼容。

## 0.9.0 历史部署（v7 新火盆 / 两格高静态方块模型，已被 0.10.0 取代）

- 部署时间：2026-11-01 09:00:40，Asia/Shanghai。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.9.0.jar`。
- 构建来源：`D:\ft工程文件\FT-Dungeon-Mobs-Forge-1.20.1\build\libs\ft-dungeon-mobs-forge-1.20.1-0.9.0.jar`，**204787 字节**。
- SHA-256：`4bfb43d07d8f432ca375748222f75b4d38c316b1bfd1bb0408b48b7e4682b4b6`，构建与部署一致（逐字节核对通过）。
- 本版为 **v7（新火盆 · 两格高静态方块模型）**：
  - **参考来源与原创声明**：造型参考『灾变（Cataclysm）』Altar of Fire 与用户提供的 gpt 解包模型风格（仅风格/构图参考）；**模型几何与贴图均为本项目原创**（`sources_v7/arena_brazier.bbmodel` + TEX-7），未复制第三方模型/贴图文件；**参考作品许可未核实**——本次为内部/个人整合包使用，公开分发前需确认许可或进一步原创化。
  - **两格高**：视觉/占用均 y0..32（lower y0..16 + upper 局部 y0..15.8），方块仍占 lower+upper 两格；不再是 v6 的三格高视觉。
  - 结构：窄条垂旗（F/B/L/R 各 1，7.2→3.6 宽）、四角铆钉外移修穿模、删除 ember chunk；新 body 纹理 64×64（TEX-7，sha `e188fe0b…`）；火焰沿用 v4（5×16×128 8 帧+mcmeta）。
  - **Java 仅渲染小改**：`ArenaTorchBlock.animateTick` 火焰粒子高度 = `position.getY()+0.3..1.0`（仅 UPPER 半部调用；= 相对 lower +1.3..2.0 格 = 世界 +20.8..+31.8 像素）；x/z ±0.35、音效与其余逻辑不变（与 0.8.0 diff 1 行）。
  - 资源（同命名覆盖 20 项）：blockstate **10 variants**；lower **40 元素/190 面/0 火焰面**；upper **51 元素/258 面/每档火焰面 8/8/8/8/8**；item **87 元素/432 面/display 7/ymax 28.60**；body 64×64、火焰 5×16×128 8 帧+5 mcmeta。
  - **预算**：编辑口径 **87 元素/432 面**（≤96/≤600）；world 口径 **91 元素/448 面**（≤112）；跨 y=16 元素 4。
  - 删除 v2/v3/v4/v5/v6 特有资源（同命名覆盖）：无 geo/animations/128² 纹理/glowmask/particle/BE 类/第三方参考素材残留。
- 构建校验：Gradle `build`/`check` 通过（离线 Gradle 8.8 + JDK17，`BUILD SUCCESSFUL in 26s`）：
  - `verifyArenaAssets`（v7 静态验证器）：`V7_BUDGET: flameFaces=8/8/8/8/8; T1..T5 edit=87el/432f world=91el/448f`；`ARENA_ASSETS_VERIFIED (v7 static)` —— 10 variants、lower 0 火焰面、upper 8/8/8/8/8、**两格高 lower/upper y≤16（世界 32）**、64×64 body、5 档 16×128 8 帧+mcmeta、Forge 全亮、item display、UV 0–16；
  - `verifyAssets`（骷髅线回归）：4 模型 / 218 骨骼 / 17 动画通过。
- JAR 内容核对：arena **恰 20 条目**、**无 v6/v5/v4/v3/v2 残留与第三方参考素材**，`Implementation-Version: 0.9.0`。
- 活动 `mods/` 中本模组仅一份；mods 可见文件数 373 前后不变（全部条目 386，含外部工作线独立模组，非本模组），其他模组抽样 SHA 未变，配置/存档未改。
- 机器可读记录：`build/reports/deployment-0.9.0.json`；集成/构建/部署证据：`D:\ft工程文件\竞技场长矛火炬模型_20261031\work\v7\integration\INTEGRATION_REPORT.md`。

启动或重启该 Forge 整合包，在“FT地牢怪物”或“功能方块”创造栏获取“竞技场长矛火炬”，也可执行 `/give @s ftdungeonmobs:arena_torch`。留出两格高度放置，右键上下任一半部即可按 1→2→3→4→5→1 调级（火焰依次白蓝、翠绿、鎏金、紫、猩红，8 帧贴图动画）。v7 为新火盆造型（参考 Cataclysm Altar of Fire 风格、原创几何与贴图）。

**回滚（六级）**：
1. **0.9.0 → 0.8.0（v6）**：退出游戏 → 备份存档 → 删除 `mods/FT地牢怪物-Forge-1.20.1-0.9.0.jar` → 从 `...\ft-mod-backups\ftdungeonmobs\20261101010040\` 拷回 `FT地牢怪物-Forge-1.20.1-0.8.0.jar`（SHA `1e33e8a2…`，201959B）+ 配置。
2. **0.8.0 → 0.7.0（v5）**：从 `...\20261101003038\` 拷回 0.7.0 jar（`4b4fdbd3…`，201317B）。
3. **0.7.0 → 0.6.0（v4）**：从 `...\20261101000936\` 拷回 0.6.0 jar（`9e9b01ed…`，203393B）。
4. **0.6.0 → 0.5.0（v3）**：从 `...\20261031234809\` 拷回 0.5.0 jar（`877d795f…`，198646B）。
5. **0.5.0 → 0.4.0（v2）**：从 `...\20261031162146\` 拷回 0.4.0 jar（`f8f5c52a…`，240858B）。
配置无需改动（`ftdungeonmobs-common.toml` SHA-256 = `46da1c10…`，部署前后一致）。确认 mods 中本模组只留一版、可见文件数 373。源码回滚：v7 前 → `work/v7/integration/backup-20261101-085958/project/`（0.8.0 源）。

**已知 P2（后续候选，不影响本版验收）**：面数/细节可再增（火盆纹样/骨饰）；火焰 core 偏白；top/bottom 视角渲染覆盖率偏低；新造型比例需实机确认；参考作品许可未核实（公开分发前需确认）。

## 0.8.0 历史部署（v6 三格高视觉，已被 0.9.0 取代）

- 部署时间：2026-11-01 08:30:38，Asia/Shanghai。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.8.0.jar`。
- 构建来源：`D:\ft工程文件\FT-Dungeon-Mobs-Forge-1.20.1\build\libs\ft-dungeon-mobs-forge-1.20.1-0.8.0.jar`，**201959 字节**。
- SHA-256：`1e33e8a2c941ea606a0e57e0a50574a965ec574beee4f36515687c204c4fd000`，构建与部署一致（逐字节核对通过）。
- 本版为 **v6（视觉三格高 / 粗矛左右对角托举 / 不穿模 / 血腥脏污基座）**：
  - **视觉 3 格高，但方块碰撞/占用仍为 lower+upper 两格（第 3 格仅视觉）**，不要宣称三格占用；可能与上方方块视觉重叠，需实机评估。
  - 结构：台座 y0..8（3 层血腥石台+铭牌+骷髅×2+骨堆×2+断箭）；**4 根粗长矛（截面 1.0）左右对角线交叉托举**（z 错位不相交），**2 根侧向对角斜撑**（绕 x ±22.5°）侧视也是 X；主斜撑两端贴住（底 y8 台座顶、顶 y32 盆底）；火盆 y32..39.7（宽口铆钉+提手+煤床+8 组盆沿外侧短矛头）；5 档火焰 34.4→48。
  - **Java 仅渲染小改**：`ArenaTorchBlock.animateTick` 火焰粒子高度改到世界 y≈+34..+48（仅 UPPER 半部调用，覆盖火盆/火焰层）；HALF/放置/破坏/右键/难度/配方/掉率逻辑不变（与 0.7.0 diff 1 行）。
  - 资源（同命名覆盖 20 项）：blockstate **10 variants**；lower **21 元素/114 面/0 火焰面**；upper **38 元素/185 面/每档火焰面 8/8/8/8/8**（localYmax 28.6→32.0）；item **53 元素/275 面/display 7**；`arena_torch_body.png` **64×64**（sha `34d15233…`）；`arena_flame_1..5.png` **16×128 8 帧**（sha 沿用 v4 最终版）+ 5 mcmeta（`b2d0e797…`）。
  - **预算**：编辑口径 **53 元素/275 面**（≤56/≤380）；world 口径 **59 元素/299 面**（≤80）。
  - **item 模型必要修正**：原版 BlockElement 坐标上限 [-16,32]，v6 item 几何 y0..44.6 会被 `BlockModel.fromStream` 拒绝；已等比缩放（x/y/z 同 factor 0.715247，x/z 绕中心、y 绕底部）使 y≤31.9，并 `display.scale` ×1.398119 补偿（7 槽），元素/面数不变；原文件备份 `work/v6/integration/fixes/arena_torch.item.original.json`。
  - 删除 v2/v3/v4/v5 特有资源（同命名覆盖）：无 geo/animations/128² 纹理/glowmask/particle/BE 类残留。
- 构建校验：Gradle `build`/`check` 通过（离线 Gradle 8.8 + JDK17，`BUILD SUCCESSFUL in 16s`）：
  - `verifyArenaAssets`（v6 静态验证器）：`V6_BUDGET: flameFaces=8/8/8/8/8; T1..T5 edit=53el/275f world=59el/299f`；`ARENA_ASSETS_VERIFIED (v6 static)` —— 10 variants、lower 0 火焰面、upper 8/8/8/8/8、**upper 局部 y≤32（世界 48）**、64×64 body、5 档 16×128 8 帧+mcmeta、Forge 全亮、item display、UV 0–16；
  - `verifyAssets`（骷髅线回归）：4 模型 / 218 骨骼 / 17 动画通过。
- JAR 内容核对：arena 20 条目、**无 v5/v4/v3/v2 残留**，`Implementation-Version: 0.8.0`。
- 活动 `mods/` 中本模组仅一份；mods 可见文件数 373 前后不变（全部条目 386，含外部工作线独立模组，非本模组），其他模组抽样 SHA 未变，配置/存档未改。
- 机器可读记录：`build/reports/deployment-0.8.0.json`；集成/构建/部署证据：`D:\ft工程文件\竞技场长矛火炬模型_20261031\work\v6\integration\INTEGRATION_REPORT.md`。

启动或重启该 Forge 整合包，在“FT地牢怪物”或“功能方块”创造栏获取“竞技场长矛火炬”，也可执行 `/give @s ftdungeonmobs:arena_torch`。留出两格高度放置（视觉高 3 格，但占位仍是两格），右键上下任一半部即可按 1→2→3→4→5→1 调级（火焰依次白蓝、翠绿、鎏金、紫、猩红，8 帧贴图动画）。v6 为粗矛左右对角托举的三格高视觉造型，火焰粒子覆盖火盆/火焰层。

**回滚（五级）**：
1. **0.8.0 → 0.7.0（v5）**：退出游戏 → 备份存档 → 删除 `mods/FT地牢怪物-Forge-1.20.1-0.8.0.jar` → 从 `...\ft-mod-backups\ftdungeonmobs\20261101003038\` 拷回 `FT地牢怪物-Forge-1.20.1-0.7.0.jar`（SHA `4b4fdbd3…`，201317B）+ 配置。
2. **0.7.0 → 0.6.0（v4）**：从 `...\ft-mod-backups\ftdungeonmobs\20261101000936\` 拷回 0.6.0 jar（`9e9b01ed…`，203393B）。
3. **0.6.0 → 0.5.0（v3）**：从 `...\ft-mod-backups\ftdungeonmobs\20261031234809\` 拷回 0.5.0 jar（`877d795f…`，198646B）。
4. **0.5.0 → 0.4.0（v2 GeckoLib）**：从 `...\ft-mod-backups\ftdungeonmobs\20261031162146\` 拷回 0.4.0 jar（`f8f5c52a…`，240858B）。
配置无需改动（`ftdungeonmobs-common.toml` SHA-256 = `46da1c10…`，部署前后一致）。确认 mods 中本模组只留一版、可见文件数 373。源码回滚：v6 前 → `work/v6/integration/backup-final-20261101-083010/project/`（0.7.0 源）。

**已知 P2（后续候选，不影响本版验收）**：面数 275 略低；火焰 core 偏白；top/bottom 视角渲染覆盖率偏低；底部骨堆偏小；三格高上方遮挡需实机确认。

## 0.7.0 历史部署（v5 细长矛束支撑，已被 0.8.0 取代）

- 部署时间：2026-11-01 08:09:36，Asia/Shanghai。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.7.0.jar`。
- 构建来源：`D:\ft工程文件\FT-Dungeon-Mobs-Forge-1.20.1\build\libs\ft-dungeon-mobs-forge-1.20.1-0.7.0.jar`，**201317 字节**。
- SHA-256：`4b4fdbd3c977b2d83e454ac12cd062655403e455374190d2a4eb9d863d6873a0`，构建与部署一致（逐字节核对通过）。
- 本版为 **v5（细长矛束支撑 / 不穿模 / 血腥脏污基座）**：
  - **Java 结构与 0.6.0 相同，本版仅资源替换 + 版本升级**（方块/物品 ID、两格结构、右键 1→2→3→4→5→1、难度接口、配方/loot/语言键、照明等级全部不变）。
  - 结构变化：**去掉粗柱**，中心为 6 根细支撑杆按六边形散开（半径 ~1.35）；**4 根前后斜向完整长矛**（z 错位交叉不相交，大银矛头高出盆沿两侧）；无链条/rim 短矛；台阶铁箍 4 片外侧贴片、提手外移；火焰 core 收窄、outer 抬高到盆沿上；**血腥脏污基座**（TEX-5）。
  - 资源（同命名覆盖 20 项）：blockstate **10 variants**；lower **24 元素/124 面/0 火焰面**；upper **34 元素/165 面/每档火焰面 8/8/8/8/8**；item **48 元素/249 面/display 7**；`arena_torch_body.png` **64×64**（sha `34d15233…`）；`arena_flame_1..5.png` **16×128 8 帧**（sha 沿用 v4 最终版：`169e4a61…`/`b78a5aee…`/`cf70726c…`/`c9cde401…`/`5f2dbaf6…`）+ 5 mcmeta（`b2d0e797…`）。
  - **预算**：编辑口径 **48 元素/249 面**（≤56/≤340）；world 口径 **58 元素/289 面**（≤68）。
  - 删除 v2/v3/v4 特有资源：无 geo/animations/128² 纹理/glowmask/particle/BE 类残留。
- 构建校验：Gradle `build`/`check` 通过（离线 Gradle 8.8 + JDK17，`BUILD SUCCESSFUL in 22s`）：
  - `verifyArenaAssets`（v5 静态验证器）：`V5_BUDGET: flameFaces=8/8/8/8/8; T1..T5 edit=48el/249f world=58el/289f`；`ARENA_ASSETS_VERIFIED (v5 static)` —— 10 variants、lower 0 火焰面、upper 8/8/8/8/8、64×64 body、5 档 16×128 8 帧+mcmeta、Forge 全亮、item display、UV 0–16；
  - `verifyAssets`（骷髅线回归）：4 模型 / 218 骨骼 / 17 动画通过。
- JAR 内容核对：arena 20 条目、**无 v4/v3/v2 残留**，`Implementation-Version: 0.7.0`。
- 活动 `mods/` 中本模组仅一份；mods 可见文件数 373 前后不变（全部条目 386，含外部工作线独立模组，非本模组），其他模组抽样 SHA 未变，配置/存档未改。
- 穿模/结构自检（Lead/critic 工具）：`support` PASS（细杆 6、粗柱 0）、`clip` PASS（0 非有意穿透）、`spec` PASS、`blood` PASS；`check_overlap.py` structural 0 对（flame-flame 层叠 6 对/档为有意叠加）。
- 机器可读记录：`build/reports/deployment-0.7.0.json`；集成/构建/部署证据：`D:\ft工程文件\竞技场长矛火炬模型_20261031\work\v5\integration\INTEGRATION_REPORT.md`。

启动或重启该 Forge 整合包，在“FT地牢怪物”或“功能方块”创造栏获取“竞技场长矛火炬”，也可执行 `/give @s ftdungeonmobs:arena_torch`。留出两格高度放置，右键上下任一半部即可按 1→2→3→4→5→1 调级（火焰依次白蓝、翠绿、鎏金、紫、猩红，8 帧贴图动画）。v5 模型为细长矛束支撑（无粗柱）、前后斜向完整长矛与大银矛头、血腥脏污基座。

**回滚（四级）**：
1. **0.7.0 → 0.6.0（v4）**：退出游戏 → 备份存档 → 删除 `mods/FT地牢怪物-Forge-1.20.1-0.7.0.jar` → 从 `...\ft-mod-backups\ftdungeonmobs\20261101000936\` 拷回 `FT地牢怪物-Forge-1.20.1-0.6.0.jar`（SHA `9e9b01ed…`，203393B）+ 配置。
2. **0.6.0 → 0.5.0（v3 简化版）**：从 `...\ft-mod-backups\ftdungeonmobs\20261031234809\` 拷回 0.5.0 jar（`877d795f…`，198646B）。
3. **0.5.0 → 0.4.0（v2 GeckoLib）**：从 `...\ft-mod-backups\ftdungeonmobs\20261031162146\` 拷回 0.4.0 jar（`f8f5c52a…`，240858B）。
配置无需改动（`ftdungeonmobs-common.toml` SHA-256 = `46da1c10…`，部署前后一致）。确认 mods 中本模组只留一版、可见文件数 373。源码回滚：v5 前 → `work/v5/integration/backup-20261101-080858/project/`（0.6.0 源）。

**已知 P2（0.7.1 候选，不影响本版验收）**：面数 249 略低；火焰 core 偏白；top/bottom 视角渲染覆盖率偏低；底部骨堆偏小。

## 0.6.0 历史部署（v4 第一版精度重置，已被 0.7.0 取代）

- 部署时间：2026-11-01 07:48:09，Asia/Shanghai。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.6.0.jar`。
- 构建来源：`D:\ft工程文件\FT-Dungeon-Mobs-Forge-1.20.1\build\libs\ft-dungeon-mobs-forge-1.20.1-0.6.0.jar`，**203393 字节**。
- SHA-256：`9e9b01ed17501aa03382a63005966a414ce6c4ee5ae4a9d43374d12b2811eee2`，构建与部署一致（逐字节核对通过）。
- 本版为 **v4 第一版精度重置版**（按用户要求回到第一版精度，非 v3 过度简化）：
  - **Java 结构与 0.5.0 相同，本版仅资源替换 + 版本升级**（方块/物品 ID、两格结构、右键 1→2→3→4→5→1、难度接口、配方/loot/语言键、照明等级全部不变）。
  - 资源（同命名覆盖 20 项）：blockstate **10 variants**；lower **24 元素/124 面/0 火焰面**；upper **38 元素/183 面/每档火焰面 8/8/8/8/8**；item **52 元素/269 面/display 7**；`arena_torch_body.png` **64×64**（sha `c2c14dc1…`）；`arena_flame_1..5.png` **16×128 8 帧**（frametime=2）+ 5 mcmeta（最终 sha `169e4a61…`/`b78a5aee…`/`cf70726c…`/`c9cde401…`/`5f2dbaf6…`）。
  - **预算（编辑口径）52 元素/269 面**（v4 预算 ≤52/≤280）；world 拆分口径 62 元素/307 面（仅参考）。**长矛交错、细节基座、底部骷髅头/骨堆/断箭回归**；火焰为清晰大块 8 帧贴图动画。
  - 删除 v2/v3 特有资源：无 geo/animations/128² 纹理/glowmask/particle/BE 类残留。
- 构建校验：Gradle `build`/`check` 通过（离线 Gradle 8.8 + JDK17，`BUILD SUCCESSFUL in 22s`）：
  - `verifyArenaAssets`（v4 静态验证器，真实 Minecraft/Forge `BlockModel.fromStream`）：`V4_BUDGET: flameFaces=8/8/8/8/8; T1..T5 edit=52el/269f world=62el/307f`；`ARENA_ASSETS_VERIFIED (v4 static)` —— 10 variants、lower 0 火焰面、upper 8/8/8/8/8、64×64 body、5 档 16×128 8 帧+mcmeta、Forge 全亮、item display、UV 0–16；
  - `verifyAssets`（骷髅线回归）：4 模型 / 218 骨骼 / 17 动画通过。
- JAR 内容核对：arena 20 条目、**无 v2/v3 残留**，`Implementation-Version: 0.6.0`。
- 活动 `mods/` 中本模组仅一份；mods 可见文件数 373 前后不变（全部条目 386，含外部工作线新增的其他模组：FT地牢挑战 1.0.0、legendarymonsters，非本模组），其他模组抽样 SHA 未变，配置/存档未改。
- 机器可读记录：`build/reports/deployment-0.6.0.json`；集成/构建/部署证据：`D:\ft工程文件\竞技场长矛火炬模型_20261031\work\v4\integration\INTEGRATION_REPORT.md`。

启动或重启该 Forge 整合包，在“FT地牢怪物”或“功能方块”创造栏获取“竞技场长矛火炬”，也可执行 `/give @s ftdungeonmobs:arena_torch`。留出两格高度放置，右键上下任一半部即可按 1→2→3→4→5→1 调级（火焰依次白蓝、翠绿、鎏金、紫、猩红，8 帧贴图动画）。v4 模型恢复第一版精度的长矛交错/基座细节/骷髅头骨堆断箭（比 v3 简化版更细致）。

**回滚（三级）**：
1. **0.6.0 → 0.5.0（v3 简化版）**：退出游戏 → 备份存档 → 删除 `mods/FT地牢怪物-Forge-1.20.1-0.6.0.jar` → 从 `...\ft-mod-backups\ftdungeonmobs\20261031234809\` 拷回 `FT地牢怪物-Forge-1.20.1-0.5.0.jar`（SHA `877d795f…`，198646B）+ 配置。
2. **0.5.0 → 0.4.0（v2 GeckoLib）**：从 `...\ft-mod-backups\ftdungeonmobs\20261031162146\` 拷回 `FT地牢怪物-Forge-1.20.1-0.4.0.jar`（SHA `f8f5c52a…`，240858B）。
配置无需改动（`ftdungeonmobs-common.toml` SHA-256 = `46da1c10…`，部署前后一致）。确认 mods 中本模组只留一版、可见文件数 373。源码回滚：v4 前 → `work/v4/integration/backup-20261101-074730/project/`（0.5.0 源）；0.4.0 前 → `work/v2/integration/backup-20261031-231910/project/`。

## 0.5.0 历史部署（v3 原版风简化版，已被 0.6.0 取代）

- 部署时间：2026-11-01 00:26:04，Asia/Shanghai。
- 当前 JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.5.0.jar`。
- 构建来源：`D:\ft工程文件\FT-Dungeon-Mobs-Forge-1.20.1\build\libs\ft-dungeon-mobs-forge-1.20.1-0.5.0.jar`，**198646 字节**。
- SHA-256：`877d795f9bf0e86803d8105035930b8ba0c012c6f49da2482b5553ca44e8891e`，构建与部署一致（逐字节核对通过）。
- 本版为 **v3 原版风简化版**：**v2 的 GeckoLib 动画方块实体已回退为静态方块模型 + 贴图动画火焰**（同 0.3.x 路线）。
  - 0.5.0 = 0.4.0 的代码/逻辑基座（Java 回退为 `DoublePlantBlock` 静态版，保留 0.3.2 COLORS 调色板/粒子/音效）+ v3 简化模型/纹理；保留 GeckoLib 仅用于四套骷髅模型动画。
  - **删除 v2 的 17 项**：`ArenaTorchBlockEntity`/`ArenaTorchRenderer`/`ArenaTorchModel`、`geo/arena_torch.geo.json`、`animations/arena_torch.animation.json`、v2 占位 `models/block/arena_torch.json`、`particle/arena_ember.png`、`arena_torch_1..5.png`（128²）+ 5 `_glowmask`。
  - **新增 v3 静态资源 20 项**：`blockstates/arena_torch.json`（10 variants）、`models/block/arena_torch_lower.json` + `arena_torch_upper{,_1..5}.json`、`models/item/arena_torch.json`（display 7 槽）、`textures/block/arena_torch_body.png`（**64×64**）、`arena_flame_1..5.png`（**16×128，8 帧，frametime=2**）+ 5 个 `.mcmeta`。
  - 模型统计（真实解析器）：lower 13 元素/56 面/**0 火焰面**；upper 33 元素/157 面/**每档火焰面 8/8/8/8/8**（4 平面 ×2 面，0/90 正交交叉）；item 40 元素/196 面；lower+upper 最大 213 面；Forge 全亮火焰面。
  - 方块/物品 ID `ftdungeonmobs:arena_torch`、两格结构、右键 1→2→3→4→5→1 循环、难度接口、配方/loot table/语言键全部不变；配置文件 SHA 前后一致（`46da1c10…`）。
- 构建校验：Gradle `build`/`check` 通过（离线 Gradle 8.8 + JDK17，`BUILD SUCCESSFUL in 21s`）：
  - `verifyArenaAssets`（v3 静态验证器，真实 Minecraft/Forge `BlockModel.fromStream`）：`ARENA_ASSETS_VERIFIED (v3 static)` —— 8 模型、10 blockstates、lower 0 火焰面、每档 upper 火焰面 8/8/8/8/8、火焰最高点逐档递增、64×64 body、5 档 16×128 8 帧+mcmeta、Forge 全亮、item display、UV 0–16；
  - `verifyAssets`（骷髅线回归，仍由 GeckoLib 渲染）：4 模型 / 218 骨骼 / 17 动画通过。
- JAR 内容核对：arena 20 条目、**无 v2 geo/animations/128²/glowmask/particle/BE 类残留**，`Implementation-Version: 0.5.0`。
- 活动 `mods/` 中本模组仅一份；mods 可见文件数 372 前后不变（全部条目 385；其中 1 个为外部工作线新增的其他模组 legendarymonsters，非本次部署），其他模组抽样 SHA 未变，配置/存档未改。
- **interim → final 记录**：本版最终资源（design-critic P1 修复+火焰条提亮）到位前曾有一次 interim 0.5.0 部署（旧 v3 runtime、jar `d568b081…` 234732B，部署于 00:21:46）；最终版已覆盖部署，interim jar 已备份于 `ft-mod-backups/ftdungeonmobs/20261031162604/`。
- **外部 1 行修复记录**：gptt 在途文件 `ArenaChallengeRun.java` 第 168 行 `SoundEvents.RAID_HORN` → `.get()`（1.20.1 Holder→SoundEvent，修复编译，不改玩法；原文件 SHA `6c163394…` → `b82fc60b…`，副本/diff 存于交付包 `work/v3/integration/external-fix/`）。该在途文件其后被外部工作线整体移除，最终源/jar 不含该特性。
- 机器可读记录：`build/reports/deployment-0.5.0.json`；集成/构建/部署证据：`D:\ft工程文件\竞技场长矛火炬模型_20261031\work\v3\integration\INTEGRATION_REPORT.md`。

启动或重启该 Forge 整合包，在“FT地牢怪物”或“功能方块”创造栏获取“竞技场长矛火炬”，也可执行 `/give @s ftdungeonmobs:arena_torch`。留出两格高度放置，右键上下任一半部即可按 1→2→3→4→5→1 调级（火焰依次白蓝、翠绿、鎏金、紫、猩红）。v3 火焰为**贴图动画（8 帧循环）**，简洁清晰，替代 v2 的骨骼动画与发光层；调级时无 flare 爆燃（回到 0.3.x 风格）。

**回滚**（两级）：
1. 回到 interim 0.5.0（旧 v3 runtime）：退出游戏 → 备份存档 → 删除 `mods/FT地牢怪物-Forge-1.20.1-0.5.0.jar` → 从 `...\ft-mod-backups\ftdungeonmobs\20261031162604\` 拷回 `FT地牢怪物-Forge-1.20.1-0.5.0.jar`（SHA `d568b081…`，234732B）。
2. 回到 0.4.0（v2 GeckoLib 版）：从 `...\ft-mod-backups\ftdungeonmobs\20261031162146\` 拷回 `FT地牢怪物-Forge-1.20.1-0.4.0.jar`（SHA `f8f5c52a…`，240858B）。
配置无需改动（`ftdungeonmobs-common.toml` SHA-256 = `46da1c10…`，部署前后一致）。确认 mods 中本模组只留一版、可见文件数 372。源码回滚：final 前 → `work/v3/integration/backup-refresh-20261101-002524/project/`；interim 前 → `work/v3/integration/backup-20261101-001943/project/`；0.4.0 前 → `work/v2/integration/backup-20261031-231910/project/`。

## 0.4.0 历史部署（v2 GeckoLib 动画方块实体，已被 0.5.0 取代）

- 部署时间：2026-10-31 23:20:00；JAR `mods\FT地牢怪物-Forge-1.20.1-0.4.0.jar`，240858B，SHA-256 `f8f5c52ad481947e5f3ec77de59ef7e3fd01fb304b3d50b36bbc78b7e0146516`；备份（0.3.2 jar `9df7fa4f…` + 配置）：`ft-mod-backups/ftdungeonmobs/20261031151957`。
- 内容：GeckoLib BE 版本（geo 50 骨骼/233 立方体、idle/flare 动画、128×128 五档纹理+glowmask+余烬粒子）；真实 Forge+GeckoLib 解析（`ARENA_ASSETS_V2_VERIFIED`）与部署校验通过；未实机客户端验收。
- 回滚存档：0.4.0 jar 备份于 `ft-mod-backups/ftdungeonmobs/20261031162146/`（本次 0.5.0 部署前备份；同目录含 interim 0.5.0 jar）。
- 机器可读回执：`build/reports/deployment-0.4.0.json`；集成报告：`work/v2/integration/INTEGRATION_REPORT.md`。

## 0.3.2 历史部署（gptt 参考图重制静态模型线，已被 0.4.0/0.5.0 取代）

- 部署时间：2026-10-31 23:17:55，Asia/Shanghai。
- 当前JAR：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\FT地牢怪物-Forge-1.20.1-0.3.2.jar`。
- 构建来源：`D:\ft工程文件\FT-Dungeon-Mobs-Forge-1.20.1\build\libs\ft-dungeon-mobs-forge-1.20.1-0.3.2.jar`，211137字节。
- SHA-256：`9df7fa4ff8e633e41fa63d1a02110919960c9ed0546f4ab0c31fcd43af6f68ea`。构建、暂存、安装文件逐字节哈希一致；JAR元数据和清单均为0.3.2。
- 旧0.3.1 JAR及部署当刻原配置备份：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\ft-mod-backups\ftdungeonmobs\20261031-231754`。旧JAR SHA-256为 `3ddb894df1916be228641a2d8234697c216b733ee7009153bed68b65e56444ef`。
- 配置原样保留，未使用此前快照覆盖用户当前设置；部署前后SHA-256均为 `46da1c106667fa356ecfb3b4e05eae69653d77302299a0003622e60a80a82515`。
- 本模组活动JAR仅一份；部署当刻其余383个mods文件的文件名、大小和修改时间保持一致。未操作其他模组、资源包、KubeJS或玩家存档。独立诊断世界不属于玩家存档。
- 本版采用用户确认继续的参考图候选：129组件编辑模型、五柄尖刃长矛、骷髅石砖台、有孔交叉铁链、暗铁火盆，五色各12帧。修复上下半部UV衔接，Java仅同步火星颜色；难度/伤害/配方/注册ID不变。没有混用历史0.3.1的双层8帧几何。
- 构建、真实Minecraft/GeckoLib资源解析、两组独立Forge GameTest、静态结构、母版/导出、七种Display与动画检查通过，详见 `VERIFICATION.md`。没有启动完整整合包客户端，没有多人联机或FPS实测结论。
- 部署脚本 `tools/deploy-reference.ps1` 默认只预检，`-Deploy` 才会安装；具有运行进程检查、旧版哈希漂移保护、单模组识别、互斥锁、暂存校验和回滚。此脚本专门用于0.3.1→0.3.2，已安装后重复执行会拒绝，不应强行绕过。
- 机器可读回执：`build/reports/deployment-0.3.2.json`；打包校验：`build/reports/reference-0.3.2-package.json`。

## 0.3.2 / 0.3.1 / 0.3.0 / 0.2.0 历史部署记录

- **0.3.2**：gptt 参考图重制静态模型线（构建于 22:41，JAR 211137B，SHA `9df7fa4f…`；已在本目录备份，不再活动）。
- **0.3.1**：2026-10-31 22:19:12 部署；JAR `FT地牢怪物-Forge-1.20.1-0.3.1.jar` 205304B，SHA `3ddb894df1916be228641a2d8234697c216b733ee7009153bed68b65e56444ef`；备份目录 `ft-mod-backups/ftdungeonmobs/20261031-221859`（含 0.3.0 JAR + 原配置）。
- **0.3.0**：2026-10-31 20:53:40 部署；JAR 187220B，SHA `2fa073396d43b4660b39346df435306a0ee30bb26cbacedca854882a474c21fb`；备份目录 `ft-mod-backups/ftdungeonmobs/20261031-205338`。
- **0.2.0**：2026-10-31 19:43:37 部署；JAR 160119B，SHA `8f8a152e974c1fc2ef4e6a8703d88a4f8ff16542e94e53f2fe6c9c1f72b96c9e`；备份目录 `ft-mod-backups/ftdungeonmobs/20261031-194333`（含 0.1.1 JAR）。
- **0.1.1 / 0.1.0**：更早版本，仅在历史备份中保留。

## 游戏中使用

启动或重启该 Forge 整合包，进入创造模式，翻页找到“FT地牢怪物”分类。分类图标为金色副色的骷髅王刷怪蛋；四种蛋顺序为骷髅战士、骷髅弓手、骷髅法师、骷髅王。也可从原版“刷怪蛋”分类或搜索“骷髅”找到。

右键地面生成对应怪物，右键原版刷怪笼设置其实体类型。命令取得方式及伤害接入说明见 `README.md`。没有添加自然生成规则。

属性编辑与地牢接口见 `DIFFICULTY.md`。默认简单 / 普通 / 困难对应一 / 二 / 三档，四、五档可用 `/ftdungeon difficulty set <targets> <tier>` 指定。修改上述 TOML 后执行 `/ftdungeon difficulty reload`，立即重新读取并应用配置。

## 部署范围与验证

- 只替换本模组旧 JAR；未删除、替换或重命名其他模组，未改动其他配置、玩家存档或原始 bbmodel。
- 目标整合包已存在 GeckoLib 4.8.4、Iron's Spells 'n Spellbooks 3.16.3、Curios、Player Animator 和 Iron's Lib 依赖，本次没有重复安装前置（GeckoLib 仍为四套骷髅所需）。
- 已完成：v3 资源静态校验、真实 Minecraft/Forge `BlockModel` 解析（`ARENA_ASSETS_VERIFIED (v3 static)`）、骷髅线 GeckoLib 回归（`VERIFIED: 4 models, 218 bones, 17 animations`）、Gradle 编译与重混淆、JAR 内容核对、部署前后 SHA/文件数/配置核对。
- 未启动完整整合包客户端，不将以上验证等同于游戏内实机画面、FPS、多人联机与其他模组交互均已实测。
- 机器可读记录：`build/reports/deployment-0.5.0.json`。

模型提供者仅标注为“一颗大豆，b站uid355533780”，没有登记为模组代码作者。
