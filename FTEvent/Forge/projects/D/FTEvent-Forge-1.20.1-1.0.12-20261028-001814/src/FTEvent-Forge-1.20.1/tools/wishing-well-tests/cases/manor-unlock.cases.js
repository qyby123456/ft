function manorEvent(candidate, actor) {
  return { server: candidate.server, player: actor || candidate.p, level: candidate.lv, block: candidate.block };
}
section('U01 both permanent unlocks are required, in either acquisition order');
for (const fullWell of [false, true]) {
  for (const contract of [false, true]) {
    const candidate = manorShopSetup({ crimsonContract: contract });
    putRaw(candidate, candidate.p.uuid, JSON.stringify({ emeralds: 0, goldPots: 0, emeraldBlocks: fullWell ? 64 : 63 }));
    eq(candidate.sandbox.ftwwManorShopUnlocked(manorEvent(candidate)), fullWell && contract,
      'AND gate: fullWell=' + fullWell + ' contract=' + contract);
    openShopGui(candidate);
    if (!fullWell) {
      eq(candidate.p.guiOpens, 0, 'no shop before the personal well unlock');
      continue;
    }
    eq(candidate.p.gui.buttons.filter(button => button.y === 3).length, contract ? 2 : 0,
      'locked manor entries are absent, not disabled buttons');
    eq(candidate.p.gui.buttons.length, contract ? 14 : 12, 'eleven summons and progress survive the manor gate');
  }
}
section('U02 hidden weapon and spell purchases cannot be forged or queued');
for (const kind of ['weapon', 'spell']) {
  const candidate = manorShopSetup({ crimsonContract: false });
  candidate.wallet.balances.set(candidate.p.uuid, 100000000);
  eq(candidate.sandbox.ftwwPurchase(manorEvent(candidate), kind), false, 'direct purchase rejected: ' + kind);
  eq(candidate.sandbox.ftwwQueuePurchase(manorEvent(candidate), kind), false, 'queue request rejected: ' + kind);
  eq(candidate.sandbox.global.ftwwPurchaseQueue.length, 0, 'rejected order never enters queue: ' + kind);
  eq(candidate.wallet.get + candidate.wallet.set, 0, 'locked request never reads or debits money: ' + kind);
  eq(candidate.wallet.balances.get(candidate.p.uuid), 100000000, 'locked request leaves balance intact: ' + kind);
}
section('U03 old GUI callbacks and already queued orders recheck current qualification');
for (const revokeBeforeQueue of [false, true]) {
  const candidate = manorShopSetup(); openShopGui(candidate);
  const button = manorSpellButton(candidate);
  if (revokeBeforeQueue) candidate.crimsonContracts.set(candidate.p.uuid, false);
  button.click({ setHandled() {} });
  candidate.crimsonContracts.set(candidate.p.uuid, false);
  ticks(candidate, 2);
  eq(candidate.wallet.balances.get(candidate.p.uuid), 5000000, 'stale callback or order does not debit');
  eq(countMain(candidate, 'irons_spellbooks:scroll'), 0, 'stale callback or order delivers no scroll');
  eq(candidate.sandbox.global.ftwwPurchaseQueue.length, 0, 'stale order is discarded');
}
section('U04 qualifying player unlocks permanently, without blocking another player or summons');
{
  const candidate = manorShopSetup({ crimsonContract: false });
  candidate.crimsonContracts.set(candidate.p.uuid, true);
  const other = player(candidate, { uuid: 'other-manor-player', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(candidate, other.uuid, JSON.stringify({ emeralds: 0, goldPots: 0, emeraldBlocks: 64 }));
  eq(candidate.sandbox.ftwwManorShopUnlocked(manorEvent(candidate)), true, 'recorded acquisition qualifies without carrying a weapon');
  eq(candidate.sandbox.ftwwManorShopUnlocked(manorEvent(candidate, other)), false, 'another player does not inherit qualification');
  eq(buyManorSpell(candidate), true, 'qualified player still buys the real manor scroll');
}
{
  const candidate = manorShopSetup({ crimsonContract: false });
  candidate.p.inventory.add('farmingtales:gold_bucket', 2);
  eq(buySummon(candidate), true, 'summons do not require crimson contract');
  eq(countMain(candidate, 'farmingtales:gold_bucket'), 0, 'summon price remains two pots');
}
section('U05 missing or unreadable acquisition service fails closed without breaking the base shop');
for (const options of [{ manorAccessMissing: true }, { manorAccessThrows: true }]) {
  const candidate = manorShopSetup(options); openShopGui(candidate);
  eq(candidate.p.gui.buttons.filter(button => button.y === 3).length, 0, 'lookup failure hides manor purchase entries');
  candidate.p.closeContainer(); ticks(candidate, 1);
  eq(buyManorSpell(candidate), false, 'lookup failure cannot authorize a direct purchase');
  eq(candidate.wallet.set, 0, 'lookup failure never debits money');
}
