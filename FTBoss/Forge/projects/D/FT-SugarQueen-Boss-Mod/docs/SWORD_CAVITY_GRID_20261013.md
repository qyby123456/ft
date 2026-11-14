# 剑腔自由网格与材料构筑

日期：2026-10-13。历史阶段记录：此文的四件原生默认与 C23 候选已被后续“逐格展开、格内不能堆叠材料”要求取代，不能作为当前交付或部署依据。单格单件版本见 `SWORD_CAVITY_SINGLE_PIECE_20261013.md`；正式 JAR 尚未被本轮覆盖。

## 界面与操作

- 剑材料页使用独立、自适应的大面板；剑腔九列三行，共 27 格。背包 27 格与快捷栏 9 格同时可见，不再翻原料页。
- 虚拟槽位间距 32，物品图标放大绘制。缩放按当前 GUI 逻辑窗口计算，不修改玩家 GUI 设置；极小 GUI 窗口仍会缩小，不保证所有分辨率下槽位都更大。
- 常驻说明只显示所选材料的简短作用，最多两行；材料名称、原生／加装状态、完整作用放在悬停中，不再展示剑部位职责或当前值到预览值的比较。
- 原生件、已装材料均显示真实物品图标；原生／加装／缺件角标区分状态。金框为来源，绿线为目标。必需件缺失与选配材料脱落使用不同提示。
- 手持剑，在对应石像打开材料页。未绑定可先绑定；选择背包材料和剑腔格后点“放入”。每次消耗一个对应材料。
- 选择已装材料，再选择另一格，点“移位”可移动或交换；点“取出”归还本轮实际投入且留存记录的材料。等待服务端确认时禁止重复编辑和普通返回，刷新 Open 包不会提前解锁。
- 新插入材料存储实际物品的一份完整 NBT，图标、名称、附加数据和返还使用这份记录。替换时先检查旧材料返还空间；背包装不下则拒绝，不吞旧件也不消耗新材料。
- 原生材料、没有实物记录的兼容旧数据、创造模式免费装入的材料不生成可返还实物。移位／交换不额外收费，也不复制材料。

## 默认结构与兼容

- 默认只保留刃部、框架、握持、固定四个必需原生件，其余格为空；不会凭空填满护手、核心、配重等选配件。
- 原生刃部／框架依据实际剑等级或修复材料识别；握持／固定以木质基材呈现。自定义剑找不到已接入材料时使用实际修复原料或武器本身作为图标后备。
- 原生结构维持武器固有能力，不额外赠送加装材料被动或羁绊。正常加装材料可以放在全部剑腔格，不再要求羁绊材料必须占某个固定部位。
- 兼容读取既有三材料与一版剑腔 NBT；只看界面不改武器、不改变旧构筑数值。已装材料、真实缺失记录、武器 UUID 和其他 NBT 均保留。
- 第一次确认修改才写入二版网格和新的网格加值标记。旧可选原生占位不算免费装入材料，也不冒充损耗后掉落。
- 全网格贡献采用相同规则；同材料多份递减并设上限，羁绊每组只计算一次。移位不改变完整剑的加值；移动必需件却不补回其位置会造成结构缺失。
- 耐久损耗仍会让加装件、原生必需件脱落。修复任务只修耐久，不免费重生已经失去的材料；耗尽仍是禁用而非删掉武器，不恢复经验修补。

## 羁绊

羁绊检查实际加装的材料组合，允许多组同时生效；原生件不凑羁绊。结构缺失时不激活羁绊。以下组合已接入实际耐久、损耗、伤害、攻速、触及构筑计算，不只是 UI 标签。

| 羁绊 | 组合与作用 |
|---|---|
| 接通：霜晶剑骨（耐久 +8%，损耗 -6%） | 霜糖＋钻石：耐久 · 耐磨 |
| 接通：导电回路（攻速 +4%，触及 +0.10） | 电容＋铜/红石：攻速 · 触及 |
| 屠夫钢刃 | 屠夫＋黑钢/凋灵锭：伤害 · 耐磨 |
| 茶香编织 | 茶＋木/皮革：攻速 · 耐磨 |
| 陈酿盛宴 | 酒＋苹果/奶酪：伤害 · 攻速 |
| 孢子根系 | 孢子＋木/骨：耐久 · 耐磨 |
| 牧羊契约 | 牧羊＋骨/奶酪：耐久 · 伤害 |
| 潮汐导流 | 潮汐＋石英/末影锭：耐磨 · 触及 |
| 炽炎锻造 | 炽炎/烈焰＋黑曜石/下界锭：耐久 · 伤害 |
| 奥术晶格 | 秘银/紫晶＋青金石/末影锭：耐磨 · 攻速 · 触及 |
| 远古合金 | 远古锭＋铁/黑钢：耐久 · 耐磨 |
| 宝石王冠 | 金＋绿宝石/钻石：伤害 · 攻速 · 触及 |

材料本身继续调用既有命中、防护、击杀和宠物支援入口；此轮没有另造“飞行”“召唤”战斗事件，也没有把属性型羁绊说成新增技能触发器。

## 胸腔 Mod 参考

- 参考项目：[Chest Cavity（Modrinth）](https://modrinth.com/mod/chest-cavity)。下载版本 2.17.2.1，Fabric 1.20.1，仅存放于工作目录，不加载、不执行、不装入 Forge。
- 参考 JAR SHA256：1C5D01B10CC514DC0BFC80A0C226FED488AEA68BCFF2340DBF8C2A943B08FD4E。
- 实際下载 JAR 的 ChestCavityInventory 无参构造反汇编确认容量为 27；源码参考九列容器、完整背包、实际器官图标和器官加值展示方式。
- 参考源码：[Tigereye504/chestcavity](https://github.com/Tigereye504/chestcavity)，下载提交 0b67c3f2df84e7d528509daba4fa0078a689ad93，CC0-1.0。源码分支与发行 JAR 有差异；27 格容量依据发行 JAR，而非混用分支推断。
- 本轮采用交互和展示思路，不复制其资源，不新增 Fabric 依赖。

## 构建与验证

- 基线：6AD89A24F5B3F5891DC79B688738B595724CBA08DC6614A3D6739D7D87DFCA14。
- 唯一完整候选：D:/ft工程文件/FT-SugarQueen-Boss-Mod/_work/food-weapon-sword-cavity-grid-20261013/build-project/_work/verification-release/qys-bosses.validation-only.jar。
- 候选 SHA256：C23ACAD7CBDF668536EC6ED29BEEC7E1244B7E0291A3B89119C13A4D30E4BAEF。
- 候选只修改 14 条目：12 个既有条目修改，2 个新增类，没有删除。新增类为 StatueSwordCavityScreen 和 StatueMenuNetwork 的 CavityLayoutAction。
- 剑相关实际生产源码为 7 个 Java 文件，加两份语言；共享 Hub／WeaponMaterialBuild 保留尚未部署的非剑源码分支，隔离候选不编入这些分支。没有宣称整个共享源码树均已编译。
- JDK 实际编译、重映射、12 个变更类的严格字段／方法／Handle 链接通过，842 个直接／Handle 引用及 10 个保留入口引用通过，不初始化游戏。
- 120 个无关方法、3153 个既有条目内容保持。构建器保留清单是 3152；另一个原样条目 ftboss.mixins.json 单独随验证输入处理，两种计数已核对，并非遗漏。
- 73 个 Boss、法术、奖励和非石像网络保护入口不变。弓／杖暂停的七类未编入候选，原有 Mixin 配置与三个生命周期 Mixin 字节不变。
- ZIP CRC、重复条目、Java ZipInputStream、STORED／descriptor 规则通过；完整候选有 3167 条目。增量 JAR 是补丁载荷，不是完整 Mod，禁止直接安装。
- EasterBossEntity 字节不变；全类 StackMapTable 231 个、1430 帧。基线与候选六个保护类均通过 -Xverify:all 的不初始化严格类链接。
- 生命周期 Hook 静态验证通过 official 8／SRG 8 个目标及单一经验修补过滤组；这不是实际 Mixin 应用或实机验收。
- 纯规则 1,796,002 项、真实 Minecraft NBT 桥接回归 25,727 项、新 UI 静态／几何回归 1,690 项，零失败；另有已有材料安全 20 项、生命周期静态接线 35 项和已有材料 UI 回归通过。
- NBT 回归使用真实 Minecraft NBT 编解码，但物品／玩家／注册表为明确夹具，耐久钩子手动驱动，不是 GameTest。
- 两种语言在候选中各增加 94 个允许键，原有 1602 个键不变。共享源码语言另保留未编入候选的暂停分支键，原有 1622 个共享键不变。

## 复跑入口与证据

在唯一源码根目录运行：

- 纯规则：powershell -File tools/verification/run_sword_cavity_grid_checks.ps1。
- NBT：powershell -File tools/verification/run_sword_cavity_grid_checks.ps1 -Nbt。需要本轮工作目录内的显式夹具、已发布剑专属生命周期源码和缓存 dependency-classpath.txt，可通过参数提供同等输入。
- UI：node tools/verification/check_sword_cavity_grid_ui.cjs _work/food-weapon-sword-cavity-grid-20261013/ui-static-rerun.json。
- 重建候选：在 build-project 目录以缓存 Python 运行 tools/build_food_weapon_increment.py 与工作目录的 build-plan-release.json；任何基线变化必须停止并重基，不拿旧候选继续部署。
- 核心证据在 _work/food-weapon-sword-cavity-grid-20261013：candidate-preservation-release.json、lifecycle-hook-release.json、ui-static-release.json、shared-source-freeze-release.json、delivery.json。
- 最终构建报告与方法差异位于 build-project/_work/verification-release/verification-report.json、method-delta.json；历史 verification／verification-final 保留但不是本轮最终交付。
- 现有两个公共 SwordCavity 回归入口已更新为当前契约，旧测试与失败运行保留在工作目录备份和日志，不删失败证据。
- sword-cavity-layout-preview.png 为实际素材和虚拟坐标的静态布局预览；字体为替代字体，不是 Minecraft 游戏截图。

## 实机待验与部署边界

- 待验真实 Minecraft 字体、鼠标悬停、GUI 缩放、长自定义名称和真实模组剑修复原料图标。
- 待验单机与联机放入／替换／移动／交换／取出、满背包拒绝、断线重连和高延迟回包；客户端与服务端需同步使用协议 9。
- 待验实际耐久消耗、材料脱落、修复任务、禁修补与禁用状态，以及实际战斗中的材料被动和羁绊数值生效。
- 此轮正式 JAR 写入为零，NeoForge 写入为零；弓与法杖扩展继续暂停。
- 部署只允许 Forge 1.5.2 的 mods/qys-bosses.jar，必须先确认游戏退出，在两把共享锁内重读即时基线、复核输入、备份，然后使用既有 deploy_jar.ps1 的期望哈希与 File.Replace 路径；不直接拷贝覆盖。
