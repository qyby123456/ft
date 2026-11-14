# 剑腔单格单件与原生剑形展开

日期：2026-10-13。状态：共享剑源码及离线完整候选通过；正式 JAR 未写入。此记录取代旧四件默认、格内数量堆叠和旧阶段候选，不适用于弓、法杖或 NeoForge。

## 本轮契约

- 剑构成格一格一件，不再用同格数量表示多份材料。原生实物图标和后续加装都为 Count=1；背包仍是原版真实库存，可正常堆叠。
- 铁剑：木棍一格，18 个铁粒逐格展开；金剑同样为一格木棍加 18 个金粒。没有新增加工成本或可拆出的免费物品。
- 剑腔保留 9×3、27 格，默认铁金布局占 19 格，其余 8 格可放入额外材料。轮廓为下面的横向剑体：

```text
..MMMMM..
WMMMMMMMM
..MMMMM..
```

M 为对应金属粒，W 为木棍。四个旧逻辑必需槽序号保留，显示位置重新映射；渲染、命中、选择和移动仍发送同一个逻辑槽号，不改变存档槽名或原有网络序号。

- 钻石、木、石剑仅展现两份实际剑体材料和一份木棍，第四必需支撑是零预算共享参考，不为凑形状虚构用量。
- 下界合金完整账本保留钻石 2、木棍 1、金粒 36、合金碎片 4，共 43 单位；27 格中使用 19 格单件代表布局，不承诺将全部 43 单位显示出来。各材料类型可识别，显示数量与来源均不超过原配方预算。
- 模组剑优先读当前已同步的 RecipeManager；同 ID 配方覆盖取运行时有效结果，不叠加基础包和覆盖包。铁金锭转粒、已知原版剑与下界合金锭展开只做一次；未知嵌套模组武器不推断配方。
- 炎葬采用当前有效配方的合金剑、炽炎锭和烈焰棒等成分；下界砖及黑曜石只是单独标记的环节参考，不能冒充实际合成成本。焰魔是 Boss，不是武器 ID。

## 账本与安全

- SwordNativeMaterials.budget 保存完整成本与 RECIPE/THEME/SHARED 来源；parts 返回独立的 27 格单件布局。已有 Version=1 原生账本可继续读取，不截断保存的材料数量。
- 首次绑定或真实编辑才保存服务端账本，单纯打开 UI 不写 NBT。布局为空白的 air 标记不被解释成材料。
- NATIVE 材料不提供免费加装属性、羁绊或退款。共享支撑与主题参考也不能拆出物品；只有实际单件投入的 INSTALLED 材料才可按已有规则移位或返还。
- 已保存 native 状态遇到新布局空格时不再回退画出材料或武器图标。实际磨损的 missing 状态保持，不因重开界面、重载配方或再次 capture 恢复。
- 含实物 Item 记录但 Count 不为 1、注册物品无效或材料身份不符的二版安装槽按缺件处理，不授予加值，不可移动或退款；没有 Item 的历史材料存档仍保留既有迁移语义。
- 现有 12 种材料羁绊、实际物品 NBT、一次消耗一个、满背包预检、耐久与修复任务入口保留。没有新增飞行、召唤等战斗事件，也没有改写 Boss 攻速或法术逻辑。

## 候选与范围

- 正式及只读基线 SHA256：6AD89A24F5B3F5891DC79B688738B595724CBA08DC6614A3D6739D7D87DFCA14。
- 最新唯一完整候选：D:/ft工程文件/FT-SugarQueen-Boss-Mod/_work/food-weapon-sword-cavity-grid-20261013/build-project/_work/verification-unstacked/qys-bosses.validation-only.jar。
- 候选 SHA256：8AFDC1CFC059FAF40678F0FA74056095214F37FC16227FDE05B13A143310834F。
- 增量 SHA256：4344DDF6652659C97038AC53531706A7E6EB05A20727CDC3D9F903DF605E94E7；增量是补丁载荷，不是完整 Mod，不可直接安装。
- 完整候选修改 19 条目：12 个既有条目修改、7 个新类、没有删除。严格链接 17 个变更类、949 个直接或 Handle 引用以及 10 个保留入口引用通过。
- 119 个无关方法和 3153 个既有条目内容保持；构建报告保留计数 3152，另一个原样 ftboss.mixins.json 单独核验。
- 73 个 Boss、法术、奖励及非石像网络保护条目不变。EasterBossEntity 全类 SHA256 为 365738E00C6184327504CFD2261AC8DD5860194ABE82B6729B35D78406959E6F，与基线相同；已知全类 231 张 StackMapTable、1430 帧未被本轮修改。
- 基线和候选均对六个保护类执行不初始化的 -Xverify:all 链接。生命周期 Mixin 三类字节原样保留，official 8/SRG 8 个 Hook 及单一经验修补过滤组验证通过；这些不是实际 Mixin 应用或实机测试。
- 完整 ZIP 3172 条目，265 个 STORED；JDK ZipInputStream、CRC、重复条目和 descriptor 校验通过。只使用 JDK jar 创建候选。
- 两种语言各新增 100 个允许键；候选原有 1602 个语言键不变，共享原有 1622 个语言键也保留。共享 Hub/WeaponMaterialBuild 的暂停非剑分支不编入剑专属候选，其余 7 个直接构建 Java 输入与共享源码一致。
- 共享剑范围冻结 74 个生产输入，对比前期 71 个输入为 63 不变、8 修改、3 新增。正式写入为 0，NeoForge 写入为 0，弓/杖暂停的七类不在候选中。

## 回归与复跑

- 原有剑腔规则：1,796,002 项通过；原生配方预算：1,225,563 项通过；单件布局：2,339,285 项通过；真实 NBT 桥接：26,358 项通过；UI 静态和几何：1,709 项通过。合计 5,388,917 项，0 失败。
- 另有既有材料 UI、20 项材料安全、35 项生命周期静态接线通过。旧失败输入与历史候选保留，不删除或复用。
- NBT 使用真实 Minecraft NBT 编解码；物品/玩家/注册表与 RecipeManager 使用明确夹具，耐久钩子手动调用，不是 GameTest。
- 在唯一工程根目录运行 powershell -File tools/verification/run_sword_cavity_grid_checks.ps1；加 -Native 跑预算，加 -Layout 跑单件布局，加 -Nbt 跑 NBT 桥接，三种选项互斥。
- UI 复跑：node tools/verification/check_sword_cavity_grid_ui.cjs _work/food-weapon-sword-cavity-grid-20261013/ui-static-rerun-single-piece.json。
- 最新构建计划是 _work/food-weapon-sword-cavity-grid-20261013/build-plan-unstacked.json。重建必须使用新输出目录和当刻最新基线；基线变化立即停止并重基，不复用旧候选。
- 完整构建证据在 build-project/_work/verification-unstacked/verification-report.json、method-delta.json、verification.log。保护报告、输入冻结和交接记录在本工作目录中带 unstacked 名称的文件。

## 预览与待实机项

- sword-cavity-single-piece-preview.png 与 sword-cavity-single-piece-small-preview.png 使用已验证布局和正式基线中的实际素材；字体替代，非 Minecraft 截图。已实际查看，铁金 18 个单件粒子和木棍、空白格、角标以及合金代表布局正确，未出现数量标签堆叠或界面重叠。
- 待实机核验真实字体、GUI 缩放、联机单件放入/移动/交换/取出、原生配方同步优先级、材料磨损/修复与高延迟 ACK。当前 ACK 仍为位置关联，普通等待期间禁止关闭/重复编辑；强制关闭或断线重入的极端回包需要实机确认。
- 此轮未改模型或贴图，不触发模型四视图修改验收；Chest Cavity 仅作 27 格容器参考，未安装 Fabric 依赖。
- 后续部署合并继续交给 dsh；收到部署指令后，必须确认游戏退出，取得 .ftboss-152-deploy.lock 与 mods/.qys-bosses-install.lock 两把锁，在锁内复核即时基线和冻结输入，先备份，再通过 tools/deploy_jar.ps1 的 NoBuild、显式成品、期望 SHA256 与 File.Replace 路径部署。不能直接覆盖，不能安装历史候选；基线漂移立即停止重基。
