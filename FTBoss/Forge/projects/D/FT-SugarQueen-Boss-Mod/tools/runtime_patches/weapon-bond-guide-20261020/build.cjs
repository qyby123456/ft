'use strict';

const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const assert = require('node:assert/strict');
const { spawnSync } = require('node:child_process');
const root = path.resolve(__dirname, '../../..');
const output = path.join(root, '_work/statue-weapon-bond-guide-20261020/build-' + Date.now());
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const live = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const gradle = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle');
const mappings = path.join(gradle, 'minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg');
const renamer = path.join(gradle, 'maven_downloader/net/minecraftforge/ForgeAutoRenamingTool/1.0.2/ForgeAutoRenamingTool-1.0.2-all.jar');
const forge = path.join(gradle, 'minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar');
const officialForge = path.join(gradle, 'minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const hash = contents => crypto.createHash('sha256').update(contents).digest('hex');
const sourceFiles = [
  'src/main/java/net/farmingtales/ftboss/client/StatueWeaponBondGuideEntry.java',
  'src/main/java/net/farmingtales/ftboss/client/StatueWeaponBondGuideScreen.java',
  'src/main/java/net/farmingtales/ftboss/util/WeaponBondGuideLayout.java',
  'src/main/resources/assets/ftboss/weapon_bonds/guide.json',
  'src/main/resources/assets/ftboss/lang/zh_cn.json',
  'src/main/resources/assets/ftboss/lang/en_us.json'
];
fs.mkdirSync(output, { recursive: true });
const expected = hash(fs.readFileSync(live));
const sourceHashes = Object.fromEntries([...sourceFiles, 'src/main/java/net/farmingtales/ftboss/client/BossPreviewScreen.java']
  .map(relative => [relative, hash(fs.readFileSync(path.join(root, relative)))]));
const baseline = path.join(output, 'baseline.jar');
const mapped = path.join(output, 'baseline-named.jar');
fs.copyFileSync(live, baseline);
function run(command, args, cwd = root) {
  const result = spawnSync(command, args, { cwd, encoding: 'utf8', windowsHide: true, maxBuffer: 32 * 1024 * 1024 });
  fs.appendFileSync(path.join(output, 'build.log'), result.stdout + result.stderr);
  if (result.status !== 0) throw new Error(result.stdout + result.stderr);
  console.log('PASS ' + path.basename(command) + ' ' + (args.find(value => typeof value === 'string' && value.endsWith('Check')) || ''));
  return result.stdout;
}
const java = args => run(path.join(jdk, 'java.exe'), args);
function walk(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => entry.isDirectory()
    ? walk(path.join(directory, entry.name)) : [path.join(directory, entry.name)]);
}
const catalog = JSON.parse(fs.readFileSync(path.join(root, sourceFiles[3]), 'utf8'));
assert.equal(catalog.length, 25);
assert.deepEqual([0, 1, 2, 3].map(tab => catalog.filter(entry => entry.tab === tab).length), [1, 12, 3, 9]);
assert.equal(new Set(catalog.map(entry => entry.id)).size, 25);
const languages = ['zh_cn', 'en_us'].map(locale => JSON.parse(fs.readFileSync(path.join(root, 'src/main/resources/assets/ftboss/lang/' + locale + '.json'), 'utf8')));
const guideKeys = languages.map(language => Object.keys(language).filter(key => key.startsWith('guide.ftboss.bonds.')).sort());
assert.deepEqual(guideKeys[0], guideKeys[1]);
for (const key of guideKeys[0]) assert.equal((languages[0][key].match(/%s/g) || []).length, (languages[1][key].match(/%s/g) || []).length, key);
for (const entry of catalog) {
  for (const suffix of ['title', 'requirement', 'effect', 'note']) for (const language of languages) assert(language['guide.ftboss.bonds.' + entry.id + '.' + suffix]);
  for (const group of entry.groups) {
    assert(group.count >= 1 && group.count <= 3 && group.items.length >= 1 && group.items.length <= 2);
    for (const item of group.items) assert(/^[a-z0-9_.-]+:[a-z0-9_/.-]+$/.test(item));
  }
}
const screen = fs.readFileSync(path.join(root, sourceFiles[1]), 'utf8');
assert(!/sendToServer|setTag\(|getOrCreateTag|setDamageValue|\.shrink\(|addFreshEntity/.test(screen));
assert(screen.includes('client.player.closeContainer()') && screen.includes('!available.getAsBoolean()'));
const rendered = screen.slice(screen.indexOf('public void render('), screen.indexOf('public boolean mouseScrolled'));
assert(!rendered.includes('rebuild()') && !rendered.includes('getResourceManager()'));
const checks = path.join(output, 'checks');
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-d', checks,
  path.join(root, sourceFiles[2]), path.join(root, 'tools/verification/WeaponBondGuideLayoutCheck.java')]);
java(['-Xverify:all', '-cp', checks, 'WeaponBondGuideLayoutCheck']);
for (const check of ['check_boss_preview_layout.cjs', 'check_boss_preview_privacy.cjs', 'check_boss_preview_summary.cjs']) {
  run(process.execPath, [path.join(root, 'tools/verification', check), path.join(output, check.replace('.cjs', ''))]);
}
java(['-Xmx2G', '-jar', renamer, '--input', baseline, '--output', mapped, '--map', mappings, '--lib', forge, '--threads', '2']);
const classpath = fs.readFileSync(path.join(root, '_work/food-weapon-build-20261012/build-project/build/verification/dandan-classpath.txt'), 'utf8').trim();
const classes = path.join(output, 'classes');
const empty = path.join(output, 'empty-sourcepath');
fs.mkdirSync(empty);
run(path.join(jdk, 'javac.exe'), ['-J-Dfile.encoding=UTF-8', '--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none',
  '-sourcepath', empty, '-classpath', mapped + path.delimiter + classpath, '-d', classes,
  ...sourceFiles.slice(0, 3).map(relative => path.join(root, relative))]);
const named = path.join(output, 'named.jar');
const srg = path.join(output, 'srg.jar');
run(path.join(jdk, 'jar.exe'), ['--create', '--file', named, '-C', classes, '.']);
java(['-Xmx2G', '-jar', renamer, '--input', named, '--output', srg, '--map', mappings, '--reverse', '--lib', officialForge, '--lib', mapped, '--threads', '2']);
const payload = path.join(output, 'payload');
fs.mkdirSync(payload);
run(path.join(jdk, 'jar.exe'), ['xf', srg], payload);
fs.unlinkSync(path.join(payload, 'META-INF/MANIFEST.MF'));
const asm = ['asm', 'asm-tree', 'asm-util', 'asm-analysis', 'asm-commons'].map(name => path.join(minecraft, 'libraries/org/ow2/asm/' + name + '/9.7.1/' + name + '-9.7.1.jar'));
const validation = path.join(output, 'validation');
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-cp', asm.join(path.delimiter), '-d', validation,
  path.join(__dirname, 'PatchWeaponBondGuide.java'),
  ...['FoodWeaponBuildLinkCheck.java', 'ZipStreamCheck.java', 'EncodeJvmArguments.java', 'WeaponBondGuideAudit.java'].map(name => path.join(root, 'tools/verification', name))]);
java(['-Xverify:all', '-cp', [validation, ...asm].join(path.delimiter), 'PatchWeaponBondGuide', baseline, payload]);
const originalLanguages = path.join(output, 'original-languages');
fs.mkdirSync(originalLanguages);
run(path.join(jdk, 'jar.exe'), ['xf', baseline, 'assets/ftboss/lang/zh_cn.json', 'assets/ftboss/lang/en_us.json'], originalLanguages);
for (const [index, locale] of ['zh_cn', 'en_us'].entries()) {
  const relative = 'assets/ftboss/lang/' + locale + '.json';
  const original = JSON.parse(fs.readFileSync(path.join(originalLanguages, relative), 'utf8'));
  const merged = { ...original };
  for (const key of guideKeys[index]) merged[key] = languages[index][key];
  for (const [key, value] of Object.entries(original)) if (!key.startsWith('guide.ftboss.bonds.')) assert.equal(merged[key], value);
  const target = path.join(payload, relative);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.writeFileSync(target, JSON.stringify(merged, null, 2) + '\n');
}
const catalogTarget = path.join(payload, 'assets/ftboss/weapon_bonds/guide.json');
fs.mkdirSync(path.dirname(catalogTarget), { recursive: true });
fs.copyFileSync(path.join(root, sourceFiles[3]), catalogTarget);
const candidate = path.join(output, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
const entries = walk(payload).map(filename => path.relative(payload, filename).replaceAll('\\', '/')).filter(relative => !relative.startsWith('META-INF/'));
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, ...entries.flatMap(entry => ['-C', payload, entry])]);
const beforeTree = path.join(output, 'before-tree');
const afterTree = path.join(output, 'after-tree');
fs.mkdirSync(beforeTree);
fs.mkdirSync(afterTree);
run(path.join(jdk, 'jar.exe'), ['xf', baseline], beforeTree);
run(path.join(jdk, 'jar.exe'), ['xf', candidate], afterTree);
let preserved = 0;
for (const filename of walk(beforeTree)) {
  const relative = path.relative(beforeTree, filename).replaceAll('\\', '/');
  if (entries.includes(relative)) continue;
  assert.equal(hash(fs.readFileSync(filename)), hash(fs.readFileSync(path.join(afterTree, relative))), 'Unrelated jar entry changed: ' + relative);
  preserved++;
}
for (const filename of walk(afterTree)) {
  const relative = path.relative(afterTree, filename).replaceAll('\\', '/');
  assert(entries.includes(relative) || fs.existsSync(path.join(beforeTree, relative)), 'Unexpected entry: ' + relative);
}
const libraries = walk(path.join(minecraft, 'libraries')).filter(filename => filename.endsWith('.jar') && !/natives|sources|javadoc/.test(path.basename(filename)));
const mods = fs.readdirSync(path.dirname(live)).filter(name => name.endsWith('.jar') && name !== 'qys-bosses.jar').map(name => path.join(path.dirname(live), name));
const runtimeClasspath = [candidate, validation, forge, ...asm, ...libraries, ...mods].join(path.delimiter);
const argumentsFile = path.join(output, 'link.args');
fs.writeFileSync(argumentsFile, ['-Xverify:all', '-cp', runtimeClasspath, 'FoodWeaponBuildLinkCheck', payload, candidate]
  .map(value => '"' + value.replaceAll('\\', '/') + '"').join('\n') + '\n');
const nativeArguments = path.join(output, 'link-native.args');
java(['-cp', validation, 'EncodeJvmArguments', argumentsFile, nativeArguments, path.join(output, 'aliases')]);
java(['-Dfile.encoding=UTF-8', '@' + nativeArguments]);
java(['-Xverify:all', '-cp', [validation, ...asm].join(path.delimiter), 'WeaponBondGuideAudit', baseline, candidate]);
java(['-cp', validation, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(root, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
run(process.execPath, [path.join(root, 'tools/verification/check_blank_statue_regression.cjs'), path.join(output, 'statue-checks'), candidate]);
assert.equal(hash(fs.readFileSync(live)), expected, 'Live baseline changed; rebuild before deploying');
for (const [relative, expectedHash] of Object.entries(sourceHashes)) assert.equal(hash(fs.readFileSync(path.join(root, relative))), expectedHash);
fs.writeFileSync(path.join(output, 'report.json'), JSON.stringify({ status: 'PASS_NOT_DEPLOYED', baselineSHA256: expected,
  candidateSHA256: hash(fs.readFileSync(candidate)), candidate, pages: catalog.length, translationKeys: guideKeys[0].length,
  changedEntries: entries, preservedEntries: preserved, sourceHashes, gameplayChanged: false, inGameTested: false }, null, 2) + '\n');
console.log('CANDIDATE ' + candidate);
