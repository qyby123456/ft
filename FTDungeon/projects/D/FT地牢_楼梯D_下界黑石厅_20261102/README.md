# D · 下界黑石楼梯厅 · 独立候选交付

2026-11-02 / Minecraft Java 1.20.1 / Forge 47.4.10 / Java 17。

## 结果

- 已完成 `out/room.nbt`、`out/room.json`、`out/preview.png`、`out/qa.json`。
- 临时 ID：`STAIR_D_NETHER_HALL`。**未注册槽位、未部署、未改 FT 核心、未触碰正式玩家存档。**
- 29×23×33，22,011 条完整体积记录（含显式空气），gzip 单调色板，DataVersion 3465。
- 黑石金饰九格宽大台阶、平台、七格宽上行第二梯段、五格宽下行梯段；六根金带玄武岩柱、灵魂吊灯、暖色灯笼火盆、红旗、下层吊灯。
- 真实熔岩源保留：左右各8个，黑石底/侧/端与橙色玻璃顶六面全密封，外加矮墙。不是岩浆块冒充熔岩。

![实际 NBT 东北剖切预览](out/preview.png)

这是**原版模型/材质软件渲染**，不是 AI 概念图、客户端实机截图或 FPS 证据；红旗简化为同色色块，玻璃/光照采用软件近似。最终 NBT 有完整屋顶与外壳，仅预览隐藏近侧墙、屋顶和部分显示用断面。

## 原图与接入规范

- 原图：`D:\ft工程文件\image2.5\dungeon-stair-rooms-20261102\out\stair-nether-hall.png`，1536×1024。
- 原图 SHA-256：`9d5461ff3fa0058ba614ab44a1d24f55c373f0b321fa35cab6eafc2e8a7a0736`，已实读核验并复制到 `reference/design.png`。
- 原说明、manifest 和 FT 接入契约副本在 `reference/`。本次只使用已有设计，没有重新生图或调用收费 API。
- 遵守独立工程边界：只交付本目录；不写 `incoming/`、共享队列、FT 核心、其它 ID、实例 `mods/` 或 `saves/`。Gradle 正常使用系统依赖缓存。

## 相对接口与高差

坐标为模板相对整数 XYZ，**玩家脚部位置**，不是脚下地板。

| 接口 | 玩家脚位 | 朝向 | 净开口 | 与主层高差 |
| --- | --- | --- | --- | --- |
| 北主入口 | (14,7,0) | north | 3宽×3高 | 0 |
| 南上层出口 | (14,17,32) | south | 3宽×3高 | +10 |
| 南下层出口 | (22,1,32) | south | 3宽×3高 | -6 |

北入口中置；三处入口脚下为完整方块地板，无门，无石饰突入。主层脚位 Y=7 → 六格上行主梯 → 平台 Y=13 → 四格上行梯 → 上层 Y=17。东侧六格下行梯 → 下层 Y=1，直达独立南口。上/下梯均真实可双向通行，不是外观假台阶。

- 主梯：X=8…16 / Z=8…13 / 9格宽；上梯：X=10…16 / Z=23…26 / 7格宽。
- 下梯：X=20…24 / Z=8…13 / 5格宽，高侧朝北；下层走廊 X=20…24 / Z=14…31，吊灯下仍保留玩家通行净空。
- 安全点：(14,7,4)、(12,13,18)、(14,17,30)、(22,1,29)。
- 112个台阶均由完整砌体直接支撑；梯段两侧、平台边缘和下行洞口使用连续实体黑石/金顶护栏。
- 概念图火盆改为受支撑的暖色灯笼底座，无裸火、可燃家具、奖励、刷怪笼或预置实体。装饰旗帜为唯一方块实体，无库存或战利品。

**接入注意**：协调者须按高差分别连接三个接口，做全体积占用/实体/流体拒绝、世界边界和幂等验证。完整显式空气不是允许清空玩家建筑的授权。此房间本身不证明任何 FT 大厅路线已经接通。

## 真实验收证据

| 证据 | 内容 | 结果 |
| --- | --- | --- |
| `qa/static.json` | 完整体积、白名单、壳体、三处接口、基础屋顶、熔岩组件边界、支撑、每条路线及全宽台阶、四安全点 | 292项通过 |
| `qa/static.json` 负例 | 缺屋顶、开熔岩顶、堵入口、断吊链、缺台阶、缺护栏、额外熔岩 | 7项全部拒绝 |
| `qa/native.json` | Forge 原生注册表/属性、NBT load/save、四向实际放置，逐记录状态/生存条件、支撑、接口、屋顶与外壳、液体边界、碰撞路径 | PASS |
| `qa/native.json` 稳定性 | 强制加载夹具区块；逐方块邻居更新；16个熔岩源/方向主动安排 fluid tick；继续200 tick后重验 | 88,044条记录，0状态漂移、0外溢、0掉落实体 |
| `qa/native.json` 负例 | 断链、开熔岩顶、堵入口、缺台阶、缺屋顶，每项×四向 | 20项全部拒绝 |
| `qa/native-placed-*.nbt` | 四向200 tick之后原生从实际世界回采的完整结构 | 4份 |
| `qa/capture-verification.json` | 独立 Python 重读四份回采，按旋转变换逐状态对照最终交付 NBT | 88,044条一致 |
| `qa/reproducibility.json` | 再跑生成器并核对最终 NBT 指纹不变 | 详见机器报告 |
| `qa/forge-gametest.log` | `STAIR_D_NATIVE_PASS`、`All 1 required tests passed`、`BUILD SUCCESSFUL in 5m 49s` | 原始日志保留 |

碰撞验证使用原生碰撞形状、0.6×1.8玩家体积、0.25格采样，相邻高差不得超过0.6；三条跨层路线和全部9+7+5列台阶正反双向逐段检查，不是只检查端点。Native 负例在同一 tick 内恢复，随后再执行邻居/流体更新和200 tick复验。

测试只在本工程 `qa/forge-run/world` 的受 `GameTestServer` 类型保护的无客户端隔离世界运行。日志含首次启动自动补默认 `server.properties`、Forge联网版本检查超时及批量放置时的单次 `Can't keep up` 警告；最终 GameTest 与 Gradle 成功。这不构成整合包客户端性能、多人测试或部署证据。

## 预览与复核

- `preview/four-views.png`：四向软件剖切拼图。
- `preview/exterior.png`：最终完整屋顶外壳。
- `preview/native-world-capture.png`：从 Forge 200 tick 后回采 NBT 渲染，仍不是客户端截图。
- `preview/lower-profile.png`：直接读取 X=22 的下行纵剖，查看半格台阶、地下走廊与支撑。
- `preview/lava-enclosure.png`：读取 Z=8 的熔岩封闭横剖。
- `preview/logic-plan.png`：三层相对坐标平面与安全点。

## 复现

Python 运行环境：`C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`；Pillow 13.0.0。

依次运行 `tools/build_room.py`、`tools/qa_static.py`、`tools/run_native.py`、`tools/verify_captures.py`、`tools/render_room.py`、`tools/finalize_delivery.py`。Forge 需要已缓存 Gradle 8.8、Java 17、Forge 1.20.1-47.4.10，原生验收启动器使用离线 Gradle。软件预览读取本机缓存的原版1.20.1 `client.jar`，不打包其资源。

`tools/nbtio.py` 和 `tools/texture_engine.py` 基于已有独立房间的 NBT/软件材质解析助手复制到本目录使用；没有修改来源工程。测试 mod 仅用于验收，**不要把 build 目录下的 QA mod 安装到正式实例**。本次不提供正式部署 JAR。
