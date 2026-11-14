const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const assert = require('node:assert/strict');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/shared-combat-statuses-20261026'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must remain in project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const instance = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2');
const live = path.join(instance, 'mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const forge = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge');
const mapped = path.join(forge, '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const runtime = path.join(forge, '1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar');
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
for (const name of ['classes', 'payload', 'empty-sourcepath']) fs.mkdirSync(path.join(task, name), { recursive: true });
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
if (hash(baseline) !== baselineHash) throw new Error('Baseline changed; choose a fresh task directory');
const sources = ['boss/ai/CombatStatusRules', 'boss/ai/CombatStatusHooks', 'effect/CombatStatusEffect',
  'effect/CombatStatusEffects', 'client/CombatStatusClient'].map(name => path.join(project, 'src/main/java/net/farmingtales/ftboss', name + '.java'));
const tools = ['runtime_patches/SharedCombatStatusesPatch', 'runtime_patches/ZicaiVoidPatch',
  'runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames', 'verification/SharedCombatStatusesCheck',
  'verification/WeaponUiJarClosureCheck', 'verification/ZipStreamCheck'].map(name => path.join(project, 'tools', name + '.java'));
const generator = path.join(project, 'tools/generate_combat_status_icons.cjs');
const behavior = path.join(project, 'tools/verification/check_shared_combat_statuses_behavior.cjs');
const languageFiles = ['zh_cn', 'en_us'].map(language => path.join(project, 'src/main/resources/assets/ftboss/lang', language + '.json'));
const sourceChanges = ['entity/EasterBossEntity', 'entity/StockpotEntity', 'entity/XueliKnifeEntity', 'boss/ai/HealBan']
  .map(name => path.join(project, 'src/main/java/net/farmingtales/ftboss', name + '.java'));
run(process.execPath, [generator, path.join(task, 'icons.png')]);
const ids = ['armor_break', 'bone_fracture', 'grievous_wound', 'stun', 'rooted', 'ignite', 'airborne', 'flickering_darkness', 'valor'];
const icons = ids.map(id => path.join(project, 'src/main/resources/assets/ftboss/textures/mob_effect', id + '.png'));
const tracked = [...sources, ...tools, ...sourceChanges, ...languageFiles, ...icons, generator, behavior, __filename,
  path.join(project, 'tools/deploy_shared_combat_statuses.ps1')];
const hashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
run(process.execPath, [behavior, task]);
run(path.join(jdk, 'javac.exe'), ['-J-Dfile.encoding=UTF-8', '-encoding', 'UTF-8', '-proc:none', '--release', '17',
  '-cp', asm.join(path.delimiter), '-d', classes, ...tools, sources[0]]);
const toolClasspath = [classes, ...asm].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, baseline, path.join(task, 'baseline-mapped.jar')]);
const dependencies = classpathJar('compile-dependencies', [mapped, path.join(task, 'baseline-mapped.jar'), ...libraries, ...mods]);
run(path.join(jdk, 'javac.exe'), ['-J-Dfile.encoding=UTF-8', '-encoding', 'UTF-8', '-proc:none', '-implicit:none', '--release', '17',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', classes, ...sources]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [toolClasspath, dependencies].join(path.delimiter),
  'SharedCombatStatusesPatch', baseline, classes, mappings, payload]);
for (const language of ['zh_cn', 'en_us']) {
  const entry = `assets/ftboss/lang/${language}.json`;
  const extracted = spawnSync(path.join(jdk, 'jar.exe'), ['--extract', '--file', baseline, entry],
    { cwd: payload, encoding: 'utf8', windowsHide: true });
  if (extracted.status !== 0) throw new Error('Cannot extract language');
  const destination = path.join(payload, entry);
  const json = JSON.parse(fs.readFileSync(destination, 'utf8'));
  const source = JSON.parse(fs.readFileSync(path.join(project, 'src/main/resources', entry), 'utf8'));
  for (const id of ids) {
    const key = 'effect.ftboss.' + id;
    assert.equal(typeof source[key], 'string');
    json[key] = source[key];
  }
  fs.writeFileSync(destination, JSON.stringify(json, null, 2) + '\n');
}
for (const icon of icons) {
  const output = path.join(payload, 'assets/ftboss/textures/mob_effect', path.basename(icon));
  fs.mkdirSync(path.dirname(output), { recursive: true });
  fs.copyFileSync(icon, output);
}
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net', '-C', payload, 'assets']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', toolClasspath, 'SharedCombatStatusesCheck', baseline, candidate]);
const runtimeDependencies = classpathJar('runtime-dependencies', [runtime, ...libraries, ...mods]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [candidate, classes, runtimeDependencies, ...asm].join(path.delimiter),
  'SharedCombatStatusesCheck', 'link', candidate]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'WeaponUiJarClosureCheck', candidate,
  'net/farmingtales/ftboss/boss/ai/CombatStatusHooks', 'net/farmingtales/ftboss/effect/CombatStatusEffects',
  'net/farmingtales/ftboss/boss/ai/CombatStatusHooks$Anchor', 'net/farmingtales/ftboss/boss/ai/CombatStatusRules',
  'net/farmingtales/ftboss/effect/CombatStatusEffect',
  'net/farmingtales/ftboss/client/CombatStatusClient', 'net/farmingtales/ftboss/entity/EasterBossEntity']);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed while building; rebuild from current baseline');
for (const [file, expected] of Object.entries(hashes)) if (hash(file) !== expected) throw new Error('Source changed while building: ' + file);
const report = { passed: true, baseline, baselineSha256: baselineHash, candidate, candidateSha256: hash(candidate),
  sources: hashes, gameplayTested: false, ids, neoForgeTouched: false, docsTouched: false,
  docsProposal: path.join(project, '_work/docs-proposals/shared-combat-statuses.md'), preview: path.join(task, 'icons.png') };
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify(report, null, 2) + '\n');
console.log('PASS: verified shared combat status candidate ' + candidate);
