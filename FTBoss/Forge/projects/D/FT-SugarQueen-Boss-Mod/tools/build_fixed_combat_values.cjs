const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/fixed-combat-values-20261026'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must remain inside project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const live = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const classes = path.join(task, 'classes');
const payload = path.join(task, 'payload');
for (const directory of [classes, payload]) fs.mkdirSync(directory, { recursive: true });
const log = path.join(task, 'build.log');
fs.writeFileSync(log, '');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
function run(executable, argumentsList) {
  const result = spawnSync(executable, argumentsList, { cwd: project, encoding: 'utf8', windowsHide: true, maxBuffer: 64 * 1024 * 1024 });
  fs.appendFileSync(log, `${executable} ${argumentsList.join(' ')}\n${result.stdout || ''}${result.stderr || ''}\n`);
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error(`Command failed: ${result.status}`);
}

const baseline = path.join(task, 'baseline.jar');
const baselineHash = hash(live);
if (!fs.existsSync(baseline)) fs.copyFileSync(live, baseline);
if (hash(baseline) !== baselineHash) throw new Error('Installed baseline changed; use a fresh task directory');
const sources = ['runtime_patches/FixedCombatValuesPatch.java', 'verification/FixedCombatValuesCheck.java',
  'verification/WeaponUiJarClosureCheck.java', 'verification/ZipStreamCheck.java'].map(file => path.join(project, 'tools', file));
const sourceBefore = path.join(task, 'source-before.json');
if (!fs.existsSync(sourceBefore)) throw new Error('Canonical source backup manifest required');
const canonical = Object.keys(JSON.parse(fs.readFileSync(sourceBefore, 'utf8')));
const helpers = ['ManorWeaponRules', 'ManorWeaponBalanceRules', 'XuanlanRules'].map(name =>
  path.join(project, 'src/main/java/net/farmingtales/ftboss/util', name + '.java'));
const zipCheck = path.join(project, 'tools/verification/check_jar_zip_rules.cjs');
const deployer = path.join(project, 'tools/deploy_fixed_combat_values.ps1');
const hashes = Object.fromEntries([__filename, ...sources, ...canonical, ...helpers, zipCheck, deployer].map(file => [file, hash(file)]));
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', asm.join(path.delimiter), '-d', classes, ...sources]);
const classpath = [classes, ...asm].join(path.delimiter);
const sourceRoot = path.join(project, 'src/main/java/net/farmingtales/ftboss');
const pureSources = canonical.filter(file => file.startsWith(path.join(sourceRoot, 'util') + path.sep)
  || file.endsWith(path.join('item', 'weapons', 'XueliWeaponRules.java')));
const sourceClasses = ['source-classes-before', 'source-classes-after'].map(name => path.join(task, name));
for (let index = 0; index < sourceClasses.length; index++) {
  fs.mkdirSync(sourceClasses[index], { recursive: true });
  const files = pureSources.map(file => index === 0 ? path.join(task, 'source-before', path.relative(sourceRoot, file)) : file);
  run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', baseline,
    '-d', sourceClasses[index], ...files]);
}
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classpath, 'FixedCombatValuesPatch', baseline, payload]);
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classpath, 'FixedCombatValuesCheck', baseline, candidate, ...sourceClasses]);
const methods = fs.readFileSync(path.join(task, 'folded-methods.tsv'), 'utf8').trim().split(/\r?\n/).filter(Boolean);
const changedClasses = [...new Set(methods.map(line => line.slice(0, line.indexOf('.'))))];
run(path.join(jdk, 'java.exe'), ['-cp', classpath, 'WeaponUiJarClosureCheck', candidate, ...changedClasses]);
run(path.join(jdk, 'java.exe'), ['-cp', classes, 'ZipStreamCheck', candidate]);
run(process.execPath, [zipCheck, candidate]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed during verification; rebuild against the latest baseline');
for (const [file, expected] of Object.entries(hashes)) if (hash(file) !== expected) throw new Error('Source changed during verification: ' + file);
const report = { passed: true, baseline, baselineSha256: baselineHash, candidate, candidateSha256: hash(candidate),
  sources: hashes, changedEntries: changedClasses.map(name => name + '.class'),
  foldedOperations: methods.reduce((sum, line) => sum + Number(line.split('\t')[1]), 0), foldedMethods: methods.length,
  sourceFullRebuild: false, gameplayTested: false, dynamicRulesRetained: true, docsTouched: false, neoForgeTouched: false,
  docsProposal: path.join(project, '_work/docs-proposals/fixed-combat-values.md') };
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify(report, null, 2) + '\n');
console.log('PASS: fixed combat values verified; candidate ' + candidate);
