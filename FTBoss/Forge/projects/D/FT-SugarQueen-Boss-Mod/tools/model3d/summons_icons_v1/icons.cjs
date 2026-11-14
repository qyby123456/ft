const { Pixels, glyphs } = require('./art.cjs');
function drawSummon(spec) {
  const pixel = new Pixels(32);
  if (spec.id.includes('egg')) {
    pixel.poly([[12, 6], [19, 6], [22, 10], [25, 17], [25, 23], [22, 27], [10, 27], [7, 23], [7, 17], [10, 10]], spec.base);
    pixel.poly([[12, 7], [15, 7], [11, 15], [10, 22], [8, 22], [8, 17]], spec.light);
    pixel.poly([[21, 12], [24, 18], [24, 23], [21, 26], [12, 26], [10, 24], [20, 24]], spec.trim);
    pixel.rect(10, 22, 13, 2, spec.trim); pixel.rect(11, 22, 5, 1, spec.light);
    pixel.rect(12, 12, 9, 9, spec.trim);
    pixel.stamp(glyphs[spec.motif], 13, 13, { '#': spec.light });
    if (spec.id.includes('youzi')) { pixel.poly([[16, 6], [17, 3], [24, 3], [20, 6]], 'green'); pixel.rect(16, 5, 2, 3, 'wood'); }
    if (spec.id.includes('ningmengmao') || spec.id === 'benger_spawn_egg') {
      pixel.poly([[9, 10], [7, 3], [13, 7]], spec.trim); pixel.poly([[19, 7], [25, 3], [23, 11]], spec.trim); pixel.dot(16, 18, 'ink');
    }
    if (spec.id.includes('dandan')) {
      pixel.poly([[7, 15], [3, 10], [3, 18], [7, 21]], 'white'); pixel.poly([[25, 15], [28, 10], [28, 18], [25, 21]], 'white'); pixel.rect(12, 4, 8, 1, 'gold');
    }
    if (spec.id.includes('duola')) pixel.stamp(glyphs.star, 13, 2, { '#': 'goldLight' });
    if (spec.id.includes('gongdi')) { pixel.line(13, 7, 17, 11, 'coral'); pixel.line(17, 10, 20, 8, 'red'); }
    if (spec.id.includes('qingyi')) { pixel.rect(6, 8, 2, 12, 'ice'); pixel.rect(4, 8, 6, 2, 'cyan'); pixel.rect(6, 5, 2, 4, 'white'); }
    if (spec.id.includes('wenxuanlan')) pixel.poly([[7, 13], [5, 15], [4, 20], [9, 24], [25, 20], [28, 16], [25, 13], [26, 17], [23, 19], [9, 22], [6, 20]], 'silver');
    if (spec.id.includes('zheergen')) { pixel.line(12, 26, 8, 29, 'lime'); pixel.line(20, 26, 24, 29, 'lime'); pixel.line(16, 26, 16, 29, 'cream'); pixel.poly([[15, 7], [12, 2], [17, 4], [20, 2], [19, 7]], 'green'); }
    if (spec.id.includes('zicai')) for (const [horizontal, vertical] of [[5, 16], [6, 23], [25, 22], [26, 15], [10, 7], [22, 7]]) pixel.rect(horizontal, vertical, 2, 3, 'pinkLight');
    if (spec.id.includes('xueli')) { pixel.rect(10, 4, 12, 5, 'white'); pixel.rect(7, 4, 5, 3, 'white'); pixel.rect(20, 3, 5, 4, 'white'); pixel.rect(11, 8, 11, 2, 'blue'); }
  } else if (spec.id === 'sugar_queen_crystal') {
    pixel.poly([[16, 3], [23, 11], [21, 21], [16, 25], [9, 21], [8, 12]], 'pink');
    pixel.poly([[16, 3], [16, 24], [10, 20], [9, 12]], 'pinkLight'); pixel.poly([[16, 4], [21, 12], [16, 16], [12, 11]], 'white'); pixel.poly([[16, 16], [21, 12], [19, 20]], 'pinkDark');
    pixel.poly([[7, 18], [10, 21], [13, 19], [16, 23], [20, 19], [22, 21], [25, 18], [23, 26], [9, 26]], 'gold'); pixel.rect(10, 25, 13, 2, 'goldLight'); pixel.rect(15, 24, 3, 2, 'red');
  } else if (spec.id === 'meat_heart') {
    pixel.rect(12, 3, 4, 7, 'redDark'); pixel.rect(13, 3, 2, 5, 'coral'); pixel.rect(19, 4, 3, 6, 'red');
    pixel.poly([[7, 9], [13, 7], [16, 11], [20, 8], [25, 12], [25, 20], [17, 28], [11, 25], [6, 17]], 'red'); pixel.poly([[7, 10], [12, 8], [15, 12], [10, 15], [8, 19], [6, 16]], 'coral'); pixel.poly([[18, 12], [22, 10], [25, 14], [23, 22], [17, 27], [15, 24]], 'redDark');
    pixel.line(14, 13, 18, 22, 'rose'); pixel.line(13, 16, 18, 14, 'cream'); pixel.line(14, 20, 19, 18, 'cream'); pixel.line(17, 24, 21, 21, 'cream'); pixel.rect(8, 12, 2, 3, 'rose');
  } else if (spec.id === 'tea_seed') {
    pixel.poly([[15, 13], [22, 16], [25, 24], [19, 28], [10, 27], [6, 22], [9, 16]], 'wood'); pixel.poly([[15, 14], [20, 16], [12, 19], [10, 25], [7, 22], [10, 17]], 'woodLight');
    pixel.line(15, 17, 18, 26, 'gold'); pixel.line(16, 18, 17, 26, 'goldLight'); pixel.line(15, 17, 16, 7, 'green');
    pixel.poly([[16, 10], [17, 5], [24, 3], [24, 8], [20, 11]], 'green'); pixel.poly([[15, 12], [9, 11], [5, 6], [11, 5], [16, 9]], 'lime'); pixel.line(18, 8, 22, 5, 'mint'); pixel.line(10, 7, 14, 10, 'mint');
  } else if (spec.id === 'wine_charm') {
    pixel.rect(12, 3, 8, 4, 'woodLight'); pixel.rect(11, 7, 10, 4, 'gold'); pixel.rect(12, 8, 3, 2, 'goldLight');
    pixel.poly([[12, 10], [20, 10], [20, 13], [25, 17], [25, 26], [22, 28], [9, 28], [7, 25], [7, 17], [12, 13]], 'purpleDark'); pixel.rect(9, 18, 14, 8, 'purple'); pixel.rect(9, 18, 3, 6, 'lilac'); pixel.rect(10, 14, 2, 3, 'silver');
    pixel.line(12, 11, 21, 19, 'gold'); pixel.disk(20, 20, 4, 'redDark'); pixel.disk(20, 19, 3, 'red'); pixel.rect(20, 18, 1, 3, 'rose');
  } else if (spec.id === 'rat_bait') {
    pixel.poly([[5, 17], [23, 7], [27, 12], [27, 24], [8, 28], [5, 25]], 'gold'); pixel.poly([[5, 17], [23, 7], [27, 12], [9, 22]], 'lemon'); pixel.poly([[9, 22], [27, 12], [27, 24], [9, 28]], 'yellow');
    pixel.disk(19, 13, 2, 'gold'); pixel.disk(22, 22, 2, 'goldDark'); pixel.rect(12, 23, 2, 3, 'goldDark'); pixel.rect(6, 20, 2, 3, 'woodLight'); pixel.line(7, 17, 12, 24, 'red'); pixel.line(12, 24, 25, 17, 'red'); pixel.rect(13, 18, 3, 3, 'coral');
  } else if (spec.id === 'defect_core') {
    pixel.rect(6, 8, 20, 18, 'dark'); pixel.rect(9, 5, 14, 24, 'steel'); pixel.rect(5, 11, 22, 11, 'slate'); pixel.rect(9, 9, 14, 15, 'blueDark'); pixel.poly([[16, 9], [22, 16], [16, 23], [10, 16]], 'cyan'); pixel.poly([[16, 12], [19, 16], [16, 20], [13, 16]], 'ice');
    for (const [horizontal, vertical] of [[8, 8], [22, 8], [8, 24], [22, 24]]) pixel.rect(horizontal, vertical, 2, 2, 'goldLight');
    pixel.rect(11, 3, 2, 3, 'red'); pixel.rect(20, 3, 2, 3, 'cyan'); pixel.rect(23, 13, 5, 2, 'red'); pixel.rect(4, 20, 5, 2, 'cyan');
  } else if (spec.id === 'compost_summon') {
    pixel.rect(7, 13, 19, 14, 'wood'); pixel.rect(9, 13, 3, 14, 'woodLight'); pixel.rect(16, 13, 2, 14, 'woodDark'); pixel.rect(23, 13, 2, 14, 'woodDark'); pixel.rect(6, 13, 21, 3, 'steel'); pixel.rect(7, 24, 19, 3, 'steel'); pixel.rect(8, 14, 8, 1, 'silver');
    pixel.poly([[6, 12], [9, 8], [15, 8], [17, 5], [22, 9], [25, 11], [26, 13]], 'woodDark');
    for (const [horizontal, color] of [[9, 'red'], [12, 'orange'], [15, 'yellow'], [18, 'green'], [21, 'cyan']]) pixel.rect(horizontal, 11, 3, 2, color);
    pixel.line(16, 10, 17, 5, 'lime'); pixel.poly([[17, 6], [19, 3], [24, 4], [21, 7]], 'green'); pixel.stamp(glyphs.sprout, 13, 17, { '#': 'mint' });
  } else if (spec.id === 'forbidden_apple') {
    pixel.line(16, 9, 18, 3, 'wood'); pixel.poly([[18, 5], [20, 2], [25, 3], [22, 6]], 'green'); pixel.poly([[7, 10], [12, 8], [16, 10], [21, 8], [26, 12], [27, 20], [23, 26], [18, 28], [15, 26], [11, 27], [6, 21], [5, 14]], 'red');
    pixel.poly([[7, 12], [12, 10], [15, 12], [10, 15], [8, 20], [6, 17]], 'coral'); pixel.rect(9, 12, 2, 3, 'rose'); pixel.poly([[23, 12], [26, 14], [26, 20], [22, 25], [17, 27], [18, 23], [22, 20]], 'redDark');
    pixel.line(8, 23, 24, 12, 'gold'); pixel.line(7, 21, 21, 10, 'goldLight'); pixel.disk(17, 18, 3, 'goldDark'); pixel.rect(16, 16, 3, 4, 'goldLight');
  } else if (spec.id === 'benger_whistle') {
    pixel.rect(6, 5, 9, 1, 'gold'); pixel.rect(4, 7, 2, 7, 'gold'); pixel.rect(6, 14, 5, 2, 'gold'); pixel.rect(14, 6, 2, 5, 'gold');
    pixel.poly([[15, 10], [22, 12], [26, 18], [25, 23], [19, 27], [12, 24], [9, 19], [6, 19], [5, 15], [10, 14]], 'steel'); pixel.poly([[15, 11], [21, 13], [18, 16], [9, 18], [7, 16], [10, 15]], 'silver'); pixel.poly([[22, 15], [25, 18], [24, 22], [19, 25], [16, 23]], 'slate');
    pixel.stamp(glyphs.paw, 14, 16, { '#': 'goldLight' }); pixel.rect(9, 14, 3, 2, 'dark');
  }
  pixel.outline(); return pixel;
}
function drawDisc(spec) {
  const pixel = new Pixels(32);
  pixel.disk(15.5, 15.5, 14.6, 'ink'); pixel.disk(15.5, 15.5, 13.3, 'dark'); pixel.disk(15.5, 15.5, 12.3, 'void');
  for (const radius of [11.4, 9.5]) for (let vertical = 2; vertical < 30; vertical++) for (let horizontal = 2; horizontal < 30; horizontal++) if (Math.abs(Math.hypot(horizontal - 15.5, vertical - 15.5) - radius) < 0.4) pixel.dot(horizontal, vertical, horizontal + vertical < 28 ? 'slate' : 'dark');
  pixel.disk(15.5, 15.5, 7.8, spec.base); pixel.disk(15.5, 15.5, 6.6, spec.light); pixel.disk(15.5, 15.5, 5.5, spec.base); pixel.stamp(glyphs[spec.motif], 12, 10, { '#': spec.light });
  pixel.rect(15, 17, 2, 2, 'void'); pixel.rect(14, 19, 4, 1, spec.light); pixel.line(5, 9, 10, 4, 'silver'); pixel.line(7, 10, 11, 6, 'slate'); pixel.rect(22, 24, 4, 2, spec.base); pixel.rect(23, 23, 2, 1, spec.light);
  if (spec.id.endsWith('_p2')) {
    pixel.rect(11, 25, 2, 3, spec.light); pixel.rect(15, 26, 2, 3, spec.light);
    if (spec.id.includes('apple')) { pixel.line(19, 6, 22, 9, 'lime'); pixel.line(22, 9, 20, 12, 'lime'); }
    else ['red', 'orange', 'yellow', 'green', 'cyan', 'purple'].forEach((color, index) => pixel.rect(9 + index * 2, 4, 2, 1, color));
  }
  return pixel;
}
module.exports = { drawSummon, drawDisc };
