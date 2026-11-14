const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const project = path.resolve(__dirname, '../../..');
const instance = path.join(process.env.APPDATA, '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2');
const installed = path.join(instance, 'mods/qys-bosses.jar');
const baselineId = crypto.createHash('sha256').update(fs.readFileSync(installed)).digest('hex').slice(0, 10);
const task = path.join(project, '_work/armor-blessings-20261027-' + baselineId);
const jdk = path.join(process.env.APPDATA, '.minecraft/runtime/java-runtime-gamma-snapshot/bin');
const dependencies = path.join(project, '_work/zicai-theme-zheergen-trail-20261025/compile-dependencies.jar');
const runtime = path.join(project, '_work/zicai-theme-zheergen-trail-20261025/runtime-dependencies.jar');
const mapped = path.join(project, 'build/fg_cache/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const baseline = path.join(task, 'baseline.jar');
const candidate = path.join(task, 'qys-bosses.jar');
const tools = path.join(task, 'tool-classes');
const compiled = path.join(task, 'compiled');
const remapped = path.join(task, 'remapped');
const payload = path.join(task, 'payload');
const templates = path.join(task, 'templates/net/farmingtales/ftboss');
const sourceRoot = path.join(project, 'src/main/java/net/farmingtales/ftboss');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
function run(executable, args, cwd = project, quiet = false) {
  if (path.basename(executable).toLowerCase() === 'java.exe') args = ['-Dfile.encoding=UTF-8', ...args];
  if (path.basename(executable).toLowerCase() === 'javac.exe') args = ['-J-Dfile.encoding=UTF-8', ...args];
  const result = spawnSync(executable, args, { cwd, windowsHide: true, encoding: 'utf8', maxBuffer: 16 * 1024 * 1024 });
  if (!quiet) process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error('Command failed: ' + result.status);
  return result.stdout;
}
function block(source, signature) {
  const start = source.indexOf(signature);
  if (start < 0) throw new Error('Missing source block: ' + signature);
  let depth = 0;
  for (let index = source.indexOf('{', start); index < source.length; index++) {
    if (source[index] === '{') depth++;
    if (source[index] === '}' && --depth === 0) return source.slice(start, index + 1);
  }
  throw new Error('Unclosed block: ' + signature);
}
for (const directory of [task, tools, compiled, remapped, payload, path.join(templates, 'item'), path.join(templates, 'client')]) fs.mkdirSync(directory, { recursive: true });
if (!fs.existsSync(baseline)) fs.copyFileSync(installed, baseline);
if (hash(baseline) !== hash(installed)) throw new Error('Installed baseline changed; use a fresh task workspace');
const tracked = [__filename, path.join(__dirname, 'PatchArmorBlessings.java'), path.join(__dirname, 'deploy.ps1'),
  path.join(project, 'tools/runtime_patches/statue-exclusive-blessings/MapBlessingCheckJar.java'),
  ...['ArmorBlessingCatalog', 'ArmorFoodBlessings', 'FoodWeaponEvolution', 'ArrowPassiveDamage', 'FoodBlessingCombat', 'StatueBlessingCombat'].map(name => path.join(sourceRoot, 'item', name + '.java')),
  path.join(sourceRoot, 'client/StatueHubScreen.java'), path.join(project, 'tools/verification/ArmorBlessingCheck.java'),
  path.join(project, 'tools/verification/ArmorBlessingRuntimeCheck.java')];
tracked.push(...['BlessingCombatCheck', 'OriginalBlessingPassiveCheck', 'ExtractBlessingCheck', 'StatueBlessingCombatCheck', 'StatueBlessingCatalogCheck', 'VerifyBlessingWiring']
  .map(name => path.join(project, 'tools/verification', name + '.java')), path.join(project, 'tools/verification/BlessingEffectAudit.java'), path.join(sourceRoot, 'util/MeleeEnhancementRules.java'),
  path.join(sourceRoot, 'item/StatueBlessingCatalog.java'));
tracked.push(...['RangedHybridCombatCheck', 'ExtractRangedHybridCheck', 'BowBlessingTriggerCheck', 'FoodWeaponRangedHookCheck', 'VolleyPassiveNumbersCheck']
  .map(name => path.join(project, 'tools/verification', name + '.java')));
const sourceHashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
const source = fs.readFileSync(path.join(sourceRoot, 'item/FoodWeaponEvolution.java'), 'utf8');
const skills = block(source, 'public enum EvolutionSkill').split('private final')[0].replace(/\("[^"]+", SkillEffect\.\w+\)/g, '').trim();
const template = source.slice(0, source.indexOf('/**')) + '\npublic final class FoodWeaponEvolution {\n'
  + 'private static final int MAX_FOOD_BLESSINGS=6; private static final String FOOD_BLESSINGS="FoodBlessings", PROFILE="Profile";\n'
  + 'public enum FoodType { MEAT,CHEESE,TEA,WINE,SWEET,APPLE,FISH,VEGETABLE,ENERGY,FOOD; native Component displayName(); }\n'
  + skills + '\n}\n' + block(source, 'public enum Profile') + '\n'
  + 'private enum SocketTier { BASIC(1),FINE(2),GOURMET(3); private final int level; SocketTier(int level){this.level=level;} native Component displayName(); }\n'
  + 'public record FoodBlessingView(Component name,Component description,String rank){}\n'
  + 'private static native Profile boundProfile(ItemStack stack); private static native ListTag foodBlessingList(ItemStack stack);\n'
  + 'private static native SocketTier foodTier(ItemStack stack); private static native CompoundTag writableData(ItemStack stack,Profile profile);\n'
  + 'private static native String itemId(ItemStack stack); private static native String toRoman(int level); private static native int foodBlessingLevel(CompoundTag entry);\n'
  + 'private static native List<StatueBlessingCatalog.Definition> blessingPool(Profile profile); private static native boolean weaponCandidate(ItemStack stack);\n'
  + 'private static native FoodType foodType(ItemStack stack); public static native ItemStack heldEvolutionWeapon(ServerPlayer player); public static native double damageEvolutionMultiplier(ItemStack stack);\n'
  + ['private static boolean applyFoodBlessing(', 'public static List<FoodBlessingView> foodBlessingViews(',
    'public static boolean isBlessingCandidate(', 'public static boolean applyFoodBlessingFromUi('].map(signature => block(source, signature)).join('\n') + '\n}\n';
fs.writeFileSync(path.join(templates, 'item/FoodWeaponEvolution.java'), template);
fs.writeFileSync(path.join(templates, 'item/FoodWeaponFamilyBuild.java'),
  'package net.farmingtales.ftboss.item; public final class FoodWeaponFamilyBuild { public static native boolean isBuildCandidate(net.minecraft.world.item.ItemStack stack); public static native double damageMultiplier(net.minecraft.world.item.ItemStack stack); }\n');
fs.writeFileSync(path.join(templates, 'item/StatueBlessingCombat.java'),
  'package net.farmingtales.ftboss.item; import java.util.UUID; import java.nio.charset.StandardCharsets; import net.minecraft.world.entity.ai.attributes.*; import net.minecraftforge.event.ItemAttributeModifierEvent; public final class StatueBlessingCombat {\n'
  + block(fs.readFileSync(path.join(sourceRoot, 'item/StatueBlessingCombat.java'), 'utf8'), 'private static void add(ItemAttributeModifierEvent event') + '\n}\n');
fs.writeFileSync(path.join(templates, 'item/EquipmentAttributeSlots.java'),
  'package net.farmingtales.ftboss.item; public final class EquipmentAttributeSlots { public static native java.util.UUID modifierId(java.util.UUID original, net.minecraft.world.item.ItemStack stack); }\n');
const client = fs.readFileSync(path.join(sourceRoot, 'client/StatueHubScreen.java'), 'utf8');
fs.writeFileSync(path.join(templates, 'client/StatueHubScreen.java'),
  'package net.farmingtales.ftboss.client; import net.minecraft.world.item.ItemStack; import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;\n'
  + 'public class StatueHubScreen extends net.minecraft.client.gui.screens.Screen {\n'
  + 'private enum Section { ENCHANT }; private Section section; private StatueHubScreen(){super(net.minecraft.network.chat.Component.empty());}\n'
  + block(client, 'private ItemStack evolutionWeapon()') + '\n}\n');
const srgRoot = path.join(process.env.USERPROFILE, '.gradle/caches/modules-2/files-2.1/net.minecraftforge/srgutils/0.4.15');
const srg = fs.readdirSync(srgRoot).map(folder => path.join(srgRoot, folder, 'srgutils-0.4.15.jar')).find(fs.existsSync);
const toolClasspath = [tools, dependencies, srg].join(path.delimiter);
const shared = path.join(project, 'tools/runtime_patches/statue-exclusive-blessings');
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', toolClasspath, '-d', tools,
  path.join(__dirname, 'PatchArmorBlessings.java'), path.join(project, 'tools/runtime_patches/zicai-curio-silence-20261018/RemapSilence.java'),
  path.join(shared, 'MapBlessingCheckJar.java'), ...['ZipStreamCheck', 'VerifyBlessingWiring', 'ArmorBlessingRuntimeCheck', 'FoodWeaponRangedHookCheck'].map(name => path.join(project, 'tools/verification', name + '.java'))]);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none', '-sourcepath', path.join(task, 'empty'),
  '-cp', dependencies, '-d', compiled, path.join(templates, 'item/FoodWeaponEvolution.java'), path.join(templates, 'client/StatueHubScreen.java'),
  path.join(templates, 'item/FoodWeaponFamilyBuild.java'),
  path.join(templates, 'item/StatueBlessingCombat.java'),
  path.join(templates, 'item/EquipmentAttributeSlots.java'),
  ...['ArmorBlessingCatalog', 'ArmorFoodBlessings', 'StatueBlessingCatalog', 'ArrowPassiveDamage'].map(name => path.join(sourceRoot, 'item', name + '.java'))]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapSilence', path.join(project, 'build/createMcpToSrg/output.tsrg'), compiled, remapped, mapped, compiled]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'PatchArmorBlessings', baseline, remapped, payload]);
for (const name of fs.readdirSync(path.join(remapped, 'net/farmingtales/ftboss/item'))) {
  if (name.startsWith('Armor') || name.startsWith('StatueBlessingCatalog') || name === 'ArrowPassiveDamage.class') fs.copyFileSync(path.join(remapped, 'net/farmingtales/ftboss/item', name), path.join(payload, 'net/farmingtales/ftboss/item', name));
}
const checks = path.join(task, 'check-classes');
fs.mkdirSync(checks, { recursive: true });
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', [compiled, dependencies].join(path.delimiter), '-d', checks,
  path.join(project, 'tools/verification/ArmorBlessingCheck.java')]);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', [compiled, dependencies].join(path.delimiter), '-d', checks,
  path.join(project, 'tools/verification/BlessingEffectAudit.java')]);
const data = run(path.join(jdk, 'java.exe'), ['-cp', [checks, compiled, dependencies].join(path.delimiter), 'ArmorBlessingCheck', '--data'], project, true);
fs.writeFileSync(path.join(task, 'armor-catalog.tsv'), 'id\tnameZh\tnameEn\tdescriptionZh\tdescriptionEn\n' + data);
const catalog = data.trim().split(/\r?\n/).map(line => { const [id, nameZh, nameEn, descriptionZh, descriptionEn] = line.split('\t'); return { id, nameZh, nameEn, descriptionZh, descriptionEn }; });
if (catalog.length !== 100) throw new Error('Expected 100 armor language definitions');
const weaponIds = [...fs.readFileSync(path.join(sourceRoot, 'item/StatueBlessingCatalog.java'), 'utf8').matchAll(/\bd\("([^"]+)"/g)].map(match => match[1]);
if (weaponIds.length !== 100 || new Set([...weaponIds, ...catalog.map(entry => entry.id)]).size !== 200) throw new Error('Expected 200 distinct weapon/armor definitions');
const originals = path.join(task, 'baseline-resources');
fs.mkdirSync(originals, { recursive: true });
run(path.join(jdk, 'jar.exe'), ['--extract', '--file', baseline, 'assets/ftboss/lang/zh_cn.json', 'assets/ftboss/lang/en_us.json'], originals);
for (const language of ['zh_cn', 'en_us']) {
  const relative = `assets/ftboss/lang/${language}.json`;
  const original = JSON.parse(fs.readFileSync(path.join(originals, relative), 'utf8'));
  const sourceFile = path.join(project, 'src/main/resources', relative);
  const translations = JSON.parse(fs.readFileSync(sourceFile, 'utf8'));
  const additions = {};
  const reach = fs.readFileSync(path.join(sourceRoot, 'item/StatueBlessingCatalog.java'), 'utf8')
    .match(/d\("rat_whisker_luck", "rat", "([^"]+)", "([^"]+)",\s*"([^"]+)",\s*"([^"]+)"/);
  if (!reach) throw new Error('Missing Whisker Reach definition');
  additions['food_blessing.ftboss.rat_whisker_luck'] = reach[language === 'zh_cn' ? 1 : 2];
  additions['tooltip.ftboss.food_blessing.rat_whisker_luck'] = reach[language === 'zh_cn' ? 3 : 4];
  for (const entry of catalog) {
    additions['food_blessing.ftboss.' + entry.id] = language === 'zh_cn' ? entry.nameZh : entry.nameEn;
    additions['tooltip.ftboss.food_blessing.' + entry.id] = (language === 'zh_cn' ? entry.descriptionZh : entry.descriptionEn).replaceAll('%', '%%');
  }
  const missing = Object.entries(additions).filter(([key, value]) => translations[key] !== value);
  for (let offset = 0; offset < missing.length; offset += 20) {
    const chunk = missing.slice(offset, offset + 20);
    const existing = chunk.filter(([key]) => key in translations);
    const fresh = chunk.filter(([key]) => !(key in translations));
    const lines = fs.readFileSync(sourceFile, 'utf8').split('\n');
    let patch = `*** Begin Patch\n*** Update File: ${sourceFile.replaceAll('\\', '/')}\n`;
    for (const [key, value] of existing) {
      const old = lines.find(line => line.trimStart().startsWith(JSON.stringify(key) + ':'));
      patch += '@@\n-' + old + '\n+  ' + JSON.stringify(key) + ': ' + JSON.stringify(value) + (old.trimEnd().endsWith(',') ? ',' : '') + '\n';
    }
    if (fresh.length) {
    const anchor = Object.keys(translations).at(-1);
    const oldLine = '  ' + JSON.stringify(anchor) + ': ' + JSON.stringify(translations[anchor]);
    patch += `@@\n-${oldLine}\n+${oldLine},\n`
      + fresh.map(([key, value], index) => '+  ' + JSON.stringify(key) + ': ' + JSON.stringify(value) + (index < fresh.length - 1 ? ',' : '')).join('\n') + '\n';
    }
    patch += '*** End Patch\n';
    run(path.join(process.env.LOCALAPPDATA, 'OpenAI/Codex/bin/37f54d68e5533a51/codex.exe'), ['--codex-run-as-apply-patch', patch]);
    Object.assign(translations, Object.fromEntries(chunk));
  }
  for (const key of ['screen.ftboss.statue_hub.enchant.no_weapon', 'screen.ftboss.statue_hub.enchant.rule', 'screen.ftboss.statue_hub.enchant.complete',
    'message.ftboss.armor_blessing.applied', 'tooltip.ftboss.armor_blessing.header']) additions[key] = translations[key];
  const target = path.join(payload, relative);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  const merged = { ...original, ...additions };
  for (const id of [...weaponIds, ...catalog.map(entry => entry.id)]) {
    for (const prefix of ['food_blessing.ftboss.', 'tooltip.ftboss.food_blessing.']) {
      const value = merged[prefix + id];
      if (typeof value !== 'string' || !value.trim() || value.includes('\uFFFD')) throw new Error('Missing/corrupt translation: ' + language + '/' + prefix + id);
    }
  }
  fs.writeFileSync(target, JSON.stringify(merged, null, 2) + '\n');
  sourceHashes[sourceFile] = hash(sourceFile);
}
function files(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => entry.isDirectory() ? files(path.join(directory, entry.name)) : [path.join(directory, entry.name)]);
}
const entries = files(payload).map(file => path.relative(payload, file).replaceAll('\\', '/'));
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, ...entries.flatMap(entry => ['-C', payload, entry])]);
run(path.join(jdk, 'java.exe'), ['-cp', tools, 'ZipStreamCheck', candidate]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'VerifyBlessingWiring', candidate]);
const hookPayload = path.join(task, 'hook-targets');
fs.mkdirSync(hookPayload, { recursive: true });
run(path.join(jdk, 'jar.exe'), ['--extract', '--file', candidate,
  ...['FoodWeaponBowMixin', 'FoodWeaponCrossbowMixin', 'FoodWeaponStaffCastMixin'].map(name => 'net/farmingtales/ftboss/mixin/' + name + '.class')], hookPayload);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'FoodWeaponRangedHookCheck', mapped,
  path.join(project, 'build/fg_cache/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar'),
  path.join(project, 'libs/irons_spellbooks-1.20.1-3.16.3.jar'), hookPayload]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [candidate, tools, runtime].join(path.delimiter), 'ArmorBlessingRuntimeCheck', baseline, candidate]);
const named = path.join(task, 'candidate-mapped-check.jar');
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'MapBlessingCheckJar', path.join(project, 'build/createMcpToSrg/output.tsrg'), candidate, mapped, named]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [checks, named, compiled, dependencies].join(path.delimiter), 'BlessingEffectAudit', candidate, mapped, path.join(task, 'effect-audit.json')]);
const bowChecks = path.join(task, 'bow-checks');
fs.mkdirSync(bowChecks, { recursive: true });
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', dependencies, '-d', bowChecks,
  ...['RangedHybridCombatCheck', 'ExtractRangedHybridCheck', 'BowBlessingTriggerCheck'].map(name => path.join(project, 'tools/verification', name + '.java')),
  path.join(sourceRoot, 'util/MeleeEnhancementRules.java')]);
run(path.join(jdk, 'java.exe'), ['-cp', [bowChecks, dependencies].join(path.delimiter), 'ExtractRangedHybridCheck', named, bowChecks, '--bow-only']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [bowChecks, dependencies].join(path.delimiter), 'BowBlessingTriggerCheck']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [checks, named, compiled, dependencies].join(path.delimiter), 'ArmorBlessingCheck', named, path.join(task, 'armor-behavior.json')]);
const oldChecks = path.join(task, 'weapon-checks');
fs.mkdirSync(oldChecks, { recursive: true });
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', dependencies, '-d', oldChecks,
  ...['StatueBlessingCombatCheck', 'BlessingCombatCheck', 'ExtractBlessingCheck', 'StatueBlessingCatalogCheck', 'OriginalBlessingPassiveCheck'].map(name => path.join(project, 'tools/verification', name + '.java')),
  path.join(sourceRoot, 'util/MeleeEnhancementRules.java'),
  path.join(sourceRoot, 'item/StatueBlessingCatalog.java')]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [oldChecks, dependencies].join(path.delimiter), 'StatueBlessingCombatCheck', named]);
run(path.join(jdk, 'java.exe'), ['-cp', [oldChecks, dependencies].join(path.delimiter), 'ExtractBlessingCheck', named, oldChecks]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', oldChecks, 'BlessingCombatCheck', '--hit-effects-only']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', oldChecks, 'OriginalBlessingPassiveCheck']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', oldChecks, 'StatueBlessingCatalogCheck', '--verify-only']);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
for (const [file, digest] of Object.entries(sourceHashes)) if (hash(file) !== digest) throw new Error('Source changed during build: ' + file);
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: hash(baseline), candidate,
  candidateSha256: hash(candidate), sources: sourceHashes, entries, armorBlessings: 100, weaponBlessings: 100, gameplayTested: false, docsTouched: false }, null, 2) + '\n');
fs.writeFileSync(path.join(project, '_work/armor-blessings-latest.json'), JSON.stringify({ task, candidateSha256: hash(candidate) }, null, 2) + '\n');
console.log('PASS candidate ready: ' + candidate);
