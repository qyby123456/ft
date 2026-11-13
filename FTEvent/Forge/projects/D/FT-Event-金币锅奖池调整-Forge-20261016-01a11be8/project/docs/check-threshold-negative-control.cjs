const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { createRequire } = require('node:module');

const root = path.resolve(__dirname, '..', '..');
const harnessPath = path.join(__dirname, 'wishing_well.behavior.test.js');
const candidatePath = path.join(root, 'integration', 'kubejs', 'server_scripts', 'block_events', 'wishing_well.js');
const baselinePath = process.argv[2];
assert.ok(baselinePath, 'Pass the pre-fix wishing_well.js path');
const baseline = fs.readFileSync(baselinePath);
const nativeRequire = createRequire(harnessPath);
const output = [];
let exitCode = null;
const interceptedFs = {
  ...fs,
  readFileSync(filename, options) {
    if (path.resolve(String(filename)) !== candidatePath) return fs.readFileSync(filename, options);
    const encoding = typeof options === 'string' ? options : options && options.encoding;
    return encoding ? baseline.toString(encoding) : Buffer.from(baseline);
  }
};
const context = vm.createContext({
  __dirname: __dirname,
  __filename: harnessPath,
  Buffer: Buffer,
  require: name => name === 'fs' ? interceptedFs : nativeRequire(name),
  console: { log: (...values) => output.push(values.join(' ')) },
  process: { exit: code => { exitCode = code; } }
});
vm.runInContext(fs.readFileSync(harnessPath, 'utf8'), context, { filename: harnessPath, timeout: 30000 });
const lostRewards = output.filter(line => line.includes('  - actual inventory plus explicit drops retains every reward after menu restoration'));
assert.equal(exitCode, 1, 'The updated harness must reject the pre-fix baseline');
assert.equal(lostRewards.length, 3, 'All three threshold-crossing loss cases must fail against the old delivery order');
console.log(JSON.stringify({
  negativeControl: 'PASS: the old script is rejected for actual reward loss, not only its hash or display text',
  baseline: path.resolve(baselinePath),
  detectedRewardLossCases: lostRewards
}, null, 2));
