export const plan = {
  id: 'corridor-M03',
  name: 'M03 · 血誓烛廊',
  size: [13, 12, 41],
  entrance: [6, 1, 0],
  interior: [6, 1, 4],
  exit: [6, 1, 40],
  archStations: [0, 8, 16, 24, 32, 40],
  bayStations: [4, 12, 20, 28, 36],
  mainLane: { minimumX: 4, maximumX: 8, floorY: 0, clearHeight: 3 },
}

export default function build(api) {
  const structure = api.createStructure(plan.size)
  const place = (position, block, properties, nbt) => api.setBlock(structure, position, block, properties, nbt)
  const fill = (start, end, block, properties) => api.fill(structure, start, end, block, properties)
  const masonry = position => {
    const sample = (position[0] * 37 + position[1] * 17 + position[2] * 53) % 31
    if (sample === 0) return 'minecraft:cracked_stone_bricks'
    if (sample === 1) return 'minecraft:mossy_stone_bricks'
    return 'minecraft:stone_bricks'
  }
  fill([0, 0, 0], [12, 11, 40], 'minecraft:air')
  for (let depth = 0; depth < 41; depth++) {
    for (let across = 0; across < 13; across++) {
      let floor = 'minecraft:polished_deepslate'
      if (across >= 5 && across <= 7 && depth > 0 && depth < 40) floor = 'minecraft:red_wool'
      if (across === 4 || across === 8) floor = 'minecraft:deepslate_tiles'
      if (across === 3 || across === 9) floor = depth % 8 === 0 ? 'minecraft:chiseled_polished_blackstone' : 'minecraft:red_nether_bricks'
      if (across <= 2 || across >= 10) floor = masonry([across, 0, depth])
      place([across, 0, depth], floor)
      place([across, 11, depth], 'minecraft:deepslate_bricks')
    }
    for (const across of [0, 1, 11, 12]) {
      for (let elevation = 1; elevation <= 10; elevation++) {
        const darkCourse = elevation === 1 || elevation === 4 || elevation === 10
        place([across, elevation, depth], darkCourse ? 'minecraft:deepslate_bricks' : masonry([across, elevation, depth]))
      }
    }
    for (const across of [2, 10]) fill([across, 8, depth], [across, 10, depth], masonry([across, 8, depth]))
    for (const across of [3, 9]) fill([across, 9, depth], [across, 10, depth], 'minecraft:deepslate_bricks')
    for (const across of [4, 8]) place([across, 10, depth], 'minecraft:deepslate_tiles')
  }
  for (const depth of plan.archStations) {
    for (const across of [2, 10]) {
      fill([across, 1, depth], [across, 6, depth], 'minecraft:stone_bricks')
      place([across, 1, depth], 'minecraft:chiseled_stone_bricks')
      place([across, 5, depth], 'minecraft:chiseled_stone_bricks')
      place([across, 6, depth], 'minecraft:polished_andesite')
    }
    fill([2, 7, depth], [3, 7, depth], 'minecraft:stone_bricks')
    fill([9, 7, depth], [10, 7, depth], 'minecraft:stone_bricks')
    for (const [across, facing] of [[3, 'west'], [9, 'east']]) {
      place([across, 6, depth], 'minecraft:stone_brick_stairs', { facing, half: 'top', shape: 'straight', waterlogged: 'false' })
    }
    fill([3, 8, depth], [4, 8, depth], 'minecraft:stone_bricks')
    fill([8, 8, depth], [9, 8, depth], 'minecraft:stone_bricks')
    for (const [across, facing] of [[4, 'west'], [8, 'east']]) {
      place([across, 7, depth], 'minecraft:stone_brick_stairs', { facing, half: 'top', shape: 'straight', waterlogged: 'false' })
    }
    fill([4, 9, depth], [8, 9, depth], 'minecraft:stone_bricks')
    place([6, 9, depth], 'minecraft:chiseled_stone_bricks')
    place([6, 10, depth], depth % 16 === 0 ? 'minecraft:cracked_stone_bricks' : 'minecraft:stone_bricks')
    for (const across of [0, 12]) fill([across, 2, depth], [across, 10, depth], 'minecraft:polished_andesite')
  }
  for (const depth of plan.bayStations) {
    for (const [across, facing] of [[2, 'east'], [10, 'west']]) {
      const anchorX = across === 2 ? 1 : 11
      fill([across, 1, depth - 1], [across, 1, depth + 1], 'minecraft:deepslate_bricks')
      fill([across, 2, depth - 1], [across, 2, depth + 1], 'minecraft:polished_andesite')
      for (const candleDepth of [depth - 1, depth + 1]) {
        place([across, 3, candleDepth], 'minecraft:candle', { candles: candleDepth < depth ? '3' : '4', lit: 'true', waterlogged: 'false' })
      }
      place([across, 6, depth], 'minecraft:red_wall_banner', { facing }, api.createBlockNbt({
        id: 'minecraft:banner',
        Patterns: [
          { Color: 15, Pattern: 'mr' },
          { Color: 14, Pattern: 'sc' },
          { Color: 15, Pattern: 'bo' },
        ],
      }))
      place([anchorX, 6, depth], 'minecraft:chiseled_stone_bricks')
      place([anchorX, 8, depth], 'minecraft:chiseled_stone_bricks')
    }
  }
  return { structure, metadata: { name: 'room' } }
}
