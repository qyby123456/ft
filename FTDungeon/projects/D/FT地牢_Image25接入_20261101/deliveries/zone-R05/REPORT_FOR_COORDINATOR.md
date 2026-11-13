# zone-R05 破碎档案厅 · 独立交付

四件套：room.nbt、room.json、preview.png、qa.json。

尺寸 27×16×29；北入口脚位 [13,2,0]，内部安全点 [13,2,4]，后部安全点 [13,2,24]。地板 y=1，脚位 y=2；入口五格宽、五格高，主路五格宽。

NBT SHA-256：`380a7385e2180890c6372c831f3c93f2db3783ab8319a407598ac8bea5c18387`。
参考图 SHA-256：`8440f06ed3f0330b74660e57fa1d51376a212541c49b7e540eaa7f9759b3a810`。

验收：17 项静态检查；北南西东内外八视图；Minecraft Builder / Lodestone 对最终 NBT 的哈希核验与四张软件渲染；Forge 1.20.1 / 47.4.10 原生注册表和属性、模板往返、四旋转放置、全部邻居通知、600 tick 后精确状态对比、完整地板屋顶、支撑与原生 0.6×1.8 玩家体积扫掠通路、空库存及夹具回滚通过。

每向 12528 条显式记录、6404 次原生生存条件检查、243 次专项支撑检查、417 个可达脚位、7932 个外围 halo 单元不变。32 格书架（两组低矮倒伏书架残件加一组立式书架）、2 个空木桶、3 面带纹样墙旗、4 盏悬灯；零奖励和零战利品表。

视觉：views/interior-four-views.png、views/exterior-four-views.png；Lodestone 视图见 views/lodestone-*.png。preview.png 从最终 NBT 渲染，剖切仅发生在图像，不修改模板屋顶。旗帜软件外观近似，图案 NBT 保留且原生加载。

设计适配：原版书架没有横躺方块状态，使用落地低矮书架残件表达倒伏；纸张和散落书采用地毯形制，不生成可拾取实体；残廊局部缺口在完整屋顶下，具有墙体、地柱和梁支撑；图中破窗改为冷色玻璃封闭围护，保留铁格和裂纹视觉。

先前拒绝证据保留：缺少空夹具的首轮启动日志；原生栏杆精确状态拒绝记录 qa/native-world-first-fence-rejection.json。最终通过日志：qa\forge-gametest-20261103-162337.log。

复核入口：qa.json；详细证据：qa/static-qa.json、qa/native-registry-qa.json、qa/native-world-qa.json、qa/four-view-review.json、qa/lodestone-preview.json。四件套文件指纹：qa/delivery-artifacts.json。

重建与 Forge 复验：运行本目录 Run-QA.ps1 -Rebuild；四向软件图运行 source/render_views.py；Lodestone 按 source/capture-preview.mjs 的 URL 参数连接实际预览服务。全部测试仅用本交付目录独立 GameTest 世界。

未部署、未改正式源码/JAR/玩家世界、未占用其他 ID、未分配全局槽位。等待统一接入负责人分配槽位并完成大厅连接验收。软件预览与隔离 Forge 测试不等于完整整合包客户端/多人游玩验收。
