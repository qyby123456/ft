export default function build(api) {
  const size = [35, 20, 43]
  const structure = api.createStructure(size)
  const block = (east, up, south, name) => api.setBlock(structure, [east, up, south], `minecraft:${name}`)
  const box = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name}`)
  const masonry = (east, up, south) => (east * 19 + up * 11 + south * 13) % 23 < 4 ? 'prismarine' : 'prismarine_bricks'
  box([0, 0, 0], [34, 19, 42], 'water[level=0]')
  box([0, 0, 0], [34, 1, 42], 'dark_prismarine')
  for (let east = 0; east < 35; east++) for (let south = 0; south < 43; south++) {
    const aisle = east >= 14 && east <= 20
    const tile = aisle ? east === 14 || east === 20 ? 'dark_prismarine' : south % 6 === 0 && east === 17 ? 'sea_lantern' : 'prismarine_bricks' : (east * 7 + south * 11) % 29 < 4 ? 'sandstone' : masonry(east, 2, south)
    block(east, 2, south, tile)
  }
  for (let up = 3; up <= 16; up++) {
    for (let east = 0; east < 35; east++) {
      if (up <= 14 + (east >= 9 && east <= 25 ? 1 : 0)) for (const south of [0, 1]) block(east, up, south, masonry(east, up, south))
      if (up <= 4 && (east < 13 || east > 21)) for (const south of [41, 42]) block(east, up, south, masonry(east, up, south))
    }
    for (let south = 0; south < 43; south++) if (up <= (south < 5 ? 15 : 13)) for (const east of [0, 1, 33, 34]) block(east, up, south, masonry(east, up, south))
  }
  for (const [east, south] of [[1, 1], [30, 1], [0, 39], [31, 39]]) {
    box([east, 3, south], [east + 3, 15, south + 2], 'prismarine_bricks')
    box([east, 15, south], [east + 3, 15, south + 2], 'prismarine')
    block(east + 1, 9, south + 2, 'sea_lantern')
  }
  const arch = (side, center, width, height) => {
    const position = (offset, up, depth) => side === 'north' ? [center + offset, up, depth] : side === 'south' ? [center + offset, up, 42 - depth] : side === 'west' ? [depth, up, center + offset] : [34 - depth, up, center + offset]
    const radius = Math.floor(width / 2)
    for (let depth = 0; depth <= 2; depth++) for (let offset = -radius - 1; offset <= radius + 1; offset++) {
      const ceiling = 3 + height - Math.max(0, Math.abs(offset) - 1)
      for (let up = 3; up <= height + 5; up++) {
        const frame = Math.abs(offset) > radius || up >= ceiling
        if (side !== 'south' || up <= 7) api.setBlock(structure, position(offset, up, depth), frame ? 'minecraft:prismarine_bricks' : 'minecraft:water[level=0]')
      }
      api.setBlock(structure, position(offset, 2, depth), 'minecraft:prismarine_bricks')
    }
  }
  arch('north', 17, 7, 10)
  arch('west', 20, 5, 8)
  arch('east', 20, 5, 8)
  for (const [east, south] of [[10, 10], [24, 10], [10, 21], [24, 21], [10, 32], [24, 32]]) {
    box([east - 1, 3, south - 1], [east + 1, 3, south + 1], 'prismarine_bricks')
    box([east, 4, south], [east, 11, south], 'prismarine_bricks')
    block(east, 8, south, 'sea_lantern')
    box([east - 1, 12, south - 1], [east + 1, 12, south + 1], 'prismarine')
    box([east, 11, south], [east, 11, south], 'dark_prismarine')
  }
  for (const south of [7, 30]) for (const east of [2, 29]) {
    box([east, 3, south], [east + 3, 4, south + 5], 'prismarine')
    box([east, 5, south], [east + 3, 5, south + 5], 'sandstone')
    block(east + 1, 6, south + 1, 'sea_lantern')
  }
  for (const side of ['west', 'east']) for (const south of [9, 30]) {
    const east = side === 'west' ? 2 : 32
    for (let up = 7; up <= 11; up++) for (let offset = -1; offset <= 1; offset++) {
      block(east, up, south + offset, offset === 0 && (up === 9 || up === 10) ? 'cyan_terracotta' : up === 7 ? 'yellow_terracotta' : 'blue_terracotta')
    }
  }
  for (const east of [7, 27]) for (let up = 8; up <= 12; up++) for (let offset = -1; offset <= 1; offset++) block(east + offset, up, 2, offset === 0 && up >= 9 && up <= 11 ? 'cyan_terracotta' : up === 8 ? 'yellow_terracotta' : 'blue_terracotta')
  box([1, 13, 14], [5, 13, 16], 'prismarine_bricks')
  box([29, 13, 14], [33, 13, 16], 'prismarine_bricks')
  box([2, 3, 38], [5, 4, 40], 'prismarine')
  box([29, 3, 38], [32, 4, 40], 'prismarine')
  const coral = (east, up, south, type, height, direction) => {
    for (let rise = 0; rise < height; rise++) block(east, up + rise, south, `${type}_coral_block`)
    block(east, up + height, south, `${type}_coral[waterlogged=true]`)
    block(east + direction, up + height - 1, south, `${type}_coral_block`)
    block(east + direction, up + height, south, `${type}_coral_fan[waterlogged=true]`)
  }
  for (const [east, up, south, type, height, direction] of [[4, 6, 11, 'fire', 2, 1], [30, 6, 11, 'brain', 2, -1], [4, 6, 32, 'brain', 2, 1], [30, 6, 32, 'fire', 2, -1], [4, 14, 15, 'bubble', 1, 1], [30, 14, 15, 'fire', 1, -1], [7, 16, 1, 'brain', 1, 1], [27, 16, 1, 'fire', 1, -1], [4, 5, 39, 'brain', 2, 1], [30, 5, 39, 'fire', 2, -1]]) {
    block(east, up - 1, south, 'prismarine')
    block(east + direction, up + height - 2, south, 'prismarine')
    coral(east, up, south, type, height, direction)
  }
  for (const [east, south] of [[5, 5], [29, 5], [5, 14], [29, 14], [5, 27], [29, 27], [5, 36], [29, 36]]) {
    block(east, 3, south, 'sea_pickle[pickles=3,waterlogged=true]')
    block(east + (east < 17 ? 1 : -1), 3, south, 'seagrass')
  }
  for (const [east, south, height] of [[7, 15, 6], [27, 15, 7], [7, 28, 7], [27, 28, 6], [3, 5, 8], [31, 5, 9], [3, 36, 6], [31, 36, 7]]) {
    for (let up = 3; up < height; up++) block(east, up, south, 'kelp_plant')
    block(east, height, south, 'kelp[age=25]')
  }
  for (const entry of structure.getBlocks()) {
    if (!entry.state.getName().toString().endsWith('_coral_block')) continue
    const wet = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]].some(offset => {
      const neighbor = structure.getBlock(entry.pos.map((coordinate, index) => coordinate + offset[index]))
      return neighbor && (neighbor.state.getName().toString() === 'minecraft:water' || neighbor.state.getProperties().waterlogged === 'true')
    })
    if (!wet) throw new Error(`Coral has no water neighbor: ${entry.pos}`)
  }
  return {structure, metadata: {name: 's5-04-coral-processional-hall', reference: 'S5-04-great-hall.png', size, ports: [{anchor: [17, 3, 0], outward: 'north', width: 7}, {anchor: [17, 3, 42], outward: 'south', width: 7}, {anchor: [0, 3, 20], outward: 'west', width: 5}, {anchor: [34, 3, 20], outward: 'east', width: 5}], pillarCenters: [[10, 10], [24, 10], [10, 21], [24, 21], [10, 32], [24, 32]], bannerSubstitute: 'Durable terracotta wall mosaics, not functional banner block entities'}}
}
