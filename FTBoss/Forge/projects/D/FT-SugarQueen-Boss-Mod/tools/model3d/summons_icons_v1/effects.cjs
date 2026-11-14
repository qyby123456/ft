const { Pixels, glyphs } = require('./art.cjs');
function drawEffect(spec) {
  const pixel = new Pixels(18);
  if (spec.id === 'infection') {
    pixel.disk(8, 9, 5, 'green'); pixel.disk(8, 8, 4, 'lime');
    for (const [startX, startY, endX, endY] of [[8, 1, 8, 5], [2, 3, 5, 6], [1, 10, 4, 10], [4, 15, 6, 12], [12, 15, 11, 12], [15, 8, 12, 8], [13, 2, 11, 5]]) { pixel.line(startX, startY, endX, endY, 'green'); pixel.dot(startX, startY, 'mint'); }
    pixel.rect(6, 6, 2, 2, 'mint'); pixel.rect(10, 8, 2, 2, 'greenDark'); pixel.dot(7, 11, 'greenDark');
  } else if (spec.id === 'poop_confusion') {
    pixel.poly([[7, 3], [10, 5], [8, 7], [12, 7], [14, 10], [13, 12], [15, 14], [3, 14], [3, 12], [5, 10], [4, 8], [7, 7]], 'wood'); pixel.line(6, 10, 11, 10, 'woodLight'); pixel.line(5, 13, 12, 13, 'gold'); pixel.rect(7, 8, 1, 1, 'white'); pixel.rect(10, 8, 1, 1, 'white'); pixel.line(2, 6, 2, 3, 'cyan'); pixel.line(2, 3, 5, 3, 'cyan'); pixel.dot(4, 2, 'ice'); pixel.line(15, 11, 15, 6, 'lilac'); pixel.dot(14, 7, 'lilac');
  } else if (spec.id === 'shadow_erosion') {
    pixel.poly([[8, 2], [14, 6], [13, 11], [8, 15], [3, 11], [2, 6]], 'purple'); pixel.poly([[8, 4], [12, 7], [8, 12], [4, 8]], 'lilac'); pixel.poly([[9, 2], [7, 7], [10, 8], [7, 12], [10, 15], [11, 9], [8, 7]], 'void'); pixel.rect(2, 3, 2, 2, 'lilac'); pixel.rect(13, 13, 2, 2, 'purple'); pixel.dot(3, 14, 'pink');
  } else if (spec.id === 'heal_ban') {
    pixel.rect(6, 2, 5, 13, 'mint'); pixel.rect(2, 6, 13, 5, 'mint'); pixel.rect(7, 3, 2, 4, 'white'); pixel.line(3, 14, 14, 3, 'redDark', 2); pixel.line(3, 13, 13, 3, 'coral', 2);
  } else if (spec.id === 'blood_rage' || spec.id === 'blood_rage_fake') {
    const real = spec.id === 'blood_rage';
    pixel.poly([[3, 4], [6, 7], [8, 2], [10, 7], [14, 3], [15, 9], [12, 14], [8, 16], [3, 12]], real ? 'red' : 'orange'); pixel.poly([[5, 8], [8, 5], [9, 9], [12, 6], [12, 11], [8, 14], [5, 11]], real ? 'coral' : 'goldLight'); pixel.rect(5, 9, 2, 2, 'ink'); pixel.rect(10, 9, 2, 2, 'ink'); pixel.rect(7, 12, 3, 1, 'cream');
    if (!real) { pixel.line(2, 2, 15, 15, 'dark'); pixel.dot(14, 2, 'silver'); pixel.dot(15, 3, 'silver'); }
  } else if (spec.id === 'blue_fire') {
    pixel.poly([[8, 1], [10, 7], [13, 4], [15, 10], [13, 14], [9, 16], [4, 14], [2, 10], [4, 5], [5, 9]], 'blue'); pixel.poly([[8, 5], [10, 10], [12, 8], [12, 13], [8, 15], [5, 12], [5, 9], [7, 11]], 'cyan'); pixel.poly([[8, 9], [10, 13], [8, 15], [6, 13]], 'ice');
  } else if (spec.id === 'qingyi_charge') {
    pixel.stamp(glyphs.trident, 2, 2, { '#': 'cyan' }, 2); pixel.poly([[9, 3], [6, 9], [9, 9], [7, 15], [12, 7], [9, 7]], 'goldLight');
  } else if (spec.id === 'bleed') {
    pixel.poly([[8, 2], [13, 9], [13, 13], [10, 16], [6, 16], [3, 13], [3, 10]], 'red'); pixel.poly([[8, 4], [8, 10], [6, 13], [4, 12], [5, 9]], 'coral'); pixel.rect(5, 10, 1, 2, 'rose'); pixel.line(12, 3, 15, 6, 'silver'); pixel.line(13, 2, 16, 5, 'silver');
  } else if (spec.id === 'shadow_blind') {
    pixel.poly([[1, 8], [5, 4], [12, 4], [16, 8], [12, 12], [5, 12]], 'purple'); pixel.poly([[3, 8], [6, 6], [11, 6], [14, 8], [11, 10], [6, 10]], 'lilac'); pixel.rect(7, 6, 3, 5, 'void'); pixel.line(3, 14, 13, 3, 'ink', 2); pixel.line(4, 14, 14, 3, 'purpleDark');
  } else if (spec.id === 'tremor') {
    pixel.poly([[7, 3], [11, 3], [13, 8], [11, 12], [7, 12], [5, 8]], 'gold'); pixel.rect(7, 4, 3, 2, 'goldLight'); pixel.line(2, 5, 1, 8, 'cream'); pixel.line(1, 8, 3, 11, 'cream'); pixel.line(15, 5, 16, 8, 'cream'); pixel.line(16, 8, 14, 11, 'cream'); pixel.line(3, 14, 8, 14, 'slate'); pixel.line(8, 14, 10, 16, 'slate'); pixel.line(10, 16, 13, 14, 'slate');
  } else if (spec.id === 'armor_shred') {
    pixel.poly([[3, 2], [13, 2], [14, 9], [8, 15], [2, 9]], 'steel'); pixel.poly([[4, 3], [8, 3], [8, 12], [4, 8]], 'silver'); pixel.poly([[10, 2], [7, 7], [10, 8], [7, 15], [12, 7], [9, 6], [12, 2]], 'redDark'); pixel.line(12, 11, 16, 13, 'coral'); pixel.rect(14, 5, 2, 2, 'silver');
  }
  pixel.outline(); return pixel;
}
module.exports = { drawEffect };
