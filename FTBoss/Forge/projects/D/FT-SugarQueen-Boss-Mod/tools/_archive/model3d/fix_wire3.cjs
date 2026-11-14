/* 修 wire_compost.cjs 的两个真问题(纯 ASCII 匹配):
 *  ① rep() 的"已存在"判断用了替换文本的第一行, 而那一行往往就是锚点本身 -> 误判跳过
 *     改成用**整段替换文本**判断
 *  ② 之前删行留下了半条 Java 语句(带 \u3010 转义的行) -> 删掉它
 */
const fs = require('fs');
const path = require('path');
const p = path.join(process.argv[2], 'tools/model3d/wire_compost.cjs');
let s = fs.readFileSync(p, 'utf8');
const before = s.length;
/* ① 修判断 */
const oldCheck = "if (s.includes(b.trim().split('\\n')[0])) { console.log('  skip ' + label); return true; }";
const newCheck = "if (s.includes(b)) { console.log('  skip ' + label); return true; }";
if (s.includes(oldCheck)) { s = s.replace(oldCheck, newCheck); console.log('  ① rep 判断: 已修(改用整段文本)'); }
else console.log('  ① rep 判断: 未命中(可能已是整段判断)');
/* ② 删掉残留的半条 Java 语句(含 \\u3010 转义的那一行) */
const lines = s.split('\n');
const kept = lines.filter(l => !(l.includes('u3010') && l.trim().startsWith('"')));
fs.writeFileSync(p, kept.join('\n'));
console.log(`  ② 删除残留半条语句: ${lines.length - kept.length} 行`);
console.log(`  文件: ${before} -> ${fs.statSync(p).size} 字节`);
try { new (require('vm').Script)(fs.readFileSync(p, 'utf8')); console.log('  语法检查: 通过 OK'); }
catch (e) { console.log('  语法检查: 失败 -> ' + e.message); }
