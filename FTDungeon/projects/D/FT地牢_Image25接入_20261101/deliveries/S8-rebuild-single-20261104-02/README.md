# 第8组第2张：低方台晶室，等待独立验收

唯一参考图S8-02-medium-room.png。S8-01独立登记ACCEPTED_FOR_SEQUENTIAL_PRODUCTION之后才开始本张；登记文件见D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S8-01-ACCEPTANCE.json。S8-01产物未修改，S8-03及以后未启动。

## 实际交付

- incoming/S8R_IMG02/room.nbt：实际Java1.20.1 gzip vanilla structure，25×15×25，完整屋顶与外壳。
- 同目录room.json、preview.png、qa.json、summary.json和materials.json。
- recipe.mjs为独立新配方，没有调用第1张建筑配方或复制其NBT；验证/渲染工具复用不等于建筑复用。
- REFERENCE_ANALYSIS.md说明参考观察、尺寸估计、前后/左右映射和原版材质限制。结构不是第1张缩小换材质：低矩形台、菱形地带、墙内支墩、偏置双向通行、书架/箱子/侧门龛均不同。

## 接口与可达性

坐标原点在西北底部，+X东/+Y上/+Z南；脚位不是脚下方块。

|接口|脚位|朝外|
|---|---|---|
|入口|[12,2,0]|北|
|偏置后拱|[14,2,24]|南|

两口均至少3宽×3高、干燥有支撑，所有周界空气都落在这两个门洞包络内。右后双木门是可手动开闭的小储藏龛，背墙封闭，默认关闭，不是第三个运行接口。箱子为空，不自动添加库存、战利品、刷怪或机关。

完整9目标：安全点[12,2,3]、画面左通行[18,2,12]、画面右通行[6,2,12]、后拱前[14,2,23]、书架前[21,2,20]、箱子前[7,2,19]、木门前[5,2,20]、北口[12,2,0]、南口[14,2,24]。低石台是陈设，不要求玩家登台或触碰晶簇。

assembly.json记录单模块原点、尺寸、哈希、锚点、顺序及旋转。顺时针90度[x,y,z]→[24-z,y,x]，同步旋转原版状态与锚点。未分配世界位置或运行槽位。包围体包含显式空气，接入前检查整个25×15×25体积、实体、液体与边界；有占用即拒绝，不能清空现有世界。

## 验证等级

- qa/static.json：完整唯一体积、边界、地板/屋顶、周界开口枚举、禁用块/流体/实体排除、全部非空气六面连到地基。这是体素支撑图，不是假装结构力学模拟。
- qa/native.json：真实原版注册表、属性枚举、StructureTemplate回读；四向各9目标，采用原版碰撞AABB、0.6×1.8玩家、半格网格、最大半格升降、全段扫掠和净空。含正阶/反阶/整格障碍/低顶控制，不能凭邻近楼梯豁免升降。
- 每个旋转32处簇/灯笼/门静态canSurvive检查通过，直接邻域来自候选NBT；无植物、流体或生命周期标签依赖。本条不代替世界邻居更新。
- qa/native-provenance.json、qa/commands.jsonl、qa/classpath.txt：实际Java源/依赖哈希、精确命令及完整类路径。tools/CollisionRoutes.java为本张可单独复跑源。
- qa/material-independent.json：80份只读结构对照，忽略材质、四向精确占位、归一化内部体素及主层净空；含S8-01直接对照。旧拒绝批量包仅作失败对照，不能恢复候选身份。筛查不是全局语义/体验唯一性证明。
- qa/visual-review.json为制作方目检，不是独立审计结论。

## 实际Lodestone图

views/<方向>/interior-<方向>.png和exterior-<方向>.png，共八张真实bundled Lodestone WebGL图。每个内部方向使用各自剖切NBT，并非旋转同一剖面。qa/cutaway-provenance.json逐坐标证明只把声明的屋顶、近墙和顶梁变为空气；其它状态不变。检查件inspection-only-*.nbt不允许进入候选或部署。qa/lodestone.json校验浏览器实际读取的NBT及PNG哈希。

views/interior-four-views.png、views/exterior-four-views.png为实际截图拼图。preview.png为北向内部截图，书架位于画面左后、木门和箱子位于右后，与参考图相对关系一致。最终结构的完整屋顶/外墙没有被剖去。

## 可复跑

Python使用C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe，而不是WindowsApps占位程序。本目录依次运行：

```
python pipeline.py prepare
python pipeline.py native
python pipeline.py duplicates
```

渲染前在已安装minecraft-builder-skill目录启动服务器：

```
node scripts/preview.mjs --nbt "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-02/incoming/S8R_IMG02/room.nbt" --port 5602 --no-open
```

再在本目录执行python pipeline.py previews；实际目检后运行python pipeline.py review记录制作方说明，再以python pipeline.py package更新四件套与清单。review子命令只记录制作方观察，不会自动完成视觉判断或独立验收。服务器运行时完整结构可交互查看http://127.0.0.1:5602/；截图捕获会在浏览器页面临时覆盖相机和对应NBT响应，不修改服务器文件。关闭服务器后链接失效。

## 状态与边界

AWAITING_INDEPENDENT_ACCEPTANCE：制作方自检通过不是独立登记。registration-request.json仅为落盘请求，不证明已收到/已验收。必须对本张最终哈希独立登记后才可开始S8-03。

世界四向实际放置、400tick、邻居更新、区块重载、Forge游玩和正式部署全部NOT_RUN/NOT_DEPLOYED。原版紫晶为不透明立方体，普通灯笼不会生成参考图紫色泛光，紫色悬挂植物以有支撑倒晶转译；未宣称像素一致或游戏实测。
