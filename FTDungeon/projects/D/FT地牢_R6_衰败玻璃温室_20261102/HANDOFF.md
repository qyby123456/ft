# R6 交接

交付根目录：`D:\ft工程文件\FT地牢_R6_衰败玻璃温室_20261102`。

- ID：`R6`，衰败玻璃温室，15×10×17（X×Y×Z），原版 1.20.1 / DataVersion 3465。
- 北入口脚部 `(7,1,0)`，内部安全脚部 `(7,1,10)`，北入口 3×3；2550 条完整体积记录。
- 四件套在根目录：`room.nbt`、`room.json`、`preview.png`、`qa.json`；精确指纹见 `delivery-manifest.json`，语义/独立身份指纹见 `qa.json`。
- 来源参考 SHA-256：`525b5a4b3c1043be2d45abe0502274509c0d5f1ffa376bb1251a4a964aee13ee`；没有新生图请求。
- 原生证据：`qa/native.json`、`evidence/forge-native-console.log`、`evidence/forge-native-server.log`；静态与九个负例分别在 `qa/static.json`、`qa/negative.json`。
- 四向放置必须沿用现有 FT 核心 known-shape 契约（flags 18，keepLiquids=false），等待光照传播后再触发依赖照度的支撑检查。首次直接 UPDATE_ALL 在未完成光照传播时作物脱落的失败日志保留，不能宣称该方式通过。
- 屋顶/玻璃外壳完整；中央三格干路，侧边密封水槽；树叶持久化，成熟作物，四菌光体床灯，链条吊篮/吊灯有顶锚，垂藤 age=25。
- 无实体、方块实体、奖励、刷怪笼、命令/结构/拼图方块；没有加载玩家存档、启动客户端或部署。

**不替接入负责人决定** slot/origin/catalog 指纹；不修改共享核心、正式存档、其它交付和并行文件。需要接入负责人另做大厅通路、全体积预检、占用拒绝、持久标记/幂等与部署验证。

软件预览不是实机截图，原生 GameTest 不是完整整合包客户端/多人/FPS 验收。预览为剖切，但最终 NBT 未剖切。模板重复放置不是核心标记幂等证明。
