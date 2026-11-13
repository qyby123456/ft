# S4-01 沼泽古树主室

## 独立复核修订1

旧哈希 `9c8461f6a47a9e66e30e72d9579400daa923de757103e6823a659275b660e353` 被独立复核判定 NEEDS_REVISION。旧版NBT、配方、截图、登记和校验清单保留在 `revision-history/rejected-9c8461f6/`，均不进入本次候选。

实际几何修复：在(17,20,25)补一格 persistent oak_leaves，为(17,20,26)的north=true藤蔓提供北侧附着面。其余结构不变，不新增屋顶，不跨组、不开始S4-02。修复后仍待独立复验，不自放行。

`qa/VineStability.java` 对所有藤蔓和睡莲调用原版canSurvive；对所有藤蔓逐一调用六方向updateShape并反复迭代到固定点，要求无藤蔓消失或失去支撑。此为内存LevelAccessor检查，不是临时世界随机刻、流体刻或游戏游玩测试。

### 本次可复跑命令

```powershell
$python = 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe'
$root = 'D:/ft工程文件/FT地牢_第4组逐图返工_20261104/S4-01'
& $python "$root/review.py"
& $python "$root/compare.py"
node "$root/capture.cjs"
& $python "$root/finalize.py"
```

其中review.py会重新复制builder产物并校验元数据中的全部12个route_waypoints：北(24,5,0)、东(48,5,24)、南(24,5,48)、西(0,5,24)，台座北(24,8,17)、东(31,8,24)、南(24,8,31)、西(17,8,24)，外圈(5,5,24)、(43,5,24)、(24,5,5)、(24,5,43)。同时检查四锚点5×4净空、液体边界和睡莲支撑。

本次截图服务器端口5415，指向实际incoming/S4-01/room.nbt；capture.cjs会回读HTTP提供的NBT，要求与交付文件逐字节一致并登记SHA256。启动命令：

```powershell
node 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs' --nbt 'D:/ft工程文件/FT地牢_第4组逐图返工_20261104/S4-01/incoming/S4-01/room.nbt' --port 5415 --no-open
```

原版映射JAR残留签名在LevelAccessor反射加载MinecraftServer时触发签名错误；review.py仅在本目录qa生成移除陈旧签名/清单的隔离测试副本，类字节不变，来源及哈希见fixture-provenance.json。原始依赖与正式实例不修改。

## 状态

仅返工第4组第1张，未开始第2张。全新程序几何，不导入旧候选。旧第4组文件保持不变，不属于本次候选。

本张已通过本地自检，提交独立验收；不等于协调方接受、正式整合或游戏实测。验收登记见本目录 `acceptance.json`。

## 实际文件

- `incoming/S4-01/room.nbt`：交付用 vanilla structure NBT，gzip，Java 1.20.1 / DataVersion 3465。
- `incoming/S4-01/room.json`：参考图指纹、尺寸、原点、方向、四向连接点、路线点和偏差。
- `nbt/s4-01-swamp-heart-rebuild-v1.nbt`：builder 原始输出，与交付 NBT 内容一致。
- `recipe.mjs`：独立构建配方；通过 minecraft-builder-skill 的 run-build.mjs 生成。
- `preview-reverse.png`：实际交付 NBT 的 Lodestone 预览，没有用概念图冒充游戏结构。
- `preview-default.png`、`preview-night.png`：另一侧及夜间检查。
- `qa/native-review.json`：原版 Minecraft 1.20.1 方块注册表、属性、碰撞通路、StructureTemplate 加载及保存回读。
- `geometry-check.json`、`repetition-check.json`：液体边界、睡莲支撑、禁用方块及本地重复度证据。

## 尺寸与参考图

结构边界为 49×31×49；可见最高方块 y=28，边界留有余量。不是 19×19 提示词名义尺寸，也没有为单区块压缩。

按 PNG 中可见方块比例，采用约49格跨度。外缘走廊宽4格，桥面宽5格，中央底台23×23，顶台17×17；三段楼梯由桥面 y=4 上到顶台 y=7。古树中心约(24,24)，树冠横跨约28格，北/西高墙约18格，南/东为6至10格左右破损矮墙加拱门。

保留环水盆地、中央多级台座、偏弯主干与根、宽树冠、悬挂蛙明灯、原木扶壁、苔石拱门和四向木桥。四入口经台座外围干燥环路互通，不需要穿过水、攀根或跳跃。水深2格，有完整池底和边界。

注意：参考图是带雾和光影的插画，无法给出唯一精确方块计数。本结构尺寸是视觉估算；原版材质下水色、树叶颜色和光照不同；没有伪造雾气与萤火虫实体。前侧低墙是实际遗迹形态，不是隐藏正式版围墙的假预览；整个建筑露天，没有额外屋顶。

## 原点与连接

NBT 原点(0,0,0)为西北底角；+X东、+Y上、+Z南。以下为玩家脚部中心，桥面方块统一位于 y=4：

| 方向 | 脚部锚点 | 保证通道 |
| --- | --- | --- |
| 北/入口 | (24,5,0) | 宽5，净高至少4 |
| 东/出口 | (48,5,24) | 宽5，净高至少4 |
| 南/出口 | (24,5,48) | 宽5，净高至少4 |
| 西/出口 | (0,5,24) | 宽5，净高至少4 |

单件 NBT 无拼接顺序。若北锚点拟连接世界脚部坐标(A,B,C)，放置原点为(A−24,B−5,C)。必须保留完整49×31×49占用检查，不得只检查显式方块；NBT稀疏坐标不承诺清空既有地形。

49格超过结构方块默认保存范围，不要尝试用默认48格保存尺寸重存。由整合方使用原版 StructureTemplate 或数据包的结构模板加载方式导入；本交付没有执行任何世界放置命令，没有改动存档、mods、KubeJS、配置或NeoForge。

## 验证与重现

在 `C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill` 下运行：

```powershell
node scripts/run-build.mjs 'D:/ft工程文件/FT地牢_第4组逐图返工_20261104/S4-01/recipe.mjs' --out-dir 'D:/ft工程文件/FT地牢_第4组逐图返工_20261104/S4-01/nbt' --data-version 3465 --json
node scripts/preview.mjs --nbt 'D:/ft工程文件/FT地牢_第4组逐图返工_20261104/S4-01/incoming/S4-01/room.nbt' --port 5414 --no-open
```

用已配置 Python 运行 `review.py`，会更新交付副本、元数据、独立几何检查，编译本地隔离的原版验收器并运行；`compare.py` 对可用本地自然主题结构快照做去重比较；`capture.cjs` 用 Edge headless WebGL 拍摄真实 Lodestone 视图。

原版通路自检发现初稿东/南长树根挡住台座环路，已将根端缩回内圈并重新生成、重新加载验证。当前通路检查覆盖四边界锚点、四个台座侧面点和外缘走廊点；1271个原版碰撞可达脚部位置。没有实机玩家行走或方块随机刻验证。

重复度检查读取了101个去重后的本地 NBT（含旧失败对照、其他组和预览裁切文件），没有相同几何；最高归一化内区实体占用 Jaccard 为0.3448。此指标只是粗筛，不代替逐图视觉独立验收，也不覆盖未来其他线程尚未产出的结构。
