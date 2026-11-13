export default function build(api) {
  const structure = api.createStructure([19, 12, 19])
  const put = (east, up, south, block, properties, nbt) => api.setBlock(structure, [east, up, south], block, properties, nbt)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, block, properties)
  const weathered = (east, up, south) => {
    const seed = (east * 31 + up * 19 + south * 47) % 17
    return 'minecraft:' + (seed < 7 ? 'mossy_stone_bricks' : seed < 9 ? 'cracked_stone_bricks' : 'stone_bricks')
  }
  fill([0, 0, 0], [18, 11, 18], 'minecraft:air')
  for (let east = 0; east < 19; east++) {
    for (let south = 0; south < 19; south++) {
      put(east, 0, south, 'minecraft:stone_bricks')
      put(east, 1, south, weathered(east, 1, south))
      put(east, 10, south, weathered(east, 10, south))
      put(east, 11, south, weathered(east, 11, south))
      for (let up = 2; up <= 9; up++) {
        if (east <= 1 || east >= 17 || south <= 1 || south >= 17) put(east, up, south, weathered(east, up, south))
      }
    }
  }
  fill([7, 2, 0], [11, 6, 1], 'minecraft:air')
  fill([17, 2, 7], [18, 6, 11], 'minecraft:air')
  for (let up = 2; up <= 7; up++) {
    for (const east of [6, 12]) fill([east, up, 0], [east, up, 1], up === 7 ? 'minecraft:chiseled_stone_bricks' : 'minecraft:stone_bricks')
    for (const south of [6, 12]) fill([17, up, south], [18, up, south], up === 7 ? 'minecraft:chiseled_stone_bricks' : 'minecraft:stone_bricks')
  }
  fill([6, 7, 0], [12, 7, 1], 'minecraft:chiseled_stone_bricks')
  fill([17, 7, 6], [18, 7, 12], 'minecraft:chiseled_stone_bricks')
  const route = (east, south) => (east >= 7 && east <= 11 && south <= 11) || (east >= 9 && south >= 7 && south <= 11)
  for (let east = 0; east < 19; east++) {
    for (let south = 0; south < 19; south++) {
      if (route(east, south)) put(east, 1, south, 'minecraft:' + ((east + south) % 7 === 0 ? 'chiseled_stone_bricks' : 'polished_andesite'))
    }
  }
  for (const [east, south] of [[5, 4], [14, 4], [4, 10], [14, 14]]) {
    fill([east - 1, 2, south - 1], [east + 1, 2, south + 1], 'minecraft:mossy_stone_bricks')
    fill([east, 3, south], [east, 8, south], 'minecraft:stone_bricks')
    put(east, 4, south, 'minecraft:chiseled_stone_bricks')
    fill([east - 1, 9, south - 1], [east + 1, 9, south + 1], 'minecraft:mossy_stone_bricks')
  }
  fill([2, 8, 2], [2, 8, 16], 'minecraft:mossy_stone_bricks')
  fill([2, 8, 16], [16, 8, 16], 'minecraft:mossy_stone_bricks')
  fill([16, 8, 2], [16, 8, 16], 'minecraft:mossy_stone_bricks')
  fill([2, 8, 2], [16, 8, 2], 'minecraft:mossy_stone_bricks')
  fill([3, 2, 13], [7, 2, 16], 'minecraft:mossy_stone_bricks')
  fill([3, 2, 12], [7, 2, 12], 'minecraft:mossy_stone_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  fill([3, 3, 14], [7, 3, 16], 'minecraft:stone_bricks')
  fill([3, 4, 15], [7, 4, 16], 'minecraft:mossy_stone_bricks')
  fill([5, 5, 15], [5, 6, 15], 'minecraft:cut_sandstone')
  put(5, 7, 15, 'minecraft:chiseled_sandstone')
  for (const east of [4, 6]) put(east, 5, 15, 'minecraft:sandstone')
  fill([3, 5, 16], [3, 7, 16], 'minecraft:chiseled_stone_bricks')
  fill([7, 5, 16], [7, 7, 16], 'minecraft:chiseled_stone_bricks')
  fill([3, 8, 15], [7, 8, 16], 'minecraft:mossy_stone_bricks')
  for (const east of [3, 7]) put(east, 4, 14, 'minecraft:candle', { candles: '2', lit: 'true', waterlogged: 'false' })
  put(5, 4, 14, 'minecraft:flower_pot')
  for (const [east, south] of [[2, 4], [2, 7]]) fill([east, 2, south], [east, 5, south], 'minecraft:dark_oak_log', { axis: 'y' })
  fill([2, 2, 4], [3, 2, 7], 'minecraft:dark_oak_planks')
  fill([2, 4, 4], [3, 4, 7], 'minecraft:dark_oak_planks')
  put(3, 3, 5, 'minecraft:potted_fern')
  put(3, 5, 6, 'minecraft:potted_brown_mushroom')
  put(3, 3, 6, 'minecraft:flower_pot')
  const emptyBarrel = () => api.createBlockNbt({ id: 'minecraft:barrel', Items: [] })
  put(16, 2, 14, 'minecraft:barrel', { facing: 'up', open: 'false' }, emptyBarrel())
  put(16, 2, 15, 'minecraft:barrel', { facing: 'up', open: 'false' }, emptyBarrel())
  put(12, 2, 16, 'minecraft:barrel', { facing: 'up', open: 'false' }, emptyBarrel())
  put(16, 5, 14, 'minecraft:red_wall_banner', { facing: 'west' }, api.createBlockNbt({ id: 'minecraft:banner', Patterns: [{ Color: 4, Pattern: 'mr' }, { Color: 4, Pattern: 'bo' }] }))
  for (const [east, south] of [[3, 9], [9, 15], [15, 5], [15, 13]]) {
    fill([east, 7, south], [east, 9, south], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
    put(east, 6, south, 'minecraft:lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const [east, south] of [[2, 11], [2, 14], [8, 16], [11, 16], [16, 5]]) {
    put(east, 8, south, 'minecraft:rooted_dirt')
    put(east, 7, south, 'minecraft:hanging_roots', { waterlogged: 'false' })
  }
  for (const [east, south, bottom, top, face] of [
    [2, 8, 2, 7, 'west'], [2, 12, 3, 6, 'west'], [2, 15, 2, 6, 'west'],
    [8, 16, 2, 6, 'south'], [10, 16, 2, 7, 'south'], [13, 16, 3, 7, 'south'],
    [16, 3, 3, 7, 'east'], [16, 6, 2, 7, 'east'], [16, 12, 3, 7, 'east'],
    [3, 2, 3, 7, 'north'], [15, 2, 2, 7, 'north']
  ]) {
    for (let up = bottom; up <= top; up++) {
      if (structure.getBlock([east, up, south]).state.getName().toString() !== 'minecraft:air') continue
      put(east, up, south, 'minecraft:vine', { east: 'false', north: 'false', south: 'false', up: 'false', west: 'false', [face]: 'true' })
    }
  }
  for (const [east, south] of [[3, 11], [3, 15], [8, 15], [10, 15], [15, 15], [15, 3], [4, 2], [13, 16]]) {
    if (!route(east, south) && structure.getBlock([east, 2, south]).state.getName().toString() === 'minecraft:air') {
      put(east, 1, south, 'minecraft:moss_block')
      put(east, 2, south, 'minecraft:moss_carpet')
    }
  }
  for (const [east, south] of [[2, 13], [2, 15], [4, 16], [6, 16], [9, 16], [12, 16], [16, 14], [16, 3], [4, 2]]) {
    put(east, 9, south, 'minecraft:azalea_leaves', { distance: '7', persistent: 'true', waterlogged: 'false' })
  }
  return { structure, metadata: { name: 'room' } }
}

