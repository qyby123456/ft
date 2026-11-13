export default function build(api) {
  const structure = api.createStructure([17, 11, 17])
  const put = (position, block, properties, nbt) => api.setBlock(structure, position, block, properties, nbt)
  const box = (from, to, block, properties) => api.fill(structure, from, to, block, properties)
  box([0, 0, 0], [16, 10, 16], 'minecraft:air')
  box([0, 0, 0], [16, 1, 16], 'minecraft:stone_bricks')
  for (let blockX = 1; blockX < 16; blockX++) {
    for (let blockZ = 1; blockZ < 16; blockZ++) {
      const weathering = (blockX * 17 + blockZ * 31) % 19
      put([blockX, 1, blockZ], weathering < 2 ? 'minecraft:coarse_dirt' : weathering < 4 ? 'minecraft:mossy_stone_bricks' : weathering < 7 ? 'minecraft:cracked_stone_bricks' : 'minecraft:spruce_planks')
    }
  }
  for (const [from, to] of [ [[0, 2, 0], [16, 9, 0]], [[0, 2, 16], [16, 9, 16]], [[0, 2, 1], [0, 9, 15]], [[16, 2, 1], [16, 9, 15]] ]) box(from, to, 'minecraft:stone_bricks')
  for (let blockX = 0; blockX < 17; blockX++) {
    for (let blockY = 2; blockY < 9; blockY++) {
      for (const blockZ of [0, 16]) {
        const weathering = (blockX * 13 + blockY * 11 + blockZ) % 17
        if (weathering < 3) put([blockX, blockY, blockZ], blockY < 5 ? 'minecraft:mossy_stone_bricks' : 'minecraft:cracked_stone_bricks')
      }
    }
  }
  for (let blockZ = 1; blockZ < 16; blockZ++) {
    for (let blockY = 2; blockY < 9; blockY++) {
      for (const blockX of [0, 16]) {
        if ((blockZ * 7 + blockY * 5 + blockX) % 11 < 3) put([blockX, blockY, blockZ], blockY < 5 ? 'minecraft:mossy_stone_bricks' : 'minecraft:cracked_stone_bricks')
      }
    }
  }
  for (const blockX of [1, 15]) {
    for (const blockZ of [1, 8, 15]) {
      put([blockX, 2, blockZ], 'minecraft:chiseled_stone_bricks')
      box([blockX, 3, blockZ], [blockX, 8, blockZ], 'minecraft:stripped_spruce_log', { axis: 'y' })
      put([blockX, 9, blockZ], 'minecraft:spruce_wood', { axis: 'y' })
    }
  }
  box([0, 10, 0], [16, 10, 16], 'minecraft:spruce_planks')
  for (const blockZ of [1, 8, 15]) box([1, 9, blockZ], [15, 9, blockZ], 'minecraft:spruce_log', { axis: 'x' })
  for (const blockX of [1, 15]) box([blockX, 9, 1], [blockX, 9, 15], 'minecraft:spruce_log', { axis: 'z' })
  for (const blockZ of [4, 5, 6]) box([0, 4, blockZ], [0, 6, blockZ], 'minecraft:glass')
  for (let blockX = 4; blockX <= 6; blockX++) box([blockX, 4, 16], [blockX, 6, 16], 'minecraft:glass')
  box([1, 3, 4], [1, 3, 6], 'minecraft:stone_brick_slab', { type: 'top', waterlogged: 'false' })
  box([4, 3, 15], [6, 3, 15], 'minecraft:stone_brick_slab', { type: 'top', waterlogged: 'false' })
  box([1, 2, 10], [1, 5, 13], 'minecraft:bookshelf')
  box([2, 2, 15], [6, 5, 15], 'minecraft:bookshelf')
  box([10, 2, 15], [13, 4, 15], 'minecraft:bookshelf')
  put([12, 4, 15], 'minecraft:spruce_planks')
  put([13, 4, 15], 'minecraft:air')
  for (const blockZ of [5, 6]) {
    put([3, 2, blockZ], 'minecraft:spruce_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
    put([4, 2, blockZ], 'minecraft:spruce_slab', { type: 'top', waterlogged: 'false' })
    put([5, 2, blockZ], 'minecraft:spruce_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
  }
  put([3, 3, 5], 'minecraft:white_carpet')
  put([4, 3, 6], 'minecraft:brown_carpet')
  put([5, 3, 5], 'minecraft:lantern', { hanging: 'false', waterlogged: 'false' })
  put([4, 2, 8], 'minecraft:spruce_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  put([3, 2, 9], 'minecraft:spruce_slab', { type: 'bottom', waterlogged: 'false' })
  box([14, 2, 6], [15, 5, 10], 'minecraft:stone_bricks')
  box([14, 2, 7], [14, 4, 9], 'minecraft:air')
  box([15, 2, 7], [15, 4, 9], 'minecraft:polished_blackstone_bricks')
  box([14, 2, 7], [14, 2, 9], 'minecraft:blackstone_slab', { type: 'bottom', waterlogged: 'false' })
  box([14, 6, 8], [15, 9, 8], 'minecraft:cracked_stone_bricks')
  put([14, 6, 7], 'minecraft:potted_dead_bush')
  put([14, 6, 9], 'minecraft:candle', { candles: '1', lit: 'false', waterlogged: 'false' })
  for (const [blockX, blockZ, facing] of [[2, 2, 'south'], [13, 14, 'north'], [14, 3, 'west']]) {
    put([blockX, 2, blockZ], 'minecraft:barrel', { facing, open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel', Items: [] }))
  }
  put([2, 3, 2], 'minecraft:spruce_planks')
  put([13, 3, 14], 'minecraft:lantern', { hanging: 'false', waterlogged: 'false' })
  put([14, 3, 3], 'minecraft:potted_fern')
  for (const [blockX, blockZ] of [[3, 8], [12, 8]]) {
    box([blockX, 7, blockZ], [blockX, 8, blockZ], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
    put([blockX, 6, blockZ], 'minecraft:lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const position of [[1, 7, 2], [2, 7, 15], [14, 7, 15], [15, 7, 6], [1, 6, 13]]) put(position, 'minecraft:cobweb')
  for (let blockX = 6; blockX <= 10; blockX++) {
    for (let blockZ = 8; blockZ <= 12; blockZ++) {
      const edge = blockX === 6 || blockX === 10 || blockZ === 8 || blockZ === 12
      put([blockX, 1, blockZ], edge ? 'minecraft:brown_wool' : 'minecraft:red_wool')
    }
  }
  put([6, 1, 12], 'minecraft:spruce_planks')
  put([10, 1, 9], 'minecraft:coarse_dirt')
  put([6, 2, 13], 'minecraft:white_carpet')
  put([11, 2, 12], 'minecraft:white_carpet')
  put([2, 2, 12], 'minecraft:brown_carpet')
  for (const position of [[12, 2, 6], [13, 2, 11]]) put(position, 'minecraft:cobblestone_slab', { type: 'bottom', waterlogged: 'false' })
  box([7, 2, 0], [9, 5, 0], 'minecraft:air')
  box([6, 2, 0], [6, 6, 0], 'minecraft:chiseled_stone_bricks')
  box([10, 2, 0], [10, 6, 0], 'minecraft:chiseled_stone_bricks')
  box([7, 6, 0], [9, 6, 0], 'minecraft:spruce_log', { axis: 'x' })
  box([7, 2, 1], [9, 5, 14], 'minecraft:air')
  return { structure, metadata: { name: 'room', id: 'zone-A01', title: 'A01 废弃书房', entrance: [8, 2, 0], interior: [8, 2, 4], reference_sha256: 'e28fe9c5d774a0f141b616ebeb683f09bc6a8c0bc90fe74e39d88b4f1f2d9a0d' } }
}
