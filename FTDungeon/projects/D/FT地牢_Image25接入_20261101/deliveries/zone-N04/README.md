# zone-N04 · 寻门枢纽

独立 Minecraft Java 1.20.1 地牢分区房间；使用 Minecraft Builder / Lodestone 生成。尺寸 **35×16×35**，DataVersion 3465，gzip、单一 palette，完整记录 19,600 个坐标（含 10,216 格空气）。该候选未部署，正式 JAR、整合包配置、玩家存档与其它制作 ID 均未修改。

## 四件套与接口

- `room.nbt`：完整结构。SHA-256：`14c978b39244c763c15356388bd1176b1b2452ef37f3a5bed6963890be7cc8d5`。
- `room.json`：相对模板原点的整数 XYZ、接口、安全点、源图、材料、取舍与接入要求。
- `preview.png`：最终 NBT 的朝北眼平内景，屋顶完整，非 AI 概念图或游戏客户端截图。
- `qa.json`：最终汇总；静态、逐图目检、Forge 实际放置报告均绑定上述 NBT 哈希。

| 方向 | 脚位 | 材料识别 | 对外方向 |
| --- | --- | --- | --- |
| 北／约定主入口 | [17,2,0] | 紫晶与黑石 | -Z |
| 东 | [34,2,17] | 深板岩与黑石 | +X |
| 南 | [17,2,34] | 红砖与封蜡氧化铜 | +Z |
| 西 | [0,2,17] | 砂岩 | -X |

四口均接同高五格宽十字主路，地板 y=1，玩家脚位 y=2，保证五格净空；开口至少五宽五高。内部主安全点 [17,2,17]；额外安全点 [17,2,6]、[6,2,17]、[28,2,17]、[17,2,28]。所有点与四口可步行往返。端口向外的走廊尚由统一接入负责人另行连接，预览门外的暗色为软件背景。

坐标以原始北向模板为准。Minecraft `StructureTemplate` 围绕 (0,0,0) 旋转时，顺时针90°为 (x,y,z)→(-z,y,x)，180°为 (-x,y,-z)，逆时针90°为 (z,y,-x)，再加放置原点；需同时变换入口、安全点、朝向与包围体。完整旋转示例见 Forge 测试源码。

## 寻门主题与参考

参考源：`D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/hub/N04-hub-portalfinder-hub.png`。

参考 SHA-256：`81f17850087057a9a0c1925f1fe747220c5a8e163003799c55fa5a97a1b756c3`。

核对同批 README、docs/manifest.json、docs/verify.json 与父项目 CONTRACT.md，原文件指纹和 N04 对应记录保存在 `qa/source-provenance.json`。没有调用生图接口。

保留灰石砖大厅、四根落地承重柱、紫晶罗盘地坪、暖色链吊灯、书架、静态木箱、紫色纹章与不同材料门廊。中心磁石属于普通装饰方块；方块排列表达寻门主题，**没有实现传送、自动寻路或跨维度功能**。北向紫晶门廊可以直接步行通过，不含下界传送门、末地传送门、末地折跃门或命令。纹章使用紫色混凝土和紫晶，木箱使用去皮云杉原木，无容器、物品、奖励、实体或刷怪。

概念图的敞顶剖视改成完整双层屋顶；实际结构保留梁、柱与灯链。仅软件剖切临时隐藏 y>=14 的屋顶和观察侧两层外墙。四种材质分别指示四个真实步行出口；概念图中形似传送场的视觉效果改成紫晶实体装饰及上部门楣玻璃，地坪主路不被装饰遮挡。

## 验证证据

- `qa/static.json`：NBT 反读 19,600 唯一坐标，1,225 格基础、2,450 格屋顶、325 格五宽五高十字主路，813 个可达脚位；四口与五个安全点连通；8 盏吊灯、4 盏立灯和32节锚定链有支撑；无液体、实体、方块实体或传送门。
- `views/`：北东南西四向完整屋顶眼平内景、完整外壳及剖视，共十二张 1536×1024 实际 NBT 软件渲染。`views/contact-sheet.png` 为总览；`views/manifest.json` 保存相机、模式、原始/显示记录数、输入 NBT 与图片哈希。`qa/visual-review.json` 记录制作端实际逐张目检结果；软件光照不等于游戏内亮度/FPS。
- `qa/forge-native.json`：Forge 47.4.10 / Minecraft 1.20.1 原生注册表及完整状态属性、StructureTemplate 反读回写、NONE/顺时针90/180/逆时针90四旋转实际放置通过。每旋转19,600条状态经强制邻居更新及240 tick后全部不变，原生碰撞可达813脚位、全部接口/安全点可达；灯链存活，零掉落物、液体或方块实体。
- `qa/forge-result.json` 与 `qa/forge-run.log`：进程退出码0，包含 `N04_FORGE_PASS` 与 `BUILD SUCCESSFUL`。最终隔离世界 `forge/run/20261103-145725/` 使用超平坦基础，四份模板均在绝对Y=200的空域放置；每个目标坐标先断言为空气。它不是玩家世界。
- 首轮自然地形世界的延时掉落物断言失败；存档法证发现40个铁轨、2个火把、4个紫晶碎片，均不在模板方块材料中。原始日志、失败结果、实体NBT摘要与修正原因在 `qa/rejected-first-run/`。复测使用相同 NBT 哈希、空超平坦高空空域，保留零掉落要求，没有删除掉落物或关闭方块掉落。

此为制作端候选验收。未做完整整合包客户端画面、多人、帧率、现有大厅外接路线、占用拒绝或持久幂等接入验收；未实现运行时传送。接收者应对最终哈希独立复核。

## 复现与接入

使用本机已配置 Node、Python、Java17 与缓存 Gradle8.8。Forge 仅在本交付目录离线运行，不安装到正式 mods。命令示例：

```powershell
$delivery = 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-N04'
$skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
node "$skill/scripts/run-build.mjs" "$delivery/tools/build.mjs" --out-dir $delivery --name room --data-version 3465 --json
node "$delivery/tools/static-qa.mjs"
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' -B -X utf8 "$delivery/tools/run-forge.py"
node "$skill/scripts/preview.mjs" --nbt "$delivery/room.nbt" --port 5374 --no-open
```

预览服务器启动后，另一终端执行 `node "$delivery/tools/render-views.mjs" "http://127.0.0.1:5374/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=N04"`。执行 `tools/sheet.py` 生成总览。重新生成图片后 manifest 自动回到待目检状态；必须实际逐张核图并更新目检证据，最后执行 `tools/finalize.py`。本次目检结果只适用于所列哈希，不能当成自动视觉检测器。

首次接入须预检**整个35×16×35体积，包括空气坐标**、世界边界、实体与液体占用；拒绝占用，不把空气记录当作无条件清空授权。为 zone-N04 分配独立槽位与独立持久标记，不复用原有N04或其它房间ID；入口、安全点和外接走廊随旋转校正。不得覆盖玩家修改，不重复生成已成功房间，安全失败不写成功。部署与大厅连接由统一负责人处理。

`handoff.json` 是接收入口；`delivery-manifest.json` 绑定四件套、源码与静态/视觉/Forge/失败证据的文件指纹。未把 Forge 构建缓存和测试世界整体打入指纹清单；本地隔离世界仍保留供法证检查。
