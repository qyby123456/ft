# S6-01-R2：按独立退回意见修订，等待复核

只修第6组第1张。S6-02仍锁定、未开始。独立退回依据为 `D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/REVIEW.md`；没有自行修改该复核档案、写ACCEPTED或通知其他聊天。

## 对退回项的响应

1. **中央不再是并排矩形盒体。** 使用19→19→14→4→1→1→1格的逐层石峰轮廓，最高层一格尖峰；玻璃仅局部观察面，真实熔岩核心仅一源，外侧高低肩峰不用连续玻璃吞没轮廓。台座仍是9格去角低台、两级阶梯，不抬高成机器底座。详见 `qa/stability-boundary.json.peak_layers`。
2. **比例有依据，而非跨区块即合格。** 见 `analysis.md`：原图后拱口约5～6块高、房内跨度约5～6个门高。取净门高5，内跨27，厅跨/门高5.4；外包29×18×29。低台9/27=1/3，台高2格，环行带至少5格；顶底至地坪脚位约15/5=3个门高。图像估尺有不确定性，仍待视觉复核，不自称规模精确还原合格。
3. **后双拱与熔岩瀑重新定比。** 后口3宽5高、单格拱顶；两门脚位(7,2,28)/(21,2,28)。左右瀑热窗高11/9格，约2.2/1.8个门高。保留原图双瀑和双拱逻辑，不添加其他图的迷宫或长条隔脊。
4. **补实际Lodestone截图。** `lodestone/lodestone-exterior.png` 是完整最终NBT的浏览器WebGL查看器截图；`lodestone/lodestone-interior-cutaway.png` 是最终NBT确定性剖切副本的实际Lodestone截图。`screenshots.json` 绑定最终NBT、剖切NBT、浏览器加载端点及每张PNG哈希，`cutaway-provenance.json` 记录移除规则。它们不是软件检查图重命名，也不是Minecraft实机截图。

## 当前实际结构与接口

- 最终权威 `room.nbt`：Java1.20.1、DataVersion3465、gzip、单调色板，29×18×29，15,138条含空气记录。
- SHA-256：`125d206d0b50efb6617265095739cd430501c1cd3884e0bec4d82122c5ed7a66`。
- 原点西北底部(0,0,0)，+X东、+Y上、+Z南；脚位y2、地板y1。
- 北口(14,2,0)，内部安全点(14,2,2)，双南口(7,2,28)/(21,2,28)，均3宽5高。
- 7个目标点：双出口、左右环路(8,2,14)/(20,2,14)、台前/台后(14,2,8)/(14,2,21)、台阶上沿(14,4,11)。全部实际原版碰撞/楼梯路径通过。
- 单个结构，无需拆片。放置顺序仅一步，从相对原点放 `room.nbt`；世界槽位未分配。完整锚点在 `assembly.json`。

## 重跑校验结果

- `qa/static.json`：体积/唯一坐标/全部5格门口净空和支撑/所有目标可达/完整屋顶/链灯连接/所有固体组件与地面连通。
- `qa/native.json`：Minecraft1.20.1原生状态/回读、StructureTemplate加载与保存、全部7个目标点实际碰撞路径；额外逐palette调用原版FireBlock.getBurnOdds和getIgniteOdds，17种状态均为0。
- `qa/stability-boundary.json`：全部熔岩六方向与固体或同种流体相邻、源状态level0、没有外露体积边界熔岩；全体积白名单不含木/叶/羊毛等燃料，没有火/营火。静态稳定边界成立，不等于运行ServerLevel或Forge液体tick。
- `qa/duplicates.json`：重新扫描49份本地其他候选/历史结构，材质无关、平移旋转镜像规范化的精确几何未匹配。仍不能替代最终跨组近似度/路线语义审计。
- `materials.json` 是最终NBT清单；`views/` 和 `preview.png` 仍明确标为软件方块检查图。内部检查预览隐藏高梁/顶盖仅影响视图，不能拿它代替完整NBT。
- `manifest.json` 重算本修订所有交付文件哈希；`registration.json` 状态为待独立复核，accepted=false，next_image_may_start=false。

## 预览入口与复现

完整实际Lodestone：<http://127.0.0.1:5371/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S6-01-R2-exterior>。

剖切实际Lodestone：<http://127.0.0.1:5372/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S6-01-R2-interior>。

`preview.ps1` 默认启动完整视图，`-Cutaway`启动检查副本。先运行check.py的static/native/render/duplicates，运行cutaway.py派生检查副本，再启动两预览、用capture.cjs获取真实浏览器截图，最后check.py package校验端点与文件哈希。查看器使用bundled Lodestone，截图使用bundled Playwright与Edge的headless WebGL，不控制用户游戏或存档。

## 保留与边界

旧候选 `../01-large-chamber/room.nbt` 哈希仍为 `e9fc292970d9ea94671dc462105366dc2c752e61419946935de6aa977456adba`，未覆盖。更早封存目录也未改。

当前仅申请复核，不声称已经ACCEPTED或规模/造型全部合格。没有Forge世界放置、外部世界易燃邻居检查、旋转拼接、tick稳定性或战斗实测；没有部署、写存档、mods或配置。外部接入必须先检查完整29×18×29体积和外部接口邻域，冲突就拒绝，不能盲目清空。只留在本聊天待验收，不放行或预生成S6-02。
