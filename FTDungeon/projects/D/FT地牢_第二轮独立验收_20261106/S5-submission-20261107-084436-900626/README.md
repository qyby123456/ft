# S5 海底珊瑚 11—20 接手续作

## 两桶针对性修复（当前版本）

以 UPPER-BARRELS-FIX.md 为最新说明。17两高架桶降低三格并各加托架，源17及合并各仅6格变化，其余九源不变；37交互、74双向站位、变更源/合并1200自然刻、分片/旋转/支撑和两NBT复现均通过。合并当前e0c71315；此前eb8ac4bc的35/37与两项失败是保留的历史结果。接入命令/大厅不变，包内更新第08分片并继承已关闭实测，未安装正式存档。

## 最新交互与外部接入补交

见 INTERACTION-ACCESS-FOLLOWUP.md：当前 eb8ac4bc 保持不变。37交互目标中35项已给可达站位、眼点/命中距离、首命中射线和use证据，双向70站位检查；17高架两桶明确失败，未宣称全过。access-package/s5access.zip 提供保留57482屏障的外部大厅/入口/终点触发器方案，具体固定坐标及安装步骤见其README；尚未安装正式存档。下文历史段落关于未配置外部方案和未验证距离射线的说明，以本补交为准。

## 正式复核后最新补交

以 REVISION-REVIEW.md 和 ready-for-review.json 为最新约定：19已随NBT内置13格深水景遗迹；五格走廊的三格门喉明确登记、两边缘失败保留；两退役端各20格封口。qa/world-revision2已实际放置十房和合并，交付1200刻前后完整NBT；刻后886目标双向检查及FakePlayer原生use记录见qa。qa/reproduction-final重新生成11个NBT字节一致。下文旧阶段描述中的“未打包外景/未测合并世界”已由本次证据替代，真人移动/够到/射线、生存与氧气仍未验证。

本目录唯一可写；旧S5只读，来源逐文件SHA256见 inheritance-source-manifest.json。十张候选及配方继承，旧报告与渲染只作历史证据，新输出逐哈希复核。

独立视觉意见以 S5-preliminary-20261107-025045-361444/visual-review.md 为准，当前 REQUEST_CHANGES，后续新哈希仅制作方自检闭合。20拉杆 f99a78… 已另获独立缺陷关闭，详 lever-revisions-20261107-033714-416096/REVIEW.md；不等于整组放行。

十房候选、完整外壳、配方、接口/目标设计、内外真实Lodestone图、原生QA已交付；20为密道藏宝。17项目桌实腿承托并补准确近景；18中心头骨壁龛与前湿带；19保留高台、半阶梯及武器架，降内栏杆、增外观景窗并提供眼高视线图；20保留转角、拉杆、拱口、箱台半阶，修复背衬。所有房间在原布局补厚门框和半砖檐口，未替换为同一房盒。逐房差异与实际目检见 VISUAL-SELF-REVIEW.md。

最新原生结果：qa/world-delivery/natural-results.json 对十房当前SHA运行1200自然流体/邻居/随机刻，randomTickSpeed=3，活珊瑚及原空气未变化/未进水；六扇铁门外侧关闭、内侧再开通过。先前17、20珊瑚底座失水的失败留在world-r3；补侧水后重测，不关闭随机刻。qa/all-support-final 对每个非空气方块canSurvive逐格检查，十房和合并无失败/不支持；普通registry、模板往返及0.6×1.8人体路线见每房及merged/qa。类名Independent表示复用只读工具，不把自跑结果称独立验收。

新合并 merged/merged.nbt 尺寸217×29×94，591542逐格记录、27方块实体空载荷；两翼U形连接十房，外围57482屏障，两个未用外端各20格玻璃封口。source-integrity.json逐源逆旋转比较全部块状态/载荷（仅40封口格例外）；10个分片任一轴≤48，parts-reconstruction.json逐格及全部27载荷反拼一致。没有配置外部入口/传送，当前方案后续使用design.json内部entry/finish脚点。

复现：技能 scripts/run-build.mjs 本房recipe.mjs --out-dir 本房；node render.cjs S5-XX；配置Python check.py S5-XX。十房全通过后 run-build.mjs assemble.mjs --out-dir merged；node split.mjs；node verify-parts.mjs；Python verify-merged.py；node render.cjs merged；run-all-support.ps1。世界测试在全新本组qa目录复制native-test/src、build.gradle、gradle.properties、settings.gradle与run.ps1后运行，已有世界脚本会拒绝覆盖。

水与氧气：干燥舱、围护湿区；单房开放端口不能直接置于深海。自然刻只在外部空气的隔离Forge1.20.1服务器测试，未验证整组合并世界放置、深海倒灌、氧气消耗、玩家实际路线、容器交互或梯子攀爬。19观景窗外没有打包海底环境；17/19/14部分物件为明确方块雕塑。无生存通关承诺。不写正式saves/mods/config/KubeJS。

所有检查是制作方自检，非独立放行。待审清单供 01a1a685-045b-76d2-aa2b-2a3f24520603 读取。
