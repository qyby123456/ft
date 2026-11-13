# 33ceb33f 门拉杆与容器交互范围补证

当前合并体SHA-256：`33ceb33f633cfa6e797cc184cd3c5ff34ce9f0901e9b909da161782ec5cc0e6c`。只新增补证文件；原ready-for-review.json及artifact-manifest.json未重写，原498项声明文件已全部重新核对且哈希不变。十房、合并体、15片及原目标均未修改。

刻前结构及实际服务器刻后NBT，各40件均有从入口连通的完整碰撞路径、起始站位、眼睛坐标、命中点/面和原生outline首命中判定。31容器及8拉杆可站立交互；井中链吊空桶需要跳起，不将其记作干地站立可触及。每次射线命中距离限制为原版生存4.5格，细站位偏移最多每轴0.19格且做11点身体及地面支撑检查。

新建runtime-interaction-r2隔离Forge 1.20.1世界，放置同哈希完整合并体并自然运行600刻，再逐件定位至上述入口连通站位，实际检查原生玩家碰撞箱、地面支撑、Player.getEyePosition及Level.clip(OUTLINE, NONE)。只有首命中正确、距离<=4.5格且身体无遮挡时才调用ServerPlayerGameMode.useItemOn：32容器服务端菜单全部打开，库存全空；8牢门经真实射线两次操作全部开关复位。最大实际眼睛至命中距离为4.334478547格。

悬桶在合并坐标[197,11,93]，原生jumpFromGround后使用Player.travel推进，未注入向上速度，未在跳跃中moveTo。实测脚坐标[197.5, 9.419999986886978, 88.69000000000005]，眼睛[197.5, 11.03999999165535, 88.69000000000005]；实际射线距离4.334478547格并成功打开BarrelMenu。实际跳跃轨迹在interaction-runtime.json/jump_trace。离线报告中planned_jump_rise=0.9只表示候选几何，不冒充实测跃高。

首次runtime-interaction-r1因0.2格细站位贴墙触发原生碰撞失败，原日志和源码保留；最终站位退为0.19格，离线身体尺度也改为保守0.600002×1.800001，随后实服通过。没有通过改平家具、删除目标、移动桶或改变实体位置解决问题。

范围：每件起始站位仍由操作员定位；完整入口路径由离线原生碰撞检查证明。本次实服验证局部伸手交互和一次跳跃，不等于真人客户端GUI、全程逐件走路或父线程独立世界复跑。整组仍待独立验收。

入口：qa/interaction-range-current/supplement-summary.json；逐件路径及射线：current_source-interactions.json与real_server_posttick-interactions.json；实服输入、源码、控制台、exit与报告：runtime-interaction-r2；全部新增证据路径/字节数/哈希：qa/interaction-range-current/supplement-manifest.json。
