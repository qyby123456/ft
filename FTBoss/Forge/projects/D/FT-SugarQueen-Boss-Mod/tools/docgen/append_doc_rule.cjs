/* 把"文档只写最终值"这条规矩写进文档自身的 README 与 AGENTS.md（用户 2026-09-14：
 * "以后写文档只写修正后数值而不用写之前的各种修改修正"）。
 * 用 Node 追加/替换，避免 PowerShell 改中文文件的老坑。用法: node append_doc_rule.cjs
 */
const fs = require('fs');
const P = 'D:/ft工程文件/FT-SugarQueen-Boss-Mod/';
const RULE = [
    '',
    '★ 写入规则（用户 2026-09-14 定）：**只写"当前生效的数值/机制"，不要写历史修正**。',
    '  即：不要出现"v118 改成 12（原 6×4/3=8）""原来 4.5，现在 8.0"这类对照 ——',
    '  数值一律以**代码里当前的值**为准（改完代码重跑 check_numbers.cjs，它会把不一致点出来）。',
    '  版本沿革只记在 docs/CHANGELOG.md，那份才是变更历史。',
    '',
].join('\n');

/* ① docs/boss_reference/_README.txt：插在"数值维护约定："之前 */
{
    const p = P + 'docs/boss_reference/_README.txt';
    let t = fs.readFileSync(p, 'utf8');
    if (t.includes('只写"当前生效的数值/机制"')) console.log('  — _README.txt 已有该规则');
    else {
        const anchor = '数值维护约定：';
        if (t.includes(anchor)) { t = t.replace(anchor, RULE.trim() + '\n\n' + anchor); fs.writeFileSync(p, t, 'utf8'); console.log('  ✓ _README.txt 已插入规则'); }
        else { fs.writeFileSync(p, t + RULE, 'utf8'); console.log('  ✓ _README.txt 追加到末尾'); }
    }
}

/* ② AGENTS.md：挂在"文档流水线"那一行后面 */
{
    const p = P + 'AGENTS.md';
    let t = fs.readFileSync(p, 'utf8');
    if (t.includes('只写"当前生效的数值/机制"')) console.log('  — AGENTS.md 已有该规则');
    else {
        const anchor = 'make_boss_doc.cjs';
        const at = t.indexOf(anchor);
        if (at < 0) { t += '\n' + RULE; console.log('  ! AGENTS.md 没找到锚点, 追加到末尾'); }
        else {
            const lineEnd = t.indexOf('\n', at);
            t = t.slice(0, lineEnd + 1) + RULE.replace(/^\n/, '') + t.slice(lineEnd + 1);
            console.log('  ✓ AGENTS.md 已插入规则');
        }
        fs.writeFileSync(p, t, 'utf8');
    }
}
