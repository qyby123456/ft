# Ingot and raw material compatibility

既有条目只修改 WeaponEnvironmentMaterials.class，并新增 WeaponMaterialBonusRules（含两个record）及 WeaponMaterialBonuses 共4个class。按已安装模组精确ID补齐35种原料，追加用户要求的稀有度法强/蓝量和高阶战斗属性；详细档位及边界见 RARITY_BONUSES.md。保留原有9种锭及配件记录格式；同页升级品和成品装备不因此放行。

## 构建

- 基线：_work/ingot-material-parts-20261020/baseline/qys-bosses.jar，修改源文件前留有同目录备份。
- 执行 node tools/runtime_patches/ingot-material-parts-20261020/build.cjs，Java17精确编译三个源文件、只对本次五个class重映射；SingleClassOverlay 要求其余4118个既有条目内容字节完全不变，并恰新增4个class。首次构建前先从冻结基线生成 candidate-mapped.jar 作为编译依赖，不得用不完整的主工程整包代替。
- 编译包含主工程其他尚未完成的改动是不允许的；不可用完整Gradle产物替代生产基线。
- 纯规则回归检查35项补充、9种已有锭、旧分类/属性和同页非原料拒绝；同时回归上一轮33环境材料。

## 隔离游戏验收

- probe-src/ 是持久探针源文件；需复制至自己的任务目录 probe-src/ 后编译。
- 将最终候选用 ForgeAutoRenamingTool 的 srg_to_official_1.20.1.tsrg 映射为 candidate-mapped.jar，仅供测试编译。
- prepare-probe.cjs 的运行位置为 _work/<task>/，不在本工具目录直接运行；它使用已有隔离世界模板，按当前实例的每一个模组名称及哈希构建新测试实例。相同文件可在D盘硬链接；跨盘/新增文件独立复制。
- PROBE_FLAVOR 指定新的实例名；拒绝覆盖已有实例。测试包必须是精确的最终生产候选。
- launch.ps1 位于任务目录，检测到用户游戏时拒绝启动；不可删除该保护或终止用户游戏。
- 测试81材料×9武器×有/无自定义NBT共1458组存取、18组主副手真实鼠标操作、28组弓弩网络装填与288条缓存回归。
- 原料矩阵走真实注册表及服务端菜单规则；真实数量守恒和鼠标动作由9种代表原料的18组UI用例验证，不把全部1458组宣称为鼠标测试。
- 不修改生产存档，不将静态检查或合成世界验收表述为用户所有武器个体/FPS验收。

## 部署

执行 powershell.exe -NoProfile -ExecutionPolicy Bypass -File tools/runtime_patches/ingot-material-parts-20261020/deploy-validated.ps1 -Flavor rarity02。

该脚本要求所有最终证据通过、候选SHA与游戏验收一致、其他模组一致、用户及测试游戏均退出，持有三个共用部署锁，当前目标仍与冻结基线同哈希。只调用共享 tools/deploy_jar.ps1 写当前Forge v1.5.2；不写桌面、fix2或NeoForge。部署前保留整包备份，之后回读SHA和逐条差异。若目标基线变化，必须重新合并并重跑验收，禁止去掉预期哈希门禁。

未写 docs/**；文档影响提案位于 _work/docs-proposals/ingot-material-parts.md。
