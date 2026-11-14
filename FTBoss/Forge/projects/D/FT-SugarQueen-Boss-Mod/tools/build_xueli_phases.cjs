const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/xueli-phases-20261026'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must remain inside project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const instance = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2');
const live = path.join(instance, 'mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const forge = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge');
const mapped = path.join(forge, '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const runtime = path.join(forge, '1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar');
const version = JSON.parse(fs.readFileSync(path.join(instance, 'FarmingTales_ForgeⅡ v1.5.2.json'), 'utf8'));
const libraries = version.libraries.filter(library => !library.name.includes('natives')).map(library => {
  const [group, artifact, release, classifier] = library.name.split(':');
  const relative = library.downloads?.artifact?.path
    || `${group.replaceAll('.', '/')}/${artifact}/${release}/${artifact}-${release}${classifier ? '-' + classifier : ''}.jar`;
  return path.join(minecraft, 'libraries', relative);
}).filter(file => fs.existsSync(file));
libraries.push(...['fmlcore', 'javafmllanguage', 'lowcodelanguage', 'mclanguage'].map(name =>
  path.join(minecraft, `libraries/net/minecraftforge/${name}/1.20.1-47.4.10/${name}-1.20.1-47.4.10.jar`)));
const mods = fs.readdirSync(path.join(project, 'libs')).filter(name => name.endsWith('.jar')).map(name => path.join(project, 'libs', name));
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
const classes = path.join(task, 'classes');
const checks = path.join(task, 'checks');
const payload = path.join(task, 'payload');
const empty = path.join(task, 'empty-sourcepath');
for (const directory of [classes, checks, payload, empty]) fs.mkdirSync(directory, { recursive: true });
const transcript = path.join(task, 'build.log');
fs.writeFileSync(transcript, '');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
function run(executable, argumentsList, cwd = project) {
  const result = spawnSync(executable, argumentsList, { cwd, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, windowsHide: true });
  fs.appendFileSync(transcript, `${executable} ${argumentsList.join(' ')}\n${result.stdout || ''}${result.stderr || ''}\n`);
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error(`Command failed (${result.status})`);
}

function classpath(name, entries) {
  let line = 'Class-Path: ' + entries.map(file => pathToFileURL(file).href.replace(/[^\x00-\x7F]/g, value => encodeURIComponent(value))).join(' ');
  let manifest = 'Manifest-Version: 1.0\r\n';
  while (line.length > 70) {
    manifest += line.slice(0, 70) + '\r\n';
    line = ' ' + line.slice(70);
  }
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
const sources = ['src/main/java/net/farmingtales/ftboss/util/XueliPhaseRules.java',
  'src/main/java/net/farmingtales/ftboss/boss/ai/XueliPhases.java'];
const helpers = ['tools/runtime_patches/XueliPhasePatch.java', 'tools/runtime_patches/ZicaiVoidPatch.java',
  'tools/verification/XueliPhaseRulesCheck.java', 'tools/verification/XueliPhaseJarCheck.java',
  'tools/verification/XueliPhaseComponentCheck.java',
  'tools/verification/FoodWeaponBuildLinkCheck.java', 'tools/verification/WeaponUiJarClosureCheck.java',
  'tools/verification/ZipStreamCheck.java'];
const tracked = [...sources, ...helpers, 'src/main/java/net/farmingtales/ftboss/entity/EasterBossEntity.java',
  'src/main/java/net/farmingtales/ftboss/boss/ai/XueliMaids.java', 'tools/build_xueli_phases.cjs',
  'tools/deploy_xueli_phases.ps1', 'tools/verification/check_jar_zip_rules.cjs'].map(file => path.join(project, file));
const hashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
const compileCp = classpath('compile-dependencies', [mapped, baseline, ...mods, ...asm, ...libraries]);
const javac = ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none', '-sourcepath', empty];
run(path.join(jdk, 'javac.exe'), [...javac, '-cp', compileCp, '-d', classes, ...sources.map(file => path.join(project, file))]);
run(path.join(jdk, 'javac.exe'), [...javac, '-cp', classes + path.delimiter + compileCp, '-d', checks,
  ...helpers.map(file => path.join(project, file))]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', checks + path.delimiter + classes, 'XueliPhaseRulesCheck']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', checks + path.delimiter + classes + path.delimiter + compileCp,
  'XueliPhaseComponentCheck', path.join(classes, 'net/farmingtales/ftboss/boss/ai/XueliPhases.class')]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', checks + path.delimiter + classes + path.delimiter + compileCp,
  'XueliPhasePatch', baseline, classes, path.join(project, 'build/createMcpToSrg/output.tsrg'), payload]);
function walk(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => entry.isDirectory()
    ? walk(path.join(directory, entry.name)) : [path.join(directory, entry.name)]);
}
const entries = walk(payload).map(file => path.relative(payload, file).replaceAll('\\', '/')).sort();
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, ...entries.flatMap(entry => ['-C', payload, entry])]);
const runtimeCp = classpath('runtime-dependencies', [runtime, ...mods, ...asm, ...libraries]);
const validationCp = candidate + path.delimiter + checks + path.delimiter + runtimeCp;
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', validationCp, 'XueliPhaseRulesCheck']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', validationCp, 'XueliPhaseJarCheck', baseline, candidate]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', validationCp, 'FoodWeaponBuildLinkCheck', payload, candidate]);
run(path.join(jdk, 'java.exe'), ['-cp', validationCp, 'WeaponUiJarClosureCheck', candidate, '--all',
  'net/farmingtales/ftboss/boss/ai/XueliPhases']);
run(path.join(jdk, 'java.exe'), ['-cp', checks, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(live) !== baselineHash) throw new Error('Installed baseline changed while building; rebuild against the current JAR');
for (const [file, expected] of Object.entries(hashes)) {
  if (hash(file) !== expected) throw new Error('Source changed while building: ' + file);
  const target = path.join(task, 'sources', path.relative(project, file));
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.copyFileSync(file, target);
}
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: baselineHash,
  candidate, candidateSha256: hash(candidate), sources: hashes, entries, gameplayTested: false, neoForgeTouched: false,
  docsTouched: false, docsProposal: path.join(project, '_work/docs-proposals/xueli-health-phases.md') }, null, 2) + '\n');
console.log('PASS: verified Xueli phase candidate ' + candidate);
