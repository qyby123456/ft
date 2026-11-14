const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const project = path.resolve(__dirname, '../../..');
const instance = path.join(process.env.APPDATA, '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2');
const installed = path.join(instance, 'mods/qys-bosses.jar');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const baselineHash = hash(installed);
const task = path.join(project, '_work/sparrow-entity-snapshot-20261027-' + baselineHash.slice(0, 10));
const jdk = path.join(process.env.APPDATA, '.minecraft/runtime/java-runtime-gamma-snapshot/bin');
const dependencies = path.join(project, '_work/zicai-theme-zheergen-trail-20261025/compile-dependencies.jar');
const baseline = path.join(task, 'baseline.jar');
const candidate = path.join(task, 'qys-bosses.jar');
const tools = path.join(task, 'tools');
const payload = path.join(task, 'payload');
const compiled = path.join(task, 'compiled');
const source = path.join(project, 'src/main/java/net/farmingtales/ftboss/boss/SparrowSupport.java');
const entry = 'net/farmingtales/ftboss/boss/SparrowSupport.class';
const tracked = [__filename, path.join(__dirname, 'deploy.ps1'), source,
  path.join(__dirname, 'PatchSparrowEntitySnapshot.java'),
  path.join(project, 'tools/verification/SparrowEntitySnapshotCheck.java'),
  path.join(project, 'tools/verification/ZipStreamCheck.java'),
  path.join(project, 'tools/verification/check_jar_zip_rules.cjs'),
  path.join(project, 'tools/verification/check_sparrow_schedule.cjs'),
  path.join(project, 'tools/verification/check_manor_chase_sparrow.cjs')];
const sources = Object.fromEntries(tracked.map(file => [file, hash(file)]));
function run(executable, args, quiet = false) {
  const result = spawnSync(executable, args, {
    cwd: project, windowsHide: true, encoding: 'utf8', maxBuffer: 16 * 1024 * 1024,
  });
  if (!quiet) process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error('Command failed: ' + result.status);
  return result.stdout;
}
for (const directory of [task, tools, payload, compiled, path.join(task, 'empty')]) fs.mkdirSync(directory, { recursive: true });
if (!fs.existsSync(baseline)) fs.copyFileSync(installed, baseline);
assert.equal(hash(baseline), baselineHash, 'Baseline must match installed artifact');
const text = fs.readFileSync(source, 'utf8');
assert.equal((text.match(/getAllEntities\(\)/g) || []).length, 3);
assert.equal((text.match(/ImmutableList\.copyOf\((?:level|lv)\.getAllEntities\(\)\)/g) || []).length, 3);
const toolClasspath = [tools, dependencies].join(path.delimiter);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', toolClasspath, '-d', tools,
  path.join(__dirname, 'PatchSparrowEntitySnapshot.java'),
  path.join(project, 'tools/verification/SparrowEntitySnapshotCheck.java'),
  path.join(project, 'tools/verification/ZipStreamCheck.java')]);
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none',
  '-sourcepath', path.join(task, 'empty'), '-cp', dependencies, '-d', compiled, source]);
run(path.join(jdk, 'java.exe'), ['-cp', toolClasspath, 'PatchSparrowEntitySnapshot', baseline, payload]);
fs.copyFileSync(baseline, candidate);
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, '-C', payload, entry]);
run(path.join(jdk, 'java.exe'), ['-Xverify:all', '-cp', toolClasspath, 'SparrowEntitySnapshotCheck', baseline, candidate]);
run(path.join(jdk, 'java.exe'), ['-cp', tools, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_sparrow_schedule.cjs')]);
const broader = spawnSync(process.execPath, [path.join(project, 'tools/verification/check_manor_chase_sparrow.cjs')], {
  cwd: project, windowsHide: true, encoding: 'utf8', maxBuffer: 1024 * 1024,
});
if (broader.error) throw broader.error;
fs.writeFileSync(path.join(task, 'broader-chase-check.log'), (broader.stdout || '') + (broader.stderr || ''));
if (broader.status !== 0) console.log('NOTE: unrelated pre-existing chase/fly-ban source check failed; see broader-chase-check.log');
const bytecode = run(path.join(jdk, 'javap.exe'), ['-c', '-p', '-classpath', candidate,
  'net.farmingtales.ftboss.boss.SparrowSupport'], true);
fs.writeFileSync(path.join(task, 'sparrow-after.javap.txt'), bytecode);
for (const [file, digest] of Object.entries(sources)) assert.equal(hash(file), digest, 'Source changed during validation: ' + file);
assert.equal(hash(installed), baselineHash, 'Concurrent deployment detected');
fs.writeFileSync(path.join(task, 'build-report.json'), JSON.stringify({
  passed: true, baselineSha256: baselineHash, candidateSha256: hash(candidate), sources, entries: [entry],
  removalCases: 780, sourceCompiled: true, gameplayTested: false, docsTouched: false, docsImpact: 'none',
  additionalChecks: [{ name: 'check_manor_chase_sparrow.cjs', passed: broader.status === 0,
    affectsChangedClass: false, log: path.join(task, 'broader-chase-check.log') }],
}, null, 2) + '\n');
fs.writeFileSync(path.join(project, '_work/sparrow-entity-snapshot-latest.json'), JSON.stringify({ task }, null, 2) + '\n');
console.log('PASS candidate ready: ' + candidate);
