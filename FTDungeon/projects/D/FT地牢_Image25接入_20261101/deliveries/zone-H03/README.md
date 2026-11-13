# H03 · 烛仪大厅 · 独立交付

四件套：room.nbt、room.json、preview.png、qa.json。仅写 deliveries/zone-H03；未部署、未接入大厅、未修改正式文件或其他房间，未打开玩家存档，未调用生图接口。

- 尺寸 29×16×35，Java 1.20.1 / DataVersion 3465，gzip、单调色板，16,240 条显式体积记录，非空气 6876 格。
- 北入口脚位 [14,2,0]，内部安全点 [14,2,4]，朝北；入口净宽 5、净高 4，主路 X=12…16、Z=0…26 净高至少 3。坐标均为模板局部玩家脚位。
- 四根落地石柱与四边梁连接双层完整屋顶 Y=14/15；后部两排朝南台阶连接祭台安全落脚 [14,4,29]。
- 中央黑白仪式纹章、紫金地面镶嵌、11 面紫金纹章旗、36 组共 140 支点燃蜡烛、2 座十字烛盘与 10 格屋顶锚链。所有蜡烛均落在实心支撑上；祭台有完整底座。
- 地毯用齐平方块镶嵌表达，避免细地毯改变脚位；不加入烟雾实体、明火、流体、自动战利品或战斗机制。

验收：静态 QA PASS；Forge 47.4.10 独立 GameTest 四向 0/90/180/270° 实际 placeInWorld，强制邻接形状及事件更新后观察 240 tick，各向全部 16,240 条状态保持，135 个主路脚位通过真实碰撞，各向 647 个地面脚位可达，2,030 格屋顶核验，全部 canSurvive、旗帜 NBT、蜡烛与铁链支撑稳定，无掉落物。FakePlayer 使用真实 Entity.move 每向 580 个 0.1 格移动步，验证北入口到祭台上行与原路返回。

视觉证据：previews/four-directions-sheet.png 汇集四向内部剖视、四向完整外壳和两张完整屋顶内景。所有 PNG 由最终 room.nbt 经 Lodestone 渲染；剖切只修改内存视图，不修改 NBT。软件渲染不是整合包客户端截图。Lodestone 墙旗只显示简化紫面，Forge 已逐面验证两条金色图案记录。

参考图：D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/halls/H03-halls-ceremonial-hall-candles.png

已核对批次 README.md、docs/manifest.json、docs/verify.json；逐条指纹证据见 qa/reference.json。

- PNG SHA-256：c8bfb5116ed1f75a9870537c25f27aa8be7bb21113e79c6e13eefeac0aba7158
- NBT SHA-256：63420cfb4a6a4d91c354a820e62c2df773752d98d541b4282c565b797ba84631

复现：

1. node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs recipe.mjs --out-dir . --name room --data-version 3465 --compression gzip
2. node tools/audit.mjs
3. powershell -ExecutionPolicy Bypass -File tools/run-forge.ps1
4. node tools/serve-preview.mjs 5183（如端口占用会输出实际端口）
5. node tools/capture-preview.mjs http://127.0.0.1:实际端口/，重新视觉检查后 node tools/finalize.mjs

未来接入必须先检查整个 29×16×35 体积为空，包括显式空气位置；不能用模板清空既有建筑。旋转时同时变换入口/安全点/方块状态。首次成功放置后写独立持久生成标记，再次进入不覆盖玩家修改。独立服务器验收不等同于正式整合包客户端、多人、性能或核心连接路线验收。

工程证据：recipe.mjs、tools/、viewer/、native-test/src/；完整状态验收、日志、原生往返 NBT、材料和交付指纹清单均保留。qa/forge-runtime、qa/vite-cache、native-test/build 与 native-test/.gradle 是本房间隔离测试缓存，不是待部署资源。
