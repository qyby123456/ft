section('Q01 same public wish entry against candidate and previous unmodified script');
{
  const candidate = rewardSetup();
  candidate.sandbox.ftwwResolveWish(rewardEvent(candidate), 'acceptance', { type: 'wood', item: 'minecraft:oak_log', min: 2, max: 2 });
  eq(givenCount(candidate, 'minecraft:oak_log'), 2, 'emerald wishes deliver the original ordinary quantity');
}
{
  const candidate = rewardSetup({ rng: () => 0 });
  candidate.sandbox.ftwwResolveWish(rewardEvent(candidate), 'acceptance', { type: 'wood', item: 'minecraft:oak_log', min: 2, max: 2 });
  eq(givenCount(candidate, 'minecraft:oak_log'), 2, 'emerald wishes do not get the gold-pot jackpot even at random zero');
  eq(candidate.p.messages.some(message => message.includes('中大奖')), false, 'emerald wishes do not announce a gold-pot jackpot');
}
{
  const candidate = rewardSetup();
  candidate.sandbox.ftwwResolveWish(rewardEvent(candidate), 'acceptance', { type: 'food', item: 'minecraft:bread', min: 1, max: 1 });
  eq(givenCount(candidate, 'minecraft:bread'), 1, 'emerald food stays the selected original reward');
  eq(candidate.priceCalls, 0, 'emerald food has no gold-pot minimum-value requirement');
}
