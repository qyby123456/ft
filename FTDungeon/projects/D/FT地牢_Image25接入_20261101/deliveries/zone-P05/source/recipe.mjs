export const specification = {
  id: 'P05', name: '石柱跃厅', size: [17, 14, 29], entrance: [8, 5, 0],
  interior: [8, 5, 2], exit: [8, 5, 28], facing: 'north',
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/parkour/P05-parkour-pillar-hop-hall.png',
  reference_sha256: '671abf0d3795ca83ff0554600bbee74ef6a1f3894325624155a06dcfc6f193df',
  pillars: [6, 10, 14, 18, 22].map((startZ, index) => ({
    id: index + 1, from: [7, 1, startZ], to: [8, 4, startZ + 1],
    landing_bounds: [[7, 5, startZ], [8, 5, startZ + 1]],
  })),
  recovery_ladders: [3, 13].map(blockX => ({ x: blockX, z: 4, y: [1, 4], facing: 'south' })),
}

export default function build(api) {
  const structure = api.createStructure(specification.size)
  const place = (position, block) => api.setBlock(structure, position, block)
  const fill = (minimum, maximum, block) => api.fill(structure, minimum, maximum, block)
  const masonry = (blockX, blockY, blockZ) => {
    const variation = (blockX * 17 + blockY * 13 + blockZ * 23) % 19
    if (variation < 4) return 'minecraft:mossy_stone_bricks'
    if (variation < 6) return 'minecraft:cracked_stone_bricks'
    return 'minecraft:stone_bricks'
  }
  fill([0, 0, 0], [16, 13, 28], 'minecraft:air')
  for (let blockX = 0; blockX < 17; blockX++) {
    for (let blockZ = 0; blockZ < 29; blockZ++) {
      const pool = blockX > 0 && blockX < 16 && blockZ >= 4 && blockZ <= 25
      place([blockX, 0, blockZ], pool
        ? ((blockX + blockZ * 3) % 7 < 2 ? 'minecraft:prismarine' : 'minecraft:dark_prismarine')
        : masonry(blockX, 0, blockZ))
      place([blockX, 12, blockZ], masonry(blockX, 12, blockZ))
      if (blockX === 0 || blockX === 16 || blockZ === 0 || blockZ === 28) {
        place([blockX, 13, blockZ], 'minecraft:stone_brick_slab[type=bottom,waterlogged=false]')
      }
      for (let blockY = 1; blockY < 12; blockY++) {
        if (blockX === 0 || blockX === 16 || blockZ === 0 || blockZ === 28) {
          place([blockX, blockY, blockZ], masonry(blockX, blockY, blockZ))
        }
      }
    }
  }
  fill([1, 1, 1], [15, 4, 3], 'minecraft:stone_bricks')
  fill([1, 1, 26], [15, 4, 27], 'minecraft:stone_bricks')
  fill([1, 1, 4], [15, 1, 25], 'minecraft:water[level=0]')
  for (const bankZ of [0, 1, 2, 3, 26, 27, 28]) {
    for (let blockX = 1; blockX < 16; blockX++) {
      place([blockX, 4, bankZ], (blockX >= 7 && blockX <= 9)
        ? 'minecraft:polished_andesite' : masonry(blockX, 4, bankZ))
    }
  }
  for (const sideX of [1, 15]) {
    for (const stationZ of [2, 8, 14, 20, 26]) {
      for (let blockY = 1; blockY <= 11; blockY++) {
        place([sideX, blockY, stationZ], blockY === 4 || blockY === 10
          ? 'minecraft:chiseled_stone_bricks' : masonry(sideX, blockY, stationZ))
      }
    }
  }
  for (const stationZ of [2, 8, 14, 20, 26]) {
    fill([1, 11, stationZ], [15, 11, stationZ], 'minecraft:stone_bricks')
    for (const sideX of [2, 14]) place([sideX, 10, stationZ], 'minecraft:stone_bricks')
  }
  for (const pillar of specification.pillars) {
    for (let blockX = 7; blockX <= 8; blockX++) {
      for (let blockZ = pillar.from[2]; blockZ <= pillar.to[2]; blockZ++) {
        for (let blockY = 1; blockY <= 4; blockY++) {
          place([blockX, blockY, blockZ], blockY === 4 ? 'minecraft:polished_andesite'
            : blockY === 3 ? 'minecraft:chiseled_stone_bricks'
              : blockY === 1 ? 'minecraft:mossy_stone_bricks' : 'minecraft:stone_bricks')
        }
      }
    }
  }
  for (const ladder of specification.recovery_ladders) {
    for (let blockY = 1; blockY <= 4; blockY++) {
      place([ladder.x, blockY, 4], `minecraft:ladder[facing=south,waterlogged=${blockY === 1}]`)
    }
    place([ladder.x, 4, 3], 'minecraft:chiseled_stone_bricks')
  }
  for (const bankZ of [3, 26]) {
    for (const blockX of [1, 2, 4, 5, 11, 12, 14, 15]) {
      place([blockX, 5, bankZ], 'minecraft:oak_fence[waterlogged=false]')
    }
  }
  for (const stationZ of [5, 11, 17, 23]) {
    place([1, 6, stationZ], 'minecraft:wall_torch[facing=east]')
    place([15, 6, stationZ], 'minecraft:wall_torch[facing=west]')
  }
  for (const lampX of [4, 12]) {
    for (const lampZ of [8, 14, 20]) {
      for (let blockY = 9; blockY <= 10; blockY++) {
        place([lampX, blockY, lampZ], 'minecraft:chain[axis=y,waterlogged=false]')
      }
      place([lampX, 8, lampZ], 'minecraft:lantern[hanging=true,waterlogged=false]')
    }
  }
  for (const portZ of [0, 28]) {
    for (const frameX of [6, 10]) fill([frameX, 4, portZ], [frameX, 8, portZ], 'minecraft:chiseled_stone_bricks')
    fill([6, 8, portZ], [10, 8, portZ], 'minecraft:chiseled_stone_bricks')
    fill([7, 5, portZ], [9, 7, portZ], 'minecraft:air')
  }
  for (const blockX of [7, 8, 9]) {
    place([blockX, 4, 0], 'minecraft:yellow_terracotta')
    place([blockX, 4, 28], 'minecraft:lime_terracotta')
  }
  return { structure, metadata: { name: 'room', ...specification } }
}
