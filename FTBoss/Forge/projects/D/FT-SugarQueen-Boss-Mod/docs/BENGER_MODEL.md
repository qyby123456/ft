# Benger 黑白边牧模型

按用户 2026-09-16 提供的黑白边牧头像重新制作 Boss 本体。参考图只约束外观，不改变 Boss 的技能、数值、掉落或碰撞箱。

## 外观与兼容

- 尖耳与深灰内耳、额头连续白纹、低垂眼睑、黑色鼻头与较长的白色吻部。
- 分层黑色颊毛、白胸鬃、白袜、五节白尖尾。保留红项圈和叼棒战斗特征。
- 27 根骨骼、122 个方块，512×512 独立逐面图集；主体目标密度 2 px/模型单位，不是整张脸贴照片。
- 四肢沿用大腿→小腿→爪，躯干沿用胸→脊柱→腰；保留全部 9 段动画的名称、时长和关键帧取值。
- 下颌、耳根、叼棒挂点按新外观调整；其余骨骼接口保持兼容。
- `BengerRenderer` 继续引用原有三个资源路径，虚化渲染不变。跟班幼犬使用相同资源、0.55 倍缩放，因此同步获得新外观。
- 静态耳尖约 3.52 格；宽度小于原有 1.7 格碰撞箱。四足纵深沿用原设计的大体尺度。

## 真源与重建

`tools/model3d/benger_spec.cjs` 管几何与骨架，`benger_paint.cjs` 管逐面绘制，`benger_animation_spec.json` 保留原动画数值。统一入口：

```powershell
node tools/model3d/gen_benger.cjs art/benger_reference
node tools/model3d/validate.cjs art/benger_reference/benger.geo.json
node tools/model3d/validate_anim.cjs art/benger_reference/benger.geo.json art/benger_reference/benger.animation.json src/main/java
node tools/model3d/anim_key_order.cjs --check art/benger_reference/benger.animation.json
node tools/model3d/verify_benger.cjs art/benger_reference
node tools/model3d/preview_benger.cjs art/benger_reference
```

预览和专用检查脚本需要 `FT_NODE_MODULES` 指向安装了 `@napi-rs/canvas` 的 Node 模块目录；模型生成器本身只使用 Node 内置库和仓库 PNG 工具。

输出包含 `.geo.json`、`.animation.json`、PNG 图集、内嵌贴图与动画的 Blockbench 工程、UV 布局、九姿势图和验证报告。预览使用正式几何与正式贴图，并复用 `cube_space.cjs` 的骨骼变换。

## 验收边界

- 已验证结构、父级顺序、逐面 UV 不重叠且不透明、动画引用与时间键序、动画值不变、24 fps 采样下四肢及尾链 AABB 不分离、可重复生成。
- 通用动画检查器会提示原有 idle 的四条小腿轨道只有一个恒定关键帧。这是保留的原始内容，不在模型重做中改动动作数据。
- AABB 检查只是粗筛，不证明任意旋转姿势下每一处表面都没有穿插；毛层和关节本来需要重叠。
- 离线图不是游戏截图；需重启客户端后实际召唤 Boss 验收。不得宣称已经完成游戏内验证。

## 部署与备份

旧生成器与旧资源保存在 `_backup/benger_reference_20260916/`。正式资源位于 `src/main/resources/assets/ftboss/{geo,animations,textures/entity}/`。

构建后只通过 `tools/deploy_jar.ps1` 部署；游戏运行时必须跳过替换。退出客户端后执行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/deploy_jar.ps1
```

不使用 `-Force`，不关闭用户的游戏进程，也不在运行中清资源缓存。
