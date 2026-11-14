const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/melee-effect-balance-20261026'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must remain inside project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const live = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const classes = path.join(task, 'checks');
const payload = path.join(task, 'payload');
for (const directory of [classes, payload]) fs.mkdirSync(directory, { recursive: true });
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const baseline = path.join(task, 'baseline.jar');
if (!fs.existsSync(baseline)) fs.copyFileSync(live, baseline);
const baselineHash = hash(baseline);
if (hash(live) !== baselineHash) throw new Error('Installed baseline changed; use a fresh task directory');
let recovery = null;
const priorTask = path.join(project, '_work/melee-effect-balance-20261026-a4');
const priorReport = JSON.parse(fs.readFileSync(path.join(priorTask, 'build-report.json'), 'utf8'));
if (baselineHash === priorReport.candidateSha256) {
  recovery = path.join(priorTask, 'qys-bosses-before-deploy.jar');
  if (hash(recovery) !== priorReport.baselineSha256) throw new Error('Original pre-enhancement recovery checksum mismatch');
}
const sources = ['tools/runtime_patches/MeleeEnhancementPatch.java', 'tools/verification/MeleeEnhancementCheck.java',
  'src/main/java/net/farmingtales/ftboss/util/MeleeEnhancementRules.java'].map(file => path.join(project, file));
const combatSources = ['StatueBlessingCombatCheck.java', 'MeleeEnhancementCombatCheck.java', 'MeleeEnhancementMixinCheck.java'].map(file => path.join(project, 'tools/verification', file));
const mappingSource = path.join(project, 'tools/runtime_patches/statue-exclusive-blessings/MapBlessingCheckJar.java');
const sourceHashes = Object.fromEntries([__filename, ...sources, ...combatSources, mappingSource,
  path.join(project, 'tools/deploy_melee_enhancement.ps1')].map(file => [file, hash(file)]));
const log = path.join(task, 'build.log');
fs.writeFileSync(log, '');
function run(executable, args) {
  const result = spawnSync(executable, args, { cwd: project, windowsHide: true, encoding: 'utf8', maxBuffer: 16 * 1024 * 1024 });
  fs.appendFileSync(log, (result.stdout || '') + (result.stderr || ''));
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error('Command failed: ' + result.status);
}
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-util', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
const cp = [classes, ...asm].join(path.delimiter);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', cp, '-d', classes,
  ...sources, path.join(project, 'tools/verification/ZipStreamCheck.java'), path.join(project, 'tools/verification/WeaponUiJarClosureCheck.java')]);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-d', payload, sources[2]]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', cp, 'MeleeEnhancementPatch', baseline, payload, ...(recovery ? [recovery] : [])]);
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
const runtime = path.join(project, '_work/zicai-theme-zheergen-trail-20261025/runtime-dependencies.jar');
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [candidate, cp, runtime].join(path.delimiter), 'MeleeEnhancementCheck', baseline, candidate]);
const dependencies = path.join(project, '_work/zicai-theme-zheergen-trail-20261025/compile-dependencies.jar');
const srgRoot = path.join(process.env.USERPROFILE, '.gradle/caches/modules-2/files-2.1/net.minecraftforge/srgutils/0.4.15');
const srg = fs.readdirSync(srgRoot).map(folder => path.join(srgRoot, folder, 'srgutils-0.4.15.jar')).find(fs.existsSync);
const mappingCp = [cp, dependencies, srg].join(path.delimiter);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', mappingCp, '-d', classes,
  mappingSource, ...combatSources]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [candidate, cp, runtime].join(path.delimiter), 'MeleeEnhancementMixinCheck', candidate]);
const mapped = path.join(project, 'build/fg_cache/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const mappedCheck = path.join(task, 'mapped-check.jar');
run(path.join(jdk, 'java.exe'), ['-cp', mappingCp, 'MapBlessingCheckJar', path.join(project, 'build/createMcpToSrg/output.tsrg'), candidate, mapped, mappedCheck]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [classes, dependencies].join(path.delimiter),
  'MeleeEnhancementCombatCheck', mappedCheck, path.join(task, 'combat-dump')]);
run(path.join(jdk, 'java.exe'), ['-cp', cp, 'WeaponUiJarClosureCheck', candidate, '--all',
  'net.farmingtales.ftboss.item.FoodWeaponEvolution', 'net.farmingtales.ftboss.item.StatueBlessingCombat',
  'net.farmingtales.ftboss.item.FoodBlessingCombat']);
run(path.join(jdk, 'java.exe'), ['-cp', classes, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed during verification; rebuild from current baseline');
for (const [file, expected] of Object.entries(sourceHashes)) if (hash(file) !== expected) throw new Error('Source changed: ' + file);
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: baselineHash,
  candidate, candidateSha256: hash(candidate), sources: sourceHashes, gameplayTested: false, docsTouched: false }, null, 2) + '\n');
console.log('PASS verified candidate: ' + candidate);
