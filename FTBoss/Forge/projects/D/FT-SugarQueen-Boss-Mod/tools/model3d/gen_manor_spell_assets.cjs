const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { makeCanvas } = require('./lib_itemtex.cjs');
const third = require('./manor_third_assets.cjs');
const root = path.resolve(__dirname, '../..');
const owners = ['柚子', '蛋蛋', '朵拉', '功底', '柠檬猫', '青衣', '文玄蓝', '折耳根', '紫菜', '麻雀', '雪黎', '祁崽'];
const ownersEn = ['Youzi', 'Dandan', 'Dora', 'Gongdi', 'Lemon Cat', 'Qingyi', 'Wen Xuanlan', 'Zheergen', 'Zicai', 'Maque', 'Xueli', 'Qizai'];
const text = [
  ['偶像连星', 'Effigy Volley', '向锁定方向连射3枚灵偶光弹，每枚命中一个目标。', 'Fire three effigy bolts toward the locked aim point; each hits one target.'],
  ['灵偶守御', 'Effigy Ward', '为自己赋予伤害吸收和短暂抗性；护盾及持续时间固定为满阶。', 'Gain absorption and resistance; ward and duration are fixed at full power.'],
  ['暮烛收割', 'Candle Reap', '收割前方6格扇区，回复实际造成伤害的15%，每次最多回复6点。', 'Reap a 6-block cone and recover 15% of health damage dealt, capped at 6 health.'],
  ['暮烛囚笼', 'Candle Prison', '在锁定位置脉冲5次，伤害、减速并向中心牵引敌人。', 'Create five damaging pulses that slow enemies and pull them toward the locked point.'],
  ['秘银齐射', 'Arcane Fusillade', '向锁定方向连射4束精准聚能激光；每束命中一个敌人。', 'Fire four precision focus-laser bursts toward the locked point, each hitting one enemy.'],
  ['破阵榴弹', 'Breach Shell', '标记区域1秒后爆破，击退并虚弱敌人；不破坏方块。', 'Detonate the marked area after 1 second, knocking back and weakening enemies without breaking blocks.'],
  ['黑刃裂甲', 'Rending Cleave', '横斩前方扇区，命中后施加3秒护甲撕裂。', 'Cleave a frontal cone and inflict armor shred for 3 seconds on hit.'],
  ['赤誓战意', 'Crimson Oath', '强化自身力量、抗性和移动速度，持续时间固定为满阶。', 'Gain strength, resistance and movement speed; duration is fixed at full power.'],
  ['炽锅翻焰', 'Searing Pan', '翻起前方扇形火浪，命中敌人后点燃4秒。', 'Unleash a frontal fire wave and ignite enemies for 4 seconds on hit.'],
  ['炉心暖宴', 'Hearth Feast', '在目标位置展开4次暖宴脉冲，治疗自己、队友和自己的宠物并补充饱食度。', 'Create four pulses at the aim point, healing yourself, teammates and owned pets and restoring hunger.'],
  ['四海奔流', 'Four Tides', '推出10格宽幅潮浪，击退、减速敌人并熄灭其火焰。', 'Send a broad 10-block tide that knocks back, slows and extinguishes enemies.'],
  ['百药甘霖', 'Elixir Rain', '锁定区域落下4次药雨，伤害敌人并施加中毒与虚弱。', 'Rain four pulses over the locked area, damaging, poisoning and weakening enemies.'],
  ['机轮卫星', 'Orbital Gears', '环刃跟随施法者移动，4次切割周围3格内的敌人。', 'Moving orbital blades slice enemies within 3 blocks of the caster four times.'],
  ['高压贯星', 'Pressure Lance', '释放最远24格的细束贯穿魔法，命中沿线敌人，遇墙停止。', 'Release a narrow piercing beam up to 24 blocks, stopping at solid walls.'],
  ['影缚镇印', 'Shadow Brand', '锁定区域产生3次暗影脉冲，造成伤害、失明和减速。', 'Create three shadow pulses at the locked point, damaging, blinding and slowing enemies.'],
  ['虚空回响', 'Void Echo', '在锁定位置爆发两次，第二次在1.2秒后到来；不绕过无敌保护。', 'Burst twice at the locked point, with the second echo after 1.2 seconds; respects invulnerability.'],
  ['耀环斥退', 'Luminous Rebuke', '光环震退周围5格的敌人并将其标记为发光。', 'Repel enemies within 5 blocks and mark them with glowing.'],
  ['圣环庇所', 'Halo Sanctuary', '在目标位置展开4次光环脉冲，治疗自己、队友和自己的宠物并赋予短暂抗性。', 'Create four sanctuary pulses at the aim point, healing yourself, teammates and owned pets and granting resistance.'],
  ['千羽散华', 'Feather Fan', '射出5路羽刃，命中后标记发光，同一目标每次施法仅受伤一次。', 'Fire five feather rays that mark enemies with glowing; each target can be hit only once per cast.'],
  ['乘羽轻身', 'Wingstep', '获得迅捷、跳跃提升和缓降；持续时间固定为满阶。', 'Gain speed, jump boost and slow falling; duration is fixed at full power.'],
  ['凝霜飞针', 'Frost Needles', '射出3路冰针，命中后冻结并减速，同一目标每次施法仅受伤一次。', 'Fire three frost needles that freeze and slow enemies; each target can be hit only once per cast.'],
  ['白夜禁疗', 'Whiteout Edict', '锁定区域产生4次冰霜脉冲，伤害、减速并施加可见的3秒禁疗效果。', 'Create four frost pulses that damage, slow and apply a visible 3-second healing ban.']
];
text.push(...third.text);
text.push(
  ['星轨十字', 'Astral Cross', '蓄力锁定落点，释放两条交叉星光。每条长12格、宽1.4格，同一敌人只受伤一次；可离开十字区域躲避，不穿墙。', 'Lock a point while charging, then release two crossing star beams, each 12 blocks long and 1.4 blocks wide. Each enemy is hit once; leaving the cross or taking cover avoids damage.'],
  ['绯星连奏', 'Rose Starfall', '蓄力锁定中心及四周共5处落点，释放后每隔0.4秒依次坠星，每处半径1.6格。落点不追踪目标，不穿墙、不破坏地形。', 'Lock five marks at the center and four surrounding points. After charging, stars strike in sequence every 0.4 seconds within 1.6 blocks. Marks do not track targets; walls and terrain are respected.']
);
const rules = fs.readFileSync(path.join(root, 'src/main/java/net/farmingtales/ftboss/util/ManorSpellRules.java'), 'utf8');
const spells = [...rules.matchAll(/\b([A-Z_]+)\((\d+), "([a-z_]+)", "([a-z]+)", (\d+), (\d+), (\d+), (\d+), (0x[\dA-F]+)\)/g)]
  .map((match, index) => ({ kind: match[1], owner: Number(match[2]), id: match[3], school: match[4], damage: Number(match[5]),
    mana: Number(match[6]), cooldown: Number(match[7]), ticks: Number(match[8]), color: Number(match[9]), text: text[index] }));
assert.equal(spells.length, 35);

function languages() {
  const zh = { 'ui.ftboss.manor.pulse_damage': '每次命中 / 脉冲：%s 点法术伤害', 'ui.ftboss.manor.support': '满阶辅助法术' };
  const en = { 'ui.ftboss.manor.pulse_damage': 'Damage per hit / pulse: %s', 'ui.ftboss.manor.support': 'Full-power support spell' };
  owners.forEach((owner, index) => { zh[`ui.ftboss.manor.owner.${index}`] = owner; en[`ui.ftboss.manor.owner.${index}`] = ownersEn[index]; });
  for (const spell of spells) {
    zh[`spell.ftboss.${spell.id}`] = spell.text[0]; en[`spell.ftboss.${spell.id}`] = spell.text[1];
    zh[`spell.ftboss.${spell.id}.effect`] = spell.text[2]; en[`spell.ftboss.${spell.id}.effect`] = spell.text[3];
    zh[`spell.ftboss.${spell.id}.guide`] = `来自${owners[spell.owner]}的独立法术。${spell.text[2]} 击败对应 Boss 可获得卷轴，也可在卷轴撰写台制作，并在法术抄写台抄入法术书。`;
    en[`spell.ftboss.${spell.id}.guide`] = `An independent spell of ${ownersEn[spell.owner]}. ${spell.text[3]} Obtain its scroll from this boss or the Scroll Forge, then inscribe it into a spellbook at the Inscription Table.`;
    if (spell.owner === 11) {
      zh[`spell.ftboss.${spell.id}.guide`] = `祁崽的独立唤魔法术。${spell.text[2]} 通关庄园战后从战利品箱获得卷轴，也可在卷轴撰写台制作，再抄入法术书。卷轴固定为满威力传说一阶。`;
      en[`spell.ftboss.${spell.id}.guide`] = `Qizai's evocation spell. ${spell.text[3]} Obtain the full-power Legendary level-one scroll from the manor victory chest or Scroll Forge, then inscribe it into a spellbook.`;
    }
  }
  return { zh_cn: zh, en_us: en };
}

function icon(spell, second) {
  const canvas = makeCanvas(64, 64);
  const accent = [spell.color >> 16 & 255, spell.color >> 8 & 255, spell.color & 255];
  const light = accent.map(value => Math.min(255, 140 + value * 0.45));
  const shade = accent.map(value => value * 0.45);
  function dot(centerX, centerY, radius, color) {
    for (let pixelY = Math.floor(centerY - radius); pixelY <= centerY + radius; pixelY++) {
      for (let pixelX = Math.floor(centerX - radius); pixelX <= centerX + radius; pixelX++) {
        if (Math.hypot(pixelX - centerX, pixelY - centerY) <= radius) canvas.put(pixelX, pixelY, color);
      }
    }
  }
  function line(startX, startY, endX, endY, width, color = accent) {
    const steps = Math.max(1, Math.ceil(Math.hypot(endX - startX, endY - startY) * 2));
    for (let step = 0; step <= steps; step++) dot(startX + (endX - startX) * step / steps, startY + (endY - startY) * step / steps, width, color);
  }
  function arc(centerX, centerY, radius, from, to, color = accent, width = 1) {
    for (let step = 0; step <= 80; step++) {
      const angle = from + (to - from) * step / 80;
      dot(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius, width, color);
    }
  }
  function diamond(centerX, centerY, size, color = light) {
    line(centerX, centerY - size, centerX + size * 0.55, centerY, 2, color);
    line(centerX + size * 0.55, centerY, centerX, centerY + size, 2, color);
    line(centerX, centerY + size, centerX - size * 0.55, centerY, 2, color);
    line(centerX - size * 0.55, centerY, centerX, centerY - size, 2, color);
  }
  for (let pixelY = 0; pixelY < 64; pixelY++) for (let pixelX = 0; pixelX < 64; pixelX++) {
    const radius = Math.hypot(pixelX - 31.5, pixelY - 31.5);
    if (radius <= 31) canvas.put(pixelX, pixelY, radius > 28.5 ? shade : [16, 19, 32]);
  }
  if (second) arc(32, 32, 24, 0, Math.PI * 2, shade);
  switch (spell.owner) {
    case 0:
      if (!second) for (const [centerX, centerY] of [[18, 38], [32, 22], [45, 38]]) {
        dot(centerX, centerY - 6, 4, light); line(centerX, centerY, centerX, centerY + 9, 3); line(centerX - 5, centerY + 4, centerX + 5, centerY + 4, 1);
      } else { line(16, 19, 32, 14, 2); line(32, 14, 48, 19, 2); arc(32, 25, 17, 0, Math.PI, accent, 2); diamond(32, 28, 9); }
      break;
    case 1:
      if (!second) { line(39, 18, 39, 51, 2, shade); arc(37, 34, 21, Math.PI, Math.PI * 1.75, light, 3); }
      else { for (const column of [17, 27, 37, 47]) { line(column, 24, column, 46, 1.5); diamond(column, 19, 4); } arc(32, 39, 18, 0, Math.PI, light); }
      break;
    case 2:
      if (!second) for (const offset of [-12, 0, 12]) { line(16 + offset, 47, 40 + offset, 18, 1, shade); line(26 + offset, 35, 40 + offset, 18, 2, light); }
      else { dot(32, 34, 11, accent); arc(32, 34, 17, 0, 6.3, shade); line(34, 24, 41, 13, 2, light); for (let index = 0; index < 6; index++) { const angle = index * Math.PI / 3; line(32 + Math.cos(angle) * 17, 34 + Math.sin(angle) * 17, 32 + Math.cos(angle) * 22, 34 + Math.sin(angle) * 22, 1); } }
      break;
    case 3:
      if (!second) { line(20, 49, 40, 17, 2, light); arc(34, 23, 15, -1.1, 1.3, accent, 5); line(17, 26, 26, 31, 1); }
      else { diamond(32, 32, 16); dot(25, 24, 6, accent); dot(39, 24, 6, accent); line(19, 43, 45, 17, 1, light); }
      break;
    case 4:
      if (!second) { dot(31, 28, 12, shade); arc(31, 28, 12, 0, 6.3, light, 2); line(36, 38, 44, 51, 3); line(17, 15, 23, 8, 1); line(43, 17, 49, 10, 1); }
      else { arc(32, 30, 17, 0, Math.PI, accent, 3); line(15, 29, 49, 29, 2, light); for (const column of [23, 32, 41]) arc(column, 20, 5, 2, 4.7, accent); }
      break;
    case 5:
      if (!second) { line(32, 15, 32, 49, 2, light); line(22, 19, 22, 29, 2); line(42, 19, 42, 29, 2); line(22, 29, 42, 29, 2); for (const height of [38, 46]) arc(32, height - 15, 20, 0.5, 2.6, accent, 2); }
      else { line(25, 16, 39, 16, 2, light); line(26, 17, 26, 25, 1); line(38, 17, 38, 25, 1); arc(32, 33, 12, -0.7, 3.8, accent, 3); for (const column of [19, 31, 43]) diamond(column, 49, 3); }
      break;
    case 6:
      if (!second) { arc(32, 32, 14, 0, 6.3, accent, 4); dot(32, 32, 4, light); for (let index = 0; index < 8; index++) { const angle = index * Math.PI / 4; line(32 + Math.cos(angle) * 14, 32 + Math.sin(angle) * 14, 32 + Math.cos(angle) * 21, 32 + Math.sin(angle) * 21, 2); } }
      else { line(14, 49, 49, 14, 4, shade); line(14, 49, 49, 14, 1, light); arc(26, 38, 12, -2.6, -0.8); arc(39, 24, 12, 0.6, 2.4); }
      break;
    case 7:
      if (!second) { diamond(32, 32, 18, accent); line(18, 22, 46, 42, 1, shade); line(18, 42, 46, 22, 1, shade); dot(32, 32, 4, light); }
      else { arc(25, 29, 13, 0, 6.3, accent, 2); arc(39, 36, 13, 0, 6.3, light, 2); diamond(32, 32, 5); }
      break;
    case 8:
      arc(32, 26, 16, 0, 6.3, accent, 2);
      if (!second) { for (let index = 0; index < 8; index++) { const angle = index * Math.PI / 4; line(32 + Math.cos(angle) * 20, 26 + Math.sin(angle) * 20, 32 + Math.cos(angle) * 25, 26 + Math.sin(angle) * 25, 1, light); } }
      else { line(32, 23, 32, 49, 3, light); line(23, 39, 41, 39, 3, light); }
      break;
    case 9:
      for (const offset of second ? [-10, 10] : [-14, 0, 14]) { line(32, 49, 32 + offset, 15, 2, light); for (let step = 0; step < 4; step++) line(32 + offset * (1 - step / 5), 17 + step * 6, 24 + offset * (1 - step / 5), 22 + step * 6, 2); }
      if (second) arc(32, 48, 17, 3.3, 6, accent, 2);
      break;
    case 10:
      if (!second) for (const offset of [-12, 0, 12]) { line(18 + offset, 49, 42 + offset, 17, 1.5, light); diamond(40 + offset, 20, 5); }
      else { for (let index = 0; index < 6; index++) { const angle = index * Math.PI / 3; line(32, 32, 32 + Math.cos(angle) * 20, 32 + Math.sin(angle) * 20, 1.5, accent); } arc(32, 32, 13, 0, 6.3, light, 2); line(22, 42, 42, 22, 2, light); }
      break;
    case 11:
      if (!second) {
        line(12, 32, 52, 32, 4, shade); line(32, 12, 32, 52, 4, shade);
        line(12, 32, 52, 32, 1.5, light); line(32, 12, 32, 52, 1.5, light);
        arc(32, 32, 13, 0, Math.PI * 2, accent, 1); diamond(32, 32, 7, light);
      } else {
        for (const [centerX, centerY] of [[32, 20], [18, 28], [46, 28], [24, 44], [42, 44]]) {
          line(centerX - 5, centerY - 10, centerX, centerY, 1.5, accent); diamond(centerX, centerY, 4, light);
        }
      }
      break;
  }
  for (let marker = 0; marker < (second ? 2 : 1); marker++) dot(30 + marker * 5, 57, 1.2, light);
  return canvas;
}

function generate(resources, preview) {
  const folder = path.join(resources, 'assets/ftboss/textures/gui/spell_icons');
  fs.mkdirSync(folder, { recursive: true });
  const sheet = makeCanvas(owners.length * 128, 3 * 128);
  const additions = makeCanvas(6 * 128, 2 * 128);
  spells.forEach((spell, index) => {
    const second = spell.owner === 11 ? spell.id === 'rose_starfall' : index % 2 === 1;
    const isThird = index >= 22 && index < 33;
    const canvas = isThird ? third.icon(spell) : icon(spell, second);
    canvas.save(path.join(folder, spell.id + '.png'));
    for (let pixelY = 0; pixelY < 128; pixelY++) for (let pixelX = 0; pixelX < 128; pixelX++) {
      const offset = (Math.floor(pixelY / 2) * 64 + Math.floor(pixelX / 2)) * 4;
      sheet.put(spell.owner * 128 + pixelX, (isThird ? 256 : second ? 128 : 0) + pixelY, canvas.buf.subarray(offset, offset + 4));
      if (isThird) additions.put((index - 22) % 6 * 128 + pixelX, Math.floor((index - 22) / 6) * 128 + pixelY, canvas.buf.subarray(offset, offset + 4));
    }
  });
  for (const [locale, additions] of Object.entries(languages())) {
    const file = path.join(resources, `assets/ftboss/lang/${locale}.json`);
    const original = JSON.parse(fs.readFileSync(path.join(root, `src/main/resources/assets/ftboss/lang/${locale}.json`), 'utf8'));
    fs.mkdirSync(path.dirname(file), { recursive: true });
    fs.writeFileSync(file, JSON.stringify({ ...original, ...additions }, null, 2) + '\n');
  }
  if (preview) {
    fs.mkdirSync(path.dirname(preview), { recursive: true });
    sheet.save(preview);
    additions.save(preview.replace(/\.png$/, '-new.png'));
  }
  console.log(`Generated ${spells.length} manor spell icons and locale additions`);
}

if (require.main === module) {
  assert(process.argv[2], 'Provide a staging resource directory');
  generate(path.resolve(process.argv[2]), process.argv[3]);
}
module.exports = { generate, spells, owners, languages };
