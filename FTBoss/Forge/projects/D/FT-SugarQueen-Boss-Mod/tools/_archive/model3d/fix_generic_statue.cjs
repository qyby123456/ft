/* 通用石像 ftboss:statue 的补齐(2026-09-12)
 *
 * 背景: 这座石像是【交易用】的(BossHandler 右键它 → TradeHandler.tryTrade),
 * 但它在某次"石像贴图损坏"事故里丢了 blockstate 与物品模型 ——
 * 只剩 models/block/statue_a.json(下半) + statue_b.json(上半) 两个模型没人引用,
 * 结果: 物品栏里是紫黑 missing 模型, 放下也渲染不出来, 右键交易永远触发不了。
 *
 * 本脚本一次补齐四样:
 *   assets/ftboss/blockstates/statue.json                 half=lower -> statue_a, half=upper -> statue_b
 *   assets/ftboss/models/item/statue.json                 3D 物品模型(上下半合并, 缩到一格内)
 *   data/ftboss/loot_tables/blocks/statue.json            破坏掉落自身
 *   (创造栏条目与 TINT 档位在 Java 侧, 见 FTCreativeTabs / CandyStatueBlock)
 *
 * 用法: node fix_generic_statue.cjs <resourcesRoot>
 */
const fs = require('fs');
const path = require('path');
const RES = process.argv[2];                 // src/main/resources
const A = path.join(RES, 'assets/ftboss');

const rd = (p) => JSON.parse(fs.readFileSync(p, 'utf8'));
const r2 = (n) => Math.round(n * 1000) / 1000;
const lower = rd(path.join(A, 'models/block/statue_a.json'));
const upper = rd(path.join(A, 'models/block/statue_b.json'));

/* ---------- 1) blockstate ---------- */
const variants = {};
for (const white of ['true', 'false']) {
  variants[`half=lower,white=${white}`] = { model: 'ftboss:block/statue_a' };
  variants[`half=upper,white=${white}`] = { model: 'ftboss:block/statue_b' };
}
fs.writeFileSync(path.join(A, 'blockstates/statue.json'),
  JSON.stringify({ variants }, null, 2) + '\n');

/* ---------- 2) 3D 物品模型: 上下半合并 + 等比缩到一格 ---------- */
const xf = (v, isUpper) => [
  r2((v[0] - 8) * 0.5 + 8),
  r2(isUpper ? (v[1] + 16) * 0.5 : v[1] * 0.5),
  r2((v[2] - 8) * 0.5 + 8),
];
const convert = (el, isUpper) => {
  const o = JSON.parse(JSON.stringify(el));
  o.from = xf(el.from, isUpper);
  o.to = xf(el.to, isUpper);
  if (o.rotation && o.rotation.origin) o.rotation.origin = xf(o.rotation.origin, isUpper);
  return o;
};
const tex = {};
for (const src of [lower, upper]) for (const k in src.textures || {}) if (k !== 'particle' && !tex[k]) tex[k] = src.textures[k];
tex.particle = lower.textures.particle || Object.values(tex)[0];
const itemModel = {
  parent: 'minecraft:block/block',
  ambientocclusion: false,
  textures: tex,
  elements: [
    ...lower.elements.map((e) => convert(e, false)),
    ...upper.elements.map((e) => convert(e, true)),
  ],
};
fs.writeFileSync(path.join(A, 'models/item/statue.json'), JSON.stringify(itemModel, null, 2) + '\n');

/* ---------- 3) 战利品表 ---------- */
const loot = {
  type: 'minecraft:block',
  pools: [{
    rolls: 1,
    entries: [{ type: 'minecraft:item', name: 'ftboss:statue' }],
    conditions: [{
      condition: 'minecraft:block_state_property',
      block: 'ftboss:statue',
      properties: { half: 'lower' },
    }],
  }],
};
fs.writeFileSync(path.join(RES, 'data/ftboss/loot_tables/blocks/statue.json'),
  JSON.stringify(loot, null, 2) + '\n');

console.log(`通用石像补齐: blockstate 4 变体, 物品模型 ${itemModel.elements.length} 方块, 战利品表 1 张`);
