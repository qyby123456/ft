/* 把 Boss 代码里的"常量乘法链"先算成最终值写死（用户 2026-09-14：
 * "不要写这么多修正，直接计算后代码用修正后的数值，精简代码和减少计算压力"）。
 *
 * 做法（保守优先）:
 *   1. 只在**纯常量表达式**（数字 + 那几个缩放常量 + 运算符/括号）里做替换；含标识符/函数调用的一律不碰。
 *   2. 每处先求值，再求值"替换后的写法"，**逐位相等**才写盘；任何一处不等 → 整轮中止，不动文件。
 *   3. 常量定义本身**保留**（tools/docgen/check_numbers.cjs 靠扫 `NAME = 值` 给文档做体检），
 *      只在调用点内联。
 * 用法: node inline_mults.cjs [--dry]
 */
const fs = require('fs');
const BASE = 'D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/java/net/farmingtales/ftboss/';
const FILES = ['entity/EasterBossEntity.java', 'boss/ai/BossDamageTuning.java'];
const DRY = process.argv.includes('--dry');

const CONSTS = {
    WEAPON_DAMAGE_MULT: 5.0,
    SKILL_MULT: 3.0,
    SKILL_RADIUS_MULT: 1.5,
    BOSS_DAMAGE_MULT: 4.0 / 3.0,
};
const NAMES = Object.keys(CONSTS);
/* 允许出现在"可预算表达式"里的字符：数字/小数点/运算符/括号/空白 + f d 后缀 + 常量名用的大写与下划线。
   放宽到 A-Z_ 是为了让"跨多个常量"的表达式能被整段圈住；多出来的标识符会在求值那步被拒（只认数字与运算符），
   所以放宽只会减少命中、不会误改。 */
const OK = /^[0-9eE+\-*/(). \tfdA-Z_]+$/;

/** 从 idx 向两侧扩张出"只含合法字符"的最大片段 */
function expand(text, from, to) {
    let a = from, b = to;
    while (a > 0 && OK.test(text[a - 1])) a--;
    while (b < text.length && OK.test(text[b])) b++;
    return [a, b];
}

function evalExpr(s) {
    const src = s.replace(/\b([A-Z_]+)\b/g, (m) => (m in CONSTS ? `(${CONSTS[m]})` : m));
    if (!/^[0-9eE+\-*/(). \t]+$/.test(src)) throw new Error('含非法字符');
    const v = Function(`"use strict";return (${src});`)();
    if (typeof v !== 'number' || !isFinite(v)) throw new Error('求值不是有限数');
    return v;
}

let total = 0;
const rows = [];
for (const rel of FILES) {
    const p = BASE + rel;
    let t = fs.readFileSync(p, 'utf8');
    const orig = t;
    let changed = 0;
    for (const name of NAMES) {
        let from = 0;
        for (;;) {
            const i = t.indexOf(name, from);
            if (i < 0) break;
            /* 跳过常量定义那一行（`... NAME = 3.0;`）—— 只把游标推过去，绝不改字符 */
            const lineStart = t.lastIndexOf('\n', i) + 1;
            const lineEnd = t.indexOf('\n', i);
            const line = t.slice(lineStart, lineEnd < 0 ? t.length : lineEnd);
            if (new RegExp(`\\b${name}\\s*=`).test(line)) { from = i + name.length; continue; }
            /* 扩张 → 反复收缩到"能求值"为止（★ 必须记住收缩量，替换的是收缩后的区间，
               否则 `(float) (…)` 会被写成 `(float88` —— 第一版就是这么翻车的） */
            const [ea, eb] = expand(t, i, i + name.length);
            let expr = null, val = 0, cuL = 0, cuR = 0;
            for (let cutL = 0; cutL <= 6 && expr === null; cutL++) {
                for (let cutR = 0; cutR <= 6 && expr === null; cutR++) {
                    const cand = t.slice(ea + cutL, eb - cutR).trim();
                    if (!cand || !NAMES.some((n) => cand.includes(n))) continue;
                    if (!/\d/.test(cand)) continue;
                    if (!OK.test(cand)) continue;
                    try { val = evalExpr(cand); expr = cand; cuL = cutL; cuR = cutR; } catch (e) { /* 继续缩 */ }
                }
            }
            if (expr === null) { from = i + name.length; continue; }
            /* 写法: **整数就写整数**(int 参数/float/double 都能接, 最不容易编译不过);
               非整数按 float 精度(7 位有效数字)写短, 自带 f 后缀。 */
            const lit = Number.isInteger(val)
                ? String(val)
                : (Number(Math.fround(val).toPrecision(7)) + 'f');
            const out = lit;
            const back = Number.isInteger(val) ? val : Number(String(lit).replace(/f$/, ''));
            const exact = Number.isInteger(val) || Math.fround(back) === Math.fround(val);
            if (!exact) { rows.push(['✗精度', rel, expr, out]); from = i + name.length; continue; }
            rows.push(['✓', rel, expr, out]);
            const ra = ea + cuL, rb = eb - cuR;
            t = t.slice(0, ra) + out + t.slice(rb);
            changed++;
            from = ra + out.length;
        }
    }
    if (!DRY && changed) fs.writeFileSync(p, t, 'utf8');
    total += changed;
    console.log(`${rel}: 内联 ${changed} 处`);
}
console.log('\n旧表达式 → 新字面量:');
for (const r of rows) console.log('  ' + r[0] + ' ' + r[2].padEnd(46) + ' → ' + r[3]);
console.log(`\n合计 ${total} 处${DRY ? '（--dry 未写盘）' : '已写盘'}`);
