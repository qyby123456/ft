const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { decodePNG } = require('../model3d/png.cjs');
const root = path.resolve(__dirname, '../..');
const read = relative => fs.readFileSync(path.join(root, relative), 'utf8');
const entity = read('src/main/java/net/farmingtales/ftboss/entity/EasterBossEntity.java');
const model = read('src/main/java/net/farmingtales/ftboss/client/model/EasterBossModel.java');
const method = name => {
  const match = new RegExp(`(?:private|public|protected) [\\w.<>]+ ${name}\\([^)]*\\) \\{`).exec(entity);
  assert(match, name);
  let depth = 1;
  let end = match.index + match[0].length;
  const start = end;
  while (depth && end < entity.length) {
    if (entity[end] === '{') depth++;
    if (entity[end] === '}') depth--;
    end++;
  }
  return entity.slice(start, end - 1);
};
assert(method('equipWeapon').includes('this.getVariant() == DORA_VARIANT ? DUOLA_MELEE_ID'));
for (const name of ['startReload', 'finishReload']) {
  assert(!method(name).includes('setItemInHand'), 'reload must preserve both enchanted weapons');
}
assert(method('tick').includes('this.doraCombatTick()'));
assert(method('tick').includes('while (this.burstShots > 0 && this.burstAcc >= need)'));
assert(method('tick').includes('this.burstShots <= 0 && this.reloadTicks <= 0'));
assert(method('tickDoraSpear').includes('this.hasLineOfSight(player)'));
assert(method('spearChargeHit').includes('if (!p.hurt('));
assert(method('tickDoraSpear').includes('DORA_SPEAR_HITS'));
assert(method('spearChargeHit').includes('DORA_SPEAR_REACH'));
assert(method('doraCombatTick').includes('this.isDeadOrDying() || this.isNoAi() || this.isMini()'));
assert(method('doraCombatTick').includes('this.combatPlayer(player)'));
assert(!method('reloadTick').includes('.hurt('), 'one spear damage path only');
assert.equal((method('pumpBurst').match(/this\.doraHit\(player\)/g) || []).length, 1);
assert(method('pumpBurst').includes('if (physicalHit || magicHit)'));
assert(method('doraKnockback').includes('direction.normalize()'));
assert(method('doraKnockback').includes('player.hurtMarked = true'));
for (const field of ['DATA_DORA_RELOADING', 'DATA_DORA_SPEAR_TICKS']) {
  assert(method('defineSynchedData').includes(`this.entityData.define(${field},`));
  assert(entity.includes(`this.entityData.set(${field},`));
}
assert(model.includes('this.applyDoraDualPose(entity, partialTick, ageInTicks, actionBlend)'));
assert(read('src/main/java/net/farmingtales/ftboss/effect/FTEffects.java').includes('EFFECTS.register("lethal_tempo", LethalTempoEffect::new)'));
for (const locale of ['zh_cn', 'en_us']) {
  const lang = JSON.parse(read(`src/main/resources/assets/ftboss/lang/${locale}.json`));
  assert(lang['effect.ftboss.lethal_tempo']);
  assert(lang['effect.ftboss.lethal_tempo.desc']);
}
const icon = decodePNG(path.join(root, 'src/main/resources/assets/ftboss/textures/mob_effect/lethal_tempo.png'));
assert.equal(icon.w, 18);
assert.equal(icon.h, 18);
console.log('PASS: Dora dual-wield, hit gating, synced poses, live tempo scheduling, translations and icon wiring');
