export default function build(api) {
  const structure = api.createStructure([25, 15, 25])
  const put = (blockX, blockY, blockZ, name, properties) => api.setBlock(structure, [24 - blockX, blockY, blockZ], `minecraft:${name}`, properties?.hinge ? { ...properties, hinge: properties.hinge === 'left' ? 'right' : 'left' } : properties)
  const fill = (first, last, name, properties) => api.fill(structure, [24 - last[0], first[1], first[2]], [24 - first[0], last[1], last[2]], `minecraft:${name}`, properties)
  fill([0, 0, 0], [24, 14, 24], 'air')
  fill([0, 0, 0], [24, 0, 24], 'deepslate_bricks')
  for (let blockX = 0; blockX < 25; blockX++) for (let blockZ = 0; blockZ < 25; blockZ++) {
    const distance = Math.abs(blockX - 12) + Math.abs(blockZ - 12)
    const diamond = distance >= 6 && distance <= 7
    const vein = (blockX * 11 + blockZ * 7 + Math.floor(blockZ / 3)) % 31
    put(blockX, 1, blockZ, diamond ? ((blockX + blockZ) % 3 ? 'amethyst_block' : 'calcite') : vein < 3 ? 'amethyst_block' : vein < 6 ? 'polished_deepslate' : 'deepslate_tiles')
    put(blockX, 14, blockZ, 'deepslate_tiles')
    if (blockX === 0 || blockX === 24 || blockZ === 0 || blockZ === 24) for (let blockY = 2; blockY < 14; blockY++) {
      const veinColumn = (blockX * 3 + blockZ * 5) % 11 <= 1
      put(blockX, blockY, blockZ, veinColumn && blockY % 5 !== 0 ? 'amethyst_block' : (blockY === 6 || blockY === 10) && (blockX + blockZ) % 7 < 3 ? 'calcite' : 'deepslate_bricks')
    }
  }
  for (const [first, last] of [[[0, 2, 4], [2, 13, 6]], [[0, 2, 18], [2, 13, 20]], [[22, 2, 4], [24, 13, 6]], [[22, 2, 18], [24, 13, 20]], [[5, 2, 22], [7, 13, 24]], [[14, 2, 22], [16, 13, 24]]]) {
    fill(first, last, 'deepslate_bricks')
    for (const level of [6, 10]) fill([first[0], level, first[2]], [last[0], level, last[2]], 'calcite')
    for (const level of [5, 9]) fill([first[0], level, first[2]], [last[0], level, last[2]], 'amethyst_block')
  }
  fill([1, 13, 1], [23, 13, 2], 'deepslate_bricks')
  fill([1, 13, 22], [23, 13, 23], 'deepslate_bricks')
  fill([1, 13, 1], [2, 13, 23], 'deepslate_bricks')
  fill([22, 13, 1], [23, 13, 23], 'deepslate_bricks')
  fill([9, 2, 10], [15, 2, 14], 'polished_deepslate')
  fill([10, 3, 11], [14, 3, 13], 'calcite')
  fill([11, 3, 11], [13, 3, 13], 'amethyst_block')
  const crystal = (blockX, blockZ, bottom, top) => {
    fill([blockX, bottom, blockZ], [blockX, top, blockZ], 'amethyst_block')
    put(blockX, top + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  crystal(12, 12, 4, 6)
  crystal(11, 12, 4, 4)
  crystal(13, 12, 4, 4)
  for (const [center, blockZ] of [[12, 0], [10, 24]]) {
    for (let offset = -4; offset <= 4; offset++) for (let blockY = 2; blockY <= 9; blockY++) {
      const open = Math.abs(offset) <= 2 && blockY <= 6 || Math.abs(offset) <= 1 && blockY === 7
      const rim = Math.abs(offset) <= 3 && blockY <= 7 || Math.abs(offset) <= 2 && blockY === 8
      put(center + offset, blockY, blockZ, open ? 'air' : rim ? 'amethyst_block' : 'deepslate_bricks')
    }
  }
  fill([17, 2, 23], [22, 7, 24], 'deepslate_bricks')
  fill([18, 2, 22], [21, 6, 22], 'calcite')
  fill([19, 2, 22], [20, 5, 23], 'air')
  fill([19, 4, 22], [20, 5, 22], 'tinted_glass')
  for (const [blockX, hinge] of [[19, 'left'], [20, 'right']]) {
    for (const [blockY, half] of [[2, 'lower'], [3, 'upper']]) put(blockX, blockY, 22, 'dark_oak_door', { facing: 'north', half, hinge, open: 'false', powered: 'false' })
  }
  fill([2, 2, 22], [4, 3, 22], 'bookshelf')
  fill([2, 4, 22], [4, 4, 22], 'dark_oak_planks')
  put(3, 5, 22, 'amethyst_block')
  put(3, 6, 22, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  put(17, 2, 21, 'chest', { facing: 'north', type: 'single', waterlogged: 'false' })
  for (const [blockX, blockZ, peak] of [[2, 2, 5], [22, 2, 6], [2, 14, 5], [22, 14, 4], [4, 10, 4], [20, 9, 4]]) {
    fill([blockX - 1, 2, blockZ - 1], [blockX + 1, 2, blockZ + 1], 'calcite')
    crystal(blockX, blockZ, 3, peak)
    crystal(blockX + 1, blockZ, 3, peak - 1)
  }
  for (const [blockX, blockZ, length] of [[5, 22, 3], [16, 22, 4], [2, 10, 3], [22, 10, 4], [7, 2, 2], [18, 2, 3]]) {
    fill([blockX, 12, blockZ], [blockX, 13, blockZ], 'amethyst_block')
    fill([blockX, 12 - length, blockZ], [blockX, 11, blockZ], 'amethyst_block')
    put(blockX, 11 - length, blockZ, 'amethyst_cluster', { facing: 'down', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[4, 7], [20, 7], [9, 21], [18, 20], [6, 16], [21, 16]]) {
    fill([blockX, 10, blockZ], [blockX, 13, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX, 9, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  return { structure, metadata: { name: 'room' } }
}
