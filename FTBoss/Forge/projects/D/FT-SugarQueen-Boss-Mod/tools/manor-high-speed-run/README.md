# 庄园高速跑步：原生方法级合并桥

主 `client/manorrun` 目录保存本轮 Mojmap 源码。正式包还包含历史 JAR-only 人物装饰器与功底 renderer；不能以不完整的主工程全量包覆盖它们。

- `PatchRun.java`：在最终 `EasterBossModel.setupAnim` 返回前加入新跑步，在功底 `Timeline.select` 后、actor 构造前加入实际 frame 跑步；对既有 `RunAnimator.prepare/apply` 添加仅限庄园变体的提前返回，保留麻雀分支并避免双层跑步。
- `VerifyDelta.java`：验证 Java 17 与 ASM 栈；去除两个新调用和两个守卫后，三个共享 class 的全部方法及元数据恢复基线。除明确合并点外不得替换其他人物动作或资源。
- 完整编译源码、API 检查、状态机、原生 IK、完整人物链探针、当前贴图四视图与安全部署脚本：`D:\ft工程文件\_work\manor-high-speed-run-20261010`。
- 已部署记录与独立备份见该目录 `deployment-report.json`。只有通过当前正式基线、受控完整姿态链和最终预览一致性检查后，才能使用主 `tools/deploy_jar.ps1 -NoBuild -ArtifactPath ... -TargetPath ... -ExpectedTargetHash ...` 合并部署；必须持共享部署锁并先确认游戏关闭。

不要直接复制历史完整 JAR，不要对已经含有本轮调用的模型重复插入。后续更新只替换审核过的私有 helper，或重新审计共享合并点。

范围、迟滞阈值与实机待验边界见 `docs/manor-high-speed-run-20261010.md`。
