const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');
const root = path.resolve(__dirname, '../../..');
const families = {
  METAL: ['金属类', 'Metal', ['minecraft:iron_ingot', 'minecraft:copper_ingot', 'minecraft:gold_ingot']],
  STONE: ['石材类', 'Stone', ['minecraft:cobblestone', 'minecraft:obsidian', 'minecraft:stone']],
  CRYSTAL: ['晶矿类', 'Crystal', ['minecraft:diamond', 'minecraft:amethyst_shard', 'minecraft:quartz']],
  NATURAL: ['草木类', 'Plant', ['minecraft:stick', 'ftboss:tea_material', 'ftboss:apple_material']],
  FLEXIBLE: ['韧性类', 'Flexible', ['minecraft:leather', 'minecraft:bone', 'ftboss:butcher_material']],
  ENERGY: ['能源类', 'Energy', ['minecraft:coal', 'minecraft:redstone', 'minecraft:blaze_rod']]
};
const names = {
  FORGED_FRAME: ['锻铸骨架', 'Forged Frame'],
  HONED_INLAY: ['磨砺镶嵌', 'Honed Inlay'],
  BALANCED_FRAME: ['轻衡框架', 'Balanced Frame'],
  SUPPLE_BINDING: ['柔韧缠结', 'Supple Binding'],
  CONDUCTIVE_CORE: ['导能接点', 'Conductive Core'],
  MINERAL_RESONANCE: ['矿晶共振', 'Mineral Resonance'],
  ROOTED_FRAME: ['植根支架', 'Rooted Frame'],
  PADDED_GUARD: ['缓冲护架', 'Padded Guard'],
  LIVING_FOCUS: ['生息聚焦', 'Living Focus'],
  PRISMATIC_CHANNEL: ['棱光导流', 'Prismatic Channel'],
  FIELD_BINDING: ['田野扎束', 'Field Binding'],
  KINDLED_GRIP: ['燃芯握持', 'Kindled Grip']
};
function resources() {
  const utility = path.join(root, 'src/main/java/net/farmingtales/ftboss/util');
  const familySource = fs.readFileSync(path.join(utility, 'CommonMaterialBondRules.java'), 'utf8');
  const linkSource = fs.readFileSync(path.join(utility, 'SwordCavityRules.java'), 'utf8');
  const baseline = path.join(root, '_work/common-material-bonds-20261025/baseline.jar');
  const javap = path.join(process.env.APPDATA, '.minecraft/runtime/java-runtime-gamma-snapshot/bin/javap.exe');
  const enumeration = execFileSync(javap, ['-classpath', baseline, '-p', 'net.farmingtales.ftboss.util.FoodWeaponBuildRules$Material'],
    { encoding: 'utf8', windowsHide: true });
  const available = new Set([...enumeration.matchAll(/\$Material ([A-Z_]+);/g)].map(match => match[1]));
  const members = Object.fromEntries([...familySource.matchAll(/\b([A-Z]+)\(("[\s\S]*?)\)[,;]/g)]
    .map(match => [match[1], [...match[2].matchAll(/"([A-Z_]+)"/g)].map(member => member[1]).filter(name => available.has(name))]));
  const recipes = [...familySource.matchAll(/case "([A-Z_]+)" -> pair\(Family\.([A-Z]+), Family\.([A-Z]+)\)/g)]
    .map(match => ({ name: match[1], id: match[1].toLowerCase(), groups: [match[2], match[3]] }));
  if (recipes.length !== 12 || Object.keys(members).length !== 6) throw new Error('Unexpected common bond catalog');
  const lang = {};
  for (const [languageIndex, language] of ['zh_cn', 'en_us'].entries()) {
    const source = JSON.parse(fs.readFileSync(path.join(root, `src/main/resources/assets/ftboss/lang/${language}.json`)));
    const result = {};
    for (const [family, definition] of Object.entries(families)) {
      result[`guide.ftboss.bonds.group.common_${family.toLowerCase()}`] = definition[languageIndex];
    }
    for (const recipe of recipes) {
      const tuple = new RegExp(`\\b${recipe.name}\\(([^)]+)\\)`).exec(linkSource);
      const bonuses = tuple[1].split(',').map(Number);
      if (bonuses[0] !== 0) throw new Error('Common bonds must not alter the durability contract');
      const effect = languageIndex === 0
        ? [bonuses[1] && `损耗系数 −${Math.round(-bonuses[1] * 100)}%`,
          bonuses[2] && `伤害系数 +${Math.round(bonuses[2] * 100)}%`,
          bonuses[3] && `攻速系数 +${Math.round(bonuses[3] * 100)}%`,
          bonuses[4] && `触及距离 +${bonuses[4].toFixed(2)}`].filter(Boolean).join('；') + '。'
        : [bonuses[1] && `Wear coefficient -${Math.round(-bonuses[1] * 100)}%`,
          bonuses[2] && `damage coefficient +${Math.round(bonuses[2] * 100)}%`,
          bonuses[3] && `attack-speed coefficient +${Math.round(bonuses[3] * 100)}%`,
          bonuses[4] && `reach +${bonuses[4].toFixed(2)}`].filter(Boolean).join('; ') + '.';
      const categories = recipe.groups.map(family => families[family][languageIndex]);
      const choices = recipe.groups.map((family, index) => {
        const materialNames = members[family].map(material => {
          const key = `material.ftboss.weapon.${material.toLowerCase()}`;
          if (!source[key]) throw new Error('Missing material translation: ' + key);
          return source[key];
        }).join(languageIndex === 0 ? '、' : ', ');
        return languageIndex === 0 ? `【${categories[index]}】${materialNames}，任选一种 ×1`
          : `${categories[index]}: any one of ${materialNames} x1`;
      });
      const title = names[recipe.name][languageIndex] + (languageIndex === 0 ? '（入门）' : ' (Basic)');
      result[`link.ftboss.sword.${recipe.id}`] = title;
      result[`link.ftboss.sword.detail.${recipe.id}`] = categories.join(' + ') + '：' + effect;
      result[`guide.ftboss.bonds.${recipe.id}.title`] = title;
      result[`guide.ftboss.bonds.${recipe.id}.requirement`] = choices.join(languageIndex === 0 ? ' ＋ ' : ' + ') + '。';
      result[`guide.ftboss.bonds.${recipe.id}.effect`] = effect;
      result[`guide.ftboss.bonds.${recipe.id}.note`] = languageIndex === 0
        ? '入门弱加成；同一武器结构格中，两类各实装一个已解锁且功能生效的配件即可。构筑须可用；原生参考件、背包和食物槽不计。同名羁绊只结算一次，可与其他羁绊并存。'
        : 'A small basic bonus. Install one functional accessory from each category in unlocked structural slots on the same usable weapon. Native references, inventory and food sockets do not count. Each named bond applies once and can coexist with other bonds.';
    }
    result['guide.ftboss.bonds.intro.status'] = languageIndex === 0
      ? '已激活的羁绊与实际效果，可在“羁绊·倍率·联动”中查看。新增12种入门羁绊，按金属、石材、晶矿、草木、韧性、能源六类组合；两类各装一个生效配件即可。入门效果为伤害/攻速系数 +2%～3%、损耗系数 −2%～3% 或触及 +0.04。原高级配方和强度不变。'
      : 'View active bonds and effects in Bonds, multipliers and synergies. Twelve new basic bonds pair metal, stone, crystal, plant, flexible and energy accessories. Install one functional accessory from each required category. Basic bonuses grant +2-3% damage/speed coefficient, -2-3% wear coefficient, or +0.04 reach. Advanced recipes and strength are unchanged.';
    lang[language] = result;
  }
  const guide = recipes.map(recipe => ({ tab: 1, id: recipe.id,
    groups: recipe.groups.map(family => ({ label: `common_${family.toLowerCase()}`, count: 1, items: families[family][2] })),
    weapons: ['sword', 'bow', 'crossbow', 'staff', 'axe', 'trident'] }));
  return { lang, guide };
}
module.exports = resources;
if (require.main === module) {
  const generated = resources();
  let patch = '*** Begin Patch\n';
  for (const [language, additions] of Object.entries(generated.lang)) {
    const relative = `src/main/resources/assets/ftboss/lang/${language}.json`;
    const source = fs.readFileSync(path.join(root, relative), 'utf8').trimEnd();
    const lines = source.split('\n');
    const existing = JSON.parse(source);
    const missing = {};
    let updates = '';
    for (const [key, value] of Object.entries(additions)) {
      if (!(key in existing)) missing[key] = value;
      else if (existing[key] !== value) {
        const oldLine = lines.find(line => line.trimStart().startsWith(JSON.stringify(key) + ':'));
        const suffix = oldLine.endsWith(',') ? ',' : '';
        updates += `@@\n-${oldLine}\n+  ${JSON.stringify(key)}: ${JSON.stringify(value)}${suffix}\n`;
      }
    }
    if (Object.keys(missing).length) {
      const last = lines[lines.length - 2];
      const entries = JSON.stringify(missing, null, 2).split('\n').slice(1, -1);
      updates += `@@\n-${last}\n-}\n+${last},\n${entries.map(line => '+' + line).join('\n')}\n+}\n`;
    }
    if (updates) patch += `*** Update File: ${relative}\n${updates}`;
  }
  const relative = 'src/main/resources/assets/ftboss/weapon_bonds/guide.json';
  const original = JSON.parse(fs.readFileSync(path.join(root, relative)));
  const missing = [];
  let updates = '';
  for (const entry of generated.guide) {
    const old = original.find(candidate => candidate.id === entry.id);
    if (!old) missing.push(entry);
    else if (JSON.stringify(old) !== JSON.stringify(entry)) {
      const indent = value => {
        const lines = JSON.stringify(value, null, 2).split('\n').map(line => '  ' + line);
        if (original.indexOf(old) < original.length - 1) lines[lines.length - 1] += ',';
        return lines;
      };
      updates += `@@\n${indent(old).map(line => '-' + line).join('\n')}\n${indent(entry).map(line => '+' + line).join('\n')}\n`;
    }
  }
  if (missing.length) {
    const entries = JSON.stringify(missing, null, 2).split('\n').slice(1, -1);
    updates += `@@\n-  }\n-]\n+  },\n${entries.map(line => '+' + line).join('\n')}\n+]\n`;
  }
  if (updates) patch += `*** Update File: ${relative}\n${updates}`;
  patch += '*** End Patch\n';
  process.stdout.write(patch);
}
