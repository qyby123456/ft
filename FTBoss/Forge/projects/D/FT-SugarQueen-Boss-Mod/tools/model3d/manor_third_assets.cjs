const { makeCanvas } = require('./lib_itemtex.cjs');

const text = [
  ['星偶巡礼', 'Effigy Procession', '在锁定位置布置三尊星偶，0.6秒后开始3次范围脉冲，伤害并虚弱敌人。', 'Place three effigies at the locked point; after 0.6s they pulse three times, damaging and weakening enemies.'],
  ['烛月蚀魂', 'Candle Eclipse', '烛月预警1.2秒后收割3.5格内敌人，回复实际生命伤害的15%，最多6点。', 'After a 1.2s warning, reap enemies within 3.5 blocks and heal for 15% of health damage, capped at 6.'],
  ['棱晶炮阵', 'Prism Battery', '三棱晶炮台锁定4格区域，0.6秒后齐射3轮，造成伤害并标记发光。', 'Lock a 4-block area with three prism turrets; after 0.6s fire three damaging volleys that mark enemies.'],
  ['赤刃裁决', 'Blood Verdict', '巨刃悬停预警1秒后落下，重创锁定区域并施加4秒护甲撕裂。', 'After a 1s warning a greatblade strikes the locked area, dealing damage and shredding armor for 4s.'],
  ['柠日焰轮', 'Citrus Nova', '锁定区域浮现柠檬焰轮，0.6秒后炸开，伤害、点燃并击退4格内敌人。', 'A citrus wheel bursts after 0.6s, damaging, igniting and repelling enemies within 4 blocks.'],
  ['碧潮莲华', 'Tidal Lotus', '水莲净化自身4格内的自己、队友与宠物：灭火、解毒、回复8点生命，附带水下呼吸和抗性。', 'Cleanse yourself and allies within 4 blocks: extinguish, remove poison, heal 8 health and grant water breathing and resistance.'],
  ['磁暴天仪', 'Magnetic Storm', '锁定4格区域，0.6秒后产生4次磁暴，伤害、轻微牵引并减速敌人。', 'After 0.6s, pulse four magnetic storms over a locked 4-block area, damaging, gently pulling and slowing enemies.'],
  ['永夜交锋', 'Nightfall Blades', '四柄影刃在锁定区域交叉斩击两次，首击延迟0.6秒，两击相隔1秒，并施加虚弱。', 'Four shadow blades cross twice at the locked point, after 0.6s and 1s later, damaging and weakening enemies.'],
  ['炽羽天降', 'Seraph Descent', '翼光长枪悬停预警1秒后坠落，伤害、击退并标记4格内敌人。', 'After a 1s warning, a winged spear descends, damaging, repelling and marking enemies within 4 blocks.'],
  ['霜刻时钟', 'Winter Clock', '0.6秒后冰钟连续敲响3次，冻结并伤害锁定区域敌人，减速逐次增强至III级。', 'After 0.6s, ring the frost clock three times, damaging and freezing enemies with slowness increasing to III.'],
  ['归羽结界', 'Feather Refuge', '为自身4格内的自己、队友和宠物赋予伤害吸收、缓降与生命恢复；麻雀专属支援法术。', 'Grant yourself, allies and owned pets within 4 blocks absorption, slow falling and regeneration; Maque\'s support spell.']
];

function icon(spell) {
  const canvas = makeCanvas(64, 64);
  const accent = [spell.color >> 16 & 255, spell.color >> 8 & 255, spell.color & 255];
  const light = accent.map(value => Math.round(155 + value * 0.39));
  const shade = accent.map(value => Math.round(value * 0.48));
  const dark = [12, 16, 30];
  function dot(centerX, centerY, radius, color) {
    for (let pixelY = Math.floor(centerY - radius); pixelY <= centerY + radius; pixelY++) {
      for (let pixelX = Math.floor(centerX - radius); pixelX <= centerX + radius; pixelX++) {
        if (Math.hypot(pixelX - centerX, pixelY - centerY) <= radius) canvas.put(pixelX, pixelY, color);
      }
    }
  }
  function line(startX, startY, endX, endY, width = 1, color = accent) {
    const count = Math.max(1, Math.ceil(Math.hypot(endX - startX, endY - startY) * 2));
    for (let step = 0; step <= count; step++) dot(startX + (endX - startX) * step / count, startY + (endY - startY) * step / count, width, color);
  }
  function arc(centerX, centerY, radius, start = 0, end = Math.PI * 2, width = 1, color = accent) {
    for (let step = 0; step <= 100; step++) {
      const angle = start + (end - start) * step / 100;
      dot(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius, width, color);
    }
  }
  function polygon(points, color) {
    for (let pixelY = 0; pixelY < 64; pixelY++) for (let pixelX = 0; pixelX < 64; pixelX++) {
      let inside = false;
      for (let current = 0, previous = points.length - 1; current < points.length; previous = current++) {
        const [firstX, firstY] = points[current];
        const [secondX, secondY] = points[previous];
        if ((firstY > pixelY) !== (secondY > pixelY) && pixelX < (secondX - firstX) * (pixelY - firstY) / (secondY - firstY) + firstX) inside = !inside;
      }
      if (inside) canvas.put(pixelX, pixelY, color);
    }
  }
  function gem(centerX, centerY, width, height) {
    polygon([[centerX, centerY - height], [centerX + width, centerY], [centerX, centerY + height], [centerX - width, centerY]], accent);
    polygon([[centerX, centerY - height], [centerX, centerY + height], [centerX - width, centerY]], light);
    line(centerX, centerY - height, centerX + width, centerY, 0.6, shade);
  }
  function star(centerX, centerY, size) {
    polygon([[centerX, centerY - size], [centerX + 1.5, centerY - 1.5], [centerX + size, centerY], [centerX + 1.5, centerY + 1.5], [centerX, centerY + size], [centerX - 1.5, centerY + 1.5], [centerX - size, centerY], [centerX - 1.5, centerY - 1.5]], light);
  }
  dot(31.5, 31.5, 31, shade);
  dot(31.5, 31.5, 28.5, dark);
  arc(32, 32, 25, -2.6, -0.6, 0.6, shade);
  arc(32, 32, 25, 0.6, 2.6, 0.6, shade);
  switch (spell.id) {
    case 'effigy_procession':
      star(32, 12, 6);
      for (const [centerX, centerY] of [[17, 32], [32, 27], [47, 32]]) {
        line(32, 15, centerX, centerY - 6, 0.6, shade);
        dot(centerX, centerY, 4, light);
        polygon([[centerX - 3, centerY + 6], [centerX + 3, centerY + 6], [centerX + 6, centerY + 17], [centerX - 6, centerY + 17]], accent);
        line(centerX - 6, centerY + 9, centerX + 6, centerY + 9, 1, light);
        dot(centerX, centerY + 10, 1, light);
      }
      break;
    case 'candle_eclipse':
      dot(33, 24, 15, accent); dot(39, 20, 13, dark); arc(33, 24, 16, 1.2, 4.1, 0.7, light);
      for (const [centerX, centerY] of [[17, 43], [31, 48], [47, 42]]) {
        line(centerX, centerY - 2, centerX, centerY + 6, 2, light); gem(centerX, centerY - 8, 3, 5);
      }
      star(43, 18, 4);
      break;
    case 'prism_battery':
      for (const [centerX, centerY] of [[17, 22], [47, 22], [32, 44]]) {
        line(centerX, centerY + 3, 32, 31, 1.2, light);
        gem(centerX, centerY, 5, 9);
        line(centerX - 5, centerY + 10, centerX, centerY + 4, 1, shade);
        line(centerX + 5, centerY + 10, centerX, centerY + 4, 1, shade);
      }
      star(32, 30, 6);
      break;
    case 'blood_verdict':
      polygon([[27, 24], [37, 24], [35, 42], [32, 50], [28, 42]], accent);
      polygon([[27, 24], [32, 24], [32, 49], [28, 42]], light);
      line(22, 23, 42, 23, 2, shade); line(24, 22, 40, 22, 1, light);
      line(32, 12, 32, 22, 2, accent); gem(32, 11, 3, 4);
      for (const side of [-1, 1]) { line(32, 50, 32 + side * 18, 47, 1, accent); line(32 + side * 10, 48, 32 + side * 16, 39, 0.7, light); }
      break;
    case 'citrus_nova':
      for (let index = 0; index < 8; index++) {
        const angle = index * Math.PI / 4;
        line(32 + Math.cos(angle) * 18, 33 + Math.sin(angle) * 18, 32 + Math.cos(angle + 0.1) * 24, 33 + Math.sin(angle + 0.1) * 24, 2, shade);
      }
      dot(32, 33, 16, accent); dot(32, 33, 12, shade);
      for (let index = 0; index < 8; index++) { const angle = index * Math.PI / 4; line(32, 33, 32 + Math.cos(angle) * 11, 33 + Math.sin(angle) * 11, 1, light); }
      arc(32, 33, 14, 0, 6.3, 1, light); polygon([[33, 15], [40, 8], [49, 9], [43, 15]], [116, 192, 93]);
      break;
    case 'tidal_lotus':
      for (const offset of [-1, 1]) {
        polygon([[32, 45], [32 + offset * 23, 34], [32 + offset * 19, 19], [32 + offset * 6, 28]], shade);
        polygon([[32, 45], [32 + offset * 15, 31], [32 + offset * 10, 15], [32, 27]], accent);
      }
      polygon([[32, 45], [24, 29], [32, 10], [40, 29]], light);
      gem(32, 31, 3, 7); arc(32, 35, 18, 0.15, 3, 1, accent); line(14, 52, 50, 52, 1, shade);
      break;
    case 'magnetic_storm':
      arc(32, 32, 20, 0.2, 2.8, 2, accent); arc(32, 32, 20, 3.3, 6, 2, shade);
      polygon([[35, 10], [21, 34], [31, 32], [27, 54], [44, 26], [34, 28]], light);
      for (const [centerX, centerY] of [[14, 22], [48, 42], [18, 47], [47, 15]]) gem(centerX, centerY, 3, 4);
      break;
    case 'nightfall_blades':
      arc(32, 32, 20, 0, 6.3, 1, shade);
      for (const side of [-1, 1]) {
        polygon([[32 + side * 19, 12], [32 + side * 7, 15], [32 - side * 9, 40], [32 - side * 4, 42]], accent);
        line(32 + side * 18, 13, 32 - side * 6, 40, 1, light);
        line(32 - side * 14, 38, 32 + side * 1, 46, 1.5, shade);
        line(32 - side * 7, 43, 32 - side * 13, 51, 2, light);
      }
      star(32, 29, 4);
      break;
    case 'seraph_descent':
      for (const side of [-1, 1]) for (let feather = 0; feather < 4; feather++) {
        polygon([[32 + side * 5, 31], [32 + side * (10 + feather * 4), 12 + feather * 3], [32 + side * (10 + feather * 3), 29 + feather * 4]], feather % 2 ? light : accent);
      }
      line(32, 18, 32, 48, 1.5, light); gem(32, 47, 4, 7); arc(32, 14, 8, 0, 6.3, 1.1, accent);
      break;
    case 'winter_clock':
      arc(32, 31, 19, 0, 6.3, 2, accent); arc(32, 31, 16, 0, 6.3, 0.6, light);
      for (let index = 0; index < 12; index++) { const angle = index * Math.PI / 6; line(32 + Math.cos(angle) * 12, 31 + Math.sin(angle) * 12, 32 + Math.cos(angle) * 15, 31 + Math.sin(angle) * 15, 0.7, light); }
      line(32, 31, 32, 20, 1, light); line(32, 31, 40, 36, 1.5, accent); dot(32, 31, 2, light);
      for (const centerX of [18, 32, 46]) gem(centerX, 50, 3, 5);
      break;
    case 'feather_refuge':
      for (const side of [-1, 1]) {
        polygon([[32, 12], [32 + side * 18, 20], [32 + side * 22, 35], [32 + side * 16, 50], [32 + side * 8, 39]], shade);
        line(32 + side * 16, 49, 32 + side * 13, 26, 1, light);
        line(32 + side * 13, 26, 32, 12, 1, light);
        for (let barb = 0; barb < 5; barb++) line(32 + side * 14, 26 + barb * 4, 32 + side * (4 + barb * 2), 26 + barb * 4, 1.5, accent);
      }
      polygon([[32, 26], [39, 30], [37, 39], [32, 44], [27, 39], [25, 30]], light);
      star(32, 34, 5); dot(32, 34, 2, shade);
      break;
    default: throw new Error(`Unknown third spell ${spell.id}`);
  }
  for (const centerX of [27, 32, 37]) dot(centerX, 57, 1, light);
  return canvas;
}

module.exports = { text, icon };
