# Catalog expansion-v3：原房间与 runtime-next v2 精确合同

本交付仅新增本目录。不改原两廊道基线、expansion-v2或任何其他组件/源房间。原两套清单所有文件及ready/progress重新验哈希。**七个非走廊原房均保持 accepted=false，只有独立源建筑验收，没有新的独立运行时验收。**

## 冻结合同

runtime/next 生产者能力JAR精确SHA-256：f7d73680b20d390a6f08a090fea66b66000deb33bc0a6556ad282340035c6188。按其ready文件清单冻结并逐一检查57个文件，加ready自身共58个快照文件（包括JAR、源码、制作方世界/独立进程重启日志及fixture证据）。runtime-contract.json列出全部路径与哈希，runtime-next-snapshot保持原目录结构。

此合同是真实生产者v2能力，不是独立验收通过：支持single chest/barrel/banner和原门/半砖/木材/装饰白名单；保留载荷、默认BE、日志版本2。禁止实体载荷、流体/水浸、双箱、未允许的BE字段/loot/commands/caps。原制作方日志声称5房×4旋转、入口/大厅/宝库组装及BE跨进程重启，这里冻结原证据，不重命名为catalog/集成方现场测试，也不把7个新候选加入发行资源。

## 七个非走廊原房的当前状态

|原房|静态载荷与白名单对照|当前真正阻碍|
|---|---|---|
|S8-01 祭坛厅|无BE，无流体；已全在v2白名单|北口内向台阶不是矩形空气洞；需要原生碰撞/步高/净空及四旋转路线验证，不删台阶|
|S8-02 储藏书房|chest方块无显式NBT，v2默认BE合同覆盖|缺独立扩展运行时验收，不再声称BE不支持|
|S8-03 小宝箱房|1份chest显式NBT符合有限字段规则|缺独立扩展运行时验收|
|S8-04 主大厅|8份banner显式NBT符合规则|缺独立扩展运行时验收；真实接口有Y2/Y4，属于必须保留的几何约束，不误报新BE阻塞|
|S8-07 真入口|6份barrel/banner显式NBT符合规则|缺独立扩展运行时验收；北Y2宽3、南Y4宽5及内向台阶/开门须原生全程复核|
|S8-08 庭院|原9格lava；deepslate_brick_wall/lava/vine不在合同白名单|流体封装/更新顺序/传播/tick和藤蔓支撑待验，不允许；不得拿无BE当允许|
|S8-09 宝库|8份chest/barrel/banner显式NBT符合规则|缺独立扩展运行时验收|

所有七房资源为原始独立验收SHA绑定的字节副本，放在 candidate-resources/data/biomedungeon/structures。candidate-manifest.json是**禁止部署的候选清单**，并非可替换生产manifest。02/03/04/07/09既有BE/白名单缺口已被这个v2合同覆盖，但静态对照不等于MC原生加载/整条内部路线/玩家交互/独立验收通过。01和08均明确排除正式池。未将05/06廊道重复算作非走廊新增房。

## 给 core/next-stairs 的真实接口证据

core-stairs-consumption.json可直接读取：01与07所有原接口的脚点、宽高、干湿，以及每口向内depth0—6、整个真实宽度、脚点Y-2至Y+height+2各格**精确坐标、完整方块状态和支撑格**。既有独立route-input/routes/native证据随每房源验收快照保留。局部采样不是整个房间可达性证明。

- **S8-01北口** feet=(17,1,0), width=3,height=3,dry；边界全空气，下方(17,0,0)为deepslate_bricks。向内(17,1,1)、(17,2,2)为deepslate_brick_stairs，facing=south,half=bottom,shape=straight,waterlogged=false；depth3中心(17,2,3)是deepslate_tiles平台，上方脚Y3。南口实际feet=(17,3,34)，不可统一成Y1或删楼梯。
- **S8-07北口** feet=(12,2,0),width=3,height=3,dry；depth0/1底面Y1为deepslate_tiles。depth2 (12,2,2)、depth3 (12,3,3)为同样south/bottom/straight干楼梯。depth4底面(12,3,4)为polished_deepslate，脚Y4；(12,4,4)/(12,5,4)为原始open=true的dark_oak_door上下半，facing=north,hinge=right,powered=false。南口feet=(12,4,26),width=5,height=3,dry。不得套用合并布局的Y1/Y3。
- 楼梯可能形成半格支撑高度，必须调用原生VoxelShape/玩家体积与step-up进行连续路径检查；本目录不把坐标状态采样伪称完整碰撞解。门open=true仍有原生碰撞形状，不能按完整空气消失处理。未用口封闭、走廊landing与内台阶的交界必须另验。
- medium按声明边界洞口的水占用实读，不按群系名/房内液体猜。**包括08的原声明门口在内均dry；这不消除08房内熔岩风险。** 每格状态与waterlogged属性保留，混合水气洞口会标记未解决，不自动混成dry。

## 测试/交付与限制

使用现有Python解释器 C:\Users\YOUR_USERNAME\.codex\.venvs\imagegen\Scripts\python.exe -B freeze_v3.py，然后同解释器 -B test_v3.py。无生图API调用。tests.log记录6项检查：58份合同文件/JAR；七原字节/证据/accepted=false；所有采样格与载荷回读；实际01/07台阶与脚点；新旧阻塞区分/08明确拒绝；两套旧基线完整哈希不变。finalize_v3.py生成本目录SHA清单和ready/progress。

这里是目录/合同/原几何证据交付，不是原生世界验收。没有执行新MC世界测试，没有把制作方证据升级为独立证据；没有向其他线程写文件或消息。staticV2PolicyError为空仅表示本批简单载荷通过有限字段/空列表/名称/锁对照，**不是完整复制所有NBT类型检查或原生注册表验证器**；生产者原生验证证据保留，仍需独立复现。原验收仅证明此前建筑候选，accepted继续false，直到新的独立验收有明确哈希记录。
