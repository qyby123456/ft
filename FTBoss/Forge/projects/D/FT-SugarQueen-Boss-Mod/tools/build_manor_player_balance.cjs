const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');
const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/manor-player-balance-20261027'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task escapes project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const instance = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2');
const live = path.join(instance, 'mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const mappedForge = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge',
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
for (const directory of ['classes', 'tools', 'payload', 'empty-sourcepath']) fs.mkdirSync(path.join(task, directory), { recursive: true });
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const baseline = path.join(task, 'baseline.jar');
if (!fs.existsSync(baseline)) fs.copyFileSync(live, baseline);
const baselineHash = hash(baseline);
if (hash(live) !== baselineHash) throw new Error('Installed baseline changed; use a fresh task path');
fs.writeFileSync(path.join(task, 'build.log'), '');
function run(executable, args) {
  const result = spawnSync(executable, args, { cwd: project, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, windowsHide: true });
  const output = (result.stdout || '') + (result.stderr || '');
  fs.appendFileSync(path.join(task, 'build.log'), output);
  process.stdout.write(output);
  if (result.status !== 0) throw new Error(`${executable} failed: ${result.status}`);
}
const sourceRoot = path.join(project, 'src/main/java/net/farmingtales/ftboss');
const compileSources = ['util/ManorDamageLimitRules', 'util/ManorRewardBalanceRules', 'util/ManorWeaponBalanceRules',
  'integ/spells/FullPowerBossSpell', 'integ/spells/ManorRewardBalance'].map(name => path.join(sourceRoot, name + '.java'));
const tools = ['runtime_patches/ManorPlayerBalancePatch', 'runtime_patches/ZicaiVoidPatch',
  'runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames', 'verification/ZipStreamCheck',
  'verification/WeaponUiJarClosureCheck'].map(name => path.join(project, 'tools', name + '.java'));
const check = path.join(project, 'tools/verification/ManorPlayerBalanceCheck.java');
const sources = [...compileSources, ...['item/EquipmentWeaponBuild', 'integ/spells/ManorIronSpells',
  'integ/spells/ManorSignatureSpell', 'integ/spells/QizaiSpell', 'integ/spells/QizaiBarrageSpell']
  .map(name => path.join(sourceRoot, name + '.java')), ...tools, check, __filename,
  path.join(project, 'tools/deploy_manor_player_balance.ps1')];
const hashes = Object.fromEntries(sources.map(file => [file, hash(file)]));
const toolsDirectory = path.join(task, 'tools');
const classes = path.join(task, 'classes');
const payload = path.join(task, 'payload');
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-cp', asm.join(path.delimiter),
  '-d', toolsDirectory, ...tools]);
const toolClasspath = [toolsDirectory, ...asm].join(path.delimiter);
const mappedBaseline = path.join(task, 'baseline-mapped.jar');
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, baseline, mappedBaseline]);
let line = 'Class-Path: ' + [mappedForge, mappedBaseline, ...libraries, ...mods]
  .map(file => pathToFileURL(file).href.replaceAll('[', '%5B').replaceAll(']', '%5D')).join(' ');
let manifest = 'Manifest-Version: 1.0\r\n';
while (line.length > 70) { manifest += line.slice(0, 70) + '\r\n'; line = ' ' + line.slice(70); }
const manifestFile = path.join(task, 'compile-dependencies.mf');
fs.writeFileSync(manifestFile, manifest + line + '\r\n\r\n', 'ascii');
const dependencies = path.join(task, 'compile-dependencies.jar');
run(path.join(jdk, 'jar.exe'), ['--create', '--file', dependencies, '--manifest', manifestFile]);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-implicit:none',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', classes, ...compileSources]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', toolClasspath, 'ManorPlayerBalancePatch', baseline, classes, mappings, payload]);
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-cp', [candidate, toolClasspath].join(path.delimiter),
  '-d', toolsDirectory, check]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [candidate, toolClasspath].join(path.delimiter),
  'ManorPlayerBalanceCheck', baseline, candidate]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'WeaponUiJarClosureCheck', candidate,
  'net/farmingtales/ftboss/integ/spells/ManorRewardBalance', 'net/farmingtales/ftboss/util/ManorRewardBalanceRules']);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed during build');
for (const [file, expected] of Object.entries(hashes)) if (hash(file) !== expected) throw new Error('Source changed: ' + file);
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: baselineHash,
  candidate, candidateSha256: hash(candidate), sources: hashes, gameplayTested: false, docsTouched: false,
  rolling20TickDamageLimit: 400, separateHitOrKindCap: false, blackCleaverBaseDamage: 36,
  sanguineBaseDamage: 18, armorStrengthAtRequest: 0.5, armorStrength: 1.5,
  concurrentArmorIncreasePreserved: true,
  playerRewardSpellDamageMultiplier: 0.5, ordinaryRewardSpellManaMultiplier: 2,
  manaExceptions: ['qizai_star_inferno', 'qizai_laser_barrage'], exceptionCurrentMana: 600,
  docsProposal: path.join(project, '_work/docs-proposals/manor-player-balance.md') }, null, 2) + '\n');
console.log('PASS: incremental candidate verified; deployment not performed by build');
