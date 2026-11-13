export default function build(api) {
  const structure = api.createStructure([29, 20, 29])
  const put = (blockX, blockY, blockZ, block, properties = {}) => {
    const defaults = {}
    if (['mycelium', 'podzol'].includes(block)) defaults.snowy = 'false'
    if (block.includes('mushroom_block') || block === 'mushroom_stem') {
      for (const face of ['north', 'east', 'south', 'west', 'up', 'down']) defaults[face] = 'true'
    }
    api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`, { ...defaults, ...properties })
  }
  const fill = (start, end, block, properties) => {
    for (let blockX = start[0]; blockX <= end[0]; blockX++) {
      for (let blockY = start[1]; blockY <= end[1]; blockY++) {
        for (let blockZ = start[2]; blockZ <= end[2]; blockZ++) put(blockX, blockY, blockZ, block, properties)
      }
    }
  }
  const stone = (blockX, blockY, blockZ) => {
    const seed = (blockX * 17 + blockZ * 31 + blockY * 11) % 19
    return seed < 3 ? 'mossy_stone_bricks' : seed < 5 ? 'cracked_deepslate_bricks' : 'deepslate_bricks'
  }
  fill([0, 0, 0], [28, 19, 28], 'air')
  fill([0, 0, 0], [28, 0, 28], 'deepslate_bricks')
  for (let blockX = 0; blockX < 29; blockX++) {
    for (let blockZ = 0; blockZ < 29; blockZ++) {
      const variant = (blockX * 13 + blockZ * 7) % 23
      put(blockX, 1, blockZ, variant < 4 ? 'mycelium' : variant < 7 ? 'purple_terracotta' : variant < 9 ? 'cracked_stone_bricks' : 'stone_bricks')
      if (blockX === 0 || blockX === 28 || blockZ === 0 || blockZ === 28) {
        for (let blockY = 2; blockY <= 18; blockY++) put(blockX, blockY, blockZ, stone(blockX, blockY, blockZ))
      }
      put(blockX, 19, blockZ, 'deepslate_tiles')
    }
  }
  for (const blockX of [1, 27]) {
    for (let blockZ = 1; blockZ <= 27; blockZ++) {
      put(blockX, 17, blockZ, 'polished_blackstone_bricks')
      put(blockX, 18, blockZ, 'polished_blackstone_bricks')
    }
  }
  for (const blockZ of [1, 27]) fill([1, 18, blockZ], [27, 18, blockZ], 'polished_blackstone_bricks')
  for (let blockX = 1; blockX <= 27; blockX++) {
    for (let blockZ = 1; blockZ <= 19; blockZ++) {
      if (blockZ <= 6 || blockX <= 6 || blockX >= 22) {
        put(blockX, 7, blockZ, 'polished_blackstone_bricks')
        put(blockX, 8, blockZ, (blockX + blockZ) % 4 ? 'mycelium' : 'rooted_dirt')
      }
    }
  }
  for (const blockX of [2, 26]) {
    for (const blockZ of [3, 9, 18]) {
      fill([blockX, 2, blockZ], [blockX, 7, blockZ], 'chiseled_stone_bricks')
      put(blockX, 4, blockZ, 'shroomlight')
    }
  }
  for (const blockX of [7, 21]) fill([blockX, 2, 3], [blockX, 7, 3], 'chiseled_stone_bricks')
  for (const blockX of [6, 22]) {
    for (let blockZ = 7; blockZ <= 19; blockZ++) {
      put(blockX, 9, blockZ, 'stone_brick_wall', { up: 'true', north: 'low', south: 'low', west: 'none', east: 'none', waterlogged: 'false' })
    }
  }
  for (let blockX = 6; blockX <= 22; blockX++) {
    put(blockX, 9, 6, 'stone_brick_wall', { up: 'true', north: 'none', south: 'none', west: 'low', east: 'low', waterlogged: 'false' })
  }
  for (const baseX of [2, 24]) {
    for (let step = 0; step < 7; step++) {
      const blockZ = 26 - step
      if (step) fill([baseX, 2, blockZ], [baseX + 2, step + 1, blockZ], 'deepslate_bricks')
      fill([baseX, step + 2, blockZ], [baseX + 2, step + 2, blockZ], 'stone_brick_stairs', { facing: 'north', half: 'bottom', shape: 'straight', waterlogged: 'false' })
    }
  }
  for (let tier = 0; tier < 3; tier++) {
    const minimum = 10 + tier
    const maximum = 18 - tier
    fill([minimum, 2 + tier, minimum], [maximum, 2 + tier, maximum], tier === 2 ? 'mycelium' : 'mossy_stone_bricks')
  }
  for (let step = 0; step < 3; step++) {
    fill([13, 2 + step, 19 - step], [15, 2 + step, 19 - step], 'stone_brick_stairs', { facing: 'north', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  }
  const mushroom = (baseX, baseY, baseZ, stemHeight, radius) => {
    fill([baseX, baseY, baseZ], [baseX, baseY + stemHeight - 1, baseZ], 'mushroom_stem')
    for (let deltaX = -radius; deltaX <= radius; deltaX++) {
      for (let deltaZ = -radius; deltaZ <= radius; deltaZ++) {
        if (Math.abs(deltaX) === radius && Math.abs(deltaZ) === radius) continue
        put(baseX + deltaX, baseY + stemHeight, baseZ + deltaZ, 'red_mushroom_block')
        if (Math.abs(deltaX) === radius || Math.abs(deltaZ) === radius) put(baseX + deltaX, baseY + stemHeight - 1, baseZ + deltaZ, 'red_mushroom_block')
        if (Math.abs(deltaX) <= radius - 1 && Math.abs(deltaZ) <= radius - 1) put(baseX + deltaX, baseY + stemHeight + 1, baseZ + deltaZ, 'red_mushroom_block')
      }
    }
    put(baseX, baseY + stemHeight - 2, baseZ, 'shroomlight')
  }
  mushroom(14, 5, 14, 6, 4)
  mushroom(4, 9, 8, 7, 3)
  mushroom(24, 9, 8, 7, 3)
  mushroom(7, 2, 24, 4, 2)
  mushroom(21, 2, 24, 4, 2)
  const arch = (direction, middle, floor, breadth = 5) => {
    const plane = direction === 'north' ? 0 : direction === 'west' ? 0 : 28
    const north = direction === 'north'
    const radius = Math.floor(breadth / 2)
    for (let lateral = -radius - 1; lateral <= radius + 1; lateral++) {
      for (let blockY = floor + 1; blockY <= floor + 6; blockY++) {
        const distance = Math.abs(lateral)
        const open = distance <= radius && blockY <= floor + 4 + (distance < radius ? 1 : 0)
        put(north ? middle + lateral : plane, blockY, north ? plane : middle + lateral, open ? 'air' : 'stone_bricks')
      }
      put(north ? middle + lateral : plane, floor, north ? plane : middle + lateral, 'stone_bricks')
    }
  }
  arch('west', 14, 1)
  arch('east', 14, 1)
  arch('north', 14, 8)
  arch('north', 14, 1, 3)
  for (const [baseX, baseZ] of [[7, 10], [20, 10], [13, 22]]) {
    fill([baseX - 1, 1, baseZ - 1], [baseX + 2, 1, baseZ + 2], 'polished_blackstone_bricks')
    fill([baseX, 1, baseZ], [baseX + 1, 1, baseZ + 1], 'water', { level: '0' })
  }
  for (const [baseX, baseY, baseZ] of [[10, 2, 10], [18, 2, 18], [10, 2, 18], [18, 2, 10], [4, 9, 4], [24, 9, 4], [7, 2, 26], [21, 2, 26]]) {
    put(baseX, baseY, baseZ, 'polished_blackstone_bricks')
    put(baseX, baseY + 1, baseZ, 'shroomlight')
  }
  for (const [baseX, baseZ] of [[8, 8], [20, 8], [8, 19], [20, 19], [11, 23], [18, 23]]) {
    put(baseX, 1, baseZ, 'amethyst_block')
    put(baseX, 2, baseZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[8, 25], [20, 25], [10, 9], [18, 9], [9, 18], [19, 18], [3, 5], [25, 5], [3, 12], [25, 12]]) {
    const floor = blockX < 6 || blockX > 22 ? 8 : 1
    put(blockX, floor, blockZ, 'mycelium')
    put(blockX, floor + 1, blockZ, 'red_mushroom')
  }
  for (const blockZ of [4, 12, 18]) {
    for (const blockX of [1, 27]) {
      for (let blockY = 11; blockY <= 15; blockY++) {
        put(blockX, blockY, blockZ, 'vine', { [blockX === 1 ? 'west' : 'east']: 'true', up: 'false' })
      }
    }
  }
  for (const blockX of [8, 20]) {
    fill([blockX, 15, 2], [blockX, 18, 2], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX, 14, 2, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  return { structure, metadata: { name: 'room' } }
}
