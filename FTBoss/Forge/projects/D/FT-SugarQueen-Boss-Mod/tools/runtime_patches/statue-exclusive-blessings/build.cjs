const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const project = path.resolve(__dirname, '../../..');
const task = path.join(project, '_work/statue-exclusive-blessings-20261025');
const jdk = path.join(process.env.APPDATA, '.minecraft/runtime/java-runtime-gamma-snapshot/bin');
const dependencies = path.join(project, '_work/zicai-theme-zheergen-trail-20261025/compile-dependencies.jar');
const mapped = path.join(project, 'build/fg_cache/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const baseline = path.join(task, 'baseline.jar');
const candidate = path.join(task, 'qys-bosses.jar');
const classes = path.join(task, 'tool-classes');
const compiled = path.join(task, 'compiled');
const templates = path.join(task, 'templates/net/farmingtales/ftboss/item');
const remapped = path.join(task, 'remapped');
const payload = path.join(task, 'payload');
const sourceRoot = path.join(project, 'src/main/java/net/farmingtales/ftboss/item');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const tracked = ['FoodWeaponEvolution.java', 'FoodBlessingCombat.java', 'StatueBlessingCatalog.java', 'StatueBlessingCombat.java']
  .map(file => path.join(sourceRoot, file));
tracked.push(__filename, path.join(__dirname, 'PatchStatueBlessings.java'), path.join(__dirname, 'MapBlessingCheckJar.java'),
  ...['StatueBlessingCatalogCheck.java', 'StatueBlessingCombatCheck.java', 'StatueBlessingRuntimeCheck.java',
    'BlessingCombatCheck.java', 'ExtractBlessingCheck.java'].map(file => path.join(project, 'tools/verification', file)),
  ...['zh_cn', 'en_us'].map(language => path.join(task, `design/lang_${language}.fragment.json`)));
tracked.push(...['zh_cn', 'en_us'].map(language => path.join(project, `src/main/resources/assets/ftboss/lang/${language}.json`)));
const sourceHashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
for (const folder of [classes, compiled, templates, remapped, payload]) fs.mkdirSync(folder, { recursive: true });
function run(executable, args, cwd = project) {
  const result = spawnSync(executable, args, { cwd, windowsHide: true, encoding: 'utf8', maxBuffer: 16 * 1024 * 1024 });
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error('Command failed: ' + result.status);
}
function block(source, signature) {
  const start = source.indexOf(signature);
  if (start < 0) throw new Error('Missing source block: ' + signature);
  let depth = 0;
  for (let index = source.indexOf('{', start); index < source.length; index++) {
    if (source[index] === '{') depth++;
    if (source[index] === '}' && --depth === 0) return source.slice(start, index + 1);
  }
  throw new Error('Unclosed source block: ' + signature);
}
const source = fs.readFileSync(path.join(sourceRoot, 'FoodWeaponEvolution.java'), 'utf8');
const originalBlessings = new Set([...block(source, 'private enum FoodBlessing {')
  .matchAll(/^\s*\w+\("([^"]+)", FoodBlessingKind\./gm)].map(match => match[1]));
if (originalBlessings.size !== 30) throw new Error('Expected unchanged original 30 blessing ids');
const imports = source.slice(0, source.indexOf('/**'));
const skills = block(source, 'public enum EvolutionSkill').split('private final')[0].replace(/\("[^"]+", SkillEffect\.\w+\)/g, '').trim();
const template = imports + '\npublic final class FoodWeaponEvolution {\n'
  + 'private static final int MAX_FOOD_BLESSINGS = 6; private static final String FOOD_BLESSINGS="FoodBlessings", PROFILE="Profile";\n'
  + 'public enum FoodType { MEAT,CHEESE,TEA,WINE,SWEET,APPLE,FISH,VEGETABLE,ENERGY,FOOD; native Component displayName(); }\n'
  + skills + '\n}\n'
  + block(source, 'public enum Profile') + '\n'
  + 'private enum SocketTier { BASIC(1),FINE(2),GOURMET(3); private final int level; SocketTier(int level){this.level=level;} native Component displayName(); }\n'
  + 'public record FoodBlessingView(Component name,Component description,String rank){}\n'
  + 'private record AcquiredSkill(Profile profile,EvolutionSkill skill){}\n'
  + 'private static native Profile boundProfile(ItemStack stack); private static native ListTag foodBlessingList(ItemStack stack);\n'
  + 'private static native SocketTier foodTier(ItemStack stack); private static native CompoundTag writableData(ItemStack stack,Profile profile);\n'
  + 'private static native String itemId(ItemStack stack); private static native String toRoman(int level);\n'
  + 'private static native int foodBlessingLevel(CompoundTag entry); private static native List<AcquiredSkill> gourmetSkills(ItemStack stack);\n'
  + ['private static List<StatueBlessingCatalog.Definition> blessingPool(', 'private static boolean applyFoodBlessing(',
    'public static List<FoodBlessingView> foodBlessingViews(', 'private static EvolutionSkill drawGourmetSkill(']
    .map(signature => block(source, signature)).join('\n') + '\n}\n';
fs.writeFileSync(path.join(templates, 'FoodWeaponEvolution.java'), template);
const srgRoot = path.join(process.env.USERPROFILE, '.gradle/caches/modules-2/files-2.1/net.minecraftforge/srgutils/0.4.15');
const srg = fs.readdirSync(srgRoot).map(folder => path.join(srgRoot, folder, 'srgutils-0.4.15.jar')).find(fs.existsSync);
const toolClasspath = [classes, dependencies, srg].join(path.delimiter);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', toolClasspath, '-d', classes,
  path.join(__dirname, 'PatchStatueBlessings.java'), path.join(__dirname, '../zicai-curio-silence-20261018/RemapSilence.java'),
  path.join(project, 'tools/verification/ZipStreamCheck.java'), path.join(project, 'tools/verification/StatueBlessingRuntimeCheck.java'),
  path.join(project, 'tools/verification/VerifyBlessingWiring.java'), path.join(__dirname, 'MapBlessingCheckJar.java')]);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', compiled,
  path.join(templates, 'FoodWeaponEvolution.java'), path.join(sourceRoot, 'StatueBlessingCatalog.java'), path.join(sourceRoot, 'StatueBlessingCombat.java')]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapSilence', path.join(project, 'build/createMcpToSrg/output.tsrg'),
  compiled, remapped, mapped, compiled]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'PatchStatueBlessings', baseline, remapped, payload]);
for (const file of fs.readdirSync(path.join(remapped, 'net/farmingtales/ftboss/item'))) {
  if (file.startsWith('StatueBlessing')) fs.copyFileSync(path.join(remapped, 'net/farmingtales/ftboss/item', file),
    path.join(payload, 'net/farmingtales/ftboss/item', file));
}
const baselineResources = path.join(task, 'baseline-resources');
fs.mkdirSync(baselineResources, { recursive: true });
run(path.join(jdk, 'jar.exe'), ['--extract', '--file', baseline, 'assets/ftboss/lang/zh_cn.json', 'assets/ftboss/lang/en_us.json'], baselineResources);
for (const language of ['zh_cn', 'en_us']) {
  const original = JSON.parse(fs.readFileSync(path.join(baselineResources, `assets/ftboss/lang/${language}.json`), 'utf8'));
  const fragment = JSON.parse(fs.readFileSync(path.join(task, `design/lang_${language}.fragment.json`), 'utf8'));
  const additions = Object.fromEntries(Object.entries(fragment)
    .filter(([key]) => !originalBlessings.has(key.slice(key.lastIndexOf('.') + 1)))
    .map(([key, value]) => [key, value.replaceAll('%', '%%').replaceAll('%%s', '%s')]));
  if (Object.keys(additions).length !== 140) throw new Error('Expected 70 new names and 70 new descriptions: ' + language);
  const sourceLanguage = JSON.parse(fs.readFileSync(path.join(project, `src/main/resources/assets/ftboss/lang/${language}.json`), 'utf8'));
  for (const suffix of ['rule', 'complete']) {
    const key = 'screen.ftboss.statue_hub.enchant.' + suffix;
    additions[key] = sourceLanguage[key];
  }
  const merged = { ...original, ...additions };
  const target = path.join(payload, `assets/ftboss/lang/${language}.json`);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.writeFileSync(target, JSON.stringify(merged, null, 2) + '\n');
}
function files(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const target = path.join(directory, entry.name);
    return entry.isDirectory() ? files(target) : [target];
  });
}
const entries = files(payload).map(file => path.relative(payload, file).replaceAll('\\', '/'));
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, ...entries.flatMap(entry => ['-C', payload, entry])]);
run(path.join(jdk, 'java.exe'), ['-cp', classes, 'ZipStreamCheck', candidate]);
const runtime = path.join(project, '_work/zicai-theme-zheergen-trail-20261025/runtime-dependencies.jar');
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [candidate, classes, runtime].join(path.delimiter),
  'StatueBlessingRuntimeCheck', baseline, candidate]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'VerifyBlessingWiring', candidate]);
const namedCheck = path.join(task, 'candidate-mapped-check.jar');
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'MapBlessingCheckJar', path.join(project, 'build/createMcpToSrg/output.tsrg'),
  candidate, mapped, namedCheck]);
const combatChecks = path.join(task, 'combat-classes');
const legacyChecks = path.join(task, 'legacy-classes');
const catalogChecks = path.join(task, 'catalog-classes');
for (const directory of [combatChecks, legacyChecks, catalogChecks]) fs.mkdirSync(directory, { recursive: true });
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', dependencies, '-d', combatChecks,
  path.join(project, 'tools/verification/StatueBlessingCombatCheck.java')]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [combatChecks, dependencies].join(path.delimiter),
  'StatueBlessingCombatCheck', namedCheck, path.join(task, 'combat-dump')]);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', dependencies, '-d', legacyChecks,
  ...['BlessingCombatCheck.java', 'ExtractBlessingCheck.java'].map(file => path.join(project, 'tools/verification', file))]);
run(path.join(jdk, 'java.exe'), ['-cp', [legacyChecks, dependencies].join(path.delimiter), 'ExtractBlessingCheck', namedCheck, legacyChecks]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', legacyChecks, 'BlessingCombatCheck', '--hit-effects-only']);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-d', catalogChecks,
  path.join(sourceRoot, 'StatueBlessingCatalog.java'), path.join(project, 'tools/verification/StatueBlessingCatalogCheck.java')]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', catalogChecks, 'StatueBlessingCatalogCheck']);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
for (const file of tracked) if (hash(file) !== sourceHashes[file]) throw new Error('Source changed during build: ' + file);
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: hash(baseline),
  candidate, candidateSha256: hash(candidate), entries, sources: sourceHashes, gameplayTested: false }, null, 2) + '\n');
console.log('PASS candidate ready: ' + candidate);
