export default function build(api) {
  const structure = api.createStructure([23, 13, 23])
  const put = (blockX, blockY, blockZ, name, properties) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (first, last, name, properties) => api.fill(structure, first, last, `minecraft:${name}`, properties)
  fill([0, 0, 0], [22, 12, 22], 'air')
  for (let blockX = 0; blockX < 23; blockX++) for (let blockZ = 0; blockZ < 23; blockZ++) {
    const radius = Math.hypot(blockX - 11, blockZ - 11)
    const ring = radius >= 5.8 && radius <= 7
    const mosaic = ring ? (blockX + blockZ) % 3 === 0 ? 'calcite' : 'amethyst_block' : (blockX * 11 + blockZ * 7) % 13 < 2 ? 'polished_deepslate' : 'deepslate_tiles'
    put(blockX, 0, blockZ, mosaic)
    put(blockX, 12, blockZ, 'deepslate_tiles')
    if (blockX === 0 || blockX === 22 || blockZ === 0 || blockZ === 22) {
      for (let blockY = 1; blockY < 12; blockY++) put(blockX, blockY, blockZ, (blockX * 3 + blockZ * 5 + blockY) % 19 === 0 ? 'amethyst_block' : 'deepslate_bricks')
    }
    if (radius <= 4.2) put(blockX, 1, blockZ, 'polished_deepslate')
    if (radius <= 3.2) put(blockX, 2, blockZ, 'deepslate_bricks')
    if (radius <= 2.2) put(blockX, 3, blockZ, 'amethyst_block')
  }
  for (const [blockZ, level] of [[6, 1], [8, 2], [9, 3]]) {
    for (let blockX = 10; blockX <= 12; blockX++) {
      if (level > 1) fill([blockX, 1, blockZ], [blockX, level - 1, blockZ], 'calcite')
      put(blockX, level, blockZ, 'smooth_quartz_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
    }
  }
  const spire = (blockX, blockZ, bottom, top) => {
    fill([blockX, bottom, blockZ], [blockX, top, blockZ], 'amethyst_block')
    put(blockX, top + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  spire(11, 11, 4, 8)
  spire(10, 11, 4, 6)
  spire(12, 11, 4, 5)
  spire(10, 12, 4, 5)
  spire(11, 12, 4, 7)
  spire(12, 12, 4, 5)
  for (const [blockX, blockZ] of [[8, 11], [14, 11], [9, 13], [13, 13]]) spire(blockX, blockZ, 3, 3)
  for (const [cornerX, cornerZ] of [[3, 3], [18, 3], [3, 18], [18, 18]]) {
    fill([cornerX - 1, 1, cornerZ - 1], [cornerX + 2, 1, cornerZ + 2], 'polished_deepslate')
    fill([cornerX, 2, cornerZ], [cornerX + 1, 11, cornerZ + 1], 'deepslate_bricks')
    for (const level of [3, 7]) fill([cornerX, level, cornerZ], [cornerX + 1, level, cornerZ + 1], 'calcite')
    for (const level of [4, 8]) fill([cornerX, level, cornerZ], [cornerX + 1, level, cornerZ + 1], 'amethyst_block')
    fill([cornerX - 1, 10, cornerZ - 1], [cornerX + 2, 10, cornerZ + 2], 'deepslate_bricks')
  }
  fill([3, 11, 3], [19, 11, 4], 'deepslate_bricks')
  fill([3, 11, 18], [19, 11, 19], 'deepslate_bricks')
  fill([3, 11, 3], [4, 11, 19], 'deepslate_bricks')
  fill([18, 11, 3], [19, 11, 19], 'deepslate_bricks')
  const arches = [{ direction: 'north', center: 11 }, { direction: 'south', center: 11 }, { direction: 'east', center: 12 }, { direction: 'west', center: 12 }]
  for (const arch of arches) {
    for (let offset = -3; offset <= 3; offset++) for (let blockY = 1; blockY <= 6; blockY++) {
      const position = arch.direction === 'north' || arch.direction === 'south' ? [arch.center + offset, blockY, arch.direction === 'north' ? 0 : 22] : [arch.direction === 'west' ? 0 : 22, blockY, arch.center + offset]
      const opening = Math.abs(offset) <= 1 && blockY <= 4 || offset === 0 && blockY === 5
      const rim = Math.abs(offset) <= 2 && blockY <= 5 || offset === 0
      put(...position, opening ? 'air' : rim ? 'amethyst_block' : 'deepslate_bricks')
    }
  }
  for (const [blockX, blockZ] of [[6, 4], [16, 4], [4, 16], [18, 16], [8, 20], [14, 20]]) {
    fill([blockX, 9, blockZ], [blockX, 11, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX, 8, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[7, 3], [15, 3], [3, 8], [19, 8], [7, 19], [15, 19]]) {
    put(blockX, 11, blockZ, 'amethyst_block')
    put(blockX, 10, blockZ, 'amethyst_cluster', { facing: 'down', waterlogged: 'false' })
  }
  for (const [blockX, blockZ, peak] of [[1, 6, 2], [21, 7, 3], [2, 16, 2], [20, 16, 1], [7, 21, 2], [15, 21, 3]]) {
    put(blockX, 1, blockZ, 'calcite')
    spire(blockX, blockZ, 2, peak + 1)
  }
  return { structure, metadata: { name: 's8-reset-img01-crystal-chamber' } }
}
