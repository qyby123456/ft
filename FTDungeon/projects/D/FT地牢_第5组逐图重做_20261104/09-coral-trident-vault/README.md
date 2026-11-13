# S5-09 三叉戟珊瑚宝藏室：待独立验收

仅按正确单图`S5-09-treasure.png`独立新建，原图/提示复制在`reference/`，正确图SHA见`metadata.json`。S5-08已独立放行。本次不做10，不部署，不改正式存档/mods/配置；前8张已验收主NBT哈希复查不变。

## 关键构图与海底近似

31×22×31房间，北部后墙有三叉戟壁画和阶梯山花；后部三级高台上有1只真实含水原版宝箱，左右两尊有头/眼/肩/双腿的海晶石守卫像。两侧拱门、边侧物资台/4空桶、砂岩罐近似、两幅蓝色旗饰马赛克、前景两座海晶灯矮台、外围珊瑚和扎根海带保留参考布局。无上层环廊/天井体系，不复用上一张庭院。

三叉戟是海晶石拼嵌，不是掉落武器；旗饰为耐水陶瓦马赛克，不是旗实体；砂岩和含水砂岩墙表达罐形，不是装饰陶罐实体。守卫像是静态方块，不是怪物/盔甲架。宝箱保持原版空库存，无战利品表，不能误称已经放入宝藏。真实珊瑚色形与插画分枝不同；砂岩表达沙色拼块，不放重力沙。全浸无空气带，不提供水下火/气泡/光束/呼吸保障。

## NBT、源码、材料、参考与数据包

- `nbt/s5-09-coral-trident-vault.nbt`：Vanilla Java1.20.1，DataVersion3465，gzip，31×22×31=21142显式记录，无structure_void。
- SHA256：`57bca6ec9a9cbaac058ef611a9cf518173cb4a6d0549fc3558182d2b38925c11`。
- `recipe.mjs`为新配方，`metadata.json`含正确参考SHA、入口/容器/海水合同；`qa/materials.json`按原版ID完整计数，总21142。最大边31小于结构方块48限制，不需分片。
- `datapack/`pack_format15，结构ID `ft_s5_redo:s5_09_coral_trident_vault`；其NBT/主候选/`worldtest/frozen.nbt`逐字节相同。未装入任何正式存档。

仅独立测试世界准备好下述完整海水环境后，可尝试 `/place template ft_s5_redo:s5_09_coral_trident_vault ~ ~ ~`。会覆盖完整矩形内方块/水/底板，本次未向正式世界导入。

## 永久保留的海水接入合同

西北下角为原点，+X东/+Z南。Y0..2连续实床，结构水顶Y21。两端入口都在Y2实底/脚位Y3：

|接口|脚位XYZ|向外|净宽×高×内深|
|---|---|---|---|
|西拱|(0,3,18)|西|5×3×3|
|东拱|(30,3,18)|东|5×3×3|

水平四周至少8格源水环，顶部Y22..29至少8层源水盖，结构与外环Y0..2连续实体支承；两门各向外延伸至少3格，5宽×3高源水与实体Y2门槛。**不接干走廊，不得半浸/悬空后引用本测试。**本体NBT不包含外环/盖水/门外延伸，接入方必须提供；本次隔离槽已提供并逐格核验。

|旋转|归一化变换|尺寸|原生pivot相对归一化原点|
|---|---|---|---|
|NONE|(x,y,z)|31×22×31|(0,0,0)|
|CLOCKWISE_90|(30-z,y,x)|31×22×31|(30,0,0)|
|CLOCKWISE_180|(30-x,y,30-z)|31×22×31|(30,0,30)|
|COUNTERCLOCKWISE_90|(z,y,30-x)|31×22×31|(0,0,30)|

四向门坐标/方向/门外检查见`qa/integration.json`，实际世界原点/pivot与14目标路线每步见`worldtest/<ROTATION>/report.json`。

## 原版容器、接近路线和箱盖净空

宝箱(15,6,7)，单箱、朝南、waterlogged=true，实体id=minecraft:chest，Items空、无LootTable。脚下Y5实顶支撑。箱盖上方(15,7..9,7)三格纯level0源水/空碰撞；原生服务器额外检查menu provider非null，说明在提供构型下无上方实块阻挡容器菜单。未模拟玩家点开/箱盖动画/手臂触及射线。

4桶位于左右物资台，均有原生实体/空库存/实心脚下。箱和桶的完整实体NBT在12份捕获中三时点一致；`qa/vault-features.json`逐容器记录最终NBT与旋转坐标。

15块含水海晶石砖楼梯由内庭Y2逐级通向宝箱平台Y5/脚位Y6。原版六方向三维BFS脚位Y3..9、身体两格碰撞净空，14目标覆盖两门、内庭、高台、箱前相邻格和物资台前，每向4765位置可达。`vault-check.mjs`从4份最终捕获另核13步中轴游泳路线，身体两格均纯源水，从(15,3,18)升到(15,6,8)，箱前相邻格；不是玩家实际浮力/运动、半格扫掠、氧气/AI/多人认证。

## 有限隔离世界证据

`worldtest/run.ps1`启动全新隔离Forge1.20.1-47.4.10 GameTest，Java17/Gradle8.8，非NeoForge；保留`invocation.json`/`console.log`/`exit.json`/`tests.xml`与世界。4项JUnit通过、exit0。不可重复使用该测试世界，复测必须新建目录。

注册表/属性/模板21142记录回读、knownShape=false/flags3四旋转放置通过；源状态映射→实际放置0差。显式邻居更新/流体调度及每向1200服务端刻后，完整59×30×59=104430点无状态漂移。每向三时点（更新前/更新后/1200刻后）原版捕获，共12份；`evidence-check.mjs`全量Lodestone重解码状态与方块实体签名，同向三时点一致。

每向21142原版canSurvive通过、1空箱/4空桶/15含水阶梯/三格箱盖源水净空通过；死珊瑚0、珊瑚身份变化0、水生无法存活0、原水丢失0、掉落物0。

randomTickSpeed=3，Forge FakePlayerFactory非旁观创造玩家加入ServerLevel并每刻更新区块源，自然区块随机刻活跃，无直接randomTick调用。Y170/Y186每槽各64株明亮小麦自然长大：NONE12/11、顺90为6/5、180为10/14、逆90为8/9。海带age25限制长高，未关闭随机刻保活珊瑚。

`integration-check.mjs`从4最终捕获逐格核8格环水/8层盖水/连续实床与双5宽湿口内外3深。人工密闭隔离槽、有限1200刻，不等于自然海洋生成/正式整合/无限稳定/拆壳/生存呼吸保障。全部为制作方服务器运行及回读；独立复核重解码不等于另跑服务器。

日志保留flat默认layers、Forge版本联网检查、API/Gradle弃用等警告；最终4必测通过，不删除警告或掩盖失败。

## 真实Lodestone视图与精确派生追溯

完整水体：`qa/full-water-exterior-southeast.png`全景、`qa/full-water-exterior-northwest.png`反面、`qa/full-water-interior-axis.png`人眼构图、`qa/full-water-interior-chest-detail.png`宝箱/守卫/箱盖细节。全部原候选完整矩形，无裁切、无移水；`qa/preview.json`每张记录实际HTTP供给NBT哈希/相机/页面错误。Lodestone没有游戏水下雾/体积光，内景看不到水面不代表移水。相机临时响应覆盖，不改技能安装文件。

`preview-only/preview-only-s5-09-water-hidden-not-importable.nbt`与`qa/water-hidden-GEOMETRY-ONLY.png`仅几何，不可导入/用于世界证据。`vault-check.mjs`逐点比对：同尺寸同21142记录，14996格水→空气、48格waterlogged=true→false，其余方块ID/状态/方块实体完全一致，未裁切。源及派生SHA、计数见`qa/vault-features.json`。

`qa/static.json`为边界/体积/原版名空间/精确去材质几何去重；`qa/near-duplicate-screen.json`为裁边归一化16³占据/四旋转Jaccard粗筛，与第8张0.3504、第7张0.3959、第4张0.4403。粗筛不等于语义独立验收；其余组只读比较，不改变状态。

## 冻结与独立复核入口

查`qa/SHA256.json`、`metadata.json`、`acceptance.json`、`qa/materials.json`、`qa/vault-features.json`、`qa/world-evidence-check.json`、`qa/integration.json`以及源码/JUnit/日志/12捕获。清单排除自身及Gradle build/.gradle，逐项读回；正式候选/冻结样本/数据包同哈希，前8张主NBT复核未变。

**制作方检查通过，等待独立验收；第10张未开始，未动正式存档/mods/配置，未部署。**
