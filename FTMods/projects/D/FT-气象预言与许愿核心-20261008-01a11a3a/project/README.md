# FT 天气预报机 / 预言机 / 许愿井核心重制

- 天气预报机：胡桃木柜体、氧化铜面板、黄铜气压表、天气图示屏、百叶式气象传感器、雨量收集器与风速杯。
- 预言机：完整玄武岩底座、金边木质占卜台、星盘桌面、分层紫晶球、护爪、占卜之眼拱架及符文饰板。
- 许愿井核心：苔青雕石底座、四面许愿铭文、八角翡翠供盘、金色莲瓣和切面翡翠许愿印。
- 使用原版方块 JSON 模型和 128×128 像素材质，不增加渲染器、依赖或运行时逻辑。
- 模型保持单格边界；预言机保留原有四向放置，天气预报机与许愿井核心已由后续朝向补丁补齐水平四向放置。
- 兼容 Forge 旧存档里的 `ftboss:oracle`：仅替换旧版方块和物品的模型父级，指向同一份新模型。
- 模型重制本身不修改交易、合成、天气预报、事件预言、方块注册、碰撞或存档；四向放置由独立朝向补丁提供。

## 文件

`payload/assets/qys_event` 是可安装的模型和材质；`build_assets.py` 可重建。

`install_assets.py` 是初次交付时使用的双版本历史安装工具，不用于本轮最终部署；请勿在当前版本中运行它。最终部署和复核只使用 `final_deploy.py` 与 `final_deploy.py --verify-only`，范围仅为当前 Forge。

Forge 的 qys-bosses JAR 未改动。`install_resourcepack.py` 安装并启用独立的 `FT_Instruments_Remodel_20261008` 资源包，为旧版 `ftboss:oracle` 提供同款外观，也覆盖当前两种仪器。仅追加资源包列表，保留其他游戏设置；`--verify-only` 可复核资源包。旧主模组无法原子替换时不强行写入。

初次 JAR 备份在 `backups`，`installation-report.json` 是历史快照，不代表当前安装状态。最终备份、部署和校验记录位于 `final-release`。

`viewer.html` 直接展示生成的原版模型与材质；`preview.js` 启动本地预览并检查各视角。

预览支持三模型切换、自由旋转/缩放、六向与立体查看、线框、可暂停/调速/拖动时间轴的转台展示和 1.8 格高的人物比例。转台只旋转网页展示，游戏几何和动画不变。所有依赖和数据使用相对路径，可通过统一入口的子路径挂载。

用户主要入口仅使用 `http://127.0.0.1:18790/`；统一挂载配置及交接说明见 `web-handoff.json`，不另建主要入口。

## 最终优化轮

仅移除被不透明实体完整覆盖的内部面，保留外露面 UV、所有体积、旋转、物品变换、材质和功能。天气机面数 330→288，预言机 588→510，许愿核心 342→282；总面数减少 14.29%。旋转元素不做近似剔除；完全隐藏的元素仍保留一面，满足原版JSON解析要求。`optimization/*.json` 记录每个删减面与遮挡证据；`test_optimization.py` 验证拼接、缝隙、旋转、同平面、非空面列表及原模型参数保持一致。

`full_validation.py` 重跑几何、材质、许愿脚本模拟、结构、21视角完整交互和42角度前后图像对比；图像对比允许很小的WebGL抗锯齿差异，不等于Minecraft实机检查。

网页采用按需绘制：暂停且无交互时不重复提交GPU绘制；模型切换、视角、旋转、缩放、线框、窗口变化或动画播放时正常更新。自动检查包含静止状态渲染帧计数，避免为了静态预览持续重绘。

`final_deploy.py` 仅以部署当刻 Forge 当前 qys-event JAR 为基线，独占锁、备份SHA256、逐项合并与原子替换；锁定/基线变化直接停止，不用旧全量JAR覆盖，也不改NeoForge或qys-bosses。本次写入的核心和已启用Forge资源包同样按当前文件备份校验。部署明细与待实机项目见 `final-release/deployment.json`。

早期工程保留在 `revisions/superseded-preoptimization`，只标记 superseded，交由统一通知后续集中审计，不删除。

## 许愿井核心

`build_core.py` 生成 `farmingtales:wishing_well_core` 模型和独立材质；`install_core.py` 是双版本历史安装工具，不用于最终轮。最终核心资源由 `final_deploy.py` 仅写入当前 Forge。Forge 现有仪器资源包也包含这份模型，不新增资源包。

核心几何完全限制在原碰撞框内：底座为 16×4×16，上层为中央 10×4×10。保留方块 ID、两段碰撞箱、亮度、合成、`xyj.nbt` 结构验证、绿宝石投掷/右键许愿和奖励逻辑。未改独立的 `qys_event:wishing_well` 整井模型。

游戏已运行时，资源重载不保证能读取已替换的 JAR，完整退出并重新启动对应整合包最稳妥。

## 四向放置补丁

当前 Forge 的独立朝向补丁见 `_work/event-four-way-20261008/README.md`：天气预报机类、核心 `cardinal` 注册与状态 JSON 及本资源包四向状态不由此次模型最终轮覆盖。最终部署逐项保留当刻现有类、脚本与 blockstate；历史双版本记录不表示当前 NeoForge 已安装最终模型。本轮 NeoForge 只读哈希证据见 `final-release/neoforge-unchanged-verification.json`。
