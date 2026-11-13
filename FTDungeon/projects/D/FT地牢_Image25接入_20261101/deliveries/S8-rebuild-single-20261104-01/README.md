# 第8组第1张：接手重构R2，等待独立审计登记

当前仅处理S8-01-large-chamber.png。有效制作线程01a1a64f-3e44-7c72-ab6d-8d3efa9147a3；旧九模块包仍被排除。第2张及后续图片未领取、未制作、未预生成。

## 当前交付

- 实际原版结构：incoming/S8R_IMG01/room.nbt，35×21×35，DataVersion 3465。
- 生产配方：recipe-r2.mjs；recipe.mjs及checkpoint-original仅是旧版证据，不是当前配方。
- 当前元数据、材料、预览、QA：incoming/S8R_IMG01/。
- 原图及尺度解释：REFERENCE_ANALYSIS_R2.md。35格为有说明的建模估计，不冒充原图精确测量。
- 旧检查点42项初始清单全部匹配；之后保留checkpoint-original并重构同一张。细节见qa/takeover-checkpoint-audit.json。

## 验证结果

- 原版NBT解码、注册表、方块状态和StructureTemplate回环通过。
- 四个接口、左右/后绕行及观晶台路径通过；原生脚位搜索可达789格。
- 0/90/180/270度原版状态旋转、锚点变换后，各9个路线目标通过；不是世界放置/400tick测试。
- 74份只读对照：忽略材质的四向精确占位无重复；归一化内部占位无0.9筛查触发。最相近0.6898是本张被替代旧检查点，不是另一候选。
- 同构路线不能靠换材质通过；共同中央环道属于本图必须还原的要素。当前筛查不等于全组最终图论/体验终审。

## 预览与拼装

preview.png是实际Lodestone WebGL检查图，不是AI图片。qa/takeover-preview-provenance.json验证检查NBT只是在最终NBT上剖掉指定屋顶、近侧墙梁，没有添加方块。检查视图可能切掉局部支撑；最终结构仍保留完整屋顶、外墙与支撑。

完整Lodestone预览：http://127.0.0.1:5588/ 。检查视图：http://127.0.0.1:5589/ 。服务关闭后链接失效。

views/single-01-r2/为当前NBT的软件内部/外壳四视图；views/single-01/为旧版历史快照，禁止混合验收。真正内部主证据为qa/takeover-lodestone-cutaway-north.png。

assembly.json记录单模块35×21×35的原点、尺寸、哈希、顺序、旋转和锚点。北接口脚位[17,1,0]，南[17,3,34]，东[34,3,18]，西[0,3,18]；北口比其它口低2格，接入方必须按锚点高度拼接，不能按统一Y误接。

结构包含显式空气。接入前检查整个包围体、实体、液体和世界边界；有占用就拒绝，不允许清空现有建筑。未分配世界位置/运行槽位，未部署，未改存档/JAR/配置。

## 复现

使用已配置Python运行deliver.py build、deliver.py static、review.py native、review.py render。注意build必须使用recipe-r2.mjs；四视图会拒绝覆盖不同NBT哈希的快照。

在已安装minecraft-builder-skill目录运行scripts/preview.mjs，将--nbt指向本目录incoming/S8R_IMG01/room.nbt，--port 5588 --no-open；用cutaway-preview.mjs构建qa/inspection-only后另开5589检查服务。

随后在本目录运行node capture-takeover.cjs、python takeover_audit.py、python finalize_takeover.py。这里python须指向已配置运行环境，不是WindowsApps占位程序。package.py为新打包入口的兼容包装。

## 不得越过的闸门

状态：AWAITING_INDEPENDENT_AUDIT_REGISTRATION。AUDIT_REQUEST.md和registration-request.json只是落盘请求，不代表独立审计已接收或通过。

未运行世界四向实际放置、400tick稳定性、Forge邻居更新、区块重载和实机游玩。原版晶体不透明、不会复现概念图紫光泛光。这些缺口没有伪写PASS。

只有独立审计线程对本张最终NBT哈希明确登记通过，制作方才可开始S8-02。制作方不得自行解锁，当前没有改写共享排队真值。
