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

## 运行时增量

- `runtime-delta/Forge-resourcepack.zip`：1.20.1 资源包，三套新模型及旧版 `ftboss:oracle` 兼容；放到对应版本的 `resourcepacks` 并启用。
- `runtime-delta/NeoForge-resourcepack.zip`：1.21.1 资源包，三套新模型；放到对应版本的 `resourcepacks` 并启用。
- `runtime-delta/qys-event-assets.zip`：qys-event 模组资源增量，仅八份模型/材质，不包含类文件、配方或方块状态。
- `runtime-delta/kubejs-core-assets.zip`：许愿井核心的四份 KubeJS 资源，ZIP 内的 `assets` 目录对应整合包的 `kubejs/assets`；不替换启动或服务端脚本。

原工作目录已经完成安装：两个版本的 qys-event JAR 仅改模型和材质；两个版本的许愿井核心写入 KubeJS 资源。Forge 的已启用仪器资源包同时提供三个模型与旧预言机兼容。不覆盖 qys-bosses 的其他模型编辑，不改整井 `qys_event:wishing_well`。

归档目录中的安装脚本使用原工作目录相对布局，不要直接在归档里执行它们。可用资源包安装；若需要重跑原安装流程，应把工程复制回对应整合包的 `ftboss_model_edit/ft_instruments_remodel_20261008`，先备份现有资源。

## 预览与校验

- `project/previews`：三套模型的七向视角及总览。
- `project/asset-verification.json`：模型 UV、材质、旋转角和几何边界检查。
- `project/preview-verification.json`：21 个视角，切换、旋转、缩放、线框和移动端布局检查。
- `project/installation-report.json`、`project/core-installation-report.json`、`project/resourcepack-installation.json`：安装和资源逐项校验。
- `project/core-gameplay-verification.txt`：结构、投掷/右键许愿、冷却、扣除一颗绿宝石、奖励与命令回归测试输出。
- `reference`：只读的既有许愿井脚本与结构布局，作为现有效果与测试参考，不是运行时覆盖增量。
- `rollback`：本任务安装前的目标模型、材质、qys-event JAR 和资源包设置备份；仅用于精确回滚，不要整包盲目覆盖当前整合包。
- `CHECKSUMS.json`：归档全部交付文件的大小及 SHA-256 清单，不对自身做递归哈希。

核心的实际几何仍限制在原两段碰撞框：底座 16×4×16，上层中央 10×4×10。许愿事件、亮度、配方、结构验证及奖励分布保持不变。回归测试为脚本模拟测试，本次没有启动 Minecraft 实测；预览光照不等于游戏内光照。

源工作目录原文件保留，归档是复制；独立交付目录不覆盖其他线程成果。
