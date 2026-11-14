/* 精准修 wire_compost.cjs: 删除残留的悬空 rep( 两行(上面那行 + 含"女王幻影"的实参行) */
const fs = require('fs');
const path = require('path');
const p = path.join(process.argv[2], 'tools/model3d/wire_compost.cjs');
const lines = fs.readFileSync(p, 'utf8').split('\n');
const out = [];
let removed = 0;
for (let i = 0; i < lines.length; i++) {
    if (lines[i].includes('女王幻影')) {
        // 丢掉本行与上一行(如果上一行是 rep( 开头)
        if (out.length && out[out.length - 1].trim().startsWith('rep(')) { out.pop(); removed += 2; }
        else { removed += 1; }
        continue;
    }
    out.push(lines[i]);
}
fs.writeFileSync(p, out.join('\n'));
console.log(`删除悬空行: ${removed} 行 (${lines.length} -> ${out.length})`);
/* 语法自检: 用 node --check 等价的编译检查 */
try {
    new (require('vm').Script)(fs.readFileSync(p, 'utf8'));
    console.log('语法检查: 通过 OK');
} catch (e) {
    console.log('语法检查: 失败 -> ' + e.message);
}
