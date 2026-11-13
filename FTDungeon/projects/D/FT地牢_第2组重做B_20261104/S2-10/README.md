# S2-10 砂日多殿地牢总览

第2组第10张候选，待独立复核。第11张未开始。对应唯一总览参考 `D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-10-overview.png`，参考SHA256见room.json/registration.json。

完整Java1.20.1原版NBT位于incoming/G2B-10/room.nbt，DataVersion3465，65×18×67。独立新建8个不等尺寸区域：入口、前库、仪式厅、池庭院、后大厅、东宝藏小室、封顶密室和长廊；通过10段实际有地板/净空/顶盖的通路连接。入口到庭院/后大厅的中轴、西侧转角分支和东长廊回路均为真实方块布局，非预览拼接。详见ANALYSIS.md和layout.json。

自检：42个关键身体目标（1外接口+8区域中心+10连接中心+23细节目标），0.6×1.8原版碰撞体/半格网/步高0.5/无游泳无跳跃；每10段连接全幅净空5高与地板支持逐列检查；42个带原版标签的静态canSurvive点。五只真箱10堆固定示例物品、两只空27格桶均原生载入；箱前3高空气、箱上2格空气、原版静态isChestBlockedAt均通过。复用独立工具自检不等于独立验收。

超48分片必须与总源一起保留：tiles/part-01..04.nbt。尺寸分别32×18×34、33×18×34、32×18×33、33×18×33。原点分别(0,0,0)、(32,0,0)、(0,0,34)、(32,0,34)，共享同一世界基准原点，不旋转镜像、不跳过空气，顺序1→2→3→4。assembly.json记录哈希、原点、次序、空气覆盖和校验；qa/tile-reconstruction.json为78390格状态与7实体完全相等的重组证据。qa/reconstructed-from-tiles.nbt是校验产物，压缩字节可因调色板顺序不同，不以二进制相同替代解码相等。

11张真实Lodestone浏览器截图：exterior/reverse为完整源全景及反面；detail/grand/ritual/gallery/shrine为完整源内部细节。后四图只在浏览器响应临时改变相机中心，无NBT/模型/可见性修改，无技能文件修改；相机参数记录于qa/lodestone-screenshots.json。north/south/west/east仅为preview-only空气裁切，声明坐标与派生哈希见derivation.json；全部7容器实体保持一致。显示裁切不可部署，不能与完整源混称。

水池只有40个level0初态，全部Y3、底为实心切制砂岩，水平邻接无空气。无高位水源/跌水柱。流体刻、源水生成、分片放置接缝更新均未模拟，不认证流水稳定。顶带/真天井和完整顶盖逐区统计见room.json与qa/dynamic-boundaries.json。自然光/天气/刷怪/随机刻/箱盖动画/猫挡箱/菜单交互/物品拿取/旋转镜像/实际玩法和正式部署均NOT_RUN。

所有源码、材料、参考哈希、前9张冻结源哈希复核与交接文件哈希齐备。使用已有local-verification.jar，全部class字节与mapped源一致，来源证明在qa/native-runtime-provenance.json。未改正式存档、mods、配置或验证jar。

复现顺序：技能run-build.mjs recipe.mjs --out-dir incoming/G2B-10 --name room --data-version 3465；prepare.py（含分片和重组）；validate-native.ps1；capture.mjs；finalize.py。finalize固定最终源哈希。依赖路径在脚本中；FILE_MANIFEST.json排除自身和Python缓存。
