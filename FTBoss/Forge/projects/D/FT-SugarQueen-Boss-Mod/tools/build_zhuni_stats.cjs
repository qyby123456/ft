const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/xuanlan-zhuni-fixed-stats-20261027'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw Error('Task escapes project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const instance = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2');
const live = path.join(instance, 'mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const mapped = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge',
  '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const mappings = path.join(project, 'build/createMcpToSrg/output.tsrg');
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
  const result = spawnSync(executable, argumentsList, { cwd: project, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, windowsHide: true });
  fs.appendFileSync(log, `${executable} ${argumentsList.join(' ')}\n${result.stdout || ''}${result.stderr || ''}\n`);
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || Error(`Command failed (${result.status})`);
}

const baseline = path.join(task, 'baseline.jar');
const baselineHash = hash(live);
if (!fs.existsSync(baseline)) fs.copyFileSync(live, baseline);
if (hash(baseline) !== baselineHash) throw Error('Installed baseline changed; use a fresh task directory');
const suffixes = ['boss/ai/ZhuniPairRules', 'boss/ai/ManorCombatHalving', 'turret/ZhuniEntity'];
const sources = suffixes.map(name => path.join(project, 'src/main/java/net/farmingtales/ftboss', name + '.java'));
const tools = ['runtime_patches/ZhuniStatsPatch', 'runtime_patches/ZicaiVoidPatch',
  'runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames',
  'verification/WeaponUiJarClosureCheck', 'verification/ZipStreamCheck'].map(name => path.join(project, 'tools', name + '.java'));
const tracked = [...sources, ...tools, __filename, path.join(project, 'tools/deploy_zhuni_stats.ps1'),
  path.join(project, 'tools/verification/check_manor_combat_halving.cjs'),
  path.join(project, 'tools/verification/ManorCombatHalvingFixture.java'),
  path.join(project, 'tools/verification/check_jar_zip_rules.cjs')];
const sourceHashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
run(process.execPath, [path.join(project, 'tools/verification/check_manor_combat_halving.cjs'), task]);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17',
  '-cp', asm.join(path.delimiter), '-d', toolsDirectory, ...tools]);
const toolClasspath = [toolsDirectory, ...asm].join(path.delimiter);
const baselineMapped = path.join(task, 'baseline-mapped.jar');
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, baseline, baselineMapped]);
let line = 'Class-Path: ' + [...new Set([mapped, baselineMapped, ...libraries, ...mods])].map(file => {
  if (!fs.existsSync(file)) throw Error('Missing dependency: ' + file);
  return pathToFileURL(file).href.replaceAll('[', '%5B').replaceAll(']', '%5D');
}).join(' ');
let manifest = 'Manifest-Version: 1.0\r\n';
while (line.length > 70) { manifest += line.slice(0, 70) + '\r\n'; line = ' ' + line.slice(70); }
const manifestPath = path.join(task, 'dependencies.mf');
fs.writeFileSync(manifestPath, manifest + line + '\r\n\r\n', 'ascii');
const dependencies = path.join(task, 'dependencies.jar');
run(path.join(jdk, 'jar.exe'), ['--create', '--file', dependencies, '--manifest', manifestPath]);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-implicit:none',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', classes, ...sources]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [classes, toolClasspath, dependencies].join(path.delimiter),
  'ZhuniStatsPatch', 'payload', classes, payload, mappings]);
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [toolClasspath, dependencies].join(path.delimiter),
  'ZhuniStatsPatch', 'check', baseline, candidate, mappings]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'WeaponUiJarClosureCheck', candidate,
  ...suffixes.map(name => 'net/farmingtales/ftboss/' + name)]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(live) !== baselineHash) throw Error('Installed JAR changed during build');
for (const [file, expected] of Object.entries(sourceHashes)) if (hash(file) !== expected) throw Error('Source changed during build: ' + file);
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: baselineHash,
  candidate, candidateSha256: hash(candidate), sourceHashes, gameTested: false, docsTouched: false,
  changedClasses: suffixes, docsProposal: path.join(project, '_work/docs-proposals/xuanlan-zhuni-fixed-stats.md') }, null, 2) + '\n');
console.log('PASS verified fixed-stats candidate: ' + candidate);
