# 中等地牢多风格原生回归

C 仅修改 build.gradle 的测试源集配置并新增 src/eclipseStyleTest；没有修改 src/main Java、版本号，没有构建、部署或写正式实例。宿主版本号变更由宿主维护。

独立 Gradle sourceSet 为 eclipseStyleTest，源码目录 src/eclipseStyleTest/java，资源目录 src/eclipseStyleTest/resources，主产物只引用 main。指定 `-PeclipseStyleOnly=true` 后 runGameTestServer 仅加载该测试源集、仅启用 namespace fteclipsestylesqa；优先于 styleOnly，不复制 moss obsidian fixture，也不编译 gateTest/styleTest/mossTest 旧测试。仍可用 gateTestRoot/gateTestDirectory 指向宿主全新隔离服务端目录。原命令不带 eclipseStyleOnly 时保留原逻辑。

接口稳定后由宿主串行运行，例如：

```powershell
./gradlew.bat runGameTestServer -PeclipseStyleOnly=true -PgateTestDirectory=eclipse-styles-r1
```

C 没有执行上述命令。宿主已通知主源码 compileJava 通过；本测试采用当前已落盘的 style/styleFingerprint 接口，现可由宿主串行编译执行。两个 required GameTest 使用独立 empty.nbt 模板，不调用旧测试 helper。

1. distinctPlansStoreReloadAndLegacyClockwork：四个 fteclipsegen:clockwork/forest/desert/frost 同 seed/anchor 的 ID 必须不同，计划重复创建完全确定；明确验证 Plan.style/styleFingerprint，计算全部模板实际映射材质的哈希，四种风格均不同。四个不重叠 PLANNED job 完整 Store 往返一致，输入 tag 不变；unknown profile/plan 拒绝；合法 payloadHash 下改 styleFingerprint 仍拒绝。构造 style/styleFingerprint 都缺省、旧 v1 ID 与旧 planHash 的内存 clockwork 存档，验证旧 ID/plan/Store 完整保留、解析为 clockwork、所有模板材质完全不变。该兼容样本是合成旧格式，未提供真实历史 .dat，不能声称实际旧世界进程兼容验证。
2. actualMenuFourBuildsStyleIsolationAndEntry：在实际 Eclipse 维度按相同 seed、分离 anchor 串行完成四次 Engine 建造，unknown style 不得先分配 live job。对完成世界抽查最多24个实际变换且旋转后的模板块材质，避开 Boss 战斗屏障和 socket 支撑；每种非默认风格至少有真实 palette 替换，clockwork 也必须有实际块样本。每个实例用独立 overworld 十二格原生 FT 门、tier2 火盆、真实 ServerPlayer 打开实际菜单，逐一点击四个 provider 核对门 generatorId 绑定及图标标题；随后选回当前风格，重复 prepare 只能复用精确 style+seed 的 COMPLETE，不能复用前面同 seed 的其他风格。普通玩家真正 clicked slot25 进入，断言菜单关闭及抵达精确实例入口；实际 provider.leave 返回原来源并消费返回记录。测试玩家通过 finally 清理。全部完成后完整 Store 重载四个实际 job，输出成功标记。

成功标记 ECLIPSE_STYLE_STORE_PASS 与 ECLIPSE_STYLE_RUNTIME_PASS 必须以宿主实际日志为准。目前仅源码静态核对、空模板 SHA 核对，不代表编译或运行通过。每个实际建造最多19000次 tick 回调，总 timeoutTicks=80000；没有强制区块或测试生物，不额外改写共享 Store job。隔离运行目录必须是全新目录，避免与其他源集旧构建坐标相撞。

当前专用 API 是 Profiles.all/get、Profiles.blocks(plan,key)/map(plan,state)、Planner.create(style,seed,anchor)、Engine.plan(level,style,seed,anchor)。catalog fingerprint 保持旧值；styleFingerprint 独立保存；Piece 没有 style 字段。测试不假设 moss Catalog.get(style)/Piece.style/temporaryTicketCount API。

旧兼容断言额外独立证明 hash 规则：先从原始 plan JSON 剥离 style/styleFingerprint 字段，再计算旧 `v1:seed:anchor.asLong:catalogFingerprint` 的16位 ID；使用 Catalog.GSON 对该无新字段 JsonObject 序列化计算 SHA256，直接作为 fixture 的原 planHash。验证 typed POJO 默认 null 不参与序列化、实际保存 payload 确实没有两个新字段且原 hash 一致，Store.load 后不能替换旧 ID 或重算原 hash。新建显式 style 计划仍按当前 Planner 的规则测试，不把历史 style-less 兼容混为新计划 ID 规则。

本轮未新增通路 BFS：中等风格实现保留模板布局与碰撞形状，只改变 palette。本测试不验证 Boss 战斗与风格发光效果，也不将“新材质”和“新几何”混为一谈。纯菜单测试通过不等于客户端视觉验证。
