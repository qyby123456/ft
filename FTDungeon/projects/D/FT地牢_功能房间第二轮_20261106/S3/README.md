# S3 第二轮：冰原霜穴功能房间 11—20

本组仅在此S3目录写入。参考索引为references.json；工单为上级WORK_ORDER.md。按11→20逐张实际看图、单独配方、NBT、原生检查与Lodestone真渲染；十张完成后才进行新合并。progress.json记录实际进度，不把制作方自检等同于独立验收。

## 产物与复现

- room11.mjs等为逐房间配方；common.mjs仅提供落块、长方体、冰挂与灯具等底层助手，不生成通用房盒。
- rooms/s3-XX包含完整结构NBT（Java1.20.1/DataVersion3465）、design.json（尺寸、入口出口脚点/净宽/净高、功能靶点、差异）、cells.json（制作落块清单）、summary.json及完整/展示剖切PNG。
- qa.py复用旧组只读原生1.20.1检查类，Java单进程、堆上限1536MB，在当前房间qa目录运行；日志、调用参数、工具哈希与候选哈希均留存。
- render.cjs启动技能官方preview.mjs，通过真实Lodestone WebGL渲染当前NBT；剖切只作用显示副本，不修改交付NBT。
- review.py在实际看图后登记制作方视觉复核，核验参考图片哈希与候选/预览报告一致。

命令示例（在本目录）：

```powershell
node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs ./room11.mjs --out-dir ./rooms/s3-11
C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe ./qa.py s3-11
node ./render.cjs s3-11
```

## 检查边界

原生工具校验实际方块注册/属性、StructureTemplate回读、部分脆弱装饰canSurvive，以及0.6×1.8人体、半格网格、最高0.5格抬脚、采样扫掠碰撞路线。上下文相关碰撞未作保证，工具详细限制见各qa原始报告。仅空容器，不投放生存奖励；蓝冰/浮冰替代易融冰，优先无流动液体/无熔岩方案。未执行真实世界放置、邻居更新、流体/随机tick、真实照明、生存或战斗验收，全部保留待复核。没有写入saves/mods、旧八组或其他线程目录。

## 当前修正记录

S3-11首轮书架阻挡东侧出口，R2贯穿书架形成3×4通道；R3将过长的横穿顶梁缩为后墙嵌入梁，改善与参考图的一致性。前两轮候选与报告保留在rooms/s3-11/qa/r1、r2。独立验收均为PENDING。
