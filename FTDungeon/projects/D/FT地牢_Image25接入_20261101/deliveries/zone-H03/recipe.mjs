export const design = {
  id: 'zone-H03', design_id: 'H03', name: '烛仪大厅', size: [29, 16, 35],
  entrance: [14, 2, 0], interior: [14, 2, 4], altar_landing: [14, 4, 29], facing: 'north',
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/halls/H03-halls-ceremonial-hall-candles.png',
  reference_sha256: 'c8bfb5116ed1f75a9870537c25f27aa8be7bb21113e79c6e13eefeac0aba7158',
  pillars: [[7, 10], [21, 10], [7, 24], [21, 24]],
  chandeliers: [[14, 7], [14, 22]], main_route: { x: [12, 16], y: 2, z: [0, 26], minimum_clear_height: 3 },
  adaptations: ['概念图剖切补为双层完整屋顶；剖切仅用于软件预览。', '紫金长毯采用与地面齐平的方块镶嵌，中央保留仪式纹章。', '保留四柱、悬链烛盘、墙旗、独立烛台与两级祭台；不使用烟雾实体、真实火或自动战利品。', '北入口净宽五格、净高四格；两排朝南台阶连接后部祭台。']
}

export default function build(api) {
  const structure = api.createStructure(design.size)
  const put = (blockX, blockY, blockZ, name, properties, payload) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties, payload)
  const box = (from, to, name, properties) => api.fill(structure, from, to, `minecraft:${name}`, properties)
  const candle = (blockX, blockY, blockZ, count = 4) => put(blockX, blockY, blockZ, 'candle', { candles: String(count), lit: 'true', waterlogged: 'false' })
  const banner = (blockX, blockY, blockZ, facing) => put(blockX, blockY, blockZ, 'purple_wall_banner', { facing }, api.createBlockNbt({ id: 'minecraft:banner', Patterns: [{ Pattern: 'sc', Color: 4 }, { Pattern: 'bo', Color: 4 }] }))
  box([0, 0, 0], [28, 15, 34], 'air')
  box([0, 0, 0], [28, 1, 34], 'stone_bricks')
  for (let blockX = 0; blockX < 29; blockX += 1) {
    for (let blockZ = 0; blockZ < 35; blockZ += 1) {
      const variant = (blockX * 37 + blockZ * 19) % 23
      put(blockX, 1, blockZ, variant === 0 ? 'cracked_stone_bricks' : (blockX + blockZ) % 4 === 0 ? 'polished_andesite' : 'stone_bricks')
    }
  }
  for (let blockY = 2; blockY <= 13; blockY += 1) {
    for (let blockX = 0; blockX < 29; blockX += 1) {
      for (const blockZ of [0, 34]) put(blockX, blockY, blockZ, (blockX * 13 + blockY * 7 + blockZ) % 19 === 0 ? 'cracked_stone_bricks' : 'stone_bricks')
    }
    for (let blockZ = 1; blockZ < 34; blockZ += 1) {
      for (const blockX of [0, 28]) put(blockX, blockY, blockZ, (blockX + blockY * 17 + blockZ * 5) % 23 === 0 ? 'mossy_stone_bricks' : 'stone_bricks')
    }
  }
  box([0, 14, 0], [28, 15, 34], 'stone_bricks')
  for (const blockX of [1, 27]) {
    box([blockX, 2, 1], [blockX, 2, 33], 'polished_andesite')
    box([blockX, 12, 1], [blockX, 13, 33], 'polished_andesite')
    for (const blockZ of [4, 16, 28]) {
      box([blockX, 3, blockZ], [blockX, 11, blockZ], 'stone_bricks')
      for (const blockY of [3, 7, 11]) put(blockX, blockY, blockZ, 'chiseled_stone_bricks')
    }
  }
  for (const blockZ of [1, 33]) box([1, 12, blockZ], [27, 13, blockZ], 'polished_andesite')
  for (let blockZ = 0; blockZ <= 28; blockZ += 1) {
    for (let blockX = 12; blockX <= 16; blockX += 1) put(blockX, 1, blockZ, blockX === 12 || blockX === 16 ? 'yellow_terracotta' : (blockZ % 7 === 0 ? 'gilded_blackstone' : 'purple_terracotta'))
  }
  for (const blockX of [3, 25]) {
    for (let blockZ = 2; blockZ <= 32; blockZ += 1) put(blockX, 1, blockZ, blockZ % 5 === 0 ? 'yellow_terracotta' : 'purple_terracotta')
  }
  for (let offsetX = -4; offsetX <= 4; offsetX += 1) {
    for (let offsetZ = -4; offsetZ <= 4; offsetZ += 1) {
      const radius = offsetX * offsetX + offsetZ * offsetZ
      if (radius > 20) continue
      const cross = offsetX === 0 && Math.abs(offsetZ) <= 3 || offsetZ === 0 && Math.abs(offsetX) <= 3
      put(14 + offsetX, 1, 17 + offsetZ, radius >= 13 ? 'polished_blackstone_bricks' : cross || Math.abs(offsetX) === 2 && Math.abs(offsetZ) === 2 ? 'calcite' : 'polished_andesite')
    }
  }
  for (const [pillarX, pillarZ] of design.pillars) {
    box([pillarX - 2, 2, pillarZ - 2], [pillarX + 2, 3, pillarZ + 2], 'polished_andesite')
    box([pillarX - 1, 4, pillarZ - 1], [pillarX + 1, 11, pillarZ + 1], 'stone_bricks')
    for (const blockY of [4, 7, 10]) {
      put(pillarX, blockY, pillarZ - 1, 'chiseled_stone_bricks')
      put(pillarX, blockY, pillarZ + 1, 'chiseled_stone_bricks')
    }
    box([pillarX - 2, 12, pillarZ - 2], [pillarX + 2, 13, pillarZ + 2], 'polished_andesite')
    banner(pillarX, 9, pillarZ - 2, 'north')
  }
  for (const blockZ of [10, 24]) box([7, 13, blockZ], [21, 13, blockZ], 'chiseled_stone_bricks')
  for (const blockX of [7, 21]) box([blockX, 13, 10], [blockX, 13, 24], 'chiseled_stone_bricks')
  for (const blockX of [4, 9, 19, 24]) {
    box([blockX, 2, 33], [blockX, 11, 33], 'polished_andesite')
    put(blockX, 8, 33, 'chiseled_stone_bricks')
  }
  for (const blockX of [7, 14, 21]) banner(blockX, 10, 33, 'north')
  for (const blockZ of [10, 22]) {
    banner(1, 9, blockZ, 'east')
    banner(27, 9, blockZ, 'west')
  }
  for (const blockX of [2, 26]) {
    for (const blockZ of [5, 11, 17, 23, 29]) {
      put(blockX, 2, blockZ, 'chiseled_stone_bricks')
      candle(blockX, 3, blockZ, blockZ % 3 + 2)
    }
  }
  for (const blockX of [10, 18]) {
    for (const blockZ of [6, 14, 22]) {
      put(blockX, 2, blockZ, 'polished_blackstone_bricks')
      put(blockX, 3, blockZ, 'chiseled_stone_bricks')
      candle(blockX, 4, blockZ)
    }
    put(blockX, 2, 1, 'chiseled_stone_bricks')
    candle(blockX, 3, 1)
  }
  box([10, 2, 29], [18, 3, 32], 'polished_andesite')
  box([11, 2, 28], [17, 2, 28], 'stone_bricks')
  for (let blockX = 11; blockX <= 17; blockX += 1) {
    put(blockX, 2, 27, 'stone_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
    put(blockX, 3, 28, 'stone_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  }
  for (let blockZ = 29; blockZ <= 30; blockZ += 1) {
    for (let blockX = 12; blockX <= 16; blockX += 1) put(blockX, 3, blockZ, blockX === 12 || blockX === 16 ? 'yellow_terracotta' : 'purple_terracotta')
  }
  box([12, 4, 31], [16, 5, 32], 'polished_blackstone_bricks')
  for (const blockX of [12, 14, 16]) {
    for (const blockZ of [31, 32]) candle(blockX, 6, blockZ, blockX === 14 ? 4 : 3)
  }
  for (const blockX of [10, 18]) for (const blockZ of [30, 32]) candle(blockX, 4, blockZ)
  for (const [centerX, centerZ] of design.chandeliers) {
    box([centerX - 1, 8, centerZ], [centerX + 1, 8, centerZ], 'polished_blackstone_bricks')
    box([centerX, 8, centerZ - 1], [centerX, 8, centerZ + 1], 'polished_blackstone_bricks')
    for (const [offsetX, offsetZ] of [[-1, 0], [1, 0], [0, -1], [0, 1]]) candle(centerX + offsetX, 9, centerZ + offsetZ)
    box([centerX, 9, centerZ], [centerX, 13, centerZ], 'chain', { axis: 'y', waterlogged: 'false' })
  }
  for (const blockX of [0, 28]) for (const blockZ of [5, 17, 29]) {
    put(blockX, 5, blockZ, 'shroomlight')
    put(blockX === 0 ? 1 : 27, 5, blockZ, 'yellow_stained_glass')
  }
  for (const blockX of [11, 17]) box([blockX, 2, 0], [blockX, 7, 1], 'polished_andesite')
  box([11, 6, 0], [17, 7, 1], 'chiseled_stone_bricks')
  box([12, 2, 0], [16, 5, 1], 'air')
  return { structure, metadata: { ...design, name: 'room' } }
}
