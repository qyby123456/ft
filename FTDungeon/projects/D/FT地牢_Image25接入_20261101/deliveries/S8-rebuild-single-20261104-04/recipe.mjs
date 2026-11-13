export default function build(api) {
  const structure = api.createStructure([31, 19, 39])
  const put = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (first, last, name, properties = {}) => api.fill(structure, first, last, `minecraft:${name}`, properties)
  fill([0, 0, 0], [30, 18, 38], 'air')
  fill([0, 0, 0], [30, 3, 38], 'deepslate_tiles')
  fill([0, 18, 0], [30, 18, 38], 'deepslate_tiles')
  for (let blockX = 0; blockX < 31; blockX++) for (let blockZ = 0; blockZ < 39; blockZ++) {
    const onAxis = blockX >= 13 && blockX <= 17 && blockZ >= 6
    const border = (blockX === 12 || blockX === 18) && blockZ >= 6
    const diamond = Math.abs(blockX - 15) + Math.abs(blockZ % 8 - 4)
    const vein = (blockX * 3 + blockZ * 2) % 19 < 2
    put(blockX, 3, blockZ, border ? 'calcite' : onAxis ? diamond <= 2 ? 'amethyst_block' : diamond === 3 ? 'calcite' : 'polished_deepslate' : vein ? 'amethyst_block' : 'deepslate_bricks')
    if (blockX < 2 || blockX > 28 || blockZ < 2 || blockZ > 36) {
      for (let blockY = 4; blockY < 18; blockY++) {
        const pale = (blockY === 7 || blockY === 15) && (blockX + blockZ) % 9 < 4
        const purple = (blockX * 3 + blockZ * 5 + Math.floor(blockY / 3)) % 23 < 2
        put(blockX, blockY, blockZ, pale ? 'calcite' : purple ? 'amethyst_block' : blockY % 4 === 0 ? 'cobbled_deepslate' : 'deepslate_bricks')
      }
    }
  }
  for (const blockX of [8, 22]) for (const blockZ of [9, 17, 25, 33]) {
    fill([blockX - 2, 4, blockZ - 2], [blockX + 2, 4, blockZ + 2], 'deepslate_bricks')
    fill([blockX - 1, 5, blockZ - 1], [blockX + 1, 5, blockZ + 1], 'smooth_quartz')
    fill([blockX - 1, 6, blockZ - 1], [blockX + 1, 14, blockZ + 1], 'deepslate_bricks')
    for (const side of [-1, 1]) {
      fill([blockX + side, 6, blockZ], [blockX + side, 13, blockZ], 'purple_stained_glass')
      fill([blockX, 6, blockZ + side], [blockX, 13, blockZ + side], 'purple_stained_glass')
      put(blockX + side, 5, blockZ, 'amethyst_block')
    }
    for (const level of [9, 14]) fill([blockX - 1, level, blockZ - 1], [blockX + 1, level, blockZ + 1], 'smooth_quartz')
    fill([blockX - 2, 15, blockZ - 2], [blockX + 2, 15, blockZ + 2], 'smooth_quartz')
    fill([blockX - 2, 16, blockZ - 2], [blockX + 2, 17, blockZ + 2], 'deepslate_bricks')
    put(blockX, 6, blockZ - 1, 'amethyst_block')
    put(blockX, 7, blockZ - 1, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
    api.setBlock(structure, [blockX, 12, blockZ - 2], 'minecraft:purple_wall_banner', { facing: 'north' }, api.createBlockNbt({ id: 'minecraft:banner', Patterns: [] }))
    fill([blockX + (blockX < 15 ? 2 : -2), 13, blockZ], [blockX + (blockX < 15 ? 2 : -2), 17, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX + (blockX < 15 ? 2 : -2), 12, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const blockZ of [9, 17, 25, 33]) {
    fill([2, 17, blockZ - 1], [28, 17, blockZ + 1], 'deepslate_bricks')
    for (const blockX of [3, 27]) {
      fill([blockX - 1, 4, blockZ - 1], [blockX + 1, 14, blockZ + 1], 'deepslate_bricks')
      fill([blockX - 1, 14, blockZ - 1], [blockX + 1, 15, blockZ + 1], 'calcite')
      put(blockX, 5, blockZ - 1, 'amethyst_block')
      put(blockX, 6, blockZ - 1, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
    }
  }
  fill([14, 16, 19], [16, 17, 21], 'amethyst_block')
  fill([14, 14, 19], [16, 15, 21], 'purple_stained_glass')
  fill([15, 13, 20], [15, 15, 20], 'amethyst_block')
  for (const [blockX, blockZ, level] of [[15, 20, 12], [14, 19, 13], [16, 19, 13], [14, 21, 13], [16, 21, 13]]) {
    put(blockX, level + 1, blockZ, 'amethyst_block')
    put(blockX, level, blockZ, 'amethyst_cluster', { facing: 'down', waterlogged: 'false' })
  }
  for (const blockX of [3, 27]) for (const blockZ of [4, 13, 21, 29, 35]) {
    put(blockX, 4, blockZ, 'calcite')
    fill([blockX, 5, blockZ], [blockX, 6 + blockZ % 2, blockZ], 'amethyst_block')
    put(blockX, 7 + blockZ % 2, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  const arch = (direction, center, floorY) => {
    for (let offset = -4; offset <= 4; offset++) for (let blockY = floorY; blockY <= floorY + 8; blockY++) {
      const distance = Math.abs(offset)
      const top = floorY + 6 - Math.max(0, distance - 1)
      const open = distance <= 2 && blockY < top
      const frame = distance === 3 && blockY <= floorY + 3 || distance <= 3 && blockY === top
      for (const depth of [0, 1]) {
        const blockX = direction === 'north' || direction === 'south' ? center + offset : direction === 'west' ? depth : 30 - depth
        const blockZ = direction === 'west' || direction === 'east' ? center + offset : direction === 'north' ? depth : 38 - depth
        put(blockX, blockY, blockZ, open ? 'air' : frame ? 'calcite' : 'deepslate_bricks')
      }
    }
  }
  fill([11, 2, 0], [19, 3, 4], 'air')
  fill([11, 1, 0], [19, 1, 4], 'deepslate_tiles')
  fill([12, 2, 4], [18, 2, 4], 'deepslate_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  fill([12, 3, 5], [18, 3, 5], 'deepslate_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  arch('north', 15, 2)
  arch('south', 15, 4)
  arch('west', 20, 4)
  arch('east', 20, 4)
  return { structure, metadata: { name: 'room' } }
}
