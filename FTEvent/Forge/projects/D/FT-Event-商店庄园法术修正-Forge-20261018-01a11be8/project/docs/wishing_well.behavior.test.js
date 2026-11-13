'use strict';
const fs = require('fs');
const path = require('path');
const vm = require('vm');
const root = path.resolve(__dirname, '..', '..');
const reference = fs.readFileSync(path.join(root, 'reference', '1.0.7-wishing_well.behavior.test.js'), 'utf8');
const context = vm.createContext({ require, console, __dirname, process });
vm.runInContext(reference.slice(0, reference.indexOf("console.log('harness root='")), context, { filename: 'preserved-1.0.7-fixtures.js' });
vm.runInContext(fs.readFileSync(path.join(__dirname, 'manor-shop.fixtures.js'), 'utf8'), context, { filename: 'manor-shop.fixtures.js' });
const instanceIndex = process.argv.indexOf('--instance');
if (instanceIndex >= 0) {
  if (!process.argv[instanceIndex + 1] || process.argv.includes('--baseline')) throw new Error('Use --instance with a path and without --baseline');
  context.installedRoot = path.resolve(process.argv[instanceIndex + 1]);
  vm.runInContext("for (const name of ['well', 'coins', 'tooltip']) FILE[name] = path.join(installedRoot, path.relative(path.join(ROOT, 'integration'), FILE[name]))", context);
}
if (process.argv.includes('--baseline')) {
  vm.runInContext("FILE.well = path.join(ROOT, 'reference', '1.0.8-before-manor-shop-wishing_well.js')", context);
}
vm.runInContext(fs.readFileSync(path.join(__dirname, 'manor-shop.acceptance.js'), 'utf8'), context, { filename: 'manor-shop.acceptance.js' });
if (!process.argv.includes('--baseline')) {
  vm.runInContext(fs.readFileSync(path.join(__dirname, 'manor-shop.cases.js'), 'utf8'), context, { filename: 'manor-shop.cases.js' });
  vm.runInContext(fs.readFileSync(path.join(__dirname, 'emerald-upgrade.acceptance.js'), 'utf8'), context, { filename: 'emerald-upgrade.acceptance.js' });
  vm.runInContext(fs.readFileSync(path.join(__dirname, 'emerald-upgrade.cases.js'), 'utf8'), context, { filename: 'emerald-upgrade.cases.js' });
  const retained = [
    ['function shopSetup(', "section('T16 shop:"],
    ["section('T16b full bag", "section('T25 candidate"],
    ['function shopLifecycleSetup(', "section('T18 enabled lootable"],
    ["section('T19 dropped-entry", "section('T23 tooltip"]
  ];
  for (const [start, end] of retained) {
    const startIndex = reference.indexOf(start);
    const endIndex = reference.indexOf(end);
    if (startIndex < 0 || endIndex <= startIndex) throw new Error('Preserved regression boundaries are missing');
    vm.runInContext(reference.slice(startIndex, endIndex), context, { filename: 'unchanged-1.0.7-shop-regressions.js' });
  }
}
const result = vm.runInContext('({ pass, fail, failures })', context);
console.log(JSON.stringify(result, null, 2));
process.exitCode = result.fail ? 1 : 0;
