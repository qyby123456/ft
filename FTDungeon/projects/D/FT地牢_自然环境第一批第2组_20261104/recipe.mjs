export const specs = [
  ['large-chamber', '荒漠日轮祭坛', [19, 12, 19], ['north', 'south', 'east']],
  ['medium-room', '砂岩蓄水间', [15, 9, 15], ['north', 'south', 'east']],
  ['small-room', '沙埋藏宝室', [9, 7, 9], ['north']],
  ['great-hall', '双列柱大厅', [21, 12, 15], ['north', 'south', 'east']],
  ['corridor', '砂岩直廊', [7, 8, 19], ['north', 'south']],
  ['corner', '砂岩直角廊', [13, 8, 13], ['north', 'east']],
  ['entrance', '荒漠守卫前厅', [15, 10, 13], ['north', 'south']],
  ['courtyard', '绿洲天井庭院', [19, 11, 19], ['north', 'south', 'east', 'west']],
  ['treasure', '日轮宝库祭坛', [15, 10, 15], ['north']],
]

export default function build(api) {
  const index = Number(process.env.FT_GROUP2_INDEX ?? 0)
  const [kind, title, conceptSize, directions] = specs[index]
  const authoritySizes = [[19, 16, 19], [13, 8, 13], [7, 8, 7], [19, 8, 13], [5, 8, 17], [13, 8, 13], [13, 10, 13], [19, 16, 19], [19, 16, 19]]
  const size = authoritySizes[index]
  const [width, height, depth] = size
  const centerX = Math.floor(width / 2)
  const centerZ = Math.floor(depth / 2)
  const structure = api.createStructure(size)
  const put = (blockX, blockY, blockZ, block, properties, data) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`, properties, data)
  const fill = (minimum, maximum, block) => api.fill(structure, minimum, maximum, `minecraft:${block}`)
  const footprint = (blockX, blockZ) => kind !== 'corner' || (Math.abs(blockX - centerX) <= 3 && blockZ <= centerZ + 3) || (blockX >= centerX - 3 && Math.abs(blockZ - centerZ) <= 3)
  for (let blockX = 0; blockX < width; blockX++) for (let blockZ = 0; blockZ < depth; blockZ++) {
    for (let blockY = 0; blockY < height; blockY++) put(blockX, blockY, blockZ, 'air')
    if (!footprint(blockX, blockZ)) continue
    const boundary = [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([deltaX, deltaZ]) => blockX + deltaX < 0 || blockX + deltaX >= width || blockZ + deltaZ < 0 || blockZ + deltaZ >= depth || !footprint(blockX + deltaX, blockZ + deltaZ))
    put(blockX, 0, blockZ, Math.abs(blockX - centerX) === Math.abs(blockZ - centerZ) || (blockX + blockZ) % 8 === 0 ? 'orange_terracotta' : 'smooth_sandstone')
    if (!(kind === 'courtyard' && blockX > 4 && blockX < width - 5 && blockZ > 4 && blockZ < depth - 5)) put(blockX, height - 1, blockZ, 'cut_sandstone')
    if (boundary) for (let blockY = 1; blockY < height - 1; blockY++) put(blockX, blockY, blockZ, blockY === 3 || blockY === height - 3 ? 'orange_terracotta' : blockY === 4 ? 'chiseled_sandstone' : 'sandstone')
  }
  const pillar = (blockX, blockZ, top = height - 2) => {
    for (let blockY = 1; blockY <= top; blockY++) put(blockX, blockY, blockZ, blockY === 3 ? 'chiseled_sandstone' : blockY === 1 || blockY === top ? 'cut_sandstone' : 'smooth_sandstone')
  }
  const lamp = (blockX, blockZ) => {
    fill([blockX, 1, blockZ], [blockX, 2, blockZ], 'cut_sandstone')
    put(blockX, 3, blockZ, 'lantern', { hanging: 'false', waterlogged: 'false' })
  }
  if (kind !== 'corner' && kind !== 'corridor') for (const blockX of [1, width - 2]) {
    for (const blockZ of [2, depth - 3]) pillar(blockX, blockZ)
    for (let blockZ = 3; blockZ < depth - 2; blockZ++) put(blockX, height - 2, blockZ, 'cut_sandstone')
    put(blockX, 1, 4, 'terracotta')
    put(blockX, 2, 4, 'flower_pot')
    lamp(blockX, depth - 4)
  }
  if (kind === 'large-chamber' || kind === 'treasure') {
    fill([centerX - 2, 1, centerZ - 2], [centerX + 2, 1, centerZ + 2], 'cut_sandstone')
    fill([centerX - 1, 2, centerZ - 1], [centerX + 1, 2, centerZ + 1], 'chiseled_sandstone')
    for (const deltaX of [-2, 2]) for (const deltaZ of [-2, 2]) put(centerX + deltaX, 2, centerZ + deltaZ, 'lantern', { hanging: 'false', waterlogged: 'false' })
    if (kind === 'treasure') put(centerX, 3, centerZ, 'chest', { facing: 'north', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest' }))
  }
  if (kind === 'small-room') put(centerX, 1, centerZ, 'chest', { facing: 'north', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest' }))
  if (kind === 'medium-room' || kind === 'courtyard') {
    for (let blockX = centerX - 2; blockX <= centerX + 2; blockX++) for (let blockZ = centerZ - 2; blockZ <= centerZ + 2; blockZ++) {
      const water = Math.abs(blockX - centerX) < 2 && Math.abs(blockZ - centerZ) < 2
      put(blockX, 1, blockZ, water ? 'water' : 'cut_sandstone', water ? { level: '0' } : undefined)
    }
    if (kind === 'courtyard') {
      for (let blockY = 1; blockY <= 5; blockY++) put(centerX + 4, blockY, centerZ, 'oak_log', { axis: 'y' })
      for (let blockX = centerX + 2; blockX <= centerX + 6; blockX++) for (let blockZ = centerZ - 2; blockZ <= centerZ + 2; blockZ++) put(blockX, 6, blockZ, 'oak_leaves', { persistent: 'true', distance: '1', waterlogged: 'false' })
      for (const blockZ of [3, depth - 4]) {
        put(3, 1, blockZ, 'sand')
        put(3, 2, blockZ, 'dead_bush')
      }
    }
  }
  if (kind === 'great-hall') {
    for (const blockX of [5, width - 6]) for (const blockZ of [3, 7, 11]) pillar(blockX, blockZ)
    for (const blockX of [5, width - 6]) for (let blockZ = 2; blockZ <= depth - 3; blockZ++) put(blockX, height - 2, blockZ, 'cut_sandstone')
    for (const blockZ of [3, 7, 11]) {
      for (let blockX = 6; blockX <= width - 7; blockX++) put(blockX, height - 2, blockZ, 'cut_sandstone')
      put(centerX, height - 3, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
    }
  }
  if (kind === 'entrance') for (const blockX of [3, width - 4]) {
    const sideX = blockX === 3 ? 1 : width - 2
    pillar(sideX, 6, 5)
    put(sideX, 6, 6, 'chiseled_sandstone')
    put(sideX, 7, 6, 'lantern', { hanging: 'false', waterlogged: 'false' })
  }
  if (kind === 'corridor') for (const blockZ of [3, 7, 11, depth - 2]) {
    for (let blockX = 1; blockX < width - 1; blockX++) put(blockX, height - 2, blockZ, 'cut_sandstone')
    put(centerX, height - 3, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
    for (const blockX of [0, width - 1]) pillar(blockX, blockZ)
  }
  if (kind === 'corner') {
    pillar(centerX - 3, centerZ + 3)
    lamp(centerX - 2, centerZ + 2)
    for (const [blockX, blockZ] of [[centerX, 2], [width - 3, centerZ]]) put(blockX, height - 2, blockZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  if (kind === 'entrance') {
    fill([1, 1, 4], [7, 3, 4], 'cut_sandstone')
    fill([5, 1, 9], [width - 2, 3, 9], 'cut_sandstone')
    put(7, 3, 4, 'chiseled_sandstone')
    put(5, 3, 9, 'chiseled_sandstone')
  }
  if (kind === 'large-chamber') {
    fill([centerX, 3, centerZ], [centerX, 9, centerZ], 'chiseled_sandstone')
    put(centerX, 10, centerZ, 'gold_block')
    fill([6, 1, 5], [6, 3, 13], 'cut_sandstone')
    for (let step = 0; step < 4; step++) for (const blockX of [3, 4]) {
      if (step > 0) fill([blockX, 1, 4 + step], [blockX, step, 4 + step], 'smooth_sandstone')
      put(blockX, 1 + step, 4 + step, 'smooth_sandstone_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
    }
    fill([2, 4, 8], [5, 4, 13], 'cut_sandstone')
    for (const blockX of [2, 5]) for (const blockZ of [8, 13]) fill([blockX, 1, blockZ], [blockX, 3, blockZ], 'smooth_sandstone')
    for (const blockX of [2, 5]) fill([blockX, 5, 8], [blockX, 5, 13], 'cut_sandstone')
    fill([2, 5, 13], [5, 5, 13], 'cut_sandstone')
  }
  if (kind === 'medium-room') {
    fill([2, 1, 9], [2, 1, 9], 'chiseled_sandstone')
    put(2, 2, 9, 'lever', { face: 'floor', facing: 'north', powered: 'false' })
    put(3, 1, 9, 'redstone_lamp', { lit: 'false' })
    fill([4, 1, 3], [8, 2, 3], 'cut_sandstone')
    fill([4, 1, 9], [8, 2, 9], 'cut_sandstone')
  }
  if (kind === 'treasure') fill([4, 1, 5], [width - 5, 3, 5], 'cut_sandstone')
  for (const direction of directions) for (let offset = -2; offset <= 2; offset++) {
    const blockX = direction === 'west' ? 0 : direction === 'east' ? width - 1 : centerX + offset
    const blockZ = direction === 'north' ? 0 : direction === 'south' ? depth - 1 : centerZ + offset
    put(blockX, 4, blockZ, 'chiseled_sandstone')
    if (Math.abs(offset) <= 1) for (let blockY = 1; blockY <= 3; blockY++) put(blockX, blockY, blockZ, 'air')
  }
  return { structure, metadata: { name: `g2-${String(index + 1).padStart(2, '0')}-${kind}`, title } }
}
