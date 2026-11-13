export default function build(api) {
  const structure = api.createStructure([33, 23, 33])
  const put = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (first, last, name, properties = {}) => api.fill(structure, first, last, `minecraft:${name}`, properties)
  fill([0, 0, 0], [32, 22, 32], 'air')
  fill([0, 0, 0], [32, 2, 32], 'deepslate_tiles')
  for (let blockX = 0; blockX < 33; blockX++) for (let blockZ = 0; blockZ < 33; blockZ++) {
    const vein = (blockX * 5 + blockZ * 3 + Math.floor(blockX / 4)) % 31
    put(blockX, 2, blockZ, vein < 4 ? 'amethyst_block' : vein === 7 ? 'calcite' : 'deepslate_bricks')
    if (blockX < 2 || blockX > 30 || blockZ < 2 || blockZ > 30) {
      for (let blockY = 3; blockY <= 22; blockY++) put(blockX, blockY, blockZ, (blockY === 7 || blockY === 12 || blockY === 19) && (blockX + blockZ) % 9 < 5 ? 'calcite' : (Math.floor(blockX / 4) + Math.floor(blockZ / 4) + Math.floor(blockY / 3)) % 7 < 2 ? 'amethyst_block' : 'deepslate_bricks')
    }
    if (!(blockX >= 11 && blockX <= 21 && blockZ >= 11 && blockZ <= 21)) put(blockX, 22, blockZ, blockX === 10 || blockX === 22 || blockZ === 10 || blockZ === 22 ? 'amethyst_block' : 'deepslate_tiles')
    if (blockX >= 2 && blockX <= 30 && blockZ >= 2 && blockZ <= 30 && (blockX <= 7 || blockX >= 25 || blockZ <= 7 || blockZ >= 25)) put(blockX, 12, blockZ, 'deepslate_bricks')
  }
  for (const blockX of [4, 28]) for (const blockZ of [4, 28]) {
    fill([blockX - 1, 3, blockZ - 1], [blockX + 1, 21, blockZ + 1], 'deepslate_bricks')
    for (const level of [7, 12, 19]) fill([blockX - 1, level, blockZ - 1], [blockX + 1, level, blockZ + 1], 'calcite')
    fill([blockX, 14, blockZ - 1], [blockX, 17, blockZ - 1], 'amethyst_block')
  }
  for (let blockX = 7; blockX <= 25; blockX++) for (let blockZ = 7; blockZ <= 25; blockZ++) {
    if (blockX !== 7 && blockX !== 25 && blockZ !== 7 && blockZ !== 25) continue
    const connects = (neighborX, neighborZ) => neighborX >= 7 && neighborX <= 25 && neighborZ >= 7 && neighborZ <= 25 && (neighborX === 7 || neighborX === 25 || neighborZ === 7 || neighborZ === 25)
    put(blockX, 13, blockZ, 'deepslate_brick_wall', { waterlogged: 'false', up: 'true', north: connects(blockX, blockZ - 1) ? 'low' : 'none', south: connects(blockX, blockZ + 1) ? 'low' : 'none', east: connects(blockX + 1, blockZ) ? 'low' : 'none', west: connects(blockX - 1, blockZ) ? 'low' : 'none' })
  }
  for (let step = 0; step < 10; step++) {
    const blockZ = 8 + step
    fill([3, 3, blockZ], [5, 12, blockZ], 'air')
    if (step > 0) fill([3, 3, blockZ], [5, 2 + step, blockZ], 'deepslate_bricks')
    fill([3, 3 + step, blockZ], [5, 3 + step, blockZ], 'deepslate_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  }
  fill([11, 3, 11], [21, 3, 21], 'deepslate_bricks')
  fill([12, 4, 12], [20, 4, 20], 'deepslate_bricks')
  fill([13, 5, 13], [19, 5, 19], 'amethyst_block')
  for (const [blockX, blockZ] of [[13, 13], [13, 19], [19, 13], [19, 19]]) put(blockX, 5, blockZ, 'calcite')
  for (const [blockX, blockZ, peak] of [[16, 16, 15], [14, 16, 12], [18, 16, 10], [16, 18, 9], [16, 14, 11]]) {
    fill([blockX, 6, blockZ], [blockX, peak, blockZ], 'amethyst_block')
    put(blockX, peak + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[12, 14], [12, 18], [20, 14], [20, 18], [14, 12], [18, 20]]) {
    put(blockX, 5, blockZ, 'amethyst_block')
    put(blockX, 6, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[9, 3], [29, 7], [29, 24], [9, 29], [24, 29]]) {
    fill([blockX - 1, 3, blockZ - 1], [blockX + 1, 3, blockZ + 1], 'deepslate_bricks')
    fill([blockX, 4, blockZ], [blockX, 6, blockZ], 'amethyst_block')
    put(blockX, 7, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[6, 6], [26, 6], [26, 26], [6, 26]]) {
    put(blockX, 13, blockZ, 'calcite')
    fill([blockX, 14, blockZ], [blockX, 16, blockZ], 'amethyst_block')
    put(blockX, 17, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[9, 6], [23, 6], [9, 26], [23, 26]]) {
    fill([blockX, 18, blockZ], [blockX, 21, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX, 17, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
    fill([blockX, 9, blockZ], [blockX, 11, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX, 8, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[10, 10], [22, 10], [10, 22], [22, 22]]) {
    fill([blockX, 19, blockZ], [blockX, 21, blockZ], 'amethyst_block')
    put(blockX, 18, blockZ, 'amethyst_cluster', { facing: 'down', waterlogged: 'false' })
  }
  for (const blockZ of [10, 20, 27]) for (let blockY = 16; blockY <= 21; blockY++) put(30, blockY, blockZ, 'vine', { east: 'true', north: 'false', south: 'false', west: 'false', up: 'false' })
  for (const blockX of [10, 20]) for (let blockY = 17; blockY <= 21; blockY++) put(blockX, blockY, 2, 'vine', { north: 'true', east: 'false', south: 'false', west: 'false', up: 'false' })
  fill([4, 1, 25], [8, 3, 29], 'deepslate_bricks')
  fill([5, 2, 26], [7, 2, 28], 'lava', { level: '0' })
  fill([5, 3, 26], [7, 3, 28], 'glass')
  const arch = (direction) => {
    for (let offset = -4; offset <= 4; offset++) for (let blockY = 3; blockY <= 11; blockY++) {
      const distance = Math.abs(offset)
      const top = 10 - Math.max(0, distance - 1)
      for (const depth of [0, 1]) put(direction === 'north' ? 16 + offset : 32 - depth, blockY, direction === 'north' ? depth : 16 + offset, distance <= 2 && blockY < top ? 'air' : distance === 3 || blockY === top ? 'calcite' : 'deepslate_bricks')
    }
  }
  arch('north')
  arch('east')
  return { structure, metadata: { name: 'room' } }
}
