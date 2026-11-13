export default function build(api) {
  const size = [13, 11, 40]
  const structure = api.createStructure(size)
  const put = (position, block) => api.setBlock(structure, position, `minecraft:${block}`)
  const box = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const masonry = (blockX, blockY, blockZ, floor = false) => {
    const variation = (blockX * 61 + blockY * 37 + blockZ * 97 + blockX * blockZ * 13) % 100
    if ((floor || blockY < 3) && variation < 12) return 'mossy_stone_bricks'
    if (variation < 31) return 'cracked_stone_bricks'
    return 'stone_bricks'
  }
  box([0, 0, 0], [12, 10, 39], 'air')
  for (let blockX = 0; blockX < size[0]; blockX++) {
    for (let blockZ = 0; blockZ < size[2]; blockZ++) {
      put([blockX, 0, blockZ], masonry(blockX, 0, blockZ, true))
      put([blockX, 10, blockZ], masonry(blockX, 10, blockZ))
    }
  }
  for (const blockX of [0, 1, 11, 12]) {
    for (let blockY = 1; blockY < 10; blockY++) {
      for (let blockZ = 0; blockZ < size[2]; blockZ++) {
        put([blockX, blockY, blockZ], masonry(blockX, blockY, blockZ))
      }
    }
  }
  for (const blockZ of [0, 10, 20, 30, 39]) {
    for (const blockX of [2, 10]) {
      box([blockX, 1, blockZ], [blockX, 7, blockZ], 'stone_bricks')
      put([blockX, 1, blockZ], 'chiseled_stone_bricks')
      put([blockX, 7, blockZ], 'chiseled_stone_bricks')
    }
    put([3, 7, blockZ], 'stone_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
    put([9, 7, blockZ], 'stone_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    box([2, 8, blockZ], [4, 8, blockZ], 'stone_bricks')
    box([8, 8, blockZ], [10, 8, blockZ], 'stone_bricks')
    box([2, 9, blockZ], [10, 9, blockZ], 'stone_bricks')
    put([6, 9, blockZ], 'chiseled_stone_bricks')
  }
  for (const blockZ of [0, 39]) {
    box([0, 1, blockZ], [2, 9, blockZ], 'stone_bricks')
    box([10, 1, blockZ], [12, 9, blockZ], 'stone_bricks')
    box([3, 5, blockZ], [3, 9, blockZ], 'stone_bricks')
    box([9, 5, blockZ], [9, 9, blockZ], 'stone_bricks')
    box([4, 7, blockZ], [4, 9, blockZ], 'stone_bricks')
    box([8, 7, blockZ], [8, 9, blockZ], 'stone_bricks')
    box([5, 8, blockZ], [7, 9, blockZ], 'stone_bricks')
    put([4, 6, blockZ], 'stone_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
    put([8, 6, blockZ], 'stone_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    put([6, 8, blockZ], 'chiseled_stone_bricks')
  }
  for (const nicheZ of [3, 13, 23, 33]) {
    for (const wallX of [1, 11]) {
      box([wallX, 3, nicheZ - 1], [wallX, 5, nicheZ + 1], 'air')
      const backX = wallX === 1 ? 0 : 12
      box([backX, 2, nicheZ - 1], [backX, 5, nicheZ + 1], 'polished_andesite')
      put([wallX, 2, nicheZ], 'chiseled_stone_bricks')
      const shelfX = wallX === 1 ? 2 : 10
      put([shelfX, 2, nicheZ], 'polished_blackstone')
      put([shelfX, 1, nicheZ], 'iron_bars')
      put([shelfX, 3, nicheZ], 'candle[candles=3,lit=true,waterlogged=false]')
      put([wallX, 6, nicheZ], 'chiseled_stone_bricks')
    }
  }
  for (const cageZ of [6, 16, 26, 35]) {
    for (const cageX of [3, 7]) {
      for (let localX = 0; localX < 3; localX++) {
        for (let localZ = 0; localZ < 3; localZ++) {
          put([cageX + localX, 5, cageZ + localZ], 'iron_trapdoor[facing=north,half=top,open=false,powered=false,waterlogged=false]')
          put([cageX + localX, 8, cageZ + localZ], 'iron_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]')
          if (localX === 0 || localX === 2 || localZ === 0 || localZ === 2) {
            box([cageX + localX, 6, cageZ + localZ], [cageX + localX, 7, cageZ + localZ], 'iron_bars')
          }
        }
      }
      put([cageX + 1, 8, cageZ + 1], 'polished_blackstone')
      put([cageX + 1, 9, cageZ + 1], 'chain[axis=y,waterlogged=false]')
    }
  }
  for (const blockX of [2, 10]) {
    for (const blockZ of [5, 15, 25, 34]) put([blockX, 7, blockZ], 'cobweb')
    for (const blockZ of [7, 19, 29]) put([blockX, 1, blockZ], 'cobweb')
    for (const blockZ of [8, 18, 28, 36]) put([blockX, 1, blockZ], 'stone_brick_slab[type=bottom,waterlogged=false]')
  }
  return {
    structure,
    metadata: {
      name: 'room', design_id: 'M12', delivery_id: 'corridor-M12', title: '陈旧石砖吊笼烛廊',
      axis: 'north-south', guaranteed_clear_lane: { x: [4, 8], y: [1, 4], z: [0, 39] },
      entrance: [6, 1, 0], exit: [6, 1, 39], cages: 8, candle_niches: 8,
      restrictions: 'Independent candidate only; no deployment or player-save access.'
    }
  }
}
