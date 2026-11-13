# FT地牢挑战 · Forge 1.20.1

独立联动模块 `ftdungeonchallenges`，版本 `1.0.0`。需要现有 **FT地牢怪物 0.5.x** 及其既有依赖。本模块只增加挑战逻辑、语言与奖励表，不替换怪物主 JAR，不新增、恢复或打包已经放弃的模型。

## 玩家用法

1. 在有安全地面的竞技场放置现有双格长矛火炬。
2. 普通右键选择 **1～5档**；五档火焰颜色沿用主模组。
3. **主手、副手都留空，潜行右键火炬**，启动挑战。默认准备15秒。
4. 队友在首波开始前，用相同操作加入同一火炬，默认最多4人。不会把路过的玩家自动算作队友。
5. 清掉本波全部敌人，休息8秒后下一波。顶端进度条显示难度、波次、待清敌人数和剩余时间。
6. 最终波通关后，向仍在范围内、存活且未被淘汰的参与者发放该档奖励与经验。

战斗期间普通右键只看进度，不再切档。开战时固定档位、人数上限、波次参数与竞技场半径，防止开高档后降档领奖。普通怪物仍按原机制跟随火炬；挑战怪物通过原 `DungeonDifficultyApi.setTier` 固定档位，生命、攻击、物理护甲、铁魔法魔抗、法强全部复用原有配置和原生伤害结算，没有第二次伤害乘算。

外部 API、管理员 `/setblock` 或其他模组如果改变本场火炬的档位/结构，本场在下一次检测中止，不发通关奖。本模块不修改主模组 Java API。管理员仍可主动改实体属性，故正式挑战中应避免使用批量属性管理命令。

## 默认五档

| 档位 | 波数 | 首波基础敌人数（单人） | 法师权重 | 末波首领 |
| --- | ---: | ---: | ---: | --- |
| 1 苍蓝 | 3 | 4 | 0 | 无 |
| 2 翠绿 | 4 | 5 | 1 | 骷髅王 |
| 3 鎏金 | 5 | 6 | 2 | 骷髅王 |
| 4 紫焰 | 6 | 7 | 3 | 骷髅王 |
| 5 猩红 | 7 | 8 | 4 | 骷髅王 |

战士/弓手默认权重4/2；各档法师按上表加入随机权重。后续每波增加2只，每多一名已报名队友增加2只，单波默认上限24只。末波以一只骷髅王替代普通敌人，不额外突破数量上限。所有值可改。

## 胜负、退出与安全

- 默认每波最多300秒；找不到足够安全刷怪位，30秒后判负，不把未刷出的怪算击杀。
- 阵亡即本场淘汰；复活不能重新参战或领取通关奖。全队阵亡直接判负。
- 全队离场、断线、旁观或换维度，最多等待30秒；期间不继续刷怪，超时中止。已有波次倒计时不重置。
- 发起者可以 `/ftchallenge stop` 主动退出；其他队员不能替全队取消，管理员除外。
- 火炬被破坏/卸载、敌人被移除/卸载、敌人超出半径外8格、切和平、关服、禁用挑战均中止并清理，无通关奖。
- 只承认真正完成且未被其他模组取消的死亡事件；卸载、`discard`、刷怪失败不能伪造胜利。
- 不强制加载区块、不改天气/时间、不破坏地形。敌人仍使用主模组原有战斗 AI；未报名的附近玩家仍可能遭到怪物攻击。
- 挑战范围沿用 `ftdungeonmobs-common.toml` 的 `arenaTorch.radius`，默认32格球形范围。活动挑战范围不能重叠；全服跨维度默认最多3场。
- 火炬与每个参与者各有180秒冷却，结束/失败/取消同样生效，换火炬不能绕过个人冷却。存于主世界 `data/ftdungeonchallenges_cooldowns.dat`，不靠系统时钟；重启不会清零。进行中每10秒刷新冷却保底。
- **不恢复中断波次**：正常关服主动中止；崩溃重启后，带本模块挑战标记但无有效会话的敌人会在加载时清理。不会因重启额外发奖。未加载的残留敌人在其区块下次加载时清理，不为清理强制加载区块。

## 命令与权限

普通玩家：

```mcfunction
/ftchallenge
/ftchallenge start
/ftchallenge join
/ftchallenge status
/ftchallenge stop
/ftchallenge start <x> <y> <z>
```

`start/join/status/stop` 都支持可选坐标；不填时使用视线6格内的方块。参与者须在竞技场范围内，开战/加入仍检查原版交互保护。右键入口尊重已被其他模组取消的交互事件；命令入口不会自动接入每一种领地模组的私有权限 API，需由服务器权限插件限制相应命令。

管理员（权限等级2）：

```mcfunction
/ftchallenge list
/ftchallenge cancel <x> <y> <z>
/ftchallenge reload
```

原有 `/ftdungeon difficulty ...` 和动画预览命令完全保留，未降低其权限。

## 配置与奖励数据包

独立配置：`config/ftdungeonmobs-challenges.toml`，首次加载自动生成。完整示例见 `config-examples/ftdungeonmobs-challenges.toml`。

保存后运行 `/ftchallenge reload`。进行中的挑战保留开战快照，新配置用于新挑战；`enabled=false` 是即时总开关，会在下一次每秒检测时中止现有挑战。原五项属性倍率仍在主模组配置，通过原 `/ftdungeon difficulty reload` 重载。

奖励可用数据包覆盖，五个路径分别为：

```text
data/ftdungeonchallenges/loot_tables/challenges/tier_1.json
data/ftdungeonchallenges/loot_tables/challenges/tier_2.json
data/ftdungeonchallenges/loot_tables/challenges/tier_3.json
data/ftdungeonchallenges/loot_tables/challenges/tier_4.json
data/ftdungeonchallenges/loot_tables/challenges/tier_5.json
```

使用 `minecraft:gift` 上下文，提供 `this_entity`（领奖玩家）和 `origin`。可改为整合包内货币/票券/装备。默认1～2档铁锭+绿宝石、3档金锭+绿宝石、4～5档钻石+绿宝石；经验为每档10点。物品优先入背包，放不下的余量掉落在领奖者脚边。默认奖励只是初始平衡，不保证适合现有经济。

## Java接口与事件

所有接口只在逻辑服务端主线程调用；异步调用方应使用 `server.execute(...)`。

```java
import net.farmingtales.ftdungeonchallenges.ArenaChallengeApi;

ArenaChallengeApi.Snapshot started = ArenaChallengeApi.start(serverPlayer, torchPosition);
ArenaChallengeApi.Snapshot joined = ArenaChallengeApi.join(teammate, torchPosition);
ArenaChallengeApi.find(serverLevel, torchPosition);
ArenaChallengeApi.active(server);
ArenaChallengeApi.isLocked(serverLevel, torchPosition);
ArenaChallengeApi.stop(serverPlayer, torchPosition);
ArenaChallengeApi.cancel(serverLevel, torchPosition);
```

`Snapshot` 是不可变记录，包含会话UUID、维度、火炬位置、固定档位、阶段、波次、存活/待刷数量、倒计时和参与者UUID列表。`start/join` 拒绝条件抛 `ChallengeException`，带可翻译的 `component()`。上、下半部都能定位同一场挑战。

Forge事件总线发送 `ArenaChallengeEvent.Started`、`WaveStarted`、`Finished`；都带 `level()` 和 `challenge()`，结束事件另有 `reason()`。它们是通知事件，不可取消；任务/成就/商店联动可监听 `Finished` 且 `reason()==VICTORY`。每场结束只通知一次。额外奖励监听器应自行持久化去重，不能把“准备结束”当成通关。

## 构建与验证

JDK17、Forge47.4.10、官方1.20.1映射、Gradle8.8。工作区 `libs` 已配好编译依赖；源码导出不捆绑其他模组JAR，外部依赖清单见 `libs/README.md`。

```powershell
.\BUILD.ps1 -Task build -Offline
.\BUILD.ps1 -Task runGameTestServer -Offline
```

GameTest仅使用本工程 `run/challenge-verification` 诊断世界，不读取玩家正式存档。测试类和结构模板不进入正式JAR。具体验证结果见 `VERIFICATION.md`。构建检查还需要Node.js运行资源校验脚本。没有用静态检查替代完整整合包客户端、联机网络或帧率测试。

已以额外JAR部署到当前实例，原怪物主模块和难度配置保持不变；重启后加载。安装位置、备份和撤除说明见 `DEPLOYMENT.md`。

## 参考来源

用户给出的桌面两个JAR在本次检查时不存在；因此参考 **Majrusz's Progressive Difficulty 官方1.9.10标签源码**中亡灵军团的“准备→波次→休整→胜负”、参与者范围与奖励通知结构，并重新实现为 Forge 联动模块。

- 官方仓库：https://github.com/Majrusz/MajruszsProgressiveDifficultyMod/tree/1.9.10
- 标签提交：`1cdcabe72235d090c5ee9555b7c372836a51ee0b`
- 不安装 Fabric参考JAR或 NeoForge参考库，不复制其 Java 实现、图像、音效或其他素材，不引入 Majrusz 依赖。
- 本模块没有模型提供者署名需求；既有 FT 模型署名仍由原模组 `CREDITS.md` 保留，不把模型提供者写成此挑战代码作者。
