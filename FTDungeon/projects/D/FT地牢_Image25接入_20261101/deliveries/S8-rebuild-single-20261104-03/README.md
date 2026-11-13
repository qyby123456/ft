# S8-03 单箱壁龛小晶室

状态：制作侧校验完成，等待独立验收。仅本张；S8-04未开始、未授权。没有部署或修改正式存档、mods、配置。

## 正式交付

- `incoming/S8R_IMG03/room.nbt`：完整外壳、屋顶和显式空气，17×12×17，DataVersion 3465。
- SHA-256：`8db768f622ed5f0b676edea272993162d35297f95c8349b4059eb8dd2b42569c`。
- `room.json`记录局部坐标、参考图/配方哈希、箱体和连接点；`assembly.json`记录单片装配信息。
- 北入口[8,2,0]、东出口[16,2,7]；主地面方块Y1，玩家脚位Y2。箱体[8,3,9]，箱前[8,2,7]。
- `views/`下NBT仅供剖切检查，不能代替正式NBT放置。四张外观图读取完整正式NBT。

## 已有证据与边界

`qa/static.json`检查边界、完整体积、地基/屋顶、实体与方块白名单、六面支撑连通和门口范围。`qa/native.json`及provenance记录原版注册表、StructureTemplate往返读取、真实碰撞形状采样路线：四个旋转方向各九个目标无跳跃可达，各26处脆弱方块静态canSurvive检查。此路线是0.6×1.8身体、半格采样和扫掠AABB的补充检查，不等于游戏玩家实走。

本张箱体显式携带`id=minecraft:chest, Items=[]`，没有LootTable。原版BlockEntity.loadStatic读取该实体并确认空箱；不宣称四向世界放置或开箱交互已测。

`qa/lodestone.json`记录八次浏览器截图、实际读取NBT及图片哈希；`qa/cutaway-provenance.json`核对切除范围外方块状态和方块实体负载保留。`qa/visual-review.json`是制作侧对照观察，不是独立通过结论。壁龛背板紫色偏连续、地面纹样偏规则、原版晶体不透明等偏差已记录。

`qa/material-independent.json`是81项去材质几何筛查：未发现精确重合或内部IoU≥0.9；不是全局语义独创性保证。前两张仅作为只读对照。

世界放置、400tick、邻居更新、区块重载、实际开箱、Forge实机均为`NOT_RUN`。NBT显式空气会覆盖目标空间；任何部署前必须检查整个体积与世界边界，不得自动清场。

## 复现

使用已配置的Python运行`pipeline.py prepare`、`native`、`duplicates`；`previews`需要本技能Lodestone预览服务器监听5603端口与浏览器依赖。参考图与前两张只读审查依赖路径见脚本。实际命令、classpath、日志位于`qa/`。重新生成后须重新检查预览和视觉报告，再运行`pipeline.py package`刷新清单。`checksums.json`覆盖交付文件（排除自身与__pycache__）。
