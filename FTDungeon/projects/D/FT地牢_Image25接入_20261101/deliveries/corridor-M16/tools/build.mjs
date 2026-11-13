export default function build(api) {
  const structure = api.createStructure([15, 11, 40])
  const put = (position, block, properties) => api.setBlock(structure, position, `minecraft:${block}`, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, `minecraft:${block}`, properties)
  const masonry = (blockX, blockY, blockZ) => {
    const value = (blockX * 17 + blockY * 11 + blockZ * 7) % 23
    return value < 5 ? 'mossy_stone_bricks' : value === 7 ? 'chiseled_stone_bricks' : 'stone_bricks'
  }
  fill([0, 0, 0], [14, 10, 39], 'air')
  fill([0, 0, 0], [14, 0, 39], 'stone_bricks')
  for (let blockX = 0; blockX < 15; blockX++) {
    for (let blockZ = 0; blockZ < 40; blockZ++) {
      put([blockX, 10, blockZ], blockX === 0 || blockX === 14 || blockZ === 0 || blockZ === 39 ? 'polished_deepslate' : 'stone_bricks')
      put([blockX, 1, blockZ], 'dark_prismarine')
    }
  }
  for (const fromX of [0, 13]) {
    for (let blockX = fromX; blockX <= fromX + 1; blockX++) {
      for (let blockY = 1; blockY <= 9; blockY++) {
        for (let blockZ = 0; blockZ < 40; blockZ++) {
          put([blockX, blockY, blockZ], blockY === 1 || blockY === 8 ? 'polished_deepslate' : masonry(blockX, blockY, blockZ))
        }
      }
    }
  }
  for (let blockZ = 0; blockZ < 40; blockZ++) {
    for (let blockX = 5; blockX <= 9; blockX++) {
      put([blockX, 1, blockZ], blockX === 5 || blockX === 9 ? 'stone_bricks' : blockZ % 7 === 0 && blockX === 7 ? 'chiseled_stone_bricks' : 'polished_andesite')
    }
    if (blockZ >= 2 && blockZ <= 37) {
      for (const blockX of [2, 3, 4, 10, 11, 12]) put([blockX, 1, blockZ], 'water', { level: '0' })
    }
  }
  for (const blockZ of [0, 1, 38, 39]) fill([2, 1, blockZ], [12, 1, blockZ], 'stone_bricks')
  for (const blockZ of [0, 39]) {
    fill([2, 2, blockZ], [12, 9, blockZ], 'stone_bricks')
    fill([5, 2, blockZ], [9, 5, blockZ], 'air')
    fill([6, 6, blockZ], [8, 6, blockZ], 'air')
    for (const blockX of [4, 10]) fill([blockX, 2, blockZ], [blockX, 5, blockZ], 'chiseled_stone_bricks')
    fill([5, 7, blockZ], [9, 7, blockZ], 'chiseled_stone_bricks')
  }
  const ribs = [2, 9, 16, 23, 30, 37]
  for (const blockZ of ribs) {
    fill([1, 9, blockZ], [13, 9, blockZ], 'spruce_log', { axis: 'x' })
    for (const blockX of [2, 12]) {
      fill([blockX, 1, blockZ], [blockX, 7, blockZ], 'stone_bricks')
      put([blockX, 2, blockZ], 'chiseled_stone_bricks')
      fill([blockX === 2 ? 2 : 11, 8, blockZ], [blockX === 2 ? 3 : 12, 8, blockZ], 'stone_bricks')
    }
    for (const blockX of [3, 11]) {
      fill([blockX, 6, blockZ], [blockX, 8, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
      put([blockX, 5, blockZ], 'lantern', { hanging: 'true', waterlogged: 'false' })
    }
  }
  for (const blockX of [3, 11]) {
    for (let blockZ = 1; blockZ <= 38; blockZ++) {
      if (!ribs.includes(blockZ)) put([blockX, 9, blockZ], 'spruce_log', { axis: 'z' })
    }
  }
  for (const startZ of [5, 19, 33]) {
    for (const startX of [3, 10]) {
      fill([startX, 5, startZ], [startX + 1, 5, startZ + 1], 'smooth_stone_slab', { type: 'top', waterlogged: 'false' })
      fill([startX, 6, startZ], [startX + 1, 7, startZ + 1], 'iron_bars', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
      const anchorX = startX === 3 ? 3 : 11
      put([anchorX, 8, startZ], 'chain', { axis: 'y', waterlogged: 'false' })
    }
  }
  for (const blockZ of [5, 26]) {
    for (const blockX of [2, 12]) {
      for (const borderZ of [blockZ - 1, blockZ + 1]) fill([blockX, 1, borderZ], [blockX, 5, borderZ], 'mossy_stone_bricks')
      fill([blockX, 2, blockZ], [blockX, 4, blockZ], 'water', { level: '0' })
      fill([blockX === 2 ? 3 : 11, 2, blockZ], [blockX === 2 ? 3 : 11, 4, blockZ], 'cyan_stained_glass')
      put([blockX, 5, blockZ], 'chiseled_stone_bricks')
    }
  }
  for (let blockZ = 3; blockZ <= 36; blockZ++) {
    if (ribs.includes(blockZ) || [4, 5, 6, 25, 26, 27].includes(blockZ) || blockZ % 4 !== 0) continue
    for (const blockX of [2, 12]) {
      for (let blockY = 4; blockY <= 7; blockY++) {
        put([blockX, blockY, blockZ], 'vine', { north: 'false', east: String(blockX === 12), south: 'false', west: String(blockX === 2), up: 'false' })
      }
    }
  }
  return { structure, metadata: { name: 'room', id: 'corridor-M16', theme: 'intact flooded corridor', lengthChunks: 2.5 } }
}
