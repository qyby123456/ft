# M14 独立交付回执

- 交付根目录：`D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-M14`
- 设计编号 M14；本批唯一模板标识 corridor-M14，本地预留排队号 54（只读核对现有 slots.json，不是世界位置），未写共享账本。生产位置及接入由统一负责人确认。
- 原图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M14-fungal-cave-aged.png`
- 原图 SHA-256：`aeea1468c5e12912daa7b14bded792897778cd3331cca76999c302646e11ea31`
- 成品 SHA-256：`186bb72489d05fdf57c68ac68b84320a43af61d6b7842702b2b8585e2288d1ca`
- 规格 15×12×41，2.5625 区块中型直廊；北脚部 `[7,2,0]`，安全脚部 `[7,2,4]`，南脚部 `[7,2,40]`；主路 5×4 净空，两端 3×5。
- `room.nbt`、`room.json`、`preview.png`、`qa.json` 齐全；13 项静态、北南西东四向内部+外壳、Forge 原生四向放置及 400 tick 稳定性全部通过。
- Forge 47.4.10 / MC 1.20.1，4×7,380 条状态逐格立即/稳定后复核；每向 4,283 次原生生存条件检查、316 干燥连通脚位、2,854 外围空气格。
- 成功日志 `evidence/forge-gametest-m14-attempt2-20261103.log`；退出码 0，含 M14_NATIVE_GAME_TEST_PASS / All 1 required tests passed / BUILD SUCCESSFUL。首轮空模板缺失失败证据保留。
- 视觉证据：`preview/four-views/m14-final/corridor-M14/interior-four-views.png`、`exterior-four-views.png` 与根目录 `preview.png`；同目录 NBT 快照与最终原件哈希一致。
- 设计取舍：以菌光体+完整橙玻璃安全还原熔岩辉光；青菌为海晶灯+诡异菌块组合；采用稳定灰化土/盆栽，未放传播菌丝或裸露液体/火焰。
- 已完成本线程自验，可进入协调方独立复验/唯一槽位确认；不把自验冒充协调方已收件或已接入。
- 未部署、不改正式存档、不写其它房间、核心、生产 JAR、NeoForge、KubeJS、资源包或配置；测试世界仅在本目录 `native-test/runs`。
- 这不是整合包客户端、多人或 FPS 实机验收。全部直接交付文件指纹见 `delivery-manifest.json`。
