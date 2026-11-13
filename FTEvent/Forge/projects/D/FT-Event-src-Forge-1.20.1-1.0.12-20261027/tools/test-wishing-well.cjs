'use strict';
const fs = require('fs');
const path = require('path');
const vm = require('vm');
const project = path.resolve(__dirname, '..');
const fixturesRoot = path.join(__dirname, 'wishing-well-tests');
const index = process.argv.indexOf('--instance');
const target = index >= 0 ? path.resolve(process.argv[index + 1]) : path.join(project, 'integration');
const reference = fs.readFileSync(path.join(fixturesRoot, 'reference', '1.0.7-wishing_well.behavior.test.js'), 'utf8');
const context = vm.createContext({ require, console, __dirname, process, fixturesRoot, target });
const prefix = reference.slice(0, reference.indexOf("console.log('harness root='"))
  .replace("const ROOT = path.resolve(__dirname, '..', '..');", 'const ROOT = fixturesRoot;');
vm.runInContext(prefix, context, { filename: 'preserved-1.0.7-fixtures.js' });
vm.runInContext("FILE.well = path.join(target, 'kubejs/server_scripts/block_events/wishing_well.js'); FILE.tooltip = path.join(target, 'kubejs/client_scripts/tooltips/wishing_well.js'); FILE.coins = path.join(ROOT, 'reference', 'current-coins.js');", context);
for (const name of ['manor-shop.fixtures.js', 'reward.fixtures.js', 'coin.cases.js', 'reward.acceptance.js', 'reward.cases.js', 'manor-shop.acceptance.js', 'manor-shop.cases.js', 'emerald-upgrade.acceptance.js', 'emerald-upgrade.cases.js', 'ui.cases.js', 'manor-unlock.cases.js']) {
  vm.runInContext(fs.readFileSync(path.join(fixturesRoot, 'cases', name), 'utf8'), context, { filename: name });
}
for (const [start, end] of [
  ['function shopSetup(', "section('T16 shop:"],
  ["section('T16b full bag", "section('T25 candidate"],
  ['function shopLifecycleSetup(', "section('T16h reporting failures"],
  ["section('T16i wrapper availability", "section('T18 enabled lootable"],
  ["section('T19 dropped-entry", "section('T23 tooltip"]
]) {
  const startIndex = reference.indexOf(start);
  const endIndex = reference.indexOf(end);
  if (startIndex < 0 || endIndex <= startIndex) throw new Error('Missing retained regression boundaries');
  const retained = reference.slice(startIndex, endIndex)
    .replaceAll("indexOf('1 金币锅')", "indexOf(K(c, 'FTWW_SUMMON_PRICE') + ' 金币锅')")
    .replace("eq(countAll(c, 'farmingtales:gold_bucket'), 1, 'only the untouched pot remains in the restored real backpack after one queued summon');",
      "eq(countAll(c, 'farmingtales:gold_bucket'), 2 - K(c, 'FTWW_SUMMON_PRICE'), 'configured summon price is deducted from the restored backpack');");
  vm.runInContext(retained, context, { filename: 'retained-regressions.js' });
}
const result = vm.runInContext('({ pass, fail, failures })', context);
console.log(JSON.stringify(result, null, 2));
process.exitCode = result.fail ? 1 : 0;
