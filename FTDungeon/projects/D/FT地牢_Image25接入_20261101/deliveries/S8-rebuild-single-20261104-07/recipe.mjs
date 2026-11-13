export default function build(api) {
  const structure = api.createStructure([25, 15, 27])
  const put = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (first, last, name, properties = {}) => api.fill(structure, first, last, `minecraft:${name}`, properties)
  fill([0, 0, 0], [24, 14, 26], 'air')
  fill([0, 0, 0], [24, 1, 26], 'deepslate_tiles')
  fill([0, 14, 0], [24, 14, 26], 'deepslate_tiles')
  fill([0, 2, 4], [24, 3, 26], 'deepslate_bricks')
  for (let blockX = 0; blockX < 25; blockX++) for (let blockZ = 4; blockZ < 27; blockZ++) {
    const ring = Math.max(Math.abs(blockX - 12), Math.abs(blockZ - 15))
    put(blockX, 3, blockZ, ring === 3 ? 'calcite' : ring <= 1 ? 'amethyst_block' : (blockX * 3 + blockZ * 5) % 29 < 3 ? 'amethyst_block' : 'polished_deepslate')
    if (blockX < 2 || blockX > 22 || blockZ < 6 || blockZ > 24) {
      for (let blockY = 4; blockY < 14; blockY++) put(blockX, blockY, blockZ, (blockY === 6 || blockY === 10) && (blockX + blockZ) % 7 < 3 ? 'calcite' : (Math.floor(blockX / 3) + Math.floor(blockZ / 3) + blockY) % 9 < 2 ? 'amethyst_block' : 'deepslate_bricks')
    }
  }
  fill([9, 2, 2], [14, 2, 2], 'deepslate_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  fill([9, 2, 3], [14, 2, 3], 'deepslate_bricks')
  fill([9, 3, 3], [14, 3, 3], 'deepslate_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  fill([10, 4, 4], [13, 8, 5], 'calcite')
  fill([11, 4, 4], [12, 6, 5], 'air')
  for (const blockX of [11, 12]) for (const half of ['lower', 'upper']) put(blockX, half === 'lower' ? 4 : 5, 4, 'dark_oak_door', { facing: 'north', half, hinge: blockX === 11 ? 'left' : 'right', open: 'true', powered: 'false' })
  for (const blockX of [8, 15]) {
    fill([blockX, 4, 3], [blockX, 8, 5], 'deepslate_bricks')
    fill([blockX, 8, 3], [blockX, 13, 3], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX, 7, 3, 'lantern', { hanging: 'true', waterlogged: 'false' })
    api.setBlock(structure, [blockX < 12 ? 7 : 16, 8, 3], 'minecraft:purple_wall_banner', { facing: 'north' }, api.createBlockNbt({ id: 'minecraft:banner', Patterns: [] }))
    put(blockX < 12 ? 7 : 16, 8, 4, 'deepslate_bricks')
  }
  for (const blockX of [1, 23]) {
    fill([blockX, 4, 19], [blockX, 10, 22], 'amethyst_block')
    fill([blockX, 5, 20], [blockX, 9, 21], 'air')
    put(blockX, 4, 20, 'calcite')
    put(blockX, 5, 20, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
    fill([blockX, 9, 21], [blockX, 10, 21], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX, 8, 21, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  fill([2, 6, 10], [2, 10, 16], 'stripped_spruce_log', { axis: 'z' })
  for (let blockY = 7; blockY <= 9; blockY++) for (let blockZ = 11; blockZ <= 15; blockZ++) put(2, blockY, blockZ, (blockY + blockZ) % 4 === 0 ? 'green_terracotta' : (blockY * 3 + blockZ) % 5 === 0 ? 'brown_terracotta' : 'birch_planks')
  for (const [blockX, blockZ] of [[3, 10], [3, 14], [4, 15], [21, 21]]) api.setBlock(structure, [blockX, 4, blockZ], 'minecraft:barrel', { facing: 'up', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel', Items: [] }))
  fill([20, 4, 16], [22, 4, 18], 'deepslate_bricks')
  put(21, 5, 17, 'polished_deepslate')
  put(21, 6, 17, 'amethyst_block')
  put(21, 7, 17, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  put(20, 5, 17, 'soul_lantern', { hanging: 'false', waterlogged: 'false' })
  for (const [blockX, blockZ, height] of [[3, 7, 6], [21, 7, 7], [3, 23, 6], [21, 24, 6], [6, 24, 5], [18, 24, 5]]) {
    put(blockX, 4, blockZ, 'calcite')
    fill([blockX, 5, blockZ], [blockX, height, blockZ], 'amethyst_block')
    put(blockX, height + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const blockX of [8, 16]) {
    fill([blockX, 10, 23], [blockX, 13, 23], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX, 9, 23, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (let offset = -4; offset <= 4; offset++) for (let blockY = 4; blockY <= 12; blockY++) {
    const distance = Math.abs(offset)
    const top = 11 - Math.max(0, distance - 1)
    const open = distance <= 2 && blockY < top
    for (const blockZ of [25, 26]) put(12 + offset, blockY, blockZ, open ? 'air' : distance === 3 || blockY === top ? 'calcite' : 'deepslate_bricks')
  }
  return { structure, metadata: { name: 'room' } }
}
