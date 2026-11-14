const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const project = path.resolve(__dirname, '../../..');
const instance = path.join(process.env.APPDATA, '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2');
const installed = path.join(instance, 'mods/qys-bosses.jar');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const task = path.join(project, '_work/armor-resistances-20261027-' + hash(installed).slice(0, 10));
const jdk = path.join(process.env.APPDATA, '.minecraft/runtime/java-runtime-gamma-snapshot/bin');
const dependencies = path.join(project, '_work/zicai-theme-zheergen-trail-20261025/compile-dependencies.jar');
const runtime = path.join(project, '_work/zicai-theme-zheergen-trail-20261025/runtime-dependencies.jar');
const mapped = path.join(project, 'build/fg_cache/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const source = path.join(project, 'src/main/java/net/farmingtales/ftboss/item');
const baseline = path.join(task, 'baseline.jar');
const candidate = path.join(task, 'qys-bosses.jar');
const compiled = path.join(task, 'compiled');
const remapped = path.join(task, 'remapped');
const tools = path.join(task, 'tools');
const checks = path.join(task, 'checks');
const payload = path.join(task, 'payload');
const originals = path.join(task, 'originals');
function run(executable, args, cwd = project, quiet = false) {
  const executableName = path.basename(executable).toLowerCase();
  if (executableName === 'java.exe') args = ['-Dfile.encoding=UTF-8', ...args];
  if (executableName === 'javac.exe') args = ['-J-Dfile.encoding=UTF-8', ...args];
  const result = spawnSync(executable, args, { cwd, windowsHide: true, encoding: 'utf8', maxBuffer: 16 * 1024 * 1024 });
  if (!quiet) process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error('Command failed: ' + result.status);
  return result.stdout;
}
for (const directory of [task, compiled, remapped, tools, checks, payload, originals]) fs.mkdirSync(directory, { recursive: true });
if (!fs.existsSync(baseline)) fs.copyFileSync(installed, baseline);
if (hash(baseline) !== hash(installed)) throw new Error('Installed baseline changed');
const srgRoot = path.join(process.env.USERPROFILE, '.gradle/caches/modules-2/files-2.1/net.minecraftforge/srgutils/0.4.15');
const srg = fs.readdirSync(srgRoot).map(folder => path.join(srgRoot, folder, 'srgutils-0.4.15.jar')).find(fs.existsSync);
const toolClasspath = [tools, dependencies, srg].join(path.delimiter);
const tracked = [__filename, path.join(__dirname, 'deploy.ps1'), path.join(project, 'tools/deploy_jar.ps1'),
  ...['ArmorBlessingCatalog', 'ArmorFoodBlessings'].map(name => path.join(source, name + '.java')),
  ...['ArmorBlessingCheck', 'ArmorBlessingRuntimeCheck', 'ArmorResistanceCatalogDeltaCheck'].map(name => path.join(project, 'tools/verification', name + '.java')),
  ...['is_magic', 'is_poison'].map(name => path.join(project, 'src/main/resources/data/ftboss/tags/damage_type', name + '.json'))];
const sources = Object.fromEntries(tracked.map(file => [file, hash(file)]));
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', toolClasspath, '-d', tools,
  path.join(project, 'tools/runtime_patches/zicai-curio-silence-20261018/RemapSilence.java'),
  path.join(project, 'tools/runtime_patches/statue-exclusive-blessings/MapBlessingCheckJar.java'),
  ...['ZipStreamCheck', 'ArmorBlessingRuntimeCheck', 'ArmorResistanceCatalogDeltaCheck'].map(name => path.join(project, 'tools/verification', name + '.java'))]);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none',
  '-sourcepath', path.join(task, 'empty'), '-cp', dependencies, '-d', compiled,
  ...['ArmorBlessingCatalog', 'ArmorFoodBlessings'].map(name => path.join(source, name + '.java'))]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapSilence', path.join(project, 'build/createMcpToSrg/output.tsrg'), compiled, remapped, mapped, compiled]);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', [compiled, dependencies].join(path.delimiter), '-d', checks,
  path.join(project, 'tools/verification/ArmorBlessingCheck.java')]);
const data = run(path.join(jdk, 'java.exe'), ['-cp', [checks, compiled, dependencies].join(path.delimiter), 'ArmorBlessingCheck', '--data'], project, true);
const catalog = data.trim().split(/\r?\n/).map(line => {
  const [id, nameZh, nameEn, descriptionZh, descriptionEn] = line.split('\t');
  return { id, nameZh, nameEn, descriptionZh, descriptionEn };
});
if (catalog.length !== 100) throw new Error('Armor catalog size changed');
fs.writeFileSync(path.join(task, 'catalog.tsv'), data);
run(path.join(jdk, 'jar.exe'), ['--extract', '--file', baseline, 'assets/ftboss/lang/zh_cn.json', 'assets/ftboss/lang/en_us.json'], originals);
for (const language of ['zh_cn', 'en_us']) {
  const relative = 'assets/ftboss/lang/' + language + '.json';
  const sourceFile = path.join(project, 'src/main/resources', relative);
  const text = fs.readFileSync(sourceFile, 'utf8');
  const translations = JSON.parse(text);
  const original = JSON.parse(fs.readFileSync(path.join(originals, relative), 'utf8'));
  const additions = {};
  for (const entry of catalog) {
    additions['food_blessing.ftboss.' + entry.id] = language === 'zh_cn' ? entry.nameZh : entry.nameEn;
    additions['tooltip.ftboss.food_blessing.' + entry.id] = (language === 'zh_cn' ? entry.descriptionZh : entry.descriptionEn).replaceAll('%', '%%');
  }
  const changes = Object.entries(additions).filter(([key, value]) => translations[key] !== value);
  if (changes.length) {
    let patch = '*** Begin Patch\n*** Update File: ' + sourceFile.replaceAll('\\', '/') + '\n';
    for (const [key, value] of changes) {
      const old = text.split('\n').find(line => line.trimStart().startsWith(JSON.stringify(key) + ':'));
      if (!old) throw new Error('Missing existing language key: ' + key);
      patch += '@@\n-' + old + '\n+  ' + JSON.stringify(key) + ': ' + JSON.stringify(value) + (old.trimEnd().endsWith(',') ? ',' : '') + '\n';
    }
    patch += '*** End Patch\n';
    run(path.join(process.env.LOCALAPPDATA, 'OpenAI/Codex/bin/37f54d68e5533a51/codex.exe'), ['--codex-run-as-apply-patch', patch]);
  }
  sources[sourceFile] = hash(sourceFile);
  const destination = path.join(payload, relative);
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  fs.writeFileSync(destination, JSON.stringify({ ...original, ...additions }, null, 2) + '\n');
}
const classRoot = 'net/farmingtales/ftboss/item';
fs.mkdirSync(path.join(payload, classRoot), { recursive: true });
for (const name of fs.readdirSync(path.join(remapped, classRoot))) {
  if (!name.startsWith('ArmorBlessingCatalog') && !name.startsWith('ArmorFoodBlessings')) throw new Error('Unexpected compiled class: ' + name);
  fs.copyFileSync(path.join(remapped, classRoot, name), path.join(payload, classRoot, name));
}
for (const name of ['is_magic', 'is_poison']) {
  const relative = 'data/ftboss/tags/damage_type/' + name + '.json';
  const destination = path.join(payload, relative);
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  fs.copyFileSync(path.join(project, 'src/main/resources', relative), destination);
  const tag = JSON.parse(fs.readFileSync(destination, 'utf8'));
  if (tag.replace || tag.values.some(value => typeof value === 'object' && value.required !== false)) throw new Error('Optional compatibility is not optional');
}
function files(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => entry.isDirectory() ? files(path.join(directory, entry.name)) : [path.join(directory, entry.name)]);
}
const entries = files(payload).map(file => path.relative(payload, file).replaceAll('\\', '/'));
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, ...entries.flatMap(entry => ['-C', payload, entry])]);
run(path.join(jdk, 'java.exe'), ['-cp', tools, 'ZipStreamCheck', candidate]);
run(path.join(jdk, 'java.exe'), ['-cp', tools, 'ArmorResistanceCatalogDeltaCheck', baseline, candidate]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [candidate, tools, runtime].join(path.delimiter), 'ArmorBlessingRuntimeCheck', baseline, candidate]);
const named = path.join(task, 'candidate-named.jar');
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'MapBlessingCheckJar', path.join(project, 'build/createMcpToSrg/output.tsrg'), candidate, mapped, named]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [checks, named, compiled, dependencies].join(path.delimiter), 'ArmorBlessingCheck', named, path.join(task, 'behavior.json')]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
for (const [file, digest] of Object.entries(sources)) if (hash(file) !== digest) throw new Error('Source changed during validation: ' + file);
if (hash(installed) !== hash(baseline)) throw new Error('Concurrent deployment detected; rebuild');
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baselineSha256: hash(baseline), candidateSha256: hash(candidate),
  sources, entries, gameplayTested: false, docsTouched: false }, null, 2) + '\n');
fs.writeFileSync(path.join(project, '_work/armor-resistances-latest.json'), JSON.stringify({ task }, null, 2) + '\n');
console.log('PASS candidate ready: ' + candidate);
