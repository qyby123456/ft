function plannedCount(plan) {
  return plan.rewards.reduce((count, reward) => count + reward.deliveries.reduce((total, stack) => total + stack.count, 0), 0);
}
section('Q02 actual quantity preparation, preserved category probabilities and protected rare items');
{
  const candidate = rewardSetup();
  const baseline = create();
  vm.runInContext(fs.readFileSync(path.join(ROOT, 'reference', 'before-threefold-wishing_well.js'), 'utf8'), baseline.vmCtx);
  eq(JSON.stringify(K(candidate, 'FTWW_LEVEL_ODDS')), JSON.stringify(K(baseline, 'FTWW_LEVEL_ODDS')), 'all emerald category odds and all upgrade tiers are byte-for-byte numerically identical');
  eq(K(candidate, 'FTWW_REWARD_MULTIPLIER'), 3, 'normal multiplier is exactly 3');
  eq(K(candidate, 'FTWW_JACKPOT_CHANCE'), 0.01, 'material jackpot chance is exactly 1 percent');
  eq(K(candidate, 'FTWW_JACKPOT_MULTIPLIER'), 10, 'jackpot multiplies the new reward by another 10');
}
for (const [type, id, baseCount] of [
  ['junk', 'minecraft:cobblestone', 5], ['poop', 'kubejs:poop', 3],
  ['wood', 'minecraft:oak_log', 4], ['coin', 'farmingtales:gold_bundle', 9],
  ['seed_bag', 'farmingtales:carrot_packet', 1], ['mineral', 'cataclysm:ignitium_block', 2],
  ['emerald', 'minecraft:emerald', 8], ['geode', 'farmingtales:geode', 1],
  ['skill_experience', 'farmingtales:farming_experience_book', 1],
  ['manor_reward', 'ftboss:music_disc_manor_battle', 1]
]) {
  const candidate = rewardSetup(); const stack = candidate.sandbox.Item.of(id, baseCount);
  stack.nbt = { OriginalReward: id };
  const plan = candidate.sandbox.ftwwPrepareWishReward(rewardEvent(candidate), { type }, stack);
  eq(plannedCount(plan), baseCount * 3, 'normal ' + type + ' quantity triples');
  eq(stack.count, baseCount, 'preparation does not mutate original native stack');
  eq(plan.rewards.every(reward => reward.deliveries.every(delivery => delivery.count <= delivery.getMaxStackSize() && delivery.count <= 64)), true, 'every prepared delivery respects real stack limits');
  eq(plan.rewards.every(reward => reward.deliveries.every(delivery => delivery.nbt.OriginalReward === id)), true, 'split deliveries preserve reward NBT');
}
for (const [type, id] of [['spell', 'irons_spellbooks:scroll'], ['manor_reward', 'irons_spellbooks:scroll'], ['manor_reward', 'ftboss:dora_nova_launcher']]) {
  const candidate = rewardSetup({ rng: () => 0 });
  const stack = candidate.sandbox.Item.of(id); stack.nbt = { SpellOrWeapon: true };
  const plan = candidate.sandbox.ftwwPrepareWishReward(rewardEvent(candidate), { type }, stack);
  eq(plannedCount(plan), 1, 'weapon and scroll quantities remain one even on a jackpot random value');
  eq(plan.jackpot, false, 'weapons and spells cannot enter material jackpot branch');
  eq(plan.rewards[0].deliveries[0].nbt.SpellOrWeapon, true, 'protected reward NBT preserved');
}
section('Q03 exact jackpot boundary, useful-only branch and thirty-times quantities');
{
  const candidate = rewardSetup(); let jackpots = 0;
  for (let ticket = 0; ticket < 100; ticket++) {
    candidate.sandbox.Math.random = () => (ticket + 0.5) / 100;
    const plan = candidate.sandbox.ftwwPrepareWishReward(rewardEvent(candidate), { type: 'mineral' }, new Stack('minecraft:diamond', 2));
    if (plan.jackpot) jackpots++;
    eq(plannedCount(plan), ticket === 0 ? 60 : 6, 'jackpot ticket quantity ' + ticket);
  }
  eq(jackpots, 1, 'exactly one of 100 equally spaced jackpot tickets wins');
  for (const type of ['junk', 'poop']) {
    candidate.sandbox.Math.random = () => 0;
    const plan = candidate.sandbox.ftwwPrepareWishReward(rewardEvent(candidate), { type }, new Stack('minecraft:dirt', 2));
    eq(plannedCount(plan), 6, 'junk and poop still triple but never get a disappointing jackpot');
    eq(plan.jackpot, false, 'no false jackpot announcement for ' + type);
  }
}
section('Q04 32 verified meals, three distinct dishes, current market discount and >=1000 floor');
{
  const candidate = rewardSetup();
  const catalogIds = K(candidate, 'FTWW_FOOD_REWARDS').map(entry => entry.item);
  eq(catalogIds.length, 32, 'food choice expands to 32 dishes');
  eq([...catalogIds].sort().join(','), [...foodIds].sort().join(','), 'every configured food has a verified actual model and real positive shipping price');
}
for (const discount of [0, 15, 40, 60]) {
  const candidate = rewardSetup({ marketDiscount: discount, rng: mulberry32(discount + 818) });
  for (let wish = 0; wish < 64; wish++) {
    const plan = candidate.sandbox.ftwwPrepareWishReward(rewardEvent(candidate), { type: 'food' }, new Stack('minecraft:bread', 1));
    const unique = new Set(plan.rewards.map(reward => reward.stack.id));
    const value = plan.rewards.reduce((total, reward) => total + Math.floor(shippingPrices[reward.stack.id] * (100 - discount) / 100) * reward.stack.count, 0);
    eq(unique.size, 3, 'three distinct foods at discount ' + discount + ' wish ' + wish);
    eq(value >= (plan.jackpot ? 10000 : 1000), true, 'actual discounted value floor including jackpot at discount ' + discount);
    eq(plan.foodValue, value, 'reported food value equals conservative actual per-item calculation');
    eq(plan.rewards.every(reward => reward.deliveries.every(stack => stack.count > 0 && stack.count <= 16)), true, 'all food deliveries split to native-size stacks');
  }
}
section('Q05 real hand/drop payment and reward delivery for both wish pools, bulk and full backpack');
{
  const candidate = rewardSetup({ category: 'mineral', rewardId: 'minecraft:diamond_block', baseCount: 2 }); unlockRewards(candidate);
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 1);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 0, 'one actual gold pot pays one wish');
  eq(givenCount(candidate, 'minecraft:diamond_block'), 6, 'gold-pot route really delivers tripled mineral blocks');
  eq(prog(candidate, candidate.p.uuid).goldPots, 1, 'reward scaling never creates extra pot rolls');
  eq(candidate.nativeWishCalls, 1, 'native pool is not rerolled to manufacture jackpots');
}
{
  const candidate = rewardSetup({ rng: () => 0, category: 'coin', rewardId: 'farmingtales:gold_bundle', baseCount: 9 }); unlockRewards(candidate);
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 1);
  rcBlock(candidate, candidate.p, payment);
  eq(givenCount(candidate, 'farmingtales:gold_bundle'), 9, 'native gold-pot coin quantity is exact and never multiplied');
  eq(candidate.p.given.every(stack => stack.count <= 64), true, 'actual coin delivery never exceeds stack size');
}
for (const dropped of [false, true]) {
  const candidate = rewardSetup({ marketDiscount: 60 });
  candidate.sandbox.ftwwRollOutcome = () => ({ type: 'food', item: 'minecraft:bread', min: 1, max: 1 });
  const payment = dropped ? makeEntity({ id: 'minecraft:emerald', count: 2, x: 101, y: 64, z: 101, level: candidate.lv, owner: candidate.p }) : candidate.p.inventory.add('minecraft:emerald', 2);
  if (dropped) { spawn(candidate, payment); ticks(candidate, 2); }
  else rcBlock(candidate, candidate.p, payment);
  eq((dropped ? payment.item : payment).count, 1, 'food wish consumes exactly one emerald, drop=' + dropped);
  eq(givenFoodValue(candidate, 60) >= 1000, true, 'actual delivered emerald-food value meets floor, drop=' + dropped);
  eq(new Set(candidate.p.given.map(stack => stack.id)).size, 3, 'actual emerald-food deliveries have three distinct dishes');
}
{
  const candidate = rewardSetup(); unlockRewards(candidate); candidate.p.shift = true;
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 64);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 0, 'bulk 64-pot food wishes consume exactly 64 pots');
  eq(prog(candidate, candidate.p.uuid).goldPots, 64, 'bulk food rewards preserve independent per-pot counting');
  eq(givenFoodValue(candidate) >= 64000, true, 'bulk actual food delivery meets 1000-per-wish guarantee');
  eq(candidate.priceCalls, 32, 'bulk pricing is resolved once for the batch, not 2048 repeated market lookups');
  eq(candidate.nativeWishCalls, 1, 'bulk native roll called once');
  eq(candidate.p.given.every(stack => !foodIds.has(stack.id) || stack.count <= 16), true, 'bulk food split to safe stack sizes');
  eq(candidate.p.droppedRewards.length > 0, true, 'bulk overflow explicitly drops instead of disappearing');
}
{
  const candidate = rewardSetup({ rng: () => 0 }); unlockRewards(candidate); fillMain(candidate, 'minecraft:stone', 64);
  const payment = makeEntity({ id: 'farmingtales:gold_bucket', count: 1, x: 101, y: 64, z: 101, level: candidate.lv, owner: candidate.p });
  spawn(candidate, payment); ticks(candidate, 2);
  eq(payment.removed, true, 'full-bag water wish commits one pot');
  eq(givenFoodValue(candidate) >= 10000, true, 'food jackpot remains >=10000 with completely full backpack');
  eq(candidate.p.droppedRewards.reduce((total, stack) => total + stack.count, 0), givenCount(candidate), 'every overflow reward actually drops, no prize is lost');
  eq(countMain(candidate, 'minecraft:stone'), 36 * 64, 'jackpot does not overwrite existing inventory');
}
section('Q06 pre-payment validation, missing prices/foods and save-failure refunds');
for (const options of [{ priceAPIMissing: true }, { priceOverrides: {} }, { missingFoods: [...foodIds].slice(2) }]) {
  for (const currency of ['minecraft:emerald', 'farmingtales:gold_bucket']) {
    const candidate = rewardSetup(options); unlockRewards(candidate);
    candidate.sandbox.ftwwRollOutcome = () => ({ type: 'food', item: 'minecraft:bread', min: 1, max: 1 });
    const before = candidate.raw.get(key(candidate, candidate.p.uuid));
    const payment = candidate.p.inventory.add(currency, 1);
    rcBlock(candidate, candidate.p, payment);
    eq(payment.count, 1, 'unverifiable food fails before charging ' + currency);
    eq(candidate.p.given.length, 0, 'unverifiable food grants no substitute or invisible reward');
    eq(candidate.raw.get(key(candidate, candidate.p.uuid)), before, 'rejected food never overwrites progress');
  }
}
for (const currency of ['minecraft:emerald', 'farmingtales:gold_bucket']) {
  const candidate = rewardSetup(); unlockRewards(candidate); candidate.server.failSave = true;
  candidate.sandbox.ftwwRollOutcome = () => ({ type: 'food', item: 'minecraft:bread', min: 1, max: 1 });
  const payment = candidate.p.inventory.add(currency, 1); rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 1, 'food-plan save failure refunds ' + currency);
  eq(candidate.p.given.length, 0, 'failed save never delivers a free food jackpot');
}
section('Q07 safe no-reward branch and accurate player tooltip');
{
  const candidate = rewardSetup({ rng: () => 0 });
  candidate.sandbox.ftwwResolveWish(rewardEvent(candidate), 'none', { type: 'nothing' });
  eq(candidate.p.given.length, 0, 'nothing remains no reward, not multiplied into an extra outcome');
  eq(candidate.p.messages.some(message => message.includes('中大奖')), false, 'nothing cannot announce a jackpot');
  loadTooltip(candidate); const lines = [];
  candidate.handlers.tooltip({ addAdvanced: (id, callback) => callback({}, {}, { add: line => lines.push(String(line)) }) });
  eq(lines.length <= 6, true, 'core tooltip remains a short introduction rather than a reward manual');
  eq(lines.some(line => /蓝图|搭建|xyj\.nbt|Y[+\-]?\d/.test(line)), false, 'core tooltip has no blueprint or construction text');
  eq(lines.some(line => line.includes('绿宝石许愿')), true, 'short introduction still explains the basic wish');
}
