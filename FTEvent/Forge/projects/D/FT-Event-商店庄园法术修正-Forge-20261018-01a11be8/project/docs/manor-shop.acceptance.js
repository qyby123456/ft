section('M01 same manor-only acceptance against candidate and unmodified prior script');
{
  const candidate = create({ rng: mulberry32(18423) }); load(candidate);
  const allowed = new Map([['ftboss:effigy_volley', [1, 5]], ['ftboss:candle_reap', [2, 3]]]);
  const seen = new Set();
  let foreign = 0;
  let invalidLevel = 0;
  for (let index = 0; index < 4000; index++) {
    const scroll = candidate.sandbox.ftwwRandomSpell();
    const spellId = scroll.scroll.spellId;
    seen.add(spellId);
    const range = allowed.get(spellId);
    if (!range) foreign++;
    if (range && (scroll.scroll.level < range[0] || scroll.scroll.level > range[1])) invalidLevel++;
  }
  eq(foreign, 0, '5m shop samples contain only enabled manor spells, never ordinary Iron spells');
  eq(invalidLevel, 0, 'manor scroll levels stay within each spell range');
  eq([...seen].sort().join(','), [...allowed.keys()].sort().join(','), 'all enabled catalog spells can roll even when allowLooting is false');
  eq(candidate.manorCatalogCalls.length > 0, true, 'real shop function consults manor catalog');
}
{
  const candidate = create({ manorSpells: [] }); load(candidate); world(candidate);
  const actor = player(candidate, { uuid: 'no-manor', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(candidate, actor.uuid, JSON.stringify({ emeralds: 0, goldPots: 0, emeraldBlocks: 64 }));
  candidate.wallet.balances.set(actor.uuid, 5000000);
  const purchased = candidate.sandbox.ftwwPurchase({ server: candidate.server, player: actor, level: candidate.level, block: coreBlock(candidate, actor) }, 'spell');
  eq(purchased, false, 'empty manor pool cannot fall back to regular Iron spells');
  eq(candidate.wallet.balances.get(actor.uuid), 5000000, 'empty manor pool does not debit 5 million');
  eq(actor.given.length, 0, 'empty manor pool generates no general-registry reward');
}
