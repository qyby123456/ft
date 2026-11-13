# S7R2-08 天井晶泉菌庭

状态：AWAITING_INDEPENDENT_REGISTRATION_ACCEPTANCE。只提交08，等待独立复核，不制作09，不部署。

正式候选：incoming/S7R2-08/room.nbt，Java 1.20.1 DataVersion 3465，33×23×33，gzip结构NBT。SHA256：d0542dd4ce41ab92118ab0035d99572adfba20625e6f486242ff4b09e682f9d1。

依据第7组第8张courtyard原图重新编写recipe.mjs；未复用先前房间几何。中央多层紫晶尖塔、环状水盆、高位三边石沿与木栏杆、分层巨菇，顶部17×17天井。y22有289格原始空气和800格实体顶缘，完整源低位图可验证天井并非裁切产生。水源100格（环盆84格，另4×4浅池16格），原版静态水，不声称流动泉效果或紫色动态光。

北[9,2,0]、东[32,2,9]两个3×4地面口；脚高y2。16个目标通过原生0.6×1.8玩家体积、半格平面网格、双向边扫掠碰撞检查，2873个可达节点。上层沿台y10、栅栏y11，71个栅栏底部原生坚固支撑通过；原图未见楼梯，未编造地面至上层路径，上层玩家路线NOT_RUN。

原生模板加载、方块注册与标签、87个脆弱方块静态生存检查通过。静态模型亮度假设15；不是实际世界光传播。34个既有模板作四旋转材质无关几何排重，无完全匹配。没有方块实体、战利品或容器。

4张实际Lodestone GPU图已检查：exterior、skylight-detail、courtyard-eye展示完整正式源；interior展示仅将指定方块改成air的预览派生。views/derivative.json记录逐格裁切与哈希。派生裁西/南墙和顶缘，可能令西墙高藤在派生中悬空，正式源有墙面支撑且原生canSurvive通过。courtyard-eye仅在浏览器响应调整相机，未修改NBT或技能文件。软件方块投影图另有明确标签。

装配必须预留完整33×23×33包围盒，包括显式air；天井上方外部地层未知。永久边界：S7-06的L形外接方盒中，L外仍是显式air，原版放置会清除相应地形；需完整包围盒预留，或另行实现并验证跳过空象限的放置器。

世界邻居更新、流体刻、随机刻、实际交互、玩法、四旋转世界放置、运行时注册和部署均NOT_RUN。原图有机轮廓、雾、光束和青色发光生长物仅作原版方块近似。独立复核尚未完成。

复核入口：manifest.json、REGISTRATION_PENDING.json、connections.json、rotated-connectors.json、qa/latest-native-run.json、qa/visual-review.json、qa/skylight.json、SHA256SUMS.json。ZIP包含候选、原配方、验证工具、实景、原图来源/哈希、前置07接受快照与完整证据；ZIP逐项哈希及CRC回读验证。

执行Preview.ps1开启完整源Lodestone预览，默认http://localhost:5384。不要将views内预览派生注册为候选。
