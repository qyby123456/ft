/* 40_summons_weapons_loot.md 收尾：唱片名/时长刷成最终值 + 补上缺的 2 张（manor_guignol / benger）。
 * 用法: node fix_boss_docs3.cjs
 */
const fs = require('fs');
const p = 'D:/ft工程文件/FT-SugarQueen-Boss-Mod/docs/boss_reference/40_summons_weapons_loot.md';
let t = fs.readFileSync(p, 'utf8');
const miss = [];
const rep = (from, to) => {
    if (!t.includes(from)) { miss.push(from.slice(0, 52)); return; }
    t = t.split(from).join(to);
};

/* ① 显示名 */
rep('| ftboss:music_disc_manor_cue | 庄园·序 |', '| ftboss:music_disc_manor_cue | 庄园唱片 · 序 |');
rep('| ftboss:music_disc_manor_battle | 庄园·战 |', '| ftboss:music_disc_manor_battle | 庄园唱片 · 战 |');
rep('| ftboss:music_disc_manor_dual | 庄园·双轨 |', '| ftboss:music_disc_manor_dual | 庄园唱片 · 双轨 |');
rep('苹果爵士唱片·虫蛀暴走（二阶段）', '苹果爵士唱片 · 二阶段');
rep('炫彩答辩超人唱片 · 二阶段（二阶段）', '炫彩答辩超人唱片 · 二阶段');
rep('| ftboss:music_disc_defect | 故障机器人 |', '| ftboss:music_disc_defect | 故障机器人唱片 |');

/* ② 两首重做曲的时长/tick */
rep('music/butcher.ogg，2907 tick（145.34 s，2:25）', 'music/butcher.ogg，4568 tick（228.38 s，3:48）');
rep('music/defect_robot.ogg，3121 tick（156.04 s，2:36）', 'music/defect_robot.ogg，3696 tick（184.80 s，3:05）');

/* ③ 补上缺的两行（排在 compost_p2 那行之后） */
{
    const anchor = '| ftboss:music_disc_compost_p2 |';
    const at = t.indexOf(anchor);
    if (at < 0) miss.push('compost_p2 行(插入锚点)');
    else {
        const lineEnd = t.indexOf('\n', at);
        const add = '\n'
            + '| ftboss:music_disc_manor_guignol | 庄园唱片 · 鏖战 | music/manor_guignol.ogg，4173 tick（208.67 s，3:29），EPIC |\n'
            + '| ftboss:music_disc_benger | 边牧犬王唱片 | music/benger.ogg，3708 tick（185.41 s，3:05），EPIC |';
        t = t.slice(0, lineEnd) + add + t.slice(lineEnd);
    }
}

/* ④ 张数：13 → 15，庄园 3 → 4 张，补边牧犬王 */
rep('- 13 张全部写在', '- 15 张全部写在');
rep('**13 个唱片专用事件**', '**15 个唱片专用事件**');
rep('庄园 3 张（`music_disc_manor_cue` / `manor_battle` / `manor_dual`）',
    '庄园 4 张（`music_disc_manor_cue` / `manor_battle` / `manor_dual` / `manor_guignol`）');
rep('，它们与同名 BGM **共用音频文件**', '+ 边牧犬王 1 张（`music_disc_benger`），它们与同名 BGM **共用音频文件**');

fs.writeFileSync(p, t, 'utf8');
console.log(miss.length ? '没匹配: ' + miss.join(' | ') : '✓ 全部替换成功');
console.log('唱片行数: ' + (t.match(/\| ftboss:music_disc_/g) || []).length + ' （应为 15）');
