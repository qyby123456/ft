const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[3]);
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must remain in project _work');
fs.mkdirSync(task, { recursive: true });
const baseline = path.resolve(process.argv[2]);
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const instance = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2');
const gradleRepo = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge');
const mapped = path.join(gradleRepo, '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const runtime = path.join(gradleRepo, '1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar');
const version = JSON.parse(fs.readFileSync(path.join(instance, 'FarmingTales_ForgeⅡ v1.5.2.json'), 'utf8'));
const libraries = version.libraries.filter(library => !library.name.includes('natives')).map(library => {
  const [group, artifact, release, classifier] = library.name.split(':');
  const relative = library.downloads?.artifact?.path
    || `${group.replaceAll('.', '/')}/${artifact}/${release}/${artifact}-${release}${classifier ? '-' + classifier : ''}.jar`;
  return path.join(minecraft, 'libraries', relative);
}).filter(file => fs.existsSync(file));
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
libraries.push(...['fmlcore', 'javafmllanguage', 'lowcodelanguage', 'mclanguage'].map(name =>
  path.join(minecraft, `libraries/net/minecraftforge/${name}/1.20.1-47.4.10/${name}-1.20.1-47.4.10.jar`)));
const classes = path.join(task, 'classes');
const payload = path.join(task, 'payload');
fs.mkdirSync(classes, { recursive: true });
fs.mkdirSync(payload, { recursive: true });
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const sources = [
  'src/main/java/net/farmingtales/ftboss/util/AppleBlastLightRules.java',
  'src/main/java/net/farmingtales/ftboss/network/AppleBlastNetwork.java',
  'src/main/java/net/farmingtales/ftboss/client/render/AppleBlastLightVisual.java',
  'tools/runtime_patches/AppleBlastLightPatch.java',
  'tools/verification/AppleBlastLightCheck.java',
  'tools/verification/ZipStreamCheck.java'
].map(relative => path.join(project, relative));
const tracked = [...sources, __filename,
  path.join(project, 'src/main/java/net/farmingtales/ftboss/entity/AppleBossEntity.java'),
  path.join(project, 'src/main/java/net/farmingtales/ftboss/entity/AppleCloneEntity.java')];
const sourceHashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
const transcript = path.join(task, 'build.log');
fs.writeFileSync(transcript, '');
function run(executable, argumentsList) {
  const result = spawnSync(executable, argumentsList, { encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, windowsHide: true });
  fs.appendFileSync(transcript, `${executable} ${argumentsList.join(' ')}\n${result.stdout || ''}${result.stderr || ''}\n`);
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error(`Command failed: ${executable} (${result.status})`);
}
function classpathJar(name, entries) {
  let line = 'Class-Path: ' + entries.map(file => {
    if (!fs.existsSync(file)) throw new Error(`Missing dependency ${file}`);
    let address = pathToFileURL(file).href.replaceAll('[', '%5B').replaceAll(']', '%5D');
    if (fs.statSync(file).isDirectory()) address += '/';
    return address;
  }).join(' ');
  let manifest = 'Manifest-Version: 1.0\r\n';
  while (line.length > 70) {
    manifest += line.slice(0, 70) + '\r\n';
    line = ' ' + line.slice(70);
  }
  manifest += line + '\r\n\r\n';
  const manifestPath = path.join(task, name + '.mf');
  fs.writeFileSync(manifestPath, manifest, 'ascii');
  const output = path.join(task, name + '.jar');
  run(path.join(jdk, 'jar.exe'), ['--create', '--file', output, '--manifest', manifestPath]);
  return output;
}
const compileDependencies = classpathJar('compile-dependencies', [mapped, ...asm, ...libraries]);
run(path.join(jdk, 'javac.exe'), ['-J-Duser.language=en', '-encoding', 'UTF-8', '-proc:none', '--release', '17',
  '-cp', compileDependencies, '-d', classes, ...sources]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classes + path.delimiter + compileDependencies,
  'AppleBlastLightPatch', baseline, classes, path.join(project, 'build/createMcpToSrg/output.tsrg'), payload]);
function walk(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry =>
    entry.isDirectory() ? walk(path.join(directory, entry.name)) : [path.join(directory, entry.name)]);
}
const entries = walk(payload).map(file => path.relative(payload, file).replaceAll('\\', '/')).sort();
if (entries.length !== 7) throw new Error('Expected exactly seven class payload entries');
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate,
  ...entries.flatMap(entry => ['-C', payload, entry])]);
const runtimeDependencies = classpathJar('runtime-dependencies', [runtime, ...asm, ...libraries]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', candidate + path.delimiter + classes + path.delimiter + runtimeDependencies,
  'AppleBlastLightCheck', baseline, candidate, path.join(task, 'verification.json')]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classes + path.delimiter + runtimeDependencies,
  'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
for (const file of tracked) {
  if (hash(file) !== sourceHashes[file]) throw new Error('Source changed during build: ' + file);
  const snapshot = path.join(task, 'sources', path.relative(project, file));
  fs.mkdirSync(path.dirname(snapshot), { recursive: true });
  fs.copyFileSync(file, snapshot);
}
const report = { passed: true, baseline, baselineSha256: hash(baseline), candidate,
  candidateSha256: hash(candidate), entries, sourceHashes, gameTested: false };
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify(report, null, 2) + '\n');
console.log('PASS candidate ready: ' + candidate);
