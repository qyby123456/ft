# QYS Event 独立导出 · Forge 1.0.6

本轮只导出独立 Event 模组，不合并进 ftboss，也不包含 Boss 模组。

## 运行文件

`qys-event-forge-1.20.1-1.0.6.jar`

- Minecraft：1.20.1。
- Forge：47.4.10 或以上的 1.20.1 版本。
- 可选季节兼容：Serene Seasons 9.1.0.3 系列。
- 不直接依赖 ftboss Java 类，按注册名可选联动 Boss 实体和效果。
- 保留节气天气、每日世界事件、预言机、天气预报机和许愿井系统。
- 保留预言机、天气预报机四向放置，以及针对 `farmingtales:wishing_well_core` 的旋转/镜像补丁。
- `farmingtales:wishing_well_core` 本体由整合包 KubeJS 脚本注册；该脚本及其资源包不包含在这个独立模组 JAR 中。此补丁不会自行创建核心方块。
- 保留旧 `ftboss:oracle` 和 `solarweather:weather_forecaster` 方块/物品的注册名迁移。

## 验证

导出内容与已经通过离线检查的独立 Event 候选包完全一致，版本号没有人为递增。

SHA256：`9fc66cd0a2cfb7079e0712227385cabd7657cdaa402ab1f143ac56c99ec142f1`

- JAR CRC、重复条目检查通过。
- 许愿井核心旋转/镜像及作用范围检查：60 项通过。
- 预言机、天气预报机及 KubeJS API 检查：21 项通过。
- 每日事件、特殊天气和相关效果规则检查通过。
- 实际 Forge 启动、旧世界迁移、Mixin 应用和游戏内交互尚待验收。

## 安装注意

这次操作仅导出，没有修改当前实例的 `mods`、KubeJS、资源包或存档。不要同时启用多个 QYS Event JAR。

当前 ftboss 中旧 Event 实现的剥离仍是待安装候选；若要部署到当前实例，应先关闭游戏并完成该分离，避免同时保留旧 Boss 预言机注册。此导出包没有包含或替换 ftboss。
