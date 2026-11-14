const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');

const root = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(root, '_work/xuanlan-zhuni-pair-20261027'));
if (!task.startsWith(path.join(root, '_work') + path.sep)) throw Error('Task escapes project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const instance = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2');
const live = path.join(instance, 'mods/qys-bosses.jar');
const java = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const mapped = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge',
  '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const mappings = path.join(root, 'build/createMcpToSrg/output.tsrg');
const version = JSON.parse(fs.readFileSync(path.join(instance, 'FarmingTales_ForgeⅡ v1.5.2.json'), 'utf8'));
const libraries = version.libraries.filter(library => !library.name.includes('natives')).map(library => {
  const [group, artifact, release, classifier] = library.name.split(':');
  const relative = library.downloads?.artifact?.path
    || `${group.replaceAll('.', '/')}/${artifact}/${release}/${artifact}-${release}${classifier ? '-' + classifier : ''}.jar`;
  return path.join(minecraft, 'libraries', relative);
}).filter(file => fs.existsSync(file));
libraries.push(...['fmlcore', 'javafmllanguage', 'lowcodelanguage', 'mclanguage'].map(name =>
  path.join(minecraft, `libraries/net/minecraftforge/${name}/1.20.1-47.4.10/${name}-1.20.1-47.4.10.jar`)));
const mods = fs.readdirSync(path.join(instance, 'mods')).filter(name => name.endsWith('.jar') && name !== 'qys-bosses.jar')
  .map(name => path.join(instance, 'mods', name));
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
for (const name of ['tools', 'classes', 'payload', 'empty-sourcepath']) fs.mkdirSync(path.join(task, name), { recursive: true });
const toolsDirectory = path.join(task, 'tools');
const classes = path.join(task, 'classes');
const payload = path.join(task, 'payload');
const log = path.join(task, 'build.log');
fs.writeFileSync(log, '');
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: false, state: 'building' }) + '\n');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');

function run(executable, argumentsList) {
  const result = spawnSync(executable, argumentsList, { cwd: root, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, windowsHide: true });
  fs.appendFileSync(log, `${executable} ${argumentsList.join(' ')}\n${result.stdout || ''}${result.stderr || ''}\n`);
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || Error(`Command failed (${result.status})`);
}

function dependencyJar(entries) {
  let line = 'Class-Path: ' + [...new Set(entries)].map(file => {
    if (!fs.existsSync(file)) throw Error('Missing dependency: ' + file);
    let address = pathToFileURL(file).href.replaceAll('[', '%5B').replaceAll(']', '%5D');
    if (fs.statSync(file).isDirectory()) address += '/';
    return address;
  }).join(' ');
  let manifest = 'Manifest-Version: 1.0\r\n';
  while (line.length > 70) { manifest += line.slice(0, 70) + '\r\n'; line = ' ' + line.slice(70); }
  const file = path.join(task, 'dependencies.mf');
  fs.writeFileSync(file, manifest + line + '\r\n\r\n', 'ascii');
  const output = path.join(task, 'dependencies.jar');
  run(path.join(java, 'jar.exe'), ['--create', '--file', output, '--manifest', file]);
  return output;
}

const baseline = path.join(task, 'baseline.jar');
const baselineHash = hash(live);
if (!fs.existsSync(baseline)) fs.copyFileSync(live, baseline);
if (hash(baseline) !== baselineHash) throw Error('Live baseline changed; use a fresh task directory');
const suffixes = ['boss/ai/ZhuniPairRules', 'boss/ai/ZhuniPairCombat', 'boss/ai/XuanlanEncounterLimits',
  'turret/ZhuniEntity', 'turret/ZhuniStage', 'turret/ZhuniTurretDrops'];
const sources = suffixes.map(name => path.join(root, 'src/main/java/net/farmingtales/ftboss', name + '.java'));
const tools = ['runtime_patches/ZhuniPairPatch', 'runtime_patches/ZicaiVoidPatch',
  'runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames',
  'verification/WeaponUiJarClosureCheck', 'verification/ZipStreamCheck'].map(name => path.join(root, 'tools', name + '.java'));
const check = path.join(root, 'tools/verification/ZhuniPairCheck.java');
const tracked = [...sources, ...tools, check, __filename, path.join(root, 'tools/deploy_zhuni_pair.ps1'),
  path.join(root, 'tools/verification/check_jar_zip_rules.cjs')];
const sourceHooks = Object.fromEntries(Object.entries({
  'entity/EasterBossEntity': ['ZhuniPairCombat.deferDeath(this, source)', 'ZhuniPairCombat.flywheelLimit(this)'],
  'boss/ai/ManorHealthBalance': ['ZhuniPairCombat.normalize(entity)'],
  'boss/ai/BossScaling': ['ZhuniPairCombat.maximumHealth(base * hpMul, boss)'],
  'turret/XuanlanTurretEntity': ['ZhuniPairCombat.turretNoAi(boss)', 'ZhuniPairCombat.turretInvulnerable(boss)',
    'ZhuniPairCombat.turretInvisible(boss)', 'ZhuniPairCombat.turretIntroHidden(boss)'],
}).map(([name, hooks]) => [path.join(root, 'src/main/java/net/farmingtales/ftboss', name + '.java'), hooks]));
function verifySourceHooks() {
  for (const [file, hooks] of Object.entries(sourceHooks)) {
    const source = fs.readFileSync(file, 'utf8');
    for (const hook of hooks) if (!source.includes(hook)) throw Error(`Source hook changed: ${file}: ${hook}`);
  }
}
verifySourceHooks();
const sourceHashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
run(path.join(java, 'javac.exe'), ['-J-Dfile.encoding=UTF-8', '-encoding', 'UTF-8', '-proc:none', '--release', '17',
  '-cp', asm.join(path.delimiter), '-d', toolsDirectory, ...tools]);
const toolClasspath = [toolsDirectory, ...asm].join(path.delimiter);
run(path.join(java, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, baseline, path.join(task, 'baseline-mapped.jar')]);
const dependencies = dependencyJar([mapped, path.join(task, 'baseline-mapped.jar'), ...libraries, ...mods]);
run(path.join(java, 'javac.exe'), ['-J-Dfile.encoding=UTF-8', '-encoding', 'UTF-8', '-proc:none', '--release', '17',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', classes, ...sources]);
run(path.join(java, 'java.exe'), ['-Xverify:all', '-cp', [toolClasspath, classes, dependencies].join(path.delimiter),
  'ZhuniPairPatch', baseline, classes, mappings, payload]);
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(java, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
run(path.join(java, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, candidate, path.join(task, 'candidate-mapped.jar')]);
run(path.join(java, 'javac.exe'), ['-J-Dfile.encoding=UTF-8', '-encoding', 'UTF-8', '-proc:none', '--release', '17',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', [classes, dependencies, ...asm].join(path.delimiter), '-d', toolsDirectory, check]);
const testClasspath = [toolClasspath, path.join(task, 'candidate-mapped.jar'), dependencies].join(path.delimiter);
run(path.join(java, 'java.exe'), ['-Xverify:all', '-cp', testClasspath, 'ZhuniPairCheck', baseline, candidate, root]);
run(path.join(java, 'java.exe'), ['-cp', toolClasspath, 'WeaponUiJarClosureCheck', candidate,
  ...suffixes.map(name => 'net/farmingtales/ftboss/' + name)]);
run(path.join(java, 'java.exe'), ['-cp', toolClasspath, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(root, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(live) !== baselineHash) throw Error('Live JAR changed during build; rebuild from the current baseline');
for (const [file, expected] of Object.entries(sourceHashes)) if (hash(file) !== expected) throw Error('Source changed during build: ' + file);
verifySourceHooks();
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: baselineHash,
  candidate, candidateSha256: hash(candidate), sourceHashes, sourceHooks, gameTested: false, docsTouched: false,
  docsProposal: path.join(root, '_work/docs-proposals/xuanlan-zhuni-pair.md') }, null, 2) + '\n');
console.log('PASS verified candidate: ' + candidate);
