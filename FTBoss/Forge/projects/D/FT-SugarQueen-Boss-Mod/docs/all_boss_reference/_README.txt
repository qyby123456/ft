FT 全 Boss 机制与技能数值 —— 源文件说明（2026-09-19 新建）
==========================================================

这份目录是「全 21 只 Boss 一份文档」的**内容源**（10 只独立 Boss + 甜苹果庄园 11 位轮换），由 2026-09-19 对当前源码逐类重新核对后全新写成，
不是从旧文档（docs/boss_reference/、docs/non_hidden_boss_quickref/、docs/hidden_boss_quickref/）
沿用来的 —— 旧文只当"要覆盖哪些机制"的线索，所有数值一律回源码取，并带 `常量名 = 值（文件:行）` 出处。

生成 Word：
    node tools/docgen/make_boss_doc.cjs "C:\Users\YOUR_USERNAME\Desktop\FT-全Boss机制与技能数值_20260921.docx" ^
         docs/all_boss_reference "副标题写构建戳"

最近一次生成（2026-10-18，用户要求「写一份新版的 ftboss Boss 机制技能数值报告放到桌面」）：
    FT-全Boss机制与技能数值_20261018.docx —— 4,662,713 B，6 个 OOXML 部件，151 张表，7591 段，
    副标题「全 21 只 Boss · 构建戳 easter-v155-sword-flat-face-turkey-display-java · 源码快照 2026-10-18」，
    OneDrive 桌面 + C:\Users\YOUR_USERNAME\Desktop 同名同哈希
    `73C63B723115C94B9B2B5E9A70E0771070061E6382D07F5822AF126AEFCF0829`。
    本版把 2026-10-10～10-18 的一批机制按当前主工程源码同步回参考文档（沿革仍记 docs/CHANGELOG.md）：
    · 全局：最终伤害 >300 整次归零（=300 放行、/kill 例外；鼠群整体不适用）、非庄园 Boss 陆地移速 ×2、
      Boss 负面效果事件层 DENY 已关闭（各实体按自身自清/免疫口径如实写）；
    · 非庄园：鼠群重写为 20000 主王 + 12/16 只副鼠群共享 400 血池 + 奶酪装置循环；
      benger 追猎突击与虚化客户端同步；8 只非庄园 Boss 的护甲/抗击退/移速与 300 规则统一；
    · 庄园：基础生命 1500、物理限伤 200 点/20 tick + 半血护甲翻倍、进场与开场增益复制、
      麻雀 500 支援血/沉默驱散/随机借招/千羽散华、玄蓝飞轮上限 15 与领域数值回调、竹鸟登场与共享血池；
    · 33 祁崽基础生命改 1500。
    两个体检：check_ref 格式问题 0 类；check_numbers 一致 146 / 不一致 0。

上一次生成（2026-09-21 晚，用户明确要求「补：新增一节写祁崽，然后重新生成 Word」）：
    FT-全Boss机制与技能数值_20260921.docx —— 4,620,958 B，6 个 OOXML 部件，156 张表，7828 段，
    副标题「全 21 只 Boss · 构建戳 easter-v155-sword-flat-face-turkey-display-java · 生成于 2026-09-21」，
    桌面 + `OneDrive\桌面` 同名同哈希 `CA66AA3A6AD99FE9A7CA97A90BDDC075D072084DEDD95E77DE5D5E8007C574E8`。
    ★ 这一版**已含祁崽**（variant 11，见 33_qizai.md），并顺带改正了它进轮换后过期的计数：
    庄园轮换 10 位 → **11 位**、`MANOR_COUNT` 10 → 11、胜利进度 n/10 → n/11、
    刷怪蛋 10 个 → **11 个**、胜利宝箱武器 19 项 → **20 项**、
    共享属性表的护甲 12.0 → 6.0 与移速 0.33 → 0.1（v176 的减半此前没同步）。

上一次生成（2026-09-21 白天，同一天更早的一版，已被本次覆盖）：
    4,390,600 B / 146 张表，副标题写「全 20 只 Boss」—— 不含祁崽。
（更早的 `FT-全Boss机制与技能数值_20260919.docx` 仍留在桌面，未动。）

生成前两个体检（都要 exit 0）：
    node tools/docgen/check_ref.cjs     docs/all_boss_reference
    node tools/docgen/check_numbers.cjs docs/all_boss_reference

当前小节（按文件名排序拼装）：
    00_global.md                 全局规则：伤害倍率与上限 / 人数缩放 / 强化包 / 狂暴 / 禁疗削甲 /
                                 召唤限制 / 连死怜悯 / 仇恨 / 脱困 / 麻雀支援 / 血条 / BGM / 交易
    10_sugar_queen.md            糖霜女皇（姜饼王）
    11_butcher.md                屠夫（肉山）
    12_saya.md                   沙耶（茶树古树）
    13_wine_barrel.md            酒魔·酒桶王
    14_rat.md                    鼠群（主王与副鼠群共享血池 / 奶酪装置）
    15_defect.md                 故障机器人
    16_compost.md                炫彩答辩超人
    17_apple.md                  苹果爵士·蜜脆
    18_benger.md                 边牧犬王
    19_tide_fish.md              潮宴鱼王·沧鳞
    30_manor_shared.md           隐藏 Boss（甜苹果庄园十一人）共享机制
    31_manor_variants.md         隐藏 Boss 各角色专属技能与数值（十一位逐表，祁崽另见 33）
    32_manor_fight.md            庄园战整体流程（演出 / 轮换 / 防逃课拴绳 / 玄蓝阶段 / 奖励）
    33_qizai.md                  祁崽（庄园第 11 位）：十二步出招循环 / 六门铁魔法 / 三颗浮游法晶 / 终结难度
    40_summons_weapons_loot.md   附录：召唤物 / Boss 掉落 / 音乐物品 / 交易

写作口径（与 docs/boss_reference/_README.txt 一致）：
- 只写当前生效的数值与机制，不写历史沿革（沿革记 docs/CHANGELOG.md）。
- 代码里查不到的写 `?`，禁止猜数字。
- 时间 tick（1 秒 = 20 tick），距离「格」；伤害写字面常量并注明是否还要乘全局倍率。
- markdown 子集限制：H1 每份文件恰好 1 个、列表不嵌套、表格列数一致、单元格内不出现竖线、不用代码块。
