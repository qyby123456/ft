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
          rewards.push({ category: () => 'manor_reward', stack: () => new JavaStack('ftboss:dora_nova_launcher', options.emptyReward ? 0 : 1, { GoldPoolRoll: index }) });
        }
        return options.goldPoolMode === 'short' ? rewards.slice(1) : rewards;
      }
    };
  };
  load(candidate);
  candidate.lv = world(candidate, options.world);
  candidate.p = player(candidate, { uuid: 'gold-pot-player', x: 100.5, y: 64.5, z: 100.5 });
  candidate.block = coreBlock(candidate, candidate.p);
  return candidate;
}
function upgradeEvent(candidate) {
  return { server: candidate.server, level: candidate.lv, player: candidate.p, block: candidate.block };
}
function savedUpgrade(candidate, upgradePots, goldPots) {
  putRaw(candidate, candidate.p.uuid, JSON.stringify({ emeralds: 0, goldPots: goldPots || 0, emeraldBlocks: 0, upgradePots }));
}
function upgradeDrop(candidate, id, count, owner) {
  return makeEntity({ id, count, x: 101, y: 64, z: 101, level: candidate.lv, owner });
}
section('U02 cumulative payment, cooldown, offhand and removed block payment');
{
  const candidate = upgradeSetup();
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 64);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 63, 'ordinary right-click pays one upgrade pot');
  eq(prog(candidate, candidate.p.uuid).upgradePots, 1, 'first payment is saved');
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, candidate.p), 0, 'one pot no longer upgrades to level one');
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 63, 'same-tick repeated click cannot pay twice');
  ticks(candidate, 12);
  rcBlock(candidate, candidate.p, payment, 'OFF_HAND');
  eq(payment.count, 63, 'offhand upgrade is ignored');
  candidate.p.shift = true;
  rcBlock(candidate, candidate.p, payment);
  eq(prog(candidate, candidate.p.uuid).upgradePots, 64, 'shift payment saves the full remaining stack');
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, candidate.p), 0, 'partial payment has only the initial level');
}
{
  const candidate = upgradeSetup(); savedUpgrade(candidate, 99);
  candidate.p.shift = true;
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 64);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 63, 'only remaining one pot is charged');
  eq(prog(candidate, candidate.p.uuid).upgradePots, 100, 'progress caps at exactly 100 pots');
  eq(candidate.unlockedCalls.length, 0, 'upgrade completion never calls the reward pool');
  eq(candidate.sandbox.ftwwDonateUpgradePots(upgradeEvent(candidate), payment, 64, null), false, 'full player cannot be charged for a second upgrade');
  eq(payment.count, 63, 'attempted repeated upgrade preserves excess pots');
}
{
  const candidate = upgradeSetup();
  const payment = candidate.p.inventory.add('minecraft:emerald_block', 64);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 64, 'emerald blocks are no longer upgrade currency');
  eq(prog(candidate, candidate.p.uuid), null, 'obsolete payment writes no progress');
  const dropped = upgradeDrop(candidate, 'minecraft:emerald_block', 64, candidate.p);
  spawn(candidate, dropped); ticks(candidate, 2);
  eq(dropped.item.count, 64, 'dropped emerald blocks are not consumed');
}
{
  const candidate = upgradeSetup({ world: { overrides: { '-2,0,-2': 'minecraft:air' } } });
  candidate.p.shift = true;
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 64);
  const click = rcBlock(candidate, candidate.p, payment);
  eq(click.cancelled, true, 'broken well blocks gold-pot item conversion');
  eq(payment.count, 64, 'incomplete structure cannot consume upgrade pots');
  eq(prog(candidate, candidate.p.uuid), null, 'incomplete well cannot unlock');
}
section('U03 dropped payments, ownership, excess, rollback and GUI failure');
{
  const candidate = upgradeSetup(); savedUpgrade(candidate, 36);
  const payment = upgradeDrop(candidate, 'farmingtales:gold_bucket', 64, candidate.p);
  spawn(candidate, payment); ticks(candidate, 2);
  eq(payment.removed, true, 'owned upgrade stack is consumed from well water');
  eq(payment.discarded, 1, 'empty upgrade entity discarded once');
  eq(prog(candidate, candidate.p.uuid).upgradePots, 100, 'water payment completes the same personal upgrade');
  eq(candidate.p.guiOpens, 1, 'water upgrade opens shop');
  eq(candidate.unlockedCalls.length, 0, 'water upgrade pots do not roll prizes');
}
{
  const candidate = upgradeSetup();
  const payment = upgradeDrop(candidate, 'farmingtales:gold_bucket', 64, null);
  spawn(candidate, payment); ticks(candidate, 4);
  eq(payment.item.count, 64, 'ownerless upgrade cannot charge an arbitrary nearby player');
  eq(prog(candidate, candidate.p.uuid), null, 'ownerless upgrade writes no progress');
  payment.owner = candidate.p;
  candidate.p.level = { dimension: 'minecraft:the_nether' };
  ticks(candidate, 4);
  eq(payment.item.count, 64, 'owner in another dimension cannot donate');
}
{
  const candidate = upgradeSetup(); savedUpgrade(candidate, 99);
  const payment = upgradeDrop(candidate, 'farmingtales:gold_bucket', 64, candidate.p);
  spawn(candidate, payment); ticks(candidate, 8);
  eq(payment.item.count, 63, 'water upgrade retains all excess pots across later scans');
  eq(payment.removed, false, 'partly consumed entity remains');
  eq(prog(candidate, candidate.p.uuid).upgradePots, 100, 'partial water payment completes upgrade');
  eq(candidate.unlockedCalls.length, 0, 'excess cannot silently become wishes on the following tick');
}
for (const dropped of [false, true]) {
  const candidate = upgradeSetup(); candidate.server.failSave = true;
  const entity = dropped ? upgradeDrop(candidate, 'farmingtales:gold_bucket', 64, candidate.p) : null;
  const payment = entity ? entity.item : candidate.p.inventory.add('farmingtales:gold_bucket', 64);
  eq(candidate.sandbox.ftwwDonateUpgradePots(upgradeEvent(candidate), payment, 64, entity), false, 'save failure rejects upgrade');
  eq((entity ? entity.item : payment).count, 64, 'save failure restores all pots, hand/drop=' + dropped);
  eq(candidate.p.guiOpens, 0, 'failed save cannot open shop');
  eq(prog(candidate, candidate.p.uuid), null, 'failed save leaves progress untouched');
}
{
  const candidate = upgradeSetup({ goldPoolMissing: true }); savedUpgrade(candidate, 36);
  candidate.p.throwOnGui = true;
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 64);
  eq(candidate.sandbox.ftwwDonateUpgradePots(upgradeEvent(candidate), payment, 64, null), true, 'GUI failure does not undo committed upgrade');
  eq(payment.count, 0, 'committed upgrade charged once even without reward pool');
  eq(prog(candidate, candidate.p.uuid).upgradePots, 100, 'unlock survives GUI failure');
  candidate.p.throwOnGui = false;
  openShopGui(candidate);
  eq(candidate.p.guiOpens, 2, 'empty-hand click can reopen after GUI failure');
}
section('U04 first post-upgrade pot, bulk rolls and native inventory lifecycle');
for (const previousPots of [0, 1, 99, 100]) {
  for (const amount of [1, 64]) {
    const candidate = upgradeSetup({ nativeInventoryLifecycle: true }); savedUpgrade(candidate, 100, previousPots);
    candidate.p.shift = amount > 1;
    const payment = candidate.p.inventory.add('farmingtales:gold_bucket', amount);
    rcBlock(candidate, candidate.p, payment);
    eq(payment.count, 0, 'unlocked payment consumed: previous=' + previousPots + ' amount=' + amount);
    eq(candidate.unlockedCalls.length, 1, 'new native API invoked once');
    eq(candidate.unlockedCalls[0] && candidate.unlockedCalls[0].amount, amount, 'every post-upgrade pot gets a roll');
    eq(prog(candidate, candidate.p.uuid).goldPots, previousPots + amount, 'wish counter saved separately');
    eq(prog(candidate, candidate.p.uuid).upgradePots, 100, 'wishes cannot cause a second upgrade');
    eq(countMain(candidate, 'ftboss:dora_nova_launcher'), Math.min(36, amount), 'unique-NBT weapons fill real inventory');
    eq(candidate.p.droppedRewards.reduce((count, stack) => count + stack.count, 0), Math.max(0, amount - 36), 'overflow rewards explicitly drop');
    eq(candidate.p.given[0] && candidate.p.given[0].nbt.GoldPoolRoll, 0, 'reward NBT preserved');
    eq(candidate.p.guiOpens, 0, 'reward inventory cannot be captured by a new upgrade menu');
    ticks(candidate, 3);
    eq(countMain(candidate, 'ftboss:dora_nova_launcher') + candidate.p.droppedRewards.reduce((count, stack) => count + stack.count, 0), amount, 'all rewards survive later ticks');
  }
}
{
  const candidate = upgradeSetup({ nativeInventoryLifecycle: true }); savedUpgrade(candidate, 36);
  candidate.p.inventory.add('minecraft:stone', 64);
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 64);
  candidate.sandbox.ftwwDonateUpgradePots(upgradeEvent(candidate), payment, 64, null);
  const dropped = upgradeDrop(candidate, 'farmingtales:gold_bucket', 1, candidate.p);
  spawn(candidate, dropped); ticks(candidate, 2);
  eq(dropped.item.count, 1, 'wish waits while upgrade menu captures inventory');
  eq(candidate.unlockedCalls.length, 0, 'no invisible reward generated inside captured inventory');
  candidate.p.gui.close(); ticks(candidate, 2);
  eq(dropped.removed, true, 'owned pot processed after menu closes and inventory restores');
  eq(countMain(candidate, 'ftboss:dora_nova_launcher'), 1, 'first post-upgrade weapon stays in backpack');
  eq(countMain(candidate, 'minecraft:stone'), 64, 'original inventory preserved');
}
for (const options of [{ goldPoolMissing: true }, { goldPoolMode: 'short' }, { emptyReward: true }]) {
  const candidate = upgradeSetup(options); savedUpgrade(candidate, 100);
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 1);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 1, 'invalid wish pool fails before payment: ' + JSON.stringify(options));
  eq(prog(candidate, candidate.p.uuid).goldPots, 0, 'invalid pool writes no wish total');
}
{
  const candidate = upgradeSetup(); savedUpgrade(candidate, 100); candidate.server.failSave = true;
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 1);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 1, 'wish save failure refunds first unlocked pot');
  eq(candidate.p.given.length, 0, 'failed wish save grants no duplicate reward');
}
{
  const candidate = upgradeSetup({ nativeInventoryLifecycle: true }); savedUpgrade(candidate, 100);
  fillMain(candidate, 'minecraft:stone', 64);
  const payment = upgradeDrop(candidate, 'farmingtales:gold_bucket', 1, candidate.p);
  spawn(candidate, payment); ticks(candidate, 2);
  eq(payment.removed, true, 'full backpack permits paid water wish');
  eq(candidate.p.droppedRewards.length, 1, 'full backpack drops protected reward');
}
section('U05 migration, corrupt records, emerald wishes and tooltip');
for (const [previousPots, expectedLevel] of [[0, 0], [1, 0], [10, 0], [99, 0], [100, 3], [320, 3]]) {
  const candidate = upgradeSetup();
  const raw = JSON.stringify({ emeralds: 7, goldPots: previousPots });
  putRaw(candidate, candidate.p.uuid, raw);
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, candidate.p), expectedLevel, 'legacy pots map to only initial/full: ' + previousPots);
  eq(candidate.sandbox.ftwwProgress(candidate.server, candidate.p).upgradePots, Math.min(100, previousPots), 'legacy donated pots credited once');
  eq(candidate.raw.get(key(candidate, candidate.p.uuid)), raw, 'reading legacy data does not rewrite saves');
}
{
  const candidate = upgradeSetup();
  putRaw(candidate, candidate.p.uuid, JSON.stringify({ emeralds: 193, goldPots: 64, emeraldBlocks: 64 }));
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, candidate.p), 3, 'legacy completed 64-block upgrade keeps personal access');
}
{
  const candidate = upgradeSetup();
  putRaw(candidate, candidate.p.uuid, JSON.stringify({ emeralds: 0, goldPots: 99, emeraldBlocks: 0 }));
  const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 2);
  candidate.p.shift = true;
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 1, 'legacy 99-pot record needs only one additional upgrade pot');
  eq(prog(candidate, candidate.p.uuid).upgradePots, 100, 'migration is persisted on actual payment');
  eq(prog(candidate, candidate.p.uuid).goldPots, 99, 'migration retains historical pot total without counting it twice');
}
for (const field of ['upgradePots', 'emeraldBlocks']) {
  for (const invalid of [-1, 0.5, '100', true, null, MAXSAFE + 1]) {
    const candidate = upgradeSetup();
    putRaw(candidate, candidate.p.uuid, JSON.stringify({ emeralds: 0, goldPots: 0, emeraldBlocks: 0, upgradePots: 0, [field]: invalid }));
    const payment = candidate.p.inventory.add('farmingtales:gold_bucket', 64);
    candidate.p.shift = true;
    rcBlock(candidate, candidate.p, payment);
    eq(payment.count, 64, 'invalid progress fails closed: ' + field + '=' + JSON.stringify(invalid));
    eq(candidate.p.guiOpens, 0, 'invalid progress cannot open shop');
  }
}
{
  const candidate = upgradeSetup({ rng: () => 0.99 });
  const payment = candidate.p.inventory.add('minecraft:emerald', 3);
  rcBlock(candidate, candidate.p, payment);
  eq(payment.count, 2, 'emerald wishes still cost one emerald');
  eq(prog(candidate, candidate.p.uuid).emeralds, 1, 'emerald wish total retained');
  eq(prog(candidate, candidate.p.uuid).upgradePots, 0, 'emerald wishes never pay upgrade costs');
  eq(candidate.sandbox.ftwwUsefulChance(0), 31.1, 'unupgraded emerald useful rate unchanged');
  eq(candidate.sandbox.ftwwUsefulChance(3), 80, 'full emerald useful rate unchanged');
}
{
  const candidate = create(); loadTooltip(candidate);
  const lines = [];
  candidate.handlers.tooltip({ addAdvanced: (id, callback) => callback({}, {}, { add: line => lines.push(String(line)) }) });
  eq(lines.some(line => line.includes('100 个金币锅') && line.includes('一次升满')), true, 'tooltip describes actual single upgrade');
  eq(lines.some(line => line.includes('升级用锅不抽奖')), true, 'tooltip distinguishes upgrade cost from paid wishes');
  eq(lines.some(line => line.includes('64 个绿宝石块') || line.includes('1 / 10 / 100')), false, 'obsolete upgrade descriptions removed');
}
section('U06 multiplayer same-well isolation, owner attribution and reconnection');
{
  const candidate = upgradeSetup(); savedUpgrade(candidate, 0, 320);
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, candidate.p), 0, 'explicit new upgrade counter prevents historical wish counts from creating free upgrades');
}
{
  const candidate = upgradeSetup();
  const other = player(candidate, { uuid: 'other-uuid', username: candidate.p.username, x: 101, y: 64, z: 101, shift: true });
  candidate.p.shift = true;
  rcBlock(candidate, candidate.p, candidate.p.inventory.add('farmingtales:gold_bucket', 64));
  rcBlock(candidate, other, other.inventory.add('farmingtales:gold_bucket', 64));
  eq(prog(candidate, candidate.p.uuid).upgradePots, 64, 'A payment is separate despite same display name and well');
  eq(prog(candidate, other.uuid).upgradePots, 64, 'B payment is not pooled with A');
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, other), 0, 'combined 128 pots cannot unlock either player');
  ticks(candidate, 12);
  rcBlock(candidate, candidate.p, candidate.p.inventory.add('farmingtales:gold_bucket', 36));
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, candidate.p), 3, 'A unlocks after own 100 pots');
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, other), 0, 'A upgrade does not unlock B at the same core');
  const before = candidate.raw.get(key(candidate, candidate.p.uuid));
  rcBlock(candidate, other, new Stack('minecraft:air', 0));
  eq(other.guiOpens, 0, 'B cannot open the upgraded player shop');
  eq(candidate.sandbox.ftwwShopAvailable({ server: candidate.server, player: other, level: candidate.lv, block: candidate.block }), false, 'server rejects B even at A shopping location');
  const reconnect = player(candidate, { uuid: candidate.p.uuid, username: 'Renamed', x: 100.5, y: 64.5, z: 100.5 });
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, reconnect), 3, 'same UUID keeps upgrade after reconnect and rename');
  eq(candidate.sandbox.ftwwSafeLevel(candidate.server, other), 0, 'reconnection cannot transfer full level to B');
  eq(candidate.raw.get(key(candidate, candidate.p.uuid)), before, 'other player checks do not rewrite A record');
}
{
  const candidate = upgradeSetup();
  const other = player(candidate, { uuid: 'nearer-player', x: 101, y: 64, z: 101 });
  const payment = upgradeDrop(candidate, 'farmingtales:gold_bucket', 64, candidate.p);
  spawn(candidate, payment); ticks(candidate, 2);
  eq(prog(candidate, candidate.p.uuid).upgradePots, 64, 'water donation belongs to actual owner, not nearest bystander');
  eq(prog(candidate, other.uuid), null, 'nearer bystander receives no upgrade credit');
  eq(other.given.length, 0, 'bystander receives no prizes');
}
