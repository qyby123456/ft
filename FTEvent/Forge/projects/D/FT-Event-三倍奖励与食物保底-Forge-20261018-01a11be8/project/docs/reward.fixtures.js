const shippingPrices = JSON.parse(fs.readFileSync(path.join(ROOT, 'reference', 'base-shipping-prices.json'), 'utf8'));
const actualPriceRuntime = vm.createContext({ global: {}, console: { info() {}, warn() {}, error() {} } });
vm.runInContext(fs.readFileSync(path.join(ROOT, 'reference', 'shipping-price-list.js'), 'utf8'), actualPriceRuntime, { filename: 'unmodified-shipping-price-list.js' });
const verifiedFoods = JSON.parse(fs.readFileSync(path.join(ROOT, 'audit', 'selected-foods.json'), 'utf8').replace(/^\uFEFF/, ''));
const foodIds = new Set(verifiedFoods.map(food => food.id));
const createBeforeRewardPrices = create;
create = function (options) {
  options = options || {};
  const candidate = createBeforeRewardPrices(options);
  candidate.priceCalls = 0;
  candidate.sandbox.getItemPrice = id => {
    candidate.priceCalls++;
    return options.priceOverrides ? options.priceOverrides[id] || 0 : actualPriceRuntime.global.getItemPrice(id, 0, {});
  };
  if (options.priceAPIMissing) delete candidate.sandbox.getItemPrice;
  candidate.sandbox.ftsbMarketGetDiscount = () => options.marketDiscount || 0;
  const originalItem = candidate.sandbox.Item.of;
  candidate.sandbox.Item.of = (id, count) => {
    const stack = originalItem(id, count);
    if (foodIds.has(stack.id)) {
      stack.maxCount = 16;
      if (options.missingFoods && options.missingFoods.includes(stack.id)) return new Stack('minecraft:air', 0);
    }
    if (stack.id.startsWith('ftboss:music_disc_')) stack.maxCount = 1;
    return stack;
  };
  return candidate;
};
function rewardSetup(options) {
  options = options || {};
  const candidate = create(Object.assign({ nativeInventoryLifecycle: true, rng: () => 0.5 }, options));
  const originalLoader = candidate.sandbox.Java.loadClass;
  candidate.nativeWishCalls = 0;
  candidate.sandbox.Java.loadClass = name => {
    if (name !== 'cn.farmingtales.qysevent.wishingwell.GoldPotWishes') return originalLoader(name);
    return { rollUnlockedRewards(level, amount) {
      candidate.nativeWishCalls++;
      const results = [];
      for (let index = 0; index < amount; index++) {
        results.push({
          category: () => options.category || 'food',
          stack: () => new JavaStack(options.rewardId || 'farmersdelight:kelp_roll', options.baseCount || 1, { WishRoll: index })
        });
      }
      return results;
    } };
  };
  load(candidate);
  candidate.lv = world(candidate);
  candidate.p = player(candidate, { uuid: 'reward-player', x: 100.5, y: 64.5, z: 100.5 });
  candidate.block = coreBlock(candidate, candidate.p);
  return candidate;
}
function rewardEvent(candidate) {
  return { server: candidate.server, level: candidate.lv, player: candidate.p, block: candidate.block };
}
function unlockRewards(candidate) {
  putRaw(candidate, candidate.p.uuid, JSON.stringify({ emeralds: 0, goldPots: 0, emeraldBlocks: 64 }));
}
function givenCount(candidate, id) {
  return candidate.p.given.filter(stack => !id || stack.id === id).reduce((total, stack) => total + stack.count, 0);
}
function givenFoodValue(candidate, discount) {
  return candidate.p.given.reduce((value, stack) => value + Math.floor((shippingPrices[stack.id] || 0) * (100 - (discount || 0)) / 100) * stack.count, 0);
}
