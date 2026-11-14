/* 修根因: 头部层数组只写了 5 个值, 代码却读 L[5](undefined) -> NaN -> JSON null -> GeckoLib JsonNull 崩溃
 *  改: 偏移用 L[4], 旋转用 L[3]
 *  然后重生成两个变体 + 同步到资源 + 校验 JSON 里没有 null
 */
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const ROOT = process.argv[2];
const gen = path.join(ROOT, 'tools/model3d/gen_compost_boss_v4.cjs');
let s = fs.readFileSync(gen, 'utf8');
let n = 0;
const fixes = [
    ["cube('head', [-L[0] / 2 + L[5], hy, -L[2] / 2], [L[0], L[1], L[2]], solid, [0, L[4], 0]);",
     "cube('head', [-L[0] / 2 + L[4], hy, -L[2] / 2], [L[0], L[1], L[2]], solid, [0, L[3], 0]);"],
    ["cube('head', [-L[0] / 2 + L[5] - 0.2, hy + L[1] - 0.6, -L[2] / 2 - 0.15], [L[0] + 0.4, 0.8, 0.5], deep);",
     "cube('head', [-L[0] / 2 + L[4] - 0.2, hy + L[1] - 0.6, -L[2] / 2 - 0.15], [L[0] + 0.4, 0.8, 0.5], deep);"]
];
for (const [a, b] of fixes) { if (s.includes(a)) { s = s.replace(a, b); n++; } else console.log('  未命中: ' + a.slice(0, 50)); }
fs.writeFileSync(gen, s);
console.log(`生成器修复: ${n}/2 处 (L[5]->L[4], L[4]->L[3])`);

/* 重生成两个变体 */
for (const mode of ['brown', 'rainbow']) {
    const r = spawnSync('node', ['gen_compost_boss_v4.cjs', 'out_compost_boss', mode], { cwd: path.join(ROOT, 'tools/model3d'), stdio: 'inherit' });
    console.log(`  ${mode}: 退出码 ${r.status}`);
}
/* 同步几何到资源(贴图不变) */
const A = path.join(ROOT, 'src/main/resources/assets/ftboss');
fs.copyFileSync(path.join(ROOT, 'tools/model3d/out_compost_boss/compost_superman.geo.json'), path.join(A, 'geo/compost_superman.geo.json'));
fs.copyFileSync(path.join(ROOT, 'tools/model3d/out_compost_boss/compost_superman.geo.json'), path.join(A, 'geo/compost_superman_p2.geo.json'));
console.log('  已同步两个 geo(内容相同, 几何一致)');
/* 校验: 递归找 null / NaN */
function scan(o, pathStr, bad) {
    if (o === null) { bad.push(pathStr + ' = null'); return; }
    if (typeof o === 'number' && !isFinite(o)) { bad.push(pathStr + ' = ' + o); return; }
    if (Array.isArray(o)) o.forEach((v, i) => scan(v, pathStr + '[' + i + ']', bad));
    else if (typeof o === 'object') for (const k of Object.keys(o)) scan(o[k], pathStr + '.' + k, bad);
}
for (const f of ['geo/compost_superman.geo.json', 'geo/compost_superman_p2.geo.json']) {
    const j = JSON.parse(fs.readFileSync(path.join(A, f), 'utf8'));
    const bad = [];
    scan(j, '', bad);
    console.log(`  ${f}: ${bad.length === 0 ? '✅ 无 null/NaN' : '⚠ ' + bad.length + ' 处异常 -> ' + bad.slice(0, 5).join(', ')}`);
}
