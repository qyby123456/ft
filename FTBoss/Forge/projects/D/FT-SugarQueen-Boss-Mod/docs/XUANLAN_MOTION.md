# 玄蓝月武：充能骨骼、回收变形与飞行拖影

> 2026-09-18 后续：玩家现可同时维持两枚飞轮，手持模型按两个槽位分别恢复；最新容量、回收和动作 kind 扩展见 `XUANLAN_DUAL_BLADES.md`。本页其余部分保留初版动画设计与验证记录。

## 表现

- **绿枪充能**：上、下枪轨从枪膛枢轴分别张开，悬浮能量轨前移；三道青色导光环沿枪膛汇聚，枪口的外青内白聚焦环随充能增强。发射后滑轨和枪托后坐，8 tick 内复位。
- **白飞轮掷出**：手持模型的两片月刃向中空轮毂折叠，锁扣旋转，清晰区别“武器就绪”和“飞轮尚在外面”。飞行中仍是双月刃盘，回程轻微收刃。
- **白飞轮回收**：只在服务端确认飞轮到达玩家时播放，月刃重新展开、锁扣过冲回弹、青白环脉冲消退；12 tick 后精确回到原姿态。撞墙消失或丢失的飞轮不会伪装成成功回收，最多按原 60 tick 飞行寿命自动复原手持外形。
- **飞行拖影**：玩家白飞轮和玄蓝 Boss 飞轮共用轨迹拖影；青白双层细带沿实际位置转弯，近处带少量渐隐月牙轮廓，不复制整套高面数模型。
- 主手、副手和第三人称分别读取持有者状态；绿枪不会借用另一只手的充能。Boss 绿枪从同步 beam cue 读取阶段，兼容上一轮缩短后的 11 tick 前摇。

## 实现边界

本轮是外观改造，不改充能门槛/时长、伤害、能量、标记、耐久、冷却、飞轮轨迹、碰撞、命中上限、掉落或 NBT 存档规则。

既有 155 方块绿枪和 192 方块白刃、原图集、原 `.bbmodel`、背包/掉落物/物品框变换均保留。生成器将原模型精确划分为 9 个网格部件，不重画或重采样贴图。`ModelPart` 骨骼驱动分件网格，`XuanlanMotionRules` 统一提供平滑关键姿态；每次绘制重新赋值，不累加变形。

`XuanlanWeaponModel` 在原物品模型完成形态选择及显示变换后，仅把第一/第三人称手持交给定制渲染器。GUI、GROUND、FIXED、HEAD 仍返回原模型，避免背包图标或 Boss 飞轮的既有缩放被误改。动作快照不持有实体；渲染器的线程局部预备状态消费后立即释放，重载资源时也清除。

`XuanlanWeaponMotion` 按 UUID + 主/副手保存短时动作；有效动作最多 128 条、80 tick 后回收，世界对象改变或断开清空。沿用 `ftboss:xuanlan_weapons` 通道与原包结构，新增 kind 6–11 用于主/副手开火、掷出、回收；只在已接受动作后发包，不每 tick 发送骨骼、粒子或轨迹数据。旧 kind 与协议编码不变，旧客户端可忽略新动作 kind。

## 性能与可见性

- 复用 `CombatVisualEntities` 同 tick 分类快照，不引入逐帧全实体扫描。
- 最多记录 64 枚飞轮，每枚固定 9 个位置，离相机超过 48 格不记录；近处最多 8 点、远处最多 4 点。
- 完整/减少/最少粒子档每帧总预算分别为 384/192/96 条带段、12/6/0 个月牙残像；替代原玩家飞轮逐 tick END_ROD 粒子。
- 位置突变超过 6 格、采样间隔大于 2 tick、时间回退都会截断轨迹，防止传送拉出长线；移除、换维度、断开时清空引用。
- 世界拖影使用深度测试、只写颜色不写深度，不透墙，不以大片不透明光片挡视线。充能光效放在枪膛骨骼局部空间，不再根据玩家眼睛位置猜枪口。
- 暂停不推进采样；动画取世界 tick + partial tick，速度不随 FPS 改变。充能环只在实际充能/开火时增加层数。

## 校验与预览

从主工程运行：

```text
node tools/verification/check_xuanlan_motion.cjs
node tools/verification/check_xuanlan_client.cjs
node tools/verification/check_xuanlan_weapons.cjs
node tools/verify_project.cjs
```

前两项已加入全量 `verify_project.cjs`。规则测试运行真实 Java 动画/轨迹代码，并输出 `build/verification/xuanlan-motion/poses.json`。客户端测试以最小 API 替身驱动真实动作跟踪器和模型包装器，覆盖手别、维度、动作上限、过期、Boss cue、静态展示回退和资源重载。完整 Forge 构建负责真实 API 类型兼容。

生成模型到临时目录（不要覆盖原 v2 模型）：

```text
node tools/model3d/gen_xuanlan_rig.cjs <临时目录>
node tools/model3d/preview_xuanlan_rig.cjs <预览目录>
powershell -NoProfile -ExecutionPolicy Bypass -File tools/model3d/caption_xuanlan_rig.ps1 -OutputRoot <预览目录>
```

六姿态预览直接使用 Java 测试导出的骨骼参数；它是离线姿态检查，不是游戏截图，也不替代实际着色器/多人渲染验收。

## 交付与回滚

主工程：`D:\ft工程文件\FT-SugarQueen-Boss-Mod`。

本轮原源码、资源、工具、文档、美术源文件和改前成品备份于：
`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales Forge\_work\xuanlan-motion-20260918\backup\`。

同级 `project/` 是隔离构建，`preview/` 是离线姿态预览，最终核查及部署记录保存在该任务目录。只经 `tools/deploy_jar.ps1` 同步桌面根、桌面成品目录、152beta3 实例三处；不写旧主实例 mods，不修改其他模组/存档。需回滚时退出 Minecraft，再用安全部署脚本的 `-NoBuild -ArtifactPath` 指向备份 jar。

游戏内验收建议：重启 152beta3，绿枪右键蓄力并松开发射；Shift + 右键切白刃，观察掷出后的收拢与返回扣合；测试副手、第三人称、暂停、回程撞墙、退出重进及最少粒子档。没有声称本轮已经进行实机 FPS/TPS 或着色器兼容实测。

## 本次验证结果

- 动画/轨迹规则：51,030 条断言通过，并直接对最终重混淆 jar 重跑通过；真实客户端状态与模型路由夹具 36 条断言通过。
- 全量 `verify_project.cjs` 十项检查通过；原玄蓝武器接线和原模型/图集逐字节一致性检查通过。既有性能缓存与狗群回归保留通过。
- 隔离 `clean build checkHiddenBossCasts checkWeaponFx --rerun-tasks` 成功；583 项运行资源、1,031 个非目录 jar 条目验证通过。
- 新旧 jar 比对仅包含本轮玄蓝/共用实体分类的类变更，以及 9 个分件 JSON；既有资源、音频、掉落、数值规则和本轮之前的其他功能类不变。
- 成品：40,423,649 字节；SHA-256 `E040DC9268ED53E4E2E4C92316044196CD9687889A86F5C3441F41D3C5CAFF2A`。
- 三处安全部署与哈希校验通过；旧主实例 jar 未改写。离线六姿态图在任务目录 `preview/xuanlan-poses.png`。
