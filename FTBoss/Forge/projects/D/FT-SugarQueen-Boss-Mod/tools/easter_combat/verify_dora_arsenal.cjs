const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');
const root = path.resolve(__dirname, '../..');
const output = path.resolve(process.argv[2] || path.join(root, 'build/dora-arsenal-check'));
const read = relative => fs.readFileSync(path.join(root, relative), 'utf8');
const entity = read('src/main/java/net/farmingtales/ftboss/entity/EasterBossEntity.java');
const model = read('src/main/java/net/farmingtales/ftboss/client/model/EasterBossModel.java');
const visual = read('src/main/java/net/farmingtales/ftboss/client/render/EasterSkillVisual.java');
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
assert(entity.includes('DORA_SMG_ID = "ftboss:dora_nova_launcher"'));
assert(entity.includes('DORA_FOCUS_ID = "bren:machine_gun"'));
assert(method('equipWeapon').includes('id = DORA_SMG_ID'));
assert(method('castSkill').includes('this.skillDoraGun(sl, target)'));
assert(method('skillSuppress').includes('!this.equipDoraGun(false)'));
assert(method('skillSuppress').includes('this.doraSniperNext = true'));
assert(method('startDoraSniper').includes('!this.equipDoraGun(true)'));
assert(method('skillCommitted').includes('this.doraSniper.busy()'));
assert(method('skillDoraGun').includes('this.doraSniper.busy() || this.burstShots > 0 || this.reloadTicks > 0'));
assert(method('equipDoraGun').includes('"Unbreakable", true'));
assert(!method('equipDoraGun').includes('OFF_HAND'));
for (const name of ['clearDoraCombat', 'cancelSkillWindups', 'die', 'remove', 'startReload']) {
  assert(method(name).includes('this.cancelDoraSniper()'), `${name} cancels sniper`);
}
assert(method('doraCombatTick').indexOf('this.tickDoraSpear(') < method('doraCombatTick').indexOf('this.doraSniper.busy()'));
assert(!method('tickDoraSpear').includes('this.castTicks > 0'));
assert(!method('tickDoraSpear').includes('this.cancelDoraSniper()'));
assert(method('tickDoraSniper').includes('this.getNavigation().stop()'));
assert(method('tickDoraSniper').includes('Step.TRACKING') && method('tickDoraSniper').includes('Step.LOCK'));
assert(!method('fireDoraSniper').includes('getTarget()'));
for (const guard of ['this.clipSkillRay', 'bounds.clip(start, end)', 'start.distanceToSqr(intersection) < nearest',
  'victim instanceof Player player', 'this.combatPlayer(player)', '!this.isAlliedTo(player)', 'Kind.DORA_IMPACT']) {
  assert(method('fireDoraSniper').includes(guard), guard);
}
assert(!/explode\(|destroyBlock\(|setHealth\(|sendParticles|ParticleTypes/.test(method('fireDoraSniper')));
assert(method('syncDoraSniperCue').includes('"doraLocked", this.doraSniper.locked()'));
assert(model.includes('applyDoraSniperPose') && model.includes('cue.getBoolean("doraSniper")'));
assert(visual.includes('0xFF5268') && visual.includes('0xFFCB67') && visual.includes('0x67FFE0'));
assert(method('addAdditionalSaveData').includes('"doraSniperNext"'));
assert(method('readAdditionalSaveData').includes('"doraSniperNext"'));
for (const language of ['zh_cn', 'en_us']) {
  const entries = JSON.parse(read(`src/main/resources/assets/ftboss/lang/${language}.json`));
  assert(entries['message.ftboss.dora_sniper.charge']);
  assert(entries['message.ftboss.dora_sniper.lock']);
}
fs.mkdirSync(output, { recursive: true });
const javaHome = process.env.JAVA_HOME || path.join(process.env.APPDATA, '.minecraft/runtime/java-runtime-gamma-snapshot');
execFileSync(path.join(javaHome, 'bin/javac.exe'), ['-encoding', 'UTF-8', '-d', output,
  path.join(root, 'src/main/java/net/farmingtales/ftboss/util/DoraSniperCycle.java'),
  path.join(root, 'src/main/java/net/farmingtales/ftboss/util/HiddenBossCastRules.java'),
  path.join(root, 'src/main/java/net/farmingtales/ftboss/util/DoraCombatTempo.java'),
  path.join(__dirname, 'DoraSniperProbe.java'), path.join(__dirname, 'DoraTempoProbe.java')], { stdio: 'inherit' });
for (const probe of ['DoraSniperProbe', 'DoraTempoProbe']) {
  execFileSync(path.join(javaHome, 'bin/java.exe'), ['-cp', output, probe], { stdio: 'inherit' });
}
console.log('PASS: dual-gun switching, concurrent triple spear, synced cues/poses, cancellation, translations');
