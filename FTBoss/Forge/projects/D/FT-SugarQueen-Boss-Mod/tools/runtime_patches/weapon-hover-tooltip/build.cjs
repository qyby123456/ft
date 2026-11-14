const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const project = path.resolve(__dirname, '../../..');
const task = path.join(project, '_work/weapon-hover-lag-20261025');
const jdk = path.join(process.env.APPDATA, '.minecraft/runtime/java-runtime-gamma-snapshot/bin');
const previous = path.join(project, '_work/zicai-theme-zheergen-trail-20261025');
const dependencies = path.join(previous, 'compile-dependencies.jar');
const runtime = path.join(previous, 'runtime-dependencies.jar');
const classes = path.join(task, 'classes');
const templates = path.join(task, 'templates/net/farmingtales/ftboss');
const compiled = path.join(task, 'compiled');
const remapped = path.join(task, 'remapped');
const payload = path.join(task, 'payload');
const baseline = path.join(task, 'baseline.jar');
const candidate = path.join(task, 'qys-bosses.jar');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const sources = ['item/FoodWeaponEvolution.java', 'item/FoodWeaponSkillLoadout.java', 'client/FoodWeaponSkillWheel.java']
  .map(relative => path.join(project, 'src/main/java/net/farmingtales/ftboss', relative));
const tracked = [...sources, __filename, path.join(project, 'tools/runtime_patches/WeaponHoverTooltipPatch.java'),
  path.join(project, 'tools/verification/WeaponHoverTooltipCheck.java')];
const sourceHashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
for (const directory of [classes, compiled, remapped, payload, path.join(templates, 'item'), path.join(templates, 'client')])
  fs.mkdirSync(directory, { recursive: true });
function run(executable, args, cwd = project) {
  const result = spawnSync(executable, args, { cwd, encoding: 'utf8', windowsHide: true, maxBuffer: 16 * 1024 * 1024 });
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error(`Command failed: ${result.status}`);
}
function method(file, signature) {
  const source = fs.readFileSync(file, 'utf8');
  const start = source.indexOf(signature);
  if (start < 0) throw new Error('Missing method: ' + signature);
  let depth = 0;
  for (let index = source.indexOf('{', start); index < source.length; index++) {
    if (source[index] === '{') depth++;
    if (source[index] === '}' && --depth === 0) return source.slice(start, index + 1);
  }
  throw new Error('Unterminated method');
}
const header = 'import java.util.*; import net.minecraft.world.item.ItemStack;\n';
fs.writeFileSync(path.join(templates, 'item/FoodWeaponEvolution.java'), 'package net.farmingtales.ftboss.item;\n' + header
  + 'public final class FoodWeaponEvolution {\nprivate static final class Profile { private String id; }\n'
  + 'private static native Profile boundProfile(ItemStack stack); private static native Profile nativeProfile(ItemStack stack);\n'
  + 'public static native boolean isGourmetEvolutionWeapon(ItemStack stack); private static native List<?> gourmetSkills(ItemStack stack);\n'
  + 'public static native int evolutionLevel(ItemStack stack);\n' + method(sources[0], 'public static int selectableSkillCount(') + '\n}\n');
fs.writeFileSync(path.join(templates, 'item/FoodWeaponSkillLoadout.java'), 'package net.farmingtales.ftboss.item;\n' + header
  + 'public final class FoodWeaponSkillLoadout { public static native List<Integer> carried(ItemStack stack);\n'
  + method(sources[1], 'public static int carriedSkillCount(') + '\n}\n');
fs.writeFileSync(path.join(templates, 'client/FoodWeaponSkillWheel.java'), 'package net.farmingtales.ftboss.client;\n' + header
  + 'import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout; import net.minecraftforge.event.entity.player.ItemTooltipEvent;\n'
  + 'import net.minecraft.network.chat.Component; import net.minecraft.ChatFormatting;\n'
  + 'public final class FoodWeaponSkillWheel { public static native Component keyName();\n'
  + method(sources[2], 'public static void tooltip(') + '\n}\n');
const srgRoot = path.join(process.env.USERPROFILE, '.gradle/caches/modules-2/files-2.1/net.minecraftforge/srgutils/0.4.15');
const srg = fs.readdirSync(srgRoot).map(folder => path.join(srgRoot, folder, 'srgutils-0.4.15.jar')).find(fs.existsSync);
const toolClasspath = [classes, dependencies, srg].join(path.delimiter);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', toolClasspath, '-d', classes,
  path.join(project, 'tools/runtime_patches/WeaponHoverTooltipPatch.java'), path.join(project, 'tools/verification/WeaponHoverTooltipCheck.java'),
  path.join(project, 'tools/runtime_patches/zicai-curio-silence-20261018/RemapSilence.java'), path.join(project, 'tools/verification/ZipStreamCheck.java')]);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', compiled,
  ...['item/FoodWeaponEvolution.java', 'item/FoodWeaponSkillLoadout.java', 'client/FoodWeaponSkillWheel.java'].map(file => path.join(templates, file))]);
const mapped = path.join(project, 'build/fg_cache/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapSilence', path.join(project, 'build/createMcpToSrg/output.tsrg'),
  compiled, remapped, mapped, compiled]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'WeaponHoverTooltipPatch', baseline, remapped, payload]);
const entries = sources.map(file => path.relative(path.join(project, 'src/main/java'), file).replaceAll('\\', '/').replace(/\.java$/, '.class'));
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, ...entries.flatMap(entry => ['-C', payload, entry])]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [candidate, classes, runtime].join(path.delimiter), 'WeaponHoverTooltipCheck', baseline, candidate]);
run(path.join(jdk, 'java.exe'), ['-cp', classes, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
for (const file of tracked) if (hash(file) !== sourceHashes[file]) throw new Error('Source changed during build: ' + file);
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: hash(baseline),
  candidate, candidateSha256: hash(candidate), sources: sourceHashes, entries, gameplayTested: false }, null, 2) + '\n');
console.log('PASS candidate ready: ' + candidate);
