const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');
const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/manor-combat-halving-20261027'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must remain inside _work');
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
const classes = path.join(task, 'classes');
const toolsDirectory = path.join(task, 'tools');
const payload = path.join(task, 'payload');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
fs.writeFileSync(path.join(task, 'build.log'), '');
function run(executable, args) {
  const result = spawnSync(executable, args, { cwd: project, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, windowsHide: true });
  const output = (result.stdout || '') + (result.stderr || '');
  fs.appendFileSync(path.join(task, 'build.log'), output);
  process.stdout.write(output);
  if (result.status !== 0) throw new Error(`${executable} failed: ${result.status}`);
}
function classpathJar(name, files) {
  let line = 'Class-Path: ' + files.map(file => pathToFileURL(file).href.replaceAll('[', '%5B').replaceAll(']', '%5D')).join(' ');
  let manifest = 'Manifest-Version: 1.0\r\n';
  while (line.length > 70) { manifest += line.slice(0, 70) + '\r\n'; line = ' ' + line.slice(70); }
  const file = path.join(task, name + '.mf');
  fs.writeFileSync(file, manifest + line + '\r\n\r\n', 'ascii');
  const output = path.join(task, name + '.jar');
  run(path.join(jdk, 'jar.exe'), ['--create', '--file', output, '--manifest', file]);
  return output;
}
const baseline = path.join(task, 'baseline.jar');
const baselineHash = hash(live);
if (!fs.existsSync(baseline)) fs.copyFileSync(live, baseline);
if (hash(baseline) !== baselineHash) throw new Error('Baseline changed; use a fresh task directory');
const source = path.join(project, 'src/main/java/net/farmingtales/ftboss/boss/ai/ManorCombatHalving.java');
const tools = ['runtime_patches/ManorCombatHalvingPatch', 'runtime_patches/ZicaiVoidPatch',
  'runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames', 'verification/ManorCombatHalvingCheck',
  'verification/WeaponUiJarClosureCheck', 'verification/ZipStreamCheck'].map(name => path.join(project, 'tools', name + '.java'));
const tracked = [source, ...tools, __filename,
  ...['verification/check_manor_combat_halving.cjs', 'verification/ManorCombatHalvingFixture.java',
    'deploy_manor_combat_halving.ps1'].map(name => path.join(project, 'tools', name)),
  ...['BlueFireEffect', 'ShadowErosionEffect'].map(name => path.join(project, 'src/main/java/net/farmingtales/ftboss/effect', name + '.java'))];
const hashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
run(process.execPath, [path.join(project, 'tools/verification/check_manor_combat_halving.cjs'), task]);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-cp', asm.join(path.delimiter),
  '-d', toolsDirectory, ...tools]);
const toolClasspath = [toolsDirectory, ...asm].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, baseline, path.join(task, 'baseline-mapped.jar')]);
const dependencies = classpathJar('compile-dependencies', [mapped, path.join(task, 'baseline-mapped.jar'), ...libraries, ...mods]);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-implicit:none',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', classes, source]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [toolClasspath, classes, dependencies].join(path.delimiter),
  'ManorCombatHalvingPatch', baseline, classes, mappings, payload]);
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', toolClasspath, 'ManorCombatHalvingCheck', baseline, candidate]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'WeaponUiJarClosureCheck', candidate,
  'net/farmingtales/ftboss/boss/ai/ManorCombatHalving', 'net/farmingtales/ftboss/effect/BlueFireEffect',
  'net/farmingtales/ftboss/effect/ShadowErosionEffect']);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed during build');
for (const [file, expected] of Object.entries(hashes)) if (hash(file) !== expected) throw new Error('Source changed: ' + file);
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: baselineHash,
  candidate, candidateSha256: hash(candidate), sources: hashes, gameplayTested: false, docsTouched: false,
  effectiveManorBaseHealth: 4000, effectiveSupportBaseHealth: 1250, outgoingDamageMultiplier: 0.5,
  existingDifficultyAndMultiplayerScalingPreserved: true }, null, 2) + '\n');
console.log('PASS: manor and summon halving candidate verified; live instance not modified by build');
