# FT 仪器模型重制交付

范围：天气预报机、预言机、许愿井核心。源工程、贴图、效果说明、运行时增量、预览与校验一起交付，不是截图包。

## 工程

- `project/build_assets.py`：天气预报机、预言机及像素材质的可编辑生成工程。
- `project/build_core.py`：许愿井核心及翡翠、雕石、符文材质的可编辑生成工程。
- `project/payload/assets/*/models`：原版 JSON 方块与物品模型，可在 Blockbench 以 Java 方块/物品模型导入编辑。
- `project/payload/assets/*/textures`：独立 PNG 像素材质与挖掘粒子材质。
- `project/effects-manifest.json`：现有效果与本次改动范围说明。模型是静态原版 JSON，没有新增骨骼动画、发光着色器或动态屏幕；原游戏音效和粒子不变。
- `project/viewer.html`、`project/vendor`、`project/preview.js`：使用实际交付模型和材质的独立三维预览。

Python 重建需 Pillow：先运行 `python build_assets.py`，再运行 `python build_core.py`，最后运行 `python verify_assets.py`。在 `project` 目录运行 `node preview.js --serve`，打开输出的本机地址；许愿核心可在地址末尾附加 `#wishing_well_core`。仅启动预览不需要 Playwright；自动视角测试需要 Playwright 和 Microsoft Edge。

主要查看入口统一为 `http://127.0.0.1:18790/`，由“统一通知”协调线程挂载 `instruments` 子页面；本地 `preview.js --serve` 只用于开发验收，不作为另一个用户主要入口。网页支持三模型切换、旋转缩放、六向检查、暂停/时间轴/调速/重置以及 1.8 格人物比例。动画明确为仅预览转台，不是新增游戏骨骼动画。依赖、模型与材质使用相对地址，子路径挂载无需根路径别名。交接配置见 `project/web-handoff.json`。

## 运行时增量

- `runtime-delta/Forge-resourcepack.zip`：1.20.1 资源包，三套新模型及旧版 `ftboss:oracle` 兼容；放到对应版本的 `resourcepacks` 并启用。
- `runtime-delta/NeoForge-resourcepack.zip`：1.21.1 资源包，三套新模型；放到对应版本的 `resourcepacks` 并启用。
- `runtime-delta/qys-event-assets.zip`：qys-event 模组资源增量，仅八份模型/材质，不包含类文件、配方或方块状态。
- `runtime-delta/kubejs-core-assets.zip`：许愿井核心的四份 KubeJS 资源，ZIP 内的 `assets` 目录对应整合包的 `kubejs/assets`；不替换启动或服务端脚本。

最终版本仅部署到当前 Forge：以当刻 qys-event JAR 增量合并模型和材质，更新当前 KubeJS 核心资源及已启用仪器资源包。初次双版本安装记录只作为历史快照；NeoForge 本轮保持既定基线，未安装最终优化资源。不覆盖 qys-bosses 的其他模型编辑，不改整井 `qys_event:wishing_well`。

归档目录中的安装脚本使用原工作目录相对布局，不要直接在归档里执行它们。历史 `install_assets.py` 与 `install_core.py` 会涉及双版本，不用于最终轮。当前 Forge 仅使用源工程的 `final_deploy.py`，执行前须重跑完整校验；该工具按当前基线备份并增量合并，不能拿旧全量 JAR 覆盖。

## 预览与校验

- `project/previews`：三套模型的七向视角及总览。
- `project/asset-verification.json`：模型 UV、材质、旋转角和几何边界检查。
- `project/preview-verification.json`：21 个视角，切换、旋转、缩放、线框和移动端布局检查。
- `project/unified-preview-verification.json`：在实际统一服务 `http://127.0.0.1:18790/instruments/viewer.html` 下复核模型、材质、依赖加载，21 个视角及转台/人物比例控制通过，没有 HTTP 资源错误或浏览器脚本错误。
- `project/installation-report.json`、`project/core-installation-report.json`、`project/resourcepack-installation.json`：安装和资源逐项校验。
- `project/core-gameplay-verification.txt`：结构、投掷/右键许愿、冷却、扣除一颗绿宝石、奖励与命令回归测试输出。
- `reference`：只读的既有许愿井脚本与结构布局，作为现有效果与测试参考，不是运行时覆盖增量。
- `rollback`：本任务安装前的目标模型、材质、qys-event JAR 和资源包设置备份；仅用于精确回滚，不要整包盲目覆盖当前整合包。
- `CHECKSUMS.json`：归档全部交付文件的大小及 SHA-256 清单，不对自身做递归哈希。
- `project/verify_final_archive.py`：独立复核全部归档大小/哈希、当前可编辑源文件、四份增量 ZIP 的 CRC/重复项/最终资源一致性和最终部署备份。

核心的实际几何仍限制在原两段碰撞框：底座 16×4×16，上层中央 10×4×10。许愿事件、亮度、配方、结构验证及奖励分布保持不变。回归测试为脚本模拟测试，本次没有启动 Minecraft 实测；预览光照不等于游戏内光照。

源工作目录原文件保留，归档是复制；独立交付目录不覆盖其他线程成果。

## 最终保守优化与Forge部署

最终面数：天气预报机 330→288，预言机 588→510，许愿井核心 342→282，总共移除180个完整遮挡的内部四边形，减少14.29%。完全隐藏的元素仍保留一面，满足原版JSON每元素至少一面的解析要求。全部原几何边界、外露UV、旋转、PNG纹理、放置朝向、物品变换及碰撞保持不变。删减依据在 `project/optimization/*.json`，8项优化单测和42个视角/转台角度图像回归记录在 `project/final-release` 与 `project/optimization/visual-comparison.json`。图像比较为同一网页渲染器的离线对比，允许每通道大于3的变化像素比例不超过0.01%，不声称逐像素绝对相等或游戏实测。

`project/final-release/deployment.json` 记录最终Forge部署当刻当前qys-event JAR的新旧SHA256、完整备份、8项资源写入列表、非目标条目/类完整保留及18份文件资源逐项校验。不采用以前的完整JAR作为部署基线；独占锁或基线变化直接停止且保留暂存与备份，不重试强写。最终轮没有写入NeoForge实例，没有修改共享qys-bosses。旧 `installation-report.json` 与 `core-installation-report.json` 是初次安装记录，最终状态以 `final-release/deployment.json` 为准。

`runtime-delta` 的4份ZIP已用最终优化资源重新生成，NeoForge ZIP只是可复用资源交付，并未在本轮安装到该实例。`project/revisions/superseded-preoptimization` 及 `revisions/superseded-runtime-delta-preoptimization` 保留早期工程和增量，明确标注superseded，不删除；后续由统一通知集中审计清理。

完整最终回归：`project/final-release/full-validation.json` 与 `test-logs`。仍待实机验证资源烘焙、方块相邻光照、物品栏及第一人称摆放、井内交互、音效和粒子效果；离线模拟检查不能代替这些项目。

网页实现增加按需绘制：静止/暂停期间不持续提交GPU绘制，有交互或播放时正常刷新。`preview-verification.json` 与 `unified-preview-verification.json` 中的 `idleDemandRenderingPassed` 验证暂停帧计数稳定；这是网页实现优化，不声称实测Minecraft帧率提升。
