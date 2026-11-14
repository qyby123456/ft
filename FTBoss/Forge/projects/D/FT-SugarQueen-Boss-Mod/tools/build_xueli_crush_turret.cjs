const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/xueli-crush-xuanlan-rate-20261026'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must remain inside project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const live = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const mapped = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge',
  '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const eventbus = path.join(minecraft, 'libraries/net/minecraftforge/eventbus/6.0.5/eventbus-6.0.5.jar');
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const baseline = path.join(task, 'baseline.jar');
if (!fs.existsSync(baseline)) throw new Error('Back up the runtime baseline and source before building');
const baselineHash = hash(baseline);
if (hash(live) !== baselineHash) throw new Error('Installed baseline changed; use a fresh task');
const classes = path.join(task, 'classes');
const donor = path.join(task, 'donor');
const payload = path.join(task, 'payload');
const empty = path.join(task, 'empty-sourcepath');
for (const directory of [classes, donor, payload, empty]) fs.mkdirSync(directory, { recursive: true });
const transcript = path.join(task, 'build.log');
fs.writeFileSync(transcript, '');
function run(executable, argumentsList) {
  const result = spawnSync(executable, argumentsList, { cwd: project, encoding: 'utf8', maxBuffer: 32 * 1024 * 1024, windowsHide: true });
  fs.appendFileSync(transcript, `${executable} ${argumentsList.join(' ')}\n${result.stdout || ''}${result.stderr || ''}\n`);
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error(`Command failed (${result.status})`);
}
const production = ['entity/EasterBossEntity', 'balance/ManorExpansionRules', 'boss/ai/XueliCrushDamage', 'boss/ai/BossOutgoingDamageNerfs']
  .map(name => path.join(project, 'src/main/java/net/farmingtales/ftboss', name + '.java'));
const patch = path.join(project, 'tools/runtime_patches/XueliCrushTurretPatch.java');
const check = path.join(project, 'tools/verification/XueliCrushTurretCheck.java');
const zip = path.join(project, 'tools/verification/ZipStreamCheck.java');
const zipRules = path.join(project, 'tools/verification/check_jar_zip_rules.cjs');
const deploy = path.join(project, 'tools/deploy_xueli_crush_turret.ps1');
const sources = Object.fromEntries([...production, patch, check, zip, zipRules, deploy, __filename].map(file => [file, hash(file)]));
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none', '-sourcepath', empty,
  '-cp', [mapped, eventbus, baseline].join(path.delimiter), '-d', donor, production[1], production[2]]);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none', '-sourcepath', empty,
  '-cp', asm.join(path.delimiter), '-d', classes, patch, check, zip]);
const classpath = [classes, ...asm].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classpath, 'XueliCrushTurretPatch', baseline, donor, payload]);
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classpath, 'XueliCrushTurretCheck', baseline, candidate]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classes, 'ZipStreamCheck', candidate]);
run(process.execPath, [zipRules, candidate]);
if (hash(live) !== baselineHash) throw new Error('Installed baseline changed during build');
for (const [file, expected] of Object.entries(sources)) if (hash(file) !== expected) throw new Error('Source changed during build: ' + file);
const report = { passed: true, baseline, baselineSha256: baselineHash, candidate, candidateSha256: hash(candidate), sources,
  changedEntries: production.map(file => path.relative(path.join(project, 'src/main/java'), file).replaceAll('\\', '/').replace(/\.java$/, '.class')),
  crushDamagePerSecond: 5, crushDurationTicks: 60, turretIntervalTicks: 80,
  gameplayTested: false, neoForgeTouched: false, docsTouched: false,
  docsProposal: path.join(project, '_work/docs-proposals/xueli-crush-xuanlan-turret-rate.md') };
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify(report, null, 2) + '\n');
console.log('PASS: verified candidate ' + candidate);
