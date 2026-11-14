# 2026-09-19 本批十线程同步记录

## 同步范围

主工程：`D:/ft工程文件/FT-SugarQueen-Boss-Mod`。

逐项对比本批十个线程的最终工程、发布清单、备份和验证记录，确认运行时代码已经全部汇入主工程。没有用任何较早的线程快照整树覆盖主工程，也没有修改伤害、冷却或掉落数值。

| 线程 | 本次统一保留的改动 |
| --- | --- |
| 柠檬献祭 | 一次性献祭自有召唤体系，继承能力30分钟；之后每次只召一只Boss；继承到期和召唤归属清理 |
| 庄园二次确认 | 两步带凭证确认，首次不发补给；最终确认只触发一次；投降规则提醒 |
| 折耳根 | 专属黑暗滤镜与两张透明贴图；挥剑、近战和铁魔法并行 |
| 功底 | 大杀四方不再被铁魔法替换；原生武器技能与铁魔法独立进行 |
| 柚子 | 迷你分身三门专属弱化魔法，35%威力，独立施法与冷却，不掉卷轴 |
| 雪黎 | 粉碎轮不额外击退；女仆UUID追踪、存档恢复、脱队回收和护盾归属 |
| 青衣、蛋蛋 | 青衣近战与施法并行、每秒落戟、上限与寿命；蛋蛋六波镰击 |
| 玄蓝 | Boss技能半径调整；超能光束固定20tick蓄力；不改玩家卷轴范围 |
| 紫菜 | 极高等级减速的属性溢出修复，避免减益反而加速 |
| 朵拉 | 狙击命中半径0.24；虚空、物理、魔法三段各240，总计720 |

此前的双飞轮、武器模型与动画光效、70门法术视效、非庄园Boss并行施法也保留。源码与资源接线检查不等于客户端视觉实机验收。

## 本次工程调整

- 全量GameTest入口补入之前未覆盖的柚子分身6项、折耳根7项、铁魔法原生粒子2项，汇总规模从171项变为186项。
- 柚子和折耳根专项使用独立批次，避免直接挤入原有默认并发批次。
- 非庄园并行施法测试补充失败诊断，不改断言标准和生产逻辑。
- README修正桌面与OneDrive为独立目录、必须同步四处的部署说明。

## 验证结果与限制

- `clean build --rerun-tasks` 的正式构建阶段及全部构建检查通过：887个运行时资源、1315个正式jar条目。
- 最终独立执行标准 `gradle build` 返回成功，未跳过任何构建检查；日志为 `release-build.log`。
- `node tools/verify_project.cjs` 的14组检查全部通过。
- 武器特效、玄蓝武器、隐藏Boss前摇、青衣资源、70门法术图集、原生模型接线、原生光效、折耳根滤镜八组补充检查通过。
- 独立朵拉伤害表检查通过：240虚空+240物理+240魔法；本次未改变数值。
- 最新整合回归为 **185/186**，未宣称全绿：青衣 `oneFallingTridentPerSecondWhileCasting` 的真实落戟数量断言失败。此前两轮分别出现两项延迟法术伤害失败和一项非庄园并行施法失败，这三项在最新整合回归中通过，存在非稳定表现，具体根因尚未定位，不能仅凭重跑通过视为已根治。
- 青衣与非庄园并行施法独立复验为 **16/17**：青衣8项全部通过，包括整合时失败的落戟计数；非庄园并行施法8/9，`allSevenBossesChargeAndDefectLockDoesNotBlock` 的女王专属施法排队断言失败，此项在最新整合回归中通过。保留失败记录，不将分次通过拼成“186项全绿”。
- 另有既存静态脚本 `check_dora_pulse_sniper.cjs` 仍断言提示包含旧伤害28，但现行倍率为3、运行代码与中英文提示均为84；该脚本、数值和语言文件与本次开始前完全一致。本次不回退游戏数值，也未把此项宣称通过。
- 最终jar的全部1315个条目与本次开始前的最新合并发布包逐字节相同：新增0、修改0、删除0，无测试类或测试场景混入。这次交付是核对并重新同步既有完整合并版本，不是在回归未全绿时引入新的运行时代码。
- 2643个构建输入与主工程逐项哈希核对；最终jar另做关键类反混淆字节码核验。未进行整合包客户端人工验收。

## 成品与源码

统一SHA256：`6D9B42A7185B85583630B007608952427BEB71C16F5FBC89AC965EE601033100`。

指定部署位置：

1. `C:/Users/YOUR_USERNAME/Desktop/qys-bosses.jar`
2. `C:/Users/YOUR_USERNAME/Desktop/FT经济-波动mod成品/qys-bosses.jar`
3. `C:/Users/YOUR_USERNAME/OneDrive/桌面/FT经济-波动mod成品/qys-bosses.jar`
4. `C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2_beta3/mods/qys-bosses.jar`

同步状态：2026-09-19 09:58 已通过主工程安全部署脚本重新同步四处成品，随后逐一读取文件核对SHA256，四处完全一致。旧 `FarmingTales Forge/mods` 未修改。

工程快照目标：`C:/Users/YOUR_USERNAME/OneDrive/桌面/ft工程文件/FTBoss-全部线程同步-20260919`。保留源码、运行时资源、美术源文件、构建配置、编译依赖、测试运行依赖、工具及文档；排除构建缓存、node_modules、音乐生成中间输出、日志和临时备份。需要重生成音乐时，在对应工具目录根据package.json恢复Node依赖。不会覆盖旧工程归档。

本次证据目录：`C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales Forge/_work/all-thread-sync-20260919-0935`。

- `source-audit.json`：十线程源码/发布清单审计。
- `release-inputs.json`：已构建源码哈希。
- `artifact-audit.json`：全部jar条目一致性及功能类核验。
- `build-all-gametest.log`、`build-all-gametest-isolated.log`、`all-gametest-diagnostic.log`：保留全部整合回归结果，包括失败。
- `focused-recheck.log`：青衣与非庄园并行施法复验。
- `project-checks-final.log`、`supplemental-checks.log`：工程与补充静态校验。
- `release-bytecode.txt`：最终成品字节码。
- `deploy.log`、`deployed-hashes.json`：安全部署结果与四处最终哈希。
