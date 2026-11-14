const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2]);
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must be inside _work');
if (fs.existsSync(path.join(task, 'deployment-receipt.json'))) throw new Error('Already deployed');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const instance = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2');
const live = path.join(instance, 'mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const forge = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge');
const mapped = path.join(forge, '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const mappings = path.join(project, 'build/createMcpToSrg/output.tsrg');
const version = JSON.parse(fs.readFileSync(path.join(instance, 'FarmingTales_ForgeⅡ v1.5.2.json'), 'utf8'));
const libraries = version.libraries.filter(library => !library.name.includes('natives')).map(library => {
  const [group, artifact, release, classifier] = library.name.split(':');
  return path.join(minecraft, 'libraries', library.downloads?.artifact?.path
    || `${group.replaceAll('.', '/')}/${artifact}/${release}/${artifact}-${release}${classifier ? '-' + classifier : ''}.jar`);
}).filter(file => fs.existsSync(file));
libraries.push(...['fmlcore', 'javafmllanguage', 'lowcodelanguage', 'mclanguage'].map(name =>
  path.join(minecraft, `libraries/net/minecraftforge/${name}/1.20.1-47.4.10/${name}-1.20.1-47.4.10.jar`)));
const mods = fs.readdirSync(path.join(instance, 'mods')).filter(name => name.endsWith('.jar') && name !== 'qys-bosses.jar')
  .map(name => path.join(instance, 'mods', name));
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
for (const directory of ['classes', 'tool-classes', 'payload', 'empty-sourcepath']) fs.mkdirSync(path.join(task, directory), { recursive: true });
const classes = path.join(task, 'classes');
const toolClasses = path.join(task, 'tool-classes');
const payload = path.join(task, 'payload');
const log = path.join(task, 'build.log');
fs.writeFileSync(log, '');
if (fs.existsSync(path.join(task, 'build-report.json'))) fs.unlinkSync(path.join(task, 'build-report.json'));
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
function run(executable, args) {
  const result = spawnSync(executable, args, { cwd: project, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, windowsHide: true });
  fs.appendFileSync(log, `${executable} ${args.join(' ')}\n${result.stdout || ''}${result.stderr || ''}\n`);
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error(`Command failed (${result.status})`);
}
function classpathJar(entries, name) {
  let line = 'Class-Path: ' + [...new Set(entries)].map(file => {
    if (!fs.existsSync(file)) throw new Error('Missing dependency: ' + file);
    let address = pathToFileURL(file).href.replaceAll('[', '%5B').replaceAll(']', '%5D');
    if (fs.statSync(file).isDirectory()) address += '/';
    return address;
  }).join(' ');
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
const sources = ['util/DoraCloneRules', 'boss/ai/DoraPursuitClone']
  .map(name => path.join(project, 'src/main/java/net/farmingtales/ftboss', name + '.java'));
const tools = ['runtime_patches/DoraClonePatch', 'runtime_patches/ZicaiVoidPatch',
  'runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames',
  'verification/ZipStreamCheck', 'verification/WeaponUiJarClosureCheck', 'verification/DoraCloneLifecycleCheck',
  'verification/DoraCloneRuntimeCheck']
  .map(name => path.join(project, 'tools', name + '.java'));
const ruleCheck = path.join(project, 'tools/verification/DoraCloneRulesCheck.java');
const tracked = [...sources, ...tools, ruleCheck, __filename, path.join(project, 'tools/deploy_dora_clone.ps1'),
  ...['boss/BossHandler', 'boss/ManorFight', 'boss/ai/BossScaling', 'boss/ai/ManorHealthBalance', 'boss/ai/BossMercy']
    .map(name => path.join(project, 'src/main/java/net/farmingtales/ftboss', name + '.java'))];
const hashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', asm.join(path.delimiter),
  '-d', toolClasses, ...tools]);
const toolClasspath = [toolClasses, ...asm].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, baseline, path.join(task, 'baseline-mapped.jar')]);
const dependencies = classpathJar([mapped, path.join(task, 'baseline-mapped.jar'), ...libraries, ...mods], 'dependencies');
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', classes, ...sources, ruleCheck]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classes, 'DoraCloneRulesCheck']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', toolClasspath, 'DoraCloneLifecycleCheck', classes]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [toolClasspath, dependencies].join(path.delimiter),
  'DoraClonePatch', baseline, classes, mappings, payload]);
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
const runtimeForge = path.join(forge, '1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar');
const runtime = classpathJar([candidate, toolClasses, runtimeForge, ...asm, ...libraries, ...mods], 'runtime');
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtime, 'DoraCloneRuntimeCheck', baseline, candidate]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'WeaponUiJarClosureCheck', candidate,
  'net/farmingtales/ftboss/boss/ai/DoraPursuitClone', 'net/farmingtales/ftboss/util/DoraCloneRules']);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
run('C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe', ['-c',
  'import sys,zipfile,pathlib; baseline,candidate,payload=sys.argv[1:]; allowed={p.relative_to(payload).as_posix() for p in pathlib.Path(payload).rglob("*.class")}; before=zipfile.ZipFile(baseline); after=zipfile.ZipFile(candidate); old=set(before.namelist()); new=set(after.namelist()); assert old<=new; changed={name for name in old if before.read(name)!=after.read(name)}; assert changed<=allowed,(changed-allowed); assert new-old<=allowed,(new-old-allowed); print("PASS ZIP preservation:",len(old-changed),"unchanged entries;",len(changed),"changed;",len(new-old),"added")',
  baseline, candidate, payload]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed during build');
for (const [file, expected] of Object.entries(hashes)) if (hash(file) !== expected) throw new Error('Source changed: ' + file);
for (const file of tracked) {
  const snapshot = path.join(task, 'sources', path.relative(project, file));
  fs.mkdirSync(path.dirname(snapshot), { recursive: true });
  fs.copyFileSync(file, snapshot);
}
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: baselineHash,
  candidate, candidateSha256: hash(candidate), sources: hashes, gameplayTested: false, docsTouched: false,
  neoForgeTouched: false }, null, 2) + '\n');
console.log('PASS: verified Dora pursuit clone candidate ' + candidate);
