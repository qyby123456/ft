section('U01 exactly one upgrade at 100 personal gold pots');
{
  const candidate = create(); load(candidate); world(candidate);
  const actor = player(candidate, { uuid: 'upgrade', x: 100.5, y: 64.5, z: 100.5, shift: true });
  const first = actor.inventory.add('farmingtales:gold_bucket', 64);
  rcBlock(candidate, actor, first);
  eq(first.count, 0, 'first 64 pots are paid toward the one-time upgrade');
  eq(prog(candidate, actor.uuid).upgradePots, 64, 'partial payment is persisted');
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, actor), 0, '64 pots do not create an intermediate level');
  eq(actor.guiOpens, 0, 'partial payment cannot open the shop');
  eq(actor.given.length, 0, 'upgrade pots do not roll rewards');
  ticks(candidate, 12);
  const second = actor.inventory.add('farmingtales:gold_bucket', 64);
  rcBlock(candidate, actor, second);
  eq(second.count, 28, 'only the remaining 36 pots are charged');
  eq(prog(candidate, actor.uuid).upgradePots, 100, 'total upgrade payment is exactly 100');
  eq(prog(candidate, actor.uuid).goldPots, 0, 'upgrade payment does not increment the wish counter');
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, actor), 3, '100 pots jump directly to full access');
  eq(actor.guiOpens, 1, 'completed upgrade opens the shop once');
  eq(actor.given.length, 0, 'excess pots are returned, not silently rolled');
}
