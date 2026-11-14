const fs = require('fs');
const path = require('path');
const os = require('os');
const crypto = require('crypto');
const { spawnSync } = require('child_process');
const ROOT = path.resolve(__dirname, '../../../..');
const WORK = path.join(ROOT, '_work/boss-relics-refine-20261021/wine');
const python = process.env.FTBOSS_WINE_RELIC_PYTHON || path.join(os.homedir(), '.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe');
const resources = [
  'src/main/resources/assets/ftboss/textures/item/aged_wine_seal.png',
  'src/main/resources/assets/ftboss/textures/item/boss_relics/aged_wine_seal.png',
  'src/main/resources/assets/ftboss/models/item/aged_wine_seal.json',
  'src/main/resources/assets/ftboss/models/item/boss_relics/aged_wine_seal.json',
  'src/main/resources/assets/ftboss/models/item/boss_relics/aged_wine_seal_gui.json',
  'art/boss_relics/blockbench/aged_wine_seal.bbmodel'
];
fs.mkdirSync(WORK, { recursive: true });
for (const relative of resources) {
  const source = path.join(ROOT, relative);
  const backup = path.join(WORK, 'backup', relative);
  if (!fs.existsSync(backup) && fs.existsSync(source)) {
    fs.mkdirSync(path.dirname(backup), { recursive: true });
    fs.copyFileSync(source, backup);
  }
}
const steps = [
  [process.execPath, ['tools/model3d/boss_relics_refine/wine/build.cjs']],
  [python, ['tools/model3d/boss_relics_refine/wine/render.py', '--make-icon']],
  [process.execPath, ['tools/model3d/lint_java_model.cjs', 'src/main/resources/assets/ftboss/models/item/boss_relics/aged_wine_seal.json', 'src/main/resources/assets/ftboss/models/item/boss_relics/aged_wine_seal_gui.json']],
  [process.execPath, ['tools/model3d/check_uv_coverage.cjs', 'src/main/resources/assets/ftboss/models/item/boss_relics/aged_wine_seal.json', 'src/main/resources/assets/ftboss/textures/item/boss_relics/aged_wine_seal.png']],
  [python, ['tools/model3d/boss_relics_refine/wine/validate.py']]
];
const logs = [];
for (const [executable, argumentsList] of steps) {
  const result = spawnSync(executable, argumentsList, { cwd: ROOT, encoding: 'utf8', windowsHide: true });
  const log = { executable, arguments: argumentsList, exitCode: result.status, stdout: result.stdout, stderr: result.stderr, error: result.error?.message };
  logs.push(log);
  console.log(result.stdout || '');
  if (result.stderr) console.error(result.stderr);
  if (result.status !== 0) {
    fs.writeFileSync(path.join(WORK, 'checks.json'), JSON.stringify(logs, null, 2) + '\n');
    process.exit(result.status || 1);
  }
}
fs.writeFileSync(path.join(WORK, 'checks.json'), JSON.stringify(logs, null, 2) + '\n');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const hashes = {};
for (const relative of resources) hashes[relative] = hash(path.join(ROOT, relative));
for (const file of fs.readdirSync(__dirname)) {
  const target = path.join(__dirname, file);
  if (fs.statSync(target).isFile()) hashes[path.relative(ROOT, target).replaceAll('\\', '/')] = hash(target);
}
for (const file of fs.readdirSync(path.join(ROOT, 'art/boss_relics/refine/wine'))) {
  const target = path.join(ROOT, 'art/boss_relics/refine/wine', file);
  if (fs.statSync(target).isFile()) hashes[path.relative(ROOT, target).replaceAll('\\', '/')] = hash(target);
}
fs.writeFileSync(path.join(WORK, 'sha256.json'), JSON.stringify(hashes, null, 2) + '\n');
console.log('Wine-relic-only asset rebuild and final-runtime verification passed.');
