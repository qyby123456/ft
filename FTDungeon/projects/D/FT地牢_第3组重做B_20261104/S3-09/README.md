# S3-09 霜旗骨卫宝藏室 — 第9张候选待独立复核

唯一参考：`D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-09-treasure.png`。已只读核验08独立放行记录`S3-08-independent/independent-details.json`及冻结NBT哈希。当前只制作09，不读取/开始10，不改前八张完整清单或正式saves/mods/config/统一incoming。建筑recipe为本图独立新写，不导入已有房间；检查与截图工具复用但新增容器加载/箱顶检查。

## 原图解读与离散尺度

原图1536×1024是宝藏阶台房间的斜视概念切面：中央偏后单宝箱置于小石阶台，左右各一尊骨像，后墙三面蓝色雪花旗、两灯，左右低木石拱和侧灯，前景冰沟及右侧栏杆桥，木箱和罐分布两旁。画面上缘只表现切开的雪冰墙冠，不能据此证明整个房间露天。本候选最终有完整屋顶，预览另给精确删顶切面。

采用37×25×37，主地板Y4/脚Y5，阶台地板Y7/脚Y8，冰沟蓝冰面Y3。近景单箱约1格、侧低门5格、骨像约6～8格高作为比例锚；长度与封顶高度为离散推定，非精确像素测量。完整碰撞空间能走到宝箱前后，不以镜头看见宝箱就冒称路线可达。

|原图特征|实际NBT|
|---|---|
|单箱和阶台|唯一真实chest[18,8,10]，朝南type=single；主体台面x14..22/z6..14、南向入口有3排5宽真楼梯，共15楼梯方块，脚高从5到8|
|骨像守卫|x11/25、z12，独立石基座，骨像主体Y8..14；74骨块与6黑石按钮表达双眼/口/四肢，不用实体盔甲架或刷怪器|
|三面旗和主灯|后墙x7/18/29处blue_wall_banner，facing=south，方块实体Patterns包含白色flo纹；2主灯和2侧拱灯，共4灯4链|
|侧低拱|西[0,5,16]、东[36,5,16]，各5宽4高保守净空，外围木梁/石墙；两侧与前室相通，无捏造隐藏宝库|
|冰沟和桥|x4..32/z26..31冰沟下沉1格，blue_ice而非活水；右桥x26..30/z25..32为木面和两侧木栏，南入口在[29,5,36]，主路跨桥后进入阶台前室|
|箱罐冰骨|侧木箱为spruce_planks/半砖装饰，无额外库存；两罐为橙陶块+flower_pot，无陶罐战利品；墙冠冰挂、侧冰簇、前景少量骨块与积雪|

蓝旗白色flo纹只是雪花近似，不能冒称精确雪花图；默认Lodestone可能只渲染短蓝片而不显示纹样，NBT和原生banner加载结果是存在证据，不宣称实机旗面已看。方块骨像和冰晶无法复现概念图细小雕刻/尖晶体；薄雪改整snow_block或齐平雪地，原图坛前独立小箱座简化为完整台面，左右罐纹不复刻。未凭参考创造奖励配方或机关。

## 容器、箱盖和奖励边界

宝箱是原版单箱方块，NBT为`id:minecraft:chest, Items:[]`，无LootTable、无Lock、无命令、无预填战利品；是空库存的建筑候选。周围没有相邻箱块，避免双箱拼接。Y9..12同列全部空气，箱前脚位[18,8,12]和后方[18,8,8]都有身体可达路线。container-check.json记录状态、实体、箱盖净空、检查范围。

NativeQA实际对1箱+3旗调用BlockEntity.loadStatic和saveWithFullMetadata，箱类型确认为ChestBlockEntity、27格库存逐槽为空。箱顶用本版ChestBlock.isBlockedChestByBlock的原生方法检查；该私有静态方法通过反射调用，不自己猜透明方块规则。最初尝试完整LevelAccessor代理时映射jar残留签名导致MinecraftServer类校验失败，未修改jar或忽略失败冒称通过；最终限定LevelReader代理及原生方块阻挡分支。猫坐箱/世界实体遮挡分支NOT_RUN，模板中没有实体，目标世界仍需另查。实际点击、开盖动画、GUI、存取、掉落、世界重载均NOT_RUN。

## 接口和身体路线

原点[0,0,0]，+X东/+Z南，图无罗盘，人工指定前方为南。入口南[29,5,36]，西[0,5,16]，东[36,5,16]，共3口，每口5宽4高、下有完整方块。安全点[28,5,33]，主路经右桥脚Y5到前室，再登阶台至脚Y8。14个目标为三口、安全点、桥、前室、两骨像前、3阶、箱前/后、左罐前。目标是整数脚位格，身体中心x+0.5/z+0.5。

冰沟低1格，入口路线不要求跳沟；木栏不等于全室不会跌落，不声明玩家所有任意路线安全。rotated-connectors.json按非负包围盒计算四旋转，均37×25×37，接入仍须补偿引擎pivot。另一结构门面放在本门外侧相邻一格、朝向相反，不共面覆盖；端口脚Y5不能沿用07前口Y2。未实机旋转拼装。

显式空气覆盖完整37×25×37体积，会清除已有方块。未来部署须先检验整个体积的占用、实体、世界边界和玩家建筑；冲突即拒绝，禁止先清空。当前无正式部署、无运行时槽位。

## 原生检查与材料交接

inspect.mjs真实读回DataVersion3465、34225完整唯一有界记录、原版ID、无实体/指令/危险活流体、准确3口15个边界脚位、1369列完整顶盖、15楼梯、箱顶净空、箱与3旗实体唯一性、无奖励/锁、所有实物面连地基、剖面逐格仅改为空气及实体完全不改。

NativeQA实际读原版1.20.1 mapped jar和170个原版block tags，验证注册表、每个显式属性、StructureTemplate.load/save，全部34225记录canSurvive；另做4个方块实体的实际原生加载/保存和容器阻挡检查。14身体目标采用0.6×1.8、0.5格水平/高程网格、每边≤0.5升降，原生形状检查脚下接触与中心支撑、较高脚位起/终点和0.25格边中点净空。它是离线静态抬步可达，不是连续扫掠、加速度、跳跃或冰面滑行。

materials.json给出最终每个ID及显式状态的精确数量、空气/实物记录和方块实体；这是实际NBT材料清单，不是生存合成配方或奖励程序。recipe.mjs为完整原始生成配方，summary.json为导出读回摘要。正确参考文件哈希在room.json；finalize额外回读参考，防止错图。validation-provenance.json绑定依赖、08独立放行、前八张完整清单。SHA256.json覆盖当前全部交接文件（自身除外）并逐文件回读。

原生执行在独立Temp目录复制NBT/路线输入后编译运行，只拷结果日志回当前候选。无普通ice、无snow薄雪、无活水；暖灯周边为snow_block、packed_ice/blue_ice。世界放置、光照传播、邻接/随机刻、重载、真实互动与Forge玩法均NOT_RUN；自检不代替独立验收或部署验收。

## 实际视图和派生追溯

interior-01..04.png为只删空气剖面四个对角方位，exterior-01..04.png为完整源NBT四向外景（含背面），eight-view-sheet.png仅是8张缩略拼版。nave-full-nbt.png沿用工具名，实际为完整宝藏室相机[28,13,32]朝[18,12,10]；reverse-full-nbt.png从后阶台反看冰沟/桥/前墙，chest-lid-full-nbt.png近看箱与上方净空，bridge-channel-full-nbt.png近看冰沟和栏桥。4张完整局部视图只通过Playwright响应内覆盖viewer相机，不改方块或安装脚本。view-map.json绑定真实PNG哈希、源NBT哈希、相机和派生标记。

cutaway.mjs精确规则为Y≥23删顶、南墙z≥34且Y≥8、东墙x≥34且Y≥15只改为空气；箱和旗实体不删不改，不能以切面冒称最终露天。最终实物比例与跨房间重复度PENDING：本张单空箱阶台+双骨像+冰沟桥的拓扑不等于08树池双层庭院，材质共通不替代独立判断。完工停候09复核，不并做10。

## 复现

在`C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill`执行：

```powershell
$room = 'D:/ft工程文件/FT地牢_第3组重做B_20261104/S3-09'
node scripts/run-build.mjs "$room/recipe.mjs" --out-dir $room --name room --data-version 3465
node scripts/run-build.mjs "$room/cutaway.mjs" --out-dir "$room/preview-only" --data-version 3465
node "$room/inspect.mjs"
& "$room/Check-Native.ps1"
```

viewer各占一个独立终端，启动后核对端口，再第三终端截图/定稿：

```powershell
node scripts/preview.mjs --nbt "$room/room.nbt" --port 5343 --no-open
node scripts/preview.mjs --nbt "$room/preview-only/preview-only-cutaway.nbt" --port 5344 --no-open
node "$room/capture.cjs"
node "$room/finalize.mjs"
```

拼版上内下外各4张缩为360×275，不替代完整PNG。
