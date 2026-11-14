/* weapons_parent_report.cjs —— 把"带几何的物品模型"按父链分组（只读）
 *
 * 为什么需要它：2026-09-15 用户反馈"几乎全部武器除了已经自带模型的武器外都有一样的问题"。
 * 分组后一眼能看出规律：
 *   甲组 = 带 elements + `parent: item/handheld|generated`
 *   乙组 = 带 elements + 无父链 / 方块父链
 * 如果只有甲组出问题，那罪魁就是**生成式父链**（MC 会走"读 layer0 烘平面"那条路，
 * 把 256² 的逐面 UV 图集当一张平面图标画出来 = 用户说的"贴图被展开"）。
 *
 * 用法: node tools/model3d/weapons_parent_report.cjs
 */
'use strict';

const fs = require('fs');
const path = require('path');
const { decodePNG } = require('./png.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const DIR = path.join(ROOT, 'src/main/resources/assets/ftboss/models/item');
const TEX = path.join(ROOT, 'src/main/resources/assets/ftboss/textures/item');

const generated = [];
const plain = [];

for (const f of fs.readdirSync(DIR)) {
  if (!f.endsWith('.json')) continue;
  let m;
  try { m = JSON.parse(fs.readFileSync(path.join(DIR, f), 'utf8')); } catch { continue; }
  const els = (m.elements || []).length;
  if (els === 0) continue;                         // 纯平面图标不在本次范围
  const id = f.replace('.json', '');
  const parent = m.parent || '(none)';
  const res = (m.textures && m.textures.layer0) || '';
  let dim = '?';
  const tex = res ? path.join(TEX, res.split(':').pop() + '.png') : null;
  if (tex && fs.existsSync(tex)) { const { w, h } = decodePNG(tex); dim = w + 'x' + h; }
  const row = id.padEnd(24) + ' parent=' + parent.padEnd(26) + ' layer0=' + dim.padEnd(10) + ' elements=' + els;
  if (/item\/(handheld|generated)/.test(parent)) generated.push(row);
  else plain.push(row);
}

console.log('=== A 组：带几何 + parent 是 item/handheld|generated（' + generated.length + ' 个）===');
generated.forEach(r => console.log('  ' + r));
console.log('');
console.log('=== B 组：带几何 + 无父链 / 其它父链（' + plain.length + ' 个）===');
plain.forEach(r => console.log('  ' + r));
console.log('');
console.log('判读：如果用户说"没问题"的那批全在 B 组，那罪魁就是 A 组的生成式父链。');
