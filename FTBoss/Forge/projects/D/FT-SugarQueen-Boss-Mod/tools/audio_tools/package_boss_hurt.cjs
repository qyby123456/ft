const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');

const project = path.resolve(__dirname, '../..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/boss-vanilla-hurt-sounds-20261012'));
assert.ok(task.startsWith(path.join(project, '_work') + path.sep));
const jar = 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot/bin/jar.exe';
const baseline = path.join(task, 'baseline.jar');
const compiled = path.join(task, 'after-srg.jar');
const candidate = path.join(task, 'qys-bosses.jar');
const tree = path.join(task, 'staged-content');
fs.mkdirSync(tree);
const entries = execFileSync(jar, ['tf', baseline], { encoding: 'utf8' }).trim().split(/\r?\n/);
for (const entry of entries) {
  assert.ok(!entry.startsWith('/') && !entry.includes('..') && !entry.includes('\\'), `Unsafe jar entry: ${entry}`);
  assert.ok(!/^META-INF\/.*\.(SF|RSA|DSA)$/i.test(entry), 'Signed baseline cannot be patched');
}
execFileSync(jar, ['xf', baseline], { cwd: tree });
const classes = ['net/farmingtales/ftboss/sound/BossHurtProfile.class',
  'net/farmingtales/ftboss/sound/BossHurtSounds.class', 'net/farmingtales/ftboss/mixin/BossHurtSoundMixin.class'];
execFileSync(jar, ['xf', compiled, ...classes], { cwd: tree });
for (const entry of classes) assert.ok(fs.statSync(path.join(tree, entry)).size > 0);
const jsonFiles = ['assets/ftboss/sounds.json', 'assets/ftboss/lang/zh_cn.json', 'assets/ftboss/lang/en_us.json'];
const changed = [...classes];
for (const relative of jsonFiles) {
  const destination = path.join(tree, relative);
  const original = JSON.parse(fs.readFileSync(destination, 'utf8'));
  const prefix = relative.endsWith('sounds.json') ? 'boss_hurt_' : 'ftboss.subtitle.boss_hurt_';
  const customKeys = Object.keys(original).filter(key => key.startsWith(prefix));
  assert.equal(customKeys.length, 11);
  for (const key of customKeys) delete original[key];
  fs.writeFileSync(destination, JSON.stringify(original, null, 2) + '\n');
  changed.push(relative);
}
const audioDirectory = 'assets/ftboss/sounds/effects/hurt';
const audio = entries.filter(entry => entry.startsWith(audioDirectory + '/') && entry.endsWith('.ogg'));
assert.equal(audio.length, 33);
for (const relative of audio) {
  assert.ok(/^[a-z]+_[123]\.ogg$/.test(path.basename(relative)));
  fs.unlinkSync(path.join(tree, relative));
}
fs.rmdirSync(path.join(tree, audioDirectory));
execFileSync(jar, ['--create', '--file', candidate, '--no-manifest', '-C', tree, '.']);
fs.writeFileSync(path.join(task, 'verification/allowed-entries.json'), JSON.stringify(changed, null, 2) + '\n');
fs.writeFileSync(path.join(task, 'verification/removed-entries.json'), JSON.stringify(audio, null, 2) + '\n');
console.log('PASS: vanilla-only candidate; all 33 custom sounds and 33 custom event/subtitle keys removed, unrelated resources preserved');
