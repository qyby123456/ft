export default function build(api) {
  const size = [19, 12, 19]
  const structure = api.createStructure(size)
  const put = (blockX, blockY, blockZ, name, properties) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (first, last, name, properties) => api.fill(structure, first, last, `minecraft:${name}`, properties)
  fill([0, 0, 0], [18, 11, 18], 'air')
  for (let blockX = 0; blockX < 19; blockX++) for (let blockZ = 0; blockZ < 19; blockZ++) {
    const radius = Math.hypot(blockX - 9, blockZ - 9)
    const ring = radius >= 5.1 && radius < 6.3
    const spokes = (Math.abs(blockX - 9) <= 1 || Math.abs(blockZ - 10) <= 1) && radius >= 6.3
    put(blockX, 0, blockZ, ring ? (blockX + blockZ) % 3 === 0 ? 'calcite' : 'amethyst_block' : spokes && (blockX + blockZ) % 4 === 0 ? 'calcite' : (blockX * 7 + blockZ * 3) % 9 === 0 ? 'polished_deepslate' : 'deepslate_tiles')
    put(blockX, 11, blockZ, 'deepslate_tiles')
    if (blockX === 0 || blockX === 18 || blockZ === 0 || blockZ === 18) {
      for (let blockY = 1; blockY < 11; blockY++) put(blockX, blockY, blockZ, (blockX * 3 + blockZ + blockY) % 17 === 0 ? 'amethyst_block' : 'deepslate_bricks')
    }
    if (radius <= 3.5) put(blockX, 1, blockZ, 'polished_deepslate')
    if (radius <= 2.5) put(blockX, 2, blockZ, 'deepslate_bricks')
    if (radius <= 1.5) put(blockX, 3, blockZ, 'amethyst_block')
  }
  for (const [blockZ, level] of [[5, 1], [7, 2], [8, 3]]) {
    for (let blockX = 8; blockX <= 10; blockX++) {
      if (level > 1) fill([blockX, 1, blockZ], [blockX, level - 1, blockZ], 'calcite')
      put(blockX, level, blockZ, 'smooth_quartz_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
    }
  }
  const spire = (blockX, blockZ, base, peak) => {
    fill([blockX, base, blockZ], [blockX, peak, blockZ], 'amethyst_block')
    put(blockX, peak + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ, peak] of [[9, 9, 7], [8, 9, 5], [10, 9, 4], [8, 10, 4], [9, 10, 6], [10, 10, 5]]) spire(blockX, blockZ, 4, peak)
  for (const [blockX, blockZ] of [[6, 9], [12, 9], [8, 12], [10, 12]]) spire(blockX, blockZ, 2, 2)
  for (const [pillarX, pillarZ] of [[2, 3], [16, 3], [4, 15], [14, 15]]) {
    fill([pillarX - 1, 1, pillarZ - 1], [pillarX + 1, 1, pillarZ + 1], 'polished_deepslate')
    fill([pillarX, 2, pillarZ], [pillarX, 10, pillarZ], 'deepslate_bricks')
    for (const blockY of [3, 7]) put(pillarX, blockY, pillarZ, 'calcite')
    for (const blockY of [4, 8]) put(pillarX, blockY, pillarZ, 'amethyst_block')
    fill([pillarX - 1, 10, pillarZ], [pillarX + 1, 10, pillarZ], 'deepslate_bricks')
  }
  fill([2, 10, 3], [16, 10, 3], 'deepslate_bricks')
  fill([4, 10, 15], [14, 10, 15], 'deepslate_bricks')
  fill([2, 10, 3], [2, 10, 15], 'deepslate_bricks')
  fill([16, 10, 3], [16, 10, 15], 'deepslate_bricks')
  for (const [blockX, blockZ] of [[5, 3], [13, 3], [4, 12], [14, 12], [9, 16]]) {
    fill([blockX, 8, blockZ], [blockX, 10, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put(blockX, 7, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[6, 4], [12, 4], [3, 14], [15, 14], [8, 16], [10, 16]]) {
    put(blockX, 10, blockZ, 'amethyst_block')
    put(blockX, 9, blockZ, 'amethyst_cluster', { facing: 'down', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[1, 6], [17, 6], [2, 13], [16, 13], [6, 17], [12, 17]]) spire(blockX, blockZ, 1, (blockX + blockZ) % 2 + 1)
  for (const [side, center] of [['north', 9], ['south', 9], ['west', 10], ['east', 10]]) {
    const coordinate = (offset, blockY) => side === 'north' || side === 'south' ? [center + offset, blockY, side === 'north' ? 0 : 18] : [side === 'west' ? 0 : 18, blockY, center + offset]
    for (let offset = -2; offset <= 2; offset++) for (let blockY = 1; blockY <= 5; blockY++) {
      const position = coordinate(offset, blockY)
      const opening = Math.abs(offset) <= 1 && blockY <= 3 || offset === 0 && blockY === 4
      put(...position, opening ? 'air' : 'amethyst_block')
    }
  }
  return { structure, metadata: { name: 's8-01-crystal-reference' } }
}
