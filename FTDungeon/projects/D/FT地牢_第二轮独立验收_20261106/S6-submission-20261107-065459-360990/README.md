# S6 火山熔岩 11—20 修订待审包

状态：当前候选制作、自检和有限隔离运行完成；待独立复核，accepted=false。审查意见3的外部步行接入仍未关闭。未写正式 saves/mods/config/KubeJS；隔离测试世界仅位于本包qa/runtime内。旧S6登记的218个文件哈希未变，无付费生图。

入口：ready-for-review.json；delivery-manifest.json绑定当前文件及外部参考图/提示词；qa/delivery-audit.json汇总11份候选的当前哈希检查。历史目录、构建缓存及隔离世界不作为正式候选。

## 本轮修订

- 15训练场移除规则棋盘噪声，改连续沙地和连片踩踏斑块，保留草人、横栏、坡障及绕行。
- 19观景台随包加入44格峡谷纵深，具有近侧台地、中景岩柱和石桥、远处熔岩瀑；原台体整体z+44，所有旧目标同步平移而未删除。player-canyon.png为未剖切玩家眼高视图；canyon-overview.png展示环境全貌。
- 12六扇铁门每门内外各设一个同侧可触及拉杆，原生射线和距离检查后分别开闭通过。
- 其余房间保持先前修订。14为方块武器护甲陈设；18头颅使用真实方块实体，石棺不可开启；19铜质观测器无放大功能；20拉杆无线路、宝箱为空且无锁。

## 合并

assembly/s6-volcanic-connected.nbt：207×36×180；SHA256 5b72d94c89871ebe9c262f92d6c00275ee0c8cfbf32343be96c9cd8e538f1336。下翼移至z145以容纳峡谷。layout.json登记实际偏移及端口。58个内部路线目标静态可达，走廊3×4全截面2688空气采样通过。联合外壳47553屏障格；唯一源改动例外是20退役外口12空气格封玻璃。

20个分片每轴≤48，assembly/slices目录仅保留当前分片。反拼252421记录的状态及45份BE载荷逐项一致，结构实体0。旧分片归档。

## 当前哈希实测

十房及合并注册属性、模板回写、全非空气canSurvive、原生液体碰撞面/可燃性静态自检通过。11份配方重建压缩字节一致；另从复制并冻结的十房输入重建合并，输出字节一致且原输入未变，见qa/frozen-assembly-reproduction/result.json。

真实Forge47.4.10/MC1.20.1隔离GameTestServer逐份放置11份候选，每份400实际服务端刻，doFireTick=true、randomTickSpeed=3、强制流体排程和邻居更新，另执行20次随机刻分派。无新火、外溢流体、危险方块变化或刻后失去支撑。15和合并各5处普通墙连接状态随邻居更新变化，完整列于result.json，未冒称刻后逐字节不变。六门内外控制在单房及合并均实际开闭；所有容器以FakePlayer原生use和槽位放入/取回验证。日志、源代码和11份刻后NBT均随包；刻后NBT仅为证据，正式候选仍以ready-for-review.json为准。

## 复现与清单

技能run-build.mjs配合各recipe.mjs，--data-version 3465；recheck.ps1跑静态、原生和渲染；split-verify.mjs核反拼；verify-reproduction.mjs核11份重建；reproduce-frozen-assembly.mjs核冻结输入合并；qa/runtime包含独立Forge测试工程、run.log、result.json与after-S6-*.nbt。finalize-review2.mjs重新核绑定并生成此清单；status.mjs与finalize.mjs均转入该脚本。

文件名带independent的兼容报告由制作方调用原生工具生成，不代表独立验收。每房qa/self-review.json为静态阶段记录，后续运行结论由本轮qa/runtime-coverage.json按当前哈希补充。清单完整列入非历史的工具、配方、设计、参考索引、渲染、QA、日志、冻结副本、运行工程及20分片。排除项和理由明确记入manifest；manifest自身不自哈希。

## 尚未验证

- 外部世界到封闭屏障内的步行接入；当前entry仅是内部接入脚点，未交付外部步行开口或传送机制。
- 真实网络客户端移动、攀梯、跳跃障碍及战斗/生存通关。
- 长期运行、多朝向旋转、随机种子与整合包全部模组共同运行。
- 独立方对当前冻结候选的视觉、外部接入及最终放行。

内部入口脚点为2,4,36，其与外部之间仍有封闭屏障。未认证步行外部入口，未实现传送机制；不得据此部署或宣布整组放行。
