/* 修 wire_compost.cjs 里两处问题(纯 ASCII 匹配, 不碰中文):
 *   ① 删除引用不存在的 BossMercy.broadcast 的那两行
 *   ② 删除多余的 'probe' 探测调用
 */
const fs = require('fs');
const path = require('path');
const p = path.join(process.argv[2], 'tools/model3d/wire_compost.cjs');
let lines = fs.readFileSync(p, 'utf8').split('\n');
const before = lines.length;
lines = lines.filter(l => !l.includes('BossMercy.broadcast(') && !l.includes("'probe'"));
fs.writeFileSync(p, lines.join('\n'));
console.log(`wire_compost.cjs: ${before} -> ${lines.length} 行 (删除 ${before - lines.length} 行)`);
/* 再确认那两行确实没了 */
const s = fs.readFileSync(p, 'utf8');
console.log('BossMercy.broadcast 残留: ' + (s.includes('BossMercy.broadcast') ? '有 ⚠' : '无 OK'));
console.log('probe 残留: ' + (s.includes("'probe'") ? '有 ⚠' : '无 OK'));
