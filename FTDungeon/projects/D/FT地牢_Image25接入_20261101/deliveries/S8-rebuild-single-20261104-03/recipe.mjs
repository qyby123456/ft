export default function build(api) {
  const structure = api.createStructure([17, 12, 17])
  const put = (blockX, blockY, blockZ, name, properties) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (first, last, name, properties) => api.fill(structure, first, last, `minecraft:${name}`, properties)
  fill([0, 0, 0], [16, 11, 16], 'air')
  fill([0, 0, 0], [16, 0, 16], 'deepslate_bricks')
  for (let blockX = 0; blockX < 17; blockX++) for (let blockZ = 0; blockZ < 17; blockZ++) {
    const vein = (blockX * 7 + blockZ * 11 + Math.floor(blockX / 3)) % 19
    const brokenBand = blockZ === 5 && blockX >= 4 && blockX <= 11 || blockX === 12 && blockZ >= 6 && blockZ <= 12 || blockZ === 13 && blockX >= 5 && blockX <= 11
    put(blockX, 1, blockZ, brokenBand ? (blockX + blockZ) % 3 ? 'amethyst_block' : 'calcite' : vein < 2 ? 'amethyst_block' : vein < 5 ? 'calcite' : vein < 9 ? 'polished_deepslate' : 'deepslate_tiles')
    put(blockX, 11, blockZ, 'deepslate_tiles')
    if (blockX === 0 || blockX === 16 || blockZ === 0 || blockZ === 16) for (let blockY = 2; blockY < 11; blockY++) {
      const purpleVein = (blockX * 3 + blockZ * 5) % 9 < 2
      put(blockX, blockY, blockZ, purpleVein && blockY % 4 !== 0 ? 'amethyst_block' : (blockY === 4 || blockY === 8) && (blockX + blockZ) % 8 < 4 ? 'calcite' : 'deepslate_bricks')
    }
  }
  for (const [first, last] of [[[0, 2, 0], [2, 10, 2]], [[14, 2, 0], [16, 10, 2]], [[0, 2, 14], [2, 10, 16]], [[14, 2, 14], [16, 10, 16]], [[7, 2, 15], [9, 10, 16]]]) {
    fill(first, last, 'deepslate_bricks')
    for (const level of [4, 8]) fill([first[0], level, first[2]], [last[0], level, last[2]], 'calcite')
    fill([first[0], 5, first[2]], [last[0], 5, last[2]], 'amethyst_block')
  }
  fill([1, 10, 1], [15, 10, 1], 'deepslate_bricks')
  fill([1, 10, 15], [15, 10, 15], 'deepslate_bricks')
  fill([1, 10, 1], [1, 10, 15], 'deepslate_bricks')
  fill([15, 10, 1], [15, 10, 15], 'deepslate_bricks')
  for (const [first, last] of [[[0, 2, 4], [1, 8, 7]], [[0, 2, 9], [1, 8, 12]], [[3, 2, 15], [6, 8, 16]], [[10, 2, 15], [13, 8, 16]]]) {
    fill(first, last, 'amethyst_block')
    for (const level of [3, 4, 6, 7]) {
      if (first[0] === 0) fill([1, level, first[2]], [1, level, last[2]], 'air')
      else fill([first[0], level, 15], [last[0], level, 15], 'air')
    }
    for (const level of [2, 5]) fill([first[0], level, first[2]], [last[0], level, last[2]], 'deepslate_tile_slab', { type: 'top', waterlogged: 'false' })
  }
  const cluster = (blockX, supportY, blockZ) => {
    put(blockX, supportY, blockZ, 'amethyst_block')
    put(blockX, supportY + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[1, 5], [1, 6], [1, 10], [1, 11], [4, 15], [5, 15], [11, 15], [12, 15]]) {
    cluster(blockX, 2, blockZ)
    cluster(blockX, 5, blockZ)
  }
  fill([6, 2, 8], [10, 2, 10], 'polished_deepslate')
  put(6, 2, 8, 'amethyst_block')
  put(10, 2, 10, 'amethyst_block')
  put(6, 3, 8, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  put(10, 3, 10, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  api.setBlock(structure, [8, 3, 9], 'minecraft:chest', { facing: 'north', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest', Items: [] }))
  for (const [blockX, blockZ, high] of [[3, 3, 4], [13, 3, 3], [13, 13, 4]]) {
    put(blockX, 2, blockZ, 'calcite')
    fill([blockX, 3, blockZ], [blockX, high, blockZ], 'amethyst_block')
    put(blockX, high + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[4, 13], [12, 13]]) {
    fill([blockX, 8, blockZ], [blockX, 10, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX, 7, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[1, 8], [8, 15], [15, 12]]) {
    fill([blockX, 8, blockZ], [blockX, 10, blockZ], 'amethyst_block')
    put(blockX, 7, blockZ, 'amethyst_cluster', { facing: 'down', waterlogged: 'false' })
  }
  for (let offset = -3; offset <= 3; offset++) for (let blockY = 2; blockY <= 7; blockY++) {
    const opening = Math.abs(offset) <= 1 && blockY <= 5 || offset === 0 && blockY === 6
    for (const blockZ of [0, 1]) put(8 + offset, blockY, blockZ, opening ? 'air' : Math.abs(offset) <= 2 ? 'amethyst_block' : 'deepslate_bricks')
  }
  fill([15, 2, 5], [16, 6, 9], 'amethyst_block')
  fill([15, 2, 6], [16, 4, 8], 'air')
  return { structure, metadata: { name: 'room' } }
}
