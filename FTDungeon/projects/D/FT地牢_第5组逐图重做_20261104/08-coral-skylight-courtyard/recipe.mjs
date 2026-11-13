export default function build(api) {
  const size = [35, 22, 35]
  const structure = api.createStructure(size)
  const block = (east, up, south, name) => api.setBlock(structure, [east, up, south], `minecraft:${name}`)
  const box = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name}`)
  box([0, 0, 0], [34, 21, 34], 'water[level=0]')
  box([0, 0, 0], [34, 1, 34], 'dark_prismarine')
  for (let east = 0; east < 35; east++) for (let south = 0; south < 35; south++) {
    const ring = (east === 10 || east === 24) && south >= 9 && south <= 25 || (south === 10 || south === 24) && east >= 9 && east <= 25
    block(east, 2, south, ring ? 'dark_prismarine' : (east < 8 || east > 26 || south < 8 || south > 26) && (east * 7 + south * 11) % 19 < 5 ? 'sandstone' : 'prismarine_bricks')
  }
  box([0, 3, 0], [34, 12, 1], 'prismarine_bricks')
  box([0, 3, 0], [1, 12, 34], 'prismarine_bricks')
  box([33, 3, 0], [34, 12, 34], 'prismarine_bricks')
  box([0, 3, 33], [34, 4, 34], 'prismarine_bricks')
  for (const east of [1, 31]) for (const south of [1, 12, 24, 31]) {
    box([east, 3, south], [east + 2, 19, south + 2], 'prismarine_bricks')
    block(east + 1, 8, south + 2, 'sea_lantern')
    block(east + 1, 16, south + 2, 'sea_lantern')
  }
  box([1, 12, 1], [5, 12, 33], 'prismarine')
  box([29, 12, 1], [33, 12, 33], 'prismarine')
  box([1, 12, 1], [33, 12, 5], 'prismarine')
  for (let south = 6; south <= 30; south++) for (let up = 13; up <= 14; up++) {
    block(5, up, south, 'iron_bars[waterlogged=true]')
    block(29, up, south, 'iron_bars[waterlogged=true]')
  }
  for (let east = 5; east <= 29; east++) for (let up = 13; up <= 14; up++) block(east, up, 5, 'iron_bars[waterlogged=true]')
  box([0, 19, 0], [5, 19, 34], 'prismarine_bricks')
  box([29, 19, 0], [34, 19, 34], 'prismarine_bricks')
  box([0, 19, 0], [34, 19, 5], 'prismarine_bricks')
  box([0, 19, 29], [34, 19, 34], 'prismarine_bricks')
  for (const side of ['north', 'west', 'east']) {
    const position = (across, up, depth) => side === 'north' ? [17 + across, up, depth] : side === 'west' ? [depth, up, 18 + across] : [34 - depth, up, 18 + across]
    for (let depth = 0; depth <= 2; depth++) for (let across = -4; across <= 4; across++) {
      for (let up = 3; up <= 12; up++) {
        const ceiling = 11 - Math.max(0, Math.abs(across) - 1)
        api.setBlock(structure, position(across, up, depth), Math.abs(across) <= 2 && up < ceiling ? 'minecraft:water[level=0]' : 'minecraft:prismarine_bricks')
      }
      api.setBlock(structure, position(across, 2, depth), 'minecraft:prismarine_bricks')
    }
    api.setBlock(structure, position(-4, 8, 2), 'minecraft:sea_lantern')
    api.setBlock(structure, position(4, 8, 2), 'minecraft:sea_lantern')
  }
  box([13, 3, 13], [21, 3, 21], 'prismarine_bricks')
  box([14, 4, 14], [20, 4, 20], 'prismarine')
  box([15, 5, 15], [19, 5, 19], 'prismarine_bricks')
  box([16, 6, 16], [18, 6, 18], 'prismarine')
  for (const [east, south] of [[13, 13], [21, 13], [13, 21], [21, 21]]) block(east, 4, south, 'sea_lantern')
  const coral = (east, up, south, type, height, branchEast, branchSouth) => {
    block(east, up - 1, south, 'prismarine')
    for (let rise = 0; rise < height; rise++) block(east, up + rise, south, `${type}_coral_block`)
    block(east, up + height, south, `${type}_coral[waterlogged=true]`)
    block(east + branchEast, up + height - 2, south + branchSouth, 'prismarine')
    block(east + branchEast, up + height - 1, south + branchSouth, `${type}_coral_block`)
    block(east + branchEast, up + height, south + branchSouth, `${type}_coral_fan[waterlogged=true]`)
  }
  coral(17, 7, 17, 'brain', 4, 1, 0)
  coral(15, 6, 16, 'fire', 2, 0, 1)
  coral(19, 6, 18, 'bubble', 2, 0, -1)
  coral(15, 5, 20, 'bubble', 2, 1, 0)
  coral(20, 5, 15, 'fire', 2, 0, 1)
  for (const [east, south, type, branchEast, branchSouth] of [[5, 7, 'fire', 1, 0], [29, 7, 'brain', -1, 0], [5, 29, 'brain', 1, 0], [29, 29, 'fire', -1, 0], [10, 3, 'bubble', 1, 0], [24, 3, 'brain', -1, 0]]) {
    box([east - 1, 3, south - 1], [east + 1, 4, south + 1], 'prismarine')
    coral(east, 5, south, type, 2, branchEast, branchSouth)
  }
  for (const [east, south, type, branchEast, branchSouth] of [[3, 8, 'brain', 0, 1], [31, 8, 'fire', 0, 1], [3, 28, 'bubble', 0, -1], [31, 28, 'brain', 0, -1], [10, 3, 'fire', 1, 0], [24, 3, 'brain', -1, 0]]) coral(east, 13, south, type, 2, branchEast, branchSouth)
  for (const [east, south, height, bottom] of [[7, 6, 9, 3], [27, 6, 9, 3], [6, 11, 9, 3], [28, 11, 8, 3], [7, 28, 8, 3], [27, 28, 9, 3], [3, 10, 18, 13], [31, 10, 18, 13], [8, 3, 18, 13], [26, 3, 18, 13]]) {
    for (let up = bottom; up < height; up++) block(east, up, south, 'kelp_plant')
    block(east, height, south, 'kelp[age=25]')
  }
  for (const [east, south] of [[8, 8], [26, 8], [8, 26], [26, 26], [10, 7], [24, 7], [10, 28], [24, 28]]) block(east, 3, south, 'sea_pickle[pickles=3,waterlogged=true]')
  for (const [east, south] of [[8, 10], [26, 10], [10, 26], [24, 26]]) block(east, 3, south, 'seagrass')
  for (const entry of structure.getBlocks()) {
    if (!entry.state.getName().toString().endsWith('_coral_block')) continue
    const wet = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]].some(offset => {
      const neighbor = structure.getBlock(entry.pos.map((coordinate, index) => coordinate + offset[index]))
      return neighbor && (neighbor.state.getName().toString() === 'minecraft:water' || neighbor.state.getProperties().waterlogged === 'true')
    })
    if (!wet) throw Error(`Coral missing water neighbor: ${entry.pos}`)
  }
  return {structure, metadata: {name: 's5-08-coral-skylight-courtyard', reference: 'S5-08-courtyard.png', size, ports: [{anchor: [17, 3, 0], outward: 'north', width: 5}, {anchor: [0, 3, 18], outward: 'west', width: 5}, {anchor: [34, 3, 18], outward: 'east', width: 5}], centralFeature: 'Four-tier coral fountain mound with four base sea lanterns, not flowing-water jet', upperGallery: 'U-shaped supported floorY12/feetY13 with waterlogged iron-bar railingY13..14', skylight: 'Pure source-water apertureX6..28,Z6..28 at roofY19', upperAccess: 'Swim upward through the flooded shaft and over railing; no air or staircase'}}
}
