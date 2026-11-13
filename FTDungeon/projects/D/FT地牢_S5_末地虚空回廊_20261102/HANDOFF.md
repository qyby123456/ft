# S5 末地虚空回廊 · 独立交付报告

状态：静态 QA、7 个破坏性负例、Minecraft 1.20.1 / Forge 47.4.10 实际四向放置 QA 均通过。未部署、未写正式存档、未改核心或其它房间。

## 交付

- 目录：`D:\ft工程文件\FT地牢_S5_末地虚空回廊_20261102\out\S5`
- 四件套：`room.nbt`、`room.json`、`preview.png`、`qa.json`
- 尺寸 XYZ：7×8×13；北入口脚部 [3,3,0]；安全内点脚部 [3,3,10]；朝向 north。
- 模板本体 Y=2 地板，Y=3 脚部，Y=7 完整封顶。入口净宽/高 3×3，地板必须保留；连接道路要适配脚部高度 +3。
- 保留 5×11、净高 4 的设计内腔，两侧陈设使净主路维持三格。两组玻璃封护裂隙、六根有支撑末地烛、两朵 age=5 紫颂花、两块 eye=false 门框残片。
- 728 条显式方块记录（包括空气）、36 格双向安全地面；无奖励库存、容器、流体、实体、方块实体或可激活传送门。

## 指纹

- 原始 S5 参考 PNG：`dc508de8c23a414740405a67eb1169553a3337b398dbdee54297ab9bdcf73bcf`
- gzip 结构 SHA-256：`92d4e37edfafeffa144915c505cc3a0b95dd4df981ed2500f1509c433ab6d67d`
- 解压 NBT SHA-256：`0d3374e5fc280b4f2a2030d00538f21de7186bae39e39cd70bd34cc46b9292f1`
- 规范方块语义 SHA-256：`1bf5f3ed9e6992a82190e0f2b1d5a1e7f0542a3a402bf9b381e1f2986d199845`
- 独立交付指纹：`1cd6046b2c35293f83da007ead50d5a8a34bf103768b544cc4984a4058a02c75`
- room.json SHA-256：`dc133fdd3b10fe453d1c298bb2a3ef1cf6aaa62a5b65a6461f22c8b774161e30`
- preview.png SHA-256：`ef799676cc6bd14b6cc28287fc435f62bfe530ccaf815c39cafb613bb32f140c`
- qa.json SHA-256：`0d1fa3e9db60fb294eb7dab120d229f2a8c644efe85a493ae556d5db538f62b7`

独立交付指纹的载荷与算法见 qa.json，不等同于依赖 slot 的 DungeonRoomCatalog 运行时指纹。未擅自选择目录槽位；由唯一接入负责人计算最终 slot/origin/fingerprint。

## 原生证据

- `D:\ft工程文件\FT地牢_S5_末地虚空回廊_20261102\qa\native-placement.json`：四种 rotation，各两次 StructureTemplate.placeInWorld（共8次），邻居更新后和100 server tick后逐一比较728状态；原生 canSurvive、0.6×1.8 玩家碰撞体、三格连续干燥接近区/通路、非入口护边、玻璃地板及末地烛/紫颂花支撑全部通过。
- `D:\ft工程文件\FT地牢_S5_末地虚空回廊_20261102\qa\forge-native-console.log`：`S5_NATIVE_PLACEMENT_PASS`、`All 1 required tests passed`、`BUILD SUCCESSFUL`。
- `D:\ft工程文件\FT地牢_S5_末地虚空回廊_20261102\qa\forge-run\logs\latest.log`：原生服务器日志。
- `D:\ft工程文件\FT地牢_S5_末地虚空回廊_20261102\qa\negative-checks.json`：地板缺口、入口阻挡、末地烛失去基座、活跃紫颂花、带眼门框、屋顶缺块、通路熔岩七个破坏性负例均被拒绝。
- `D:\ft工程文件\FT地牢_S5_末地虚空回廊_20261102\delivery-manifest.json`：全部最终文件哈希和证据哈希。

## 必须如实转交的边界

参考是 image2.5 七次失败后的本地 THREE 方块示意渲染，不是 AI 成功图，也不是实机证据。preview 是从最终 NBT 读取的原版纹理软件预览，仅预览移除屋顶和近侧遮挡，NBT 本体完整封护。

原生验收在独立 GameTest 世界完成，不冒充完整整合包客户端实机游玩、多人/FPS或大厅接入验收。原生重复放置不等于核心生成标记的幂等验收。未加入 catalog、incoming 或已部署 JAR，未修改门/火盆/五档/玩家返回记录。后续由接入线程 `01a19758-b0e5-75c2-822a-4860527a1e8b` 追加固定槽位与道路、复验占用拒绝和生成标记，再协调部署。
