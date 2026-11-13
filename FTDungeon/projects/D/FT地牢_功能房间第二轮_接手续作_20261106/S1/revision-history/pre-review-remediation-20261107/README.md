# S1 苔藓古林第二轮：接手续作 11—20

本目录继承旧 S1 十房候选及配方，旧目录只读。继承来源为 D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S1，逐文件 SHA-256 在 inheritance-manifest.json；十份初始 NBT 复制前后哈希相同。未发现属于本组的 node/python/java 残留进程；没有终止其他组任务，没有写 saves/mods/config/KubeJS，没有生成付费图片。

已阅读 WORK_ORDER、references、progress、README、revision-status 和独立验收目录 S1 最新 visual-review 及 ALL-SUPPORT 补审。独立验收旧冻结版本结论 REQUEST_CHANGES；没有继承整组通过，也没有把制作方调用独立工具当作独立放行。

## 整改与最终状态

- 15 训练场：保留原四个路线目标，新增 A 字坡七个表面脚点及三个跨栏分区目标；最终哈希 de6760c4485588e01844bfb5242b969615a5e13786d98589b481e526d1819906 下全部 14 目标原生碰撞检查通过。先纠正了工具将 x/z 输入额外加 0.5 的坐标约定，没有移动几何或删除目标。跨栏跳跃物理仍未测。
- 16 水井：发现继承配方已更新而 NBT 未重建，已真实重建、渲染；新增贴墙根系曾堵住原入口脚点，失败 NBT 与路线保存在 16-cistern/rejected-port-root。修复 enhance.mjs 对原入口内侧缓冲区避让，原目标保留并全部通过。
- 17—20 已逐图查看参考和提示词并完成新 NBT、Lodestone 内外图和原生检查。18 的头颅/骨块近景、19 的桌面望远镜替代近景、20 的藏宝近景均实际查看；20 是密道藏宝。
- 19 新支柱曾挡住原高位出口；失败候选与路线保留在 rejected-upper-port-column。修复后原高差、阶梯、出口目标均保留并通过。池水另检出 16 处朝空气的潜在外流邻格，已补封池底/侧壁，rejected-water-air 保存失败证据，最终潜在直通空气项为零；未执行真实水流刻。
- 11—15 随公共构件修复重新生成并核验；没有重新设计或抛弃已完成功能。十房最终哈希、原生状态/全非空气 canSurvive/路线报告及图片已交叉核对；十份当前配方在 reproduction 中独立输出，reproduction-report.json 证明逐份 NBT 字节完全一致。

## 最终交付

ready-for-review.json 和 handoff-for-independent-review.json 已写入，供独立复核聊天 01a1a685-045b-76d2-aa2b-2a3f24520603 读取；状态是 ready_for_independent_review_not_accepted，independent_acceptance=false。

完整合并：merged/s1-mossy-functional-11-20.nbt，尺寸 492×34×74，SHA-256 d21d7d5046155969902c665eb940f1010e3c6c70863415bf707569e03313a766。采用有顶主廊加十个保留原房的支路，不旋转房间，按原接口方向处理转弯；19 号高位出口另设半格坡道回接主廊。保留原房每个显式空气、方块状态、方块实体载荷及两具盔甲架载荷。

merged/qa 真实运行了 Java 1.20.1 原生状态/模板回读、0.6×1.8 碰撞路线、全非空气 canSurvive。76/76 路线目标可达；240330 个非空气格支撑失败 0、不支持 0。上述为制作方离线自检，不是独立验收或真实世界模拟。

merged/chunks 共 22 份，每边≤48；merged/integrity.json 记录原房逐格保真与分片反拼。回读反拼 368975 条方块记录，全部方块状态、方块实体和实体载荷一致。外围联合包络六面屏障位于原房范围外，内部连接路线已一并检查。没有将旧八组固定布局当作本组布局。

完整外观及分段剖视在 merged/complete.png、cutaway.png、atlas-11-13.png、atlas-14-17.png、atlas-18-20.png、connector-watch.png；真实 Lodestone 显示副本按 hiddenRanges 剖切，源 NBT 未裁切。单房总览在 complete-contact.jpg、cutaway-contact.jpg、function-detail-contact.jpg。

old-source-readonly-verification.json 对交接清单内 227 个旧文件重新计算哈希：无修改。初始继承哈希与最终哈希分开保存，不把继承旧证据冒称新候选通过。

qa-room.ps1 只使用当前目录配方与当前输出目录；check.py 的 ROOT 为本文件所在目录。takeover.py 是首次迁移记录，已执行，不要重复执行以免重复追加路线目标。revision-setup.py 等继承辅助脚本尚未审定，不作为交付入口。

## 复现与证据

依次执行 `./qa-room.ps1 <room>`，再用工单 Python 执行 `check.py <room>`。Java 单进程堆上限 1536m；qa 内报告绑定当前 NBT SHA-256。完整外观 complete.png、剖切 cutaway.png 及焦点图均由真实 Lodestone WebGL 渲染；剖切只删除显示副本，完整 NBT 保留墙顶。

合并复现顺序：先确保十房当前 QA 有效，运行 merge.py，再运行 merge-views.py 和 `node render.cjs <当前目录>/merged`，随后 `check.py merged`，最后 finalize.py 与 publish.py。reproduce.py 单独复现十房 NBT 并比对哈希。公共工具与原生类路径均保留在脚本及 qa/commands.json，原生检查类、mapped jar 和标签来源只读。

新哈希必须重新验证。继承的历史报告需按哈希核对；存在报告不代表新候选通过。progress.json、delivery-index.json 与 ready-for-review.json 指向本目录最终候选及真实证据，未把自检标为独立放行。

未执行世界自然刻、液体/重力/光照更新、实际按键开关门、实体显示/交互、生存或战斗验收；这些都不声称通过。独立视觉审查和独立整组放行仍待完成。
