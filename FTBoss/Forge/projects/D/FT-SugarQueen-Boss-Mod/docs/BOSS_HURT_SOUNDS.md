# Boss 受击音效

所有 10 位非庄园 Boss 和庄园十二人直接使用 Minecraft 原版受击音效，不附带自制或外部素材。音量为 0.55，音高随机范围为 0.94–1.06。

| Boss | 音色 |
|---|---|
| 糖霜女皇 | 原版玩家受伤声 |
| 屠夫 | 原版劫掠兽受伤声 |
| 沙耶 | 原版玩家受伤声 |
| 酒桶王 | 原版铁傀儡受伤声 |
| 鼠群首领 | 原版蠹虫受伤声 |
| 故障机器人 | 原版铁傀儡受伤声 |
| 苹果王 | 原版玩家受伤声 |
| 炫彩答辩超人 | 原版玩家受伤声 |
| Benger | 原版狼受伤声 |
| 潮宴鱼王 | 原版守卫者受伤声 |
| 庄园十二人 | 原版玩家受伤声 |

`BossHurtSoundMixin` 接入 `LivingEntity.playHurtSound` 的原版受击时机，不单独监听伤害事件，不新增受击频率，不重复播放 Benger 的原版狼受伤声。沿用实体音效类别、距离衰减及静音规则。小老鼠、其他召唤物、分身和非 ftboss 实体不使用这些音效。

`BossHurtSounds.soundFor` 直接返回 `SoundEvents` 的原版对象，无额外 SoundEvent 注册。游戏使用原版素材及其字幕。此前生成的 33 份 Ogg、自定义音效事件及对应中英字幕已从源码和运行 JAR 移除。

本次增量、受击改动前的运行包及被移除的音频备份保存在 `_work/boss-vanilla-hurt-sounds-20261012/`。不会同步桌面、fix2 或 NeoForge 实例。

## 验证与部署

- `BossHurtProfileCheck` 验证十一种实体使用原版受伤音效及命名空间隔离；`check_boss_hurt_playback.cjs` 使用轻量 Minecraft/Forge 替身运行真实播放组件，检查 2200 次单次播放、原版音效映射、音量音高、静音与小鼠排除。
- `check_boss_hurt.cjs` 验证自制音频、注册和字幕均已移除，开发环境/运行环境 Mixin 目标保留；`check_boss_hurt_jar.cjs` 比较成品与实际运行包，要求仅修改两份受击类、清理三份 JSON 的自制音效键并移除 33 份自制 Ogg，其余文件逐条字节一致。
- 独立构建、重混淆、JDK ZIP 流读取与 Forge ZIP 规则校验通过；首次完整 `gradle build` 因既有 `FoodBossInventoryRewards` 缺 FTB Quests 编译依赖、`GongdiRunFrame` 缺动作采样器等共 49 个错误未通过，没有改动这些无关功能，也没有声称完整构建成功。
- 部署使用 `tools/deploy_boss_hurt_sounds.ps1 -TaskRoot <本次工作目录>`：运行中拒绝安装、双部署锁、运行包基线哈希检查、备份、原子替换及资源索引缓存清理，仅写当前 Forge 1.5.2 实例。
- 未进行真实游戏内听感验收；替身测试与字节码/资源检查不等同于 Forge 启动和实机音频测试。
