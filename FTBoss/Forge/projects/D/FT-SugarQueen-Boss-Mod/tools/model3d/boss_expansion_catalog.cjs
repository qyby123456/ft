const spells = [
  ['sugar_hail', 0xA9DCFF, '糖晶雹雨', 'Sugar Hail', '落点半径4格连续降下3轮冰雹，每轮伤害并积累冰冻、施加缓慢。', 'Three icy pulses in a 4-block field; each hit builds frost and slows enemies.'],
  ['glacial_prism', 0xCEF6FF, '霜棱镜甲', 'Glacial Prism', '获得8秒护盾与4秒抗性；冰冻并减速3格内敌人，不造成直接伤害。', 'Gain an 8-second shield and 4 seconds of resistance; freeze and slow nearby enemies.'],
  ['royal_shatter', 0xAFAAFF, '王冠碎冰', 'Royal Shatter', '落点预警1秒后碎裂；消耗敌人的冰冻，令本次伤害提高50%，并击退。', 'After a 1-second warning, shatter the area; consume frost for 50% bonus damage and knockback.'],
  ['briar_procession', 0x85C77C, '荆棘巡行', 'Briar Procession', '沿锁定方向4、8、12格依次刺出荆棘；每名敌人仅受击一次，并被强烈减速。', 'Thorns advance 4, 8 and 12 blocks along a locked direction; each enemy is hit once and heavily slowed.'],
  ['scarlet_pollen', 0xED92B0, '绯红花粉', 'Scarlet Pollen', '落点留下4轮花粉脉冲；命中施加中毒与虚弱，持续约3秒。', 'Four pollen pulses over 3 seconds poison and weaken enemies in the target area.'],
  ['camellia_rebirth', 0xACE5B1, '山茶再生', 'Camellia Rebirth', '连续3次治疗自身及5格内同盟，每次移除中毒；显示数值为每次治疗量。', 'Heal yourself and allies within 5 blocks three times, cleansing poison; the stat is healing per pulse.'],
  ['flesh_bloom', 0xC9566E, '血肉绽放', 'Flesh Bloom', '血学派：落点预警1秒后血肉花瓣撕裂敌人；汲取实际伤害的20%，每次最多治疗6点。', 'Blood school: after a 1-second warning, rend the area and drain 20% of actual damage, healing at most 6.'],
  ['sanguine_suture', 0xEF6475, '血线缝合', 'Sanguine Suture', '血学派：连接18格内单一敌人，共汲取3次；每次按实际伤害30%治疗，最多4点；遮挡或离开范围即断线。', 'Blood school: tether one enemy within 18 blocks for three drains; heal 30% of actual damage, capped at 4 per pulse. Range or cover breaks the tether.'],
  ['cinder_breath', 0xFFB052, '烈酿吐息', 'Cinder Breath', '朝锁定方向喷出3轮10格锥形火息，点燃敌人3秒；不会点燃方块。', 'Three fire-breath pulses in a locked 10-block cone ignite enemies for 3 seconds without igniting blocks.'],
  ['intoxicating_toast', 0xEDC46F, '醉勇祝酒', 'Intoxicating Toast', '获得8秒护盾、6秒力量与速度，强化贴身战斗；不造成直接伤害。', 'Gain an 8-second shield plus 6 seconds of strength and speed for close combat.'],
  ['vintage_minefield', 0xF77844, '陈酿雷区', 'Vintage Minefield', '落点周围3处酒印预警后依次爆燃；小范围伤害、点燃与击退，不破坏地形或掉落物。', 'Three marked casks around the aim point burst in sequence, burning and pushing enemies without harming terrain or dropped items.'],
  ['ion_rail', 0x7FEBF5, '离子轨道炮', 'Ion Rail', '发射24格贯穿射线，最多命中4人；后续目标每次递减15%伤害，命中施加发光，墙体阻挡。', 'Fire a wall-stopped 24-block rail through up to four enemies; damage falls 15% per target and marks them glowing.'],
  ['static_lattice', 0xAB91FF, '静电囚网', 'Static Lattice', '落点电网连续放电3次，施加缓慢与挖掘疲劳；电网不会生成方块。', 'A static grid discharges three times, slowing enemies and applying mining fatigue without placing blocks.'],
  ['emergency_reboot', 0x82F2BC, '紧急重启', 'Emergency Reboot', '治疗自身、清除虚弱和挖掘疲劳并获得5秒抗性；重启代价为自身缓慢1.5秒。', 'Heal, cleanse weakness and mining fatigue, and gain 5 seconds of resistance; reboot slows you for 1.5 seconds.'],
  ['spore_eruption', 0xD3DE69, '孢囊喷发', 'Spore Eruption', '落点预警0.8秒后喷发孢子，造成伤害、向上掀起敌人并施加5秒中毒。', 'After a 0.8-second warning, spores erupt, damage and lift enemies, and poison them for 5 seconds.'],
  ['rotting_requiem', 0xBA85D0, '腐朽挽歌', 'Rotting Requiem', '腐化波由2格扩至6格，每个敌人只命中一次；消耗中毒提高50%伤害，并施加凋零。', 'A rot wave expands from 2 to 6 blocks, hitting each enemy once; consume poison for 50% bonus damage and apply wither.'],
  ['living_mulch', 0x96D772, '活性腐殖', 'Living Mulch', '治疗自身及4格内同盟并清除中毒、凋零；成功净化的同盟额外获得8点护盾。', 'Heal yourself and allies within 4 blocks and cleanse poison and wither; cleansed allies also gain 8 absorption.'],
  ['solar_orchard', 0xFFE9A1, '日轮果园', 'Solar Orchard', '3颗光果环绕自身并连续爆发3轮，伤害3.5格内敌人、轻推并施加发光。', 'Three orbiting fruits pulse three times, damaging enemies within 3.5 blocks, lightly pushing and marking them glowing.'],
  ['judgment_seed', 0xFFE064, '审判种子', 'Judgment Seed', '落点圣种预警1.2秒后爆发；消耗发光令伤害提高50%，命中后施加虚弱。', 'A holy seed detonates after 1.2 seconds; consume glowing for 50% bonus damage and weaken enemies.'],
  ['golden_harvest', 0xD6F093, '金枝收获', 'Golden Harvest', '收割落点敌人；消耗发光提高50%伤害，每个被收割的印记给予4点护盾，每次最多12点。', 'Reap enemies at the aim point; consume glowing for 50% bonus damage and gain 4 absorption per mark, capped at 12.'],
  ['undertow_snare', 0x67B4CE, '暗流牵引', 'Undertow Snare', '落点形成3轮暗流，持续向中心牵引、造成伤害并施加虚弱；不会生成水。', 'Three undertow pulses damage, weaken and pull enemies toward the center without placing water.'],
  ['abyssal_carapace', 0x80E3D9, '深海泡甲', 'Abyssal Carapace', '获得9秒护盾、5秒抗性和10秒水下呼吸，熄灭自身火焰并推开4格内敌人。', 'Gain a 9-second shield, 5 seconds of resistance and 10 seconds of water breathing; extinguish yourself and push nearby enemies away.'],
  ['riptide_corridor', 0x86BEFF, '裂潮长廊', 'Riptide Corridor', '预警1秒后沿锁定方向5、10、15格逐段涌潮；每名敌人只受击一次，被推开、减速并熄火。', 'After a 1-second warning, surges advance 5, 10 and 15 blocks along a locked corridor; each enemy is hit once, pushed, slowed and extinguished.']
];

function paint(index, tools) {
  const { ellipse, line, polygon, arc, accent, bright, dark } = tools;
  const diamond = (centerX, centerY, size, color = bright) => polygon([[centerX, centerY - size], [centerX + size * 0.65, centerY], [centerX, centerY + size], [centerX - size * 0.65, centerY]], color);
  const leaf = (centerX, centerY, color = accent) => { ellipse(centerX, centerY, 8, 4, color); line(centerX - 6, centerY, centerX + 6, centerY, 0.6, bright); };
  const apple = (centerX, centerY, size = 6) => { ellipse(centerX - size * 0.35, centerY, size * 0.7, size, accent); ellipse(centerX + size * 0.35, centerY, size * 0.7, size, bright); line(centerX, centerY - size, centerX + 1, centerY - size - 4, 1, accent); };
  const drop = (centerX, centerY, size = 7) => { polygon([[centerX, centerY - size], [centerX + size, centerY + 3], [centerX, centerY + size], [centerX - size, centerY + 3]], accent); ellipse(centerX - 2, centerY + 1, 2, 3, bright); };
  switch (index) {
    case 0:
      for (const [centerX, centerY] of [[19, 23], [33, 17], [46, 26], [28, 41], [43, 47]]) { diamond(centerX, centerY, 6); line(centerX + 3, centerY - 11, centerX, centerY - 7, 1, accent); }
      break;
    case 1:
      polygon([[32, 9], [51, 21], [47, 43], [32, 55], [17, 43], [13, 21]], accent);
      polygon([[32, 15], [44, 24], [41, 40], [32, 47], [23, 40], [20, 24]], dark);
      diamond(32, 30, 13); line(18, 48, 11, 53, 1, bright); line(46, 48, 53, 53, 1, bright);
      break;
    case 2:
      polygon([[16, 24], [25, 30], [31, 13], [36, 29], [48, 21], [45, 37], [18, 40]], accent);
      line(16, 46, 24, 41, 2, bright); line(34, 43, 42, 48, 2, bright); diamond(51, 43, 6); diamond(11, 18, 5);
      polygon([[33, 23], [28, 33], [35, 32], [29, 44], [39, 31]], dark);
      break;
    case 3:
      line(16, 51, 30, 37, 2, accent); line(30, 37, 24, 26, 2, accent); line(24, 26, 43, 13, 2, accent);
      for (const [centerX, centerY] of [[18, 46], [30, 34], [37, 18]]) polygon([[centerX - 7, centerY + 4], [centerX + 3, centerY - 12], [centerX + 6, centerY + 5]], bright);
      break;
    case 4:
      for (const [centerX, centerY] of [[21, 23], [37, 22], [44, 35], [29, 37], [18, 43]]) { ellipse(centerX, centerY, 8, 7, accent); ellipse(centerX - 2, centerY - 2, 3, 2, bright); }
      for (const [centerX, centerY] of [[13, 14], [49, 17], [34, 51], [11, 33], [50, 49]]) ellipse(centerX, centerY, 2, 2, bright);
      break;
    case 5:
      arc(32, 32, 23, 0.5, 5.8, accent, 2); polygon([[52, 18], [44, 18], [48, 10]], bright);
      line(31, 48, 33, 25, 2, bright); leaf(23, 30); leaf(42, 25); ellipse(33, 17, 5, 6, bright);
      break;
    case 6:
      for (let petal = 0; petal < 7; petal++) { const angle = petal * Math.PI * 2 / 7; ellipse(32 + Math.cos(angle) * 12, 30 + Math.sin(angle) * 12, 8, 9, accent); }
      ellipse(32, 30, 10, 6, bright); ellipse(32, 30, 3, 6, dark); drop(46, 49, 5);
      break;
    case 7:
      arc(33, 25, 15, 2.4, 6.4, bright, 2); line(46, 24, 19, 48, 1.6, accent); drop(17, 19, 7);
      for (const offset of [0, 8, 16]) line(21 + offset, 36 - offset, 29 + offset, 44 - offset, 1.4, bright);
      break;
    case 8:
      polygon([[12, 37], [46, 12], [39, 31], [53, 28], [44, 48], [16, 47]], accent);
      polygon([[19, 39], [36, 27], [34, 40], [44, 39], [34, 48]], bright); ellipse(14, 44, 5, 7, dark);
      break;
    case 9:
      polygon([[13, 17], [28, 22], [24, 35], [14, 31]], accent); polygon([[51, 17], [36, 22], [40, 35], [50, 31]], bright);
      line(19, 33, 16, 46, 2, accent); line(45, 33, 48, 46, 2, bright); line(10, 48, 23, 48, 2, accent); line(41, 48, 54, 48, 2, bright);
      diamond(32, 14, 5); line(28, 40, 36, 40, 1, accent);
      break;
    case 10:
      for (const [centerX, centerY] of [[20, 40], [44, 40], [32, 20]]) { ellipse(centerX, centerY, 7, 9, accent); line(centerX - 6, centerY - 4, centerX + 6, centerY - 4, 1, bright); line(centerX - 6, centerY + 4, centerX + 6, centerY + 4, 1, bright); line(centerX, centerY - 8, centerX + 2, centerY - 13, 1, bright); }
      break;
    case 11:
      line(12, 51, 45, 18, 3, accent); line(12, 51, 45, 18, 1, bright); polygon([[39, 11], [54, 9], [51, 25]], bright);
      for (const offset of [0, 10, 20]) line(12 + offset, 32 - offset, 30 + offset, 50 - offset, 1.5, accent);
      break;
    case 12:
      for (const coordinate of [16, 32, 48]) { line(coordinate, 14, coordinate, 50, 1, accent); line(14, coordinate, 50, coordinate, 1, accent); }
      for (const centerX of [16, 32, 48]) for (const centerY of [16, 32, 48]) diamond(centerX, centerY, 3);
      break;
    case 13:
      arc(32, 33, 19, -0.9, 4, accent, 3); line(32, 12, 32, 33, 3, bright); polygon([[14, 23], [8, 33], [21, 31]], bright);
      line(25, 46, 39, 46, 2, bright); line(32, 40, 32, 52, 2, bright);
      break;
    case 14:
      ellipse(32, 28, 17, 10, accent); line(32, 29, 32, 48, 4, bright); line(13, 51, 51, 51, 2, dark);
      for (const offset of [-15, 0, 15]) { line(32 + offset, 17, 32 + offset * 1.3, 9, 1, accent); ellipse(32 + offset * 1.3, 9, 2, 2, bright); }
      break;
    case 15:
      arc(32, 33, 24, 0.2, 5.6, accent, 1); arc(32, 33, 19, 0.7, 6.2, accent, 1);
      ellipse(32, 28, 12, 12, bright); ellipse(27, 28, 3, 4, dark); ellipse(38, 28, 3, 4, dark);
      polygon([[24, 35], [40, 35], [38, 44], [26, 44]], accent); line(31, 37, 31, 43, 1, dark); line(36, 37, 36, 43, 1, dark);
      break;
    case 16:
      for (const height of [39, 45, 51]) { line(15, height, 48, height - 2, 2, accent); }
      line(32, 39, 32, 17, 2, bright); leaf(22, 23); leaf(42, 17); drop(48, 32, 4);
      break;
    case 17:
      arc(32, 32, 21, 0, Math.PI * 2, accent, 1); apple(32, 13); apple(15, 42); apple(49, 42); diamond(32, 32, 8);
      break;
    case 18:
      line(32, 9, 32, 31, 2, bright); line(23, 13, 23, 26, 1, accent); line(41, 13, 41, 26, 1, accent);
      diamond(32, 38, 12, accent); ellipse(30, 37, 3, 6, bright); arc(32, 45, 17, 0.1, 3, bright, 1);
      break;
    case 19:
      line(16, 53, 39, 21, 2, accent); arc(35, 23, 15, 3, 6.6, bright, 3); apple(26, 34, 9); leaf(42, 45);
      break;
    case 20:
      for (let step = 0; step < 130; step++) { const angle = step * 0.09; const radius = 24 - step * 0.15; ellipse(32 + Math.cos(angle) * radius, 32 + Math.sin(angle) * radius, 1.5, 1.5, accent); }
      line(32, 23, 32, 42, 2, bright); polygon([[24, 34], [32, 45], [40, 34]], bright);
      break;
    case 21:
      arc(32, 32, 24, 0, Math.PI * 2, accent, 2); arc(30, 30, 19, 3.5, 4.7, bright, 2);
      for (const offset of [-12, -6, 0, 6, 12]) line(32, 47, 32 + offset, 24 - (12 - Math.abs(offset)) * 0.5, 1.7, bright);
      arc(32, 33, 14, 3.2, 6.2, accent, 2);
      break;
    case 22:
      for (const height of [23, 34, 45]) { arc(22, height, 9, 3.3, 6.3, accent, 2); line(15, height + 3, 42, height + 3, 2, accent); }
      line(39, 48, 50, 17, 2, bright); polygon([[43, 19], [52, 10], [56, 26]], bright);
      break;
    default: throw new Error('Missing expanded spell icon: ' + index);
  }
}

module.exports = { spells, paint };
