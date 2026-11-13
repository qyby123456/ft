const size = [25, 13, 25]

export default function build(api) {
  const structure = api.createStructure(size)
  const put = (east, up, south, block, properties, nbt) => api.setBlock(structure, [east, up, south], block, properties, nbt)
  const fill = (minimum, maximum, block, properties) => api.fill(structure, minimum, maximum, block, properties)
  const noise = (east, up, south) => (east * 73856093 ^ up * 19349663 ^ south * 83492791) >>> 0
  const stone = (east, up, south) => noise(east, up, south) % 17 < 3 ? 'minecraft:cracked_stone_bricks' : 'minecraft:stone_bricks'
  const slab = { type: 'bottom', waterlogged: 'false' }
  const barrel = (east, up, south) => put(east, up, south, 'minecraft:barrel', { facing: 'up', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel' }))
  const banner = (east, south, facing) => put(east, 7, south, 'minecraft:red_wall_banner', { facing }, api.createBlockNbt({ id: 'minecraft:banner', Patterns: [{ Color: 15, Pattern: 'gra' }, { Color: 15, Pattern: 'bts' }] }))
  fill([0, 0, 0], [24, 12, 24], 'minecraft:air')
  for (let east = 0; east < 25; east++) {
    for (let south = 0; south < 25; south++) {
      put(east, 0, south, stone(east, 0, south))
      put(east, 12, south, 'minecraft:deepslate_tiles')
      for (let up = 1; up < 12; up++) {
        if (east < 2 || east > 22 || south < 2 || south > 22) {
          const burnt = east < 10 && south > 16 && up > 2
          put(east, up, south, burnt && noise(east, up, south) % 3 ? 'minecraft:deepslate_bricks' : stone(east, up, south))
        }
      }
    }
  }
  for (let east = 8; east <= 17; east++) {
    for (let south = 7; south <= 17; south++) {
      const border = east === 8 || east === 17 || south === 7 || south === 17
      const burnt = noise(east, 0, south) % 7 < 2 || (east < 12 && south > 14)
      put(east, 0, south, burnt ? 'minecraft:black_wool' : border ? 'minecraft:red_terracotta' : 'minecraft:red_wool')
    }
  }
  for (const [east, south] of [[4, 5], [20, 5], [4, 20], [20, 20]]) {
    fill([east - 1, 1, south - 1], [east + 1, 1, south + 1], 'minecraft:stone_bricks')
    fill([east, 2, south], [east, 10, south], 'minecraft:polished_andesite')
    for (const up of [2, 8]) put(east, up, south, 'minecraft:polished_deepslate')
    fill([east - 1, 10, south - 1], [east + 1, 10, south + 1], 'minecraft:stone_bricks')
    fill([east - 1, 11, south - 1], [east + 1, 11, south + 1], 'minecraft:deepslate_bricks')
  }
  for (const south of [5, 20]) fill([4, 10, south], [20, 10, south], 'minecraft:dark_oak_log', { axis: 'x' })
  for (const east of [4, 20]) fill([east, 10, 5], [east, 10, 20], 'minecraft:dark_oak_log', { axis: 'z' })
  for (const south of [2, 22]) fill([2, 8, south], [22, 8, south], 'minecraft:dark_oak_log', { axis: 'x' })
  for (const east of [2, 22]) fill([east, 8, 2], [east, 8, 22], 'minecraft:dark_oak_log', { axis: 'z' })
  fill([11, 1, 0], [13, 4, 1], 'minecraft:air')
  fill([23, 1, 11], [24, 4, 13], 'minecraft:air')
  for (const east of [10, 14]) fill([east, 1, 0], [east, 5, 1], 'minecraft:polished_andesite')
  fill([10, 5, 0], [14, 5, 1], 'minecraft:chiseled_stone_bricks')
  for (const south of [10, 14]) fill([23, 1, south], [24, 5, south], 'minecraft:polished_andesite')
  fill([23, 5, 10], [24, 5, 14], 'minecraft:chiseled_stone_bricks')
  for (const east of [0, 24]) for (const south of [0, 24]) fill([east, 1, south], [east, 12, south], 'minecraft:polished_deepslate')
  for (let east = 2; east <= 10; east++) {
    for (let south = 15; south <= 22; south++) {
      const distance = Math.abs(east - 4) + Math.abs(south - 22)
      if (distance > 10) continue
      const height = Math.max(0, 4 - Math.floor(distance / 2) + noise(east, 1, south) % 2)
      put(east, 0, south, 'minecraft:blackstone')
      for (let up = 1; up <= height; up++) put(east, up, south, noise(east, up, south) % 4 ? 'minecraft:cracked_deepslate_bricks' : 'minecraft:cobbled_deepslate')
      if (height && noise(east, height, south) % 3 === 0) put(east, height + 1, south, 'minecraft:cobbled_deepslate_slab', slab)
    }
  }
  for (let step = 0; step <= 6; step++) {
    for (const east of [8, 9]) for (const south of [17 + step, 18 + step]) put(east, 1 + step, south, 'minecraft:dark_oak_log', { axis: 'z' })
  }
  for (const [east, south] of [[5, 17], [9, 20], [3, 19]]) {
    put(east, 1, south, 'minecraft:shroomlight')
    put(east, 2, south, 'minecraft:orange_stained_glass')
    for (const [offsetEast, offsetSouth] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) put(east + offsetEast, 1, south + offsetSouth, 'minecraft:polished_blackstone')
  }
  for (const [east, south] of [[6, 14], [10, 17], [12, 20], [15, 21], [17, 18], [19, 17]]) {
    put(east, 0, south, 'minecraft:blackstone')
    put(east, 1, south, 'minecraft:cobbled_deepslate_slab', slab)
  }
  for (const south of [7, 10]) fill([2, 1, south], [2, 4, south], 'minecraft:dark_oak_log', { axis: 'y' })
  for (const up of [1, 3, 5]) fill([2, up, 7], [3, up, 10], 'minecraft:dark_oak_planks')
  for (const coordinates of [[3, 2, 7], [3, 2, 9], [3, 4, 8], [3, 4, 10], [21, 1, 18], [22, 1, 18], [22, 2, 18], [8, 1, 9]]) barrel(...coordinates)
  for (const east of [5, 7]) for (const south of [9, 12]) put(east, 1, south, 'minecraft:dark_oak_fence', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
  fill([5, 2, 9], [7, 2, 12], 'minecraft:dark_oak_slab', { type: 'top', waterlogged: 'false' })
  put(6, 3, 10, 'minecraft:candle', { candles: '3', lit: 'true', waterlogged: 'false' })
  for (const [east, south] of [[3, 13], [18, 21]]) {
    put(east, 1, south, 'minecraft:chiseled_stone_bricks')
    put(east, 2, south, 'minecraft:stone_brick_wall', { up: 'true', north: 'none', east: 'none', south: 'none', west: 'none', waterlogged: 'false' })
    put(east, 3, south, 'minecraft:stone_bricks')
    put(east, 4, south, 'minecraft:candle', { candles: '4', lit: 'true', waterlogged: 'false' })
  }
  for (const south of [15, 16, 17]) {
    put(18, 1, south, 'minecraft:dark_oak_log', { axis: 'z' })
    put(19, 1, south, 'minecraft:dark_oak_slab', slab)
  }
  for (const [east, south] of [[5, 5], [19, 5], [19, 20], [6, 20]]) {
    fill([east, 7, south], [east, 9, south], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
    put(east, 6, south, 'minecraft:lantern', { hanging: 'true', waterlogged: 'false' })
    put(east, 10, south, 'minecraft:dark_oak_log', { axis: 'x' })
  }
  banner(2, 5, 'east')
  banner(2, 13, 'east')
  banner(16, 22, 'north')
  banner(22, 17, 'west')
  return { structure, metadata: {
    name: 'room', id: 'zone-C04', title: '焦毁塌角', size,
    entrance: [12, 1, 0], interior: [12, 1, 4], facing: 'north',
    connections: [{ side: 'north', feet: [12, 1, 0], width: 3, height: 4 }, { side: 'east', feet: [24, 1, 12], width: 3, height: 4 }],
    protectedRoute: [{ min: [11, 1, 0], max: [13, 3, 13] }, { min: [11, 1, 11], max: [24, 3, 13] }],
    reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/corners/C04-corners-burnt-corner-collapse.png',
    referenceSha256: '2c960f11d1cb074939998fd525e3059bd3c21adae2c7c005c75de3317d96ce0c',
    conceptFeatures: ['four grounded banded pillars', 'scorched red rug inset flush in floor', 'southwest collapse and continuous fallen timber', 'chain lanterns', 'burnt red banners', 'shelving and candle table', 'grounded rubble', 'complete shell with north east corner connection'],
    limits: ['Block scale interpretation of concept', 'Embers use shroomlight and orange glass', 'Cutaways are preview-only; final NBT retains roof']
  } }
}
