/* 修 boss 文档里体检器抓到的数值不一致 + 按新规则把雪黎那招改成"只写最终值"。
 * 只用短锚点(ASCII 常量名/数字为主), 避免长中文串匹配失败。
 * 用法: node fix_boss_docs.cjs
 */
const fs = require('fs');
const D = 'D:/ft工程文件/FT-SugarQueen-Boss-Mod/docs/boss_reference/';
const log = [];

function patch(file, pairs) {
    const p = D + file;
    let t = fs.readFileSync(p, 'utf8');
    let n = 0;
    for (const [from, to] of pairs) {
        if (!t.includes(from)) { log.push(`  ! ${file}: 没匹配 "${from.slice(0, 40)}"`); continue; }
        const c = t.split(from).length - 1;
        t = t.split(from).join(to);
        n += c;
    }
    fs.writeFileSync(p, t, 'utf8');
    log.push(`  ✓ ${file}: 改 ${n} 处`);
}

/* ① 数值不一致（体检器点名的 7 处，含 MINI_CAP 两处） */
patch('30_manor_shared.md', [['MINI_CAP=4', 'MINI_CAP=6']]);
patch('31_manor_variants.md', [
    ['XUANLAN_BEAM_DMG', 'XUANLAN_BEAM_DMG'],          // 占位, 真正改在下面按行处理
]);

/* 按"常量名所在行"改数字：只替换常量名**之后**的第一个旧值 */
function patchLine(file, constName, oldVal, newVal) {
    const p = D + file;
    const lines = fs.readFileSync(p, 'utf8').split('\n');
    let n = 0;
    for (let i = 0; i < lines.length; i++) {
        const at = lines[i].indexOf(constName);
        if (at < 0) continue;
        const head = lines[i].slice(0, at + constName.length);
        const tail = lines[i].slice(at + constName.length);
        const re = new RegExp(`\\b${oldVal.replace('.', '\\.')}\\b`);
        if (re.test(tail)) { lines[i] = head + tail.replace(re, newVal); n++; }
    }
    fs.writeFileSync(p, lines.join('\n'), 'utf8');
    log.push(`  ${n ? '✓' : '!'} ${file}: ${constName} ${oldVal} → ${newVal} (${n} 处)`);
}
patchLine('31_manor_variants.md', 'XUANLAN_BEAM_DMG', '72', '360');
patchLine('31_manor_variants.md', 'SHADOW_STREAK_HITS', '50', '25');

/* ② 雪黎⑦ 改成五连砸的最终数值（只写当前值） */
patch('31_manor_variants.md', [
    ['`XUELI_SLAM_WARN_TICKS = 60` tick（3 秒）', '`XUELI_SLAM_WARN_TICKS = 20` tick（每发 1 秒）'],
    ['`XUELI_SLAM_RADIUS = 4.5` 格', '`XUELI_SLAM_RADIUS = 8.0` 格'],
    ['`XUELI_SLAM_STUN_TICKS = 60` tick（3 秒）', '`XUELI_SLAM_STUN_TICKS = 20` tick（1 秒 = 两发间隔）'],
    ['要躲就得在 3 秒内跑出雪圈', '要躲就得在 1 秒内跑出雪圈'],
    ['每秒一声音调递增的提示音', '音调递增的提示音'],
    ['并向外不击退（只用眩晕定身）。',
     '并向外不击退（只用眩晕定身）。一次施放 = **5 连砸**（`XUELI_SLAM_COMBO = 5`）：'
     + '每发蓄势 1 秒 → 砸落 → 落地停 `XUELI_SLAM_GAP_TICKS = 20` tick（1 秒）→ 再窜上去砸下一发；'
     + '整段连砸期间不接别的招；进坑前 2 拍收掉混响送、0.1 拍把长音通道表情归零（掉坑那套手法）。'],
]);
console.log(log.join('\n'));
