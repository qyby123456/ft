export default function build(api) {
  const size = [27, 19, 25]
  const structure = api.createStructure(size)
  const block = (east, up, south, name) => api.setBlock(structure, [east, up, south], `minecraft:${name}`)
  const box = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name}`)
  const stone = (east, up, south) => (east * 13 + up * 31 + south * 17) % 23 < 5 ? 'prismarine' : 'prismarine_bricks'
  box([0, 0, 0], [26, 18, 24], 'water[level=0]')
  box([0, 0, 0], [26, 1, 24], 'dark_prismarine')
  for (let east = 0; east < 27; east++) for (let south = 0; south < 25; south++) {
    const sand = ((east * 7 + south * 11) % 31 < 4) || (south === 21 && east > 5 && east < 22)
    block(east, 2, south, sand ? 'sandstone' : stone(east, 2, south))
  }
  for (let up = 3; up <= 14; up++) {
    for (let east = 0; east < 27; east++) {
      const height = east < 7 ? 14 : east < 16 ? 12 : 11
      if (up <= height) for (const south of [0, 1]) block(east, up, south, stone(east, up, south))
      if (up <= 4) for (const south of [23, 24]) block(east, up, south, stone(east, up, south))
    }
    for (let south = 0; south < 25; south++) {
      const height = south < 5 ? 14 : south < 19 ? 11 : 7
      if (up <= height) for (const east of [0, 1]) block(east, up, south, stone(east, up, south))
      if (up <= 5) for (const east of [25, 26]) block(east, up, south, stone(east, up, south))
    }
  }
  for (const [east, south, height] of [[1, 1, 15], [5, 1, 15], [15, 0, 12], [23, 0, 12], [0, 4, 12], [0, 13, 12], [0, 20, 8], [23, 22, 7]]) {
    box([east, 3, south], [east + 2, height, south + 2], 'prismarine_bricks')
    box([east, height, south], [east + 2, height, south + 2], 'prismarine')
    block(east + 1, Math.min(height - 2, 7), south + 2, 'sea_lantern')
  }
  box([1, 15, 1], [7, 15, 4], 'prismarine_bricks')
  for (let south = 5; south <= 13; south++) for (let depth = 0; depth <= 2; depth++) {
    const offset = Math.abs(south - 9)
    for (let up = 3; up <= 10; up++) block(depth, up, south, offset >= 3 || up >= 9 - Math.max(0, offset - 1) ? 'prismarine_bricks' : 'water[level=0]')
    block(depth, 2, south, 'prismarine_bricks')
  }
  block(2, 10, 9, 'sea_lantern')
  box([1, 11, 6], [2, 11, 12], 'prismarine_brick_slab[type=top,waterlogged=true]')
  box([1, 12, 6], [1, 13, 12], 'dark_prismarine')
  box([2, 12, 6], [2, 13, 6], 'prismarine_bricks')
  box([2, 12, 12], [2, 13, 12], 'prismarine_bricks')
  box([2, 14, 6], [2, 14, 12], 'prismarine_brick_slab[type=bottom,waterlogged=true]')
  box([9, 3, 0], [13, 8, 0], 'dark_prismarine')
  box([9, 4, 1], [13, 8, 2], 'water[level=0]')
  box([9, 4, 2], [13, 8, 2], 'dark_oak_fence[waterlogged=true]')
  box([8, 3, 2], [8, 9, 3], 'prismarine_bricks')
  box([14, 3, 2], [14, 9, 3], 'prismarine_bricks')
  box([8, 9, 2], [14, 9, 3], 'prismarine_bricks')
  block(14, 7, 3, 'sea_lantern')
  box([17, 3, 2], [23, 3, 5], 'sandstone')
  for (const east of [18, 22]) {
    api.setBlock(structure, [east, 4, 3], 'minecraft:chest', {facing: 'south', type: 'single', waterlogged: 'true'}, api.createBlockNbt({id: 'minecraft:chest', Items: []}))
    box([east, 5, 2], [east, 5, 3], 'dark_oak_slab[type=top,waterlogged=true]')
  }
  box([19, 5, 2], [21, 5, 3], 'dark_oak_slab[type=top,waterlogged=true]')
  box([20, 4, 2], [20, 4, 3], 'dark_oak_fence[waterlogged=true]')
  box([3, 3, 16], [5, 3, 19], 'sandstone')
  box([3, 4, 16], [4, 4, 18], 'prismarine_brick_slab[type=top,waterlogged=true]')
  box([11, 3, 12], [17, 3, 17], 'prismarine_brick_slab[type=top,waterlogged=true]')
  for (const [east, south] of [[12, 13], [16, 13], [12, 16], [16, 16]]) box([east, 4, south], [east, 5, south], 'dark_prismarine')
  box([11, 6, 12], [17, 6, 17], 'prismarine_brick_slab[type=top,waterlogged=true]')
  block(14, 7, 14, 'sea_lantern')
  const coral = (east, up, south, type, height, direction) => {
    for (let rise = 0; rise < height; rise++) block(east, up + rise, south, `${type}_coral_block`)
    block(east, up + height, south, `${type}_coral[waterlogged=true]`)
    const branchEast = east + (direction === 'east' ? 1 : direction === 'west' ? -1 : 0)
    const branchSouth = south + (direction === 'south' ? 1 : direction === 'north' ? -1 : 0)
    block(branchEast, up + height - 1, branchSouth, `${type}_coral_block`)
    block(branchEast, up + height, branchSouth, `${type}_coral_fan[waterlogged=true]`)
  }
  coral(12, 7, 14, 'brain', 1, 'north')
  coral(16, 7, 15, 'fire', 1, 'south')
  coral(19, 6, 3, 'fire', 1, 'south')
  coral(21, 6, 3, 'brain', 1, 'south')
  coral(23, 6, 2, 'bubble', 2, 'south')
  box([3, 3, 3], [4, 4, 4], 'bubble_coral_block')
  box([3, 3, 3], [3, 4, 3], 'prismarine')
  coral(4, 5, 4, 'bubble', 2, 'east')
  box([23, 3, 16], [24, 4, 18], 'prismarine')
  coral(24, 5, 17, 'brain', 2, 'west')
  box([0, 11, 18], [3, 11, 20], 'prismarine_bricks')
  coral(2, 12, 19, 'fire', 1, 'north')
  coral(4, 16, 2, 'brain', 1, 'east')
  box([18, 11, 0], [21, 11, 2], 'prismarine_bricks')
  coral(20, 12, 1, 'brain', 1, 'west')
  box([24, 5, 6], [26, 6, 9], 'prismarine')
  coral(25, 7, 7, 'fire', 1, 'south')
  for (const [east, up, south] of [[5, 4, 19], [6, 3, 20], [17, 4, 4], [23, 4, 5], [3, 3, 14], [6, 3, 5], [20, 3, 20], [22, 3, 21]]) {
    block(east, up - 1, south, 'sandstone')
    block(east, up, south, 'sea_pickle[pickles=3,waterlogged=true]')
  }
  for (const [east, south, height] of [[6, 3, 10], [7, 3, 8], [3, 15, 7], [24, 12, 9], [22, 21, 7], [4, 21, 6]]) {
    for (let up = 3; up < height; up++) block(east, up, south, 'kelp_plant')
    block(east, height, south, 'kelp[age=25]')
  }
  for (const entry of structure.getBlocks()) {
    if (!entry.state.getName().toString().endsWith('_coral_block')) continue
    const wet = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]].some(offset => {
      const neighbor = structure.getBlock(entry.pos.map((coordinate, index) => coordinate + offset[index]))
      return neighbor && (neighbor.state.getName().toString() === 'minecraft:water' || neighbor.state.getProperties().waterlogged === 'true' || neighbor.state.getName().toString().includes('kelp'))
    })
    if (!wet) throw new Error(`Coral has no water neighbor: ${entry.pos}`)
  }
  return {structure, metadata: {name: 's5-02-coral-study', reference: 'S5-02-medium-room.png', size, ports: [{anchor: [0, 3, 9], outward: 'west', clearance: [5, 3, 3]}], landmarks: {table: [14, 6, 14], gratedNiche: [11, 5, 2], chestBench: [20, 4, 3]}}}
}
