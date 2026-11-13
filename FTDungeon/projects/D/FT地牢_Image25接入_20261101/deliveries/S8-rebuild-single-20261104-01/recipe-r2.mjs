export default function build(api) {
  const structure = api.createStructure([35, 21, 35])
  const put = (blockX, blockY, blockZ, name, properties) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (first, last, name, properties) => api.fill(structure, first, last, `minecraft:${name}`, properties)
  fill([0, 0, 0], [34, 20, 34], 'air')
  fill([0, 0, 0], [34, 1, 34], 'deepslate_bricks')
  for (let blockX = 0; blockX < 35; blockX++) for (let blockZ = 0; blockZ < 35; blockZ++) {
    const radius = Math.hypot(blockX - 17, blockZ - 17)
    const ring = radius >= 8.7 && radius <= 10.5
    const vein = (blockX * 7 + blockZ * 11 + Math.floor(blockX / 4)) % 29
    put(blockX, 2, blockZ, ring ? (blockX + blockZ) % 4 === 0 ? 'calcite' : 'amethyst_block' : vein < 2 ? 'amethyst_block' : vein < 6 ? 'polished_deepslate' : 'deepslate_tiles')
    put(blockX, 20, blockZ, 'deepslate_tiles')
    if (blockX === 0 || blockX === 34 || blockZ === 0 || blockZ === 34) {
      for (let blockY = 3; blockY < 20; blockY++) put(blockX, blockY, blockZ, (blockX * 3 + blockZ * 7 + blockY * 5) % 41 < 3 ? 'amethyst_block' : 'deepslate_bricks')
    }
    if (radius <= 7.2) put(blockX, 3, blockZ, 'polished_deepslate')
    if (radius <= 5.8) put(blockX, 4, blockZ, 'deepslate_bricks')
    if (radius <= 4.2) put(blockX, 5, blockZ, 'polished_deepslate')
  }
  for (const level of [3, 17, 19]) {
    fill([1, level, 1], [33, level, 1], 'deepslate_bricks')
    fill([1, level, 33], [33, level, 33], 'deepslate_bricks')
    fill([1, level, 1], [1, level, 33], 'deepslate_bricks')
    fill([33, level, 1], [33, level, 33], 'deepslate_bricks')
  }
  for (const [cornerX, cornerZ] of [[4, 4], [28, 4], [4, 28], [28, 28]]) {
    fill([cornerX - 1, 3, cornerZ - 1], [cornerX + 3, 3, cornerZ + 3], 'polished_deepslate')
    fill([cornerX, 4, cornerZ], [cornerX + 2, 18, cornerZ + 2], 'deepslate_bricks')
    for (const level of [7, 13]) fill([cornerX, level, cornerZ], [cornerX + 2, level, cornerZ + 2], 'calcite')
    for (const level of [8, 9, 14]) fill([cornerX, level, cornerZ], [cornerX + 2, level, cornerZ + 2], 'amethyst_block')
    fill([cornerX - 1, 18, cornerZ - 1], [cornerX + 3, 18, cornerZ + 3], 'deepslate_bricks')
  }
  fill([4, 19, 4], [30, 19, 6], 'deepslate_bricks')
  fill([4, 19, 28], [30, 19, 30], 'deepslate_bricks')
  fill([4, 19, 4], [6, 19, 30], 'deepslate_bricks')
  fill([28, 19, 4], [30, 19, 30], 'deepslate_bricks')
  const spire = (blockX, blockZ, bottom, top) => {
    fill([blockX, bottom, blockZ], [blockX, top, blockZ], 'amethyst_block')
    put(blockX, top + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  fill([16, 6, 16], [18, 10, 18], 'amethyst_block')
  spire(17, 17, 11, 13)
  spire(16, 17, 11, 11)
  spire(18, 18, 11, 12)
  for (const [blockX, blockZ, top] of [[15, 17, 9], [19, 17, 8], [16, 19, 9], [18, 15, 8], [14, 18, 7], [20, 19, 7], [15, 20, 6], [19, 20, 6]]) spire(blockX, blockZ, 6, top)
  for (const [blockZ, level] of [[9, 3], [11, 4], [13, 5]]) {
    fill([15, 3, blockZ], [19, level, blockZ], 'calcite')
    fill([15, level, blockZ], [19, level, blockZ], 'smooth_quartz_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  }
  for (const [direction, center] of [['north', 17], ['south', 17], ['east', 18], ['west', 18]]) {
    for (let offset = -4; offset <= 4; offset++) for (let blockY = 3; blockY <= 12; blockY++) {
      const opening = Math.abs(offset) <= 2 && blockY <= 8 || Math.abs(offset) <= 1 && blockY === 9 || offset === 0 && blockY === 10
      const rim = Math.abs(offset) <= 3 && blockY <= 9 || Math.abs(offset) <= 2 && blockY === 10 || offset === 0 && blockY === 11
      for (const inset of [0, 1]) {
        const position = direction === 'north' || direction === 'south' ? [center + offset, blockY, direction === 'north' ? inset : 34 - inset] : [direction === 'west' ? inset : 34 - inset, blockY, center + offset]
        put(...position, opening ? 'air' : rim ? 'amethyst_block' : 'deepslate_bricks')
      }
    }
  }
  fill([15, 1, 0], [19, 5, 2], 'air')
  fill([15, 1, 1], [19, 1, 1], 'deepslate_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  fill([15, 1, 2], [19, 1, 2], 'deepslate_bricks')
  fill([15, 2, 2], [19, 2, 2], 'deepslate_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  for (const [blockX, blockZ] of [[8, 6], [26, 6], [6, 24], [28, 24], [12, 31], [22, 31]]) {
    fill([blockX, 16, blockZ], [blockX, 19, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX, 15, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const [blockX, blockZ, length] of [[10, 5, 3], [23, 5, 4], [5, 12, 4], [29, 12, 3], [10, 29, 3], [23, 29, 4], [17, 31, 2]]) {
    fill([blockX, 18, blockZ], [blockX + 1, 19, blockZ + 1], 'amethyst_block')
    fill([blockX, 19 - length, blockZ], [blockX, 17, blockZ], 'amethyst_block')
    put(blockX, 18 - length, blockZ, 'amethyst_cluster', { facing: 'down', waterlogged: 'false' })
    put(blockX + 1, 17, blockZ + 1, 'amethyst_cluster', { facing: 'down', waterlogged: 'false' })
  }
  for (const [blockX, blockZ, peak] of [[2, 9, 10], [2, 25, 7], [32, 9, 8], [32, 25, 10], [9, 32, 8], [25, 32, 7], [9, 2, 7], [25, 2, 8]]) {
    fill([blockX - 1, 3, blockZ - 1], [blockX + 1, 4, blockZ + 1], 'calcite')
    spire(blockX, blockZ, 5, peak)
    spire(blockX + 1, blockZ, 5, peak - 2)
    spire(blockX, blockZ + 1, 5, peak - 3)
  }
  return { structure, metadata: { name: 's8-img01-crystal-chamber-r2' } }
}
