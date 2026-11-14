const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/manor-domain-rebalance-20261026'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must remain in project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const live = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const classes = path.join(task, 'classes');
const payload = path.join(task, 'payload');
const empty = path.join(task, 'empty-sourcepath');
for (const directory of [classes, payload, empty]) fs.mkdirSync(directory, { recursive: true });
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
if (hash(baseline) !== baselineHash) throw new Error('Baseline changed; choose a fresh task directory');
const sources = ['ManorDomainRules', 'ManorDomainEffectRules'].map(name =>
  path.join(project, 'src/main/java/net/farmingtales/ftboss/util', name + '.java'));
const checks = ['ManorDomainRebalanceCheck', 'ZipStreamCheck', 'WeaponUiJarClosureCheck'].map(name =>
  path.join(project, 'tools/verification', name + '.java'));
const zipRules = path.join(project, 'tools/verification/check_jar_zip_rules.cjs');
const deployer = path.join(project, 'tools/deploy_manor_domain_rebalance.ps1');
const hashes = Object.fromEntries([...sources, ...checks, zipRules, deployer, __filename].map(file => [file, hash(file)]));
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none',
  '-sourcepath', empty, '-classpath', baseline, '-d', payload, ...sources]);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none',
  '-sourcepath', empty, '-classpath', asm.join(path.delimiter), '-d', classes, ...checks]);
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classes, 'ManorDomainRebalanceCheck', baseline, candidate]);
const toolClasspath = [classes, ...asm].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'WeaponUiJarClosureCheck', candidate,
  'net/farmingtales/ftboss/boss/ai/ManorGroundSupport', 'net/farmingtales/ftboss/util/ManorDomainRules',
  'net/farmingtales/ftboss/util/ManorDomainEffectRules']);
run(path.join(jdk, 'java.exe'), ['-cp', classes, 'ZipStreamCheck', candidate]);
run(process.execPath, [zipRules, candidate]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed while building; rebuild from current baseline');
for (const [file, expected] of Object.entries(hashes)) if (hash(file) !== expected) throw new Error('Source changed while building: ' + file);
const report = { passed: true, baseline, baselineSha256: baselineHash, candidate, candidateSha256: hash(candidate),
  sources: hashes, changedEntries: sources.map(file => path.relative(path.join(project, 'src/main/java'), file).replaceAll('\\', '/').replace(/\.java$/, '.class')),
  gameplayTested: false, neoForgeTouched: false, docsTouched: false,
  docsProposal: path.join(project, '_work/docs-proposals/manor-domain-rebalance.md') };
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify(report, null, 2) + '\n');
console.log('PASS: verified domain rebalance candidate ' + candidate);
