const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/lemon-summon-limit-20261027'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must stay in project _work');
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
const transcript = path.join(task, 'build.log');
fs.writeFileSync(transcript, '');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
function run(executable, argumentsList) {
  const result = spawnSync(executable, argumentsList, { cwd: project, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, windowsHide: true });
  fs.appendFileSync(transcript, `${executable} ${argumentsList.join(' ')}\n${result.stdout || ''}${result.stderr || ''}\n`);
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error(`Command failed (${result.status})`);
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
if (hash(baseline) !== baselineHash) throw new Error('Baseline changed; choose a fresh task directory');
const sources = ['util/LemonBalanceRules', 'boss/ai/LemonSummonLimit'].map(name =>
  path.join(project, 'src/main/java/net/farmingtales/ftboss', name + '.java'));
const tools = ['runtime_patches/LemonSummonLimitPatch', 'runtime_patches/ZicaiVoidPatch',
  'runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames',
  'verification/LemonSummonLimitCheck', 'verification/FoodWeaponBuildLinkCheck', 'verification/WeaponUiJarClosureCheck', 'verification/ZipStreamCheck']
  .map(name => path.join(project, 'tools', name + '.java'));
const tracked = [...sources, ...tools, __filename,
  ...['verification/check_lemon_summon_limit.cjs', 'verification/LemonSummonLimitFixture.java', 'deploy_lemon_summon_limit.ps1',
    'verification/LemonBalanceFixture.java', 'verification/LemonBalanceCheck.java', 'verification/check_lemon_summon_sacrifice.cjs']
    .map(name => path.join(project, 'tools', name)),
  ...['boss/ManorFightMembers.java', 'entity/EasterBossEntity.java'].map(name => path.join(project, 'src/main/java/net/farmingtales/ftboss', name))];
const hashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-cp', asm.join(path.delimiter), '-d', toolsDirectory, ...tools]);
const toolClasspath = [toolsDirectory, ...asm].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'LemonSummonLimitPatch', baseline, path.join(task, 'empty-sourcepath'), mappings, payload]);
const bridge = path.join(task, 'bridge.jar');
fs.copyFileSync(baseline, bridge);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', bridge, '-C', payload, 'net']);
const named = path.join(task, 'baseline-mapped.jar');
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, bridge, named]);
const dependencies = classpathJar('compile-dependencies', [mapped, named, ...libraries, ...mods]);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-implicit:none',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', classes, ...sources]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [toolClasspath, classes, dependencies].join(path.delimiter),
  'LemonSummonLimitPatch', baseline, classes, mappings, payload]);
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', toolClasspath, 'LemonSummonLimitCheck', baseline, candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_lemon_summon_limit.cjs'), task]);
run(process.execPath, [path.join(project, 'tools/verification/check_lemon_summon_sacrifice.cjs')]);
run(process.execPath, [path.join(project, 'tools/verification/check_lemon_inherited_damage.cjs')]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'WeaponUiJarClosureCheck', candidate, '--all',
  'net/farmingtales/ftboss/boss/ai/LemonSummonLimit']);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
const candidateNamed = path.join(task, 'candidate-mapped.jar');
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, candidate, candidateNamed]);
const linkDependencies = classpathJar('link-dependencies', [candidateNamed, mapped, ...libraries, ...mods]);
const production = path.join(task, 'production');
fs.mkdirSync(production, { recursive: true });
for (const name of ['util/LemonBalanceRules', 'boss/ai/LemonSummonLimit']) {
  const result = spawnSync(path.join(jdk, 'jar.exe'), ['--extract', '--file', candidateNamed,
    'net/farmingtales/ftboss/' + name + '.class'], { cwd: production, encoding: 'utf8', windowsHide: true });
  if (result.status !== 0) throw new Error('Production extraction failed: ' + result.stderr);
}
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [toolClasspath, linkDependencies].join(path.delimiter),
  'FoodWeaponBuildLinkCheck', production, candidateNamed]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed while building; rebuild from current baseline');
for (const [file, expected] of Object.entries(hashes)) if (hash(file) !== expected) throw new Error('Source changed while building: ' + file);
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: baselineHash,
  candidate, candidateSha256: hash(candidate), sources: hashes, gameplayTested: false, docsTouched: false,
  docsProposal: path.join(project, '_work/docs-proposals/lemon-summon-limit.md') }, null, 2) + '\n');
console.log('PASS verified Lemon summon-limit candidate: ' + candidate);
