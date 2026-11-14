# 食武剑部分部署记录（2026-10-13）

## 实际结果

- 上海时间：2026-10-13T08:28:29.4059877+08:00。
- 用户明确授权父线程代为安装；dsh 保持只读并完成独立审计。
- 状态：正式 Forge 1.5.2 JAR 已安装，且对实际安装路径重新完成只读严格类/成员链接与 ZIP 检查。
- 唯一正式目标：C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar。
- 部署前 SHA256：62447A952C5FDA7776C9B8CDEE8CE4A38045D5190E57871E4CE4BACDD008F467。
- 实际安装 SHA256：2482DB5DB810FD649144B5F9F02E6559183AF4DF93AD86095856D2743C27FBD5。
- 完整基线备份：D:\ft工程文件\FT-SugarQueen-Boss-Mod\_work\food-weapon-sword-deploy-20261013\baseline-backup\qys-bosses-before-62447A952C5F.jar。
- 本次任务及收据：D:/ft工程文件/FT-SugarQueen-Boss-Mod/_work/food-weapon-sword-deploy-20261013/deployment-receipt.json；D:/ft工程文件/FT-SugarQueen-Boss-Mod/_work/food-weapon-sword-deploy-20261013/post-install-verification.json。

## 剑部分范围与暂停边界

- 包含食武材料构筑、Boss 材料掉落/原版与灾变/铁魔法秘银兼容、进化与技能稀有度、耐久损失丢部件、耗尽禁用保留、禁经验/普通修补、石像修复任务及对应 UI/网络门禁。
- 保留上一阶段共享的进化/耐久/修复规则，已有进化武器仍受其约束；“剑部分”表示本次不发布新增的弓/法杖专属扩展，不表示删除已有弓杖功能。
- 本轮弓/法杖专属部件权重、拉弓/装填计时、射弹伤害与初速快照、铁魔法施法/法力桥接、零耐久法杖虚拟耐久及专属 UI 均未打入正式包。
- 七个暂停弓杖类前缀及其类内引用均为零；原有 30 个弓/法杖专属条目逐字节保持。
- 共享工程中暂停扩展源码不回滚、不删除；部署前后 68 项共享输入哈希一致。
- 36 个显式编译源来自已核验的剑源码文件，不以任何旧候选 JAR 为输入：35 个源原样保持，FoodWeaponEvolution 仅保留 Item.getAttributeModifiers(slot, stack) 的一行属性事件防重入修正；该修正在当前共享源中已存在。

## 门禁与验证

- 构建和实际安装分别取得 .ftboss-152-deploy.lock 与 mods/.qys-bosses-install.lock 两把排他锁；安装当刻确认游戏退出、最新正式基线与候选/输入哈希未漂移。
- 以锁内当前正式包重新快照、映射并编译 36 个显式真实 Java 源，使用 116 个外部 JAR；不做全源码 Gradle 构建、不编译暂停或其它未发布原型。
- 88 个变更条目：65 类（50 新类＋15 方法级既有类）、20 模型/贴图、2 双语文件、1 Mixin 配置。3069 个无关条目、244 个无关方法和 741 个保护条目保持。
- 部署基线 EasterBossEntity：66 个 StackMapTable／392 帧；全类字节及 StackMap 保留。六个受保护类在基线、候选和实际安装路径均通过严格链接。
- 官方/SRG 生命周期 Hook 目标、65 类直接/Handle 成员引用、ZIP CRC/重复条目/JDK ZipInputStream/ZIP 规则检查通过；未声称实际 Mixin 应用通过。
- 双语仅修改每种语言的 265 个白名单键；保留其余 1288 个基线键。
- 本轮纯规则执行 5,047,709 次检查，0 失败；实际 Minecraft NBT 夹具 864 次检查，0 失败。夹具使用明确的 ItemStack/玩家/Forge 桥接占位，并非 GameTest。
- 九项 UI/静态回归通过，其中生命周期 UI 3419 断言、材料安全 20 检查、生命周期接线 35 检查。隔离辅助输入缺失的首轮失败记录保留，未删测试或放宽断言。
- 从本轮完整候选重新提取 20 个最终运行模型/贴图并核对 SHA；重新渲染十种材料正/背/左/右共 40 视图，父线程及 dsh 实际查看，未见缺面、异常遮挡、透明/UV 或裁切问题。
- 四视图目录：D:/ft工程文件/FT-SugarQueen-Boss-Mod/_work/food-weapon-sword-deploy-20261013/art-runtime/four-view。
- 实际安装包再次严格类/成员及六个保护类链接、ZIP 流/规则检查通过；其余 354 个模组 JAR 名单和 SHA 均不变。

## 实际写入范围

- 正式 JAR：仅当前 Forge 1.5.2 的 mods/qys-bosses.jar，写入一次；安装临时文件通过原子替换消耗，无残留。
- D 盘本任务目录：隔离源、构建产物、模型预览、验证报告、基线完整备份；本部署文档。
- 既有两把共享锁仅用于排他访问；未修改存档/配置，未删除其它模组。
- 当前实例 lightspeed-cache 不存在，未发生缓存删除。
- NeoForge、桌面、OneDrive 桌面、fix2：写入零。
- 增量 JAR 仅是审计载荷，不是完整 mod，不得放入 mods；实际安装的是新完整候选，未安装旧 6242005A 等候选。

## 待实机与后续恢复

- 未启动 Minecraft、未执行 GameTest 或实际 Mixin 共同应用验证。
- 待验收：损耗节点掉部件、耗尽不碎及各使用入口禁用；禁经验/普通修补；耗尽未绑定武器绑定→三个交付任务→领取修复闭环。
- 待验收：换手/换武器/跨石像/重放/重登的 UUID/revision/ACK 门禁；技能实际伤害倍率、GUI 缩放和联机协议 7。联机双方需要同版 JAR。
- 弓和法杖继续暂停。恢复时必须以当刻最新正式包重新读取、快照和重基；本次安装后正式包 SHA 为 2482DB5DB810FD649144B5F9F02E6559183AF4DF93AD86095856D2743C27FBD5，不得继续沿用旧 62447 基线增量或旧弓杖候选。
- 本次隔离剑源可作为重基的源码比对参考，但不能代替当刻正式基线与锁内检查。
