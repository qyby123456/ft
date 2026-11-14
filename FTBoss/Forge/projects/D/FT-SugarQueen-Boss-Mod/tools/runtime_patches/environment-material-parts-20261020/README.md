# 环境辅料配件准入补丁

- 运行源码：util/WeaponEnvironmentMaterials.java、SwordCompatibilityRules.material/itemFunctions。现有分类与专属属性优先；仅精确ID回退，不开放未知物品或成品武器。
- 运行时另将 SwordNativeMaterials.defaults 和 SwordCavityBuild.nativeMaterial 的旧 SwordItem 强制转换改为源码已有的 TieredItem 安全检测，保留线上升级配方前置分支及其他方法。
- 本目录 build.cjs 接收隔离工作目录作为第一个参数，默认 _work/environment-material-parts-20261020；工作目录必须有 baseline.jar、MethodPatch.java 和 evidence/。不要全量主工程构建覆盖生产包。
- prepare-probe.cjs 和 launch.ps1 使用时先复制到隔离工作目录；依赖项目既有的四武器编译classpath与合成存档。PROBE_FLAVOR 用于新建不覆盖的实例名称。
- 纯规则测试：tools/verification/WeaponEnvironmentMaterialCheck.java，参数依次为基线JAR、候选JAR、输出JSON。
- probe-src 包含33辅料×9武器×无标签/带标签两种状态的594组存取/NBT/非法输入矩阵、奥术原质18个主副手真实鼠标UI案例，以及既有弓弩装填和作者缓存回归。首次候选崩溃证据保留于工作目录，不能算完整通过。
- deploy-validated.ps1 仅在完整候选回归通过、所有Minecraft客户端正常结束、三把共享锁取得且目标基线SHA未变化时调用共享部署工具。部署目标仅当前 Forge v1.5.2。
- 文档影响提案：_work/docs-proposals/environment-material-parts.md。没有修改 docs/**。
