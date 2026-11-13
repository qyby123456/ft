# 源码与成品精确关系

本目录为独立 D 盘封存，不代表已部署或允许启用。

## 新封存成品

路径：`deliverables/qys-event-forge-1.20.1-1.0.6.jar`。
SHA256：`03A04FA16F728BA5ED904E09627278CB7ADFA4597C278CABFE49C8A82D7A511E`。
字节数：192947。内部 modId `qys_event` / version `1.0.6`。
它直接对应 `project` 的完整构建输入，固定 ForgeGradle 6.0.54、Gradle 8.8、Microsoft Java 17.0.8+7-LTS。
两次干净构建 hash 相同；源码及构建配置逐文件 hash 见 `audit/build-inputs.json`。
Java 32 个、资源 31 个、测试 Java 3 个；对应成品 55 个 class 与 32 个资源/清单条目。
源码 ZIP 同时包含构建配置、资源、模型/贴图、独立 KubeJS 集成、历史参考与审查证据，不包含依赖缓存与生成的构建目录。

## 活动旧包不是本批部署结果

观察包 SHA256：`262B32847663B253CE5F117ED6B6DE49817A0016AF6EE75F60149A08A4D27B06`，190667 字节。
它来自 10/12 金币锅增量部署，在 `9FC66CD0...` 独立分离版上增加五个金币锅类。
新封存包与该观察包有完全相同的 87 个实际文件条目，全部资源/清单字节不变。
四个 class 文件的二进制 hash 不同，但忽略常量池索引后，SRG 方法签名、指令、分支偏移与操作数全部一致。
不同的类为 CoreFacingPolicy、CoreFacingMixin、WeatherForecasterBlock 与 GoldPotWishRules$Category。
这些记录区分“字节相同”与“已审查的指令相同”，不宣称新构建包整体等于旧增量包。
完整对应与差异见 `audit/source-jar-map.json`、`audit/unified-versus-observed.json` 和 `audit/bytecode-comparison.json`。

## 审查证明与限制

完整链接检查覆盖 55 类 / 453 方法 / 12527 项断言，无缺失类型、成员或字节码栈失败。
48 个通用类通过禁止客户端类的加载核验，7 个客户端类隔离；未执行游戏类初始化或独立服务端启动。
发货模拟回归 671 项通过，金币锅完整 10000 签与失败不消费验证通过。
模型为预言机 510 面、天气机 288 面、许愿井核心 282 面，模型和图集来自冻结运行资源。
JSON、PNG chunk CRC、三种方块的四向 blockstates 和自定义贴图引用闭环通过。
本批没有游戏内实测，没有执行启用、禁用、替换实例包或修改 Boss/NF/存档。
禁用异常与待批准安全方案见 `audit/GPT-REVIEW.md`、`audit/SAFE-DISABLE-PLAN.md`。

`SHA256SUMS.txt` 覆盖每个封存交付文件、源码 ZIP 与最终回执；自身不自哈希，缓存/生成目录明确不属于交付范围。
