# zone-R03 蔓生废祠

独立房间四件套：room.nbt、room.json、preview.png、qa.json。成品尺寸 25×16×27；Minecraft Java 1.20.1，DataVersion 3465，gzip 单调色板，全体积 10,800 条显式记录。

北侧中央入口脚位 [12,2,0]，内部安全脚位 [12,2,4]，主安全点 [12,2,16]，神龛前 [12,2,19]。入口五格宽、五格高，中央五格宽三格高通路可往返。坐标均相对模板原点，脚下地板为 y=1。

参考图：D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/ruins/R03-ruins-overgrown-ruined-shrine.png。
参考 SHA-256：b96b07a7f353a684e808da98efafdc11feca3b17fc72606d9859a8750be8fa58。
成品 NBT SHA-256：5035cce02b72189a0a75310657011031da7d3b58dd9a20ec97390ca0a79df326。
批次依据见 room.json 的 source_documents，原 README、manifest、verify 均只读。本次未调用生图 API。

## 建筑与偏差

保留苔石、四根落地支柱、柱梁网、73 格贴实体石面的藤蔓、挂灯、后殿神龛、圆形祭窗、双浅池、地面倒柱与断柱。四盏暖灯以八节铁链接横梁；两盏灵魂灯和盆栽有实体承托。两处倒柱与残骸都在主路外落地，断柱不承担完整屋顶横梁。

为满足交付契约，原图露天/剖切顶改为 y=13 连续密封屋顶，内含 135 格玻璃采光顶；圆形后窗采用密封绿玻璃与实体背板；蓝色发光蘑菇以原版灵魂灯表达；木箱、木桶替换为石台、盆栽和残骸，无容器、战利品、实体或新刷怪逻辑。浅池共 24 格源水，均位于地面凹槽，有底、有四边，主路不涉水。

## 验证证据

- qa/static.json：14 项通过，覆盖原图哈希、NBT/调色板、坐标唯一/边界、全地基/屋顶、北入口、安全往返、液体四边、所有藤蔓直接实体支撑、挂灯/植物支撑、柱梁贯通、非液体全连通和神龛台座/拱墙背托。
- views/four-direction-review.png：正北、南、西、东四向内部剖视与完整外壳。views/four-internal.png、views/four-external.png 为较大复核拼图，views/axis.png 和 reverse.png 为轴向视角，preview.png 是实际 NBT 等距剖视。剖切仅修改浏览器显示，不改 NBT；剖面切掉的承托面不代表成品悬空。
- qa/visual-review.json：已对四向拼图、轴向与等距视图进行视觉复核，支撑、穿插、入口、倒柱和屋顶无待修项。
- qa/forge-native.json 与 qa/forge-run.log：Minecraft 1.20.1 / Forge 47.4.10 隔离 GameTest。四种旋转共检查 43,200 条状态，原生注册表/全属性/模板往返、canSurvive、0.6×1.8 玩家碰撞通路、柱梁/完整屋顶和液体边界通过。强制邻居/形状更新并等待 240 tick 后，每向保持 10,800 状态、382 可达脚位、73 藤蔓、13 植物、24 水格、675 屋顶格；无掉落和越界流体。
- qa/forge-run-*.log 保留初次编译常量命名修正前的失败记录；最终日志和实际 JSON 为 PASS。checksums.sha256 与 delivery-manifest.json 记录四件套和证据完整 SHA-256。

隔离测试只创建本目录 forge/run/zone-r03-* 的测试世界，randomTickSpeed=0 排除随机植物生长，并强制测试区块。软件渲染不是客户端截图；没有宣称完整整合包客户端、长期藤蔓生长、多人游玩、大厅连路或持久化/占用拒绝验收。

## 复现与交接

使用 C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill 的 scripts/run-build.mjs 生成 recipe.mjs，输出本目录的 room.nbt（--name room --data-version 3465）。随后在本目录执行 node tools/static-qa.mjs 和 tools/run-forge.ps1。后者只运行本目录的独立 Forge 测试工程。

预览使用技能 scripts/preview.mjs --nbt 本目录/room.nbt --port 5573 --no-open，再执行 node tools/capture-views.mjs、node tools/compose-views.mjs。最终 node tools/finalize.mjs 核对静态、原生、渲染及视觉复核哈希并生成 qa.json 和校验表；修改结构后须重新执行对应 QA。

该交付未部署、未访问玩家存档、未修改正式源码/JAR、未写入其他房间或并行线程目录。唯一槽位仍为 null，待统一交接线程 01a19c8f-6747-7572-a77a-6e90084bbd34 分配和独立接收。forge/build、forge/.gradle、forge/run 是隔离生成物，不应随房间导入，也不要把本测试模组安装到游戏。
