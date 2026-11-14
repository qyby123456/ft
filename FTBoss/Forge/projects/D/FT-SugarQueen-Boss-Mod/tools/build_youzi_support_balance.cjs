const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/youzi-sparrow-support-20261026'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must remain in project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const live = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const classes = path.join(task, 'classes');
const payload = path.join(task, 'payload');
for (const directory of [classes, payload]) fs.mkdirSync(directory, { recursive: true });
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

const baseline = path.join(task, 'baseline.jar');
const baselineHash = hash(live);
if (!fs.existsSync(baseline)) fs.copyFileSync(live, baseline);
if (hash(baseline) !== baselineHash) throw new Error('Baseline changed; use a fresh task directory');
const source = path.join(project, 'src/main/java/net/farmingtales/ftboss/entity/EasterBossEntity.java');
const sourceText = fs.readFileSync(source, 'utf8');
if (!sourceText.includes('YOUZI_MINION_SHARE = 0.05f;') || !sourceText.includes('SPARROW_HEAL_PERCENT = 0.0;'))
  throw new Error('Canonical source does not match the intended support balance');
const tools = ['runtime_patches/YouziSupportBalancePatch', 'verification/YouziSupportBalanceCheck',
  'verification/WeaponUiJarClosureCheck', 'verification/ZipStreamCheck'].map(name => path.join(project, 'tools', name + '.java'));
const zipRules = path.join(project, 'tools/verification/check_jar_zip_rules.cjs');
const deployer = path.join(project, 'tools/deploy_youzi_support_balance.ps1');
const hashes = Object.fromEntries([source, ...tools, zipRules, deployer, __filename].map(file => [file, hash(file)]));
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', asm.join(path.delimiter), '-d', classes, ...tools]);
const classpath = [classes, ...asm].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classpath, 'YouziSupportBalancePatch', baseline, payload]);
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classpath, 'YouziSupportBalanceCheck', baseline, candidate]);
run(path.join(jdk, 'java.exe'), ['-cp', classpath, 'WeaponUiJarClosureCheck', candidate,
  'net/farmingtales/ftboss/entity/EasterBossEntity']);
run(path.join(jdk, 'java.exe'), ['-cp', classes, 'ZipStreamCheck', candidate]);
run(process.execPath, [zipRules, candidate]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed while building; rebuild from the latest baseline');
for (const [file, expected] of Object.entries(hashes)) if (hash(file) !== expected) throw new Error('Source changed while building: ' + file);
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline,
  baselineSha256: baselineHash, candidate, candidateSha256: hash(candidate), sources: hashes,
  changedEntries: ['net/farmingtales/ftboss/entity/EasterBossEntity.class'], gameplayTested: false,
  sourceFullRebuild: false, sparrowPercentageHealingAlreadyDisabled: true, docsTouched: false, neoForgeTouched: false,
  docsProposal: path.join(project, '_work/docs-proposals/youzi-sparrow-support.md') }, null, 2) + '\n');
console.log('PASS: verified support balance candidate ' + candidate);
