# N01 独立房间交付报告

- 状态：PASS / NOT_DEPLOYED；交付根目录 `D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-N01`。
- `room.nbt` SHA-256：`e605ff944681a414ab25c113388e51863a37f987417456bd0244d74315737f63`。
- 参考 PNG SHA-256：`fc72ab0f0f5650c07a959cf31a1de9bbb263af60ca5d7356d6255d35fac954fc`，与同批 manifest/verify 核对一致。
- 尺寸25×12×25；地面Y=1、脚点Y=2；四接口5宽×5高。N=(12,2,0)、E=(24,2,12)、S=(12,2,24)、W=(0,2,12)。
- 中心安全脚点(12,2,9)，其块中心(12.5,2,9.5)可作候选传送点；3×3安全区和三格环路均五格净空，中心灯台不能作为落点。
- 静态QA PASS；四向真实NBT内外预览PASS；隔离MC1.20.1/Forge47.4.10 GameTest PASS：4旋转、每阶段30,000条状态、400tick不变、48条有向接口路径/阶段；7项故障注入全部正确拒绝。
- 证据：`qa/static.json`、`qa/forge-native.json`（每条路径坐标）、`qa/forge-run.log`、`qa/views.json`、`preview/four-interior.png`、`preview/four-exterior.png`、`preview/interface-plan.png`。
- 已保留完整屋顶，剖切只用于预览；12个涂蜡纯文字牌为唯一方块实体，0普通实体、0容器、0奖励/刷怪逻辑。所有文字为模板局部方向，随原点旋转，世界接口见原生报告。
- 所有新增文件限定本房间目录；未修改正式模组、注册表、存档、其他交付与共享总清单。没有发送跨聊天消息。
- 局限：隔离原生验收不等于完整整合包客户端/多人/FPS测试；未来含显式空气放置必须先验证完整旋转体积和周界占用。
