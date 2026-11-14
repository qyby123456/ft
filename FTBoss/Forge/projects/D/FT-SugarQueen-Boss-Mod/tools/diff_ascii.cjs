/* 对比"备份原文"与"被损坏文件"的**代码骨架**(去掉所有非 ASCII)差异(2026-09-12) */
const fs = require('fs');
const [aPath, bPath] = process.argv.slice(2);

const ascii = (s) => s.replace(/[^\x00-\x7F]/g, '@');
const strip = (s) => s.replace(/[^\x00-\x7F]/g, '').replace(/\s+/g, ' ').trim();

const A = fs.readFileSync(aPath, 'utf8').split('\n').map(strip);
const B = fs.readFileSync(bPath, 'utf8').split('\n').map(strip);

/* 简易 LCS 行级 diff */
const n = A.length, m = B.length;
const dp = Array.from({ length: n + 1 }, () => new Int32Array(m + 1));
for (let i = n - 1; i >= 0; i--) {
    for (let j = m - 1; j >= 0; j--) {
        dp[i][j] = A[i] === B[j] ? dp[i + 1][j + 1] + 1 : Math.max(dp[i + 1][j], dp[i][j + 1]);
    }
}
const ops = [];
let i = 0, j = 0;
while (i < n && j < m) {
    if (A[i] === B[j]) { ops.push(['=', i, j]); i++; j++; }
    else if (dp[i + 1][j] >= dp[i][j + 1]) { ops.push(['-', i, j]); i++; }
    else { ops.push(['+', i, j]); j++; }
}
while (i < n) ops.push(['-', i++, j]);
while (j < m) ops.push(['+', i, j++]);

const onlyA = ops.filter((o) => o[0] === '-');
const onlyB = ops.filter((o) => o[0] === '+');
console.log(`备份行数 ${n}  被损坏文件行数 ${m}`);
console.log(`仅备份有的行: ${onlyA.length}    仅损坏文件有的行: ${onlyB.length}`);
console.log('--- 仅损坏文件有的行(即 09-11 之后新增/改动的代码) ---');
for (const [, , jj] of onlyB.slice(0, 40)) console.log(`  +${jj + 1}: ${B[jj].slice(0, 140)}`);
console.log('--- 仅备份有的行(前 20) ---');
for (const [, ii] of onlyA.slice(0, 20)) console.log(`  -${ii + 1}: ${A[ii].slice(0, 140)}`);
