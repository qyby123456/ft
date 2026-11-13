export default function build(api) {
  const structure = api.createStructure([15, 11, 16])
  const put = (blockX, blockY, blockZ, block) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`)
  const box = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  box([0, 0, 0], [14, 10, 15], 'air')
  box([0, 0, 0], [14, 1, 15], 'polished_blackstone_bricks')
  box([0, 2, 0], [14, 2, 15], 'deepslate_tiles')
  box([0, 3, 0], [2, 8, 15], 'polished_blackstone_bricks')
  box([12, 3, 0], [14, 8, 15], 'polished_blackstone_bricks')
  box([0, 9, 0], [14, 10, 15], 'deepslate_tiles')
  for (let blockZ = 0; blockZ < 16; blockZ++) {
    for (let blockY = 3; blockY <= 8; blockY++) {
      for (const blockX of [2, 12]) {
        const pattern = (blockZ * 17 + blockY * 13 + blockX * 3) % 23
        if (pattern < 3) put(blockX, blockY, blockZ, 'cracked_polished_blackstone_bricks')
        if (pattern === 6 || pattern === 7) put(blockX, blockY, blockZ, 'mossy_stone_bricks')
      }
    }
    for (const blockX of [6, 7, 8]) {
      put(blockX, 2, blockZ, (blockZ + blockX) % 4 === 0 ? 'polished_deepslate' : 'deepslate_tiles')
    }
    for (const blockX of [3, 11]) put(blockX, 2, blockZ, 'polished_blackstone')
  }
  for (const startX of [4, 9]) box([startX, 2, 2], [startX + 1, 2, 13], 'water[level=0]')
  for (const archZ of [0, 5, 10, 15]) {
    for (const blockX of [3, 11]) {
      box([blockX, 3, archZ], [blockX, 7, archZ], 'polished_blackstone_bricks')
      put(blockX, 3, archZ, 'chiseled_polished_blackstone')
      put(blockX, 6, archZ, 'chiseled_polished_blackstone')
    }
    put(4, 7, archZ, 'polished_blackstone_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    put(10, 7, archZ, 'polished_blackstone_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
    box([4, 8, archZ], [10, 8, archZ], 'polished_blackstone_bricks')
    put(7, 8, archZ, 'chiseled_polished_blackstone')
  }
  for (const startZ of [3, 9]) {
    for (const side of ['west', 'east']) {
      const reservoirX = side === 'west' ? 1 : 13
      const windowX = side === 'west' ? 2 : 12
      const counterX = side === 'west' ? 3 : 11
      box([reservoirX, 3, startZ - 1], [reservoirX, 5, startZ + 3], 'nether_bricks')
      box([reservoirX, 4, startZ], [reservoirX, 4, startZ + 2], 'lava[level=0]')
      box([windowX, 4, startZ], [windowX, 4, startZ + 2], 'glass')
      box([counterX, 3, startZ], [counterX, 3, startZ + 2], 'polished_blackstone')
      box([windowX, 5, startZ], [windowX, 5, startZ + 2], 'chiseled_polished_blackstone')
    }
  }
  for (const blockZ of [2, 7, 13]) {
    for (const blockX of [3, 11]) put(blockX, 3, blockZ, 'anvil[facing=north]')
    for (const blockX of [4, 10]) {
      put(blockX, 8, blockZ, 'chain[axis=y,waterlogged=false]')
      put(blockX, 7, blockZ, 'chain[axis=y,waterlogged=false]')
      put(blockX, 6, blockZ, 'soul_lantern[hanging=true,waterlogged=false]')
    }
  }
  for (const blockZ of [2, 7, 13]) {
    for (const blockY of [5, 6]) {
      put(2, blockY, blockZ, 'mossy_stone_bricks')
      put(12, blockY, blockZ, 'mossy_stone_bricks')
    }
  }
  for (const blockZ of [0, 15]) {
    for (const blockX of [4, 10]) {
      box([blockX, 3, blockZ], [blockX, 5, blockZ], 'chiseled_polished_blackstone')
      put(blockX, 6, blockZ, 'chiseled_polished_blackstone')
      const insetZ = blockZ === 0 ? 1 : 14
      box([blockX, 3, insetZ], [blockX, 5, insetZ], 'iron_bars')
    }
    box([5, 6, blockZ], [9, 6, blockZ], 'polished_blackstone_bricks')
    box([4, 7, blockZ], [10, 7, blockZ], 'polished_blackstone_bricks')
    box([3, 8, blockZ], [11, 8, blockZ], 'polished_blackstone_bricks')
    put(5, 5, blockZ, 'polished_blackstone_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    put(9, 5, blockZ, 'polished_blackstone_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
  }
  return { structure, metadata: { name: 'room', theme: 'S18 flooded lava forge corridor', size: [15, 11, 16] } }
}
