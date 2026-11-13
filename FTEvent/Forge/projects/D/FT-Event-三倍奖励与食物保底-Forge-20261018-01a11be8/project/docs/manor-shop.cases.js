function manorShopSetup(options) {
  const candidate = create(Object.assign({ nativeInventoryLifecycle: true }, options || {})); load(candidate);
  candidate.lv = world(candidate);
  candidate.p = player(candidate, { uuid: 'manor-shop', x: 100.5, y: 64.5, z: 100.5 });
  candidate.block = coreBlock(candidate, candidate.p);
  putRaw(candidate, candidate.p.uuid, JSON.stringify({ emeralds: 0, goldPots: 0, emeraldBlocks: 64 }));
  candidate.wallet.balances.set(candidate.p.uuid, 5000000);
  return candidate;
}
function buyManorSpell(candidate) {
  return candidate.sandbox.ftwwPurchase({ player: candidate.p, level: candidate.lv, block: candidate.block, server: candidate.server }, 'spell');
}
function manorSpellButton(candidate) {
  return candidate.p.gui.buttons.find(button => String(button.text.getString()).includes('500 万'));
}
section('M02 manor-only direct purchase, enabled catalog, legal randomized levels and exact price');
{
  const candidate = manorShopSetup({ rejectGeneralRegistry: true, rng: () => 0 });
  eq(buyManorSpell(candidate), true, 'manor purchase succeeds without reading general spell registry');
  eq(candidate.wallet.balances.get(candidate.p.uuid), 0, 'exactly 5 million SDM is debited');
  eq(countMain(candidate, 'irons_spellbooks:scroll'), 1, 'paid manor scroll reaches real backpack');
  const scroll = candidate.p.inventory.stacks.find(stack => stack.id === 'irons_spellbooks:scroll' && stack.count === 1);
  eq(scroll.nbt.spellId, 'ftboss:effigy_volley', 'scroll container carries manor spell ID, not just a renamed tooltip');
  eq(scroll.nbt.level, 1, 'minimum legal manor level can roll');
  eq(candidate.wallet.set, 1, 'direct purchase debits once');
  eq(K(candidate, 'FTWW_WEAPON_PRICE'), 50000000, 'weapon price is not changed');
}
{
  const candidate = create({ rng: () => 0.999999 }); load(candidate);
  const scroll = candidate.sandbox.ftwwRandomSpell();
  eq(scroll.scroll.spellId, 'ftboss:candle_reap', 'disabled manor spell is excluded from selection');
  eq(scroll.scroll.level, 3, 'maximum legal manor level can roll');
}
{
  const candidate = manorShopSetup(); candidate.wallet.balances.set(candidate.p.uuid, 4999999);
  eq(buyManorSpell(candidate), false, 'less than 5 million cannot buy');
  eq(candidate.wallet.set, 0, 'insufficient funds do not debit');
  eq(countMain(candidate, 'irons_spellbooks:scroll'), 0, 'insufficient funds grant no scroll');
}
section('M03 absent, disabled, foreign or malformed manor pools fail before money moves');
for (const options of [
  { manorCatalogMissing: true },
  { manorKindsMissing: true },
  { manorSpells: [] },
  { manorSpells: [{ id: 'ftboss:effigy_volley', enabled: false, min: 1, max: 5 }] },
  { manorSpells: [{ id: 'irons_spellbooks:fireball', enabled: true, min: 1, max: 5 }] },
  { manorSpells: [{ id: 'other_mod:spell', enabled: true, min: 1, max: 5 }] },
  { manorSpells: [{ id: 'ftboss:effigy_volley', enabled: true, min: 0, max: 5 }] },
  { manorSpells: [{ id: 'ftboss:effigy_volley', enabled: true, min: 3, max: 2 }] },
  { manorSpells: [{ id: 'ftboss:effigy_volley', enabled: true, min: 1, max: 1.5 }] }
]) {
  const candidate = manorShopSetup(options);
  eq(buyManorSpell(candidate), false, 'invalid manor pool is rejected: ' + JSON.stringify(options));
  eq(candidate.wallet.balances.get(candidate.p.uuid), 5000000, 'invalid pool preserves the full wallet');
  eq(candidate.wallet.set, 0, 'invalid pool does not invoke debit or refund as a substitute for validation');
  eq(countMain(candidate, 'irons_spellbooks:scroll'), 0, 'invalid pool grants no regular-spell fallback');
}
section('M04 actual queued GUI flow: restore inventory first, then pay and deliver manor NBT');
{
  const candidate = manorShopSetup(); candidate.p.inventory.add('minecraft:stone', 64);
  openShopGui(candidate);
  const button = manorSpellButton(candidate);
  eq(String(button.text.getString()).includes('庄园'), true, 'actual shop button names the manor-only product');
  eq(String(button.text.getString()).includes('不含常规铁魔法'), true, 'actual shop button excludes regular Iron spells');
  eq(countMain(candidate, 'minecraft:stone'), 0, 'native menu captures original real inventory');
  button.click({ setHandled() {} });
  button.click({ setHandled() {} });
  eq(candidate.wallet.balances.get(candidate.p.uuid), 5000000, 'click tick does not debit before native restore');
  eq(countMain(candidate, 'irons_spellbooks:scroll'), 0, 'click tick never inserts into captured inventory');
  eq(candidate.p.openInventory, candidate.server.inventoryMenu, 'purchase request closes custom menu');
  ticks(candidate, 1);
  eq(candidate.server.restoreCounts.get(candidate.p.uuid), 1, 'native restore executes once before shop queue');
  eq(countMain(candidate, 'minecraft:stone'), 64, 'original stack survives queued manor purchase');
  eq(countMain(candidate, 'irons_spellbooks:scroll'), 1, 'queued purchase delivers one manor scroll');
  eq(candidate.wallet.balances.get(candidate.p.uuid), 0, 'queued purchase charges exactly 5 million');
  const scroll = candidate.p.inventory.stacks.find(stack => stack.id === 'irons_spellbooks:scroll' && stack.count === 1);
  eq(scroll.nbt.spellId.startsWith('ftboss:'), true, 'queued manor spell NBT survives ItemStack wrapping and restore');
  eq(scroll.nbt.spellContainer, true, 'queued reward has a genuine validated spell container');
  ticks(candidate, 8);
  eq(countMain(candidate, 'irons_spellbooks:scroll'), 1, 'scroll neither disappears nor duplicates on later ticks');
  eq(candidate.wallet.set, 1, 'double click cannot cause a second payment');
}
{
  const candidate = manorShopSetup(); fillMain(candidate, 'minecraft:stone', 64);
  openShopGui(candidate); manorSpellButton(candidate).click({ setHandled() {} }); ticks(candidate, 1);
  eq(candidate.wallet.balances.get(candidate.p.uuid), 5000000, 'full restored main backpack refuses purchase without charge');
  eq(countMain(candidate, 'irons_spellbooks:scroll'), 0, 'full backpack does not claim invisible scroll delivery');
  eq(countMain(candidate, 'minecraft:stone'), 36 * 64, 'full backpack remains intact');
}
{
  const candidate = manorShopSetup(); candidate.p.inventory.add('farmingtales:gold_bucket', 1);
  const summonId = K(candidate, 'FTWW_SUMMONS')[0];
  openShopGui(candidate);
  candidate.p.gui.buttons.find(button => button.stack.id === summonId).click({ setHandled() {} });
  ticks(candidate, 1);
  eq(countMain(candidate, summonId), 1, 'summon purchase queue remains functional');
  eq(countMain(candidate, 'farmingtales:gold_bucket'), 0, 'summon price remains one pot');
  eq(candidate.wallet.balances.get(candidate.p.uuid), 5000000, 'summon does not charge SDM');
}
{
  const candidate = manorShopSetup(); candidate.wallet.balances.set(candidate.p.uuid, 50000000);
  openShopGui(candidate);
  candidate.p.gui.buttons.find(button => String(button.text.getString()).includes('5000 万')).click({ setHandled() {} });
  ticks(candidate, 1);
  eq(candidate.wallet.balances.get(candidate.p.uuid), 0, 'weapon price and queued payment remain 50 million');
  const weapons = candidate.p.inventory.stacks.filter(stack => stack.id.startsWith('ftboss:') && stack.count > 0);
  eq(weapons.length, 1, 'weapon queue still delivers a manor weapon');
  eq(weapons.some(stack => stack.id === 'ftboss:gourmet_blank_blade'), false, 'weapon shop still excludes gourmet blank blade');
}
section('M05 tooltip and manor catalog membership, not just namespace filtering');
{
  const candidate = create(); loadTooltip(candidate); const lines = [];
  candidate.handlers.tooltip({ addAdvanced: (id, callback) => callback({}, {}, { add: line => lines.push(String(line)) }) });
  eq(lines.some(line => line.includes('500 万') && line.includes('庄园法术卷轴') && line.includes('不含常规铁魔法')), true, 'tooltip documents exact new product and unchanged price');
}
{
  const candidate = create({ spells: [{ id: 'ftboss:not_a_manor_spell', lootable: true, min: 1, max: 5 }], rng: () => 0 }); load(candidate);
  eq(candidate.sandbox.ftwwRandomSpell().scroll.spellId, 'ftboss:effigy_volley', 'unrelated ftboss-namespace spells in general registry cannot enter manor catalog pool');
}
section('M06 dynamically enumerated native Boss catalog, not a hard-coded two-spell pool');
{
  const ids = JSON.parse(fs.readFileSync(path.join(ROOT, 'audit', 'native-manor-spell-ids.json'), 'utf8').replace(/^\uFEFF/, ''));
  const definitions = ids.map(id => ({ id, enabled: true, min: 1, max: 5 }));
  const candidate = create({ manorSpells: definitions, rejectGeneralRegistry: true, rng: mulberry32(3518) }); load(candidate);
  const seen = new Set();
  for (let index = 0; index < 4000; index++) seen.add(candidate.sandbox.ftwwRandomSpell().scroll.spellId);
  eq([...seen].sort().join(','), [...ids].sort().join(','), 'all current native manor catalog entries can roll and no others appear');
  eq(seen.size, ids.length, 'script does not hard-code the manor spell count or IDs');
}
