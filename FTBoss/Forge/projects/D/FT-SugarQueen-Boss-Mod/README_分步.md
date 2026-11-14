# FT经济 · 财富定价核心 mod 化（分步计划）

目标：把"价格波动/财富定价"从 KubeJS 迁移成 Java mod（Forge 1.20.1 / 47.4.10），
绕开 Rhino 限制（重载歧义、SRG 裸反射、脚本热重载状态清零）。

数据完全兼容旧 KubeJS：
- 玩家累计收入：persistentData 键 `ftEconTotal`（旧存档不丢）
- 全局开关：`config/farmingtales_wealth_switch.json` = `{"enabled":bool}`（默认 true）
- 档位/复杂度参数：与 ft_econ_progression.js 顶部完全一致

## 阶段

### v1（当前工程）定价引擎 Java 化
- WealthData   收入读写（NBT + 会话内存兜底）
- WealthEngine 档位比例 / 复杂度 factor / 倍率计算（纯逻辑，可单测）
- WealthSwitch 开关文件读写
- FteconApi    静态 API（v1.5 供 KubeJS 桥接）
- /ftecon test         打印与 /ftprice 同口径的状态(收入/比例/样例成交价)
- /ftecon wealth true|false|status  同 /ft wealth（mod 侧测试用，与脚本并存不冲突）

构建后把 `build/libs/ftecon-1.0.0.jar` 放进整合包 `mods/`，进服 `/ftecon test`，
与旧 `/ftprice` 输出对比：收入/ratio/每个样例价应完全一致 → 验证通过。

### v1.5 KubeJS 桥接（可选项）
把 ft_econ_progression.js 改成"瘦壳"：global.ftEconMultiplier / ftEconRecordIncome /
ftEconTotalIncomeOf 内部改调 Java.loadClass("net.farmingtales.ftecon.FteconApi")
对应方法。SDM 商店挂接、/ftprice、/ft wealth 命令仍留脚本里（避免动 SDM API）。

### v2（之后）彻底 Java 化
- /ft wealth、/ftprice 命令迁入 mod（注册 /ft wealth 前需先停用脚本同名命令）
- SDM 买/卖调价：先反编译 sdmshoprework / sdm_economy 确认 Java 事件 API，迁入 mod
- 出货箱整体（方块/容器/收据）迁入 mod 后，ft_econ_progression.js / selling_bin.js 退役

## 本机构建

1. 装 JDK17：`winget install EclipseAdoptium.Temurin.17.JDK`
2. 双击/命令行运行 `build.bat`（自动找 JDK 与缓存的 gradle-8.8；首次联网拉依赖较久）
3. 产物：`build\libs\ftecon-1.0.0.jar` → 放入实例 `mods\` 并重启游戏

## 目录
src/main/java/net/farmingtales/ftecon/  全部源码
src/main/resources/META-INF/mods.toml   mod 描述
