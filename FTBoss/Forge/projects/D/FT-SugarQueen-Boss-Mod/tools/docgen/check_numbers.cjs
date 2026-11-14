#!/usr/bin/env node
/*
 * check_numbers.cjs —— 核对 docs/boss_reference/*.md 里写的常量数值跟代码真值是否一致。
 *
 * 为什么需要: 那份 Word 的约定是"只写代码里真实存在的数字"，但改代码时极容易忘记同步文档 →
 *   v112 抓到 `XUANLAN_BEAM_DMG` 文档 24 / 代码 72、庄园三张唱片长度过期；
 *   v113 又抓到 `T_BEAM` 245 / 代码 285、`T_PHASE` 32 / 代码 52。
 *
 * 查两种写法:
 *   ① `NAME = 123`                       正文
 *   ② `- Xxx.java: NAME 123 / NAME2 4.5`  "关键常量"汇总行(整行必须带 `.java:`, 免得把散文数字当常量)
 *
 * 判定(宁可漏报也不误报) —— 下列情况一律**不算错**:
 *   · 名字前面(跳过空格)紧跟 × * + - ÷ → 派生算式(如 `6 × 20 × SKILL_MULT = 360`);
 *   · 数字后面紧跟 `/` → 分数(如 `BOSS_DAMAGE_MULT = 4/3` 里的 4);
 *   · 代码值 = 文档值 × 20(或 ×1200) → 文档写的是**秒**, 代码是 tick, 单位不同不算错;
 *   · 同名常量在多个类里都有(MAX_LIFE / RADIUS / BUFF_TICKS 很常见) → 任一命中即算一致;
 *   · 代码里是符号式(10.0 * 3.0 * BOSS_DAMAGE_MULT) → 跳过, 不做文本比较。
 * 用法: node tools/docgen/check_numbers.cjs [源目录] [java 根目录]      SHOW_NOTES=1 打印提示项
 * 退出码: 有不一致时 1。
 */
const fs = require('fs');
const path = require('path');

const REF_DIR = process.argv[2] || path.join(__dirname, '..', '..', 'docs', 'boss_reference');
const JAVA_DIR = process.argv[3] || path.join(__dirname, '..', '..', 'src', 'main', 'java');

/* ---------- 1. 代码常量索引(支持一行多个声明) ---------- */
const decls = new Map();
let allJava = '';
(function walk(dir) {
    for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
        const p = path.join(dir, e.name);
        if (e.isDirectory()) { walk(p); continue; }
        if (!e.name.endsWith('.java')) continue;
        const txt = fs.readFileSync(p, 'utf8');
        allJava += '\n' + txt;
        const declRe = /\b(?:static\s+final|final\s+static)\s+[\w.<>\[\]]+\s+([^;]+);/g;
        let d;
        while ((d = declRe.exec(txt)) !== null) {
            // 一行可能写多个: `double A = 20.0, B = 52.0;`
            for (const part of d[1].split(',')) {
                const m = /^\s*([A-Z][A-Z0-9_]{2,})\s*=\s*(.+)$/.exec(part);
                if (!m) continue;
                const n = m[1];
                if (!decls.has(n)) decls.set(n, []);
                decls.get(n).push({ expr: m[2].trim(), file: path.relative(JAVA_DIR, p) });
            }
        }
    }
})(JAVA_DIR);

/** 纯数字字面量/纯算式 → 数值; 含标识符 → null */
function valueOf(str) {
    const s = String(str).replace(/[_\s]/g, '').replace(/[fFdDlL]$/, '');
    if (!/^-?[\d.+\-*/()]+$/.test(s)) return null;
    try {
        const v = Function('"use strict";return (' + s + ')')();
        return Number.isFinite(v) ? v : null;
    } catch { return null; }
}
const same = (a, b) => Math.abs(a - b) < 1e-6;

/* ---------- 2. 扫文档 ---------- */
const files = fs.readdirSync(REF_DIR).filter((f) => f.toLowerCase().endsWith('.md')).sort();
let hits = 0, ok = 0, skipped = 0, skipDerived = 0, mismatch = 0, notConst = 0, noName = 0;
const mismatches = [], notes = [];

function judge(file, line, name, docNum, where, matchIndex, matchEnd) {
    hits++;
    const before = line.slice(0, matchIndex).replace(/\s+$/, '');
    const after = line.slice(matchEnd === undefined ? matchIndex + String(name).length : matchEnd);
    if (/[×*+\-/÷]$/.test(before)) { skipDerived++; return; }          // 派生算式: 6 × 20 × SKILL_MULT
    if (/^\s*[×÷*]/.test(after)) { skipDerived++; return; }            // 连乘: 20×30
    if (/^\s*\/\s*[\d.]/.test(after)) { skipDerived++; return; }       // 分数: 4/3（` / ` 是汇总行分隔符, 不算）
    const cand = decls.get(name);
    if (!cand) {
        if (new RegExp('\\b' + name + '\\b').test(allJava)) {
            notConst++;
            notes.push(`${file}${where}: ${name} = ${docNum} —— 代码里不是 static final 常量(可能是 Attributes 参数/局部量)`);
        } else {
            noName++;
            notes.push(`${file}${where}: ${name} = ${docNum} —— 代码里搜不到这个名字`);
        }
        return;
    }
    const vals = cand.map((c) => valueOf(c.expr)).filter((v) => v !== null);
    if (vals.length === 0) { skipped++; return; }
    // 一致: 数值相同, 或"文档写秒、代码写 tick"(×20 / ×60×20)
    if (vals.some((v) => same(v, docNum) || same(v, docNum * 20) || same(v, docNum * 60 * 20))) { ok++; return; }
    mismatch++;
    mismatches.push(`${file}${where}: ${name} 文档写 ${docNum}, 代码是 ` +
        cand.filter((c) => valueOf(c.expr) !== null).map((c) => `${c.expr} (${c.file})`).join(' 或 '));
}

for (const f of files) {
    const lines = fs.readFileSync(path.join(REF_DIR, f), 'utf8').replace(/\r\n?/g, '\n').split('\n');
    lines.forEach((line, i) => {
        const where = `:L${i + 1}`;
        let re = /([A-Z][A-Z0-9_]{2,})\s*=\s*(-?\d+(?:\.\d+)?)(?![\d.])/g, m;
        while ((m = re.exec(line)) !== null) judge(f, line, m[1], parseFloat(m[2]), where, m.index, m.index + m[0].length);
        if (/\.java:/.test(line)) {                                   // 关键常量汇总行
            re = /([A-Z][A-Z0-9_]{2,})\s+(-?\d+(?:\.\d+)?)(?![\d.])/g;
            while ((m = re.exec(line)) !== null) judge(f, line, m[1], parseFloat(m[2]), where + '(汇总行)', m.index, m.index + m[0].length);
        }
    });
}

/* ---------- 3. 报告 ---------- */
console.log(`扫描 ${files.length} 份 md ｜ 命中 ${hits} 处（派生/分数跳过 ${skipDerived}）`);
console.log(`  一致 ${ok} ｜ **不一致 ${mismatch}** ｜ 符号式跳过 ${skipped} ｜ 非常量 ${notConst} ｜ 查无此名 ${noName}`);
if (mismatches.length) {
    console.log('\n★ 数值不一致（必须改文档或改代码）:');
    for (const p of mismatches) console.log('  · ' + p);
}
if (notes.length && process.env.SHOW_NOTES === '1') {
    console.log('\n仅提示（人工确认）:');
    for (const p of notes) console.log('  · ' + p);
}
process.exit(mismatch > 0 ? 1 : 0);
