const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');

const scriptPath = process.argv[2];
assert.ok(scriptPath, 'Pass the deployed wishing_well.js path');
const script = fs.readFileSync(scriptPath, 'utf8');
const noop = () => {};

function makeStack(id, count) {
  return { id, count, isEmpty() { return this.count <= 0; }, shrink(amount) { this.count -= amount; } };
}

function setup(previousPots, count, options = {}) {
  const key = 'ftww_progress_v1_test-player';
  const saved = new Map([[key, JSON.stringify({ emeralds: 7, goldPots: previousPots })]]);
  const given = [];
  let engineCalls = 0;
  let openedShops = 0;
  const context = vm.createContext({
    global: {}, console: { info: noop, warn: noop, error: noop },
    EntityEvents: { spawned: noop }, BlockEvents: { rightClicked: noop },
    ServerEvents: { tick: noop, commandRegistry: noop, recipes: noop },
    Text: { of: value => value },
    Item: { of: (value, amount) => typeof value === 'string' ? makeStack(value, amount) : value },
    Java: { loadClass(name) {
      assert.equal(name, 'cn.farmingtales.qysevent.wishingwell.GoldPotWishes');
      engineCalls++;
      if (options.engineFailure) throw new Error('missing Event');
      return { rollRewards(level, previous, donated) {
        const rolls = options.mismatch ? 0 : Math.max(0, Math.min(donated, previous + donated - 100));
        return Array.from({ length: rolls }, () => ({
          category: () => 'manor_reward',
          stack: () => ({ ...makeStack('ftboss:test_reward', options.empty ? 0 : 1), nbt: { preserved: true } })
        }));
      } };
    } }
  });
  vm.runInContext(script, context, { filename: scriptPath });
  context.ftwwTell = noop;
  context.ftwwShowProgress = noop;
  context.ftwwSafeOpenShop = () => { openedShops++; };
  context.ftwwPlayResult = (event, outcome, stack) => {
    assert.equal(outcome.type, 'manor_reward');
    assert.equal(stack.nbt.preserved, true);
    given.push(stack);
  };
  const event = {
    level: {}, player: { uuid: 'test-player' },
    server: { persistentData: {
      getString: entry => saved.get(entry) || '',
      putString(entry, value) {
        if (options.saveFailure) throw new Error('save failed');
        saved.set(entry, value);
      }
    } }
  };
  const stack = makeStack('farmingtales:gold_bucket', count);
  const dropped = options.dropped ? { item: stack, removed: false, discard() { this.removed = true; } } : null;
  return {
    donate: amount => context.ftwwDonatePots(event, stack, amount, dropped), stack, dropped, given,
    progress: () => JSON.parse(saved.get(key)),
    engineCalls: () => engineCalls, shops: () => openedShops
  };
}

let state = setup(99, 1);
assert.equal(state.donate(1), true);
assert.equal(state.stack.count, 0);
assert.equal(state.progress().goldPots, 100);
assert.equal(state.engineCalls(), 0);
assert.equal(state.given.length, 0);
assert.equal(state.shops(), 1);

state = setup(99, 3);
assert.equal(state.donate(3), true);
assert.equal(state.progress().goldPots, 102);
assert.equal(state.progress().emeralds, 7);
assert.equal(state.given.length, 2);

for (const amount of [1, 64]) {
  state = setup(100, amount);
  assert.equal(state.donate(amount), true);
  assert.equal(state.stack.count, 0);
  assert.equal(state.given.length, amount);
  assert.equal(state.progress().goldPots, 100 + amount);
}

state = setup(100, 4, { dropped: true });
assert.equal(state.donate(4), true);
assert.equal(state.dropped.item.count, 0);
assert.equal(state.dropped.removed, true);
assert.equal(state.given.length, 4);

state = setup(100, 4, { dropped: true });
assert.equal(state.donate(1), true);
assert.equal(state.dropped.item.count, 3);
assert.equal(state.dropped.removed, false);
assert.equal(state.given.length, 1);

for (const options of [{ engineFailure: true }, { empty: true }, { mismatch: true }, { saveFailure: true },
  { engineFailure: true, dropped: true }, { saveFailure: true, dropped: true }]) {
  state = setup(100, 4, options);
  assert.equal(state.donate(4), false);
  assert.equal(state.dropped ? state.dropped.item.count : state.stack.count, 4);
  assert.equal(state.progress().goldPots, 100);
  assert.equal(state.given.length, 0);
  if (state.dropped) assert.equal(state.dropped.removed, false);
}

state = setup(Number.MAX_SAFE_INTEGER, 1);
assert.equal(state.donate(1), false);
assert.equal(state.stack.count, 1);
assert.equal(state.engineCalls(), 0);

console.log('Gold-pot bridge verified: upgrade/bulk/drop, NBT retention, count validation, no-charge failures and rollback.');
