const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/qizai-ultimate-balance-20261026'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must stay inside project _work');
const instance = path.join(process.env.APPDATA, '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2');
const target = path.join(instance, 'mods/qys-bosses.jar');
const jdk = path.join(process.env.APPDATA, '.minecraft/runtime/java-runtime-gamma-snapshot/bin');
const dependencyRoot = path.join(project, '_work/qizai-barrage-spell-20261026-final');
const compileDependencies = path.join(dependencyRoot, 'compile-dependencies.jar');
const runtimeDependencies = path.join(dependencyRoot, 'runtime-dependencies.jar');
const classes = path.join(task, 'classes');
const payload = path.join(task, 'payload');
fs.mkdirSync(classes, { recursive: true });
fs.mkdirSync(payload, { recursive: true });
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const baseline = path.join(task, 'baseline.jar');
if (!fs.existsSync(baseline)) fs.copyFileSync(target, baseline);
const baselineHash = hash(baseline);
if (hash(target) !== baselineHash) throw new Error('Installed baseline changed; choose a fresh task directory');
const log = path.join(task, 'verification.log');
fs.writeFileSync(log, '');
function run(executable, argumentsList, cwd = project) {
  const result = spawnSync(executable, argumentsList, { cwd, encoding: 'utf8', windowsHide: true, maxBuffer: 16 * 1024 * 1024 });
  fs.appendFileSync(log, `${result.stdout || ''}${result.stderr || ''}\n`);
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error(`Command failed (${result.status})`);
}
const sources = [
  'src/main/java/net/farmingtales/ftboss/integ/spells/ManorSignatureSpell.java',
  'src/main/java/net/farmingtales/ftboss/util/ManorSpellRules.java',
  'src/main/java/net/farmingtales/ftboss/util/ManorSpellCastRules.java',
  'src/main/java/net/farmingtales/ftboss/util/QizaiBarrageRules.java',
  'tools/runtime_patches/QizaiUltimateTimingPatch.java',
  'tools/verification/QizaiUltimateTimingCheck.java',
  'tools/verification/ManorSpellTimingCheck.java',
  'tools/verification/FoodWeaponBuildLinkCheck.java',
  'tools/verification/ZipStreamCheck.java'
].map(file => path.join(project, file));
const tracked = [...sources, __filename,
  path.join(project, 'src/main/java/net/farmingtales/ftboss/integ/spells/QizaiBarrageSpell.java'),
  ...['zh_cn', 'en_us'].map(locale => path.join(project, `src/main/resources/assets/ftboss/lang/${locale}.json`))];
const hashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
run(path.join(jdk, 'javac.exe'), ['-J-Duser.language=en', '-encoding', 'UTF-8', '--release', '17', '-proc:none',
  '-cp', baseline + path.delimiter + compileDependencies, '-d', classes, ...sources]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classes + path.delimiter + compileDependencies,
  'QizaiUltimateTimingPatch', baseline, classes, payload]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classes + path.delimiter + baseline + path.delimiter + runtimeDependencies,
  'ManorSpellTimingCheck', path.join(task, 'spell-cast-times.csv')]);
run(path.join(jdk, 'jar.exe'), ['--extract', '--file', baseline,
  'assets/ftboss/lang/zh_cn.json', 'assets/ftboss/lang/en_us.json'], payload);
for (const locale of ['zh_cn', 'en_us']) {
  const relative = `assets/ftboss/lang/${locale}.json`;
  const output = path.join(payload, relative);
  const language = JSON.parse(fs.readFileSync(output, 'utf8'));
  const source = JSON.parse(fs.readFileSync(path.join(project, 'src/main/resources', relative), 'utf8'));
  for (const id of ['qizai_star_inferno', 'qizai_laser_barrage'])
    language[`spell.ftboss.${id}.guide`] = source[`spell.ftboss.${id}.guide`];
  fs.writeFileSync(output, JSON.stringify(language, null, 2) + '\n');
}
function walk(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry =>
    entry.isDirectory() ? walk(path.join(directory, entry.name)) : [path.join(directory, entry.name)]);
}
const entries = walk(payload).map(file => path.relative(payload, file).replaceAll('\\', '/')).sort();
if (entries.length !== 7) throw new Error('Expected exactly five classes and two language files');
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, ...entries.flatMap(entry => ['-C', payload, entry])]);
const runtimeClasspath = candidate + path.delimiter + classes + path.delimiter + runtimeDependencies;
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtimeClasspath, 'QizaiUltimateTimingCheck', baseline, candidate]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtimeClasspath, 'FoodWeaponBuildLinkCheck', payload, candidate]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classes, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(target) !== baselineHash) throw new Error('Installed baseline changed during build');
for (const file of tracked) {
  if (hash(file) !== hashes[file]) throw new Error('Source changed during build: ' + file);
  const snapshot = path.join(task, 'sources', path.relative(project, file));
  fs.mkdirSync(path.dirname(snapshot), { recursive: true });
  fs.copyFileSync(file, snapshot);
}
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline: target,
  baselineSha256: baselineHash, candidate, candidateSha256: hash(candidate), entries, sources: hashes,
  runtimeDependencies, gameplayTested: false, docsTouched: false,
  spells: { qizai_star_inferno: { cooldownSeconds: 90, castTicks: 300, mana: 600 },
    qizai_laser_barrage: { cooldownSeconds: 90, castTicks: 300, mana: 600 } } }, null, 2) + '\n');
console.log('PASS timing candidate: ' + candidate);
