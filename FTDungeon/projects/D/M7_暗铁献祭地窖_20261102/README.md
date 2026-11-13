# M7 · 暗铁献祭地窖

独立制作工程，2026-11-02（Asia/Shanghai）。只负责第三批 **M7**，不占用正式房间槽位，不修改其他房间、核心源码、已部署 JAR 或玩家存档。

## 交付与接入

- 最小交付目录：`delivery/M7/`，包含 `room.nbt`、`room.json`、`preview.png`、`qa.json`。
- 模板预定资源 ID：`ftdungeonmobs:image25/m7`。槽位、固定原点和大厅道路由唯一接入负责人协调，不在本工程擅自分配。
- 坐标为相对 XYZ：尺寸 **[19,14,19]**，北入口脚位 **[9,3,0]**，安全内点 **[9,3,4]**。北口三格宽、三格高，脚下完整方块。所有尺寸以 NBT 和 JSON 为准。
- 对接聊天：`01a19758-b0e5-75c2-822a-4860527a1e8b`。书面接入报告在 `handoff/TO_INTEGRATION.md`；本工程不自动修改统一 incoming 或 catalog。
- NBT 为 gzip、Java 1.20.1 DataVersion 3465、单一调色板，体积内全部坐标显式记录，包括空气。**这不是清空许可**：正式接入仍须对整个体积、实体和边界预检；占用即拒绝，独立持久化，二次入场不得覆盖玩家修改。

## 原图与转译

参考图：`D:/ft工程文件/image2.5/dungeon-design-batch3-20261101/out/M7-cataclysm-sacrifice-crypt-hero.png`。

完整 SHA-256：`c9b49119f192c80c1d8aca9299efc8787ec1d7cafdb342745c78fea180f057be`。制作前已核验，没有付费重生图，也没有调用图像 API。

原设计标注 13×13、高8格。为实现完整屋顶、三道拱肋、双侧行走空间、合法吊链与三格接口，落地调整为 19×19、高14格。布局和几何原创；Cataclysm 仅作为暗铁、黑曜石、金饰、红符文和哥特拱肋的视觉语言，不复制其模型/贴图。

主题构成：六根落地柱、三道承重拱肋、四条垂直吊链与四盏暖光灯、两组轴向横链、中央三级献祭台、六面红旗、四座头骨台、十组黑烛、两座有护栏的火盆和后墙封闭符文龛。

- 血池采用红玻璃覆盖红石块，不使用不存在的血液方块或自定义流体，不造成溺水/接触伤害。
- 32格真熔岩位于基础内的闭合环槽，底、边、顶六面都有非可燃边界。上盖为完整橙色玻璃或镀金黑石；地面不暴露真熔岩。玩家主动拆封后的风险不在模板初始安全保证之内。
- 火盆使用原版常燃营火，位于实体台座中，四面黑石墙隔离，不在主路上。没有开放火方块、可燃木梁、木桥或危险穿越点。
- 红旗背靠实体外墙；头骨和烛群落地；横链两端接柱；灯链直达完整屋顶。预览剖切不改变交付结构。
- 不含战利品表、奖励库存、刷怪笼、命令方块、结构方块、拼图方块、实体、活动传送门、新战斗逻辑或自动部署逻辑。原版装饰材料仍可由有权限的玩家自行挖掘。

## 证据分级

1. `qa/static.json`：源图和结构指纹、调色板资源可用性、全体积坐标、完整屋顶、基础连通支撑图、吊件/旗帜支撑、熔岩封闭、三格入口与两侧干燥回路。
2. `qa/native-placement.json`、`qa/forge-gametest-final.log`：隔离 Minecraft 1.20.1 / Forge 47.4.10 GameTest；四个方向实际 StructureTemplate 放置；每格原生状态、方块实体、邻接形状、canSurvive、原生碰撞路径及120 tick后稳定性与熔岩边界。
3. `preview/` 和交付预览：从同哈希 NBT、原版方块模型和纹理绘制的软件图，**不是游戏截图**。旗帜/头骨几何简化且不渲染旗帜纹章图案；非全整合包材质或光影验收。

隔离测试不等于完整整合包客户端或多人游玩验收，也未证明大厅道路、正式槽位、入口/返程、难度逻辑和幂等已经接入；这些由统一接入阶段验证。

## 复现

Python：`C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe`，需 Pillow。

依次运行 `tools/build_m7.py`、`tools/verify_static.py`、`tools/render_m7.py`。构建会重建交付 NBT，随后必须重新运行原生 QA 和汇总；不能沿用旧结构哈希的原生报告。

将进程级 `JAVA_HOME` 设置为 `C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot`，在本目录运行：

```powershell
& 'C:/Users/YOUR_USERNAME/.gradle/wrapper/dists/gradle-8.8-bin/dl7vupf4psengwqhwktix4v1/gradle-8.8/bin/gradle.bat' '--offline' '-Dnet.minecraftforge.gradle.check.certs=false' '--console' 'plain' 'runGameTestServer'
```

ForgeGradle 的额外环境联网探测曾阻塞，故仅在此离线构建命令跳过该探测；未更改全局 TLS 校验或 Gradle 配置。所有依赖来自已有本机缓存。运行目录仅为本工程 `qa/isolated-forge/`；验证模组绝不能部署至正式实例。

在保留 `BUILD SUCCESSFUL`、`All 1 required tests passed` 和 `M7_NATIVE_PLACEMENT_PASS` 的最终日志后运行 `tools/finalize.py` 生成汇总、交接和可回滚交付压缩包。
