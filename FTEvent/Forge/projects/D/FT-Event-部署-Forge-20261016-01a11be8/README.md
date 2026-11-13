# Event 1.0.7 部署记录

本轮用户明确要求部署，因此将 1.0.7 从离线候选变为启用状态。仅部署 FarmingTales_ForgeⅡ v1.5.2 当前实例，不涉及 NeoForge、Boss 内容或存档。

最终成功回执：deployment-complete.json；实际事务与前置备份见 attempt-02/deployment-receipt.json 和 attempt-02/backups。

## 已安装

- mods/qys-event-forge-1.20.1-1.0.7.jar，SHA256 B0D747554C43A2A7C4796DFE2B0CD480A90654624EE33328010274398A6B8C1C。
- kubejs/server_scripts/block_events/wishing_well.js，SHA256 77F44ED9470D833FC682ADEFDB8AB9A82690A036E19268A19E4D1B269D3AF4A7。
- 原 1.0.6.jar.disabled 保留且未改写；扫描所有启用 JAR 的 mods.toml，确认 qys_event 仅启用新版本一份。
- Boss JAR、许愿井启动注册、核心模型/方块状态与金币脚本部署前后哈希一致。没有写入 NeoForge、资源包或存档。

## 验证与限制

部署后直接从已安装 JAR 加载概率类，穷举 100000 个类别签和 10000 个矿物签通过；已安装 KubeJS 脚本语法检查通过。文件哈希与经过两次可复现构建的候选一致。

未启动 Minecraft、未进入世界或执行游戏内购买/掉落验收。下次完整启动当前 Forge 实例加载新 JAR；仅 /reload 不能加载新 Mod。

## 回退材料与过程记录

备份脚本为 attempt-02/backups/wishing_well.js.before；禁用旧包的备份也保存在该目录。回退前应先关闭游戏，将新 JAR 恢复禁用并还原备份脚本；不要自动启用旧版本，不未经备份与单独授权打开原存档。

根目录 deploy.ps1 与 deployment-receipt.json 保留第一次尝试的历史：PowerShell 把原子替换调用中的空值转换为空路径，替换前即停止，实例脚本和活动 JAR 均未改变。修正后的部署脚本与成功回执在 attempt-02，第一次遗留的非活动暂存文件已核对哈希并移至 aborted-attempt-staging，不留在运行目录。
