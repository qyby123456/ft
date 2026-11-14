'use strict';

const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');
const project = path.resolve(__dirname, '..');
const root = path.join(project, '_work/manor-ritual-20261025');
const transition = path.join(project, '_work/manor-transition-20261025');
const finale = path.join(project, '_work/manor-finale-20261025');
const jdk = path.join(process.env.APPDATA, '.minecraft/runtime/java-runtime-gamma-snapshot/bin');
const java = path.join(jdk, 'java.exe');
const jar = path.join(jdk, 'jar.exe');
const read = file => JSON.parse(fs.readFileSync(file, 'utf8'));
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const intro = read(path.join(root, 'LATEST_CANDIDATE.json'));
const transitionReport = read(path.join(transition, 'MANIFEST.json'));
const finaleReport = read(path.join(finale, 'manifest.json'));
for (const directory of [transition, finale]) {
  if (!fs.existsSync(path.join(directory, 'HANDOFF.md'))) throw new Error('Independent task not handed off: ' + directory);
}
if (!intro.passed) throw new Error('Intro candidate has not passed verification');
const work = path.join(root, 'merged-' + new Date().toISOString().replace(/[:.]/g, '-'));
fs.mkdirSync(work, { recursive: true });
const scriptHash = hash(__filename);
const log = path.join(work, 'verification.log');
function run(executable, args, cwd = project) {
  const result = spawnSync(executable, args, { cwd, encoding: 'utf8', maxBuffer: 32 * 1024 * 1024, windowsHide: true });
  fs.appendFileSync(log, `${executable} ${args.join(' ')}\n${result.stdout || ''}${result.stderr || ''}\n`);
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  if (result.error || result.status !== 0) throw result.error || new Error('Command failed: ' + result.status);
}
const live = intro.baseline;
const baselineHash = hash(live);
const baseline = path.join(work, 'baseline.jar');
fs.copyFileSync(live, baseline);
const originalBaseline = path.join(path.dirname(intro.candidate), 'baseline.jar');
if (hash(originalBaseline) !== intro.baselineSha256) throw new Error('Original task baseline snapshot changed');
for (const expected of [intro.baselineSha256, transitionReport.baseline.sha256, finaleReport.baselineSha256]) {
  if (intro.baselineSha256 !== expected.toLowerCase()) throw new Error('Independent task baselines differ; reconcile before merging');
}
if (hash(baseline) !== baselineHash) throw new Error('Baseline snapshot changed while copying');
const metadataFiles = [path.join(root, 'LATEST_CANDIDATE.json'), path.join(transition, 'MANIFEST.json'), path.join(finale, 'manifest.json')];
const metadataHashes = Object.fromEntries(metadataFiles.map(file => [file, hash(file)]));
for (const [file, expected] of Object.entries(intro.sources)) {
  if (hash(file) !== expected.toLowerCase()) throw new Error('Intro source drift: ' + file);
}
for (const source of transitionReport.sourceFiles) {
  if (hash(path.join(project, source.path)) !== source.sha256.toLowerCase()) throw new Error('Transition source drift: ' + source.path);
}
const payload = path.join(work, 'payload');
const entries = new Map();
function copyEntry(directory, entry, expected, owner) {
  if (entries.has(entry)) throw new Error('Overlapping task ownership: ' + entry);
  const source = path.resolve(directory, entry);
  if (!source.startsWith(path.resolve(directory) + path.sep)) throw new Error('Escaping payload path: ' + entry);
  const digest = hash(source);
  if (expected && digest !== expected.toLowerCase()) throw new Error('Payload digest mismatch: ' + entry);
  const destination = path.join(payload, entry);
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  fs.copyFileSync(source, destination);
  entries.set(entry, { owner, sha256: digest });
}
const introSnapshot = path.dirname(intro.candidate);
if (hash(intro.candidate) !== intro.candidateSha256) throw new Error('Intro candidate changed');
const extractedIntro = path.join(work, 'verified-intro-entries');
fs.mkdirSync(extractedIntro, { recursive: true });
run(jar, ['--extract', '--file', intro.candidate, ...intro.entries], extractedIntro);
for (const entry of intro.entries) {
  const expected = hash(path.join(extractedIntro, entry));
  copyEntry(intro.payload, entry, expected, 'intro');
}
for (const file of transitionReport.overlay) copyEntry(path.join(transition, 'overlay'), file.entry, file.sha256, 'transition');
const shared = finaleReport.entries.filter(file => file.operation === 'replace').map(file => file.entry).sort();
for (const file of finaleReport.entries.filter(file => file.operation === 'add')) {
  copyEntry(path.join(finale, finaleReport.payloadDirectory), file.entry, file.sha256, 'finale');
}
const checks = path.join(introSnapshot, 'checks');
const finaleTools = path.join(finale, 'tools');
const transitionTools = path.join(transition, 'checks');
const runtime = path.join(introSnapshot, 'runtime.jar');
const rebaseChecks = path.join(work, 'rebase-checks');
const rebaseSource = path.join(project, 'tools/runtime_patches/ManorPresentationRebaseCheck.java');
const rebaseSourceHash = hash(rebaseSource);
fs.mkdirSync(rebaseChecks, { recursive: true });
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-d', rebaseChecks, rebaseSource]);
const rebaseEntries = path.join(work, 'rebase-protected-entries.txt');
fs.writeFileSync(rebaseEntries, [...entries.keys(), ...shared].sort().join('\n') + '\n');
run(java, ['-Xverify:all', '-cp', rebaseChecks, 'ManorPresentationRebaseCheck', originalBaseline, baseline, rebaseEntries]);
const candidate = path.join(work, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
function update(list, directory = payload) {
  run(jar, ['--update', '--file', candidate, ...list.flatMap(entry => ['-C', directory, entry])]);
}
update([...entries.keys()].sort());
const dependencies = [checks, finaleTools, transitionTools, runtime].join(path.delimiter);
const replay = path.join(work, 'finale-replayed');
run(java, ['-Xverify:all', '-cp', dependencies, 'PatchManorFinale', candidate, replay]);
for (const entry of shared) copyEntry(replay, entry, null, 'finale-shared-hook');
update(shared);
const repeated = path.join(work, 'idempotency-replay');
run(java, ['-Xverify:all', '-cp', dependencies, 'PatchManorFinale', candidate, repeated]);
for (const entry of shared) {
  if (hash(path.join(repeated, entry)) !== hash(path.join(payload, entry))) throw new Error('Non-idempotent finale hook: ' + entry);
}
const controlled = [...entries.keys()].sort();
const allowlist = path.join(work, 'entries.txt');
fs.writeFileSync(allowlist, controlled.join('\n') + '\n');
const integrationChecks = path.join(work, 'integration-checks');
fs.mkdirSync(integrationChecks, { recursive: true });
run(path.join(jdk, 'javac.exe'), ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp',
  candidate + path.delimiter + runtime, '-d', integrationChecks,
  path.join(project, 'tools/verification/ManorCeremonyCheck.java'),
  ...['ManorCeremonyModel', 'ManorCeremonyGeometry', 'ManorIntroTimeline', 'ManorVictoryGeometry']
    .map(name => path.join(project, 'src/main/java/net/farmingtales/ftboss/util', name + '.java'))]);
const classpath = [candidate, integrationChecks, dependencies].join(path.delimiter);
function verify(name, args = []) {
  run(java, ['-Xverify:all', '-cp', classpath, name, ...args]);
}
verify('ManorRitualPack', ['verify', baseline, candidate, allowlist]);
verify('FoodWeaponBuildLinkCheck', [payload, candidate]);
verify('ManorFinaleBoundaryCheck', [baseline, candidate]);
verify('ManorFinaleRuntimeLinkCheck');
verify('ManorFinaleSymbolCheck', [payload]);
verify('ManorFinaleSideCheck', [payload]);
verify('ManorFinalePacketCheck');
verify('ManorFinaleGeometryCheck');
verify('ManorFinalePlaybackCheck');
verify('ManorTransitionBytecodeCheck', [candidate, path.join(transition, 'overlay')]);
verify('ManorTransitionCheck', [path.join(work, 'transition-runtime-mesh')]);
verify('ManorIntroCheck', [path.join(work, 'mesh_samples.json')]);
if (hash(path.join(work, 'mesh_samples.json')) !== hash(intro.runtimeGeometrySamples)) {
  throw new Error('Integrated intro geometry differs from the visually reviewed runtime samples');
}
run(java, ['-Xverify:all', '-cp', integrationChecks, 'ManorCeremonyCheck',
  path.join(work, 'legacy-source-ceremony-regression.png'), project]);
verify('ZipStreamCheck', [candidate]);
run(process.execPath, [path.join(project, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
if (hash(live) !== baselineHash) throw new Error('Installed JAR changed during integration');
for (const [file, expected] of Object.entries(metadataHashes)) {
  if (hash(file) !== expected) throw new Error('Task manifest changed during integration: ' + file);
}
if (hash(__filename) !== scriptHash) throw new Error('Integration script changed while running');
if (hash(rebaseSource) !== rebaseSourceHash) throw new Error('Rebase guard changed while running');
const report = {
  passed: true, baseline: live, baselineSha256: baselineHash, candidate, candidateSha256: hash(candidate),
  originalTaskBaselineSha256: intro.baselineSha256, rebaseGuardSha256: rebaseSourceHash,
  payload, entries: controlled.map(entry => ({ entry, ...entries.get(entry) })),
  sourceReports: metadataHashes, integrationScriptSha256: scriptHash,
  previews: {
    intro: path.join(root, 'art'), transition: path.join(transition, 'verification'), finale: path.join(finale, 'previews')
  },
  protocol: finaleReport.network.newProtocol, legacySourceRegression: true, gameplayTested: false, deployed: false
};
fs.copyFileSync(__filename, path.join(work, path.basename(__filename)));
fs.writeFileSync(path.join(work, 'build-report.json'), JSON.stringify(report, null, 2) + '\n');
fs.writeFileSync(path.join(root, 'LATEST_MERGED.json'), JSON.stringify(report, null, 2) + '\n');
console.log('PASS integrated manor presentation: ' + candidate);
