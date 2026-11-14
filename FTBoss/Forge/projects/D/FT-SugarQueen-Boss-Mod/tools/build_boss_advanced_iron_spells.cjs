const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');
const { isDeepStrictEqual } = require('node:util');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/advanced-iron-spells-20261103-a2'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must stay inside project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const instance = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2');
const live = path.join(instance, 'mods/qys-bosses.jar');
const baseline = path.join(task, 'baseline-qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const mapped = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge',
  '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const mappings = path.join(project, 'build/createMcpToSrg/output.tsrg');
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
const sources = ['integ/spells/BossIronSpellLevels', 'mixin/BossAdvancedIronSpellMixin', 'integ/WizardSummon',
  'boss/IronSpellBridge', 'integ/spells/LemonSpellBarrage'].map(name => path.join(project, `src/main/java/net/farmingtales/ftboss/${name}.java`));
const tools = ['tools/runtime_patches/BossAdvancedIronSpellPatch.java', 'tools/runtime_patches/ZicaiVoidPatch.java',
  'tools/runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames.java', 'tools/verification/ZipStreamCheck.java']
  .map(name => path.join(project, name));
const configPath = path.join(project, 'src/main/resources/ftboss.mixins.json');
const checkSource = path.join(project, 'tools/verification/BossIronSpellLevelsCheck.java');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const walk = directory => fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
  const file = path.join(directory, entry.name);
  return entry.isDirectory() ? walk(file) : [file];
});
const fixtures = walk(path.join(project, 'tools/verification/boss-iron-levels-fixture')).filter(file => file.endsWith('.java'));
fs.mkdirSync(task, { recursive: true });
const baselineHash = hash(live);
if (fs.existsSync(baseline) && hash(baseline) !== baselineHash) throw new Error('Live baseline changed; use a fresh task');
if (!fs.existsSync(baseline)) fs.copyFileSync(live, baseline);
const tracked = [...sources, ...tools, ...fixtures, configPath, checkSource,
  path.join(project, 'tools/deploy_boss_advanced_iron_spells.ps1'), __filename];
const hashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
for (const name of ['tools', 'classes', 'fixture-classes', 'payload', 'empty-sourcepath']) fs.mkdirSync(path.join(task, name), { recursive: true });
const transcript = path.join(task, 'verification.log');
fs.writeFileSync(transcript, '');
function run(executable, argumentsList) {
  const result = spawnSync(executable, argumentsList, { cwd: project, encoding: 'utf8', maxBuffer: 24 * 1024 * 1024, windowsHide: true });
  fs.appendFileSync(transcript, (result.stdout || '') + (result.stderr || ''));
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error) throw result.error;
  if (result.status !== 0) throw new Error(`${path.basename(executable)} failed: ${result.status}`);
  return result.stdout;
}
function classpathJar(name, files) {
  const manifest = path.join(task, `${name}.mf`);
  const classpath = 'Class-Path: ' + [...new Set(files)].map(file => pathToFileURL(file).href).join(' ');
  const folded = classpath.match(/.{1,68}/g).join('\r\n ');
  fs.writeFileSync(manifest, 'Manifest-Version: 1.0\r\n' + folded + '\r\n\r\n');
  const jar = path.join(task, `${name}.jar`);
  run(path.join(jdk, 'jar.exe'), ['--create', '--file', jar, '--manifest', manifest, '-C', path.join(task, 'empty-sourcepath'), '.']);
  return jar;
}
const toolDirectory = path.join(task, 'tools');
const classes = path.join(task, 'classes');
const payload = path.join(task, 'payload');
run(path.join(jdk, 'javac.exe'), ['-J-Dfile.encoding=UTF-8', '-encoding', 'UTF-8', '-proc:none', '--release', '17',
  '-cp', asm.join(path.delimiter), '-d', toolDirectory, ...tools]);
const toolClasspath = [toolDirectory, ...asm].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, baseline, path.join(task, 'baseline-mapped.jar')]);
const version = JSON.parse(fs.readFileSync(path.join(instance, 'FarmingTales_ForgeⅡ v1.5.2.json'), 'utf8'));
const libraries = version.libraries.filter(library => !library.name.includes('natives')).map(library => {
  const [group, artifact, release, classifier] = library.name.split(':');
  const relative = library.downloads?.artifact?.path
    || `${group.replaceAll('.', '/')}/${artifact}/${release}/${artifact}-${release}${classifier ? '-' + classifier : ''}.jar`;
  return path.join(minecraft, 'libraries', relative);
}).filter(file => fs.existsSync(file));
libraries.push(...['fmlcore', 'javafmllanguage', 'lowcodelanguage', 'mclanguage'].map(name =>
  path.join(minecraft, `libraries/net/minecraftforge/${name}/1.20.1-47.4.10/${name}-1.20.1-47.4.10.jar`)));
const dependencies = classpathJar('compile-dependencies', [mapped, path.join(task, 'baseline-mapped.jar'),
  ...walk(path.join(project, 'libs')).filter(file => file.endsWith('.jar')), ...libraries]);
run(path.join(jdk, 'javac.exe'), ['-J-Dfile.encoding=UTF-8', '-encoding', 'UTF-8', '-proc:none', '--release', '17',
  '-implicit:none', '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', classes, ...sources]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [toolClasspath, classes, dependencies].join(path.delimiter),
  'BossAdvancedIronSpellPatch', 'patch', baseline, classes, mappings, payload]);
const configDump = path.join(task, 'runtime-config');
fs.mkdirSync(configDump, { recursive: true });
const extraction = spawnSync(path.join(jdk, 'jar.exe'), ['--extract', '--file', baseline, 'ftboss.mixins.json'],
  { cwd: configDump, encoding: 'utf8', windowsHide: true });
if (extraction.status !== 0) throw new Error('Cannot read baseline Mixin config');
const config = JSON.parse(fs.readFileSync(path.join(configDump, 'ftboss.mixins.json'), 'utf8'));
if (config.mixins.includes('BossAdvancedIronSpellMixin')) throw new Error('Mixin already registered in baseline');
config.mixins.unshift('BossAdvancedIronSpellMixin');
fs.writeFileSync(path.join(payload, 'ftboss.mixins.json'), JSON.stringify(config, null, 2) + '\n');
const sourceConfig = JSON.parse(fs.readFileSync(configPath, 'utf8'));
if (!isDeepStrictEqual({ ...sourceConfig, mixins: [...sourceConfig.mixins].sort() }, { ...config, mixins: [...config.mixins].sort() }))
  throw new Error('Source and runtime Mixin configuration diverged beyond existing registration order');
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net', '-C', payload, 'ftboss.mixins.json']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', toolClasspath, 'BossAdvancedIronSpellPatch', 'verify', baseline, candidate]);
const fixtureClasses = path.join(task, 'fixture-classes');
run(path.join(jdk, 'javac.exe'), ['-J-Dfile.encoding=UTF-8', '-encoding', 'UTF-8', '-proc:none', '--release', '17',
  '-cp', asm.join(path.delimiter), '-d', fixtureClasses, sources[0], ...fixtures, checkSource]);
const native = fs.readdirSync(path.join(instance, 'mods')).filter(name => name.includes('irons_spellbooks') && name.endsWith('.jar'));
if (native.length !== 1) throw new Error('Expected one installed Iron spellbook JAR');
const nativeJar = path.join(instance, 'mods', native[0]);
const behavior = run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [fixtureClasses, ...asm].join(path.delimiter),
  'BossIronSpellLevelsCheck', candidate, nativeJar]);
run(process.execPath, [path.join(project, 'tools/verification/check_no_camera_shake_attacks.cjs')]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(live) !== baselineHash || hash(baseline) !== baselineHash) throw new Error('Baseline changed while building');
for (const [file, expected] of Object.entries(hashes)) if (hash(file) !== expected) throw new Error('Source changed while building: ' + file);
const report = { status: 'PASS_OFFLINE', baseline, baselineSha256: baselineHash, candidate, candidateSha256: hash(candidate),
  nativeJar, nativeSha256: hash(nativeJar), sources: hashes, behavior: behavior.trim(), docsTouched: false, gameplayTested: false,
  changedRuntimeMethods: 9, addedRuntimeClasses: 2, addedNativeSpells: 6 };
fs.writeFileSync(path.join(task, 'verification.json'), JSON.stringify(report, null, 2) + '\n');
console.log('PASS_OFFLINE: scoped advanced Iron spells artifact retained at ' + candidate);
