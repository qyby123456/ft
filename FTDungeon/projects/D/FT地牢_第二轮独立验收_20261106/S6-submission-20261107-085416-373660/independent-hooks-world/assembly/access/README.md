# S6 外部授权接入包

此包为原生 Minecraft Java 1.20.1 数据包，随附当前合并的20个结构分片。通过外侧接入台与内侧入口之间的授权传送提供接入，保留整个47553格屏障壳。主候选NBT、57个原房目标、58个合并目标和退役末端12格封口均不改动。不是外围步行开洞方案，也不要求修改地牢生成器。

本目录仅提供待审工件。制作方没有把数据包、结构或命令写入正式实例。

## 安装约定

单个世界仅支持一组未旋转实例，维度固定为overworld，坐标原点必须为整数，Y须在-64..284范围以容纳36格结构。由后续部署责任人选择空置、预留且未设置其他forceload的区域；安装会写入主结构范围207×36×180，以及相对X=-10..-1、Y=0..7、Z=32..40的外侧接入台。它不会自动适配地形，也不连接其他副本。必须先完成独立审查再由责任人部署。

1. 将本目录的`fts6-access-mc1.20.1.zip`放入所选测试世界的datapacks目录，或使用`datapack`目录内容；运行`/reload`。这是部署说明，本轮没有执行此操作到正式世界。
2. 以OP执行`/execute in minecraft:overworld positioned X Y Z run function fts6_access:install`。安装函数在选定原点放置20片、外侧接入台及两个marker。已有本包origin marker时拒绝重复安装；没有自动覆盖/搬迁入口功能。
3. 给可进入的玩家执行`/tag 玩家名 add fts6_authorized`。权限由管理者控制；普通玩家不能自行调用`/function`或添加授权tag。
4. 玩家走到外侧台中央雕纹黑石上，在半径2格内执行`/trigger fts6_enter set 1`。下一个服务端tick校验授权、维度和位置后传送进入；普通玩家无需OP权限。
5. 返回内侧入口半径2格内，执行`/trigger fts6_exit set 1`返回外侧台。退出不要求保留授权tag，避免授权撤销后被困。远处伪造的进入/退出分数会被位置检查拒绝。

以安装原点X=2048、Y=128、Z=2048为例，外侧接入点为2042.5 132 2084.5，内侧落点为2050.5 132 2084.5。通用公式为外侧原点+[-5.5,4,36.5]、内侧原点+[2.5,4,36.5]。外台南边预留4格步行开口；把该外台与外部道路衔接属于场地部署，不改主结构屏障。

## 防逃边界

此方案允许且仅通过声明的授权入口/自愿返回点跨越封闭边界，不在屏障上开口。原结构静态路径和屏障洪泛证据继续绑定5b72d94c89871ebe9c262f92d6c00275ee0c8cfbf32343be96c9cd8e538f1336。接入测试额外核对原结构所有252421条记录的方块类型及全部47553个屏障位置，并在原生世界以普通实体移动碰撞尝试穿越西侧屏障。

这是普通移动碰撞和入口授权的证据；不承诺防止管理员/创造模式、其他模组传送、末影珍珠等全部跨界机制。生存玩家真实网络客户端的全流程测试仍待独立方执行。

## 隔离复现

源函数在datapack/data/fts6_access/functions。build-access.mjs按当前20分片生成包；相同函数/结构被复制到qa/runtime/src/main/resources/data/fts6_access供实际Forge测试加载。qa/runtime的AccessTests.java通过原生命令分发器实际执行安装、权限0的trigger、授权与位置检查、返回和移动碰撞，结果见runtime-result.json；日志在qa/runtime/access-run.log；after-access-zone.nbt是外台和内侧落点邻域的刻后捕获。

运行既有隔离Forge工程时加`-PqaNamespace=fts6access`只运行接入测试，不重写先前11份自然刻报告。该工程的工作目录始终为新S6内的qa/runtime/world-isolated。测试将FakePlayer注册到本隔离服务器玩家列表供原生@a选择器查询，并换用原生ServerGamePacketListenerImpl，只抑制向不存在客户端发送数据包，保留原生teleport位置更新；没有手写传送逻辑或伪造客户端确认。因此本测试不构成真实客户端接收/确认传送的证明。

两个marker区块由安装函数保持forceload，使无玩家时入口锚点仍可查找。安装时临时加载主结构范围，完成后释放该范围，仅保留锚点所需区块。因此部署预留区不得含其他责任方的forceload；当前不提供自动卸载/迁移函数。

## 自动调度补测

此前AccessTests的13项测试显式调用load/tick函数，只证明函数行为，不证明数据包自动调度。此缺口另由AccessHooksTests补测：交付ZIP中的data/minecraft/tags/functions/load.json及tick.json原样复制到实际测试资源，并由Minecraft函数标签调度。测试源码没有显式调用fts6_access:load或fts6_access:tick。

首次阶段使用全新的qa/runtime/world-hooks-isolated世界，验证load标签自然创建trigger目标、tick标签实际注册、锚点自然加载，权限0模拟玩家执行trigger后仅等待自然服务端tick即可进入和撤权返回；未授权伪造请求与半径外伪造请求均被自然tick拒绝。随后调用与/reload同一服务端重载资源路径，等待异步重载完成，再次靠自然tick往返。第二阶段在前一JVM正常退出保存世界后，用不同JVM重启相同世界，不执行安装或手动forceload，查找原锚点，再完成相同往返及reload后往返。

运行参数：初次为-PqaNamespace=fts6hooks -PaccessPhase=initial -PqaWorkDir=world-hooks-isolated；正常退出后改-PaccessPhase=restart运行第二阶段。首次复现应使用一个新的qaWorkDir目录；不要对含前几轮测试残留的旧世界重做首次安装。结果为hooks-initial-result.json与hooks-restart-result.json，日志在qa/runtime/hooks-initial-run.log及hooks-restart-run.log。首次阶段共150个延迟等待自然刻，重启阶段共300刻，不包括启动时间；报告显式load/tick调用数为0。首次查锚点前等待50刻；重启阶段等待200刻供持久化区块及实体完成加载，不能宣称启动第一刻就可找到锚点。

这个测试覆盖真实函数标签调度，但玩家仍为已注册的FakePlayer，数据包作为测试模组资源加载，与ZIP资源逐项哈希核对；不是把ZIP装入正式实例，也不是网络客户端完整通关证明。普通数据包ZIP安装说明保持不变。测试初期旧世界中的重复锚点及夹具查询范围错误均保留在qa/runtime/history/hooks-attempts，不作为通过证据。
