FT-Boss 数值与机制文档 —— 源文件说明
======================================

★★★ 2026-09-14 用户明确要求：**不需要每次改代码都同步这份文档** ★★★
    原话:「不需要频繁更新boss技能介绍那些文档」。
    所以改代码时**默认不动这里**（也不要在桌面上生成新的 vXXX.docx 版本）——
    只有用户点名要这份文档、或者攒了一大批数值改动时，才跑下面两个体检 + 重新生成。
    （代码改动照旧记进 docs/CHANGELOG.md，那份是变更历史，不算"技能介绍文档"。）

这个目录里的 *.md 就是那份 Word 文档的**内容源**（按文件名排序拼装，所以用 00_ / 10_ / 30_ / 40_ 前缀控顺序）。

生成 Word：
    node tools/docgen/make_boss_doc.cjs "C:\Users\YOUR_USERNAME\OneDrive\桌面\FT-Boss-数值与机制速查_vXXX.docx" ^
         docs/boss_reference "副标题里写构建戳 easter-vXXX-…"
    （第三个参数是副标题；生成完再复制一份到 C:\Users\YOUR_USERNAME\Desktop\）

★ 生成前跑**两个**体检（都退出码 0 才算过）：
    node tools/docgen/check_ref.cjs       # 格式：行数/字数、各标题数、表格列数是否整齐
                                          #   ⚠ 会挑出 Word 排版会变丑的写法：代码块(```)、**嵌套列表**、
                                          #     表格列数不一致、H1 不是 1 个 —— 嵌套列表是最常见的翻车点
    node tools/docgen/check_numbers.cjs   # 数值：文档里写的「常量 = 值」跟代码真值是否一致
                                          #   只认 `NAME = 123` 与「关键常量」汇总行里带 `.java:` 的 `NAME 123`；
                                          #   派生算式(6 × 20 × SKILL_MULT = 360)、分数(4/3)、秒 vs tick(30 秒 = 30*20)
                                          #   都会自动跳过，不会误报；同名常量跨类碰撞时任一命中即算对。
                                          #   SHOW_NOTES=1 额外打印"不是 static final 常量/查无此名"的提示项。
    （v112/v113 就是靠 check_numbers 抓到：XUANLAN_BEAM_DMG 文档 24 / 代码 72、故障机器人 T_BEAM 245 / 285、
      T_PHASE 32 / 52、以及庄园三张唱片的过期长度。）

音频时长（唱片长度参数必须等于真实时长，别凭印象写）：
    node tools/audio_tools/ogg_dur.cjs <ogg...>     # 纯 JS 扫 OggS 页取最大 granule ÷ 采样率
                                                    # 输出「秒 + tick(= 秒×20) + 字节数」

脚本只吃一个很小的 markdown 子集：
    # / ## / ###   标题（H1 会强制分页，所以每份文件只写一个 #）
    - 文字         列表（只支持一层，别嵌套）
    | a | b | c |  表格：表头行 + |---|---| 分隔行 + 数据行（单元格里不要出现 | 字符）
    普通段落
    行内可用 `代码`（等宽）与 **粗体**
代码块(```)、HTML、图片、单元格内换行都不支持。

★ 写入规则（用户 2026-09-14 定）：**只写"当前生效的数值/机制"，不要写历史修正**。
  即：不要出现"v118 改成 12（原 6×4/3=8）""原来 4.5，现在 8.0"这类对照 ——
  数值一律以**代码里当前的值**为准（改完代码重跑 check_numbers.cjs，它会把不一致点出来）。
  版本沿革只记在 docs/CHANGELOG.md，那份才是变更历史。

数值维护约定：
- 只写代码里真实存在的数字，并在括号/说明里给出出处（常量名 或 文件:方法）；查不到的写「?」，不要猜。
- 伤害一律写代码里的字面常量；实际打到玩家身上还要乘全局系数（EasterBossEntity.BOSS_DAMAGE_MULT = 4/3 等），
  条目里用「×全局倍率」注明。
- 改完代码记得重跑生成器（数值会随版本变），并把构建戳写进副标题。
- 改完**务必跑 `check_numbers.cjs`** —— 它专门抓"代码改了、文档忘了同步"这一类。

当前小节：
    00_global.md                 全局公共规则（伤害体系 / 狂暴 / 限制 / 禁疗 / 麻雀 / 音乐 / 交易）
    10_sugar_queen.md            糖霜女皇（姜饼王）
    11_butcher.md                屠夫
    12_saya.md                   沙耶（茶树）
    13_wine_barrel.md            酒桶王（酒魔）
    14_rat.md                    鼠群
    15_defect.md                 故障机器人
    16_compost.md                炫彩答辩超人
    17_apple.md                  苹果爵士·蜜脆
    18_benger.md                 边牧犬王（第 9 位主 Boss）
    30_manor_shared.md           隐藏 Boss（庄园彩蛋十人）共享机制
    31_manor_variants.md         隐藏 Boss 各角色专属技能
    32_manor_fight.md            庄园战整体流程（演出 / 轮换 / 终章 / 奖励）
    40_summons_weapons_loot.md   召唤物 / 武器 / 掉落 / 音乐物品

另有一份**独立的精简速查**（不在上面这套里、不会被并进主文档）：
    docs/hidden_boss_quickref/00_hidden_boss.md
        隐藏 Boss（甜苹果庄园 · 彩蛋十人）**只讲技能与伤害**，不含演出/台词/提示文案/音乐。
        生成： node tools/docgen/make_boss_doc.cjs "C:\Users\YOUR_USERNAME\OneDrive\桌面\FT-隐藏Boss技能与伤害速查_vXXX.docx" ^
                   docs/hidden_boss_quickref "副标题里写构建戳"
        （它跟主文档一样要过 check_ref.cjs / check_numbers.cjs 两个体检。）

还有一份**与之互为补集**的常规 Boss 报告（2026-09-18 新增，同样独立、不进主文档）：
    docs/non_hidden_boss_quickref/   00_non_hidden.md + 20_~29_ 每只一份
        覆盖**除隐藏 Boss 外的 10 只**：糖霜女皇 / 屠夫 / 沙耶 / 酒桶王 / 鼠群 /
        故障机器人 / 答辩超人 / 苹果爵士 / 边牧犬王 / 潮宴鱼王；
        00_ 写公共机制（伤害限制 / 人数缩放 / 强化包 / 狂暴 / 召唤限制 / 信标增益 /
        禁疗削甲 / 连死怜悯 / 血条 / 音乐 / 掉落交易）。
        生成： node tools/docgen/make_boss_doc.cjs "C:\Users\YOUR_USERNAME\OneDrive\桌面\FT-常规Boss机制报告_vXXX.docx" ^
                   docs/non_hidden_boss_quickref "副标题里写构建戳"
        ★ 内容取自 2026-09-18 对源码的逐类重新核对，不是照抄 boss_reference/ 的旧文 ——
          两者不一致时**以本目录为准**（差异清单见 docs/CHANGELOG.md 同日条目）。
