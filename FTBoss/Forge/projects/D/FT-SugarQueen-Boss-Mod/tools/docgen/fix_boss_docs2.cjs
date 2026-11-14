/* 文档收尾第二刀（用户 2026-09-14 "继续"）:
 *  ① _README.txt 索引加 18_benger.md
 *  ② 40_summons_weapons_loot.md：唱片名改成 15 张新名 + 补 benger 唱片/牧野犬王、两首重做曲的新时长
 *  ③ 32_manor_fight.md：音乐流程改成 v126 现状（演出=《双轨坦克曲》全曲、战斗=《鏖战》+《双轨·战斗高潮》轮流）
 *  ④ 00_global.md：BGM 调度补"开演/换曲前清场(防双播)"与落幕钟声进名单
 * 用法: node fix_boss_docs2.cjs
 */
const fs = require('fs');
const D = 'D:/ft工程文件/FT-SugarQueen-Boss-Mod/docs/boss_reference/';
const log = [];

/* ① 索引 */
{
    const p = D + '_README.txt';
    let t = fs.readFileSync(p, 'utf8');
    if (t.includes('18_benger.md')) log.push('  — 索引已含 18_benger.md');
    else {
        const anchor = '30_manor_shared.md';
        const at = t.indexOf(anchor);
        const lineStart = t.lastIndexOf('\n', at) + 1;
        t = t.slice(0, lineStart) + '    18_benger.md                 边牧犬王（第 9 位主 Boss）\n' + t.slice(lineStart);
        fs.writeFileSync(p, t, 'utf8');
        log.push('  ✓ 索引已加 18_benger.md');
    }
}

/* 通用: 逐条精确字符串替换, 命中不到就报出来 */
function patch(file, pairs) {
    const p = D + file;
    let t = fs.readFileSync(p, 'utf8');
    let ok = 0, miss = [];
    for (const [from, to] of pairs) {
        if (!t.includes(from)) { miss.push(from.slice(0, 46)); continue; }
        const c = t.split(from).length - 1;
        t = t.split(from).join(to);
        ok += c;
    }
    fs.writeFileSync(p, t, 'utf8');
    log.push(`  ${miss.length ? '△' : '✓'} ${file}: 改 ${ok} 处${miss.length ? '，没匹配: ' + miss.join(' | ') : ''}`);
}

/* ② 唱片与音乐物品 */
patch('40_summons_weapons_loot.md', [
    ['音乐唱片 · ', ''],
    ['（战斗曲）', ''], ['（第二首战斗曲）', ''], ['（演出曲）', ''],
    ['（坦克版＋真吉他版叠放）', ''],
    ['（苹果爵士·蜜脆）', ''], ['（苹果爵士二阶段）', ''], ['（糖霜女皇）', ''],
    ['（屠夫）', ''], ['（沙耶）', ''], ['（酒魔·酒桶王）', ''], ['（鼠群）', ''],
    ['（炫彩答辩超人）', ''], ['（炫彩答辩超人二阶段）', ''],
    ['（边牧犬王战斗曲）', ''],
    ['果园教父', '苹果爵士唱片'], ['木偶剧场·加洛普', '糖霜女皇唱片'], ['肉山祭坛', '屠夫唱片'],
    ['茶の唄', '沙耶唱片'], ['醉乡酒馆', '酒桶王唱片'], ['鼠潮夜行', '鼠群唱片'],
    ['堆肥·强袭', '炫彩答辩超人唱片'], ['堆肥·熔毁', '炫彩答辩超人唱片 · 二阶段'],
]);

/* ③ 庄园音乐现状 */
patch('32_manor_fight.md', [
    ['起播**演出曲《庄园·序》**（曲长 **45.8 秒** = 916 tick，`manor_cue.ogg`；v112 用 `tools/audio_tools/ogg_dur.cjs` 实测重标）',
     '起播**演出曲《双轨坦克曲》全曲**（`MANOR_DUAL_TICKS = 5130` 刻 = 256.5 秒，`manor_dual.ogg`）；正式开打后战斗阶段在 **《庄园·鏖战》**（`GUIGNOL_TICKS = 4173`）与 **《双轨·战斗高潮》**（`DUAL_BATTLE_TICKS = 2960`）之间轮流，每首放完换另一首'],
    ['曲目组：庄园用 `Group.MANOR_EPIC`', '曲目组：庄园用 `Group.MANOR_EPIC`（演出/战斗由 `manorPhase` 区分，战斗曲号记在 `manorTrack`）'],
]);

/* ④ 全局 BGM 调度 */
patch('00_global.md', [
    ['`boss_music_manor_*` 由服务端全程托管。',
     '`boss_music_manor_*` 由服务端全程托管。**开演与换曲前先 `stopAllBgmNearby()` 把附近全部 BGM 事件停干净**（实体自己播的二阶段曲 `compost_p2`/`apple_p2`、落幕钟声 `boss_music_curtain` 也在这个名单里）—— 不清场会出现"两首同时播"。'],
]);

console.log(log.join('\n'));
