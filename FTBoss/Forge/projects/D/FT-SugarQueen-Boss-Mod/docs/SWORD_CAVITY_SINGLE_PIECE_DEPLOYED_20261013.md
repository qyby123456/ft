# 单格单件剑腔正式部署收据

部署时刻（Asia/Shanghai）：2026-10-13T16:06:59.1937883+08:00。

状态：已部署至 Forge 1.5.2。DSH 独立只读交接复核通过；它没有写入权限，持有用户本轮“部署”明确授权的主线程执行安装。此记录是后续正式状态；此前 delivery-unstacked.json 和 SWORD_CAVITY_SINGLE_PIECE_20261013.md 保留离线阶段原始验证输入，不追改其冻结哈希。

## 目标与 SHA256

- 正式目标：C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\qys-bosses.jar。
- 安装前基线：6AD89A24F5B3F5891DC79B688738B595724CBA08DC6614A3D6739D7D87DFCA14。
- 安装后正式 JAR：8AFDC1CFC059FAF40678F0FA74056095214F37FC16227FDE05B13A143310834F。
- 本次备份：D:\ft工程文件\FT-SugarQueen-Boss-Mod\_work\food-weapon-sword-cavity-grid-20261013\deploy-unstacked-20261013160007\qys-bosses-before-6AD89A24F5B3.jar。
- 备份 SHA256 与安装前基线一致；完整候选与安装后 JAR SHA256 一致。
- 最新唯一候选仍为 D:/ft工程文件/FT-SugarQueen-Boss-Mod/_work/food-weapon-sword-cavity-grid-20261013/build-project/_work/verification-unstacked/qys-bosses.validation-only.jar。所有以 6AD89A24 开头旧基线为部署前提的其他候选，后续不得直接安装，必须按新正式 SHA256 停止并重基。

## 实际写入边界

- 正式模组只写当前 Forge 1.5.2 的 mods/qys-bosses.jar 一处，使用 tools/deploy_jar.ps1 的 NoBuild、显式成品/目标、ExpectedTargetHash 与 File.Replace。
- JAR 差异为 19 条目（12 个既有条目＋7 个新增类），无删除。119 个无关方法、3153 个既有条目、73 个 Boss/法术/奖励/非石像网络保护条目保留。
- mods 中安装前后均有 355 个 JAR；其他模组 SHA256 完全不变。NeoForge、桌面、fix2 和弓/杖扩展写入为零。
- 本实例 lightspeed-cache 在安装前不存在，安装后仍不存在，没有额外缓存删除。部署临时文件经原子替换消耗；备份和审计资料位于 D 盘工作目录。

## 双锁与即时验证

- 两把共享锁同时独占持有：.ftboss-152-deploy.lock 与 mods/.qys-bosses-install.lock；先检查游戏退出，再复核即时基线与 74 个共享/16 个隔离输入，完成备份后再次复核，才进行原子替换。
- 锁内安装前、安装后正式目标六个保护类严格无初始化链接通过。安装后实际 JAR 17 个变更类、949 个直接/Handle 引用及 10 个保留入口引用通过。
- ZIP 为 3172 条目；CRC、重复条目、STORED/descriptor 与 JDK ZipInputStream 通过，差异与审批清单一致。
- 当刻正式 EasterBossEntity 原样保持，SHA256=365738E00C6184327504CFD2261AC8DD5860194ABE82B6729B35D78406959E6F。安装前/后 javap 核对 231 张 StackMapTable、1430 帧；两次严格链接均通过。
- 原有 5,388,917 项规则/预算/单件布局/NBT/UI 回归零失败。安装流程不等于游戏启动、Mixin 实际应用或 GameTest。

## 凭证与待实机项

- 完整收据：D:/ft工程文件/FT-SugarQueen-Boss-Mod/_work/food-weapon-sword-cavity-grid-20261013/deploy-unstacked-20261013160007/deployment-receipt.json。
- 锁内报告：preflight-guard.json、quick-guard.json、installed-guard.json；完整执行日志为 deployment.log，同目录保存严格链接、ZIP 和 Easter StackMap 日志。
- 待实机验证真实字体/GUI 缩放、配方同步与覆盖优先级、联机材料放入/移动/交换/取出、高延迟 ACK、耐久脱落/修复以及材料被动与羁绊战斗效果。
- 本次铁/金剑使用 18 个独立粒材料格＋1 个木棍格；原生与加装格始终单件。合金完整 43 单位账本另存，单件代表布局不额外收费或返还物品。
