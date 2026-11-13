export const dimensions = [15, 12, 48]
export const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M18-ice-frost-intact.png'
export const referenceSha256 = '8d2f06845f6e1310619e555ab8d3d9cf389cc8415cafa70f728eb94127a11ea8'

export default function build(api) {
  const structure = api.createStructure(dimensions)
  const set = (position, block, properties) => api.setBlock(structure, position, `minecraft:${block}`, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, `minecraft:${block}`, properties)
  const stair = (position, facing) => set(position, 'stone_brick_stairs', { facing, half: 'top', shape: 'straight', waterlogged: 'false' })
  const slab = (position, block, type) => set(position, block, { type, waterlogged: 'false' })
  fill([0, 0, 0], [14, 11, 47], 'air')
  fill([0, 0, 0], [14, 0, 47], 'stone_bricks')
  fill([0, 1, 0], [1, 10, 47], 'stone_bricks')
  fill([13, 1, 0], [14, 10, 47], 'stone_bricks')
  fill([0, 11, 0], [14, 11, 47], 'stone_bricks')
  fill([2, 10, 0], [12, 10, 47], 'packed_ice')
  for (let depth = 0; depth < 48; depth += 1) {
    for (let across = 2; across <= 12; across += 1) {
      let block = 'packed_ice'
      if ([2, 4, 10, 12].includes(across)) block = 'calcite'
      if (across >= 5 && across <= 9 && (across + depth) % 4 === 0) block = 'blue_ice'
      if ([0, 47].includes(depth) && across >= 5 && across <= 9) block = 'smooth_stone'
      if (depth % 8 === 0 && across >= 5 && across <= 9) block = 'calcite'
      set([across, 0, depth], block)
    }
    for (const side of [2, 12]) {
      set([side, 1, depth], 'stone_bricks')
      fill([side, 2, depth], [side, 7, depth], 'packed_ice')
      fill([side, 8, depth], [side, 9, depth], 'calcite')
      if (depth % 8 >= 2 && depth % 8 <= 5) set([side, 6, depth], 'blue_ice')
      stair([side === 2 ? 3 : 11, 9, depth], side === 2 ? 'west' : 'east')
    }
    if (depth % 8 === 4) {
      fill([3, 10, depth], [11, 10, depth], 'calcite')
      fill([5, 10, depth], [9, 10, depth], 'blue_ice')
    }
  }
  const ribs = [0, 8, 16, 24, 32, 40, 47]
  for (const depth of ribs) {
    for (const across of [2, 3, 11, 12]) {
      fill([across, 1, depth], [across, 8, depth], [3, 11].includes(across) ? 'packed_ice' : 'stone_bricks')
      set([across, 1, depth], 'chiseled_stone_bricks')
      set([across, 7, depth], 'calcite')
      set([across, 8, depth], 'stone_bricks')
    }
    fill([4, 9, depth], [10, 10, depth], 'stone_bricks')
    set([7, 9, depth], 'chiseled_stone_bricks')
    stair([4, 8, depth], 'west')
    stair([10, 8, depth], 'east')
    stair([5, 9, depth], 'west')
    stair([9, 9, depth], 'east')
  }
  const candleSupports = []
  const icicles = []
  for (const depth of [4, 12, 20, 28, 36, 44]) {
    for (const across of [2, 12]) {
      fill([across, 2, depth - 1], [across, 5, depth + 1], 'air')
      fill([across === 2 ? 1 : 13, 2, depth - 1], [across === 2 ? 1 : 13, 5, depth + 1], 'stone_bricks')
      fill([across, 1, depth - 1], [across, 1, depth + 1], 'calcite')
      set([across, 2, depth], 'candle', { candles: '3', lit: 'true', waterlogged: 'false' })
      candleSupports.push({ candle: [across, 2, depth], support: [across, 1, depth] })
      for (const offset of [-2, 2]) fill([across, 2, depth + offset], [across, 6, depth + offset], 'stone_bricks')
      fill([across, 6, depth - 1], [across, 6, depth + 1], 'calcite')
      stair([across, 5, depth - 1], 'north')
      stair([across, 5, depth + 1], 'south')
      const inner = across === 2 ? 3 : 11
      slab([inner, 1, depth], 'quartz_slab', 'top')
      stair([inner, 6, depth], across === 2 ? 'west' : 'east')
      set([inner, 8, depth], 'blue_ice')
      set([inner, 7, depth], 'light_blue_stained_glass_pane', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
      icicles.push({ anchor: [inner, 9, depth], cap: [inner, 8, depth], tip: [inner, 7, depth] })
      if ([4, 44].includes(depth)) set([inner, 1, depth + 1], 'snow_block')
    }
  }
  const cages = []
  for (const depth of [8, 24, 40]) {
    for (const across of [5, 9]) {
      fill([across - 1, 5, depth - 1], [across + 1, 5, depth + 1], 'stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
      for (let horizontal = -1; horizontal <= 1; horizontal += 1) {
        for (let longitudinal = -1; longitudinal <= 1; longitudinal += 1) {
          if (horizontal === 0 && longitudinal === 0) continue
          for (const height of [6, 7]) set([across + horizontal, height, depth + longitudinal], 'iron_bars', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
        }
      }
      fill([across - 1, 8, depth - 1], [across + 1, 8, depth + 1], 'stone_brick_slab', { type: 'top', waterlogged: 'false' })
      set([across, 8, depth], 'chiseled_stone_bricks')
      set([across, 9, depth], 'chain', { axis: 'y', waterlogged: 'false' })
      cages.push({ center: [across, 6, depth], anchor: [across, 10, depth], chain: [across, 9, depth], cap: [across, 8, depth], bottom: [across, 5, depth] })
    }
  }
  for (const depth of [0, 47]) {
    fill([0, 1, depth], [4, 10, depth], 'stone_bricks')
    fill([10, 1, depth], [14, 10, depth], 'stone_bricks')
    fill([5, 6, depth], [9, 10, depth], 'stone_bricks')
    fill([4, 1, depth], [4, 4, depth], 'chiseled_stone_bricks')
    fill([10, 1, depth], [10, 4, depth], 'chiseled_stone_bricks')
    stair([5, 5, depth], 'west')
    stair([9, 5, depth], 'east')
    set([7, 6, depth], 'calcite')
    fill([2, 2, depth], [2, 7, depth], 'packed_ice')
    fill([12, 2, depth], [12, 7, depth], 'packed_ice')
    set([2, 1, depth], 'calcite')
    set([12, 1, depth], 'calcite')
  }
  return { structure, metadata: {
    name: 'room', id: 'corridor-M18', title: '冰霜烛龛悬笼直廊', size: dimensions,
    entrance: [7, 1, 0], interior: [7, 1, 3], exit: [7, 1, 47], facing: 'north',
    reference, reference_sha256: referenceSha256,
    blueprint: { ribs, candle_supports: candleSupports, icicles, cages, clear_aisle: { min: [5, 1, 0], max: [9, 3, 47] } },
  } }
}
