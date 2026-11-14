const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/manor-reward-crash-20261027'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task escapes project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const instance = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2');
const live = path.join(instance, 'mods/qys-bosses.jar');
const baseline = path.join(task, 'baseline.jar');
const candidate = path.join(task, 'qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const hook = 'net/farmingtales/ftboss/integ/spells/ManorRewardBalance';
const mappings = path.join(project, 'build/createMcpToSrg/output.tsrg');
const source = path.join(project, 'src/main/java', hook + '.java');
const check = path.join(project, 'tools/verification/ManorPlayerBalanceCheck.java');
const tools = ['runtime_patches/ManorRewardDamagePatch', 'runtime_patches/ZicaiVoidPatch',
  'runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames', 'verification/ZipStreamCheck',
  'verification/WeaponUiJarClosureCheck'].map(name => path.join(project, 'tools', name + '.java'));
const gates = path.join(project, 'tools/verification/check_jar_zip_rules.cjs');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const sourceHashes = Object.fromEntries([source, check, mappings, ...tools, gates, __filename].map(file => [file, hash(file)]));
for (const directory of ['classes', 'tools', 'payload', 'empty-sourcepath']) fs.mkdirSync(path.join(task, directory), { recursive: true });
if (fs.existsSync(candidate)) throw new Error('Candidate already exists; use a fresh task path');
if (!fs.existsSync(baseline)) fs.copyFileSync(live, baseline);
const baselineHash = hash(baseline);
if (hash(live) !== baselineHash) throw new Error('Installed baseline changed; use a fresh task path');
const transcript = path.join(task, 'build.log');
fs.writeFileSync(transcript, '');
function run(executable, args, expectLinkageFailure = false) {
  const result = spawnSync(executable, args, { cwd: project, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, windowsHide: true });
  const output = (result.stdout || '') + (result.stderr || '');
  fs.appendFileSync(transcript, output);
  if (expectLinkageFailure) {
    if (result.status === 0 || !output.includes('NoSuchMethodError') || !output.includes('getEntity')) {
      throw new Error('Broken baseline must reproduce the production getter linkage failure: ' + output);
    }
    console.log('PASS negative control: original installed class reproduces NoSuchMethodError');
  } else {
    process.stdout.write(output);
    if (result.status !== 0) throw new Error(`${executable} failed: ${result.status}`);
  }
}
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
const toolsDirectory = path.join(task, 'tools');
const toolClasspath = [toolsDirectory, ...asm].join(path.delimiter);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-cp', asm.join(path.delimiter),
  '-d', toolsDirectory, ...tools]);
const mappedBaseline = path.join(task, 'baseline-mapped.jar');
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, baseline, mappedBaseline]);
const mappedForge = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge',
  '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
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
let line = 'Class-Path: ' + [mappedForge, mappedBaseline, ...libraries, ...mods]
  .map(file => pathToFileURL(file).href.replaceAll('[', '%5B').replaceAll(']', '%5D')).join(' ');
let manifest = 'Manifest-Version: 1.0\r\n';
while (line.length > 70) { manifest += line.slice(0, 70) + '\r\n'; line = ' ' + line.slice(70); }
const manifestFile = path.join(task, 'compile-dependencies.mf');
fs.writeFileSync(manifestFile, manifest + line + '\r\n\r\n', 'ascii');
const dependencies = path.join(task, 'compile-dependencies.jar');
run(path.join(jdk, 'jar.exe'), ['--create', '--file', dependencies, '--manifest', manifestFile]);
const classes = path.join(task, 'classes');
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-implicit:none',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', classes, source]);
const payload = path.join(task, 'payload');
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', toolClasspath, 'ManorRewardDamagePatch', 'remap',
  path.join(classes, hook + '.class'), mappings, payload]);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17',
  '-cp', [baseline, toolClasspath].join(path.delimiter), '-d', toolsDirectory, check]);
const runtimeClasspath = [baseline, toolClasspath].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtimeClasspath, 'ManorPlayerBalanceCheck', 'event-only', baseline], true);
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
const candidateClasspath = [candidate, toolClasspath].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', candidateClasspath, 'ManorPlayerBalanceCheck', 'event-only', candidate]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', candidateClasspath, 'ManorPlayerBalanceCheck', baseline, candidate, 'skip-archive']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', toolClasspath, 'ManorRewardDamagePatch', 'verify', baseline, candidate]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'WeaponUiJarClosureCheck', candidate, hook]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'ZipStreamCheck', candidate]);
run(process.execPath, [gates, candidate]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed during build');
for (const [file, expected] of Object.entries(sourceHashes)) if (hash(file) !== expected) throw new Error('Source changed: ' + file);
fs.copyFileSync(source, path.join(task, 'ManorRewardBalance.after.java'));
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: baselineHash,
  candidate, candidateSha256: hash(candidate), sources: sourceHashes, changedEntries: [hook + '.class'],
  negativeControlReproduced: true, productionNameEventAssertions: 128, gameplayTested: false, docsTouched: false,
  docsImpact: 'none: linkage repair only; balance, resources, identifiers and persistence unchanged' }, null, 2) + '\n');
console.log('PASS verified single-class crash repair; deployment not performed by build');
