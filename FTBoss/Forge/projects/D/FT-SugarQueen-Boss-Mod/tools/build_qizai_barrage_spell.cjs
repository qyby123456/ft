const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[3] || path.join(project, '_work/qizai-barrage-spell-20261026'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must be inside project _work');
const instance = path.join(process.env.APPDATA, '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2');
const baseline = path.resolve(process.argv[2] || path.join(instance, 'mods/qys-bosses.jar'));
const minecraft = path.join(process.env.APPDATA, '.minecraft');
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
const payload = path.join(task, 'payload');
fs.mkdirSync(classes, { recursive: true });
fs.mkdirSync(payload, { recursive: true });
const transcript = path.join(task, 'build.log');
fs.writeFileSync(transcript, '');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const baselineHash = hash(baseline);
const snapshot = path.join(task, 'baseline.jar');
if (!fs.existsSync(snapshot)) fs.copyFileSync(baseline, snapshot);
if (hash(snapshot) !== baselineHash) throw new Error('Baseline changed; use a fresh task directory');

function run(executable, argumentsList, cwd = project) {
  const result = spawnSync(executable, argumentsList, { cwd, encoding: 'utf8', maxBuffer: 16 * 1024 * 1024, windowsHide: true });
  fs.appendFileSync(transcript, `${executable} ${argumentsList.join(' ')}\n${result.stdout || ''}${result.stderr || ''}\n`);
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error(`Command failed (${result.status})`);
}

function classpathJar(name, entries) {
  let line = 'Class-Path: ' + entries.map(file => {
    if (!fs.existsSync(file)) throw new Error(`Missing dependency: ${file}`);
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

const sources = [
  'src/main/java/net/farmingtales/ftboss/util/QizaiLaserBarrageRules.java',
  'src/main/java/net/farmingtales/ftboss/util/QizaiBarrageRules.java',
  'src/main/java/net/farmingtales/ftboss/integ/spells/QizaiBarrageSpell.java',
  'src/main/java/net/farmingtales/ftboss/integ/spells/QizaiBarrageSpells.java',
  'src/main/java/net/farmingtales/ftboss/integ/spells/QizaiBarrageEffects.java',
  'src/main/java/net/farmingtales/ftboss/network/QizaiBarrageNetwork.java',
  'src/main/java/net/farmingtales/ftboss/client/render/QizaiBarrageVisual.java',
  'tools/runtime_patches/QizaiBarragePack.java',
  'tools/runtime_patches/ZicaiVoidPatch.java',
  'tools/verification/QizaiBarrageCheck.java',
  'tools/verification/QizaiBarrageJarCheck.java',
  'tools/verification/ZipStreamCheck.java'
].map(file => path.join(project, file));
run(process.execPath, [path.join(project, 'tools/generate_qizai_barrage_icon.cjs')]);
const icon = 'assets/ftboss/textures/gui/spell_icons/qizai_laser_barrage.png';
const tracked = [...sources, __filename, path.join(project, 'tools/generate_qizai_barrage_icon.cjs'),
  path.join(project, 'src/main/resources', icon),
  ...['zh_cn', 'en_us'].map(locale => path.join(project, `src/main/resources/assets/ftboss/lang/${locale}.json`))];
const hashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
const compileDependencies = classpathJar('compile-dependencies', [mapped, snapshot, ...mods, ...asm, ...libraries]);
run(path.join(jdk, 'javac.exe'), ['-J-Duser.language=en', '-encoding', 'UTF-8', '-proc:none', '--release', '17',
  '-cp', compileDependencies, '-d', classes, ...sources]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classes + path.delimiter + compileDependencies, 'QizaiBarrageCheck']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classes + path.delimiter + compileDependencies,
  'QizaiBarragePack', classes, payload, path.join(project, 'build/createMcpToSrg/output.tsrg')]);
run(path.join(jdk, 'jar.exe'), ['--extract', '--file', snapshot,
  'assets/ftboss/lang/zh_cn.json', 'assets/ftboss/lang/en_us.json'], payload);
for (const locale of ['zh_cn', 'en_us']) {
  const relative = `assets/ftboss/lang/${locale}.json`;
  const target = path.join(payload, relative);
  const language = JSON.parse(fs.readFileSync(target, 'utf8'));
  const source = JSON.parse(fs.readFileSync(path.join(project, 'src/main/resources', relative), 'utf8'));
  for (const suffix of ['', '.effect', '.guide']) {
    const key = 'spell.ftboss.qizai_laser_barrage' + suffix;
    if (language[key] !== undefined) throw new Error('Spell already exists in baseline: ' + key);
    language[key] = source[key];
  }
  fs.writeFileSync(target, JSON.stringify(language, null, 2) + '\n');
}
fs.mkdirSync(path.dirname(path.join(payload, icon)), { recursive: true });
fs.copyFileSync(path.join(project, 'src/main/resources', icon), path.join(payload, icon));
function walk(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry =>
    entry.isDirectory() ? walk(path.join(directory, entry.name)) : [path.join(directory, entry.name)]);
}
const entries = walk(payload).map(file => path.relative(payload, file).replaceAll('\\', '/')).sort();
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(snapshot, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, ...entries.flatMap(entry => ['-C', payload, entry])]);
const runtimeDependencies = classpathJar('runtime-dependencies', [runtime, ...mods, ...asm, ...libraries]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', candidate + path.delimiter + classes + path.delimiter + runtimeDependencies,
  'QizaiBarrageJarCheck', snapshot, candidate]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', classes, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(baseline) !== baselineHash) throw new Error('Baseline changed during build; rebuild from current JAR');
for (const file of tracked) {
  if (hash(file) !== hashes[file]) throw new Error('Source changed during build: ' + file);
  const destination = path.join(task, 'sources', path.relative(project, file));
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  fs.copyFileSync(file, destination);
}
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline,
  baselineSha256: baselineHash, candidate, candidateSha256: hash(candidate), entries, sources: hashes,
  gameplayTested: false, docsTouched: false }, null, 2) + '\n');
console.log('PASS candidate ready: ' + candidate);
