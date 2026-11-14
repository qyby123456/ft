const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '../..');
const read = relative => fs.readFileSync(path.join(root, relative), 'utf8');
const entity = read('src/main/java/net/farmingtales/ftboss/entity/EasterBossEntity.java');
const model = read('src/main/java/net/farmingtales/ftboss/client/model/EasterBossModel.java');
const visual = read('src/main/java/net/farmingtales/ftboss/client/render/EasterSkillVisual.java');
const body = name => {
  const declaration = new RegExp(`(?:private|public|protected) [\\w.<>]+ ${name}\\([^)]*\\) \\{`).exec(entity);
  assert(declaration, name);
  const start = declaration.index + declaration[0].length;
  let depth = 1;
  let end = start;
  while (depth && end < entity.length) {
    if (entity[end] === '{') depth++;
    if (entity[end] === '}') depth--;
    end++;
  }
  return entity.slice(start, end - 1);
};
for (const key of ['DATA_COMBAT_ANIMATION', 'DATA_SKILL_CUE']) {
  assert(entity.includes(`this.entityData.define(${key}, new CompoundTag())`));
  assert(entity.includes(`this.entityData.set(${key},`));
}
for (const [start, execute, timer, radius] of [
  ['skillCleave', 'executeCleave', 'cleaveWind', 'CLEAVE_RADIUS'],
  ['skillPanFlip', 'executePanFlip', 'panWind', 'PAN_RADIUS'],
  ['skillPoisonBurst', 'executePoisonBurst', 'poisonWind', 'POISON_RADIUS'],
  ['skillXueliPanSlap', 'executeXueliPanSlap', 'xueliPanWind', 'XUELI_PAN_RADIUS'],
  ['skillShadowStrike', 'executeShadowStrike', 'shadowStrikeWind', '4.0']
]) {
  assert(!body(start).includes('.hurt('), start + ' must not deal immediate damage');
  assert(body(start).includes('beginSkillCue(') && body(start).includes(radius));
  assert(body(execute).includes('.hurt(') && body(execute).includes('releaseSkillCue('));
  assert.equal((entity.match(new RegExp(`--this\\.${timer}`, 'g')) || []).length, 1, timer + ' decrements once');
  assert(body('tickSkillWindups').includes(execute + '('));
  assert(body('cancelSkillWindups').includes(`this.${timer} = 0`));
}
assert(body('tick').indexOf('tickSkillWindups(') < body('tick').indexOf('this.castSkill(t)'));
assert(body('doHurtTarget').includes('this.skillCommitted()'));
assert(body('doHurtTarget').includes('if (this.getVariant() == XUELI_VARIANT) this.startCrush(p)'));
assert(body('xueliRollTick').includes('this.startCrush(p)'));
assert(entity.includes('XUELI_ROLLING_PIN_QUOTE_CHANCE = 0.20f'));
assert(entity.includes('XUELI_ROLLING_PIN_QUOTE_COOLDOWN = 5 * 20'));
assert.match(body('startCrush'), /if \(this\.level\(\) instanceof ServerLevel sl\) \{[^}]*sl\.getGameTime\(\) >= this\.xueliRollingPinQuoteReadyAt\s*&& this\.random\.nextFloat\(\) < XUELI_ROLLING_PIN_QUOTE_CHANCE/s);
assert(body('startCrush').includes('this.xueliRollingPinQuoteReadyAt = sl.getGameTime() + XUELI_ROLLING_PIN_QUOTE_COOLDOWN'));
assert(body('startCrush').includes('this.sayXueliDialogue("摩托罗拉哒！")'));
assert(!body('xueliCrushTick').includes('QUOTE') && !body('xueliCrushTick').includes('startCrush('));
assert.equal(entity.split('摩托罗拉哒！').length - 1, 1);
assert(body('tick').includes('this.tickXueliDialogue()'));
assert(body('tick').indexOf('this.tickXueliDialogue()') < body('tick').indexOf('this.xueliCloneTick('));
for (const guard of ['XUELI_VARIANT', '!this.isMini()', '!this.isGiant()', 'this.isAlive()', '!this.isNoAi()', '!net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(this)']) {
  assert(body('canSpeakXueliDialogue').includes(guard), guard);
}
assert(body('tickXueliDialogue').includes('instanceof ServerLevel'));
assert(body('tickXueliDialogue').includes('instanceof Player'));
assert(body('tickXueliDialogue').includes('player.isCreative() || player.isSpectator()'));
assert(body('tickXueliDialogue').includes('this.xueliDialogue.nextStage('));
assert(body('tickXueliDialogue').includes('this.xueliDialogue.scheduleRandom('));
assert(body('skillXueliRoll').includes('XueliDialogueState.ROLLER'));
assert(body('xueliSlamDown').includes('if (done == 1) this.sayXueliDialogue(net.farmingtales.ftboss.util.XueliDialogueState.SLAM)'));
assert(body('skillXueliClones').includes('if (made > 0) this.sayXueliDialogue(net.farmingtales.ftboss.util.XueliDialogueState.CLONES)'));
assert(body('skillXueliClones').includes('if (!sl.addFreshEntity(clone)) continue'));
assert(body('onXueliShieldActivated').includes('XueliDialogueState.SHIELD'));
assert(body('sayXueliDialogue').includes('this.tellNearby(level, "§b雪黎：§f" + line)'));
assert(body('setVariant').includes('this.xueliDialogue.restore(0,'));
assert(body('addAdditionalSaveData').includes('tag.putInt("xueliDialogueProgress", this.xueliDialogue.progress())'));
assert(body('readAdditionalSaveData').includes('this.xueliDialogue.restore(tag.getInt("xueliDialogueProgress"),'));
const maids = read('src/main/java/net/farmingtales/ftboss/boss/ai/XueliMaids.java');
assert.equal(maids.split('this.boss.onXueliShieldActivated()').length - 1, 1);
assert(maids.indexOf('this.boss.onXueliShieldActivated()') > maids.indexOf('if (spawned <= 0)'));
assert(maids.indexOf('this.boss.onXueliShieldActivated()') > maids.indexOf('this.shield = true'));
assert(maids.includes('if (!sl.addFreshEntity(m)) continue'));
assert(body('xueliCloneTick').includes('this.skillCommitted()'));
assert(body('xuanlanBeamFx').includes('clipSkillRay('));
assert(!body('xuanlanBeamFx').includes('sendParticles('));
/* 2026-09-20: 光束不再造成伤害 —— 结算方法改名 xuanlanBeamPunish, 只做眩晕 + 清增益 */
assert(body('xuanlanBeamPunish').includes('clipSkillRay('));
assert(body('xuanlanBeamPunish').includes('!this.combatPlayer(p) || this.xlBeamPunished.contains(p.getUUID())'));
assert(body('xuanlanBeamPunish').includes('this.stunPlayerAny(p, XUANLAN_BEAM_STUN, "超能光束")'));
assert(body('xuanlanBeamPunish').includes('net.farmingtales.ftboss.util.PlayerBuffRules.clearBuffs(p)'));
assert(!body('xuanlanBeamPunish').includes('p.hurt(') && !body('xuanlanBeamPunish').includes('manorSkillDamage('));
/* 「不要锁头」: 引导分支与前摇分支都不许再调 xuanlanAimAtTarget */
const fireBranch = body('xuanlanBeamTick').split('if (this.xuanlanBeamFire > 0) {')[1].split('return true;')[0];
const chargeBranch = body('xuanlanBeamTick').split('if (this.xuanlanBeamCharge > 0) {')[1].split('return true;')[0];
assert(!fireBranch.includes('xuanlanAimAtTarget()') && !chargeBranch.includes('xuanlanAimAtTarget()'));
assert(body('beamStrike').includes('to = this.clipSkillRay(from, to)'));
assert(!model.includes('this.actionBlend') && !model.includes('this.castBlend') && !model.includes('this.lastAge'));
assert(model.includes('this.rightArm.resetPose()') && model.includes('getActionProgress(partialTick)'));
assert(visual.includes('MAX_VISIBLE_CUES = 24') && visual.includes('PARTICLE_BUDGET = 32'));
assert(visual.includes('.setWriteMaskState(COLOR_WRITE)') && visual.includes('.setDepthTestState(LEQUAL_DEPTH_TEST)'));
assert(!visual.includes('static final List') && !visual.includes('static final Map'));
for (const relative of [
  'src/main/java/net/farmingtales/ftboss/util/EasterCombatMath.java',
  'src/main/java/net/farmingtales/ftboss/client/render/EasterSkillVisual.java'
]) {
  const text = read(relative);
  assert(!text.includes('\r') && !text.startsWith('\uFEFF'));
  assert(!/ +$/m.test(text) && text.endsWith('\n') && !text.endsWith('\n\n'));
}
console.log('PASS: synced state, five delayed skills, single-tick timers, cancellation, beam occlusion, stateless poses, FX budgets');
