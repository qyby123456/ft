export default function build(api) {
  const structure = api.createStructure([25, 14, 25])
  const set = (blockX, blockY, blockZ, block, properties = {}) => {
    const defaults = {}
    if (['mycelium', 'podzol'].includes(block)) defaults.snowy = 'false'
    if (['mushroom_stem', 'red_mushroom_block'].includes(block)) {
      for (const face of ['north', 'east', 'south', 'west', 'up', 'down']) defaults[face] = 'true'
    }
    api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`, { ...defaults, ...properties })
  }
  const fill = (minimum, maximum, block, properties = {}) => {
    for (let blockX = minimum[0]; blockX <= maximum[0]; blockX++) {
      for (let blockY = minimum[1]; blockY <= maximum[1]; blockY++) {
        for (let blockZ = minimum[2]; blockZ <= maximum[2]; blockZ++) set(blockX, blockY, blockZ, block, properties)
      }
    }
  }
  fill([0, 0, 0], [24, 13, 24], 'air')
  for (let blockX = 0; blockX <= 24; blockX++) {
    for (let blockZ = 0; blockZ <= 24; blockZ++) {
      if (blockX >= 12 && blockZ <= 12) continue
      set(blockX, 0, blockZ, 'deepslate_bricks')
      const patch = (blockX * 7 + blockZ * 17 + Math.floor(blockX / 3)) % 19
      set(blockX, 1, blockZ, patch < 7 ? 'mycelium' : patch < 10 ? 'purple_terracotta' : patch < 14 ? 'rooted_dirt' : 'mossy_stone_bricks')
      const inside = blockX >= 2 && blockX <= 9 && blockZ >= 1 && blockZ <= 22 || blockX >= 2 && blockX <= 23 && blockZ >= 15 && blockZ <= 22
      if (!inside) {
        for (let blockY = 2; blockY < 13; blockY++) {
          const weathering = (blockX * 11 + blockY * 7 + blockZ * 3) % 13
          set(blockX, blockY, blockZ, blockY >= 9 ? weathering < 9 ? 'rooted_dirt' : 'mycelium' : weathering < 5 ? 'mossy_stone_bricks' : 'deepslate_bricks')
        }
      }
      set(blockX, 13, blockZ, 'rooted_dirt')
    }
  }
  fill([10, 2, 15], [13, 2, 16], 'mossy_stone_bricks')
  fill([11, 3, 15], [12, 3, 15], 'chiseled_stone_bricks')
  set(11, 4, 15, 'shroomlight')
  fill([10, 5, 15], [14, 5, 15], 'rooted_dirt')
  fill([10, 6, 15], [14, 6, 15], 'mycelium')
  const mushroom = (stemX, stemY, stemZ, height, radius) => {
    fill([stemX, stemY, stemZ], [stemX, stemY + height - 1, stemZ], 'mushroom_stem')
    for (let offsetX = -radius; offsetX <= radius; offsetX++) {
      for (let offsetZ = -radius; offsetZ <= radius; offsetZ++) {
        if (Math.abs(offsetX) === radius && Math.abs(offsetZ) === radius) continue
        set(stemX + offsetX, stemY + height, stemZ + offsetZ, 'red_mushroom_block')
        if (Math.abs(offsetX) < radius && Math.abs(offsetZ) < radius) set(stemX + offsetX, stemY + height + 1, stemZ + offsetZ, 'red_mushroom_block')
      }
    }
  }
  mushroom(12, 7, 15, 3, 2)
  fill([2, 4, 4], [3, 4, 7], 'rooted_dirt')
  fill([2, 5, 4], [3, 5, 7], 'mycelium')
  mushroom(2, 6, 5, 2, 1)
  fill([18, 5, 21], [21, 5, 22], 'rooted_dirt')
  fill([18, 6, 21], [21, 6, 22], 'mycelium')
  mushroom(20, 7, 22, 2, 1)
  for (const [baseX, baseZ] of [[3, 17], [17, 20]]) {
    fill([baseX - 1, 1, baseZ - 1], [baseX + 2, 1, baseZ + 2], 'polished_blackstone_bricks')
    fill([baseX, 1, baseZ], [baseX + 1, 1, baseZ + 1], 'water', { level: '0' })
  }
  for (const [fungusX, fungusY, fungusZ] of [[2, 3, 11], [9, 3, 5], [9, 4, 10], [13, 7, 15], [19, 7, 21], [22, 3, 22], [2, 3, 21]]) {
    set(fungusX, fungusY - 1, fungusZ, 'mycelium')
    set(fungusX, fungusY, fungusZ, (fungusX + fungusZ) % 2 ? 'red_mushroom' : 'brown_mushroom')
  }
  for (const [crystalX, crystalZ] of [[8, 13], [2, 8], [15, 15], [22, 21]]) {
    set(crystalX, 2, crystalZ, 'amethyst_block')
    set(crystalX, 3, crystalZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [wallX, side] of [[2, 'west'], [9, 'east']]) {
    for (const vineZ of [4, 10]) {
      for (let blockY = 7; blockY <= 11; blockY++) set(wallX, blockY, vineZ, 'vine', { [side]: 'true', up: 'false' })
    }
  }
  for (const vineX of [13, 17, 22]) {
    for (let blockY = 7; blockY <= 11; blockY++) set(vineX, blockY, 15, 'vine', { north: 'true', up: 'false' })
  }
  for (const [lampX, lampZ] of [[1, 5], [10, 9], [16, 14], [22, 23]]) set(lampX, 6, lampZ, 'shroomlight')
  for (const [lichenX, lichenZ, side] of [[2, 12, 'west'], [9, 7, 'east'], [16, 15, 'north'], [21, 22, 'south']]) set(lichenX, 5, lichenZ, 'glow_lichen', { [side]: 'true', waterlogged: 'false' })
  for (let blockX = 4; blockX <= 8; blockX++) {
    for (let blockY = 2; blockY <= 7; blockY++) set(blockX, blockY, 0, blockX >= 5 && blockX <= 7 && blockY <= 5 || blockX === 6 && blockY === 6 ? 'air' : 'stone_bricks')
    set(blockX, 1, 0, 'stone_bricks')
  }
  for (let blockZ = 16; blockZ <= 20; blockZ++) {
    for (let blockY = 2; blockY <= 7; blockY++) set(24, blockY, blockZ, blockZ >= 17 && blockZ <= 19 && blockY <= 5 || blockZ === 18 && blockY === 6 ? 'air' : 'stone_bricks')
    set(24, 1, blockZ, 'stone_bricks')
  }
  return { structure, metadata: { name: 'room' } }
}
