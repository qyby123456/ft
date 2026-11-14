/* fix_handheld_parent.cjs —— 拆掉"生成式父链"，改成和已验证正常的奶酪锤/鸡腿同款写法
 *
 * 问题（2026-09-15 用户实测定位）：
 *   带 `elements` 的物品模型一旦挂 `parent: item/handheld`（根是 `builtin/generated`），
 *   MC 会走"读 #layer0 烘成一张平面图标"那条路 —— 于是 gen_weapons3d 出的 **256² 逐面 UV 图集**
 *   被当平面贴图画出来，游戏里就是"贴图被展开/零件散开"。
 *   对照：`cheese_wheel_mace`（同生成器、同图集、**无父链**）正常；`turkey_legs`/`candy_cane` 同理。
 *
 * 做法（保持显示效果完全不变）：
 *   1. 把父链提供的 display 槽**显式补进模型**（数值取自本机 client-extra.jar 的
 *      `assets/minecraft/models/item/generated.json`，不靠记忆）：
 *        ground / head / fixed 三个槽 + `gui_light: "front"`
 *      （`gui` 在原版里本来就没有条目 = 单位矩阵，所以不用补）
 *   2. 删掉 `parent` 字段。
 *   模型自带的 4 个手持槽（third/…、first/…）原样保留 → 手持姿态与现在完全一致。
 *
 * 用法:
 *   node tools/model3d/fix_handheld_parent.cjs --dry     只报告不改
 *   node tools/model3d/fix_handheld_parent.cjs           备份并改
 */
'use strict';

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..');
const DIR = path.join(ROOT, 'src/main/resources/assets/ftboss/models/item');
const BAK = path.join(ROOT, '_backup', 'handheld_parent_fix_20260915');
const DRY = process.argv.includes('--dry');

/* 取自 client-extra.jar: assets/minecraft/models/item/generated.json */
const FROM_GENERATED = {
  ground: { rotation: [0, 0, 0], translation: [0, 2, 0], scale: [0.5, 0.5, 0.5] },
  head: { rotation: [0, 180, 0], translation: [0, 13, 7], scale: [1, 1, 1] },
  fixed: { rotation: [0, 180, 0], scale: [1, 1, 1] }
};

if (!DRY && !fs.existsSync(BAK)) fs.mkdirSync(BAK, { recursive: true });

const changed = [];
const skipped = [];

for (const f of fs.readdirSync(DIR)) {
  if (!f.endsWith('.json')) continue;
  const p = path.join(DIR, f);
  const m = JSON.parse(fs.readFileSync(p, 'utf8'));
  const parent = m.parent || '';
  if (!/item\/(handheld|generated)/.test(parent)) continue;
  if ((m.elements || []).length === 0) { skipped.push(f + '（没有 elements，纯平面，父链是对的）'); continue; }

  const before = JSON.stringify(m);
  m.display = m.display || {};
  const added = [];
  for (const [slot, val] of Object.entries(FROM_GENERATED)) {
    if (!m.display[slot]) { m.display[slot] = val; added.push(slot); }
  }
  if (!m.gui_light) { m.gui_light = 'front'; added.push('gui_light'); }
  delete m.parent;

  if (JSON.stringify(m) === before) { skipped.push(f + '（无需改动）'); continue; }

  if (!DRY) {
    fs.copyFileSync(p, path.join(BAK, f));                 // 先备份原件
    fs.writeFileSync(p, JSON.stringify(m, null, 2) + '\n', 'utf8');
  }
  changed.push({ f, added });
}

console.log('=== 拆生成式父链 ' + (DRY ? '(dry-run，未写盘)' : '') + ' ===');
console.log('改动 ' + changed.length + ' 个模型：');
for (const c of changed) console.log('  ' + c.f.padEnd(30) + ' 补: ' + c.added.join(', ') + '   并删掉 parent');
if (skipped.length) {
  console.log('');
  console.log('跳过 ' + skipped.length + ' 个：');
  for (const s of skipped) console.log('  · ' + s);
}
if (!DRY) console.log('\n原件备份在: ' + path.relative(ROOT, BAK));
