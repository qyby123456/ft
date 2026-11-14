'use strict';

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const assert = require('assert/strict');
const { spawnSync } = require('child_process');
const root = path.resolve(__dirname, '..');
const live = path.resolve(process.argv[2]);
const classpathFile = path.resolve(process.argv[3]);
const output = path.resolve(process.argv[4]);
assert(output.startsWith(path.join(root, '_work') + path.sep));
assert(!fs.existsSync(output), 'Build output must be fresh');
fs.mkdirSync(output, { recursive: true });
const sha = contents => crypto.createHash('sha256').update(contents).digest('hex');
const expected = sha(fs.readFileSync(live));
const baseline = path.join(output, 'baseline.jar');
fs.copyFileSync(live, baseline);
const minecraft = path.join(process.env.APPDATA, '.minecraft');
const jdk = path.join(minecraft, 'runtime/java-runtime-gamma-snapshot/bin');
const gradle = path.join(process.env.USERPROFILE, '.gradle/caches/forge_gradle');
const mapping = path.join(gradle, 'minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg');
const renamer = path.join(gradle, 'maven_downloader/net/minecraftforge/ForgeAutoRenamingTool/1.0.2/ForgeAutoRenamingTool-1.0.2-all.jar');
const forge = path.join(gradle, 'minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar');
const officialForge = path.join(gradle, 'minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar');
const mapped = path.join(output, 'baseline-mapped.jar');
const transcript = path.join(output, 'build.log');
const sourceHashes = {};
const guideOnly = process.argv.includes('--guide-only');
const layoutOnly = process.argv.includes('--layout-only');
const privacyOnly = process.argv.includes('--privacy-only');
const summaryOnly = process.argv.includes('--summary-only');
const sources = (summaryOnly ? ['menu/BossPreviewMenu.java', 'client/BossPreviewScreen.java', 'util/BossPreviewLayout.java', 'util/BossPreviewSummaryRules.java']
  : privacyOnly ? ['client/BossPreviewScreen.java', 'util/BossPreviewLayout.java', 'util/BossPreviewSummaryRules.java'] : layoutOnly ? ['client/BossPreviewScreen.java', 'util/BossPreviewLayout.java', 'util/BossPreviewSummaryRules.java']
  : guideOnly ? ['client/BossPreviewScreen.java', 'util/BossGuidePagination.java', 'util/BossPreviewLayout.java', 'util/BossPreviewSummaryRules.java'] : ['util/BossPreviewRules.java', 'integ/BossPreviewTitleAccess.java', 'block/BlankStatueContent.java',
  'block/BlankStatueBlock.java', 'menu/BossPreviewMenu.java', 'client/BlankStatueClient.java', 'client/BossPreviewScreen.java', 'util/BossGuidePagination.java', 'util/BossPreviewLayout.java', 'util/BossPreviewSummaryRules.java'])
  .map(relative => 'src/main/java/net/farmingtales/ftboss/' + relative);
for (const relative of sources) sourceHashes[relative] = sha(fs.readFileSync(path.join(root, relative)));
for (const relative of ['tools/build_boss_preview_increment.cjs', 'tools/boss_preview_summaries.cjs',
  'tools/gen_boss_preview_guide.cjs', 'tools/verification/check_boss_preview_summary.cjs',
  'tools/verification/BossPreviewSummaryRulesCheck.java', 'tools/verification/BossPreviewSummaryAudit.java',
  'tools/verification/check_boss_preview.cjs']) sourceHashes[relative] = sha(fs.readFileSync(path.join(root, relative)));

function run(executable, argumentsList, cwd = root) {
  const result = spawnSync(executable, argumentsList, { encoding: 'utf8', cwd, maxBuffer: 32 * 1024 * 1024 });
  fs.appendFileSync(transcript, result.stdout + result.stderr);
  if (result.status !== 0) throw new Error(result.stdout + result.stderr);
  console.log('PASS ' + path.basename(executable));
  return result.stdout;
}
function java(argumentsList, cwd) { return run(path.join(jdk, 'java.exe'), argumentsList, cwd); }
function walk(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => entry.isDirectory()
    ? walk(path.join(directory, entry.name)) : [path.join(directory, entry.name)]);
}
run(process.execPath, [path.join(root, 'tools/verification/check_boss_preview_layout.cjs'), path.join(output, 'layout')]);
run(process.execPath, [path.join(root, 'tools/verification/check_boss_preview_privacy.cjs'), path.join(output, 'privacy')]);
run(process.execPath, [path.join(root, 'tools/verification/check_boss_preview_summary.cjs'), path.join(output, 'summary')]);
if (summaryOnly) run(process.execPath, [path.join(root, 'tools/verification/check_boss_preview.cjs'), path.join(output, 'behavior')]);
if (!layoutOnly && !privacyOnly && !summaryOnly) {
const factsTools = path.join(output, 'facts-tools');
const runtimeFacts = path.join(output, 'runtime-facts.json');
const factResources = path.join(output, 'facts-resources');
fs.mkdirSync(factResources);
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '--release', '17', '-d', factsTools,
  path.join(root, 'tools/verification/BossPreviewRuntimeFacts.java')]);
java(['-Xverify:all', '-cp', factsTools, 'BossPreviewRuntimeFacts', baseline, runtimeFacts]);
run(path.join(jdk, 'jar.exe'), ['xf', baseline, 'assets/ftboss/lang/zh_cn.json'], factResources);
fs.copyFileSync(path.join(factResources, 'assets/ftboss/lang/zh_cn.json'), path.join(output, 'runtime-zh_cn.json'));
run(process.execPath, [path.join(root, 'tools/verification/check_boss_preview.cjs'), path.join(output, 'behavior'), runtimeFacts]);
}
java(['-Xmx2G', '-jar', renamer, '--input', baseline, '--output', mapped, '--map', mapping, '--lib', forge, '--threads', '2']);
const classes = path.join(output, 'classes');
const empty = path.join(output, 'empty-sourcepath');
fs.mkdirSync(empty);
const exportedClasspath = fs.readFileSync(classpathFile, 'utf8').trim();
run(path.join(jdk, 'javac.exe'), ['-J-Dfile.encoding=UTF-8', '--release', '17', '-encoding', 'UTF-8', '-proc:none',
  '-sourcepath', empty, '-classpath', mapped + path.delimiter + exportedClasspath, '-d', classes, ...sources.map(relative => path.join(root, relative))]);
const official = path.join(output, 'official.jar');
run(path.join(jdk, 'jar.exe'), ['--create', '--file', official, '-C', classes, '.']);
const incrementClasses = path.join(output, 'srg.jar');
java(['-Xmx2G', '-jar', renamer, '--input', official, '--output', incrementClasses, '--map', mapping, '--reverse',
  '--lib', officialForge, '--lib', mapped, '--threads', '2']);
const payload = path.join(output, 'payload');
fs.mkdirSync(payload);
run(path.join(jdk, 'jar.exe'), ['xf', incrementClasses], payload);
const generatedManifest = path.join(payload, 'META-INF/MANIFEST.MF');
if (fs.existsSync(generatedManifest)) fs.unlinkSync(generatedManifest);
const patchAsm = ['asm', 'asm-tree', 'asm-util'].map(name => path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
const patchTools = path.join(output, 'constructor-tools');
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '--release', '17', '-cp', patchAsm.join(path.delimiter), '-d', patchTools,
  path.join(root, 'tools/verification/BlankStatueConstructorPatch.java')]);
if (!guideOnly && !layoutOnly && !privacyOnly && !summaryOnly) java(['-Xverify:all', '-cp', [patchTools, ...patchAsm].join(path.delimiter), 'BlankStatueConstructorPatch', baseline,
  path.join(payload, 'net/farmingtales/ftboss/FTBossMod.class')]);
const resources = path.join(root, 'src/main/resources');
for (const filename of walk(resources)) {
  if (layoutOnly || privacyOnly || summaryOnly) continue;
  const relative = path.relative(resources, filename).replaceAll('\\', '/');
  if (relative !== 'assets/ftboss/boss_preview/catalog.json' && (guideOnly || !relative.includes('statue_blank'))) continue;
  const target = path.join(payload, relative);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.copyFileSync(filename, target);
  sourceHashes['src/main/resources/' + relative] = sha(fs.readFileSync(filename));
}
if (summaryOnly) {
  const extractedCatalog = path.join(output, 'original-catalog');
  fs.mkdirSync(extractedCatalog);
  const relative = 'assets/ftboss/boss_preview/catalog.json';
  run(path.join(jdk, 'jar.exe'), ['xf', baseline, relative], extractedCatalog);
  const { summarizeCatalog } = require('./boss_preview_summaries.cjs');
  const current = JSON.parse(fs.readFileSync(path.join(extractedCatalog, relative), 'utf8'));
  const target = path.join(payload, relative);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.writeFileSync(target, JSON.stringify(summarizeCatalog(current), null, 2) + '\n');
}
const extracted = path.join(output, 'original-languages');
fs.mkdirSync(extracted);
run(path.join(jdk, 'jar.exe'), ['xf', baseline, 'assets/ftboss/lang/zh_cn.json', 'assets/ftboss/lang/en_us.json'], extracted);
for (const locale of ['zh_cn', 'en_us']) {
  if (layoutOnly) continue;
  const relative = `assets/ftboss/lang/${locale}.json`;
  const language = JSON.parse(fs.readFileSync(path.join(extracted, relative), 'utf8'));
  const current = JSON.parse(fs.readFileSync(path.join(resources, relative), 'utf8'));
  const summaryLabels = require('./boss_preview_summaries.cjs').labels[locale];
  for (const [key, value] of Object.entries(current)) {
    if (summaryOnly ? Object.hasOwn(summaryLabels, key.replace(/^ui\.ftboss\.preview\./, '')) && key.startsWith('ui.ftboss.preview.')
      : privacyOnly ? ['ui.ftboss.preview.empty', 'ui.ftboss.preview.title_required'].includes(key)
      : key === 'block.ftboss.statue_blank' || key.startsWith('ui.ftboss.preview.')) language[key] = value;
  }
  if (privacyOnly || summaryOnly) {
    const original = JSON.parse(fs.readFileSync(path.join(extracted, relative), 'utf8'));
    for (const [key, value] of Object.entries(original)) {
      if (summaryOnly ? key.startsWith('ui.ftboss.preview.') && Object.hasOwn(summaryLabels, key.slice('ui.ftboss.preview.'.length))
        : key === 'ui.ftboss.preview.empty' || key === 'ui.ftboss.preview.title_required') continue;
      assert.equal(language[key], value, 'Unrelated language changed: ' + key);
    }
  }
  fs.mkdirSync(path.dirname(path.join(payload, relative)), { recursive: true });
  fs.writeFileSync(path.join(payload, relative), JSON.stringify(language, null, 2) + '\n');
}
const candidate = path.join(output, 'qys-bosses.jar');
fs.copyFileSync(baseline, candidate);
const entries = walk(payload).map(filename => path.relative(payload, filename).replaceAll('\\', '/')).sort();
run(path.join(jdk, 'jar.exe'), ['--update', '--file', candidate, ...entries.flatMap(entry => ['-C', payload, entry])]);
const increment = path.join(output, 'boss-preview-increment.jar');
run(path.join(jdk, 'jar.exe'), ['--create', '--file', increment, ...entries.flatMap(entry => ['-C', payload, entry])]);
const originalTree = path.join(output, 'original-tree');
const candidateTree = path.join(output, 'candidate-tree');
fs.mkdirSync(originalTree);
fs.mkdirSync(candidateTree);
run(path.join(jdk, 'jar.exe'), ['xf', baseline], originalTree);
run(path.join(jdk, 'jar.exe'), ['xf', candidate], candidateTree);
let preserved = 0;
for (const filename of walk(originalTree)) {
  const relative = path.relative(originalTree, filename).replaceAll('\\', '/');
  if (entries.includes(relative)) continue;
  assert.equal(sha(fs.readFileSync(filename)), sha(fs.readFileSync(path.join(candidateTree, relative))), 'Unrelated entry changed: ' + relative);
  preserved++;
}
for (const filename of walk(candidateTree)) {
  const relative = path.relative(candidateTree, filename).replaceAll('\\', '/');
  assert(entries.includes(relative) || fs.existsSync(path.join(originalTree, relative)), 'Unexpected new entry: ' + relative);
}
const asm = ['asm', 'asm-tree', 'asm-commons', 'asm-util', 'asm-analysis'].map(name => path.join(minecraft, `libraries/org/ow2/asm/${name}/9.7.1/${name}-9.7.1.jar`));
const tools = path.join(output, 'validation-tools');
run(path.join(jdk, 'javac.exe'), ['-encoding', 'UTF-8', '--release', '17', '-classpath', asm.join(path.delimiter), '-d', tools,
  path.join(root, 'tools/verification/FoodWeaponBuildLinkCheck.java'), path.join(root, 'tools/verification/ZipStreamCheck.java'),
  path.join(root, 'tools/verification/BossPreviewSummaryAudit.java'),
  path.join(root, 'tools/verification/EncodeJvmArguments.java')]);
const libraries = walk(path.join(minecraft, 'libraries')).filter(filename => filename.endsWith('.jar') && !/natives|sources|javadoc/.test(path.basename(filename)));
const mods = fs.readdirSync(path.dirname(live)).filter(filename => filename.endsWith('.jar') && filename !== path.basename(live))
  .map(filename => path.join(path.dirname(live), filename));
const runtimeClasspath = [candidate, tools, forge, ...asm, ...libraries, ...mods].join(path.delimiter);
const argumentsFile = path.join(output, 'runtime-link.args');
fs.writeFileSync(argumentsFile, ['-Xverify:all', '-cp', runtimeClasspath, 'FoodWeaponBuildLinkCheck', payload, candidate]
  .map(value => '"' + value.replaceAll('\\', '/') + '"').join('\n') + '\n');
const nativeArguments = path.join(output, 'runtime-link-native.args');
java(['-cp', tools, 'EncodeJvmArguments', argumentsFile, nativeArguments, path.join(output, 'runtime-library-aliases')]);
java(['-Dfile.encoding=UTF-8', '@' + nativeArguments]);
java(['-Xverify:all', '-cp', [tools, ...asm].join(path.delimiter), 'BossPreviewSummaryAudit', baseline, candidate]);
java(['-cp', tools, 'ZipStreamCheck', candidate]);
run(process.execPath, [path.join(root, 'tools/verification/check_jar_zip_rules.cjs'), candidate]);
run(process.execPath, [path.join(root, 'tools/verification/check_blank_statue_regression.cjs'), path.join(output, 'statue-registration'), candidate]);
assert.equal(sha(fs.readFileSync(live)), expected, 'Live jar changed during build: rebase required');
for (const [relative, expectedSource] of Object.entries(sourceHashes)) assert.equal(sha(fs.readFileSync(path.join(root, relative))), expectedSource);
for (const relative of Object.keys(sourceHashes)) {
  const snapshot = path.join(output, 'sources', relative);
  fs.mkdirSync(path.dirname(snapshot), { recursive: true });
  fs.copyFileSync(path.join(root, relative), snapshot);
}
fs.writeFileSync(path.join(output, 'report.json'), JSON.stringify({ status: 'PASS_NOT_DEPLOYED', baselineSHA256: expected,
  candidateSHA256: sha(fs.readFileSync(candidate)), incrementSHA256: sha(fs.readFileSync(increment)),
  entries, preservedEntries: preserved, sourceHashes, statueRegistrationRegressionGuard: true,
  actualForgeStartup: false, actualProductionWrites: 0, inGameTested: false }, null, 2) + '\n');
console.log('PASS candidate and increment; preserved unrelated runtime entries: ' + preserved);
