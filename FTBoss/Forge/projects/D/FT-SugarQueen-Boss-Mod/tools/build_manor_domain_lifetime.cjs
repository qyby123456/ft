const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');

const project = path.resolve(__dirname, '..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/manor-domain-lifetime-20261027'));
if (!task.startsWith(path.join(project, '_work') + path.sep)) throw new Error('Task must remain in project _work');
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const instance = path.join(minecraft, 'versions/FarmingTales_ForgeⅡ v1.5.2');
const live = path.join(instance, 'mods/qys-bosses.jar');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const mapped = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge',
  '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const mappings = path.join(project, 'build/createMcpToSrg/output.tsrg');
const version = JSON.parse(fs.readFileSync(path.join(instance, 'FarmingTales_ForgeⅡ v1.5.2.json'), 'utf8'));
const libraries = version.libraries.filter(library => !library.name.includes('natives')).map(library => {
  const [group, artifact, release, classifier] = library.name.split(':');
  const relative = library.downloads?.artifact?.path
    || `${group.replaceAll('.', '/')}/${artifact}/${release}/${artifact}-${release}${classifier ? '-' + classifier : ''}.jar`;
  return path.join(minecraft, 'libraries', relative);
}).filter(file => fs.existsSync(file));
libraries.push(...['fmlcore', 'javafmllanguage', 'lowcodelanguage', 'mclanguage'].map(name =>
  path.join(minecraft, `libraries/net/minecraftforge/${name}/1.20.1-47.4.10/${name}-1.20.1-47.4.10.jar`)));
const mods = fs.readdirSync(path.join(instance, 'mods')).filter(name => name.endsWith('.jar') && name !== 'qys-bosses.jar')
  .map(name => path.join(instance, 'mods', name));
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons'].map(name =>
  path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
for (const name of ['tools', 'classes', 'payload', 'empty-sourcepath', 'fixtures']) fs.mkdirSync(path.join(task, name), { recursive: true });
const toolsDirectory = path.join(task, 'tools');
const classes = path.join(task, 'classes');
const payload = path.join(task, 'payload');
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
function classpathJar(name, files) {
  let line = 'Class-Path: ' + files.map(file => pathToFileURL(file).href.replaceAll('[', '%5B').replaceAll(']', '%5D')).join(' ');
  let manifest = 'Manifest-Version: 1.0\r\n';
  while (line.length > 70) { manifest += line.slice(0, 70) + '\r\n'; line = ' ' + line.slice(70); }
  const file = path.join(task, name + '.mf');
  fs.writeFileSync(file, manifest + line + '\r\n\r\n', 'ascii');
  const output = path.join(task, name + '.jar');
  run(path.join(jdk, 'jar.exe'), ['--create', '--file', output, '--manifest', file]);
  return output;
}
const baseline = path.join(task, 'baseline.jar');
const baselineHash = hash(live);
if (!fs.existsSync(baseline)) fs.copyFileSync(live, baseline);
if (hash(baseline) !== baselineHash) throw new Error('Baseline changed; choose a fresh task directory');
const names = ['util/ManorDomainLifetimeRules', 'boss/ai/ManorDomainLifetime'];
const sources = names.map(name => path.join(project, 'src/main/java/net/farmingtales/ftboss', name + '.java'));
const tools = ['runtime_patches/ManorDomainLifetimePatch', 'runtime_patches/ZicaiVoidPatch',
  'runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames', 'verification/ManorDomainLifetimeCheck',
  'verification/FoodWeaponBuildLinkCheck', 'verification/WeaponUiJarClosureCheck', 'verification/ZipStreamCheck']
  .map(name => path.join(project, 'tools', name + '.java'));
const fixture = path.join(project, 'tools/verification/ManorDomainLifetimeFixture.java');
const zipRules = path.join(project, 'tools/verification/check_jar_zip_rules.cjs');
const deployer = path.join(project, 'tools/deploy_manor_domain_lifetime.ps1');
const hashes = Object.fromEntries([...sources, ...tools, fixture, zipRules, deployer, __filename].map(file => [file, hash(file)]));
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', asm.join(path.delimiter), '-d', toolsDirectory, ...tools]);
const toolClasspath = [toolsDirectory, ...asm].join(path.delimiter);
const named = path.join(task, 'baseline-mapped.jar');
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, baseline, named]);
const dependencies = classpathJar('compile-dependencies', [mapped, named, ...libraries, ...mods]);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none',
  '-sourcepath', path.join(task, 'empty-sourcepath'), '-cp', dependencies, '-d', classes, ...sources]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [toolClasspath, classes, dependencies].join(path.delimiter),
  'ManorDomainLifetimePatch', baseline, classes, mappings, payload]);
const candidate = path.join(task, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, 'net']);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', toolClasspath, 'ManorDomainLifetimeCheck', baseline, candidate, path.join(task, 'fixtures')]);
const stubs = {
  'net/minecraft/world/level/Level': 'public class Level {}',
  'net/minecraft/server/level/ServerLevel': 'public class ServerLevel extends net.minecraft.world.level.Level { public long time; public long getGameTime() { return time; } }',
  'net/minecraft/client/multiplayer/ClientLevel': 'public class ClientLevel { public long time; public long m_46467_() { return time; } }',
  'net/farmingtales/ftboss/skill/ManorDomainPacket': 'public class ManorDomainPacket {}',
  'net/minecraft/nbt/Tag': 'public class Tag {}',
  'net/minecraft/nbt/CompoundTag': `public class CompoundTag extends Tag {
    private final java.util.Map<String, Object> values = new java.util.HashMap<>();
    private Object get(String key) { return values.get(key); }
    public boolean isEmpty() { return values.isEmpty(); }
    public Tag put(String key, Tag value) { return (Tag) values.put(key, value); }
    public CompoundTag getCompound(String key) { return get(key) instanceof CompoundTag tag ? tag : new CompoundTag(); }
    public boolean hasUUID(String key) { return get(key) instanceof java.util.UUID; }
    public java.util.UUID getUUID(String key) { return (java.util.UUID) get(key); }
    public void putUUID(String key, java.util.UUID value) { values.put(key, value); }
    public int getInt(String key) { return get(key) instanceof Integer value ? value : 0; }
    public void putInt(String key, int value) { values.put(key, value); }
    public long getLong(String key) { return get(key) instanceof Long value ? value : 0L; }
    public void putLong(String key, long value) { values.put(key, value); }
    public boolean getBoolean(String key) { return Boolean.TRUE.equals(get(key)); }
    public void putBoolean(String key, boolean value) { values.put(key, value); }
    public CompoundTag copy() { CompoundTag result = new CompoundTag(); values.forEach((key, value) -> result.values.put(key, value instanceof CompoundTag tag ? tag.copy() : value)); return result; }
  }`,
  'net/farmingtales/ftboss/entity/EasterBossEntity': `public class EasterBossEntity {
    public net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
    public final net.minecraft.server.level.ServerLevel world;
    public float health = 30.0f; public int variant; public int writes;
    public EasterBossEntity(net.minecraft.server.level.ServerLevel level) { world = level; }
    public net.minecraft.world.level.Level level() { return world; }
    public net.minecraft.nbt.CompoundTag getPersistentData() { return data; }
    public int getVariant() { return variant; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return 100.0f; }
    public void setHealth(float value) { health = value; writes++; }
  }`
};
const stubFiles = Object.entries(stubs).map(([name, body]) => {
  const file = path.join(task, 'stub-sources', name + '.java');
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, `package ${name.slice(0, name.lastIndexOf('/')).replaceAll('/', '.')};\n${body}\n`);
  return file;
});
const fixtures = path.join(task, 'fixtures');
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', classes, '-d', fixtures, ...stubFiles, fixture]);
for (const name of names) {
  const relative = 'net/farmingtales/ftboss/' + name + '.class';
  fs.mkdirSync(path.dirname(path.join(fixtures, relative)), { recursive: true });
  fs.copyFileSync(path.join(classes, relative), path.join(fixtures, relative));
}
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', fixtures, 'ManorDomainLifetimeFixture']);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'WeaponUiJarClosureCheck', candidate, '--all',
  'net/farmingtales/ftboss/boss/ai/ManorDomainLifetime']);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'ZipStreamCheck', candidate]);
run(process.execPath, [zipRules, candidate]);
const candidateNamed = path.join(task, 'candidate-mapped.jar');
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'RemapRuntimeNames', mappings, candidate, candidateNamed]);
const linkDependencies = classpathJar('link-dependencies', [candidateNamed, mapped, ...libraries, ...mods]);
const production = path.join(task, 'production');
fs.mkdirSync(production, { recursive: true });
run(path.join(jdk, 'jar.exe'), ['--extract', '--file', candidateNamed,
  ...names.map(name => 'net/farmingtales/ftboss/' + name + '.class')], production);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', [toolClasspath, linkDependencies].join(path.delimiter),
  'FoodWeaponBuildLinkCheck', production, candidateNamed]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed while building; rebuild from current baseline');
for (const [file, expected] of Object.entries(hashes)) if (hash(file) !== expected) throw new Error('Source changed while building: ' + file);
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({ passed: true, baseline, baselineSha256: baselineHash,
  candidate, candidateSha256: hash(candidate), sources: hashes, toolClasspath, gameplayTested: false, docsTouched: false,
  docsProposal: path.join(project, '_work/docs-proposals/manor-domain-lifetime.md') }, null, 2) + '\n');
console.log('PASS verified domain-lifetime candidate: ' + candidate);
