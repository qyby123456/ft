# qys-event · NeoForge 1.21.1

正式工程位置：

`D:/ft工程文件/QYS-Event-NeoForge-1.21.1`

目标环境：Minecraft 1.21.1、NeoForge 21.1.233、Java 21。

## 内容

- 二十四节气天气、普通天气与特殊天气；雨水颜色在客户端统一渲染，粒子仅保留无雨天气和冰雹等少量局部点缀。
- 每日世界事件、预言机和 OP 2 级测试指令；预测文本允许出现错乱或错误内容。
- 天气与事件只在新的一天开始时提醒，避免每 tick 或反复刷屏。
- 许愿井：非绿宝石交互保持静默，只有放置失败提示；石头、木头奖励通过可选标签兼容其他模组的方块物品。
- 事件替代生物带有独立标记，离所有玩家 48 格后经过缓冲期自动清理；重启、登出时清理内存缓存。

## 构建与验证

在本目录使用 Java 21：

`gradle clean build`

运行纯逻辑验证：

`gradle -I run-verifier.init.gradle runQysEventVerifier`

产物：`build/libs/qys-event-neoforge-1.21.1-1.0.7.jar`。

