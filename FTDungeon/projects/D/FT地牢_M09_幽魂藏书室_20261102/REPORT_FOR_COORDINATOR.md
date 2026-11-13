# M09 幽魂藏书室 · 独立交接报告

## 结果

静态 QA 与真实 Forge 四向放置/正常邻居更新/120 tick 稳定性 QA 均通过；尚未接入或部署。

- 最终模板 SHA-256：`712b28249cb70489b9d79e7dd22071c01b4bcf973eca0ce5ead4fb148b5ec0ab`。
- 参考图 SHA-256：`4bafda1f3ec74ed1aaa0daa66d58a5e0f3e1267b1287d8635331143df256f16b`。
- 实际 NBT 软件预览 SHA-256：`8d7710423f084a1367d5f226129468f4e1074deae3988f07447b7656ae5af5b8`。
- 交付：`out/M09/room.nbt`、`room.json`、`preview.png`、`qa.json`。
- 尺寸 21×12×21，5292 条显式体积记录；北入口 `[10,1,0]`，内安全点 `[10,1,2]`。

## 实际验证

- 静态 20 项检查全部通过；NBT 单调色板、DataVersion 3465、完整地板/441 格实体屋顶和唯一北口。
- 原版注册表与状态属性真实解码；StructureTemplate 原生加载和保存往返通过。
- NONE / CLOCKWISE_90 / CLOCKWISE_180 / COUNTERCLOCKWISE_90 四向真实放置，每次放置后主动执行正常邻居更新，再等待 120 个服务端 tick。
- 每快照核对 21168 条四向记录，三次快照共 63504 条；原生 canSurvive 支撑、141 格书架直接承托、三道横梁/纵梁/柱及灯具均通过。
- 原生 0.6×1.8 玩家体积和边间扫掠碰撞检查覆盖入口、安全点、书墙、残骸侧、书桌两侧和椅后回路。
- 每方向两只空木桶，共 54 个实际库存格逐格为空；讲台无书、无 LootTable，无实体/掉落物。
- 测试夹具从已验证空气体积放置，测试后恢复本次夹具；不清理其它线程或玩家数据。

| 旋转 | 原生可达脚位 | 非空气生存检查/快照 | 空库存格 | 120 tick |
| --- | ---: | ---: | ---: | --- |
| NONE | 292 | 2122 | 54 | PASS |
| CLOCKWISE_90 | 292 | 2122 | 54 | PASS |
| CLOCKWISE_180 | 292 | 2122 | 54 | PASS |
| COUNTERCLOCKWISE_90 | 292 | 2122 | 54 | PASS |

## 失败留证与修正

首轮原生测试拒绝了不正确的铁栏 `south=false`。相邻青色玻璃需要 `south=true`，现已在生成源修正并用新独立世界全量重测。原失败模板/元数据/JSON 与 attempt-1 日志保留，不将失败轮次记为通过。

## 证据与边界

- `qa/static-qa.json`、`qa/native-registry-qa.json`、`qa/native-world-qa.json`、`qa/preview-provenance.json`。
- 最终原生进程日志：`evidence/forge-gametest-attempt-2.log`，SHA-256 `8f0f680e7bd4cb2fcca92f7e61939a32f8ceb35e4b5b99e09f4433d4067eafc3`。
- `evidence/preview-full-envelope.png`：完整屋顶外壳；`evidence/plan-dry-routes.png`：真实 NBT 路径。
- 交付清单与压缩包指纹：`delivery-manifest.json`、`package-receipt.json`；ZIP 不包含开发缓存与测试世界。
- 未修改核心/正式 mods/正式存档/并行房间，未调用付费生图；没有执行 FT 连接路线、战斗波次或奖励逻辑。
- 未验完整整合包客户端/多人游玩/FPS；预览是原生 AABB 软件渲染而非实机截图，透明模型与方块实体动画的显示有限。
- 日志中的单次四房模板施工/全量核验触发约 12.5 秒 behind 警告；不将此隔离 QA 宣称为性能达标或无卡顿。
