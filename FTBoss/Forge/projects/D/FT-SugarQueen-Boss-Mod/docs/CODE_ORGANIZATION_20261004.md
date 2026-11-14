# 工程结构与代码优化（2026-10-04）

## 本轮边界

不改注册 ID、NBT 键、资源路径、伤害数值、触发概率和掉落。考虑到部署包中仍有仅字节码保留的兼容类，本轮不批量移动 Java package，避免产生运行时 `NoClassDefFoundError`。

## 文件与校验结构

- 将原 845 行的 `gradle/verification.gradle` 改为短入口，任务按核心完整性、Boss/庄园、祁崽/法术、武器/近期回归四组放入 `gradle/verification/`。任务名与依赖关系保持不变。
- 新增 `checkProjectLayout`：检查 Java package 与路径一致、源码树无备份/日志/class、`assets/` 与 `data/` 路径为小写 ASCII、根目录无构建历史日志。
- 扩展安全整理脚本，使 `merge-*.log` 与 `build_err.txt` 也进入带 SHA-256 清单的归档，而不是直接删除。
- `CODE_STYLE.md` 明确 entity / boss / item / client / util 的放置边界；`util` 不再作为新功能默认落点。

## 食武代码优化

`FoodWeaponEvolution` 的属性刷新、武器命中、手持效果、击杀效果和 tooltip 过去会在同一次事件中多次读取同一份插槽 NBT，并重复构造插槽列表。现在每个事件先生成一个只读 `SocketStats` 快照，再复用计数、威力和套装等级。

- 属性刷新：最多 11 次插槽列表解析降为 1 次。
- 武器命中/手持效果/tooltip：多次解析降为各 1 次。
- 防御食物附魔：三次附魔列表扫描降为 1 次。
- 食物附魔 ID 改为初始化时索引；属性修饰符 UUID 改为枚举初始化时预计算。
- 三件套等级仍按“最高三个食材等级的整数平均”计算；新增穷举回归覆盖 0～8 个、每个 1～3 级的 9,841 种组合，确认新旧算法相同。

## 后续拆分顺序

1. 先把 `EasterBossEntity` 各变体技能按 Boss 抽到 `boss/ai`，保留实体公开入口和 NBT。
2. 再把 `FoodWeaponEvolution` 的每日任务、食物附魔、插槽分别抽成 package-private 组件。
3. 最后处理 `util/` 的领域归位；迁移前必须核对部署包仅字节码类的引用，不能直接批量改包。

## 验证与交付

- 根目录 18 份历史构建/合并日志（273,695 字节）已移动到 `_dev_backup/organization-20261004-173637-015/`，清单含原路径、大小和 SHA-256；再次预览为 0 项。
- 隔离树 `_work/project-organization-20261004/build-project` 执行 `clean build`：97 个任务成功，926 项运行资源通过，源码构建 JAR 校验 1,739 个文件条目。
- `node tools/verify_project.cjs` 全部通过；新增套装等级等价性穷举 9,841 组。
- 部署前对当前实机包做逐条目差异：发现 54 个仅运行包存在的兼容文件，并将庄园法术模型/贴图、荆棘护甲客户端、Punchy Inspect、血腥契约和较新万味胚刃动画全部加入显式合并白名单。
- 合并后旧包 1,791 个文件全部保留；最终包只新增 `FoodWeaponEvolution$SocketStats` 与 `FoodWeaponEvolution$FoodBlessingStats`，共同条目中仅食武进化类族发生变化。
- 最终 JAR：46,282,458 字节，SHA-256 `9C393BAEA7CBCACB20741FF82865A21A57C7FC36954177714AC7797C1EAC10BE`；Forge ZIP 规则与 JDK `ZipInputStream` 检查通过。
- 已安全同步桌面两处、OneDrive 成品目录、`FarmingTales_ForgeⅡ v1.5.2` 和 `v1.5.2fix2`；`v1.5.2_beta3` 目录不存在，按安全部署规则跳过。
- 本轮未修改模型、贴图或动画内容；兼容资源从旧包逐字节保留，因此不触发模型四视图改动验收。
