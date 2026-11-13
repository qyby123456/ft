export default function build(api) {
  const size = [31, 22, 31]
  const structure = api.createStructure(size)
  const block = (east, up, south, name) => api.setBlock(structure, [east, up, south], `minecraft:${name}`)
  const box = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name}`)
  box([0, 0, 0], [30, 21, 30], 'water[level=0]')
  box([0, 0, 0], [30, 1, 30], 'dark_prismarine')
  for (let east = 0; east < 31; east++) for (let south = 0; south < 31; south++) block(east, 2, south, east === 11 || east === 19 ? 'dark_prismarine' : (east * 11 + south * 7) % 23 < 5 ? 'sandstone' : 'prismarine_bricks')
  box([0, 3, 0], [30, 14, 1], 'prismarine_bricks')
  box([0, 3, 0], [1, 13, 30], 'prismarine_bricks')
  box([29, 3, 0], [30, 13, 30], 'prismarine_bricks')
  box([0, 3, 29], [30, 3, 30], 'prismarine_bricks')
  for (const east of [0, 27]) for (const south of [0, 27]) box([east, 3, south], [east + 3, 15, south + 3], 'prismarine_bricks')
  for (let east = 4; east <= 26; east++) {
    const top = 14 + (east >= 7 && east <= 23 ? 1 : 0) + (east >= 11 && east <= 19 ? 1 : 0) + (east >= 13 && east <= 17 ? 1 : 0)
    box([east, 14, 0], [east, top, 2], 'prismarine')
  }
  for (const side of ['west', 'east']) {
    const position = (across, up, depth) => [side === 'west' ? depth : 30 - depth, up, 18 + across]
    for (let depth = 0; depth <= 2; depth++) for (let across = -4; across <= 4; across++) {
      const ceiling = 11 - Math.max(0, Math.abs(across) - 1)
      for (let up = 3; up <= 13; up++) api.setBlock(structure, position(across, up, depth), Math.abs(across) <= 2 && up < ceiling ? 'minecraft:water[level=0]' : 'minecraft:prismarine_bricks')
      api.setBlock(structure, position(across, 2, depth), 'minecraft:prismarine_bricks')
    }
    for (const across of [-4, 4]) api.setBlock(structure, position(across, 8, 2), 'minecraft:sea_lantern')
  }
  box([7, 3, 3], [23, 3, 12], 'dark_prismarine')
  box([9, 4, 4], [21, 4, 10], 'prismarine_bricks')
  box([11, 5, 5], [19, 5, 9], 'prismarine')
  for (let east = 13; east <= 17; east++) {
    block(east, 3, 13, 'prismarine_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=true]')
    block(east, 4, 12, 'prismarine_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=true]')
    box([east, 3, 11], [east, 4, 11], 'prismarine_bricks')
    block(east, 5, 10, 'prismarine_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=true]')
  }
  for (const east of [11, 19]) block(east, 6, 9, 'sea_lantern')
  api.setBlock(structure, [15, 6, 7], 'minecraft:chest', {facing: 'south', type: 'single', waterlogged: 'true'}, api.createBlockNbt({id: 'minecraft:chest', Items: []}))
  for (const east of [8, 22]) {
    box([east - 1, 4, 6], [east + 1, 4, 8], 'prismarine_bricks')
    box([east - 1, 5, 7], [east - 1, 7, 7], 'prismarine')
    box([east + 1, 5, 7], [east + 1, 7, 7], 'prismarine')
    box([east - 1, 8, 7], [east + 1, 10, 7], 'prismarine_bricks')
    box([east - 2, 8, 7], [east - 2, 9, 7], 'prismarine')
    box([east + 2, 8, 7], [east + 2, 9, 7], 'prismarine')
    box([east - 1, 11, 7], [east + 1, 12, 8], 'prismarine')
    block(east - 1, 12, 9, 'dark_prismarine')
    block(east + 1, 12, 9, 'dark_prismarine')
    block(east, 11, 9, 'prismarine_bricks')
  }
  box([12, 6, 2], [18, 14, 2], 'dark_prismarine')
  box([15, 7, 3], [15, 12, 3], 'prismarine')
  box([13, 10, 3], [17, 10, 3], 'prismarine')
  box([13, 11, 3], [13, 13, 3], 'prismarine')
  box([17, 11, 3], [17, 13, 3], 'prismarine')
  for (const east of [6, 23]) {
    box([east, 8, 2], [east + 2, 13, 2], 'blue_terracotta')
    box([east + 1, 9, 3], [east + 1, 12, 3], 'cyan_terracotta')
    box([east, 8, 3], [east + 2, 8, 3], 'yellow_terracotta')
  }
  for (const east of [3, 25]) {
    box([east, 3, 7], [east + 2, 3, 10], 'prismarine_bricks')
    for (const south of [8, 10]) api.setBlock(structure, [east + 1, 4, south], 'minecraft:barrel', {facing: 'up', open: 'false'}, api.createBlockNbt({id: 'minecraft:barrel', Items: []}))
    block(east, 4, 8, 'sandstone')
    block(east, 5, 8, 'sandstone_wall[up=true,waterlogged=true]')
  }
  for (const [east, south] of [[8, 22], [23, 22]]) {
    box([east - 1, 3, south - 1], [east + 1, 3, south + 1], 'prismarine_bricks')
    block(east, 4, south, 'prismarine')
    block(east, 5, south, 'sea_lantern')
  }
  const coral = (east, up, south, type, height, branch) => {
    block(east, up - 1, south, 'prismarine')
    for (let rise = 0; rise < height; rise++) block(east, up + rise, south, `${type}_coral_block`)
    block(east, up + height, south, `${type}_coral[waterlogged=true]`)
    block(east + branch, up + height - 2, south, 'prismarine')
    block(east + branch, up + height - 1, south, `${type}_coral_block`)
    block(east + branch, up + height, south, `${type}_coral_fan[waterlogged=true]`)
  }
  for (const [east, up, south, type, height, branch] of [[12, 6, 5, 'fire', 2, -1], [18, 6, 5, 'brain', 2, 1], [5, 4, 24, 'brain', 2, 1], [26, 4, 24, 'bubble', 2, -1], [5, 4, 14, 'bubble', 2, 1], [25, 4, 14, 'fire', 2, -1], [5, 15, 1, 'brain', 2, 1], [25, 15, 1, 'fire', 2, -1], [1, 14, 10, 'brain', 2, 1], [29, 14, 10, 'bubble', 2, -1], [1, 14, 26, 'fire', 2, 1], [29, 14, 26, 'brain', 2, -1]]) {
    if (up === 4) box([east - 1, 3, south - 1], [east + 1, 3, south + 1], 'prismarine')
    coral(east, up, south, type, height, branch)
  }
  for (const [east, south, height] of [[6, 12, 8], [24, 12, 8], [5, 20, 8], [26, 20, 8], [5, 27, 8], [25, 27, 8]]) {
    for (let up = 3; up < height; up++) block(east, up, south, 'kelp_plant')
    block(east, height, south, 'kelp[age=25]')
  }
  for (const [east, south] of [[7, 16], [23, 16], [10, 24], [21, 24], [7, 27], [23, 27]]) block(east, 3, south, 'sea_pickle[pickles=3,waterlogged=true]')
  for (const entry of structure.getBlocks()) {
    if (!entry.state.getName().toString().endsWith('_coral_block')) continue
    const wet = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]].some(offset => {
      const neighbor = structure.getBlock(entry.pos.map((coordinate, index) => coordinate + offset[index]))
      return neighbor && (neighbor.state.getName().toString() === 'minecraft:water' || neighbor.state.getProperties().waterlogged === 'true')
    })
    if (!wet) throw Error(`Coral missing water neighbor: ${entry.pos}`)
  }
  return {structure, metadata: {name: 's5-09-coral-trident-vault', reference: 'S5-09-treasure.png', size, ports: [{anchor: [0, 3, 18], outward: 'west', width: 5}, {anchor: [30, 3, 18], outward: 'east', width: 5}], chest: [15, 6, 7], chestApproach: [15, 6, 9], guardianStatueCenters: [[8, 8, 7], [22, 8, 7]], adaptation: 'Original single waterlogged empty chest and4empty barrels; static prismarine guardian statues; terracotta banners and trident mosaic are not entities; sandstone/waterlogged wall urn approximations'}}
}
