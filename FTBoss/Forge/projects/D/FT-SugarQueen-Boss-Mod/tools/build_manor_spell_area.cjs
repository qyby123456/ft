const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/manor-spell-area-20261027'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task escapes project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const instance = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2');
const live = path.join(instance, 'mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const mappings = path.join(project, 'build/createMcpToSrg/output.tsrg');
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const sourceRoot = path.join(project, 'src/main/java/net/farmingtales/ftboss');
const compileSources = ['util/ManorSpellAreaRules', 'util/ManorSpellCastRules', 'integ/spells/BossSpellTuning',
  'skill/SpellVisuals', 'integ/spells/ZicaiVoidSpells', 'skill/QizaiConstellation', 'integ/spells/QizaiSpell',
  'integ/spells/QizaiInfernoEffects', 'integ/spells/QizaiBarrageEffects', 'client/render/ZicaiVoidVisual',
  'client/render/QizaiSigilRenderer', 'client/render/QizaiLaserBarrageVisual', 'client/render/QizaiLaserBarrageModel',
  'skill/SpellVisualPacket', 'client/render/SpellVisualRenderer', 'util/SpellBeamModels', 'util/SpellVisualGeometry']
  .map(name => path.join(sourceRoot, name + '.java'));
const tools = ['runtime_patches/ManorSpellAreaPatch', 'runtime_patches/ZicaiVoidPatch',
  'runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames', 'verification/ZipStreamCheck',
  'verification/WeaponUiJarClosureCheck'].map(name => path.join(project, 'tools', name + '.java'));
const checks = ['ManorSpellAreaCheck', 'ManorPlayerBalanceCheck', 'ManorSpellTimingCheck', 'ManorSpellAreaModelCheck', 'QizaiLaserBarrageModelCheck']
  .map(name => path.join(project, 'tools/verification', name + '.java'));
const sources = [...compileSources, ...tools, ...checks, __filename, path.join(project, 'tools/deploy_manor_spell_area.ps1')];
const hashes = Object.fromEntries(sources.map(file => [file, hash(file)]));
for (const directory of ['classes', 'tools', 'payload', 'empty-sourcepath', 'source-after'])
  fs.mkdirSync(path.join(task, directory), { recursive: true });
const baseline = path.join(task, 'baseline.jar');
if (!fs.existsSync(baseline)) fs.copyFileSync(live, baseline);
const baselineHash = hash(baseline);
const installedHash = hash(live);
const previousReport = path.join(task, 'build-report.json');
const previous = fs.existsSync(previousReport) ? JSON.parse(fs.readFileSync(previousReport, 'utf8')) : null;
if (installedHash !== baselineHash && (previous?.baselineSha256 !== baselineHash || previous?.candidateSha256 !== installedHash))
  throw new Error('Installed baseline changed outside this verified task; use a fresh task path');
fs.writeFileSync(path.join(task, 'build.log'), '');
function run(executable, args) {
  const result = spawnSync(executable, args, { cwd: project, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, windowsHide: true });
  const output = (result.stdout || '') + (result.stderr || '');
  fs.appendFileSync(path.join(task, 'build.log'), output);
  process.stdout.write(output);
  if (result.status !== 0) throw new Error(`${executable} failed: ${result.status}`);
}
const toolsDirectory = path.join(task, 'tools');
const classes = path.join(task, 'classes');
const payload = path.join(task, 'payload');
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-cp', asm.join(path.delimiter),
  '-d', toolsDirectory, ...tools]);
const toolClasspath = [toolsDirectory, ...asm].join(path.delimiter);
const mappedBaseline = path.join(task, 'baseline-mapped.jar');
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, baseline, mappedBaseline]);
const oldDependencies = path.join(project, '_work/manor-player-balance-20261027/rebased-02/compile-dependencies.mf');
const oldMapped = path.join(project, '_work/manor-player-balance-20261027/rebased-02/baseline-mapped.jar');
const { pathToFileURL } = require('node:url');
const unfolded = fs.readFileSync(oldDependencies, 'ascii').replace(/\r?\n /g, '');
let line = unfolded.split(/\r?\n/).find(value => value.startsWith('Class-Path: '));
line = line.replace(pathToFileURL(oldMapped).href, pathToFileURL(mappedBaseline).href);
if (!line.includes(pathToFileURL(mappedBaseline).href)) throw new Error('Baseline manifest substitution failed');
let manifest = 'Manifest-Version: 1.0\r\n';
while (line.length > 70) { manifest += line.slice(0, 70) + '\r\n'; line = ' ' + line.slice(70); }
const manifestFile = path.join(task, 'compile-dependencies.mf');
fs.writeFileSync(manifestFile, manifest + line + '\r\n\r\n', 'ascii');
const dependencies = path.join(task, 'compile-dependencies.jar');
run(path.join(jdk, 'jar.exe'), ['--create', '--file', dependencies, '--manifest', manifestFile]);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-implicit:none',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', classes, ...compileSources]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [dependencies, toolClasspath].join(path.delimiter),
  'ManorSpellAreaPatch', baseline, classes, mappings, payload]);
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17',
  '-cp', [candidate, toolClasspath].join(path.delimiter), '-d', toolsDirectory, ...checks]);
const runtimeClasspath = [candidate, toolClasspath].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtimeClasspath, 'ManorSpellAreaCheck', baseline, candidate, mappings]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtimeClasspath, 'ManorPlayerBalanceCheck',
  path.join(project, '_work/manor-player-balance-20261027/rebased-02/baseline.jar'), candidate, 'skip-archive']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtimeClasspath, 'ManorSpellTimingCheck', path.join(task, 'spell-timing.csv')]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-Djava.awt.headless=true', '-cp', runtimeClasspath, 'ManorSpellAreaModelCheck', path.join(task, 'art')]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-Djava.awt.headless=true', '-cp', runtimeClasspath, 'QizaiLaserBarrageModelCheck', path.join(task, 'art/barrage-regression.png')]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'WeaponUiJarClosureCheck', candidate,
  ...compileSources.map(file => 'net/farmingtales/ftboss/' + path.relative(sourceRoot, file).replaceAll(path.sep, '/').replace(/\.java$/, '')),
  'net/farmingtales/ftboss/integ/spells/QizaiInfernoEffects$Cast', 'net/farmingtales/ftboss/integ/spells/QizaiBarrageEffects$Cast']);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(live) !== installedHash) throw new Error('Installed JAR changed during build');
for (const [file, expected] of Object.entries(hashes)) if (hash(file) !== expected) throw new Error('Source changed: ' + file);
for (const file of compileSources) {
  const destination = path.join(task, 'source-after', path.relative(sourceRoot, file));
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  fs.copyFileSync(file, destination);
}
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: baselineHash,
  candidate, candidateSha256: hash(candidate), expectedInstalledSha256: installedHash, sources: hashes, ultimateCastTicks: 240, playerRadiusScale: 2,
  bossRangesPreserved: true, priorBalancePreserved: true, gameplayTested: false, docsTouched: false }, null, 2) + '\n');
console.log('PASS: manor spell area and casting candidate verified');
