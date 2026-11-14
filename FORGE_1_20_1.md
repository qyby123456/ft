# FT Forge 1.20.1 三部分开发内容

本次范围：FTBoss、FTEvent、FT地牢。沿用原有 main 历史，NeoForge 工程保持独立。

| 部分 | 开发入口 | 本次新增或更新文件 | 已发布原件复核未变 |
| --- | --- | ---: | ---: |
| FTBoss | `FTBoss/Forge/projects/D/FT-SugarQueen-Boss-Mod` | 6392 | 0 |
| FTEvent | `FTEvent/Forge/projects/D/QYS-Event-Forge-1.20.1` | 0 | 1061 |
| FT地牢 | `FTDungeon/projects/D/FT地牢_大小型风格合并_20261109`；独立模块见 PROJECTS.md | 0 | 110174 |

## Boss 源码和运行基线的边界

- `src/main/java` 是可维护源码；`src/main/resources` 是现有资源；`art` 和 `tools` 保存编辑资料与开发工具。
- `runtime-reference/java` 仅为历史运行版反编译参考，不加入 Gradle 源集。现有基线记录有 604 个线上顶层类在主源码缺失，1343 份反编译参考；全量源码构建尚未恢复验证。
- `runtime-reference/qys-bosses.jar` 是现存固定运行基线，本次以 Git 普通二进制对象保存，不依赖 LFS，不等同于本次重新编译。SHA256：`c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942`。
- 构建依赖仍按原 build.gradle 的 libs 方式提供；三个第三方依赖不在本次重分发范围，文件名、大小、SHA256 见 `backup-index/FORGE_1_20_1_NOT_UPLOADED.json`。需从可信原分发或本地备份恢复，不能宣称开箱构建验收通过。

## 恢复和核验

新增或更新文件的路径、大小、SHA256 及脱敏原件哈希见 `backup-index/FORGE_1_20_1_20261114.jsonl.gz`，先 gzip 解压再按行读取 JSON。

原 Event/地牢历史文件和 Release 附件仍按 `PROJECTS.md`、`RESTORE.md` 及原 `backup-index/SOURCE_INDEX.jsonl.gz` 恢复；本次索引只覆盖新增或更新文件，不替代原索引。

辅助脚本与文档中个人用户目录仅在发布副本中换成 YOUR_USERNAME，运行前按本地环境配置。原件未修改；脱敏副本不作为删除原件的依据。原文档仅作归档复制，没有改写 docs 正文。

未执行源码构建、游戏测试或部署；未删除原工程、运行实例、历史证据、失败材料或备份。已不存在或不在当前扫描范围的旧源文件保留远端原版本，详见排除清单；未把缺失当成删除指令。
