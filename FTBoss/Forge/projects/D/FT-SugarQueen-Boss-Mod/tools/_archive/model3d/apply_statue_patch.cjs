// 一次性补丁(2026-09-10):
//   1) 给 5 座白膜石像的方块模型每个面加 "tintindex": 0 —— 让客户端 BlockColor 能按 tint 状态染色
//   2) 删除老屠夫雕像(butcher_statue)的全部资源 + 掉落表 + 语言键
// 用法: node apply_statue_patch.cjs <工程根目录>
const fs = require('fs');
const path = require('path');

const root = process.argv[2] || '.';
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const data = path.join(root, 'src/main/resources/data/ftboss');

/* ---------- 1) 石像模型加 tintindex ---------- */
const STATUES = ['statue_sugar_queen', 'statue_butcher', 'statue_saya', 'statue_wine_barrel', 'statue_rat_swarm'];
let faces = 0, files = 0;
for (const s of STATUES) {
  for (const half of ['lower', 'upper']) {
    const f = path.join(assets, 'models/block', `${s}_${half}.json`);
    if (!fs.existsSync(f)) { console.log(`  [跳过] ${path.basename(f)} 不存在`); continue; }
    const j = JSON.parse(fs.readFileSync(f, 'utf8'));
    for (const el of (j.elements || [])) {
      for (const face of Object.values(el.faces || {})) {
        if (face.tintindex === undefined) { face.tintindex = 0; faces++; }
      }
    }
    fs.writeFileSync(f, JSON.stringify(j, null, 1));
    files++;
  }
}
console.log(`石像模型: ${files} 个文件, 共 ${faces} 个面加上 tintindex:0`);

/* ---------- 2) 删除老屠夫雕像 ---------- */
const dead = [
  'blockstates/butcher_statue.json',
  'models/block/butcher_statue.json',
  'models/block/butcher_statue_lower.json',
  'models/block/butcher_statue_upper.json',
  'models/item/butcher_statue.json',
  'textures/block/butcher_statue.png',
  'textures/block/butcher_statue_texture.png',
  'textures/block/butcher_stone_material.png',
  'textures/item/butcher_statue.png'
];
for (const rel of dead) {
  const f = path.join(assets, rel);
  if (fs.existsSync(f)) { fs.unlinkSync(f); console.log(`  删除 ${rel}`); }
}
const loot = path.join(data, 'loot_tables/blocks/butcher_statue.json');
if (fs.existsSync(loot)) { fs.unlinkSync(loot); console.log('  删除 loot_tables/blocks/butcher_statue.json'); }

/* ---------- 3) 清语言键 ---------- */
for (const lang of ['zh_cn', 'en_us']) {
  const f = path.join(assets, 'lang', `${lang}.json`);
  const j = JSON.parse(fs.readFileSync(f, 'utf8'));
  let n = 0;
  for (const k of Object.keys(j)) {
    if (k === 'block.ftboss.butcher_statue' || k === 'item.ftboss.butcher_statue') { delete j[k]; n++; }
  }
  // 顺手补: 白膜石像的提示语(告诉玩家可以上色)
  j['msg.ftboss.statue_tint'] = lang === 'zh_cn'
    ? '手持对应 Boss 的召唤物左键石像即可上色'
    : 'Left-click a statue with its boss summon item to paint it';
  fs.writeFileSync(f, JSON.stringify(j, null, 4));
  console.log(`  ${lang}.json: 删除 ${n} 个键, 补充 statue_tint 提示键`);
}
console.log('完成');
