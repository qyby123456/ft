'use strict';

const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const assert = require('node:assert/strict');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');
const root = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2]);
assert(task.startsWith(path.join(root, '_work') + path.sep));
const baseline = path.join(task, 'baseline.jar');
const mapped = path.join(task, 'baseline-mapped.jar');
assert(fs.existsSync(baseline) && fs.existsSync(mapped), 'Snapshot and mapped baseline required');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const live = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
assert.equal(hash(live), hash(baseline), 'Live baseline changed; rebase required');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const gradle = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle');
const forgeRoot = path.join(gradle, 'minecraft_user_repo/net/minecraftforge/forge');
const officialForge = path.join(forgeRoot, '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const forge = path.join(forgeRoot, '1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar');
const mapping = path.join(gradle, 'minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg');
const renamer = path.join(gradle, 'maven_downloader/net/minecraftforge/ForgeAutoRenamingTool/1.0.2/ForgeAutoRenamingTool-1.0.2-all.jar');
const asm = ['asm', 'asm-tree', 'asm-commons', 'asm-analysis', 'asm-util'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
const classes = path.join(task, 'classes');
const tools = path.join(task, 'validation-tools');
const payload = path.join(task, 'payload');
const empty = path.join(task, 'empty-sourcepath');
for (const directory of [classes, tools, payload, empty]) fs.mkdirSync(directory, { recursive: true });
const transcript = path.join(task, 'build.log');
fs.writeFileSync(transcript, '');
function run(command, args, cwd = root) {
  const result = spawnSync(command, args, { cwd, encoding: 'utf8', windowsHide: true, maxBuffer: 64 * 1024 * 1024 });
  fs.appendFileSync(transcript, `${command} ${args.join(' ')}\n${result.stdout || ''}${result.stderr || ''}\n`);
  if (result.error || result.status !== 0) throw result.error || new Error(result.stdout + result.stderr);
  process.stdout.write(result.stdout || 'PASS ' + path.basename(command) + '\n');
}
const java = args => run(path.join(jdk, 'java.exe'), args);
function walk(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => entry.isDirectory()
    ? walk(path.join(directory, entry.name)) : [path.join(directory, entry.name)]);
}
function classpathJar(name, entries) {
  let line = 'Class-Path: ' + [...new Set(entries)].map(file => {
    assert(fs.existsSync(file), file);
    return pathToFileURL(file).href.replaceAll('[', '%5B').replaceAll(']', '%5D') + (fs.statSync(file).isDirectory() ? '/' : '');
  }).join(' ');
  let manifest = 'Manifest-Version: 1.0\r\n';
  while (line.length > 70) { manifest += line.slice(0, 70) + '\r\n'; line = ' ' + line.slice(70); }
  const manifestPath = path.join(task, name + '.mf');
  fs.writeFileSync(manifestPath, manifest + line + '\r\n\r\n', 'ascii');
  const output = path.join(task, name + '.jar');
  run(path.join(jdk, 'jar.exe'), ['--create', '--file', output, '--manifest', manifestPath]);
  return output;
}
const mods = fs.readdirSync(path.dirname(live)).filter(file => file.endsWith('.jar') && file !== path.basename(live))
  .map(file => path.join(path.dirname(live), file));
const exported = fs.readFileSync(path.join(root, '_work/food-weapon-build-20261012/build-project/build/verification/dandan-classpath.txt'), 'utf8').trim().split(path.delimiter);
const sources = ['util/ManorRefinementRules.java', 'boss/ai/ManorPredictiveAttacks.java',
  'boss/ai/DandanTeleportSeal.java', 'mixin/DandanPlayerTeleportMixin.java', 'mixin/DandanConnectionTeleportMixin.java']
  .map(file => path.join(root, 'src/main/java/net/farmingtales/ftboss', file));
sources.push(path.join(root, 'tools/runtime_patches/ManorSkyTemplate.java'));
const tracked = [...sources, __filename, path.join(root, 'tools/runtime_patches/ManorSixPatch.java'),
  path.join(root, 'tools/verification/ManorSixCheck.java'), path.join(root, 'tools/boss_preview_six_refinements.cjs'),
  ...['entity/EasterBossEntity.java', 'boss/ai/XuanlanSkyStrike.java', 'boss/ai/QizaiMagic.java', 'util/GongdiCombatRules.java',
    'boss/ManorFight.java'].map(file => path.join(root, 'src/main/java/net/farmingtales/ftboss', file)),
  path.join(root, 'src/main/resources/ftboss.mixins.json'), path.join(root, 'tools/boss_preview_manor.cjs')];
const sourceHashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
const compileDependencies = classpathJar('compile-dependencies', [mapped, officialForge, ...exported, ...mods]);
run(path.join(jdk, 'javac.exe'), ['-J-Dfile.encoding=UTF-8', '--release', '17', '-encoding', 'UTF-8', '-proc:none',
  '-sourcepath', empty, '-cp', compileDependencies, '-d', classes, ...sources]);
const official = path.join(task, 'official-helpers.jar');
run(path.join(jdk, 'jar.exe'), ['--create', '--file', official, '-C', classes, '.']);
const srg = path.join(task, 'srg-helpers.jar');
java(['-Xmx2G', '-jar', renamer, '--input', official, '--output', srg, '--map', mapping, '--reverse',
  '--lib', officialForge, '--lib', mapped, '--threads', '2']);
run(path.join(jdk, 'jar.exe'), ['xf', srg], payload);
for (const file of walk(payload)) if (!file.endsWith('.class') || /ManorSkyTemplate/.test(file)) fs.unlinkSync(file);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '--release', '17', '-cp', asm.join(path.delimiter), '-d', tools,
  path.join(root, 'tools/runtime_patches/ManorSixPatch.java'), path.join(root, 'tools/verification/ManorSixCheck.java'),
  path.join(root, 'tools/verification/FoodWeaponBuildLinkCheck.java'), path.join(root, 'tools/verification/ZipStreamCheck.java')]);
java(['-Xverify:all', '-cp', [tools, ...asm].join(path.delimiter), 'ManorSixPatch', baseline, srg, payload]);
const resources = path.join(task, 'resources');
fs.mkdirSync(resources, { recursive: true });
run(path.join(jdk, 'jar.exe'), ['xf', baseline, 'ftboss.mixins.json', 'assets/ftboss/boss_preview/catalog.json'], resources);
const mixins = JSON.parse(fs.readFileSync(path.join(resources, 'ftboss.mixins.json'), 'utf8'));
for (const name of ['DandanPlayerTeleportMixin', 'DandanConnectionTeleportMixin']) {
  assert(!mixins.mixins.includes(name), 'Already patched mixin');
  mixins.mixins.push(name);
}
fs.writeFileSync(path.join(payload, 'ftboss.mixins.json'), JSON.stringify(mixins, null, 2) + '\n');
const catalogEntry = 'assets/ftboss/boss_preview/catalog.json';
const catalog = JSON.parse(fs.readFileSync(path.join(resources, catalogEntry), 'utf8'));
const additions = require('./boss_preview_six_refinements.cjs');
for (const [variant, changes] of Object.entries(additions)) {
  const matches = catalog.filter(boss => boss.variant === Number(variant));
  assert.equal(matches.length, 1, 'Guide variant ' + variant);
  const boss = matches[0];
  for (const [section, paragraphs] of Object.entries(changes)) {
    const target = boss.sections.find(value => value.id === (section === 'limits' ? 'arena' : section));
    assert(target, 'Missing section ' + section);
    target.paragraphs.push(...paragraphs);
    boss.guide.push(...paragraphs);
  }
}
fs.mkdirSync(path.dirname(path.join(payload, catalogEntry)), { recursive: true });
fs.writeFileSync(path.join(payload, catalogEntry), JSON.stringify(catalog, null, 2) + '\n');
const entries = walk(payload).map(file => path.relative(payload, file).replaceAll('\\', '/')).sort();
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, ...entries.flatMap(entry => ['-C', payload, entry])]);
const libraries = walk(path.join(minecraft, 'libraries')).filter(file => file.endsWith('.jar') && !/natives|sources|javadoc/.test(path.basename(file)));
const runtime = classpathJar('runtime-dependencies', [candidate, tools, forge, ...asm, ...libraries, ...mods]);
java(['-Xverify:all', '-cp', runtime, 'ManorSixCheck', baseline, candidate, forge, payload]);
java(['-Xverify:all', '-cp', runtime, 'FoodWeaponBuildLinkCheck', payload, candidate]);
java(['-Xverify:all', '-cp', runtime, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(root, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
run(process.execPath, [path.join(root, 'tools/verification/check_blank_statue_regression.cjs'), path.join(task, 'statue-registration'), candidate]);
assert.equal(hash(live), hash(baseline), 'Live baseline changed during build');
for (const file of tracked) {
  assert.equal(hash(file), sourceHashes[file], 'Source changed during build: ' + file);
  const snapshot = path.join(task, 'sources', path.relative(root, file));
  fs.mkdirSync(path.dirname(snapshot), { recursive: true });
  fs.copyFileSync(file, snapshot);
}
fs.writeFileSync(path.join(task, 'report.json'), JSON.stringify({ status: 'PASS_NOT_DEPLOYED',
  baselineSHA256: hash(baseline), candidateSHA256: hash(candidate), entries, sourceHashes,
  actualForgeStartup: false, actualProductionWrites: 0, inGameTested: false,
  note: 'Requires game exit before dual-lock deployment; never replace a running jar.' }, null, 2) + '\n');
console.log('PASS: ' + candidate);
