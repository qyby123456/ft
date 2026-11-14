const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const resources = require('./resources.cjs');
const project = path.resolve(__dirname, '../../..');
const task = path.join(project, '_work/common-material-bonds-20261025');
const jdk = path.join(process.env.APPDATA, '.minecraft/runtime/java-runtime-gamma-snapshot/bin');
const dependencies = path.join(project, '_work/zicai-theme-zheergen-trail-20261025/compile-dependencies.jar');
const baseline = path.join(task, 'baseline.jar');
const candidate = path.join(task, 'qys-bosses.jar');
const compiled = path.join(task, 'compiled');
const tools = path.join(task, 'tool-classes');
const payload = path.join(task, 'payload');
const extracted = path.join(task, 'baseline-resources');
const finalResources = path.join(task, 'final-resources');
const utility = 'net/farmingtales/ftboss/util/';
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const tracked = [__filename, path.join(__dirname, 'resources.cjs'), path.join(__dirname, 'PatchCommonMaterialBonds.java'), path.join(__dirname, 'deploy.ps1'),
  ...['SwordCavityRules.java', 'CommonMaterialBondRules.java'].map(file => path.join(project, 'src/main/java', utility, file)),
  ...['CommonMaterialBondCheck.java', 'AccessoryBondRulesCheck.java', 'SwordCavityRulesCheck.java', 'MaterialBowCombatRulesCheck.java', 'UpgradeReturnsCheck.java', 'WeaponBondGuideCoverageCheck.java', 'ZipStreamCheck.java']
    .map(file => path.join(project, 'tools/verification', file)),
  ...['lang/zh_cn.json', 'lang/en_us.json', 'weapon_bonds/guide.json'].map(file => path.join(project, 'src/main/resources/assets/ftboss', file))];
const sourceHashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
for (const folder of [compiled, tools, payload, extracted, finalResources]) fs.mkdirSync(folder, { recursive: true });
function run(executable, args, cwd = project) {
  const result = spawnSync(executable, args, { cwd, windowsHide: true, encoding: 'utf8', maxBuffer: 16 * 1024 * 1024 });
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error('Command failed: ' + result.status);
}
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', baseline, '-d', compiled,
  ...['SwordCavityRules.java', 'CommonMaterialBondRules.java'].map(file => path.join(project, 'src/main/java', utility, file))]);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', [compiled, baseline, dependencies].join(path.delimiter),
  '-d', tools, path.join(__dirname, 'PatchCommonMaterialBonds.java'),
  ...['CommonMaterialBondCheck.java', 'AccessoryBondRulesCheck.java', 'SwordCavityRulesCheck.java', 'MaterialBowCombatRulesCheck.java', 'UpgradeReturnsCheck.java', 'WeaponBondGuideCoverageCheck.java', 'ZipStreamCheck.java']
    .map(file => path.join(project, 'tools/verification', file))]);
const toolClasspath = [tools, dependencies].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'PatchCommonMaterialBonds', baseline, compiled, payload]);
for (const name of ['SwordCavityRules$Link', 'CommonMaterialBondRules', 'CommonMaterialBondRules$Family']) {
  const relative = utility + name + '.class';
  fs.copyFileSync(path.join(compiled, relative), path.join(payload, relative));
}
const resourceEntries = ['lang/zh_cn.json', 'lang/en_us.json', 'weapon_bonds/guide.json'].map(file => 'assets/ftboss/' + file);
run(path.join(jdk, 'jar.exe'), ['--extract', '--file', baseline, ...resourceEntries], extracted);
const generated = resources();
for (const language of ['zh_cn', 'en_us']) {
  const relative = `assets/ftboss/lang/${language}.json`;
  const original = JSON.parse(fs.readFileSync(path.join(extracted, relative)));
  const source = JSON.parse(fs.readFileSync(path.join(project, 'src/main/resources', relative)));
  for (const [key, value] of Object.entries(generated.lang[language])) {
    if (source[key] !== value) throw new Error('Source resource drift: ' + key);
    original[key] = value;
  }
  fs.mkdirSync(path.dirname(path.join(payload, relative)), { recursive: true });
  fs.writeFileSync(path.join(payload, relative), JSON.stringify(original, null, 2) + '\n');
}
const guideEntry = 'assets/ftboss/weapon_bonds/guide.json';
const originalGuide = JSON.parse(fs.readFileSync(path.join(extracted, guideEntry)));
const sourceGuide = JSON.parse(fs.readFileSync(path.join(project, 'src/main/resources', guideEntry)));
for (const entry of generated.guide) {
  if (originalGuide.some(existing => existing.id === entry.id)) throw new Error('Baseline already contains added bond: ' + entry.id);
  if (JSON.stringify(sourceGuide.find(existing => existing.id === entry.id)) !== JSON.stringify(entry)) throw new Error('Guide source drift: ' + entry.id);
  originalGuide.push(entry);
}
fs.mkdirSync(path.dirname(path.join(payload, guideEntry)), { recursive: true });
fs.writeFileSync(path.join(payload, guideEntry), JSON.stringify(originalGuide, null, 2) + '\n');
function files(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const target = path.join(directory, entry.name);
    return entry.isDirectory() ? files(target) : [target];
  });
}
const entries = files(payload).map(file => path.relative(payload, file).replaceAll('\\', '/'));
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, ...entries.flatMap(entry => ['-C', payload, entry])]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'PatchCommonMaterialBonds', '--verify', baseline, candidate]);
run(path.join(jdk, 'java.exe'), ['-cp', tools, 'ZipStreamCheck', candidate]);
const runtimeClasspath = [candidate, tools, dependencies].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtimeClasspath, 'CommonMaterialBondCheck', baseline, path.join(task, 'coverage-report.json')]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtimeClasspath, 'AccessoryBondRulesCheck']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtimeClasspath, 'SwordCavityRulesCheck', path.join(task, 'cavity-rules-report.json')]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtimeClasspath, 'MaterialBowCombatRulesCheck']);
run(path.join(jdk, 'jar.exe'), ['--extract', '--file', candidate, ...resourceEntries,
  'net/farmingtales/ftboss/client/StatueWeaponBondGuideScreen.class', 'assets/ftboss/food_socket_attributes/traits.json'], finalResources);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'WeaponBondGuideCoverageCheck', path.join(finalResources, 'assets/ftboss'),
  path.join(finalResources, 'net/farmingtales/ftboss/client/StatueWeaponBondGuideScreen.class'), '--catalog-only']);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
for (const file of tracked) if (hash(file) !== sourceHashes[file]) throw new Error('Source changed during build: ' + file);
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: hash(baseline),
  candidate, candidateSha256: hash(candidate), entries, sources: sourceHashes, gameplayTested: false }, null, 2) + '\n');
console.log('PASS candidate ready: ' + candidate);
