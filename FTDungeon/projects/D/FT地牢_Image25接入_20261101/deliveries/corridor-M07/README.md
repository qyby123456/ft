# corridor-M07 · 灾变暗铁残拱长廊

独立中型一字贯通走道；**不是既有枯井水牢房间 M07，也不是 M7**。仅交付制作与验收，不部署、不接入 FT 大厅、不修改玩家存档。

## 四件套

- `room.nbt`：Minecraft Java 1.20.1 / DataVersion 3465 / gzip / 单一调色板，17 × 13 × 41；9061 个显式坐标（含3993格空气），5068格非空气。
- `room.json`：ID、名称、尺寸、接口、来源图与完整 SHA-256。
- `preview.png`：由最终 NBT 经 Minecraft Builder 随附 Lodestone 查看器渲染的正中轴向图，不是概念图。
- `qa.json`：静态、四视图、原生往返与 Forge 实际放置稳定性的汇总及边界声明。

结构 SHA-256：`25de2806f4696546f285c949eb73ffdf58a9013867a413bf2cf72d917a07b215`。

## 几何与接口

| 项目 | 相对模板原点 XYZ / 说明 |
| --- | --- |
| 尺寸 | [17,13,41]；长轴 +Z，41格≈2.56区块 |
| 北入口脚位 | [8,1,0]，向北 |
| 内部安全脚位 | [8,1,3] |
| 南出口脚位 | [8,1,40]，向南 |
| 接口 | X=6…10、Y=1…6；5宽6高；脚下Y=0为完整地板 |
| 直行安全带 | X=6…10、Z=0…40，脚位Y=1，至少3格净高；全程同高 |
| 屋顶 | Y=11及12完整双层；残损只切除拱饰，不穿屋面 |

两端真实敞开，无转弯、分岔、路面楼梯或关闭门。结构中的楼梯方块只用于拱券倒角、柱脚与路外碎石，不改变行走高度。

## 设计来源与还原

来源：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M07-cataclysm-dark-iron-damaged.png`。

原图 SHA-256：`7aa60a98ff9c54dbb9b5e61c76973619d89a107a8e906c4787e561dd08fcccfa`，已全量核验；遵循同批 README / manifest / SPEC / corridor_index 中 medium、damaged、lanterns、broken-arches、dark iron / obsidian / gold / crimson 组合。

保留七组暗色柱列与拱券、金边、黑曜中轴赤纹、七盏暖黄吊灯、14节锁链、五处拱饰缺损及路边碎石。依接入契约只用原版方块：深板岩/黑石代暗铁，红色带釉陶瓦及红色下界砖代赤色符文；不声称是灾变模组材质、自定义发光文字或功能性机关。完整原图没有被重新生成或修改。

## 验收证据

- `qa/static.json`：14项静态断言 PASS；含完整体积、全地基/双屋顶、两端5×6、5宽3高直行带、BFS387格脚位、灯链支撑、禁用方块/实体/容器/战利品与来源哈希。
- `qa/four-views.png`：北、南、西、东四向斜视剖切总览；单图在 `qa/views/`，另有反向轴视及不剖切完整屋顶外观。`qa/render.json`记录7个真实相机、PNG哈希及来源NBT哈希；`qa/visual-review.json`记录实际目视检查。剖切只在内存显示副本，未改最终NBT。
- `qa/forge-native.json`：Minecraft1.20.1 / Forge47.4.10 / Java17.0.8隔离GameTest；注册表与完整属性校验、原生StructureTemplate往返、0°/90°/180°/270°四向实际放置、强制邻居形状/事件更新后观察240tick全部PASS。
- 每方向9061条状态完整保留，原生0.6×1.8玩家碰撞体387个可达脚位、205格五宽直行带、7灯14链、1394格双屋顶；掉落物0、异常流体0。
- `qa/forge-run.log`含 `CORRIDOR_M07_FORGE_PASS`、`All 1 required tests passed`、`BUILD SUCCESSFUL`；`qa/roundtrip.json`与`qa/native-roundtrip.nbt`保留原生往返证据。

### 如实保留的首次失败

初次GameTest原点较低，4方向初始放置均成功，但空门洞附近天然熔岩随后侵入；日志保留在`qa/forge-run-20261103-101519.log`。**没有修改模板或削弱稳定性断言**；仅将测试夹具移到绝对Y=180，并在施工前验证每个体积及一格留白确为空气。重验日志`qa/forge-run-20261103-104249.log`通过。未来复验包装脚本按时间戳建立全新测试工作目录，避免旧夹具残留；默认目录对应本次已通过的运行。

## 安全与接入边界

无预置实体、容器、物品、战利品表、刷怪笼、命令/结构/拼图方块、液体或真实火。仅在交付目录下生成独立测试世界；没有打开任何玩家世界，没有复制到mods、resourcepacks、schematics或正式结构目录，没有修改其他ID或核心代码。

接入方必须验证**完整17×13×41体积及留白**的方块、液体、实体、世界边界；占用则拒绝，不因显式空气记录而无条件清空。坐标是脚位，入口必须对应地板上一格。还需实现FT大厅可步行连接、独立持久生成标记、幂等与占用拒绝，不能把本交付宣称为已接入/已部署。

软件渲染和隔离Forge放置是两类证据；未启动完整整合包客户端，不声称已通过着色器、真实光照、多人游玩、FPS或难度/奖励系统验收。没有调用付费生图、读取密钥或修改聊天/图片连接配置。

## 复现

在PowerShell中使用本机Node及Minecraft JDK17；Lodestone依赖已在技能目录安装。所有命令只写本ID目录。

```powershell
$root = 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-M07'
$skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
node "$skill/scripts/run-build.mjs" "$root/recipe/corridor-M07.mjs" --out-dir "$root" --name room --data-version 3465 --json
node "$root/tools/static-qa.mjs"
& "$root/tools/run-forge.ps1"
node "$skill/scripts/preview.mjs" --nbt "$root/room.nbt" --no-open --port 5397
```

保持预览服务后，在另一终端运行：

```powershell
node "$root/tools/capture-views.mjs" 'http://127.0.0.1:5397/?nbt=/__minecraft_builder__/build.nbt&name=corridor-M07'
& 'C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe' "$root/tools/finalize.py"
```

截图工具使用本机Bundled Playwright与无窗口Edge；对技能查看器的测试钩子仅通过浏览器请求响应注入，未改技能文件。Python仅借用Pillow环境，不调用imagegen CLI。若重做模型或视角，需重新目视并更新`qa/visual-review.json`；打包器会拒绝不同NBT哈希或未PASS证据。`delivery-manifest.json`记录最终四件套及关键证据的哈希。

