export const specification = {
  id: 'zone-M02', name: '淹水矿井', category: 'mines',
  size: [25, 15, 25], entrance: [12, 4, 0], interior: [12, 4, 20], facing: 'north',
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/mines/M02-mines-flooded-mine-shaft.png',
  reference_sha256: '099696aab9cb43fce0acb8e3b79d54087cf1876b87204aa1d110822dec505965',
  reference_batch: 'dungeon-zone-batch-20261103', distinct_from: 'corridor-M02',
  water_levels: [1, 2], deck_y: 3, roof_y: [13, 14],
  pillars: [[4, 4], [19, 4], [4, 19], [19, 19]],
  suspension: [[7, 8], [17, 16], [12, 12], [12, 20]],
  dry_rectangles: [[11, 0, 13, 22], [3, 11, 21, 13], [10, 19, 14, 22]],
  adaptations: [
    'Independent square mine room with one north interface, distinct from archive corridor-M02.',
    'Guarded raised boardwalk replaces unsafe broken crossing; water stays below deck.',
    'Four 2x2 headframe piers join foundation to roof beams; broken brace becomes a supported stepped buttress.',
    'Submerged rails use waterlogging; tipped minecart and crates are inert block sculptures without entities or inventory.',
    'Complete double rock roof; cutaways change render copies only.'
  ]
}

export default function build(api) {
  const structure = api.createStructure(specification.size)
  const put = (posX, posY, posZ, block, properties) => api.setBlock(structure, [posX, posY, posZ], 'minecraft:' + block, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, 'minecraft:' + block, properties)
  const stone = (posX, posY, posZ) => {
    const value = (posX * 17 + posY * 11 + posZ * 23) % 19
    return value < 4 ? 'mossy_cobblestone' : value < 7 ? 'andesite' : value < 9 ? 'cobbled_deepslate' : 'stone'
  }
  fill([0, 0, 0], [24, 14, 24], 'air')
  for (let posX = 0; posX < 25; posX++) {
    for (let posZ = 0; posZ < 25; posZ++) {
      for (const posY of [0, 13, 14]) put(posX, posY, posZ, stone(posX, posY, posZ))
      for (let posY = 1; posY <= 12; posY++) {
        if (posX === 0 || posX === 24 || posZ === 0 || posZ === 24) put(posX, posY, posZ, stone(posX, posY, posZ))
      }
    }
  }
  fill([1, 1, 1], [23, 2, 23], 'water', { level: '0' })
  for (const [posX, posZ] of specification.pillars) {
    fill([posX, 1, posZ], [posX + 1, 3, posZ + 1], 'stone_bricks')
    fill([posX, 4, posZ], [posX + 1, 12, posZ + 1], 'spruce_log', { axis: 'y' })
    fill([posX, 7, posZ], [posX + 1, 7, posZ + 1], 'polished_andesite')
  }
  for (const posZ of [4, 5, 19, 20]) fill([4, 12, posZ], [20, 12, posZ], 'spruce_log', { axis: 'x' })
  for (const posX of [4, 5, 19, 20]) fill([posX, 12, 4], [posX, 12, 20], 'spruce_log', { axis: 'z' })
  for (const posZ of [8, 12, 16]) fill([1, 12, posZ], [23, 12, posZ], 'spruce_log', { axis: 'x' })
  fill([12, 12, 1], [12, 12, 23], 'spruce_log', { axis: 'z' })
  for (const sideX of [1, 23]) {
    for (const posZ of [3, 9, 15, 21]) {
      fill([sideX, 1, posZ], [sideX, 3, posZ], 'stone_bricks')
      fill([sideX, 4, posZ], [sideX, 11, posZ], 'spruce_log', { axis: 'y' })
      fill([1, 11, posZ], [23, 11, posZ], 'spruce_log', { axis: 'x' })
    }
  }
  for (const [left, near, right, far] of [[10, 0, 14, 22], [2, 10, 22, 14], [8, 18, 16, 23]]) fill([left, 3, near], [right, 3, far], 'spruce_planks')
  for (const posZ of [2, 7, 12, 17, 21]) {
    for (const posX of [10, 14]) fill([posX, 1, posZ], [posX, 2, posZ], 'spruce_log', { axis: 'y' })
  }
  for (const posX of [3, 8, 16, 21]) {
    for (const posZ of [10, 14]) fill([posX, 1, posZ], [posX, 2, posZ], 'spruce_log', { axis: 'y' })
  }
  for (const posX of [8, 16]) {
    for (const posZ of [18, 23]) fill([posX, 1, posZ], [posX, 2, posZ], 'spruce_log', { axis: 'y' })
  }
  const fence = (posX, posZ) => put(posX, 4, posZ, 'spruce_fence', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
  for (let posZ = 1; posZ <= 17; posZ++) {
    if (posZ >= 10 && posZ <= 14) continue
    fence(10, posZ); fence(14, posZ)
  }
  for (let posX = 2; posX <= 22; posX++) {
    if (posX >= 10 && posX <= 14) continue
    fence(posX, 10); fence(posX, 14)
  }
  for (let posZ = 11; posZ <= 13; posZ++) { fence(2, posZ); fence(22, posZ) }
  for (let posZ = 18; posZ <= 23; posZ++) { fence(8, posZ); fence(16, posZ) }
  for (let posX = 8; posX <= 16; posX++) fence(posX, 23)
  for (const posX of [9, 10, 14, 15]) fence(posX, 18)
  for (let posZ = 4; posZ <= 21; posZ++) put(12, 4, posZ, 'rail', { shape: 'north_south', waterlogged: 'false' })
  for (let posZ = 5; posZ <= 19; posZ++) put(7, 1, posZ, 'rail', { shape: 'north_south', waterlogged: 'true' })
  for (const [posX, posZ] of [[3, 7], [21, 16], [6, 21]]) {
    fill([posX, 1, posZ], [posX + 1, 2, posZ + 1], 'dark_oak_planks')
    fill([posX, 3, posZ], [posX + 1, 3, posZ + 1], 'stripped_spruce_log', { axis: 'y' })
  }
  fill([17, 1, 7], [19, 1, 9], 'iron_block')
  for (const [posX, posZ] of [[17, 7], [18, 7], [19, 7], [17, 8], [19, 8], [17, 9], [18, 9], [19, 9]]) put(posX, 2, posZ, 'polished_andesite')
  for (const posX of [17, 18, 19]) put(posX, 3, 7, 'polished_andesite_stairs', { facing: 'north', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  fill([17, 3, 8], [17, 3, 9], 'polished_andesite')
  for (let offset = 0; offset < 4; offset++) fill([17 + offset, 1, 17], [17 + offset, 4 + offset, 17], 'spruce_log', { axis: 'x' })
  fill([20, 8, 17], [20, 11, 17], 'spruce_log', { axis: 'y' })
  fill([20, 11, 17], [23, 11, 17], 'spruce_log', { axis: 'x' })
  for (const [posX, posZ] of specification.suspension) {
    fill([posX, 9, posZ], [posX, 11, posZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put(posX, 8, posZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const [posX, posZ] of [[8, 6], [16, 6], [8, 17], [22, 22]]) {
    put(posX, 12, posZ, 'dripstone_block')
    put(posX, 11, posZ, 'pointed_dripstone', { thickness: 'tip', vertical_direction: 'down', waterlogged: 'false' })
  }
  for (const [posX, posZ] of [[3, 3], [21, 3], [3, 21], [21, 21], [7, 15], [17, 6]]) put(posX, 0, posZ, 'sea_lantern')
  for (let posX = 2; posX <= 22; posX++) {
    for (let posZ = 2; posZ <= 22; posZ++) {
      if ((posX * 7 + posZ * 13) % 23 === 0) put(posX, 0, posZ, (posX + posZ) % 2 === 0 ? 'copper_ore' : 'deepslate_gold_ore')
    }
  }
  for (const [posX, posY, posZ, ore] of [[24, 4, 7, 'copper_ore'], [24, 5, 8, 'copper_ore'], [24, 4, 9, 'iron_ore'], [0, 4, 16, 'coal_ore'], [0, 5, 17, 'coal_ore'], [7, 4, 24, 'gold_ore'], [8, 5, 24, 'gold_ore'], [9, 4, 24, 'copper_ore']]) put(posX, posY, posZ, ore)
  put(9, 4, 21, 'crafting_table'); put(15, 4, 21, 'raw_iron_block'); put(15, 4, 22, 'waxed_weathered_cut_copper')
  fill([10, 1, 0], [14, 3, 2], 'stone_bricks')
  fill([11, 4, 0], [13, 6, 1], 'air')
  for (const posX of [9, 15]) {
    fill([posX, 3, 0], [posX, 7, 0], 'stone_bricks'); put(posX, 8, 0, 'chiseled_stone_bricks')
  }
  fill([9, 7, 0], [15, 7, 0], 'spruce_log', { axis: 'x' })
  return { structure, metadata: { name: 'room' } }
}
