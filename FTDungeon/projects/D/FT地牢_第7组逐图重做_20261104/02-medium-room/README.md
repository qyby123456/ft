# 第7组第2张：菌菇静室·紫晶石桌

**S7R2-02 单张制作自检完成，等待独立登记验收；不是已ACCEPTED、已部署或长期稳定通过。第3张未授权，未开始、未预生成。**

前置验收真值：`D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S7-01-ACCEPTANCE.json`。第1张哈希 `0cd7e686e01720603490127393db383a6fbae2894b62ed5bc1137d98d621f9f2` 保持不变。有效复核聊天 `01a1a685-045b-76d2-aa2b-2a3f24520603`；不等待或恢复旧复核聊天。

唯一指定参考：`D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S7-fungal-grove/out/S7-02-medium-room.png`。先读取该图及 `REFERENCE_ANALYSIS.md` 的空间解析，再从零编写本张 `recipe.mjs`。没有复制第1张U形双层主厅或封存旧稿；只复用通用解码/截图方法。

## 最终结构与比例

- `incoming/S7R2-02/room.nbt`：gzip vanilla structure，Minecraft Java1.20.1 / DataVersion3465，**23×14×23**，7406条完整体积记录（包含空气）。不是压进16格；图像不是测绘，23格宽为容纳桌、双侧绕行、壁龛和菇盖跨度的工程尺度。
- **单层布局**：地板y=1，脚位y=2；中央5×5四腿石桌，桌面y=3上半砖、顶y=4，中心紫晶和四角盆栽红菇；桌下中部保持空气。没有中央巨菇阶台、空中回廊、S弯、楼梯或机关。
- **主题物**：两株后侧高红菇（7格柄高、7格盖跨度）、一株前侧低红菇、一株右侧中红菇；左右带4个空桶与工作台的储物壁龛、灯、紫晶、附墙藤、小蘑菇；前景3×2和侧边2×2共10格浅水。
- 屋顶y=13完整；菌土边檐只是贴墙上缘，不是可行走第二层。方块清单 `blocks.json`，原点/单件顺序 `assembly.json`；一个完整NBT足够，不因区块边界缩水，也不无故拆件。

## 接口、锚点与路线

模板局部原点(0,0,0)，+X东、+Y上、+Z南。唯一北侧拱门沿用参考背墙门：

|用途|整数脚位|外法线|外邻脚位|净空|脚下地板|
|---|---|---|---|---|---|
|唯一入口及原路返回口|(11,2,0)|(0,0,-1)|(11,2,-1)|x10..12,y2..5,z0，3宽4高|x10..12,y1,z0，完整石砖|

左右是工作壁龛，不冒充出口；前侧剖切不冒充门。没有第二层接口，不套用S7-01的y9高口。`connections.json`逐格列出接口净空，`rotated-connectors.json`列四向旋转后脚位/法线；必须按实际引擎pivot同步平移，不能忽略边界。

从唯一入口分别通往室内安全点、桌北/东/南/西四侧、左右工作龛、前景干燥区，共8个目标。脚位坐标在room.json；真实玩家水平中心路径、每半格连边可反向返回的结果在 `qa/isolated-run-02/native-result.json` 的 `target_paths`。模型为0.6宽1.8高、y2平面、不跳跃不飞行，每边扫掠完整身体，防止把薄桌面/腿的碰撞当空气。只做单层可达，不伪造楼梯测试。

世界原点和正式槽位未分配。整个23×14×23包含显式空气；实际放置必须先检查全包围盒占用/实体/世界边界，任一冲突拒绝，不无条件清空。模板外锚点的通道由接入方另验，不能用当前边界内净空保证外部安全。

## 真实校验等级

- **静态PASS**：完整唯一坐标、界限、原版命名空间、版本、无实体/发奖/危险执行方块、完整顶层、两池底与四侧密封、四腿低桌和负空间。完整结构连接基础的静态相邻图见 `qa/structural-connectivity.json`。
- **原生PASS（隔离副本）**：实际1.20.1注册表/属性、StructureTemplate加载保存、4空桶BlockEntity加载，绑定由原版mapped JAR解析的172个block标签后58个脆弱相关方块 `canSurvive` 无失败（包括小菇、藤、灯、盆栽、地衣、紫晶），北口3×4原生净空/支撑，8目标双向体积扫掠路径。
- **Lodestone PASS**：最终源完整外壳和只换空气的剖切派生实际由技能默认资源包/headless Edge加载，无页面异常，截图 `lodestone-exterior.png`、`lodestone-interior.png`。源/派生/截图SHA均见 `qa/lodestone.json`。不是游戏截图。
- **重复度局部PASS**：材质无关、水平四旋转/平移归一化实体几何与28份现有结构（旧7组9间、当前2/3组18间、已验收S7-01）无精确重复；同时说明单层低桌终端小室与双层U廊主厅不同。不是全部组语义相似度终验，见 `qa/duplicate-review.json`。
- **软件图单独标签**：四向剖视及完整外壳来自最终NBT，标注软件方块检查图，不能拿它们代替Lodestone或游戏实测。水材质等软件近似可能与实际GPU预览不同。
- **NOT_RUN**：Forge实际世界放置、四旋转邻居更新、随机/流体tick、真实光照传播、长期生长、玩家骨粉/破坏、多人游戏及正式FT整合部署。独立复核的ACCEPTED尚未获得；本聊天不自放行下一张。

## 生长、流体与光照边界

- 两池10格water source静态有y0实体底和y1四周石框。尚未运行放置顺序/邻居/计划流体tick；外部破坏、水桶、接入区块边界可改变局面。静态密封不等于世界永久不漏。
- 小红/棕菇在菌丝上静态存活；原版随机tick仍可能按密度/地表/光照扩散，骨粉可能产生大型蘑菇。桌角盆栽为原版potted_red_mushroom，替代画面台面裸菇，避免悬空/土壤规则；这项材质/外观偏差已明确记录。
- 藤蔓当前附着方向正确，但随机增长/支撑拆除仍可能改变占用和外观；菌丝也可因环境变化传播或退化。没有安装冻结植物逻辑、修改randomTickSpeed或正式配置。
- `canSurvive`在原版标签绑定且亮度假设15的只读LevelReader执行；不是模拟真实光照传播。应按动态NOT_RUN理解，不以该PASS声称所有长期状态稳定。

## 还原偏差与范围

主要构图保留为单层桌面焦点、后门、对置工作龛、红菇高低组和浅水。原图视角省略的前墙和屋顶由最终模板补齐，内部预览只把它们及近侧高檐换成空气。菇盖为原版方块的阶梯近似，紫晶/灵魂灯/菌光体替代图片的蓝紫发光效果；没有实现孢子粒子或自定义光照着色器。工作龛使用原版空桶代替图中复杂抽屉，桌角使用盆栽代替裸菇，均不伪称逐像素还原。

## 复跑（仅这一张）

生成：`node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs recipe.mjs --out-dir incoming/S7R2-02 --name room --data-version 3465 --json`。

验证：用 `C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe` 执行 `verify.py --run-dir <新独立绝对目录>`；目标目录必须不存在。脚本只在隔离副本内编译/执行Java；完整命令参数、准确分号类路径、每JAR哈希和标签快照分别写 `commands.json`、`classpath.txt`、`classpath-files.json`、`vanilla-block-tags.json`。最新实跑目录为 `qa/isolated-run-02`，见 `qa/latest-native-run.json`；早先01保留不冒充最终截图版本。

截图：`node capture.mjs`；封装：上述Python执行 `finalize.py`。`Preview.ps1 -Interior`可打开只读剖切Lodestone；不加参数显示最终完整结构。`views/PREVIEW_ONLY_cutaway.nbt`禁止进入候选池。

唯一交接状态 `REGISTRATION_PENDING.json`；文件哈希 `SHA256SUMS.json`；单张审查包 `S7R2-02-single-image-review.zip`。本包不包含下一张素材或批量制作循环，不写其它组、正式存档、mods、配置或部署。
