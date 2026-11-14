const fs = require('node:fs');
const path = require('node:path');
const { Canvas } = require('./png.cjs');

function write(file, value) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, JSON.stringify(value, null, 2) + '\n');
}

function update(resources) {
  const assets = path.join(resources, 'assets/ftboss');
  const soundsFile = path.join(assets, 'sounds.json');
  const sounds = fs.existsSync(soundsFile) ? JSON.parse(fs.readFileSync(soundsFile, 'utf8')) : {};
  for (const suffix of ['', '_p2']) {
    for (const prefix of ['boss_music_', 'music_disc_']) {
      const id = prefix + 'tide_fish' + suffix;
      sounds[id] = { subtitle: 'ftboss.subtitle.' + id, sounds: [{ name: 'ftboss:music/tide_fish' + suffix, stream: true }] };
    }
    const id = 'music_disc_tide_fish' + suffix;
    write(path.join(assets, 'models/item/' + id + '.json'), { parent: 'minecraft:item/generated', textures: { layer0: 'ftboss:item/' + id } });
    const canvas = new Canvas(32, 32);
    const label = suffix ? [187, 91, 150] : [55, 177, 187];
    for (let pixelY = 0; pixelY < 32; pixelY++) for (let pixelX = 0; pixelX < 32; pixelX++) {
      const radius = Math.hypot(pixelX - 15.5, pixelY - 15.5);
      if (radius > 14.5 || radius < 1.7) continue;
      const groove = Math.floor(radius * 1.8) % 2;
      const colour = radius < 6 ? label : radius < 6.8 ? [231, 185, 84] : [24 + groove * 11, 32 + groove * 11, 46 + groove * 14];
      canvas.set(pixelX, pixelY, ...colour, 1);
    }
    canvas.rect(13, 10, 6, 2, 244, 210, 111, 1);
    canvas.rect(13, 8, 1, 3, 244, 210, 111, 1);
    canvas.rect(15, 7, 2, 4, 244, 210, 111, 1);
    canvas.rect(18, 8, 1, 3, 244, 210, 111, 1);
    fs.mkdirSync(path.join(assets, 'textures/item'), { recursive: true });
    canvas.save(path.join(assets, 'textures/item/' + id + '.png'));
  }
  write(soundsFile, sounds);
  const tagFile = path.join(resources, 'data/minecraft/tags/items/music_discs.json');
  const tag = fs.existsSync(tagFile) ? JSON.parse(fs.readFileSync(tagFile, 'utf8')) : { replace: false, values: [] };
  for (const suffix of ['', '_p2']) {
    const id = 'ftboss:music_disc_tide_fish' + suffix;
    if (!tag.values.includes(id)) tag.values.push(id);
  }
  write(tagFile, tag);
  const counted = (name, minimum, maximum, looting = false) => ({ rolls: 1, entries: [{ type: 'minecraft:item', name,
    functions: [{ function: 'minecraft:set_count', count: { type: 'minecraft:uniform', min: minimum, max: maximum } },
      ...(looting ? [{ function: 'minecraft:looting_enchant', count: { type: 'minecraft:uniform', min: 0, max: 1 } }] : [])] }] });
  write(path.join(resources, 'data/ftboss/loot_tables/entities/tide_fish_boss.json'), {
    type: 'minecraft:entity', pools: [counted('ftboss:tide_pearl', 4, 8, true), counted('minecraft:cooked_salmon', 8, 12),
      counted('minecraft:prismarine_crystals', 2, 5, true),
      { rolls: 1, entries: ['', '_p2'].map(suffix => ({ type: 'minecraft:item', name: 'ftboss:music_disc_tide_fish' + suffix, weight: 1 })) },
      { rolls: 1, conditions: [{ condition: 'minecraft:random_chance_with_looting', chance: 0.2, looting_multiplier: 0.05 }],
        entries: [{ type: 'minecraft:item', name: 'minecraft:heart_of_the_sea' }] }]
  });
  const translations = {
    zh_cn: {
      'message.ftboss.tide_geyser': '潮宴鱼王：潮柱裁决！三道水柱依次爆发，离开标记圈！',
      'message.ftboss.tide_sweep_p2': '炙潮·双重摆尾！前扫后回身，别急着贴近！',
      'message.ftboss.tide_volley_p2': '炙潮·鱼骨连珠！三轮七发扇形齐射，持续横移！',
      'message.ftboss.tide_dash_p2': '炙潮·折返冲锋！冲过之后会沿原路折返！',
      'message.ftboss.tide_summon_p2': '炙潮·精锐御膳！鱼卫获得加速30秒、抗性10秒！',
      'message.ftboss.tide_wave_p2': '炙潮·双重浪环！连续两圈，注意第二次起跳！',
      'message.ftboss.tide_maelstrom_p2': '炙潮·三涡沸海！三个锁定圈依次爆发，绕出危险区！',
      'message.ftboss.tide_geyser_p2': '炙潮·五柱裁决！五道潮柱接力爆发，不要踩预警圈！',
      'item.ftboss.music_disc_tide_fish': '音乐唱片', 'item.ftboss.music_disc_tide_fish.desc': '沧鳞·潮宴',
      'item.ftboss.music_disc_tide_fish_p2': '音乐唱片', 'item.ftboss.music_disc_tide_fish_p2.desc': '沧鳞·怒潮'
    },
    en_us: {
      'message.ftboss.tide_geyser': 'Tidal Judgment! Three marked geysers erupt in sequence!',
      'message.ftboss.tide_sweep_p2': 'Searing Tide: double tail sweep! Stay back for the return swing!',
      'message.ftboss.tide_volley_p2': 'Searing Tide: three seven-bolt volleys! Keep moving sideways!',
      'message.ftboss.tide_dash_p2': 'Searing Tide: returning charge! The Sovereign doubles back along the lane!',
      'message.ftboss.tide_summon_p2': 'Searing Tide: elite guards! Speed for 30s, resistance for 10s!',
      'message.ftboss.tide_wave_p2': 'Searing Tide: twin waves! Save another jump for the second ring!',
      'message.ftboss.tide_maelstrom_p2': 'Searing Tide: three boiling maelstroms! Leave the locked circles!',
      'message.ftboss.tide_geyser_p2': 'Searing Tide: five-pillar judgment! Avoid every marked circle!',
      'item.ftboss.music_disc_tide_fish': 'Music Disc', 'item.ftboss.music_disc_tide_fish.desc': 'Canglin - Royal Tide',
      'item.ftboss.music_disc_tide_fish_p2': 'Music Disc', 'item.ftboss.music_disc_tide_fish_p2.desc': 'Canglin - Raging Tide'
    }
  };
  for (const [locale, entries] of Object.entries(translations)) {
    for (const suffix of ['', '_p2']) for (const prefix of ['boss_music_', 'music_disc_']) {
      entries['ftboss.subtitle.' + prefix + 'tide_fish' + suffix] = entries['item.ftboss.music_disc_tide_fish' + suffix + '.desc'];
    }
    const file = path.join(assets, 'lang', locale + '.json');
    const current = fs.existsSync(file) ? JSON.parse(fs.readFileSync(file, 'utf8')) : {};
    write(file, { ...current, ...entries });
  }
  console.log('Tide fish: phase hints, two music events/discs, balanced loot and music subtitles.');
}

module.exports = { update };
if (require.main === module) update(path.resolve(process.argv[2] || path.join(__dirname, '../../src/main/resources')));
