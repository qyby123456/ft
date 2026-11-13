export default function build(api) {
  const structure = api.createStructure([13, 11, 16])
  const put = (position, block, properties) => api.setBlock(structure, position, 'minecraft:' + block, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, 'minecraft:' + block, properties)
  const masonry = (horizontal, vertical, depth) => {
    const variation = (horizontal * 19 + vertical * 37 + depth * 13) % 23
    if (variation < 5) return 'mossy_stone_bricks'
    if (variation < 8) return 'cracked_stone_bricks'
    if (variation === 9) return 'andesite'
    return 'stone_bricks'
  }

  fill([0, 0, 0], [12, 10, 15], 'air')
  fill([0, 0, 0], [12, 0, 15], 'deepslate_tiles')
  for (let depth = 0; depth < 16; depth++) {
    for (let horizontal = 0; horizontal < 13; horizontal++) {
      put([horizontal, 10, depth], masonry(horizontal, 10, depth))
      if (horizontal <= 1 || horizontal >= 11) {
        for (let vertical = 1; vertical < 10; vertical++) {
          put([horizontal, vertical, depth], masonry(horizontal, vertical, depth))
        }
      }
      if (horizontal >= 5 && horizontal <= 7 || depth <= 1 || depth >= 14) {
        put([horizontal, 1, depth], masonry(horizontal, 1, depth))
      } else if (horizontal >= 2 && horizontal <= 10) {
        put([horizontal, 1, depth], 'water', { level: '0' })
      }
    }
    put([5, 1, depth], 'polished_andesite')
    put([6, 1, depth], depth % 4 === 0 ? 'chiseled_stone_bricks' : 'stone_bricks')
    put([7, 1, depth], 'polished_andesite')
  }

  for (const depth of [0, 15]) {
    for (let horizontal = 0; horizontal < 13; horizontal++) {
      for (let vertical = 2; vertical < 10; vertical++) {
        put([horizontal, vertical, depth], masonry(horizontal, vertical, depth))
      }
    }
    fill([5, 2, depth], [7, 6, depth], 'air')
    for (const horizontal of [4, 8]) {
      fill([horizontal, 2, depth], [horizontal, 6, depth], 'polished_andesite')
      put([horizontal, 2, depth], 'chiseled_stone_bricks')
      put([horizontal, 6, depth], 'chiseled_stone_bricks')
    }
    fill([4, 7, depth], [8, 7, depth], 'stone_bricks')
    put([5, 6, depth], 'stone_brick_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
    put([7, 6, depth], 'stone_brick_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
  }

  for (const depth of [2, 7, 12]) {
    fill([1, 2, depth], [1, 8, depth], 'chiseled_stone_bricks')
    fill([11, 2, depth], [11, 8, depth], 'chiseled_stone_bricks')
    fill([1, 9, depth], [11, 9, depth], 'polished_andesite')
    put([2, 8, depth], 'stone_brick_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
    put([10, 8, depth], 'stone_brick_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
  }

  for (const wall of [1, 11]) {
    const interior = wall === 1 ? 2 : 10
    const facing = wall === 1 ? 'east' : 'west'
    for (const depth of [3, 8, 13]) {
      put([interior, 4, depth], 'chiseled_stone_bricks')
      put([interior, 5, depth], 'end_rod', { facing: 'up' })
      put([interior, 6, depth], 'end_rod', { facing: 'down' })
      put([interior, 7, depth], 'polished_andesite')
    }
    for (const [depth, vertical, crystal] of [[2, 3, 'amethyst_cluster'], [5, 4, 'large_amethyst_bud'], [6, 6, 'amethyst_cluster'], [9, 3, 'amethyst_cluster'], [11, 5, 'large_amethyst_bud'], [13, 3, 'medium_amethyst_bud']]) {
      put([wall, vertical, depth], 'amethyst_block')
      put([interior, vertical, depth], crystal, { facing, waterlogged: 'false' })
      if (vertical < 6) put([wall, vertical + 1, depth], 'amethyst_block')
    }
    for (const depth of [4, 10]) {
      fill([wall, 5, depth], [wall, 8, depth], 'amethyst_block')
      put([wall, 4, depth], 'calcite')
    }
  }

  for (const [horizontal, depth] of [[3, 5], [8, 9]]) {
    fill([horizontal, 5, depth], [horizontal + 1, 5, depth + 1], 'polished_deepslate')
    fill([horizontal, 6, depth], [horizontal + 1, 7, depth + 1], 'iron_bars', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
    fill([horizontal, 8, depth], [horizontal + 1, 8, depth + 1], 'polished_deepslate')
    fill([horizontal, 9, depth], [horizontal + 1, 9, depth + 1], 'chain', { axis: 'y', waterlogged: 'false' })
  }

  for (const horizontal of [2, 3, 9, 10]) {
    fill([horizontal, 2, 15], [horizontal, 6, 15], 'amethyst_block')
    fill([horizontal, 2, 14], [horizontal, 5, 14], 'iron_bars', { north: 'false', east: 'false', south: 'true', west: 'false', waterlogged: 'false' })
  }
  for (const depth of [4, 10, 13]) {
    fill([5, 10, depth], [7, 10, depth], 'amethyst_block')
    put([6, 9, depth], 'large_amethyst_bud', { facing: 'down', waterlogged: 'false' })
  }

  const fullBlocks = new Set(['stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks', 'andesite', 'polished_andesite', 'chiseled_stone_bricks', 'deepslate_tiles', 'polished_deepslate', 'amethyst_block', 'calcite'])
  const directions = { north: [0, 0, -1], east: [1, 0, 0], south: [0, 0, 1], west: [-1, 0, 0] }
  for (const placed of structure.getBlocks()) {
    if (placed.state.getName().toString() !== 'minecraft:iron_bars') continue
    const properties = { waterlogged: 'false' }
    for (const [direction, offset] of Object.entries(directions)) {
      const neighbor = structure.getBlock(placed.pos.map((coordinate, axis) => coordinate + offset[axis]))
      const name = neighbor?.state.getName().toString().replace('minecraft:', '')
      properties[direction] = String(name === 'iron_bars' || fullBlocks.has(name))
    }
    put(placed.pos, 'iron_bars', properties)
  }

  return { structure, metadata: { name: 'room', design: 'S07 crystal cave flooded short corridor', size: [13, 11, 16], entrance: [6, 2, 0], exit: [6, 2, 15] } }
}
