export const specification = {
  id: 'H04', name: '柱列中庭', size: [33, 23, 37],
  entrance: [16, 2, 0], interior: [16, 2, 7], facing: 'north',
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/halls/H04-halls-pillared-atrium.png',
  reference_sha256: '7f2d1443078ad7b3ae6611ce5af55ef41682bb80b2dbbcbde497c2bf50554e84',
  pillars: [[9, 10], [23, 10], [9, 28], [23, 28]],
  ports: { north: [16, 2, 0], east: [32, 2, 19], south: [16, 2, 36], west: [0, 2, 19] },
  balcony_safety_points: [[4, 10, 17], [28, 10, 17], [16, 10, 32]],
  roof_y: 21, basin: { floor_y: 1, water_y: 2, min: [12, 15], max: [20, 23] },
}

export default function build(api) {
  const structure = api.createStructure(specification.size)
  const put = (blockX, blockY, blockZ, block, properties, nbt) => api.setBlock(structure, [blockX, blockY, blockZ], block.includes(':') ? block : 'minecraft:' + block, properties, nbt)
  const box = (from, to, block, properties) => api.fill(structure, from, to, block.includes(':') ? block : 'minecraft:' + block, properties)
  const stairs = (blockX, blockY, blockZ, facing, half = 'bottom') => put(blockX, blockY, blockZ, 'stone_brick_stairs', { facing, half, shape: 'straight', waterlogged: 'false' })
  const lantern = (blockX, blockY, blockZ, hanging = false) => put(blockX, blockY, blockZ, 'lantern', { hanging: String(hanging), waterlogged: 'false' })
  const leaves = (blockX, blockY, blockZ) => put(blockX, blockY, blockZ, 'azalea_leaves', { persistent: 'true', distance: '7', waterlogged: 'false' })
  const masonry = (blockX, blockY, blockZ) => {
    const texture = (blockX * 37 + blockY * 13 + blockZ * 19) % 29
    return texture < 3 ? 'mossy_stone_bricks' : texture < 6 ? 'cracked_stone_bricks' : 'stone_bricks'
  }
  box([0, 0, 0], [32, 22, 36], 'air')
  box([0, 0, 0], [32, 0, 36], 'deepslate_bricks')
  for (let blockX = 0; blockX < 33; blockX++) {
    for (let blockZ = 0; blockZ < 37; blockZ++) {
      const border = blockX < 2 || blockX > 30 || blockZ < 2 || blockZ > 34
      const axial = Math.abs(blockX - 16) <= 1 || Math.abs(blockZ - 19) <= 1
      put(blockX, 1, blockZ, border ? 'chiseled_stone_bricks' : axial ? 'polished_andesite' : masonry(blockX, 1, blockZ))
      if (!border && !axial && (blockX + blockZ) % 7 === 0) put(blockX, 1, blockZ, 'smooth_stone')
      for (let blockY = 2; blockY <= 20; blockY++) if (border) put(blockX, blockY, blockZ, masonry(blockX, blockY, blockZ))
      put(blockX, 21, blockZ, 'stone_bricks')
      if (blockX === 0 || blockX === 32 || blockZ === 0 || blockZ === 36) put(blockX, 22, blockZ, 'smooth_stone')
    }
  }
  for (const blockY of [4, 9, 18, 20]) {
    box([0, blockY, 0], [32, blockY, 0], 'smooth_stone')
    box([0, blockY, 36], [32, blockY, 36], 'smooth_stone')
    box([0, blockY, 0], [0, blockY, 36], 'smooth_stone')
    box([32, blockY, 0], [32, blockY, 36], 'smooth_stone')
  }
  for (const blockZ of [5, 19, 31]) {
    for (const blockX of [0, 1, 31, 32]) box([blockX, 12, blockZ - 1], [blockX, 17, blockZ + 1], 'cyan_stained_glass')
    for (const blockX of [2, 30]) {
      box([blockX, 11, blockZ - 2], [blockX, 11, blockZ + 2], 'chiseled_stone_bricks')
      box([blockX, 18, blockZ - 2], [blockX, 18, blockZ + 2], 'smooth_stone')
      for (const sideZ of [blockZ - 2, blockZ + 2]) box([blockX, 12, sideZ], [blockX, 17, sideZ], 'polished_andesite')
    }
  }
  for (const blockX of [6, 16, 26]) {
    box([blockX - 1, 12, 35], [blockX + 1, 17, 36], 'cyan_stained_glass')
    box([blockX - 2, 11, 34], [blockX + 2, 11, 34], 'chiseled_stone_bricks')
    box([blockX - 2, 18, 34], [blockX + 2, 18, 34], 'smooth_stone')
    for (const sideX of [blockX - 2, blockX + 2]) box([sideX, 12, 34], [sideX, 17, 34], 'polished_andesite')
  }
  for (const [centerX, centerZ] of specification.pillars) {
    box([centerX - 2, 2, centerZ - 2], [centerX + 2, 2, centerZ + 2], 'mossy_stone_bricks')
    box([centerX - 2, 3, centerZ - 2], [centerX + 2, 3, centerZ + 2], 'chiseled_stone_bricks')
    box([centerX - 1, 4, centerZ - 1], [centerX + 1, 17, centerZ + 1], 'polished_diorite')
    for (const bandY of [5, 16]) box([centerX - 1, bandY, centerZ - 1], [centerX + 1, bandY, centerZ + 1], 'gold_block')
    box([centerX - 2, 18, centerZ - 2], [centerX + 2, 18, centerZ + 2], 'chiseled_stone_bricks')
    box([centerX - 2, 19, centerZ - 2], [centerX + 2, 19, centerZ + 2], 'smooth_stone')
    box([centerX - 1, 19, 1], [centerX + 1, 20, 35], 'stone_bricks')
    box([1, 19, centerZ - 1], [31, 20, centerZ + 1], 'stone_bricks')
    for (const offset of [-3, 3]) {
      stairs(centerX + offset, 18, centerZ, offset < 0 ? 'east' : 'west', 'top')
      stairs(centerX, 18, centerZ + offset, offset < 0 ? 'south' : 'north', 'top')
    }
    for (const [offsetX, offsetZ, facing] of [[0, -2, 'north'], [2, 0, 'east'], [0, 2, 'south'], [-2, 0, 'west']]) {
      const nbt = api.createBlockNbt({ id: 'minecraft:banner', Patterns: [{ Color: 4, Pattern: 'cr' }, { Color: 4, Pattern: 'bo' }] })
      put(centerX + offsetX, 14, centerZ + offsetZ, 'blue_wall_banner', { facing }, nbt)
    }
  }
  for (const [minX, maxX] of [[2, 7], [25, 30]]) {
    box([minX, 8, 15], [maxX, 9, 34], 'stone_bricks')
    for (const blockZ of [16, 22, 28, 34]) {
      const pierX = minX === 2 ? 2 : 30
      box([pierX, 2, blockZ - 1], [pierX, 7, blockZ + 1], 'polished_andesite')
      box([pierX, 7, blockZ - 2], [pierX, 7, blockZ + 2], 'chiseled_stone_bricks')
      stairs(minX === 2 ? 3 : 29, 7, blockZ, minX === 2 ? 'west' : 'east', 'top')
    }
  }
  box([2, 8, 30], [30, 9, 34], 'stone_bricks')
  for (const blockX of [6, 12, 20, 26]) {
    box([blockX - 1, 2, 34], [blockX + 1, 7, 34], 'polished_andesite')
    box([blockX - 2, 7, 34], [blockX + 2, 7, 34], 'chiseled_stone_bricks')
    stairs(blockX, 7, 33, 'south', 'top')
  }
  box([2, 8, 27], [10, 9, 29], 'stone_bricks')
  box([22, 8, 27], [30, 9, 29], 'stone_bricks')
  for (let step = 0; step < 8; step++) {
    const blockZ = 7 + step
    const blockY = 2 + step
    for (const [minX, maxX] of [[3, 5], [27, 29]]) {
      if (blockY > 2) box([minX, 2, blockZ], [maxX, blockY - 1, blockZ], 'stone_bricks')
      for (let blockX = minX; blockX <= maxX; blockX++) stairs(blockX, blockY, blockZ, 'south')
      for (const railX of [minX - 1, maxX + 1]) box([railX, 2, blockZ], [railX, blockY + 1, blockZ], 'stone_bricks')
    }
  }
  for (const blockX of [7, 25]) box([blockX, 10, 15], [blockX, 10, 29], 'iron_bars')
  box([7, 9, 29], [25, 9, 29], 'stone_bricks')
  for (let blockX = 7; blockX <= 25; blockX++) if (![8, 9, 10, 22, 23, 24].includes(blockX)) put(blockX, 10, 29, 'iron_bars')
  for (const [blockX, blockZ] of [[7, 16], [25, 16], [7, 22], [25, 22], [7, 29], [25, 29], [16, 29]]) {
    put(blockX, 10, blockZ, 'chiseled_stone_bricks')
    lantern(blockX, 11, blockZ)
  }
  for (const [blockX, blockZ] of [[6, 7], [26, 7], [6, 11], [26, 11]]) lantern(blockX, blockZ - 3, blockZ)
  for (const [blockX, blockZ] of [[4, 17], [28, 17], [4, 23], [28, 23], [16, 32]]) {
    box([blockX, 15, blockZ], [blockX, 20, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
    lantern(blockX, 14, blockZ, true)
  }
  for (const [blockX, blockZ] of [[6, 5], [26, 5], [11, 18], [21, 18], [11, 23], [21, 23]]) {
    put(blockX, 2, blockZ, 'chiseled_stone_bricks')
    lantern(blockX, 3, blockZ)
  }
  box([12, 1, 15], [20, 1, 23], 'prismarine_bricks')
  for (let blockX = 12; blockX <= 20; blockX++) {
    for (let blockZ = 15; blockZ <= 23; blockZ++) {
      if (blockX === 12 || blockX === 20 || blockZ === 15 || blockZ === 23) {
        put(blockX, 2, blockZ, 'smooth_quartz')
        put(blockX, 3, blockZ, 'smooth_stone_slab', { type: 'bottom', waterlogged: 'false' })
      } else {
        put(blockX, 2, blockZ, 'water', { level: '0' })
        if ((blockX + blockZ) % 3 === 0) put(blockX, 1, blockZ, 'sea_lantern')
      }
    }
  }
  box([15, 2, 18], [17, 4, 20], 'chiseled_stone_bricks')
  box([14, 5, 17], [18, 5, 21], 'smooth_quartz')
  put(16, 6, 19, 'sea_lantern')
  for (const [blockX, blockZ] of [[14, 19], [18, 19], [16, 17], [16, 21]]) box([blockX, 3, blockZ], [blockX, 4, blockZ], 'cyan_stained_glass')
  for (const [blockX, blockZ] of [[12, 15], [20, 15], [12, 23], [20, 23]]) {
    put(blockX, 3, blockZ, 'chiseled_stone_bricks')
    lantern(blockX, 4, blockZ)
  }
  for (const [centerX, blockZ] of [[6, 33], [26, 33]]) {
    box([centerX - 1, 2, blockZ], [centerX + 1, 2, blockZ], 'chiseled_stone_bricks')
    put(centerX, 3, blockZ, 'stone_bricks')
    put(centerX, 4, blockZ, 'polished_andesite')
    box([centerX - 1, 5, blockZ], [centerX + 1, 5, blockZ], 'polished_andesite')
    put(centerX, 6, blockZ, 'chiseled_stone_bricks')
  }
  for (const [blockX, blockZ] of [[3, 4], [29, 4], [3, 31], [29, 31], [4, 33], [28, 33]]) {
    put(blockX, 2, blockZ, 'stripped_spruce_log', { axis: 'y' })
    if (blockZ === 31) put(blockX, 3, blockZ, 'spruce_planks')
  }
  for (const [blockX, blockZ] of [[4, 24], [28, 24], [10, 32], [22, 32]]) {
    put(blockX, 10, blockZ, 'chiseled_stone_bricks')
    put(blockX, 11, blockZ, 'potted_fern')
  }
  for (const blockZ of [8, 24, 33]) for (const blockX of [2, 30]) for (let blockY = 15; blockY <= 18; blockY++) leaves(blockX, blockY, blockZ)
  box([12, 21, 15], [20, 21, 23], 'cyan_stained_glass')
  for (let blockX = 12; blockX <= 20; blockX++) {
    put(blockX, 22, 15, 'polished_andesite')
    put(blockX, 22, 23, 'polished_andesite')
  }
  for (let blockZ = 15; blockZ <= 23; blockZ++) {
    put(12, 22, blockZ, 'polished_andesite')
    put(20, 22, blockZ, 'polished_andesite')
  }
  for (const [from, to] of [[[15, 2, 0], [17, 5, 1]], [[15, 2, 35], [17, 5, 36]], [[0, 2, 18], [1, 5, 20]], [[31, 2, 18], [32, 5, 20]]]) box(from, to, 'air')
  for (const blockX of [14, 18]) {
    box([blockX, 2, 0], [blockX, 5, 1], 'chiseled_stone_bricks')
    box([blockX, 2, 35], [blockX, 5, 36], 'chiseled_stone_bricks')
  }
  box([14, 6, 0], [18, 6, 1], 'smooth_stone')
  box([14, 6, 35], [18, 6, 36], 'smooth_stone')
  for (const blockZ of [17, 21]) {
    box([0, 2, blockZ], [1, 5, blockZ], 'chiseled_stone_bricks')
    box([31, 2, blockZ], [32, 5, blockZ], 'chiseled_stone_bricks')
  }
  box([0, 6, 17], [1, 6, 21], 'smooth_stone')
  box([31, 6, 17], [32, 6, 21], 'smooth_stone')
  return { structure, metadata: { name: 'room', ...specification } }
}
