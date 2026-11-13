section('E01 real right-click acceptance, shared with the unmodified 1.0.7 negative control');
{
  const candidate = create(); load(candidate); world(candidate);
  const actor = player(candidate, { uuid: 'locked', x: 100.5, y: 64.5, z: 100.5 });
  const payment = actor.inventory.add('farmingtales:gold_bucket', 1);
  rcBlock(candidate, actor, payment);
  eq(payment.count, 1, 'before emerald-block unlock a gold pot is never consumed');
  eq(prog(candidate, actor.uuid), null, 'rejected gold donation writes no progress');
}
{
  const candidate = create(); load(candidate); world(candidate);
  const actor = player(candidate, { uuid: 'upgrade', x: 100.5, y: 64.5, z: 100.5, shift: true });
  const payment = actor.inventory.add('minecraft:emerald_block', 64);
  const click = rcBlock(candidate, actor, payment);
  eq(click.cancelled, true, 'emerald-block upgrade cancels vanilla block placement');
  eq(payment.count, 0, 'shift right-click pays exactly one stack of emerald blocks');
  const progress = prog(candidate, actor.uuid);
  eq(progress && progress.emeraldBlocks, 64, '64 paid blocks are persisted for this player');
  eq(progress && progress.goldPots, 0, 'upgrade requires no gold pots');
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, actor), 3, 'one emerald-block stack reaches the unlocked level');
  eq(actor.guiOpens, 1, 'the newly unlocked shop opens');
}
