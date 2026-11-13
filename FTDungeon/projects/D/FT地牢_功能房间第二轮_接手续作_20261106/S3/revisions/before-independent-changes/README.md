# S3 第二轮：冰原霜穴功能房间 11—20

## 接手续作交付状态（2026-11-07）

当前有效目录：D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S3。以下早期说明中的“此S3目录”均指当前新目录。旧组目录只读，十间候选字节保持原状但已在新目录重新运行原生/路线/全方块支撑检查，并逐张复看原参考、提示词、当前真实Lodestone剖切和近景。没有将旧冻结版的通过结果继承到当前合并候选。

- 当前合并 SHA-256：ef5abd63ff1f13c924e359fd84ed5d7bd202f524942fa355f373b35fb1588243。
- 完整合并：rooms/s3-merged/s3-merged.nbt，207×35×184，183526条记录，DataVersion3465。
- 修正走廊末端冰壳侵入稀疏源房空气区的根因；合并保留显式源状态，运行构建必须带 --no-block-state-updates。
- 十房40个目标和合并41个目标的0.6×1.8静态人体路线均已重新通过；没有删除原目标。全部方块canSurvive与空容器/无LootTable检查通过。
- parts/ 中20个分片各轴≤48；逐记录反拼及十个源模板的位置、方块属性、完整方块实体SNBT一致，外围101850个屏障点检查通过。
- 十房与合并均重新按配方构建，NBT逐字节哈希一致；证据在takeover-provenance.json与merged-reproduction.json。
- 新合并已重新生成真实Lodestone完整外壳、剖切、通道内景及高台连密库剖切，已实际目检。单房源未变化，保留同哈希旧真实渲染，接手方已复看。complete-shell-contact.png为十房完整外壳预览拼图。
- risk-audit.json核对实际NBT，未见流体/含水状态、易融冰/雪层或无下方支撑的沙砾；19个植物类记录为花盆装饰，实际原生支撑通过，动态tick未运行。

交付清单为ready-for-review.json，详细差异与待审项为TAKEOVER-REVIEW.md。状态仅为制作方检查完成、等待有效独立复核聊天01a1a685-045b-76d2-aa2b-2a3f24520603审查；不代表独立放行。19号封闭高墙的远眺感、20号高夹道比例仍需独立视觉判断，实际门、容器和工作方块交互均未验证。

复现用当前目录的reproduce.ps1（只写当前新目录）。单房重建后再运行review.py须由操作者实际看图；任何源变化都必须重建相关预览和QA，不能直接沿用当前视觉结论。最终打包核验脚本为finalize.py。

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
