# 血棘祭炉祭坛厅 · 图三 / M1

按用户六宫格中的**第三张：M1 Bloodthorn Chapel（血棘祭炉祭坛厅）**制作。原图来自 `D:/ft工程文件/image2.5/dungeon-room-bloodthorn-chapel-20261101/out/dungeon-room-bloodthorn-chapel-hero.png`。

**占地15×15格，墙高8格；含基础和完整屋顶，NBT尺寸XYZ=15×12×15。** 适配Minecraft Java/Forge1.20.1，全部使用原版方块，不需要新增建筑模组。

![血棘祭炉祭坛厅](preview/bloodthorn-chapel-design-sheet.png)

## 已完成的建筑

- 中央5×5阶梯祭坛：双层灰黑石台、血色砖和红玻璃意象、铁框常燃营火、四座烛柱。玩家可走到祭坛台面和火盆旁。
- 四只完整悬挂铁笼：3×3笼体、深色底座与顶盖、内部灯笼、铁链和接入完整屋顶的深色橡木横梁。没有悬空无锚点的链条。
- 后墙血棘图腾：深色木骨架、绯红菌柄和栅栏分枝、头骨核心；四面深红长幡、垂落血色藤蔓。
- 裂纹地坪、暗红地毯及斑驳砖、分层烛龛、骨块与头骨、四个空储物桶和一册真正可阅读的《血棘祭仪残卷》。
- 右侧遗物龛采用铁门和两侧石质压力板；没有脚本锁死玩家或单向陷阱。
- 北向主入口朝-Z。完整成品含四墙和屋顶；预览剖去了近侧墙和顶板，以便观察内部，成品并未缺墙缺顶。

这是**建筑与叙事版**，不包含刷怪波次、Boss、收费、通关奖励、传送或世界自然生成。无自动tick函数。

## 原版替代说明

参考图是AI概念图，并非原版逐格蓝图。暗灰石砖由深板岩砖、裂纹深板岩砖与雕纹深板岩实现；红幡为原版旗帜图案，不是破布新模型；血池用血色方块和红玻璃表达，没有引入真正血液流体、雾气或动态符文。

中央火盆由**原版营火、黑石底座和铁活板门框**组成，具有真正原版火光。它不是现有FT五档火盆，也没有冒用尚未注册的自定义 `bloodthorn_brazier`。已存在的 `原创火盆_血棘祭炉_整数方块_20261101` 模型工程保持不变，未强行当作可放置方块使用。

## 独立成品

- `out/ft-bloodthorn-chapel-1.20.1.zip`：建筑数据包。
- `out/FT_Bloodthorn_Chapel.nbt`：独立结构模板。
- `out/datapack/data/ftbloodthorn/structures/chapel.nbt`：数据包内相同的结构。
- `out/materials.json`、`out/manifest.json`：材料数量、尺寸和参考图/成品哈希。
- `preview/`：设计总图、完整外观、轴测剖面、祭坛近景和平面图，共五张。

### 测试放置

1. 备份测试世界，将ZIP放入该世界的 `datapacks` 文件夹，执行 `/reload`。
2. 用 `/function ftbloodthorn:help` 查看说明。
3. 在创造模式，站在预留空域的**西北角、最低基础高度**，向+X、+Z各预留15格、向上预留12格。整个包围盒须为空气且相关区块已加载，不能有其它实体。
4. 执行 `/function ftbloodthorn:build`。成功后自动移动到入口内侧，面向内部。

若在Y=64执行，基础最低格为Y=64，主地面方块为Y=65，主厅脚部行走高度为Y=66。入口前两格台阶连接到厅内，不会主动挖空地下或拆除已有建筑。

原生模板名为 `ftbloodthorn:chapel`，管理员也可用 `/place template ftbloodthorn:chapel <x> <y> <z>` 或结构块读取。**原生放置和结构块绕过本包防覆盖检查**，不要在已有建筑上盲放。独立NBT未自动复制进实例的schematics目录，机械动力导入未在客户端实测。

## M1独立接入版

按总协调追加要求额外制作，**不替换独立数据包，不修改FT核心、不部署、不写入存档**。

- 本工程暂存：`out/integration/M1/`。
- 协调交付：`D:/ft工程文件/FT地牢_Image25接入_20261101/incoming/M1/`。
- 四件套：`room.nbt`、`room.json`、`preview.png`、`qa.json`。
- 元数据：`id=M1`，`size=[15,12,15]`，`entrance=[7,2,0]`，`interior=[7,2,3]`，`facing=north`；`sha256`针对room.nbt原始字节。
- 北面严格3宽×3高开口；开口脚下完整门槛位于模板内部，入口通道为两格深。其余三墙、门框和整面屋顶完整。
- 从入口可到安全内点、祭坛前、祭炉旁台面、后墙图腾和侧门内踏板。火盆与可燃木材最小切比雪夫格距为5；用营火而非会蔓延的裸火，不设置岩浆。
- 接入版仅修改北墙、门槛和顶板连接，独立版ZIP/NBT保持原样。S1墓狱囚室未重复制作。

先运行 `tools/build_integration.py` 生成暂存件；通过原生验证后用 `tools/build_integration.py --publish` 发布。发布器要求最终room.nbt哈希匹配原生验证结果，遇到不同的既有交付会拒绝覆盖。

## 检查与限制

- `qa/geometry.json`：12个关键点两格净高通路、四笼完整性与锚点、烛群/头骨/书台支撑、铁门上下半部和双向踏板、空储物桶。
- `qa/datapack.json`：2700格全空域预检查、函数资源引用、无自动施工与危险范围指令，命令链低于原版65536限制。
- `qa/minecraft-format-verification.json`：真实Minecraft1.20.1注册表检查方块属性、NBT模板往返、方块实体与书籍、全部命令解析，以及M1接入版的独立NBT往返和哈希。
- `out/integration/M1/qa.json`：接入版3×3入口、内置门槛、完整墙顶、可达性、吊链与木材距离验证。
- 软件预览来自最终NBT及本机原版模型/贴图，不是游戏截图。旗帜、头骨、讲台书实体简化，不模拟真实光照、烟雾、粒子和帧率。
- 营火接触仍会造成伤害；没有主动刷怪不等于禁止世界自然刷怪。具体难度、光照、保护插件、红石改动和游戏内效果，需在测试存档实测。
- 删除数据包不会自动拆除已建建筑；防覆盖检查不等于崩溃可回滚事务，仍需存档备份。

本次未新增收费图片请求，未改参考图、旧地牢、既有模型工程或玩家存档；原生格式验证不冒充实机游玩验证。

## 复现

Python3.10+与Pillow：

```text
python tools/build_chapel.py
python tools/render_chapel.py --textures-jar "Minecraft-1.20.1-client.jar"
python tools/build_integration.py
gradle --offline --no-daemon runData
python tools/build_integration.py --publish
python tools/verify_delivery.py
```

Gradle工程中的验证模组仅用于开发检查，不应装入游戏mods目录。
