function upgradeSetup(options) {
  options = options || {};
  const candidate = create(options);
  const originalLoader = candidate.sandbox.Java.loadClass;
  candidate.unlockedCalls = [];
  candidate.sandbox.Java.loadClass = name => {
    if (name !== 'cn.farmingtales.qysevent.wishingwell.GoldPotWishes') return originalLoader(name);
    if (options.goldPoolMissing) throw new Error('GoldPotWishes missing (test)');
    return {
      rollRewards() { throw new Error('Legacy hundred-pot quota API must not be used'); },
      rollUnlockedRewards(level, amount) {
        candidate.unlockedCalls.push({ level, amount });
        const rewards = [];
        for (let index = 0; index < amount; index++) {
          rewards.push({ category: () => 'food', stack: () => new JavaStack('minecraft:golden_apple', options.emptyReward ? 0 : 1, { GoldPoolRoll: index }) });
        }
        return options.goldPoolMode === 'short' ? rewards.slice(1) : rewards;
      }
    };
  };
  load(candidate);
  candidate.lv = world(candidate, options.world);
  candidate.p = player(candidate, { uuid: 'emerald-player', x: 100.5, y: 64.5, z: 100.5 });
  candidate.block = coreBlock(candidate, candidate.p);
  return candidate;
}
function upgradeEvent(candidate) {
  return { server: candidate.server, level: candidate.lv, player: candidate.p, block: candidate.block };
}
function savedUpgrade(candidate, emeraldBlocks, goldPots) {
  putRaw(candidate, candidate.p.uuid, JSON.stringify({ emeralds: 0, goldPots: goldPots || 0, emeraldBlocks }));
}
function upgradeDrop(candidate, id, count, owner) {
  return makeEntity({ id, count, x: 101, y: 64, z: 101, level: candidate.lv, owner });
}
section('E02 cumulative payment, excess blocks, cooldown, offhand, structure');
{
  const candidate = upgradeSetup();
  const payment = candidate.p.inventory.add('minecraft:emerald_block', 64);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 63, 'ordinary right-click pays one upgrade block');
  eq(prog(candidate, candidate.p.uuid).emeraldBlocks, 1, 'partial upgrade is saved');
  eq(candidate.p.guiOpens, 0, 'partial progress does not unlock the shop');
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 63, 'same-tick repeated click cannot pay twice');
  ticks(candidate, 12);
  rcBlock(candidate, candidate.p, payment, 'OFF_HAND');
  eq(payment.count, 63, 'offhand upgrade is ignored');
  candidate.p.shift = true;
  rcBlock(candidate, candidate.p, payment);
  eq(prog(candidate, candidate.p.uuid).emeraldBlocks, 64, 'split payments reach 64');
  eq(candidate.p.guiOpens, 1, 'split payment unlock opens once');
  ticks(candidate, 12);
  const extra = new Stack('minecraft:emerald_block', 64);
  rcBlock(candidate, candidate.p, extra);
  eq(extra.count, 64, 'already unlocked players are not charged again');
}
{
  const candidate = upgradeSetup(); savedUpgrade(candidate, 63);
  candidate.p.shift = true;
  const payment = candidate.p.inventory.add('minecraft:emerald_block', 64);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 63, 'only remaining one block is charged, excess 63 retained');
  eq(prog(candidate, candidate.p.uuid).emeraldBlocks, 64, 'progress caps at required 64 blocks');
  const other = player(candidate, { uuid: 'other', x: 100.5, y: 64.5, z: 100.5 });
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, other), 0, 'unlock is per UUID, not shared by the well');
  const before = candidate.raw.get(key(candidate, candidate.p.uuid));
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, candidate.p), 3, 'persisted unlock is read back');
  eq(candidate.raw.get(key(candidate, candidate.p.uuid)), before, 'reading does not rewrite or reset progress');
}
{
  const candidate = upgradeSetup({ world: { overrides: { '-2,0,-2': 'minecraft:air' } } });
  candidate.p.shift = true;
  const payment = candidate.p.inventory.add('minecraft:emerald_block', 64);
  const click = rcBlock(candidate, candidate.p, payment);
  eq(click.cancelled, true, 'broken well still blocks vanilla placement of upgrade blocks');
  eq(payment.count, 64, 'incomplete structure cannot consume upgrade blocks');
  eq(prog(candidate, candidate.p.uuid), null, 'incomplete well cannot unlock');
}
section('E03 drop entry, ownership, excess, rollback and GUI failure');
{
  const candidate = upgradeSetup();
  const payment = upgradeDrop(candidate, 'minecraft:emerald_block', 64, candidate.p);
  spawn(candidate, payment); ticks(candidate, 2);
  eq(payment.removed, true, 'owned full upgrade stack is consumed from well water');
  eq(payment.discarded, 1, 'empty upgrade item entity discarded once');
  eq(prog(candidate, candidate.p.uuid).emeraldBlocks, 64, 'water upgrade persists all 64 blocks');
  eq(candidate.p.guiOpens, 1, 'water upgrade unlocks shop');
}
{
  const candidate = upgradeSetup();
  const payment = upgradeDrop(candidate, 'minecraft:emerald_block', 64, null);
  spawn(candidate, payment); ticks(candidate, 4);
  eq(payment.item.count, 64, 'ownerless upgrade stack cannot charge an arbitrary nearby player');
  eq(prog(candidate, candidate.p.uuid), null, 'ownerless upgrade writes no progress');
  payment.owner = candidate.p;
  candidate.p.level = { dimension: 'minecraft:the_nether' };
  ticks(candidate, 4);
  eq(payment.item.count, 64, 'owner in another dimension cannot donate');
}
{
  const candidate = upgradeSetup(); savedUpgrade(candidate, 63);
  const payment = upgradeDrop(candidate, 'minecraft:emerald_block', 64, candidate.p);
  spawn(candidate, payment); ticks(candidate, 2);
  eq(payment.item.count, 63, 'water upgrade preserves excess blocks');
  eq(payment.removed, false, 'partly consumed entity remains in world');
  eq(prog(candidate, candidate.p.uuid).emeraldBlocks, 64, 'partial water payment completes upgrade');
}
for (const dropped of [false, true]) {
  const candidate = upgradeSetup(); candidate.server.failSave = true;
  const entity = dropped ? upgradeDrop(candidate, 'minecraft:emerald_block', 64, candidate.p) : null;
  const payment = entity ? entity.item : candidate.p.inventory.add('minecraft:emerald_block', 64);
  eq(candidate.sandbox.ftwwDonateUpgradeBlocks(upgradeEvent(candidate), payment, 64, entity), false, 'save failure rejects upgrade');
  eq((entity ? entity.item : payment).count, 64, 'save failure restores every upgrade block, hand/drop=' + dropped);
  eq(candidate.p.guiOpens, 0, 'failed save cannot open an unlocked shop');
  eq(prog(candidate, candidate.p.uuid), null, 'failed save leaves progress untouched');
}
{
  const candidate = upgradeSetup({ goldPoolMissing: true }); candidate.p.throwOnGui = true;
  const payment = candidate.p.inventory.add('minecraft:emerald_block', 64);
  eq(candidate.sandbox.ftwwDonateUpgradeBlocks(upgradeEvent(candidate), payment, 64, null), true, 'GUI failure does not undo a committed upgrade');
  eq(payment.count, 0, 'committed upgrade charged once even without the gold pool');
  eq(prog(candidate, candidate.p.uuid).emeraldBlocks, 64, 'unlock survives GUI failure');
  candidate.p.throwOnGui = false;
  openShopGui(candidate);
  eq(candidate.p.guiOpens, 2, 'empty-hand click can reopen shop after GUI failure');
}
section('E04 first pot, bulk rolls, no legacy quota, genuine inventory lifecycle');
for (const previousPots of [0, 1, 99, 100]) {
  for (const amount of [1, 64]) {
    const candidate = upgradeSetup({ nativeInventoryLifecycle: true }); savedUpgrade(candidate, 64, previousPots);
    candidate.p.shift = amount > 1;
    const payment = candidate.p.inventory.add('farmingtales:gold_bucket', amount);
    rcBlock(candidate, candidate.p, payment);
    eq(payment.count, 0, 'unlocked gold payment is consumed: previous=' + previousPots + ' amount=' + amount);
    eq(candidate.unlockedCalls.length, 1, 'new native API invoked once');
    eq(candidate.unlockedCalls[0] && candidate.unlockedCalls[0].amount, amount, 'every pot, including the very first, gets a roll');
    eq(prog(candidate, candidate.p.uuid).goldPots, previousPots + amount, 'pot total persists without threshold arithmetic');
    eq(countMain(candidate, 'minecraft:golden_apple'), Math.min(36, amount), 'unique-NBT rewards fill the real inventory');
    eq(candidate.p.droppedRewards.reduce((count, stack) => count + stack.count, 0), Math.max(0, amount - 36), 'overflow unique-NBT rewards explicitly drop, none disappear');
    eq(candidate.p.given[0] && candidate.p.given[0].nbt.GoldPoolRoll, 0, 'reward NBT survives wrapping and delivery');
    eq(candidate.p.guiOpens, 0, 'gold rewards cannot be captured by a newly opened menu');
    ticks(candidate, 3);
    eq(countMain(candidate, 'minecraft:golden_apple') + candidate.p.droppedRewards.reduce((count, stack) => count + stack.count, 0), amount, 'all inventory and overflow rewards survive subsequent ticks');
  }
}
{
  const candidate = upgradeSetup({ nativeInventoryLifecycle: true });
  candidate.p.inventory.add('minecraft:stone', 64);
  const payment = candidate.p.inventory.add('minecraft:emerald_block', 64);
  candidate.sandbox.ftwwDonateUpgradeBlocks(upgradeEvent(candidate), payment, 64, null);
  const dropped = upgradeDrop(candidate, 'farmingtales:gold_bucket', 1, candidate.p);
  spawn(candidate, dropped); ticks(candidate, 2);
  eq(dropped.item.count, 1, 'pot dropped while upgrade shop captures inventory is not consumed');
  eq(candidate.unlockedCalls.length, 0, 'no invisible reward generated inside captured inventory');
  candidate.p.gui.close(); ticks(candidate, 2);
  eq(dropped.removed, true, 'owned pot is processed after shop closes and native inventory restores');
  eq(countMain(candidate, 'minecraft:golden_apple'), 1, 'first post-upgrade pot reward remains in backpack');
  eq(countMain(candidate, 'minecraft:stone'), 64, 'original inventory remains intact');
}
for (const options of [{ goldPoolMissing: true }, { goldPoolMode: 'short' }, { emptyReward: true }]) {
  const candidate = upgradeSetup(options); savedUpgrade(candidate, 64);
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 1);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 1, 'missing, short or empty gold pool fails before payment: ' + JSON.stringify(options));
  eq(prog(candidate, candidate.p.uuid).goldPots, 0, 'invalid pool writes no new pot total');
}
{
  const candidate = upgradeSetup(); savedUpgrade(candidate, 64); candidate.server.failSave = true;
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 1);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 1, 'gold save failure refunds first unlocked pot');
  eq(candidate.p.given.length, 0, 'failed gold save cannot grant a duplicate reward');
}
{
  const candidate = upgradeSetup({ nativeInventoryLifecycle: true }); savedUpgrade(candidate, 64);
  fillMain(candidate, 'minecraft:stone', 64);
  const payment = upgradeDrop(candidate, 'farmingtales:gold_bucket', 1, candidate.p);
  spawn(candidate, payment); ticks(candidate, 2);
  eq(payment.removed, true, 'full backpack still permits a paid water wish');
  eq(candidate.p.droppedRewards.length, 1, 'full backpack explicitly drops gold-wish reward instead of losing it');
}
section('E05 backwards compatibility, record validation, emerald wishes, shop and tooltip');
{
  const candidate = upgradeSetup();
  const payment = upgradeDrop(candidate, 'farmingtales:gold_bucket', 1, candidate.p);
  spawn(candidate, payment); ticks(candidate, 4);
  eq(payment.item.count, 1, 'locked water wish does not consume a pot');
  eq(prog(candidate, candidate.p.uuid), null, 'locked water wish writes no progress');
  eq(candidate.sandbox.global.ftwwPendingEmeraldDrops.length, 0, 'locked water wish stops scanning rather than spamming or silently paying after later upgrade');
}
for (const legacy of [[0, 0], [1, 1], [10, 2], [99, 2], [100, 3]]) {
  const candidate = upgradeSetup();
  putRaw(candidate, candidate.p.uuid, JSON.stringify({ emeralds: 7, goldPots: legacy[0] }));
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, candidate.p), legacy[1], 'old progress retains its level: ' + legacy[0]);
  eq(candidate.sandbox.ftwwProgress(candidate.server, candidate.p).emeraldBlocks, 0, 'old progress defaults missing upgrade field to zero');
}
for (const invalid of [-1, 0.5, '64', true, null, MAXSAFE + 1]) {
  const candidate = upgradeSetup();
  putRaw(candidate, candidate.p.uuid, JSON.stringify({ emeralds: 0, goldPots: 0, emeraldBlocks: invalid }));
  const payment = candidate.p.inventory.add('minecraft:emerald_block', 64);
  candidate.p.shift = true;
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 64, 'invalid upgrade progress fails closed: ' + JSON.stringify(invalid));
  eq(candidate.p.guiOpens, 0, 'invalid upgrade progress cannot open shop');
}
{
  const candidate = upgradeSetup({ rng: () => 0.99 });
  const payment = candidate.p.inventory.add('minecraft:emerald', 3);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 2, 'ordinary emerald wishes still cost one emerald');
  eq(prog(candidate, candidate.p.uuid).emeralds, 1, 'ordinary emerald wish total is retained');
  eq(prog(candidate, candidate.p.uuid).emeraldBlocks, 0, 'ordinary emeralds cannot accidentally unlock the shop');
  eq(candidate.sandbox.ftwwUsefulChance(0), 31.1, 'unupgraded emerald useful rate unchanged');
  eq(candidate.sandbox.ftwwUsefulChance(3), 80, 'unlocked emerald useful rate unchanged');
}
{
  const candidate = upgradeSetup({ nativeInventoryLifecycle: true }); savedUpgrade(candidate, 64);
  candidate.wallet.balances.set(candidate.p.uuid, K(candidate, 'FTWW_SPELL_PRICE'));
  openShopGui(candidate);
  candidate.p.gui.buttons.filter(button => String(button.text.getString()).includes('500 万'))[0].click({ setHandled() {} });
  ticks(candidate, 1);
  eq(countMain(candidate, 'irons_spellbooks:scroll'), 1, 'emerald-unlocked zero-pot player can buy and keep a spell');
  eq(candidate.wallet.balances.get(candidate.p.uuid), 0, 'spell price remains 5 million');
  eq(K(candidate, 'FTWW_WEAPON_PRICE'), 50000000, 'weapon price remains 50 million');
  eq(prog(candidate, candidate.p.uuid).emeraldBlocks, 64, 'shop purchase does not reset the new unlock');
}
{
  const candidate = create(); loadTooltip(candidate);
  const lines = [];
  candidate.handlers.tooltip({ addAdvanced: (id, callback) => callback({}, {}, { add: line => lines.push(String(line)) }) });
  eq(lines.some(line => line.includes('64 个') && line.includes('绿宝石块')), true, 'tooltip describes actual 64-block unlock');
  eq(lines.some(line => line.includes('每个金币锅抽奖一次')), true, 'tooltip describes first-pot access');
  eq(lines.some(line => line.includes('累计 100 锅') || line.includes('1 / 10 / 100')), false, 'obsolete pot-upgrade description removed');
}
