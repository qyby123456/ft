export default function build(api) {
  const structure = api.createStructure([27, 13, 27])
  const put = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (first, last, name, properties = {}) => api.fill(structure, first, last, `minecraft:${name}`, properties)
  fill([0, 0, 0], [26, 12, 26], 'deepslate_bricks')
  fill([0, 0, 0], [26, 0, 26], 'deepslate_tiles')
  fill([0, 12, 0], [26, 12, 26], 'deepslate_tiles')
  fill([2, 2, 2], [10, 10, 24], 'air')
  fill([2, 2, 16], [24, 10, 24], 'air')
  const passage = (blockX, blockZ) => blockX >= 2 && blockX <= 10 && blockZ >= 2 && blockZ <= 24 || blockX >= 2 && blockX <= 24 && blockZ >= 16 && blockZ <= 24
  for (let blockX = 0; blockX < 27; blockX++) for (let blockZ = 0; blockZ < 27; blockZ++) {
    if (passage(blockX, blockZ)) {
      const vein = (blockX * 7 + blockZ * 3 + Math.floor(blockZ / 5)) % 31
      put(blockX, 1, blockZ, vein < 4 ? 'amethyst_block' : vein === 7 || vein === 8 ? 'calcite' : (blockX + blockZ) % 4 === 0 ? 'polished_deepslate' : 'deepslate_bricks')
      put(blockX, 11, blockZ, (blockX * 3 + blockZ * 7) % 19 < 3 ? 'amethyst_block' : 'deepslate_tiles')
    }
    const edge = !passage(blockX, blockZ) && [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([deltaX, deltaZ]) => passage(blockX + deltaX, blockZ + deltaZ))
    if (edge || blockX === 0 || blockX === 26 || blockZ === 0 || blockZ === 26) {
      for (let blockY = 2; blockY <= 11; blockY++) {
        const purple = (Math.floor(blockX / 3) + Math.floor(blockZ / 3) + Math.floor(blockY / 2)) % 9 < 2
        const pale = (blockY === 4 || blockY === 8) && (blockX + blockZ) % 9 < 4
        put(blockX, blockY, blockZ, pale ? 'calcite' : purple ? 'amethyst_block' : (blockY + blockZ) % 5 === 0 ? 'cobbled_deepslate' : 'deepslate_bricks')
      }
    }
  }
  for (const blockZ of [5, 11]) {
    for (const blockX of [2, 10]) {
      fill([blockX, 2, blockZ - 1], [blockX, 10, blockZ + 1], 'deepslate_bricks')
      for (const level of [4, 8]) fill([blockX, level, blockZ - 1], [blockX, level, blockZ + 1], 'calcite')
      fill([blockX, 5, blockZ - 1], [blockX, 6, blockZ + 1], 'amethyst_block')
    }
    fill([3, 10, blockZ], [9, 10, blockZ], 'deepslate_bricks')
    fill([3, 8, blockZ], [3, 10, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put(3, 7, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const blockX of [16, 22]) {
    for (const blockZ of [16, 24]) {
      fill([blockX - 1, 2, blockZ], [blockX + 1, 10, blockZ], 'deepslate_bricks')
      for (const level of [4, 8]) fill([blockX - 1, level, blockZ], [blockX + 1, level, blockZ], 'calcite')
      fill([blockX - 1, 5, blockZ], [blockX + 1, 6, blockZ], 'amethyst_block')
    }
    fill([blockX, 10, 17], [blockX, 10, 23], 'deepslate_bricks')
    fill([blockX, 8, 23], [blockX, 10, 23], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX, 7, 23, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  fill([11, 2, 12], [13, 10, 15], 'amethyst_block')
  for (const level of [4, 8]) {
    fill([11, level, 12], [13, level, 15], 'calcite')
  }
  fill([11, 4, 13], [11, 7, 15], 'air')
  fill([11, 4, 15], [13, 7, 15], 'air')
  fill([11, 6, 15], [11, 7, 15], 'chain', { axis: 'y', waterlogged: 'false' })
  put(11, 5, 15, 'lantern', { hanging: 'true', waterlogged: 'false' })
  for (const [blockX, blockZ, height] of [[2, 3, 3], [10, 3, 4], [2, 8, 4], [10, 8, 3], [2, 15, 3], [2, 22, 4], [6, 24, 3], [11, 16, 4], [14, 24, 3], [19, 16, 4], [20, 24, 3], [24, 17, 4]]) {
    put(blockX, 2, blockZ, 'calcite')
    fill([blockX, 3, blockZ], [blockX, height, blockZ], 'amethyst_block')
    put(blockX, height + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[3, 8], [9, 8], [3, 18], [9, 23], [14, 17], [19, 23], [23, 17]]) {
    fill([blockX, 9, blockZ], [blockX, 11, blockZ], 'amethyst_block')
    put(blockX, 8, blockZ, 'amethyst_cluster', { facing: 'down', waterlogged: 'false' })
  }
  const arch = (direction) => {
    for (let offset = -4; offset <= 4; offset++) for (let blockY = 2; blockY <= 9; blockY++) {
      const distance = Math.abs(offset)
      const top = 8 - Math.max(0, distance - 1)
      const open = distance <= 2 && blockY < top
      const pale = distance === 3 && blockY <= 5 || distance <= 3 && blockY === top
      for (const depth of [0, 1]) {
        const blockX = direction === 'north' ? 6 + offset : 26 - depth
        const blockZ = direction === 'north' ? depth : 20 + offset
        put(blockX, blockY, blockZ, open ? 'air' : pale ? 'calcite' : 'deepslate_bricks')
        put(blockX, 1, blockZ, 'deepslate_tiles')
      }
    }
  }
  arch('north')
  arch('east')
  return { structure, metadata: { name: 'room' } }
}
