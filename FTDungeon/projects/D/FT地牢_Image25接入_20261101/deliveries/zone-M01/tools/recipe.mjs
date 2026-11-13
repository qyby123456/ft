export default function build(api) {
  const structure = api.createStructure([27, 11, 23])
  const put = (position, block, properties, nbt) => api.setBlock(structure, position, block, properties, nbt)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, block, properties)
  fill([0, 0, 0], [26, 10, 22], 'minecraft:air')
  fill([0, 0, 0], [26, 0, 22], 'minecraft:stone')
  for (let east = 0; east < 27; east++) {
    for (let south = 0; south < 23; south++) {
      const variation = (east * 19 + south * 13) % 17
      put([east, 1, south], variation < 3 ? 'minecraft:andesite' : variation === 4 ? 'minecraft:cobblestone' : 'minecraft:stone')
    }
  }
  fill([0, 2, 0], [26, 9, 0], 'minecraft:stone')
  fill([0, 2, 22], [26, 9, 22], 'minecraft:stone')
  fill([0, 2, 0], [0, 9, 22], 'minecraft:stone')
  fill([26, 2, 0], [26, 9, 22], 'minecraft:stone')
  for (let east = 0; east < 27; east++) {
    for (let height = 2; height < 9; height++) {
      for (const south of [0, 22]) {
        const variation = (east * 7 + height * 11 + south) % 29
        put([east, height, south], variation < 3 ? 'minecraft:andesite' : variation === 4 ? 'minecraft:cobblestone' : 'minecraft:stone')
      }
    }
  }
  for (let south = 1; south < 22; south++) {
    for (let height = 2; height < 9; height++) {
      for (const east of [0, 26]) put([east, height, south], (south * 7 + height * 11 + east) % 23 < 4 ? 'minecraft:andesite' : 'minecraft:stone')
    }
  }
  for (const [start, stop, ore] of [[2, 8, 'minecraft:coal_ore'], [14, 19, 'minecraft:iron_ore'], [22, 25, 'minecraft:coal_ore']]) {
    for (let east = start; east <= stop; east++) for (let height = 2; height <= 6; height++) {
      if ((east + height * 3) % 5 !== 0) put([east, height, 0], ore)
    }
  }
  for (const east of [0, 26]) for (let south = 2; south <= 5; south++) for (let height = 2; height <= 6; height++) {
    if ((south + height) % 3) put([east, height, south], 'minecraft:coal_ore')
  }
  fill([0, 9, 0], [26, 9, 22], 'minecraft:spruce_planks')
  fill([0, 10, 0], [26, 10, 22], 'minecraft:stone')
  for (const east of [0, 26]) {
    fill([east, 2, 8], [east, 6, 12], 'minecraft:air')
    for (const south of [7, 13]) fill([east, 2, south], [east, 7, south], 'minecraft:stripped_spruce_log', { axis: 'y' })
    fill([east, 7, 7], [east, 7, 13], 'minecraft:stripped_spruce_log', { axis: 'z' })
  }
  fill([11, 2, 22], [15, 6, 22], 'minecraft:air')
  for (const east of [10, 16]) fill([east, 2, 22], [east, 7, 22], 'minecraft:stripped_spruce_log', { axis: 'y' })
  fill([10, 7, 22], [16, 7, 22], 'minecraft:stripped_spruce_log', { axis: 'x' })
  for (const east of [6, 20]) {
    for (const south of [5, 16]) {
      put([east, 2, south], 'minecraft:stone_bricks')
      fill([east, 3, south], [east, 7, south], 'minecraft:stripped_spruce_log', { axis: 'y' })
    }
    fill([east, 8, 1], [east, 8, 21], 'minecraft:stripped_spruce_log', { axis: 'z' })
    for (const south of [7, 10, 14]) {
      fill([east, 6, south], [east, 7, south], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
      put([east, 5, south], 'minecraft:lantern', { hanging: 'true', waterlogged: 'false' })
    }
    put([east, 7, 6], 'minecraft:spruce_stairs', { facing: 'south', half: 'top', shape: 'straight', waterlogged: 'false' })
    put([east, 7, 15], 'minecraft:spruce_stairs', { facing: 'north', half: 'top', shape: 'straight', waterlogged: 'false' })
  }
  for (const south of [5, 16]) fill([1, 8, south], [25, 8, south], 'minecraft:stripped_spruce_log', { axis: 'x' })
  for (let east = 0; east < 27; east++) {
    put([east, 1, 10], 'minecraft:polished_andesite')
    put([east, 2, 10], 'minecraft:rail', { shape: 'east_west', waterlogged: 'false' })
    for (const south of [8, 9, 11, 12]) put([east, 1, south], 'minecraft:stone_bricks')
  }
  fill([11, 1, 13], [15, 1, 22], 'minecraft:stone_bricks')
  for (const east of [11, 15]) fill([east, 1, 17], [east, 1, 22], 'minecraft:polished_andesite')
  for (const position of [[3, 2, 3], [9, 2, 3], [23, 2, 4], [4, 2, 20]]) {
    put(position, 'minecraft:barrel', { facing: 'up', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel', Items: [] }))
  }
  for (const east of [12, 16]) fill([east, 2, 2], [east, 2, 3], 'minecraft:spruce_fence', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
  fill([12, 3, 2], [16, 3, 3], 'minecraft:spruce_slab', { type: 'bottom', waterlogged: 'false' })
  put([17, 2, 3], 'minecraft:crafting_table')
  put([18, 2, 3], 'minecraft:anvil', { facing: 'east' })
  put([21, 2, 3], 'minecraft:grindstone', { face: 'floor', facing: 'south' })
  for (const [start, ore] of [[2, 'minecraft:coal_block'], [22, 'minecraft:raw_iron_block']]) {
    fill([start, 2, 17], [start + 2, 2, 18], 'minecraft:spruce_planks')
    fill([start, 3, 17], [start + 2, 3, 18], 'minecraft:spruce_slab', { type: 'bottom', waterlogged: 'false' })
    put([start + 1, 3, 17], ore)
    put([start + 1, 3, 18], ore)
    put([start + 1, 4, 18], 'minecraft:stone_slab', { type: 'bottom', waterlogged: 'false' })
  }
  for (const east of [8, 18]) put([east, 2, 18], 'minecraft:cauldron')
  for (const [east, south] of [[3, 7], [23, 7], [3, 14], [23, 14], [8, 20], [18, 20]]) put([east, 1, south], (east + south) % 2 ? 'minecraft:coal_ore' : 'minecraft:iron_ore')
  for (const [start, stop] of [[1, 8], [18, 25]]) fill([start, 2, 21], [stop, 2, 21], 'minecraft:spruce_fence', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
  return { structure, metadata: { name: 'room', id: 'zone-M01', title: '活跃矿道', size: [27, 11, 23], roof: 'complete', deployment: 'independent only' } }
}
