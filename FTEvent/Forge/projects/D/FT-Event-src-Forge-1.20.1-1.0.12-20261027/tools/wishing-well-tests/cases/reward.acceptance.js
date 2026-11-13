section('Q01 same public wish entry against candidate and previous unmodified script');
{
  const candidate = rewardSetup();
  candidate.sandbox.ftwwResolveWish(rewardEvent(candidate), 'acceptance', { type: 'wood', item: 'minecraft:oak_log', min: 2, max: 2 });
  eq(givenCount(candidate, 'minecraft:oak_log'), 6, 'actual delivered ordinary reward is three times the original quantity');
}
{
  const candidate = rewardSetup({ rng: () => 0 });
  candidate.sandbox.ftwwResolveWish(rewardEvent(candidate), 'acceptance', { type: 'wood', item: 'minecraft:oak_log', min: 2, max: 2 });
  eq(givenCount(candidate, 'minecraft:oak_log'), 60, 'actual jackpot delivery is thirty times the original quantity');
  eq(candidate.p.messages.some(message => message.includes('中大奖')), true, 'actual jackpot is announced, not only configured');
}
{
  const candidate = rewardSetup();
  candidate.sandbox.ftwwResolveWish(rewardEvent(candidate), 'acceptance', { type: 'food', item: 'minecraft:bread', min: 1, max: 1 });
  eq(new Set(candidate.p.given.map(stack => stack.id)).size, 3, 'food wish delivers three different prepared dishes');
  eq(givenFoodValue(candidate) >= 1000, true, 'actual delivered food is worth at least 1000 using real shipping prices');
}
