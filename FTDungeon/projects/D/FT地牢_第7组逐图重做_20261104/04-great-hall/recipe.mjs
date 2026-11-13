export default function build(api) {
  const structure = api.createStructure([31, 20, 35])
  const put = (blockX, blockY, blockZ, block, properties = {}, data) => {
    const defaults = {}
    if (['mycelium', 'podzol'].includes(block)) defaults.snowy = 'false'
    if (['mushroom_stem', 'red_mushroom_block'].includes(block)) {
      for (const face of ['north', 'east', 'south', 'west', 'up', 'down']) defaults[face] = 'true'
    }
    api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`, { ...defaults, ...properties }, data)
  }
  const box = (minimum, maximum, block, properties = {}) => {
    for (let blockX = minimum[0]; blockX <= maximum[0]; blockX++) {
      for (let blockY = minimum[1]; blockY <= maximum[1]; blockY++) {
        for (let blockZ = minimum[2]; blockZ <= maximum[2]; blockZ++) put(blockX, blockY, blockZ, block, properties)
      }
    }
  }
  box([0, 0, 0], [30, 19, 34], 'air')
  box([0, 0, 0], [30, 0, 34], 'deepslate_bricks')
  for (let blockX = 0; blockX <= 30; blockX++) {
    for (let blockZ = 0; blockZ <= 34; blockZ++) {
      const fleck = (blockX * 17 + blockZ * 31 + Math.floor(blockZ / 3)) % 23
      const central = blockX >= 12 && blockX <= 18
      put(blockX, 1, blockZ, central ? (fleck < 6 ? 'mossy_stone_bricks' : fleck < 12 ? 'purple_terracotta' : 'deepslate_tiles') : fleck < 9 ? 'mycelium' : fleck < 14 ? 'rooted_dirt' : 'mossy_stone_bricks')
      if (blockX === 0 || blockX === 30 || blockZ === 0 || blockZ === 34) {
        for (let blockY = 2; blockY < 19; blockY++) {
          const fleck = (blockX * 13 + blockY * 7 + blockZ * 11) % 19
          put(blockX, blockY, blockZ, blockY >= 13 ? fleck < 12 ? 'rooted_dirt' : 'mycelium' : fleck < 4 ? 'mossy_stone_bricks' : 'deepslate_bricks')
        }
      }
      put(blockX, 19, blockZ, 'deepslate_tiles')
    }
  }
  for (const pillarX of [8, 22]) {
    for (const pillarZ of [8, 17, 26]) {
      box([pillarX - 2, 2, pillarZ - 2], [pillarX + 2, 2, pillarZ + 2], 'deepslate_bricks')
      box([pillarX - 1, 3, pillarZ - 1], [pillarX + 1, 3, pillarZ + 1], 'stone_bricks')
      box([pillarX, 4, pillarZ], [pillarX, 13, pillarZ], 'stone_bricks')
      box([pillarX - 1, 14, pillarZ - 1], [pillarX + 1, 14, pillarZ + 1], 'mossy_stone_bricks')
      for (const [offsetX, offsetZ] of [[-1, 0], [1, 0], [0, -1], [0, 1]]) {
        put(pillarX + offsetX, 3, pillarZ + offsetZ, 'amethyst_block')
        put(pillarX + offsetX, 4, pillarZ + offsetZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
      }
      put(pillarX, 3, pillarZ + 2, 'soul_lantern', { hanging: 'false', waterlogged: 'false' })
      put(pillarX, 11, pillarZ + 1, 'red_wall_banner', { facing: 'south' }, api.createBlockNbt({ id: 'minecraft:banner' }))
      put(pillarX, 13, pillarZ - 1, 'shroomlight')
    }
  }
  box([1, 11, 1], [4, 12, 33], 'rooted_dirt')
  box([26, 11, 1], [29, 12, 33], 'rooted_dirt')
  box([1, 13, 1], [4, 13, 33], 'mycelium')
  box([26, 13, 1], [29, 13, 33], 'mycelium')
  box([1, 15, 1], [29, 16, 3], 'rooted_dirt')
  box([1, 17, 1], [29, 17, 3], 'mycelium')
  const mushroom = (stemX, stemY, stemZ, height, radius) => {
    box([stemX, stemY, stemZ], [stemX, stemY + height - 1, stemZ], 'mushroom_stem')
    const capY = stemY + height
    for (let offsetX = -radius; offsetX <= radius; offsetX++) {
      for (let offsetZ = -radius; offsetZ <= radius; offsetZ++) {
        if (Math.abs(offsetX) === radius && Math.abs(offsetZ) === radius) continue
        put(stemX + offsetX, capY, stemZ + offsetZ, 'red_mushroom_block')
        if (Math.abs(offsetX) < radius && Math.abs(offsetZ) < radius) put(stemX + offsetX, capY + 1, stemZ + offsetZ, 'red_mushroom_block')
      }
    }
  }
  mushroom(3, 14, 27, 3, 3)
  mushroom(27, 14, 6, 3, 3)
  mushroom(27, 14, 15, 2, 2)
  mushroom(3, 14, 10, 2, 2)
  mushroom(10, 2, 31, 2, 2)
  mushroom(25, 2, 30, 1, 2)
  for (const baseX of [3, 25]) {
    for (const baseZ of [7, 16, 27]) {
      box([baseX - 1, 1, baseZ - 1], [baseX + 2, 1, baseZ + 3], 'polished_blackstone_bricks')
      box([baseX, 1, baseZ], [baseX + 1, 1, baseZ + 2], 'water', { level: '0' })
    }
  }
  for (const wallX of [0, 30]) {
    for (const lampZ of [5, 13, 21, 30]) put(wallX, 8, lampZ, 'shroomlight')
  }
  put(15, 12, 0, 'shroomlight')
  for (const columnX of [6, 24]) {
    for (const lampZ of [8, 17, 26]) {
      box([columnX, 11, lampZ], [columnX, 18, lampZ], 'chain', { axis: 'y', waterlogged: 'false' })
      put(columnX, 10, lampZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
    }
  }
  for (const [wallX, side] of [[1, 'west'], [29, 'east']]) {
    for (const vineZ of [4, 11, 19, 31]) {
      for (let blockY = 8; blockY <= 12; blockY++) put(wallX, blockY, vineZ, 'vine', { [side]: 'true', up: 'false' })
    }
    for (const lichenZ of [9, 18, 29]) put(wallX, 5, lichenZ, 'glow_lichen', { [side]: 'true', waterlogged: 'false' })
  }
  for (const [fungusX, fungusZ] of [[2, 4], [5, 11], [3, 23], [6, 32], [11, 28], [20, 31], [28, 3], [24, 12], [28, 25], [27, 32]]) {
    put(fungusX, 1, fungusZ, 'mycelium')
    put(fungusX, 2, fungusZ, (fungusX + fungusZ) % 2 ? 'red_mushroom' : 'brown_mushroom')
  }
  for (const [fungusX, fungusZ] of [[2, 6], [3, 19], [28, 10], [27, 25]]) put(fungusX, 14, fungusZ, 'red_mushroom')
  for (const endZ of [0, 34]) {
    for (let blockX = 12; blockX <= 18; blockX++) {
      for (let blockY = 2; blockY <= 9; blockY++) {
        const aperture = blockX >= 14 && blockX <= 16 && blockY <= 5 || blockX === 15 && blockY === 6
        put(blockX, blockY, endZ, aperture ? 'air' : 'stone_bricks')
      }
      put(blockX, 1, endZ, 'stone_bricks')
    }
  }
  for (const endX of [0, 30]) {
    for (let blockZ = 20; blockZ <= 26; blockZ++) {
      for (let blockY = 2; blockY <= 8; blockY++) {
        const aperture = blockZ >= 22 && blockZ <= 24 && blockY <= 5 || blockZ === 23 && blockY === 6
        put(endX, blockY, blockZ, aperture ? 'air' : 'stone_bricks')
      }
      put(endX, 1, blockZ, 'stone_bricks')
    }
  }
  return { structure, metadata: { name: 'room' } }
}
