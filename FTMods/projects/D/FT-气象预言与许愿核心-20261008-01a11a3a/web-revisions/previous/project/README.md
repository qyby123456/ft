# FT 天气预报机 / 预言机 / 许愿井核心重制

- 天气预报机：胡桃木柜体、氧化铜面板、黄铜气压表、天气图示屏、百叶式气象传感器、雨量收集器与风速杯。
- 预言机：完整玄武岩底座、金边木质占卜台、星盘桌面、分层紫晶球、护爪、占卜之眼拱架及符文饰板。
- 许愿井核心：苔青雕石底座、四面许愿铭文、八角翡翠供盘、金色莲瓣和切面翡翠许愿印。
- 使用原版方块 JSON 模型和 128×128 像素材质，不增加渲染器、依赖或运行时逻辑。
- 模型保持单格边界；预言机保留原有四向放置，天气预报机保持原有固定朝向。
- 兼容 Forge 旧存档里的 `ftboss:oracle`：仅替换旧版方块和物品的模型父级，指向同一份新模型。
- 不修改交易、合成、天气预报、事件预言、方块注册、碰撞或存档。

## 文件

`payload/assets/qys_event` 是可安装的模型和材质；`build_assets.py` 可重建。

`install_assets.py` 备份并原子更新两个版本的 qys-event JAR，逐项验证所有非目标资源和类文件不变；同时同步 NeoForge 工程资源。

Forge 的 qys-bosses JAR 未改动。`install_resourcepack.py` 安装并启用独立的 `FT_Instruments_Remodel_20261008` 资源包，为旧版 `ftboss:oracle` 提供同款外观，也覆盖当前两种仪器。仅追加资源包列表，保留其他游戏设置；`--verify-only` 可复核资源包。旧主模组无法原子替换时不强行写入。

`install_assets.py --verify-only` 校验已安装资源。原 JAR 备份在 `backups`，安装路径与 SHA-256 记录在 `installation-report.json`。

`viewer.html` 直接展示生成的原版模型与材质；`preview.js` 启动本地预览并检查各视角。

## 许愿井核心

`build_core.py` 生成 `farmingtales:wishing_well_core` 模型和独立材质；`install_core.py` 备份并同步 Forge 与 NeoForge 的 KubeJS 资源，`--verify-only` 校验安装。Forge 现有仪器资源包也包含这份模型，不新增资源包。

核心几何完全限制在原碰撞框内：底座为 16×4×16，上层为中央 10×4×10。保留方块 ID、两段碰撞箱、亮度、合成、`xyj.nbt` 结构验证、绿宝石投掷/右键许愿和奖励逻辑。未改独立的 `qys_event:wishing_well` 整井模型。

游戏已运行时，资源重载不保证能读取已替换的 JAR，完整退出并重新启动对应整合包最稳妥。
