section('E01 emerald pool quantities stay isolated at every level and payment entry');
for (const [type, id, count] of [
  ['junk', 'minecraft:cobblestone', 5], ['wood', 'minecraft:oak_log', 4],
  ['food', 'farmersdelight:kelp_roll', 3], ['poop', 'kubejs:poop', 3],
  ['coin', 'kubejs:coin', 16], ['seed_bag', 'farmingtales:carrot_packet', 1],
  ['mineral', 'minecraft:diamond', 2], ['emerald', 'minecraft:emerald', 8],
  ['geode', 'farmingtales:geode', 1]
]) {
  for (const level of [0, 3]) {
    for (const random of [0, 0.5]) {
      for (const dropped of [false, true]) {
        const candidate = rewardSetup({ rng: () => random });
        putRaw(candidate, candidate.p.uuid, JSON.stringify({ emeralds: 0, goldPots: [0, 1, 10, 100][level], emeraldBlocks: 0 }));
        eq(candidate.sandbox.ftwwSafeLevel(candidate.server, candidate.p), level, 'emerald isolation fixture uses level ' + level);
        candidate.sandbox.ftwwRollOutcome = () => ({ type, item: id, min: count, max: count });
        const payment = dropped
          ? makeEntity({ id: 'minecraft:emerald', count: 2, x: 101, y: 64, z: 101, level: candidate.lv, owner: candidate.p })
          : candidate.p.inventory.add('minecraft:emerald', 2);
        if (dropped) { spawn(candidate, payment); ticks(candidate, 2); }
        else rcBlock(candidate, candidate.p, payment);
        eq((dropped ? payment.item : payment).count, !dropped && id === 'minecraft:emerald' ? 1 + count : 1, 'emerald isolation consumes one payment and allows returned emeralds to merge for ' + type);
        eq(givenCount(candidate, id), count, 'original emerald ' + type + ' quantity at level ' + level + ', random=' + random + ', drop=' + dropped);
        eq(givenCount(candidate), count, 'emerald rewards contain no additional gold-pot items');
        eq(prog(candidate, candidate.p.uuid).emeralds, 1, 'emerald isolation preserves wish progress');
        eq(candidate.p.messages.some(message => message.includes('中大奖') || message.includes('丰盛料理礼包')), false, 'emerald wishes never announce gold-pot bonuses');
        eq(candidate.priceCalls, 0, 'emerald wishes do not consult gold-pot food prices');
        eq(candidate.nativeWishCalls, 0, 'emerald wishes never roll the gold-pot native pool');
      }
    }
  }
}
section('E02 only explicitly marked gold-pot preparation enables reward bonuses');
for (const preparation of [undefined, {}, { goldPot: false }, { foods: [] }]) {
  const candidate = rewardSetup();
  candidate.sandbox.Math.random = () => { throw new Error('Emerald reward must not roll a gold-pot jackpot'); };
  const stack = candidate.sandbox.Item.of('farmersdelight:kelp_roll', 3);
  stack.nbt = { OriginalFood: true };
  const plan = candidate.sandbox.ftwwPrepareWishReward(rewardEvent(candidate), { type: 'food' }, stack, preparation);
  eq(plan.rewards[0].stack.count, 3, 'unmarked preparation keeps original food quantity');
  eq(plan.rewards[0].stack.nbt.OriginalFood, true, 'unmarked preparation preserves original NBT');
  eq(plan.jackpot, false, 'unmarked preparation cannot win a gold-pot jackpot');
  eq(plan.foodValue, 0, 'unmarked preparation has no gold-pot food-value floor');
}
section('E03 emerald food stays available without gold-pot pricing or catalog');
for (const options of [{ priceAPIMissing: true }, { priceOverrides: {} }, { missingFoods: [...foodIds] }]) {
  for (const dropped of [false, true]) {
    const candidate = rewardSetup(options);
    candidate.sandbox.ftwwRollOutcome = () => ({ type: 'food', item: 'minecraft:bread', min: 1, max: 1 });
    const payment = dropped
      ? makeEntity({ id: 'minecraft:emerald', count: 2, x: 101, y: 64, z: 101, level: candidate.lv, owner: candidate.p })
      : candidate.p.inventory.add('minecraft:emerald', 2);
    if (dropped) { spawn(candidate, payment); ticks(candidate, 2); }
    else rcBlock(candidate, candidate.p, payment);
    eq((dropped ? payment.item : payment).count, 1, 'emerald food payment ignores unavailable gold-pot pricing');
    eq(givenCount(candidate, 'minecraft:bread'), 1, 'emerald food delivers its original item without gold-pot catalog');
    eq(candidate.priceCalls, 0, 'emerald food never requires the gold-pot price API');
  }
}
