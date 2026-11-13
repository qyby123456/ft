# S1 苔藓古林第二轮 11—20：复审整改交付

本版对应独立验收 `D:\ft工程文件\FT地牢_第二轮独立验收_20261106\S1-submission-20261107-045549-014527\REVIEW.md`。制作方检查已完成，等待独立复审；`independent_acceptance=false`。交付状态与入口见 `ready-for-review.json`、`handoff-for-independent-review.json`。旧 S1 目录只读，227 项继承文件重新核对无修改，见 `old-source-readonly-verification.json`。未写正式存档、mods、config、KubeJS，未生成付费图片。

## 本次整改

19 号保留平台、原路线和高位出口，新增北向透明玻璃观景窗；合并件实际包含 64 格纵深的树林侧景，树木有近中远层次。高位接廊移到东侧，避免挡住北向视线。`scenery.mjs` 与 `scenery/s1-19-forest-depth.nbt` 是实交付几何，摆放坐标见 `merged/scenery-placement.json`。11 号仅清除侵入完整接口包络的一格根系；其余房间沿用既有整改成果。本次没有重做十间房。

当前合并件 `merged/s1-mossy-functional-11-20.nbt`：492×40×138，448974 条显式方块记录；SHA-256：

`b960960f6cec50c986c7360821408573c0cac7f0c36828073ddeb4d8b9f38553`

`merged/chunks` 共 33 份，每边≤48。`merged/integrity.json` 证明十房原有显式空气、状态、方块实体及实体载荷逐项保留，并证明分片反拼一致。旧合并件、旧房间和旧根目录证据位于 `revision-history/pre-review-remediation-20261107`，不作为当前通过证据。

## 画面与通路

当前合并画面为 `merged/complete.png`、`merged/cutaway.png`、`merged/connector-watch.png`、`merged/player-watch-forest.png`，由真实 Lodestone WebGL 渲染。最后一张使用实际平台站位，眼睛坐标 [402.5,15.62,83.5]、目标 [409,18,25]、视角 72°，完整几何不剖切，可见窗外树林深度。它是玩家眼高视角的结构预览，不是 Minecraft 客户端截图。剖切图只修改显示副本。

`corridor-audit.py` 覆盖全部 6407 个声明地面采样、四格净高、相邻半格高差，以及 20 个接口完整宽高和五格纵深包络，问题 0。原生静态路线检查合并件 76/76 目标、十个单房 56/56 目标；合并件 320366 个非空气方块 canSurvive 失败与不支持均为 0。原有 56 个目标没有删减。

`merged/atlas-*.png` 为历史遗留预览，不属于当前 `merged/render.json` 的四张绑定图片，不应用于判断本版合并视线。

## 隔离世界实测

`worldqa-run4` 使用独立 Forge 1.20.1-47.4.10 GameTest ServerLevel；仅装原版、Forge 和本次测试模组。十单房与合并件均真实放置并运行 1200 个普通服务器刻，另执行邻格与流体调度。`doFireTick=true`、`randomTickSpeed=3`。按钮实际 use 并验证原铁门打开；容器检查盖子遮挡，实际 use，并执行物品放入取回。最终 `world-report.json` 的列出问题为 0：支撑失败、掉落物、火、新增外流与交互失败均为 0。两具盔甲架在对应单房和合并件中保留。

保存了 43 份刻后 NBT（十单房加合并件 33 分片）、哈希、完整日志、测试 XML 和退出码。`decoded-post-world-analysis.json` 解码比较初始与刻后状态，记录真实变化；没有声称刻后字节不变。`assemble-posttick.py` 按已验证哈希的捕获文件重组刻后候选，绑定关系见 `worldqa-run4/posttick-inputs.json`。

`worldqa-walk/walk-report.json` 在实际服务器碰撞形状下以 FakePlayer Entity.move 对 132 个目标执行去程与返程，共 85420 次移动，失败 0。`worldqa-walk-posttick/walk-report.json` 对上述刻后重组候选重复同类检查，132 个目标全部往返成功，失败 0。两份报告分别绑定初始与刻后候选哈希。

能力边界：上述移动采用半格路径节点，没有玩家逐刻重力积分、键盘操作、跨栏跳跃、攀爬或战斗认证；容器测试不证明客户端 GUI 动画；强制加载区块不证明每个方块都获得随机刻。没有人类客户端自由行走验收。装饰拉杆没有接线。

前几轮失败证据完整保留：`worldqa` 是大体积一次捕获导致内存不足；`worldqa-run2`、`worldqa-run3` 的残留掉落物来自原测试地形清场。最终第 4 轮以抑制形状更新的清场和外扩边界解决，放置前清除的 ItemEntity 为 0，放置后没有清除掉落物。失败轮次不冒充通过。

## 复现与文件绑定

`delivery-file-manifest.json` 逐文件记录当前交付的配方、公共源码、工具、design、QA、预览、刻后 NBT 和分片 SHA-256，并记录外部参考图、提示词及依赖工具的哈希。明确排除历史备份、独立复现输出及服务器/构建缓存；排除清单自身以避免递归哈希。

在本目录使用工单 Python `C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`。按需复现：

1. 十房逐一执行 `./qa-room.ps1 <房间目录>`，再执行 `python check.py <房间目录>`。脚本使用当前配方生成、补充装备实体并渲染。
2. 使用技能 `scripts/run-build.mjs` 构建 `scenery.mjs`，指定 `--out-dir scenery --data-version 3465`。
3. `python merge.py <独立输出目录>` 可独立输出合并件及分片；`python revision-tools.py reproduce` 自动独立输出并逐字节比对。当前 `merge-reproduction-report.json` 已证明主件与 33 分片一致。
4. 当前目录正式合并可执行 `python merge.py`，随后 `node render.cjs <本目录>/merged`、`python check.py merged`、`python corridor-audit.py`。不要使用旧 `merge-views.py` 改写当前视角。
5. `python reproduce.py` 独立重建十房；当前 `reproduction-report.json` 的十房逐字节一致。
6. 真实世界复测从 `worldqa/run.ps1` 与其 native-test 源码复制到新的测试目录后运行；已有 world 的目录拒绝复用，避免污染证据。刻后解码与重组分别运行 `post-world.py <运行目录>`、`assemble-posttick.py`。移动测试源码为 `world-walk-template.java`，运行入口见两份 walk 目录的 `run.ps1`；刻后输入由 `S1_WALK_INPUTS` 指向 posttick-inputs.json。
7. 交付汇总顺序为 `python finalize.py`、`python publish.py`，最后 `python revision-tools.py manifest`；哈希清单生成后不再修改交付文件。

`takeover.py`、`revision-metadata.py` 等一次性迁移脚本不是重建入口，请勿重复追加元数据或路线。所有制作方自检与实测均不替代独立复审结论。
