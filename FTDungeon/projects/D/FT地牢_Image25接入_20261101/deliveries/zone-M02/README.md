# zone-M02 · 淹水矿井（独立分区候选，未部署）

本房间依据 dungeon-zone-batch-20261103 的 mines / M02 淹水矿井制作，ID 为 **zone-M02**。它与 **corridor-M02 浸水档案长廊** 是不同设计、不同 NBT、不同交付目录，不能替换或复用后者的登记。

## 四件套与坐标

- room.nbt：Minecraft Java 1.20.1，DataVersion 3465，gzip 压缩，单一调色板；Minecraft Builder skill / Lodestone 0.6.0 生成。
- room.json：真实尺寸 **25 × 15 × 25**，9,375 条显式坐标记录（包含空气），46 个调色板状态。
- 北入口玩家脚位 **[12,4,0]**，内部安全点 **[12,4,20]**，朝向 north；入口 X=11..13、Y=4..6 三宽三高，下方 Y=3 完整石地板。
- preview.png：从实际成品 NBT 渲染的内部剖视。qa.json：静态、原生放置、液体、四向视图与保护证据汇总。

所有坐标都是模板相对 XYZ。接入方须将外部路线脚位对齐原点 Y+4；此次没有指定正式槽位，没有接入地牢，也没有验证大厅到入口的连接路线。NBT 显式空气不是对玩家世界清空或覆盖的授权。

## 建筑与安全

- 原图的积水矿井、木井架、架空轨道栈道、沉水旧铁轨、木箱、倾斜矿车轮廓、滴水石、矿脉与吊灯均有对应。矿车和木箱是无实体、无库存的方块造景，矿车不能推行或乘坐；原版完整方块阶梯撑替代图中连续斜撑。
- 四组 2×2 井架从 Y=0 石底贯穿到 Y=12 梁架并承接 Y=13/14 双层完整石顶；井架距离边墙至少三格。侧柱、交叉梁、12 格灯链和 4 盏吊灯都有实体支撑。
- 水池位于 Y=1/2：**886 格水方块 + 15 格含水铁轨 = 901 格含水位置**，所有水源有底和四周密封。栈道地板 Y=3，行走脚位 Y=4，保持三格净空。两侧支栈道封端，所有可走的木板边缘设护栏或同高承接地板；入口可往返安全平台和左右支栈道。
- 85 格护栏、209 格木板、529 格云杉原木；沉水箱堆均落地。矿脉在封闭墙面/池底，海晶灯嵌入池底照明，无熔岩、明火、危险通行块或库存奖励。
- 最终 NBT 保留全顶、全墙与池底。四向内部图及俯视仅剖切渲染副本，外部四向图不剖切。

## 原图与最终指纹

原图：D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/mines/M02-mines-flooded-mine-shaft.png。

原图 SHA-256：099696aab9cb43fce0acb8e3b79d54087cf1876b87204aa1d110822dec505965。

最终 room.nbt SHA-256：e5cf0a5562d62ffd06d5467474f0a3cd548d4a00b5a40b53c15a1ab59f57f505。

已核对该批 README、manifest.json 和 verify.json 的 mines/M02 条目。来源文件保持只读，没有付费再生图。完整四件套、原生回捕和脚本哈希见 delivery-manifest.json；清单不包含自身哈希。

## 真实 QA

- qa/static.json：16 项静态检查，包括原图指纹、独立 ID、坐标、禁用方块、无库存/实体、双层屋顶、三宽干燥路线、两层水池密封、落地井架、吊灯链、轨道/滴水石支撑和全部可走木板临水边缘防护。
- qa/forge-native.json、qa/forge-zone-m02-final-02.log：**Minecraft 1.20.1 / Forge 47.4.10 / Java 17.0.8** 独立 GameTestServer 实际放置 0/90/180/270°。每方向即时与 200 tick 后各对比 9,375 条记录，共 **75,000 次**；显式安排 **3,604 个流体刻**，randomTickSpeed=3、doFireTick=true。
- 四方向每次均保持 901 格含水位置、15 格含水铁轨、18 格干轨；每方向核验 3,018 个外壳位置，**漏水 0、漂移 0、掉落实体 0、支撑失败 0**。实际碰撞检测与 BFS 证实北口到安全点及左右支路可往返。
- 入口堵塞、水池边界缺口、吊链缺失、井架柱缺口、屋顶缺口五类负对照，每类四方向均正确拒绝。负对照在稳定阶段开始前恢复。
- qa/native-roundtrip.nbt 和 qa/native-placed-*.nbt：原生加载/保存回环与四方向真实世界回捕 NBT，哈希载于 qa/forge-native.json。
- views/：四向外观、四向内部剖视、俯视和联系表；qa/render-manifest.json 逐图绑定最终 NBT 指纹。qa/visual-review.json 记录已审视的实际视图及判断。
- 初版通过原生检查后，视图复核补齐了左右支栈道端栏与入口侧栏，并为最终复测加入显式流体刻。qa/revisions/initial-13bc0b2/ 仅保留旧指纹历史，不能作为最终成品证据。
- qa/protected-inputs-final.json：源批说明/清单/校验和 corridor-M02 四件套，七项首尾 SHA-256 完全一致。所有交付写入均局限 zone-M02。

这些是软件渲染和隔离 Forge 服务端证据，不是完整整合包客户端实玩、多人或长期压力测试。不含 FT 核心注册、正式槽位、幂等/占用拒绝或大厅路线验收。未修改正式 mods、核心源码、其它房间或玩家存档；不直接部署。

## 复现

在此目录运行，Builder 使用已安装本地依赖，不需要 Skills CLI：

    node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs ./tools/build.mjs --out-dir . --name room --data-version 3465
    node ./tools/static-qa.mjs
    ./tools/run-native.ps1 -Run zone-m02-unique-run-name
    node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs --nbt ./room.nbt --port 5342 --no-open

预览服务器启动后，在另一个终端执行 node ./tools/render-views.mjs。最终证据装配使用 Codex 捆绑 Python 执行 tools/finalize.py；重跑前须同步所用日志名与视图审查的最终指纹，禁止将历史指纹证据混入新版。
