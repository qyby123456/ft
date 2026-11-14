#!/usr/bin/env node
// 只读校验: 直接扫**原始文本字节**, 判断动画关键帧时间键是否按数值升序。
// 不做任何 JSON.parse -> stringify 往返 (那会因 JS 对象键序规则而失真正, 见 anim_key_order.cjs 的说明)。
//
// 用法: node anim_key_order_scan.cjs <animationsDir|单个文件...>
const fs = require('fs');
const path = require('path');

const args = process.argv.slice(2);
if (!args.length) { console.error('用法: node anim_key_order_scan.cjs <animationsDir|file...>'); process.exit(2); }
let files = [];
for (const a of args) {
  const st = fs.statSync(a);
  if (st.isDirectory()) files.push(...fs.readdirSync(a).filter(f => f.endsWith('.animation.json')).sort().map(f => path.join(a, f)));
  else files.push(a);
}

let tracks = 0, bad = 0, byFile = {};
for (const f of files) {
  const txt = fs.readFileSync(f, 'utf8');
  const name = path.basename(f);
  byFile[name] = 0;
  // 匹配  "rotation"|"position"|"scale": { ... }，取到该对象内紧邻行首的 "键":
  const re = /"(rotation|position|scale)":\s*\{([\s\S]*?)\n[ \t]*\}/g;
  let m;
  while ((m = re.exec(txt)) !== null) {
    const keys = [...m[2].matchAll(/\n[ \t]+"([^"]+)":/g)].map(x => x[1]);
    if (!keys.length) continue;
    const nums = keys.map(Number);
    if (nums.some(v => !Number.isFinite(v))) continue;   // 非时间键形态, 跳过
    tracks++;
    if (!nums.every((v, i) => i === 0 || nums[i - 1] <= v)) {
      bad++; byFile[name]++;
      const want = keys.slice().sort((a, b) => Number(a) - Number(b));
      console.log(`UNSORTED ${name}  [${keys.join(',')}] 应为 [${want.join(',')}]`);
    }
  }
}
console.log('');
for (const [k, v] of Object.entries(byFile)) if (v) console.log(`  ${k}: ${v} 条轨道未升序`);
console.log(`扫描 ${files.length} 个文件 / ${tracks} 个轨道对象块; 未升序 ${bad} 条`);
console.log(bad === 0 ? '=> 全部轨道的时间键已按数值升序 ✓' : '=> 仍有未升序轨道 ✗');
process.exit(bad ? 1 : 0);
