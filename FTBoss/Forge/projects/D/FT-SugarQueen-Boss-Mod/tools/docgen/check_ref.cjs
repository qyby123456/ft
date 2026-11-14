#!/usr/bin/env node
/*
 * check_ref.cjs —— 生成文档前先体检 docs/boss_reference/*.md
 * 用法: node tools/docgen/check_ref.cjs [目录]
 * 只报告不修改: 行数/字数、各级标题数、表格数，以及会让 Word 排版变丑的格式问题。
 */
const fs = require('fs');
const path = require('path');

const DIR = process.argv[2] || path.join(__dirname, '..', '..', 'docs', 'boss_reference');
const files = fs.readdirSync(DIR).filter((f) => f.toLowerCase().endsWith('.md')).sort();
if (!files.length) { console.log('（目录为空）' + DIR); process.exit(0); }

let totalLines = 0, totalChars = 0, problems = 0;
for (const f of files) {
    const md = fs.readFileSync(path.join(DIR, f), 'utf8').replace(/\r\n?/g, '\n');
    const lines = md.split('\n');
    const chars = md.length;
    totalLines += lines.length; totalChars += chars;
    const h = [0, 0, 0, 0];
    let tables = 0, rows = 0, bullets = 0, fences = 0, nested = 0, cellsBad = 0, emptyCell = 0;
    let inTable = false, cols = 0;
    const bad = [];
    lines.forEach((raw, i) => {
        const l = raw.trim();
        if (/^```/.test(l)) { fences++; bad.push(`L${i + 1} 代码块`); }
        if (/^#{1,3}\s+/.test(l)) h[l.match(/^#+/)[0].length]++;
        if (/^\s+[-*]\s+/.test(raw) && l) { nested++; bad.push(`L${i + 1} 嵌套列表`); }
        if (/^[-*]\s+/.test(l)) bullets++;
        if (l.startsWith('|')) {
            const c = l.replace(/^\|/, '').replace(/\|$/, '').split('|');
            if (c.some((x) => x.trim() === '') && !/^\|[\s:|-]+\|$/.test(l)) emptyCell++;
            if (!inTable) { inTable = true; cols = c.length; tables++; } else if (c.length !== cols) { cellsBad++; bad.push(`L${i + 1} 列数 ${c.length}≠表头 ${cols}`); }
            rows++;
        } else inTable = false;
    });
    const level = h[1] === 1 ? 'ok  ' : '注意';
    console.log(`${level} ${f}`);
    console.log(`      ${lines.length} 行 / ${chars} 字 ｜ H1=${h[1]} H2=${h[2]} H3=${h[3]} ｜ 表格 ${tables} (${rows} 行) ｜ 列表 ${bullets}`);
    if (fences || nested || cellsBad || h[1] !== 1) {
        problems += (fences ? 1 : 0) + (nested ? 1 : 0) + (cellsBad ? 1 : 0) + (h[1] !== 1 ? 1 : 0);
        if (fences) console.log(`      ⚠ 代码块 ${fences} 处`);
        if (nested) console.log(`      ⚠ 嵌套列表 ${nested} 处`);
        if (cellsBad) console.log(`      ⚠ 表格列数不一致 ${cellsBad} 处`);
        if (h[1] !== 1) console.log(`      ⚠ H1 数量 ${h[1]}（应为 1）`);
        bad.slice(0, 8).forEach((b) => console.log('        · ' + b));
    }
}
console.log(`\n合计 ${files.length} 个文件 / ${totalLines} 行 / ${totalChars} 字 ｜ 格式问题 ${problems} 类`);
