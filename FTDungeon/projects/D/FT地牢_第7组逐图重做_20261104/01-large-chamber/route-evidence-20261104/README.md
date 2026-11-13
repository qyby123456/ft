# S7-01 HOLD 补证：路线、边界和预览绑定

本记录仅补证，不修改候选，不自行登记放行。当前有效独立复核聊天为 `01a1a685-045b-76d2-aa2b-2a3f24520603`，审计目录 `D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685`。旧 `01a19c8f` 不作为有效验收方。未向其它聊天主动发消息，补证在本聊天留总结等待独立复验。

**状态仍为 HOLD，S7-02禁止。** 原始单张ZIP、旧清单、旧预览和最终NBT均未改写；新增文件不重写旧哈希清单，不假装被旧ZIP覆盖。

## 冻结源

- 最终源：`D:/ft工程文件/FT地牢_第7组逐图重做_20261104/01-large-chamber/incoming/S7R2-01/room.nbt`
- SHA-256：`0cd7e686e01720603490127393db383a6fbae2894b62ed5bc1137d98d621f9f2`
- 模板29×20×29；局部原点(0,0,0)，X东、Y上、Z南。整数feet是脚所在方块格，体积寻路使用其水平中心，即(x+0.5,y,z+0.5)。世界原点没有分配。
- 本次所有Java编译、碰撞测试和日志仅在本目录的独立副本 `isolated-run-*` 内执行，未运行ServerLevel、游戏、mods或正式存档。

## 最新完整结果

以 **`isolated-run-04/`** 为最终补证版本：

- `route-collision.json`：7个目标的完整逐步路径；每条边保留抬升/水平/下降的精确扫掠waypoints，含反向返回验证；42个楼梯方块原生形状和踏面；左右各3条走道共6条完整上下楼梯分段记录；四接口原生3×3净空和支撑格。
- `coordinate-evidence.json`：连接口脚位、外法线、外邻接格、3×3每一净空格、全部目标及两侧楼梯各踏面和全部分段坐标。
- `commands.json`：实际成功执行的javac/java完整参数数组、工作目录、Java源码SHA；不是省略类路径的伪命令。
- `classpath.txt`：本次准确完整分号类路径。`classpath-files.json`绑定每个JAR绝对路径与SHA，包含真实1.20.1 mapped jar及依赖。
- `compile.log`、`run.log`：真实编译/运行输出。终端能力与JDK Unsafe弃用警告不冒充验证失败，也不隐藏。
- `media-bindings.json`：Lodestone完整外壳/内部截图SHA，实际显示NBT SHA与最终源SHA；软件四向/外壳图独立标签，非Lodestone、非游戏截图。
- `RESULT.json`：补证局部通过，仍待独立复验；世界/邻居/随机tick/流体tick全部NOT_RUN，下一张false。

## 四连接口

|口|整数脚位|外法线|3×3开口范围|完整地板格|
|---|---|---|---|---|
|北下层新增FT口|(14,2,0)|(0,0,-1)|x=13..15,y=2..4,z=0|x=13..15,y=1,z=0|
|西下层|(0,2,14)|(-1,0,0)|x=0,y=2..4,z=13..15|x=0,y=1,z=13..15|
|东下层|(28,2,14)|(1,0,0)|x=28,y=2..4,z=13..15|x=28,y=1,z=13..15|
|北上层|(14,9,0)|(0,0,-1)|x=13..15,y=9..11,z=0|x=13..15,y=8,z=0|

四口每一净空格在本次原生复跑中确认为air；每一脚位下方调用实际原版 `isFaceSturdy(UP)` 为true。端口外是未知世界，不能由模板边界内净空推断外接走廊安全。上下北口相差7格，不能错当同层相连。

## 两侧楼梯和七目标

西侧x=2,3,4，东侧x=24,25,26。每侧7排台阶，所有42个方块均为 `stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]`。第s排（s=0..6）块坐标为 `(lane,s+2,26-s)`，向北上升、向南下降。

- 每条走道低层入口中心 `(lane+0.5,2,27.5)`，高层出口中心 `(lane+0.5,9,19.5)`，再到廊内检查格 `(lane+0.5,9,18.5)`。
- 原生踏面低侧中心 `(lane+0.5,s+2.5,26-s+0.85)`，高侧中心 `(lane+0.5,s+3,26-s+0.35)`；这是可站身体采样点，不是把整数脚格当作踏面高度。
- 每条走道从z=27.5到19.5按0.25格步长连续扫掠，记录32条段；每段同时验证上行和反向下行，不允许跳跃/飞行，不借另一侧楼梯绕路。
- 七个整数脚目标依序为：`(14,2,3)`、`(0,2,14)`、`(28,2,14)`、`(14,9,0)`、`(3,9,18)`、`(25,9,18)`、`(14,5,16)`。完整机器路径在JSON中，不仅提供这7个终点。

## 与旧“附近有楼梯”判据的区别

旧 `qa/VerifyDeliveries.java` 的离散寻路仅在高差边检查本格/邻格下存在StairBlock，不充分证明实际朝向/踏面净空。本补证不修改旧报告，而明确其限制，用独立 `RouteCollisionAudit.java` 补充更强证据：

1. 从冻结NBT副本解码真实方块状态，通过只读邻域BlockGetter取得原生碰撞形状；不把楼梯当全方块或默认空气。
2. 明确断言42个楼梯朝向北、下半、straight，取原生碰撞AABB并测试高低踏面支撑。
3. 玩家尺寸0.6宽×1.8高，扫掠整个体积，不只检查脚格和头格；水平步长0.25、高差每次0.5（小于原版步高0.6），不允许无支撑、涉水、跳跃或飞行。
4. 每次上升先扫掠身体向上0.5的空间，再扫掠水平；下降先水平再下落0.5；逐段检查真实碰撞盒和头部净空。每步起终点均有支撑，非“旁边有楼梯就能上”。
5. 对七目标路径每条边再验证反向扫掠；两侧六条固定楼梯走道另外独立验证上下，不依赖全局BFS选路。

本次BFS可达13821个采样节点，七目标通过。此方法是保守的离线几何可行路径，不是运行ServerPlayer步进物理或世界AI导航，不声称覆盖所有可能动作、跳跃、移动速度和网络碰撞。

### 保留的试跑记录

`isolated-run-01` 初次审计将高踏面探针设在z+0.15；0.6宽身体会伸到下一排更高台阶，因探针过靠边导致“高踏面碰撞”判定。未据此认定或修改候选；把可站高踏面采样移到z+0.35后在新副本复跑。01失败日志完整保留，不篡改为PASS。

02为七路径/踏面初步通过；03新增六走道完整双向轨迹；04再加入四连接口逐格原生检查。最新源码SHA与04命令记录绑定，不能拿01/02旧源码结果冒充最新完整检查。

## 复跑准确命令（只写新的独立副本目录）

PowerShell执行：

```powershell
& 'C:\Users\YOUR_USERNAME\.codex\.venvs\imagegen\Scripts\python.exe' 'D:\ft工程文件\FT地牢_第7组逐图重做_20261104\01-large-chamber\route-evidence-20261104\run_audit.py' --run-dir 'D:\ft工程文件\FT地牢_第7组逐图重做_20261104\01-large-chamber\route-evidence-20261104\independent-rerun-01'
```

该目标必须尚不存在，否则拒绝覆盖。脚本先核对冻结源SHA，再复制NBT与Java到新目录，在副本中编译、运行和输出；原结构不写入。若希望审计方另选独立目录，只更换 `--run-dir` 为一个新路径。

独立副本内的Java命令等价为（准确依赖列表读取该次生成的classpath.txt，而不是猜路径或只给一个mapped jar）：

```powershell
$run = 'D:\ft工程文件\FT地牢_第7组逐图重做_20261104\01-large-chamber\route-evidence-20261104\independent-rerun-01'
$cp = Get-Content -LiteralPath (Join-Path $run 'classpath.txt') -Raw
& 'C:\Program Files\Common Files\Oracle\Java\javapath\javac.exe' -encoding UTF-8 -cp $cp -d (Join-Path $run 'classes') (Join-Path $run 'RouteCollisionAudit.java')
& 'C:\Program Files\Common Files\Oracle\Java\javapath\java.exe' -Xmx1G -Djava.awt.headless=true -cp ((Join-Path $run 'classes') + ';' + $cp) RouteCollisionAudit (Join-Path $run 'room.nbt') (Join-Path $run 'route-collision.json')
```

完整来源类路径：mapped jar `D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar`，附加项按 `D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt` 每个非空且不含 `minecraft_user_repo` 的行原样拼接。实际绝对项及SHA都在04结果中，独立复跑可比对。

## Lodestone与软件图证据绑定

- 完整外壳：显示的就是冻结源，显示NBT SHA与最终源相同；截图为 `lodestone-exterior.png`。
- 内部：显示明确派生 `views/PREVIEW_ONLY_cutaway.nbt`，SHA `0016942370bb3a84a423d4a67ce136ec0a3270ce4fd3ccbee51f831aa4de5176`；源SHA仍记录为冻结源。剖切删屋顶、屋顶边梁、西和南边界墙，不是最终结构修改，禁止派生进入候选。
- 本次重新核对显示NBT和截图哈希；没有伪造新渲染或称为原版游戏截图。原始Lodestone报告源/派生绑定无变化。
- `views/north.png`、`south.png`、`west.png`、`east.png`、`exterior.png`继续标注 `SOFTWARE_BLOCK_RENDER_NOT_LODESTONE_NOT_GAMEPLAY`，与GPU预览分开。

## 水池、植物和tick边界

1. **水池**：3个2×2水池共12个source water，嵌在y=1地板，y=0实底及四周石框静态密封。原生路径避开水体；这只能证明当前冻结格局无侧漏/底漏空间。真实放置顺序、旋转后邻居更新、流体计划tick、外部破坏/接水/区块边界行为未实测，全部NOT_RUN。
2. **藤蔓**：当前附墙方向和静态存活已由独立复核检查；本次不拿此替代随机tick。原版藤蔓在合适邻域可继续延伸/生长，可能占用现有通道或改变外观；长期随机tick或后续砍支撑可能掉落。未设置/修改游戏randomTickSpeed，不修改正式配置、不保证长期路线完全不变。
3. **小蘑菇**：当前位于菌丝面，静态存活通过不等于停止扩散。随机tick可在允许亮度/地表及密度条件下扩散；邻居/地面变更会重新判断存活；玩家骨粉另有巨型生长逻辑。本模板不拦截这些原版行为，也没有实际观察其长期结果。
4. **菌丝/灰化土、灯与紫晶**：当前原生状态可加载；菌丝在覆盖/光照等条件变化时可衰退或扩散，照明不等于“所有随机行为冻结”。菌光体/灯支撑与紫晶当前状态只证明冻结几何；破坏支撑与周边交互仍属运行时。
5. 不加入“冻结植物”、更新拦截或整包规则，不因HOLD盲改NBT。真实世界、邻居更新、随机tick、流体tick、多人游玩一律NOT_RUN；如复核要求动态实验，另在指定隔离世界执行，不能把本次离线PASS写成全稳定。

## 留待复验

本次未发现需要修候选的确切问题，因此没有重做或改哈希。补证提交后保持HOLD，由独立复核方重跑路线/边界并决定是否登记验收。S7-02仍未开始，未预生成、未领取。不得因本文件的局部PASS自行解锁。
