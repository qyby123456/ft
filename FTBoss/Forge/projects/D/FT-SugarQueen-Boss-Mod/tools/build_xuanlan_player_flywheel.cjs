const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/xuanlan-player-flywheel-balance-20261103-a2'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must remain inside project _work');
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
const classes = path.join(task, 'classes');
const payload = path.join(task, 'payload');
fs.mkdirSync(classes, { recursive: true });
fs.mkdirSync(payload, { recursive: true });
const transcript = path.join(task, 'build.log');
fs.writeFileSync(transcript, '');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');

function run(executable, argumentsList, cwd = project) {
  if (path.basename(executable).toLowerCase() === 'javac.exe') {
    argumentsList = ['-J-Duser.language=en', '-J-Duser.country=US', '-J-Dfile.encoding=UTF-8', ...argumentsList];
  }
  const result = spawnSync(executable, argumentsList, { cwd, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, windowsHide: true });
  fs.appendFileSync(transcript, `${executable} ${argumentsList.join(' ')}\n${result.stdout || ''}${result.stderr || ''}\n`);
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error(`Command failed (${result.status})`);
}

function classpathJar(name, entries) {
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
const sources = ['util/XuanlanRules', 'skill/XuanlanBladeRoster', 'skill/XuanlanCombat',
  'item/XuanlanMoonWeaponItem', 'entity/PlayerMoonChakramEntity'].map(name =>
  path.join(project, 'src/main/java/net/farmingtales/ftboss', name + '.java'));
const tools = ['runtime_patches/XuanlanPlayerFlywheelPatch', 'runtime_patches/ZicaiVoidPatch',
  'runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames',
  'verification/WeaponUiJarClosureCheck', 'verification/ZipStreamCheck'].map(name =>
  path.join(project, 'tools', name + '.java'));
const checks = ['XuanlanRulesCheck.java', 'XuanlanBladesCheck.java', 'XuanlanVolleyCheck.java',
  'check_xuanlan_blades.cjs', 'check_xuanlan_weapons.cjs', 'xuanlan_item_fixtures.cjs'].map(name =>
  path.join(project, 'tools/verification', name));
const languages = ['zh_cn', 'en_us'].map(name => path.join(project, `src/main/resources/assets/ftboss/lang/${name}.json`));
const tracked = [...sources, ...tools, ...checks, ...languages, __filename,
  path.join(project, 'tools/verification/check_jar_zip_rules.cjs'),
  path.join(project, 'tools/deploy_xuanlan_player_flywheel.ps1')];
const hashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
run(process.execPath, [path.join(project, 'tools/verification/check_xuanlan_blades.cjs'), path.join(task, 'behavior')]);
run(process.execPath, [path.join(project, 'tools/verification/check_xuanlan_weapons.cjs')]);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-cp', asm.join(path.delimiter),
  '-d', classes, ...tools, checks[0], sources[0]]);
const toolClasspath = [classes, ...asm].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, baseline, path.join(task, 'baseline-mapped.jar')]);
const dependencies = classpathJar('compile-dependencies', [mapped, path.join(task, 'baseline-mapped.jar'), ...libraries, ...mods]);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '-proc:none', '--release', '17', '-implicit:none',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', classes, ...sources]);
const verificationClasspath = [toolClasspath, dependencies].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', verificationClasspath,
  'XuanlanPlayerFlywheelPatch', 'patch', baseline, classes, mappings, payload]);
const extracted = path.join(task, 'baseline-resources');
fs.mkdirSync(extracted, { recursive: true });
const keys = ['message.ftboss.xuanlan.returning', 'tooltip.ftboss.xuanlan.white', 'tooltip.ftboss.xuanlan.combo'];
for (const language of ['zh_cn', 'en_us']) {
  const relative = `assets/ftboss/lang/${language}.json`;
  run(path.join(jdk, 'jar.exe'), ['xf', baseline, relative], extracted);
  const current = JSON.parse(fs.readFileSync(path.join(extracted, relative), 'utf8'));
  const source = JSON.parse(fs.readFileSync(path.join(project, 'src/main/resources', relative), 'utf8'));
  const merged = { ...current };
  for (const key of keys) merged[key] = source[key];
  for (const [key, value] of Object.entries(current)) if (!keys.includes(key)) assert.deepEqual(merged[key], value);
  const destination = path.join(payload, relative);
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  fs.writeFileSync(destination, JSON.stringify(merged, null, 2) + '\n');
}
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net', '-C', payload, 'assets']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', verificationClasspath,
  'XuanlanPlayerFlywheelPatch', 'verify', baseline, candidate, classes, mappings]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [candidate, toolClasspath].join(path.delimiter), 'XuanlanRulesCheck']);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'WeaponUiJarClosureCheck', candidate,
  'net/farmingtales/ftboss/item/XuanlanMoonWeaponItem', 'net/farmingtales/ftboss/entity/PlayerMoonChakramEntity',
  'net/farmingtales/ftboss/skill/XuanlanBladeRoster', 'net/farmingtales/ftboss/skill/XuanlanCombat']);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed during build');
for (const [file, expected] of Object.entries(hashes)) if (hash(file) !== expected) throw new Error('Source changed during build: ' + file);
const report = { passed: true, baseline, baselineSha256: baselineHash, candidate, candidateSha256: hash(candidate),
  sources: hashes, verificationClasspath, mappings, classes, gameplayTested: false, docsTouched: false,
  docsProposal: path.join(project, '_work/docs-proposals/xuanlan-player-flywheel-balance.md'),
  balance: { damage: 10, maximum: 18, perVolley: 3, cooldownTicks: 4, ignoresHurtCooldown: true },
  preserved: 'Unchanged green rifle, boss combat, all other class methods and all unrelated JAR entries; only three language keys updated per locale' };
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify(report, null, 2) + '\n');
console.log('PASS: verified player flywheel candidate ' + candidate);
