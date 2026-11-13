section('C01 exact six-tier gold-pot payouts through real payment and delivery');
for (const random of [0, 0.5]) {
  const candidate = rewardSetup({ rng: () => random });
  candidate.sandbox.ftwwResolveWish(rewardEvent(candidate), 'emerald-regression', { type: 'coin', item: 'kubejs:coin', min: 10, max: 10 });
  eq(givenCount(candidate, 'kubejs:coin'), random === 0 ? 300 : 30, 'emerald coin wishes retain existing multiplier and jackpot');
}
for (const [amount, id, count] of [
  [1, 'kubejs:coin', 1], [100, 'farmingtales:gold_bundle', 1],
  [1000, 'farmingtales:gold_chest', 1], [10000, 'farmingtales:gold_bucket', 1],
  [100000, 'farmingtales:gold_bucket', 10], [1000000, 'farmingtales:gold_bucket', 100]
]) {
  for (const random of [0, 0.5]) {
    for (const dropped of [false, true]) {
      const candidate = rewardSetup({ category: 'coin', rewardId: id, baseCount: count, rng: () => random });
      unlockRewards(candidate);
      if (dropped) {
        const payment = makeEntity({ id: 'farmingtales:gold_bucket', count: 1, x: 101, y: 64, z: 101, level: candidate.lv, owner: candidate.p });
        spawn(candidate, payment); ticks(candidate, 2);
        eq(payment.removed, true, 'coin water donation pays exactly one pot');
      } else {
        const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 1);
        rcBlock(candidate, candidate.p, payment);
        eq(payment.count, 0, 'coin right-click donation pays exactly one pot');
      }
      eq(givenCount(candidate, id), count, 'exact payout for ' + amount + ' coins, random=' + random);
      eq(candidate.p.given.every(stack => stack.count <= 64), true, 'million-coin prizes split into legal stacks');
      eq(candidate.p.messages.some(message => message.includes('中大奖')), false, 'coin payout never gains quantity jackpot');
      eq(prog(candidate, candidate.p.uuid).goldPots, 1, 'coin donation progress is saved once');
    }
  }
}
{
  const candidate = rewardSetup({ category: 'coin', rewardId: 'farmingtales:gold_bucket', baseCount: 100, rng: () => 0 });
  unlockRewards(candidate); candidate.p.shift = true;
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 64);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 0, 'bulk coins consume 64 pots');
  eq(givenCount(candidate, 'farmingtales:gold_bucket'), 6400, 'bulk coin rewards are exact, not multiplied');
  eq(candidate.nativeWishCalls, 1, 'bulk native draw and config read occur once');
  eq(candidate.p.droppedRewards.length > 0, true, 'bulk full-inventory overflow is delivered as drops');
}
{
  const candidate = rewardSetup({ category: 'coin', rewardId: 'farmingtales:gold_bucket', baseCount: 100 });
  unlockRewards(candidate); candidate.server.failSave = true;
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 1);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 1, 'failed coin progress save refunds the pot');
  eq(candidate.p.given.length, 0, 'failed coin save delivers no free payout');
}
{
  const candidate = rewardSetup(); unlockRewards(candidate);
  const originalLoader = candidate.sandbox.Java.loadClass;
  candidate.sandbox.Java.loadClass = name => name === 'cn.farmingtales.qysevent.wishingwell.GoldPotWishes'
    ? { rollUnlockedRewards() { throw new Error('Invalid external coin config'); } } : originalLoader(name);
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 1);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 1, 'invalid coin config fails before payment');
  eq(candidate.p.given.length, 0, 'invalid config grants no substitute reward');
}
