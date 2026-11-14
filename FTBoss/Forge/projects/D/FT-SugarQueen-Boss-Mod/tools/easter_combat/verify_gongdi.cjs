const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '../..');
const read = relative => fs.readFileSync(path.join(root, relative), 'utf8');
const entity = read('src/main/java/net/farmingtales/ftboss/entity/EasterBossEntity.java');
const skills = read('src/main/java/net/farmingtales/ftboss/boss/ai/GongdiSkills.java');
const visual = read('src/main/java/net/farmingtales/ftboss/client/render/EasterSkillVisual.java');
const model = read('src/main/java/net/farmingtales/ftboss/client/model/EasterBossModel.java');
for (const connection of ['this.gongdiSkills.tick(level)', 'this.gongdiSkills.onHit(pl)',
  'this.gongdiSkills.tryStart(sl, target)', 'this.gongdiSkills.isCasting()', 'this.gongdiSkills.onAbilityHit(level, player']) {
  assert(entity.includes(connection), connection);
}
assert.equal((entity.match(/this\.gongdiSkills\.reset\(\)/g) || []).length, 4, 'variant, no-AI, death and removal cleanup');
const nativeStart = entity.indexOf('if (this.getVariant() == 3 && !frozen && !this.isInvulnerable()');
const genericStart = entity.indexOf('if (this.getVariant() != 3 && !frozen && !this.skillCommitted()');
assert(nativeStart >= 0 && genericStart > nativeStart, 'Gongdi has an independent native scheduler');
const nativeScheduler = entity.slice(nativeStart, genericStart);
assert(nativeScheduler.includes('!this.skillCommitted()'), 'native windups cannot overwrite each other');
assert(nativeScheduler.includes('this.gongdiSkills.validTarget(player, CLEAVE_RADIUS)'), 'invalid targets do not consume cooldown');
assert(nativeScheduler.includes('ManorBossCombatRules.spellInterval(3)')
  && nativeScheduler.includes('/ net.farmingtales.ftboss.util.ManorBossCombatRules.SPELLS_PER_CAST'), 'same per-skill trigger budget as Iron double casts');
assert(!/actionTicks|castTicks|manor_spell_ready|manor_cd_/.test(nativeScheduler), 'no melee or Iron slot/cooldown lock');
assert(nativeScheduler.includes('!this.isGiant() && !finale'), 'no special channel for giant or finale actors');
for (const skill of ['decimate', 'crippling_strike', 'apprehend', 'guillotine']) {
  assert((entity + skills).includes(`"${skill}"`), skill + ' server wiring');
}
assert(skills.includes('this.boss.hasLineOfSight(player)'));
assert(skills.includes('!player.isCreative() && !player.isSpectator()'));
assert(skills.includes('level.getPlayerByUUID(player.getUUID()) != player'));
assert(skills.includes('player.hurtMarked = true'));
assert(skills.includes('if (!player.isAlive()) {\n            this.executeReadyAt'));
assert(!skills.includes('teleportTo') && !skills.includes('setHealth('), 'no wall teleport or forced death');
assert(visual.includes('renderGongdiCue(') && visual.includes('cue.contains("gongdiSkill")'));
assert(model.includes('applyGongdiPose(') && model.includes('cue.getBoolean("released")'));
assert(entity.includes('GongdiCombatRules.meleeDamage(this.getAttributeValue(Attributes.ATTACK_DAMAGE),'));
assert(entity.includes('EnchantmentHelper.getDamageBonus(this.getMainHandItem(), target.getMobType())'));
const cleave = entity.slice(entity.indexOf('private void executeCleave()'), entity.indexOf('public static final float LEMON_BORROW_DAMAGE'));
assert(cleave.includes('GongdiCombatRules.cleaveDamage(meleeDamage, outer)'));
assert(cleave.indexOf('double attackDamage = this.getAttributeValue(Attributes.ATTACK_DAMAGE)') < cleave.indexOf('for (Player player'));
assert(cleave.indexOf('this.applyCleaveAtkBoost()') > cleave.indexOf('outerHits++'), 'post-cast buff must not inflate the cast');
assert.equal((skills.match(/GongdiCombatRules\.executeDamage\(this\.boss\.gongdiMeleeDamage\(player\), stacks\)/g) || []).length,
  2, 'execute prediction and impact share scaling');
assert(skills.includes('GongdiCombatRules.strikeDamage(this.boss.gongdiMeleeDamage(player))'));
assert(!entity.includes('CLEAVE_DAMAGE =') && !skills.includes('GongdiCombatRules.STRIKE_DAMAGE'), 'no fixed skill damage');
const damage = JSON.parse(read('src/main/resources/data/ftboss/damage_type/gongdi_guillotine.json'));
assert.equal(damage.scaling, 'never');
for (const tag of ['bypasses_armor', 'bypasses_resistance', 'bypasses_enchantments', 'bypasses_shield']) {
  const data = JSON.parse(read(`src/main/resources/data/minecraft/tags/damage_type/${tag}.json`));
  assert.equal(data.replace, false);
  assert(data.values.includes('ftboss:gongdi_guillotine'));
}
for (const language of ['zh_cn', 'en_us']) {
  const data = JSON.parse(read(`src/main/resources/assets/ftboss/lang/${language}.json`));
  assert(data['death.attack.ftboss.gongdi_guillotine']);
}
console.log('Gongdi integration and resource checks passed');
