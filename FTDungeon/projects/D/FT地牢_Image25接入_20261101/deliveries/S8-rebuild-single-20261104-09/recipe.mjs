export default function build(api) {
  const structure = api.createStructure([29, 17, 27])
  const put = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (first, last, name, properties = {}) => api.fill(structure, first, last, `minecraft:${name}`, properties)
  fill([0, 0, 0], [28, 16, 26], 'air')
  fill([0, 0, 0], [28, 1, 26], 'deepslate_tiles')
  fill([0, 16, 0], [28, 16, 26], 'deepslate_tiles')
  for (let blockX = 0; blockX < 29; blockX++) for (let blockZ = 0; blockZ < 27; blockZ++) {
    const distance = Math.abs(blockX - 14) + Math.abs(blockZ - 8)
    put(blockX, 1, blockZ, distance <= 3 ? distance % 2 === 0 ? 'amethyst_block' : 'calcite' : (blockX * 3 + blockZ * 7) % 31 < 3 ? 'amethyst_block' : 'deepslate_bricks')
    if (blockX < 2 || blockX > 26 || blockZ < 2 || blockZ > 24) for (let blockY = 2; blockY < 16; blockY++) put(blockX, blockY, blockZ, (blockY === 5 || blockY === 12) && (blockX + blockZ) % 8 < 4 ? 'calcite' : (Math.floor(blockX / 3) + Math.floor(blockZ / 3) + Math.floor(blockY / 3)) % 9 < 2 ? 'amethyst_block' : 'deepslate_bricks')
  }
  fill([8, 2, 14], [20, 2, 23], 'deepslate_bricks')
  fill([9, 3, 15], [19, 3, 23], 'deepslate_bricks')
  fill([10, 4, 16], [18, 4, 23], 'smooth_quartz')
  for (let step = 0; step < 3; step++) fill([12, 2 + step, 14 + step], [16, 2 + step, 14 + step], 'deepslate_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  api.setBlock(structure, [14, 5, 20], 'minecraft:chest', { facing: 'north', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest', Items: [] }))
  for (const blockX of [12, 16]) put(blockX, 5, 20, 'lantern', { hanging: 'false', waterlogged: 'false' })
  for (const blockX of [8, 20]) {
    fill([blockX - 1, 2, 20], [blockX + 1, 3, 22], 'deepslate_bricks')
    fill([blockX, 4, 21], [blockX, 9, 21], 'smooth_quartz')
    fill([blockX - 1, 8, 21], [blockX + 1, 8, 21], 'smooth_quartz')
    put(blockX, 10, 21, 'chiseled_stone_bricks')
    put(blockX, 7, 20, 'amethyst_block')
  }
  for (const blockX of [5, 23]) {
    fill([blockX - 1, 2, 20], [blockX + 1, 2, 23], 'spruce_planks')
    fill([blockX - 1, 5, 21], [blockX + 1, 5, 23], 'spruce_planks')
    fill([blockX - 1, 3, 23], [blockX - 1, 4, 23], 'spruce_log', { axis: 'y' })
    fill([blockX + 1, 3, 23], [blockX + 1, 4, 23], 'spruce_log', { axis: 'y' })
    for (const blockZ of [20, 22]) api.setBlock(structure, [blockX, 3, blockZ], 'minecraft:barrel', { facing: 'up', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel', Items: [] }))
    put(blockX, 6, 22, 'flower_pot')
  }
  for (const blockX of [6, 14, 22]) {
    fill([blockX, 8, 24], [blockX, 13, 24], 'amethyst_block')
    api.setBlock(structure, [blockX, 11, 23], 'minecraft:purple_wall_banner', { facing: 'north' }, api.createBlockNbt({ id: 'minecraft:banner', Patterns: [] }))
  }
  for (const [blockX, blockZ, peak] of [[3, 4, 5], [25, 4, 6], [3, 15, 5], [25, 15, 5], [10, 23, 7], [18, 23, 7]]) {
    put(blockX, 2, blockZ, 'calcite')
    fill([blockX, 3, blockZ], [blockX, peak, blockZ], 'amethyst_block')
    put(blockX, peak + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[8, 4], [20, 4], [8, 12], [20, 12]]) {
    fill([blockX, 2, blockZ], [blockX, 3, blockZ], 'deepslate_bricks')
    put(blockX, 4, blockZ, 'amethyst_block')
    put(blockX, 5, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[4, 10], [24, 10], [10, 23], [18, 23]]) {
    fill([blockX, 11, blockZ], [blockX, 15, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX, 10, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const side of [0, 27]) for (let offset = -4; offset <= 4; offset++) for (let blockY = 2; blockY <= 10; blockY++) {
    const distance = Math.abs(offset)
    const top = 9 - Math.max(0, distance - 1)
    for (const blockX of [side, side + 1]) put(blockX, blockY, 10 + offset, distance <= 2 && blockY < top ? 'air' : distance === 3 || blockY === top ? 'calcite' : 'deepslate_bricks')
  }
  return { structure, metadata: { name: 'room' } }
}
