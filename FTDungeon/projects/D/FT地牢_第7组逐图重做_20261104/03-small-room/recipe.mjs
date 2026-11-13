export default function build(api) {
  const structure = api.createStructure([17, 13, 17])
  const place = (blockX, blockY, blockZ, block, properties = {}, data) => {
    const defaults = {}
    if (['mycelium', 'podzol'].includes(block)) defaults.snowy = 'false'
    if (['mushroom_stem', 'red_mushroom_block'].includes(block)) {
      for (const face of ['north', 'east', 'south', 'west', 'up', 'down']) defaults[face] = 'true'
    }
    api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`, { ...defaults, ...properties }, data)
  }
  const fill = (minimum, maximum, block, properties) => {
    for (let blockX = minimum[0]; blockX <= maximum[0]; blockX++) {
      for (let blockY = minimum[1]; blockY <= maximum[1]; blockY++) {
        for (let blockZ = minimum[2]; blockZ <= maximum[2]; blockZ++) place(blockX, blockY, blockZ, block, properties)
      }
    }
  }
  fill([0, 0, 0], [16, 12, 16], 'air')
  fill([0, 0, 0], [16, 0, 16], 'deepslate_bricks')
  for (let blockX = 0; blockX <= 16; blockX++) {
    for (let blockZ = 0; blockZ <= 16; blockZ++) {
      const scatter = (blockX * 29 + blockZ * 11) % 19
      place(blockX, 1, blockZ, scatter < 5 ? 'mycelium' : scatter < 9 ? 'purple_terracotta' : scatter < 13 ? 'rooted_dirt' : 'mossy_stone_bricks')
      if (blockX === 0 || blockX === 16 || blockZ === 0 || blockZ === 16) {
        for (let blockY = 2; blockY < 12; blockY++) {
          const mottled = (blockX * 7 + blockY * 13 + blockZ * 3) % 13
          place(blockX, blockY, blockZ, blockY >= 8 && mottled < 7 ? 'rooted_dirt' : mottled < 3 ? 'mossy_stone_bricks' : 'deepslate_bricks')
        }
      }
      place(blockX, 12, blockZ, 'deepslate_tiles')
    }
  }
  fill([1, 4, 1], [5, 4, 4], 'mossy_stone_bricks')
  fill([1, 5, 1], [5, 5, 4], 'mycelium')
  fill([1, 2, 3], [1, 3, 4], 'chiseled_stone_bricks')
  fill([1, 2, 1], [2, 3, 1], 'mossy_stone_bricks')
  fill([2, 3, 1], [5, 3, 1], 'air')
  place(4, 2, 1, 'chest', { facing: 'south', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest' }))
  fill([11, 7, 1], [15, 7, 3], 'rooted_dirt')
  fill([11, 8, 1], [15, 8, 3], 'mycelium')
  fill([1, 9, 6], [1, 10, 15], 'rooted_dirt')
  fill([15, 8, 1], [15, 8, 14], 'mycelium')
  const cap = (stemX, floorY, stemZ, stemLength, radius) => {
    fill([stemX, floorY, stemZ], [stemX, floorY + stemLength - 1, stemZ], 'mushroom_stem')
    const capY = floorY + stemLength
    for (let deltaX = -radius; deltaX <= radius; deltaX++) {
      for (let deltaZ = -radius; deltaZ <= radius; deltaZ++) {
        if (Math.abs(deltaX) === radius && Math.abs(deltaZ) === radius) continue
        place(stemX + deltaX, capY, stemZ + deltaZ, 'red_mushroom_block')
        if (Math.abs(deltaX) === radius || Math.abs(deltaZ) === radius) place(stemX + deltaX, capY - 1, stemZ + deltaZ, 'red_mushroom_block')
        if (Math.abs(deltaX) < radius && Math.abs(deltaZ) < radius) place(stemX + deltaX, capY + 1, stemZ + deltaZ, 'red_mushroom_block')
      }
    }
  }
  cap(3, 6, 3, 3, 2)
  cap(13, 9, 3, 1, 2)
  cap(14, 9, 7, 1, 1)
  place(8, 2, 8, 'mossy_stone_bricks')
  place(8, 3, 8, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  for (const [baseX, baseZ] of [[4, 11], [12, 6]]) {
    fill([baseX - 1, 1, baseZ - 1], [baseX + 2, 1, baseZ + 2], 'polished_blackstone_bricks')
    fill([baseX, 1, baseZ], [baseX + 1, 1, baseZ + 1], 'water', { level: '0' })
  }
  for (const [blockX, blockY, blockZ] of [[1, 3, 7], [6, 6, 0], [11, 5, 0], [16, 5, 8]]) place(blockX, blockY, blockZ, 'shroomlight')
  for (const [blockX, blockZ] of [[2, 10], [13, 13]]) {
    place(blockX, 2, blockZ, 'amethyst_block')
    place(blockX, 3, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[2, 6], [6, 3], [13, 4], [14, 14], [7, 14]]) place(blockX, 2, blockZ, 'soul_lantern', { hanging: 'false', waterlogged: 'false' })
  for (const [blockX, blockY, blockZ] of [[2, 6, 2], [5, 6, 3], [2, 2, 13], [6, 2, 13], [11, 2, 13], [14, 2, 9], [14, 9, 11]]) {
    place(blockX, blockY - 1, blockZ, 'mycelium')
    place(blockX, blockY, blockZ, (blockX + blockZ) % 2 ? 'red_mushroom' : 'brown_mushroom')
  }
  for (const blockZ of [5, 9, 14]) {
    for (let blockY = 6; blockY <= 8; blockY++) place(1, blockY, blockZ, 'vine', { west: 'true', up: 'false' })
  }
  for (const blockX of [6, 10, 14]) {
    for (let blockY = 8; blockY <= 10; blockY++) place(blockX, blockY, 1, 'vine', { north: 'true', up: 'false' })
  }
  for (const blockZ of [4, 13]) place(15, 4, blockZ, 'glow_lichen', { east: 'true', waterlogged: 'false' })
  for (let blockX = 6; blockX <= 10; blockX++) {
    for (let blockY = 2; blockY <= 7; blockY++) {
      const open = blockX >= 7 && blockX <= 9 && blockY <= 5 || blockX === 8 && blockY === 6
      place(blockX, blockY, 0, open ? 'air' : 'stone_bricks')
    }
    place(blockX, 1, 0, 'stone_bricks')
  }
  for (let blockZ = 9; blockZ <= 12; blockZ++) {
    for (let blockY = 2; blockY <= 5; blockY++) place(16, blockY, blockZ, blockZ >= 10 && blockZ <= 11 && blockY <= 4 ? 'air' : 'stone_bricks')
    place(16, 1, blockZ, 'stone_bricks')
  }
  return { structure, metadata: { name: 'room' } }
}
