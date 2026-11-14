const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const child = require('node:child_process');
const normalize = value => value.split(path.sep).join('/');
const root = normalize(path.resolve(__dirname, '..'));
const task = root + '/_work/zhuni-blackwall-20261020';
const minecraft = normalize(process.env.APPDATA) + '/.minecraft';
const instance = minecraft + '/versions/FarmingTales_ForgeⅡ v1.5.2';
const java = minecraft + '/runtime/java-runtime-gamma-snapshot/bin';
const home = normalize(process.env.USERPROFILE);
const mappings = root + '/build/createMcpToSrg/output.tsrg';
const srgMap = home + '/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg';
const fart = home + '/.gradle/caches/forge_gradle/maven_downloader/net/minecraftforge/ForgeAutoRenamingTool/1.0.2/ForgeAutoRenamingTool-1.0.2-all.jar';
const dependencies = fs.readFileSync(root + '/_work/four-weapon-ui-20261019/classpath.txt', 'utf8').trim().split(';').slice(1)
  .filter(file => !/natives-windows-(arm64|x86)/.test(file));
const asm = ['asm', 'asm-tree', 'asm-analysis', 'asm-commons', 'asm-util'].map(name =>
  minecraft + '/libraries/org/ow2/asm/' + name + '/9.7.1/' + name + '-9.7.1.jar');
const sha = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
for (const directory of ['tools', 'classes', 'empty-sourcepath', 'payload', 'evidence', 'preview', 'tmp']) fs.mkdirSync(task + '/' + directory, { recursive: true });
const sources = ['util/ZhuniBlackwallRules', 'boss/ai/ZhuniBlackwallDisruption', 'network/ZhuniBlackwallNetwork',
  'client/render/ZhuniBlackwallVisual', 'boss/ai/SparrowDisruption'].map(name => root + '/src/main/java/net/farmingtales/ftboss/' + name + '.java');
const tools = ['tools/runtime_patches/ZhuniBlackwallPatch.java', 'tools/runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames.java',
  'tools/verification/WeaponUiJarClosureCheck.java', 'tools/verification/ZipStreamCheck.java', 'tools/verification/ZhuniBlackwallRulesCheck.java',
  'tools/verification/ZhuniBlackwallShaderCheck.java', 'tools/verification/ZhuniBlackwallJarCheck.java'].map(file => root + '/' + file);
const resources = ['shaders/core/zhuni_blackwall.json', 'shaders/core/zhuni_blackwall.vsh', 'shaders/core/zhuni_blackwall.fsh',
  'textures/gui/blackwall_cells.png', 'textures/gui/blackwall_cells.png.mcmeta'].map(name => 'assets/ftboss/' + name);
const tracked = [...sources, ...tools, __filename, root + '/src/main/java/net/farmingtales/ftboss/turret/ZhuniEntity.java',
  ...resources.filter(name => !name.endsWith('.png')).map(name => root + '/src/main/resources/' + name)];
const sourceHashes = Object.fromEntries(tracked.map(file => [file, sha(file)]));
function run(executable, argumentsList, label, cwd = root) {
  const result = child.spawnSync(executable, argumentsList, { cwd, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, windowsHide: true });
  fs.writeFileSync(task + '/evidence/' + label + '.log', (result.stdout || '') + (result.stderr || ''));
  console.log(label + ': ' + result.status);
  if (result.error || result.status !== 0) throw result.error || new Error(result.stdout + result.stderr);
}
function compile(name, files, destination, classpath) {
  const argumentsList = ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-sourcepath', task + '/empty-sourcepath', '-cp', classpath.join(';'), '-d', destination, ...files];
  const argfile = task + '/' + name + '.args';
  fs.writeFileSync(argfile, argumentsList.map(value => JSON.stringify(normalize(value))).join('\n'));
  run(java + '/javac.exe', ['-J-Dfile.encoding=UTF-8', '@' + argfile], name);
}
const baseline = task + '/baseline.jar';
fs.copyFileSync(instance + '/mods/qys-bosses.jar', baseline);
compile('compile-tools', [...tools, sources[0]], task + '/tools', [...asm, ...dependencies]);
run(java + '/java.exe', ['-cp', [task + '/tools', ...asm].join(';'), 'RemapRuntimeNames', mappings, baseline, task + '/baseline-mapped.jar'], 'map-baseline');
compile('compile-source', sources, task + '/classes', [task + '/baseline-mapped.jar', ...dependencies]);
run(java + '/java.exe', ['-Xverify:all', '-cp', task + '/tools', 'ZhuniBlackwallRulesCheck'], 'rules');
run(java + '/java.exe', ['-Djava.io.tmpdir=' + task + '/tmp', '-Dorg.lwjgl.system.SharedLibraryExtractPath=' + task + '/natives',
  '-cp', [task + '/tools', ...dependencies].join(';'), 'ZhuniBlackwallShaderCheck', root, task,
  instance + '/screenshots/2026-10-09_22.13.42.png'], 'shader');
run(java + '/jar.exe', ['cf', task + '/payload-named.jar', '-C', task + '/classes', '.'], 'pack-named');
run(java + '/java.exe', ['-Xmx2G', '-jar', fart, '--input', task + '/payload-named.jar', '--output', task + '/payload-srg.jar',
  '--map', srgMap, '--reverse', '--lib', dependencies[0], '--lib', task + '/baseline-mapped.jar', '--threads', '2'], 'remap');
run(java + '/jar.exe', ['xf', task + '/payload-srg.jar', 'net'], 'extract', task + '/payload');
run(java + '/java.exe', ['-Xverify:all', '-cp', [task + '/tools', ...asm].join(';'), 'ZhuniBlackwallPatch', baseline, task + '/payload'], 'patch-runtime');
for (const resource of resources) {
  const destination = task + '/payload/' + resource;
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  fs.copyFileSync(root + '/src/main/resources/' + resource, destination);
}
function walk(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => entry.isDirectory() ? walk(directory + '/' + entry.name) : [directory + '/' + entry.name]);
}
const entries = walk(task + '/payload').map(file => file.slice((task + '/payload/').length)).sort();
const candidate = task + '/qys-bosses.jar';
fs.copyFileSync(baseline, candidate);
run(java + '/jar.exe', ['--update', '--file', candidate, ...entries.flatMap(entry => ['-C', task + '/payload', entry])], 'update-jar');
run(java + '/java.exe', ['-Xverify:all', '-cp', [task + '/tools', ...asm].join(';'), 'ZhuniBlackwallJarCheck', baseline, candidate,
  task + '/verification.json'], 'exact-delta');
run(java + '/java.exe', ['-Xverify:all', '-cp', [task + '/tools', ...asm].join(';'), 'WeaponUiJarClosureCheck', candidate,
  ...entries.filter(entry => entry.endsWith('.class')).map(entry => entry.slice(0, -6))], 'closure');
run(java + '/java.exe', ['-cp', task + '/tools', 'ZipStreamCheck', candidate], 'zip-stream');
run(process.execPath, [root + '/tools/verification/check_jar_zip_rules.cjs', candidate], 'zip-rules');
for (const [file, hash] of Object.entries(sourceHashes)) if (sha(file) !== hash) throw Error('Source changed during build: ' + file);
const report = { passed: true, baseline, baselineSha256: sha(baseline), candidate, candidateSha256: sha(candidate), entries, sourceHashes, gameTested: false };
fs.writeFileSync(task + '/build-report.json', JSON.stringify(report, null, 2) + '\n');
console.log('PASS: ' + candidate);
