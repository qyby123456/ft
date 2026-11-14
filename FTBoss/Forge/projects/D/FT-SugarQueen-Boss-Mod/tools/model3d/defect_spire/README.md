# 故障机器人 / 尖塔风格重制

本目录是新模型的唯一生成源，不运行旧 `gen_defect_core.cjs`。新资源名 `defect_spire` 与旧模型隔离，`DefectCoreGeoModel` 指向新资源。

## 外观

- 破损黄铜头壳、不对称青蓝镜片、外露机械颈与腰肋、三指机械爪、四段青绿披风。
- 40 根骨骼、149 个立方体，逐面 UV；主体目标密度 1.5 px/模型单位。小面最低一像素，密度可高于目标。
- 眼睛始终青蓝，胸口随现有五类充能球变色；五套匹配的 `_glowmask` 接入现有自动发光层。
- 保留渲染缩放 1.2 与原碰撞尺寸，不改变 Boss 血量、攻击伤害、技能和充能球实体。

## 动作

17 套实际接线动作，24 fps 烘焙：悬浮、漂移、左右转向、五种施法、基础施法、补球、阶段切换、回想、受击、光束、衰弱和死亡。

施法释放关键帧分别为玻璃 0.55s、雷电 0.45s、冰霜/离子 0.3s、黑暗 1.3s，与 `DefectOrbs.releaseDelay` 一致。黑暗在末帧推出，随后由控制器过渡收势。

光束在第 8 秒推出，与现有 160 tick 蓄力相符；保持 14.25 秒资源长度以兼容现有控制器上限。服务器在实际引导结束时同步 `exhausted`，客户端直接转入衰弱，不等资源播完。

客户端动作采用工程现有 `BossActionState`；此次不重写并行工作加入的同步实现。进入跟踪范围时的动画进度与网络延迟仍需游戏内验收。

规格使用右手模型坐标；导出 Bedrock 时 X/Z 旋转取负。预览按 GeckoLib X 坐标反射还原，再复用 `cube_space.cjs` 计算骨骼链。预览不是游戏截图。

## 掉落武器

当前工程此前没有注册故障机器人的专属武器。本次新增 `ftboss:defect_claw`（充能机械爪 / Charged Claw），39 个部件，完整八槽 display，无生成式 parent。

黄铜腕箍、内置握杆、三指弯爪、对握拇指和青蓝电容。新增基础近战武器，使用钻石剑档属性：总攻击伤害 7，攻速 1.6，耐久 1561，史诗稀有度。未添加右键技能或自动电击。

由 `BossMercy.giveWeaponDrop` 在击败机器人时掉落 1 件，创造页可取。怜悯清除仍走 discard，不掉落。武器 `_e` 纹理仅供兼容发光资源环境使用，不声称原版自动自发光。

## 生成与检查

```powershell
node tools/model3d/defect_spire/build.cjs <output>
node tools/model3d/defect_spire/verify.cjs <output>
node tools/model3d/defect_spire/preview.cjs <output> --video
node tools/model3d/validate.cjs <output>/assets/ftboss/geo/defect_spire.geo.json
node tools/model3d/validate_anim.cjs <output>/assets/ftboss/geo/defect_spire.geo.json <output>/assets/ftboss/animations/defect_spire.animation.json src/main/java
node tools/model3d/lint_java_model.cjs <output>/assets/ftboss/models/item/defect_claw.json
node tools/model3d/check_uv_coverage.cjs <output>/assets/ftboss/models/item/defect_claw.json <output>/assets/ftboss/textures/item/defect_claw.png
```

预览使用本机 `@napi-rs/canvas`；可通过 `FT_NODE_MODULES` 指定依赖目录。视频帧写入输出的 `frames`，可用 ffmpeg 转 MP4。

只合入 `output/assets/ftboss` 中本次生成的 16 个文件，不覆盖其他资源。构建与部署使用工程的 `tools/deploy_jar.ps1`，游戏运行时拒绝替换 JAR。

## 实机验收

重启客户端，在测试存档使用 `/give @s ftboss:defect_claw` 与 `/summon ftboss:defect_core`。

检查第一/第三人称左右手握持、物品栏/展示框/落地显示、五种施法起手与实际伤害时点、光束结束转衰弱、死亡跪倒及实际战利品。离线测试不能保证玩家手部矩阵和网络时序完全一致。
