export default function build(api) {
  const structure = api.createStructure([15, 12, 43])
  const put = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (first, last, name, properties = {}) => api.fill(structure, first, last, `minecraft:${name}`, properties)
  fill([0, 0, 0], [14, 11, 42], 'air')
  fill([0, 0, 0], [14, 1, 42], 'deepslate_tiles')
  fill([0, 11, 0], [14, 11, 42], 'deepslate_tiles')
  for (let blockZ = 0; blockZ < 43; blockZ++) {
    for (let blockX = 0; blockX < 15; blockX++) {
      const vein = (blockZ * 3 + blockX * 5) % 29
      put(blockX, 1, blockZ, vein < 3 ? 'amethyst_block' : vein === 4 || vein === 5 ? 'calcite' : (blockX + blockZ) % 4 === 0 ? 'polished_deepslate' : 'deepslate_bricks')
      if (blockX >= 2 && blockX <= 12) put(blockX, 10, blockZ, (blockX * 7 + blockZ * 3) % 23 < 4 ? 'amethyst_block' : 'deepslate_tiles')
      if (blockX < 2 || blockX > 12 || blockZ < 2 || blockZ > 40) {
        for (let blockY = 2; blockY < 11; blockY++) {
          const purple = (Math.floor(blockZ / 3) + Math.floor(blockY / 2) + blockX) % 8 < 2
          const pale = (blockY === 3 || blockY === 7) && (blockZ + blockX * 2) % 11 < 4
          put(blockX, blockY, blockZ, pale ? 'calcite' : purple ? 'amethyst_block' : (blockZ + blockY) % 5 === 0 ? 'cobbled_deepslate' : 'deepslate_bricks')
        }
      }
    }
  }
  for (const blockZ of [5, 11, 17, 23, 29, 35]) {
    for (const blockX of [2, 12]) {
      fill([blockX, 2, blockZ - 1], [blockX, 9, blockZ + 1], 'deepslate_bricks')
      for (const level of [3, 7]) fill([blockX, level, blockZ - 1], [blockX, level + 1, blockZ + 1], 'calcite')
      fill([blockX, 5, blockZ - 1], [blockX, 6, blockZ + 1], 'amethyst_block')
      fill([blockX, 9, blockZ - 1], [blockX < 7 ? 3 : 11, 9, blockZ + 1], 'deepslate_bricks')
      const lightX = blockX < 7 ? 3 : 11
      fill([lightX, 7, blockZ], [lightX, 9, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
      put(lightX, 6, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
    }
    fill([3, 9, blockZ], [11, 9, blockZ], 'deepslate_bricks')
  }
  for (const [blockX, blockZ, height] of [[2, 3, 4], [12, 3, 3], [2, 8, 3], [12, 8, 4], [2, 14, 4], [12, 14, 3], [2, 20, 3], [12, 20, 4], [2, 26, 4], [12, 26, 3], [2, 32, 3], [12, 32, 4], [2, 38, 4], [12, 38, 3]]) {
    put(blockX, 2, blockZ, 'calcite')
    fill([blockX, 3, blockZ], [blockX, height, blockZ], 'amethyst_block')
    put(blockX, height + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
    const inwardX = blockX < 7 ? blockX + 1 : blockX - 1
    put(inwardX, 1, blockZ + 1, 'amethyst_block')
    put(inwardX, 2, blockZ + 1, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ, lowest] of [[3, 8, 7], [11, 8, 6], [3, 20, 6], [11, 20, 7], [3, 32, 7], [11, 32, 6]]) {
    fill([blockX, lowest + 1, blockZ], [blockX, 10, blockZ], 'amethyst_block')
    put(blockX, lowest, blockZ, 'amethyst_cluster', { facing: 'down', waterlogged: 'false' })
    put(blockX + (blockX < 7 ? -1 : 1), 9, blockZ + 1, 'amethyst_block')
    put(blockX + (blockX < 7 ? -1 : 1), 8, blockZ + 1, 'amethyst_cluster', { facing: 'down', waterlogged: 'false' })
  }
  for (const end of [0, 41]) {
    for (let blockX = 3; blockX <= 11; blockX++) for (let blockY = 2; blockY <= 9; blockY++) {
      const distance = Math.abs(blockX - 7)
      const top = 8 - Math.max(0, distance - 1)
      const open = distance <= 2 && blockY < top
      const pale = distance === 3 && blockY <= 5 || distance <= 3 && blockY === top
      for (const blockZ of [end, end + 1]) put(blockX, blockY, blockZ, open ? 'air' : pale ? 'calcite' : 'deepslate_bricks')
    }
  }
  return { structure, metadata: { name: 'room' } }
}
