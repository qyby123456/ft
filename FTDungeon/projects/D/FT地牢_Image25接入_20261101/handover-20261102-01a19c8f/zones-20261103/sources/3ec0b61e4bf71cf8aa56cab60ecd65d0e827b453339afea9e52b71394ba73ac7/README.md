# 地牢分区房间概念图 · 7 类 × 5 = 35 张

- **需求**：拐角房间 / 简单跑酷通道 / 大厅 / 交通枢纽 / 矿井 / 废弃结构 / 废弃房间，每类 5 张，多线程完成。
- **执行**：Agent Teams 3 子线程并行（zone-a 拐角+跑酷、zone-b 大厅+枢纽、zone-c 矿井+废弃结构）+ 主线程自做废弃房间并汇总。
- **链路**：gen.mjs --url（CLI b64 故障）；每张存在性检查、≤2 次尝试；仅写各自 out 子目录。
- **统一规格**：Minecraft 方块风、3/4 等距剖视、1536×1024、quality high、n=1；布局逻辑自检（柱独立落地、家具留走道、贴墙/吊挂正确、无悬浮穿插）。

![35 张总览](out/zone-batch-overview.png)

## 目录
- [拐角房间](out/corners/)（CORNER ROOM）：5 张
- [跑酷通道](out/parkour/)（PARKOUR PASSAGE）：5 张
- [大厅](out/halls/)（GREAT HALL）：5 张
- [交通枢纽](out/hub/)（TRANSPORT HUB）：5 张
- [矿井](out/mines/)（MINE）：5 张
- [废弃结构](out/ruins/)（ABANDONED STRUCTURE）：5 张
- [废弃房间](out/abandoned/)（ABANDONED ROOM）：5 张

## 校验
- 35/35 PNG、1536×1024、RGB 可解码；sha256/bytes 清单见 docs/verify.json、docs/manifest.json。
- 说明：AI 概念图非实机截图；本批最终 35/35 成功。