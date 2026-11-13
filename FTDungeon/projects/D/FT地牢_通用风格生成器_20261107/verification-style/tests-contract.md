# 通用风格原生测试合同 C

Java17，namespace ftstylesqa，PrefixGameTestTemplate(false)，template="empty"。C 只写 styleTest 与 verification-style（不写 ready-B）及通用风格测试说明，本轮另获授权新增 tools/style-author.py；不改 main/build/旧测试，不部署、不写正式存档或配置。

显式接口：Profiles.get(String)/all()，Style 的公开配置字段；Catalog.get(style),Catalog.blocks(style,key)；Planner.create(style,seed,anchor)；Engine.plan(level,style,seed,anchor)。Plan/Piece.style 冻结风格。forest/desert/frost 同一引擎、forest 维度和 Store，不建立风格 Java/NBT 副本。

测试必须独立按各风格实际块碰撞和支撑建立脚点 BFS，不以模块跳数替代距离；最低 eligible 非走廊 trigger 最远，核对全部冻结距离、graphEdges。三风格同 seed/anchor 确定但 IDs 不同；比较占用几何而非仅 BlockState/palette。

真实完整建造至少 desert 和 frost，复用实例验证生产 FT 菜单及 provider 绑定/进入/离开；原生 Boss 门 metadata 与风格/实例一致。Store 跨风格 NBT 往返必须保存 style，修改配置指纹必须拒绝加载而非重建。未知 style 与非法 palette 必须 fail closed。

宿主需在测试服务端启动前将第四个 JSON fixture 安装到全新 run/config/ft-dungeon-styles/demo.json；fixture ID obsidian，使用独立 title/icon/palette/Boss 数值和 decorations=[]，不依赖未知装饰 recipe。Profiles 启动加载后 get(obsidian) 和生产 provider 菜单都应出现，测试不能自己 register Java provider 冒充 JSON 动态加载。fixture schema 以 host 最终 Profiles 为准，未落盘不得猜造通过。

附加独立源码审计检查仅一个 moss/generator 核心，不存在 desert/frost/obsidian 的 Java 引擎目录或27NBT复制。此项文件证据与原生执行证据分开记录。测试未跑不写 PASS；宿主串行编译/执行，C 不跑 Gradle。

r1 宿主日志确认全部三个 required PASS。本轮新增 cap/barrier AIR/slab/water/BE 拒绝、palette water 拒绝，以及合法带状态装饰校验和仅 AIR 增量验证，待宿主重跑。CLI 只写显式 output、拒绝覆盖、基础校验参数；新几何仍需连接端口和独立 BFS 验证。

Runtime 同时增加独立 desert 非玩家真实击杀 LOCKED 与真实 ServerPlayer 击杀 VICTORY（通关态）子例：精确匹配 Mob intent UUID/role/type/instance，核对实际返程十二格开闭、风格屏障、奖励/胜者、其他实例不解锁和 Store 往返。增加第三次真实建造，清理玩家、实例实体、强制区块；不直接调用 Gameplay.death 或手改状态冒充真实死亡。
