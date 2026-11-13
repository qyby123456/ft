# S7 interaction-traversal-v5 补充待审交付

继承local-relief-v4的十房、合并和15分块NBT，全部保持原哈希。父版已完成七房体块细化、十房及合并1200刻世界更新、66项服务器交互、字节复现及连续宽度几何检查。本轮补充原生移动、攀梯和抵达宝库后的操作证据；自检不冒充独立验收，整组accepted=false。

恢复时复核542份父版文件，全部匹配，无相关残留进程、tmp/partial候选。正常Gradle锁和session.lock保留；父版两个失败运行及最终成功运行均核对退出码，未删历史世界。

合并原102目标及新增2个宝库操作站位全部抵达，执行222030次Entity.move。逐目标从原起点开始，中间不瞬移、不注入正Y速度；水平位移和向下重力探针经过原生碰撞与自动跨步，有效步高限制0.6格。

抵达脚点后箱桶创建菜单4项全部通过，包括宝库箱；宝库拉杆原生use切换再复原通过。操作无额外站位瞬移，仍不声明客户端射线及网络包链路通过。

图书馆使用Player.travel向前顶住有支撑的攀梯，记录onClimbable及horizontalCollision，上升后横向移到二层并站稳。ladder_climbed=true、upper_landing_reached=true。

最终运行仅worldqa-vault-final，104目标通过、exit=0。此前worldqa-validated已通过102原目标；两个早期失败运行worldqa与worldqa-diagnostic原样保留。失败是探针在边界约1e-8浮点重叠处提前退出，诊断含实际坐标和碰撞盒；最终仅将附加重叠诊断盒内缩1e-6，原生运动碰撞盒未改，并取消Forge额外跨步加成，候选模型未改。

AuthorPathExport.java是旧碰撞审查器的本地派生，新增父节点路径重建；输出保留部分旧scope措辞仅表明算法来源，当前执行者为制作方。不能据此称为独立验收。

模拟同步执行Entity.move/Player.travel，不是完整逐刻survival输入循环。各目标从起点重置，不等于真人客户端一次连续遍历；不认证跳跃、战斗、呼吸、网络包或射线瞄准。computer-use所需node_repl未出现在当前工具集，未采用替代UI助手，未修改正在运行的生产客户端。

复现：prepare.py验证父版并输出104目标路径；worldqa-vault-final/run.ps1实际启动隔离Forge；submit.py校验父版和本轮原始结果。已有世界拒绝覆盖，重跑需在新隔离目录保留该源码、inputs.json和server.properties。父版ready-for-review.json与本版清单须共同冻结，本版是补充证据层。

合并SHA256：09a18f7227e051a160585c1c42a5b1ac8b5c38db108c74329dc9ef022a71849e

全部写入仅在S7独占目录，未触及正式saves/mods/config/KubeJS及共享生成器。

仍待完成：
- 当前v4结构及v5补充证据的独立复核，accepted=false。
- 真人客户端输入/网络包链路、射线瞄准后的门箱拉杆操作与连续完整行走；当前会话缺少computer-use所需node_repl。
- 训练障碍跳跃未实测；当前世界和移动结果仅零旋转，其他朝向未认证。
