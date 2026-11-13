# S3-05 冰苔木撑直廊 — 仅第5张候选待独立验收

唯一参考：`D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-05-corridor.png`。已只读核验04 R2独立ACCEPTED记录和实际NBT哈希。未读取/制作06；不改前四张清单或部署目录。建筑recipe为本张新写，不导入先前建筑；复用原生验收与viewer截图工具。

## 图像解读与离散比例

原图1536×1024是一条近低远深的单轴石廊，两侧木撑贴近墙面而非殿内独立柱阵；地面混合冰斑与雪，墙根冰笋、顶部冰挂、青色植被、零散骨骸、暖灯连续引导至远端冰口。近侧木柱约1～2格宽、柱基约3格，廊内横跨约13～17格；可辨约5～6对木撑，透视不能证明精确总长。采用21×16×49的包围盒、内宽17、6对8格间距木撑，是参考比例转译而非从像素精确反推。

|要素|本张NBT|
|---|---|
|贴墙木撑|x3/17，z5/13/21/29/37/45；3×3灰石柱基Y2～4，单木芯Y5～11，短木挑梁、石帽；非04的3×3独立柱身|
|低顶连续廊|侧顶底Y11、中央约Y14，横向石肋与多组冰挂；最终顶面Y15完整封闭，非露天|
|冰雪步道|脚位Y2，完整地板Y1；冰雪与石地混合，中央x8..12连续5格宽；只两个相对端口，无侧门/旁室/绕中央物路线|
|暖灯与植被|12盏暖吊灯+12链；180格附墙发光地衣近似青色藤纹，邻墙方向明确，不用悬空藤蔓冒充有支撑|
|冰笋/骨骸|两侧墙根短冰柱与4组小骨块，远端冰框衬口；没有中央晶台、骨架龛或箱子|

具体近似边界：方块冰挂/冰笋不能等同原图尖细半透明晶体；满格snow_block及齐平地面雪斑代替薄雪，骨块只是小型骨骸符号；glow_lichen的灰绿贴图/发光不能精确复制原图青藤。末端只转译为可连接的冰边出口，不添加传送机制，也没有收进未知下游洞穴。原图细小台阶/雪厚不全复现，主路统一同层以保持可接入。

## 坐标与连接

原点[0,0,0]，+X东、+Z南；图无罗盘，人工指定前→后为北→南。北入口脚位[10,2,0]，南出口[10,2,48]，安全点[10,2,4]；门洞保守净空5宽4高，地板Y1，上部可见拱高不算接入必须净空。route-targets.json含10个边界门脚位、14个检查目标及地标。接入另一房间时，两门面相邻而不共面覆盖，朝向相反；按rotated-connectors.json补偿引擎pivot，90/270度包围盒49×16×21。未实机旋转放置。

本体显式空气覆盖完整21×16×49体积，未来接入前必须检查整个体积的占用、实体、世界边界、玩家建筑，冲突就拒绝，不得先清空。入口脚位Y2不得硬编码Y1。

## 检查与真实边界

static-qa.json验证DataVersion3465、16464唯一完整坐标、仅原版ID、无实体/方块实体/容器/命令/活流体、准确2端口、中央5格宽净空、1029列屋顶、所有非空气体素面连地面以及剖视与最终NBT准确只删为空气。体素附着不是世界力学。

NativeQA.java读取真实1.20.1映射jar及170个原版block tags，验证注册表及每个显式属性、StructureTemplate.load/save，遍历全部记录canSurvive（含附墙地衣和灯链）。14个目标用0.6×1.8身体、0.5格网格和0.25格边中点、固定脚Y2、原生碰撞形状检查可达；native-qa.json保留每步路线。它不是连续扫掠、跳跃、冰面滑行或战斗模拟。

暖灯原版亮度15，使用不融化的packed_ice/blue_ice和snow_block，无普通ice、无snow雪层、无活水。静态canSurvive不能证明世界光照、邻接更新、随机刻、区块重载、真实玩家冰面移动；这些及Forge实机均NOT_RUN，不能冒称实机通过。没有刷怪、奖励、机关、战利品或运行时槽位。

## 源NBT视图与独特性

interior-01..04.png是同源只删剖面四个对角方位，exterior-01..04.png为完整NBT四向外景；eight-view-sheet.png为缩略拼版。nave-full-nbt.png保留工具兼容文件名，实际为本张完整低廊内部相机[10.5,5.5,2.5]朝[10.5,6.5,45]，不是04长殿或剖视；仅Playwright响应内覆盖viewer相机，未修改安装脚本或NBT。来源和各PNG哈希在view-map.json。

本张拓扑是无分支直线通道：贴墙支撑、低顶冰挂、脚下冰雪及短距离侧边障碍形成持续推进。04是高顶宽殿、殿内双排独立柱、四个侧门加两轴口及分流侧廊；05没有缩小复制该结构。01中央晶台绕行、02低池休息、03单口壁龛也不在本张出现。冰木灰石主题及暖灯相同是参考组内语汇，不能据此宣称跨组重复度已验收；最终视觉还原/重复度仍待独立复核。

## 复现

在`C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill`运行run-build.mjs，本张recipe.mjs导出到当前目录，--name room --data-version 3465；cutaway.mjs导出到preview-only/。依次运行inspect.mjs、Check-Native.ps1。后者在新独立Temp目录复制NBT、编译执行，只复制日志回本张，不写游戏目录。

```powershell
$room = 'D:/ft工程文件/FT地牢_第3组重做B_20261104/S3-05'
node scripts/run-build.mjs "$room/recipe.mjs" --out-dir $room --name room --data-version 3465
node scripts/run-build.mjs "$room/cutaway.mjs" --out-dir "$room/preview-only" --data-version 3465
node "$room/inspect.mjs"
& "$room/Check-Native.ps1"
```

完整viewer端口5325，剖视5326，均用官方scripts/preview.mjs --nbt路径 --port端口 --no-open启动；之后运行capture.cjs和finalize.mjs。finalize核验04独立放行、前四张各自完整清单、当前来源哈希及逐文件读回。待本张独立ACCEPTED后才可06；本地PASS不是独立放行。未部署到saves/mods/config/统一incoming。

```powershell
node scripts/preview.mjs --nbt "$room/room.nbt" --port 5325 --no-open
node scripts/preview.mjs --nbt "$room/preview-only/preview-only-cutaway.nbt" --port 5326 --no-open
node "$room/capture.cjs"
node "$room/finalize.mjs"
```

两个viewer命令各占一个独立终端，启动后才在第三终端截图/定稿。缩略拼版使用System.Drawing将八张实际PNG按内景上排/外景下排各四张缩至360×275，不用于替代完整PNG证据。
