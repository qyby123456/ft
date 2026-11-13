// Farming Tales multiblock Wishing Well.
// One emerald buys one server-authoritative roll. Valuable results are deliberately rare.
const FTWW_CORE = 'farmingtales:wishing_well_core'
const FTWW_PAYMENT = 'minecraft:emerald'
const FTWW_GOLD_POT = 'farmingtales:gold_bucket'
const FTWW_UPGRADE_PAYMENT = 'minecraft:emerald_block'
const FTWW_UPGRADE_BLOCKS = 64
const FTWW_REWARD_MULTIPLIER = 3
const FTWW_JACKPOT_CHANCE = 0.01
const FTWW_JACKPOT_MULTIPLIER = 10
const FTWW_MIN_FOOD_VALUE = 1000
const FTWW_FOOD_VARIETY = 3
const FTWW_SUMMON_PRICE = 2
const FTWW_WEAPON_PRICE = 50000000
const FTWW_SPELL_PRICE = 5000000
const FTWW_PROGRESS_PREFIX = 'ftww_progress_v1_'
const FTWW_LEGACY_LEVEL_THRESHOLDS = [0, 1, 10, 100]
const FTWW_OUTCOME_TYPES = ['nothing', 'junk', 'wood', 'food', 'poop', 'coin', 'seed_bag', 'mineral', 'emerald', 'geode']
const FTWW_LEVEL_ODDS = [
  [39.9, 22, 10, 10, 7, 5, 3, 2.5, 0.5, 0.1],
  [32, 18, 14, 14, 5, 8, 4, 4, 0.8, 0.2],
  [22, 14, 17, 17, 4, 11, 6, 7, 1.5, 0.5],
  [10, 8, 20, 20, 2, 16, 10, 10, 3, 1]
]
const FTWW_SUMMONS = [
  'ftboss:sugar_queen_crystal', 'ftboss:meat_heart', 'ftboss:defect_core',
  'ftboss:tea_seed', 'ftboss:rat_bait', 'ftboss:wine_charm',
  'ftboss:compost_summon', 'ftboss:benger_whistle', 'ftboss:forbidden_apple',
  'ftboss:tide_banquet', 'ftboss:benger_spawn_egg'
]
const FTWW_DROP_SCAN_INTERVAL = 2
const FTWW_DROP_TRACK_TICKS = 200
const FTWW_DROP_PLAYER_RADIUS = 12
const FTWW_QUEUE_MAX_TICKS = 40

global.ftwwPendingEmeraldDrops = []
global.ftwwDropScanTick = 0
global.ftwwRightClickCooldowns = {}
global.ftwwGoldUseGuards = {}
global.ftwwShopCooldowns = {}
global.ftwwPurchaseQueue = []
global.ftwwGoldUseBlocked = player => {
  try {
    return Number(global.ftwwGoldUseGuards[ftwwPlayerKey(player)] || 0) > Number(global.ftwwDropScanTick || 0)
  } catch (error) {
    try { console.error('[FarmingTales][WishingWell] gold pot guard could not resolve player identity: ' + error) } catch (ignored) {}
    return true
  }
}

// Exact source: schematics/xyj.nbt
// SHA-256: 15BB26FC45FFCA41AC59D208EB747EE6B69A4CD2FACE3E5FEBDED78B4030157F
// NBT bounds are 7x5x7. The controller is palette position (3,1,3).
const FTWW_BLUEPRINT_SHA256 = '15BB26FC45FFCA41AC59D208EB747EE6B69A4CD2FACE3E5FEBDED78B4030157F'
const FTWW_STONE_BRICKS = 'minecraft:stone_bricks'
const FTWW_STONE_STAIRS = 'minecraft:stone_brick_stairs'
const FTWW_STONE_WALL = 'minecraft:stone_brick_wall'
const FTWW_STONE_SLAB = 'minecraft:stone_brick_slab'
const FTWW_EMERALD_BLOCK = 'minecraft:emerald_block'
const FTWW_WATER = 'minecraft:water'

const FTWW_JUNK_REWARDS = [
  { item: 'minecraft:cobblestone', min: 1, max: 5, weight: 22 },
  { item: 'minecraft:stone', min: 1, max: 4, weight: 16 },
  { item: 'minecraft:gravel', min: 1, max: 4, weight: 16 },
  { item: 'minecraft:dirt', min: 1, max: 5, weight: 14 },
  { item: 'minecraft:stick', min: 1, max: 6, weight: 12 },
  { item: 'minecraft:rotten_flesh', min: 1, max: 3, weight: 8 },
  { item: 'minecraft:flint', min: 1, max: 2, weight: 6 },
  { item: 'minecraft:string', min: 1, max: 3, weight: 6 }
]

const FTWW_WOOD_REWARDS = [
  { item: 'minecraft:oak_log', min: 1, max: 4, weight: 28 },
  { item: 'minecraft:spruce_log', min: 1, max: 4, weight: 24 },
  { item: 'minecraft:birch_log', min: 1, max: 4, weight: 20 },
  { item: 'minecraft:acacia_log', min: 1, max: 3, weight: 12 },
  { item: 'minecraft:dark_oak_log', min: 1, max: 3, weight: 10 },
  { item: 'minecraft:mangrove_log', min: 1, max: 3, weight: 6 }
]

const FTWW_FOOD_REWARDS = [
  { item: 'farmersdelight:kelp_roll', min: 1, max: 3, weight: 1 },
  { item: 'farmersdelight:salmon_roll', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:golden_salad', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:sweet_and_sour_pork_rice_bowl', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:stir_fried_pork_with_peppers_rice_bowl', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:fish_flavored_shredded_pork_rice_bowl', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:beef_meatball_soup', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:four_joy_meatball_soup', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:fried_spring_roll', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:donkey_burger', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:fondant_pie', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:shengjian_mantou', min: 1, max: 3, weight: 1 },
  { item: 'farm_and_charm:farmers_breakfast', min: 1, max: 3, weight: 1 },
  { item: 'farm_and_charm:sausage_with_oat_patty', min: 1, max: 3, weight: 1 },
  { item: 'farm_and_charm:beef_patty_with_vegetables', min: 1, max: 3, weight: 1 },
  { item: 'farm_and_charm:goulash', min: 1, max: 3, weight: 1 },
  { item: 'farm_and_charm:barley_soup', min: 1, max: 3, weight: 1 },
  { item: 'farm_and_charm:barley_patties_with_potatoes', min: 1, max: 3, weight: 1 },
  { item: 'farm_and_charm:corn_grits', min: 1, max: 3, weight: 1 },
  { item: 'farm_and_charm:pasta_with_onion_sauce', min: 1, max: 3, weight: 1 },
  { item: 'farm_and_charm:cooked_cod', min: 1, max: 3, weight: 1 },
  { item: 'farm_and_charm:cooked_salmon', min: 1, max: 3, weight: 1 },
  { item: 'farm_and_charm:roasted_corn', min: 1, max: 3, weight: 1 },
  { item: 'farm_and_charm:chicken_wrapped_in_bacon', min: 1, max: 3, weight: 1 },
  { item: 'farm_and_charm:lamb_with_corn', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:spicy_chicken', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:pan_seared_knight_steak', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:frost_lamb_chop', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:blaze_lamb_chop', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:crystal_lamb_chop', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:stuffed_tiger_skin_pepper', min: 1, max: 3, weight: 1 },
  { item: 'kaleidoscope_cookery:spicy_rabbit_head', min: 1, max: 3, weight: 1 }
]

const FTWW_MINERAL_REWARDS = [
  { item: 'minecraft:coal', min: 2, max: 6, weight: 30 },
  { item: 'minecraft:raw_copper', min: 2, max: 5, weight: 25 },
  { item: 'minecraft:raw_iron', min: 1, max: 3, weight: 20 },
  { item: 'minecraft:redstone', min: 2, max: 6, weight: 12 },
  { item: 'minecraft:lapis_lazuli', min: 2, max: 5, weight: 8 },
  { item: 'minecraft:raw_gold', min: 1, max: 2, weight: 4 },
  { item: 'minecraft:diamond', min: 1, max: 1, weight: 1 }
]


// Every registered Farming Tales seed packet can appear here. Equal weights
// keep the category simple: first roll the 3% seed-bag event, then one bag.
const FTWW_SEED_BAG_REWARDS = [
  { item: 'farmingtales:wheat_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:beetroot_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:carrot_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:potato_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:flax_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:kernels_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:oat_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:barley_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:lettuce_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:tomato_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:onion_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:strawberry_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:cabbage_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:hops_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:ancient_fruit_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:tea_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:yerba_mate_leaf_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:rooibos_leaf_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:f_tomatoe_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:melon_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:pumpkin_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:rice_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:nether_wart_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:sugar_cane_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:cocoa_beans_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:glow_berries_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:sweet_berries_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:foul_berries_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:apple_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:coffee_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:jalapeno_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:lemon_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:orange_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:starfruit_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:young_coconut_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:grass_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:hoary_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:leek_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:spongekin_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:plum_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:walnut_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:blueberries_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:grape_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:redbean_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:soybean_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:cucumber_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:canola_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:pepper_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:sweet_potato_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:houttuynia_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:pomegranate_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:dragon_fruit_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:lime_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:lucuma_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:date_packet', min: 1, max: 1, weight: 1 },
  { item: 'farmingtales:eggplant_packet', min: 1, max: 1, weight: 1 }
]

function ftwwText(message) {
  try {
    if (typeof global.ftText === 'function') return global.ftText(message)
  } catch (error) {}
  return Text.of(message)
}

function ftwwTell(player, message) {
  try { player.tell(ftwwText(message)) } catch (error) {}
}

function ftwwStatus(player, message) {
  try { player.setStatusMessage(ftwwText(message)) } catch (error) {
    ftwwTell(player, message)
  }
}

function ftwwBlockId(level, x, y, z) {
  try { return String(level.getBlock(x, y, z).id) } catch (error) { return 'minecraft:air' }
}

function ftwwMissing(x, y, z, expected, found, dx, dy, dz) {
  return { x: x, y: y, z: z, expected: expected, found: found, dx: dx, dy: dy, dz: dz }
}

function ftwwRequireBlock(level, cx, cy, cz, dx, dy, dz, expectedId, label) {
  let found = ftwwBlockId(level, cx + dx, cy + dy, cz + dz)
  if (found === expectedId) return null
  return ftwwMissing(cx + dx, cy + dy, cz + dz, label, found, dx, dy, dz)
}

function ftwwCheckStructure(level, core) {
  let cx = Math.floor(Number(core.x))
  let cy = Math.floor(Number(core.y))
  let cz = Math.floor(Number(core.z))
  let missing = null

  // xyj.nbt Y=0, relative Y=-1: 5x5 stone-brick foundation.
  for (let dx = -2; dx <= 2; dx++) {
    for (let dz = -2; dz <= 2; dz++) {
      missing = ftwwRequireBlock(level, cx, cy, cz, dx, -1, dz, FTWW_STONE_BRICKS, '石砖地基')
      if (missing) return missing
    }
  }

  // Four entrance steps in the middle of each side of the foundation.
  let stairPositions = [[-3, 0], [0, -3], [0, 3], [3, 0]]
  for (let i = 0; i < stairPositions.length; i++) {
    let dx = stairPositions[i][0]
    let dz = stairPositions[i][1]
    missing = ftwwRequireBlock(level, cx, cy, cz, dx, -1, dz, FTWW_STONE_STAIRS, '朝向井心的石砖楼梯')
    if (missing) return missing
  }

  // xyj.nbt Y=1, relative Y=0: 5x5 basin. Cardinal rim centers are slabs;
  // the other twelve rim cells are walls, and eight inner cells are water.
  for (let dx = -2; dx <= 2; dx++) {
    for (let dz = -2; dz <= 2; dz++) {
      if (dx === 0 && dz === 0) continue
      if (Math.max(Math.abs(dx), Math.abs(dz)) === 2) {
        let cardinalCenter = (dx === 0 && Math.abs(dz) === 2) || (dz === 0 && Math.abs(dx) === 2)
        let expected = cardinalCenter ? FTWW_STONE_SLAB : FTWW_STONE_WALL
        let label = cardinalCenter ? '下半石砖台阶井沿' : '石砖墙井沿'
        missing = ftwwRequireBlock(level, cx, cy, cz, dx, 0, dz, expected, label)
      } else {
        missing = ftwwRequireBlock(level, cx, cy, cz, dx, 0, dz, FTWW_WATER, '水源')
      }
      if (missing) return missing
    }
  }

  // xyj.nbt Y=2..3, relative Y=+1..+2: two-high wall posts at the four corners.
  let corners = [[-2, -2], [-2, 2], [2, -2], [2, 2]]
  for (let height = 1; height <= 2; height++) {
    for (let i = 0; i < corners.length; i++) {
      let dx = corners[i][0]
      let dz = corners[i][1]
      missing = ftwwRequireBlock(level, cx, cy, cz, dx, height, dz, FTWW_STONE_WALL, '石砖墙立柱')
      if (missing) return missing
    }
  }

  // xyj.nbt Y=4, relative Y=+3: a 5x5 perimeter roof. Four cardinal
  // centers are emerald blocks; the other twelve cells are stone-brick slabs.
  for (let dx = -2; dx <= 2; dx++) {
    for (let dz = -2; dz <= 2; dz++) {
      if (Math.max(Math.abs(dx), Math.abs(dz)) !== 2) continue
      let cardinalCenter = (dx === 0 && Math.abs(dz) === 2) || (dz === 0 && Math.abs(dx) === 2)
      let expected = cardinalCenter ? FTWW_EMERALD_BLOCK : FTWW_STONE_SLAB
      let label = cardinalCenter ? '绿宝石块屋顶节点' : '石砖台阶屋顶'
      missing = ftwwRequireBlock(level, cx, cy, cz, dx, 3, dz, expected, label)
      if (missing) return missing
    }
  }

  // xyj.nbt also contains one torchmaster:invisible_light helper. It is visual
  // lighting metadata, not a player-buildable structural requirement.
  return null
}

function ftwwShowGuide(player) {
  ftwwTell(player, '§6§l许愿井 §7· 投下心愿，等待井水回应。')
  ftwwTell(player, '§a用 1 颗绿宝石许愿：手持右键核心，或投入井水。')
  ftwwTell(player, '§6累计投入 64 个绿宝石块永久满级，解锁金币锅许愿与商店。')
  ftwwTell(player, '§7满级后空手右键打开商店；潜行投入绿宝石块或金币锅时使用整叠。')
  ftwwShowProgress(player.server, player)
}

function ftwwHighlightMissing(event, missing) {
  let x = Number(missing.x) + 0.5
  let y = Number(missing.y) + 0.6
  let z = Number(missing.z) + 0.5
  event.server.runCommandSilent(`particle minecraft:angry_villager ${x} ${y} ${z} 0.22 0.22 0.22 0.02 14 force @a`)
  event.server.runCommandSilent(`playsound minecraft:block.note_block.bass block @a ${x} ${y} ${z} 0.75 0.65`)
}

function ftwwWeightedChoice(entries) {
  let total = 0
  for (let i = 0; i < entries.length; i++) total += Number(entries[i].weight || 0)
  let roll = Math.random() * total
  for (let i = 0; i < entries.length; i++) {
    roll -= Number(entries[i].weight || 0)
    if (roll < 0) return entries[i]
  }
  return entries[entries.length - 1]
}

function ftwwRandomCount(entry) {
  let minimum = Number(entry.min || 1)
  let maximum = Math.max(minimum, Number(entry.max || minimum))
  return minimum + Math.floor(Math.random() * (maximum - minimum + 1))
}

function ftwwRollOutcome(server, player) {
  let roll = Math.random() * 100
  let odds = FTWW_LEVEL_ODDS[ftwwSafeLevel(server, player)]
  for (let index = 0; index < odds.length; index++) {
    roll -= odds[index]
    if (roll < 0) return ftwwTestOutcome(FTWW_OUTCOME_TYPES[index])
  }
  return ftwwTestOutcome('geode')
}

function ftwwOutcomeEntry(outcome) {
  if (outcome.pool) return ftwwWeightedChoice(outcome.pool)
  return outcome
}

function ftwwPlanWishItem(stack, count) {
  if (!stack || stack.isEmpty() || !isFinite(count) || count < 1 || count > 100000 || Math.floor(count) !== count) {
    throw new Error('Invalid wish reward quantity')
  }
  let maximum = Number(stack.maxStackSize)
  if (!isFinite(maximum) || maximum < 1) maximum = Number(stack.getMaxStackSize())
  if (!isFinite(maximum) || maximum < 1 || Math.floor(maximum) !== maximum) throw new Error('Invalid wish stack limit')
  maximum = Math.min(64, maximum)
  let aggregate = stack.copy()
  aggregate.count = count
  let deliveries = []
  for (let remaining = count; remaining > 0; remaining -= maximum) {
    let delivery = stack.copy()
    delivery.count = Math.min(maximum, remaining)
    deliveries.push(delivery)
  }
  return { stack: aggregate, deliveries: deliveries }
}

function ftwwFoodUnitValue(server, stack) {
  if (typeof global.getItemPrice !== 'function') throw new Error('Shipping price API is unavailable')
  let price = Number(global.getItemPrice(String(stack.id), 0, stack.nbt))
  let discount = 0
  if (typeof global.ftsbMarketGetDiscount === 'function') discount = Number(global.ftsbMarketGetDiscount(server, String(stack.id)))
  if (!isFinite(price) || price <= 0 || !isFinite(discount) || discount < 0 || discount > 60) return 0
  return Math.floor(price * (100 - discount) / 100)
}

function ftwwFoodCatalog(server) {
  let foods = []
  for (let index = 0; index < FTWW_FOOD_REWARDS.length; index++) {
    let food = Item.of(FTWW_FOOD_REWARDS[index].item)
    if (food.isEmpty()) continue
    let unitValue = ftwwFoodUnitValue(server, food)
    if (unitValue > 0) foods.push({ stack: food, unitValue: unitValue })
  }
  if (foods.length < FTWW_FOOD_VARIETY) throw new Error('Not enough registered priced foods')
  return foods
}

function ftwwPrepareWishReward(event, outcome, stack, preparation) {
  let plan = { outcome: outcome, rewards: [], jackpot: false, foodValue: 0 }
  if (outcome.type === 'nothing') return plan
  if (!stack || stack.isEmpty()) throw new Error('Wish reward is unavailable')
  let id = String(stack.id)
  let fixed = outcome.exactCoins === true || outcome.type === 'spell' || outcome.type === 'weapon' || id === 'irons_spellbooks:scroll'
    || (outcome.type === 'manor_reward' && id.indexOf('ftboss:music_disc_') !== 0)
  if (fixed) {
    plan.rewards.push(ftwwPlanWishItem(stack, Number(stack.count)))
    return plan
  }
  if (outcome.type !== 'junk' && outcome.type !== 'poop') plan.jackpot = Math.random() < FTWW_JACKPOT_CHANCE
  let bonus = plan.jackpot ? FTWW_JACKPOT_MULTIPLIER : 1
  if (outcome.type === 'food') {
    let catalog = preparation && preparation.foods ? preparation.foods : ftwwFoodCatalog(event.server)
    if (preparation) preparation.foods = catalog
    let foods = catalog.slice()
    let minimumPerDish = Math.ceil(FTWW_MIN_FOOD_VALUE / FTWW_FOOD_VARIETY)
    let baseCount = Math.ceil(Number(stack.count) * FTWW_REWARD_MULTIPLIER / FTWW_FOOD_VARIETY)
    for (let index = 0; index < FTWW_FOOD_VARIETY; index++) {
      let choice = Math.floor(Math.random() * foods.length)
      let food = foods.splice(choice, 1)[0]
      let count = Math.max(baseCount, Math.ceil(minimumPerDish / food.unitValue)) * bonus
      plan.rewards.push(ftwwPlanWishItem(food.stack, count))
      plan.foodValue += food.unitValue * count
    }
    if (plan.foodValue < FTWW_MIN_FOOD_VALUE * bonus) throw new Error('Food value guarantee failed')
    return plan
  }
  plan.rewards.push(ftwwPlanWishItem(stack, Number(stack.count) * FTWW_REWARD_MULTIPLIER * bonus))
  return plan
}

function ftwwPrepareEmeraldWish(event, forcedOutcome) {
  let outcome = forcedOutcome || ftwwRollOutcome(event.server, event.player)
  if (outcome.type === 'nothing') return ftwwPrepareWishReward(event, outcome, null)
  let reward = ftwwOutcomeEntry(outcome)
  return ftwwPrepareWishReward(event, outcome, Item.of(String(reward.item), ftwwRandomCount(reward)))
}

function ftwwPrepareEmeraldWishOrReport(event) {
  try {
    if (ftwwShopMenuOpen(event.player)) throw new Error('Close the shop before wishing')
    return ftwwPrepareEmeraldWish(event)
  } catch (error) {
    console.error('[FarmingTales][WishingWell] Wish preparation failed: ' + error)
    ftwwTell(event.player, '§c奖励或食物价格暂不可用，请关闭商店后重试；本次未消耗绿宝石。')
    return null
  }
}

function ftwwDeliverWish(event, plan, trigger) {
  if (plan.jackpot) {
    ftwwTell(event.player, '§6§l中大奖！§r§e 本次有用实物在三倍奖励基础上再乘 10！')
    event.server.runCommandSilent(`playsound minecraft:ui.toast.challenge_complete player @a ${event.block.x} ${event.block.y} ${event.block.z} 1 1`)
  }
  if (plan.foodValue > 0) ftwwTell(event.player, `§a丰盛料理礼包：${FTWW_FOOD_VARIETY} 种料理，当前保守出货估值 ${plan.foodValue} 金币。`)
  if (!plan.rewards.length) ftwwPlayResult(event, plan.outcome, null, trigger)
  for (let index = 0; index < plan.rewards.length; index++) {
    let reward = plan.rewards[index]
    ftwwPlayResult(event, plan.outcome, reward.stack, trigger, reward.deliveries)
  }
}

function ftwwPlayResult(event, outcome, stack, trigger, deliveries) {
  let rewardLog = stack ? (String(stack.id) + ' x' + Number(stack.count)) : 'none'
  try { console.info('[FarmingTales][WishingWell] trigger=' + String(trigger || 'unknown') + ' player=' + String(event.player.username) + ' outcome=' + String(outcome.type) + ' reward=' + rewardLog) } catch (error) {}

  let x = Number(event.block.x) + 0.5
  let y = Number(event.block.y) + 1.0
  let z = Number(event.block.z) + 0.5

  if (outcome.type === 'nothing') {
    event.server.runCommandSilent(`particle minecraft:smoke ${x} ${y} ${z} 0.35 0.25 0.35 0.02 18 force @a`)
    event.server.runCommandSilent(`playsound minecraft:block.fire.extinguish block @a ${x} ${y} ${z} 0.7 0.7`)
    let emptyMessages = [
      '§7水面恢复了平静……这次什么也没有。',
      '§8绿宝石沉入井底，愿望没有回应。',
      '§7井中传来一声回响，仅此而已。'
    ]
    ftwwTell(event.player, emptyMessages[Math.floor(Math.random() * emptyMessages.length)])
    return
  }

  let name = String(stack.id)
  try { name = stack.displayName.getString() } catch (error) {}
  let count = Number(stack.count)
  let stacks = deliveries || [stack]
  for (let index = 0; index < stacks.length; index++) event.player.give(stacks[index])

  if (outcome.type === 'poop') {
    event.server.runCommandSilent(`particle minecraft:poof ${x} ${y} ${z} 0.35 0.35 0.35 0.05 24 force @a`)
    event.server.runCommandSilent(`playsound minecraft:entity.slime.squish block @a ${x} ${y} ${z} 0.9 0.65`)
    ftwwTell(event.player, `§6愿望实现了？§r 获得 §f${name} ×${count}`)
  } else if (outcome.type === 'coin') {
    event.server.runCommandSilent(`particle farmersdelight:star ${x} ${y} ${z} 0.45 0.45 0.45 0.08 18 force @a`)
    event.server.runCommandSilent(`playsound minecraft:entity.experience_orb.pickup block @a ${x} ${y} ${z} 0.9 1.2`)
    ftwwTell(event.player, `§6井底闪过金光：获得 §e${name} ×${count}`)
  } else if (outcome.type === 'seed_bag') {
    event.server.runCommandSilent(`particle minecraft:happy_villager ${x} ${y} ${z} 0.5 0.35 0.5 0.08 28 force @a`)
    event.server.runCommandSilent(`playsound minecraft:item.crop.plant block @a ${x} ${y} ${z} 1 1.05`)
    ftwwTell(event.player, `§a井水托起一份生机：获得 §f${name} ×${count}`)
  } else if (outcome.type === 'mineral') {
    event.server.runCommandSilent(`particle minecraft:glow ${x} ${y} ${z} 0.45 0.45 0.45 0.08 26 force @a`)
    event.server.runCommandSilent(`playsound minecraft:block.amethyst_block.chime block @a ${x} ${y} ${z} 1 1.15`)
    ftwwTell(event.player, `§b罕见愿望：获得 §f${name} ×${count}`)
  } else if (outcome.type === 'emerald') {
    event.server.runCommandSilent(`particle minecraft:firework ${x} ${y} ${z} 0.55 0.65 0.55 0.12 40 force @a`)
    event.server.runCommandSilent(`particle minecraft:happy_villager ${x} ${y} ${z} 0.55 0.55 0.55 0.08 24 force @a`)
    event.server.runCommandSilent(`playsound minecraft:ui.toast.challenge_complete block @a ${x} ${y} ${z} 0.8 1.1`)
    ftwwTell(event.player, `§a§l愿望成真！§r§a 获得 ${name} ×${count}`)
  } else if (outcome.type === 'geode') {
    event.server.runCommandSilent(`particle minecraft:reverse_portal ${x} ${y} ${z} 0.55 0.65 0.55 0.08 55 force @a`)
    event.server.runCommandSilent(`particle minecraft:glow ${x} ${y} ${z} 0.45 0.45 0.45 0.05 32 force @a`)
    event.server.runCommandSilent(`playsound minecraft:block.amethyst_block.chime block @a ${x} ${y} ${z} 1 0.75`)
    ftwwTell(event.player, `§d§l井水凝成晶球！§r§d 获得 ${name} ×${count}`)
  } else if (outcome.type === 'manor_reward') {
    event.server.runCommandSilent(`particle minecraft:firework ${x} ${y} ${z} 0.55 0.65 0.55 0.12 40 force @a`)
    event.server.runCommandSilent(`playsound minecraft:ui.toast.challenge_complete block @a ${x} ${y} ${z} 0.8 1.1`)
    ftwwTell(event.player, `§6§l庄园奇迹（0.035%）！§r§d 获得庄园战奖励 ${name} ×${count}`)
  } else {
    event.server.runCommandSilent(`particle minecraft:splash ${x} ${y} ${z} 0.45 0.25 0.45 0.08 22 force @a`)
    event.server.runCommandSilent(`playsound minecraft:entity.item.pickup block @a ${x} ${y} ${z} 0.8 0.9`)
    ftwwTell(event.player, `§7许愿结果：获得 §f${name} ×${count}`)
  }

  try { event.player.sendData('IAdisplay', `{"item":"${String(stack.id)}"}`) } catch (error) {}
}


function ftwwResolveWish(event, trigger, forcedOutcome, preparedWish) {
  let plan = preparedWish || ftwwPrepareEmeraldWish(event, forcedOutcome)
  let x = Number(event.block.x) + 0.5
  let y = Number(event.block.y) + 0.9
  let z = Number(event.block.z) + 0.5
  event.server.runCommandSilent(`particle minecraft:enchant ${x} ${y} ${z} 0.55 0.35 0.55 0.18 36 force @a`)
  event.server.runCommandSilent(`playsound minecraft:entity.item.pickup block @a ${x} ${y} ${z} 0.8 0.55`)

  ftwwDeliverWish(event, plan, trigger)
}

function ftwwDroppedStack(entity) {
  try { return entity.item } catch (error) {}
  try { return entity.getItem() } catch (error) {}
  return null
}

function ftwwDroppedStackId(stack) {
  if (!stack) return 'minecraft:air'
  try { if (stack.isEmpty()) return 'minecraft:air' } catch (error) {}
  try { return String(stack.id) } catch (error) {}
  try { return String(stack.getId()) } catch (error) {}
  return 'minecraft:air'
}

function ftwwDroppedStackCount(stack) {
  try { return Number(stack.count) } catch (error) {}
  try { return Number(stack.getCount()) } catch (error) {}
  return 0
}

function ftwwSetDroppedStack(entity, itemId, count) {
  let replacement = Item.of(itemId, count)
  try {
    entity.item = replacement
    return true
  } catch (error) {}
  try {
    entity.setItem(replacement)
    return true
  } catch (error) {}
  return false
}

function ftwwPayEmerald(event, stack, entity) {
  let count = ftwwDroppedStackCount(stack)
  if (ftwwDroppedStackId(stack) !== FTWW_PAYMENT || count < 1) return false
  if (!entity && event.player.isCreative()) return true
  let progress = ftwwProgressOrReport(event.server, event.player)
  if (!progress) return false
  if (Number(progress.emeralds) + 1 > FTWW_PROGRESS_MAX) {
    ftwwTell(event.player, '§c累计绿宝石已达上限，本次未消耗绿宝石。')
    return false
  }
  progress.emeralds++
  if (entity) {
    if (!ftwwSetDroppedStack(entity, FTWW_PAYMENT, count - 1)) return false
  } else stack.shrink(1)
  try {
    ftwwSaveProgress(event.server, event.player, progress)
  } catch (error) {
    if (entity) ftwwSetDroppedStack(entity, FTWW_PAYMENT, count)
    else stack.count = count
    ftwwTell(event.player, '§c投币记录保存失败，绿宝石已退回。')
    console.error('[FarmingTales][WishingWell] Wish payment rolled back: ' + error)
    return false
  }
  if (entity && count === 1) entity.discard()
  return true
}

function ftwwEntityRemoved(entity) {
  if (!entity) return true
  try { if (entity.isRemoved()) return true } catch (error) {}
  try { if (!entity.isAlive()) return true } catch (error) {}
  return false
}

function ftwwFindCoreForDroppedEmerald(level, entity) {
  let ex = Math.floor(Number(entity.x))
  let ey = Math.floor(Number(entity.y))
  let ez = Math.floor(Number(entity.z))

  // Item entities float at the water surface, so either their current block or
  // the block directly below may be one of the blueprint's eight water cells.
  for (let waterY = ey; waterY >= ey - 1; waterY--) {
    if (ftwwBlockId(level, ex, waterY, ez) !== FTWW_WATER) continue
    for (let cx = ex - 1; cx <= ex + 1; cx++) {
      for (let cz = ez - 1; cz <= ez + 1; cz++) {
        if (cx === ex && cz === ez) continue
        if (ftwwBlockId(level, cx, waterY, cz) !== FTWW_CORE) continue
        return level.getBlock(cx, waterY, cz)
      }
    }
  }
  return null
}

function ftwwNearestPlayer(server, level, x, y, z) {
  let nearest = null
  let nearestDistance = FTWW_DROP_PLAYER_RADIUS * FTWW_DROP_PLAYER_RADIUS

  function consider(player) {
    if (!player) return
    try {
      let dx = Number(player.x) - x
      let dy = Number(player.y) - y
      let dz = Number(player.z) - z
      let distance = dx * dx + dy * dy + dz * dz
      if (distance <= nearestDistance) {
        nearestDistance = distance
        nearest = player
      }
    } catch (error) {}
  }

  try {
    level.getEntitiesWithin({
      type: 'minecraft:player',
      x: x,
      y: y,
      z: z,
      radius: FTWW_DROP_PLAYER_RADIUS
    }).forEach(consider)
  } catch (error) {}

  if (nearest) return nearest
  try {
    server.players.forEach(player => {
      try {
        if (String(player.level.dimension) !== String(level.dimension)) return
      } catch (error) {}
      consider(player)
    })
  } catch (error) {}
  return nearest
}

// Returns true when this tracked entity no longer needs to be checked.
function ftwwTryDroppedEmerald(server, entity) {
  if (ftwwEntityRemoved(entity)) return true
  let stack = ftwwDroppedStack(entity)
  let isGoldPot = ftwwDroppedStackId(stack) === FTWW_GOLD_POT
  let isUpgrade = ftwwDroppedStackId(stack) === FTWW_UPGRADE_PAYMENT
  if ((!isGoldPot && !isUpgrade && ftwwDroppedStackId(stack) !== FTWW_PAYMENT) || ftwwDroppedStackCount(stack) < 1) return true

  let level = null
  try { level = entity.level } catch (error) {}
  if (!level) return false

  let core = ftwwFindCoreForDroppedEmerald(level, entity)
  if (!core) return false

  let x = Number(entity.x)
  let y = Number(entity.y)
  let z = Number(entity.z)
  let player = null
  try { player = entity.getOwner() } catch (error) {
    try { player = entity.m_19749_() } catch (ownerError) {}
  }
  if (player && String(player.type) !== 'minecraft:player') player = null
  if (player) {
    let distance = Math.pow(Number(player.x) - x, 2) + Math.pow(Number(player.y) - y, 2) + Math.pow(Number(player.z) - z, 2)
    if (distance > FTWW_DROP_PLAYER_RADIUS * FTWW_DROP_PLAYER_RADIUS || String(player.level.dimension) !== String(level.dimension)) return false
  }
  if (!player && !isGoldPot && !isUpgrade) player = ftwwNearestPlayer(server, level, x, y, z)
  if (!player) return false

  let syntheticEvent = { server: server, level: level, block: core, player: player }
  let missing = ftwwCheckStructure(level, core)
  if (missing) {
    ftwwHighlightMissing(syntheticEvent, missing)
    ftwwTell(player, `§c投入未生效：许愿井结构不完整。相对坐标 §f(${missing.dx}, ${missing.dy}, ${missing.dz}) §c需要§f${missing.expected}§c，当前为§8${missing.found}`)
    try { console.warn('[FarmingTales][WishingWell] trigger=drop rejected=structure player=' + String(player.username) + ' missing=(' + missing.dx + ',' + missing.dy + ',' + missing.dz + ') expected=' + missing.expected + ' found=' + missing.found) } catch (error) {}
    return true
  }

  if (isUpgrade || isGoldPot) {
    let progress = ftwwProgressOrReport(server, player)
    if (!progress) return true
    if (isUpgrade && ftwwLevel(progress) === 3) {
      ftwwTell(player, '§a商店和金币锅许愿已经解锁，无需继续投入绿宝石块。')
      return true
    }
    if (isGoldPot && ftwwLevel(progress) !== 3) {
      ftwwTell(player, '§e请先累计投入 64 个绿宝石块，再重新投入金币锅许愿；本次未消耗金币锅。')
      return true
    }
    if (isUpgrade) return ftwwDonateUpgradeBlocks(syntheticEvent, stack, ftwwDroppedStackCount(stack), entity)
    return ftwwDonatePots(syntheticEvent, stack, ftwwDroppedStackCount(stack), entity)
  }

  let preparedWish = ftwwPrepareEmeraldWishOrReport(syntheticEvent)
  if (!preparedWish) return true
  if (!ftwwPayEmerald(syntheticEvent, stack, entity)) {
    try { console.error('[FarmingTales][WishingWell] trigger=drop rejected=consume_failed player=' + String(player.username)) } catch (error) {}
    return true
  }

  try { console.info('[FarmingTales][WishingWell] trigger=drop accepted player=' + String(player.username) + ' core=(' + core.x + ',' + core.y + ',' + core.z + ')') } catch (error) {}
  try { player.swing() } catch (error) {}
  ftwwResolveWish(syntheticEvent, 'drop', null, preparedWish)
  return true
}

EntityEvents.spawned(event => {
  let entity = event.entity
  if (!entity || String(entity.type) !== 'minecraft:item') return
  let stack = ftwwDroppedStack(entity)
  if (ftwwDroppedStackId(stack) !== FTWW_PAYMENT && ftwwDroppedStackId(stack) !== FTWW_GOLD_POT
      && ftwwDroppedStackId(stack) !== FTWW_UPGRADE_PAYMENT) return

  global.ftwwPendingEmeraldDrops.push({ entity: entity, age: 0 })
  try { console.info('[FarmingTales][WishingWell] trigger=drop queued count=' + ftwwDroppedStackCount(stack)) } catch (error) {}
})

function ftwwAdvanceInteractionTick() {
  let current = Number(global.ftwwDropScanTick || 0)
  if (!isFinite(current) || current < 0 || current > 9007199254740980 || Math.floor(current) !== current) current = 0
  current = current + 1
  global.ftwwDropScanTick = current
  return current
}

ServerEvents.tick(event => {
  let interactionTick = ftwwAdvanceInteractionTick()
  ftwwProcessShopQueue(event.server, interactionTick)
  if (interactionTick % FTWW_DROP_SCAN_INTERVAL !== 0) return

  let pending = global.ftwwPendingEmeraldDrops || []
  let retained = []
  for (let i = 0; i < pending.length; i++) {
    let entry = pending[i]
    try {
      if (!entry || !entry.entity) continue
      entry.age = Number(entry.age || 0) + FTWW_DROP_SCAN_INTERVAL
      if (entry.age > FTWW_DROP_TRACK_TICKS) continue
      if (!ftwwTryDroppedEmerald(event.server, entry.entity)) {
        entry.failures = 0
        retained.push(entry)
      }
    } catch (error) {
      entry.failures = Number(entry.failures || 0) + 1
      try { console.error('[FarmingTales][WishingWell] dropped entry scan failed attempt=' + entry.failures + ' error=' + error) } catch (ignored) {}
      if (entry.failures < 3) retained.push(entry)
    }
  }
  global.ftwwPendingEmeraldDrops = retained
})


function ftwwPlayerKey(player) {
  try { if (player.uuid) return String(player.uuid) } catch (error) {}
  try {
    let uuid = player.getUuid()
    if (uuid) return String(uuid)
  } catch (error) {}
  throw new Error('Wishing well progress requires a player UUID')
}

const FTWW_PROGRESS_FIELDS = ['emeralds', 'goldPots', 'emeraldBlocks']
const FTWW_PROGRESS_MAX = 9007199254740991

function ftwwProgressText(server, player) {
  let raw = null
  try { raw = server.persistentData.getString(FTWW_PROGRESS_PREFIX + ftwwPlayerKey(player)) } catch (error) {
    throw new Error('Wishing well progress is unreadable: ' + error)
  }
  if (raw === null || raw === undefined) return ''
  return String(raw).trim()
}

function ftwwProgress(server, player) {
  let text = ftwwProgressText(server, player)
  if (!text) return { emeralds: 0, goldPots: 0, emeraldBlocks: 0 }
  let parsed = null
  try { parsed = JSON.parse(text) } catch (error) {
    throw new Error('Wishing well progress is not valid JSON: ' + text)
  }
  if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
    throw new Error('Wishing well progress is not an object: ' + text)
  }
  let progress = {}
  for (let index = 0; index < FTWW_PROGRESS_FIELDS.length; index++) {
    let field = FTWW_PROGRESS_FIELDS[index]
    let value = parsed[field]
    if (value === undefined) { progress[field] = 0; continue }
    if (typeof value !== 'number' || !isFinite(value) || value < 0 || value > FTWW_PROGRESS_MAX || Math.floor(value) !== value) {
      throw new Error('Wishing well progress field is invalid: ' + field + '=' + String(value))
    }
    progress[field] = value
  }
  return progress
}

function ftwwProgressOrReport(server, player) {
  try {
    return ftwwProgress(server, player)
  } catch (error) {
    try { console.error('[FarmingTales][WishingWell] progress rejected player=' + String(player && player.username) + ' error=' + error) } catch (ignored) {}
    ftwwTell(player, '§c许愿井数据异常，已中止本次操作，未扣除任何物品。')
    return null
  }
}

function ftwwSafeLevel(server, player) {
  try {
    return ftwwLevel(ftwwProgress(server, player))
  } catch (error) {
    try { console.error('[FarmingTales][WishingWell] level lookup rejected error=' + error) } catch (ignored) {}
    return 0
  }
}

function ftwwSaveProgress(server, player, progress) {
  server.persistentData.putString(FTWW_PROGRESS_PREFIX + ftwwPlayerKey(player), JSON.stringify(progress))
}

function ftwwLevel(progress) {
  if (Number(progress.emeraldBlocks || 0) >= FTWW_UPGRADE_BLOCKS) return 3
  for (let level = FTWW_LEGACY_LEVEL_THRESHOLDS.length - 1; level > 0; level--) {
    if (progress.goldPots >= FTWW_LEGACY_LEVEL_THRESHOLDS[level]) return level
  }
  return 0
}

function ftwwUsefulChance(level) {
  let odds = FTWW_LEVEL_ODDS[level]
  return Math.round((100 - odds[0] - odds[1] - odds[4]) * 10) / 10
}

function ftwwShowProgress(server, player) {
  let progress = ftwwProgressOrReport(server, player)
  if (!progress) return false
  let level = ftwwLevel(progress)
  ftwwTell(player, `§6许愿井 Lv.${level} §7| 升级绿宝石块 ${progress.emeraldBlocks}/${FTWW_UPGRADE_BLOCKS}；许愿绿宝石 ${progress.emeralds}，金币锅 ${progress.goldPots}`)
  ftwwTell(player, `§a绿宝石有用物品概率：${ftwwUsefulChance(level)}% §7| ${level === 3 ? '已解锁：每个金币锅抽奖一次；空手右键核心打开商店' : '还需投入 ' + (FTWW_UPGRADE_BLOCKS - progress.emeraldBlocks) + ' 个绿宝石块解锁商店和金币锅许愿'}`)
  if (level === 3) {
    ftwwTell(player, '§7金币锅奖池：铁魔法 15%，食物 30%，金币 5%，晶球 15%，矿物块 25%，技能经验书 9.965%，庄园战奖励（武器/唱片/法术）0.035%。')
    ftwwTell(player, '§6金币档位：1 / 100 / 1000 / 10000 / 100000 / 1000000；默认每锅金币期望 9000，不叠加三倍或数量大奖。')
  }
}

function ftwwDonateUpgradeBlocks(event, stack, amount, droppedEntity) {
  let progress = ftwwProgressOrReport(event.server, event.player)
  if (!progress) return false
  if (ftwwLevel(progress) === 3) {
    ftwwTell(event.player, '§a商店和金币锅许愿已经解锁，无需继续投入绿宝石块。')
    return false
  }
  try {
    if (ftwwShopMenuOpen(event.player)) return false
  } catch (error) {
    ftwwTell(event.player, '§c暂时无法确认背包状态，本次未消耗绿宝石块。')
    return false
  }
  let previousCount = ftwwDroppedStackCount(stack)
  amount = Math.min(previousCount, Math.floor(Number(amount)), FTWW_UPGRADE_BLOCKS - progress.emeraldBlocks)
  if (!isFinite(amount) || amount < 1 || String(stack.id) !== FTWW_UPGRADE_PAYMENT) return false
  progress.emeraldBlocks += amount
  if (droppedEntity) {
    if (!ftwwSetDroppedStack(droppedEntity, FTWW_UPGRADE_PAYMENT, previousCount - amount)) return false
  } else {
    stack.shrink(amount)
  }
  try {
    ftwwSaveProgress(event.server, event.player, progress)
  } catch (error) {
    if (droppedEntity) ftwwSetDroppedStack(droppedEntity, FTWW_UPGRADE_PAYMENT, previousCount)
    else stack.count = previousCount
    console.error('[FarmingTales][WishingWell] Emerald block upgrade rolled back: ' + error)
    ftwwTell(event.player, '§c记录保存失败，绿宝石块已退回。')
    return false
  }
  if (droppedEntity && previousCount === amount) droppedEntity.discard()
  ftwwTell(event.player, `§6投入 ${amount} 个升级绿宝石块，个人累计 ${progress.emeraldBlocks}/${FTWW_UPGRADE_BLOCKS} 个。`)
  ftwwShowProgress(event.server, event.player)
  if (ftwwLevel(progress) === 3) {
    ftwwTell(event.player, '§d许愿井升至 Lv.3！商店和金币锅许愿永久解锁，每个金币锅抽奖一次。')
    ftwwSafeOpenShop(event)
  }
  return true
}

function ftwwDonatePots(event, stack, amount, droppedEntity) {
  let progress = ftwwProgressOrReport(event.server, event.player)
  if (!progress) return false
  if (ftwwLevel(progress) !== 3) {
    ftwwTell(event.player, '§e请先累计投入 64 个绿宝石块，解锁商店和金币锅许愿；本次未消耗金币锅。')
    return false
  }
  try {
    if (ftwwShopMenuOpen(event.player)) {
      ftwwTell(event.player, '§e请先关闭商店再投入金币锅，本次未消耗金币锅。')
      return false
    }
  } catch (error) {
    ftwwTell(event.player, '§c暂时无法确认背包状态，本次未消耗金币锅。')
    return false
  }
  let previousCount = ftwwDroppedStackCount(stack)
  amount = Math.min(previousCount, Math.floor(Number(amount)))
  if (!isFinite(amount) || amount < 1 || amount > 64 || String(stack.id) !== FTWW_GOLD_POT) return false
  if (Number(progress.goldPots) + amount > FTWW_PROGRESS_MAX) {
    ftwwTell(event.player, '§c累计金币锅已达上限，本次未消耗金币锅。')
    return false
  }
  let goldRewards = []
  let preparation = { foods: null }
  try {
    let wishes = Java.loadClass('cn.farmingtales.qysevent.wishingwell.GoldPotWishes')
    wishes.rollUnlockedRewards(event.level, amount).forEach(reward => {
      let rewardStack = Item.of(reward.stack())
      if (rewardStack.isEmpty()) throw new Error('Gold pot reward is empty')
      let category = String(reward.category())
      goldRewards.push(ftwwPrepareWishReward(event, { type: category, exactCoins: category === 'coin' }, rewardStack, preparation))
    })
    if (goldRewards.length !== amount) throw new Error('Gold pot reward count mismatch')
  } catch (error) {
    console.error('[FarmingTales][WishingWell] Gold pot rewards unavailable: ' + error)
    ftwwTell(event.player, '§c金币锅奖池尚未加载或奖励配置异常，本次未消耗金币锅。请确认已更新 Event 并重启游戏。')
    return false
  }
  progress.goldPots += amount
  if (droppedEntity) {
    if (!ftwwSetDroppedStack(droppedEntity, FTWW_GOLD_POT, previousCount - amount)) return false
  } else {
    stack.shrink(amount)
  }
  try {
    ftwwSaveProgress(event.server, event.player, progress)
  } catch (error) {
    if (droppedEntity) ftwwSetDroppedStack(droppedEntity, FTWW_GOLD_POT, previousCount)
    else stack.count = previousCount
    console.error('[FarmingTales][WishingWell] Donation rolled back: ' + error)
    ftwwTell(event.player, '§c记录保存失败，金币锅已退回。')
    return false
  }
  if (droppedEntity && previousCount === amount) droppedEntity.discard()
  ftwwTell(event.player, `§6投入 ${amount} 个金币锅，个人累计 ${progress.goldPots} 个。`)
  ftwwShowProgress(event.server, event.player)
  for (let index = 0; index < goldRewards.length; index++) {
    ftwwDeliverWish(event, goldRewards[index], 'gold_pot')
  }
  return true
}

function ftwwRandomWeapon() {
  let catalog = Java.loadClass('net.farmingtales.ftboss.item.ManorWeaponCatalog')
  let rewards = []
  catalog.ENTRIES.forEach(entry => {
    if (String(entry.item()) === 'ftboss:gourmet_blank_blade') return
    let stack = catalog.stack(entry)
    if (!stack.isEmpty()) rewards.push(stack)
  })
  if (!rewards.length) throw new Error('No registered manor weapons')
  return rewards[Math.floor(Math.random() * rewards.length)].copy()
}

function ftwwRandomSpell() {
  let catalog = Java.loadClass('net.farmingtales.ftboss.integ.spells.ManorIronSpells')
  let kinds = Java.loadClass('net.farmingtales.ftboss.util.ManorSpellRules$Kind').values()
  let container = Java.loadClass('io.redspace.ironsspellbooks.api.spells.ISpellContainer')
  let spells = []
  for (let index = 0; index < kinds.length; index++) {
    let spell = catalog.spell(kinds[index])
    if (!spell || !spell.isEnabled()) continue
    if (String(spell.getSpellId()).indexOf('ftboss:') !== 0) throw new Error('Non-manor spell in manor catalog')
    if (spell.getMaxLevel() >= spell.getMinLevel() && spell.getMinLevel() >= 1) spells.push(spell)
  }
  if (!spells.length) throw new Error('No enabled manor spells')
  let spell = spells[Math.floor(Math.random() * spells.length)]
  let minimum = Number(spell.getMinLevel())
  let maximum = Number(spell.getMaxLevel())
  if (!isFinite(minimum) || !isFinite(maximum) || minimum < 1 || maximum < minimum || Math.floor(minimum) !== minimum || Math.floor(maximum) !== maximum) {
    throw new Error('Manor spell level range is invalid: ' + String(spell.getSpellId()))
  }
  let level = minimum + Math.floor(Math.random() * (maximum - minimum + 1))
  let stack = Item.of('irons_spellbooks:scroll')
  if (stack.isEmpty()) throw new Error('Scroll item is unavailable')
  container.createScrollContainer(spell, level, stack)
  ftwwVerifyScrollContainer(container, stack, spell, level)
  return stack
}

function ftwwVerifyScrollContainer(container, stack, spell, level) {
  let written = null
  try { written = container.get(stack) } catch (error) { written = null }
  if (!written) throw new Error('Scroll container was not written')
  let slots = null
  try { slots = written.getAllSpells() } catch (error) { slots = null }
  if (!slots || !slots.length) throw new Error('Scroll container is empty after creation')
  for (let index = 0; index < slots.length; index++) {
    let slot = slots[index]
    if (!slot) continue
    let slotSpell = null
    let slotLevel = -1
    try { slotSpell = slot.getSpell() } catch (error) { slotSpell = null }
    try { slotLevel = Number(slot.getLevel()) } catch (error) { slotLevel = -1 }
    if (!slotSpell) continue
    if (String(slotSpell.getSpellId()) === String(spell.getSpellId()) && slotLevel === level) return true
  }
  throw new Error('Scroll contents do not match the rolled spell ' + String(spell.getSpellId()) + ' level ' + level)
}

function ftwwShopAvailable(event) {
  let player = event.player
  if (String(player.level.dimension) !== String(event.level.dimension)) return false
  let distance = Math.pow(Number(player.x) - Number(event.block.x) - 0.5, 2)
    + Math.pow(Number(player.y) - Number(event.block.y) - 0.5, 2)
    + Math.pow(Number(player.z) - Number(event.block.z) - 0.5, 2)
  return distance <= 64 && ftwwBlockId(event.level, event.block.x, event.block.y, event.block.z) === FTWW_CORE
    && ftwwSafeLevel(event.server, player) === 3 && !ftwwCheckStructure(event.level, event.block)
}

function ftwwManorShopUnlocked(event) {
  if (ftwwSafeLevel(event.server, event.player) !== 3) return false
  try {
    let access = Java.loadClass('cn.farmingtales.qysevent.wishingwell.ManorShopAccess')
    return access.hasCrimsonContract(event.player) === true
  } catch (error) {
    try { console.error('[FarmingTales][WishingWell] manor unlock lookup failed: ' + error) } catch (ignored) {}
    return false
  }
}

function ftwwShopMenuOpen(player) {
  let menu = player.openInventory
  if (menu === null || menu === undefined) return false
  let menuClass = Java.loadClass('dev.latvian.mods.kubejs.gui.chest.CustomChestMenu')
  return menu instanceof menuClass
}

function ftwwRejectShopRequest(player, message) {
  try { ftwwTell(player, '§c' + String(message)) } catch (tellError) {
    try { console.error('[FarmingTales][WishingWell] shop request message failed: ' + tellError) } catch (ignored) {}
  }
  return false
}

function ftwwQueuePurchase(event, kind, itemId) {
  let player = event.player
  if ((kind === 'weapon' || kind === 'spell') && !ftwwManorShopUnlocked(event)) {
    return ftwwRejectShopRequest(player, '庄园购入尚未解锁：需要满级许愿井并获取猩红契约；未扣款。')
  }
  let key = null
  try {
    key = ftwwPlayerKey(player)
    ftwwShopMenuOpen(player)
  } catch (error) {
    try { console.error('[FarmingTales][WishingWell] shop request rejected error=' + error) } catch (ignored) {}
    return ftwwRejectShopRequest(player, '许愿井商店状态无法确认，本次未扣款，请关闭界面后重试。')
  }
  let queue = global.ftwwPurchaseQueue || []
  let now = Number(global.ftwwDropScanTick || 0)
  for (let index = 0; index < queue.length; index++) {
    let pending = queue[index]
    try { if (pending && String(pending.key) === key) return ftwwRejectShopRequest(player, '已有一笔购买正在结算，请稍候。') } catch (entryError) {}
  }
  let level = event.level
  let block = event.block
  if (!level || !block) return ftwwRejectShopRequest(player, '许愿井坐标不可用，本次未扣款。')
  let blockRef = { x: Number(block.x), y: Number(block.y), z: Number(block.z) }
  if (!isFinite(blockRef.x) || !isFinite(blockRef.y) || !isFinite(blockRef.z)) return ftwwRejectShopRequest(player, '许愿井坐标不可用，本次未扣款。')
  queue.push({
    key: key, server: event.server, player: player, level: level, block: blockRef,
    kind: String(kind), itemId: itemId === undefined || itemId === null ? null : String(itemId), tick: now
  })
  global.ftwwPurchaseQueue = queue
  let closed = false
  try {
    player.closeContainer()
    closed = !ftwwShopMenuOpen(player)
  } catch (closeError) {
    try { console.error('[FarmingTales][WishingWell] shop closeContainer failed: ' + closeError) } catch (ignored) {}
  }
  if (!closed) ftwwTell(player, '§7订单已记录：请按 Esc 关闭商店界面，关闭后立即结算。')
  return true
}

function ftwwShopQueueReject(player, message) {
  try { ftwwTell(player, '§c' + String(message)) } catch (tellError) {
    try { console.error('[FarmingTales][WishingWell] shop queue message failed: ' + tellError) } catch (ignored) {}
  }
  return true
}

function ftwwProcessShopQueue(server, now) {
  let queue = global.ftwwPurchaseQueue || []
  global.ftwwPurchaseQueue = []
  if (!queue.length) return
  for (let index = 0; index < queue.length; index++) {
    let entry = queue[index]
    try {
      if (!entry || !entry.player || !entry.server || !entry.level || !entry.block) continue
      let player = entry.player
      if (ftwwPlayerKey(player) !== String(entry.key)) continue
      if (!server || server.players.indexOf(player) < 0) continue
      if (!player.isAlive() || player.isSpectator()) continue
      let queuedTick = Number(entry.tick)
      if (!isFinite(queuedTick)) queuedTick = now
      if (ftwwShopMenuOpen(player)) {
        if (now - queuedTick >= FTWW_QUEUE_MAX_TICKS) {
          ftwwShopQueueReject(player, '购买已取消：商店界面在超时前没有关闭，未扣除任何物品。')
          continue
        }
        global.ftwwPurchaseQueue.push(entry)
        continue
      }
      if (now <= queuedTick) {
        global.ftwwPurchaseQueue.push(entry)
        continue
      }
      let purchaseEvent = { server: server, player: player, level: entry.level, block: entry.block }
      let purchased = false
      try {
        purchased = ftwwPurchase(purchaseEvent, entry.kind, entry.itemId)
      } catch (purchaseError) {
        try { console.error('[FarmingTales][WishingWell] queued purchase failed error=' + purchaseError) } catch (ignored) {}
      }
      if (!purchased) ftwwShopQueueReject(player, '订单处理失败，请查看购买反馈及服务端日志。')
    } catch (entryError) {
      try { console.error('[FarmingTales][WishingWell] queued purchase entry failed error=' + entryError) } catch (ignored) {}
    }
  }
}

function ftwwPurchase(event, kind, itemId) {
  let available = false
  try { available = ftwwShopAvailable(event) } catch (error) {
    try { console.error('[FarmingTales][WishingWell] shop availability check failed error=' + error) } catch (ignored) {}
    ftwwTell(event.player, '§c许愿井数据异常，无法购买；未扣款。')
    return false
  }
  if (!available) {
    ftwwTell(event.player, '§c请靠近结构完整的许愿井，并累计投入 64 个绿宝石块解锁商店。')
    return false
  }
  if ((kind === 'weapon' || kind === 'spell') && !ftwwManorShopUnlocked(event)) {
    return ftwwRejectShopRequest(event.player, '庄园购入尚未解锁：需要满级许愿井并获取猩红契约；未扣款。')
  }
  let menuOpen = false
  try {
    menuOpen = ftwwShopMenuOpen(event.player)
  } catch (menuError) {
    try { console.error('[FarmingTales][WishingWell] open menu state unreadable: ' + menuError) } catch (ignored) {}
    return ftwwRejectShopRequest(event.player, '商店界面状态无法确认，未扣款。')
  }
  if (menuOpen) return ftwwRejectShopRequest(event.player, '请先关闭商店界面，再完成购买；未扣款。')
  let player = event.player
  let key = ftwwPlayerKey(player)
  let now = Number(global.ftwwDropScanTick || 0)
  if (now < Number(global.ftwwShopCooldowns[key] || 0)) return false
  global.ftwwShopCooldowns[key] = now + 10
  let target = { inventory: player.inventory, handler: null, snapshot: null }
  let wallet = null
  let balance = null
  let charged = false
  let slots = 0
  try {
    let reward
    let potPayments = []
    let price = 0
    let inventory = target.inventory
    target.handler = ftwwMainInventoryWrapper(inventory, player)
    slots = ftwwMainInventorySlots()
    if (kind === 'summon') {
      if (FTWW_SUMMONS.indexOf(String(itemId)) < 0) throw new Error('Not a non-manor summon')
      reward = Item.of(itemId)
      let remaining = FTWW_SUMMON_PRICE
      for (let slot = 0; slot < slots && remaining > 0; slot++) {
        let stack = inventory.getStackInSlot(slot)
        if (String(stack.id) !== FTWW_GOLD_POT || Number(stack.count) <= 0) continue
        let amount = Math.min(Number(stack.count), remaining)
        potPayments.push({ slot: slot, amount: amount })
        remaining -= amount
      }
      if (remaining > 0) { ftwwTell(player, `§c购买需要 ${FTWW_SUMMON_PRICE} 个金币锅。`); return false }
    } else {
      if (kind !== 'weapon' && kind !== 'spell') throw new Error('Unknown shop product')
      wallet = Java.loadClass('net.sixik.sdmshoprework.SDMShopR')
      balance = Number(wallet.getMoney(player))
      price = kind === 'weapon' ? FTWW_WEAPON_PRICE : FTWW_SPELL_PRICE
      if (!isFinite(balance) || balance > 9007199254740991 || Math.floor(balance) !== balance) throw new Error('Unsafe SDM balance')
      if (balance < price) { ftwwTell(player, `§cSDM 余额不足，需要 ${price}，当前 ${balance}。`); return false }
      reward = kind === 'weapon' ? ftwwRandomWeapon() : ftwwRandomSpell()
    }
    if (!reward || reward.isEmpty()) throw new Error('Reward is unavailable')
    if (!ftwwCanFit(target.handler, reward)) {
      ftwwTell(player, '§c背包空间不足，请先留出空位；未扣款。')
      return false
    }
    target.snapshot = ftwwSnapshotMainInventory(inventory)
    let ownedBefore = ftwwCountMainInventory(inventory, reward)
    if (kind === 'summon') {
      for (let payment of potPayments) inventory.getStackInSlot(payment.slot).shrink(payment.amount)
    } else {
      charged = true
      wallet.setMoney(player, balance - price)
      if (Number(wallet.getMoney(player)) !== balance - price) throw new Error('SDM debit verification failed')
    }
    let remainder = target.handler.insertItem(reward.copy(), false)
    if (remainder && !remainder.isEmpty()) throw new Error('Reward insertion returned a remainder of ' + String(remainder.count))
    let ownedAfter = ftwwCountMainInventory(inventory, reward)
    if (ownedAfter - ownedBefore !== Number(reward.count)) {
      throw new Error('Reward not present in the target inventory after insert expected=' + Number(reward.count) + ' delta=' + (ownedAfter - ownedBefore))
    }
    ftwwSyncMainInventory(target.handler, player)
    try { ftwwTell(player, '§a购买成功：' + String(reward.displayName.getString())) } catch (tellError) {
      try { console.error('[FarmingTales][WishingWell] purchase success message failed: ' + tellError) } catch (ignored) {}
    }
    try { console.info('[FarmingTales][WishingWell] purchase player=' + String(player.username) + ' kind=' + kind + ' reward=' + String(reward.id) + ' price=' + (kind === 'summon' ? FTWW_SUMMON_PRICE + ' gold pots' : price) + ' delivered=' + Number(reward.count) + ' slots=' + slots) } catch (logError) {
      try { console.error('[FarmingTales][WishingWell] purchase log failed: ' + logError) } catch (ignored) {}
    }
    return true
  } catch (error) {
    let failedSlots = []
    try {
      failedSlots = ftwwRestoreMainInventory(target.inventory, target.snapshot) || []
    } catch (rollbackError) {
      console.error('[FarmingTales][WishingWell] Purchase inventory rollback failed: ' + rollbackError)
      failedSlots = ['unknown']
    }
    if (failedSlots.length > 0) {
      console.error('[FarmingTales][WishingWell] Purchase inventory rollback incomplete slots=' + failedSlots.join(','))
      try { ftwwTell(player, '§c购买失败：物品回滚异常，请在服务端日志查看，如已扣款请联系管理员。') } catch (slotTellError) {}
    }
    if (charged && wallet) {
      let refunded = false
      try {
        wallet.setMoney(player, balance)
        refunded = Number(wallet.getMoney(player)) === Number(balance)
      } catch (refundError) {
        console.error('[FarmingTales][WishingWell] Purchase refund failed: ' + refundError)
      }
      if (!refunded) {
        console.error('[FarmingTales][WishingWell] Purchase refund could not be verified amount=' + Number(balance))
        try { ftwwTell(player, '§c购买失败且退款未确认，请在服务端日志查看。') } catch (refundTellError) {}
      }
    }
    try { ftwwSyncMainInventory(target.handler, player) } catch (syncError) { console.error('[FarmingTales][WishingWell] Purchase rollback sync failed: ' + syncError) }
    console.error('[FarmingTales][WishingWell] Purchase failed: ' + error)
    try { ftwwTell(player, '§c购买失败，已尝试退还扣款；请查看服务端日志。') } catch (tellError) {}
    return false
  }
}

function ftwwCanFit(handler, reward) {
  let remainder = handler.insertItem(reward.copy(), true)
  return !remainder || remainder.isEmpty()
}

function ftwwMainInventorySlots() {
  return 36
}

function ftwwMainInventoryWrapper(inventory, player) {
  let wrapper = Java.loadClass('dev.latvian.mods.kubejs.item.PlayerMainInvWrapper')
  if (!wrapper) throw new Error('Main inventory wrapper is unavailable')
  if (!inventory) throw new Error('Main inventory is unavailable')
  if (!player) throw new Error('Purchasing player is unavailable')
  return new wrapper(inventory)
}

function ftwwSnapshotMainInventory(inventory) {
  let slots = ftwwMainInventorySlots()
  let snapshot = []
  for (let slot = 0; slot < slots; slot++) snapshot.push(inventory.getStackInSlot(slot).copy())
  return snapshot
}

function ftwwStacksMatch(first, second) {
  let utils = Java.loadClass('dev.latvian.mods.kubejs.item.ItemHandlerUtils')
  if (!utils) throw new Error('ItemHandlerUtils is unavailable')
  return utils.canItemStacksStack(first, second) === true
}

function ftwwCountMainInventory(inventory, reward) {
  let slots = ftwwMainInventorySlots()
  let total = 0
  for (let slot = 0; slot < slots; slot++) {
    let stack = inventory.getStackInSlot(slot)
    if (!stack || Number(stack.count) < 1) continue
    if (ftwwStacksMatch(stack, reward)) total += Number(stack.count)
  }
  return total
}

function ftwwVerifyInventoryRestore(inventory, snapshot) {
  let slots = ftwwMainInventorySlots()
  let failedSlots = []
  for (let slot = 0; slot < slots; slot++) {
    let expected = snapshot[slot]
    if (!expected) { failedSlots.push(slot); continue }
    let actual = inventory.getStackInSlot(slot)
    if (!actual) { failedSlots.push(slot); continue }
    if (Number(actual.count) !== Number(expected.count)) { failedSlots.push(slot); continue }
    if (Number(expected.count) < 1) continue
    if (String(actual.id) !== String(expected.id)) { failedSlots.push(slot); continue }
    if (!ftwwStacksMatch(actual, expected)) failedSlots.push(slot)
  }
  return failedSlots
}

function ftwwRestoreMainInventory(inventory, snapshot) {
  if (!snapshot) return []
  let failedSlots = []
  let slots = ftwwMainInventorySlots()
  for (let slot = 0; slot < slots; slot++) {
    try {
      inventory.setStackInSlot(slot, snapshot[slot].copy())
    } catch (slotError) {
      failedSlots.push(slot)
      try { console.error('[FarmingTales][WishingWell] slot restore failed slot=' + slot + ' error=' + slotError) } catch (ignored) {}
    }
  }
  try {
    let verifyFailed = ftwwVerifyInventoryRestore(inventory, snapshot)
    for (let index = 0; index < verifyFailed.length; index++) {
      if (failedSlots.indexOf(verifyFailed[index]) < 0) failedSlots.push(verifyFailed[index])
    }
  } catch (verifyError) {
    console.error('[FarmingTales][WishingWell] inventory restore verification failed: ' + verifyError)
    for (let slot = 0; slot < slots; slot++) if (failedSlots.indexOf(slot) < 0) failedSlots.push(slot)
  }
  return failedSlots
}

function ftwwSyncMainInventory(handler, player) {
  player.sendInventoryUpdate()
  if (handler) handler.setChanged()
}

function ftwwOpenShop(event) {
  if (!ftwwShopAvailable(event)) return false
  let manorUnlocked = ftwwManorShopUnlocked(event)
  let potCount = -1
  try { potCount = Number(ftwwProgress(event.server, event.player).goldPots) } catch (error) { potCount = -1 }
  let header = potCount >= 0 ? '已解锁 · 累计 ' + potCount + ' 锅 · 有用物品 80%' : '累计数据读取失败 · 有用物品 80%'
  let active = true
  let requestPurchase = (kind, itemId) => {
    if (!active) return false
    active = false
    if (ftwwQueuePurchase(event, kind, itemId)) return true
    if (!event.player.openInventory) active = true
    return false
  }
  event.player.openChestGUI(Text.of('许愿井 Lv.3 · 左键购买'), 4, gui => {
    gui.playerSlots = true
    gui.closed = () => { active = false }
    gui.button(4, 0, Item.of(FTWW_GOLD_POT), Text.of(header), click => click.setHandled())
    FTWW_SUMMONS.forEach((itemId, index) => {
      let stack = Item.of(itemId)
      if (stack.isEmpty()) return
      gui.button(index % 9, 1 + Math.floor(index / 9), stack, Text.of(stack.displayName.getString() + ' · ' + FTWW_SUMMON_PRICE + ' 金币锅'), click => {
        click.setHandled()
        requestPurchase('summon', itemId)
      })
    })
    if (manorUnlocked) {
      gui.button(3, 3, Item.of('minecraft:netherite_sword'), Text.of('随机庄园武器 · SDM 5000 万（不含万味胚刃）'), click => {
        click.setHandled()
        requestPurchase('weapon')
      })
      gui.button(5, 3, Item.of('irons_spellbooks:scroll'), Text.of('随机庄园法术卷轴 · SDM 500 万（随机等级，不含常规铁魔法）'), click => {
        click.setHandled()
        requestPurchase('spell')
      })
    }
  })
  return true
}

function ftwwSafeOpenShop(event) {
  try {
    ftwwOpenShop(event)
  } catch (error) {
    try { console.error('[FarmingTales][WishingWell] shop open failed after commit error=' + error) } catch (ignored) {}
    ftwwTell(event.player, '§c商店界面打开失败；累计数据没有变化，可稍后再次空手右键核心。')
  }
}

BlockEvents.rightClicked(FTWW_CORE, event => {
  let player = event.player
  let item = event.item
  if (!player || !item) return

  let itemId = String(item.id)
  let isGoldPot = itemId === FTWW_GOLD_POT
  let isUpgrade = itemId === FTWW_UPGRADE_PAYMENT
  if (isUpgrade) event.cancel()
  if (isGoldPot) {
    try {
      global.ftwwGoldUseGuards[ftwwPlayerKey(player)] = Number(global.ftwwDropScanTick || 0) + 1
    } catch (error) {
      try { console.error('[FarmingTales][WishingWell] gold pot guard could not be armed error=' + error) } catch (ignored) {}
    }
    event.cancel()
  }
  if (String(event.hand).toUpperCase() === 'OFF_HAND') { event.cancel(); return }
  try { console.info('[FarmingTales][WishingWell] trigger=right_click detected player=' + String(player.username) + ' item=' + itemId) } catch (error) {}
  if (player.isShiftKeyDown() && !isGoldPot && !isUpgrade) {
    ftwwShowGuide(player)
    event.cancel()
    return
  }

  if (itemId !== FTWW_PAYMENT && !isGoldPot && !isUpgrade) {
    if (item.isEmpty()) {
      if (ftwwSafeLevel(event.server, player) === 3) ftwwSafeOpenShop(event)
      else ftwwShowProgress(event.server, player)
      event.cancel()
    }
    return
  }

  // Do not rely on Minecraft's global emerald-item cooldown: another mod may
  // apply one and silently block this interaction. Keep a local well cooldown.
  let clickKey = null
  try {
    clickKey = ftwwPlayerKey(player)
  } catch (error) {
    try { console.error('[FarmingTales][WishingWell] interaction rejected identity error=' + error) } catch (ignored) {}
    ftwwTell(player, '§c无法识别玩家身份，本次操作未生效，未扣除任何物品。')
    event.cancel()
    return
  }
  let clickNow = Number(global.ftwwDropScanTick || 0)
  let clickReadyAt = Number(global.ftwwRightClickCooldowns[clickKey] || 0)
  if (clickNow < clickReadyAt) {
    try { console.info('[FarmingTales][WishingWell] trigger=right_click rejected=local_cooldown player=' + String(player.username) + ' remaining=' + (clickReadyAt - clickNow)) } catch (error) {}
    event.cancel()
    return
  }

  let missing = ftwwCheckStructure(event.level, event.block)
  if (missing) {
    try { console.warn('[FarmingTales][WishingWell] trigger=right_click rejected=structure player=' + String(player.username) + ' missing=(' + missing.dx + ',' + missing.dy + ',' + missing.dz + ') expected=' + missing.expected + ' found=' + missing.found) } catch (error) {}
    ftwwHighlightMissing(event, missing)
    ftwwTell(player, `§c许愿井结构不完整：相对坐标 §f(${missing.dx}, ${missing.dy}, ${missing.dz}) §c需要§f${missing.expected}§c，当前为§8${missing.found}`)
    ftwwTell(player, '§7请补齐提示位置后再试；潜行右键可查看玩法简介。')
    event.cancel()
    return
  }

  global.ftwwRightClickCooldowns[clickKey] = clickNow + 12
  event.cancel()
  if (isUpgrade) {
    ftwwDonateUpgradeBlocks(event, item, player.isShiftKeyDown() ? Number(item.count) : 1, null)
    player.swing()
    return
  }
  if (isGoldPot) {
    ftwwDonatePots(event, item, player.isShiftKeyDown() ? Number(item.count) : 1, null)
    player.swing()
    return
  }
  let preparedWish = ftwwPrepareEmeraldWishOrReport(event)
  if (!preparedWish) return
  if (!ftwwPayEmerald(event, item, null)) return
  try { player.addItemCooldown(FTWW_PAYMENT, 12) } catch (error) {}
  player.swing()

  try { console.info('[FarmingTales][WishingWell] trigger=right_click accepted player=' + String(player.username) + ' core=(' + event.block.x + ',' + event.block.y + ',' + event.block.z + ')') } catch (error) {}
  ftwwResolveWish(event, 'right_click', null, preparedWish)
})


const FTWW_TEST_EVENTS = [
  { key: 'nothing', label: '无事发生', chance: '39.9%', description: '烟雾、落空提示，不发放物品' },
  { key: 'junk', label: '无用材料', chance: '22%', description: '石头、泥土、砂砾等杂物池' },
  { key: 'wood', label: '木材', chance: '10%', description: '随机原木奖励池' },
  { key: 'food', label: '食物', chance: '10%', description: '随机食物奖励池' },
  { key: 'poop', label: '屎', chance: '7%', description: '粪便奖励及对应特效' },
  { key: 'coin', label: '金币', chance: '5%', description: '金币奖励及金光特效' },
  { key: 'seed_bag', label: '随机种子袋', chance: '3%', description: '随机获得 1 个已注册的种子袋' },
  { key: 'mineral', label: '矿物', chance: '2.5%', description: '随机矿物奖励及稀有特效' },
  { key: 'emerald', label: '更多绿宝石', chance: '0.5%', description: '2～8 颗绿宝石及大奖特效' },
  { key: 'geode', label: '晶球', chance: '0.1%', description: '极罕见地获得 1 个晶球' }
]

function ftwwTestOutcome(type) {
  if (type === 'nothing') return { type: 'nothing' }
  if (type === 'junk') return { type: 'junk', pool: FTWW_JUNK_REWARDS }
  if (type === 'wood') return { type: 'wood', pool: FTWW_WOOD_REWARDS }
  if (type === 'food') return { type: 'food', pool: FTWW_FOOD_REWARDS }
  if (type === 'poop') return { type: 'poop', item: 'kubejs:poop', min: 1, max: 3 }
  if (type === 'coin') return { type: 'coin', item: 'kubejs:coin', min: 3, max: 16 }
  if (type === 'seed_bag') return { type: 'seed_bag', pool: FTWW_SEED_BAG_REWARDS }
  if (type === 'mineral') return { type: 'mineral', pool: FTWW_MINERAL_REWARDS }
  if (type === 'emerald') return { type: 'emerald', item: 'minecraft:emerald', min: 2, max: 8 }
  if (type === 'geode') return { type: 'geode', item: 'farmingtales:geode', min: 1, max: 1 }
  return null
}

function ftwwSourcePlayer(source) {
  try { if (source.player) return source.player } catch (error) {}
  try { return source.getPlayer() } catch (error) {}
  return null
}

function ftwwTestButton(label, command, hover) {
  let component = Text.of(label)
  try { component = component.clickRunCommand(command) } catch (error) {}
  try { component = component.hover(Text.of(hover)) } catch (error) {}
  return component
}

function ftwwTellComponent(player, component) {
  try { player.tell(component) } catch (error) {
    try { player.tell(ftwwText(String(component))) } catch (ignored) {}
  }
}

function ftwwFindNearestCore(level, player) {
  let px = Math.floor(Number(player.x))
  let py = Math.floor(Number(player.y))
  let pz = Math.floor(Number(player.z))
  let best = null
  let bestDistance = Number.MAX_VALUE

  // A command is used rarely, so a bounded block scan is simpler and more
  // reliable than maintaining a persistent controller registry.
  for (let dy = -6; dy <= 6; dy++) {
    for (let dx = -12; dx <= 12; dx++) {
      for (let dz = -12; dz <= 12; dz++) {
        let x = px + dx
        let y = py + dy
        let z = pz + dz
        if (ftwwBlockId(level, x, y, z) !== FTWW_CORE) continue
        let distance = dx * dx + dy * dy + dz * dz
        if (distance >= bestDistance) continue
        bestDistance = distance
        best = level.getBlock(x, y, z)
      }
    }
  }
  return best
}

function ftwwShowTestMenu(player) {
  try { console.info('[FarmingTales][WishingWell] trigger=command menu player=' + String(player.username)) } catch (error) {}
  ftwwTell(player, '§6§l========== 许愿井事件测试 ==========')
  ftwwTell(player, '§7点击事件名称即可在附近完整许愿井上强制触发。')
  ftwwTell(player, '§c测试不消耗绿宝石，但物品奖励会真实发放。')
  ftwwShowProgress(player.server, player)
  let odds = FTWW_LEVEL_ODDS[ftwwLevel(ftwwProgress(player.server, player))]

  let randomLine = Text.of('§7[随机] ')
  try {
    randomLine = randomLine.append(ftwwTestButton('§a[触发正常随机抽取]', '/wishingwelltest trigger random', '按正式概率随机一次'))
  } catch (error) {}
  ftwwTellComponent(player, randomLine)

  for (let i = 0; i < FTWW_TEST_EVENTS.length; i++) {
    let definition = FTWW_TEST_EVENTS[i]
    let line = Text.of(`§7[${odds[i]}%] `)
    try {
      line = line
        .append(ftwwTestButton(`§e[${definition.label}]`, `/wishingwelltest trigger ${definition.key}`, definition.description))
        .append(Text.of(` §8${definition.key}`))
    } catch (error) {}
    ftwwTellComponent(player, line)
  }
  ftwwTell(player, '§8文字用法：/ft wishingwell trigger <事件英文名>')
}

function ftwwRunTestCommand(ctx, type) {
  let player = ftwwSourcePlayer(ctx.source)
  if (!player) {
    try { console.warn('[FarmingTales][WishingWell] trigger=command rejected=no_player selected=' + String(type)) } catch (error) {}
    return 0
  }
  try { console.info('[FarmingTales][WishingWell] trigger=command requested player=' + String(player.username) + ' selected=' + String(type)) } catch (error) {}
  let server = null
  try { server = player.server } catch (error) {}
  if (!server) {
    try { server = ctx.source.server } catch (error) {}
  }
  let level = null
  try { level = player.level } catch (error) {}
  if (!server || !level) {
    try { console.warn('[FarmingTales][WishingWell] trigger=command rejected=no_server_or_level player=' + String(player.username) + ' selected=' + String(type)) } catch (error) {}
    ftwwTell(player, '§c无法读取当前服务器或维度。')
    return 0
  }

  let core = ftwwFindNearestCore(level, player)
  if (!core) {
    try { console.warn('[FarmingTales][WishingWell] trigger=command rejected=no_nearby_core player=' + String(player.username) + ' selected=' + String(type)) } catch (error) {}
    ftwwTell(player, '§c附近 12 格内没有找到许愿井核心。请站到许愿井旁再测试。')
    return 0
  }

  let missing = ftwwCheckStructure(level, core)
  let syntheticEvent = { server: server, level: level, block: core, player: player }
  if (missing) {
    ftwwHighlightMissing(syntheticEvent, missing)
    ftwwTell(player, `§c测试被结构校验拦截：相对坐标 §f(${missing.dx}, ${missing.dy}, ${missing.dz}) §c需要§f${missing.expected}§c，当前为§8${missing.found}`)
    try { console.warn('[FarmingTales][WishingWell] trigger=command rejected=structure player=' + String(player.username) + ' missing=(' + missing.dx + ',' + missing.dy + ',' + missing.dz + ') expected=' + missing.expected + ' found=' + missing.found) } catch (error) {}
    return 0
  }

  let forcedOutcome = null
  if (type !== 'random') {
    forcedOutcome = ftwwTestOutcome(type)
    if (!forcedOutcome) {
      ftwwTell(player, '§c未知事件：' + String(type))
      ftwwShowTestMenu(player)
      return 0
    }
  }

  try { console.info('[FarmingTales][WishingWell] trigger=command accepted player=' + String(player.username) + ' selected=' + String(type) + ' core=(' + core.x + ',' + core.y + ',' + core.z + ')') } catch (error) {}
  ftwwTell(player, `§d[测试] §f正在触发许愿井事件：§e${String(type)}`)
  ftwwResolveWish(syntheticEvent, 'command:' + String(type), forcedOutcome)
  return 1
}

function ftwwBuildTestCommand(Commands, rootName) {
  let root = Commands.literal(rootName)
    .requires(source => source.hasPermission(2))
    .executes(ctx => {
      let player = ftwwSourcePlayer(ctx.source)
      if (!player) return 0
      ftwwShowTestMenu(player)
      return 1
    })

  root = root.then(Commands.literal('list').executes(ctx => {
    let player = ftwwSourcePlayer(ctx.source)
    if (!player) return 0
    ftwwShowTestMenu(player)
    return 1
  }))

  let trigger = Commands.literal('trigger')
  trigger = trigger.then(Commands.literal('random').executes(ctx => ftwwRunTestCommand(ctx, 'random')))
  for (let i = 0; i < FTWW_TEST_EVENTS.length; i++) {
    let type = FTWW_TEST_EVENTS[i].key
    trigger = trigger.then(Commands.literal(type).executes(ctx => ftwwRunTestCommand(ctx, type)))
  }
  return root.then(trigger)
}

ServerEvents.commandRegistry(event => {
  let Commands = event.commands
  // The standalone alias remains available even if another script changes the
  // shared /ft command tree.
  event.register(ftwwBuildTestCommand(Commands, 'wishingwelltest'))
  event.register(Commands.literal('ft').then(ftwwBuildTestCommand(Commands, 'wishingwell')))
})

ServerEvents.recipes(event => {
  event.shaped('farmingtales:wishing_well_core', [
    'SES',
    'CWC',
    'SSS'
  ], {
    S: 'minecraft:stone_bricks',
    E: 'minecraft:emerald',
    C: 'minecraft:chiseled_stone_bricks',
    W: 'minecraft:water_bucket'
  }).id('farmingtales:wishing_well_core')
})
