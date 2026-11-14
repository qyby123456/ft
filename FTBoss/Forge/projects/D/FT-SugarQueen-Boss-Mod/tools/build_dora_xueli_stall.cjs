const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2]);
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must stay inside _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const instance = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2');
const live = path.join(instance, 'mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const forge = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge');
const mapped = path.join(forge, '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const mappings = path.join(project, 'build/createMcpToSrg/output.tsrg');
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
const version = JSON.parse(fs.readFileSync(path.join(instance, 'FarmingTales_ForgeⅡ v1.5.2.json'), 'utf8'));
const libraries = version.libraries.filter(library => !library.name.includes('natives')).map(library => {
  const [group, artifact, release, classifier] = library.name.split(':');
  return path.join(minecraft, 'libraries', library.downloads?.artifact?.path
    || `${group.replaceAll('.', '/')}/${artifact}/${release}/${artifact}-${release}${classifier ? '-' + classifier : ''}.jar`);
}).filter(file => fs.existsSync(file));
libraries.push(...['fmlcore', 'javafmllanguage', 'lowcodelanguage', 'mclanguage'].map(name =>
  path.join(minecraft, `libraries/net/minecraftforge/${name}/1.20.1-47.4.10/${name}-1.20.1-47.4.10.jar`)));
const mods = fs.readdirSync(path.join(instance, 'mods')).filter(name => name.endsWith('.jar') && name !== 'qys-bosses.jar')
  .map(name => path.join(instance, 'mods', name));
for (const name of ['classes', 'tool-classes', 'payload', 'empty-sourcepath', 'donor']) fs.mkdirSync(path.join(task, name), { recursive: true });
const classes = path.join(task, 'classes');
const tools = path.join(task, 'tool-classes');
const payload = path.join(task, 'payload');
const baseline = path.join(task, 'baseline.jar');
const candidate = path.join(task, 'qys-bosses.jar');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const baselineHash = hash(live);
if (!fs.existsSync(baseline)) fs.copyFileSync(live, baseline);
if (hash(baseline) !== baselineHash) throw new Error('Installed JAR changed; use a fresh baseline directory');
fs.writeFileSync(path.join(task, 'build.log'), '');
const report = path.join(task, 'build-report.json');
if (fs.existsSync(report)) fs.unlinkSync(report);
function run(executable, args) {
  const result = spawnSync(executable, args, { cwd: project, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, windowsHide: true });
  fs.appendFileSync(path.join(task, 'build.log'), `${executable} ${args.join(' ')}\n${result.stdout || ''}${result.stderr || ''}\n`);
  process.stdout.write(result.stdout || ''); process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error(`Command failed: ${result.status}`);
}
function classpathJar(entries, name) {
  let line = 'Class-Path: ' + [...new Set(entries)].map(file => {
    if (!fs.existsSync(file)) throw new Error('Missing dependency: ' + file);
    return pathToFileURL(file).href.replaceAll('[', '%5B').replaceAll(']', '%5D') + (fs.statSync(file).isDirectory() ? '/' : '');
  }).join(' ');
  let manifest = 'Manifest-Version: 1.0\r\n';
  while (line.length > 70) { manifest += line.slice(0, 70) + '\r\n'; line = ' ' + line.slice(70); }
  const file = path.join(task, name + '.mf');
  fs.writeFileSync(file, manifest + line + '\r\n\r\n', 'ascii');
  const output = path.join(task, name + '.jar');
  run(path.join(jdk, 'jar.exe'), ['--create', '--file', output, '--manifest', file]);
  return output;
}
function method(source, name) {
  const position = source.indexOf(' ' + name + '(');
  if (position < 0) throw new Error('Missing source method: ' + name);
  const start = source.lastIndexOf('\n', position) + 1;
  const opening = source.indexOf('{', position);
  let ending = opening + 1;
  let depth = 1;
  while (depth && ending < source.length) { depth += (source[ending] === '{') - (source[ending] === '}'); ending++; }
  if (depth) throw new Error('Unclosed source method: ' + name);
  return source.slice(start, ending);
}
const boss = 'net/farmingtales/ftboss/entity/EasterBossEntity';
const sourceFile = path.join(project, 'src/main/java', boss + '.java');
const clock = path.join(project, 'src/main/java/net/farmingtales/ftboss/util/ManorCombatClock.java');
const toolFiles = ['runtime_patches/DoraXueliStallPatch', 'runtime_patches/ZicaiVoidPatch',
  'runtime_patches/SharedCombatCadencePatch', 'runtime_patches/SharedCombatFrameRepair',
  'runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames',
  'verification/DoraXueliStallCheck', 'verification/ManorStallLifecycleCheck', 'verification/ZipStreamCheck'].map(name => path.join(project, 'tools', name + '.java'));
const tracked = [sourceFile, clock, ...toolFiles, __filename];
const hashes = Object.fromEntries(tracked.map(file => [file, hash(file)]));
const source = fs.readFileSync(sourceFile, 'utf8');
const donor = `package net.farmingtales.ftboss.entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Monster;
public class EasterBossEntity extends Monster {
    private static final int DORA_VARIANT = 2;
    private static EntityDataAccessor<CompoundTag> DATA_COMBAT_ANIMATION;
    private int actionTicks, castTicks, xueliSlamWarn, xueliSlamLeft, xueliSlamGap;
    private EasterBossEntity() { super(null, null); }
    public int getVariant() { return 0; }
    private boolean skillCommitted() { return false; }
    private boolean movementLocked() { return false; }
${['cancelXueliSlam', 'repairXueliSlam', 'expireCombatPoseTimers', 'canScheduleSharedSkill'].map(name => method(source, name)).join('\n')}
}
`;
const donorFile = path.join(task, 'donor/EasterBossEntity.java');
fs.writeFileSync(donorFile, donor);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', asm.join(path.delimiter), '-d', tools, ...toolFiles]);
const toolClasspath = [tools, ...asm].join(path.delimiter);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, baseline, path.join(task, 'baseline-mapped.jar')]);
const dependencies = classpathJar([mapped, path.join(task, 'baseline-mapped.jar'), ...libraries, ...mods], 'dependencies');
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', classes, donorFile, clock]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [toolClasspath, dependencies].join(path.delimiter),
  'DoraXueliStallPatch', baseline, classes, mappings, payload]);
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
const runtimeForge = path.join(forge, '1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar');
const runtime = classpathJar([candidate, tools, runtimeForge, ...asm, ...libraries, ...mods], 'runtime');
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtime, 'SharedCombatFrameRepair', payload,
  path.join(task, 'frame-repairs.txt'), boss, 'm_8119_,launchXueliSlam,skillXueliSlam,xueliSlamTick']);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [toolClasspath, payload].join(path.delimiter),
  'DoraXueliStallCheck', baseline, candidate, path.join(task, 'regression-results.json')]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtime, 'DoraXueliStallCheck', 'link']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', toolClasspath, 'ManorStallLifecycleCheck', candidate,
  path.join(task, 'manor-lifecycle-audit.json')]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', runtime, 'ManorStallLifecycleCheck', 'link']);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed during build');
for (const [file, expected] of Object.entries(hashes)) if (hash(file) !== expected) throw new Error('Source changed: ' + file);
for (const file of tracked) {
  const snapshot = path.join(task, 'sources', path.relative(project, file));
  fs.mkdirSync(path.dirname(snapshot), { recursive: true }); fs.copyFileSync(file, snapshot);
}
fs.writeFileSync(report, JSON.stringify({ passed: true, baselineSha256: baselineHash, candidate,
  candidateSha256: hash(candidate), sources: hashes, regression: JSON.parse(fs.readFileSync(path.join(task, 'regression-results.json'), 'utf8')),
  manorLifecycleAudit: JSON.parse(fs.readFileSync(path.join(task, 'manor-lifecycle-audit.json'), 'utf8')),
  strictClassLinkPassed: true, gameplayTested: false, deployed: false, docsTouched: false, docsImpact: 'none', neoForgeTouched: false }, null, 2) + '\n');
console.log('PASS verified Dora/Xueli stall candidate; no deployment performed: ' + candidate);
