const { colors, rgba, Pixels, glyphs } = require('./art.cjs');
const materials = Object.keys(colors);
function makeAtlas() {
  const atlas = new Pixels(64);
  materials.forEach((material, index) => {
    const left = index % 8 * 8; const top = Math.floor(index / 8) * 8;
    const base = rgba(material);
    for (let vertical = 0; vertical < 8; vertical++) for (let horizontal = 0; horizontal < 8; horizontal++) {
      const shift = vertical === 1 ? 9 : vertical === 6 ? -10 : (horizontal * 7 + vertical * 3 + index) % 17 === 0 ? -6 : 0;
      atlas.image.data.set([...base.slice(0, 3).map(value => Math.max(0, Math.min(255, value + shift))), 255], ((top + vertical) * 64 + left + horizontal) * 4);
    }
  });
  return atlas;
}
function modelFor(spec) {
  const elements = [];
  function box(name, from, to, material, rotation) {
    const index = materials.indexOf(material);
    if (index < 0) throw new Error(`Unknown material ${material}`);
    const left = index % 8 * 2; const top = Math.floor(index / 8) * 2;
    const faces = Object.fromEntries(['north', 'south', 'east', 'west', 'up', 'down'].map(side => [side, { uv: [left + 0.25, top + 0.25, left + 1.75, top + 1.75], texture: '#atlas' }]));
    elements.push({ name: `${name}_${elements.length}`, from, to, faces, ...(rotation ? { rotation } : {}) });
  }
  function rounded(name, bottom, top, width, depth, material, centerX = 8, centerZ = 8) {
    const bevel = Math.min(1, width / 5, depth / 5);
    box(name, [centerX - width / 2 + bevel, bottom, centerZ - depth / 2], [centerX + width / 2 - bevel, top, centerZ + depth / 2], material);
    box(name, [centerX - width / 2, bottom, centerZ - depth / 2 + bevel], [centerX - width / 2 + bevel, top, centerZ + depth / 2 - bevel], material);
    box(name, [centerX + width / 2 - bevel, bottom, centerZ - depth / 2 + bevel], [centerX + width / 2, top, centerZ + depth / 2 - bevel], material);
  }
  function ring(name, bottom, top, width, depth, material, thickness = 0.55) {
    box(name, [8 - width / 2, bottom, 8 - depth / 2], [8 + width / 2, top, 8 - depth / 2 + thickness], material);
    box(name, [8 - width / 2, bottom, 8 + depth / 2 - thickness], [8 + width / 2, top, 8 + depth / 2], material);
    box(name, [8 - width / 2, bottom, 8 - depth / 2 + thickness], [8 - width / 2 + thickness, top, 8 + depth / 2 - thickness], material);
    box(name, [8 + width / 2 - thickness, bottom, 8 - depth / 2 + thickness], [8 + width / 2, top, 8 + depth / 2 - thickness], material);
  }
  function badge(motif, centerX, bottom, front, material, size = 0.5) {
    const rows = glyphs[motif];
    rows.forEach((row, vertical) => [...row].forEach((pixel, horizontal) => {
      if (pixel !== '#') return;
      const left = centerX + (horizontal - 3.5) * size; const low = bottom + (rows.length - vertical - 1) * size;
      box(`徽记_${motif}`, [left, low, front - 0.15], [left + size, low + size, front], material);
    }));
  }
  function leaf(name, centerX, bottom, centerZ, angle, material) {
    box(name, [centerX - 2.3, bottom, centerZ - 1.2], [centerX + 2.3, bottom + 0.7, centerZ + 1.2], material, { origin: [centerX, bottom, centerZ], axis: 'z', angle });
    box(`${name}_叶脉`, [centerX - 1.8, bottom + 0.71, centerZ - 0.16], [centerX + 1.8, bottom + 0.85, centerZ + 0.16], 'mint', { origin: [centerX, bottom, centerZ], axis: 'z', angle });
  }
  if (spec.id.includes('egg')) {
    for (const [bottom, top, width, depth] of [[2.5, 3.5, 6, 5], [3.5, 5, 8.5, 7], [5, 8, 10, 8], [8, 10, 9, 7.5], [10, 12, 7, 6], [12, 13, 4.5, 4]]) rounded('灵蛋外壳', bottom, top, width, depth, spec.base);
    ring('纹章束环', 4.1, 4.85, 8.7, 7.2, spec.trim);
    box('徽章底', [5.5, 5.4, 3.76], [10.5, 9.5, 3.97], spec.trim);
    badge(spec.motif, 8, 5.7, 3.74, spec.light, 0.56);
    box('釉面高光', [4, 6.3, 3.92], [4.4, 7.65, 4.04], spec.light);
    if (spec.id.includes('youzi')) {
      box('柚梗', [7.7, 12.8, 7.6], [8.5, 14.3, 8.3], 'wood'); leaf('柚叶', 10, 13.6, 8, 22.5, 'green');
      for (const horizontal of [5.1, 10.4]) box('果皮纵瓣', [horizontal, 5.1, 3.86], [horizontal + 0.35, 7.9, 4.01], 'gold');
    }
    if (spec.id.includes('dandan')) {
      for (const direction of [-1, 1]) for (let feather = 0; feather < 3; feather++) {
        const centerX = 8 + direction * (5.1 + feather * 0.7);
        box('月白羽翼', [centerX - 0.42, 6 + feather * 0.6, 7], [centerX + 0.42, 10.3 + feather * 0.75, 8.8], 'white', { origin: [centerX, 7, 8], axis: 'z', angle: direction * -22.5 });
      }
      ring('月金光环', 14.4, 14.7, 6, 5, 'goldLight', 0.3);
    }
    if (spec.id.includes('duola')) {
      badge('star', 8, 12.5, 7.4, 'goldLight', 0.48);
      box('星芒左翼', [2, 7.5, 7.7], [3.5, 8.4, 8.4], 'gold'); box('星芒右翼', [12.5, 7.5, 7.7], [14, 8.4, 8.4], 'gold');
    }
    if (spec.id.includes('gongdi')) {
      for (const horizontal of [5.2, 9.5]) box('铁肩锁扣', [horizontal, 9, 4.6], [horizontal + 1.3, 11.6, 5.4], 'redDark');
      box('血色裂隙', [7.6, 10.2, 4.8], [8.2, 12.6, 5.05], 'coral', { origin: [8, 11, 5], axis: 'z', angle: -22.5 });
    }
    if (spec.id.includes('ningmengmao') || spec.id === 'benger_spawn_egg') {
      for (const direction of [-1, 1]) {
        const centerX = 8 + direction * 3;
        box('兽耳', [centerX - 1, 11.2, 6.2], [centerX + 1, 14, 8.4], spec.trim, { origin: [centerX, 11.2, 7.3], axis: 'z', angle: direction * -22.5 });
        box('内耳', [centerX - 0.5, 11.9, 6.05], [centerX + 0.5, 13.3, 6.3], spec.light, { origin: [centerX, 11.2, 7.3], axis: 'z', angle: direction * -22.5 });
      }
    }
    if (spec.id.includes('qingyi')) {
      box('潮汐三叉戟柄', [2.1, 3.5, 7.3], [2.6, 13.5, 8], 'ice'); box('三叉戟肩', [0.8, 11.7, 7.2], [3.9, 12.25, 8.1], 'cyan');
      for (const horizontal of [0.8, 3.3]) box('三叉戟齿', [horizontal, 11.8, 7.2], [horizontal + 0.6, 13.8, 8.1], 'ice');
    }
    if (spec.id.includes('wenxuanlan')) {
      ring('飞轮环', 6.85, 7.45, 12.5, 10.6, 'silver', 0.65);
      for (const horizontal of [0.75, 14.45]) box('飞轮齿', [horizontal, 6.4, 7.2], [horizontal + 0.8, 7.8, 8.8], 'blue');
    }
    if (spec.id.includes('zheergen')) {
      leaf('根生叶', 6.1, 13.4, 8, -22.5, 'green'); leaf('根生叶', 9.7, 13.6, 8, 22.5, 'lime');
      for (const direction of [-1, 1]) box('虚空根须', [7.7 + direction * 2, 0.9, 7.3], [8.3 + direction * 2, 3.7, 8.3], 'cream', { origin: [8 + direction * 2, 3.5, 8], axis: 'z', angle: direction * -22.5 });
      box('主根', [7.7, 0.5, 7.5], [8.3, 2.6, 8.4], 'lime');
    }
    if (spec.id.includes('zicai')) {
      for (const centerX of [2.4, 13.6]) for (const bottom of [5.5, 8.4]) box('河豚侧刺', [centerX - 0.75, bottom, 7.2], [centerX + 0.75, bottom + 0.8, 8.6], 'pinkLight');
      for (const centerX of [5.6, 10.4]) box('河豚顶刺', [centerX - 0.4, 11.3, 7.5], [centerX + 0.4, 13.3, 8.3], 'pink');
    }
    if (spec.id.includes('xueli')) {
      rounded('厨师帽檐', 12, 13.2, 7, 6, 'blue'); rounded('厨师帽', 13.2, 15, 7.5, 6.5, 'white');
      rounded('厨帽左泡', 13.7, 15.8, 3.5, 5, 'white', 5.2); rounded('厨帽右泡', 13.7, 15.6, 3.5, 5, 'white', 10.8);
    }
  } else if (spec.id === 'sugar_queen_crystal') {
    rounded('王冠底座', 2.2, 3.2, 9, 7, 'gold'); ring('金冠围边', 3.2, 4.8, 8.5, 6.5, 'gold');
    for (const horizontal of [4.1, 7.65, 11.2]) box('王冠尖齿', [horizontal, 4.5, 4.6], [horizontal + 0.65, 6.4, 5.3], 'goldLight');
    for (const [bottom, top, width, depth, material] of [[3.3, 5.7, 4, 3.5, 'pinkDark'], [5.7, 10.4, 5.5, 4.5, 'pink'], [10.4, 12.1, 3.8, 3.2, 'pinkLight'], [12.1, 13.7, 2.2, 2.1, 'pinkLight'], [13.7, 14.7, 0.8, 1, 'white']]) rounded('主糖晶', bottom, top, width, depth, material);
    box('晶面反光', [6, 6.2, 5.66], [6.6, 10.2, 5.78], 'white'); box('冠心宝石', [7.3, 2.9, 4.37], [8.7, 4.3, 4.65], 'red');
    for (const direction of [-1, 1]) box('侧糖晶', [7 + direction * 3, 4.4, 7], [9 + direction * 3, 8.4, 9], 'pinkLight', { origin: [8 + direction * 3, 4.4, 8], axis: 'z', angle: direction * -22.5 });
  } else if (spec.id === 'meat_heart') {
    rounded('心尖', 2, 4, 3, 3.5, 'redDark', 8.5); rounded('心室', 4, 6, 6, 5, 'red'); rounded('心室', 6, 10, 9, 6, 'red');
    rounded('左心房', 10, 12, 4, 5, 'coral', 5.6); rounded('右心房', 9.8, 11.5, 4.6, 5.2, 'red', 10.2);
    box('主动脉', [6.7, 11.3, 7.1], [8.1, 14.6, 8.6], 'redDark'); box('血管断口', [6.9, 14.61, 7.3], [7.9, 14.66, 8.4], 'void');
    box('侧血管', [10, 11, 7.1], [11, 13.7, 8.2], 'red', { origin: [10.5, 11, 7.6], axis: 'z', angle: -22.5 });
    for (let stitch = 0; stitch < 4; stitch++) box('象牙缝合线', [7 + stitch * 0.3, 5.6 + stitch * 1.2, 4.78], [9.3 + stitch * 0.3, 5.87 + stitch * 1.2, 5.03], 'cream', { origin: [8.3, 8, 5], axis: 'z', angle: 22.5 });
    box('心室高光', [4.9, 7.1, 4.86], [5.5, 9.4, 5.02], 'rose');
  } else if (spec.id === 'tea_seed') {
    rounded('茶种底', 2.3, 3.5, 5, 5, 'woodDark'); rounded('茶种壳', 3.5, 7, 9, 7, 'wood'); rounded('茶种肩', 7, 8.5, 6.5, 5.5, 'woodLight');
    box('种皮裂缝', [7.7, 3.7, 4.36], [8.2, 6.8, 4.6], 'gold'); box('茶芽梗', [7.7, 8.2, 7.7], [8.3, 12.5, 8.3], 'green');
    leaf('嫩茶左叶', 5.7, 10.8, 8, -22.5, 'lime'); leaf('嫩茶右叶', 10.2, 12.1, 8, 22.5, 'green');
    badge('leaf', 10.5, 4.2, 4.36, 'goldLight', 0.3);
  } else if (spec.id === 'wine_charm') {
    rounded('瓶底', 2, 3, 7.5, 6.5, 'purpleDark'); rounded('葡萄酒瓶身', 3, 9, 9, 7, 'purple'); rounded('瓶肩', 9, 10.4, 6.5, 5.5, 'purpleDark');
    rounded('瓶颈', 10.4, 13, 3.5, 3.5, 'purpleDark'); ring('黄铜瓶箍', 11.7, 12.5, 4, 4, 'gold'); rounded('软木塞', 12.6, 14.6, 3, 3, 'woodLight');
    box('玻璃高光', [4.7, 3.6, 4.38], [5.2, 8.5, 4.53], 'lilac'); box('封签', [7.4, 6.5, 4.28], [8, 11, 4.5], 'gold');
    rounded('酒红蜡封', 5.4, 8.5, 3.4, 0.5, 'redDark', 9.1, 4.15); badge('glass', 9.1, 5.8, 3.88, 'goldLight', 0.32);
  } else if (spec.id === 'rat_bait') {
    for (let slice = 0; slice < 6; slice++) {
      const left = 3 + slice * 1.5;
      box('奶酪楔片', [left, 3, 4], [left + 1.5, 5.3 + slice * 1.2, 12], slice % 2 ? 'yellow' : 'gold');
      box('奶酪顶面', [left, 5.3 + slice * 1.2, 4], [left + 1.5, 5.5 + slice * 1.2, 12], 'lemon');
    }
    for (const [horizontal, bottom, size] of [[5, 3.9, 0.9], [8, 5.1, 1.4], [10.5, 7.7, 1.1]]) rounded('奶酪孔', bottom, bottom + size, size, 0.18, 'goldDark', horizontal, 3.92);
    box('诱饵红绳', [6.9, 3.1, 3.8], [7.5, 8.1, 4.05], 'red'); box('诱饵红绳背', [6.9, 3.1, 11.95], [7.5, 8.1, 12.2], 'red'); box('红绳结', [6.5, 7.7, 7], [8, 8.2, 9], 'coral');
  } else if (spec.id === 'defect_core') {
    rounded('机芯外壳', 3, 13, 9, 7, 'dark'); ring('机芯上下框', 3, 4, 9.6, 7.6, 'steel'); ring('机芯上下框', 12, 13, 9.6, 7.6, 'steel');
    for (const horizontal of [3.4, 11.5]) box('机芯侧柱', [horizontal, 4, 4.3], [horizontal + 1.1, 12, 11.7], 'slate');
    box('反应窗底', [5, 5, 4.35], [11, 11, 4.55], 'blueDark'); box('离子晶体', [6, 6, 3.5], [10, 10, 4.3], 'cyan', { origin: [8, 8, 4], axis: 'z', angle: 45 });
    box('离子晶核', [7, 7, 3.23], [9, 9, 3.55], 'ice', { origin: [8, 8, 4], axis: 'z', angle: 45 });
    for (const horizontal of [4, 11.4]) for (const bottom of [3.3, 12.3]) box('框架铆钉', [horizontal, bottom, 4.1], [horizontal + 0.6, bottom + 0.45, 4.27], 'goldLight');
    for (const [horizontal, material] of [[6, 'red'], [9.6, 'cyan']]) box('故障端子', [horizontal, 13, 7.1], [horizontal + 0.7, 14.5, 8.2], material);
    box('后盖散热片', [5.5, 6, 11.05], [10.5, 10, 11.5], 'slate');
  } else if (spec.id === 'compost_summon') {
    rounded('堆肥桶底', 2, 3, 8, 7, 'woodDark'); rounded('堆肥桶', 3, 10, 10, 8, 'wood');
    for (const horizontal of [4, 6, 8, 10]) box('木板拼缝', [horizontal, 3.2, 3.88], [horizontal + 0.18, 9.8, 4.03], 'woodDark');
    ring('铁箍', 3, 3.8, 10.3, 8.3, 'steel'); ring('铁箍', 9, 10, 10.3, 8.3, 'steel');
    rounded('腐殖土', 10, 11.3, 8, 6, 'woodDark'); rounded('腐殖土堆', 11.3, 12.3, 5.5, 4, 'woodDark');
    for (const [index, material] of ['red', 'orange', 'yellow', 'green', 'cyan', 'purple'].entries()) box('虹彩菌落', [4.2 + index * 1.25, 10.1, 4.65], [5.35 + index * 1.25, 10.65, 5.4], material);
    box('新芽', [8, 12, 7.5], [8.5, 14.1, 8.1], 'lime'); leaf('堆肥新叶', 10, 13.7, 8, 22.5, 'green'); badge('sprout', 8, 4.3, 3.82, 'mint', 0.5);
  } else if (spec.id === 'forbidden_apple') {
    rounded('果底', 2.8, 4, 6, 6, 'redDark'); rounded('果肉', 4, 6, 9, 8, 'red'); rounded('果腹', 6, 10, 11, 9, 'red'); rounded('左果肩', 10, 11.5, 4.5, 6.5, 'red', 5.5); rounded('右果肩', 10, 11.5, 4.5, 6.5, 'coral', 10.5);
    box('禁果梗', [7.7, 10.2, 7.5], [8.5, 13.7, 8.3], 'wood', { origin: [8, 10.2, 8], axis: 'z', angle: -22.5 }); leaf('禁果叶', 10.2, 13.2, 8, 22.5, 'green');
    ring('禁果封印', 6.4, 7.05, 11.25, 9.25, 'gold', 0.35); box('封印锁', [7.15, 5.9, 3.1], [8.85, 8.1, 3.55], 'goldLight'); box('锁孔', [7.8, 6.5, 2.98], [8.2, 7.4, 3.13], 'redDark'); box('果皮高光', [4.4, 8, 3.38], [5.1, 9.4, 3.53], 'rose');
  } else if (spec.id === 'benger_whistle') {
    rounded('哨腔底', 3, 4.5, 5, 5.5, 'slate', 9); rounded('牧羊哨腔', 4.5, 8.5, 7, 6.5, 'steel', 9); rounded('哨腔顶', 8.5, 9.5, 5, 5.5, 'silver', 9);
    box('吹嘴', [2.5, 6.4, 6.5], [6.5, 8.2, 9.5], 'silver'); box('吹嘴孔', [2.35, 6.85, 7], [2.55, 7.7, 9], 'ink'); box('哨口', [4.8, 8.21, 7], [6, 8.28, 9], 'ink');
    box('挂环左', [7, 9.3, 7.7], [7.5, 13.8, 8.3], 'gold'); box('挂环右', [10.5, 9.3, 7.7], [11, 13.8, 8.3], 'gold'); box('挂环顶', [7.5, 13.3, 7.7], [10.5, 13.8, 8.3], 'goldLight');
    badge('paw', 9, 4.9, 4.61, 'goldLight', 0.48);
  }
  const transform = (rotation, translation, scale) => ({ rotation, translation, scale: [scale, scale, scale] });
  return {
    credit: `FTBoss / ${spec.concept}`, texture_size: [64, 64], ambientocclusion: false, gui_light: 'front',
    textures: { atlas: 'ftboss:item/summons_redraw/atlas', particle: `ftboss:item/${spec.id}` }, elements,
    display: {
      gui: transform([18, -30, 0], [0, 0, 0], 0.85), ground: transform([0, 0, 0], [0, 3, 0], 0.48),
      fixed: transform([0, 180, 0], [0, 0, 0], 0.85), head: transform([0, 180, 0], [0, 10, 4], 0.75),
      thirdperson_righthand: transform([0, -90, 0], [0, 2.5, 0], 0.65), thirdperson_lefthand: transform([0, 90, 0], [0, 2.5, 0], 0.65),
      firstperson_righthand: transform([0, -35, 8], [0.5, 1.4, 0], 0.7), firstperson_lefthand: transform([0, 35, -8], [0.5, 1.4, 0], 0.7)
    }
  };
}
module.exports = { modelFor, makeAtlas };
