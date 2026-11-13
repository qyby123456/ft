export default function build(api) {
  const size = [19, 11, 19]
  const structure = api.createStructure(size)
  const set = (position, block, properties, nbt) => api.setBlock(structure, position, block, properties, nbt)
  const fill = (first, last, block, properties) => api.fill(structure, first, last, block, properties)
  const agedStone = (width, height, depth) => {
    const pattern = (width * 31 + height * 17 + depth * 13) % 19
    return pattern < 3 ? 'minecraft:mossy_stone_bricks' : pattern < 6 ? 'minecraft:cracked_stone_bricks' : 'minecraft:stone_bricks'
  }
  fill([0, 0, 0], [18, 10, 18], 'minecraft:air')
  for (let width = 0; width < 19; width++) {
    for (let depth = 0; depth < 19; depth++) {
      set([width, 0, depth], agedStone(width, 0, depth))
      for (let height = 1; height <= 8; height++) {
        if (width === 0 || width === 18 || depth === 0 || depth === 18) set([width, height, depth], agedStone(width, height, depth))
      }
      set([width, 9, depth], (width + depth * 3) % 11 === 0 ? 'minecraft:cracked_stone_bricks' : 'minecraft:stone_bricks')
    }
  }
  for (const width of [1, 17]) {
    for (const depth of [1, 7, 12, 17]) {
      fill([width, 1, depth], [width, 7, depth], 'minecraft:stone_bricks')
      fill([width - (width === 17 ? 1 : 0), 7, depth], [width + (width === 1 ? 1 : 0), 7, depth], 'minecraft:chiseled_stone_bricks')
    }
  }
  for (const depth of [5, 10, 12, 15]) fill([1, 8, depth], [17, 8, depth], 'minecraft:stripped_spruce_log', { axis: 'x' })
  for (const width of [1, 17]) fill([width, 8, 1], [width, 8, 17], 'minecraft:stripped_spruce_log', { axis: 'z' })
  for (let width = 0; width < 19; width++) {
    for (const depth of [0, 18]) set([width, 10, depth], 'minecraft:stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
  }
  for (let depth = 1; depth < 18; depth++) {
    for (const width of [0, 18]) set([width, 10, depth], 'minecraft:stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
  }
  fill([8, 1, 0], [10, 4, 0], 'minecraft:air')
  fill([7, 1, 0], [7, 5, 0], 'minecraft:polished_andesite')
  fill([11, 1, 0], [11, 5, 0], 'minecraft:polished_andesite')
  fill([7, 5, 0], [11, 5, 0], 'minecraft:chiseled_stone_bricks')
  for (const wallX of [0, 18]) {
    for (const depth of [5, 6, 7]) {
      for (const height of [4, 5]) set([wallX, height, depth], 'minecraft:iron_bars')
    }
    fill([wallX, 3, 5], [wallX, 3, 7], 'minecraft:polished_andesite')
    fill([wallX, 6, 5], [wallX, 6, 7], 'minecraft:polished_andesite')
  }
  fill([3, 0, 14], [7, 0, 17], 'minecraft:polished_basalt', { axis: 'y' })
  fill([3, 1, 14], [3, 5, 17], 'minecraft:stone_bricks')
  fill([7, 1, 14], [7, 5, 17], 'minecraft:stone_bricks')
  fill([3, 1, 17], [7, 5, 17], 'minecraft:cracked_stone_bricks')
  fill([3, 5, 14], [7, 5, 17], 'minecraft:stone_bricks')
  fill([4, 6, 15], [6, 6, 17], 'minecraft:cobblestone')
  fill([4, 7, 16], [6, 10, 17], 'minecraft:stone_bricks')
  fill([3, 1, 13], [7, 1, 13], 'minecraft:stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
  set([5, 1, 15], 'minecraft:campfire', { facing: 'north', lit: 'false', signal_fire: 'false', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:campfire', Items: [] }))
  set([5, 1, 16], 'minecraft:polished_basalt', { axis: 'y' })
  set([5, 2, 16], 'minecraft:cauldron')
  set([4, 4, 16], 'minecraft:cobweb')
  for (const depth of [4, 7]) fill([2, 1, depth], [2, 5, depth], 'minecraft:stripped_spruce_log', { axis: 'y' })
  for (const height of [1, 3, 5]) fill([1, height, 4], [2, height, 7], 'minecraft:spruce_planks')
  for (const position of [[2, 2, 5], [2, 4, 6]]) set(position, 'minecraft:flower_pot')
  set([2, 6, 5], 'minecraft:white_carpet')
  set([2, 6, 6], 'minecraft:potted_dead_bush')
  for (const depth of [12, 16]) fill([16, 1, depth], [16, 5, depth], 'minecraft:stripped_spruce_log', { axis: 'y' })
  for (const height of [1, 3, 5]) fill([16, height, 12], [17, height, 16], 'minecraft:spruce_planks')
  for (const position of [[16, 2, 13], [16, 4, 14], [16, 4, 15]]) set(position, 'minecraft:flower_pot')
  set([17, 6, 16], 'minecraft:cobweb')
  const barrels = [[2, 1, 9], [16, 1, 8], [16, 2, 15]]
  for (const position of barrels) set(position, 'minecraft:barrel', { facing: 'up', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel', Items: [] }))
  fill([10, 1, 16], [13, 1, 17], 'minecraft:spruce_planks')
  fill([9, 2, 16], [13, 2, 17], 'minecraft:spruce_planks')
  set([9, 1, 16], 'minecraft:furnace', { facing: 'north', lit: 'false' }, api.createBlockNbt({ id: 'minecraft:furnace', Items: [], BurnTime: 0, CookTime: 0, CookTimeTotal: 200 }))
  set([9, 1, 17], 'minecraft:bricks')
  fill([9, 6, 17], [13, 6, 17], 'minecraft:stripped_spruce_log', { axis: 'x' })
  for (const width of [10, 11, 12]) set([width, 5, 17], 'minecraft:grindstone', { face: 'ceiling', facing: 'north' })
  for (const position of [[10, 3, 16], [12, 3, 17]]) set(position, 'minecraft:flower_pot')
  set([13, 3, 16], 'minecraft:crafting_table')
  for (const width of [7, 12]) {
    for (const depth of [10, 12]) set([width, 1, depth], 'minecraft:stripped_spruce_log', { axis: 'y' })
  }
  fill([7, 2, 10], [12, 2, 12], 'minecraft:spruce_planks')
  for (const position of [[8, 2, 10], [11, 2, 12]]) set(position, 'minecraft:stripped_spruce_log', { axis: 'x' })
  set([8, 3, 11], 'minecraft:cauldron')
  set([10, 3, 11], 'minecraft:flower_pot')
  set([11, 3, 10], 'minecraft:brown_carpet')
  for (const width of [8, 11]) set([width, 1, 8], 'minecraft:spruce_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  for (const width of [8, 11]) set([width, 1, 14], 'minecraft:spruce_slab', { type: 'bottom', waterlogged: 'false' })
  const lamps = [[4, 5, 5], [14, 5, 5], [10, 5, 10], [14, 5, 15], [4, 5, 12]]
  for (const [width, height, depth] of lamps) {
    for (let chainY = height + 1; chainY <= 7; chainY++) set([width, chainY, depth], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
    set([width, height, depth], 'minecraft:lantern', { hanging: 'true', waterlogged: 'false' })
  }
  const supportColumns = [[3, 1, 14, 5], [7, 1, 14, 5], [2, 1, 4, 5], [2, 1, 7, 5], [16, 1, 12, 5], [16, 1, 16, 5], [7, 1, 10, 2], [7, 1, 12, 2], [12, 1, 10, 2], [12, 1, 12, 2]]
  return { structure, metadata: {
    name: 'room', id: 'A03', title: '荒废厨房', size,
    entrance: [9, 1, 0], interior: [9, 1, 4], facing: 'north', lamps, barrels, supportColumns,
    routeWaypoints: [[9, 1, 0], [9, 1, 4], [5, 1, 9], [5, 1, 11], [9, 1, 6], [14, 1, 10], [14, 1, 14]],
    hearth: { bounds: [[3, 0, 13], [7, 10, 17]], coldCampfire: [5, 1, 15], cauldron: [5, 2, 16], fullCauldronPlinth: [5, 1, 16], liveFire: false },
    roof: { fullPlaneY: 9, previewCutawayOnly: true },
  } }
}

