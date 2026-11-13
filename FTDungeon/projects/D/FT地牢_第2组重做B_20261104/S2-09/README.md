# S2-09 砂日纹饰宝藏室

第2组第9张候选，一图一交付，待独立复核。第10张未开始。Java1.20.1 / DataVersion3465，29×18×29，交付文件 `incoming/G2B-09/room.nbt`。正确参考为 `D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-09-treasure.png`；哈希见room.json与registration.json。

独立新建的宝藏室包含橙黄菱形后墙、嵌纹地面、抬高宝座主宝箱、两侧雕像、低位藏物箱和侧拱道。北接口Y1，经两级台阶到主厅Y3；东西接口Y3；宝座Y5。中央真实敞顶并非预览隐藏屋顶。设计近似和玩法适配见ANALYSIS.md。

三只单箱均有真实minecraft:chest方块实体和固定原版物品，主箱4堆、侧箱各3堆。原版ChestBlockEntity原生读取与isChestBlockedAt静态检查通过，箱上两格空气，箱前三格高净空；三处接近目标均经过玩家身体路线验证。实机箱盖动画、菜单打开、拿取、猫挡箱、多人与战利品平衡均NOT_RUN。

自检覆盖原版注册表/属性/模板回写、20个带原版标签的静态存活点、21个完整身体路线目标（3接口+18目标）、固体六面连接到Y0、6处砂支撑、8盏灯支撑与3箱实体。复用独立审查工具不代表已经独立验收；0.6×1.8身体离线形状检查不是世界物理模拟。

原有mapped.jar在触发新类加载时出现过时签名digest异常。本候选只读使用已有local-verification.jar；finalize.py逐字节比对所有class与原mapped.jar一致，差异仅META-INF元数据。详情见qa/native-runtime-provenance.json。没有修改两个jar或正式环境。

7张实际Lodestone浏览器截图：exterior/reverse/detail加载完整源NBT；north/south/west/east加载空气替换剖切副本。所有派生保留三只箱及完全相同的实体NBT，移除坐标和哈希见derivation.json。preview-only不可部署。

源码recipe.mjs、材料materials.json、参考哈希、路线和容器检查、动态边界、原8份已验收源的哈希复核、文件交接清单均已提供。FILE_MANIFEST.json记录交付文件SHA256（不将自身或Python缓存列入）。

复现顺序：技能run-build.mjs recipe.mjs --out-dir incoming/G2B-09 --name room --data-version 3465；prepare.py；validate-native.ps1；capture.mjs；finalize.py。需要已有的本地技能、node/Python环境与原版映射jar/classpath，路径保留在脚本中。

未放置正式世界、未部署、未改mods或配置。天气、刷怪、自然光、邻居更新、随机刻、旋转镜像和交互玩法均未运行，完整空气包络投放前需要全体积覆盖审查。
